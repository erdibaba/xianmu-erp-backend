package io.renren.modules.erp.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

@Data
@TableName("erp_warehouse")
public class ErpWarehouseEntity implements Serializable {
  private static final long serialVersionUID = 1L;

  @TableId
  private Long id;
  private String warehouseCode;
  private String warehouseName;
  private String warehouseType;
  private Integer ownedByCompany;
  private String contactName;
  private String contactPhone;
  private String address;
  private Integer freeStorageDays;
  private BigDecimal dailyStorageFee;
  private BigDecimal dailyColdFee;
  private BigDecimal frozenStorageFee;
  private BigDecimal chilledStorageFee;
  private BigDecimal frozenColdFee;
  private BigDecimal chilledColdFee;
  private String feeUnit;
  private Integer status;
  private String remark;
  private Long createUserId;
  private Date createTime;
  private Date updateTime;
}
