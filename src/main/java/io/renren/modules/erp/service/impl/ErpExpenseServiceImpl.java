package io.renren.modules.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.renren.common.utils.PageUtils;
import io.renren.common.utils.Query;
import io.renren.modules.erp.dao.ErpExpenseDao;
import io.renren.modules.erp.dao.ErpInboundOrderDao;
import io.renren.modules.erp.dao.ErpPartnerDao;
import io.renren.modules.erp.dao.ErpPresaleConfirmDao;
import io.renren.modules.erp.dao.ErpPresaleConfirmItemDao;
import io.renren.modules.erp.dao.ErpSaleOrderDao;
import io.renren.modules.erp.dao.ErpSaleOrderItemDao;
import io.renren.modules.erp.dao.ErpSaleOutboundBatchDao;
import io.renren.modules.erp.dao.ErpSaleOutboundReceiptItemDao;
import io.renren.modules.erp.entity.ErpExpenseEntity;
import io.renren.modules.erp.entity.ErpInboundOrderEntity;
import io.renren.modules.erp.entity.ErpPartnerEntity;
import io.renren.modules.erp.entity.ErpPresaleConfirmEntity;
import io.renren.modules.erp.entity.ErpPresaleConfirmItemEntity;
import io.renren.modules.erp.entity.ErpSaleOrderEntity;
import io.renren.modules.erp.entity.ErpSaleOrderItemEntity;
import io.renren.modules.erp.entity.ErpSaleOutboundBatchEntity;
import io.renren.modules.erp.entity.ErpSaleOutboundReceiptItemEntity;
import io.renren.modules.erp.entity.ErpWarehouseFeeRateEntity;
import io.renren.modules.erp.service.ErpExpenseService;
import io.renren.modules.erp.service.ErpWarehouseFeeRateService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("erpExpenseService")
public class ErpExpenseServiceImpl extends ServiceImpl<ErpExpenseDao, ErpExpenseEntity> implements ErpExpenseService {
  private static final BigDecimal KG_PER_TON = new BigDecimal("1000");
  private static final ZoneId ZONE = ZoneId.systemDefault();

  @Autowired
  private ErpWarehouseFeeRateService erpWarehouseFeeRateService;
  @Autowired
  private ErpPresaleConfirmDao erpPresaleConfirmDao;
  @Autowired
  private ErpPresaleConfirmItemDao erpPresaleConfirmItemDao;
  @Autowired
  private ErpInboundOrderDao erpInboundOrderDao;
  @Autowired
  private ErpSaleOrderDao erpSaleOrderDao;
  @Autowired
  private ErpSaleOrderItemDao erpSaleOrderItemDao;
  @Autowired
  private ErpSaleOutboundBatchDao erpSaleOutboundBatchDao;
  @Autowired
  private ErpSaleOutboundReceiptItemDao erpSaleOutboundReceiptItemDao;
  @Autowired
  private ErpPartnerDao erpPartnerDao;

  @Override
  public PageUtils queryPage(Map<String, Object> params) {
    QueryWrapper<ErpExpenseEntity> wrapper = new QueryWrapper<ErpExpenseEntity>();
    String keyword = stringValue(params.get("keyword"));
    if (StringUtils.isNotBlank(keyword)) {
      wrapper.and(w -> w.like("expense_no", keyword)
          .or().like("expense_name", keyword)
          .or().like("contract_no", keyword)
          .or().like("partner_name", keyword)
          .or().like("warehouse_name", keyword)
          .or().like("sale_order_no", keyword));
    }
    String expenseType = stringValue(params.get("expenseType"));
    if (StringUtils.isNotBlank(expenseType)) {
      wrapper.eq("expense_type", expenseType);
    }
    String sourceType = stringValue(params.get("sourceType"));
    if (StringUtils.isNotBlank(sourceType)) {
      wrapper.eq("source_type", sourceType);
    }
    wrapper.orderByDesc("business_end_date", "id");
    IPage<ErpExpenseEntity> page = this.page(new Query<ErpExpenseEntity>().getPage(params), wrapper);
    return new PageUtils(page);
  }

  @Override
  public List<ErpExpenseEntity> listBySource(String sourceType, Long sourceId) {
    if (StringUtils.isBlank(sourceType) || sourceId == null) {
      return new ArrayList<ErpExpenseEntity>();
    }
    List<ErpExpenseEntity> list = this.list(new QueryWrapper<ErpExpenseEntity>()
        .eq("source_type", sourceType)
        .eq("source_id", sourceId)
        .eq("status", 1)
        .orderByAsc("id"));
    return list == null ? new ArrayList<ErpExpenseEntity>() : list;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void regenerateInboundHandlingExpense(ErpInboundOrderEntity order, Long userId) {
    if (order == null || order.getId() == null) {
      return;
    }
    Long presaleOrderId = order.getPresaleOrderId();
    ErpPresaleConfirmEntity confirm = loadConfirm(presaleOrderId);
    BigDecimal weightKg = sumConfirmWeightKg(confirm == null ? null : confirm.getId());
    if (weightKg.compareTo(BigDecimal.ZERO) <= 0 || order.getWarehouseId() == null) {
      deleteBySource(TYPE_INBOUND_HANDLING, SOURCE_INBOUND_ORDER, order.getId());
      return;
    }
    String temperatureZone = normalizeTemperature(confirm == null ? null : confirm.getColdFreshType());
    Date businessDate = order.getOrderDate() == null ? new Date() : order.getOrderDate();
    ErpWarehouseFeeRateEntity rate = erpWarehouseFeeRateService.getEffectiveRate(order.getWarehouseId(), businessDate);
    BigDecimal unitRate = handlingRate(rate, temperatureZone);
    BigDecimal weightTon = toTon(weightKg);
    BigDecimal amount = money(weightTon.multiply(unitRate));
    ErpExpenseEntity expense = baseExpense(TYPE_INBOUND_HANDLING, SOURCE_INBOUND_ORDER, order.getId(), userId);
    expense.setExpenseName("入库装卸费");
    expense.setSourceNo(order.getContractNo());
    expense.setInboundOrderId(order.getId());
    expense.setPresaleOrderId(presaleOrderId);
    expense.setContractNo(order.getContractNo());
    expense.setWarehouseId(order.getWarehouseId());
    expense.setWarehouseName(order.getWarehouseName());
    expense.setTemperatureZone(temperatureZone);
    expense.setBusinessStartDate(stripDate(businessDate));
    expense.setBusinessEndDate(stripDate(businessDate));
    expense.setFreeDays(0);
    expense.setChargeDays(1);
    expense.setWeightKg(weightKg.setScale(3, RoundingMode.HALF_UP));
    expense.setWeightTon(weightTon);
    expense.setRate(unitRate);
    expense.setAmount(amount);
    expense.setTaxRate(BigDecimal.ZERO);
    expense.setTaxAmount(BigDecimal.ZERO);
    expense.setTotalAmount(amount);
    expense.setRemark("入库时按确认函重量自动生成，装卸费只在入库收取");
    upsertExpense(expense);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void regenerateOutboundStorageExpense(Long outboundBatchId, Long userId) {
    ErpSaleOutboundBatchEntity batch = outboundBatchId == null ? null : erpSaleOutboundBatchDao.selectById(outboundBatchId);
    if (batch == null || batch.getSaleOrderId() == null) {
      return;
    }
    ErpSaleOrderEntity order = erpSaleOrderDao.selectById(batch.getSaleOrderId());
    if (order == null) {
      return;
    }
    BigDecimal weightKg = resolveOutboundWeightKg(batch);
    if (weightKg.compareTo(BigDecimal.ZERO) <= 0) {
      deleteBySource(TYPE_OUTBOUND_STORAGE, SOURCE_OUTBOUND_BATCH, batch.getId());
      return;
    }
    List<ErpSaleOrderItemEntity> saleItems = loadSaleItems(order.getId());
    Long presaleOrderId = resolvePresaleOrderId(order, saleItems);
    ErpInboundOrderEntity inbound = resolveInboundOrder(order, saleItems, presaleOrderId);
    Long warehouseId = firstLong(order.getWarehouseId(), firstItemWarehouseId(saleItems), inbound == null ? null : inbound.getWarehouseId());
    String warehouseName = firstNonBlank(order.getWarehouseName(), firstItemWarehouseName(saleItems), inbound == null ? null : inbound.getWarehouseName());
    if (warehouseId == null) {
      deleteBySource(TYPE_OUTBOUND_STORAGE, SOURCE_OUTBOUND_BATCH, batch.getId());
      return;
    }
    ErpPresaleConfirmEntity confirm = loadConfirm(presaleOrderId);
    String temperatureZone = normalizeTemperature(confirm == null ? null : confirm.getColdFreshType());
    Date startDate = resolveStorageStartDate(order, saleItems, inbound);
    Date endDate = batch.getConfirmTime() == null ? new Date() : batch.getConfirmTime();
    int freeDays = resolveFreeDays(order.getSecondaryPartnerId());
    StorageAmount storage = calcStorageAmount(warehouseId, temperatureZone, weightKg, startDate, endDate, freeDays);
    ErpExpenseEntity expense = baseExpense(TYPE_OUTBOUND_STORAGE, SOURCE_OUTBOUND_BATCH, batch.getId(), userId);
    expense.setExpenseName("出库冷库费");
    expense.setSourceChildId(batch.getId());
    expense.setSourceNo(batch.getBatchNo());
    expense.setSaleOrderId(order.getId());
    expense.setSaleOrderNo(order.getOrderNo());
    expense.setOutboundBatchId(batch.getId());
    expense.setPresaleOrderId(presaleOrderId);
    expense.setContractNo(order.getContractNo());
    expense.setPartnerId(order.getSecondaryPartnerId());
    expense.setPartnerName(order.getSecondaryPartnerName());
    expense.setWarehouseId(warehouseId);
    expense.setWarehouseName(warehouseName);
    expense.setTemperatureZone(temperatureZone);
    expense.setBusinessStartDate(stripDate(startDate));
    expense.setBusinessEndDate(stripDate(endDate));
    expense.setFreeDays(freeDays);
    expense.setChargeDays(storage.chargeDays);
    expense.setWeightKg(weightKg.setScale(3, RoundingMode.HALF_UP));
    expense.setWeightTon(toTon(weightKg));
    expense.setRate(storage.averageRate);
    expense.setAmount(storage.amount);
    expense.setTaxRate(BigDecimal.ZERO);
    expense.setTaxAmount(BigDecimal.ZERO);
    expense.setTotalAmount(storage.amount);
    expense.setRemark("出库批次确认时按二批商冷库减免天数自动生成");
    upsertExpense(expense);
  }

  @Override
  public void deleteBySource(String expenseType, String sourceType, Long sourceId) {
    if (StringUtils.isBlank(expenseType) || StringUtils.isBlank(sourceType) || sourceId == null) {
      return;
    }
    this.remove(new QueryWrapper<ErpExpenseEntity>()
        .eq("expense_type", expenseType)
        .eq("source_type", sourceType)
        .eq("source_id", sourceId));
  }

  private ErpExpenseEntity baseExpense(String expenseType, String sourceType, Long sourceId, Long userId) {
    ErpExpenseEntity existing = this.getOne(new QueryWrapper<ErpExpenseEntity>()
        .eq("expense_type", expenseType)
        .eq("source_type", sourceType)
        .eq("source_id", sourceId)
        .last("limit 1"));
    Date now = new Date();
    ErpExpenseEntity expense = existing == null ? new ErpExpenseEntity() : existing;
    expense.setExpenseNo("EXP-" + expenseType + "-" + sourceId);
    expense.setExpenseType(expenseType);
    expense.setSourceType(sourceType);
    expense.setSourceId(sourceId);
    expense.setStatus(1);
    if (expense.getId() == null) {
      expense.setCreateUserId(userId);
      expense.setCreateTime(now);
    }
    expense.setUpdateTime(now);
    return expense;
  }

  private void upsertExpense(ErpExpenseEntity expense) {
    if (expense.getId() == null) {
      this.save(expense);
    } else {
      this.updateById(expense);
    }
  }

  private BigDecimal sumConfirmWeightKg(Long confirmId) {
    if (confirmId == null) {
      return BigDecimal.ZERO;
    }
    List<ErpPresaleConfirmItemEntity> items = erpPresaleConfirmItemDao.selectList(
        new QueryWrapper<ErpPresaleConfirmItemEntity>().eq("confirm_id", confirmId));
    BigDecimal total = BigDecimal.ZERO;
    if (items != null) {
      for (ErpPresaleConfirmItemEntity item : items) {
        BigDecimal quantity = nvl(item.getQuantity());
        if (isTonUnit(item.getUnit())) {
          quantity = quantity.multiply(KG_PER_TON);
        }
        total = total.add(quantity);
      }
    }
    return total;
  }

  private BigDecimal resolveOutboundWeightKg(ErpSaleOutboundBatchEntity batch) {
    if (batch.getShippedTotalWeight() != null && batch.getShippedTotalWeight().compareTo(BigDecimal.ZERO) > 0) {
      return batch.getShippedTotalWeight();
    }
    List<ErpSaleOutboundReceiptItemEntity> items = erpSaleOutboundReceiptItemDao.selectList(
        new QueryWrapper<ErpSaleOutboundReceiptItemEntity>().eq("batch_id", batch.getId()));
    BigDecimal total = BigDecimal.ZERO;
    if (items != null) {
      for (ErpSaleOutboundReceiptItemEntity item : items) {
        total = total.add(nvl(item.getTotalWeight()));
      }
    }
    return total;
  }

  private StorageAmount calcStorageAmount(Long warehouseId, String temperatureZone, BigDecimal weightKg, Date startDate, Date endDate, int freeDays) {
    LocalDate start = toLocalDate(startDate);
    LocalDate end = toLocalDate(endDate);
    if (start == null || end == null || end.isBefore(start)) {
      return new StorageAmount(0, BigDecimal.ZERO, BigDecimal.ZERO);
    }
    LocalDate chargeDate = start.plusDays(Math.max(freeDays, 0));
    BigDecimal weightTon = toTon(weightKg);
    BigDecimal amount = BigDecimal.ZERO;
    BigDecimal rateTotal = BigDecimal.ZERO;
    int chargeDays = 0;
    while (!chargeDate.isAfter(end)) {
      ErpWarehouseFeeRateEntity rate = erpWarehouseFeeRateService.getEffectiveRate(warehouseId, fromLocalDate(chargeDate));
      BigDecimal dailyRate = storageRate(rate, temperatureZone);
      amount = amount.add(weightTon.multiply(dailyRate));
      rateTotal = rateTotal.add(dailyRate);
      chargeDays++;
      chargeDate = chargeDate.plusDays(1);
    }
    BigDecimal averageRate = chargeDays <= 0 ? BigDecimal.ZERO : rateTotal.divide(BigDecimal.valueOf(chargeDays), 6, RoundingMode.HALF_UP);
    return new StorageAmount(chargeDays, money(amount), averageRate);
  }

  private Date resolveStorageStartDate(ErpSaleOrderEntity order, List<ErpSaleOrderItemEntity> items, ErpInboundOrderEntity inbound) {
    if ("SPOT".equalsIgnoreCase(order.getSaleType())) {
      return order.getCreateTime() == null ? new Date() : order.getCreateTime();
    }
    if (inbound != null) {
      return inbound.getOrderDate() == null ? inbound.getCreateTime() : inbound.getOrderDate();
    }
    for (ErpSaleOrderItemEntity item : items) {
      if (item.getInboundDate() != null) {
        return item.getInboundDate();
      }
    }
    return order.getCreateTime() == null ? new Date() : order.getCreateTime();
  }

  private ErpInboundOrderEntity resolveInboundOrder(ErpSaleOrderEntity order, List<ErpSaleOrderItemEntity> items, Long presaleOrderId) {
    if (presaleOrderId != null) {
      ErpInboundOrderEntity inbound = erpInboundOrderDao.selectOne(
          new QueryWrapper<ErpInboundOrderEntity>().eq("presale_order_id", presaleOrderId).last("limit 1"));
      if (inbound != null) {
        return inbound;
      }
    }
    for (ErpSaleOrderItemEntity item : items) {
      if (item.getSourceInboundOrderId() != null) {
        return erpInboundOrderDao.selectById(item.getSourceInboundOrderId());
      }
    }
    return null;
  }

  private Long resolvePresaleOrderId(ErpSaleOrderEntity order, List<ErpSaleOrderItemEntity> items) {
    if (order.getSourcePresaleOrderId() != null) {
      return order.getSourcePresaleOrderId();
    }
    for (ErpSaleOrderItemEntity item : items) {
      if (item.getSourcePresaleOrderId() != null) {
        return item.getSourcePresaleOrderId();
      }
      if (item.getSourceInboundOrderId() != null) {
        ErpInboundOrderEntity inbound = erpInboundOrderDao.selectById(item.getSourceInboundOrderId());
        if (inbound != null && inbound.getPresaleOrderId() != null) {
          return inbound.getPresaleOrderId();
        }
      }
    }
    return null;
  }

  private ErpPresaleConfirmEntity loadConfirm(Long presaleOrderId) {
    if (presaleOrderId == null) {
      return null;
    }
    return erpPresaleConfirmDao.selectOne(
        new QueryWrapper<ErpPresaleConfirmEntity>().eq("presale_order_id", presaleOrderId).last("limit 1"));
  }

  private List<ErpSaleOrderItemEntity> loadSaleItems(Long saleOrderId) {
    List<ErpSaleOrderItemEntity> items = erpSaleOrderItemDao.selectList(
        new QueryWrapper<ErpSaleOrderItemEntity>().eq("sale_order_id", saleOrderId).orderByAsc("line_no", "id"));
    return items == null ? new ArrayList<ErpSaleOrderItemEntity>() : items;
  }

  private int resolveFreeDays(Long partnerId) {
    if (partnerId == null) {
      return 0;
    }
    ErpPartnerEntity partner = erpPartnerDao.selectById(partnerId);
    return partner == null || partner.getColdStorageFreeDays() == null ? 0 : Math.max(partner.getColdStorageFreeDays(), 0);
  }

  private Long firstItemWarehouseId(List<ErpSaleOrderItemEntity> items) {
    for (ErpSaleOrderItemEntity item : items) {
      if (item.getWarehouseId() != null) {
        return item.getWarehouseId();
      }
    }
    return null;
  }

  private String firstItemWarehouseName(List<ErpSaleOrderItemEntity> items) {
    for (ErpSaleOrderItemEntity item : items) {
      if (StringUtils.isNotBlank(item.getWarehouseName())) {
        return item.getWarehouseName();
      }
    }
    return null;
  }

  private BigDecimal handlingRate(ErpWarehouseFeeRateEntity rate, String temperatureZone) {
    if (rate == null) {
      return BigDecimal.ZERO;
    }
    return isFrozen(temperatureZone) ? nvl(rate.getFrozenColdFee()) : nvl(rate.getChilledColdFee());
  }

  private BigDecimal storageRate(ErpWarehouseFeeRateEntity rate, String temperatureZone) {
    if (rate == null) {
      return BigDecimal.ZERO;
    }
    return isFrozen(temperatureZone) ? nvl(rate.getFrozenStorageFee()) : nvl(rate.getChilledStorageFee());
  }

  private String normalizeTemperature(String value) {
    String text = StringUtils.trimToEmpty(value);
    if (text.contains("冻") || text.toUpperCase().contains("FROZEN")) {
      return "冷冻";
    }
    return "冷藏";
  }

  private boolean isFrozen(String value) {
    return StringUtils.trimToEmpty(value).contains("冻") || StringUtils.trimToEmpty(value).toUpperCase().contains("FROZEN");
  }

  private boolean isTonUnit(String unit) {
    String text = StringUtils.trimToEmpty(unit).toUpperCase();
    return text.contains("吨") || text.contains("TON");
  }

  private BigDecimal toTon(BigDecimal weightKg) {
    return nvl(weightKg).divide(KG_PER_TON, 6, RoundingMode.HALF_UP);
  }

  private BigDecimal money(BigDecimal value) {
    return nvl(value).setScale(2, RoundingMode.HALF_UP);
  }

  private BigDecimal nvl(BigDecimal value) {
    return value == null ? BigDecimal.ZERO : value;
  }

  private Long firstLong(Long... values) {
    if (values == null) {
      return null;
    }
    for (Long value : values) {
      if (value != null) {
        return value;
      }
    }
    return null;
  }

  private String firstNonBlank(String... values) {
    if (values == null) {
      return "";
    }
    for (String value : values) {
      if (StringUtils.isNotBlank(value)) {
        return value;
      }
    }
    return "";
  }

  private String stringValue(Object value) {
    return value == null ? null : String.valueOf(value).trim();
  }

  private Date stripDate(Date date) {
    LocalDate localDate = toLocalDate(date);
    return localDate == null ? null : fromLocalDate(localDate);
  }

  private LocalDate toLocalDate(Date date) {
    return date == null ? null : date.toInstant().atZone(ZONE).toLocalDate();
  }

  private Date fromLocalDate(LocalDate date) {
    return date == null ? null : Date.from(date.atStartOfDay(ZONE).toInstant());
  }

  private static class StorageAmount {
    private final int chargeDays;
    private final BigDecimal amount;
    private final BigDecimal averageRate;

    private StorageAmount(int chargeDays, BigDecimal amount, BigDecimal averageRate) {
      this.chargeDays = chargeDays;
      this.amount = amount;
      this.averageRate = averageRate;
    }
  }
}
