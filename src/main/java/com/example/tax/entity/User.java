package com.example.tax.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class User {

    /** 用户ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 账号 */
    private String username;

    /** 密码 */
    private String password;

    /** 角色(0管理员/1审核员/2纳税人) */
    private Integer role;

    /** 纳税人ID(纳税人角色关联sys_taxpayer,其他为NULL) */
    private Long taxpayerId;

    /** 逻辑删除(0未删除/1已删除) */
    @TableLogic
    private Integer deleted;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
