package io.renren.modules.erp.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

@Data
public class ErpFuturesInventoryVo implements Serializable {
  private static final long serialVersionUID = 1L;

  private Long presaleOrderId;
  private Long packingId;
  private Long packingItemId;
  private String presaleOrderNo;
  private String contractNo;
  private String customerReference;
  private Long brandId;
  private String brandName;
  private String containerNo;
  private Date orderDate;
  private Date expectedArrivalDate;
  private Long productId;
  private String productCode;
  private String productName;
  private String productNameEn;
  private Integer futuresBoxes;
  private Integer futuresSoldBoxes;
  private Integer futuresAvailableBoxes;
  private Integer transferredSpotBoxes;
  private Integer notInboundBoxes;
  private BigDecimal totalWeightKg;
  private Integer shelfLifeDays;
  private Date productionDate;
  private Date expiryDate;
  private String transferStatus;
  private Integer freshnessWarningDays;
  private Integer freshnessWarning;
}
