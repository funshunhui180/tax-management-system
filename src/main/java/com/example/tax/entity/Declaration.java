package com.example.tax.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("biz_declaration")
public class Declaration {

    /** 申报ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 纳税人ID */
    private Long taxpayerId;

    /** 企业名称(联表查询展示用，不持久化) */
    @TableField(exist = false)
    private String companyName;

    /** 税种(增值税/所得税等) */
    private String taxType;

    /** 申报金额 */
    private BigDecimal amount;

    /** 申报所属期(如2023-10) */
    private String declareMonth;

    /** 状态(0待审核/1已通过/2已驳回) */
    private Integer status;

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
