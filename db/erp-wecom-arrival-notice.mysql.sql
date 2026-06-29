SET @schema_name := DATABASE();

SET @sql := IF(
  EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'erp_ship_notice' AND column_name = 'notice_type'),
  'SELECT 1',
  'ALTER TABLE erp_ship_notice ADD COLUMN notice_type VARCHAR(32) NOT NULL DEFAULT ''SCHEDULE'' COMMENT ''通知类型 SCHEDULE-船期通知 ARRIVAL-到港通知'' AFTER presale_order_id'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := IF(
  EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'erp_ship_notice' AND column_name = 'confirm_id'),
  'SELECT 1',
  'ALTER TABLE erp_ship_notice ADD COLUMN confirm_id BIGINT NULL COMMENT ''客户订单确认函ID'' AFTER presale_order_id'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := IF(
  EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'erp_ship_notice' AND column_name = 'actual_arrival_date'),
  'SELECT 1',
  'ALTER TABLE erp_ship_notice ADD COLUMN actual_arrival_date DATETIME NULL COMMENT ''实际到港日期'' AFTER expected_arrival_date'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE erp_ship_notice
SET notice_type = 'SCHEDULE'
WHERE notice_type IS NULL OR notice_type = '';

UPDATE erp_ship_notice sn
JOIN erp_presale_confirm pc
  ON pc.presale_order_id = sn.presale_order_id
 AND pc.contract_no = sn.contract_no
SET sn.confirm_id = pc.id
WHERE sn.confirm_id IS NULL;

SET @sql := IF(
  EXISTS(SELECT 1 FROM information_schema.statistics WHERE table_schema = @schema_name AND table_name = 'erp_ship_notice' AND index_name = 'idx_ship_notice_confirm_type'),
  'SELECT 1',
  'ALTER TABLE erp_ship_notice ADD INDEX idx_ship_notice_confirm_type (confirm_id, notice_type)'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
