-- 仓库扫码费配置：仓库控制是否启用，费用历史控制计费方式和单价。
SET @db = DATABASE();

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='erp_warehouse' AND COLUMN_NAME='scan_fee_enabled')=0,
  'ALTER TABLE erp_warehouse ADD COLUMN scan_fee_enabled TINYINT NOT NULL DEFAULT 1 COMMENT ''是否启用扫码费：0否1是'' AFTER fee_unit',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='erp_warehouse_fee_rate' AND COLUMN_NAME='scan_fee_unit')=0,
  'ALTER TABLE erp_warehouse_fee_rate ADD COLUMN scan_fee_unit VARCHAR(16) NOT NULL DEFAULT ''TON'' COMMENT ''扫码费计费方式：BOX按箱，TON按吨'' AFTER chilled_cold_fee',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='erp_warehouse_fee_rate' AND COLUMN_NAME='scan_fee_rate')=0,
  'ALTER TABLE erp_warehouse_fee_rate ADD COLUMN scan_fee_rate DECIMAL(18,2) NOT NULL DEFAULT 15.00 COMMENT ''扫码费单价'' AFTER scan_fee_unit',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

UPDATE erp_warehouse
SET scan_fee_enabled = 1
WHERE scan_fee_enabled IS NULL;

UPDATE erp_warehouse_fee_rate r
JOIN erp_warehouse w ON w.id = r.warehouse_id
SET r.scan_fee_unit = CASE WHEN w.warehouse_name LIKE '%上海%' THEN 'BOX' ELSE 'TON' END,
    r.scan_fee_rate = CASE WHEN w.warehouse_name LIKE '%上海%' THEN 0.35 ELSE 15.00 END
WHERE r.scan_fee_rate IS NULL
   OR r.scan_fee_rate = 0
   OR (r.scan_fee_unit = 'TON' AND r.scan_fee_rate = 15.00);
