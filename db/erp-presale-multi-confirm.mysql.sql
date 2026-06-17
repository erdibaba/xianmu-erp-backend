-- 预售单大合同支持多张客户订单确认函小合同。
-- 执行前请先备份数据库；本脚本按当前线上库已存在 ERP 表结构编写。

ALTER TABLE erp_presale_order
  ADD COLUMN deposit_rate DECIMAL(10,2) DEFAULT 30.00 COMMENT '定金比例，默认30%' AFTER remarks,
  ADD COLUMN deposit_reference_amount DECIMAL(18,2) DEFAULT 0.00 COMMENT '定金参考金额=确认函总金额汇总*定金比例' AFTER deposit_rate,
  ADD COLUMN deposit_recognized_amount DECIMAL(18,2) DEFAULT 0.00 COMMENT '定金付款凭证识别金额' AFTER deposit_reference_amount,
  ADD COLUMN deposit_modified_amount DECIMAL(18,2) DEFAULT 0.00 COMMENT '定金付款确认金额' AFTER deposit_recognized_amount,
  ADD COLUMN deposit_payment_date DATE COMMENT '定金付款日期' AFTER deposit_modified_amount,
  ADD COLUMN deposit_file_path VARCHAR(500) COMMENT '定金付款凭证归档路径' AFTER deposit_payment_date,
  ADD COLUMN deposit_file_name VARCHAR(255) COMMENT '定金付款凭证文件名' AFTER deposit_file_path,
  ADD COLUMN deposit_raw_text LONGTEXT COMMENT '定金付款凭证OCR原文' AFTER deposit_file_name,
  ADD COLUMN deposit_refund_recognized_amount DECIMAL(18,2) DEFAULT 0.00 COMMENT '定金退款凭证识别金额' AFTER deposit_raw_text,
  ADD COLUMN deposit_refund_modified_amount DECIMAL(18,2) DEFAULT 0.00 COMMENT '定金退款确认金额' AFTER deposit_refund_recognized_amount,
  ADD COLUMN deposit_refund_date DATE COMMENT '定金退款日期' AFTER deposit_refund_modified_amount,
  ADD COLUMN deposit_refund_file_path VARCHAR(500) COMMENT '定金退款凭证归档路径' AFTER deposit_refund_date,
  ADD COLUMN deposit_refund_file_name VARCHAR(255) COMMENT '定金退款凭证文件名' AFTER deposit_refund_file_path,
  ADD COLUMN deposit_refund_raw_text LONGTEXT COMMENT '定金退款凭证OCR原文' AFTER deposit_refund_file_name,
  ADD COLUMN deposit_status TINYINT DEFAULT 0 COMMENT '定金状态：0待付定金，1已付定金，2可退定金，3已退定金，4退款异常' AFTER deposit_refund_raw_text;

ALTER TABLE erp_presale_packing
  ADD COLUMN confirm_id BIGINT COMMENT '关联客户订单确认函ID（小合同）' AFTER presale_order_id,
  ADD COLUMN confirm_contract_no VARCHAR(100) COMMENT '关联确认函合同号（小合同）' AFTER confirm_id;

ALTER TABLE erp_presale_attachment
  ADD COLUMN confirm_id BIGINT COMMENT '关联客户订单确认函ID（小合同）' AFTER presale_order_id;

ALTER TABLE erp_inbound_order
  ADD COLUMN confirm_id BIGINT COMMENT '关联客户订单确认函ID（小合同）' AFTER presale_order_id;

ALTER TABLE erp_funder_payment_allocation
  ADD COLUMN confirm_id BIGINT COMMENT '关联客户订单确认函ID（小合同）' AFTER presale_order_id,
  ADD COLUMN confirm_contract_no VARCHAR(100) COMMENT '确认函合同号（小合同）' AFTER confirm_id;

ALTER TABLE erp_funder_loan
  ADD COLUMN confirm_id BIGINT COMMENT '关联客户订单确认函ID（小合同）' AFTER presale_order_id,
  ADD COLUMN confirm_contract_no VARCHAR(100) COMMENT '确认函合同号（小合同）' AFTER confirm_id;

UPDATE erp_presale_packing pp
JOIN (
  SELECT presale_order_id, MIN(id) AS confirm_id
  FROM erp_presale_confirm
  GROUP BY presale_order_id
) pc ON pc.presale_order_id = pp.presale_order_id
SET pp.confirm_id = COALESCE(pp.confirm_id, pc.confirm_id)
WHERE pp.confirm_id IS NULL;

UPDATE erp_presale_packing pp
JOIN erp_presale_confirm pc ON pc.id = pp.confirm_id
SET pp.confirm_contract_no = COALESCE(pp.confirm_contract_no, pc.contract_no)
WHERE pp.confirm_id IS NOT NULL;

UPDATE erp_presale_attachment pa
JOIN (
  SELECT presale_order_id, MIN(id) AS confirm_id
  FROM erp_presale_confirm
  GROUP BY presale_order_id
) pc ON pc.presale_order_id = pa.presale_order_id
SET pa.confirm_id = COALESCE(pa.confirm_id, pc.confirm_id)
WHERE pa.confirm_id IS NULL;

UPDATE erp_inbound_order io
JOIN erp_presale_confirm pc ON pc.presale_order_id = io.presale_order_id
  AND (pc.contract_no = io.contract_no OR pc.container_no = io.container_no)
SET io.confirm_id = COALESCE(io.confirm_id, pc.id)
WHERE io.confirm_id IS NULL;

UPDATE erp_inbound_order io
JOIN (
  SELECT presale_order_id, MIN(id) AS confirm_id
  FROM erp_presale_confirm
  GROUP BY presale_order_id
) pc ON pc.presale_order_id = io.presale_order_id
SET io.confirm_id = COALESCE(io.confirm_id, pc.confirm_id)
WHERE io.confirm_id IS NULL;

UPDATE erp_funder_payment_allocation a
JOIN erp_presale_confirm pc ON pc.presale_order_id = a.presale_order_id
  AND pc.contract_no = a.seller_contract_no
SET a.confirm_id = COALESCE(a.confirm_id, pc.id),
    a.confirm_contract_no = COALESCE(a.confirm_contract_no, pc.contract_no)
WHERE a.confirm_id IS NULL;

UPDATE erp_funder_payment_allocation a
JOIN (
  SELECT presale_order_id, MIN(id) AS confirm_id
  FROM erp_presale_confirm
  GROUP BY presale_order_id
) pc0 ON pc0.presale_order_id = a.presale_order_id
JOIN erp_presale_confirm pc ON pc.id = pc0.confirm_id
SET a.confirm_id = COALESCE(a.confirm_id, pc.id),
    a.confirm_contract_no = COALESCE(a.confirm_contract_no, pc.contract_no)
WHERE a.confirm_id IS NULL;

UPDATE erp_funder_loan fl
JOIN erp_funder_payment_allocation a ON a.id = fl.allocation_id
SET fl.confirm_id = COALESCE(fl.confirm_id, a.confirm_id),
    fl.confirm_contract_no = COALESCE(fl.confirm_contract_no, a.confirm_contract_no)
WHERE fl.confirm_id IS NULL;

DROP INDEX uk_erp_inbound_order_presale ON erp_inbound_order;
CREATE INDEX idx_inbound_presale_order_id ON erp_inbound_order (presale_order_id);
CREATE UNIQUE INDEX uk_inbound_confirm_id ON erp_inbound_order (confirm_id);
CREATE INDEX idx_packing_confirm_id ON erp_presale_packing (confirm_id);
CREATE INDEX idx_attachment_confirm_type ON erp_presale_attachment (confirm_id, attachment_type);
CREATE INDEX idx_payment_allocation_confirm ON erp_funder_payment_allocation (confirm_id);
CREATE INDEX idx_funder_loan_confirm ON erp_funder_loan (confirm_id);
