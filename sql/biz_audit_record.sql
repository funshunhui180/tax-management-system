-- 审核记录表
CREATE TABLE biz_audit_record (
    id             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    declaration_id BIGINT       NOT NULL COMMENT '申报ID',
    audit_result   TINYINT      NOT NULL COMMENT '审核结果(1通过/2驳回)',
    auditor        VARCHAR(50)  NOT NULL COMMENT '审核人',
    audit_remark   VARCHAR(255) DEFAULT NULL COMMENT '审核意见',
    audit_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '审核时间',
    deleted        TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除(0未删除/1已删除)',
    create_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_declaration_id (declaration_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '审核记录表';
