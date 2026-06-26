package io.renren.modules.erp.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
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
  private Date productionDate;
  private Date expiryDate;
  private Integer availableBoxes;
  private BigDecimal sourceAmount;
  private BigDecimal allocatedAmount;
  private BigDecimal basisWeightKg;
  private BigDecimal totalBasisWeightKg;
  private String remark;
}
