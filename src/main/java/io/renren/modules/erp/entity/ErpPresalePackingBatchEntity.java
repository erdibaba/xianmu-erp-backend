package io.renren.modules.erp.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

@Data
@TableName("erp_presale_packing_batch")
public class ErpPresalePackingBatchEntity implements Serializable {
  private static final long serialVersionUID = 1L;

  @TableId
  private Long id;
  private Long packingItemId;
  private Integer lineNo;
  private Date productionDate;
  private Date expiryDate;
  private Integer boxCount;
  private BigDecimal weight;
  private Date createTime;
  private Date updateTime;
}
