package io.renren.modules.erp.vo;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

@Data
public class ErpInboundListVo implements Serializable {
  private static final long serialVersionUID = 1L;

  private Long id;
  private Long presaleOrderId;
  private Long brandId;
  private String brandName;
  private String contractNo;
  private String customerName;
  private Long warehouseId;
  private String warehouseName;
  private Date orderDate;
  private Date expectedArrivalDate;
  private Integer uploadStatus;
}
