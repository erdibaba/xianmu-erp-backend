-- 司机信息维护：仅作为独立主数据使用，暂不关联入库单。
CREATE TABLE IF NOT EXISTS `erp_driver` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '司机信息ID',
  `driver_name` varchar(100) NOT NULL COMMENT '司机姓名',
  `plate_no` varchar(50) NOT NULL COMMENT '车牌号',
  `mobile` varchar(50) NOT NULL COMMENT '手机号',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1启用，0停用',
  `create_user_id` bigint DEFAULT NULL COMMENT '创建人用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_erp_driver_plate_no` (`plate_no`),
  KEY `idx_erp_driver_name` (`driver_name`),
  KEY `idx_erp_driver_mobile` (`mobile`),
  KEY `idx_erp_driver_status_name` (`status`, `driver_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='司机信息档案表';

-- 重复执行时同步修复历史环境中的字段注释。
ALTER TABLE `erp_driver`
  COMMENT = '司机信息档案表',
  MODIFY COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT '司机信息ID',
  MODIFY COLUMN `driver_name` varchar(100) NOT NULL COMMENT '司机姓名',
  MODIFY COLUMN `plate_no` varchar(50) NOT NULL COMMENT '车牌号',
  MODIFY COLUMN `mobile` varchar(50) NOT NULL COMMENT '手机号',
  MODIFY COLUMN `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1启用，0停用',
  MODIFY COLUMN `create_user_id` bigint DEFAULT NULL COMMENT '创建人用户ID',
  MODIFY COLUMN `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  MODIFY COLUMN `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';

INSERT INTO `sys_menu` (`menu_id`, `parent_id`, `name`, `url`, `perms`, `type`, `icon`, `order_num`)
SELECT 94, 40, '司机信息维护', 'erp/driver', NULL, 1, 'fa fa-id-card-o', 4
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_menu` WHERE `menu_id` = 94 OR `url` = 'erp/driver'
);

INSERT INTO `sys_menu` (`menu_id`, `parent_id`, `name`, `url`, `perms`, `type`, `icon`, `order_num`)
SELECT 95, 94, '查看', NULL, 'erp:driver:list,erp:driver:info', 2, NULL, 0
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_menu` WHERE `menu_id` = 95 OR `perms` = 'erp:driver:list,erp:driver:info'
);

INSERT INTO `sys_menu` (`menu_id`, `parent_id`, `name`, `url`, `perms`, `type`, `icon`, `order_num`)
SELECT 96, 94, '新增', NULL, 'erp:driver:save', 2, NULL, 0
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_menu` WHERE `menu_id` = 96 OR `perms` = 'erp:driver:save'
);

INSERT INTO `sys_menu` (`menu_id`, `parent_id`, `name`, `url`, `perms`, `type`, `icon`, `order_num`)
SELECT 97, 94, '修改', NULL, 'erp:driver:update', 2, NULL, 0
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_menu` WHERE `menu_id` = 97 OR `perms` = 'erp:driver:update'
);

INSERT INTO `sys_menu` (`menu_id`, `parent_id`, `name`, `url`, `perms`, `type`, `icon`, `order_num`)
SELECT 98, 94, '删除', NULL, 'erp:driver:delete', 2, NULL, 0
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_menu` WHERE `menu_id` = 98 OR `perms` = 'erp:driver:delete'
);

UPDATE `sys_menu` SET `name` = '司机信息维护' WHERE `menu_id` = 94 AND `url` = 'erp/driver';
UPDATE `sys_menu` SET `name` = '查看' WHERE `menu_id` = 95 AND `perms` = 'erp:driver:list,erp:driver:info';
UPDATE `sys_menu` SET `name` = '新增' WHERE `menu_id` = 96 AND `perms` = 'erp:driver:save';
UPDATE `sys_menu` SET `name` = '修改' WHERE `menu_id` = 97 AND `perms` = 'erp:driver:update';
UPDATE `sys_menu` SET `name` = '删除' WHERE `menu_id` = 98 AND `perms` = 'erp:driver:delete';

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, menu.menu_id
FROM `sys_menu` menu
WHERE menu.menu_id IN (94, 95, 96, 97, 98)
  AND NOT EXISTS (
    SELECT 1
    FROM `sys_role_menu` role_menu
    WHERE role_menu.role_id = 1
      AND role_menu.menu_id = menu.menu_id
  );
