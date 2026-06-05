package io.renren.modules.erp.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
  private Date createTime;
  private Date updateTime;
}
