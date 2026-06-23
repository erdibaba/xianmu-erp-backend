-- 司机档案增加身份证号，入库单关联司机档案。

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'erp_driver' AND column_name = 'id_card_no'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE erp_driver ADD COLUMN id_card_no varchar(100) DEFAULT NULL COMMENT ''身份证号'' AFTER mobile',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

ALTER TABLE erp_driver
  MODIFY COLUMN id_card_no varchar(100) DEFAULT NULL COMMENT '身份证号';

SET @idx_exists := (
  SELECT COUNT(1) FROM information_schema.statistics
  WHERE table_schema = DATABASE() AND table_name = 'erp_driver' AND index_name = 'idx_driver_id_card_no'
);
SET @sql := IF(@idx_exists = 0,
  'CREATE INDEX idx_driver_id_card_no ON erp_driver (id_card_no)',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'erp_inbound_order' AND column_name = 'driver_id'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE erp_inbound_order ADD COLUMN driver_id bigint DEFAULT NULL COMMENT ''司机档案ID'' AFTER container_no',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

ALTER TABLE erp_inbound_order
  MODIFY COLUMN driver_id bigint DEFAULT NULL COMMENT '司机档案ID';

SET @idx_exists := (
  SELECT COUNT(1) FROM information_schema.statistics
  WHERE table_schema = DATABASE() AND table_name = 'erp_inbound_order' AND index_name = 'idx_inbound_driver_id'
);
SET @sql := IF(@idx_exists = 0,
  'CREATE INDEX idx_inbound_driver_id ON erp_inbound_order (driver_id)',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
