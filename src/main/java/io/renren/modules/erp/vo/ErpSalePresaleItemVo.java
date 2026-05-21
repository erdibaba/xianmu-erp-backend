package io.renren.modules.erp.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

@Data
public class ErpSalePresaleItemVo implements Serializable {
  private static final long serialVersionUID = 1L;

  private Long presaleOrderId;
  private Long presaleOrderItemId;
  private String presaleOrderNo;
  private String sellerContractNo;
  private String customerReference;
  private Long productId;
  private String productCode;
  private String productName;
  private String productNameEn;
  private BigDecimal quantityTon;
  private BigDecimal quantityKg;
  private Date orderDate;
}
