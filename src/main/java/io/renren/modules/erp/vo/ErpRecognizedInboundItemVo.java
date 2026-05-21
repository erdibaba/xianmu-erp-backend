package io.renren.modules.erp.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

@Data
public class ErpRecognizedInboundItemVo implements Serializable {
  private static final long serialVersionUID = 1L;

  private Long productId;
  private String productCode;
  private String skuCode;
  private String productName;
  private String productNameEn;
  private String productSpec;
  private String unit;
  private Integer expectedQty;
  private Integer actualQty;
  private Integer packingBoxes;
  private String temperatureZone;
  private Date productionDate;
  private Date expiryDate;
  private Integer shelfLifeDays;
  private BigDecimal specWeight;
}
