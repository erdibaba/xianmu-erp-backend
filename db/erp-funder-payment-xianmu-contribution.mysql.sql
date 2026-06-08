-- 预销售单打款管理：资方全款按合同号记录鲜牧出资款。
-- 贷款本金 = 资方全款分摊金额 - 鲜牧出资款确认金额。

ALTER TABLE erp_funder_payment_allocation
  ADD COLUMN xianmu_contribution_recognized_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '鲜牧出资款OCR识别金额' AFTER allocation_amount,
  ADD COLUMN xianmu_contribution_modified_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '鲜牧出资款确认金额' AFTER xianmu_contribution_recognized_amount,
  ADD COLUMN xianmu_contribution_date DATE NULL COMMENT '鲜牧出资款打款日期' AFTER xianmu_contribution_modified_amount,
  ADD COLUMN xianmu_contribution_file_path VARCHAR(500) NULL COMMENT '鲜牧出资款凭证归档路径' AFTER xianmu_contribution_date,
  ADD COLUMN xianmu_contribution_file_name VARCHAR(255) NULL COMMENT '鲜牧出资款凭证原文件名' AFTER xianmu_contribution_file_path,
  ADD COLUMN xianmu_contribution_raw_text LONGTEXT NULL COMMENT '鲜牧出资款凭证OCR原文' AFTER xianmu_contribution_file_name;

CREATE INDEX idx_funder_alloc_xianmu_file ON erp_funder_payment_allocation(xianmu_contribution_file_path);
