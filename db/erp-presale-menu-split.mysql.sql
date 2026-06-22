-- 拆分预售单大合同与客户订单确认函小合同菜单。
-- 预售单管理：只维护大预售合同；客户订单确认：按确认函合同号走后续装箱、报关、检疫等流程。

INSERT INTO sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num)
SELECT 93, 40, CONVERT(0xE9A284E594AEE58D95E7AEA1E79086 USING utf8mb4), 'erp/presale-contract', NULL, 1, 'fa fa-file-text-o', 6
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 93 OR url = 'erp/presale-contract');

UPDATE sys_menu
SET name = CONVERT(0xE5AEA2E688B7E8AEA2E58D95E7A1AEE8AEA4 USING utf8mb4),
    url = 'erp/customer-confirm',
    icon = 'fa fa-check-square-o',
    order_num = 7
WHERE menu_id = 73;

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, m.menu_id
FROM sys_menu m
WHERE m.menu_id IN (73, 93)
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 1 AND rm.menu_id = m.menu_id
  );
