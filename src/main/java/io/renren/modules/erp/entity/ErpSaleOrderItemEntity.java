package io.renren.modules.erp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

@Data
@TableName("erp_sale_order_item")
public class ErpSaleOrderItemEntity implements Serializable {
  private static final long serialVersionUID = 1L;

  @TableId
  private Long id;
  private Long saleOrderId;
  private Integer lineNo;
  private String saleType;
  private Long productId;
  private String productCode;
  private String productName;
  private String productNameEn;
  private String marketCirculationName;
  private String productSpec;
  private String unit;
  private Integer boxes;
  private Long sourcePresaleOrderId;
  private String sourcePresaleOrderNo;
  private Long sourcePresaleOrderItemId;
  private Long sourceInboundOrderId;
  private Long sourceInboundItemId;
  private String sourceContainerNo;
  private Long warehouseId;
  private String warehouseName;
  private Long brandId;
  private String brandName;
  private Date inboundDate;
  private Date productionDate;
  private Date expiryDate;
  private BigDecimal specWeight;
  private BigDecimal salePriceKg;
  private BigDecimal contractQuantityKg;
  private String contractFactoryNo;
  private String contractPortCold;
  private String remark;
  private Date createTime;
  private Date updateTime;

  @TableField(exist = false)
  private String ownershipName;
}
