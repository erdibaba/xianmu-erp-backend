-- 销售单出库回单OCR与出库确认
ALTER TABLE erp_sale_order
  ADD COLUMN outbound_receipt_confirmed TINYINT(1) NOT NULL DEFAULT 0 COMMENT '出库回单确认状态：0未确认 1已确认' AFTER presale_link_confirmed;

ALTER TABLE erp_sale_order_file
  MODIFY COLUMN file_type VARCHAR(50) DEFAULT NULL COMMENT '文件类型：SIGNED_CONTRACT盖章合同/BUYER_PAYMENT_PROOF二批打款凭证/BUYER_BANK_SLIP二批来款水单/FUNDER_PAYMENT_PROOF资方打款凭证/OUTBOUND_RECEIPT出库回单/OUTBOUND_ATTACHMENT出库附件';

CREATE TABLE IF NOT EXISTS erp_sale_outbound_receipt (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  sale_order_id BIGINT NOT NULL COMMENT '销售单ID',
  wms_order_no VARCHAR(100) DEFAULT NULL COMMENT 'WMS单号',
  outbound_order_no VARCHAR(100) DEFAULT NULL COMMENT '出库订单编号',
  customer_code VARCHAR(100) DEFAULT NULL COMMENT '客户编码',
  customer_name VARCHAR(200) DEFAULT NULL COMMENT '客户名称',
  sale_total_boxes INT NOT NULL DEFAULT 0 COMMENT '销售单总箱数',
  shipped_total_boxes INT NOT NULL DEFAULT 0 COMMENT '出库回单发货总箱数',
  matched TINYINT(1) NOT NULL DEFAULT 0 COMMENT '箱数校验状态：0不匹配 1匹配',
  match_message VARCHAR(500) DEFAULT NULL COMMENT '箱数校验提示',
  raw_text MEDIUMTEXT DEFAULT NULL COMMENT 'OCR原始文本',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_sale_outbound_receipt_order (sale_order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售单出库回单OCR主表';

CREATE TABLE IF NOT EXISTS erp_sale_outbound_receipt_item (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  receipt_id BIGINT NOT NULL COMMENT '出库回单ID',
  sale_order_id BIGINT NOT NULL COMMENT '销售单ID',
  line_no INT DEFAULT NULL COMMENT '行号',
  product_id BIGINT DEFAULT NULL COMMENT '系统产品ID',
  product_code VARCHAR(100) DEFAULT NULL COMMENT '系统产品编码',
  recognized_product_code VARCHAR(100) DEFAULT NULL COMMENT '识别产品编码',
  product_name VARCHAR(255) DEFAULT NULL COMMENT '产品中文名称',
  product_name_en VARCHAR(500) DEFAULT NULL COMMENT '产品英文名称',
  product_spec VARCHAR(100) DEFAULT NULL COMMENT '规格',
  unit VARCHAR(50) DEFAULT NULL COMMENT '单位',
  order_qty INT DEFAULT NULL COMMENT '订单数',
  shipped_qty INT DEFAULT NULL COMMENT '发货数',
  container_no VARCHAR(100) DEFAULT NULL COMMENT '柜号',
  factory_no VARCHAR(100) DEFAULT NULL COMMENT '厂号',
  avg_weight DECIMAL(10,4) DEFAULT NULL COMMENT '均重',
  total_weight DECIMAL(12,3) DEFAULT NULL COMMENT '合计重量',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_outbound_receipt_item_receipt (receipt_id),
  KEY idx_outbound_receipt_item_order (sale_order_id),
  KEY idx_outbound_receipt_item_product (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售单出库回单OCR明细表';
