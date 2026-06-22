-- 资方按出库批次结算：不同资方不同还款和费用公式，批次确认后生成结算并分摊到小合同贷款。

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'erp_partner' AND column_name = 'funder_fee_rule_type'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE erp_partner ADD COLUMN funder_fee_rule_type varchar(32) DEFAULT NULL COMMENT ''资方费用规则类型 RUIHEXIANG/CHAOYUE/WANXIANG'' AFTER annual_interest_rate',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

ALTER TABLE `erp_partner`
  MODIFY COLUMN `funder_fee_rule_type` varchar(32) DEFAULT NULL COMMENT '资方费用规则类型 RUIHEXIANG-瑞和祥 CHAOYUE-超跃 WANXIANG-万翔';

UPDATE erp_partner
SET funder_fee_rule_type = 'WANXIANG'
WHERE partner_name LIKE '%万翔%' AND business_role LIKE '%FUNDER%' AND (funder_fee_rule_type IS NULL OR funder_fee_rule_type = '');

INSERT INTO erp_partner (
  partner_code, partner_name, partner_type, business_role, annual_interest_rate, funder_fee_rule_type, status, remark, create_time, update_time
)
SELECT
  CONCAT('P', LPAD(IFNULL((SELECT MAX(CAST(SUBSTRING(p.partner_code, 2) AS UNSIGNED)) FROM erp_partner p WHERE p.partner_code REGEXP '^P[0-9]+$'), 0) + 1, 4, '0')),
  '瑞和祥',
  1,
  'FUNDER',
  6.0000000000,
  'RUIHEXIANG',
  1,
  '系统初始化资方：瑞和祥费用规则',
  NOW(),
  NOW()
WHERE NOT EXISTS (SELECT 1 FROM erp_partner p WHERE p.partner_name LIKE '%瑞和祥%' AND p.business_role LIKE '%FUNDER%');

INSERT INTO erp_partner (
  partner_code, partner_name, partner_type, business_role, annual_interest_rate, funder_fee_rule_type, status, remark, create_time, update_time
)
SELECT
  CONCAT('P', LPAD(IFNULL((SELECT MAX(CAST(SUBSTRING(p.partner_code, 2) AS UNSIGNED)) FROM erp_partner p WHERE p.partner_code REGEXP '^P[0-9]+$'), 0) + 1, 4, '0')),
  '超跃',
  1,
  'FUNDER',
  6.9840000000,
  'CHAOYUE',
  1,
  '系统初始化资方：超跃费用规则',
  NOW(),
  NOW()
WHERE NOT EXISTS (SELECT 1 FROM erp_partner p WHERE p.partner_name LIKE '%超跃%' AND p.business_role LIKE '%FUNDER%');

UPDATE erp_partner
SET funder_fee_rule_type = 'RUIHEXIANG', annual_interest_rate = IFNULL(annual_interest_rate, 6.0000000000)
WHERE partner_name LIKE '%瑞和祥%' AND business_role LIKE '%FUNDER%';

UPDATE erp_partner
SET funder_fee_rule_type = 'CHAOYUE', annual_interest_rate = IFNULL(annual_interest_rate, 6.9840000000)
WHERE partner_name LIKE '%超跃%' AND business_role LIKE '%FUNDER%';

CREATE TABLE IF NOT EXISTS `erp_funder_batch_settlement` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '资方出库批次结算ID',
  `settlement_no` varchar(64) NOT NULL COMMENT '结算单号',
  `outbound_batch_id` bigint NOT NULL COMMENT '出库批次ID',
  `sale_order_id` bigint NOT NULL COMMENT '销售单ID',
  `batch_no` varchar(64) DEFAULT NULL COMMENT '出库批次号快照',
  `sale_order_no` varchar(64) DEFAULT NULL COMMENT '销售单号快照',
  `funder_id` bigint NOT NULL COMMENT '资方ID',
  `funder_name` varchar(200) NOT NULL COMMENT '资方名称快照',
  `rule_type` varchar(32) NOT NULL COMMENT '资方费用规则类型',
  `ownership_name` varchar(200) DEFAULT NULL COMMENT '货权名称快照',
  `settlement_date` date NOT NULL COMMENT '结算/还款日期',
  `system_principal_amount` decimal(18,2) NOT NULL DEFAULT 0.00 COMMENT '系统计算还本金额',
  `confirmed_principal_amount` decimal(18,2) NOT NULL DEFAULT 0.00 COMMENT '确认还本金额',
  `interest_amount` decimal(18,2) NOT NULL DEFAULT 0.00 COMMENT '利息/资金成本',
  `storage_fee_amount` decimal(18,2) NOT NULL DEFAULT 0.00 COMMENT '仓储费用',
  `handling_fee_amount` decimal(18,2) NOT NULL DEFAULT 0.00 COMMENT '出入库/装卸费用',
  `code_scan_fee_amount` decimal(18,2) NOT NULL DEFAULT 0.00 COMMENT '抄码费用',
  `stamp_tax_amount` decimal(18,2) NOT NULL DEFAULT 0.00 COMMENT '印花税',
  `deposit_amount` decimal(18,2) NOT NULL DEFAULT 0.00 COMMENT '保证金',
  `tax_adjust_amount` decimal(18,2) NOT NULL DEFAULT 0.00 COMMENT '补税点费用',
  `gross_weight_fee_amount` decimal(18,2) NOT NULL DEFAULT 0.00 COMMENT '毛重费用',
  `other_fee_amount` decimal(18,2) NOT NULL DEFAULT 0.00 COMMENT '其他费用',
  `expected_payment_amount` decimal(18,2) NOT NULL DEFAULT 0.00 COMMENT '系统预计应付资方金额',
  `recognized_payment_amount` decimal(18,2) NOT NULL DEFAULT 0.00 COMMENT '还款凭证OCR识别金额',
  `confirmed_payment_amount` decimal(18,2) NOT NULL DEFAULT 0.00 COMMENT '确认应付资方金额',
  `file_path` varchar(500) DEFAULT NULL COMMENT '还款凭证归档路径',
  `file_name` varchar(255) DEFAULT NULL COMMENT '还款凭证文件名',
  `raw_text` longtext COMMENT '还款凭证OCR原始文本',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态 1-已确认',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_user_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_funder_settlement_no` (`settlement_no`),
  UNIQUE KEY `uk_funder_settlement_batch` (`outbound_batch_id`),
  KEY `idx_funder_settlement_funder_date` (`funder_id`, `settlement_date`),
  KEY `idx_funder_settlement_sale_order` (`sale_order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资方出库批次结算表';

CREATE TABLE IF NOT EXISTS `erp_funder_batch_settlement_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '资方出库批次结算明细ID',
  `settlement_id` bigint NOT NULL COMMENT '资方出库批次结算ID',
  `outbound_batch_id` bigint NOT NULL COMMENT '出库批次ID',
  `plan_item_id` bigint DEFAULT NULL COMMENT '出库批次计划明细ID',
  `sale_order_item_id` bigint DEFAULT NULL COMMENT '销售单明细ID',
  `loan_id` bigint NOT NULL COMMENT '资方贷款ID',
  `confirm_id` bigint DEFAULT NULL COMMENT '客户订单确认函ID',
  `confirm_contract_no` varchar(100) DEFAULT NULL COMMENT '确认函合同号',
  `line_no` int NOT NULL COMMENT '行号',
  `product_id` bigint DEFAULT NULL COMMENT '产品ID',
  `product_code` varchar(100) DEFAULT NULL COMMENT '产品编码快照',
  `product_name` varchar(255) DEFAULT NULL COMMENT '产品名称快照',
  `container_no` varchar(100) DEFAULT NULL COMMENT '柜号快照',
  `factory_no` varchar(100) DEFAULT NULL COMMENT '厂号快照',
  `shipped_boxes` int NOT NULL DEFAULT 0 COMMENT '本批次实际出库箱数',
  `shipped_weight` decimal(18,3) NOT NULL DEFAULT 0.000 COMMENT '本批次实际出库重量KG',
  `unit_price_incl_tax` decimal(18,6) NOT NULL DEFAULT 0.000000 COMMENT '订单确认函产品单价',
  `cost_amount` decimal(18,2) NOT NULL DEFAULT 0.00 COMMENT '按确认函单价计算的货值',
  `system_principal_amount` decimal(18,2) NOT NULL DEFAULT 0.00 COMMENT '系统计算还本金额',
  `confirmed_principal_amount` decimal(18,2) NOT NULL DEFAULT 0.00 COMMENT '确认还本金额',
  `loan_days` int NOT NULL DEFAULT 0 COMMENT '计息天数',
  `interest_amount` decimal(18,2) NOT NULL DEFAULT 0.00 COMMENT '利息/资金成本',
  `storage_fee_amount` decimal(18,2) NOT NULL DEFAULT 0.00 COMMENT '仓储费用',
  `handling_fee_amount` decimal(18,2) NOT NULL DEFAULT 0.00 COMMENT '出入库/装卸费用',
  `code_scan_fee_amount` decimal(18,2) NOT NULL DEFAULT 0.00 COMMENT '抄码费用',
  `stamp_tax_amount` decimal(18,2) NOT NULL DEFAULT 0.00 COMMENT '印花税',
  `deposit_amount` decimal(18,2) NOT NULL DEFAULT 0.00 COMMENT '保证金',
  `tax_adjust_amount` decimal(18,2) NOT NULL DEFAULT 0.00 COMMENT '补税点费用',
  `gross_weight_fee_amount` decimal(18,2) NOT NULL DEFAULT 0.00 COMMENT '毛重费用',
  `other_fee_amount` decimal(18,2) NOT NULL DEFAULT 0.00 COMMENT '其他费用',
  `expected_payment_amount` decimal(18,2) NOT NULL DEFAULT 0.00 COMMENT '系统预计应付资方金额',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_funder_settle_item_settlement` (`settlement_id`),
  KEY `idx_funder_settle_item_batch` (`outbound_batch_id`),
  KEY `idx_funder_settle_item_loan` (`loan_id`),
  KEY `idx_funder_settle_item_confirm` (`confirm_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资方出库批次结算明细表';

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'erp_funder_loan_repayment' AND column_name = 'settlement_id'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE erp_funder_loan_repayment ADD COLUMN settlement_id bigint DEFAULT NULL COMMENT ''资方批次结算ID'' AFTER loan_id',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'erp_funder_loan_repayment' AND column_name = 'settlement_item_id'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE erp_funder_loan_repayment ADD COLUMN settlement_item_id bigint DEFAULT NULL COMMENT ''资方批次结算明细ID'' AFTER settlement_id',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'erp_funder_loan_repayment' AND column_name = 'outbound_batch_id'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE erp_funder_loan_repayment ADD COLUMN outbound_batch_id bigint DEFAULT NULL COMMENT ''出库批次ID'' AFTER settlement_item_id',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'erp_funder_loan_repayment' AND column_name = 'outbound_batch_no'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE erp_funder_loan_repayment ADD COLUMN outbound_batch_no varchar(64) DEFAULT NULL COMMENT ''出库批次号'' AFTER outbound_batch_id',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'erp_funder_loan_repayment' AND column_name = 'repayment_source'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE erp_funder_loan_repayment ADD COLUMN repayment_source varchar(32) DEFAULT NULL COMMENT ''还款来源 MANUAL手工 BATCH出库批次'' AFTER outbound_batch_no',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

ALTER TABLE `erp_funder_loan_repayment`
  MODIFY COLUMN `settlement_id` bigint DEFAULT NULL COMMENT '资方批次结算ID',
  MODIFY COLUMN `settlement_item_id` bigint DEFAULT NULL COMMENT '资方批次结算明细ID',
  MODIFY COLUMN `outbound_batch_id` bigint DEFAULT NULL COMMENT '出库批次ID',
  MODIFY COLUMN `outbound_batch_no` varchar(64) DEFAULT NULL COMMENT '出库批次号',
  MODIFY COLUMN `repayment_source` varchar(32) DEFAULT NULL COMMENT '还款来源 MANUAL手工 BATCH出库批次';

SET @idx_exists := (
  SELECT COUNT(1) FROM information_schema.statistics
  WHERE table_schema = DATABASE() AND table_name = 'erp_funder_loan_repayment' AND index_name = 'idx_funder_repayment_batch'
);
SET @sql := IF(@idx_exists = 0,
  'CREATE INDEX idx_funder_repayment_batch ON erp_funder_loan_repayment (outbound_batch_id, loan_id)',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
