package io.renren.modules.erp.entity;

import com.baomidou.mybatisplus.annotation.TableId;
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
  private BigDecimal allocationAmount;
  private BigDecimal xianmuContributionRecognizedAmount;
  private BigDecimal xianmuContributionModifiedAmount;
  @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
  private Date xianmuContributionDate;
  private String xianmuContributionFilePath;
  private String xianmuContributionFileName;
  private String xianmuContributionRawText;
  private Date createTime;
  private Date updateTime;
}
