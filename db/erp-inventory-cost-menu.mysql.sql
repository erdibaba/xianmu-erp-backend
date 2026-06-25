-- 库存成本查询：基于当前剩余库存重量分摊采购和费用成本。
INSERT INTO `sys_menu` (`menu_id`, `parent_id`, `name`, `url`, `perms`, `type`, `icon`, `order_num`)
SELECT 104, 40, CONVERT(0xe5ba93e5ad98e68890e69cace69fa5e8afa2 USING utf8mb4), 'erp/inventory-cost', NULL, 1, 'el-icon-s-data', 56
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 104 OR `url` = 'erp/inventory-cost');

INSERT INTO `sys_menu` (`menu_id`, `parent_id`, `name`, `url`, `perms`, `type`, `icon`, `order_num`)
SELECT 105, 104, CONVERT(0xe69fa5e79c8b USING utf8mb4), NULL, 'erp:inventory-cost:list', 2, NULL, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 105 OR `perms` = 'erp:inventory-cost:list');

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id
FROM sys_menu
WHERE menu_id IN (104, 105)
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_menu
    WHERE role_id = 1 AND sys_role_menu.menu_id = sys_menu.menu_id
  );
