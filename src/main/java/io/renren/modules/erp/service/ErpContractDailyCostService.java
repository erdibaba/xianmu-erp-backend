package io.renren.modules.erp.service;

import io.renren.common.utils.PageUtils;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ErpContractDailyCostService {
  PageUtils queryPage(Map<String, Object> params);

  List<Map<String, Object>> queryDetails(Long dailyCostId);

  void refresh(Map<String, Object> params, Long userId);

  void generateSnapshot(LocalDate costDate, Long userId);
}
