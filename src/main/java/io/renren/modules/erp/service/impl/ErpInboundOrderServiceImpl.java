package io.renren.modules.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.renren.common.utils.PageUtils;
import io.renren.common.utils.Query;
import io.renren.modules.erp.dao.ErpInboundOrderDao;
import io.renren.modules.erp.dao.ErpInboundOrderFileDao;
import io.renren.modules.erp.dao.ErpInboundOrderItemDao;
import io.renren.modules.erp.dao.ErpDriverDao;
import io.renren.modules.erp.dao.ErpPresaleConfirmDao;
import io.renren.modules.erp.dao.ErpPresaleOrderDao;
import io.renren.modules.erp.dao.ErpPresalePackingDao;
import io.renren.modules.erp.dao.ErpPresalePackingItemDao;
import io.renren.modules.erp.dao.ErpProductDao;
import io.renren.modules.erp.dao.ErpWarehouseDao;
import io.renren.modules.erp.entity.ErpInboundOrderEntity;
import io.renren.modules.erp.entity.ErpInboundOrderFileEntity;
import io.renren.modules.erp.entity.ErpInboundOrderItemEntity;
import io.renren.modules.erp.entity.ErpDriverEntity;
import io.renren.modules.erp.entity.ErpPresaleConfirmEntity;
import io.renren.modules.erp.entity.ErpPresaleOrderEntity;
import io.renren.modules.erp.entity.ErpPresalePackingEntity;
import io.renren.modules.erp.entity.ErpPresalePackingItemEntity;
import io.renren.modules.erp.entity.ErpProductEntity;
import io.renren.modules.erp.entity.ErpWarehouseEntity;
import io.renren.modules.erp.service.ErpExpenseService;
import io.renren.modules.erp.service.ErpInboundOrderService;
import io.renren.modules.erp.vo.ErpInboundListVo;
import io.renren.modules.erp.vo.ErpRecognizedInboundDraftVo;
import io.renren.modules.erp.vo.ErpRecognizedInboundFileVo;
import io.renren.modules.erp.vo.ErpRecognizedInboundItemVo;
import io.renren.modules.erp.vo.ErpRecognizedInboundResultVo;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service("erpInboundOrderService")
public class ErpInboundOrderServiceImpl extends ServiceImpl<ErpInboundOrderDao, ErpInboundOrderEntity>
    implements ErpInboundOrderService {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  static {
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    OBJECT_MAPPER.setDateFormat(dateFormat);
  }

  @Autowired
  private ErpInboundOrderItemDao erpInboundOrderItemDao;
  @Autowired
  private ErpInboundOrderFileDao erpInboundOrderFileDao;
  @Autowired
  private ErpPresaleOrderDao erpPresaleOrderDao;
  @Autowired
  private ErpPresaleConfirmDao erpPresaleConfirmDao;
  @Autowired
  private ErpPresalePackingDao erpPresalePackingDao;
  @Autowired
  private ErpPresalePackingItemDao erpPresalePackingItemDao;
  @Autowired
  private ErpProductDao erpProductDao;
  @Autowired
  private ErpWarehouseDao erpWarehouseDao;
  @Autowired
  private ErpExpenseService erpExpenseService;
  @Autowired
  private ErpDriverDao erpDriverDao;

  @Override
  public PageUtils queryPage(Map<String, Object> params) {
    String keyword = params.get("keyword") == null ? null : params.get("keyword").toString();
    IPage<ErpInboundListVo> page = new Query<ErpInboundListVo>().getPage(params);
    List<ErpInboundListVo> records = baseMapper.queryReadyPresaleList(page, keyword);
    page.setRecords(records);
    return new PageUtils(page);
  }

  @Override
  public ErpInboundOrderEntity getDetail(Long presaleOrderId, Long confirmId) {
    QueryWrapper<ErpInboundOrderEntity> query = new QueryWrapper<ErpInboundOrderEntity>()
        .eq("presale_order_id", presaleOrderId);
    if (confirmId != null && confirmId > 0) {
      query.eq("confirm_id", confirmId);
    }
    ErpInboundOrderEntity inbound = this.getOne(query.last("limit 1"));
    if (inbound == null) {
      inbound = buildDefaultOrder(presaleOrderId, confirmId);
      inbound.setUploadStatus(0);
      return inbound;
    }
    loadChildren(inbound);
    inbound.setUploadStatus(1);
    return inbound;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void saveOrder(ErpInboundOrderEntity order, Long userId) {
    upsertOrder(order, userId);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void updateOrder(ErpInboundOrderEntity order, Long userId) {
    upsertOrder(order, userId);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void saveItemDamage(Long itemId, BigDecimal damageWeightKg, String damageReason) {
    if (itemId == null || itemId <= 0) {
      throw new RuntimeException("入库明细不能为空");
    }
    ErpInboundOrderItemEntity item = erpInboundOrderItemDao.selectById(itemId);
    if (item == null) {
      throw new RuntimeException("入库明细不存在");
    }
    if (damageWeightKg != null && damageWeightKg.compareTo(BigDecimal.ZERO) < 0) {
      throw new RuntimeException("报损重量不能小于0");
    }
    String reason = StringUtils.trimToEmpty(damageReason);
    if (reason.length() > 200) {
      throw new RuntimeException("报损原因最多200字");
    }
    item.setDamageWeightKg(damageWeightKg == null ? null : damageWeightKg.setScale(2, RoundingMode.HALF_UP));
    item.setDamageReason(StringUtils.isBlank(reason) ? null : reason);
    item.setUpdateTime(new Date());
    erpInboundOrderItemDao.updateById(item);
  }

  @Override
  public ErpRecognizedInboundResultVo recognize(Long presaleOrderId, Long confirmId, MultipartFile[] files) throws Exception {
    if (files == null || files.length == 0) {
      throw new RuntimeException("请先上传入库单文件");
    }
    List<Path> tempFiles = new ArrayList<Path>();
    List<Path> savedPaths = new ArrayList<Path>();
    Path listFile = null;
    try {
      for (MultipartFile file : files) {
        String suffix = getSuffix(file.getOriginalFilename());
        Path tempFile = Files.createTempFile("erp-inbound-", suffix);
        tempFiles.add(tempFile);
        file.transferTo(tempFile.toFile());
        savedPaths.add(saveInboundFile(file, tempFile, suffix));
      }
      listFile = Files.createTempFile("erp-inbound-paths-", ".txt");
      List<String> pathLines = new ArrayList<String>();
      for (Path savedPath : savedPaths) {
        pathLines.add(savedPath.toAbsolutePath().toString());
      }
      Files.write(listFile, pathLines, StandardCharsets.UTF_8);

      File scriptFile = ensureScriptFile();
      ProcessBuilder processBuilder = new ProcessBuilder(
          "python",
          "-X",
          "utf8",
          scriptFile.getAbsolutePath(),
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
        throw new RuntimeException("入库单OCR识别失败: " + output);
      }
      ErpRecognizedInboundResultVo result = OBJECT_MAPPER.readValue(output, ErpRecognizedInboundResultVo.class);
      if (result == null || result.getInboundDraft() == null) {
        throw new RuntimeException("入库单OCR结果为空");
      }
      result.setSuccess(Boolean.TRUE);
      enrichDraft(presaleOrderId, confirmId, result.getInboundDraft(), savedPaths);
      // The raw OCR dump is only used for debugging and can be very large. Returning it to
      // the browser makes the response unnecessarily heavy and increases the chance of the
      // client aborting the request before it finishes.
      result.setRawText(null);
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
  public ResponseEntity<byte[]> downloadFile(Long fileId) {
    ErpInboundOrderFileEntity file = erpInboundOrderFileDao.selectById(fileId);
    return download(file == null ? null : file.getFilePath(), file == null ? null : file.getFileName());
  }

  @Transactional(rollbackFor = Exception.class)
  protected void upsertOrder(ErpInboundOrderEntity order, Long userId) {
    if (order == null || order.getPresaleOrderId() == null || order.getPresaleOrderId() <= 0) {
      throw new RuntimeException("预销售单不能为空");
    }
    Date now = new Date();
    QueryWrapper<ErpInboundOrderEntity> existingQuery = new QueryWrapper<ErpInboundOrderEntity>()
        .eq("presale_order_id", order.getPresaleOrderId());
    if (order.getConfirmId() != null && order.getConfirmId() > 0) {
      existingQuery.eq("confirm_id", order.getConfirmId());
    }
    ErpInboundOrderEntity existing = this.getOne(existingQuery.last("limit 1"));
    normalizeOrder(order, userId, now, existing == null);
    if (existing == null) {
      this.save(order);
    } else {
      order.setId(existing.getId());
      this.updateById(order);
      erpInboundOrderItemDao.delete(new QueryWrapper<ErpInboundOrderItemEntity>().eq("inbound_order_id", existing.getId()));
      erpInboundOrderFileDao.delete(new QueryWrapper<ErpInboundOrderFileEntity>().eq("inbound_order_id", existing.getId()));
    }
    saveItems(order, now);
    saveFiles(order, now);
    erpExpenseService.regenerateInboundHandlingExpense(order, userId);
  }

  private void normalizeOrder(ErpInboundOrderEntity order, Long userId, Date now, boolean create) {
    ErpInboundOrderEntity defaults = buildDefaultOrder(order.getPresaleOrderId(), order.getConfirmId());
    if (order.getWarehouseId() == null || order.getWarehouseId() <= 0) {
      throw new RuntimeException("请选择仓库");
    }
    ErpWarehouseEntity warehouse = erpWarehouseDao.selectById(order.getWarehouseId());
    if (warehouse == null) {
      throw new RuntimeException("所选仓库不存在");
    }
    order.setBrandId(order.getBrandId() == null ? defaults.getBrandId() : order.getBrandId());
    order.setConfirmId(order.getConfirmId() == null ? defaults.getConfirmId() : order.getConfirmId());
    order.setBrandName(firstNonBlank(order.getBrandName(), defaults.getBrandName()));
    order.setContractNo(firstNonBlank(order.getContractNo(), defaults.getContractNo()));
    order.setCustomerName(firstNonBlank(order.getCustomerName(), defaults.getCustomerName()));
    order.setWarehouseName(warehouse.getWarehouseName());
    order.setOrderDate(order.getOrderDate() == null ? defaults.getOrderDate() : order.getOrderDate());
    order.setExpectedArrivalDate(order.getExpectedArrivalDate() == null ? defaults.getExpectedArrivalDate() : order.getExpectedArrivalDate());
    order.setContainerNo(firstNonBlank(order.getContainerNo(), defaults.getContainerNo()));
    applyDriverSnapshot(order);
    if (create) {
      order.setCreateUserId(userId);
      order.setCreateTime(now);
    }
    order.setUpdateTime(now);
  }

  private void applyDriverSnapshot(ErpInboundOrderEntity order) {
    if (order == null || order.getDriverId() == null || order.getDriverId() <= 0) {
      return;
    }
    ErpDriverEntity driver = erpDriverDao.selectById(order.getDriverId());
    if (driver == null) {
      throw new RuntimeException("所选司机不存在");
    }
    if (driver.getStatus() != null && driver.getStatus() != 1) {
      throw new RuntimeException("所选司机已停用");
    }
    order.setDriverName(firstNonBlank(driver.getDriverName(), order.getDriverName()));
    order.setTruckNo(firstNonBlank(driver.getPlateNo(), order.getTruckNo()));
    order.setDriverPhone(firstNonBlank(driver.getMobile(), order.getDriverPhone()));
    order.setIdCardNo(firstNonBlank(driver.getIdCardNo(), order.getIdCardNo()));
  }

  private void saveItems(ErpInboundOrderEntity order, Date now) {
    Map<String, Integer> packingBoxMap = loadPackingBoxMap(order.getPresaleOrderId(), order.getConfirmId());
    List<ErpInboundOrderItemEntity> items = order.getItemList() == null ? new ArrayList<ErpInboundOrderItemEntity>() : order.getItemList();
    Map<String, Integer> actualQtyByCode = new HashMap<String, Integer>();
    int lineNo = 1;
    for (ErpInboundOrderItemEntity item : items) {
      if (item == null) {
        continue;
      }
      validateRequiredItemFields(item, lineNo);
      enrichItem(order.getPresaleOrderId(), item, packingBoxMap);
      actualQtyByCode.put(item.getProductCode(),
          defaultInt(actualQtyByCode.get(item.getProductCode())) + defaultInt(item.getActualQty()));
      item.setInboundOrderId(order.getId());
      item.setLineNo(lineNo++);
      item.setCreateTime(now);
      item.setUpdateTime(now);
      erpInboundOrderItemDao.insert(item);
    }
    validatePackingBoxMatch(actualQtyByCode, packingBoxMap);
  }

  private void validateRequiredItemFields(ErpInboundOrderItemEntity item, int lineNo) {
    if (StringUtils.isBlank(item.getSkuCode())) {
      throw new RuntimeException("第" + lineNo + "行SKU不能为空");
    }
    if (item.getProductId() == null && StringUtils.isBlank(item.getProductCode())) {
      throw new RuntimeException("第" + lineNo + "行产品编码不能为空");
    }
    if (item.getExpectedQty() == null) {
      throw new RuntimeException("第" + lineNo + "行预期数不能为空");
    }
    if (item.getActualQty() == null) {
      throw new RuntimeException("第" + lineNo + "行实收数不能为空");
    }
  }

  private void validatePackingBoxMatch(Map<String, Integer> actualQtyByCode, Map<String, Integer> packingBoxMap) {
    for (Map.Entry<String, Integer> entry : packingBoxMap.entrySet()) {
      String productCode = entry.getKey();
      int packingBoxes = defaultInt(entry.getValue());
      int actualQty = defaultInt(actualQtyByCode.get(productCode));
      if (actualQty != packingBoxes) {
        throw new RuntimeException("产品" + productCode + "的实收数与装箱单箱数不一致，请核对");
      }
    }
    int totalActualQty = 0;
    for (Integer actualQty : actualQtyByCode.values()) {
      totalActualQty += defaultInt(actualQty);
    }
    int totalPackingBoxes = 0;
    for (Integer packingBoxes : packingBoxMap.values()) {
      totalPackingBoxes += defaultInt(packingBoxes);
    }
    if (totalActualQty != totalPackingBoxes) {
      throw new RuntimeException("入库单总实收数与装箱单总箱数不一致，请核对");
    }
  }

  private void saveFiles(ErpInboundOrderEntity order, Date now) {
    List<ErpInboundOrderFileEntity> files = order.getFileList() == null ? new ArrayList<ErpInboundOrderFileEntity>() : order.getFileList();
    int lineNo = 1;
    for (ErpInboundOrderFileEntity file : files) {
      if (file == null || StringUtils.isBlank(file.getFilePath())) {
        continue;
      }
      file.setInboundOrderId(order.getId());
      file.setLineNo(lineNo++);
      file.setFileName(firstNonBlank(file.getFileName(), new File(file.getFilePath()).getName()));
      file.setCreateTime(now);
      file.setUpdateTime(now);
      erpInboundOrderFileDao.insert(file);
    }
  }

  private void loadChildren(ErpInboundOrderEntity inbound) {
    inbound.setItemList(erpInboundOrderItemDao.selectList(
        new QueryWrapper<ErpInboundOrderItemEntity>().eq("inbound_order_id", inbound.getId()).orderByAsc("line_no", "id")));
    inbound.setFileList(erpInboundOrderFileDao.selectList(
        new QueryWrapper<ErpInboundOrderFileEntity>().eq("inbound_order_id", inbound.getId()).orderByAsc("line_no", "id")));
    inbound.setExpenseList(erpExpenseService.listBySource(ErpExpenseService.SOURCE_INBOUND_ORDER, inbound.getId()));
  }

  private ErpInboundOrderEntity buildDefaultOrder(Long presaleOrderId, Long confirmId) {
    ErpPresaleOrderEntity presale = erpPresaleOrderDao.selectById(presaleOrderId);
    if (presale == null) {
      throw new RuntimeException("预销售单不存在");
    }
    ErpPresaleConfirmEntity confirm = loadConfirm(presaleOrderId, confirmId);
    ErpPresalePackingEntity packing = loadPacking(presaleOrderId, confirm == null ? confirmId : confirm.getId());
    ErpInboundOrderEntity inbound = new ErpInboundOrderEntity();
    inbound.setPresaleOrderId(presaleOrderId);
    inbound.setConfirmId(confirm == null ? confirmId : confirm.getId());
    inbound.setBrandId(presale.getBrandId());
    inbound.setBrandName(presale.getBrandName());
    inbound.setContractNo(firstNonBlank(confirm == null ? null : confirm.getContractNo(), presale.getSellerContractNo()));
    inbound.setCustomerName(presale.getCustomerReference());
    inbound.setOrderDate(presale.getOrderDate());
    inbound.setExpectedArrivalDate(confirm == null ? presale.getExpectedDate() : firstNonBlankDate(confirm.getExpectedArrivalDate(), presale.getExpectedDate()));
    inbound.setContainerNo(firstNonBlank(confirm == null ? null : confirm.getContainerNo(), packing == null ? null : packing.getContainerNo()));
    inbound.setItemList(new ArrayList<ErpInboundOrderItemEntity>());
    inbound.setFileList(new ArrayList<ErpInboundOrderFileEntity>());
    return inbound;
  }

  private ErpPresaleConfirmEntity loadConfirm(Long presaleOrderId, Long confirmId) {
    if (confirmId != null && confirmId > 0) {
      ErpPresaleConfirmEntity confirm = erpPresaleConfirmDao.selectById(confirmId);
      if (confirm != null && (presaleOrderId == null || presaleOrderId.equals(confirm.getPresaleOrderId()))) {
        return confirm;
      }
    }
    return erpPresaleConfirmDao.selectOne(
        new QueryWrapper<ErpPresaleConfirmEntity>()
            .eq("presale_order_id", presaleOrderId)
            .orderByDesc("id")
            .last("limit 1"));
  }

  private ErpPresalePackingEntity loadPacking(Long presaleOrderId, Long confirmId) {
    if (confirmId != null && confirmId > 0) {
      ErpPresalePackingEntity packing = erpPresalePackingDao.selectOne(
          new QueryWrapper<ErpPresalePackingEntity>()
              .eq("confirm_id", confirmId)
              .last("limit 1"));
      if (packing != null) {
        return packing;
      }
    }
    return erpPresalePackingDao.selectOne(
        new QueryWrapper<ErpPresalePackingEntity>()
            .eq("presale_order_id", presaleOrderId)
            .orderByDesc("id")
            .last("limit 1"));
  }

  private void enrichDraft(Long presaleOrderId, Long confirmId, ErpRecognizedInboundDraftVo draft, List<Path> savedPaths) {
    ErpInboundOrderEntity defaults = buildDefaultOrder(presaleOrderId, confirmId);
    draft.setPresaleOrderId(presaleOrderId);
    draft.setConfirmId(defaults.getConfirmId());
    draft.setBrandId(defaults.getBrandId());
    draft.setBrandName(defaults.getBrandName());
    draft.setContractNo(firstNonBlank(draft.getContractNo(), defaults.getContractNo()));
    draft.setCustomerName(firstNonBlank(draft.getCustomerName(), defaults.getCustomerName()));
    draft.setOrderDate(draft.getOrderDate() == null ? defaults.getOrderDate() : draft.getOrderDate());
    draft.setExpectedArrivalDate(draft.getExpectedArrivalDate() == null ? defaults.getExpectedArrivalDate() : draft.getExpectedArrivalDate());
    draft.setContainerNo(firstNonBlank(draft.getContainerNo(), defaults.getContainerNo()));
    draft.setFileList(new ArrayList<ErpRecognizedInboundFileVo>());
    for (Path savedPath : savedPaths) {
      ErpRecognizedInboundFileVo fileVo = new ErpRecognizedInboundFileVo();
      fileVo.setFilePath(savedPath.toAbsolutePath().toString());
      fileVo.setFileName(savedPath.getFileName().toString());
      draft.getFileList().add(fileVo);
    }
    Map<String, Integer> packingBoxMap = loadPackingBoxMap(presaleOrderId, defaults.getConfirmId());
    if (draft.getItemList() == null) {
      draft.setItemList(new ArrayList<ErpRecognizedInboundItemVo>());
    }
    for (ErpRecognizedInboundItemVo item : draft.getItemList()) {
      enrichDraftItem(item, packingBoxMap);
    }
  }

  private void enrichDraftItem(ErpRecognizedInboundItemVo item, Map<String, Integer> packingBoxMap) {
    if (item == null) {
      return;
    }
    String code = normalizeProductCode(firstNonBlank(item.getProductCode(), extractProductCode(item.getSkuCode())));
    item.setProductCode(code);
    ErpProductEntity product = resolveProduct(code);
    if (product != null) {
      item.setProductId(product.getId());
      item.setProductCode(product.getProductCode());
      item.setProductName(firstNonBlank(product.getProductName(), item.getProductName()));
      item.setProductNameEn(firstNonBlank(product.getProductNameEn(), item.getProductNameEn()));
      item.setProductSpec(firstNonBlank(product.getProductSpec(), item.getProductSpec()));
      item.setUnit(firstNonBlank(product.getUnit(), item.getUnit()));
      item.setPackingBoxes(packingBoxMap.get(product.getProductCode()));
    } else {
      item.setPackingBoxes(packingBoxMap.get(code));
    }
    if (item.getExpectedQty() == null) {
      item.setExpectedQty(0);
    }
    if (item.getActualQty() == null) {
      item.setActualQty(0);
    }
    if (item.getPackingBoxes() == null) {
      item.setPackingBoxes(0);
    }
    if (item.getShelfLifeDays() == null && item.getProductionDate() != null && item.getExpiryDate() != null) {
      long diff = (item.getExpiryDate().getTime() - item.getProductionDate().getTime()) / (24L * 60L * 60L * 1000L);
      item.setShelfLifeDays((int) diff);
    }
  }

  private void enrichItem(Long presaleOrderId, ErpInboundOrderItemEntity item, Map<String, Integer> packingBoxMap) {
    String code = normalizeProductCode(firstNonBlank(item.getProductCode(), extractProductCode(item.getSkuCode())));
    ErpProductEntity product = resolveProduct(code);
    if (product == null) {
      throw new RuntimeException("入库单存在未匹配系统产品的明细，产品编码：" + StringUtils.defaultIfBlank(code, "-"));
    }
    item.setProductId(product.getId());
    item.setProductCode(product.getProductCode());
    item.setProductName(product.getProductName());
    item.setProductNameEn(product.getProductNameEn());
    item.setProductSpec(firstNonBlank(product.getProductSpec(), item.getProductSpec()));
    item.setUnit(firstNonBlank(product.getUnit(), item.getUnit()));
    item.setPackingBoxes(packingBoxMap.get(product.getProductCode()) == null ? 0 : packingBoxMap.get(product.getProductCode()));
    item.setExpectedQty(item.getExpectedQty() == null ? 0 : item.getExpectedQty());
    item.setActualQty(item.getActualQty() == null ? 0 : item.getActualQty());
    item.setSpecWeight(scale4(item.getSpecWeight()));
    if (item.getExpiryDate() == null && item.getProductionDate() != null && item.getShelfLifeDays() != null) {
      item.setExpiryDate(new Date(item.getProductionDate().getTime() + item.getShelfLifeDays() * 24L * 60L * 60L * 1000L));
    }
  }

  @Override
  public Map<String, Integer> getPackingBoxMap(Long presaleOrderId, Long confirmId) {
    return loadPackingBoxMap(presaleOrderId, confirmId);
  }

  private Map<String, Integer> loadPackingBoxMap(Long presaleOrderId, Long confirmId) {
    Map<String, Integer> result = new HashMap<String, Integer>();
    ErpPresalePackingEntity packing = loadPacking(presaleOrderId, confirmId);
    if (packing == null) {
      return result;
    }
    List<ErpPresalePackingItemEntity> items = erpPresalePackingItemDao.selectList(
        new QueryWrapper<ErpPresalePackingItemEntity>().eq("packing_id", packing.getId()));
    for (ErpPresalePackingItemEntity item : items) {
      if (StringUtils.isBlank(item.getProductCode())) {
        continue;
      }
      result.put(item.getProductCode(), item.getTotalBoxes() == null ? 0 : item.getTotalBoxes());
    }
    return result;
  }

  private ErpProductEntity resolveProduct(String code) {
    if (StringUtils.isBlank(code)) {
      return null;
    }
    ErpProductEntity byCode = erpProductDao.selectOne(new QueryWrapper<ErpProductEntity>().eq("product_code", code).last("limit 1"));
    if (byCode != null) {
      return byCode;
    }
    String normalizedCode = normalizeProductCode(code);
    if (StringUtils.isNotBlank(normalizedCode) && !StringUtils.equals(normalizedCode, code)) {
      byCode = erpProductDao.selectOne(new QueryWrapper<ErpProductEntity>().eq("product_code", normalizedCode).last("limit 1"));
      if (byCode != null) {
        return byCode;
      }
    }
    List<ErpProductEntity> products = erpProductDao.selectList(new QueryWrapper<ErpProductEntity>().isNotNull("alias_codes"));
    for (ErpProductEntity product : products) {
      if (StringUtils.isBlank(product.getAliasCodes())) {
        continue;
      }
      String[] aliases = product.getAliasCodes().split(",");
      for (String alias : aliases) {
        if (StringUtils.equalsIgnoreCase(alias.trim(), code.trim())) {
          return product;
        }
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

  private File ensureScriptFile() throws Exception {
    File target = new File(System.getProperty("java.io.tmpdir"), "erp_inbound_ocr.py");
    ClassPathResource resource = new ClassPathResource("ocr/erp_inbound_ocr.py");
    try (InputStream inputStream = resource.getInputStream()) {
      Files.copy(inputStream, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }
    FileUtils.touch(target);
    return target;
  }

  private String getSuffix(String filename) {
    if (StringUtils.isBlank(filename) || filename.lastIndexOf(".") < 0) {
      return ".tmp";
    }
    return filename.substring(filename.lastIndexOf("."));
  }

  private Path saveInboundFile(MultipartFile file, Path tempFile, String suffix) throws Exception {
    String baseDir = "D:\\renren-fast-vue\\renren-fast\\uploads\\inbound";
    String dayFolder = new SimpleDateFormat("yyyyMMdd").format(new Date());
    Path dir = Paths.get(baseDir, dayFolder);
    Files.createDirectories(dir);
    String originalName = StringUtils.defaultString(file.getOriginalFilename(), "inbound-file")
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">");
    String safeName = originalName.replaceAll("[\\\\/:*?\"<>|\\s]+", "_");
    if (StringUtils.isBlank(safeName)) {
      safeName = "inbound-file" + suffix;
    }
    if (!safeName.toLowerCase().endsWith(suffix.toLowerCase())) {
      safeName = safeName + suffix;
    }
    Path target = dir.resolve(System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + "_" + safeName);
    Files.copy(tempFile, target, StandardCopyOption.REPLACE_EXISTING);
    return target;
  }

  private ResponseEntity<byte[]> download(String filePath, String fileName) {
    if (StringUtils.isBlank(filePath)) {
      throw new RuntimeException("文件不存在");
    }
    try {
      File file = new File(filePath);
      if (!file.exists()) {
        throw new RuntimeException("文件不存在");
      }
      byte[] bytes = Files.readAllBytes(file.toPath());
      String downloadName = StringUtils.defaultIfBlank(fileName, file.getName());
      return ResponseEntity.ok()
          .contentType(MediaType.APPLICATION_OCTET_STREAM)
          .header(HttpHeaders.CONTENT_DISPOSITION,
              ContentDisposition.attachment().filename(downloadName, StandardCharsets.UTF_8).build().toString())
          .body(bytes);
    } catch (Exception ex) {
      throw new RuntimeException("文件下载失败: " + ex.getMessage(), ex);
    }
  }

  private int defaultInt(Integer value) {
    return value == null ? 0 : value;
  }

  private BigDecimal scale4(BigDecimal value) {
    return value == null ? null : value.setScale(4, RoundingMode.HALF_UP);
  }

  private String firstNonBlank(String... values) {
    for (String value : values) {
      if (StringUtils.isNotBlank(value)) {
        return value;
      }
    }
    return null;
  }

  private Date firstNonBlankDate(Date first, Date second) {
    return first == null ? second : first;
  }
}
