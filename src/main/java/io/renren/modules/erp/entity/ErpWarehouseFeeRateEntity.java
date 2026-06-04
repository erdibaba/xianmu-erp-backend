package io.renren.modules.erp.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

@Data
@TableName("erp_warehouse_fee_rate")
public class ErpWarehouseFeeRateEntity implements Serializable {
  private static final long serialVersionUID = 1L;

  @TableId
  private Long id;
  private Long warehouseId;
  private String warehouseName;
  @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
  private Date effectiveDate;
  private BigDecimal frozenStorageFee;
  private BigDecimal chilledStorageFee;
  private BigDecimal frozenColdFee;
  private BigDecimal chilledColdFee;
  private String remark;
  private Integer status;
  private Long createUserId;
  private Date createTime;
  private Date updateTime;
}
