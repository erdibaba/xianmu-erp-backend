SET NAMES utf8mb4;

ALTER TABLE `erp_inbound_order`
  ADD COLUMN IF NOT EXISTS `warehouse_id` bigint DEFAULT NULL COMMENT '仓库ID' AFTER `customer_name`,
  ADD COLUMN IF NOT EXISTS `warehouse_name` varchar(200) DEFAULT NULL COMMENT '仓库名称' AFTER `warehouse_id`;

ALTER TABLE `erp_inbound_order` COMMENT = '入库单主表';
ALTER TABLE `erp_inbound_order_item` COMMENT = '入库单明细表';
ALTER TABLE `erp_inbound_order_file` COMMENT = '入库单原件归档表';

CREATE TABLE IF NOT EXISTS `erp_sale_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_no` varchar(64) DEFAULT NULL COMMENT '销售单号',
  `sale_type` varchar(32) NOT NULL COMMENT '销售类型：FUTURES期货单/SPOT现货单',
  `secondary_partner_id` bigint DEFAULT NULL COMMENT '二批商ID',
  `secondary_partner_name` varchar(200) DEFAULT NULL COMMENT '二批商名称',
  `warehouse_id` bigint DEFAULT NULL COMMENT '仓库ID',
  `warehouse_name` varchar(200) DEFAULT NULL COMMENT '仓库名称',
  `contract_no` varchar(100) DEFAULT NULL COMMENT '合同号',
  `contract_token` varchar(64) DEFAULT NULL COMMENT '合同公开链接令牌',
  `status` int DEFAULT 1 COMMENT '流程状态：1待回传盖章合同 2待上传二批付款材料 3待上传资方付款凭证 4已完成',
  `remark` varchar(1000) DEFAULT NULL COMMENT '备注',
  `create_user_id` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_erp_sale_order_no` (`order_no`),
  KEY `idx_erp_sale_order_type` (`sale_type`),
  KEY `idx_erp_sale_order_secondary` (`secondary_partner_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售单主表';

CREATE TABLE IF NOT EXISTS `erp_sale_order_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `sale_order_id` bigint NOT NULL COMMENT '销售单ID',
  `line_no` int DEFAULT NULL COMMENT '行号',
  `sale_type` varchar(32) DEFAULT NULL COMMENT '销售类型',
  `product_id` bigint DEFAULT NULL COMMENT '产品ID',
  `product_code` varchar(100) DEFAULT NULL COMMENT '产品编码',
  `product_name` varchar(255) DEFAULT NULL COMMENT '产品中文名称',
  `product_name_en` varchar(500) DEFAULT NULL COMMENT '产品英文名称',
  `product_spec` varchar(100) DEFAULT NULL COMMENT '规格',
  `unit` varchar(50) DEFAULT NULL COMMENT '单位',
  `boxes` int DEFAULT NULL COMMENT '箱数',
  `source_presale_order_id` bigint DEFAULT NULL COMMENT '来源预销售单ID',
  `source_presale_order_no` varchar(100) DEFAULT NULL COMMENT '来源预销售单号',
  `source_presale_order_item_id` bigint DEFAULT NULL COMMENT '来源预销售单明细ID',
  `source_inbound_order_id` bigint DEFAULT NULL COMMENT '来源入库单ID',
  `source_inbound_item_id` bigint DEFAULT NULL COMMENT '来源入库单明细ID',
  `source_container_no` varchar(100) DEFAULT NULL COMMENT '来源柜号/集装箱号',
  `warehouse_id` bigint DEFAULT NULL COMMENT '仓库ID',
  `warehouse_name` varchar(200) DEFAULT NULL COMMENT '仓库名称',
  `brand_id` bigint DEFAULT NULL COMMENT '品牌方ID',
  `brand_name` varchar(200) DEFAULT NULL COMMENT '品牌方名称',
  `inbound_date` datetime DEFAULT NULL COMMENT '入库时间',
  `production_date` datetime DEFAULT NULL COMMENT '生产日期',
  `expiry_date` datetime DEFAULT NULL COMMENT '过期日期',
  `spec_weight` decimal(10,4) DEFAULT NULL COMMENT '规格重量',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_erp_sale_order_item_order` (`sale_order_id`),
  KEY `idx_erp_sale_order_item_product` (`product_id`),
  KEY `idx_erp_sale_order_item_inbound_item` (`source_inbound_item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售单明细表';

CREATE TABLE IF NOT EXISTS `erp_sale_order_file` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `sale_order_id` bigint NOT NULL COMMENT '销售单ID',
  `file_type` varchar(50) DEFAULT NULL COMMENT '文件类型：SIGNED_CONTRACT/BUYER_PAYMENT_PROOF/BUYER_BANK_SLIP/FUNDER_PAYMENT_PROOF',
  `line_no` int DEFAULT NULL COMMENT '同类型文件序号',
  `file_path` varchar(500) DEFAULT NULL COMMENT '文件路径',
  `file_name` varchar(255) DEFAULT NULL COMMENT '文件名称',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_erp_sale_order_file_order` (`sale_order_id`),
  KEY `idx_erp_sale_order_file_type` (`file_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售单附件归档表';
