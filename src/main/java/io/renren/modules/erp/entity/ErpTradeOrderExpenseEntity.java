package io.renren.modules.erp.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

@Data
@TableName("erp_trade_order_expense")
public class ErpTradeOrderExpenseEntity implements Serializable {
  private static final long serialVersionUID = 1L;

  @TableId
  private Long id;
  private Long orderId;
  private String expenseType;
  private String expenseName;
  private BigDecimal amount;
  private BigDecimal taxRate;
  private BigDecimal taxAmount;
  private BigDecimal totalAmount;
  private String remark;
  private Date createTime;
  private Date updateTime;
}
