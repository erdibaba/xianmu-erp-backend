package io.renren.modules.erp.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

@Data
@TableName("erp_expense")
public class ErpExpenseEntity implements Serializable {
  private static final long serialVersionUID = 1L;

  @TableId
  private Long id;
  private String expenseNo;
  private String expenseType;
  private String expenseName;
  private String sourceType;
  private Long sourceId;
  private Long sourceChildId;
  private String sourceNo;
  private Long saleOrderId;
  private String saleOrderNo;
  private Long inboundOrderId;
  private Long outboundBatchId;
  private Long presaleOrderId;
  private String contractNo;
  private Long partnerId;
  private String partnerName;
  private Long warehouseId;
  private String warehouseName;
  private String temperatureZone;
  @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
  private Date businessStartDate;
  @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
  private Date businessEndDate;
  private Integer freeDays;
  private Integer chargeDays;
  private BigDecimal weightKg;
  private BigDecimal weightTon;
  private BigDecimal rate;
  private BigDecimal amount;
  private BigDecimal taxRate;
  private BigDecimal taxAmount;
  private BigDecimal totalAmount;
  private Integer status;
  private String remark;
  private Long createUserId;
  private Date createTime;
  private Date updateTime;
}
