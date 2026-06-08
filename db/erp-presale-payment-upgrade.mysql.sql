-- 预销售单打款管理：支持资方全款与鲜牧全款两种付款类型。
-- 说明：生产执行建议先确认字段是否存在；本脚本为结构留档。

ALTER TABLE erp_funder_payment
  ADD COLUMN payment_type TINYINT NOT NULL DEFAULT 1 COMMENT '付款类型 1-资方全款 2-鲜牧全款' AFTER payment_no,
  ADD COLUMN payer_id BIGINT NULL COMMENT '付款主体ID' AFTER payment_type,
  ADD COLUMN payer_name VARCHAR(200) NULL COMMENT '付款主体名称快照' AFTER payer_id;

ALTER TABLE erp_funder_payment
  MODIFY COLUMN funder_id BIGINT NULL COMMENT '资方ID，仅资方全款时有值',
  MODIFY COLUMN funder_name VARCHAR(200) NULL COMMENT '资方名称快照，仅资方全款时有值',
  MODIFY COLUMN payment_no VARCHAR(50) NOT NULL COMMENT '预销售单打款单号',
  MODIFY COLUMN recognized_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '凭证OCR识别金额',
  MODIFY COLUMN modified_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '用户确认金额',
  MODIFY COLUMN payment_date DATE NOT NULL COMMENT '打款日期',
  MODIFY COLUMN file_path VARCHAR(500) NOT NULL COMMENT '打款凭证归档路径',
  MODIFY COLUMN file_name VARCHAR(255) NULL COMMENT '打款凭证原文件名',
  MODIFY COLUMN raw_text LONGTEXT NULL COMMENT '打款凭证OCR原文',
  MODIFY COLUMN status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1-已确认',
  MODIFY COLUMN remark VARCHAR(500) NULL COMMENT '备注',
  COMMENT = '预销售单打款记录';

UPDATE erp_funder_payment
SET payment_type = IFNULL(payment_type, 1),
    payer_id = IFNULL(payer_id, funder_id),
    payer_name = IFNULL(payer_name, funder_name);

CREATE INDEX idx_funder_payment_type_date ON erp_funder_payment(payment_type, payment_date);
CREATE INDEX idx_funder_payment_payer_date ON erp_funder_payment(payer_id, payment_date);

UPDATE sys_menu
SET name = '预销售单打款管理'
WHERE menu_id = 79;
