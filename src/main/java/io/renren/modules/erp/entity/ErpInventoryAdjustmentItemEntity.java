package io.renren.modules.erp.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

@Data
@TableName("erp_inventory_adjustment_item")
public class ErpInventoryAdjustmentItemEntity implements Serializable {
  private static final long serialVersionUID = 1L;

  @TableId
  private Long id;
  private Long adjustmentId;
  private Integer lineNo;
  private String adjustmentType;
  private Long sourceAdjustmentItemId;
  private Long sourceInboundOrderId;
  private Long sourceInboundItemId;
  private Long sourcePackingItemId;
  private Long sourceBatchId;
  private Long productId;
  private String productCode;
  private String productName;
  private String productNameEn;
  private String productSpec;
  private String unit;
  private Long sourceWarehouseId;
  private String sourceWarehouseName;
  private Long targetWarehouseId;
  private String targetWarehouseName;
  private String containerNo;
  private String factoryNo;
  private String sourceTemperatureZone;
  private String targetTemperatureZone;
  private Date productionDate;
  private Date sourceExpiryDate;
  private Date targetExpiryDate;
  private Integer transferBoxes;
  private BigDecimal transferWeightKg;
  private String remark;
  private Date createTime;
  private Date updateTime;
}
