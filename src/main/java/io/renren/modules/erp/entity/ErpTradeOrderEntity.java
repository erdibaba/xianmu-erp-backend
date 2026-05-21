package io.renren.modules.erp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import lombok.Data;

@Data
@TableName("erp_trade_order")
public class ErpTradeOrderEntity implements Serializable {
  private static final long serialVersionUID = 1L;

  @TableId
  private Long id;
  private String orderNo;
  private String orderType;
  private String bizType;
  private Long partnerId;
  private String partnerName;
  private Long brandId;
  private String brandName;
  private Long secondaryPartnerId;
  private String secondaryPartnerName;
  private Long funderId;
  private String funderName;
  private String contractNo;
  private String relatedContractNo;
  private String containerNo;
  private Long warehouseId;
  private String warehouseName;
  private Long sourceOrderId;
  private String sourceOrderNo;
  private Date storageStartDate;
  private Date orderDate;
  private Date expectedDate;
  private Date paymentDueDate;
  private Date actualOutDate;
  private String currency;
  private BigDecimal itemAmount;
  private BigDecimal expenseAmount;
  private BigDecimal taxAmount;
  private BigDecimal totalAmount;
  private Integer status;
  private Integer paymentStatus;
  private Integer invoiceStatus;
  private Integer autoOutbound;
  private Integer storageFeeStartDays;
  private String orderSource;
  private String remark;
  private Long createUserId;
  private Date createTime;
  private Date updateTime;

  @TableField(exist = false)
  private List<ErpTradeOrderItemEntity> itemList;

  @TableField(exist = false)
  private List<ErpTradeOrderExpenseEntity> expenseList;
}
