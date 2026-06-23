-- 客户订单确认函合同号唯一约束。
SET @has_unique_confirm_contract_index = (
  SELECT COUNT(*)
    FROM information_schema.statistics
   WHERE table_schema = DATABASE()
     AND table_name = 'erp_presale_confirm'
     AND index_name = 'uk_presale_confirm_contract_no'
);

SET @add_unique_confirm_contract_index = IF(
  @has_unique_confirm_contract_index = 0,
  'ALTER TABLE `erp_presale_confirm` ADD UNIQUE INDEX `uk_presale_confirm_contract_no` (`contract_no`)',
  'SELECT 1'
);

PREPARE add_unique_confirm_contract_index_stmt FROM @add_unique_confirm_contract_index;
EXECUTE add_unique_confirm_contract_index_stmt;
DEALLOCATE PREPARE add_unique_confirm_contract_index_stmt;
