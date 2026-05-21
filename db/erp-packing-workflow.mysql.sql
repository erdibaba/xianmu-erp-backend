CREATE TABLE IF NOT EXISTS `erp_presale_packing` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `presale_order_id` bigint NOT NULL COMMENT '预销售单ID',
  `contract_no` varchar(100) DEFAULT NULL COMMENT '合同号',
  `container_no` varchar(100) DEFAULT NULL COMMENT '集装箱号',
  `shelf_life_days` int DEFAULT NULL COMMENT '保质期天数',
  `total_boxes` int DEFAULT NULL COMMENT '识别总箱数',
  `total_weight` decimal(18,2) DEFAULT NULL COMMENT '识别总重量(KG)',
  `file_path` varchar(500) DEFAULT NULL COMMENT '原始装箱单文件路径',
  `file_name` varchar(255) DEFAULT NULL COMMENT '原始装箱单文件名',
  `raw_text` longtext COMMENT 'OCR原始文本',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_user_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_presale_order_id` (`presale_order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预销售装箱单主表';

CREATE TABLE IF NOT EXISTS `erp_presale_packing_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `packing_id` bigint NOT NULL COMMENT '装箱单主表ID',
  `line_no` int DEFAULT NULL COMMENT '行号',
  `product_id` bigint DEFAULT NULL COMMENT '产品ID',
  `product_code` varchar(100) DEFAULT NULL COMMENT '系统产品编码',
  `source_product_code` varchar(100) DEFAULT NULL COMMENT '单据原始产品编码',
  `product_name` varchar(255) DEFAULT NULL COMMENT '产品中文名称',
  `product_name_en` varchar(255) DEFAULT NULL COMMENT '产品英文名称',
  `total_boxes` int DEFAULT NULL COMMENT '按产品汇总总箱数',
  `total_weight` decimal(18,2) DEFAULT NULL COMMENT '按产品汇总总重量(KG)',
  `shelf_life_days` int DEFAULT NULL COMMENT '保质期天数',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_packing_id` (`packing_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预销售装箱单产品汇总表';

CREATE TABLE IF NOT EXISTS `erp_presale_packing_batch` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `packing_item_id` bigint NOT NULL COMMENT '装箱单产品汇总ID',
  `line_no` int DEFAULT NULL COMMENT '行号',
  `production_date` datetime DEFAULT NULL COMMENT '生产日期',
  `expiry_date` datetime DEFAULT NULL COMMENT '过期日期',
  `box_count` int DEFAULT NULL COMMENT '箱数',
  `weight` decimal(18,2) DEFAULT NULL COMMENT '重量(KG)',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_packing_item_id` (`packing_item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预销售装箱单产品批次明细表';
