-- 将 FontAwesome 菜单图标替换为当前前端已内置的 SVG 图标，避免未引入字体库导致侧边栏不显示图标。
UPDATE sys_menu SET icon = 'geren' WHERE menu_id = 41 AND icon = 'fa fa-address-book';
UPDATE sys_menu SET icon = 'tubiao' WHERE menu_id = 42 AND icon = 'fa fa-tags';
UPDATE sys_menu SET icon = 'zonghe' WHERE menu_id = 43 AND icon = 'fa fa-line-chart';
UPDATE sys_menu SET icon = 'daohang' WHERE menu_id = 44 AND icon = 'fa fa-truck';
UPDATE sys_menu SET icon = 'editor' WHERE menu_id = 45 AND icon = 'fa fa-shopping-cart';
UPDATE sys_menu SET icon = 'zonghe' WHERE menu_id = 46 AND icon = 'fa fa-cubes';
UPDATE sys_menu SET icon = 'mudedi' WHERE menu_id = 68 AND icon = 'fa fa-building-o';
UPDATE sys_menu SET icon = 'bianji' WHERE menu_id = 73 AND icon = 'fa fa-check-square-o';
UPDATE sys_menu SET icon = 'zhedie' WHERE menu_id = 88 AND icon = 'fa fa-exchange';
UPDATE sys_menu SET icon = 'editor' WHERE menu_id = 93 AND icon = 'fa fa-file-text-o';
UPDATE sys_menu SET icon = 'geren' WHERE menu_id = 94 AND icon = 'fa fa-id-card-o';
UPDATE sys_menu SET icon = 'geren' WHERE menu_id = 108 AND icon = 'fa fa-user-circle-o';
