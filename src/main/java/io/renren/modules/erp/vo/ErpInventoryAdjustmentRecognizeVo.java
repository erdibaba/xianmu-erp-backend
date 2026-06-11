package io.renren.modules.erp.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class ErpInventoryAdjustmentRecognizeVo implements Serializable {
  private static final long serialVersionUID = 1L;

  private Boolean success;
  private String rawText;
  private List<ErpRecognizedInboundFileVo> fileList = new ArrayList<ErpRecognizedInboundFileVo>();
  private List<ErpInventoryAdjustmentRecognizedItemVo> itemList = new ArrayList<ErpInventoryAdjustmentRecognizedItemVo>();
}
