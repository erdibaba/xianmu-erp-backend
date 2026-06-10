-- 销售单出库批次：二批来款水单OCR识别与核对字段

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'erp_sale_outbound_batch' AND column_name = 'bank_voucher_template'
);
SET @sql := IF(@col_exists = 0, 'ALTER TABLE erp_sale_outbound_batch ADD COLUMN bank_voucher_template VARCHAR(100) NULL COMMENT ''银行回单模板'' AFTER shipped_total_weight', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'erp_sale_outbound_batch' AND column_name = 'bank_payer_name_recognized'
);
SET @sql := IF(@col_exists = 0, 'ALTER TABLE erp_sale_outbound_batch ADD COLUMN bank_payer_name_recognized VARCHAR(200) NULL COMMENT ''识别付款人'' AFTER bank_voucher_template', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'erp_sale_outbound_batch' AND column_name = 'bank_payer_name_modified'
);
SET @sql := IF(@col_exists = 0, 'ALTER TABLE erp_sale_outbound_batch ADD COLUMN bank_payer_name_modified VARCHAR(200) NULL COMMENT ''确认付款人'' AFTER bank_payer_name_recognized', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'erp_sale_outbound_batch' AND column_name = 'bank_payee_name_recognized'
);
SET @sql := IF(@col_exists = 0, 'ALTER TABLE erp_sale_outbound_batch ADD COLUMN bank_payee_name_recognized VARCHAR(200) NULL COMMENT ''识别收款人'' AFTER bank_payer_name_modified', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'erp_sale_outbound_batch' AND column_name = 'bank_payee_name_modified'
);
SET @sql := IF(@col_exists = 0, 'ALTER TABLE erp_sale_outbound_batch ADD COLUMN bank_payee_name_modified VARCHAR(200) NULL COMMENT ''确认收款人'' AFTER bank_payee_name_recognized', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'erp_sale_outbound_batch' AND column_name = 'bank_amount_recognized'
);
SET @sql := IF(@col_exists = 0, 'ALTER TABLE erp_sale_outbound_batch ADD COLUMN bank_amount_recognized DECIMAL(18,2) NULL COMMENT ''识别金额'' AFTER bank_payee_name_modified', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'erp_sale_outbound_batch' AND column_name = 'bank_amount_modified'
);
SET @sql := IF(@col_exists = 0, 'ALTER TABLE erp_sale_outbound_batch ADD COLUMN bank_amount_modified DECIMAL(18,2) NULL COMMENT ''确认金额'' AFTER bank_amount_recognized', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'erp_sale_outbound_batch' AND column_name = 'bank_payment_date_recognized'
);
SET @sql := IF(@col_exists = 0, 'ALTER TABLE erp_sale_outbound_batch ADD COLUMN bank_payment_date_recognized DATETIME NULL COMMENT ''识别付款日期'' AFTER bank_amount_modified', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'erp_sale_outbound_batch' AND column_name = 'bank_payment_date_modified'
);
SET @sql := IF(@col_exists = 0, 'ALTER TABLE erp_sale_outbound_batch ADD COLUMN bank_payment_date_modified DATETIME NULL COMMENT ''确认付款日期'' AFTER bank_payment_date_recognized', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'erp_sale_outbound_batch' AND column_name = 'bank_serial_no_recognized'
);
SET @sql := IF(@col_exists = 0, 'ALTER TABLE erp_sale_outbound_batch ADD COLUMN bank_serial_no_recognized VARCHAR(100) NULL COMMENT ''识别流水号'' AFTER bank_payment_date_modified', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'erp_sale_outbound_batch' AND column_name = 'bank_serial_no_modified'
);
SET @sql := IF(@col_exists = 0, 'ALTER TABLE erp_sale_outbound_batch ADD COLUMN bank_serial_no_modified VARCHAR(100) NULL COMMENT ''确认流水号'' AFTER bank_serial_no_recognized', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'erp_sale_outbound_batch' AND column_name = 'bank_expected_amount'
);
SET @sql := IF(@col_exists = 0, 'ALTER TABLE erp_sale_outbound_batch ADD COLUMN bank_expected_amount DECIMAL(18,2) NULL COMMENT ''批次应收金额'' AFTER bank_serial_no_modified', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'erp_sale_outbound_batch' AND column_name = 'bank_amount_diff'
);
SET @sql := IF(@col_exists = 0, 'ALTER TABLE erp_sale_outbound_batch ADD COLUMN bank_amount_diff DECIMAL(18,2) NULL COMMENT ''确认金额与批次应收金额差异'' AFTER bank_expected_amount', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'erp_sale_outbound_batch' AND column_name = 'bank_receipt_raw_text'
);
SET @sql := IF(@col_exists = 0, 'ALTER TABLE erp_sale_outbound_batch ADD COLUMN bank_receipt_raw_text MEDIUMTEXT NULL COMMENT ''银行水单OCR原文'' AFTER bank_amount_diff', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
