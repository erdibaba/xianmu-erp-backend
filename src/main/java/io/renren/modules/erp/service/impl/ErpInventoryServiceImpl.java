package io.renren.modules.erp.service.impl;

import io.renren.modules.erp.dao.ErpInventoryDao;
import io.renren.modules.erp.service.ErpInventoryService;
import io.renren.modules.erp.vo.ErpFuturesInventoryVo;
import io.renren.modules.erp.vo.ErpInventoryBatchVo;
import io.renren.modules.erp.vo.ErpInventoryRecordVo;
import io.renren.modules.erp.vo.ErpInventorySummaryVo;
import io.renren.modules.erp.vo.ErpSpotInventoryVo;
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

  @Override
  public List<ErpSpotInventoryVo> querySpot(Map<String, Object> params) {
    String keyword = getString(params, "keyword");
    String warehouseName = getString(params, "warehouseName");
    String containerNo = getString(params, "containerNo");
    Integer onlyAvailable = getFlag(params, "onlyAvailable");
    return erpInventoryDao.querySpot(keyword, warehouseName, containerNo, onlyAvailable);
  }

  @Override
  public List<ErpFuturesInventoryVo> queryFutures(Map<String, Object> params) {
    String keyword = getString(params, "keyword");
    String contractNo = getString(params, "contractNo");
    String containerNo = getString(params, "containerNo");
    Integer onlyAvailable = getFlag(params, "onlyAvailable");
    return erpInventoryDao.queryFutures(keyword, contractNo, containerNo, onlyAvailable);
  }

  @Override
  public List<ErpInventoryBatchVo> querySpotBatches(Map<String, Object> params) {
    Long presaleOrderId = getLong(params, "presaleOrderId");
    Long productId = getLong(params, "productId");
    if (presaleOrderId == null || productId == null) {
      throw new RuntimeException("缺少库存批次查询条件");
    }
    return erpInventoryDao.querySpotBatches(presaleOrderId, productId);
  }

  @Override
  public List<ErpInventoryBatchVo> queryFuturesBatches(Map<String, Object> params) {
    Long packingItemId = getLong(params, "packingItemId");
    if (packingItemId == null) {
      throw new RuntimeException("缺少装箱单产品明细");
    }
    return erpInventoryDao.queryFuturesBatches(packingItemId);
  }

  @Override
  public List<ErpInventoryRecordVo> queryRecords(Map<String, Object> params) {
    String keyword = getString(params, "keyword");
    String recordType = getString(params, "recordType");
    String contractNo = getString(params, "contractNo");
    String warehouseName = getString(params, "warehouseName");
    String containerNo = getString(params, "containerNo");
    return erpInventoryDao.queryRecords(keyword, recordType, contractNo, warehouseName, containerNo);
  }

  private String getString(Map<String, Object> params, String key) {
    Object value = params.get(key);
    return value == null ? null : value.toString().trim();
  }

  private Integer getFlag(Map<String, Object> params, String key) {
    Object value = params.get(key);
    if (value == null || value.toString().trim().length() == 0) {
      return 0;
    }
    return "1".equals(value.toString()) || "true".equalsIgnoreCase(value.toString()) ? 1 : 0;
  }

  private Long getLong(Map<String, Object> params, String key) {
    Object value = params.get(key);
    if (value == null || value.toString().trim().length() == 0) {
      return null;
    }
    return Long.valueOf(value.toString());
  }
}
