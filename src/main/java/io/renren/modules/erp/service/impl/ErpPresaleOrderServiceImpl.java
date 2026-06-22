package io.renren.modules.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.renren.common.utils.PageUtils;
import io.renren.common.utils.Query;
import io.renren.modules.erp.dao.ErpPartnerDao;
import io.renren.modules.erp.dao.ErpPresaleAttachmentDao;
import io.renren.modules.erp.dao.ErpPresaleConfirmDao;
import io.renren.modules.erp.dao.ErpPresaleConfirmItemDao;
import io.renren.modules.erp.dao.ErpPresalePackingBatchDao;
import io.renren.modules.erp.dao.ErpPresalePackingDao;
import io.renren.modules.erp.dao.ErpPresalePackingItemDao;
import io.renren.modules.erp.dao.ErpPresaleOrderDao;
import io.renren.modules.erp.dao.ErpPresaleOrderItemDao;
import io.renren.modules.erp.dao.ErpProductDao;
import io.renren.modules.erp.entity.ErpPartnerEntity;
import io.renren.modules.erp.entity.ErpPresaleAttachmentEntity;
import io.renren.modules.erp.entity.ErpPresaleConfirmEntity;
import io.renren.modules.erp.entity.ErpPresaleConfirmItemEntity;
import io.renren.modules.erp.entity.ErpPresalePackingBatchEntity;
import io.renren.modules.erp.entity.ErpPresalePackingEntity;
import io.renren.modules.erp.entity.ErpPresalePackingItemEntity;
import io.renren.modules.erp.entity.ErpPresaleOrderEntity;
import io.renren.modules.erp.entity.ErpPresaleOrderItemEntity;
import io.renren.modules.erp.entity.ErpProductEntity;
import io.renren.modules.erp.service.ErpPresaleOrderService;
import io.renren.modules.erp.service.ErpWecomService;
import io.renren.modules.erp.vo.ErpRecognizedPackingBatchVo;
import io.renren.modules.erp.vo.ErpRecognizedPackingDraftVo;
import io.renren.modules.erp.vo.ErpRecognizedPackingItemVo;
import io.renren.modules.erp.vo.ErpRecognizedOrderDraftVo;
import io.renren.modules.erp.vo.ErpRecognizedOrderItemVo;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service("erpPresaleOrderService")
public class ErpPresaleOrderServiceImpl extends ServiceImpl<ErpPresaleOrderDao, ErpPresaleOrderEntity>
    implements ErpPresaleOrderService {
  @Autowired
  private ErpPresaleOrderItemDao erpPresaleOrderItemDao;
  @Autowired
  private ErpPresaleAttachmentDao erpPresaleAttachmentDao;
  @Autowired
  private ErpPresaleConfirmDao erpPresaleConfirmDao;
  @Autowired
  private ErpPresaleConfirmItemDao erpPresaleConfirmItemDao;
  @Autowired
  private ErpPresalePackingDao erpPresalePackingDao;
  @Autowired
  private ErpPresalePackingItemDao erpPresalePackingItemDao;
  @Autowired
  private ErpPresalePackingBatchDao erpPresalePackingBatchDao;
  @Autowired
  private ErpProductDao erpProductDao;
  @Autowired
  private ErpPartnerDao erpPartnerDao;
  @Autowired
  private ErpWecomService erpWecomService;

  @Override
  public PageUtils queryPage(Map<String, Object> params) {
    String keyword = params.get("keyword") == null ? null : params.get("keyword").toString();
    QueryWrapper<ErpPresaleOrderEntity> wrapper = new QueryWrapper<ErpPresaleOrderEntity>()
        .orderByDesc("order_date", "id");
    if (StringUtils.isNotBlank(keyword)) {
      wrapper.and(q -> q.like("order_no", keyword)
          .or().like("seller_contract_no", keyword)
          .or().like("customer_reference", keyword)
          .or().like("brand_name", keyword));
    }
    IPage<ErpPresaleOrderEntity> page = this.page(new Query<ErpPresaleOrderEntity>().getPage(params), wrapper);
    for (ErpPresaleOrderEntity entity : page.getRecords()) {
      Integer confirmCount = erpPresaleConfirmDao.selectCount(new QueryWrapper<ErpPresaleConfirmEntity>().eq("presale_order_id", entity.getId()));
      entity.setConfirmCount(confirmCount == null ? 0 : confirmCount);
      entity.setConfirmUploaded(entity.getConfirmCount() > 0 ? 1 : 0);
      entity.setPackingUploaded(hasPacking(entity.getId()) ? 1 : 0);
      entity.setCustomsUploaded(hasAttachment(entity.getId(), "CUSTOMS") ? 1 : 0);
      entity.setQuarantineUploaded(hasAttachment(entity.getId(), "QUARANTINE") ? 1 : 0);
    }
    return new PageUtils(page);
  }

  @Override
  public PageUtils queryConfirmPage(Map<String, Object> params) {
    String keyword = params.get("keyword") == null ? null : params.get("keyword").toString();
    QueryWrapper<ErpPresaleConfirmEntity> wrapper = new QueryWrapper<ErpPresaleConfirmEntity>()
        .orderByDesc("expected_arrival_date", "id");
    List<Long> matchedPresaleIds = findPresaleIdsByKeyword(keyword);
    if (StringUtils.isNotBlank(keyword)) {
      wrapper.and(q -> {
        q.like("contract_no", keyword)
            .or().like("container_no", keyword)
            .or().like("brand_name", keyword)
            .or().like("buyer_partner_name", keyword);
        if (!matchedPresaleIds.isEmpty()) {
          q.or().in("presale_order_id", matchedPresaleIds);
        }
      });
    }
    IPage<ErpPresaleConfirmEntity> page = erpPresaleConfirmDao.selectPage(
        new Query<ErpPresaleConfirmEntity>().getPage(params), wrapper);
    for (ErpPresaleConfirmEntity confirm : page.getRecords()) {
      fillConfirmPageFields(confirm);
    }
    return new PageUtils(page);
  }

  @Override
  public ErpPresaleOrderEntity getDetail(Long id) {
    return getDetail(id, null);
  }

  @Override
  public ErpPresaleOrderEntity getDetail(Long id, Long confirmId) {
    ErpPresaleOrderEntity entity = this.getById(id);
    if (entity == null) {
      return null;
    }
    entity.setItemList(erpPresaleOrderItemDao.selectList(
        new QueryWrapper<ErpPresaleOrderItemEntity>().eq("presale_order_id", id).orderByAsc("line_no", "id")));
    List<ErpPresaleConfirmEntity> confirmList = loadConfirmList(id);
    ErpPresaleConfirmEntity confirm = resolveActiveConfirm(confirmList, confirmId);
    ErpPresalePackingEntity packing = loadPackingForConfirm(id, confirm == null ? null : confirm.getId());
    entity.setConfirmInfo(confirm);
    entity.setConfirmList(confirmList);
    entity.setConfirmCount(confirmList.size());
    entity.setPackingInfo(packing);
    Long activeConfirmId = confirm == null ? null : confirm.getId();
    List<ErpPresaleAttachmentEntity> quarantineList = listAttachments(id, activeConfirmId, "QUARANTINE");
    entity.setCustomsInfo(findAttachment(id, activeConfirmId, "CUSTOMS"));
    entity.setQuarantineInfo(quarantineList.isEmpty() ? null : quarantineList.get(0));
    entity.setQuarantineList(quarantineList);
    entity.setConfirmUploaded(confirmList.isEmpty() ? 0 : 1);
    entity.setPackingUploaded(packing == null ? 0 : 1);
    entity.setCustomsUploaded(entity.getCustomsInfo() == null ? 0 : 1);
    entity.setQuarantineUploaded(quarantineList.isEmpty() ? 0 : 1);
    return entity;
  }

  private List<Long> findPresaleIdsByKeyword(String keyword) {
    if (StringUtils.isBlank(keyword)) {
      return Collections.emptyList();
    }
    List<ErpPresaleOrderEntity> orders = this.list(new QueryWrapper<ErpPresaleOrderEntity>()
        .select("id")
        .and(q -> q.like("order_no", keyword)
            .or().like("seller_contract_no", keyword)
            .or().like("customer_reference", keyword)
            .or().like("brand_name", keyword))
        .last("limit 200"));
    if (orders == null || orders.isEmpty()) {
      return Collections.emptyList();
    }
    List<Long> ids = new ArrayList<>();
    for (ErpPresaleOrderEntity order : orders) {
      if (order.getId() != null) {
        ids.add(order.getId());
      }
    }
    return ids;
  }

  private void fillConfirmPageFields(ErpPresaleConfirmEntity confirm) {
    if (confirm == null) {
      return;
    }
    ErpPresaleOrderEntity order = confirm.getPresaleOrderId() == null ? null : this.getById(confirm.getPresaleOrderId());
    if (order != null) {
      confirm.setPresaleOrderNo(order.getOrderNo());
      confirm.setSellerContractNo(order.getSellerContractNo());
      confirm.setCustomerReference(order.getCustomerReference());
      confirm.setPresaleOrderDate(order.getOrderDate());
      confirm.setPresaleExpectedDate(order.getExpectedDate());
    }
    confirm.setPackingUploaded(hasPacking(confirm.getPresaleOrderId(), confirm.getId()) ? 1 : 0);
    confirm.setCustomsUploaded(hasAttachment(confirm.getPresaleOrderId(), confirm.getId(), "CUSTOMS") ? 1 : 0);
    confirm.setQuarantineUploaded(hasAttachment(confirm.getPresaleOrderId(), confirm.getId(), "QUARANTINE") ? 1 : 0);
  }

  private ErpPresaleConfirmEntity resolveActiveConfirm(List<ErpPresaleConfirmEntity> confirmList, Long confirmId) {
    if (confirmList == null || confirmList.isEmpty()) {
      return null;
    }
    if (confirmId != null) {
      for (ErpPresaleConfirmEntity confirm : confirmList) {
        if (confirm.getId() != null && String.valueOf(confirm.getId()).equals(String.valueOf(confirmId))) {
          return confirm;
        }
      }
    }
    return confirmList.get(0);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void saveOrder(ErpPresaleOrderEntity order, Long userId) {
    Date now = new Date();
    normalizeOrder(order, userId, now, true);
    this.save(order);
    saveItems(order, now);
    saveConfirm(order, userId, now);
    autoSendShipNotice(order.getId(), userId);
    savePacking(order, userId, now);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void updateOrder(ErpPresaleOrderEntity order, Long userId) {
    Date now = new Date();
    normalizeOrder(order, userId, now, false);
    this.updateById(order);
    erpPresaleOrderItemDao.delete(new QueryWrapper<ErpPresaleOrderItemEntity>().eq("presale_order_id", order.getId()));
    saveItems(order, now);
    replaceConfirm(order, userId, now);
    autoSendShipNotice(order.getId(), userId);
    replacePacking(order, userId, now);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void deleteOrders(Long[] ids) {
    for (Long id : ids) {
      List<ErpPresaleConfirmEntity> confirmList = erpPresaleConfirmDao.selectList(
          new QueryWrapper<ErpPresaleConfirmEntity>().eq("presale_order_id", id));
      for (ErpPresaleConfirmEntity confirm : confirmList) {
        erpPresaleConfirmItemDao.delete(new QueryWrapper<ErpPresaleConfirmItemEntity>().eq("confirm_id", confirm.getId()));
        erpPresaleConfirmDao.deleteById(confirm.getId());
      }
      List<ErpPresalePackingEntity> packingList = erpPresalePackingDao.selectList(
          new QueryWrapper<ErpPresalePackingEntity>().eq("presale_order_id", id));
      for (ErpPresalePackingEntity packing : packingList) {
        List<ErpPresalePackingItemEntity> packingItems = erpPresalePackingItemDao.selectList(
            new QueryWrapper<ErpPresalePackingItemEntity>().eq("packing_id", packing.getId()));
        for (ErpPresalePackingItemEntity packingItem : packingItems) {
          erpPresalePackingBatchDao.delete(new QueryWrapper<ErpPresalePackingBatchEntity>().eq("packing_item_id", packingItem.getId()));
        }
        erpPresalePackingItemDao.delete(new QueryWrapper<ErpPresalePackingItemEntity>().eq("packing_id", packing.getId()));
        erpPresalePackingDao.deleteById(packing.getId());
      }
      erpPresaleAttachmentDao.delete(new QueryWrapper<ErpPresaleAttachmentEntity>().eq("presale_order_id", id));
      erpPresaleOrderItemDao.delete(new QueryWrapper<ErpPresaleOrderItemEntity>().eq("presale_order_id", id));
      this.removeById(id);
    }
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void syncConfirmProductMaster(ErpRecognizedOrderDraftVo draft) {
    if (draft == null || draft.getItemList() == null) {
      return;
    }
    Date now = new Date();
    for (ErpRecognizedOrderItemVo item : draft.getItemList()) {
      if (item == null) {
        continue;
      }
      ensureConfirmProduct(item, draft.getBrandName(), now);
    }
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void syncPackingProductMaster(ErpRecognizedPackingDraftVo draft) {
    if (draft == null || draft.getItemList() == null) {
      return;
    }
    for (ErpRecognizedPackingItemVo item : draft.getItemList()) {
      if (item == null) {
        continue;
      }
      ErpProductEntity product = resolveProductByEnglishName(item.getProductNameEn());
      if (product == null) {
        throw new RuntimeException("装箱单产品未匹配主数据: " + StringUtils.defaultIfBlank(item.getProductNameEn(), "-"));
      }
      boolean needUpdate = false;
      String recognizedName = StringUtils.trimToEmpty(item.getProductName());
      String recognizedNameEn = sanitizeProductNameEn(item.getProductNameEn());
      if (StringUtils.isBlank(product.getProductName()) && StringUtils.isNotBlank(recognizedName)) {
        product.setProductName(recognizedName);
        needUpdate = true;
      }
      if (StringUtils.isNotBlank(recognizedNameEn) && !StringUtils.equals(StringUtils.trimToEmpty(product.getProductNameEn()), recognizedNameEn)) {
        product.setProductNameEn(recognizedNameEn);
        needUpdate = true;
      }
      if (needUpdate) {
        product.setUpdateTime(new Date());
        erpProductDao.updateById(product);
      }
    }
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public ErpPresaleAttachmentEntity uploadAttachment(Long presaleOrderId, Long confirmId, String attachmentType, MultipartFile file, Long userId) throws Exception {
    ErpPresaleOrderEntity order = this.getById(presaleOrderId);
    if (order == null) {
      throw new RuntimeException("预销售单不存在，无法上传附件");
    }
    if (confirmId != null) {
      ErpPresaleConfirmEntity confirm = erpPresaleConfirmDao.selectById(confirmId);
      if (confirm == null || !String.valueOf(presaleOrderId).equals(String.valueOf(confirm.getPresaleOrderId()))) {
        throw new RuntimeException("客户订单确认函不存在，无法上传附件");
      }
    }
    String normalizedType = StringUtils.upperCase(StringUtils.trimToEmpty(attachmentType));
    if (!"CUSTOMS".equals(normalizedType) && !"QUARANTINE".equals(normalizedType)) {
      throw new RuntimeException("附件类型不支持");
    }
    String suffix = getSuffix(file.getOriginalFilename());
    Path tempFile = Files.createTempFile("erp-presale-attachment-", suffix);
    try {
      file.transferTo(tempFile.toFile());
      Path savedPath = saveAttachmentFile(file, tempFile, suffix, normalizedType);
      ErpPresaleAttachmentEntity existing = "QUARANTINE".equals(normalizedType) ? null : findAttachment(presaleOrderId, confirmId, normalizedType);
      Date now = new Date();
      if (existing == null) {
        existing = new ErpPresaleAttachmentEntity();
        existing.setPresaleOrderId(presaleOrderId);
        existing.setConfirmId(confirmId);
        existing.setAttachmentType(normalizedType);
        existing.setCreateUserId(userId);
        existing.setCreateTime(now);
      }
      existing.setConfirmId(confirmId);
      existing.setFilePath(savedPath.toAbsolutePath().toString());
      existing.setFileName(extractFileName(savedPath.toString()));
      existing.setUpdateTime(now);
      if (existing.getId() == null) {
        erpPresaleAttachmentDao.insert(existing);
      } else {
        erpPresaleAttachmentDao.updateById(existing);
      }
      return existing;
    } finally {
      Files.deleteIfExists(tempFile);
    }
  }

  @Override
  public ResponseEntity<byte[]> downloadEstimateFile(Long id) {
    ErpPresaleOrderEntity order = this.getById(id);
    return downloadFile(order == null ? null : order.getEstimateFilePath(), order == null ? null : order.getEstimateFileName());
  }

  @Override
  public ResponseEntity<byte[]> downloadConfirmFile(Long id) {
    ErpPresaleConfirmEntity confirm = erpPresaleConfirmDao.selectOne(
        new QueryWrapper<ErpPresaleConfirmEntity>().eq("presale_order_id", id).last("limit 1"));
    return downloadFile(confirm == null ? null : confirm.getFilePath(), confirm == null ? null : confirm.getFileName());
  }

  @Override
  public ResponseEntity<byte[]> downloadConfirmFileByConfirmId(Long confirmId) {
    ErpPresaleConfirmEntity confirm = erpPresaleConfirmDao.selectById(confirmId);
    return downloadFile(confirm == null ? null : confirm.getFilePath(), confirm == null ? null : confirm.getFileName());
  }

  @Override
  public ResponseEntity<byte[]> downloadPackingFile(Long id) {
    ErpPresalePackingEntity packing = erpPresalePackingDao.selectOne(
        new QueryWrapper<ErpPresalePackingEntity>().eq("presale_order_id", id).last("limit 1"));
    return downloadFile(packing == null ? null : packing.getFilePath(), packing == null ? null : packing.getFileName());
  }

  @Override
  public ResponseEntity<byte[]> downloadPackingFileByConfirmId(Long confirmId) {
    ErpPresalePackingEntity packing = erpPresalePackingDao.selectOne(
        new QueryWrapper<ErpPresalePackingEntity>().eq("confirm_id", confirmId).last("limit 1"));
    return downloadFile(packing == null ? null : packing.getFilePath(), packing == null ? null : packing.getFileName());
  }

  @Override
  public ResponseEntity<byte[]> downloadAttachmentFile(Long id, String attachmentType) {
    ErpPresaleAttachmentEntity attachment = findAttachment(id, attachmentType);
    return downloadFile(attachment == null ? null : attachment.getFilePath(), attachment == null ? null : attachment.getFileName());
  }

  @Override
  public ResponseEntity<byte[]> downloadAttachmentFileByConfirmId(Long confirmId, String attachmentType) {
    ErpPresaleConfirmEntity confirm = erpPresaleConfirmDao.selectById(confirmId);
    Long presaleOrderId = confirm == null ? null : confirm.getPresaleOrderId();
    ErpPresaleAttachmentEntity attachment = findAttachment(presaleOrderId, confirmId, attachmentType);
    return downloadFile(attachment == null ? null : attachment.getFilePath(), attachment == null ? null : attachment.getFileName());
  }

  @Override
  public ResponseEntity<byte[]> downloadAttachmentFileById(Long attachmentId) {
    ErpPresaleAttachmentEntity attachment = erpPresaleAttachmentDao.selectById(attachmentId);
    return downloadFile(attachment == null ? null : attachment.getFilePath(), attachment == null ? null : attachment.getFileName());
  }

  private void normalizeOrder(ErpPresaleOrderEntity order, Long userId, Date now, boolean create) {
    validateEstimateOrderRequired(order);
    if (StringUtils.isBlank(order.getOrderNo())) {
      order.setOrderNo("PS" + new SimpleDateFormat("yyyyMMddHHmmss").format(now));
    }
    if (order.getStatus() == null) {
      order.setStatus(0);
    }
    if (StringUtils.isBlank(order.getCurrency())) {
      order.setCurrency("CNY");
    }
    if (order.getOrderDate() == null) {
      order.setOrderDate(now);
    }
    fillCustomerPartner(order);
    fillBrand(order);
    if (StringUtils.isBlank(order.getEstimateFileName()) && StringUtils.isNotBlank(order.getEstimateFilePath())) {
      order.setEstimateFileName(new File(order.getEstimateFilePath()).getName());
    }
    if (create) {
      order.setCreateUserId(userId);
      order.setCreateTime(now);
    }
    order.setUpdateTime(now);
  }

  private List<ErpPresaleConfirmEntity> loadConfirmList(Long presaleOrderId) {
    List<ErpPresaleConfirmEntity> confirmList = erpPresaleConfirmDao.selectList(
        new QueryWrapper<ErpPresaleConfirmEntity>()
            .eq("presale_order_id", presaleOrderId)
            .orderByDesc("id"));
    for (ErpPresaleConfirmEntity confirm : confirmList) {
      List<ErpPresaleConfirmItemEntity> confirmItems = erpPresaleConfirmItemDao.selectList(
          new QueryWrapper<ErpPresaleConfirmItemEntity>().eq("confirm_id", confirm.getId()).orderByAsc("line_no", "id"));
      for (ErpPresaleConfirmItemEntity confirmItem : confirmItems) {
        fillConfirmItemMarketCirculationName(confirmItem);
      }
      confirm.setItemList(confirmItems);
    }
    return confirmList;
  }

  private ErpPresalePackingEntity loadPackingForConfirm(Long presaleOrderId, Long confirmId) {
    QueryWrapper<ErpPresalePackingEntity> wrapper = new QueryWrapper<ErpPresalePackingEntity>()
        .eq("presale_order_id", presaleOrderId)
        .orderByDesc("id")
        .last("limit 1");
    if (confirmId != null) {
      wrapper.eq("confirm_id", confirmId);
    }
    ErpPresalePackingEntity packing = erpPresalePackingDao.selectOne(wrapper);
    if (packing != null) {
      List<ErpPresalePackingItemEntity> packingItems = erpPresalePackingItemDao.selectList(
          new QueryWrapper<ErpPresalePackingItemEntity>().eq("packing_id", packing.getId()).orderByAsc("line_no", "id"));
      for (ErpPresalePackingItemEntity packingItem : packingItems) {
        packingItem.setBatchList(erpPresalePackingBatchDao.selectList(
            new QueryWrapper<ErpPresalePackingBatchEntity>().eq("packing_item_id", packingItem.getId()).orderByAsc("line_no", "id")));
      }
      packing.setItemList(packingItems);
    }
    return packing;
  }

  private void validateEstimateOrderRequired(ErpPresaleOrderEntity order) {
    if (order.getBrandId() == null) {
      throw new RuntimeException("请选择预售销售单品牌方");
    }
    if (StringUtils.isBlank(order.getSellerContractNo())) {
      throw new RuntimeException("预售销售单合同号不能为空");
    }
    if (order.getCustomerPartnerId() == null) {
      throw new RuntimeException("请选择预售销售单采购方");
    }
    if (order.getOrderDate() == null) {
      throw new RuntimeException("预售销售单下单日期不能为空");
    }
    List<ErpPresaleOrderItemEntity> items = order.getItemList() == null ? new ArrayList<ErpPresaleOrderItemEntity>() : order.getItemList();
    if (items.isEmpty()) {
      throw new RuntimeException("预售销售单明细至少需要一行");
    }
    int lineNo = 1;
    for (ErpPresaleOrderItemEntity item : items) {
      if (item == null || StringUtils.isBlank(firstNonBlank(item.getProductCode(), item.getSourceProductCode()))) {
        throw new RuntimeException("预售销售单第" + lineNo + "行产品代码不能为空");
      }
      lineNo++;
    }
  }

  private void saveItems(ErpPresaleOrderEntity order, Date now) {
    List<ErpPresaleOrderItemEntity> items = order.getItemList() == null ? new ArrayList<ErpPresaleOrderItemEntity>() : order.getItemList();
    int lineNo = 1;
    for (ErpPresaleOrderItemEntity item : items) {
      if (StringUtils.isBlank(item.getSourceProductCode()) && StringUtils.isBlank(item.getProductCode())) {
        continue;
      }
      enrichPresaleItem(item);
      item.setPresaleOrderId(order.getId());
      item.setLineNo(lineNo++);
      item.setCreateTime(now);
      item.setUpdateTime(now);
      erpPresaleOrderItemDao.insert(item);
    }
  }

  private void saveConfirm(ErpPresaleOrderEntity order, Long userId, Date now) {
    if (order.getConfirmInfo() == null) {
      return;
    }
    ErpPresaleConfirmEntity confirm = order.getConfirmInfo();
    normalizeConfirm(order.getId(), confirm, userId, now, true);
    erpPresaleConfirmDao.insert(confirm);
    saveConfirmItems(confirm, now);
  }

  private void savePacking(ErpPresaleOrderEntity order, Long userId, Date now) {
    if (order.getPackingInfo() == null) {
      return;
    }
    ErpPresalePackingEntity packing = order.getPackingInfo();
    normalizePacking(order.getId(), packing, userId, now, true);
    erpPresalePackingDao.insert(packing);
    savePackingItems(packing, now);
  }

  private void replaceConfirm(ErpPresaleOrderEntity order, Long userId, Date now) {
    ErpPresaleConfirmEntity confirm = order.getConfirmInfo();
    if (confirm == null) {
      return;
    }
    if (confirm.getId() != null && confirm.getId() > 0) {
      normalizeConfirm(order.getId(), confirm, userId, now, false);
      erpPresaleConfirmDao.updateById(confirm);
      erpPresaleConfirmItemDao.delete(new QueryWrapper<ErpPresaleConfirmItemEntity>().eq("confirm_id", confirm.getId()));
      saveConfirmItems(confirm, now);
      return;
    }
    saveConfirm(order, userId, now);
  }

  private void autoSendShipNotice(Long presaleOrderId, Long userId) {
    try {
      erpWecomService.autoSendShipNoticeToLinkedFutures(presaleOrderId, userId);
    } catch (Exception ignored) {
      // 船期自动通知失败不影响预销售单保存，用户仍可在列表手动重发。
    }
  }

  private void replacePacking(ErpPresaleOrderEntity order, Long userId, Date now) {
    if (order.getPackingInfo() == null) {
      return;
    }
    normalizePacking(order.getId(), order.getPackingInfo(), userId, now, false);
    QueryWrapper<ErpPresalePackingEntity> existingWrapper = new QueryWrapper<ErpPresalePackingEntity>().eq("presale_order_id", order.getId());
    if (order.getPackingInfo().getConfirmId() != null) {
      existingWrapper.eq("confirm_id", order.getPackingInfo().getConfirmId());
    } else {
      existingWrapper.last("limit 1");
    }
    ErpPresalePackingEntity existing = erpPresalePackingDao.selectOne(existingWrapper);
    if (existing != null) {
      List<ErpPresalePackingItemEntity> packingItems = erpPresalePackingItemDao.selectList(
          new QueryWrapper<ErpPresalePackingItemEntity>().eq("packing_id", existing.getId()));
      for (ErpPresalePackingItemEntity packingItem : packingItems) {
        erpPresalePackingBatchDao.delete(new QueryWrapper<ErpPresalePackingBatchEntity>().eq("packing_item_id", packingItem.getId()));
      }
      erpPresalePackingItemDao.delete(new QueryWrapper<ErpPresalePackingItemEntity>().eq("packing_id", existing.getId()));
      erpPresalePackingDao.deleteById(existing.getId());
    }
    ErpPresalePackingEntity packing = order.getPackingInfo();
    packing.setId(null);
    normalizePacking(order.getId(), packing, userId, now, true);
    erpPresalePackingDao.insert(packing);
    savePackingItems(packing, now);
  }

  private void normalizeConfirm(Long presaleOrderId, ErpPresaleConfirmEntity confirm, Long userId, Date now, boolean create) {
    confirm.setPresaleOrderId(presaleOrderId);
    fillConfirmPartners(confirm);
    if (StringUtils.isBlank(confirm.getColdFreshType())) {
      throw new RuntimeException("请选择客户订单确认函冷冻/冷鲜");
    }
    if (StringUtils.isBlank(confirm.getCurrency())) {
      confirm.setCurrency("CNY");
    }
    if (StringUtils.isBlank(confirm.getFileName()) && StringUtils.isNotBlank(confirm.getFilePath())) {
      confirm.setFileName(new File(confirm.getFilePath()).getName());
    }
    if (confirm.getTotalAmount() == null) {
      BigDecimal total = BigDecimal.ZERO;
      if (confirm.getItemList() != null) {
        for (ErpPresaleConfirmItemEntity item : confirm.getItemList()) {
          total = total.add(nvl(item.getLineTotalInclTax()));
        }
      }
      confirm.setTotalAmount(scale(total));
    }
    if (create) {
      confirm.setCreateUserId(userId);
      confirm.setCreateTime(now);
    }
    confirm.setUpdateTime(now);
  }

  private void saveConfirmItems(ErpPresaleConfirmEntity confirm, Date now) {
    List<ErpPresaleConfirmItemEntity> items = confirm.getItemList() == null ? new ArrayList<ErpPresaleConfirmItemEntity>() : confirm.getItemList();
    int lineNo = 1;
    for (ErpPresaleConfirmItemEntity item : items) {
      if (StringUtils.isBlank(item.getSourceProductCode()) && StringUtils.isBlank(item.getProductCode())) {
        continue;
      }
      enrichConfirmItem(item, confirm.getBrandName(), now);
      item.setConfirmId(confirm.getId());
      item.setLineNo(lineNo++);
      item.setCreateTime(now);
      item.setUpdateTime(now);
      erpPresaleConfirmItemDao.insert(item);
    }
  }

  private void normalizePacking(Long presaleOrderId, ErpPresalePackingEntity packing, Long userId, Date now, boolean create) {
    packing.setPresaleOrderId(presaleOrderId);
    ErpPresaleConfirmEntity confirm = resolveConfirmForPacking(presaleOrderId, packing);
    if (confirm != null) {
      packing.setConfirmId(confirm.getId());
      packing.setConfirmContractNo(confirm.getContractNo());
      if (StringUtils.isBlank(packing.getContractNo())) {
        packing.setContractNo(confirm.getContractNo());
      }
      if (StringUtils.isBlank(packing.getContainerNo())) {
        packing.setContainerNo(confirm.getContainerNo());
      }
    }
    if (StringUtils.isBlank(packing.getFileName()) && StringUtils.isNotBlank(packing.getFilePath())) {
      packing.setFileName(new File(packing.getFilePath()).getName());
    }
    if (packing.getTotalBoxes() == null) {
      int totalBoxes = 0;
      if (packing.getItemList() != null) {
        for (ErpPresalePackingItemEntity item : packing.getItemList()) {
          totalBoxes += item.getTotalBoxes() == null ? 0 : item.getTotalBoxes();
        }
      }
      packing.setTotalBoxes(totalBoxes);
    }
    if (packing.getTotalWeight() == null) {
      BigDecimal totalWeight = BigDecimal.ZERO;
      if (packing.getItemList() != null) {
        for (ErpPresalePackingItemEntity item : packing.getItemList()) {
          totalWeight = totalWeight.add(nvl(item.getTotalWeight()));
        }
      }
      packing.setTotalWeight(scale(totalWeight));
    } else {
      packing.setTotalWeight(scale(nvl(packing.getTotalWeight())));
    }
    if (create) {
      packing.setCreateUserId(userId);
      packing.setCreateTime(now);
    }
    packing.setUpdateTime(now);
  }

  private ErpPresaleConfirmEntity resolveConfirmForPacking(Long presaleOrderId, ErpPresalePackingEntity packing) {
    if (packing == null || presaleOrderId == null) {
      return null;
    }
    if (packing.getConfirmId() != null) {
      ErpPresaleConfirmEntity confirm = erpPresaleConfirmDao.selectById(packing.getConfirmId());
      if (confirm != null) {
        return confirm;
      }
    }
    QueryWrapper<ErpPresaleConfirmEntity> wrapper = new QueryWrapper<ErpPresaleConfirmEntity>()
        .eq("presale_order_id", presaleOrderId)
        .orderByDesc("id")
        .last("limit 1");
    String contractNo = StringUtils.trimToEmpty(packing.getContractNo());
    String containerNo = StringUtils.trimToEmpty(packing.getContainerNo());
    if (StringUtils.isNotBlank(contractNo) || StringUtils.isNotBlank(containerNo)) {
      wrapper.and(q -> {
        boolean hasPrev = false;
        if (StringUtils.isNotBlank(contractNo)) {
          q.eq("contract_no", contractNo);
          hasPrev = true;
        }
        if (StringUtils.isNotBlank(containerNo)) {
          if (hasPrev) {
            q.or();
          }
          q.eq("container_no", containerNo);
        }
      });
    }
    return erpPresaleConfirmDao.selectOne(wrapper);
  }

  private void savePackingItems(ErpPresalePackingEntity packing, Date now) {
    List<ErpPresalePackingItemEntity> items = packing.getItemList() == null ? new ArrayList<ErpPresalePackingItemEntity>() : packing.getItemList();
    int lineNo = 1;
    for (ErpPresalePackingItemEntity item : items) {
      if (StringUtils.isBlank(item.getProductNameEn())) {
        continue;
      }
      enrichPackingItem(item);
      item.setPackingId(packing.getId());
      item.setLineNo(lineNo++);
      item.setCreateTime(now);
      item.setUpdateTime(now);
      erpPresalePackingItemDao.insert(item);
      savePackingBatches(item, now);
    }
  }

  private void savePackingBatches(ErpPresalePackingItemEntity item, Date now) {
    List<ErpPresalePackingBatchEntity> batchList = item.getBatchList() == null ? new ArrayList<ErpPresalePackingBatchEntity>() : item.getBatchList();
    int lineNo = 1;
    for (ErpPresalePackingBatchEntity batch : batchList) {
      batch.setPackingItemId(item.getId());
      batch.setLineNo(lineNo++);
      batch.setBoxCount(batch.getBoxCount() == null ? 0 : batch.getBoxCount());
      batch.setWeight(scale(nvl(batch.getWeight())));
      batch.setCreateTime(now);
      batch.setUpdateTime(now);
      erpPresalePackingBatchDao.insert(batch);
    }
  }

  private void enrichPresaleItem(ErpPresaleOrderItemEntity item) {
    if (item.getQuantityKg() == null && item.getQuantityTon() != null) {
      item.setQuantityKg(scale(item.getQuantityTon().multiply(new BigDecimal("1000"))));
    }
    if (StringUtils.isBlank(item.getPriceCurrency())) {
      item.setPriceCurrency("CNY");
    }
    if (StringUtils.isBlank(item.getPriceUnit())) {
      item.setPriceUnit("KG");
    }
  }

  private void enrichConfirmItem(ErpPresaleConfirmItemEntity item, String brandName, Date now) {
    ErpProductEntity product = resolveProduct(firstNonBlank(item.getProductCode(), item.getSourceProductCode()));
    if (product != null) {
      item.setProductId(product.getId());
      item.setProductCode(product.getProductCode());
      item.setProductName(firstNonBlank(product.getProductName(), item.getProductName()));
      item.setProductNameEn(firstNonBlank(product.getProductNameEn(), item.getProductNameEn()));
      item.setMarketCirculationName(product.getMarketCirculationName());
    }
    if (item.getTaxRate() == null) {
      item.setTaxRate(new BigDecimal("9.00"));
    }
    item.setQuantity(scale(nvl(item.getQuantity())));
    item.setUnitPriceInclTax(scale4(nvl(item.getUnitPriceInclTax())));
    if (item.getLineTotalInclTax() == null) {
      item.setLineTotalInclTax(scale(nvl(item.getQuantity()).multiply(nvl(item.getUnitPriceInclTax()))));
    } else {
      item.setLineTotalInclTax(scale(nvl(item.getLineTotalInclTax())));
    }
  }

  private void fillConfirmItemMarketCirculationName(ErpPresaleConfirmItemEntity item) {
    ErpProductEntity product = item.getProductId() == null ? null : erpProductDao.selectById(item.getProductId());
    if (product == null) {
      product = resolveProduct(firstNonBlank(item.getProductCode(), item.getSourceProductCode()));
    }
    if (product != null) {
      item.setMarketCirculationName(product.getMarketCirculationName());
    }
  }

  private void enrichPackingItem(ErpPresalePackingItemEntity item) {
    ErpProductEntity product = resolveProduct(StringUtils.defaultIfBlank(item.getProductCode(), item.getSourceProductCode()));
    if (product == null) {
      product = resolveProductByEnglishName(item.getProductNameEn());
    }
    if (product == null) {
      throw new RuntimeException("装箱单存在未选择系统产品的明细，无法保存");
    }
    if (StringUtils.isBlank(item.getFactoryNo())) {
      throw new RuntimeException("装箱单产品厂号不能为空");
    }
    item.setFactoryNo(StringUtils.trimToEmpty(item.getFactoryNo()));
    item.setProductId(product.getId());
    item.setProductCode(product.getProductCode());
    if (StringUtils.isBlank(item.getSourceProductCode())) {
      item.setSourceProductCode(product.getProductCode());
    }
    item.setProductName(firstNonBlank(product.getProductName(), item.getProductName()));
    item.setProductNameEn(firstNonBlank(product.getProductNameEn(), item.getProductNameEn()));
    item.setTotalBoxes(item.getTotalBoxes() == null ? 0 : item.getTotalBoxes());
    item.setTotalWeight(scale(nvl(item.getTotalWeight())));
    item.setShelfLifeDays(item.getShelfLifeDays() == null ? 0 : item.getShelfLifeDays());
    if (item.getBatchList() != null) {
      for (ErpPresalePackingBatchEntity batch : item.getBatchList()) {
        if (batch.getExpiryDate() == null && batch.getProductionDate() != null && item.getShelfLifeDays() != null) {
          batch.setExpiryDate(addDays(batch.getProductionDate(), item.getShelfLifeDays()));
        }
      }
    }
  }

  private void syncProductNamesFromConfirmItem(ErpProductEntity product, ErpPresaleConfirmItemEntity item) {
    boolean needUpdate = false;
    String confirmProductName = StringUtils.trimToEmpty(item.getProductName());
    String confirmProductNameEn = StringUtils.trimToEmpty(item.getProductNameEn());
    if (StringUtils.isBlank(product.getProductName()) && StringUtils.isNotBlank(confirmProductName)) {
      product.setProductName(confirmProductName);
      needUpdate = true;
    }
    if (StringUtils.isNotBlank(confirmProductNameEn) && !StringUtils.equals(StringUtils.trimToEmpty(product.getProductNameEn()), confirmProductNameEn)) {
      product.setProductNameEn(confirmProductNameEn);
      needUpdate = true;
    }
    if (needUpdate) {
      product.setUpdateTime(new Date());
      erpProductDao.updateById(product);
    }
  }

  private ErpProductEntity resolveProduct(String code) {
    if (StringUtils.isBlank(code)) {
      return null;
    }
    ErpProductEntity byCode = erpProductDao.selectOne(new QueryWrapper<ErpProductEntity>().eq("product_code", code).last("limit 1"));
    if (byCode != null) {
      return byCode;
    }
    String normalizedCode = code.replaceAll("[A-Za-z]+$", "");
    if (StringUtils.isNotBlank(normalizedCode) && !StringUtils.equals(normalizedCode, code)) {
      ErpProductEntity normalized = erpProductDao.selectOne(
          new QueryWrapper<ErpProductEntity>().eq("product_code", normalizedCode).last("limit 1"));
      if (normalized != null) {
        return normalized;
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

  private ErpProductEntity resolveProductByEnglishName(String productNameEn) {
    if (StringUtils.isBlank(productNameEn)) {
      return null;
    }
    String normalizedName = normalizeEnglishName(productNameEn);
    List<ErpProductEntity> products = erpProductDao.selectList(new QueryWrapper<ErpProductEntity>().isNotNull("product_name_en"));
    for (ErpProductEntity product : products) {
      if (StringUtils.equals(normalizeEnglishName(product.getProductNameEn()), normalizedName)) {
        return product;
      }
    }
    return null;
  }

  private String normalizeEnglishName(String value) {
    if (StringUtils.isBlank(value)) {
      return "";
    }
    return sanitizeProductNameEn(value).replaceAll("[^A-Za-z0-9]+", " ").trim().replaceAll("\\s+", " ").toUpperCase();
  }

  private String sanitizeProductNameEn(String value) {
    String sanitized = StringUtils.trimToEmpty(value);
    sanitized = sanitized.replaceFirst("(?i)^SYB-", "");
    return sanitized.trim();
  }

  private void fillBrand(ErpPresaleOrderEntity order) {
    if (order.getBrandId() == null) {
      return;
    }
    ErpPartnerEntity brand = erpPartnerDao.selectById(order.getBrandId());
    if (brand != null) {
      order.setBrandName(brand.getPartnerName());
    }
  }

  private ErpProductEntity ensureConfirmProduct(ErpRecognizedOrderItemVo item, String brandName, Date now) {
    String sourceCode = StringUtils.trimToEmpty(firstNonBlank(item.getSourceProductCode(), item.getProductCode()));
    String productCode = normalizeMasterProductCode(firstNonBlank(item.getProductCode(), item.getSourceProductCode()));
    ErpPartnerEntity brandPartner = resolveBrandPartner(brandName);
    if (StringUtils.isBlank(productCode) || productCode.contains("/")) {
      return null;
    }
    ErpProductEntity product = resolveProduct(productCode);
    if (product == null && StringUtils.isNotBlank(sourceCode)) {
      product = resolveProduct(sourceCode);
    }
    if (product == null) {
      product = new ErpProductEntity();
      product.setProductCode(productCode);
      product.setAliasCodes(buildAliasCodes(productCode, sourceCode));
      product.setProductName(StringUtils.trimToEmpty(item.getProductName()));
      product.setProductNameEn(StringUtils.trimToEmpty(item.getProductNameEn()));
      product.setUnit(StringUtils.defaultIfBlank(item.getUnit(), "KG"));
      product.setBrandId(brandPartner == null ? null : brandPartner.getId());
      product.setBrand(brandPartner == null ? StringUtils.trimToEmpty(brandName) : brandPartner.getPartnerName());
      product.setStatus(1);
      product.setCreateTime(now);
      product.setUpdateTime(now);
      erpProductDao.insert(product);
      return product;
    }
    boolean needUpdate = false;
    String aliasCodes = buildAliasCodes(product.getAliasCodes(), productCode, sourceCode);
    if (StringUtils.isNotBlank(aliasCodes) && !StringUtils.equals(StringUtils.trimToEmpty(product.getAliasCodes()), aliasCodes)) {
      product.setAliasCodes(aliasCodes);
      needUpdate = true;
    }
    String recognizedName = StringUtils.trimToEmpty(item.getProductName());
    String recognizedNameEn = StringUtils.trimToEmpty(item.getProductNameEn());
    if (StringUtils.isBlank(product.getProductName()) && StringUtils.isNotBlank(recognizedName)) {
      product.setProductName(recognizedName);
      needUpdate = true;
    }
    if (StringUtils.isNotBlank(recognizedNameEn) && !StringUtils.equals(StringUtils.trimToEmpty(product.getProductNameEn()), recognizedNameEn)) {
      product.setProductNameEn(recognizedNameEn);
      needUpdate = true;
    }
    if (StringUtils.isBlank(product.getBrand()) && StringUtils.isNotBlank(brandName)) {
      product.setBrand(brandName);
      needUpdate = true;
    }
    if (product.getBrandId() == null && brandPartner != null) {
      product.setBrandId(brandPartner.getId());
      product.setBrand(brandPartner.getPartnerName());
      needUpdate = true;
    }
    if (needUpdate) {
      product.setUpdateTime(now);
      erpProductDao.updateById(product);
    }
    return product;
  }

  private ErpPartnerEntity resolveBrandPartner(String brandName) {
    String resolvedName = StringUtils.trimToEmpty(brandName);
    if (StringUtils.containsIgnoreCase(resolvedName, "Silver Fern Farms")) {
      resolvedName = "银之蕨食品（上海）有限公司";
    }
    if (StringUtils.isBlank(resolvedName)) {
      return null;
    }
    return erpPartnerDao.selectOne(new QueryWrapper<ErpPartnerEntity>()
        .eq("partner_name", resolvedName)
        .apply("FIND_IN_SET('BRAND', business_role) > 0")
        .last("limit 1"));
  }

  private String normalizeMasterProductCode(String code) {
    if (StringUtils.isBlank(code)) {
      return "";
    }
    return StringUtils.trimToEmpty(code).replaceAll("[A-Za-z]+$", "");
  }

  private String buildAliasCodes(String... codes) {
    List<String> aliases = new ArrayList<String>();
    for (String code : codes) {
      if (StringUtils.isBlank(code)) {
        continue;
      }
      String[] parts = code.split(",");
      for (String part : parts) {
        String alias = StringUtils.trimToEmpty(part);
        if (StringUtils.isBlank(alias) || aliases.contains(alias)) {
          continue;
        }
        aliases.add(alias);
      }
    }
    return StringUtils.join(aliases, ",");
  }

  private void fillCustomerPartner(ErpPresaleOrderEntity order) {
    if (order.getCustomerPartnerId() == null) {
      return;
    }
    ErpPartnerEntity partner = erpPartnerDao.selectById(order.getCustomerPartnerId());
    if (partner != null) {
      order.setCustomerReference(partner.getPartnerName());
    }
  }

  private void fillConfirmPartners(ErpPresaleConfirmEntity confirm) {
    if (confirm.getBrandId() != null) {
      ErpPartnerEntity brand = erpPartnerDao.selectById(confirm.getBrandId());
      if (brand != null) {
        confirm.setBrandName(brand.getPartnerName());
      }
    }
    if (confirm.getBuyerPartnerId() != null) {
      ErpPartnerEntity buyer = erpPartnerDao.selectById(confirm.getBuyerPartnerId());
      if (buyer != null) {
        confirm.setBuyerPartnerName(buyer.getPartnerName());
        confirm.setBuyerPartnerRole(buyer.getBusinessRole());
      }
    }
  }

  private boolean hasConfirm(Long presaleOrderId) {
    return erpPresaleConfirmDao.selectCount(new QueryWrapper<ErpPresaleConfirmEntity>().eq("presale_order_id", presaleOrderId)) > 0;
  }

  private boolean hasPacking(Long presaleOrderId) {
    return erpPresalePackingDao.selectCount(new QueryWrapper<ErpPresalePackingEntity>().eq("presale_order_id", presaleOrderId)) > 0;
  }

  private boolean hasPacking(Long presaleOrderId, Long confirmId) {
    QueryWrapper<ErpPresalePackingEntity> wrapper = new QueryWrapper<ErpPresalePackingEntity>();
    if (confirmId != null) {
      wrapper.eq("confirm_id", confirmId);
    } else {
      wrapper.eq("presale_order_id", presaleOrderId);
    }
    return erpPresalePackingDao.selectCount(wrapper) > 0;
  }

  private boolean hasAttachment(Long presaleOrderId, String attachmentType) {
    return erpPresaleAttachmentDao.selectCount(new QueryWrapper<ErpPresaleAttachmentEntity>()
        .eq("presale_order_id", presaleOrderId)
        .eq("attachment_type", StringUtils.upperCase(StringUtils.trimToEmpty(attachmentType)))) > 0;
  }

  private boolean hasAttachment(Long presaleOrderId, Long confirmId, String attachmentType) {
    QueryWrapper<ErpPresaleAttachmentEntity> wrapper = new QueryWrapper<ErpPresaleAttachmentEntity>()
        .eq("attachment_type", StringUtils.upperCase(StringUtils.trimToEmpty(attachmentType)));
    if (confirmId != null) {
      wrapper.eq("confirm_id", confirmId);
    } else {
      wrapper.eq("presale_order_id", presaleOrderId);
    }
    return erpPresaleAttachmentDao.selectCount(wrapper) > 0;
  }

  private ErpPresaleAttachmentEntity findAttachment(Long presaleOrderId, String attachmentType) {
    return findAttachment(presaleOrderId, null, attachmentType);
  }

  private ErpPresaleAttachmentEntity findAttachment(Long presaleOrderId, Long confirmId, String attachmentType) {
    QueryWrapper<ErpPresaleAttachmentEntity> wrapper = new QueryWrapper<ErpPresaleAttachmentEntity>()
        .eq("attachment_type", StringUtils.upperCase(StringUtils.trimToEmpty(attachmentType)));
    if (confirmId != null) {
      wrapper.eq("confirm_id", confirmId);
    } else {
      wrapper.eq("presale_order_id", presaleOrderId);
    }
    return erpPresaleAttachmentDao.selectOne(wrapper
        .orderByDesc("id")
        .last("limit 1"));
  }

  private List<ErpPresaleAttachmentEntity> listAttachments(Long presaleOrderId, String attachmentType) {
    return listAttachments(presaleOrderId, null, attachmentType);
  }

  private List<ErpPresaleAttachmentEntity> listAttachments(Long presaleOrderId, Long confirmId, String attachmentType) {
    QueryWrapper<ErpPresaleAttachmentEntity> wrapper = new QueryWrapper<ErpPresaleAttachmentEntity>()
        .eq("attachment_type", StringUtils.upperCase(StringUtils.trimToEmpty(attachmentType)));
    if (confirmId != null) {
      wrapper.eq("confirm_id", confirmId);
    } else {
      wrapper.eq("presale_order_id", presaleOrderId);
    }
    return erpPresaleAttachmentDao.selectList(wrapper.orderByDesc("id"));
  }

  private String getSuffix(String filename) {
    if (StringUtils.isBlank(filename) || filename.lastIndexOf(".") < 0) {
      return ".tmp";
    }
    return filename.substring(filename.lastIndexOf("."));
  }

  private Path saveAttachmentFile(MultipartFile file, Path tempFile, String suffix, String attachmentType) throws Exception {
    String baseDir = "D:\\renren-fast-vue\\renren-fast\\uploads\\presale-attachments";
    String dayFolder = new SimpleDateFormat("yyyyMMdd").format(new Date());
    Path dir = Paths.get(baseDir, StringUtils.lowerCase(attachmentType), dayFolder);
    Files.createDirectories(dir);
    String originalName = StringUtils.defaultString(file.getOriginalFilename(), "presale-attachment")
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">");
    String safeName = originalName.replaceAll("[\\\\/:*?\"<>|\\s]+", "_");
    if (StringUtils.isBlank(safeName)) {
      safeName = "presale-attachment" + suffix;
    }
    if (!safeName.toLowerCase().endsWith(suffix.toLowerCase())) {
      safeName = safeName + suffix;
    }
    Path target = dir.resolve(System.currentTimeMillis() + "_" + safeName);
    Files.copy(tempFile, target, StandardCopyOption.REPLACE_EXISTING);
    return target;
  }

  private String extractFileName(String path) {
    if (StringUtils.isBlank(path)) {
      return "";
    }
    return new File(path).getName();
  }

  private ResponseEntity<byte[]> downloadFile(String filePath, String fileName) {
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

  private String firstNonBlank(String... values) {
    for (String value : values) {
      if (StringUtils.isNotBlank(value)) {
        return value;
      }
    }
    return null;
  }

  private BigDecimal nvl(BigDecimal value) {
    return value == null ? BigDecimal.ZERO : value;
  }

  private BigDecimal scale(BigDecimal value) {
    return nvl(value).setScale(2, RoundingMode.HALF_UP);
  }

  private BigDecimal scale4(BigDecimal value) {
    return nvl(value).setScale(4, RoundingMode.HALF_UP);
  }

  private Date addDays(Date date, int days) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.add(Calendar.DAY_OF_MONTH, days);
    return calendar.getTime();
  }
}
