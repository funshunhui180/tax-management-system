package com.example.tax.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.tax.annotation.RequireRoles;
import com.example.tax.common.RoleConstants;
import com.example.tax.entity.Taxpayer;
import com.example.tax.service.TaxpayerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 纳税人档案管理。
 * <p>仅管理员(0)可访问：审核员(1)和纳税人(2)的越权请求会先被
 * {@code RoleInterceptor} 的 URL 兜底规则拦截，再由本注解做第二道校验。</p>
 */
@Controller
@RequireRoles({RoleConstants.ADMIN})
@RequestMapping("/taxpayer")
public class TaxpayerController {

    private final TaxpayerService taxpayerService;

    public TaxpayerController(TaxpayerService taxpayerService) {
        this.taxpayerService = taxpayerService;
    }

    /**
     * 纳税人列表（分页 + 条件查询）
     */
    @GetMapping("/list")
    public String list(@RequestParam(defaultValue = "1") long pageNum,
                       @RequestParam(defaultValue = "10") long pageSize,
                       @RequestParam(required = false) String companyName,
                       @RequestParam(required = false) String registrationStatus,
                       Model model) {
        LambdaQueryWrapper<Taxpayer> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(companyName)) {
            wrapper.like(Taxpayer::getCompanyName, companyName.trim());
        }
        if (StringUtils.hasText(registrationStatus)) {
            wrapper.eq(Taxpayer::getStatus, parseRegistrationStatus(registrationStatus.trim()));
        }
        wrapper.orderByDesc(Taxpayer::getCreateTime);

        IPage<Taxpayer> page = taxpayerService.page(new Page<>(pageNum, pageSize), wrapper);

        model.addAttribute("page", page);
        model.addAttribute("companyName", companyName);
        model.addAttribute("registrationStatus", registrationStatus);
        return "taxpayer/list";
    }

    /**
     * 跳转新增页面
     */
    @GetMapping("/add")
    public String add(Model model) {
        model.addAttribute("taxpayer", new Taxpayer());
        return "taxpayer/edit";
    }

    /**
     * 跳转编辑页面
     */
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("taxpayer", taxpayerService.getById(id));
        return "taxpayer/edit";
    }

    /**
     * 新增 / 更新
     */
    @PostMapping("/save")
    public String save(@ModelAttribute("taxpayer") Taxpayer taxpayer,
                       RedirectAttributes redirectAttributes) {
        // 前端传中文字符串('正常')，转为数据库 tinyint(1=正常/0=停用)
        taxpayer.setStatus(parseRegistrationStatus(taxpayer.getRegistrationStatus()));
        if (taxpayer.getId() == null) {
            taxpayerService.save(taxpayer);
            redirectAttributes.addFlashAttribute("msg", "新增成功");
        } else {
            taxpayerService.updateById(taxpayer);
            redirectAttributes.addFlashAttribute("msg", "更新成功");
        }
        return "redirect:/taxpayer/list";
    }

    /**
     * 将前端登记状态中文转为数据库数值：'正常'->1，其余('停用'/'注销')->0
     */
    private int parseRegistrationStatus(String status) {
        return "正常".equals(status) ? 1 : 0;
    }

    /**
     * 逻辑删除
     */
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        taxpayerService.removeById(id);
        redirectAttributes.addFlashAttribute("msg", "删除成功");
        return "redirect:/taxpayer/list";
    }
}
