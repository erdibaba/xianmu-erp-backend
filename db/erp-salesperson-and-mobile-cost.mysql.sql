CREATE TABLE IF NOT EXISTS `erp_salesperson` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `sales_name` varchar(100) NOT NULL COMMENT '销售姓名',
  `mobile` varchar(50) DEFAULT NULL COMMENT '手机号',
  `sys_user_id` bigint DEFAULT NULL COMMENT '绑定登录用户ID',
  `sys_username` varchar(100) DEFAULT NULL COMMENT '绑定登录用户名',
  `status` tinyint DEFAULT '1' COMMENT '状态：1启用 0停用',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_user_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_erp_salesperson_name` (`sales_name`),
  KEY `idx_erp_salesperson_user` (`sys_user_id`),
  KEY `idx_erp_salesperson_status_name` (`status`, `sales_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='销售信息维护';

ALTER TABLE `erp_salesperson`
  MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  MODIFY COLUMN `sales_name` varchar(100) NOT NULL COMMENT '销售姓名',
  MODIFY COLUMN `mobile` varchar(50) DEFAULT NULL COMMENT '手机号',
  MODIFY COLUMN `sys_user_id` bigint DEFAULT NULL COMMENT '绑定登录用户ID',
  MODIFY COLUMN `sys_username` varchar(100) DEFAULT NULL COMMENT '绑定登录用户名',
  MODIFY COLUMN `status` tinyint DEFAULT '1' COMMENT '状态：1启用 0停用',
  MODIFY COLUMN `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  MODIFY COLUMN `create_user_id` bigint DEFAULT NULL COMMENT '创建人ID',
  MODIFY COLUMN `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  MODIFY COLUMN `update_time` datetime DEFAULT NULL COMMENT '更新时间';

SET @column_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE table_schema = DATABASE() AND table_name = 'erp_sale_order' AND column_name = 'salesperson_id'
);
SET @sql := IF(@column_exists = 0,
  'ALTER TABLE `erp_sale_order` ADD COLUMN `salesperson_id` bigint DEFAULT NULL COMMENT ''销售人员ID'' AFTER `secondary_partner_name`',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE table_schema = DATABASE() AND table_name = 'erp_sale_order' AND column_name = 'salesperson_name'
);
SET @sql := IF(@column_exists = 0,
  'ALTER TABLE `erp_sale_order` ADD COLUMN `salesperson_name` varchar(100) DEFAULT NULL COMMENT ''销售人员姓名'' AFTER `salesperson_id`',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

ALTER TABLE `erp_sale_order`
  MODIFY COLUMN `salesperson_id` bigint DEFAULT NULL COMMENT '销售人员ID',
  MODIFY COLUMN `salesperson_name` varchar(100) DEFAULT NULL COMMENT '销售人员姓名';

SET @index_exists := (
  SELECT COUNT(1) FROM information_schema.STATISTICS
  WHERE table_schema = DATABASE() AND table_name = 'erp_sale_order' AND index_name = 'idx_sale_order_salesperson'
);
SET @sql := IF(@index_exists = 0,
  'CREATE INDEX `idx_sale_order_salesperson` ON `erp_sale_order` (`salesperson_id`)',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

INSERT INTO `sys_menu`(`menu_id`, `parent_id`, `name`, `url`, `perms`, `type`, `icon`, `order_num`)
SELECT 106, 40, CONVERT(0xe5ba93e5ad98e68890e69cace6898be69cbae7abaf USING utf8mb4), 'erp/inventory-cost-mobile', NULL, 1, 'el-icon-mobile-phone', 57
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 106 OR `url` = 'erp/inventory-cost-mobile');

INSERT INTO `sys_menu`(`menu_id`, `parent_id`, `name`, `url`, `perms`, `type`, `icon`, `order_num`)
SELECT 107, 106, CONVERT(0xe69fa5e79c8b USING utf8mb4), NULL, 'erp:inventory-cost:list', 2, NULL, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 107);

INSERT INTO `sys_menu`(`menu_id`, `parent_id`, `name`, `url`, `perms`, `type`, `icon`, `order_num`)
SELECT 108, 40, CONVERT(0xe99480e594aee4bfa1e681afe7bbb4e68aa4 USING utf8mb4), 'erp/salesperson', NULL, 1, 'fa fa-user-circle-o', 5
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 108 OR `url` = 'erp/salesperson');

INSERT INTO `sys_menu`(`menu_id`, `parent_id`, `name`, `url`, `perms`, `type`, `icon`, `order_num`)
SELECT 109, 108, CONVERT(0xe69fa5e79c8b USING utf8mb4), NULL, 'erp:salesperson:list,erp:salesperson:info', 2, NULL, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 109 OR `perms` = 'erp:salesperson:list,erp:salesperson:info');

INSERT INTO `sys_menu`(`menu_id`, `parent_id`, `name`, `url`, `perms`, `type`, `icon`, `order_num`)
SELECT 110, 108, CONVERT(0xe696b0e5a29e USING utf8mb4), NULL, 'erp:salesperson:save', 2, NULL, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 110 OR `perms` = 'erp:salesperson:save');

INSERT INTO `sys_menu`(`menu_id`, `parent_id`, `name`, `url`, `perms`, `type`, `icon`, `order_num`)
SELECT 111, 108, CONVERT(0xe4bfaee694b9 USING utf8mb4), NULL, 'erp:salesperson:update', 2, NULL, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 111 OR `perms` = 'erp:salesperson:update');

INSERT INTO `sys_menu`(`menu_id`, `parent_id`, `name`, `url`, `perms`, `type`, `icon`, `order_num`)
SELECT 112, 108, CONVERT(0xe588a0e999a4 USING utf8mb4), NULL, 'erp:salesperson:delete', 2, NULL, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 112 OR `perms` = 'erp:salesperson:delete');

UPDATE `sys_menu` SET `name` = CONVERT(0xe5ba93e5ad98e68890e69cace6898be69cbae7abaf USING utf8mb4) WHERE `menu_id` = 106 AND `url` = 'erp/inventory-cost-mobile';
UPDATE `sys_menu` SET `name` = CONVERT(0xe99480e594aee4bfa1e681afe7bbb4e68aa4 USING utf8mb4) WHERE `menu_id` = 108 AND `url` = 'erp/salesperson';

INSERT IGNORE INTO `sys_role_menu`(`role_id`, `menu_id`)
SELECT 1, menu_id FROM `sys_menu` WHERE menu_id IN (106, 107, 108, 109, 110, 111, 112);
