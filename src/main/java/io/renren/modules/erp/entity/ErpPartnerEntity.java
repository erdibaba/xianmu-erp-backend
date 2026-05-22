package io.renren.modules.erp.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

@Data
@TableName("erp_partner")
public class ErpPartnerEntity implements Serializable {
  private static final long serialVersionUID = 1L;

  @TableId
  private Long id;
  private String partnerCode;
  private String partnerName;
  private Integer partnerType;
  private String businessRole;
  private Integer coldStorageFreeDays;
  private String taxNo;
  private String bankName;
  private String bankAccount;
  private String address;
  private String contactName;
  private String contactPhone;
  private String contactEmail;
  private String remark;
  private Integer status;
  private Long createUserId;
  private Date createTime;
  private Date updateTime;
}
