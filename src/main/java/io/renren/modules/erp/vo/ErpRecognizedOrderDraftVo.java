package io.renren.modules.erp.vo;

import java.io.Serializable;
import java.util.List;
import lombok.Data;

@Data
public class ErpRecognizedOrderDraftVo implements Serializable {
  private static final long serialVersionUID = 1L;

  private String orderType;
  private String partnerName;
  private String brandName;
  private String buyerPartnerName;
  private String contractNo;
  private String containerNo;
  private String warehouseName;
  private String orderDate;
  private String expectedDate;
  private String paymentDueDate;
  private String currency;
  private Integer status;
  private String bizType;
  private String orderSource;
  private String docStage;
  private String totalAmount;
  private String remark;
  private String sourceType;
  private List<ErpRecognizedOrderItemVo> itemList;
  private List<ErpRecognizedOrderExpenseVo> expenseList;
}
