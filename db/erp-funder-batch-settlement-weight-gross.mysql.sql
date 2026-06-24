SET @db = DATABASE();

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='erp_presale_attachment' AND COLUMN_NAME='recognized_gross_weight')=0,
  'ALTER TABLE erp_presale_attachment ADD COLUMN recognized_gross_weight DECIMAL(18,3) NULL COMMENT ''OCR识别毛重KG'' AFTER remark', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='erp_presale_attachment' AND COLUMN_NAME='confirmed_gross_weight')=0,
  'ALTER TABLE erp_presale_attachment ADD COLUMN confirmed_gross_weight DECIMAL(18,3) NULL COMMENT ''用户确认毛重KG'' AFTER recognized_gross_weight', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='erp_presale_attachment' AND COLUMN_NAME='raw_text')=0,
  'ALTER TABLE erp_presale_attachment ADD COLUMN raw_text LONGTEXT NULL COMMENT ''OCR原文'' AFTER confirmed_gross_weight', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='erp_sale_outbound_batch' AND COLUMN_NAME='funder_repayment_status')=0,
  'ALTER TABLE erp_sale_outbound_batch ADD COLUMN funder_repayment_status TINYINT NOT NULL DEFAULT 0 COMMENT ''资方还款状态：0不需要，1待还款资方，2已还款资方'' AFTER ownership_name', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='erp_funder_batch_settlement' AND COLUMN_NAME='include_code_scan_fee')=0,
  'ALTER TABLE erp_funder_batch_settlement ADD COLUMN include_code_scan_fee TINYINT NOT NULL DEFAULT 0 COMMENT ''是否计算扫码费：0否1是'' AFTER gross_weight_fee_amount', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='erp_funder_batch_settlement_item' AND COLUMN_NAME='fee_weight')=0,
  'ALTER TABLE erp_funder_batch_settlement_item ADD COLUMN fee_weight DECIMAL(18,3) NOT NULL DEFAULT 0.000 COMMENT ''资方费用计费重量KG'' AFTER shipped_weight', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='erp_funder_batch_settlement_item' AND COLUMN_NAME='packing_total_boxes')=0,
  'ALTER TABLE erp_funder_batch_settlement_item ADD COLUMN packing_total_boxes INT NULL COMMENT ''装箱单匹配总箱数'' AFTER fee_weight', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='erp_funder_batch_settlement_item' AND COLUMN_NAME='packing_total_weight')=0,
  'ALTER TABLE erp_funder_batch_settlement_item ADD COLUMN packing_total_weight DECIMAL(18,3) NULL COMMENT ''装箱单匹配总重量KG'' AFTER packing_total_boxes', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='erp_funder_batch_settlement_item' AND COLUMN_NAME='packing_avg_weight')=0,
  'ALTER TABLE erp_funder_batch_settlement_item ADD COLUMN packing_avg_weight DECIMAL(18,6) NULL COMMENT ''装箱单平均箱重KG'' AFTER packing_total_weight', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='erp_funder_batch_settlement_item' AND COLUMN_NAME='customs_gross_weight')=0,
  'ALTER TABLE erp_funder_batch_settlement_item ADD COLUMN customs_gross_weight DECIMAL(18,3) NULL COMMENT ''报关单确认毛重KG'' AFTER packing_avg_weight', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='erp_funder_batch_settlement_item' AND COLUMN_NAME='gross_diff_weight')=0,
  'ALTER TABLE erp_funder_batch_settlement_item ADD COLUMN gross_diff_weight DECIMAL(18,3) NULL COMMENT ''本次毛重差KG'' AFTER customs_gross_weight', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='erp_funder_batch_settlement_item' AND COLUMN_NAME='gross_fee_days')=0,
  'ALTER TABLE erp_funder_batch_settlement_item ADD COLUMN gross_fee_days INT NULL COMMENT ''毛重费用计费天数'' AFTER gross_diff_weight', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='erp_funder_batch_settlement_item' AND COLUMN_NAME='gross_fee_rate')=0,
  'ALTER TABLE erp_funder_batch_settlement_item ADD COLUMN gross_fee_rate DECIMAL(18,2) NULL COMMENT ''毛重费用单价元每吨每天'' AFTER gross_fee_days', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
