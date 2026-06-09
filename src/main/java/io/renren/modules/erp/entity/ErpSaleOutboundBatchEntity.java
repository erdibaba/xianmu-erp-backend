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
@TableName("erp_sale_outbound_batch")
public class ErpSaleOutboundBatchEntity implements Serializable {
  private static final long serialVersionUID = 1L;

  @TableId
  private Long id;
  private Long saleOrderId;
  private String batchNo;
  private Integer status;
  private Long bankSlipFileId;
  private Integer receiptCount;
  private Integer shippedTotalBoxes;
  private BigDecimal shippedTotalWeight;
  private String remark;
  private Long createUserId;
  private Long confirmUserId;
  private Date confirmTime;
  private Long voidUserId;
  private Date voidTime;
  private Date createTime;
  private Date updateTime;

  @TableField(exist = false)
  private ErpSaleOutboundReceiptEntity receipt;

  @TableField(exist = false)
  private ErpSaleOrderFileEntity bankSlipFile;

  @TableField(exist = false)
  private List<ErpSaleOrderFileEntity> receiptFileList = new ArrayList<ErpSaleOrderFileEntity>();

  @TableField(exist = false)
  private ErpSaleOutboundScanEntity scan;
}
