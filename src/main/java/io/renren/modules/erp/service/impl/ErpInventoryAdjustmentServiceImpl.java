package io.renren.modules.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.renren.modules.erp.dao.ErpInventoryAdjustmentDao;
import io.renren.modules.erp.dao.ErpInventoryAdjustmentItemDao;
import io.renren.modules.erp.dao.ErpInventoryDao;
import io.renren.modules.erp.dao.ErpWarehouseDao;
import io.renren.modules.erp.entity.ErpInventoryAdjustmentEntity;
import io.renren.modules.erp.entity.ErpInventoryAdjustmentItemEntity;
import io.renren.modules.erp.entity.ErpWarehouseEntity;
import io.renren.modules.erp.service.ErpInventoryAdjustmentService;
import io.renren.modules.erp.vo.ErpInventoryAdjustmentRequest;
import io.renren.modules.erp.vo.ErpInventoryBatchVo;
import io.renren.modules.erp.vo.ErpSpotInventoryVo;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("erpInventoryAdjustmentService")
public class ErpInventoryAdjustmentServiceImpl implements ErpInventoryAdjustmentService {
  private static final String TYPE_WAREHOUSE = "WAREHOUSE_TRANSFER";
  private static final String TYPE_FRESH_TO_FROZEN = "FRESH_TO_FROZEN";

  @Autowired
  private ErpInventoryDao erpInventoryDao;
  @Autowired
  private ErpInventoryAdjustmentDao erpInventoryAdjustmentDao;
  @Autowired
  private ErpInventoryAdjustmentItemDao erpInventoryAdjustmentItemDao;
  @Autowired
  private ErpWarehouseDao erpWarehouseDao;

  @Override
  public List<ErpInventoryBatchVo> queryAvailableLots(Map<String, Object> params) {
    String adjustmentType = getString(params, "adjustmentType");
    String keyword = getString(params, "keyword");
    String warehouseName = getString(params, "warehouseName");
    String containerNo = getString(params, "containerNo");
    String factoryNo = getString(params, "factoryNo");
    List<ErpInventoryBatchVo> lots = buildCurrentLots(keyword);
    List<ErpInventoryBatchVo> result = new ArrayList<>();
    for (ErpInventoryBatchVo lot : lots) {
      if (lot.getAvailableBoxes() == null || lot.getAvailableBoxes() <= 0) {
        continue;
      }
      if (TYPE_FRESH_TO_FROZEN.equals(adjustmentType) && isFrozen(lot.getTemperatureZone())) {
        continue;
      }
      if (!contains(lot.getWarehouseName(), warehouseName)
          || !contains(lot.getContainerNo(), containerNo)
          || !contains(lot.getFactoryNo(), factoryNo)) {
        continue;
      }
      result.add(lot);
    }
    result.sort(Comparator
        .comparing((ErpInventoryBatchVo lot) -> StringUtils.defaultString(lot.getProductCode()))
        .thenComparing(lot -> StringUtils.defaultString(lot.getWarehouseName()))
        .thenComparing(lot -> nullSafeDate(lot.getExpiryDate()))
        .thenComparing(lot -> StringUtils.defaultString(lot.getContainerNo())));
    return result;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void saveAdjustment(ErpInventoryAdjustmentRequest request, Long userId) {
    if (request == null || StringUtils.isBlank(request.getAdjustmentType())) {
      throw new RuntimeException("请选择调整类型");
    }
    if (!TYPE_WAREHOUSE.equals(request.getAdjustmentType()) && !TYPE_FRESH_TO_FROZEN.equals(request.getAdjustmentType())) {
      throw new RuntimeException("不支持的调整类型");
    }
    if (request.getItemList() == null || request.getItemList().isEmpty()) {
      throw new RuntimeException("请至少选择一条库存明细");
    }
    List<ErpInventoryBatchVo> currentLots = buildCurrentLots(null);
    Date now = new Date();
    ErpInventoryAdjustmentEntity adjustment = new ErpInventoryAdjustmentEntity();
    adjustment.setAdjustmentNo(buildAdjustmentNo(request.getAdjustmentType(), now));
    adjustment.setAdjustmentType(request.getAdjustmentType());
    adjustment.setRemark(StringUtils.trimToEmpty(request.getRemark()));
    adjustment.setCreateUserId(userId);
    adjustment.setCreateTime(now);
    adjustment.setUpdateTime(now);
    erpInventoryAdjustmentDao.insert(adjustment);

    int lineNo = 1;
    for (ErpInventoryAdjustmentRequest.Item input : request.getItemList()) {
      ErpInventoryBatchVo source = findLot(currentLots, input);
      if (source == null) {
        throw new RuntimeException("第" + lineNo + "行库存明细已变化，请刷新后重试");
      }
      int transferBoxes = input.getTransferBoxes() == null ? 0 : input.getTransferBoxes();
      BigDecimal transferWeight = nvl(input.getTransferWeightKg()).setScale(2, RoundingMode.HALF_UP);
      if (transferBoxes <= 0) {
        throw new RuntimeException("第" + lineNo + "行调整箱数必须大于0");
      }
      if (transferWeight.compareTo(BigDecimal.ZERO) <= 0) {
        throw new RuntimeException("第" + lineNo + "行调整重量必须大于0");
      }
      if (source.getAvailableBoxes() == null || transferBoxes > source.getAvailableBoxes()) {
        throw new RuntimeException("第" + lineNo + "行调整箱数不能大于可售箱数");
      }
      if (source.getAvailableWeightKg() != null && transferWeight.compareTo(source.getAvailableWeightKg()) > 0) {
        throw new RuntimeException("第" + lineNo + "行调整重量不能大于可售重量");
      }
      ErpWarehouseEntity targetWarehouse = null;
      Date targetExpiryDate = source.getExpiryDate();
      String targetTemperatureZone = source.getTemperatureZone();
      if (TYPE_WAREHOUSE.equals(request.getAdjustmentType())) {
        if (input.getTargetWarehouseId() == null) {
          throw new RuntimeException("第" + lineNo + "行请选择目标仓库");
        }
        targetWarehouse = erpWarehouseDao.selectById(input.getTargetWarehouseId());
        if (targetWarehouse == null) {
          throw new RuntimeException("第" + lineNo + "行目标仓库不存在");
        }
        if (source.getWarehouseId() != null && source.getWarehouseId().equals(targetWarehouse.getId())) {
          throw new RuntimeException("第" + lineNo + "行目标仓库不能和原仓库相同");
        }
      } else {
        if (isFrozen(source.getTemperatureZone())) {
          throw new RuntimeException("第" + lineNo + "行已经是冷冻库存，不能再次冷鲜转冷冻");
        }
        if (input.getTargetExpiryDate() == null) {
          throw new RuntimeException("第" + lineNo + "行请输入转冷冻后的过期日期");
        }
        targetExpiryDate = input.getTargetExpiryDate();
        targetTemperatureZone = "冷冻";
      }
      ErpInventoryAdjustmentItemEntity item = new ErpInventoryAdjustmentItemEntity();
      item.setAdjustmentId(adjustment.getId());
      item.setLineNo(lineNo);
      item.setAdjustmentType(request.getAdjustmentType());
      item.setSourceAdjustmentItemId(input.getSourceAdjustmentItemId());
      item.setSourceInboundOrderId(source.getInboundOrderId());
      item.setSourceInboundItemId(source.getInboundItemId());
      item.setSourcePackingItemId(source.getPackingItemId());
      item.setSourceBatchId(source.getBatchId());
      item.setProductId(source.getProductId());
      item.setProductCode(source.getProductCode());
      item.setProductName(source.getProductName());
      item.setProductNameEn(source.getProductNameEn());
      item.setProductSpec(source.getProductSpec());
      item.setUnit("KG");
      item.setSourceWarehouseId(source.getWarehouseId());
      item.setSourceWarehouseName(source.getWarehouseName());
      item.setTargetWarehouseId(TYPE_WAREHOUSE.equals(request.getAdjustmentType()) ? targetWarehouse.getId() : source.getWarehouseId());
      item.setTargetWarehouseName(TYPE_WAREHOUSE.equals(request.getAdjustmentType()) ? targetWarehouse.getWarehouseName() : source.getWarehouseName());
      item.setContainerNo(source.getContainerNo());
      item.setFactoryNo(source.getFactoryNo());
      item.setSourceTemperatureZone(source.getTemperatureZone());
      item.setTargetTemperatureZone(targetTemperatureZone);
      item.setProductionDate(source.getProductionDate());
      item.setSourceExpiryDate(source.getExpiryDate());
      item.setTargetExpiryDate(targetExpiryDate);
      item.setTransferBoxes(transferBoxes);
      item.setTransferWeightKg(transferWeight);
      item.setRemark(StringUtils.trimToEmpty(input.getRemark()));
      item.setCreateTime(now);
      item.setUpdateTime(now);
      erpInventoryAdjustmentItemDao.insert(item);
      lineNo++;
    }
  }

  private List<ErpInventoryBatchVo> buildCurrentLots(String keyword) {
    List<ErpSpotInventoryVo> products = erpInventoryDao.querySpot(keyword, null, null, null, 0);
    LinkedHashMap<String, ErpInventoryBatchVo> lotMap = new LinkedHashMap<>();
    for (ErpSpotInventoryVo product : products) {
      List<ErpInventoryBatchVo> batches = erpInventoryDao.querySpotBatches(product.getProductId(), null, null, null, 0);
      for (ErpInventoryBatchVo batch : batches) {
        batch.setLotType("BASE");
        lotMap.put(baseKey(batch), copyLot(batch));
      }
    }
    List<ErpInventoryAdjustmentItemEntity> adjustments = erpInventoryAdjustmentItemDao.selectList(
        new QueryWrapper<ErpInventoryAdjustmentItemEntity>().orderByAsc("id"));
    LinkedHashMap<Long, ErpInventoryBatchVo> adjustmentLotMap = new LinkedHashMap<>();
    for (ErpInventoryAdjustmentItemEntity adjustment : adjustments) {
      ErpInventoryBatchVo lot = lotFromAdjustment(adjustment);
      adjustmentLotMap.put(adjustment.getId(), lot);
    }
    for (ErpInventoryAdjustmentItemEntity adjustment : adjustments) {
      if (adjustment.getSourceAdjustmentItemId() != null) {
        subtract(adjustmentLotMap.get(adjustment.getSourceAdjustmentItemId()), adjustment);
      } else {
        subtract(lotMap.get(baseKey(adjustment)), adjustment);
      }
    }
    for (Map.Entry<Long, ErpInventoryBatchVo> entry : adjustmentLotMap.entrySet()) {
      ErpInventoryBatchVo lot = entry.getValue();
      if (matchesKeyword(lot, keyword)) {
        lotMap.put("ADJ:" + entry.getKey(), lot);
      }
    }
    return new ArrayList<>(lotMap.values());
  }

  private ErpInventoryBatchVo lotFromAdjustment(ErpInventoryAdjustmentItemEntity item) {
    ErpInventoryBatchVo lot = new ErpInventoryBatchVo();
    lot.setLotType("ADJUSTMENT");
    lot.setSourceAdjustmentItemId(item.getId());
    lot.setInboundOrderId(item.getSourceInboundOrderId());
    lot.setInboundItemId(item.getSourceInboundItemId());
    lot.setPackingItemId(item.getSourcePackingItemId());
    lot.setBatchId(item.getSourceBatchId());
    lot.setProductId(item.getProductId());
    lot.setProductCode(item.getProductCode());
    lot.setProductName(item.getProductName());
    lot.setProductNameEn(item.getProductNameEn());
    lot.setProductSpec(item.getProductSpec());
    lot.setWarehouseId(item.getTargetWarehouseId());
    lot.setWarehouseName(item.getTargetWarehouseName());
    lot.setContainerNo(item.getContainerNo());
    lot.setFactoryNo(item.getFactoryNo());
    lot.setTemperatureZone(item.getTargetTemperatureZone());
    lot.setProductionDate(item.getProductionDate());
    lot.setExpiryDate(item.getTargetExpiryDate());
    lot.setInboundBoxes(item.getTransferBoxes());
    lot.setAvailableBoxes(item.getTransferBoxes());
    lot.setInboundWeightKg(nvl(item.getTransferWeightKg()));
    lot.setAvailableWeightKg(nvl(item.getTransferWeightKg()));
    return lot;
  }

  private ErpInventoryBatchVo copyLot(ErpInventoryBatchVo source) {
    ErpInventoryBatchVo lot = new ErpInventoryBatchVo();
    lot.setLotType(source.getLotType());
    lot.setBatchId(source.getBatchId());
    lot.setPackingItemId(source.getPackingItemId());
    lot.setInboundOrderId(source.getInboundOrderId());
    lot.setInboundItemId(source.getInboundItemId());
    lot.setPresaleOrderId(source.getPresaleOrderId());
    lot.setContractNo(source.getContractNo());
    lot.setCustomerName(source.getCustomerName());
    lot.setWarehouseId(source.getWarehouseId());
    lot.setWarehouseName(source.getWarehouseName());
    lot.setContainerNo(source.getContainerNo());
    lot.setProductId(source.getProductId());
    lot.setProductCode(source.getProductCode());
    lot.setProductName(source.getProductName());
    lot.setProductNameEn(source.getProductNameEn());
    lot.setProductSpec(source.getProductSpec());
    lot.setFactoryNo(source.getFactoryNo());
    lot.setSkuCode(source.getSkuCode());
    lot.setTemperatureZone(source.getTemperatureZone());
    lot.setProductionDate(source.getProductionDate());
    lot.setExpiryDate(source.getExpiryDate());
    lot.setInboundBoxes(source.getInboundBoxes());
    lot.setAvailableBoxes(source.getAvailableBoxes());
    lot.setInboundWeightKg(nvl(source.getInboundWeightKg()));
    lot.setAvailableWeightKg(nvl(source.getAvailableWeightKg()));
    return lot;
  }

  private void subtract(ErpInventoryBatchVo lot, ErpInventoryAdjustmentItemEntity adjustment) {
    if (lot == null) {
      return;
    }
    lot.setAvailableBoxes(Math.max((lot.getAvailableBoxes() == null ? 0 : lot.getAvailableBoxes()) - (adjustment.getTransferBoxes() == null ? 0 : adjustment.getTransferBoxes()), 0));
    lot.setInboundBoxes(Math.max((lot.getInboundBoxes() == null ? 0 : lot.getInboundBoxes()) - (adjustment.getTransferBoxes() == null ? 0 : adjustment.getTransferBoxes()), 0));
    BigDecimal weight = nvl(lot.getAvailableWeightKg()).subtract(nvl(adjustment.getTransferWeightKg()));
    lot.setAvailableWeightKg(weight.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : weight.setScale(2, RoundingMode.HALF_UP));
    BigDecimal inboundWeight = nvl(lot.getInboundWeightKg()).subtract(nvl(adjustment.getTransferWeightKg()));
    lot.setInboundWeightKg(inboundWeight.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : inboundWeight.setScale(2, RoundingMode.HALF_UP));
  }

  private ErpInventoryBatchVo findLot(List<ErpInventoryBatchVo> lots, ErpInventoryAdjustmentRequest.Item input) {
    for (ErpInventoryBatchVo lot : lots) {
      if (sameLong(lot.getSourceAdjustmentItemId(), input.getSourceAdjustmentItemId())
          && sameLong(lot.getInboundItemId(), input.getSourceInboundItemId())
          && sameLong(lot.getPackingItemId(), input.getSourcePackingItemId())
          && sameLong(lot.getBatchId(), input.getSourceBatchId())) {
        return lot;
      }
    }
    return null;
  }

  private String baseKey(ErpInventoryBatchVo lot) {
    return key(lot.getInboundItemId(), lot.getPackingItemId(), lot.getBatchId());
  }

  private String baseKey(ErpInventoryAdjustmentItemEntity item) {
    return key(item.getSourceInboundItemId(), item.getSourcePackingItemId(), item.getSourceBatchId());
  }

  private String key(Long inboundItemId, Long packingItemId, Long batchId) {
    return (inboundItemId == null ? "0" : String.valueOf(inboundItemId)) + ":"
        + (packingItemId == null ? "0" : String.valueOf(packingItemId)) + ":"
        + (batchId == null ? "0" : String.valueOf(batchId));
  }

  private boolean sameLong(Long left, Long right) {
    return left == null ? right == null : left.equals(right);
  }

  private boolean matchesKeyword(ErpInventoryBatchVo lot, String keyword) {
    return StringUtils.isBlank(keyword)
        || contains(lot.getProductCode(), keyword)
        || contains(lot.getProductName(), keyword)
        || contains(lot.getProductNameEn(), keyword);
  }

  private boolean contains(String value, String keyword) {
    return StringUtils.isBlank(keyword) || StringUtils.containsIgnoreCase(StringUtils.defaultString(value), keyword);
  }

  private boolean isFrozen(String temperatureZone) {
    return StringUtils.contains(StringUtils.defaultString(temperatureZone), "冷冻");
  }

  private BigDecimal nvl(BigDecimal value) {
    return value == null ? BigDecimal.ZERO : value;
  }

  private String getString(Map<String, Object> params, String key) {
    Object value = params.get(key);
    return value == null ? null : value.toString().trim();
  }

  private Date nullSafeDate(Date date) {
    return date == null ? new Date(Long.MAX_VALUE) : date;
  }

  private String buildAdjustmentNo(String type, Date now) {
    String prefix = TYPE_FRESH_TO_FROZEN.equals(type) ? "FTF" : "WHT";
    return prefix + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(now);
  }
}
