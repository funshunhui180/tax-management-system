-- 纳税人管理表
CREATE TABLE sys_taxpayer (
    id                  BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    taxpayer_identify_no VARCHAR(30) NOT NULL COMMENT '纳税人识别号',
    company_name        VARCHAR(100) NOT NULL COMMENT '企业名称',
    legal_representative VARCHAR(50)  DEFAULT NULL COMMENT '法定代表人',
    contact_phone       VARCHAR(20)  DEFAULT NULL COMMENT '联系电话',
    register_address    VARCHAR(255) DEFAULT NULL COMMENT '注册地址',
    industry_type       VARCHAR(50)  DEFAULT NULL COMMENT '行业类型',
    registration_status VARCHAR(10)  DEFAULT '正常' COMMENT '登记状态(正常/注销)',
    deleted             TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除字段(0未删除/1已删除)',
    create_time         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_taxpayer_identify_no (taxpayer_identify_no)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '纳税人管理表';
