SET @schema_name := DATABASE();

SET @sql := IF(
  EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'erp_partner' AND column_name = 'risk_level'),
  'SELECT 1',
  'ALTER TABLE erp_partner ADD COLUMN risk_level VARCHAR(32) NOT NULL DEFAULT ''NORMAL'' COMMENT ''风险标记 NORMAL-正常 WATCH-关注 DEFAULTED-违约 BLACKLIST-黑名单'' AFTER wecom_chat_owner'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := IF(
  EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'erp_partner' AND column_name = 'risk_remark'),
  'SELECT 1',
  'ALTER TABLE erp_partner ADD COLUMN risk_remark VARCHAR(500) NULL COMMENT ''风险说明'' AFTER risk_level'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := IF(
  EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema = @schema_name AND table_name = 'erp_partner' AND column_name = 'risk_mark_date'),
  'SELECT 1',
  'ALTER TABLE erp_partner ADD COLUMN risk_mark_date DATE NULL COMMENT ''风险标记日期'' AFTER risk_remark'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE erp_partner
SET risk_level = 'NORMAL'
WHERE risk_level IS NULL OR risk_level = '';
