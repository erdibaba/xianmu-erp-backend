SET @schema_name := DATABASE();

SET @sql := IF(
  EXISTS(
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = @schema_name
      AND table_name = 'erp_partner'
      AND column_name = 'annual_interest_rate'
  ),
  'SELECT 1',
  'ALTER TABLE erp_partner ADD COLUMN annual_interest_rate DECIMAL(18,10) NULL COMMENT ''年利率（百分比）'' AFTER cold_storage_free_days'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS erp_funder_payment (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  payment_no VARCHAR(64) NOT NULL COMMENT '资方打款单号',
  funder_id BIGINT NOT NULL COMMENT '资方往来单位ID',
  funder_name VARCHAR(200) NOT NULL COMMENT '资方名称快照',
  recognized_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '凭证OCR识别金额',
  modified_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '人工确认后的打款金额',
  payment_date DATE NOT NULL COMMENT '资方打款日期',
  file_path VARCHAR(500) NOT NULL COMMENT '资方打款凭证归档路径',
  file_name VARCHAR(255) NOT NULL COMMENT '资方打款凭证文件名',
  raw_text LONGTEXT COMMENT '资方打款凭证OCR原始文本',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 0-待确认 1-已确认',
  remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
  create_user_id BIGINT DEFAULT NULL COMMENT '创建人ID',
  create_time DATETIME DEFAULT NULL COMMENT '创建时间',
  update_time DATETIME DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_funder_payment_no (payment_no),
  KEY idx_funder_payment_funder_date (funder_id, payment_date),
  KEY idx_funder_payment_status_date (status, payment_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资方打款主表';

CREATE TABLE IF NOT EXISTS erp_funder_payment_allocation (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  payment_id BIGINT NOT NULL COMMENT '资方打款主表ID',
  presale_order_id BIGINT NOT NULL COMMENT '预销售单ID',
  presale_order_no VARCHAR(64) DEFAULT NULL COMMENT '预销售单号快照',
  seller_contract_no VARCHAR(100) DEFAULT NULL COMMENT '卖方合同号快照',
  allocation_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '该预销售单分摊的贷款本金',
  create_time DATETIME DEFAULT NULL COMMENT '创建时间',
  update_time DATETIME DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_funder_payment_presale (payment_id, presale_order_id),
  KEY idx_funder_allocation_presale (presale_order_id),
  KEY idx_funder_allocation_payment (payment_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资方打款预销售单分摊表';

CREATE TABLE IF NOT EXISTS erp_funder_loan (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  loan_no VARCHAR(64) NOT NULL COMMENT '贷款记录编号',
  payment_id BIGINT NOT NULL COMMENT '资方打款主表ID',
  allocation_id BIGINT NOT NULL COMMENT '资方打款分摊ID',
  presale_order_id BIGINT NOT NULL COMMENT '预销售单ID',
  presale_order_no VARCHAR(64) DEFAULT NULL COMMENT '预销售单号快照',
  seller_contract_no VARCHAR(100) DEFAULT NULL COMMENT '卖方合同号快照',
  funder_id BIGINT NOT NULL COMMENT '资方往来单位ID',
  funder_name VARCHAR(200) NOT NULL COMMENT '资方名称快照',
  loan_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '贷款本金',
  annual_interest_rate DECIMAL(18,10) NOT NULL COMMENT '年利率快照（百分比）',
  loan_date DATE NOT NULL COMMENT '首次资方打款日期',
  repaid_principal DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '累计已还本金',
  remaining_principal DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '剩余待还本金',
  interest_amount DECIMAL(28,10) NOT NULL DEFAULT 0.0000000000 COMMENT '累计已还利息',
  status TINYINT NOT NULL DEFAULT 0 COMMENT '状态 0-待还款 1-还款完成',
  create_time DATETIME DEFAULT NULL COMMENT '创建时间',
  update_time DATETIME DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_funder_loan_no (loan_no),
  UNIQUE KEY uk_funder_loan_allocation (allocation_id),
  KEY idx_funder_loan_presale (presale_order_id),
  KEY idx_funder_loan_funder_status (funder_id, status),
  KEY idx_funder_loan_status_date (status, loan_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资方贷款明细表';

CREATE TABLE IF NOT EXISTS erp_funder_loan_repayment (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  repayment_no VARCHAR(64) NOT NULL COMMENT '还款记录编号',
  loan_id BIGINT NOT NULL COMMENT '贷款记录ID',
  line_no INT NOT NULL DEFAULT 1 COMMENT '该贷款的还款序号',
  repayment_principal DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '本次归还本金',
  annual_interest_rate DECIMAL(18,10) NOT NULL COMMENT '本次计算使用的年利率快照（百分比）',
  loan_days INT NOT NULL DEFAULT 0 COMMENT '从首次资方打款日至本次还款日的自然日差',
  interest_amount DECIMAL(28,10) NOT NULL DEFAULT 0.0000000000 COMMENT '本次应付利息',
  expected_payment_amount DECIMAL(28,10) NOT NULL DEFAULT 0.0000000000 COMMENT '本次预计打款金额（本金加利息）',
  recognized_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '还款凭证OCR识别金额',
  modified_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '人工确认后的实际打款金额',
  repayment_date DATE NOT NULL COMMENT '还款日期',
  amount_matched TINYINT NOT NULL DEFAULT 1 COMMENT '实际打款金额与预计打款金额是否一致 0-否 1-是',
  file_path VARCHAR(500) NOT NULL COMMENT '还款凭证归档路径',
  file_name VARCHAR(255) NOT NULL COMMENT '还款凭证文件名',
  raw_text LONGTEXT COMMENT '还款凭证OCR原始文本',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 0-待确认 1-已确认',
  remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
  create_user_id BIGINT DEFAULT NULL COMMENT '创建人ID',
  create_time DATETIME DEFAULT NULL COMMENT '创建时间',
  update_time DATETIME DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_funder_repayment_no (repayment_no),
  UNIQUE KEY uk_funder_repayment_line (loan_id, line_no),
  KEY idx_funder_repayment_loan_date (loan_id, repayment_date),
  KEY idx_funder_repayment_status_date (status, repayment_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资方贷款还款记录表';

ALTER TABLE erp_partner
  MODIFY COLUMN annual_interest_rate DECIMAL(18,10) NULL COMMENT '年利率（百分比）';
ALTER TABLE erp_funder_payment COMMENT='资方打款主表';
ALTER TABLE erp_funder_payment_allocation COMMENT='资方打款预销售单分摊表';
ALTER TABLE erp_funder_loan COMMENT='资方贷款明细表';
ALTER TABLE erp_funder_loan_repayment COMMENT='资方贷款还款记录表';

ALTER TABLE erp_funder_payment
  MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  MODIFY COLUMN payment_no VARCHAR(64) NOT NULL COMMENT '资方打款单号',
  MODIFY COLUMN funder_id BIGINT NOT NULL COMMENT '资方往来单位ID',
  MODIFY COLUMN funder_name VARCHAR(200) NOT NULL COMMENT '资方名称快照',
  MODIFY COLUMN recognized_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '凭证OCR识别金额',
  MODIFY COLUMN modified_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '人工确认后的打款金额',
  MODIFY COLUMN payment_date DATE NOT NULL COMMENT '资方打款日期',
  MODIFY COLUMN file_path VARCHAR(500) NOT NULL COMMENT '资方打款凭证归档路径',
  MODIFY COLUMN file_name VARCHAR(255) NOT NULL COMMENT '资方打款凭证文件名',
  MODIFY COLUMN raw_text LONGTEXT NULL COMMENT '资方打款凭证OCR原始文本',
  MODIFY COLUMN status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 0-待确认 1-已确认',
  MODIFY COLUMN remark VARCHAR(500) NULL COMMENT '备注',
  MODIFY COLUMN create_user_id BIGINT NULL COMMENT '创建人ID',
  MODIFY COLUMN create_time DATETIME NULL COMMENT '创建时间',
  MODIFY COLUMN update_time DATETIME NULL COMMENT '更新时间';

ALTER TABLE erp_funder_payment_allocation
  MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  MODIFY COLUMN payment_id BIGINT NOT NULL COMMENT '资方打款主表ID',
  MODIFY COLUMN presale_order_id BIGINT NOT NULL COMMENT '预销售单ID',
  MODIFY COLUMN presale_order_no VARCHAR(64) NULL COMMENT '预销售单号快照',
  MODIFY COLUMN seller_contract_no VARCHAR(100) NULL COMMENT '卖方合同号快照',
  MODIFY COLUMN allocation_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '该预销售单分摊的贷款本金',
  MODIFY COLUMN create_time DATETIME NULL COMMENT '创建时间',
  MODIFY COLUMN update_time DATETIME NULL COMMENT '更新时间';

ALTER TABLE erp_funder_loan
  MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  MODIFY COLUMN loan_no VARCHAR(64) NOT NULL COMMENT '贷款记录编号',
  MODIFY COLUMN payment_id BIGINT NOT NULL COMMENT '资方打款主表ID',
  MODIFY COLUMN allocation_id BIGINT NOT NULL COMMENT '资方打款分摊ID',
  MODIFY COLUMN presale_order_id BIGINT NOT NULL COMMENT '预销售单ID',
  MODIFY COLUMN presale_order_no VARCHAR(64) NULL COMMENT '预销售单号快照',
  MODIFY COLUMN seller_contract_no VARCHAR(100) NULL COMMENT '卖方合同号快照',
  MODIFY COLUMN funder_id BIGINT NOT NULL COMMENT '资方往来单位ID',
  MODIFY COLUMN funder_name VARCHAR(200) NOT NULL COMMENT '资方名称快照',
  MODIFY COLUMN loan_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '贷款本金',
  MODIFY COLUMN annual_interest_rate DECIMAL(18,10) NOT NULL COMMENT '年利率快照（百分比）',
  MODIFY COLUMN loan_date DATE NOT NULL COMMENT '首次资方打款日期',
  MODIFY COLUMN repaid_principal DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '累计已还本金',
  MODIFY COLUMN remaining_principal DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '剩余待还本金',
  MODIFY COLUMN interest_amount DECIMAL(28,10) NOT NULL DEFAULT 0.0000000000 COMMENT '累计已还利息',
  MODIFY COLUMN status TINYINT NOT NULL DEFAULT 0 COMMENT '状态 0-待还款 1-还款完成',
  MODIFY COLUMN create_time DATETIME NULL COMMENT '创建时间',
  MODIFY COLUMN update_time DATETIME NULL COMMENT '更新时间';

ALTER TABLE erp_funder_loan_repayment
  MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  MODIFY COLUMN repayment_no VARCHAR(64) NOT NULL COMMENT '还款记录编号',
  MODIFY COLUMN loan_id BIGINT NOT NULL COMMENT '贷款记录ID',
  MODIFY COLUMN line_no INT NOT NULL DEFAULT 1 COMMENT '该贷款的还款序号',
  MODIFY COLUMN repayment_principal DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '本次归还本金',
  MODIFY COLUMN annual_interest_rate DECIMAL(18,10) NOT NULL COMMENT '本次计算使用的年利率快照（百分比）',
  MODIFY COLUMN loan_days INT NOT NULL DEFAULT 0 COMMENT '从首次资方打款日至本次还款日的自然日差',
  MODIFY COLUMN interest_amount DECIMAL(28,10) NOT NULL DEFAULT 0.0000000000 COMMENT '本次应付利息',
  MODIFY COLUMN expected_payment_amount DECIMAL(28,10) NOT NULL DEFAULT 0.0000000000 COMMENT '本次预计打款金额（本金加利息）',
  MODIFY COLUMN recognized_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '还款凭证OCR识别金额',
  MODIFY COLUMN modified_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '人工确认后的实际打款金额',
  MODIFY COLUMN repayment_date DATE NOT NULL COMMENT '还款日期',
  MODIFY COLUMN amount_matched TINYINT NOT NULL DEFAULT 1 COMMENT '实际打款金额与预计打款金额是否一致 0-否 1-是',
  MODIFY COLUMN file_path VARCHAR(500) NOT NULL COMMENT '还款凭证归档路径',
  MODIFY COLUMN file_name VARCHAR(255) NOT NULL COMMENT '还款凭证文件名',
  MODIFY COLUMN raw_text LONGTEXT NULL COMMENT '还款凭证OCR原始文本',
  MODIFY COLUMN status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 0-待确认 1-已确认',
  MODIFY COLUMN remark VARCHAR(500) NULL COMMENT '备注',
  MODIFY COLUMN create_user_id BIGINT NULL COMMENT '创建人ID',
  MODIFY COLUMN create_time DATETIME NULL COMMENT '创建时间',
  MODIFY COLUMN update_time DATETIME NULL COMMENT '更新时间';

SET @erp_parent_id := 40;

INSERT INTO sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num)
SELECT 79, @erp_parent_id, '资方打款管理', 'erp/funder-payment', NULL, 1, 'el-icon-bank-card', 9
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 79 OR url = 'erp/funder-payment');

INSERT INTO sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num)
SELECT 80, 79, '查看', NULL, 'erp:funderpayment:list', 2, NULL, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 80);

INSERT INTO sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num)
SELECT 81, 79, '新增并确认', NULL, 'erp:funderpayment:save', 2, NULL, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 81);

INSERT INTO sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num)
SELECT 82, @erp_parent_id, '资方贷款明细', 'erp/funder-loan', NULL, 1, 'el-icon-coin', 10
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 82 OR url = 'erp/funder-loan');

INSERT INTO sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num)
SELECT 83, 82, '查看', NULL, 'erp:funderloan:list', 2, NULL, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 83);

INSERT INTO sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num)
SELECT 84, 82, '新增还款', NULL, 'erp:funderloan:update', 2, NULL, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 84);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu
WHERE menu_id BETWEEN 79 AND 84
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_menu
    WHERE role_id = 1 AND sys_role_menu.menu_id = sys_menu.menu_id
  );

UPDATE sys_menu SET name = '资方打款管理' WHERE menu_id = 79;
UPDATE sys_menu SET name = '查看' WHERE menu_id = 80;
UPDATE sys_menu SET name = '新增并确认' WHERE menu_id = 81;
UPDATE sys_menu SET name = '资方贷款明细' WHERE menu_id = 82;
UPDATE sys_menu SET name = '查看' WHERE menu_id = 83;
UPDATE sys_menu SET name = '新增还款' WHERE menu_id = 84;
