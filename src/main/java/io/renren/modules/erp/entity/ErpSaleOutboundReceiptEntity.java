package io.renren.modules.erp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.Data;

@Data
@TableName("erp_sale_outbound_receipt")
public class ErpSaleOutboundReceiptEntity implements Serializable {
  private static final long serialVersionUID = 1L;

  @TableId
  private Long id;
  private Long saleOrderId;
  private String wmsOrderNo;
  private String outboundOrderNo;
  private String customerCode;
  private String customerName;
  private Integer saleTotalBoxes;
  private Integer shippedTotalBoxes;
  private Integer matched;
  private String matchMessage;
  private String rawText;
  private Date createTime;
  private Date updateTime;

  @TableField(exist = false)
  private List<ErpSaleOutboundReceiptItemEntity> itemList = new ArrayList<ErpSaleOutboundReceiptItemEntity>();
}
