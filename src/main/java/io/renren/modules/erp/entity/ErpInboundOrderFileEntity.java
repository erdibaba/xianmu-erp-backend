package io.renren.modules.erp.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

@Data
@TableName("erp_inbound_order_file")
public class ErpInboundOrderFileEntity implements Serializable {
  private static final long serialVersionUID = 1L;

  @TableId
  private Long id;
  private Long inboundOrderId;
  private Integer lineNo;
  private String filePath;
  private String fileName;
  private Date createTime;
  private Date updateTime;
}
