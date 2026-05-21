SET NAMES utf8mb4;

ALTER TABLE `erp_sale_order`
  ADD COLUMN IF NOT EXISTS `contract_sign_date` datetime DEFAULT NULL COMMENT '签订日期' AFTER `contract_token`;

ALTER TABLE `erp_sale_order_item`
  ADD COLUMN IF NOT EXISTS `sale_price_kg` decimal(10,2) DEFAULT NULL COMMENT '销售价(元/千克)' AFTER `spec_weight`,
  ADD COLUMN IF NOT EXISTS `contract_quantity_kg` decimal(10,2) DEFAULT NULL COMMENT '合同数量/千克' AFTER `sale_price_kg`,
  ADD COLUMN IF NOT EXISTS `contract_factory_no` varchar(100) DEFAULT NULL COMMENT '厂号' AFTER `contract_quantity_kg`,
  ADD COLUMN IF NOT EXISTS `contract_port_cold` varchar(200) DEFAULT NULL COMMENT '港口/冷库' AFTER `contract_factory_no`;
