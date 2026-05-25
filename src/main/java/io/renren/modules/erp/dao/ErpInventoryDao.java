package io.renren.modules.erp.dao;

import io.renren.modules.erp.vo.ErpInventorySummaryVo;
import io.renren.modules.erp.vo.ErpFuturesInventoryVo;
import io.renren.modules.erp.vo.ErpInventoryBatchVo;
import io.renren.modules.erp.vo.ErpSpotInventoryVo;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ErpInventoryDao {
  List<ErpInventorySummaryVo> querySummary(@Param("productName") String productName,
                                           @Param("warehouseName") String warehouseName);

  List<ErpSpotInventoryVo> querySpot(@Param("keyword") String keyword,
                                     @Param("warehouseName") String warehouseName,
                                     @Param("containerNo") String containerNo,
                                     @Param("onlyAvailable") Integer onlyAvailable);

  List<ErpFuturesInventoryVo> queryFutures(@Param("keyword") String keyword,
                                           @Param("contractNo") String contractNo,
                                           @Param("containerNo") String containerNo,
                                           @Param("onlyAvailable") Integer onlyAvailable);

  List<ErpInventoryBatchVo> querySpotBatches(@Param("presaleOrderId") Long presaleOrderId,
                                             @Param("productId") Long productId);

  List<ErpInventoryBatchVo> queryFuturesBatches(@Param("packingItemId") Long packingItemId);
}
