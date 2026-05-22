-- 入库单SKU明细报损字段
ALTER TABLE erp_inbound_order_item
  ADD COLUMN damage_weight_kg DECIMAL(10,2) NULL COMMENT '报损重量(KG)' AFTER packing_boxes,
  ADD COLUMN damage_reason VARCHAR(200) NULL COMMENT '报损原因' AFTER damage_weight_kg;
