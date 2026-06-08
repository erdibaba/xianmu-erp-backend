-- 销售单出库批次：一个批次可包含多张出库回单和一张二批来款水单

CREATE TABLE IF NOT EXISTS erp_sale_outbound_batch (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  sale_order_id BIGINT NOT NULL COMMENT '销售单ID',
  batch_no VARCHAR(64) NOT NULL COMMENT '出库批次号',
  status TINYINT NOT NULL DEFAULT 0 COMMENT '批次状态：0待上传回单 1待上传水单 2待确认 3已完成 9已作废',
  bank_slip_file_id BIGINT NULL COMMENT '二批来款水单附件ID',
  receipt_count INT NOT NULL DEFAULT 0 COMMENT '出库回单明细行数',
  shipped_total_boxes INT NOT NULL DEFAULT 0 COMMENT '批次发货总箱数',
  shipped_total_weight DECIMAL(18,3) NOT NULL DEFAULT 0.000 COMMENT '批次发货总重量KG',
  remark VARCHAR(500) NULL COMMENT '备注',
  create_user_id BIGINT NULL COMMENT '创建人ID',
  confirm_user_id BIGINT NULL COMMENT '确认人ID',
  confirm_time DATETIME NULL COMMENT '确认时间',
  void_user_id BIGINT NULL COMMENT '作废人ID',
  void_time DATETIME NULL COMMENT '作废时间',
  create_time DATETIME NOT NULL COMMENT '创建时间',
  update_time DATETIME NOT NULL COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_sale_outbound_batch_order (sale_order_id, status),
  UNIQUE KEY uk_sale_outbound_batch_no (batch_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售单出库批次表';

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'erp_sale_order_file' AND column_name = 'batch_id'
);
SET @sql := IF(@col_exists = 0, 'ALTER TABLE erp_sale_order_file ADD COLUMN batch_id BIGINT NULL COMMENT ''出库批次ID'' AFTER sale_order_id', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

ALTER TABLE erp_sale_order_file
  MODIFY COLUMN file_type VARCHAR(50) DEFAULT NULL COMMENT '文件类型：SIGNED_CONTRACT盖章合同/BUYER_PAYMENT_PROOF二批打款凭证/BUYER_BANK_SLIP二批来款水单/FUNDER_PAYMENT_PROOF资方打款凭证/OUTBOUND_RECEIPT出库回单/OUTBOUND_ATTACHMENT出库附件/OUTBOUND_BATCH_BANK_SLIP批次二批来款水单';

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'erp_sale_outbound_receipt' AND column_name = 'batch_id'
);
SET @sql := IF(@col_exists = 0, 'ALTER TABLE erp_sale_outbound_receipt ADD COLUMN batch_id BIGINT NULL COMMENT ''出库批次ID'' AFTER sale_order_id', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'erp_sale_outbound_receipt_item' AND column_name = 'batch_id'
);
SET @sql := IF(@col_exists = 0, 'ALTER TABLE erp_sale_outbound_receipt_item ADD COLUMN batch_id BIGINT NULL COMMENT ''出库批次ID'' AFTER sale_order_id', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_exists := (
  SELECT COUNT(1)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'erp_sale_outbound_receipt'
    AND index_name = 'uk_sale_outbound_receipt_order'
);
SET @sql := IF(@idx_exists > 0, 'ALTER TABLE erp_sale_outbound_receipt DROP INDEX uk_sale_outbound_receipt_order', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_exists := (
  SELECT COUNT(1) FROM information_schema.statistics
  WHERE table_schema = DATABASE() AND table_name = 'erp_sale_order_file' AND index_name = 'idx_sale_order_file_batch'
);
SET @sql := IF(@idx_exists = 0, 'CREATE INDEX idx_sale_order_file_batch ON erp_sale_order_file (sale_order_id, batch_id, file_type)', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_exists := (
  SELECT COUNT(1) FROM information_schema.statistics
  WHERE table_schema = DATABASE() AND table_name = 'erp_sale_outbound_receipt' AND index_name = 'idx_sale_outbound_receipt_batch'
);
SET @sql := IF(@idx_exists = 0, 'CREATE INDEX idx_sale_outbound_receipt_batch ON erp_sale_outbound_receipt (sale_order_id, batch_id)', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_exists := (
  SELECT COUNT(1) FROM information_schema.statistics
  WHERE table_schema = DATABASE() AND table_name = 'erp_sale_outbound_receipt_item' AND index_name = 'idx_sale_outbound_receipt_item_batch'
);
SET @sql := IF(@idx_exists = 0, 'CREATE INDEX idx_sale_outbound_receipt_item_batch ON erp_sale_outbound_receipt_item (sale_order_id, batch_id, receipt_id)', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
