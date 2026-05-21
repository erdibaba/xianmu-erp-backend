package io.renren.modules.erp.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

@Data
@TableName("erp_trade_order_item")
public class ErpTradeOrderItemEntity implements Serializable {
  private static final long serialVersionUID = 1L;

  @TableId
  private Long id;
  private Long orderId;
  private Long sourceOrderItemId;
  private Integer lineNo;
  private Long productId;
  private String productCode;
  private String productName;
  private String productSpec;
  private String unit;
  private String batchNo;
  private String sourceContainerNo;
  private String warehouseName;
  private BigDecimal quantity;
  private BigDecimal pieceCount;
  private BigDecimal actualPieceCount;
  private BigDecimal estimatedWeight;
  private BigDecimal actualInWeight;
  private BigDecimal actualOutWeight;
  private BigDecimal lossWeight;
  private BigDecimal unitPrice;
  private BigDecimal amount;
  private BigDecimal taxRate;
  private BigDecimal taxAmount;
  private BigDecimal totalAmount;
  private Integer shelfLifeDays;
  private Date productionDate;
  private Date expiryDate;
  private String remark;
  private Date createTime;
  private Date updateTime;
}
