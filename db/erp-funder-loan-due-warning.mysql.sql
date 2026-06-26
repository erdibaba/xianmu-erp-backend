SET @schema_name := DATABASE();

SET @sql := IF(
  EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'erp_partner' AND column_name = 'funder_warning_days'),
  'SELECT 1',
  'ALTER TABLE erp_partner ADD COLUMN funder_warning_days INT NULL COMMENT ''资方账期预警天数'' AFTER funder_credit_days'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := IF(
  EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'erp_funder_loan' AND column_name = 'loan_credit_days'),
  'SELECT 1',
  'ALTER TABLE erp_funder_loan ADD COLUMN loan_credit_days INT NULL COMMENT ''账期天数快照'' AFTER loan_date'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := IF(
  EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'erp_funder_loan' AND column_name = 'warning_days'),
  'SELECT 1',
  'ALTER TABLE erp_funder_loan ADD COLUMN warning_days INT NULL COMMENT ''预警天数快照'' AFTER loan_credit_days'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := IF(
  EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'erp_funder_loan' AND column_name = 'original_due_date'),
  'SELECT 1',
  'ALTER TABLE erp_funder_loan ADD COLUMN original_due_date DATE NULL COMMENT ''原始到期日'' AFTER warning_days'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := IF(
  EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'erp_funder_loan' AND column_name = 'current_due_date'),
  'SELECT 1',
  'ALTER TABLE erp_funder_loan ADD COLUMN current_due_date DATE NULL COMMENT ''当前到期日'' AFTER original_due_date'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := IF(
  EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'erp_funder_loan' AND column_name = 'due_extend_reason'),
  'SELECT 1',
  'ALTER TABLE erp_funder_loan ADD COLUMN due_extend_reason VARCHAR(500) NULL COMMENT ''延期原因'' AFTER current_due_date'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE erp_partner
SET funder_warning_days = 14
WHERE business_role LIKE '%FUNDER%'
  AND funder_warning_days IS NULL;

UPDATE erp_funder_loan l
LEFT JOIN erp_partner p ON p.id = l.funder_id
SET
  l.loan_credit_days = COALESCE(l.loan_credit_days, p.funder_credit_days),
  l.warning_days = COALESCE(l.warning_days, p.funder_warning_days, 14),
  l.original_due_date = CASE
    WHEN l.original_due_date IS NULL AND COALESCE(l.loan_credit_days, p.funder_credit_days) IS NOT NULL
    THEN DATE_ADD(l.loan_date, INTERVAL COALESCE(l.loan_credit_days, p.funder_credit_days) DAY)
    ELSE l.original_due_date
  END,
  l.current_due_date = CASE
    WHEN l.current_due_date IS NULL AND COALESCE(l.loan_credit_days, p.funder_credit_days) IS NOT NULL
    THEN DATE_ADD(l.loan_date, INTERVAL COALESCE(l.loan_credit_days, p.funder_credit_days) DAY)
    ELSE l.current_due_date
  END
WHERE l.status = 0;
