package com.example.tax.common;

/**
 * 系统角色常量：0 管理员 / 1 审核员 / 2 纳税人。
 * <p>统一使用常量，避免各层散落 0/1/2 魔法值导致权限判断不一致。</p>
 */
public final class RoleConstants {

    /** 管理员：拥有全部权限 */
    public static final int ADMIN = 0;

    /** 审核员：查看并审核申报单，不能维护纳税人 */
    public static final int AUDITOR = 1;

    /** 纳税人：只能查看/提交本企业的申报数据 */
    public static final int TAXPAYER = 2;

    private RoleConstants() {
    }

    /**
     * 角色中文名称（用于页面展示）。
     */
    public static String label(Integer role) {
        if (role == null) {
            return "未知角色";
        }
        return switch (role) {
            case ADMIN -> "管理员";
            case AUDITOR -> "审核员";
            case TAXPAYER -> "纳税人";
            default -> "未知角色";
        };
    }
}
