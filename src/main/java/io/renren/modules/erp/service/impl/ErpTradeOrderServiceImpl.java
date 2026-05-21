package io.renren.modules.erp.service.impl;

import cn.afterturn.easypoi.excel.entity.params.ExcelExportEntity;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.renren.common.utils.ExcelUtils;
import io.renren.common.utils.PageUtils;
import io.renren.common.utils.Query;
import io.renren.modules.erp.dao.ErpPartnerDao;
import io.renren.modules.erp.dao.ErpProductDao;
import io.renren.modules.erp.dao.ErpStockLedgerDao;
import io.renren.modules.erp.dao.ErpTradeOrderDao;
import io.renren.modules.erp.dao.ErpTradeOrderExpenseDao;
import io.renren.modules.erp.dao.ErpTradeOrderItemDao;
import io.renren.modules.erp.dao.ErpWarehouseDao;
import io.renren.modules.erp.entity.ErpPartnerEntity;
import io.renren.modules.erp.entity.ErpProductEntity;
import io.renren.modules.erp.entity.ErpStockLedgerEntity;
import io.renren.modules.erp.entity.ErpTradeOrderEntity;
import io.renren.modules.erp.entity.ErpTradeOrderExpenseEntity;
import io.renren.modules.erp.entity.ErpTradeOrderItemEntity;
import io.renren.modules.erp.entity.ErpWarehouseEntity;
import io.renren.modules.erp.service.ErpTradeOrderService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("erpTradeOrderService")
public class ErpTradeOrderServiceImpl extends ServiceImpl<ErpTradeOrderDao, ErpTradeOrderEntity> implements ErpTradeOrderService {
  private static final String BIZ_PURCHASE_INBOUND = "PURCHASE_INBOUND";
  private static final String BIZ_PRESALE = "PRESALE";
  private static final String BIZ_NORMAL_OUTBOUND = "NORMAL_OUTBOUND";
  private static final String BIZ_LOSS_OUTBOUND = "LOSS_OUTBOUND";
  private static final String SOURCE_AUTO_OUTBOUND = "AUTO_OUTBOUND";
  private static final String SOURCE_MANUAL = "MANUAL";
  private static final ZoneId ZONE_ID = ZoneId.systemDefault();

  @Autowired
  private ErpTradeOrderItemDao erpTradeOrderItemDao;
  @Autowired
  private ErpTradeOrderExpenseDao erpTradeOrderExpenseDao;
  @Autowired
  private ErpStockLedgerDao erpStockLedgerDao;
  @Autowired
  private ErpPartnerDao erpPartnerDao;
  @Autowired
  private ErpProductDao erpProductDao;
  @Autowired
  private ErpWarehouseDao erpWarehouseDao;

  @Override
  public PageUtils queryPage(Map<String, Object> params, String orderType) {
    String keyword = stringValue(params.get("keyword"));
    String bizType = stringValue(params.get("bizType"));
    String paymentStatus = stringValue(params.get("paymentStatus"));
    String invoiceStatus = stringValue(params.get("invoiceStatus"));
    String status = stringValue(params.get("status"));
    QueryWrapper<ErpTradeOrderEntity> wrapper = new QueryWrapper<ErpTradeOrderEntity>()
        .eq("order_type", orderType)
        .orderByDesc("order_date", "id");
    if (StringUtils.isNotBlank(keyword)) {
      wrapper.and(w -> w.like("order_no", keyword)
          .or().like("partner_name", keyword)
          .or().like("secondary_partner_name", keyword)
          .or().like("contract_no", keyword)
          .or().like("related_contract_no", keyword)
          .or().like("container_no", keyword)
          .or().like("source_order_no", keyword));
    }
    if (StringUtils.isNotBlank(bizType)) {
      wrapper.eq("biz_type", bizType);
    }
    if (StringUtils.isNotBlank(paymentStatus)) {
      wrapper.eq("payment_status", Integer.valueOf(paymentStatus));
    }
    if (StringUtils.isNotBlank(invoiceStatus)) {
      wrapper.eq("invoice_status", Integer.valueOf(invoiceStatus));
    }
    if (StringUtils.isNotBlank(status)) {
      wrapper.eq("status", Integer.valueOf(status));
    }
    IPage<ErpTradeOrderEntity> page = this.page(new Query<ErpTradeOrderEntity>().getPage(params), wrapper);
    return new PageUtils(page);
  }

  @Override
  public ErpTradeOrderEntity getDetail(Long id) {
    ErpTradeOrderEntity entity = this.getById(id);
    if (entity == null) {
      return null;
    }
    entity.setItemList(erpTradeOrderItemDao.selectList(new QueryWrapper<ErpTradeOrderItemEntity>().eq("order_id", id).orderByAsc("line_no", "id")));
    entity.setExpenseList(erpTradeOrderExpenseDao.selectList(new QueryWrapper<ErpTradeOrderExpenseEntity>().eq("order_id", id).orderByAsc("id")));
    return entity;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void saveOrder(ErpTradeOrderEntity order, Long userId) {
    Date now = new Date();
    normalizeOrder(order, userId, now, true);
    this.save(order);
    saveChildren(order, userId, now);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void updateOrder(ErpTradeOrderEntity order, Long userId) {
    Date now = new Date();
    normalizeOrder(order, userId, now, false);
    this.updateById(order);
    removeChildren(order.getId());
    saveChildren(order, userId, now);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void deleteOrders(Long[] ids) {
    for (Long id : ids) {
      removeChildren(id);
    }
    this.removeByIds(java.util.Arrays.asList(ids));
  }

  @Override
  public ResponseEntity<byte[]> exportFinanceStatement(Map<String, Object> params, String orderType) {
    QueryWrapper<ErpTradeOrderEntity> wrapper = buildListWrapper(params, orderType);
    List<ErpTradeOrderEntity> orders = this.list(wrapper);
    List<Map<String, Object>> rows = new ArrayList<>();
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    for (ErpTradeOrderEntity order : orders) {
      Map<String, Object> row = new LinkedHashMap<>();
      row.put("bizDate", order.getOrderDate() == null ? "" : dateFormat.format(order.getOrderDate()));
      row.put("summary", buildSummary(order));
      row.put("counterparty", resolveCounterparty(order));
      row.put("income", "SALE".equals(order.getOrderType()) ? order.getTotalAmount() : BigDecimal.ZERO);
      row.put("expense", "PURCHASE".equals(order.getOrderType()) ? order.getTotalAmount() : BigDecimal.ZERO);
      row.put("paymentStatus", paymentStatusLabel(order.getPaymentStatus()));
      row.put("invoiceStatus", invoiceStatusLabel(order.getInvoiceStatus()));
      row.put("bizType", bizTypeLabel(order.getBizType()));
      row.put("contractNo", StringUtils.defaultString(order.getContractNo()));
      row.put("containerNo", StringUtils.defaultString(order.getContainerNo()));
      row.put("matchKey", buildMatchKey(order));
      rows.add(row);
    }
    List<ExcelExportEntity> columns = new ArrayList<>();
    columns.add(new ExcelExportEntity("日期", "bizDate"));
    columns.add(new ExcelExportEntity("摘要", "summary"));
    columns.add(new ExcelExportEntity("对方户名", "counterparty"));
    columns.add(new ExcelExportEntity("收入", "income"));
    columns.add(new ExcelExportEntity("支出", "expense"));
    columns.add(new ExcelExportEntity("付款状态", "paymentStatus"));
    columns.add(new ExcelExportEntity("开票状态", "invoiceStatus"));
    columns.add(new ExcelExportEntity("业务类型", "bizType"));
    columns.add(new ExcelExportEntity("合同号", "contractNo"));
    columns.add(new ExcelExportEntity("柜号", "containerNo"));
    columns.add(new ExcelExportEntity("匹配唯一号", "matchKey"));
    String prefix = "PURCHASE".equals(orderType) ? "采购" : "销售";
    return ExcelUtils.dynamicExportExcel(columns, prefix + "单好会计导出", "好会计导入", prefix + "单好会计导出.xls", rows);
  }

  private QueryWrapper<ErpTradeOrderEntity> buildListWrapper(Map<String, Object> params, String orderType) {
    String keyword = stringValue(params.get("keyword"));
    String bizType = stringValue(params.get("bizType"));
    String paymentStatus = stringValue(params.get("paymentStatus"));
    String invoiceStatus = stringValue(params.get("invoiceStatus"));
    String status = stringValue(params.get("status"));
    QueryWrapper<ErpTradeOrderEntity> wrapper = new QueryWrapper<ErpTradeOrderEntity>()
        .eq("order_type", orderType)
        .orderByDesc("order_date", "id");
    if (StringUtils.isNotBlank(keyword)) {
      wrapper.and(w -> w.like("order_no", keyword)
          .or().like("partner_name", keyword)
          .or().like("secondary_partner_name", keyword)
          .or().like("contract_no", keyword)
          .or().like("container_no", keyword)
          .or().like("source_order_no", keyword));
    }
    if (StringUtils.isNotBlank(bizType)) {
      wrapper.eq("biz_type", bizType);
    }
    if (StringUtils.isNotBlank(paymentStatus)) {
      wrapper.eq("payment_status", Integer.valueOf(paymentStatus));
    }
    if (StringUtils.isNotBlank(invoiceStatus)) {
      wrapper.eq("invoice_status", Integer.valueOf(invoiceStatus));
    }
    if (StringUtils.isNotBlank(status)) {
      wrapper.eq("status", Integer.valueOf(status));
    }
    return wrapper;
  }

  private void removeChildren(Long orderId) {
    erpTradeOrderItemDao.delete(new QueryWrapper<ErpTradeOrderItemEntity>().eq("order_id", orderId));
    erpTradeOrderExpenseDao.delete(new QueryWrapper<ErpTradeOrderExpenseEntity>().eq("order_id", orderId));
    erpStockLedgerDao.delete(new QueryWrapper<ErpStockLedgerEntity>().eq("order_id", orderId));
  }

  private void normalizeOrder(ErpTradeOrderEntity order, Long userId, Date now, boolean create) {
    if (StringUtils.isBlank(order.getOrderNo())) {
      order.setOrderNo(generateOrderNo(order.getOrderType(), order.getBizType()));
    }
    if (StringUtils.isBlank(order.getBizType())) {
      order.setBizType(defaultBizType(order.getOrderType()));
    }
    if (order.getCurrency() == null) {
      order.setCurrency("CNY");
    }
    if (order.getStatus() == null) {
      order.setStatus(0);
    }
    if (order.getOrderDate() == null) {
      order.setOrderDate(now);
    }
    if (order.getPaymentStatus() == null) {
      order.setPaymentStatus(0);
    }
    if (order.getInvoiceStatus() == null) {
      order.setInvoiceStatus(0);
    }
    if (order.getAutoOutbound() == null) {
      order.setAutoOutbound(BIZ_PRESALE.equals(order.getBizType()) ? 1 : 0);
    }
    if (order.getStorageFeeStartDays() == null) {
      order.setStorageFeeStartDays(0);
    }
    if (StringUtils.isBlank(order.getOrderSource())) {
      order.setOrderSource(SOURCE_MANUAL);
    }
    if (order.getStorageStartDate() == null) {
      order.setStorageStartDate(order.getOrderDate());
    }
    if ("SALE".equals(order.getOrderType())
        && (BIZ_NORMAL_OUTBOUND.equals(order.getBizType()) || BIZ_LOSS_OUTBOUND.equals(order.getBizType()))
        && order.getStatus() != null && order.getStatus() == 1
        && order.getActualOutDate() == null) {
      order.setActualOutDate(order.getOrderDate());
    }
    validateOrder(order);
    fillPartnerSnapshot(order);
    fillWarehouseSnapshot(order);

    List<ErpTradeOrderItemEntity> items = order.getItemList() == null ? new ArrayList<>() : order.getItemList();
    List<ErpTradeOrderExpenseEntity> expenses = order.getExpenseList() == null ? new ArrayList<>() : order.getExpenseList();
    normalizeWarehouseFee(order, items, expenses);

    BigDecimal itemAmount = BigDecimal.ZERO;
    BigDecimal expenseAmount = BigDecimal.ZERO;
    BigDecimal taxAmount = BigDecimal.ZERO;
    int lineNo = 1;
    for (ErpTradeOrderItemEntity item : items) {
      if (item.getQuantity() == null || item.getUnitPrice() == null) {
        continue;
      }
      fillProductSnapshot(item);
      if (StringUtils.isBlank(item.getWarehouseName())) {
        item.setWarehouseName(order.getWarehouseName());
      }
      if (StringUtils.isBlank(item.getSourceContainerNo())) {
        item.setSourceContainerNo(order.getContainerNo());
      }
      if (item.getActualPieceCount() == null) {
        item.setActualPieceCount(item.getPieceCount());
      }
      if (item.getEstimatedWeight() == null) {
        item.setEstimatedWeight(item.getQuantity());
      }
      item.setLineNo(lineNo++);
      item.setAmount(multiply(item.getQuantity(), item.getUnitPrice()));
      item.setTaxRate(nvl(item.getTaxRate()));
      item.setTaxAmount(percent(item.getAmount(), item.getTaxRate()));
      item.setTotalAmount(item.getAmount().add(item.getTaxAmount()));
      itemAmount = itemAmount.add(item.getAmount());
      taxAmount = taxAmount.add(item.getTaxAmount());
    }
    for (ErpTradeOrderExpenseEntity expense : expenses) {
      if (expense.getAmount() == null) {
        continue;
      }
      expense.setTaxRate(nvl(expense.getTaxRate()));
      expense.setTaxAmount(percent(expense.getAmount(), expense.getTaxRate()));
      expense.setTotalAmount(expense.getAmount().add(expense.getTaxAmount()));
      expenseAmount = expenseAmount.add(expense.getAmount());
      taxAmount = taxAmount.add(expense.getTaxAmount());
    }
    order.setItemAmount(scale(itemAmount));
    order.setExpenseAmount(scale(expenseAmount));
    order.setTaxAmount(scale(taxAmount));
    order.setTotalAmount(scale(itemAmount.add(expenseAmount).add(taxAmount)));
    if (create) {
      order.setCreateUserId(userId);
      order.setCreateTime(now);
    }
    order.setUpdateTime(now);
  }

  private void saveChildren(ErpTradeOrderEntity order, Long userId, Date now) {
    List<ErpTradeOrderItemEntity> savedItems = new ArrayList<>();
    if (order.getItemList() != null) {
      for (ErpTradeOrderItemEntity item : order.getItemList()) {
        if (item.getQuantity() == null || item.getUnitPrice() == null) {
          continue;
        }
        item.setOrderId(order.getId());
        item.setCreateTime(now);
        item.setUpdateTime(now);
        erpTradeOrderItemDao.insert(item);
        savedItems.add(item);
      }
    }
    if (order.getExpenseList() != null) {
      for (ErpTradeOrderExpenseEntity expense : order.getExpenseList()) {
        if (expense.getAmount() == null) {
          continue;
        }
        expense.setOrderId(order.getId());
        expense.setCreateTime(now);
        expense.setUpdateTime(now);
        erpTradeOrderExpenseDao.insert(expense);
      }
    }
    if (order.getStatus() != null && order.getStatus() == 1) {
      for (ErpTradeOrderItemEntity item : savedItems) {
        ErpStockLedgerEntity ledger = buildLedger(order, item, userId, now);
        erpStockLedgerDao.insert(ledger);
      }
      autoGenerateOutboundFromPresales(order, savedItems, userId, now);
    }
  }

  private ErpStockLedgerEntity buildLedger(ErpTradeOrderEntity order, ErpTradeOrderItemEntity item, Long userId, Date now) {
    ErpStockLedgerEntity ledger = new ErpStockLedgerEntity();
    ledger.setOrderId(order.getId());
    ledger.setOrderItemId(item.getId());
    ledger.setOrderType(order.getOrderType());
    ledger.setOrderNo(order.getOrderNo());
    ledger.setRelatedOrderNo(firstNonBlank(order.getRelatedContractNo(), order.getSourceOrderNo()));
    ledger.setBizType(order.getBizType());
    ledger.setProductId(item.getProductId());
    ledger.setProductCode(item.getProductCode());
    ledger.setProductName(item.getProductName());
    ledger.setProductSpec(item.getProductSpec());
    ledger.setWarehouseId(order.getWarehouseId());
    ledger.setWarehouseName(item.getWarehouseName());
    ledger.setInQuantity("PURCHASE".equals(order.getOrderType()) ? firstMeaningful(item.getActualInWeight(), item.getEstimatedWeight(), item.getQuantity()) : BigDecimal.ZERO);
    ledger.setOutQuantity("SALE".equals(order.getOrderType()) ? firstMeaningful(item.getActualOutWeight(), item.getEstimatedWeight(), item.getQuantity()) : BigDecimal.ZERO);
    ledger.setInPieces("PURCHASE".equals(order.getOrderType()) ? firstMeaningful(item.getActualPieceCount(), item.getPieceCount()) : BigDecimal.ZERO);
    ledger.setOutPieces("SALE".equals(order.getOrderType()) ? firstMeaningful(item.getActualPieceCount(), item.getPieceCount()) : BigDecimal.ZERO);
    ledger.setLossWeight(nvl(item.getLossWeight()));
    ledger.setUnitPrice(item.getUnitPrice());
    ledger.setBizDate(order.getOrderDate());
    ledger.setExpiryDate(item.getExpiryDate());
    ledger.setCreateUserId(userId);
    ledger.setCreateTime(now);
    return ledger;
  }

  private void autoGenerateOutboundFromPresales(ErpTradeOrderEntity purchaseOrder, List<ErpTradeOrderItemEntity> purchaseItems, Long userId, Date now) {
    if (!"PURCHASE".equals(purchaseOrder.getOrderType()) || purchaseOrder.getStatus() == null || purchaseOrder.getStatus() != 1) {
      return;
    }
    List<ErpTradeOrderEntity> presales = findMatchingPresales(purchaseOrder);
    if (presales.isEmpty()) {
      return;
    }
    Map<String, BigDecimal> remainingPieces = new LinkedHashMap<>();
    Map<String, BigDecimal> remainingWeights = new LinkedHashMap<>();
    for (ErpTradeOrderItemEntity purchaseItem : purchaseItems) {
      String key = productKey(purchaseItem);
      remainingPieces.put(key, remainingPieces.getOrDefault(key, BigDecimal.ZERO).add(firstMeaningful(purchaseItem.getActualPieceCount(), purchaseItem.getPieceCount())));
      remainingWeights.put(key, remainingWeights.getOrDefault(key, BigDecimal.ZERO).add(firstMeaningful(purchaseItem.getActualInWeight(), purchaseItem.getEstimatedWeight(), purchaseItem.getQuantity())));
    }
    for (ErpTradeOrderEntity presaleSummary : presales) {
      if (hasGeneratedOutbound(presaleSummary.getId(), purchaseOrder.getId())) {
        continue;
      }
      ErpTradeOrderEntity presale = getDetail(presaleSummary.getId());
      if (presale == null || presale.getItemList() == null || presale.getItemList().isEmpty()) {
        continue;
      }
      ErpTradeOrderEntity outbound = buildAutoOutbound(presale, purchaseOrder, remainingPieces, remainingWeights);
      if (outbound == null || outbound.getItemList() == null || outbound.getItemList().isEmpty()) {
        continue;
      }
      saveOrder(outbound, userId);
      repairAutoOutboundIfNeeded(outbound.getId(), presale, purchaseOrder, userId);
    }
  }

  private List<ErpTradeOrderEntity> findMatchingPresales(ErpTradeOrderEntity purchaseOrder) {
    QueryWrapper<ErpTradeOrderEntity> wrapper = new QueryWrapper<ErpTradeOrderEntity>()
        .eq("order_type", "SALE")
        .eq("biz_type", BIZ_PRESALE)
        .eq("auto_outbound", 1)
        .orderByAsc("order_date", "id");
    List<ErpTradeOrderEntity> candidates = this.list(wrapper);
    List<ErpTradeOrderEntity> matched = new ArrayList<>();
    for (ErpTradeOrderEntity candidate : candidates) {
      if (isPresaleMatch(purchaseOrder, candidate)) {
        matched.add(candidate);
      }
    }
    return matched;
  }

  private boolean isPresaleMatch(ErpTradeOrderEntity purchaseOrder, ErpTradeOrderEntity presale) {
    if (purchaseOrder.getBrandId() != null && presale.getBrandId() != null && !purchaseOrder.getBrandId().equals(presale.getBrandId())) {
      return false;
    }
    if (StringUtils.isNotBlank(purchaseOrder.getContainerNo()) && StringUtils.equalsIgnoreCase(StringUtils.trim(purchaseOrder.getContainerNo()), StringUtils.trim(presale.getContainerNo()))) {
      return true;
    }
    return overlaps(purchaseOrder.getContractNo(), presale.getRelatedContractNo())
        || overlaps(purchaseOrder.getContractNo(), presale.getContractNo())
        || overlaps(purchaseOrder.getRelatedContractNo(), presale.getRelatedContractNo())
        || overlaps(purchaseOrder.getOrderNo(), presale.getRelatedContractNo());
  }

  private boolean hasGeneratedOutbound(Long presaleId, Long purchaseId) {
    return this.count(new QueryWrapper<ErpTradeOrderEntity>()
        .eq("source_order_id", presaleId)
        .eq("related_contract_no", String.valueOf(purchaseId))
        .eq("order_source", SOURCE_AUTO_OUTBOUND)) > 0;
  }

  private ErpTradeOrderEntity buildAutoOutbound(ErpTradeOrderEntity presale, ErpTradeOrderEntity purchaseOrder, Map<String, BigDecimal> remainingPieces, Map<String, BigDecimal> remainingWeights) {
    List<ErpTradeOrderItemEntity> items = new ArrayList<>();
    for (ErpTradeOrderItemEntity presaleItem : presale.getItemList()) {
      String key = productKey(presaleItem);
      BigDecimal pieceDemand = firstMeaningful(presaleItem.getActualPieceCount(), presaleItem.getPieceCount());
      BigDecimal weightDemand = firstMeaningful(presaleItem.getActualOutWeight(), presaleItem.getEstimatedWeight(), presaleItem.getQuantity());
      BigDecimal allocatedPieces = allocateAmount(remainingPieces, key, pieceDemand);
      BigDecimal allocatedWeight = allocateAmount(remainingWeights, key, weightDemand);
      if (allocatedPieces.compareTo(BigDecimal.ZERO) <= 0 && allocatedWeight.compareTo(BigDecimal.ZERO) <= 0) {
        continue;
      }
      BigDecimal effectivePieces = firstMeaningful(presaleItem.getPieceCount(), allocatedPieces, pieceDemand);
      BigDecimal effectiveWeight = firstMeaningful(presaleItem.getQuantity(), presaleItem.getEstimatedWeight(), allocatedWeight, weightDemand);
      ErpTradeOrderItemEntity item = new ErpTradeOrderItemEntity();
      item.setSourceOrderItemId(presaleItem.getId());
      item.setProductId(presaleItem.getProductId());
      item.setProductCode(presaleItem.getProductCode());
      item.setProductName(presaleItem.getProductName());
      item.setProductSpec(presaleItem.getProductSpec());
      item.setUnit(presaleItem.getUnit());
      item.setWarehouseName(firstNonBlank(purchaseOrder.getWarehouseName(), presaleItem.getWarehouseName()));
      item.setBatchNo(firstNonBlank(presaleItem.getBatchNo(), purchaseOrder.getContainerNo()));
      item.setSourceContainerNo(firstNonBlank(purchaseOrder.getContainerNo(), presaleItem.getSourceContainerNo()));
      item.setPieceCount(effectivePieces);
      item.setActualPieceCount(effectivePieces);
      item.setEstimatedWeight(effectiveWeight);
      item.setActualOutWeight(effectiveWeight);
      item.setQuantity(item.getActualOutWeight());
      item.setLossWeight(nvl(presaleItem.getLossWeight()));
      item.setUnitPrice(presaleItem.getUnitPrice());
      item.setTaxRate(presaleItem.getTaxRate());
      item.setShelfLifeDays(presaleItem.getShelfLifeDays());
      item.setProductionDate(presaleItem.getProductionDate());
      item.setExpiryDate(presaleItem.getExpiryDate());
      item.setRemark(firstNonBlank(presaleItem.getRemark(), "Auto generated from presale"));
      items.add(item);
    }
    if (items.isEmpty()) {
      return null;
    }
    ErpTradeOrderEntity outbound = new ErpTradeOrderEntity();
    outbound.setOrderType("SALE");
    outbound.setBizType(BIZ_NORMAL_OUTBOUND);
    outbound.setPartnerId(presale.getPartnerId());
    outbound.setPartnerName(presale.getPartnerName());
    outbound.setBrandId(firstNonNullId(presale.getBrandId(), purchaseOrder.getBrandId()));
    outbound.setBrandName(firstNonBlank(presale.getBrandName(), purchaseOrder.getBrandName()));
    outbound.setSecondaryPartnerId(presale.getSecondaryPartnerId());
    outbound.setSecondaryPartnerName(presale.getSecondaryPartnerName());
    outbound.setFunderId(firstNonNullId(presale.getFunderId(), purchaseOrder.getFunderId()));
    outbound.setFunderName(firstNonBlank(presale.getFunderName(), purchaseOrder.getFunderName()));
    outbound.setContractNo(presale.getContractNo());
    outbound.setRelatedContractNo(String.valueOf(purchaseOrder.getId()));
    outbound.setContainerNo(firstNonBlank(purchaseOrder.getContainerNo(), presale.getContainerNo()));
    outbound.setWarehouseId(purchaseOrder.getWarehouseId());
    outbound.setWarehouseName(purchaseOrder.getWarehouseName());
    outbound.setSourceOrderId(presale.getId());
    outbound.setSourceOrderNo(presale.getOrderNo());
    outbound.setStorageStartDate(firstNonNullDate(purchaseOrder.getStorageStartDate(), purchaseOrder.getOrderDate()));
    outbound.setOrderDate(firstNonNullDate(purchaseOrder.getOrderDate(), new Date()));
    outbound.setExpectedDate(presale.getExpectedDate());
    outbound.setPaymentDueDate(presale.getPaymentDueDate());
    outbound.setActualOutDate(firstNonNullDate(purchaseOrder.getOrderDate(), new Date()));
    outbound.setCurrency(firstNonBlank(presale.getCurrency(), purchaseOrder.getCurrency()));
    outbound.setStatus(1);
    outbound.setPaymentStatus(0);
    outbound.setInvoiceStatus(0);
    outbound.setAutoOutbound(0);
    outbound.setStorageFeeStartDays(firstNonNullInt(presale.getStorageFeeStartDays(), purchaseOrder.getStorageFeeStartDays()));
    outbound.setOrderSource(SOURCE_AUTO_OUTBOUND);
    outbound.setRemark("Auto generated from presale " + presale.getOrderNo() + " after purchase inbound " + purchaseOrder.getOrderNo());
    outbound.setItemList(items);
    outbound.setExpenseList(new ArrayList<>());
    return outbound;
  }

  private void repairAutoOutboundIfNeeded(Long outboundOrderId, ErpTradeOrderEntity presale, ErpTradeOrderEntity purchaseOrder, Long userId) {
    ErpTradeOrderEntity saved = getDetail(outboundOrderId);
    if (saved == null || saved.getItemList() == null || saved.getItemList().isEmpty()) {
      return;
    }
    boolean needRepair = false;
    for (ErpTradeOrderItemEntity item : saved.getItemList()) {
      if (firstMeaningful(item.getQuantity(), item.getEstimatedWeight(), item.getActualOutWeight()).compareTo(BigDecimal.ZERO) <= 0) {
        needRepair = true;
        break;
      }
    }
    if (!needRepair) {
      return;
    }
    BigDecimal itemAmount = BigDecimal.ZERO;
    BigDecimal taxAmount = BigDecimal.ZERO;
    for (ErpTradeOrderItemEntity savedItem : saved.getItemList()) {
      ErpTradeOrderItemEntity presaleItem = savedItem.getSourceOrderItemId() == null ? null : erpTradeOrderItemDao.selectById(savedItem.getSourceOrderItemId());
      if (presaleItem == null) {
        continue;
      }
      savedItem.setWarehouseName(firstNonBlank(purchaseOrder.getWarehouseName(), presaleItem.getWarehouseName()));
      savedItem.setBatchNo(firstNonBlank(presaleItem.getBatchNo(), purchaseOrder.getContainerNo()));
      savedItem.setSourceContainerNo(firstNonBlank(purchaseOrder.getContainerNo(), presaleItem.getSourceContainerNo()));
      savedItem.setPieceCount(firstMeaningful(presaleItem.getPieceCount(), presaleItem.getActualPieceCount()));
      savedItem.setActualPieceCount(firstMeaningful(presaleItem.getActualPieceCount(), presaleItem.getPieceCount()));
      savedItem.setEstimatedWeight(firstMeaningful(presaleItem.getQuantity(), presaleItem.getEstimatedWeight()));
      savedItem.setActualOutWeight(firstMeaningful(presaleItem.getQuantity(), presaleItem.getEstimatedWeight()));
      savedItem.setQuantity(firstMeaningful(presaleItem.getQuantity(), presaleItem.getEstimatedWeight()));
      savedItem.setLossWeight(nvl(presaleItem.getLossWeight()));
      savedItem.setUnitPrice(presaleItem.getUnitPrice());
      savedItem.setTaxRate(nvl(presaleItem.getTaxRate()));
      savedItem.setAmount(multiply(savedItem.getQuantity(), savedItem.getUnitPrice()));
      savedItem.setTaxAmount(percent(savedItem.getAmount(), savedItem.getTaxRate()));
      savedItem.setTotalAmount(savedItem.getAmount().add(savedItem.getTaxAmount()));
      savedItem.setShelfLifeDays(presaleItem.getShelfLifeDays());
      savedItem.setProductionDate(presaleItem.getProductionDate());
      savedItem.setExpiryDate(presaleItem.getExpiryDate());
      savedItem.setRemark(firstNonBlank(presaleItem.getRemark(), "Auto repaired from presale"));
      savedItem.setUpdateTime(new Date());
      erpTradeOrderItemDao.updateById(savedItem);
      itemAmount = itemAmount.add(savedItem.getAmount());
      taxAmount = taxAmount.add(savedItem.getTaxAmount());
    }
    saved.setItemAmount(scale(itemAmount));
    saved.setTaxAmount(scale(taxAmount));
    saved.setTotalAmount(scale(itemAmount.add(nvl(saved.getExpenseAmount())).add(taxAmount)));
    saved.setUpdateTime(new Date());
    this.updateById(saved);
    erpStockLedgerDao.delete(new QueryWrapper<ErpStockLedgerEntity>().eq("order_id", saved.getId()));
    Date ledgerNow = new Date();
    for (ErpTradeOrderItemEntity savedItem : saved.getItemList()) {
      erpStockLedgerDao.insert(buildLedger(saved, savedItem, userId, ledgerNow));
    }
  }

  private BigDecimal allocateAmount(Map<String, BigDecimal> remaining, String key, BigDecimal demand) {
    BigDecimal available = remaining.getOrDefault(key, BigDecimal.ZERO);
    if (demand == null || demand.compareTo(BigDecimal.ZERO) <= 0) {
      return BigDecimal.ZERO;
    }
    if (available.compareTo(BigDecimal.ZERO) <= 0) {
      return BigDecimal.ZERO;
    }
    BigDecimal allocated = available.min(demand);
    remaining.put(key, available.subtract(allocated));
    return allocated;
  }

  private void validateOrder(ErpTradeOrderEntity order) {
    if ("SALE".equals(order.getOrderType()) && BIZ_PRESALE.equals(order.getBizType()) && order.getSecondaryPartnerId() == null) {
      throw new RuntimeException("预销售单必须关联二批主体");
    }
    if ("PURCHASE".equals(order.getOrderType()) && order.getWarehouseId() == null && StringUtils.isBlank(order.getWarehouseName())) {
      throw new RuntimeException("采购入库单必须指定仓库");
    }
  }

  private void normalizeWarehouseFee(ErpTradeOrderEntity order, List<ErpTradeOrderItemEntity> items, List<ErpTradeOrderExpenseEntity> expenses) {
    if (!"SALE".equals(order.getOrderType())) {
      return;
    }
    if (!(BIZ_NORMAL_OUTBOUND.equals(order.getBizType()) || BIZ_LOSS_OUTBOUND.equals(order.getBizType()))) {
      return;
    }
    if (order.getWarehouseId() == null || order.getActualOutDate() == null) {
      return;
    }
    ErpWarehouseEntity warehouse = erpWarehouseDao.selectById(order.getWarehouseId());
    if (warehouse == null) {
      return;
    }
    Date startDate = firstNonNullDate(order.getStorageStartDate(), order.getOrderDate());
    long freeDays = Math.max(firstNonNullInt(order.getStorageFeeStartDays(), warehouse.getFreeStorageDays()), 0);
    long chargeDays = Math.max(daysBetween(startDate, order.getActualOutDate()) - freeDays, 0);
    if (chargeDays <= 0) {
      removeAutoExpense(expenses, "WAREHOUSE_STORAGE");
      removeAutoExpense(expenses, "WAREHOUSE_COLD");
      return;
    }
    BigDecimal chargeBase = resolveFeeBase(items, warehouse.getFeeUnit());
    if (chargeBase.compareTo(BigDecimal.ZERO) <= 0) {
      return;
    }
    if (warehouse.getDailyStorageFee() != null && warehouse.getDailyStorageFee().compareTo(BigDecimal.ZERO) > 0) {
      upsertAutoExpense(expenses, "WAREHOUSE_STORAGE", "仓储费", warehouse.getDailyStorageFee().multiply(chargeBase).multiply(BigDecimal.valueOf(chargeDays)), "按仓库规则自动生成");
    }
    if (warehouse.getDailyColdFee() != null && warehouse.getDailyColdFee().compareTo(BigDecimal.ZERO) > 0) {
      upsertAutoExpense(expenses, "WAREHOUSE_COLD", "冷链费", warehouse.getDailyColdFee().multiply(chargeBase).multiply(BigDecimal.valueOf(chargeDays)), "按仓库规则自动生成");
    }
  }

  private void removeAutoExpense(List<ErpTradeOrderExpenseEntity> expenses, String expenseType) {
    expenses.removeIf(item -> expenseType.equals(item.getExpenseType()));
  }

  private void upsertAutoExpense(List<ErpTradeOrderExpenseEntity> expenses, String expenseType, String expenseName, BigDecimal amount, String remark) {
    for (ErpTradeOrderExpenseEntity expense : expenses) {
      if (expenseType.equals(expense.getExpenseType())) {
        expense.setExpenseName(expenseName);
        expense.setAmount(scale(amount));
        expense.setTaxRate(BigDecimal.ZERO);
        expense.setRemark(remark);
        return;
      }
    }
    ErpTradeOrderExpenseEntity expense = new ErpTradeOrderExpenseEntity();
    expense.setExpenseType(expenseType);
    expense.setExpenseName(expenseName);
    expense.setAmount(scale(amount));
    expense.setTaxRate(BigDecimal.ZERO);
    expense.setRemark(remark);
    expenses.add(expense);
  }

  private BigDecimal resolveFeeBase(List<ErpTradeOrderItemEntity> items, String feeUnit) {
    BigDecimal total = BigDecimal.ZERO;
    if ("WEIGHT".equalsIgnoreCase(feeUnit)) {
      for (ErpTradeOrderItemEntity item : items) {
        total = total.add(firstMeaningful(item.getActualOutWeight(), item.getEstimatedWeight(), item.getQuantity()));
      }
      return total;
    }
    if ("ORDER".equalsIgnoreCase(feeUnit)) {
      return items.isEmpty() ? BigDecimal.ZERO : BigDecimal.ONE;
    }
    for (ErpTradeOrderItemEntity item : items) {
      total = total.add(firstMeaningful(item.getActualPieceCount(), item.getPieceCount()));
    }
    return total;
  }

  private void fillProductSnapshot(ErpTradeOrderItemEntity item) {
    if (item.getProductId() == null) {
      return;
    }
    ErpProductEntity product = erpProductDao.selectById(item.getProductId());
    if (product == null) {
      return;
    }
    item.setProductCode(product.getProductCode());
    item.setProductName(product.getProductName());
    item.setProductSpec(product.getProductSpec());
    item.setUnit(product.getUnit());
    if (item.getTaxRate() == null) {
      item.setTaxRate(product.getDefaultTaxRate());
    }
  }

  private String generateOrderNo(String orderType, String bizType) {
    String prefix;
    if ("SALE".equals(orderType) && BIZ_PRESALE.equals(bizType)) {
      prefix = "PS";
    } else if ("SALE".equals(orderType) && BIZ_LOSS_OUTBOUND.equals(bizType)) {
      prefix = "LS";
    } else if ("SALE".equals(orderType)) {
      prefix = "SO";
    } else {
      prefix = "PO";
    }
    return prefix + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
  }

  private String defaultBizType(String orderType) {
    return "SALE".equals(orderType) ? BIZ_NORMAL_OUTBOUND : BIZ_PURCHASE_INBOUND;
  }

  private void fillPartnerSnapshot(ErpTradeOrderEntity order) {
    ErpPartnerEntity partner = order.getPartnerId() == null ? null : erpPartnerDao.selectById(order.getPartnerId());
    order.setPartnerName(partner == null ? order.getPartnerName() : partner.getPartnerName());
    ErpPartnerEntity brand = order.getBrandId() == null ? null : erpPartnerDao.selectById(order.getBrandId());
    order.setBrandName(brand == null ? order.getBrandName() : brand.getPartnerName());
    ErpPartnerEntity second = order.getSecondaryPartnerId() == null ? null : erpPartnerDao.selectById(order.getSecondaryPartnerId());
    order.setSecondaryPartnerName(second == null ? order.getSecondaryPartnerName() : second.getPartnerName());
    ErpPartnerEntity funder = order.getFunderId() == null ? null : erpPartnerDao.selectById(order.getFunderId());
    order.setFunderName(funder == null ? order.getFunderName() : funder.getPartnerName());
  }

  private void fillWarehouseSnapshot(ErpTradeOrderEntity order) {
    if (order.getWarehouseId() == null) {
      return;
    }
    ErpWarehouseEntity warehouse = erpWarehouseDao.selectById(order.getWarehouseId());
    if (warehouse != null) {
      order.setWarehouseName(warehouse.getWarehouseName());
      if ((order.getStorageFeeStartDays() == null || order.getStorageFeeStartDays() == 0) && warehouse.getFreeStorageDays() != null) {
        order.setStorageFeeStartDays(warehouse.getFreeStorageDays());
      }
    }
  }

  private QueryWrapper<ErpTradeOrderEntity> autoOutboundQuery(Long presaleId, Long purchaseId) {
    return new QueryWrapper<ErpTradeOrderEntity>()
        .eq("source_order_id", presaleId)
        .eq("related_contract_no", String.valueOf(purchaseId))
        .eq("order_source", SOURCE_AUTO_OUTBOUND);
  }

  private String resolveCounterparty(ErpTradeOrderEntity order) {
    if ("SALE".equals(order.getOrderType()) && StringUtils.isNotBlank(order.getSecondaryPartnerName())) {
      return order.getSecondaryPartnerName();
    }
    return StringUtils.defaultIfEmpty(order.getPartnerName(), order.getBrandName());
  }

  private String buildSummary(ErpTradeOrderEntity order) {
    String prefix;
    if (BIZ_PRESALE.equals(order.getBizType())) {
      prefix = "预销售单";
    } else if (BIZ_LOSS_OUTBOUND.equals(order.getBizType())) {
      prefix = "损耗赎单";
    } else if ("SALE".equals(order.getOrderType())) {
      prefix = "销售出库单";
    } else {
      prefix = "采购入库单";
    }
    return prefix + " " + StringUtils.defaultString(order.getOrderNo()) + " " + StringUtils.defaultString(order.getContractNo());
  }

  private String buildMatchKey(ErpTradeOrderEntity order) {
    List<String> parts = new ArrayList<>();
    if (StringUtils.isNotBlank(order.getOrderNo())) {
      parts.add(order.getOrderNo());
    }
    if (StringUtils.isNotBlank(order.getContractNo())) {
      parts.add(order.getContractNo());
    }
    if (StringUtils.isNotBlank(order.getContainerNo())) {
      parts.add(order.getContainerNo());
    }
    return StringUtils.join(parts, "|");
  }

  private String bizTypeLabel(String bizType) {
    if (BIZ_PRESALE.equals(bizType)) {
      return "预销售";
    }
    if (BIZ_LOSS_OUTBOUND.equals(bizType)) {
      return "损耗赎单";
    }
    if (BIZ_NORMAL_OUTBOUND.equals(bizType)) {
      return "正常赎单/出库";
    }
    return "采购入库";
  }

  private String paymentStatusLabel(Integer status) {
    if (status == null || status == 0) {
      return "未支付";
    }
    if (status == 1) {
      return "部分支付";
    }
    return "已支付";
  }

  private String invoiceStatusLabel(Integer status) {
    if (status == null || status == 0) {
      return "未开票";
    }
    if (status == 1) {
      return "部分开票";
    }
    return "已开票";
  }

  private boolean overlaps(String left, String right) {
    if (StringUtils.isBlank(left) || StringUtils.isBlank(right)) {
      return false;
    }
    String[] leftParts = left.replace("，", ",").replace("/", ",").split("[,\\s]+");
    String[] rightParts = right.replace("，", ",").replace("/", ",").split("[,\\s]+");
    for (String l : leftParts) {
      if (StringUtils.isBlank(l)) {
        continue;
      }
      for (String r : rightParts) {
        if (StringUtils.isBlank(r)) {
          continue;
        }
        if (StringUtils.equalsIgnoreCase(l.trim(), r.trim())) {
          return true;
        }
      }
    }
    return false;
  }

  private long daysBetween(Date start, Date end) {
    if (start == null || end == null) {
      return 0L;
    }
    LocalDate startDate = Instant.ofEpochMilli(start.getTime()).atZone(ZONE_ID).toLocalDate();
    LocalDate endDate = Instant.ofEpochMilli(end.getTime()).atZone(ZONE_ID).toLocalDate();
    return Math.max(ChronoUnit.DAYS.between(startDate, endDate), 0L);
  }

  private String productKey(ErpTradeOrderItemEntity item) {
    if (item.getProductId() != null) {
      return "ID:" + item.getProductId();
    }
    return "CODE:" + StringUtils.defaultString(item.getProductCode());
  }

  private BigDecimal multiply(BigDecimal left, BigDecimal right) {
    return scale(left.multiply(right));
  }

  private BigDecimal percent(BigDecimal amount, BigDecimal rate) {
    if (rate == null) {
      return BigDecimal.ZERO;
    }
    return scale(amount.multiply(rate).divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP));
  }

  private BigDecimal scale(BigDecimal value) {
    return value == null ? BigDecimal.ZERO : value.setScale(2, RoundingMode.HALF_UP);
  }

  private BigDecimal nvl(BigDecimal value) {
    return value == null ? BigDecimal.ZERO : value;
  }

  private BigDecimal firstNonNull(BigDecimal... values) {
    for (BigDecimal value : values) {
      if (value != null) {
        return value;
      }
    }
    return BigDecimal.ZERO;
  }

  private BigDecimal firstMeaningful(BigDecimal... values) {
    BigDecimal fallback = null;
    for (BigDecimal value : values) {
      if (value == null) {
        continue;
      }
      if (fallback == null) {
        fallback = value;
      }
      if (value.compareTo(BigDecimal.ZERO) > 0) {
        return value;
      }
    }
    return fallback == null ? BigDecimal.ZERO : fallback;
  }

  private String stringValue(Object value) {
    return value == null ? null : value.toString();
  }

  private String firstNonBlank(String... values) {
    for (String value : values) {
      if (StringUtils.isNotBlank(value)) {
        return value;
      }
    }
    return null;
  }

  private Long firstNonNullId(Long... values) {
    for (Long value : values) {
      if (value != null) {
        return value;
      }
    }
    return null;
  }

  private Date firstNonNullDate(Date... values) {
    for (Date value : values) {
      if (value != null) {
        return value;
      }
    }
    return null;
  }

  private Integer firstNonNullInt(Integer... values) {
    for (Integer value : values) {
      if (value != null) {
        return value;
      }
    }
    return 0;
  }

  private BigDecimal max(BigDecimal left, BigDecimal right) {
    if (left == null) {
      return right == null ? BigDecimal.ZERO : right;
    }
    if (right == null) {
      return left;
    }
    return left.max(right);
  }
}
