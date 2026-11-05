package com.example.tax.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.tax.common.DataScopeSupport;
import com.example.tax.common.RbacView;
import com.example.tax.common.RoleConstants;
import com.example.tax.entity.Declaration;
import com.example.tax.entity.Taxpayer;
import com.example.tax.entity.User;
import com.example.tax.interceptor.LoginInterceptor;
import com.example.tax.service.DeclarationService;
import com.example.tax.service.TaxpayerService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class IndexController {

    private static final int STATUS_PENDING = 0;
    private static final int STATUS_APPROVED = 1;

    private final TaxpayerService taxpayerService;
    private final DeclarationService declarationService;

    public IndexController(TaxpayerService taxpayerService,
                           DeclarationService declarationService) {
        this.taxpayerService = taxpayerService;
        this.declarationService = declarationService;
    }

    /**
     * 系统首页（工作台）：按 Session 中的角色动态计算统计数据。
     * <ul>
     *   <li>管理员(0)：纳税人总数、申报单总数/待审核/已通过均统计全局；</li>
     *   <li>审核员(1)：不统计纳税人总数，只统计全局的待审核/已通过数据；</li>
     *   <li>纳税人(2)：纳税人总数固定为本人这一户（1），申报相关统计
     *       <b>必须</b>附加 {@code taxpayer_id = 当前用户关联的纳税人ID} 条件。</li>
     * </ul>
     */
    @GetMapping
    public String index(HttpSession session, Model model) {
        User loginUser = (User) session.getAttribute(LoginInterceptor.LOGIN_USER);
        if (loginUser == null) {
            return "redirect:/login";
        }

        int role = loginUser.getRole() == null ? -1 : loginUser.getRole();

        // 只有纳税人角色才把数据范围收窄到本人企业；其余角色为 null（全局）
        Long taxpayerId = role == RoleConstants.TAXPAYER ? loginUser.getTaxpayerId() : null;

        long taxpayerCount;
        if (role == RoleConstants.ADMIN) {
            taxpayerCount = taxpayerService.count();
        } else if (role == RoleConstants.TAXPAYER) {
            // 纳税人只能看到自己这一户：有绑定关系时为 1；未绑定则 0，绝不回落到全局数据
            taxpayerCount = taxpayerId == null ? 0L : 1L;
        } else {
            // 审核员不需要纳税人总数，统一置 0 且页面不展示该卡片
            taxpayerCount = 0L;
        }

        long declarationCount = countDeclarations(role, taxpayerId, null);
        long pendingCount = countDeclarations(role, taxpayerId, STATUS_PENDING);
        long approvedCount = countDeclarations(role, taxpayerId, STATUS_APPROVED);

        model.addAttribute("taxpayerCount", taxpayerCount);
        model.addAttribute("declarationCount", declarationCount);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("approvedCount", approvedCount);
        // 统一下发角色权限布尔值，模板只判断 canXxx，避免 session 表达式出错
        RbacView.addRoleFlags(model, role);
        // 管理员展示全局纳税人总数；纳税人展示“1”代表本企业；审核员不展示
        model.addAttribute("showTaxpayerCount",
                role == RoleConstants.ADMIN || role == RoleConstants.TAXPAYER);
        model.addAttribute("dataScopeLabel",
                DataScopeSupport.label(role, taxpayerId, ownTaxpayerName(role, taxpayerId)));
        return "index";
    }

    /**
     * 查询纳税人本人关联的企业名称，用于首页展示“数据范围”。
     */
    private String ownTaxpayerName(int role, Long taxpayerId) {
        if (role != RoleConstants.TAXPAYER || taxpayerId == null) {
            return null;
        }
        Taxpayer taxpayer = taxpayerService.getById(taxpayerId);
        return taxpayer == null ? null : taxpayer.getCompanyName();
    }

    /**
     * 按角色统计申报单数量。
     * <p>纳税人：只统计 {@code taxpayer_id = 本人纳税人ID} 的记录；
     * 管理员/审核员：统计全局。未知角色一律返回 0，遵循“默认拒绝”。</p>
     */
    private long countDeclarations(int role, Long taxpayerId, Integer status) {
        if (role == RoleConstants.TAXPAYER) {
            if (taxpayerId == null) {
                return 0L;
            }
        } else if (role != RoleConstants.ADMIN && role != RoleConstants.AUDITOR) {
            return 0L;
        }

        LambdaQueryWrapper<Declaration> wrapper = new LambdaQueryWrapper<>();
        if (role == RoleConstants.TAXPAYER) {
            wrapper.eq(Declaration::getTaxpayerId, taxpayerId);
        }
        if (status != null) {
            wrapper.eq(Declaration::getStatus, status);
        }
        return declarationService.count(wrapper);
    }
}
