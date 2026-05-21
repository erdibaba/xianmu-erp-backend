package io.renren.modules.erp.service.impl;

import io.renren.modules.erp.dao.ErpInventoryDao;
import io.renren.modules.erp.service.ErpInventoryService;
import io.renren.modules.erp.vo.ErpInventorySummaryVo;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("erpInventoryService")
public class ErpInventoryServiceImpl implements ErpInventoryService {
  @Autowired
  private ErpInventoryDao erpInventoryDao;

  @Override
  public List<ErpInventorySummaryVo> querySummary(Map<String, Object> params) {
    String productName = params.get("productName") == null ? null : params.get("productName").toString();
    String warehouseName = params.get("warehouseName") == null ? null : params.get("warehouseName").toString();
    return erpInventoryDao.querySummary(productName, warehouseName);
  }
}
