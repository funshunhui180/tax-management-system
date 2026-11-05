package com.example.tax.common;

import org.springframework.ui.Model;

/**
 * 统一向前端下发当前角色的操作权限，模板中不再直接散落 session.role 判断。
 */
public final class RbacView {

    private RbacView() {
    }

    public static void addRoleFlags(Model model, int role) {
        model.addAttribute("role", role);
        model.addAttribute("roleName", RoleConstants.label(role));
        model.addAttribute("isTaxpayer", role == RoleConstants.TAXPAYER);

        // 填写申报：管理员 + 纳税人
        model.addAttribute("canCreateDeclaration",
                role == RoleConstants.ADMIN || role == RoleConstants.TAXPAYER);
        // 纳税人档案管理：仅管理员
        model.addAttribute("canManageTaxpayer", role == RoleConstants.ADMIN);
        // 申报编辑/删除：仅管理员
        model.addAttribute("canEditDeclaration", role == RoleConstants.ADMIN);
        model.addAttribute("canDeleteDeclaration", role == RoleConstants.ADMIN);
        // 审核通过/驳回：仅审核员
        model.addAttribute("canAuditDeclaration", role == RoleConstants.AUDITOR);
    }
}
