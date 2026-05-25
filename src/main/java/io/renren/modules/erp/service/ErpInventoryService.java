package io.renren.modules.erp.service;

import io.renren.modules.erp.vo.ErpFuturesInventoryVo;
import io.renren.modules.erp.vo.ErpInventoryBatchVo;
import io.renren.modules.erp.vo.ErpInventorySummaryVo;
import io.renren.modules.erp.vo.ErpSpotInventoryVo;
import java.util.List;
import java.util.Map;

public interface ErpInventoryService {
  List<ErpInventorySummaryVo> querySummary(Map<String, Object> params);

  List<ErpSpotInventoryVo> querySpot(Map<String, Object> params);

  List<ErpFuturesInventoryVo> queryFutures(Map<String, Object> params);

  List<ErpInventoryBatchVo> querySpotBatches(Map<String, Object> params);

  List<ErpInventoryBatchVo> queryFuturesBatches(Map<String, Object> params);
}
