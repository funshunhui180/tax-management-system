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
@TableName("biz_audit_record")
public class AuditRecord {

    /** 记录ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 申报ID */
    private Long declarationId;

    /** 审核结果(1通过/2驳回) */
    private Integer auditResult;

    /** 审核人 */
    private String auditor;

    /** 审核意见 */
    private String auditRemark;

    /** 审核时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime auditTime;

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
