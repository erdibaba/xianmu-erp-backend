package io.renren.modules.erp.service;

import io.renren.modules.erp.vo.ErpInventorySummaryVo;
import java.util.List;
import java.util.Map;

public interface ErpInventoryService {
  List<ErpInventorySummaryVo> querySummary(Map<String, Object> params);
}
