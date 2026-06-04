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
@TableName("erp_presale_confirm")
public class ErpPresaleConfirmEntity implements Serializable {
  private static final long serialVersionUID = 1L;

  @TableId
  private Long id;
  private Long presaleOrderId;
  private Long brandId;
  private String brandName;
  private Long buyerPartnerId;
  private String buyerPartnerName;
  private String buyerPartnerRole;
  private String contractNo;
  private String containerNo;
  private String coldFreshType;
  private Date expectedArrivalDate;
  private BigDecimal totalAmount;
  private String currency;
  private String filePath;
  private String fileName;
  private String rawText;
  private String remark;
  private Long createUserId;
  private Date createTime;
  private Date updateTime;

  @TableField(exist = false)
  private List<ErpPresaleConfirmItemEntity> itemList;
}
