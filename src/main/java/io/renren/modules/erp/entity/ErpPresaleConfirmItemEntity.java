package io.renren.modules.erp.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

@Data
@TableName("erp_presale_confirm_item")
public class ErpPresaleConfirmItemEntity implements Serializable {
  private static final long serialVersionUID = 1L;

  @TableId
  private Long id;
  private Long confirmId;
  private Integer lineNo;
  private Long productId;
  private String productCode;
  private String sourceProductCode;
  private String productName;
  private String productNameEn;
  private String unit;
  private BigDecimal quantity;
  private BigDecimal unitPriceInclTax;
  private BigDecimal lineTotalInclTax;
  private BigDecimal taxRate;
  private String remark;
  private Date createTime;
  private Date updateTime;
}
