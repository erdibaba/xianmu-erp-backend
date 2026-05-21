package io.renren.modules.erp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import lombok.Data;

@Data
@TableName("erp_presale_packing_item")
public class ErpPresalePackingItemEntity implements Serializable {
  private static final long serialVersionUID = 1L;

  @TableId
  private Long id;
  private Long packingId;
  private Integer lineNo;
  private Long productId;
  private String productCode;
  private String sourceProductCode;
  private String productName;
  private String productNameEn;
  private Integer totalBoxes;
  private BigDecimal totalWeight;
  private Integer shelfLifeDays;
  private Date createTime;
  private Date updateTime;

  @TableField(exist = false)
  private List<ErpPresalePackingBatchEntity> batchList;
}
