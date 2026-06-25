SET @effective_date = '2026-06-25';

INSERT INTO erp_warehouse
(warehouse_code, warehouse_name, warehouse_type, owned_by_company, free_storage_days, daily_storage_fee, daily_cold_fee,
 frozen_storage_fee, chilled_storage_fee, frozen_cold_fee, chilled_cold_fee, fee_unit, scan_fee_enabled, status, remark, create_user_id, create_time, update_time)
SELECT 'ZLX-XH', '中联兴-箱货', '冷库', 0, 0, 0, 0, 0, 0, 0, 0, 'TON', 1, 1, '按最新冷库收费标准初始化', 1, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM erp_warehouse WHERE warehouse_code = 'ZLX-XH');

INSERT INTO erp_warehouse
(warehouse_code, warehouse_name, warehouse_type, owned_by_company, free_storage_days, daily_storage_fee, daily_cold_fee,
 frozen_storage_fee, chilled_storage_fee, frozen_cold_fee, chilled_cold_fee, fee_unit, scan_fee_enabled, status, remark, create_user_id, create_time, update_time)
SELECT 'ZLX-DT', '中联兴-胴体', '冷库', 0, 0, 0, 0, 0, 0, 0, 0, 'TON', 1, 1, '按最新冷库收费标准初始化', 1, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM erp_warehouse WHERE warehouse_code = 'ZLX-DT');

INSERT INTO erp_warehouse
(warehouse_code, warehouse_name, warehouse_type, owned_by_company, free_storage_days, daily_storage_fee, daily_cold_fee,
 frozen_storage_fee, chilled_storage_fee, frozen_cold_fee, chilled_cold_fee, fee_unit, scan_fee_enabled, status, remark, create_user_id, create_time, update_time)
SELECT 'DY-XH', '德宇-箱货', '冷库', 0, 0, 0, 0, 0, 0, 0, 0, 'TON', 1, 1, '按最新冷库收费标准初始化', 1, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM erp_warehouse WHERE warehouse_code = 'DY-XH');

INSERT INTO erp_warehouse
(warehouse_code, warehouse_name, warehouse_type, owned_by_company, free_storage_days, daily_storage_fee, daily_cold_fee,
 frozen_storage_fee, chilled_storage_fee, frozen_cold_fee, chilled_cold_fee, fee_unit, scan_fee_enabled, status, remark, create_user_id, create_time, update_time)
SELECT 'DY-DT', '德宇-胴体', '冷库', 0, 0, 0, 0, 0, 0, 0, 0, 'TON', 1, 1, '按最新冷库收费标准初始化', 1, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM erp_warehouse WHERE warehouse_code = 'DY-DT');

DROP PROCEDURE IF EXISTS upsert_warehouse_fee_rate_latest;
DELIMITER //
CREATE PROCEDURE upsert_warehouse_fee_rate_latest(
  IN p_warehouse_code VARCHAR(64),
  IN p_weight_basis VARCHAR(16),
  IN p_frozen_storage DECIMAL(18,2),
  IN p_frozen_cold DECIMAL(18,2),
  IN p_chilled_storage DECIMAL(18,2),
  IN p_chilled_cold DECIMAL(18,2),
  IN p_scan_unit VARCHAR(16),
  IN p_scan_rate DECIMAL(18,2),
  IN p_wrapping_fee DECIMAL(18,2),
  IN p_wrapping_unit VARCHAR(16),
  IN p_sorting_fee DECIMAL(18,2),
  IN p_sorting_unit VARCHAR(16),
  IN p_repeated_fee DECIMAL(18,2),
  IN p_repeated_unit VARCHAR(16),
  IN p_owner_change_fee DECIMAL(18,2),
  IN p_owner_change_unit VARCHAR(16)
)
BEGIN
  SELECT id, warehouse_name INTO @wid, @wname FROM erp_warehouse WHERE warehouse_code = p_warehouse_code LIMIT 1;
  INSERT INTO erp_warehouse_fee_rate
  (warehouse_id, warehouse_name, effective_date, frozen_storage_fee, chilled_storage_fee, frozen_cold_fee, chilled_cold_fee,
   scan_fee_unit, scan_fee_rate, weight_basis, wrapping_fee, wrapping_fee_unit, sorting_fee, sorting_fee_unit,
   repeated_handling_fee, repeated_handling_fee_unit, owner_change_fee, owner_change_fee_unit, remark, status, create_user_id, create_time, update_time)
  VALUES
  (@wid, @wname, @effective_date, p_frozen_storage, p_chilled_storage, p_frozen_cold, p_chilled_cold,
   p_scan_unit, p_scan_rate, p_weight_basis, p_wrapping_fee, p_wrapping_unit, p_sorting_fee, p_sorting_unit,
   p_repeated_fee, p_repeated_unit, p_owner_change_fee, p_owner_change_unit, '按最新冷库收费标准维护', 1, 1, NOW(), NOW())
  ON DUPLICATE KEY UPDATE
    warehouse_name = VALUES(warehouse_name),
    frozen_storage_fee = VALUES(frozen_storage_fee),
    chilled_storage_fee = VALUES(chilled_storage_fee),
    frozen_cold_fee = VALUES(frozen_cold_fee),
    chilled_cold_fee = VALUES(chilled_cold_fee),
    scan_fee_unit = VALUES(scan_fee_unit),
    scan_fee_rate = VALUES(scan_fee_rate),
    weight_basis = VALUES(weight_basis),
    wrapping_fee = VALUES(wrapping_fee),
    wrapping_fee_unit = VALUES(wrapping_fee_unit),
    sorting_fee = VALUES(sorting_fee),
    sorting_fee_unit = VALUES(sorting_fee_unit),
    repeated_handling_fee = VALUES(repeated_handling_fee),
    repeated_handling_fee_unit = VALUES(repeated_handling_fee_unit),
    owner_change_fee = VALUES(owner_change_fee),
    owner_change_fee_unit = VALUES(owner_change_fee_unit),
    remark = VALUES(remark),
    status = VALUES(status),
    update_time = NOW();
END//
DELIMITER ;

CALL upsert_warehouse_fee_rate_latest('WH0001', 'NET', 1.10, 50.00, 0.00, 0.00, 'TON', 15.00, 0.00, 'PALLET', 0.00, 'BOX', 8.00, 'PALLET', 0.00, 'CONTAINER');
CALL upsert_warehouse_fee_rate_latest('ZLX-XH', 'NET', 1.00, 40.00, 0.00, 0.00, 'TON', 10.00, 0.00, 'PALLET', 0.00, 'BOX', 0.00, 'PALLET', 0.00, 'CONTAINER');
CALL upsert_warehouse_fee_rate_latest('ZLX-DT', 'NET', 2.00, 65.00, 0.00, 0.00, 'TON', 10.00, 0.00, 'PALLET', 0.00, 'BOX', 0.00, 'PALLET', 0.00, 'CONTAINER');
CALL upsert_warehouse_fee_rate_latest('SH-WW-WC', 'GROSS', 2.10, 53.00, 2.80, 55.00, 'BOX', 0.35, 3.00, 'PALLET', 0.50, 'BOX', 15.00, 'PALLET', 200.00, 'CONTAINER');
CALL upsert_warehouse_fee_rate_latest('WH0003', 'NET', 2.00, 50.00, 2.20, 50.00, 'BOX', 0.30, 0.00, 'PALLET', 0.00, 'BOX', 0.00, 'PALLET', 0.00, 'CONTAINER');
CALL upsert_warehouse_fee_rate_latest('DY-XH', 'NET', 1.10, 40.00, 0.00, 0.00, 'TON', 10.00, 0.00, 'PALLET', 0.00, 'BOX', 0.00, 'PALLET', 0.00, 'CONTAINER');
CALL upsert_warehouse_fee_rate_latest('DY-DT', 'NET', 2.00, 60.00, 0.00, 0.00, 'TON', 0.00, 0.00, 'PALLET', 0.00, 'BOX', 0.00, 'PALLET', 0.00, 'CONTAINER');

DROP PROCEDURE IF EXISTS upsert_warehouse_fee_rate_latest;

DROP PROCEDURE IF EXISTS reset_color_fee_tiers;
DELIMITER //
CREATE PROCEDURE reset_color_fee_tiers(IN p_warehouse_code VARCHAR(64))
BEGIN
  SELECT r.id, r.warehouse_id INTO @rid, @wid
  FROM erp_warehouse_fee_rate r
  JOIN erp_warehouse w ON w.id = r.warehouse_id
  WHERE w.warehouse_code = p_warehouse_code AND r.effective_date = @effective_date
  LIMIT 1;
  DELETE FROM erp_warehouse_color_fee_tier WHERE rate_id = @rid;
END//
DELIMITER ;

CALL reset_color_fee_tiers('WH0001');
INSERT INTO erp_warehouse_color_fee_tier(rate_id, warehouse_id, line_no, range_unit, range_start, range_end, fee_amount, fee_unit, remark)
VALUES
(@rid, @wid, 1, 'TON', 0, 5, 15, 'TON', '5种以下15/吨'),
(@rid, @wid, 2, 'TON', 6, 10, 20, 'TON', '6-10种20/吨'),
(@rid, @wid, 3, 'TON', 11, 15, 25, 'TON', '11-15种25/吨'),
(@rid, @wid, 4, 'TON', 16, 20, 30, 'TON', '16-20种30/吨'),
(@rid, @wid, 5, 'TON', 21, NULL, 38, 'TON', '21种及以上38/吨');

CALL reset_color_fee_tiers('ZLX-XH');
INSERT INTO erp_warehouse_color_fee_tier(rate_id, warehouse_id, line_no, range_unit, range_start, range_end, fee_amount, fee_unit, remark)
VALUES
(@rid, @wid, 1, 'TON', 2, 5, 10, 'TON', '2-5种10/吨'),
(@rid, @wid, 2, 'TON', 6, 10, 15, 'TON', '6-10种15/吨'),
(@rid, @wid, 3, 'TON', 11, 15, 20, 'TON', '11-15种20/吨'),
(@rid, @wid, 4, 'TON', 16, 20, 25, 'TON', '16-20种25/吨'),
(@rid, @wid, 5, 'TON', 21, NULL, 50, 'TON', '21种及以上50/吨');

CALL reset_color_fee_tiers('ZLX-DT');
INSERT INTO erp_warehouse_color_fee_tier(rate_id, warehouse_id, line_no, range_unit, range_start, range_end, fee_amount, fee_unit, remark)
SELECT @rid, @wid, t.line_no, t.range_unit, t.range_start, t.range_end, t.fee_amount, t.fee_unit, t.remark
FROM erp_warehouse_color_fee_tier t
JOIN erp_warehouse_fee_rate r ON r.id = t.rate_id
JOIN erp_warehouse w ON w.id = r.warehouse_id
WHERE w.warehouse_code = 'ZLX-XH' AND r.effective_date = @effective_date;

CALL reset_color_fee_tiers('SH-WW-WC');
INSERT INTO erp_warehouse_color_fee_tier(rate_id, warehouse_id, line_no, range_unit, range_start, range_end, fee_amount, fee_unit, remark)
VALUES
(@rid, @wid, 1, 'BOX', 3, 7, 0.30, 'BOX', '3-7品0.3元/箱'),
(@rid, @wid, 2, 'BOX', 8, 20, 0.50, 'BOX', '8-20品0.5元/箱');

CALL reset_color_fee_tiers('WH0003');
INSERT INTO erp_warehouse_color_fee_tier(rate_id, warehouse_id, line_no, range_unit, range_start, range_end, fee_amount, fee_unit, remark)
VALUES
(@rid, @wid, 1, 'CONTAINER', 2, 2, 200, 'CONTAINER', '2色200元/柜'),
(@rid, @wid, 2, 'CONTAINER', 3, 5, 300, 'CONTAINER', '3-5色300元/柜'),
(@rid, @wid, 3, 'CONTAINER', 6, 8, 500, 'CONTAINER', '6-8色500元/柜'),
(@rid, @wid, 4, 'CONTAINER', 9, 10, 800, 'CONTAINER', '9-10色800元/柜'),
(@rid, @wid, 5, 'CONTAINER', 11, 20, 1000, 'CONTAINER', '11-20色1000元/柜'),
(@rid, @wid, 6, 'CONTAINER', 21, 25, 1500, 'CONTAINER', '21-25色1500元/柜');

CALL reset_color_fee_tiers('DY-XH');
INSERT INTO erp_warehouse_color_fee_tier(rate_id, warehouse_id, line_no, range_unit, range_start, range_end, fee_amount, fee_unit, remark)
VALUES
(@rid, @wid, 1, 'TON', 2, 5, 10, 'TON', '2-5种10/吨'),
(@rid, @wid, 2, 'TON', 6, 9, 15, 'TON', '6-9种15/吨'),
(@rid, @wid, 3, 'TON', 10, 15, 20, 'TON', '10-15种20/吨'),
(@rid, @wid, 4, 'TON', 16, NULL, 30, 'TON', '16以上30/吨');

CALL reset_color_fee_tiers('DY-DT');
INSERT INTO erp_warehouse_color_fee_tier(rate_id, warehouse_id, line_no, range_unit, range_start, range_end, fee_amount, fee_unit, remark)
SELECT @rid, @wid, t.line_no, t.range_unit, t.range_start, t.range_end, t.fee_amount, t.fee_unit, t.remark
FROM erp_warehouse_color_fee_tier t
JOIN erp_warehouse_fee_rate r ON r.id = t.rate_id
JOIN erp_warehouse w ON w.id = r.warehouse_id
WHERE w.warehouse_code = 'DY-XH' AND r.effective_date = @effective_date;

DROP PROCEDURE IF EXISTS reset_color_fee_tiers;
