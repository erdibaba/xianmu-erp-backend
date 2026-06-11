-- 库存调整：转仓库、冷鲜转冷冻
CREATE TABLE IF NOT EXISTS erp_inventory_adjustment (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  adjustment_no VARCHAR(64) NOT NULL COMMENT '调整单号',
  adjustment_type VARCHAR(32) NOT NULL COMMENT '调整类型：WAREHOUSE_TRANSFER转仓库 FRESH_TO_FROZEN冷鲜转冷冻',
  remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
  create_user_id BIGINT DEFAULT NULL COMMENT '创建人ID',
  create_time DATETIME DEFAULT NULL COMMENT '创建时间',
  update_time DATETIME DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_inventory_adjustment_no (adjustment_no),
  KEY idx_inventory_adjustment_type_time (adjustment_type, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存调整主表';

CREATE TABLE IF NOT EXISTS erp_inventory_adjustment_item (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  adjustment_id BIGINT NOT NULL COMMENT '库存调整主表ID',
  line_no INT DEFAULT NULL COMMENT '行号',
  adjustment_type VARCHAR(32) NOT NULL COMMENT '调整类型：WAREHOUSE_TRANSFER转仓库 FRESH_TO_FROZEN冷鲜转冷冻',
  source_adjustment_item_id BIGINT DEFAULT NULL COMMENT '来源调整明细ID，来源是调整后库存时记录',
  source_inbound_order_id BIGINT DEFAULT NULL COMMENT '来源入库单ID',
  source_inbound_item_id BIGINT DEFAULT NULL COMMENT '来源入库单明细ID',
  source_packing_item_id BIGINT DEFAULT NULL COMMENT '来源装箱单产品明细ID',
  source_batch_id BIGINT DEFAULT NULL COMMENT '来源装箱单生产日期批次ID',
  product_id BIGINT NOT NULL COMMENT '产品ID',
  product_code VARCHAR(64) DEFAULT NULL COMMENT '产品编码',
  product_name VARCHAR(255) DEFAULT NULL COMMENT '产品中文名称',
  product_name_en VARCHAR(255) DEFAULT NULL COMMENT '产品英文名称',
  product_spec VARCHAR(100) DEFAULT NULL COMMENT '规格',
  unit VARCHAR(32) DEFAULT NULL COMMENT '计量单位',
  source_warehouse_id BIGINT DEFAULT NULL COMMENT '来源仓库ID',
  source_warehouse_name VARCHAR(100) DEFAULT NULL COMMENT '来源仓库名称',
  target_warehouse_id BIGINT DEFAULT NULL COMMENT '目标仓库ID',
  target_warehouse_name VARCHAR(100) DEFAULT NULL COMMENT '目标仓库名称',
  container_no VARCHAR(100) DEFAULT NULL COMMENT '柜号',
  factory_no VARCHAR(100) DEFAULT NULL COMMENT '厂号',
  source_temperature_zone VARCHAR(32) DEFAULT NULL COMMENT '来源冷冻/冷鲜',
  target_temperature_zone VARCHAR(32) DEFAULT NULL COMMENT '目标冷冻/冷鲜',
  production_date DATE DEFAULT NULL COMMENT '生产日期',
  source_expiry_date DATE DEFAULT NULL COMMENT '来源过期日期',
  target_expiry_date DATE DEFAULT NULL COMMENT '目标过期日期',
  transfer_boxes INT NOT NULL DEFAULT 0 COMMENT '调整箱数',
  transfer_weight_kg DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '调整重量KG',
  remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
  create_time DATETIME DEFAULT NULL COMMENT '创建时间',
  update_time DATETIME DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_inventory_adjustment_item_adjustment (adjustment_id),
  KEY idx_inventory_adjustment_item_source (source_inbound_item_id, source_packing_item_id, source_batch_id),
  KEY idx_inventory_adjustment_item_source_adjustment (source_adjustment_item_id),
  KEY idx_inventory_adjustment_item_product (product_id, container_no, factory_no),
  KEY idx_inventory_adjustment_item_target (target_warehouse_id, target_temperature_zone, target_expiry_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存调整明细表';

CREATE TABLE IF NOT EXISTS erp_inventory_adjustment_file (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  adjustment_id BIGINT NOT NULL COMMENT '库存调整主表ID',
  line_no INT DEFAULT NULL COMMENT '行号',
  file_path VARCHAR(500) NOT NULL COMMENT '归档文件路径',
  file_name VARCHAR(255) DEFAULT NULL COMMENT '归档文件名称',
  create_time DATETIME DEFAULT NULL COMMENT '创建时间',
  update_time DATETIME DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_inventory_adjustment_file_adjustment (adjustment_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存调整原件归档表';

DELETE FROM sys_role_menu WHERE menu_id = 87 AND EXISTS (
  SELECT 1 FROM sys_menu WHERE menu_id = 87 AND perms = 'erp:inventory-adjustment:save'
);
DELETE FROM sys_menu WHERE menu_id = 87 AND perms = 'erp:inventory-adjustment:save';

INSERT INTO sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num)
SELECT 88, 40, '库存调整', 'erp/stock-adjustment', NULL, 1, 'fa fa-exchange', 55
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 88 OR url = 'erp/stock-adjustment');

INSERT INTO sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num)
SELECT 89, 88, '查看', NULL, 'erp:inventory-adjustment:list', 2, NULL, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 89 OR perms = 'erp:inventory-adjustment:list');

INSERT INTO sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num)
SELECT 90, 88, '确认调整', NULL, 'erp:inventory-adjustment:save', 2, NULL, 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 90 OR perms = 'erp:inventory-adjustment:save');

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, m.menu_id
FROM sys_menu m
WHERE m.menu_id IN (88, 89, 90)
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_menu rm
    WHERE rm.role_id = 1 AND rm.menu_id = m.menu_id
  );
