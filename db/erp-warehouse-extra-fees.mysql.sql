SET @db = DATABASE();

DROP PROCEDURE IF EXISTS add_warehouse_fee_column_if_missing;
DELIMITER //
CREATE PROCEDURE add_warehouse_fee_column_if_missing(
  IN p_column_name VARCHAR(64),
  IN p_sql TEXT
)
BEGIN
  IF (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
      WHERE TABLE_SCHEMA = @db
        AND TABLE_NAME = 'erp_warehouse_fee_rate'
        AND COLUMN_NAME = p_column_name) = 0 THEN
    SET @ddl = p_sql;
    PREPARE stmt FROM @ddl;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END//
DELIMITER ;

CALL add_warehouse_fee_column_if_missing('weight_basis',
  'ALTER TABLE erp_warehouse_fee_rate ADD COLUMN weight_basis VARCHAR(16) NOT NULL DEFAULT ''NET'' COMMENT ''计重口径：NET净重，GROSS毛重'' AFTER scan_fee_rate');
CALL add_warehouse_fee_column_if_missing('wrapping_fee',
  'ALTER TABLE erp_warehouse_fee_rate ADD COLUMN wrapping_fee DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT ''缠膜费单价'' AFTER weight_basis');
CALL add_warehouse_fee_column_if_missing('wrapping_fee_unit',
  'ALTER TABLE erp_warehouse_fee_rate ADD COLUMN wrapping_fee_unit VARCHAR(16) NOT NULL DEFAULT ''PALLET'' COMMENT ''缠膜费计费单位：TON吨，BOX箱，PALLET托，CONTAINER柜'' AFTER wrapping_fee');
CALL add_warehouse_fee_column_if_missing('sorting_fee',
  'ALTER TABLE erp_warehouse_fee_rate ADD COLUMN sorting_fee DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT ''分拣费单价'' AFTER wrapping_fee_unit');
CALL add_warehouse_fee_column_if_missing('sorting_fee_unit',
  'ALTER TABLE erp_warehouse_fee_rate ADD COLUMN sorting_fee_unit VARCHAR(16) NOT NULL DEFAULT ''BOX'' COMMENT ''分拣费计费单位：TON吨，BOX箱，PALLET托，CONTAINER柜'' AFTER sorting_fee');
CALL add_warehouse_fee_column_if_missing('repeated_handling_fee',
  'ALTER TABLE erp_warehouse_fee_rate ADD COLUMN repeated_handling_fee DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT ''重复上下架费单价'' AFTER sorting_fee_unit');
CALL add_warehouse_fee_column_if_missing('repeated_handling_fee_unit',
  'ALTER TABLE erp_warehouse_fee_rate ADD COLUMN repeated_handling_fee_unit VARCHAR(16) NOT NULL DEFAULT ''PALLET'' COMMENT ''重复上下架费计费单位：TON吨，BOX箱，PALLET托，CONTAINER柜'' AFTER repeated_handling_fee');
CALL add_warehouse_fee_column_if_missing('owner_change_fee',
  'ALTER TABLE erp_warehouse_fee_rate ADD COLUMN owner_change_fee DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT ''存货人变更费单价'' AFTER repeated_handling_fee_unit');
CALL add_warehouse_fee_column_if_missing('owner_change_fee_unit',
  'ALTER TABLE erp_warehouse_fee_rate ADD COLUMN owner_change_fee_unit VARCHAR(16) NOT NULL DEFAULT ''CONTAINER'' COMMENT ''存货人变更费计费单位：TON吨，BOX箱，PALLET托，CONTAINER柜'' AFTER owner_change_fee');

DROP PROCEDURE IF EXISTS add_warehouse_fee_column_if_missing;

CREATE TABLE IF NOT EXISTS erp_warehouse_color_fee_tier (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  rate_id BIGINT NOT NULL COMMENT '仓库费用历史ID',
  warehouse_id BIGINT NOT NULL COMMENT '仓库ID',
  line_no INT NOT NULL DEFAULT 1 COMMENT '行号',
  range_unit VARCHAR(16) NOT NULL DEFAULT 'TON' COMMENT '阶梯数量单位：TON吨，BOX箱，CONTAINER柜',
  range_start DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '阶梯起始数量',
  range_end DECIMAL(18,2) DEFAULT NULL COMMENT '阶梯结束数量，空表示以上',
  fee_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '分色费金额',
  fee_unit VARCHAR(16) NOT NULL DEFAULT 'TON' COMMENT '费用单位：TON元/吨，BOX元/箱，CONTAINER元/柜',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_color_fee_rate (rate_id, line_no),
  KEY idx_color_fee_warehouse (warehouse_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仓库分色费阶梯表';
