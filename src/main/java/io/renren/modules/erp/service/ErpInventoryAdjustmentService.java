package io.renren.modules.erp.service;

import io.renren.modules.erp.vo.ErpInventoryAdjustmentRequest;
import io.renren.modules.erp.vo.ErpInventoryBatchVo;
import java.util.List;
import java.util.Map;

public interface ErpInventoryAdjustmentService {
  List<ErpInventoryBatchVo> queryAvailableLots(Map<String, Object> params);

  void saveAdjustment(ErpInventoryAdjustmentRequest request, Long userId);
}
