-- 销售单出库批次计划：先维护计划货物和司机，再上传出库回单核对实际出库。

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'erp_sale_outbound_batch' AND column_name = 'driver_id'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE erp_sale_outbound_batch ADD COLUMN driver_id bigint DEFAULT NULL COMMENT ''司机档案ID'' AFTER status',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'erp_sale_outbound_batch' AND column_name = 'driver_name'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE erp_sale_outbound_batch ADD COLUMN driver_name varchar(100) DEFAULT NULL COMMENT ''司机姓名快照'' AFTER driver_id',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'erp_sale_outbound_batch' AND column_name = 'plate_no'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE erp_sale_outbound_batch ADD COLUMN plate_no varchar(50) DEFAULT NULL COMMENT ''车牌号快照'' AFTER driver_name',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'erp_sale_outbound_batch' AND column_name = 'driver_mobile'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE erp_sale_outbound_batch ADD COLUMN driver_mobile varchar(50) DEFAULT NULL COMMENT ''司机手机号快照'' AFTER plate_no',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'erp_sale_outbound_batch' AND column_name = 'ownership_name'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE erp_sale_outbound_batch ADD COLUMN ownership_name varchar(200) DEFAULT NULL COMMENT ''本批次唯一货权名称'' AFTER driver_mobile',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

ALTER TABLE `erp_sale_outbound_batch`
  MODIFY COLUMN `driver_id` bigint DEFAULT NULL COMMENT '司机档案ID',
  MODIFY COLUMN `driver_name` varchar(100) DEFAULT NULL COMMENT '司机姓名快照',
  MODIFY COLUMN `plate_no` varchar(50) DEFAULT NULL COMMENT '车牌号快照',
  MODIFY COLUMN `driver_mobile` varchar(50) DEFAULT NULL COMMENT '司机手机号快照',
  MODIFY COLUMN `ownership_name` varchar(200) DEFAULT NULL COMMENT '本批次唯一货权名称';

CREATE TABLE IF NOT EXISTS `erp_sale_outbound_plan_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '出库计划明细ID',
  `batch_id` bigint NOT NULL COMMENT '出库批次ID',
  `sale_order_id` bigint NOT NULL COMMENT '销售单ID',
  `sale_order_item_id` bigint NOT NULL COMMENT '销售单明细ID',
  `line_no` int NOT NULL COMMENT '行号',
  `ownership_name` varchar(200) NOT NULL COMMENT '货权名称快照',
  `product_id` bigint NOT NULL COMMENT '产品ID',
  `product_code` varchar(100) NOT NULL COMMENT '产品编码快照',
  `product_name` varchar(255) DEFAULT NULL COMMENT '产品中文名称快照',
  `product_name_en` varchar(500) DEFAULT NULL COMMENT '产品英文名称快照',
  `container_no` varchar(100) DEFAULT NULL COMMENT '柜号快照',
  `factory_no` varchar(100) DEFAULT NULL COMMENT '厂号快照',
  `planned_boxes` int NOT NULL COMMENT '计划出库箱数',
  `planned_weight` decimal(18,3) NOT NULL DEFAULT 0.000 COMMENT '计划出库重量KG',
  `sale_price_kg` decimal(18,2) NOT NULL DEFAULT 0.00 COMMENT '销售单价元/KG快照',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_outbound_plan_batch_sale_item` (`batch_id`, `sale_order_item_id`),
  KEY `idx_outbound_plan_order_batch` (`sale_order_id`, `batch_id`),
  KEY `idx_outbound_plan_product` (`product_id`, `container_no`, `factory_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售单出库批次计划明细表';

ALTER TABLE `erp_sale_outbound_plan_item`
  COMMENT = '销售单出库批次计划明细表';

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'erp_sale_outbound_receipt_item' AND column_name = 'plan_item_id'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE erp_sale_outbound_receipt_item ADD COLUMN plan_item_id bigint DEFAULT NULL COMMENT ''匹配的出库计划明细ID'' AFTER batch_id',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

ALTER TABLE `erp_sale_outbound_receipt_item`
  MODIFY COLUMN `plan_item_id` bigint DEFAULT NULL COMMENT '匹配的出库计划明细ID';

SET @idx_exists := (
  SELECT COUNT(1)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'erp_sale_outbound_receipt_item'
    AND index_name = 'idx_outbound_receipt_plan_item'
);
SET @sql := IF(
  @idx_exists = 0,
  'CREATE INDEX idx_outbound_receipt_plan_item ON erp_sale_outbound_receipt_item (batch_id, plan_item_id)',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
