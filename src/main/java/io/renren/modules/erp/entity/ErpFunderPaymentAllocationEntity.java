package io.renren.modules.erp.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

@Data
@TableName("erp_funder_payment_allocation")
public class ErpFunderPaymentAllocationEntity implements Serializable {
  private static final long serialVersionUID = 1L;

  @TableId
  private Long id;
  private Long paymentId;
  private Long presaleOrderId;
  private String presaleOrderNo;
  private String sellerContractNo;
  @TableField(exist = false)
  private BigDecimal orderContractAmount;
  private BigDecimal allocationAmount;
  private BigDecimal xianmuContributionRecognizedAmount;
  private BigDecimal xianmuContributionModifiedAmount;
  @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
  private Date xianmuContributionDate;
  private String xianmuContributionFilePath;
  private String xianmuContributionFileName;
  private String xianmuContributionRawText;
  private BigDecimal xianmuDepositRecognizedAmount;
  private BigDecimal xianmuDepositModifiedAmount;
  @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
  private Date xianmuDepositDate;
  private String xianmuDepositFilePath;
  private String xianmuDepositFileName;
  private String xianmuDepositRawText;
  private BigDecimal xianmuBalanceRecognizedAmount;
  private BigDecimal xianmuBalanceModifiedAmount;
  @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
  private Date xianmuBalanceDate;
  private String xianmuBalanceFilePath;
  private String xianmuBalanceFileName;
  private String xianmuBalanceRawText;
  private Date createTime;
  private Date updateTime;
}
