package io.renren.modules.erp.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

@Data
@TableName("erp_product_price")
public class ErpProductPriceEntity implements Serializable {
  private static final long serialVersionUID = 1L;

  @TableId
  private Long id;
  private Long productId;
  private Long partnerId;
  private Integer priceType;
  private Date effectiveDate;
  private String currency;
  private BigDecimal unitPrice;
  private BigDecimal taxRate;
  private String remark;
  private Integer status;
  private Long createUserId;
  private Date createTime;
  private Date updateTime;
}
