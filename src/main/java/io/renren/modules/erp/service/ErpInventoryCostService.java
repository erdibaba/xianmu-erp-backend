package io.renren.modules.erp.service;

import io.renren.modules.erp.vo.ErpInventoryCostDetailVo;
import io.renren.modules.erp.vo.ErpInventoryCostVo;
import java.util.List;
import java.util.Map;

public interface ErpInventoryCostService {
  List<ErpInventoryCostVo> querySpotCost(Map<String, Object> params);

  List<ErpInventoryCostDetailVo> querySpotCostDetails(Map<String, Object> params);
}
