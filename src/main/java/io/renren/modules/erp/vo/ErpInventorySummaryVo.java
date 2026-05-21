package io.renren.modules.erp.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

@Data
public class ErpInventorySummaryVo implements Serializable {
  private static final long serialVersionUID = 1L;

  private Long productId;
  private String productCode;
  private String productName;
  private String productSpec;
  private String batchNo;
  private Date earliestBizDate;
  private Date nearestExpiryDate;
  private BigDecimal totalInPieces;
  private BigDecimal totalOutPieces;
  private BigDecimal stockPieces;
  private String warehouseName;
  private BigDecimal totalInQuantity;
  private BigDecimal totalOutQuantity;
  private BigDecimal stockQuantity;
  private BigDecimal lastUnitPrice;
  private BigDecimal safeStockBoxes;
  private Integer freshnessWarningDays;
  private Integer safetyWarning;
  private Integer freshnessWarning;
}
