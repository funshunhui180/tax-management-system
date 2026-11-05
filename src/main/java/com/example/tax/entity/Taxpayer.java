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
@TableName("sys_taxpayer")
public class Taxpayer {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 纳税人识别号 -> taxpayer_code */
    @TableField("taxpayer_code")
    private String taxpayerIdentifyNo;

    /** 企业名称 -> taxpayer_name */
    @TableField("taxpayer_name")
    private String companyName;

    /** 法定代表人 -> legal_person */
    @TableField("legal_person")
    private String legalRepresentative;

    /** 联系电话 -> phone */
    @TableField("phone")
    private String contactPhone;

    /** 注册地址(数据库中无此列，仅前端展示用，不持久化) */
    @TableField(exist = false)
    private String registerAddress;

    /** 行业类型(数据库中无此列，仅前端展示用，不持久化) */
    @TableField(exist = false)
    private String industryType;

    /** 登记状态前端字符串(正常/停用)，仅表单绑定用，不持久化 */
    @TableField(exist = false)
    private String registrationStatus;

    /** 登记状态(正常=1, 停用=0) -> status */
    @TableField("status")
    private Integer status;

    /** 逻辑删除字段(0未删除/1已删除) */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
