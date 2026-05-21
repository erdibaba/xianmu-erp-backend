package io.renren.modules.erp.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

@Data
@TableName("erp_stock_ledger")
public class ErpStockLedgerEntity implements Serializable {
  private static final long serialVersionUID = 1L;

  @TableId
  private Long id;
  private Long orderId;
  private Long orderItemId;
  private String orderType;
  private String orderNo;
  private String relatedOrderNo;
  private String bizType;
  private Long productId;
  private String productCode;
  private String productName;
  private String productSpec;
  private Long warehouseId;
  private String warehouseName;
  private BigDecimal inQuantity;
  private BigDecimal outQuantity;
  private BigDecimal inPieces;
  private BigDecimal outPieces;
  private BigDecimal lossWeight;
  private BigDecimal unitPrice;
  private Date bizDate;
  private Date expiryDate;
  private Long createUserId;
  private Date createTime;
}
