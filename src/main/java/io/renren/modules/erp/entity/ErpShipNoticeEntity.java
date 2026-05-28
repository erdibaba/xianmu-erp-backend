package io.renren.modules.erp.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

@Data
@TableName("erp_ship_notice")
public class ErpShipNoticeEntity implements Serializable {
  private static final long serialVersionUID = 1L;

  @TableId
  private Long id;
  private Long presaleOrderId;
  private Long partnerId;
  private String partnerName;
  private String chatId;
  private String chatName;
  private String sender;
  private String contractNo;
  private String containerNo;
  private Date expectedArrivalDate;
  private String content;
  private String wecomMsgId;
  private Integer status;
  private String errorMessage;
  private Long createUserId;
  private Date createTime;
  private Date updateTime;
}
