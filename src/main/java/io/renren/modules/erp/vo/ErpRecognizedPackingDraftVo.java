package io.renren.modules.erp.vo;

import java.io.Serializable;
import java.util.List;
import lombok.Data;

@Data
public class ErpRecognizedPackingDraftVo implements Serializable {
  private static final long serialVersionUID = 1L;

  private String contractNo;
  private String containerNo;
  private String shelfLifeDays;
  private String totalBoxes;
  private String totalWeight;
  private List<ErpRecognizedPackingItemVo> itemList;
}
