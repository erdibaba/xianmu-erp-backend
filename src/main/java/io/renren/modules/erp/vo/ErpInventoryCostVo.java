package io.renren.modules.erp.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class ErpInventoryCostVo implements Serializable {
  private static final long serialVersionUID = 1L;

  private Long productId;
  private String productCode;
  private String productName;
  private String productNameEn;
  private String ownershipName;
  private Integer availableBoxes;
  private BigDecimal availableWeightKg;
  private BigDecimal purchaseAmount;
  private BigDecimal allocatedFeeAmount;
  private BigDecimal totalCostAmount;
  private BigDecimal costPriceKg;
  private BigDecimal purchasePriceKg;
  private Integer costDetailCount;
}
