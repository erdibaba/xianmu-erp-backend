package io.renren.modules.erp.vo;

import java.io.Serializable;
import lombok.Data;

@Data
public class ErpRecognizedInboundFileVo implements Serializable {
  private static final long serialVersionUID = 1L;

  private String filePath;
  private String fileName;
}
