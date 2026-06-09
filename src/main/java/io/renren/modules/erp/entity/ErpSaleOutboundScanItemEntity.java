package io.renren.modules.erp.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

@Data
@TableName("erp_sale_outbound_scan_item")
public class ErpSaleOutboundScanItemEntity implements Serializable {
  private static final long serialVersionUID = 1L;

  @TableId
  private Long id;
  private Long scanId;
  private Long saleOrderId;
  private Long batchId;
  private Integer lineNo;
  private Long productId;
  private String productCode;
  private String recognizedProductCode;
  private String scanProductName;
  private String productName;
  private String productNameEn;
  private Integer boxes;
  private BigDecimal totalWeight;
  private String weightListJson;
  private Date createTime;
  private Date updateTime;
}
