package io.renren.modules.erp.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import lombok.Data;

@Data
public class ErpInventoryAdjustmentRequest implements Serializable {
  private static final long serialVersionUID = 1L;

  private String adjustmentType;
  private String remark;
  private List<Item> itemList;
  private List<ErpRecognizedInboundFileVo> fileList;

  @Data
  public static class Item implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long sourceAdjustmentItemId;
    private Long sourceInboundOrderId;
    private Long sourceInboundItemId;
    private Long sourcePackingItemId;
    private Long sourceBatchId;
    private Long productId;
    private Long targetWarehouseId;
    private String targetWarehouseName;
    private Date targetExpiryDate;
    private Integer transferBoxes;
    private BigDecimal transferWeightKg;
    private String remark;
  }
}
