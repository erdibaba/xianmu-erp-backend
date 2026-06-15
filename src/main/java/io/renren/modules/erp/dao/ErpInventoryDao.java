package io.renren.modules.erp.dao;

import io.renren.modules.erp.vo.ErpInventorySummaryVo;
import io.renren.modules.erp.vo.ErpFuturesInventoryVo;
import io.renren.modules.erp.vo.ErpInventoryBatchVo;
import io.renren.modules.erp.vo.ErpInventoryRecordVo;
import io.renren.modules.erp.vo.ErpSpotInventoryVo;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ErpInventoryDao {
  List<ErpInventorySummaryVo> querySummary(@Param("productName") String productName,
                                           @Param("warehouseName") String warehouseName);

  List<ErpSpotInventoryVo> querySpot(@Param("keyword") String keyword,
                                     @Param("warehouseId") Long warehouseId,
                                     @Param("containerNos") List<String> containerNos,
                                     @Param("factoryNo") String factoryNo,
                                     @Param("onlyAvailable") Integer onlyAvailable);

  List<ErpFuturesInventoryVo> queryFutures(@Param("keyword") String keyword,
                                           @Param("warehouseId") Long warehouseId,
                                           @Param("contractNo") String contractNo,
                                           @Param("containerNos") List<String> containerNos,
                                           @Param("factoryNo") String factoryNo,
                                           @Param("onlyAvailable") Integer onlyAvailable);

  List<ErpInventoryBatchVo> querySpotBatches(@Param("productId") Long productId,
                                             @Param("warehouseId") Long warehouseId,
                                             @Param("containerNos") List<String> containerNos,
                                             @Param("ownershipName") String ownershipName,
                                             @Param("factoryNo") String factoryNo,
                                             @Param("onlyAvailable") Integer onlyAvailable);

  List<ErpInventoryBatchVo> queryFuturesBatches(@Param("productId") Long productId,
                                                @Param("warehouseId") Long warehouseId,
                                                @Param("contractNo") String contractNo,
                                                @Param("containerNos") List<String> containerNos,
                                                @Param("ownershipName") String ownershipName,
                                                @Param("factoryNo") String factoryNo,
                                                @Param("onlyAvailable") Integer onlyAvailable);

  List<String> querySpotContainers(@Param("warehouseId") Long warehouseId,
                                   @Param("keyword") String keyword);

  List<String> queryFuturesContainers(@Param("warehouseId") Long warehouseId,
                                      @Param("keyword") String keyword);

  List<ErpInventoryRecordVo> queryRecords(@Param("keyword") String keyword,
                                          @Param("recordType") String recordType,
                                          @Param("contractNo") String contractNo,
                                          @Param("warehouseName") String warehouseName,
                                          @Param("containerNo") String containerNo,
                                          @Param("factoryNo") String factoryNo);
}
