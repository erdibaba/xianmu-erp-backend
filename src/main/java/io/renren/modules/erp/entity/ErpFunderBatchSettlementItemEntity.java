package io.renren.modules.erp.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

@Data
@TableName("erp_funder_batch_settlement_item")
public class ErpFunderBatchSettlementItemEntity implements Serializable {
  private static final long serialVersionUID = 1L;

  @TableId
  private Long id;
  private Long settlementId;
  private Long outboundBatchId;
  private Long planItemId;
  private Long saleOrderItemId;
  private Long loanId;
  private Long confirmId;
  private String confirmContractNo;
  private Integer lineNo;
  private Long productId;
  private String productCode;
  private String productName;
  private String containerNo;
  private String factoryNo;
  private Integer shippedBoxes;
  private BigDecimal shippedWeight;
  private BigDecimal unitPriceInclTax;
  private BigDecimal costAmount;
  private BigDecimal systemPrincipalAmount;
  private BigDecimal confirmedPrincipalAmount;
  private Integer loanDays;
  private BigDecimal interestAmount;
  private BigDecimal storageFeeAmount;
  private BigDecimal handlingFeeAmount;
  private BigDecimal codeScanFeeAmount;
  private BigDecimal stampTaxAmount;
  private BigDecimal depositAmount;
  private BigDecimal taxAdjustAmount;
  private BigDecimal grossWeightFeeAmount;
  private BigDecimal otherFeeAmount;
  private BigDecimal expectedPaymentAmount;
  private String remark;
  private Date createTime;
  private Date updateTime;
}
