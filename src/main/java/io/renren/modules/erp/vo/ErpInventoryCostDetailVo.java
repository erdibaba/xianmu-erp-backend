package io.renren.modules.erp.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class ErpInventoryCostDetailVo implements Serializable {
  private static final long serialVersionUID = 1L;

  private String costType;
  private String costName;
  private String sourceNo;
  private String contractNo;
  private String containerNo;
  private String factoryNo;
  private BigDecimal sourceAmount;
  private BigDecimal allocatedAmount;
  private BigDecimal basisWeightKg;
  private BigDecimal totalBasisWeightKg;
  private String remark;
}
