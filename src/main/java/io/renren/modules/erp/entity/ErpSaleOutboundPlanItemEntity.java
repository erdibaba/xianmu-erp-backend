package io.renren.modules.erp.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

@Data
@TableName("erp_sale_outbound_plan_item")
public class ErpSaleOutboundPlanItemEntity implements Serializable {
  private static final long serialVersionUID = 1L;

  @TableId
  private Long id;
  private Long batchId;
  private Long saleOrderId;
  private Long saleOrderItemId;
  private Integer lineNo;
  private String ownershipName;
  private Long productId;
  private String productCode;
  private String productName;
  private String productNameEn;
  private String containerNo;
  private String factoryNo;
  private Integer plannedBoxes;
  private BigDecimal plannedWeight;
  private BigDecimal salePriceKg;
  private Date createTime;
  private Date updateTime;

  @TableField(exist = false)
  private Integer actualBoxes;
  @TableField(exist = false)
  private BigDecimal actualWeight;
  @TableField(exist = false)
  private Integer diffBoxes;
  @TableField(exist = false)
  private BigDecimal diffWeight;
  @TableField(exist = false)
  private BigDecimal adjustmentAmount;
}
