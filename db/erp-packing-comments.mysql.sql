ALTER TABLE `erp_presale_packing`
  COMMENT = '预销售装箱单主表',
  MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  MODIFY COLUMN `presale_order_id` bigint NOT NULL COMMENT '预销售单ID',
  MODIFY COLUMN `contract_no` varchar(100) DEFAULT NULL COMMENT '合同号',
  MODIFY COLUMN `container_no` varchar(100) DEFAULT NULL COMMENT '集装箱号',
  MODIFY COLUMN `shelf_life_days` int DEFAULT NULL COMMENT '保质期天数',
  MODIFY COLUMN `total_boxes` int DEFAULT NULL COMMENT '识别总箱数',
  MODIFY COLUMN `total_weight` decimal(18,2) DEFAULT NULL COMMENT '识别总重量(KG)',
  MODIFY COLUMN `file_path` varchar(500) DEFAULT NULL COMMENT '原始装箱单文件路径',
  MODIFY COLUMN `file_name` varchar(255) DEFAULT NULL COMMENT '原始装箱单文件名',
  MODIFY COLUMN `raw_text` longtext COMMENT 'OCR原始文本',
  MODIFY COLUMN `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  MODIFY COLUMN `create_user_id` bigint DEFAULT NULL COMMENT '创建人ID',
  MODIFY COLUMN `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  MODIFY COLUMN `update_time` datetime DEFAULT NULL COMMENT '更新时间';

ALTER TABLE `erp_presale_packing_item`
  COMMENT = '预销售装箱单产品汇总表',
  MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  MODIFY COLUMN `packing_id` bigint NOT NULL COMMENT '装箱单主表ID',
  MODIFY COLUMN `line_no` int DEFAULT NULL COMMENT '行号',
  MODIFY COLUMN `product_id` bigint DEFAULT NULL COMMENT '产品ID',
  MODIFY COLUMN `product_code` varchar(100) DEFAULT NULL COMMENT '系统产品编码',
  MODIFY COLUMN `source_product_code` varchar(100) DEFAULT NULL COMMENT '单据原始产品编码',
  MODIFY COLUMN `product_name` varchar(255) DEFAULT NULL COMMENT '产品中文名称',
  MODIFY COLUMN `product_name_en` varchar(255) DEFAULT NULL COMMENT '产品英文名称',
  MODIFY COLUMN `total_boxes` int DEFAULT NULL COMMENT '按产品汇总总箱数',
  MODIFY COLUMN `total_weight` decimal(18,2) DEFAULT NULL COMMENT '按产品汇总总重量(KG)',
  MODIFY COLUMN `shelf_life_days` int DEFAULT NULL COMMENT '保质期天数',
  MODIFY COLUMN `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  MODIFY COLUMN `update_time` datetime DEFAULT NULL COMMENT '更新时间';

ALTER TABLE `erp_presale_packing_batch`
  COMMENT = '预销售装箱单产品批次明细表',
  MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  MODIFY COLUMN `packing_item_id` bigint NOT NULL COMMENT '装箱单产品汇总ID',
  MODIFY COLUMN `line_no` int DEFAULT NULL COMMENT '行号',
  MODIFY COLUMN `production_date` datetime DEFAULT NULL COMMENT '生产日期',
  MODIFY COLUMN `expiry_date` datetime DEFAULT NULL COMMENT '过期日期',
  MODIFY COLUMN `box_count` int DEFAULT NULL COMMENT '箱数',
  MODIFY COLUMN `weight` decimal(18,2) DEFAULT NULL COMMENT '重量(KG)',
  MODIFY COLUMN `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  MODIFY COLUMN `update_time` datetime DEFAULT NULL COMMENT '更新时间';
