-- Add per-line outbound receipt document fields.
-- A sale order can receive multiple outbound receipts, so document headers must be stored on each item row.

DROP PROCEDURE IF EXISTS add_erp_column_if_missing;
DROP PROCEDURE IF EXISTS add_erp_index_if_missing;

DELIMITER $$
CREATE PROCEDURE add_erp_column_if_missing(
  IN p_table_name VARCHAR(64),
  IN p_column_name VARCHAR(64),
  IN p_column_ddl TEXT
)
BEGIN
  IF NOT EXISTS (
    SELECT 1
      FROM information_schema.columns
     WHERE table_schema = DATABASE()
       AND table_name = p_table_name
       AND column_name = p_column_name
  ) THEN
    SET @erp_column_ddl = p_column_ddl;
    PREPARE erp_column_stmt FROM @erp_column_ddl;
    EXECUTE erp_column_stmt;
    DEALLOCATE PREPARE erp_column_stmt;
  END IF;
END$$

CREATE PROCEDURE add_erp_index_if_missing(
  IN p_table_name VARCHAR(64),
  IN p_index_name VARCHAR(64),
  IN p_index_ddl TEXT
)
BEGIN
  IF NOT EXISTS (
    SELECT 1
      FROM information_schema.statistics
     WHERE table_schema = DATABASE()
       AND table_name = p_table_name
       AND index_name = p_index_name
  ) THEN
    SET @erp_index_ddl = p_index_ddl;
    PREPARE erp_index_stmt FROM @erp_index_ddl;
    EXECUTE erp_index_stmt;
    DEALLOCATE PREPARE erp_index_stmt;
  END IF;
END$$
DELIMITER ;

CALL add_erp_column_if_missing(
  'erp_sale_outbound_receipt_item',
  'wms_order_no',
  'ALTER TABLE erp_sale_outbound_receipt_item ADD COLUMN wms_order_no varchar(100) NULL COMMENT ''WMS单号'' AFTER sale_order_id'
);
CALL add_erp_column_if_missing(
  'erp_sale_outbound_receipt_item',
  'outbound_order_no',
  'ALTER TABLE erp_sale_outbound_receipt_item ADD COLUMN outbound_order_no varchar(100) NULL COMMENT ''订单编号'' AFTER wms_order_no'
);
CALL add_erp_column_if_missing(
  'erp_sale_outbound_receipt_item',
  'customer_code',
  'ALTER TABLE erp_sale_outbound_receipt_item ADD COLUMN customer_code varchar(100) NULL COMMENT ''客户编码'' AFTER outbound_order_no'
);
CALL add_erp_column_if_missing(
  'erp_sale_outbound_receipt_item',
  'customer_name',
  'ALTER TABLE erp_sale_outbound_receipt_item ADD COLUMN customer_name varchar(200) NULL COMMENT ''客户名称'' AFTER customer_code'
);

CALL add_erp_index_if_missing(
  'erp_sale_outbound_receipt_item',
  'idx_out_item_wms_order',
  'ALTER TABLE erp_sale_outbound_receipt_item ADD INDEX idx_out_item_wms_order (wms_order_no)'
);
CALL add_erp_index_if_missing(
  'erp_sale_outbound_receipt_item',
  'idx_out_item_outbound_order',
  'ALTER TABLE erp_sale_outbound_receipt_item ADD INDEX idx_out_item_outbound_order (outbound_order_no)'
);

DROP PROCEDURE IF EXISTS add_erp_column_if_missing;
DROP PROCEDURE IF EXISTS add_erp_index_if_missing;
