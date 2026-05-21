SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `erp_partner` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `partner_code` varchar(64) DEFAULT NULL,
  `partner_name` varchar(200) DEFAULT NULL,
  `partner_type` int(11) DEFAULT 1 COMMENT '1-供应商 2-客户 3-供应商/客户',
  `business_role` varchar(128) DEFAULT NULL COMMENT 'BRAND/FUNDER/SECONDARY/INTERNAL',
  `tax_no` varchar(100) DEFAULT NULL,
  `bank_name` varchar(200) DEFAULT NULL,
  `bank_account` varchar(100) DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  `contact_name` varchar(64) DEFAULT NULL,
  `contact_phone` varchar(64) DEFAULT NULL,
  `contact_email` varchar(128) DEFAULT NULL,
  `remark` varchar(500) DEFAULT NULL,
  `status` int(11) DEFAULT 1,
  `create_user_id` bigint(20) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_erp_partner_code` (`partner_code`),
  KEY `idx_erp_partner_type` (`partner_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='往来单位';

CREATE TABLE IF NOT EXISTS `erp_product` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `product_code` varchar(64) DEFAULT NULL,
  `product_name` varchar(200) DEFAULT NULL,
  `product_name_en` varchar(255) DEFAULT NULL,
  `product_spec` varchar(200) DEFAULT NULL,
  `unit` varchar(32) DEFAULT NULL,
  `brand` varchar(100) DEFAULT NULL,
  `origin_country` varchar(100) DEFAULT NULL,
  `default_tax_rate` decimal(10,2) DEFAULT 0.00,
  `status` int(11) DEFAULT 1,
  `remark` varchar(500) DEFAULT NULL,
  `create_user_id` bigint(20) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_erp_product_code` (`product_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产品档案';

CREATE TABLE IF NOT EXISTS `erp_product_price` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `product_id` bigint(20) DEFAULT NULL,
  `partner_id` bigint(20) DEFAULT NULL,
  `price_type` int(11) DEFAULT 1 COMMENT '1-采购价 2-销售价',
  `effective_date` datetime DEFAULT NULL,
  `currency` varchar(16) DEFAULT 'CNY',
  `unit_price` decimal(18,4) DEFAULT 0.0000,
  `tax_rate` decimal(10,2) DEFAULT 0.00,
  `remark` varchar(500) DEFAULT NULL,
  `status` int(11) DEFAULT 1,
  `create_user_id` bigint(20) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_erp_product_price_product` (`product_id`),
  KEY `idx_erp_product_price_partner` (`partner_id`),
  KEY `idx_erp_product_price_type_date` (`price_type`,`effective_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产品价格历史';

CREATE TABLE IF NOT EXISTS `erp_trade_order` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_no` varchar(64) DEFAULT NULL,
  `order_type` varchar(32) DEFAULT NULL COMMENT 'PURCHASE-采购 SALE-销售',
  `partner_id` bigint(20) DEFAULT NULL,
  `partner_name` varchar(200) DEFAULT NULL,
  `contract_no` varchar(100) DEFAULT NULL,
  `container_no` varchar(100) DEFAULT NULL,
  `warehouse_name` varchar(128) DEFAULT NULL,
  `order_date` datetime DEFAULT NULL,
  `expected_date` datetime DEFAULT NULL,
  `payment_due_date` datetime DEFAULT NULL,
  `currency` varchar(16) DEFAULT 'CNY',
  `item_amount` decimal(18,2) DEFAULT 0.00,
  `expense_amount` decimal(18,2) DEFAULT 0.00,
  `tax_amount` decimal(18,2) DEFAULT 0.00,
  `total_amount` decimal(18,2) DEFAULT 0.00,
  `status` int(11) DEFAULT 0 COMMENT '0-草稿 1-已完成',
  `remark` varchar(1000) DEFAULT NULL,
  `create_user_id` bigint(20) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_erp_trade_order_no` (`order_no`),
  KEY `idx_erp_trade_order_type_date` (`order_type`,`order_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='进销订单';

CREATE TABLE IF NOT EXISTS `erp_trade_order_item` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_id` bigint(20) DEFAULT NULL,
  `line_no` int(11) DEFAULT NULL,
  `product_id` bigint(20) DEFAULT NULL,
  `product_code` varchar(64) DEFAULT NULL,
  `product_name` varchar(200) DEFAULT NULL,
  `product_spec` varchar(200) DEFAULT NULL,
  `unit` varchar(32) DEFAULT NULL,
  `warehouse_name` varchar(128) DEFAULT NULL,
  `quantity` decimal(18,2) DEFAULT 0.00,
  `unit_price` decimal(18,4) DEFAULT 0.0000,
  `amount` decimal(18,2) DEFAULT 0.00,
  `tax_rate` decimal(10,2) DEFAULT 0.00,
  `tax_amount` decimal(18,2) DEFAULT 0.00,
  `total_amount` decimal(18,2) DEFAULT 0.00,
  `remark` varchar(500) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_erp_trade_order_item_order` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细';

CREATE TABLE IF NOT EXISTS `erp_trade_order_expense` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_id` bigint(20) DEFAULT NULL,
  `expense_type` varchar(64) DEFAULT NULL,
  `expense_name` varchar(128) DEFAULT NULL,
  `amount` decimal(18,2) DEFAULT 0.00,
  `tax_rate` decimal(10,2) DEFAULT 0.00,
  `tax_amount` decimal(18,2) DEFAULT 0.00,
  `total_amount` decimal(18,2) DEFAULT 0.00,
  `remark` varchar(500) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_erp_trade_order_expense_order` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单费用';

CREATE TABLE IF NOT EXISTS `erp_stock_ledger` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_id` bigint(20) DEFAULT NULL,
  `order_item_id` bigint(20) DEFAULT NULL,
  `order_type` varchar(32) DEFAULT NULL,
  `order_no` varchar(64) DEFAULT NULL,
  `product_id` bigint(20) DEFAULT NULL,
  `product_code` varchar(64) DEFAULT NULL,
  `product_name` varchar(200) DEFAULT NULL,
  `product_spec` varchar(200) DEFAULT NULL,
  `warehouse_name` varchar(128) DEFAULT NULL,
  `in_quantity` decimal(18,2) DEFAULT 0.00,
  `out_quantity` decimal(18,2) DEFAULT 0.00,
  `unit_price` decimal(18,4) DEFAULT 0.0000,
  `biz_date` datetime DEFAULT NULL,
  `create_user_id` bigint(20) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_erp_stock_ledger_product` (`product_id`),
  KEY `idx_erp_stock_ledger_order` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存流水';

INSERT INTO `erp_partner`
(`partner_code`,`partner_name`,`partner_type`,`tax_no`,`bank_name`,`bank_account`,`address`,`contact_name`,`contact_phone`,`contact_email`,`remark`,`status`,`create_user_id`,`create_time`,`update_time`)
SELECT 'SUP-SFF','银之蕨食品（上海）有限公司',1,NULL,'中国银行上海市静安支行','445577041417','上海市静安区南京西路1717号会德丰国际广场903室',NULL,NULL,'chinasales@silverfernfarms.co.nz','来源：客户订单确认函',1,1,NOW(),NOW()
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM erp_partner WHERE partner_code = 'SUP-SFF');

INSERT INTO `erp_partner`
(`partner_code`,`partner_name`,`partner_type`,`tax_no`,`bank_name`,`bank_account`,`address`,`contact_name`,`contact_phone`,`contact_email`,`remark`,`status`,`create_user_id`,`create_time`,`update_time`)
SELECT 'CUS-TJXM','天津鲜牧食品科技有限公司',2,'91120118MACD05EP8X','浙商银行武汉分行营业部','5210000010120100590070','天津自贸试验区（东疆综合保税区）亚洲路6975号金融贸易中心南区1-1-1418',NULL,NULL,NULL,'来源：客户订单确认函',1,1,NOW(),NOW()
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM erp_partner WHERE partner_code = 'CUS-TJXM');

INSERT INTO `erp_partner`
(`partner_code`,`partner_name`,`partner_type`,`tax_no`,`bank_name`,`bank_account`,`address`,`contact_name`,`contact_phone`,`contact_email`,`remark`,`status`,`create_user_id`,`create_time`,`update_time`)
SELECT 'SUP-CY','程远进出口贸易（天津）有限公司',1,NULL,'兴业银行天津开发区支行','441110100100508047',NULL,'尹广朋','15075611922',NULL,'来源：销售合同',1,1,NOW(),NOW()
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM erp_partner WHERE partner_code = 'SUP-CY');

INSERT INTO `erp_partner`
(`partner_code`,`partner_name`,`partner_type`,`tax_no`,`bank_name`,`bank_account`,`address`,`contact_name`,`contact_phone`,`contact_email`,`remark`,`status`,`create_user_id`,`create_time`,`update_time`)
SELECT 'CUS-HNKT','河南凯图贸易有限公司',2,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'来源：销售合同',1,1,NOW(),NOW()
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM erp_partner WHERE partner_code = 'CUS-HNKT');

UPDATE `erp_partner` SET `business_role` = 'BRAND' WHERE `partner_code` = 'SUP-SFF';
UPDATE `erp_partner` SET `business_role` = 'INTERNAL' WHERE `partner_code` = 'CUS-TJXM';
UPDATE `erp_partner` SET `business_role` = 'SECONDARY' WHERE `partner_code` = 'CUS-HNKT';

INSERT INTO `erp_product`
(`product_code`,`product_name`,`product_name_en`,`product_spec`,`unit`,`brand`,`origin_country`,`default_tax_rate`,`status`,`remark`,`create_user_id`,`create_time`,`update_time`)
SELECT '24487N-A','去骨牛草饲精备里脊','PS-AGED BONELESS BEEF GRASS FED RESERVE TENDERLOIN','1.8KG OVER','KG','银蕨农场','新西兰',9.00,1,'采购确认函原始编码 24487N',1,NOW(),NOW()
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM erp_product WHERE product_code = '24487N-A');

INSERT INTO `erp_product`
(`product_code`,`product_name`,`product_name_en`,`product_spec`,`unit`,`brand`,`origin_country`,`default_tax_rate`,`status`,`remark`,`create_user_id`,`create_time`,`update_time`)
SELECT '24522N','去骨牛草饲精备后胸肉','PS-AGED BONELESS BEEF GRASS FED RESERVE NAVEL END BRISKET',NULL,'KG','银蕨农场','新西兰',9.00,1,'来源：采购确认函',1,NOW(),NOW()
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM erp_product WHERE product_code = '24522N');

INSERT INTO `erp_product`
(`product_code`,`product_name`,`product_name_en`,`product_spec`,`unit`,`brand`,`origin_country`,`default_tax_rate`,`status`,`remark`,`create_user_id`,`create_time`,`update_time`)
SELECT '24514N','带骨牛草饲精备牛小排','PS-AGED BONE IN BEEF GRASS FED RESERVE SHORT RIB',NULL,'KG','银蕨农场','新西兰',9.00,1,'来源：采购确认函',1,NOW(),NOW()
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM erp_product WHERE product_code = '24514N');

INSERT INTO `erp_product`
(`product_code`,`product_name`,`product_name_en`,`product_spec`,`unit`,`brand`,`origin_country`,`default_tax_rate`,`status`,`remark`,`create_user_id`,`create_time`,`update_time`)
SELECT '24487N-B','去骨牛草饲精备里脊','PS-AGED BONELESS BEEF GRASS FED RESERVE TENDERLOIN','1.8KG OVER（第二批价）','KG','银蕨农场','新西兰',9.00,1,'采购确认函同编码不同价格批次',1,NOW(),NOW()
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM erp_product WHERE product_code = '24487N-B');

INSERT INTO `erp_product`
(`product_code`,`product_name`,`product_name_en`,`product_spec`,`unit`,`brand`,`origin_country`,`default_tax_rate`,`status`,`remark`,`create_user_id`,`create_time`,`update_time`)
SELECT '24489N','去骨牛草饲精备西冷','PS-AGED BONELESS BEEF GRASS FED RESERVE STRIPLOIN','4.5-5.5KG','KG','银蕨农场','新西兰',9.00,1,'来源：采购确认函',1,NOW(),NOW()
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM erp_product WHERE product_code = '24489N');

INSERT INTO `erp_product`
(`product_code`,`product_name`,`product_name_en`,`product_spec`,`unit`,`brand`,`origin_country`,`default_tax_rate`,`status`,`remark`,`create_user_id`,`create_time`,`update_time`)
SELECT '24527N','去骨牛草饲精备前腿眼肉','PS-AGED BONELESS BEEF GRASS FED RESERVE BOLAR BLADE',NULL,'KG','银蕨农场','新西兰',9.00,1,'来源：采购确认函',1,NOW(),NOW()
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM erp_product WHERE product_code = '24527N');

INSERT INTO `erp_product`
(`product_code`,`product_name`,`product_name_en`,`product_spec`,`unit`,`brand`,`origin_country`,`default_tax_rate`,`status`,`remark`,`create_user_id`,`create_time`,`update_time`)
SELECT '24488N','去骨牛草饲精备西冷','PS-AGED BONELESS BEEF GRASS FED RESERVE STRIPLOIN','3-4.5KG','KG','银蕨农场','新西兰',9.00,1,'来源：采购确认函',1,NOW(),NOW()
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM erp_product WHERE product_code = '24488N');

INSERT INTO `erp_product`
(`product_code`,`product_name`,`product_name_en`,`product_spec`,`unit`,`brand`,`origin_country`,`default_tax_rate`,`status`,`remark`,`create_user_id`,`create_time`,`update_time`)
SELECT 'SYB-52-CHUCK','SYB 牛保乐肩（冰鲜）',NULL,'52厂, 冰鲜', '千克','SYB',NULL,9.00,1,'来源：销售合同',1,NOW(),NOW()
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM erp_product WHERE product_code = 'SYB-52-CHUCK');

INSERT INTO `erp_product_price`
(`product_id`,`partner_id`,`price_type`,`effective_date`,`currency`,`unit_price`,`tax_rate`,`remark`,`status`,`create_user_id`,`create_time`,`update_time`)
SELECT p.id, s.id, 1, '2026-03-24 00:00:00', 'CNY', 214.4404, 9.00, '采购确认函含税单价 233.74', 1, 1, NOW(), NOW()
FROM erp_product p, erp_partner s
WHERE p.product_code = '24487N-A' AND s.partner_code = 'SUP-SFF'
  AND NOT EXISTS (SELECT 1 FROM erp_product_price WHERE product_id = p.id AND partner_id = s.id AND price_type = 1 AND effective_date = '2026-03-24 00:00:00');

INSERT INTO `erp_product_price`
(`product_id`,`partner_id`,`price_type`,`effective_date`,`currency`,`unit_price`,`tax_rate`,`remark`,`status`,`create_user_id`,`create_time`,`update_time`)
SELECT p.id, s.id, 1, '2026-03-24 00:00:00', 'CNY', 55.3670, 9.00, '采购确认函含税单价 60.35', 1, 1, NOW(), NOW()
FROM erp_product p, erp_partner s
WHERE p.product_code = '24522N' AND s.partner_code = 'SUP-SFF'
  AND NOT EXISTS (SELECT 1 FROM erp_product_price WHERE product_id = p.id AND partner_id = s.id AND price_type = 1 AND effective_date = '2026-03-24 00:00:00');

INSERT INTO `erp_product_price`
(`product_id`,`partner_id`,`price_type`,`effective_date`,`currency`,`unit_price`,`tax_rate`,`remark`,`status`,`create_user_id`,`create_time`,`update_time`)
SELECT p.id, s.id, 1, '2026-03-24 00:00:00', 'CNY', 78.3486, 9.00, '采购确认函含税单价 85.40', 1, 1, NOW(), NOW()
FROM erp_product p, erp_partner s
WHERE p.product_code = '24514N' AND s.partner_code = 'SUP-SFF'
  AND NOT EXISTS (SELECT 1 FROM erp_product_price WHERE product_id = p.id AND partner_id = s.id AND price_type = 1 AND effective_date = '2026-03-24 00:00:00');

INSERT INTO `erp_product_price`
(`product_id`,`partner_id`,`price_type`,`effective_date`,`currency`,`unit_price`,`tax_rate`,`remark`,`status`,`create_user_id`,`create_time`,`update_time`)
SELECT p.id, s.id, 1, '2026-03-24 00:00:00', 'CNY', 217.1651, 9.00, '采购确认函含税单价 236.71', 1, 1, NOW(), NOW()
FROM erp_product p, erp_partner s
WHERE p.product_code = '24487N-B' AND s.partner_code = 'SUP-SFF'
  AND NOT EXISTS (SELECT 1 FROM erp_product_price WHERE product_id = p.id AND partner_id = s.id AND price_type = 1 AND effective_date = '2026-03-24 00:00:00');

INSERT INTO `erp_product_price`
(`product_id`,`partner_id`,`price_type`,`effective_date`,`currency`,`unit_price`,`tax_rate`,`remark`,`status`,`create_user_id`,`create_time`,`update_time`)
SELECT p.id, s.id, 1, '2026-03-24 00:00:00', 'CNY', 131.9541, 9.00, '采购确认函含税单价 143.83', 1, 1, NOW(), NOW()
FROM erp_product p, erp_partner s
WHERE p.product_code = '24489N' AND s.partner_code = 'SUP-SFF'
  AND NOT EXISTS (SELECT 1 FROM erp_product_price WHERE product_id = p.id AND partner_id = s.id AND price_type = 1 AND effective_date = '2026-03-24 00:00:00');

INSERT INTO `erp_product_price`
(`product_id`,`partner_id`,`price_type`,`effective_date`,`currency`,`unit_price`,`tax_rate`,`remark`,`status`,`create_user_id`,`create_time`,`update_time`)
SELECT p.id, s.id, 1, '2026-03-24 00:00:00', 'CNY', 65.3670, 9.00, '采购确认函含税单价 71.25', 1, 1, NOW(), NOW()
FROM erp_product p, erp_partner s
WHERE p.product_code = '24527N' AND s.partner_code = 'SUP-SFF'
  AND NOT EXISTS (SELECT 1 FROM erp_product_price WHERE product_id = p.id AND partner_id = s.id AND price_type = 1 AND effective_date = '2026-03-24 00:00:00');

INSERT INTO `erp_product_price`
(`product_id`,`partner_id`,`price_type`,`effective_date`,`currency`,`unit_price`,`tax_rate`,`remark`,`status`,`create_user_id`,`create_time`,`update_time`)
SELECT p.id, s.id, 1, '2026-03-24 00:00:00', 'CNY', 217.1651, 9.00, '采购确认函含税单价 236.71', 1, 1, NOW(), NOW()
FROM erp_product p, erp_partner s
WHERE p.product_code = '24488N' AND s.partner_code = 'SUP-SFF'
  AND NOT EXISTS (SELECT 1 FROM erp_product_price WHERE product_id = p.id AND partner_id = s.id AND price_type = 1 AND effective_date = '2026-03-24 00:00:00');

INSERT INTO `erp_product_price`
(`product_id`,`partner_id`,`price_type`,`effective_date`,`currency`,`unit_price`,`tax_rate`,`remark`,`status`,`create_user_id`,`create_time`,`update_time`)
SELECT p.id, c.id, 2, '2026-03-17 00:00:00', 'CNY', 51.3761, 9.00, '销售合同含税单价 56.00', 1, 1, NOW(), NOW()
FROM erp_product p, erp_partner c
WHERE p.product_code = 'SYB-52-CHUCK' AND c.partner_code = 'CUS-HNKT'
  AND NOT EXISTS (SELECT 1 FROM erp_product_price WHERE product_id = p.id AND partner_id = c.id AND price_type = 2 AND effective_date = '2026-03-17 00:00:00');

INSERT INTO `erp_trade_order`
(`order_no`,`order_type`,`partner_id`,`partner_name`,`contract_no`,`container_no`,`warehouse_name`,`order_date`,`expected_date`,`payment_due_date`,`currency`,`item_amount`,`expense_amount`,`tax_amount`,`total_amount`,`status`,`remark`,`create_user_id`,`create_time`,`update_time`)
SELECT 'PO202603240001','PURCHASE',p.id,p.partner_name,'B226783/2 B224518/2','MEDU9226389','采购方指定港口仓库','2026-03-17 00:00:00','2026-03-24 00:00:00','2026-03-23 00:00:00','CNY',2747354.48,5000.00,247261.90,2999616.38,1,'按采购确认函导入的样例采购单；单价按未税价拆分，备注中保留原始含税价格。',1,NOW(),NOW()
FROM erp_partner p
WHERE p.partner_code = 'SUP-SFF'
  AND NOT EXISTS (SELECT 1 FROM erp_trade_order WHERE order_no = 'PO202603240001');

INSERT INTO `erp_trade_order_item`
(`order_id`,`line_no`,`product_id`,`product_code`,`product_name`,`product_spec`,`unit`,`warehouse_name`,`quantity`,`unit_price`,`amount`,`tax_rate`,`tax_amount`,`total_amount`,`remark`,`create_time`,`update_time`)
SELECT o.id,1,p.id,p.product_code,p.product_name,p.product_spec,'KG','采购方指定港口仓库',1200.02,214.4404,257332.77,9.00,23159.95,280492.72,'合同含税单价 233.74',NOW(),NOW()
FROM erp_trade_order o, erp_product p
WHERE o.order_no = 'PO202603240001' AND p.product_code = '24487N-A'
  AND NOT EXISTS (SELECT 1 FROM erp_trade_order_item WHERE order_id = o.id AND line_no = 1);

INSERT INTO `erp_trade_order_item`
(`order_id`,`line_no`,`product_id`,`product_code`,`product_name`,`product_spec`,`unit`,`warehouse_name`,`quantity`,`unit_price`,`amount`,`tax_rate`,`tax_amount`,`total_amount`,`remark`,`create_time`,`update_time`)
SELECT o.id,2,p.id,p.product_code,p.product_name,p.product_spec,'KG','采购方指定港口仓库',5991.26,55.3670,331718.09,9.00,29854.63,361572.72,'合同含税单价 60.35',NOW(),NOW()
FROM erp_trade_order o, erp_product p
WHERE o.order_no = 'PO202603240001' AND p.product_code = '24522N'
  AND NOT EXISTS (SELECT 1 FROM erp_trade_order_item WHERE order_id = o.id AND line_no = 2);

INSERT INTO `erp_trade_order_item`
(`order_id`,`line_no`,`product_id`,`product_code`,`product_name`,`product_spec`,`unit`,`warehouse_name`,`quantity`,`unit_price`,`amount`,`tax_rate`,`tax_amount`,`total_amount`,`remark`,`create_time`,`update_time`)
SELECT o.id,3,p.id,p.product_code,p.product_name,p.product_spec,'KG','采购方指定港口仓库',6302.66,78.3486,493804.59,9.00,44442.41,538247.00,'合同含税单价 85.40',NOW(),NOW()
FROM erp_trade_order o, erp_product p
WHERE o.order_no = 'PO202603240001' AND p.product_code = '24514N'
  AND NOT EXISTS (SELECT 1 FROM erp_trade_order_item WHERE order_id = o.id AND line_no = 3);

INSERT INTO `erp_trade_order_item`
(`order_id`,`line_no`,`product_id`,`product_code`,`product_name`,`product_spec`,`unit`,`warehouse_name`,`quantity`,`unit_price`,`amount`,`tax_rate`,`tax_amount`,`total_amount`,`remark`,`create_time`,`update_time`)
SELECT o.id,4,p.id,p.product_code,p.product_name,p.product_spec,'KG','采购方指定港口仓库',1810.66,217.1651,393212.16,9.00,35389.09,428601.25,'合同含税单价 236.71',NOW(),NOW()
FROM erp_trade_order o, erp_product p
WHERE o.order_no = 'PO202603240001' AND p.product_code = '24487N-B'
  AND NOT EXISTS (SELECT 1 FROM erp_trade_order_item WHERE order_id = o.id AND line_no = 4);

INSERT INTO `erp_trade_order_item`
(`order_id`,`line_no`,`product_id`,`product_code`,`product_name`,`product_spec`,`unit`,`warehouse_name`,`quantity`,`unit_price`,`amount`,`tax_rate`,`tax_amount`,`total_amount`,`remark`,`create_time`,`update_time`)
SELECT o.id,5,p.id,p.product_code,p.product_name,p.product_spec,'KG','采购方指定港口仓库',3863.26,131.9541,509773.00,9.00,45879.57,555652.57,'合同含税单价 143.83',NOW(),NOW()
FROM erp_trade_order o, erp_product p
WHERE o.order_no = 'PO202603240001' AND p.product_code = '24489N'
  AND NOT EXISTS (SELECT 1 FROM erp_trade_order_item WHERE order_id = o.id AND line_no = 5);

INSERT INTO `erp_trade_order_item`
(`order_id`,`line_no`,`product_id`,`product_code`,`product_name`,`product_spec`,`unit`,`warehouse_name`,`quantity`,`unit_price`,`amount`,`tax_rate`,`tax_amount`,`total_amount`,`remark`,`create_time`,`update_time`)
SELECT o.id,6,p.id,p.product_code,p.product_name,p.product_spec,'KG','采购方指定港口仓库',3006.34,65.3670,196515.43,9.00,17686.39,214201.82,'合同含税单价 71.25',NOW(),NOW()
FROM erp_trade_order o, erp_product p
WHERE o.order_no = 'PO202603240001' AND p.product_code = '24527N'
  AND NOT EXISTS (SELECT 1 FROM erp_trade_order_item WHERE order_id = o.id AND line_no = 6);

INSERT INTO `erp_trade_order_item`
(`order_id`,`line_no`,`product_id`,`product_code`,`product_name`,`product_spec`,`unit`,`warehouse_name`,`quantity`,`unit_price`,`amount`,`tax_rate`,`tax_amount`,`total_amount`,`remark`,`create_time`,`update_time`)
SELECT o.id,7,p.id,p.product_code,p.product_name,p.product_spec,'KG','采购方指定港口仓库',2601.70,217.1651,564998.44,9.00,50849.86,615848.30,'合同含税单价 236.71',NOW(),NOW()
FROM erp_trade_order o, erp_product p
WHERE o.order_no = 'PO202603240001' AND p.product_code = '24488N'
  AND NOT EXISTS (SELECT 1 FROM erp_trade_order_item WHERE order_id = o.id AND line_no = 7);

INSERT INTO `erp_trade_order_expense`
(`order_id`,`expense_type`,`expense_name`,`amount`,`tax_rate`,`tax_amount`,`total_amount`,`remark`,`create_time`,`update_time`)
SELECT o.id,'WAREHOUSE','港口杂费',5000.00,0.00,0.00,5000.00,'演示其他费用可单独归集',NOW(),NOW()
FROM erp_trade_order o
WHERE o.order_no = 'PO202603240001'
  AND NOT EXISTS (SELECT 1 FROM erp_trade_order_expense WHERE order_id = o.id AND expense_name = '港口杂费');

INSERT INTO `erp_stock_ledger`
(`order_id`,`order_item_id`,`order_type`,`order_no`,`product_id`,`product_code`,`product_name`,`product_spec`,`warehouse_name`,`in_quantity`,`out_quantity`,`unit_price`,`biz_date`,`create_user_id`,`create_time`)
SELECT o.id,i.id,'PURCHASE',o.order_no,i.product_id,i.product_code,i.product_name,i.product_spec,i.warehouse_name,i.quantity,0.00,i.unit_price,o.order_date,1,NOW()
FROM erp_trade_order o
JOIN erp_trade_order_item i ON i.order_id = o.id
WHERE o.order_no = 'PO202603240001'
  AND NOT EXISTS (SELECT 1 FROM erp_stock_ledger l WHERE l.order_item_id = i.id);

INSERT INTO `erp_trade_order`
(`order_no`,`order_type`,`partner_id`,`partner_name`,`contract_no`,`container_no`,`warehouse_name`,`order_date`,`expected_date`,`payment_due_date`,`currency`,`item_amount`,`expense_amount`,`tax_amount`,`total_amount`,`status`,`remark`,`create_user_id`,`create_time`,`update_time`)
SELECT 'SO202603170001','SALE',p.id,p.partner_name,'CY-2025-11-273 / 283 / 284',NULL,'上海万纬-万星冷库','2026-03-17 00:00:00',NULL,'2026-03-18 00:00:00','CNY',114573.85,300.00,10311.64,125185.49,0,'按销售合同录入的样例销售单，保持草稿状态，不直接扣减库存。',1,NOW(),NOW()
FROM erp_partner p
WHERE p.partner_code = 'CUS-HNKT'
  AND NOT EXISTS (SELECT 1 FROM erp_trade_order WHERE order_no = 'SO202603170001');

INSERT INTO `erp_trade_order_item`
(`order_id`,`line_no`,`product_id`,`product_code`,`product_name`,`product_spec`,`unit`,`warehouse_name`,`quantity`,`unit_price`,`amount`,`tax_rate`,`tax_amount`,`total_amount`,`remark`,`create_time`,`update_time`)
SELECT o.id,1,p.id,p.product_code,p.product_name,p.product_spec,'千克','上海万纬-万星冷库',745.30,51.3761,38290.61,9.00,3446.15,41736.76,'柜号 SUDU1195992，合同含税单价 56.00',NOW(),NOW()
FROM erp_trade_order o, erp_product p
WHERE o.order_no = 'SO202603170001' AND p.product_code = 'SYB-52-CHUCK'
  AND NOT EXISTS (SELECT 1 FROM erp_trade_order_item WHERE order_id = o.id AND line_no = 1);

INSERT INTO `erp_trade_order_item`
(`order_id`,`line_no`,`product_id`,`product_code`,`product_name`,`product_spec`,`unit`,`warehouse_name`,`quantity`,`unit_price`,`amount`,`tax_rate`,`tax_amount`,`total_amount`,`remark`,`create_time`,`update_time`)
SELECT o.id,2,p.id,p.product_code,p.product_name,p.product_spec,'千克','上海万纬-万星冷库',743.84,51.3761,38215.60,9.00,3439.40,41655.00,'柜号 MCRU2063296，合同含税单价 56.00',NOW(),NOW()
FROM erp_trade_order o, erp_product p
WHERE o.order_no = 'SO202603170001' AND p.product_code = 'SYB-52-CHUCK'
  AND NOT EXISTS (SELECT 1 FROM erp_trade_order_item WHERE order_id = o.id AND line_no = 2);

INSERT INTO `erp_trade_order_item`
(`order_id`,`line_no`,`product_id`,`product_code`,`product_name`,`product_spec`,`unit`,`warehouse_name`,`quantity`,`unit_price`,`amount`,`tax_rate`,`tax_amount`,`total_amount`,`remark`,`create_time`,`update_time`)
SELECT o.id,3,p.id,p.product_code,p.product_name,p.product_spec,'千克','上海万纬-万星冷库',740.96,51.3761,38067.64,9.00,3426.09,41493.73,'柜号 SUDU1174114，合同含税单价 56.00',NOW(),NOW()
FROM erp_trade_order o, erp_product p
WHERE o.order_no = 'SO202603170001' AND p.product_code = 'SYB-52-CHUCK'
  AND NOT EXISTS (SELECT 1 FROM erp_trade_order_item WHERE order_id = o.id AND line_no = 3);

INSERT INTO `erp_trade_order_expense`
(`order_id`,`expense_type`,`expense_name`,`amount`,`tax_rate`,`tax_amount`,`total_amount`,`remark`,`create_time`,`update_time`)
SELECT o.id,'STORAGE','冷库费',300.00,0.00,0.00,300.00,'根据合同收费规则预置的其他费用示例',NOW(),NOW()
FROM erp_trade_order o
WHERE o.order_no = 'SO202603170001'
  AND NOT EXISTS (SELECT 1 FROM erp_trade_order_expense WHERE order_id = o.id AND expense_name = '冷库费');

INSERT INTO `sys_menu` (`menu_id`,`parent_id`,`name`,`url`,`perms`,`type`,`icon`,`order_num`)
SELECT 40,0,'进销存',NULL,NULL,0,'fa fa-cubes',2
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 40);

INSERT INTO `sys_menu` (`menu_id`,`parent_id`,`name`,`url`,`perms`,`type`,`icon`,`order_num`)
SELECT 41,40,'往来单位','erp/partner',NULL,1,'fa fa-address-book',1
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 41);

INSERT INTO `sys_menu` (`menu_id`,`parent_id`,`name`,`url`,`perms`,`type`,`icon`,`order_num`)
SELECT 42,40,'产品档案','erp/product',NULL,1,'fa fa-tags',2
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 42);

INSERT INTO `sys_menu` (`menu_id`,`parent_id`,`name`,`url`,`perms`,`type`,`icon`,`order_num`)
SELECT 43,40,'价格历史','erp/product-price',NULL,1,'fa fa-line-chart',3
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 43);

INSERT INTO `sys_menu` (`menu_id`,`parent_id`,`name`,`url`,`perms`,`type`,`icon`,`order_num`)
SELECT 44,40,'采购单','erp/purchase-order',NULL,1,'fa fa-truck',4
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 44);

INSERT INTO `sys_menu` (`menu_id`,`parent_id`,`name`,`url`,`perms`,`type`,`icon`,`order_num`)
SELECT 45,40,'销售单','erp/sales-order',NULL,1,'fa fa-shopping-cart',5
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 45);

INSERT INTO `sys_menu` (`menu_id`,`parent_id`,`name`,`url`,`perms`,`type`,`icon`,`order_num`)
SELECT 73,40,'预销售单','erp/presale-order',NULL,1,'fa fa-file-text-o',6
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 73);

INSERT INTO `sys_menu` (`menu_id`,`parent_id`,`name`,`url`,`perms`,`type`,`icon`,`order_num`)
SELECT 46,40,'库存汇总','erp/inventory',NULL,1,'fa fa-cubes',7
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 46);

INSERT INTO `sys_menu` (`menu_id`,`parent_id`,`name`,`url`,`perms`,`type`,`icon`,`order_num`)
SELECT 47,41,'查看',NULL,'erp:partner:list,erp:partner:info',2,NULL,0
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 47);
INSERT INTO `sys_menu` (`menu_id`,`parent_id`,`name`,`url`,`perms`,`type`,`icon`,`order_num`)
SELECT 48,41,'新增',NULL,'erp:partner:save',2,NULL,0
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 48);
INSERT INTO `sys_menu` (`menu_id`,`parent_id`,`name`,`url`,`perms`,`type`,`icon`,`order_num`)
SELECT 49,41,'修改',NULL,'erp:partner:update',2,NULL,0
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 49);
INSERT INTO `sys_menu` (`menu_id`,`parent_id`,`name`,`url`,`perms`,`type`,`icon`,`order_num`)
SELECT 50,41,'删除',NULL,'erp:partner:delete',2,NULL,0
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 50);

INSERT INTO `sys_menu` (`menu_id`,`parent_id`,`name`,`url`,`perms`,`type`,`icon`,`order_num`)
SELECT 51,42,'查看',NULL,'erp:product:list,erp:product:info',2,NULL,0
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 51);
INSERT INTO `sys_menu` (`menu_id`,`parent_id`,`name`,`url`,`perms`,`type`,`icon`,`order_num`)
SELECT 52,42,'新增',NULL,'erp:product:save',2,NULL,0
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 52);
INSERT INTO `sys_menu` (`menu_id`,`parent_id`,`name`,`url`,`perms`,`type`,`icon`,`order_num`)
SELECT 53,42,'修改',NULL,'erp:product:update',2,NULL,0
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 53);
INSERT INTO `sys_menu` (`menu_id`,`parent_id`,`name`,`url`,`perms`,`type`,`icon`,`order_num`)
SELECT 54,42,'删除',NULL,'erp:product:delete',2,NULL,0
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 54);

INSERT INTO `sys_menu` (`menu_id`,`parent_id`,`name`,`url`,`perms`,`type`,`icon`,`order_num`)
SELECT 55,43,'查看',NULL,'erp:productprice:list,erp:productprice:info',2,NULL,0
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 55);
INSERT INTO `sys_menu` (`menu_id`,`parent_id`,`name`,`url`,`perms`,`type`,`icon`,`order_num`)
SELECT 56,43,'新增',NULL,'erp:productprice:save',2,NULL,0
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 56);
INSERT INTO `sys_menu` (`menu_id`,`parent_id`,`name`,`url`,`perms`,`type`,`icon`,`order_num`)
SELECT 57,43,'修改',NULL,'erp:productprice:update',2,NULL,0
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 57);
INSERT INTO `sys_menu` (`menu_id`,`parent_id`,`name`,`url`,`perms`,`type`,`icon`,`order_num`)
SELECT 58,43,'删除',NULL,'erp:productprice:delete',2,NULL,0
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 58);

INSERT INTO `sys_menu` (`menu_id`,`parent_id`,`name`,`url`,`perms`,`type`,`icon`,`order_num`)
SELECT 59,44,'查看',NULL,'erp:tradeorder:list,erp:tradeorder:info',2,NULL,0
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 59);
INSERT INTO `sys_menu` (`menu_id`,`parent_id`,`name`,`url`,`perms`,`type`,`icon`,`order_num`)
SELECT 60,44,'新增',NULL,'erp:tradeorder:save',2,NULL,0
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 60);
INSERT INTO `sys_menu` (`menu_id`,`parent_id`,`name`,`url`,`perms`,`type`,`icon`,`order_num`)
SELECT 61,44,'修改',NULL,'erp:tradeorder:update',2,NULL,0
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 61);
INSERT INTO `sys_menu` (`menu_id`,`parent_id`,`name`,`url`,`perms`,`type`,`icon`,`order_num`)
SELECT 62,44,'删除',NULL,'erp:tradeorder:delete',2,NULL,0
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 62);

INSERT INTO `sys_menu` (`menu_id`,`parent_id`,`name`,`url`,`perms`,`type`,`icon`,`order_num`)
SELECT 63,45,'查看',NULL,'erp:tradeorder:list,erp:tradeorder:info',2,NULL,0
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 63);
INSERT INTO `sys_menu` (`menu_id`,`parent_id`,`name`,`url`,`perms`,`type`,`icon`,`order_num`)
SELECT 64,45,'新增',NULL,'erp:tradeorder:save',2,NULL,0
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 64);
INSERT INTO `sys_menu` (`menu_id`,`parent_id`,`name`,`url`,`perms`,`type`,`icon`,`order_num`)
SELECT 65,45,'修改',NULL,'erp:tradeorder:update',2,NULL,0
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 65);
INSERT INTO `sys_menu` (`menu_id`,`parent_id`,`name`,`url`,`perms`,`type`,`icon`,`order_num`)
SELECT 66,45,'删除',NULL,'erp:tradeorder:delete',2,NULL,0
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 66);

INSERT INTO `sys_menu` (`menu_id`,`parent_id`,`name`,`url`,`perms`,`type`,`icon`,`order_num`)
SELECT 67,46,'查看',NULL,'erp:inventory:list',2,NULL,0
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 67);

INSERT INTO `sys_role_menu` (`role_id`,`menu_id`)
SELECT 1, m.menu_id
FROM sys_menu m
WHERE (m.menu_id BETWEEN 40 AND 67 OR m.menu_id = 73)
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 1 AND rm.menu_id = m.menu_id
  );
