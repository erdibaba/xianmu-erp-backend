package io.renren.modules.erp.vo;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

@Data
public class ErpSalePresaleOrderVo implements Serializable {
  private static final long serialVersionUID = 1L;

  private Long presaleOrderId;
  private String presaleOrderNo;
  private String sellerContractNo;
  private String customerReference;
  private String brandName;
  private Date orderDate;
}
