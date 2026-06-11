package io.renren.modules.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.renren.modules.erp.dao.ErpInventoryAdjustmentDao;
import io.renren.modules.erp.dao.ErpInventoryAdjustmentFileDao;
import io.renren.modules.erp.dao.ErpInventoryAdjustmentItemDao;
import io.renren.modules.erp.dao.ErpInventoryDao;
import io.renren.modules.erp.dao.ErpWarehouseDao;
import io.renren.modules.erp.entity.ErpInventoryAdjustmentEntity;
import io.renren.modules.erp.entity.ErpInventoryAdjustmentFileEntity;
import io.renren.modules.erp.entity.ErpInventoryAdjustmentItemEntity;
import io.renren.modules.erp.entity.ErpWarehouseEntity;
import io.renren.modules.erp.service.ErpInventoryAdjustmentService;
import io.renren.modules.erp.vo.ErpInventoryAdjustmentRecognizeVo;
import io.renren.modules.erp.vo.ErpInventoryAdjustmentRecognizedItemVo;
import io.renren.modules.erp.vo.ErpInventoryAdjustmentRequest;
import io.renren.modules.erp.vo.ErpInventoryBatchVo;
import io.renren.modules.erp.vo.ErpRecognizedInboundFileVo;
import io.renren.modules.erp.vo.ErpRecognizedInboundItemVo;
import io.renren.modules.erp.vo.ErpRecognizedInboundResultVo;
import io.renren.modules.erp.vo.ErpSpotInventoryVo;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service("erpInventoryAdjustmentService")
public class ErpInventoryAdjustmentServiceImpl implements ErpInventoryAdjustmentService {
  private static final String TYPE_WAREHOUSE = "WAREHOUSE_TRANSFER";
  private static final String TYPE_FRESH_TO_FROZEN = "FRESH_TO_FROZEN";
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  static {
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    OBJECT_MAPPER.setDateFormat(dateFormat);
  }

  @Autowired
  private ErpInventoryDao erpInventoryDao;
  @Autowired
  private ErpInventoryAdjustmentDao erpInventoryAdjustmentDao;
  @Autowired
  private ErpInventoryAdjustmentFileDao erpInventoryAdjustmentFileDao;
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
  public ErpInventoryAdjustmentRecognizeVo recognizeAdjustment(String adjustmentType, MultipartFile[] files) throws Exception {
    if (!TYPE_WAREHOUSE.equals(adjustmentType) && !TYPE_FRESH_TO_FROZEN.equals(adjustmentType)) {
      throw new RuntimeException("不支持的调整类型");
    }
    String docName = TYPE_WAREHOUSE.equals(adjustmentType) ? "转仓库单据" : "鲜转冻单据";
    String pathType = TYPE_WAREHOUSE.equals(adjustmentType) ? "warehouse-transfer" : "fresh-to-frozen";
    if (files == null || files.length == 0) {
      throw new RuntimeException("请先上传" + docName);
    }
    List<Path> tempFiles = new ArrayList<>();
    List<Path> savedPaths = new ArrayList<>();
    Path listFile = null;
    try {
      for (MultipartFile file : files) {
        String suffix = getSuffix(file.getOriginalFilename());
        Path tempFile = Files.createTempFile("erp-inventory-adjustment-", suffix);
        tempFiles.add(tempFile);
        file.transferTo(tempFile.toFile());
        savedPaths.add(saveAdjustmentFile(file, tempFile, suffix, pathType));
      }
      listFile = Files.createTempFile("erp-inventory-adjustment-paths-", ".txt");
      List<String> pathLines = new ArrayList<>();
      for (Path savedPath : savedPaths) {
        pathLines.add(savedPath.toAbsolutePath().toString());
      }
      Files.write(listFile, pathLines, StandardCharsets.UTF_8);
      ProcessBuilder processBuilder = new ProcessBuilder(
          "python",
          "-X",
          "utf8",
          ensureInboundOcrScriptFile().getAbsolutePath(),
          listFile.toAbsolutePath().toString()
      );
      processBuilder.environment().put("PYTHONIOENCODING", "UTF-8");
      processBuilder.redirectErrorStream(true);
      Process process = processBuilder.start();
      String output;
      try (InputStream inputStream = process.getInputStream();
           ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
        byte[] bytes = new byte[4096];
        int len;
        while ((len = inputStream.read(bytes)) != -1) {
          buffer.write(bytes, 0, len);
        }
        output = new String(buffer.toByteArray(), StandardCharsets.UTF_8).trim();
      }
      int exitCode = process.waitFor();
      if (exitCode != 0) {
        throw new RuntimeException(docName + "OCR识别失败: " + output);
      }
      ErpRecognizedInboundResultVo inboundResult = OBJECT_MAPPER.readValue(output, ErpRecognizedInboundResultVo.class);
      ErpInventoryAdjustmentRecognizeVo result = new ErpInventoryAdjustmentRecognizeVo();
      result.setSuccess(Boolean.TRUE);
      result.setRawText(inboundResult == null ? null : inboundResult.getRawText());
      for (Path savedPath : savedPaths) {
        ErpRecognizedInboundFileVo fileVo = new ErpRecognizedInboundFileVo();
        fileVo.setFilePath(savedPath.toAbsolutePath().toString());
        fileVo.setFileName(savedPath.getFileName().toString());
        result.getFileList().add(fileVo);
      }
      List<ErpRecognizedInboundItemVo> recognizedItems = inboundResult == null || inboundResult.getInboundDraft() == null
          ? new ArrayList<>() : inboundResult.getInboundDraft().getItemList();
      List<ErpInventoryBatchVo> lots = buildCurrentLots(null);
      if (recognizedItems != null) {
        for (ErpRecognizedInboundItemVo item : recognizedItems) {
          result.getItemList().add(matchRecognizedItem(item, lots, adjustmentType));
        }
      }
      return result;
    } finally {
      if (listFile != null) {
        Files.deleteIfExists(listFile);
      }
      for (Path tempFile : tempFiles) {
        Files.deleteIfExists(tempFile);
      }
    }
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
      if (TYPE_WAREHOUSE.equals(request.getAdjustmentType())
          && source.getAvailableWeightKg() != null
          && transferWeight.compareTo(source.getAvailableWeightKg()) > 0) {
        throw new RuntimeException("第" + lineNo + "行调整重量不能大于可售重量");
      }
      if (input.getTargetWarehouseId() == null) {
        throw new RuntimeException("第" + lineNo + "行请选择目标仓库");
      }
      ErpWarehouseEntity targetWarehouse = erpWarehouseDao.selectById(input.getTargetWarehouseId());
      if (targetWarehouse == null) {
        throw new RuntimeException("第" + lineNo + "行目标仓库不存在");
      }
      Date targetExpiryDate = source.getExpiryDate();
      String targetTemperatureZone = source.getTemperatureZone();
      if (TYPE_WAREHOUSE.equals(request.getAdjustmentType())) {
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
      item.setTargetWarehouseId(targetWarehouse.getId());
      item.setTargetWarehouseName(targetWarehouse.getWarehouseName());
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
    saveFiles(adjustment.getId(), request.getFileList(), now);
  }

  private ErpInventoryAdjustmentRecognizedItemVo matchRecognizedItem(ErpRecognizedInboundItemVo item, List<ErpInventoryBatchVo> lots, String adjustmentType) {
    ErpInventoryAdjustmentRecognizedItemVo vo = new ErpInventoryAdjustmentRecognizedItemVo();
    if (item == null) {
      vo.setMatched(Boolean.FALSE);
      vo.setMatchMessage("识别行为空");
      return vo;
    }
    String code = normalizeProductCode(firstNonBlank(item.getProductCode(), extractProductCode(item.getSkuCode())));
    String containerNo = extractContainerNo(item.getSkuCode());
    String factoryNo = StringUtils.trimToEmpty(item.getFactoryNo());
    vo.setRecognizedProductCode(code);
    vo.setRecognizedSkuCode(item.getSkuCode());
    vo.setRecognizedProductName(item.getProductName());
    vo.setRecognizedFactoryNo(factoryNo);
    vo.setRecognizedContainerNo(containerNo);
    vo.setRecognizedExpectedQty(item.getExpectedQty());
    vo.setRecognizedActualQty(item.getActualQty());
    vo.setRecognizedSpecWeight(item.getSpecWeight());
    vo.setRecognizedTotalWeightKg(item.getTotalWeightKg());
    vo.setRecognizedInboundDate(item.getProductionDate());
    ErpInventoryBatchVo matched = null;
    for (ErpInventoryBatchVo lot : lots) {
      if (lot.getAvailableBoxes() == null || lot.getAvailableBoxes() <= 0) {
        continue;
      }
      if (TYPE_FRESH_TO_FROZEN.equals(adjustmentType) && isFrozen(lot.getTemperatureZone())) {
        continue;
      }
      if (!StringUtils.equalsIgnoreCase(StringUtils.trimToEmpty(lot.getProductCode()), StringUtils.trimToEmpty(code))) {
        continue;
      }
      if (StringUtils.isNotBlank(containerNo) && !sameContainer(lot.getContainerNo(), containerNo)) {
        continue;
      }
      if (StringUtils.isNotBlank(factoryNo) && !StringUtils.equalsIgnoreCase(StringUtils.trimToEmpty(lot.getFactoryNo()), factoryNo)) {
        continue;
      }
      matched = lot;
      break;
    }
    if (matched == null) {
      vo.setMatched(Boolean.FALSE);
      vo.setMatchMessage(TYPE_FRESH_TO_FROZEN.equals(adjustmentType)
          ? "未匹配到当前冷鲜可用库存"
          : "未匹配到当前可用库存");
      return vo;
    }
    fillRecognizedMatch(vo, matched);
    vo.setMatched(Boolean.TRUE);
    vo.setMatchMessage("已匹配");
    return vo;
  }

  private void fillRecognizedMatch(ErpInventoryAdjustmentRecognizedItemVo vo, ErpInventoryBatchVo lot) {
    vo.setSourceAdjustmentItemId(lot.getSourceAdjustmentItemId());
    vo.setInboundOrderId(lot.getInboundOrderId());
    vo.setInboundItemId(lot.getInboundItemId());
    vo.setPackingItemId(lot.getPackingItemId());
    vo.setBatchId(lot.getBatchId());
    vo.setProductId(lot.getProductId());
    vo.setProductCode(lot.getProductCode());
    vo.setProductName(lot.getProductName());
    vo.setProductNameEn(lot.getProductNameEn());
    vo.setProductSpec(lot.getProductSpec());
    vo.setWarehouseId(lot.getWarehouseId());
    vo.setWarehouseName(lot.getWarehouseName());
    vo.setContainerNo(lot.getContainerNo());
    vo.setFactoryNo(lot.getFactoryNo());
    vo.setTemperatureZone(lot.getTemperatureZone());
    vo.setProductionDate(lot.getProductionDate());
    vo.setExpiryDate(lot.getExpiryDate());
    vo.setAvailableBoxes(lot.getAvailableBoxes());
    vo.setAvailableWeightKg(lot.getAvailableWeightKg());
  }

  private void saveFiles(Long adjustmentId, List<ErpRecognizedInboundFileVo> fileList, Date now) {
    if (fileList == null || fileList.isEmpty()) {
      return;
    }
    int lineNo = 1;
    for (ErpRecognizedInboundFileVo file : fileList) {
      if (file == null || StringUtils.isBlank(file.getFilePath())) {
        continue;
      }
      ErpInventoryAdjustmentFileEntity entity = new ErpInventoryAdjustmentFileEntity();
      entity.setAdjustmentId(adjustmentId);
      entity.setLineNo(lineNo++);
      entity.setFilePath(file.getFilePath());
      entity.setFileName(firstNonBlank(file.getFileName(), new File(file.getFilePath()).getName()));
      entity.setCreateTime(now);
      entity.setUpdateTime(now);
      erpInventoryAdjustmentFileDao.insert(entity);
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

  private String firstNonBlank(String... values) {
    for (String value : values) {
      if (StringUtils.isNotBlank(value)) {
        return value;
      }
    }
    return null;
  }

  private String normalizeProductCode(String code) {
    if (StringUtils.isBlank(code)) {
      return null;
    }
    return StringUtils.upperCase(code).replaceAll("[^0-9]", "");
  }

  private String extractProductCode(String skuCode) {
    if (StringUtils.isBlank(skuCode)) {
      return null;
    }
    java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("C(\\d{5})").matcher(StringUtils.upperCase(skuCode));
    return matcher.find() ? matcher.group(1) : null;
  }

  private String extractContainerNo(String skuCode) {
    if (StringUtils.isBlank(skuCode)) {
      return null;
    }
    java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(MCRU\\d{7}(?:-\\d+)?)").matcher(StringUtils.upperCase(skuCode));
    return matcher.find() ? matcher.group(1) : null;
  }

  private boolean sameContainer(String left, String right) {
    String l = StringUtils.upperCase(StringUtils.trimToEmpty(left));
    String r = StringUtils.upperCase(StringUtils.trimToEmpty(right));
    return StringUtils.equals(l, r) || StringUtils.startsWith(l, r) || StringUtils.startsWith(r, l);
  }

  private File ensureInboundOcrScriptFile() throws Exception {
    File target = new File(System.getProperty("java.io.tmpdir"), "erp_inbound_ocr.py");
    ClassPathResource resource = new ClassPathResource("ocr/erp_inbound_ocr.py");
    try (InputStream inputStream = resource.getInputStream()) {
      Files.copy(inputStream, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }
    return target;
  }

  private String getSuffix(String filename) {
    if (StringUtils.isBlank(filename) || filename.lastIndexOf(".") < 0) {
      return ".tmp";
    }
    return filename.substring(filename.lastIndexOf("."));
  }

  private Path saveAdjustmentFile(MultipartFile file, Path tempFile, String suffix, String type) throws Exception {
    String baseDir = "D:\\renren-fast-vue\\renren-fast\\uploads\\inventory-adjustment";
    String dayFolder = new SimpleDateFormat("yyyyMMdd").format(new Date());
    Path dir = Paths.get(baseDir, type, dayFolder);
    Files.createDirectories(dir);
    String originalName = StringUtils.defaultString(file.getOriginalFilename(), "inventory-adjustment-file")
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">");
    String safeName = originalName.replaceAll("[\\\\/:*?\"<>|\\s]+", "_");
    if (StringUtils.isBlank(safeName)) {
      safeName = "inventory-adjustment-file" + suffix;
    }
    if (!safeName.toLowerCase().endsWith(suffix.toLowerCase())) {
      safeName = safeName + suffix;
    }
    Path target = dir.resolve(System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + "_" + safeName);
    Files.copy(tempFile, target, StandardCopyOption.REPLACE_EXISTING);
    return target;
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
