package io.renren.modules.erp.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

@Data
@TableName("erp_funder_loan_repayment")
public class ErpFunderLoanRepaymentEntity implements Serializable {
  private static final long serialVersionUID = 1L;

  @TableId
  private Long id;
  private String repaymentNo;
  private Long loanId;
  private Integer lineNo;
  private BigDecimal repaymentPrincipal;
  private BigDecimal annualInterestRate;
  private Integer loanDays;
  private BigDecimal interestAmount;
  private BigDecimal handlingFeeAmount;
  private String handlingFeeReason;
  private BigDecimal expectedPaymentAmount;
  private BigDecimal recognizedAmount;
  private BigDecimal modifiedAmount;
  @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
  private Date repaymentDate;
  private Integer amountMatched;
  private String filePath;
  private String fileName;
  private String rawText;
  private Integer status;
  private String remark;
  private Long createUserId;
  private Date createTime;
  private Date updateTime;
}
