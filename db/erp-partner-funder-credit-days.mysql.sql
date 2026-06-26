SET @schema_name := DATABASE();

SET @sql := IF(
  EXISTS(
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = @schema_name
      AND table_name = 'erp_partner'
      AND column_name = 'funder_credit_days'
  ),
  'SELECT 1',
  'ALTER TABLE erp_partner ADD COLUMN funder_credit_days INT NULL COMMENT ''资方账期天数'' AFTER annual_interest_rate'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @comment := CONVERT(0xE8B584E696B9E8B4A6E69C9FE5A4A9E695B0 USING utf8mb4);
SET @sql := CONCAT('ALTER TABLE erp_partner MODIFY COLUMN funder_credit_days INT NULL COMMENT ', QUOTE(@comment));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
