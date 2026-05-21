SET NAMES utf8mb4;

ALTER TABLE `erp_partner`
  ADD COLUMN IF NOT EXISTS `business_role` varchar(128) DEFAULT NULL COMMENT 'BRAND/FUNDER/SECONDARY/INTERNAL' AFTER `partner_type`;

ALTER TABLE `erp_product`
  ADD COLUMN IF NOT EXISTS `alias_codes` varchar(255) DEFAULT NULL COMMENT '外部编码映射，逗号分隔' AFTER `product_code`;

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

CREATE TABLE IF NOT EXISTS `erp_warehouse` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `warehouse_code` varchar(64) DEFAULT NULL,
  `warehouse_name` varchar(200) DEFAULT NULL,
  `warehouse_type` varchar(64) DEFAULT NULL,
  `owned_by_company` int(11) DEFAULT 0,
  `contact_name` varchar(64) DEFAULT NULL,
  `contact_phone` varchar(64) DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  `free_storage_days` int(11) DEFAULT 0,
  `daily_storage_fee` decimal(18,2) DEFAULT 0.00,
  `daily_cold_fee` decimal(18,2) DEFAULT 0.00,
  `fee_unit` varchar(32) DEFAULT 'PIECE',
  `status` int(11) DEFAULT 1,
  `remark` varchar(500) DEFAULT NULL,
  `create_user_id` bigint(20) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_erp_warehouse_code` (`warehouse_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仓库/冷链仓';

ALTER TABLE `erp_product`
  ADD COLUMN IF NOT EXISTS `safe_stock_boxes` decimal(18,2) DEFAULT 0.00 COMMENT '安全库存件数' AFTER `default_tax_rate`,
  ADD COLUMN IF NOT EXISTS `freshness_warning_days` int(11) DEFAULT 0 COMMENT '保鲜预警天数' AFTER `safe_stock_boxes`;

ALTER TABLE `erp_trade_order`
  ADD COLUMN IF NOT EXISTS `biz_type` varchar(64) DEFAULT NULL COMMENT '业务类型' AFTER `order_type`,
  ADD COLUMN IF NOT EXISTS `brand_id` bigint(20) DEFAULT NULL AFTER `partner_name`,
  ADD COLUMN IF NOT EXISTS `brand_name` varchar(200) DEFAULT NULL AFTER `brand_id`,
  ADD COLUMN IF NOT EXISTS `secondary_partner_id` bigint(20) DEFAULT NULL AFTER `brand_name`,
  ADD COLUMN IF NOT EXISTS `secondary_partner_name` varchar(200) DEFAULT NULL AFTER `secondary_partner_id`,
  ADD COLUMN IF NOT EXISTS `funder_id` bigint(20) DEFAULT NULL AFTER `secondary_partner_name`,
  ADD COLUMN IF NOT EXISTS `funder_name` varchar(200) DEFAULT NULL AFTER `funder_id`,
  ADD COLUMN IF NOT EXISTS `related_contract_no` varchar(100) DEFAULT NULL AFTER `contract_no`,
  ADD COLUMN IF NOT EXISTS `warehouse_id` bigint(20) DEFAULT NULL AFTER `container_no`,
  ADD COLUMN IF NOT EXISTS `storage_start_date` datetime DEFAULT NULL AFTER `warehouse_name`,
  ADD COLUMN IF NOT EXISTS `source_order_id` bigint(20) DEFAULT NULL AFTER `warehouse_name`,
  ADD COLUMN IF NOT EXISTS `source_order_no` varchar(64) DEFAULT NULL AFTER `source_order_id`,
  ADD COLUMN IF NOT EXISTS `actual_out_date` datetime DEFAULT NULL AFTER `payment_due_date`,
  ADD COLUMN IF NOT EXISTS `payment_status` int(11) DEFAULT 0 COMMENT '0未支付 1部分支付 2已支付' AFTER `status`,
  ADD COLUMN IF NOT EXISTS `invoice_status` int(11) DEFAULT 0 COMMENT '0未开票 1部分开票 2已开票' AFTER `payment_status`,
  ADD COLUMN IF NOT EXISTS `auto_outbound` int(11) DEFAULT 0 COMMENT '是否自动生成赎单' AFTER `invoice_status`,
  ADD COLUMN IF NOT EXISTS `storage_fee_start_days` int(11) DEFAULT 0 COMMENT '几天后开始算仓储费' AFTER `auto_outbound`,
  ADD COLUMN IF NOT EXISTS `order_source` varchar(64) DEFAULT NULL COMMENT '手工/OCR/导入/预售' AFTER `storage_fee_start_days`;

ALTER TABLE `erp_trade_order_item`
  ADD COLUMN IF NOT EXISTS `source_order_item_id` bigint(20) DEFAULT NULL AFTER `order_id`,
  ADD COLUMN IF NOT EXISTS `batch_no` varchar(64) DEFAULT NULL AFTER `unit`,
  ADD COLUMN IF NOT EXISTS `source_container_no` varchar(64) DEFAULT NULL AFTER `batch_no`,
  ADD COLUMN IF NOT EXISTS `piece_count` decimal(18,2) DEFAULT 0.00 AFTER `quantity`,
  ADD COLUMN IF NOT EXISTS `actual_piece_count` decimal(18,2) DEFAULT 0.00 AFTER `piece_count`,
  ADD COLUMN IF NOT EXISTS `estimated_weight` decimal(18,2) DEFAULT 0.00 AFTER `actual_piece_count`,
  ADD COLUMN IF NOT EXISTS `actual_in_weight` decimal(18,2) DEFAULT 0.00 AFTER `estimated_weight`,
  ADD COLUMN IF NOT EXISTS `actual_out_weight` decimal(18,2) DEFAULT 0.00 AFTER `actual_in_weight`,
  ADD COLUMN IF NOT EXISTS `loss_weight` decimal(18,2) DEFAULT 0.00 AFTER `actual_out_weight`,
  ADD COLUMN IF NOT EXISTS `shelf_life_days` int(11) DEFAULT 0 AFTER `total_amount`,
  ADD COLUMN IF NOT EXISTS `production_date` datetime DEFAULT NULL AFTER `shelf_life_days`,
  ADD COLUMN IF NOT EXISTS `expiry_date` datetime DEFAULT NULL AFTER `production_date`;

ALTER TABLE `erp_stock_ledger`
  ADD COLUMN IF NOT EXISTS `related_order_no` varchar(64) DEFAULT NULL AFTER `order_no`,
  ADD COLUMN IF NOT EXISTS `biz_type` varchar(64) DEFAULT NULL AFTER `related_order_no`,
  ADD COLUMN IF NOT EXISTS `warehouse_id` bigint(20) DEFAULT NULL AFTER `product_spec`,
  ADD COLUMN IF NOT EXISTS `in_pieces` decimal(18,2) DEFAULT 0.00 AFTER `out_quantity`,
  ADD COLUMN IF NOT EXISTS `out_pieces` decimal(18,2) DEFAULT 0.00 AFTER `in_pieces`,
  ADD COLUMN IF NOT EXISTS `loss_weight` decimal(18,2) DEFAULT 0.00 AFTER `out_pieces`,
  ADD COLUMN IF NOT EXISTS `expiry_date` datetime DEFAULT NULL AFTER `biz_date`;

INSERT INTO `erp_warehouse`
(`warehouse_code`,`warehouse_name`,`warehouse_type`,`owned_by_company`,`free_storage_days`,`daily_storage_fee`,`daily_cold_fee`,`fee_unit`,`status`,`remark`,`create_user_id`,`create_time`,`update_time`)
SELECT 'XM-WX','厦门万翔','PORT_COLD',0,7,0.80,0.50,'PIECE',1,'港口冷链仓',1,NOW(),NOW()
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM erp_warehouse WHERE warehouse_code = 'XM-WX');

INSERT INTO `erp_warehouse`
(`warehouse_code`,`warehouse_name`,`warehouse_type`,`owned_by_company`,`free_storage_days`,`daily_storage_fee`,`daily_cold_fee`,`fee_unit`,`status`,`remark`,`create_user_id`,`create_time`,`update_time`)
SELECT 'SH-WW-WC','上海万纬-万呈','COLD',0,7,1.20,0.80,'PIECE',1,'销售冷链仓',1,NOW(),NOW()
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM erp_warehouse WHERE warehouse_code = 'SH-WW-WC');

INSERT INTO `erp_warehouse`
(`warehouse_code`,`warehouse_name`,`warehouse_type`,`owned_by_company`,`free_storage_days`,`daily_storage_fee`,`daily_cold_fee`,`fee_unit`,`status`,`remark`,`create_user_id`,`create_time`,`update_time`)
SELECT 'TJ-TBD-01','天津待定仓1','COLD',0,5,1.00,0.60,'PIECE',1,'待确认收费标准',1,NOW(),NOW()
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM erp_warehouse WHERE warehouse_code = 'TJ-TBD-01');

INSERT INTO `erp_warehouse`
(`warehouse_code`,`warehouse_name`,`warehouse_type`,`owned_by_company`,`free_storage_days`,`daily_storage_fee`,`daily_cold_fee`,`fee_unit`,`status`,`remark`,`create_user_id`,`create_time`,`update_time`)
SELECT 'TJ-TBD-02','天津待定仓2','NORMAL',0,3,0.60,0.00,'PIECE',1,'待确认收费标准',1,NOW(),NOW()
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM erp_warehouse WHERE warehouse_code = 'TJ-TBD-02');

UPDATE `erp_partner` SET `business_role` = 'BRAND' WHERE `partner_code` = 'SUP-SFF';
UPDATE `erp_partner` SET `business_role` = 'INTERNAL' WHERE `partner_code` = 'CUS-TJXM';
UPDATE `erp_partner` SET `business_role` = 'SECONDARY' WHERE `partner_code` = 'CUS-HNKT';

INSERT INTO `erp_partner`
(`partner_code`,`partner_name`,`partner_type`,`business_role`,`remark`,`status`,`create_user_id`,`create_time`,`update_time`)
SELECT 'FUN-XMWX','厦门万翔物流管理有限公司',3,'FUNDER','客户订单确认函采购方/资方主体',1,1,NOW(),NOW()
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM erp_partner WHERE partner_code = 'FUN-XMWX');

UPDATE `erp_product` SET `alias_codes` = '24314,24314N', `product_name` = '去骨牛前腿眼肉', `product_name_en` = 'Chilled Beef-SYB SYB BOLAR BLADE VP CH - CHINA (PACK LBL)', `unit` = 'KG', `brand` = 'Silver Fern Farms' WHERE `product_code` = '24314';
UPDATE `erp_product` SET `alias_codes` = '24315,24315N', `product_name` = '去骨牛板腱', `product_name_en` = 'Chilled Beef-SYB SYB TOP BLADE MUSCLE VP CH - CHINA (PACK LBL)', `unit` = 'KG', `brand` = 'Silver Fern Farms' WHERE `product_code` = '24315';
UPDATE `erp_product` SET `alias_codes` = '24316,24316N', `product_name` = '去骨牛背肩肉卷', `product_name_en` = 'Chilled Beef-SYB SYB CHUCK ROLL 5KG OV VP CH - CHINA (PACK LBL)', `unit` = 'KG', `brand` = 'Silver Fern Farms' WHERE `product_code` = '24316';
UPDATE `erp_product` SET `alias_codes` = '24317,24317N', `product_name` = '去骨牛肩胛嫩肉（俗称牛辣椒肉）', `product_name_en` = 'Chilled Beef-SYB SYB CHUCK TENDER VP CH - CHINA (PACK LBL)', `unit` = 'KG', `brand` = 'Silver Fern Farms' WHERE `product_code` = '24317';
UPDATE `erp_product` SET `alias_codes` = '24318,24318N', `product_name` = '去骨前胸牛肉', `product_name_en` = 'Chilled Beef-SYB SYB POINT END BRISKET VP CH - CHINA (PACK LBL)', `unit` = 'KG', `brand` = 'Silver Fern Farms' WHERE `product_code` = '24318';
UPDATE `erp_product` SET `alias_codes` = '24319,24319N', `product_name` = '去骨牛小黄瓜条', `product_name_en` = 'Chilled Beef-SYB SYB EYE ROUND VP CH - CHINA Trimmed to Membrane', `unit` = 'KG', `brand` = 'Silver Fern Farms' WHERE `product_code` = '24319';
UPDATE `erp_product` SET `alias_codes` = '24320,24320N', `product_name` = '去骨牛前腿腱肉', `product_name_en` = 'Chilled Beef-SYB SYB SHANK MEAT F2 CONICAL 2VP CH - CHINA (PACK LBL)', `unit` = 'KG', `brand` = 'Silver Fern Farms' WHERE `product_code` = '24320';
UPDATE `erp_product` SET `alias_codes` = '24321,24321N', `product_name` = '去骨牛后腱', `product_name_en` = 'Chilled Beef-SYB SYB HINDSHANK H1 FD 2VP CH - CHINA (PACK LBL)', `unit` = 'KG', `brand` = 'Silver Fern Farms' WHERE `product_code` = '24321';
UPDATE `erp_product` SET `alias_codes` = '24322,24322N', `product_name` = '去骨牛后腱', `product_name_en` = 'Chilled Beef-SYB SYB HINDSHANK H2 CALF 2VP CH - CHINA (PACK LBL)', `unit` = 'KG', `brand` = 'Silver Fern Farms' WHERE `product_code` = '24322';
UPDATE `erp_product` SET `alias_codes` = '24323,24323N', `product_name` = '去骨牛腿内侧头（俗称和尚头）', `product_name_en` = 'Chilled Beef-SYB SYB KNUCKLE VP CH - CHINA (PACK LBL)', `unit` = 'KG', `brand` = 'Silver Fern Farms' WHERE `product_code` = '24323';
UPDATE `erp_product` SET `alias_codes` = '24324,24324N', `product_name` = '去骨牛西冷', `product_name_en` = 'Chilled Beef-SYB SYB STRIPLOIN 4.5-5.5KG VP CH - CHINA (PACK LBL)', `unit` = 'KG', `brand` = 'Silver Fern Farms' WHERE `product_code` = '24324';
UPDATE `erp_product` SET `alias_codes` = '24325,24325N', `product_name` = '去骨牛后腱', `product_name_en` = 'Chilled Beef-SYB SYB Hindshank Muscles VP', `unit` = 'KG', `brand` = 'Silver Fern Farms' WHERE `product_code` = '24325';
UPDATE `erp_product` SET `alias_codes` = '24326,24326N', `product_name` = '去骨去盖牛头刀', `product_name_en` = 'Chilled Beef-SYB SYB Topside Cap Off (trimmed to membrane) VP', `unit` = 'KG', `brand` = 'Silver Fern Farms' WHERE `product_code` = '24326';
UPDATE `erp_product` SET `alias_codes` = '24327,24327N', `product_name` = '去骨牛肋盖肉', `product_name_en` = 'Chilled Beef-SYB SYB Rib Cap meat', `unit` = 'KG', `brand` = 'Silver Fern Farms' WHERE `product_code` = '24327';
UPDATE `erp_product` SET `alias_codes` = '24328,24328N', `product_name` = '去骨后胸牛肉', `product_name_en` = 'Chilled Beef-SYB SYB NE BRISKET VP', `unit` = 'KG', `brand` = 'Silver Fern Farms' WHERE `product_code` = '24328';
UPDATE `erp_product` SET `alias_codes` = '24329,24329N', `product_name` = '去骨肋眼牛肉卷', `product_name_en` = 'Chilled Beef-SYB SYB Rib Eye Roll VP CH China', `unit` = 'KG', `brand` = 'Silver Fern Farms' WHERE `product_code` = '24329';
UPDATE `erp_product` SET `alias_codes` = '24340,24340N', `product_name` = '去骨牛前腱', `product_name_en` = 'Chilled Beef-SYB SYB-FORESHANK MUSCLE VP CH - CHINA', `unit` = 'KG', `brand` = 'Silver Fern Farms' WHERE `product_code` = '24340';
UPDATE `erp_product` SET `alias_codes` = '24343,24343N', `product_name` = '去骨牛肉头刀盖', `product_name_en` = 'Chilled Beef-SYB SYB-INSIDE CAP VP CH - CHINA', `unit` = 'KG', `brand` = 'Silver Fern Farms' WHERE `product_code` = '24343';

UPDATE `erp_product` SET `safe_stock_boxes` = 20, `freshness_warning_days` = 7 WHERE `product_code` = 'SYB-52-CHUCK';

INSERT INTO `sys_menu` (`menu_id`,`parent_id`,`name`,`url`,`perms`,`type`,`icon`,`order_num`)
SELECT 68,40,'仓库管理','erp/warehouse',NULL,1,'fa fa-building-o',7
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 68);

INSERT INTO `sys_menu` (`menu_id`,`parent_id`,`name`,`url`,`perms`,`type`,`icon`,`order_num`)
SELECT 69,68,'查看',NULL,'erp:warehouse:list,erp:warehouse:info',2,NULL,0
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 69);

INSERT INTO `sys_menu` (`menu_id`,`parent_id`,`name`,`url`,`perms`,`type`,`icon`,`order_num`)
SELECT 70,68,'新增',NULL,'erp:warehouse:save',2,NULL,0
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 70);

INSERT INTO `sys_menu` (`menu_id`,`parent_id`,`name`,`url`,`perms`,`type`,`icon`,`order_num`)
SELECT 71,68,'修改',NULL,'erp:warehouse:update',2,NULL,0
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 71);

INSERT INTO `sys_menu` (`menu_id`,`parent_id`,`name`,`url`,`perms`,`type`,`icon`,`order_num`)
SELECT 72,68,'删除',NULL,'erp:warehouse:delete',2,NULL,0
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 72);

INSERT INTO `sys_role_menu` (`role_id`,`menu_id`)
SELECT 1, m.menu_id
FROM sys_menu m
WHERE m.menu_id BETWEEN 68 AND 72
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 1 AND rm.menu_id = m.menu_id
  );

INSERT INTO `sys_menu` (`menu_id`,`parent_id`,`name`,`url`,`perms`,`type`,`icon`,`order_num`)
SELECT 73,40,'预销售单','erp/presale-order',NULL,1,'fa fa-file-text-o',6
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 73);

INSERT INTO `sys_role_menu` (`role_id`,`menu_id`)
SELECT 1, 73
FROM dual
WHERE NOT EXISTS (
  SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 1 AND rm.menu_id = 73
);
