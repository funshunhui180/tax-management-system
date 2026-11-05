-- 用户表
CREATE TABLE sys_user (
    id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    username    VARCHAR(50) NOT NULL COMMENT '账号',
    password    VARCHAR(100) NOT NULL COMMENT '密码',
    role        TINYINT     NOT NULL DEFAULT 2 COMMENT '角色(0管理员/1审核员/2纳税人)',
    taxpayer_id BIGINT      DEFAULT NULL COMMENT '纳税人ID(纳税人角色时关联sys_taxpayer,其他角色为NULL)',
    deleted     TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除(0未删除/1已删除)',
    create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    KEY idx_taxpayer_id (taxpayer_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户表';

-- 初始化三个账号，密码均为 123456
INSERT INTO sys_user (username, password, role, taxpayer_id) VALUES
('admin',   '123456', 0, NULL),
('auditor', '123456', 1, NULL),
('user',    '123456', 2, 1);
