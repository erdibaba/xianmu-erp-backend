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
@TableName("erp_presale_packing")
public class ErpPresalePackingEntity implements Serializable {
  private static final long serialVersionUID = 1L;

  @TableId
  private Long id;
  private Long presaleOrderId;
  private String contractNo;
  private String containerNo;
  private Integer shelfLifeDays;
  private Integer totalBoxes;
  private BigDecimal totalWeight;
  private String filePath;
  private String fileName;
  private String rawText;
  private String remark;
  private Long createUserId;
  private Date createTime;
  private Date updateTime;

  @TableField(exist = false)
  private List<ErpPresalePackingItemEntity> itemList;
}
