package io.renren.modules.erp.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

@Data
@TableName("erp_presale_order_item")
public class ErpPresaleOrderItemEntity implements Serializable {
  private static final long serialVersionUID = 1L;

  @TableId
  private Long id;
  private Long presaleOrderId;
  private Integer lineNo;
  private Long productId;
  private String productCode;
  private String sourceProductCode;
  private String productName;
  private String productNameEn;
  private BigDecimal quantityTon;
  private BigDecimal quantityKg;
  private BigDecimal priceAmount;
  private String priceCurrency;
  private String priceUnit;
  private String remark;
  private Date createTime;
  private Date updateTime;
}
