package io.renren.modules.erp.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

@Data
@TableName("erp_wecom_group")
public class ErpWecomGroupEntity implements Serializable {
  private static final long serialVersionUID = 1L;

  @TableId
  private Long id;
  private String chatId;
  private String groupName;
  private String owner;
  private Integer groupStatus;
  private Integer memberCount;
  private Date groupCreateTime;
  private Date syncTime;
  private Date createTime;
  private Date updateTime;
}
