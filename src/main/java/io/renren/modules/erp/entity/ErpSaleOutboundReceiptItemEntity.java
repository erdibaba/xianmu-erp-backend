package io.renren.modules.erp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

@Data
@TableName("erp_sale_outbound_receipt_item")
public class ErpSaleOutboundReceiptItemEntity implements Serializable {
  private static final long serialVersionUID = 1L;

  @TableId
  private Long id;
  private Long receiptId;
  private Long saleOrderId;
  private Long batchId;
  private String wmsOrderNo;
  private String outboundOrderNo;
  private String customerCode;
  private String customerName;
  private Integer lineNo;
  private Long productId;
  private String productCode;
  private String recognizedProductCode;
  private String productName;
  private String productNameEn;
  private String productSpec;
  private String unit;
  private Integer orderQty;
  private Integer shippedQty;
  private String containerNo;
  private String factoryNo;
  private BigDecimal avgWeight;
  private BigDecimal totalWeight;
  private Date createTime;
  private Date updateTime;

  @TableField(exist = false)
  private String contractNo;
  @TableField(exist = false)
  private String expectedFactoryNo;
  @TableField(exist = false)
  private String expectedContainerNo;
  @TableField(exist = false)
  private Integer expectedBoxes;
  @TableField(exist = false)
  private BigDecimal expectedWeight;
  @TableField(exist = false)
  private BigDecimal salePriceKg;
  @TableField(exist = false)
  private Integer diffBoxes;
  @TableField(exist = false)
  private BigDecimal diffWeight;
  @TableField(exist = false)
  private BigDecimal adjustmentAmount;
}
