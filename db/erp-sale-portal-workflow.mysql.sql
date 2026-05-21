SET NAMES utf8mb4;

ALTER TABLE `sys_user`
  ADD COLUMN `secondary_partner_id` bigint DEFAULT NULL COMMENT '绑定二批主体ID',
  ADD COLUMN `secondary_partner_name` varchar(200) DEFAULT NULL COMMENT '绑定二批主体名称';

ALTER TABLE `erp_sale_order`
  ADD COLUMN `signed_contract_confirmed` tinyint(1) NOT NULL DEFAULT 0 COMMENT '盖章合同是否已确认',
  ADD COLUMN `buyer_payment_confirmed` tinyint(1) NOT NULL DEFAULT 0 COMMENT '二批打款凭证是否已确认',
  ADD COLUMN `buyer_bank_confirmed` tinyint(1) NOT NULL DEFAULT 0 COMMENT '二批来款水单是否已确认',
  ADD COLUMN `funder_payment_confirmed` tinyint(1) NOT NULL DEFAULT 0 COMMENT '资方打款凭证是否已确认';
