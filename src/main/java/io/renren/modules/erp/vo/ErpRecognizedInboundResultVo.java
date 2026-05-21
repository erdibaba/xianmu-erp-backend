package io.renren.modules.erp.vo;

import java.io.Serializable;
import lombok.Data;

@Data
public class ErpRecognizedInboundResultVo implements Serializable {
  private static final long serialVersionUID = 1L;

  private Boolean success;
  private String docType;
  private String rawText;
  private String message;
  private ErpRecognizedInboundDraftVo inboundDraft;
}
