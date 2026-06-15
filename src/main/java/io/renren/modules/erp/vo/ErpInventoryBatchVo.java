package io.renren.modules.erp.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

@Data
public class ErpInventoryBatchVo implements Serializable {
  private static final long serialVersionUID = 1L;

  private Long batchId;
  private Long packingItemId;
  private Long inboundOrderId;
  private Long inboundItemId;
  private Long sourceAdjustmentItemId;
  private Long presaleOrderId;
  private String presaleOrderNo;
  private String contractNo;
  private String customerName;
  private String customerReference;
  private String brandName;
  private String warehouseName;
  private Long warehouseId;
  private String containerNo;
  private Long productId;
  private String productCode;
  private String productName;
  private String productNameEn;
  private String ownershipName;
  private String productSpec;
  private String factoryNo;
  private String skuCode;
  private String temperatureZone;
  private Date expectedArrivalDate;
  private Date productionDate;
  private Date expiryDate;
  private Integer boxes;
  private Integer packingBoxes;
  private Integer inboundBoxes;
  private Integer allocatedBoxes;
  private Integer availableBoxes;
  private Integer futuresBoxes;
  private Integer futuresSoldBoxes;
  private Integer futuresAvailableBoxes;
  private BigDecimal weightKg;
  private BigDecimal inboundWeightKg;
  private BigDecimal allocatedWeightKg;
  private BigDecimal damageWeightKg;
  private BigDecimal availableWeightKg;
  private BigDecimal totalWeightKg;
  private BigDecimal soldWeightKg;
  private Integer shelfLifeDays;
  private Integer freshnessWarningDays;
  private Integer freshnessWarning;
  private String lotType;
}
