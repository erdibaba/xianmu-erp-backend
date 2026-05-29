-- 仓库收费字段升级：仓储费、冷链费分别维护冷冻/冷藏价格
ALTER TABLE `erp_warehouse`
  ADD COLUMN IF NOT EXISTS `frozen_storage_fee` decimal(18,2) DEFAULT 0.00 COMMENT '仓储费单价(冷冻)' AFTER `daily_cold_fee`,
  ADD COLUMN IF NOT EXISTS `chilled_storage_fee` decimal(18,2) DEFAULT 0.00 COMMENT '仓储费单价(冷藏)' AFTER `frozen_storage_fee`,
  ADD COLUMN IF NOT EXISTS `frozen_cold_fee` decimal(18,2) DEFAULT 0.00 COMMENT '冷链费单价(冷冻)' AFTER `chilled_storage_fee`,
  ADD COLUMN IF NOT EXISTS `chilled_cold_fee` decimal(18,2) DEFAULT 0.00 COMMENT '冷链费单价(冷藏)' AFTER `frozen_cold_fee`;

ALTER TABLE `erp_warehouse`
  MODIFY COLUMN `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  MODIFY COLUMN `warehouse_code` varchar(64) DEFAULT NULL COMMENT '仓库编码',
  MODIFY COLUMN `warehouse_name` varchar(200) DEFAULT NULL COMMENT '仓库名称',
  MODIFY COLUMN `warehouse_type` varchar(64) DEFAULT NULL COMMENT '仓库类型 NORMAL-普通仓 COLD-冷链仓 PORT_COLD-港口冷库 OTHER-其他',
  MODIFY COLUMN `owned_by_company` int(11) DEFAULT 0 COMMENT '是否自有 0-否 1-是',
  MODIFY COLUMN `contact_name` varchar(64) DEFAULT NULL COMMENT '联系人',
  MODIFY COLUMN `contact_phone` varchar(64) DEFAULT NULL COMMENT '联系电话',
  MODIFY COLUMN `address` varchar(255) DEFAULT NULL COMMENT '地址',
  MODIFY COLUMN `free_storage_days` int(11) DEFAULT 0 COMMENT '免费仓储天数',
  MODIFY COLUMN `daily_storage_fee` decimal(18,2) DEFAULT 0.00 COMMENT '兼容字段：仓储费单价，默认取冷冻',
  MODIFY COLUMN `daily_cold_fee` decimal(18,2) DEFAULT 0.00 COMMENT '兼容字段：冷链费单价，默认取冷冻',
  MODIFY COLUMN `frozen_storage_fee` decimal(18,2) DEFAULT 0.00 COMMENT '仓储费单价(冷冻)',
  MODIFY COLUMN `chilled_storage_fee` decimal(18,2) DEFAULT 0.00 COMMENT '仓储费单价(冷藏)',
  MODIFY COLUMN `frozen_cold_fee` decimal(18,2) DEFAULT 0.00 COMMENT '冷链费单价(冷冻)',
  MODIFY COLUMN `chilled_cold_fee` decimal(18,2) DEFAULT 0.00 COMMENT '冷链费单价(冷藏)',
  MODIFY COLUMN `fee_unit` varchar(32) DEFAULT 'PIECE' COMMENT '计费单位 PIECE-按箱 WEIGHT-按重量 ORDER-按单',
  MODIFY COLUMN `status` int(11) DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
  MODIFY COLUMN `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  MODIFY COLUMN `create_user_id` bigint(20) DEFAULT NULL COMMENT '创建人ID',
  MODIFY COLUMN `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  MODIFY COLUMN `update_time` datetime DEFAULT NULL COMMENT '更新时间';

UPDATE `erp_warehouse`
SET
  `frozen_storage_fee` = COALESCE(NULLIF(`frozen_storage_fee`, 0), `daily_storage_fee`),
  `chilled_storage_fee` = COALESCE(NULLIF(`chilled_storage_fee`, 0), `daily_storage_fee`),
  `frozen_cold_fee` = COALESCE(NULLIF(`frozen_cold_fee`, 0), `daily_cold_fee`),
  `chilled_cold_fee` = COALESCE(NULLIF(`chilled_cold_fee`, 0), `daily_cold_fee`);
