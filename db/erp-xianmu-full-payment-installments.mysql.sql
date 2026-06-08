-- 预销售单打款管理：鲜牧全款按合同号记录定金与尾款凭证。
-- 状态说明：erp_funder_payment.status 0-待尾款 1-已确认。

ALTER TABLE erp_funder_payment_allocation
  ADD COLUMN xianmu_deposit_recognized_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '鲜牧全款定金OCR识别金额' AFTER xianmu_contribution_raw_text,
  ADD COLUMN xianmu_deposit_modified_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '鲜牧全款定金确认金额' AFTER xianmu_deposit_recognized_amount,
  ADD COLUMN xianmu_deposit_date DATE NULL COMMENT '鲜牧全款定金打款日期' AFTER xianmu_deposit_modified_amount,
  ADD COLUMN xianmu_deposit_file_path VARCHAR(500) NULL COMMENT '鲜牧全款定金凭证归档路径' AFTER xianmu_deposit_date,
  ADD COLUMN xianmu_deposit_file_name VARCHAR(255) NULL COMMENT '鲜牧全款定金凭证原文件名' AFTER xianmu_deposit_file_path,
  ADD COLUMN xianmu_deposit_raw_text LONGTEXT NULL COMMENT '鲜牧全款定金凭证OCR原文' AFTER xianmu_deposit_file_name,
  ADD COLUMN xianmu_balance_recognized_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '鲜牧全款尾款OCR识别金额' AFTER xianmu_deposit_raw_text,
  ADD COLUMN xianmu_balance_modified_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '鲜牧全款尾款确认金额' AFTER xianmu_balance_recognized_amount,
  ADD COLUMN xianmu_balance_date DATE NULL COMMENT '鲜牧全款尾款打款日期' AFTER xianmu_balance_modified_amount,
  ADD COLUMN xianmu_balance_file_path VARCHAR(500) NULL COMMENT '鲜牧全款尾款凭证归档路径' AFTER xianmu_balance_date,
  ADD COLUMN xianmu_balance_file_name VARCHAR(255) NULL COMMENT '鲜牧全款尾款凭证原文件名' AFTER xianmu_balance_file_path,
  ADD COLUMN xianmu_balance_raw_text LONGTEXT NULL COMMENT '鲜牧全款尾款凭证OCR原文' AFTER xianmu_balance_file_name;

CREATE INDEX idx_funder_alloc_xianmu_deposit_file ON erp_funder_payment_allocation(xianmu_deposit_file_path);
CREATE INDEX idx_funder_alloc_xianmu_balance_file ON erp_funder_payment_allocation(xianmu_balance_file_path);
