-- 申报表
CREATE TABLE biz_declaration (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '申报ID',
    taxpayer_id   BIGINT       NOT NULL COMMENT '纳税人ID',
    tax_type      VARCHAR(20)  NOT NULL COMMENT '税种(增值税/所得税等)',
    amount        DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '申报金额',
    declare_month VARCHAR(7)   NOT NULL COMMENT '申报所属期(如2023-10)',
    status        TINYINT      NOT NULL DEFAULT 0 COMMENT '状态(0待审核/1已通过/2已驳回)',
    deleted       TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除(0未删除/1已删除)',
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_taxpayer_id (taxpayer_id),
    KEY idx_declare_month (declare_month)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '申报表';
