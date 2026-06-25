-- 手工费用支出登记：用于记录报销类/其他类支出费用和附件归档。

CREATE TABLE IF NOT EXISTS `erp_manual_expense` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '费用支出登记ID',
  `expense_no` VARCHAR(64) NOT NULL COMMENT '费用编号',
  `expense_date` DATE NOT NULL COMMENT '费用日期',
  `expense_type` VARCHAR(64) DEFAULT NULL COMMENT '费用类型',
  `expense_name` VARCHAR(128) NOT NULL COMMENT '费用名称',
  `amount` DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '支出金额',
  `remark` VARCHAR(1000) DEFAULT NULL COMMENT '备注',
  `create_user_id` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_erp_manual_expense_no` (`expense_no`),
  KEY `idx_erp_manual_expense_date` (`expense_date`),
  KEY `idx_erp_manual_expense_type` (`expense_type`),
  KEY `idx_erp_manual_expense_name` (`expense_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='手工费用支出登记表';

CREATE TABLE IF NOT EXISTS `erp_manual_expense_file` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '费用支出附件ID',
  `expense_id` BIGINT NOT NULL COMMENT '费用支出登记ID',
  `line_no` INT DEFAULT 1 COMMENT '附件序号',
  `file_path` VARCHAR(500) NOT NULL COMMENT '附件存储路径',
  `file_name` VARCHAR(255) DEFAULT NULL COMMENT '附件原文件名',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_erp_manual_expense_file_expense` (`expense_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='手工费用支出附件归档表';

INSERT INTO `sys_menu` (`menu_id`, `parent_id`, `name`, `url`, `perms`, `type`, `icon`, `order_num`)
SELECT 99, 86, '费用支出登记', 'erp/manual-expense', NULL, 1, 'el-icon-document', 20
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 99 OR `url` = 'erp/manual-expense');

INSERT INTO `sys_menu` (`menu_id`, `parent_id`, `name`, `url`, `perms`, `type`, `icon`, `order_num`)
SELECT 100, 99, '查看', NULL, 'erp:manual-expense:list,erp:manual-expense:info', 2, NULL, 0
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 100 OR `perms` = 'erp:manual-expense:list,erp:manual-expense:info');

INSERT INTO `sys_menu` (`menu_id`, `parent_id`, `name`, `url`, `perms`, `type`, `icon`, `order_num`)
SELECT 101, 99, '新增', NULL, 'erp:manual-expense:save', 2, NULL, 1
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 101 OR `perms` = 'erp:manual-expense:save');

INSERT INTO `sys_menu` (`menu_id`, `parent_id`, `name`, `url`, `perms`, `type`, `icon`, `order_num`)
SELECT 102, 99, '修改/上传附件', NULL, 'erp:manual-expense:update', 2, NULL, 2
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 102 OR `perms` = 'erp:manual-expense:update');

INSERT INTO `sys_menu` (`menu_id`, `parent_id`, `name`, `url`, `perms`, `type`, `icon`, `order_num`)
SELECT 103, 99, '删除', NULL, 'erp:manual-expense:delete', 2, NULL, 3
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 103 OR `perms` = 'erp:manual-expense:delete');

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id
FROM sys_menu
WHERE menu_id IN (99, 100, 101, 102, 103)
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_menu
    WHERE role_id = 1 AND sys_role_menu.menu_id = sys_menu.menu_id
  );
