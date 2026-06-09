package io.renren.modules.erp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.Data;

@Data
@TableName("erp_sale_outbound_scan")
public class ErpSaleOutboundScanEntity implements Serializable {
  private static final long serialVersionUID = 1L;

  @TableId
  private Long id;
  private Long saleOrderId;
  private Long batchId;
  private String scanUrl;
  private String orderNum;
  private String imei;
  private Date scanOrderTime;
  private String customerName;
  private Integer totalBoxes;
  private BigDecimal totalWeight;
  private String rawJson;
  private Date createTime;
  private Date updateTime;

  @TableField(exist = false)
  private List<ErpSaleOutboundScanItemEntity> itemList = new ArrayList<ErpSaleOutboundScanItemEntity>();
}
