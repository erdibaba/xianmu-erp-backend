-- 资方贷款还款记录：手续费金额与手续费原因

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'erp_funder_loan_repayment' AND column_name = 'handling_fee_amount'
);
SET @sql := IF(@col_exists = 0, 'ALTER TABLE erp_funder_loan_repayment ADD COLUMN handling_fee_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT ''手续费金额'' AFTER interest_amount', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'erp_funder_loan_repayment' AND column_name = 'handling_fee_reason'
);
SET @sql := IF(@col_exists = 0, 'ALTER TABLE erp_funder_loan_repayment ADD COLUMN handling_fee_reason VARCHAR(200) NULL COMMENT ''手续费原因'' AFTER handling_fee_amount', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

ALTER TABLE erp_funder_loan_repayment
  MODIFY COLUMN expected_payment_amount DECIMAL(28,10) NOT NULL DEFAULT 0.0000000000 COMMENT '本次预计打款金额（本金+利息+手续费）';
