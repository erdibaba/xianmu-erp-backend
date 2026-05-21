package io.renren.modules.erp.vo;

import java.io.Serializable;
import lombok.Data;

@Data
public class ErpRecognizedOrderItemVo implements Serializable {
  private static final long serialVersionUID = 1L;

  private Long productId;
  private String productCode;
  private String sourceProductCode;
  private String productName;
  private String productNameEn;
  private String productSpec;
  private String warehouseName;
  private String quantity;
  private String quantityTon;
  private String quantityKg;
  private String unitPrice;
  private String priceAmount;
  private String priceCurrency;
  private String priceUnit;
  private String unit;
  private String unitPriceInclTax;
  private String lineTotalInclTax;
  private String taxRate;
  private String remark;
}
