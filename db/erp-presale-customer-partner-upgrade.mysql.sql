-- Link presale customer reference to an INTERNAL partner.
SELECT COUNT(1)
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'erp_presale_order'
  AND COLUMN_NAME = 'customer_partner_id'
INTO @column_exists;
SET @ddl := IF(
  @column_exists = 0,
  'ALTER TABLE `erp_presale_order` ADD COLUMN `customer_partner_id` bigint(20) DEFAULT NULL COMMENT ''采购方主体ID'' AFTER `seller_contract_no`',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE `erp_presale_order` o
JOIN `erp_partner` p
  ON p.partner_name = o.customer_reference
 AND FIND_IN_SET('INTERNAL', REPLACE(IFNULL(p.business_role, ''), ' ', '')) > 0
SET o.customer_partner_id = p.id
WHERE o.customer_partner_id IS NULL;

ALTER TABLE `erp_presale_order`
  MODIFY COLUMN `customer_partner_id` bigint(20) DEFAULT NULL COMMENT '采购方主体ID',
  MODIFY COLUMN `customer_reference` varchar(200) DEFAULT NULL COMMENT '采购方名称';
