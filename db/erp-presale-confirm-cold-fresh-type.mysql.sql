-- 客户订单确认函增加冷冻/冷鲜字段
SET @column_exists := (
  SELECT COUNT(1)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'erp_presale_confirm'
    AND column_name = 'cold_fresh_type'
);

SET @sql := IF(
  @column_exists = 0,
  'ALTER TABLE `erp_presale_confirm` ADD COLUMN `cold_fresh_type` varchar(32) DEFAULT NULL COMMENT ''冷冻/冷鲜'' AFTER `container_no`',
  'SELECT 1'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
