package io.renren.modules.erp.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

@Data
@TableName("erp_presale_attachment")
public class ErpPresaleAttachmentEntity implements Serializable {
  private static final long serialVersionUID = 1L;

  @TableId
  private Long id;
  private Long presaleOrderId;
  private Long confirmId;
  private String attachmentType;
  private String filePath;
  private String fileName;
  private String remark;
  private Long createUserId;
  private Date createTime;
  private Date updateTime;
}
