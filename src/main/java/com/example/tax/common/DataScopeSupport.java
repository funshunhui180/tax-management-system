package com.example.tax.common;

/**
 * 页面展示用的数据范围文案，便于测试时直接确认后端过滤口径。
 */
public final class DataScopeSupport {

    private DataScopeSupport() {
    }

    public static String label(int role, Long taxpayerId, String taxpayerName) {
        if (role == RoleConstants.TAXPAYER) {
            if (taxpayerId == null) {
                return "数据范围：未绑定纳税人（无数据）";
            }
            String name = taxpayerName == null || taxpayerName.isBlank()
                    ? "纳税人ID " + taxpayerId
                    : taxpayerName;
            return "数据范围：仅本企业 · " + name;
        }
        if (role == RoleConstants.ADMIN) {
            return "数据范围：全局数据（管理员）";
        }
        if (role == RoleConstants.AUDITOR) {
            return "数据范围：全局数据（审核员）";
        }
        return "数据范围：未知角色（无数据）";
    }
}
