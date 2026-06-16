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
@TableName("erp_funder_payment")
public class ErpFunderPaymentEntity implements Serializable {
  private static final long serialVersionUID = 1L;

  @TableId
  private Long id;
  private String paymentNo;
  private Integer paymentType;
  private Long payerId;
  private String payerName;
  private Long funderId;
  private String funderName;
  private BigDecimal recognizedAmount;
  private BigDecimal modifiedAmount;
  @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
  private Date paymentDate;
  private String filePath;
  private String fileName;
  private String rawText;
  private Integer status;
  private String remark;
  private Long createUserId;
  private Date createTime;
  private Date updateTime;

  @TableField(exist = false)
  private String sellerContractNos;

  @TableField(exist = false)
  private List<ErpFunderPaymentAllocationEntity> allocationList;
}
