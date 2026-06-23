package io.renren.modules.erp.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

@Data
@TableName("erp_driver")
public class ErpDriverEntity implements Serializable {
  private static final long serialVersionUID = 1L;

  @TableId
  private Long id;
  private String driverName;
  private String plateNo;
  private String mobile;
  private String idCardNo;
  private Integer status;
  private Long createUserId;
  private Date createTime;
  private Date updateTime;
}
