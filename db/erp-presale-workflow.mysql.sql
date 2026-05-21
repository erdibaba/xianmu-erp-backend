SET NAMES utf8mb4;

ALTER TABLE `erp_product`
  ADD COLUMN `alias_codes` varchar(255) DEFAULT NULL COMMENT '外部编码映射，逗号分隔' AFTER `product_code`;

CREATE TABLE IF NOT EXISTS `erp_presale_order` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_no` varchar(64) DEFAULT NULL,
  `seller_contract_no` varchar(100) DEFAULT NULL,
  `customer_reference` varchar(200) DEFAULT NULL,
  `brand_id` bigint(20) DEFAULT NULL,
  `brand_name` varchar(200) DEFAULT NULL,
  `estimate_file_path` varchar(500) DEFAULT NULL,
  `estimate_file_name` varchar(255) DEFAULT NULL,
  `estimate_raw_text` longtext,
  `currency` varchar(32) DEFAULT 'CNY',
  `order_date` datetime DEFAULT NULL,
  `expected_date` datetime DEFAULT NULL,
  `status` int(11) DEFAULT 0,
  `remark` varchar(1000) DEFAULT NULL,
  `create_user_id` bigint(20) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_erp_presale_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预销售单主单';

CREATE TABLE IF NOT EXISTS `erp_presale_order_item` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `presale_order_id` bigint(20) DEFAULT NULL,
  `line_no` int(11) DEFAULT NULL,
  `product_id` bigint(20) DEFAULT NULL,
  `product_code` varchar(64) DEFAULT NULL,
  `source_product_code` varchar(64) DEFAULT NULL,
  `product_name` varchar(200) DEFAULT NULL,
  `product_name_en` varchar(500) DEFAULT NULL,
  `quantity_ton` decimal(18,4) DEFAULT 0.0000,
  `quantity_kg` decimal(18,2) DEFAULT 0.00,
  `price_amount` decimal(18,4) DEFAULT 0.0000,
  `price_currency` varchar(32) DEFAULT 'CNY',
  `price_unit` varchar(32) DEFAULT 'KG',
  `remark` varchar(500) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_erp_presale_order_item_order` (`presale_order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预售销售单明细';

CREATE TABLE IF NOT EXISTS `erp_presale_confirm` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `presale_order_id` bigint(20) DEFAULT NULL,
  `brand_id` bigint(20) DEFAULT NULL,
  `brand_name` varchar(200) DEFAULT NULL,
  `buyer_partner_id` bigint(20) DEFAULT NULL,
  `buyer_partner_name` varchar(200) DEFAULT NULL,
  `buyer_partner_role` varchar(128) DEFAULT NULL,
  `contract_no` varchar(100) DEFAULT NULL,
  `container_no` varchar(100) DEFAULT NULL,
  `expected_arrival_date` datetime DEFAULT NULL,
  `total_amount` decimal(18,2) DEFAULT 0.00,
  `currency` varchar(32) DEFAULT 'CNY',
  `file_path` varchar(500) DEFAULT NULL,
  `file_name` varchar(255) DEFAULT NULL,
  `raw_text` longtext,
  `remark` varchar(1000) DEFAULT NULL,
  `create_user_id` bigint(20) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_erp_presale_confirm_order` (`presale_order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户订单确认函';

CREATE TABLE IF NOT EXISTS `erp_presale_confirm_item` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `confirm_id` bigint(20) DEFAULT NULL,
  `line_no` int(11) DEFAULT NULL,
  `product_id` bigint(20) DEFAULT NULL,
  `product_code` varchar(64) DEFAULT NULL,
  `source_product_code` varchar(64) DEFAULT NULL,
  `product_name` varchar(200) DEFAULT NULL,
  `product_name_en` varchar(500) DEFAULT NULL,
  `unit` varchar(32) DEFAULT NULL,
  `quantity` decimal(18,2) DEFAULT 0.00,
  `unit_price_incl_tax` decimal(18,4) DEFAULT 0.0000,
  `line_total_incl_tax` decimal(18,2) DEFAULT 0.00,
  `tax_rate` decimal(10,2) DEFAULT 9.00,
  `remark` varchar(500) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_erp_presale_confirm_item_confirm` (`confirm_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户订单确认函明细';

INSERT INTO `erp_partner`
(`partner_code`,`partner_name`,`partner_type`,`business_role`,`remark`,`status`,`create_user_id`,`create_time`,`update_time`)
SELECT 'FUN-XMWX','厦门万翔物流管理有限公司',3,'FUNDER','客户订单确认函采购方/资方主体',1,1,NOW(),NOW()
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM erp_partner WHERE partner_code = 'FUN-XMWX');

UPDATE `erp_partner` SET `business_role` = 'BRAND' WHERE `partner_code` = 'SUP-SFF';
UPDATE `erp_partner` SET `business_role` = 'INTERNAL' WHERE `partner_code` = 'CUS-TJXM';
UPDATE `erp_partner` SET `business_role` = 'SECONDARY' WHERE `partner_code` = 'CUS-HNKT';
