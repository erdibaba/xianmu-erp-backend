package io.renren.modules.erp.vo;

import java.io.Serializable;
import java.util.List;
import lombok.Data;

@Data
public class ErpRecognizedPackingItemVo implements Serializable {
  private static final long serialVersionUID = 1L;

  private String productCode;
  private String sourceProductCode;
  private String productName;
  private String productNameEn;
  private String totalBoxes;
  private String totalWeight;
  private String shelfLifeDays;
  private List<ErpRecognizedPackingBatchVo> batchList;
}
