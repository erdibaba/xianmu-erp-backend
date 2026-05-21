package io.renren.modules.erp.dao;

import io.renren.modules.erp.vo.ErpInventorySummaryVo;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ErpInventoryDao {
  List<ErpInventorySummaryVo> querySummary(@Param("productName") String productName,
                                           @Param("warehouseName") String warehouseName);
}
