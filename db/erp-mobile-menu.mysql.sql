-- 手机端菜单配置：控制登录后手机首页展示哪些方块入口。
SET @column_exists := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_menu' AND COLUMN_NAME = 'mobile_visible'
);
SET @ddl := IF(@column_exists = 0,
  'ALTER TABLE `sys_menu` ADD COLUMN `mobile_visible` tinyint(1) NOT NULL DEFAULT 0 COMMENT ''手机端是否显示 0-否 1-是'' AFTER `order_num`',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_menu' AND COLUMN_NAME = 'mobile_title'
);
SET @ddl := IF(@column_exists = 0,
  'ALTER TABLE `sys_menu` ADD COLUMN `mobile_title` varchar(64) DEFAULT NULL COMMENT ''手机端展示名称'' AFTER `mobile_visible`',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_menu' AND COLUMN_NAME = 'mobile_icon'
);
SET @ddl := IF(@column_exists = 0,
  'ALTER TABLE `sys_menu` ADD COLUMN `mobile_icon` varchar(64) DEFAULT NULL COMMENT ''手机端图标'' AFTER `mobile_title`',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_menu' AND COLUMN_NAME = 'mobile_url'
);
SET @ddl := IF(@column_exists = 0,
  'ALTER TABLE `sys_menu` ADD COLUMN `mobile_url` varchar(255) DEFAULT NULL COMMENT ''手机端跳转地址'' AFTER `mobile_icon`',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE `sys_menu`
SET `mobile_visible` = 1,
    `mobile_title` = CONVERT(0xe5ba93e5ad98e68890e69cac USING utf8mb4),
    `mobile_icon` = 'el-icon-s-data',
    `mobile_url` = '/mobile/inventory-cost'
WHERE `url` = 'erp/inventory-cost-mobile';
