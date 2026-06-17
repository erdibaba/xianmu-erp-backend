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
@TableName("erp_presale_order")
public class ErpPresaleOrderEntity implements Serializable {
  private static final long serialVersionUID = 1L;

  @TableId
  private Long id;
  private String orderNo;
  private String sellerContractNo;
  private Long customerPartnerId;
  private String customerReference;
  private Long brandId;
  private String brandName;
  private String estimateFilePath;
  private String estimateFileName;
  private String estimateRawText;
  private String currency;
  private Date orderDate;
  private Date expectedDate;
  private Integer status;
  private BigDecimal depositRate;
  private BigDecimal depositReferenceAmount;
  private BigDecimal depositRecognizedAmount;
  private BigDecimal depositModifiedAmount;
  private Date depositPaymentDate;
  private String depositFilePath;
  private String depositFileName;
  private String depositRawText;
  private BigDecimal depositRefundRecognizedAmount;
  private BigDecimal depositRefundModifiedAmount;
  private Date depositRefundDate;
  private String depositRefundFilePath;
  private String depositRefundFileName;
  private String depositRefundRawText;
  private Integer depositStatus;
  private String remark;
  private Long createUserId;
  private Date createTime;
  private Date updateTime;

  @TableField(exist = false)
  private List<ErpPresaleOrderItemEntity> itemList;

  @TableField(exist = false)
  private ErpPresaleConfirmEntity confirmInfo;

  @TableField(exist = false)
  private List<ErpPresaleConfirmEntity> confirmList;

  @TableField(exist = false)
  private ErpPresalePackingEntity packingInfo;

  @TableField(exist = false)
  private Integer confirmUploaded;

  @TableField(exist = false)
  private Integer confirmCount;

  @TableField(exist = false)
  private Integer packingUploaded;

  @TableField(exist = false)
  private ErpPresaleAttachmentEntity customsInfo;

  @TableField(exist = false)
  private ErpPresaleAttachmentEntity quarantineInfo;

  @TableField(exist = false)
  private List<ErpPresaleAttachmentEntity> quarantineList;

  @TableField(exist = false)
  private Integer customsUploaded;

  @TableField(exist = false)
  private Integer quarantineUploaded;
}
