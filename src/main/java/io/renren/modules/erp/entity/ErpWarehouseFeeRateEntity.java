package io.renren.modules.erp.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
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
  private String scanFeeUnit;
  private BigDecimal scanFeeRate;
  private String weightBasis;
  private BigDecimal wrappingFee;
  private String wrappingFeeUnit;
  private BigDecimal sortingFee;
  private String sortingFeeUnit;
  private BigDecimal repeatedHandlingFee;
  private String repeatedHandlingFeeUnit;
  private BigDecimal ownerChangeFee;
  private String ownerChangeFeeUnit;
  private String remark;
  private Integer status;
  private Long createUserId;
  private Date createTime;
  private Date updateTime;
  @TableField(exist = false)
  private List<ErpWarehouseColorFeeTierEntity> colorFeeTierList;
}
