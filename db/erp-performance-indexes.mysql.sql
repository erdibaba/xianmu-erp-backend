-- ERP performance indexes.
-- This script is idempotent: existing indexes are skipped.

DROP PROCEDURE IF EXISTS add_erp_index_if_missing;

DELIMITER $$
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

-- Master data menus: partner, product, warehouse.
CALL add_erp_index_if_missing('erp_partner', 'idx_partner_status_name', 'ALTER TABLE erp_partner ADD INDEX idx_partner_status_name (status, partner_name)');
CALL add_erp_index_if_missing('erp_partner', 'idx_partner_role_status', 'ALTER TABLE erp_partner ADD INDEX idx_partner_role_status (business_role, status)');
CALL add_erp_index_if_missing('erp_product', 'idx_product_status_code', 'ALTER TABLE erp_product ADD INDEX idx_product_status_code (status, product_code)');
CALL add_erp_index_if_missing('erp_product', 'idx_product_alias_codes', 'ALTER TABLE erp_product ADD INDEX idx_product_alias_codes (alias_codes)');
CALL add_erp_index_if_missing('erp_product', 'idx_product_name', 'ALTER TABLE erp_product ADD INDEX idx_product_name (product_name)');
CALL add_erp_index_if_missing('erp_product', 'idx_product_name_en', 'ALTER TABLE erp_product ADD INDEX idx_product_name_en (product_name_en)');
CALL add_erp_index_if_missing('erp_warehouse', 'idx_warehouse_status_name', 'ALTER TABLE erp_warehouse ADD INDEX idx_warehouse_status_name (status, warehouse_name)');

-- Presale order, confirmation and packing list workflow.
CALL add_erp_index_if_missing('erp_presale_order', 'idx_presale_date_id', 'ALTER TABLE erp_presale_order ADD INDEX idx_presale_date_id (order_date, id)');
CALL add_erp_index_if_missing('erp_presale_order', 'idx_presale_contract', 'ALTER TABLE erp_presale_order ADD INDEX idx_presale_contract (seller_contract_no)');
CALL add_erp_index_if_missing('erp_presale_order', 'idx_presale_customer_date', 'ALTER TABLE erp_presale_order ADD INDEX idx_presale_customer_date (customer_partner_id, order_date)');
CALL add_erp_index_if_missing('erp_presale_order', 'idx_presale_brand_date', 'ALTER TABLE erp_presale_order ADD INDEX idx_presale_brand_date (brand_id, order_date)');
CALL add_erp_index_if_missing('erp_presale_order_item', 'idx_presale_item_product_order', 'ALTER TABLE erp_presale_order_item ADD INDEX idx_presale_item_product_order (product_id, presale_order_id)');
CALL add_erp_index_if_missing('erp_presale_order_item', 'idx_presale_item_code', 'ALTER TABLE erp_presale_order_item ADD INDEX idx_presale_item_code (product_code)');
CALL add_erp_index_if_missing('erp_presale_confirm', 'idx_confirm_contract', 'ALTER TABLE erp_presale_confirm ADD INDEX idx_confirm_contract (contract_no)');
CALL add_erp_index_if_missing('erp_presale_confirm', 'idx_confirm_expected', 'ALTER TABLE erp_presale_confirm ADD INDEX idx_confirm_expected (expected_arrival_date)');
CALL add_erp_index_if_missing('erp_presale_confirm', 'idx_confirm_buyer', 'ALTER TABLE erp_presale_confirm ADD INDEX idx_confirm_buyer (buyer_partner_id)');
CALL add_erp_index_if_missing('erp_presale_confirm_item', 'idx_confirm_item_product', 'ALTER TABLE erp_presale_confirm_item ADD INDEX idx_confirm_item_product (product_id, confirm_id)');
CALL add_erp_index_if_missing('erp_presale_confirm_item', 'idx_confirm_item_code', 'ALTER TABLE erp_presale_confirm_item ADD INDEX idx_confirm_item_code (product_code)');
CALL add_erp_index_if_missing('erp_presale_packing', 'idx_packing_order_container', 'ALTER TABLE erp_presale_packing ADD INDEX idx_packing_order_container (presale_order_id, container_no)');
CALL add_erp_index_if_missing('erp_presale_packing', 'idx_packing_contract', 'ALTER TABLE erp_presale_packing ADD INDEX idx_packing_contract (contract_no)');
CALL add_erp_index_if_missing('erp_presale_packing', 'idx_packing_container', 'ALTER TABLE erp_presale_packing ADD INDEX idx_packing_container (container_no)');
CALL add_erp_index_if_missing('erp_presale_packing_item', 'idx_pack_item_pack_product', 'ALTER TABLE erp_presale_packing_item ADD INDEX idx_pack_item_pack_product (packing_id, product_id)');
CALL add_erp_index_if_missing('erp_presale_packing_item', 'idx_pack_item_product_pack', 'ALTER TABLE erp_presale_packing_item ADD INDEX idx_pack_item_product_pack (product_id, packing_id)');
CALL add_erp_index_if_missing('erp_presale_packing_item', 'idx_pack_item_code', 'ALTER TABLE erp_presale_packing_item ADD INDEX idx_pack_item_code (product_code)');
CALL add_erp_index_if_missing('erp_presale_packing_batch', 'idx_pack_batch_item_dates', 'ALTER TABLE erp_presale_packing_batch ADD INDEX idx_pack_batch_item_dates (packing_item_id, expiry_date, production_date, line_no, id)');

-- Inbound workflow and inbound detail lookup.
CALL add_erp_index_if_missing('erp_inbound_order', 'idx_inbound_contract', 'ALTER TABLE erp_inbound_order ADD INDEX idx_inbound_contract (contract_no)');
CALL add_erp_index_if_missing('erp_inbound_order', 'idx_inbound_container', 'ALTER TABLE erp_inbound_order ADD INDEX idx_inbound_container (container_no)');
CALL add_erp_index_if_missing('erp_inbound_order', 'idx_inbound_warehouse_date', 'ALTER TABLE erp_inbound_order ADD INDEX idx_inbound_warehouse_date (warehouse_id, order_date)');
CALL add_erp_index_if_missing('erp_inbound_order', 'idx_inbound_date_id', 'ALTER TABLE erp_inbound_order ADD INDEX idx_inbound_date_id (order_date, id)');
CALL add_erp_index_if_missing('erp_inbound_order_item', 'idx_inbound_item_product_order', 'ALTER TABLE erp_inbound_order_item ADD INDEX idx_inbound_item_product_order (product_id, inbound_order_id)');
CALL add_erp_index_if_missing('erp_inbound_order_item', 'idx_inbound_item_product_expiry', 'ALTER TABLE erp_inbound_order_item ADD INDEX idx_inbound_item_product_expiry (product_id, expiry_date)');
CALL add_erp_index_if_missing('erp_inbound_order_item', 'idx_inbound_item_code', 'ALTER TABLE erp_inbound_order_item ADD INDEX idx_inbound_item_code (product_code)');
CALL add_erp_index_if_missing('erp_inbound_order_item', 'idx_inbound_item_order_line', 'ALTER TABLE erp_inbound_order_item ADD INDEX idx_inbound_item_order_line (inbound_order_id, line_no, id)');

-- Sales order, portal upload and outbound receipt workflow.
CALL add_erp_index_if_missing('erp_sale_order', 'idx_sale_type_status_time', 'ALTER TABLE erp_sale_order ADD INDEX idx_sale_type_status_time (sale_type, status, create_time)');
CALL add_erp_index_if_missing('erp_sale_order', 'idx_sale_presale_type', 'ALTER TABLE erp_sale_order ADD INDEX idx_sale_presale_type (source_presale_order_id, sale_type)');
CALL add_erp_index_if_missing('erp_sale_order', 'idx_sale_contract', 'ALTER TABLE erp_sale_order ADD INDEX idx_sale_contract (contract_no)');
CALL add_erp_index_if_missing('erp_sale_order', 'idx_sale_contract_token', 'ALTER TABLE erp_sale_order ADD INDEX idx_sale_contract_token (contract_token)');
CALL add_erp_index_if_missing('erp_sale_order_item', 'idx_sale_item_spot_inbound', 'ALTER TABLE erp_sale_order_item ADD INDEX idx_sale_item_spot_inbound (sale_type, source_inbound_item_id)');
CALL add_erp_index_if_missing('erp_sale_order_item', 'idx_sale_item_future_presale_product', 'ALTER TABLE erp_sale_order_item ADD INDEX idx_sale_item_future_presale_product (sale_type, source_presale_order_id, product_id)');
CALL add_erp_index_if_missing('erp_sale_order_item', 'idx_sale_item_order_product_ctn', 'ALTER TABLE erp_sale_order_item ADD INDEX idx_sale_item_order_product_ctn (sale_order_id, product_id, source_container_no)');
CALL add_erp_index_if_missing('erp_sale_order_item', 'idx_sale_item_product_expiry', 'ALTER TABLE erp_sale_order_item ADD INDEX idx_sale_item_product_expiry (product_id, expiry_date)');
CALL add_erp_index_if_missing('erp_sale_order_item', 'idx_sale_item_presale_item', 'ALTER TABLE erp_sale_order_item ADD INDEX idx_sale_item_presale_item (source_presale_order_item_id)');
CALL add_erp_index_if_missing('erp_sale_order_file', 'idx_sale_file_order_type', 'ALTER TABLE erp_sale_order_file ADD INDEX idx_sale_file_order_type (sale_order_id, file_type)');
CALL add_erp_index_if_missing('erp_sale_outbound_receipt_item', 'idx_out_item_order_product_ctn', 'ALTER TABLE erp_sale_outbound_receipt_item ADD INDEX idx_out_item_order_product_ctn (sale_order_id, product_id, container_no)');
CALL add_erp_index_if_missing('erp_sale_outbound_receipt_item', 'idx_out_item_product_order', 'ALTER TABLE erp_sale_outbound_receipt_item ADD INDEX idx_out_item_product_order (product_id, sale_order_id)');

-- WeCom ship notice and sale upload notice.
CALL add_erp_index_if_missing('erp_ship_notice', 'idx_ship_notice_lookup', 'ALTER TABLE erp_ship_notice ADD INDEX idx_ship_notice_lookup (presale_order_id, partner_id, expected_arrival_date)');
CALL add_erp_index_if_missing('erp_ship_notice', 'idx_ship_notice_status_time', 'ALTER TABLE erp_ship_notice ADD INDEX idx_ship_notice_status_time (status, create_time)');
CALL add_erp_index_if_missing('erp_sale_upload_notice', 'idx_upload_notice_status_time', 'ALTER TABLE erp_sale_upload_notice ADD INDEX idx_upload_notice_status_time (status, create_time)');
CALL add_erp_index_if_missing('erp_sale_upload_notice', 'idx_upload_notice_order_status', 'ALTER TABLE erp_sale_upload_notice ADD INDEX idx_upload_notice_order_status (sale_order_id, status)');

-- Inventory and stock ledger.
CALL add_erp_index_if_missing('erp_stock_ledger', 'idx_stock_summary', 'ALTER TABLE erp_stock_ledger ADD INDEX idx_stock_summary (product_id, warehouse_id, expiry_date)');
CALL add_erp_index_if_missing('erp_stock_ledger', 'idx_stock_biz_date', 'ALTER TABLE erp_stock_ledger ADD INDEX idx_stock_biz_date (biz_date)');
CALL add_erp_index_if_missing('erp_stock_ledger', 'idx_stock_order_item', 'ALTER TABLE erp_stock_ledger ADD INDEX idx_stock_order_item (order_item_id)');

-- Legacy trade/order processing menus.
CALL add_erp_index_if_missing('erp_trade_order', 'idx_trade_type_status_date', 'ALTER TABLE erp_trade_order ADD INDEX idx_trade_type_status_date (order_type, status, order_date)');
CALL add_erp_index_if_missing('erp_trade_order', 'idx_trade_partner_date', 'ALTER TABLE erp_trade_order ADD INDEX idx_trade_partner_date (partner_id, order_date)');
CALL add_erp_index_if_missing('erp_trade_order', 'idx_trade_contract', 'ALTER TABLE erp_trade_order ADD INDEX idx_trade_contract (contract_no)');
CALL add_erp_index_if_missing('erp_trade_order', 'idx_trade_container', 'ALTER TABLE erp_trade_order ADD INDEX idx_trade_container (container_no)');
CALL add_erp_index_if_missing('erp_trade_order_item', 'idx_trade_item_product_order', 'ALTER TABLE erp_trade_order_item ADD INDEX idx_trade_item_product_order (product_id, order_id)');
CALL add_erp_index_if_missing('erp_trade_order_item', 'idx_trade_item_expiry', 'ALTER TABLE erp_trade_order_item ADD INDEX idx_trade_item_expiry (expiry_date)');
CALL add_erp_index_if_missing('erp_trade_order_item', 'idx_trade_item_container', 'ALTER TABLE erp_trade_order_item ADD INDEX idx_trade_item_container (source_container_no)');
CALL add_erp_index_if_missing('erp_trade_order_expense', 'idx_trade_expense_order_type', 'ALTER TABLE erp_trade_order_expense ADD INDEX idx_trade_expense_order_type (order_id, expense_type)');

-- WeCom group list.
CALL add_erp_index_if_missing('erp_wecom_group', 'idx_wecom_group_status_sync', 'ALTER TABLE erp_wecom_group ADD INDEX idx_wecom_group_status_sync (group_status, sync_time)');

DROP PROCEDURE IF EXISTS add_erp_index_if_missing;
