package io.renren.modules.erp.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.Data;

@Data
public class ErpRecognizedInboundDraftVo implements Serializable {
  private static final long serialVersionUID = 1L;

  private Long presaleOrderId;
  private Long brandId;
  private String brandName;
  private String contractNo;
  private String customerName;
  private Date orderDate;
  private Date expectedArrivalDate;
  private String containerNo;
  private String driverName;
  private String truckNo;
  private String driverPhone;
  private String idCardNo;
  private String customerOrderNo;
  private String wmsOrderNo;
  private String rawText;
  private String remark;
  private List<ErpRecognizedInboundItemVo> itemList = new ArrayList<ErpRecognizedInboundItemVo>();
  private List<ErpRecognizedInboundFileVo> fileList = new ArrayList<ErpRecognizedInboundFileVo>();
}
