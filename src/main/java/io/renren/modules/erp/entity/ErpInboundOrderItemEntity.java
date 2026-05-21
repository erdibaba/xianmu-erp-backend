package io.renren.modules.erp.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

@Data
@TableName("erp_inbound_order_item")
public class ErpInboundOrderItemEntity implements Serializable {
  private static final long serialVersionUID = 1L;

  @TableId
  private Long id;
  private Long inboundOrderId;
  private Integer lineNo;
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
  private Date createTime;
  private Date updateTime;
}
