-- 往来单位冷库减免天数，用于销售合同收费标准
ALTER TABLE erp_partner
  ADD COLUMN cold_storage_free_days INT NOT NULL DEFAULT 7 COMMENT '冷库减免天数' AFTER business_role;

UPDATE erp_partner
SET cold_storage_free_days = 7
WHERE cold_storage_free_days IS NULL
  OR cold_storage_free_days <= 0;
