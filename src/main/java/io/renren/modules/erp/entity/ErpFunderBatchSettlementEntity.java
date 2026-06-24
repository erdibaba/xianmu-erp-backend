package io.renren.modules.erp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.Data;

@Data
@TableName("erp_funder_batch_settlement")
public class ErpFunderBatchSettlementEntity implements Serializable {
  private static final long serialVersionUID = 1L;

  @TableId
  private Long id;
  private String settlementNo;
  private Long outboundBatchId;
  private Long saleOrderId;
  private String batchNo;
  private String saleOrderNo;
  private Long funderId;
  private String funderName;
  private String ruleType;
  private String ownershipName;
  @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
  private Date settlementDate;
  private BigDecimal systemPrincipalAmount;
  private BigDecimal confirmedPrincipalAmount;
  private BigDecimal interestAmount;
  private BigDecimal storageFeeAmount;
  private BigDecimal handlingFeeAmount;
  private BigDecimal codeScanFeeAmount;
  private BigDecimal stampTaxAmount;
  private BigDecimal depositAmount;
  private BigDecimal taxAdjustAmount;
  private BigDecimal grossWeightFeeAmount;
  private Integer includeCodeScanFee;
  private BigDecimal otherFeeAmount;
  private BigDecimal expectedPaymentAmount;
  private BigDecimal recognizedPaymentAmount;
  private BigDecimal confirmedPaymentAmount;
  private String filePath;
  private String fileName;
  private String rawText;
  private Integer status;
  private String remark;
  private Long createUserId;
  private Date createTime;
  private Date updateTime;

  @TableField(exist = false)
  private List<ErpFunderBatchSettlementItemEntity> itemList = new ArrayList<ErpFunderBatchSettlementItemEntity>();
}
