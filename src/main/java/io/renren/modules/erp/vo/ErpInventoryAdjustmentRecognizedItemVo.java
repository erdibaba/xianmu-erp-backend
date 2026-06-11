package io.renren.modules.erp.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

@Data
public class ErpInventoryAdjustmentRecognizedItemVo implements Serializable {
  private static final long serialVersionUID = 1L;

  private Boolean matched;
  private String matchMessage;
  private String recognizedProductCode;
  private String recognizedSkuCode;
  private String recognizedProductName;
  private String recognizedFactoryNo;
  private String recognizedContainerNo;
  private Integer recognizedExpectedQty;
  private Integer recognizedActualQty;
  private BigDecimal recognizedSpecWeight;
  private BigDecimal recognizedTotalWeightKg;
  private Date recognizedInboundDate;
  private Long sourceAdjustmentItemId;
  private Long inboundOrderId;
  private Long inboundItemId;
  private Long packingItemId;
  private Long batchId;
  private Long productId;
  private String productCode;
  private String productName;
  private String productNameEn;
  private String productSpec;
  private Long warehouseId;
  private String warehouseName;
  private String containerNo;
  private String factoryNo;
  private String temperatureZone;
  private Date productionDate;
  private Date expiryDate;
  private Integer availableBoxes;
  private BigDecimal availableWeightKg;
}
