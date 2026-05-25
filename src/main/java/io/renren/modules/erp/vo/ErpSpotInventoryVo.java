package io.renren.modules.erp.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

@Data
public class ErpSpotInventoryVo implements Serializable {
  private static final long serialVersionUID = 1L;

  private Long inboundOrderId;
  private Long inboundItemId;
  private Long presaleOrderId;
  private String contractNo;
  private String customerName;
  private Long warehouseId;
  private String warehouseName;
  private String containerNo;
  private Long productId;
  private String productCode;
  private String productName;
  private String productNameEn;
  private String productSpec;
  private String unit;
  private String temperatureZone;
  private Date inboundDate;
  private Date productionDate;
  private Date expiryDate;
  private Integer expectedBoxes;
  private Integer packingBoxes;
  private Integer inboundBoxes;
  private Integer allocatedBoxes;
  private Integer outboundBoxes;
  private Integer availableBoxes;
  private BigDecimal specWeight;
  private BigDecimal inboundWeightKg;
  private BigDecimal allocatedWeightKg;
  private BigDecimal availableWeightKg;
  private BigDecimal damageWeightKg;
  private Integer freshnessWarningDays;
  private Integer freshnessWarning;
}
