-- 企业微信客户群与船期通知
ALTER TABLE erp_partner
  ADD COLUMN wecom_chat_id VARCHAR(100) DEFAULT NULL COMMENT '企业微信客户群ID' AFTER contact_email,
  ADD COLUMN wecom_chat_name VARCHAR(200) DEFAULT NULL COMMENT '企业微信客户群名称' AFTER wecom_chat_id,
  ADD COLUMN wecom_chat_owner VARCHAR(100) DEFAULT NULL COMMENT '企业微信客户群群主UserID' AFTER wecom_chat_name;

CREATE TABLE IF NOT EXISTS erp_wecom_group (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  chat_id VARCHAR(100) NOT NULL COMMENT '企业微信客户群ID',
  group_name VARCHAR(200) DEFAULT NULL COMMENT '客户群名称',
  owner VARCHAR(100) DEFAULT NULL COMMENT '群主UserID',
  group_status TINYINT DEFAULT 0 COMMENT '客户群状态：0正常 1跟进人离职 2离职继承中 3离职继承完成',
  member_count INT NOT NULL DEFAULT 0 COMMENT '群成员数量',
  group_create_time DATETIME DEFAULT NULL COMMENT '企业微信客户群创建时间',
  sync_time DATETIME DEFAULT NULL COMMENT '最近同步时间',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_erp_wecom_group_chat (chat_id),
  KEY idx_erp_wecom_group_name (group_name),
  KEY idx_erp_wecom_group_owner (owner)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企业微信客户群档案';

CREATE TABLE IF NOT EXISTS erp_ship_notice (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  presale_order_id BIGINT NOT NULL COMMENT '预销售单ID',
  partner_id BIGINT NOT NULL COMMENT '二批商往来单位ID',
  partner_name VARCHAR(200) DEFAULT NULL COMMENT '二批商名称',
  chat_id VARCHAR(100) DEFAULT NULL COMMENT '企业微信客户群ID',
  chat_name VARCHAR(200) DEFAULT NULL COMMENT '企业微信客户群名称',
  sender VARCHAR(100) DEFAULT NULL COMMENT '企业微信群发发送人UserID',
  contract_no VARCHAR(100) DEFAULT NULL COMMENT '合同号',
  container_no VARCHAR(100) DEFAULT NULL COMMENT '集装箱号',
  expected_arrival_date DATETIME DEFAULT NULL COMMENT '预计到港日期',
  content TEXT COMMENT '通知内容',
  wecom_msg_id VARCHAR(100) DEFAULT NULL COMMENT '企业微信群发任务ID',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '通知状态：1已创建待成员确认 2已发送 9失败',
  error_message VARCHAR(500) DEFAULT NULL COMMENT '企业微信接口返回信息或错误原因',
  create_user_id BIGINT DEFAULT NULL COMMENT '创建人ID',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_erp_ship_notice_presale (presale_order_id),
  KEY idx_erp_ship_notice_partner (partner_id),
  KEY idx_erp_ship_notice_chat (chat_id),
  KEY idx_erp_ship_notice_msg (wecom_msg_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='船期企业微信通知任务';
