package io.renren.modules.erp.service.impl;

import io.renren.modules.erp.dao.ErpInventoryDao;
import io.renren.modules.erp.service.ErpInventoryService;
import io.renren.modules.erp.vo.ErpFuturesInventoryVo;
import io.renren.modules.erp.vo.ErpInventoryBatchVo;
import io.renren.modules.erp.vo.ErpInventoryRecordVo;
import io.renren.modules.erp.vo.ErpInventorySummaryVo;
import io.renren.modules.erp.vo.ErpSpotInventoryVo;
import java.util.ArrayList;
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
    Long warehouseId = getLong(params, "warehouseId");
    List<String> containerNos = getList(params, "containerNos");
    String factoryNo = getString(params, "factoryNo");
    Integer onlyAvailable = getFlag(params, "onlyAvailable");
    return erpInventoryDao.querySpot(keyword, warehouseId, containerNos, factoryNo, onlyAvailable);
  }

  @Override
  public List<ErpFuturesInventoryVo> queryFutures(Map<String, Object> params) {
    String keyword = getString(params, "keyword");
    Long warehouseId = getLong(params, "warehouseId");
    String contractNo = getString(params, "contractNo");
    List<String> containerNos = getList(params, "containerNos");
    String factoryNo = getString(params, "factoryNo");
    Integer onlyAvailable = getFlag(params, "onlyAvailable");
    return erpInventoryDao.queryFutures(keyword, warehouseId, contractNo, containerNos, factoryNo, onlyAvailable);
  }

  @Override
  public List<ErpInventoryBatchVo> querySpotBatches(Map<String, Object> params) {
    Long productId = getLong(params, "productId");
    if (productId == null) {
      throw new RuntimeException("缺少库存批次查询条件");
    }
    Long warehouseId = getLong(params, "warehouseId");
    List<String> containerNos = getList(params, "containerNos");
    String factoryNo = getString(params, "factoryNo");
    Integer onlyAvailable = getFlag(params, "onlyAvailable");
    return erpInventoryDao.querySpotBatches(productId, warehouseId, containerNos, factoryNo, onlyAvailable);
  }

  @Override
  public List<ErpInventoryBatchVo> queryFuturesBatches(Map<String, Object> params) {
    Long productId = getLong(params, "productId");
    if (productId == null) {
      throw new RuntimeException("缺少期货库存产品");
    }
    Long warehouseId = getLong(params, "warehouseId");
    String contractNo = getString(params, "contractNo");
    List<String> containerNos = getList(params, "containerNos");
    String factoryNo = getString(params, "factoryNo");
    Integer onlyAvailable = getFlag(params, "onlyAvailable");
    return erpInventoryDao.queryFuturesBatches(productId, warehouseId, contractNo, containerNos, factoryNo, onlyAvailable);
  }

  @Override
  public List<String> queryContainerOptions(Map<String, Object> params) {
    Long warehouseId = getLong(params, "warehouseId");
    if (warehouseId == null) {
      return new ArrayList<>();
    }
    String inventoryType = getString(params, "inventoryType");
    String keyword = getString(params, "keyword");
    if ("futures".equalsIgnoreCase(inventoryType)) {
      return erpInventoryDao.queryFuturesContainers(warehouseId, keyword);
    }
    return erpInventoryDao.querySpotContainers(warehouseId, keyword);
  }

  @Override
  public List<ErpInventoryRecordVo> queryRecords(Map<String, Object> params) {
    String keyword = getString(params, "keyword");
    String recordType = getString(params, "recordType");
    String contractNo = getString(params, "contractNo");
    String warehouseName = getString(params, "warehouseName");
    String containerNo = getString(params, "containerNo");
    String factoryNo = getString(params, "factoryNo");
    return erpInventoryDao.queryRecords(keyword, recordType, contractNo, warehouseName, containerNo, factoryNo);
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

  private List<String> getList(Map<String, Object> params, String key) {
    String value = getString(params, key);
    List<String> result = new ArrayList<>();
    if (value == null || value.length() == 0) {
      return result;
    }
    for (String item : value.split(",")) {
      String text = item == null ? "" : item.trim();
      if (text.length() > 0) {
        result.add(text);
      }
    }
    return result;
  }
}
