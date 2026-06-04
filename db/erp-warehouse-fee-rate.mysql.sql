-- 仓库费用历史价格表：按生效日期维护四类费用，统一单位为元/吨
CREATE TABLE IF NOT EXISTS `erp_warehouse_fee_rate` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `warehouse_id` bigint(20) NOT NULL COMMENT '仓库ID',
  `warehouse_name` varchar(200) DEFAULT NULL COMMENT '仓库名称',
  `effective_date` date NOT NULL COMMENT '生效日期',
  `frozen_storage_fee` decimal(18,2) DEFAULT 0.00 COMMENT '仓储冷冻费（元/吨）',
  `chilled_storage_fee` decimal(18,2) DEFAULT 0.00 COMMENT '仓储冷藏费（元/吨）',
  `frozen_cold_fee` decimal(18,2) DEFAULT 0.00 COMMENT '冷链冷冻装卸费（元/吨）',
  `chilled_cold_fee` decimal(18,2) DEFAULT 0.00 COMMENT '冷链冷藏装卸费（元/吨）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `status` int(11) DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
  `create_user_id` bigint(20) DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_erp_warehouse_fee_rate_date` (`warehouse_id`, `effective_date`),
  KEY `idx_erp_warehouse_fee_rate_lookup` (`warehouse_id`, `status`, `effective_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仓库费用历史价格表';

INSERT INTO `erp_warehouse_fee_rate` (
  `warehouse_id`,
  `warehouse_name`,
  `effective_date`,
  `frozen_storage_fee`,
  `chilled_storage_fee`,
  `frozen_cold_fee`,
  `chilled_cold_fee`,
  `remark`,
  `status`,
  `create_user_id`,
  `create_time`,
  `update_time`
)
SELECT
  w.`id`,
  w.`warehouse_name`,
  CURDATE(),
  COALESCE(w.`frozen_storage_fee`, 0),
  COALESCE(w.`chilled_storage_fee`, 0),
  COALESCE(w.`frozen_cold_fee`, 0),
  COALESCE(w.`chilled_cold_fee`, 0),
  '系统按仓库当前费用初始化',
  1,
  w.`create_user_id`,
  NOW(),
  NOW()
FROM `erp_warehouse` w
WHERE NOT EXISTS (
  SELECT 1
  FROM `erp_warehouse_fee_rate` r
  WHERE r.`warehouse_id` = w.`id`
);
