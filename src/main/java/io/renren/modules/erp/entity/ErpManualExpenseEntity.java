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
@TableName("erp_manual_expense")
public class ErpManualExpenseEntity implements Serializable {
  private static final long serialVersionUID = 1L;

  @TableId
  private Long id;
  private String expenseNo;
  @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
  private Date expenseDate;
  private String expenseType;
  private String expenseName;
  private BigDecimal amount;
  private String remark;
  private Long createUserId;
  private Date createTime;
  private Date updateTime;

  @TableField(exist = false)
  private List<ErpManualExpenseFileEntity> fileList = new ArrayList<ErpManualExpenseFileEntity>();
}
