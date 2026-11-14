package com.example.tax.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.tax.annotation.RequireRoles;
import com.example.tax.common.DataScopeSupport;
import com.example.tax.common.RbacView;
import com.example.tax.common.RoleConstants;
import com.example.tax.entity.AuditRecord;
import com.example.tax.entity.Declaration;
import com.example.tax.entity.Taxpayer;
import com.example.tax.entity.User;
import com.example.tax.exception.ForbiddenException;
import com.example.tax.interceptor.LoginInterceptor;
import com.example.tax.service.AuditRecordService;
import com.example.tax.service.DeclarationService;
import com.example.tax.service.TaxpayerService;
import javax.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/declaration")
public class DeclarationController {

    /** 状态常量：待审核 */
    private static final int STATUS_PENDING = 0;
    /** 状态常量：已通过 */
    private static final int STATUS_APPROVED = 1;
    /** 状态常量：已驳回 */
    private static final int STATUS_REJECTED = 2;

    /** 审核人(未接入登录，先用固定值) */
    private static final String AUDITOR = "admin";

    private final DeclarationService declarationService;
    private final TaxpayerService taxpayerService;
    private final AuditRecordService auditRecordService;

    public DeclarationController(DeclarationService declarationService,
                                 TaxpayerService taxpayerService,
                                 AuditRecordService auditRecordService) {
        this.declarationService = declarationService;
        this.taxpayerService = taxpayerService;
        this.auditRecordService = auditRecordService;
    }

    /**
     * 申报列表（分页 + 条件查询），并关联查出企业名称。
     * <p>数据权限：纳税人只看本人企业的申报；管理员/审核员看全局。
     * 审核员可按状态筛选待审核与历史审核结果。</p>
     */
    @GetMapping("/list")
    public String list(@RequestParam(defaultValue = "1") long pageNum,
                       @RequestParam(defaultValue = "10") long pageSize,
                       @RequestParam(required = false) String taxType,
                       @RequestParam(required = false) Integer status,
                       HttpSession session,
                       Model model) {
        User loginUser = currentUser(session);
        int role = roleOf(loginUser);
        Long ownTaxpayerId = role == RoleConstants.TAXPAYER ? loginUser.getTaxpayerId() : null;

        // 数据权限隔离（后端强制生效，无法通过 URL 参数绕过）
        // role==2：只查本企业；role==0/1：不加 taxpayer_id 条件，查全局。
        LambdaQueryWrapper<Declaration> wrapper = newScopedWrapper(role, ownTaxpayerId);
        if (StringUtils.hasText(taxType)) {
            wrapper.like(Declaration::getTaxType, taxType.trim());
        }
        if (status != null) {
            wrapper.eq(Declaration::getStatus, status);
        }
        wrapper.orderByDesc(Declaration::getCreateTime);

        IPage<Declaration> page = declarationService.page(new Page<>(pageNum, pageSize), wrapper);
        fillCompanyName(page.getRecords());

        // 列表顶部概览：统计范围必须与列表的数据权限保持一致
        long totalCount = countDeclarations(role, ownTaxpayerId, null);
        long pendingCount = countDeclarations(role, ownTaxpayerId, STATUS_PENDING);
        long approvedCount = countDeclarations(role, ownTaxpayerId, STATUS_APPROVED);
        long rejectedCount = countDeclarations(role, ownTaxpayerId, STATUS_REJECTED);

        model.addAttribute("page", page);
        model.addAttribute("taxType", taxType);
        model.addAttribute("status", status);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("approvedCount", approvedCount);
        model.addAttribute("rejectedCount", rejectedCount);
        // 前端按钮统一使用后端下发的权限标志，不在模板里重复判断 role
        RbacView.addRoleFlags(model, role);
        model.addAttribute("dataScopeLabel",
                DataScopeSupport.label(role, ownTaxpayerId, ownTaxpayerName(role, ownTaxpayerId)));
        return "declaration/list";
    }

    /**
     * 跳转填写申报表页面（新增）。
     * <p>纳税人只能选择本企业，前端下拉框被锁定；管理员可以选择全部纳税人。</p>
     */
    @RequireRoles({RoleConstants.ADMIN, RoleConstants.TAXPAYER})
    @GetMapping("/add")
    public String add(HttpSession session, Model model) {
        User loginUser = currentUser(session);
        int role = roleOf(loginUser);

        Declaration declaration = new Declaration();
        declaration.setStatus(STATUS_PENDING);

        boolean taxpayerLocked = role == RoleConstants.TAXPAYER;
        List<Taxpayer> taxpayers;
        if (taxpayerLocked) {
            Long ownTaxpayerId = loginUser.getTaxpayerId();
            if (ownTaxpayerId == null) {
                throw new ForbiddenException("当前账号未绑定纳税人，无法填写申报表");
            }
            Taxpayer own = taxpayerService.getById(ownTaxpayerId);
            if (own == null) {
                throw new ForbiddenException("当前账号关联的纳税人档案不存在，无法填写申报表");
            }
            declaration.setTaxpayerId(ownTaxpayerId);
            taxpayers = List.of(own);
        } else {
            taxpayers = taxpayerService.list();
        }

        model.addAttribute("declaration", declaration);
        model.addAttribute("taxpayers", taxpayers);
        model.addAttribute("taxpayerLocked", taxpayerLocked);
        return "declaration/add";
    }

    /**
     * 跳转编辑申报表页面（仅管理员）。
     */
    @RequireRoles({RoleConstants.ADMIN})
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("declaration", declarationService.getById(id));
        model.addAttribute("taxpayers", taxpayerService.list());
        model.addAttribute("taxpayerLocked", false);
        return "declaration/add";
    }

    /**
     * 提交申报单：新增一条申报记录，状态置为待审核(0)。
     * <p>安全检查：纳税人提交时强制使用 Session 中关联的 taxpayerId，
     * 忽略前端传入值，防止篡改表单为其他企业申报（横向越权）。</p>
     */
    @RequireRoles({RoleConstants.ADMIN, RoleConstants.TAXPAYER})
    @PostMapping("/save")
    public String submit(@ModelAttribute("declaration") Declaration declaration,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        User loginUser = currentUser(session);
        int role = roleOf(loginUser);

        if (role == RoleConstants.TAXPAYER) {
            Long ownTaxpayerId = loginUser.getTaxpayerId();
            if (ownTaxpayerId == null) {
                throw new ForbiddenException("当前账号未绑定纳税人，无法提交申报");
            }
            // 关键：以 Session 中的关联关系为准，前端传什么都不能改变申报主体
            declaration.setTaxpayerId(ownTaxpayerId);
        }

        if (declaration.getTaxpayerId() == null) {
            redirectAttributes.addFlashAttribute("msg", "请选择纳税人后再提交");
            return "redirect:/declaration/add";
        }

        declaration.setId(null);
        declaration.setStatus(STATUS_PENDING);
        declarationService.save(declaration);
        redirectAttributes.addFlashAttribute("msg", "申报提交成功，等待审核");
        return "redirect:/declaration/list";
    }

    /**
     * 更新申报单（编辑保存，保留原状态，仅管理员）。
     */
    @RequireRoles({RoleConstants.ADMIN})
    @PostMapping("/update")
    public String update(@ModelAttribute("declaration") Declaration declaration,
                         RedirectAttributes redirectAttributes) {
        declarationService.updateById(declaration);
        redirectAttributes.addFlashAttribute("msg", "申报更新成功");
        return "redirect:/declaration/list";
    }

    /**
     * 逻辑删除申报单（仅管理员）。
     */
    @RequireRoles({RoleConstants.ADMIN})
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        declarationService.removeById(id);
        redirectAttributes.addFlashAttribute("msg", "删除成功");
        return "redirect:/declaration/list";
    }

    /**
     * 审核通过：更新申报状态并写入审核记录。
     */
    @RequireRoles({RoleConstants.ADMIN, RoleConstants.AUDITOR})
    @Transactional(rollbackFor = Exception.class)
    @GetMapping("/approve/{id}")
    public String approve(@PathVariable Long id,
                          @RequestParam(required = false) String auditRemark,
                          RedirectAttributes redirectAttributes) {
        updateStatus(id, STATUS_APPROVED);
        recordAudit(id, AuditResult.PASSED, auditRemark);
        redirectAttributes.addFlashAttribute("msg", "审核通过");
        return "redirect:/declaration/list";
    }

    /**
     * 审核驳回：更新申报状态并写入审核记录。
     */
    @RequireRoles({RoleConstants.ADMIN, RoleConstants.AUDITOR})
    @Transactional(rollbackFor = Exception.class)
    @GetMapping("/reject/{id}")
    public String reject(@PathVariable Long id,
                         @RequestParam(required = false) String auditRemark,
                         RedirectAttributes redirectAttributes) {
        updateStatus(id, STATUS_REJECTED);
        recordAudit(id, AuditResult.REJECTED, auditRemark);
        redirectAttributes.addFlashAttribute("msg", "已驳回");
        return "redirect:/declaration/list";
    }

    /**
     * 统一审核接口：result=1 通过，result=2 驳回，remark 为驳回原因。
     */
    @RequireRoles({RoleConstants.ADMIN, RoleConstants.AUDITOR})
    @Transactional(rollbackFor = Exception.class)
    @GetMapping("/audit")
    public String audit(@RequestParam("id") Long id,
                        @RequestParam("result") int result,
                        @RequestParam(value = "remark", required = false) String remark,
                        RedirectAttributes redirectAttributes) {
        if (result == STATUS_REJECTED) {
            updateStatus(id, STATUS_REJECTED);
            recordAudit(id, AuditResult.REJECTED, remark);
            redirectAttributes.addFlashAttribute("msg", "已驳回");
        } else {
            updateStatus(id, STATUS_APPROVED);
            recordAudit(id, AuditResult.PASSED, remark);
            redirectAttributes.addFlashAttribute("msg", "审核通过");
        }
        return "redirect:/declaration/list";
    }

    /**
     * 构造带数据权限的基础查询条件（列表与统计共用，避免口径不一致）。
     * <p>role==2：强制 {@code taxpayer_id = 当前用户纳税人ID}；
     * role==0/1：不附加限制，查询全局；未绑定纳税人的纳税人账号查不到任何数据。</p>
     */
    private LambdaQueryWrapper<Declaration> newScopedWrapper(int role, Long taxpayerId) {
        LambdaQueryWrapper<Declaration> wrapper = new LambdaQueryWrapper<>();
        if (role == RoleConstants.TAXPAYER) {
            if (taxpayerId == null) {
                wrapper.apply("1 = 0");
            } else {
                wrapper.eq(Declaration::getTaxpayerId, taxpayerId);
            }
        } else if (role != RoleConstants.ADMIN && role != RoleConstants.AUDITOR) {
            wrapper.apply("1 = 0");
        }
        return wrapper;
    }

    /**
     * 按角色统计申报数量，统计口径与列表的数据权限保持一致。
     */
    private long countDeclarations(int role, Long taxpayerId, Integer status) {
        LambdaQueryWrapper<Declaration> wrapper = newScopedWrapper(role, taxpayerId);
        if (status != null) {
            wrapper.eq(Declaration::getStatus, status);
        }
        return declarationService.count(wrapper);
    }

    /**
     * 查询纳税人本人关联的企业名称，用于页面展示“数据范围”。
     */
    private String ownTaxpayerName(int role, Long taxpayerId) {
        if (role != RoleConstants.TAXPAYER || taxpayerId == null) {
            return null;
        }
        Taxpayer taxpayer = taxpayerService.getById(taxpayerId);
        return taxpayer == null ? null : taxpayer.getCompanyName();
    }

    private User currentUser(HttpSession session) {
        return (User) session.getAttribute(LoginInterceptor.LOGIN_USER);
    }

    private int roleOf(User user) {
        return user == null || user.getRole() == null ? -1 : user.getRole();
    }

    /**
     * 更新申报状态
     */
    private void updateStatus(Long id, int status) {
        Declaration declaration = new Declaration();
        declaration.setId(id);
        declaration.setStatus(status);
        declarationService.updateById(declaration);
    }

    /**
     * 写入审核记录（审核人、审核时间、审核意见）
     */
    private void recordAudit(Long declarationId, int auditResult, String remark) {
        LocalDateTime now = LocalDateTime.now();
        AuditRecord record = new AuditRecord();
        record.setDeclarationId(declarationId);
        record.setAuditResult(auditResult);
        record.setAuditor(AUDITOR);
        record.setAuditRemark(StringUtils.hasText(remark) ? remark.trim()
                : (auditResult == AuditResult.PASSED ? "同意" : "驳回"));
        // 手动赋值时间戳与删除位，避免依赖自动填充
        record.setAuditTime(now);
        record.setCreateTime(now);
        record.setUpdateTime(now);
        record.setDeleted(0);
        auditRecordService.save(record);
    }

    /** 审核结果常量 */
    private static final class AuditResult {
        private static final int PASSED = 1;
        private static final int REJECTED = 2;

        private AuditResult() {
        }
    }

    /**
     * 根据 taxpayerId 批量关联查出企业名称，回填到每条申报记录
     */
    private void fillCompanyName(List<Declaration> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        Set<Long> taxpayerIds = records.stream()
                .map(Declaration::getTaxpayerId)
                .collect(Collectors.toSet());
        if (taxpayerIds.isEmpty()) {
            return;
        }
        Map<Long, String> nameMap = taxpayerService.listByIds(taxpayerIds)
                .stream()
                .collect(Collectors.toMap(Taxpayer::getId, Taxpayer::getCompanyName, (a, b) -> a));
        records.forEach(record ->
                record.setCompanyName(nameMap.get(record.getTaxpayerId())));
    }
}
