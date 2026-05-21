-- DataImport 菜单与权限增量脚本
-- 适用场景：已有 MySQL 数据库，需要把 dataimport 页面菜单补出来

SET @parent_menu_id := 1;

INSERT INTO `sys_menu` (`menu_id`, `parent_id`, `name`, `url`, `perms`, `type`, `icon`, `order_num`)
SELECT 31, @parent_menu_id, '数据导入', 'generator/dataimport', NULL, 1, 'config', 8
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_menu` WHERE `menu_id` = 31 OR `url` = 'generator/dataimport'
);

INSERT INTO `sys_menu` (`menu_id`, `parent_id`, `name`, `url`, `perms`, `type`, `icon`, `order_num`)
SELECT 32, 31, '查看', NULL, 'generator:dataimport:list,generator:dataimport:info', 2, NULL, 0
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_menu` WHERE `menu_id` = 32 OR `perms` = 'generator:dataimport:list,generator:dataimport:info'
);

INSERT INTO `sys_menu` (`menu_id`, `parent_id`, `name`, `url`, `perms`, `type`, `icon`, `order_num`)
SELECT 33, 31, '导入', NULL, 'generator:dataimport:save', 2, NULL, 1
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_menu` WHERE `menu_id` = 33 OR `perms` = 'generator:dataimport:save'
);

INSERT INTO `sys_menu` (`menu_id`, `parent_id`, `name`, `url`, `perms`, `type`, `icon`, `order_num`)
SELECT 34, 31, '修改', NULL, 'generator:dataimport:update', 2, NULL, 2
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_menu` WHERE `menu_id` = 34 OR `perms` = 'generator:dataimport:update'
);

INSERT INTO `sys_menu` (`menu_id`, `parent_id`, `name`, `url`, `perms`, `type`, `icon`, `order_num`)
SELECT 35, 31, '删除', NULL, 'generator:dataimport:delete', 2, NULL, 3
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_menu` WHERE `menu_id` = 35 OR `perms` = 'generator:dataimport:delete'
);
