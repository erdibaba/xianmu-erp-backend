-- 产品档案增加品牌方主体ID，并按现有品牌方名称补全关联。
SET @column_exists := (
  SELECT COUNT(1)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'erp_product'
    AND column_name = 'brand_id'
);

SET @sql := IF(
  @column_exists = 0,
  'ALTER TABLE `erp_product` ADD COLUMN `brand_id` bigint(20) DEFAULT NULL COMMENT ''品牌方主体ID'' AFTER `unit`',
  'SELECT 1'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @index_exists := (
  SELECT COUNT(1)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'erp_product'
    AND index_name = 'idx_product_brand_id'
);

SET @sql := IF(
  @index_exists = 0,
  'ALTER TABLE `erp_product` ADD INDEX `idx_product_brand_id` (`brand_id`)',
  'SELECT 1'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE `erp_product` product
JOIN `erp_partner` partner
  ON partner.`partner_name` = product.`brand`
 AND FIND_IN_SET('BRAND', partner.`business_role`) > 0
SET product.`brand_id` = partner.`id`
WHERE product.`brand_id` IS NULL;
