-- 合同成本日报：按客户订单确认函合同号归档每天产生的资金成本、仓储费等成本。

CREATE TABLE IF NOT EXISTS `erp_contract_daily_cost` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '合同每日成本ID',
  `contract_no` VARCHAR(100) NOT NULL COMMENT '确认函合同号',
  `cost_date` DATE NOT NULL COMMENT '成本日期',
  `funder_names` VARCHAR(500) DEFAULT NULL COMMENT '涉及资方名称，多个用逗号分隔',
  `interest_amount` DECIMAL(18,10) NOT NULL DEFAULT 0.0000000000 COMMENT '当日资金利息成本',
  `storage_amount` DECIMAL(18,10) NOT NULL DEFAULT 0.0000000000 COMMENT '当日仓储成本',
  `other_amount` DECIMAL(18,10) NOT NULL DEFAULT 0.0000000000 COMMENT '当日其他成本',
  `total_amount` DECIMAL(18,10) NOT NULL DEFAULT 0.0000000000 COMMENT '当日成本合计',
  `calculation_summary` VARCHAR(1000) DEFAULT NULL COMMENT '汇总计算说明',
  `snapshot_status` INT DEFAULT 1 COMMENT '快照状态：1有效',
  `create_user_id` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_erp_contract_daily_cost_contract_date` (`contract_no`, `cost_date`),
  KEY `idx_erp_contract_daily_cost_date` (`cost_date`),
  KEY `idx_erp_contract_daily_cost_contract` (`contract_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='合同每日成本快照表';

CREATE TABLE IF NOT EXISTS `erp_contract_daily_cost_detail` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '合同每日成本明细ID',
  `daily_cost_id` BIGINT NOT NULL COMMENT '合同每日成本ID',
  `line_no` INT DEFAULT 1 COMMENT '明细行号',
  `cost_type` VARCHAR(64) NOT NULL COMMENT '成本类型：资金成本、仓储费用等',
  `cost_name` VARCHAR(128) NOT NULL COMMENT '成本名称',
  `source_no` VARCHAR(200) DEFAULT NULL COMMENT '来源单号或来源信息',
  `related_name` VARCHAR(200) DEFAULT NULL COMMENT '关联对象名称，如资方、仓库',
  `basis_amount` DECIMAL(18,10) DEFAULT 0.0000000000 COMMENT '计算基数，如本金或重量KG',
  `amount` DECIMAL(18,10) NOT NULL DEFAULT 0.0000000000 COMMENT '明细成本金额',
  `formula` VARCHAR(1000) DEFAULT NULL COMMENT '计算公式',
  `remark` VARCHAR(1000) DEFAULT NULL COMMENT '备注说明',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_erp_contract_daily_cost_detail_daily` (`daily_cost_id`),
  KEY `idx_erp_contract_daily_cost_detail_type` (`cost_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='合同每日成本计算明细表';

INSERT INTO `sys_menu` (`menu_id`, `parent_id`, `name`, `url`, `perms`, `type`, `icon`, `order_num`)
SELECT 113, 86, CONVERT(0xe59088e5908ce68890e69cace697a5e68aa5 USING utf8mb4), 'erp/contract-daily-cost', NULL, 1, 'el-icon-date', 30
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 113 OR `url` = 'erp/contract-daily-cost');

INSERT INTO `sys_menu` (`menu_id`, `parent_id`, `name`, `url`, `perms`, `type`, `icon`, `order_num`)
SELECT 114, 113, CONVERT(0xe69fa5e79c8b USING utf8mb4), NULL, 'erp:contract-daily-cost:list', 2, NULL, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 114 OR `perms` = 'erp:contract-daily-cost:list');

INSERT INTO `sys_menu` (`menu_id`, `parent_id`, `name`, `url`, `perms`, `type`, `icon`, `order_num`)
SELECT 115, 113, CONVERT(0xe9878de696b0e7949fe68890 USING utf8mb4), NULL, 'erp:contract-daily-cost:refresh', 2, NULL, 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 115 OR `perms` = 'erp:contract-daily-cost:refresh');

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id
FROM sys_menu
WHERE menu_id IN (113, 114, 115)
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_menu
    WHERE role_id = 1 AND sys_role_menu.menu_id = sys_menu.menu_id
  );

INSERT INTO `schedule_job` (`bean_name`, `params`, `cron_expression`, `status`, `remark`, `create_time`)
SELECT 'contractDailyCostTask', 'yesterday', '0 0 2 * * ?', 0,
       CONVERT(0xe6af8fe5a4a9e5878ce699a832e782b9e7949fe68890e4b88ae4b880e5a4a9e59088e5908ce68890e69cace697a5e68aa5 USING utf8mb4),
       NOW()
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `schedule_job` WHERE `bean_name` = 'contractDailyCostTask'
);

UPDATE `schedule_job`
SET `params` = 'yesterday',
    `cron_expression` = '0 0 2 * * ?',
    `status` = 0,
    `remark` = CONVERT(0xe6af8fe5a4a9e5878ce699a832e782b9e7949fe68890e4b88ae4b880e5a4a9e59088e5908ce68890e69cace697a5e68aa5 USING utf8mb4)
WHERE `bean_name` = 'contractDailyCostTask';
