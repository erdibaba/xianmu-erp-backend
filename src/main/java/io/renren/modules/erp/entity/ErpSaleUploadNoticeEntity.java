package io.renren.modules.erp.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

@Data
@TableName("erp_sale_upload_notice")
public class ErpSaleUploadNoticeEntity implements Serializable {
  private static final long serialVersionUID = 1L;

  @TableId
  private Long id;
  private Long saleOrderId;
  private String orderNo;
  private String contractNo;
  private Long partnerId;
  private String partnerName;
  private String chatId;
  private String chatName;
  private String sender;
  private String portalUrl;
  private String content;
  private String wecomMsgId;
  private Integer status;
  private String errorMessage;
  private Long createUserId;
  private Date createTime;
  private Date updateTime;
}
