package io.renren.modules.erp.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

@Data
public class ErpInventoryBatchVo implements Serializable {
  private static final long serialVersionUID = 1L;

  private Long batchId;
  private Long packingItemId;
  private Date productionDate;
  private Date expiryDate;
  private Integer boxes;
  private BigDecimal weightKg;
  private Integer shelfLifeDays;
  private Integer freshnessWarningDays;
  private Integer freshnessWarning;
}
