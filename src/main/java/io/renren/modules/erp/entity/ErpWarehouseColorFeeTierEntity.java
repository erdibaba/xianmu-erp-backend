package io.renren.modules.erp.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

@Data
@TableName("erp_warehouse_color_fee_tier")
public class ErpWarehouseColorFeeTierEntity implements Serializable {
  private static final long serialVersionUID = 1L;

  @TableId
  private Long id;
  private Long rateId;
  private Long warehouseId;
  private Integer lineNo;
  private String rangeUnit;
  private BigDecimal rangeStart;
  private BigDecimal rangeEnd;
  private BigDecimal feeAmount;
  private String feeUnit;
  private String remark;
  private Date createTime;
  private Date updateTime;
}
