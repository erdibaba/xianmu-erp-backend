SET @parent_id := 40;

INSERT INTO sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num)
SELECT 74, @parent_id, '入库管理', 'erp/inbound-manage', NULL, 1, 'el-icon-document-checked', 8
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 74 OR url = 'erp/inbound-manage');

INSERT INTO sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num)
SELECT 75, 74, '查看', NULL, 'erp:tradeorder:info', 2, NULL, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 75);

INSERT INTO sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num)
SELECT 76, 74, '上传识别', NULL, 'erp:tradeorder:save', 2, NULL, 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 76);

INSERT INTO sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num)
SELECT 77, 74, '修改', NULL, 'erp:tradeorder:update', 2, NULL, 2
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 77);

INSERT INTO sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num)
SELECT 78, 74, '列表', NULL, 'erp:tradeorder:list', 2, NULL, 3
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 78);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, 74 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 74);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, 75 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 75);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, 76 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 76);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, 77 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 77);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, 78 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 78);
