package io.renren.modules.erp.vo;

import java.io.Serializable;
import lombok.Data;

@Data
public class ErpRecognizedPackingBatchVo implements Serializable {
  private static final long serialVersionUID = 1L;

  private String productionDate;
  private String expiryDate;
  private String boxCount;
  private String weight;
}
