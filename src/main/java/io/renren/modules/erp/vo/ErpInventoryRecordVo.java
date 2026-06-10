package io.renren.modules.erp.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

@Data
public class ErpInventoryRecordVo implements Serializable {
  private static final long serialVersionUID = 1L;

  private String recordType;
  private String recordTypeName;
  private Date bizDate;
  private String orderNo;
  private String contractNo;
  private String customerName;
  private String warehouseName;
  private String containerNo;
  private String factoryNo;
  private Long productId;
  private String productCode;
  private String productName;
  private String productNameEn;
  private String productSpec;
  private String unit;
  private Integer boxes;
  private BigDecimal weightKg;
  private String sourceRemark;
}
