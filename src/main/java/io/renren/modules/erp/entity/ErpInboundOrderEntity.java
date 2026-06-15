package io.renren.modules.erp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import lombok.Data;

@Data
@TableName("erp_inbound_order")
public class ErpInboundOrderEntity implements Serializable {
  private static final long serialVersionUID = 1L;

  @TableId
  private Long id;
  private Long presaleOrderId;
  private Long brandId;
  private String brandName;
  private String contractNo;
  private String customerName;
  private Long warehouseId;
  private String warehouseName;
  private Date orderDate;
  private Date expectedArrivalDate;
  private String containerNo;
  private String driverName;
  private String truckNo;
  private String driverPhone;
  private String idCardNo;
  private String customerOrderNo;
  private String wmsOrderNo;
  private String rawText;
  private String remark;
  private Long createUserId;
  private Date createTime;
  private Date updateTime;

  @TableField(exist = false)
  private List<ErpInboundOrderItemEntity> itemList;

  @TableField(exist = false)
  private List<ErpInboundOrderFileEntity> fileList;

  @TableField(exist = false)
  private List<ErpExpenseEntity> expenseList;

  @TableField(exist = false)
  private Integer uploadStatus;
}
