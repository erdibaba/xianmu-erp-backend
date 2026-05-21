package io.renren.modules.erp.vo;

import java.io.Serializable;
import lombok.Data;

@Data
public class ErpRecognizedOrderExpenseVo implements Serializable {
  private static final long serialVersionUID = 1L;

  private String expenseType;
  private String expenseName;
  private String amount;
  private String taxRate;
  private String remark;
}
