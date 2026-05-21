package io.renren.modules.erp.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

@Data
@TableName("erp_product")
public class ErpProductEntity implements Serializable {
  private static final long serialVersionUID = 1L;

  @TableId
  private Long id;
  private String productCode;
  private String aliasCodes;
  private String productName;
  private String productNameEn;
  private String productSpec;
  private String unit;
  private String brand;
  private String originCountry;
  private BigDecimal defaultTaxRate;
  private BigDecimal safeStockBoxes;
  private Integer freshnessWarningDays;
  private Integer status;
  private String remark;
  private Long createUserId;
  private Date createTime;
  private Date updateTime;
}
