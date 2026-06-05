package io.renren.modules.erp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import lombok.Data;

@Data
@TableName("erp_funder_loan")
public class ErpFunderLoanEntity implements Serializable {
  private static final long serialVersionUID = 1L;

  @TableId
  private Long id;
  private String loanNo;
  private Long paymentId;
  private Long allocationId;
  private Long presaleOrderId;
  private String presaleOrderNo;
  private String sellerContractNo;
  private Long funderId;
  private String funderName;
  private BigDecimal loanAmount;
  private BigDecimal annualInterestRate;
  @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
  private Date loanDate;
  private BigDecimal repaidPrincipal;
  private BigDecimal remainingPrincipal;
  private BigDecimal interestAmount;
  private Integer status;
  private Date createTime;
  private Date updateTime;

  @TableField(exist = false)
  private List<ErpFunderLoanRepaymentEntity> repaymentList;
}
