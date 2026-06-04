-- 产品档案增加市场流通名称
SET @column_exists := (
  SELECT COUNT(1)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'erp_product'
    AND column_name = 'market_circulation_name'
);

SET @sql := IF(
  @column_exists = 0,
  'ALTER TABLE `erp_product` ADD COLUMN `market_circulation_name` varchar(200) DEFAULT NULL COMMENT ''市场流通名称'' AFTER `product_name_en`',
  'SELECT 1'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
