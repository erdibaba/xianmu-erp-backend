-- 销售单关联预销售单字段及确认状态
ALTER TABLE erp_sale_order
  ADD COLUMN source_presale_order_id BIGINT NULL COMMENT '关联预销售单ID' AFTER funder_payment_confirmed,
  ADD COLUMN source_presale_order_no VARCHAR(100) NULL COMMENT '关联预销售单号' AFTER source_presale_order_id,
  ADD COLUMN presale_link_confirmed TINYINT(1) NOT NULL DEFAULT 0 COMMENT '预销售单关联确认状态：0未确认 1已确认' AFTER source_presale_order_no;

-- 兼容历史数据：用销售单明细上的来源预销售单回填主表
UPDATE erp_sale_order o
LEFT JOIN (
  SELECT
    sale_order_id,
    MAX(source_presale_order_id) AS source_presale_order_id,
    MAX(source_presale_order_no) AS source_presale_order_no
  FROM erp_sale_order_item
  WHERE source_presale_order_id IS NOT NULL
  GROUP BY sale_order_id
) i ON o.id = i.sale_order_id
SET
  o.source_presale_order_id = i.source_presale_order_id,
  o.source_presale_order_no = i.source_presale_order_no
WHERE o.source_presale_order_id IS NULL
  AND i.source_presale_order_id IS NOT NULL;
