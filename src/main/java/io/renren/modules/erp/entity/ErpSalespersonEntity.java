package io.renren.modules.erp.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

@Data
@TableName("erp_salesperson")
public class ErpSalespersonEntity implements Serializable {
  private static final long serialVersionUID = 1L;

  @TableId
  private Long id;
  private String salesName;
  private String mobile;
  private Long sysUserId;
  private String sysUsername;
  private Integer status;
  private String remark;
  private Long createUserId;
  private Date createTime;
  private Date updateTime;
}
