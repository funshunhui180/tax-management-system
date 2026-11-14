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
        // 使用传统 switch 语句：Java 11 不支持 switch 表达式（case ... -> ），该语法需 Java 14+
        switch (role) {
            case ADMIN:
                return "管理员";
            case AUDITOR:
                return "审核员";
            case TAXPAYER:
                return "纳税人";
            default:
                return "未知角色";
        }
    }
}
