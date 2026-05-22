package io.renren.modules.erp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.Data;

@Data
@TableName("erp_sale_order")
public class ErpSaleOrderEntity implements Serializable {
  private static final long serialVersionUID = 1L;

  @TableId
  private Long id;
  private String orderNo;
  private String saleType;
  private Long secondaryPartnerId;
  private String secondaryPartnerName;
  private Long warehouseId;
  private String warehouseName;
  private String contractNo;
  private String contractToken;
  @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
  private Date contractSignDate;
  private Integer status;
  private Integer signedContractConfirmed;
  private Integer buyerPaymentConfirmed;
  private Integer buyerBankConfirmed;
  private Integer funderPaymentConfirmed;
  private Long sourcePresaleOrderId;
  private String sourcePresaleOrderNo;
  private Integer presaleLinkConfirmed;
  private Integer outboundReceiptConfirmed;
  private String remark;
  private Long createUserId;
  private Date createTime;
  private Date updateTime;

  @TableField(exist = false)
  private List<ErpSaleOrderItemEntity> itemList = new ArrayList<ErpSaleOrderItemEntity>();

  @TableField(exist = false)
  private List<ErpSaleOrderItemEntity> allocationItemList = new ArrayList<ErpSaleOrderItemEntity>();

  @TableField(exist = false)
  private List<ErpSaleOrderFileEntity> fileList = new ArrayList<ErpSaleOrderFileEntity>();

  @TableField(exist = false)
  private String contractUrl;

  @TableField(exist = false)
  private String buyerPortalUrl;

  @TableField(exist = false)
  private Integer signedContractUploaded;

  @TableField(exist = false)
  private Integer buyerPaymentUploaded;

  @TableField(exist = false)
  private Integer buyerBankSlipUploaded;

  @TableField(exist = false)
  private Integer funderPaymentUploaded;

  @TableField(exist = false)
  private Integer outboundReceiptUploaded;

  @TableField(exist = false)
  private Integer outboundAttachmentUploaded;

  @TableField(exist = false)
  private ErpSaleOutboundReceiptEntity outboundReceipt;

}
