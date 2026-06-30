package io.renren.modules.erp.service.impl;

import io.renren.modules.erp.dao.ErpInventoryDao;
import io.renren.modules.erp.entity.ErpWarehouseFeeRateEntity;
import io.renren.modules.erp.service.ErpInventoryCostService;
import io.renren.modules.erp.service.ErpWarehouseFeeRateService;
import io.renren.modules.erp.vo.ErpInventoryBatchVo;
import io.renren.modules.erp.vo.ErpInventoryCostDetailVo;
import io.renren.modules.erp.vo.ErpInventoryCostVo;
import io.renren.modules.erp.vo.ErpSpotInventoryVo;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service("erpInventoryCostService")
public class ErpInventoryCostServiceImpl implements ErpInventoryCostService {
  private static final BigDecimal ZERO = BigDecimal.ZERO;
  private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
  private static final BigDecimal DAYS_PER_YEAR = new BigDecimal("365");
  private static final BigDecimal KG_PER_TON = new BigDecimal("1000");

  @Autowired
  private ErpInventoryDao erpInventoryDao;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Autowired
  private ErpWarehouseFeeRateService erpWarehouseFeeRateService;

  @Override
  public List<ErpInventoryCostVo> querySpotCost(Map<String, Object> params) {
    CostContext context = buildContext(params, null, null);
    List<ErpInventoryCostVo> result = new ArrayList<ErpInventoryCostVo>();
    for (RowAccumulator accumulator : context.rows.values()) {
      result.add(accumulator.row);
    }
    return result;
  }

  @Override
  public List<ErpInventoryCostDetailVo> querySpotCostDetails(Map<String, Object> params) {
    Long productId = getLong(params, "productId");
    String ownershipName = getString(params, "ownershipName");
    CostContext context = buildContext(params, productId, ownershipName);
    return context.details;
  }

  private CostContext buildContext(Map<String, Object> params, Long detailProductId, String detailOwnershipName) {
    Map<String, Object> queryParams = normalizeSpotParams(params, false);
    List<ErpSpotInventoryVo> visibleRows = erpInventoryDao.querySpot(
        getString(queryParams, "keyword"),
        getLong(queryParams, "warehouseId"),
        getList(queryParams, "containerNos"),
        getString(queryParams, "factoryNo"),
        getFlag(queryParams, "onlyAvailable"));

    Map<String, BigDecimal> contractTotalWeight = buildContractTotalWeight();
    Map<String, List<CostComponent>> costComponents = loadCostComponents(contractTotalWeight.keySet());
    Map<Long, BigDecimal> purchaseUnitPriceCache = new HashMap<Long, BigDecimal>();
    Map<Long, List<ErpWarehouseFeeRateEntity>> warehouseRateCache = new HashMap<Long, List<ErpWarehouseFeeRateEntity>>();
    CostContext context = new CostContext();

    for (ErpSpotInventoryVo row : visibleRows) {
      if (detailProductId != null && !detailProductId.equals(row.getProductId())) {
        continue;
      }
      if (StringUtils.isNotBlank(detailOwnershipName)
          && !StringUtils.equals(detailOwnershipName, row.getOwnershipName())) {
        continue;
      }
      List<ErpInventoryBatchVo> batches = queryVisibleBatches(row, queryParams);
      for (ErpInventoryBatchVo batch : batches) {
        if (!matchDetailContract(batch, params)) {
          continue;
        }
        BigDecimal availableWeight = positive(batch.getAvailableWeightKg());
        if (availableWeight.compareTo(ZERO) <= 0) {
          continue;
        }
        RowAccumulator accumulator = getAccumulator(context.rows, row, batch);
        accumulator.availableWeightKg = accumulator.availableWeightKg.add(availableWeight);
        accumulator.availableBoxes += intValue(batch.getAvailableBoxes());

        BigDecimal purchaseUnitPrice = getPurchaseUnitPrice(batch.getInboundItemId(), purchaseUnitPriceCache);
        BigDecimal purchaseAmount = money(availableWeight.multiply(purchaseUnitPrice));
        accumulator.purchaseAmount = accumulator.purchaseAmount.add(purchaseAmount);
        if (purchaseAmount.compareTo(ZERO) > 0) {
          accumulator.detailCount++;
          context.details.add(detail("采购成本", "批次采购成本", batch.getContractNo(), batch.getContractNo(),
              batch.getContainerNo(), batch.getFactoryNo(), batch.getProductionDate(), batch.getExpiryDate(),
              batch.getAvailableBoxes(), purchaseAmount, purchaseAmount, availableWeight,
              availableWeight, "采购单价按确认函产品行带出，金额按当前剩余重量计算"));
        }

        StorageCost storageCost = calcDynamicStorageCost(batch, availableWeight, warehouseRateCache);
        if (storageCost.amount.compareTo(ZERO) > 0) {
          accumulator.detailCount++;
          accumulator.allocatedFeeAmount = accumulator.allocatedFeeAmount.add(storageCost.amount);
          context.details.add(detail("仓储费用", "动态仓储费", batch.getWarehouseName(), batch.getContractNo(),
              batch.getContainerNo(), batch.getFactoryNo(), batch.getProductionDate(), batch.getExpiryDate(),
              batch.getAvailableBoxes(), storageCost.amount, storageCost.amount, availableWeight,
              availableWeight, storageCost.remark));
        }

        List<CostComponent> components = costComponents.get(StringUtils.defaultString(batch.getContractNo()));
        if (components == null || components.isEmpty()) {
          continue;
        }
        BigDecimal totalBasisWeight = positive(contractTotalWeight.get(StringUtils.defaultString(batch.getContractNo())));
        if (totalBasisWeight.compareTo(ZERO) <= 0) {
          continue;
        }
        for (CostComponent component : components) {
          BigDecimal allocated = money(component.amount.multiply(availableWeight).divide(totalBasisWeight, 10, RoundingMode.HALF_UP));
          if (allocated.compareTo(ZERO) == 0) {
            continue;
          }
          accumulator.detailCount++;
          accumulator.allocatedFeeAmount = accumulator.allocatedFeeAmount.add(allocated);
          context.details.add(detail(component.costType, component.costName, component.sourceNo, batch.getContractNo(),
              batch.getContainerNo(), batch.getFactoryNo(), batch.getProductionDate(), batch.getExpiryDate(),
              batch.getAvailableBoxes(), component.amount, allocated, availableWeight,
              totalBasisWeight, component.remark));
        }
      }
    }

    for (RowAccumulator accumulator : context.rows.values()) {
      ErpInventoryCostVo row = accumulator.row;
      row.setAvailableBoxes(accumulator.availableBoxes);
      row.setAvailableWeightKg(weight(accumulator.availableWeightKg));
      row.setPurchaseAmount(money(accumulator.purchaseAmount));
      row.setAllocatedFeeAmount(money(accumulator.allocatedFeeAmount));
      BigDecimal totalCost = accumulator.purchaseAmount.add(accumulator.allocatedFeeAmount);
      row.setTotalCostAmount(money(totalCost));
      row.setCostDetailCount(accumulator.detailCount);
      if (accumulator.availableWeightKg.compareTo(ZERO) > 0) {
        row.setCostPriceKg(price(totalCost.divide(accumulator.availableWeightKg, 10, RoundingMode.HALF_UP)));
        row.setPurchasePriceKg(price(accumulator.purchaseAmount.divide(accumulator.availableWeightKg, 10, RoundingMode.HALF_UP)));
      } else {
        row.setCostPriceKg(ZERO.setScale(6, RoundingMode.HALF_UP));
        row.setPurchasePriceKg(ZERO.setScale(6, RoundingMode.HALF_UP));
      }
    }
    return context;
  }

  private Map<String, BigDecimal> buildContractTotalWeight() {
    Map<String, BigDecimal> totals = new HashMap<String, BigDecimal>();
    List<ErpSpotInventoryVo> allRows = erpInventoryDao.querySpot(null, null, new ArrayList<String>(), null, 1);
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("onlyAvailable", 1);
    for (ErpSpotInventoryVo row : allRows) {
      params.put("productId", row.getProductId());
      params.put("ownershipName", row.getOwnershipName());
      List<ErpInventoryBatchVo> batches = erpInventoryDao.querySpotBatches(row.getProductId(), null,
          new ArrayList<String>(), row.getOwnershipName(), null, 1);
      for (ErpInventoryBatchVo batch : batches) {
        String contractNo = StringUtils.defaultString(batch.getContractNo());
        if (StringUtils.isBlank(contractNo)) {
          continue;
        }
        BigDecimal availableWeight = positive(batch.getAvailableWeightKg());
        if (availableWeight.compareTo(ZERO) <= 0) {
          continue;
        }
        totals.put(contractNo, positive(totals.get(contractNo)).add(availableWeight));
      }
    }
    return totals;
  }

  private List<ErpInventoryBatchVo> queryVisibleBatches(ErpSpotInventoryVo row, Map<String, Object> params) {
    return erpInventoryDao.querySpotBatches(
        row.getProductId(),
        getLong(params, "warehouseId"),
        getList(params, "containerNos"),
        row.getOwnershipName(),
        getString(params, "factoryNo"),
        getFlag(params, "onlyAvailable"));
  }

  private Map<String, List<CostComponent>> loadCostComponents(Iterable<String> contractNos) {
    Map<String, List<CostComponent>> result = new HashMap<String, List<CostComponent>>();
    List<String> contracts = new ArrayList<String>();
    for (String contractNo : contractNos) {
      if (StringUtils.isNotBlank(contractNo)) {
        contracts.add(contractNo);
      }
    }
    if (contracts.isEmpty()) {
      return result;
    }
    String placeholders = placeholders(contracts.size());
    Object[] args = contracts.toArray(new Object[contracts.size()]);
    loadExpenseComponents(result, placeholders, args);
    loadFunderLoanComponents(result, placeholders, args);
    return result;
  }

  private void loadExpenseComponents(Map<String, List<CostComponent>> result, String placeholders, Object[] args) {
    String sql = "SELECT contract_no, expense_type, expense_name, expense_no, total_amount, remark "
        + "FROM erp_expense WHERE contract_no IN (" + placeholders + ") AND COALESCE(total_amount, 0) <> 0";
    for (Map<String, Object> row : jdbcTemplate.queryForList(sql, args)) {
      String contractNo = string(row.get("contract_no"));
      BigDecimal amount = decimal(row.get("total_amount"));
      addComponent(result, contractNo, new CostComponent(
          StringUtils.defaultIfBlank(string(row.get("expense_type")), "支出费用"),
          StringUtils.defaultIfBlank(string(row.get("expense_name")), "支出费用"),
          string(row.get("expense_no")),
          amount,
          string(row.get("remark"))));
    }
  }

  private void loadFunderLoanComponents(Map<String, List<CostComponent>> result, String placeholders, Object[] args) {
    String loanSql = "SELECT confirm_contract_no, loan_no, loan_amount, repaid_principal, remaining_principal, "
        + "annual_interest_rate, loan_date, interest_amount "
        + "FROM erp_funder_loan WHERE confirm_contract_no IN (" + placeholders + ") "
        + "AND (COALESCE(loan_amount, 0) <> 0 OR COALESCE(interest_amount, 0) <> 0)";
    for (Map<String, Object> row : jdbcTemplate.queryForList(loanSql, args)) {
      BigDecimal dynamicInterest = calcDynamicLoanInterest(row);
      BigDecimal recordedInterest = decimal(row.get("interest_amount"));
      BigDecimal amount = dynamicInterest.max(recordedInterest);
      if (amount.compareTo(ZERO) == 0) {
        continue;
      }
      String remark = "按贷款剩余本金、年利率、贷款日期到今天动态估算；若已登记利息更高，则按已登记利息计入";
      addComponent(result, string(row.get("confirm_contract_no")), new CostComponent(
          "资金成本", "资方贷款动态利息", string(row.get("loan_no")), amount, remark));
    }

    String repaySql = "SELECT l.confirm_contract_no, r.repayment_no, r.handling_fee_amount "
        + "FROM erp_funder_loan_repayment r INNER JOIN erp_funder_loan l ON l.id = r.loan_id "
        + "WHERE l.confirm_contract_no IN (" + placeholders + ") AND COALESCE(r.handling_fee_amount, 0) <> 0";
    for (Map<String, Object> row : jdbcTemplate.queryForList(repaySql, args)) {
      addComponent(result, string(row.get("confirm_contract_no")), new CostComponent(
          "资金成本", "资方还款手续费", string(row.get("repayment_no")), decimal(row.get("handling_fee_amount")),
          "按还款记录手续费分摊"));
    }
  }

  private BigDecimal calcDynamicLoanInterest(Map<String, Object> row) {
    BigDecimal annualRate = decimal(row.get("annual_interest_rate"));
    if (annualRate.compareTo(ZERO) <= 0) {
      return ZERO;
    }
    Date loanDate = date(row.get("loan_date"));
    LocalDate start = toLocalDate(loanDate);
    LocalDate today = LocalDate.now();
    if (start == null || today.isBefore(start)) {
      return ZERO;
    }
    BigDecimal loanAmount = decimal(row.get("loan_amount"));
    BigDecimal repaidPrincipal = decimal(row.get("repaid_principal"));
    BigDecimal remainingPrincipal = decimal(row.get("remaining_principal"));
    BigDecimal principal = remainingPrincipal.compareTo(ZERO) > 0
        ? remainingPrincipal : loanAmount.subtract(repaidPrincipal).max(ZERO);
    if (principal.compareTo(ZERO) <= 0) {
      return ZERO;
    }
    long days = ChronoUnit.DAYS.between(start, today) + 1;
    if (days <= 0) {
      return ZERO;
    }
    return principal.multiply(annualRate)
        .divide(ONE_HUNDRED, 16, RoundingMode.HALF_UP)
        .divide(DAYS_PER_YEAR, 16, RoundingMode.HALF_UP)
        .multiply(BigDecimal.valueOf(days))
        .setScale(10, RoundingMode.HALF_UP);
  }

  private StorageCost calcDynamicStorageCost(ErpInventoryBatchVo batch, BigDecimal availableWeight,
                                             Map<Long, List<ErpWarehouseFeeRateEntity>> warehouseRateCache) {
    if (batch == null || batch.getWarehouseId() == null || availableWeight.compareTo(ZERO) <= 0) {
      return StorageCost.zero();
    }
    LocalDate start = toLocalDate(batch.getInboundDate());
    LocalDate today = LocalDate.now();
    if (start == null || today.isBefore(start)) {
      return StorageCost.zero();
    }
    List<ErpWarehouseFeeRateEntity> rates = cachedWarehouseRates(batch.getWarehouseId(), warehouseRateCache);
    if (rates.isEmpty()) {
      return StorageCost.zero();
    }
    BigDecimal weightTon = availableWeight.divide(KG_PER_TON, 10, RoundingMode.HALF_UP);
    BigDecimal amount = ZERO;
    BigDecimal rateTotal = ZERO;
    int chargeDays = 0;
    LocalDate cursor = start;
    while (!cursor.isAfter(today)) {
      ErpWarehouseFeeRateEntity rate = effectiveRate(rates, cursor);
      if (rate != null) {
        BigDecimal dailyRate = storageRate(rate, batch.getTemperatureZone());
        amount = amount.add(weightTon.multiply(dailyRate));
        rateTotal = rateTotal.add(dailyRate);
        chargeDays++;
      }
      cursor = cursor.plusDays(1);
    }
    if (chargeDays <= 0 || amount.compareTo(ZERO) == 0) {
      return StorageCost.zero();
    }
    BigDecimal averageRate = rateTotal.divide(BigDecimal.valueOf(chargeDays), 6, RoundingMode.HALF_UP);
    String remark = "按当前剩余重量、入库日期到今天、仓库费率历史逐日计算；计费天数"
        + chargeDays + "天，平均费率" + price(averageRate) + "元/吨/天";
    return new StorageCost(money(amount), chargeDays, averageRate, remark);
  }

  private List<ErpWarehouseFeeRateEntity> cachedWarehouseRates(Long warehouseId,
                                                               Map<Long, List<ErpWarehouseFeeRateEntity>> cache) {
    if (warehouseId == null) {
      return new ArrayList<ErpWarehouseFeeRateEntity>();
    }
    if (cache.containsKey(warehouseId)) {
      return cache.get(warehouseId);
    }
    List<ErpWarehouseFeeRateEntity> rates = erpWarehouseFeeRateService.listByWarehouseId(warehouseId);
    if (rates == null) {
      rates = new ArrayList<ErpWarehouseFeeRateEntity>();
    }
    List<ErpWarehouseFeeRateEntity> enabled = new ArrayList<ErpWarehouseFeeRateEntity>();
    for (ErpWarehouseFeeRateEntity rate : rates) {
      if (rate != null && (rate.getStatus() == null || rate.getStatus() == 1) && rate.getEffectiveDate() != null) {
        enabled.add(rate);
      }
    }
    Collections.sort(enabled, new Comparator<ErpWarehouseFeeRateEntity>() {
      @Override
      public int compare(ErpWarehouseFeeRateEntity left, ErpWarehouseFeeRateEntity right) {
        int dateCompare = left.getEffectiveDate().compareTo(right.getEffectiveDate());
        if (dateCompare != 0) {
          return dateCompare;
        }
        Long leftId = left.getId() == null ? 0L : left.getId();
        Long rightId = right.getId() == null ? 0L : right.getId();
        return leftId.compareTo(rightId);
      }
    });
    cache.put(warehouseId, enabled);
    return enabled;
  }

  private ErpWarehouseFeeRateEntity effectiveRate(List<ErpWarehouseFeeRateEntity> rates, LocalDate businessDate) {
    ErpWarehouseFeeRateEntity matched = null;
    for (ErpWarehouseFeeRateEntity rate : rates) {
      LocalDate effectiveDate = toLocalDate(rate.getEffectiveDate());
      if (effectiveDate != null && !effectiveDate.isAfter(businessDate)) {
        matched = rate;
      }
      if (effectiveDate != null && effectiveDate.isAfter(businessDate)) {
        break;
      }
    }
    return matched;
  }

  private BigDecimal storageRate(ErpWarehouseFeeRateEntity rate, String temperatureZone) {
    if (rate == null) {
      return ZERO;
    }
    return isFrozen(temperatureZone) ? decimal(rate.getFrozenStorageFee()) : decimal(rate.getChilledStorageFee());
  }

  private boolean isFrozen(String value) {
    String text = StringUtils.defaultString(value).toUpperCase();
    return text.contains("冻") || text.contains("FROZEN");
  }

  private BigDecimal getPurchaseUnitPrice(Long inboundItemId, Map<Long, BigDecimal> cache) {
    if (inboundItemId == null) {
      return ZERO;
    }
    if (cache.containsKey(inboundItemId)) {
      return cache.get(inboundItemId);
    }
    String sql = "SELECT COALESCE(NULLIF(pci.unit_price_incl_tax, 0), "
        + "CASE WHEN COALESCE(pci.quantity, 0) <> 0 THEN COALESCE(pci.line_total_incl_tax, 0) / pci.quantity ELSE 0 END, 0) AS unit_price "
        + "FROM erp_inbound_order_item i "
        + "INNER JOIN erp_inbound_order io ON io.id = i.inbound_order_id "
        + "LEFT JOIN erp_presale_confirm pc ON (pc.id = io.confirm_id "
        + "OR (io.confirm_id IS NULL AND pc.presale_order_id = io.presale_order_id AND pc.contract_no = io.contract_no)) "
        + "LEFT JOIN erp_presale_confirm_item pci ON pci.confirm_id = pc.id AND pci.product_id = i.product_id "
        + "WHERE i.id = ? ORDER BY pci.id DESC LIMIT 1";
    BigDecimal unitPrice = ZERO;
    List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, inboundItemId);
    if (!rows.isEmpty()) {
      unitPrice = decimal(rows.get(0).get("unit_price"));
    }
    cache.put(inboundItemId, unitPrice);
    return unitPrice;
  }

  private boolean matchDetailContract(ErpInventoryBatchVo batch, Map<String, Object> params) {
    Long detailConfirmId = getLong(params, "confirmId");
    String detailContractNo = getString(params, "contractNo");
    if (detailConfirmId != null) {
      return detailConfirmId.equals(batch.getConfirmId());
    }
    if (StringUtils.isNotBlank(detailContractNo)) {
      return StringUtils.equals(detailContractNo, batch.getContractNo());
    }
    return true;
  }

  private RowAccumulator getAccumulator(Map<String, RowAccumulator> rows, ErpSpotInventoryVo source, ErpInventoryBatchVo batch) {
    String key = source.getProductId() + "|"
        + StringUtils.defaultString(source.getOwnershipName()) + "|"
        + StringUtils.defaultString(batch.getConfirmId() == null ? "" : batch.getConfirmId().toString()) + "|"
        + StringUtils.defaultString(batch.getContractNo());
    RowAccumulator existing = rows.get(key);
    if (existing != null) {
      return existing;
    }
    ErpInventoryCostVo row = new ErpInventoryCostVo();
    row.setProductId(source.getProductId());
    row.setConfirmId(batch.getConfirmId());
    row.setContractNo(batch.getContractNo());
    row.setProductCode(source.getProductCode());
    row.setProductName(source.getProductName());
    row.setProductNameEn(source.getProductNameEn());
    row.setOwnershipName(source.getOwnershipName());
    RowAccumulator accumulator = new RowAccumulator(row);
    rows.put(key, accumulator);
    return accumulator;
  }

  private ErpInventoryCostDetailVo detail(String costType, String costName, String sourceNo, String contractNo,
                                          String containerNo, String factoryNo, java.util.Date productionDate,
                                          java.util.Date expiryDate, Integer availableBoxes, BigDecimal sourceAmount,
                                          BigDecimal allocatedAmount, BigDecimal basisWeightKg,
                                          BigDecimal totalBasisWeightKg, String remark) {
    ErpInventoryCostDetailVo detail = new ErpInventoryCostDetailVo();
    detail.setCostType(costType);
    detail.setCostName(costName);
    detail.setSourceNo(sourceNo);
    detail.setContractNo(contractNo);
    detail.setContainerNo(containerNo);
    detail.setFactoryNo(factoryNo);
    detail.setProductionDate(productionDate);
    detail.setExpiryDate(expiryDate);
    detail.setAvailableBoxes(availableBoxes == null ? 0 : availableBoxes);
    detail.setSourceAmount(money(sourceAmount));
    detail.setAllocatedAmount(money(allocatedAmount));
    detail.setBasisWeightKg(weight(basisWeightKg));
    detail.setTotalBasisWeightKg(weight(totalBasisWeightKg));
    detail.setRemark(remark);
    return detail;
  }

  private Map<String, Object> normalizeSpotParams(Map<String, Object> params, boolean all) {
    Map<String, Object> result = new HashMap<String, Object>();
    if (params != null && !all) {
      result.putAll(params);
    }
    if (StringUtils.isBlank(getString(result, "onlyAvailable"))) {
      result.put("onlyAvailable", 1);
    }
    return result;
  }

  private void addComponent(Map<String, List<CostComponent>> result, String contractNo, CostComponent component) {
    if (StringUtils.isBlank(contractNo) || component.amount.compareTo(ZERO) == 0) {
      return;
    }
    List<CostComponent> list = result.get(contractNo);
    if (list == null) {
      list = new ArrayList<CostComponent>();
      result.put(contractNo, list);
    }
    list.add(component);
  }

  private String placeholders(int count) {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < count; i++) {
      if (i > 0) {
        builder.append(',');
      }
      builder.append('?');
    }
    return builder.toString();
  }

  private String getString(Map<String, Object> params, String key) {
    Object value = params == null ? null : params.get(key);
    return value == null ? null : value.toString().trim();
  }

  private Integer getFlag(Map<String, Object> params, String key) {
    String value = getString(params, key);
    if (StringUtils.isBlank(value)) {
      return 0;
    }
    return "1".equals(value) || "true".equalsIgnoreCase(value) ? 1 : 0;
  }

  private Long getLong(Map<String, Object> params, String key) {
    String value = getString(params, key);
    return StringUtils.isBlank(value) ? null : Long.valueOf(value);
  }

  private List<String> getList(Map<String, Object> params, String key) {
    String value = getString(params, key);
    List<String> result = new ArrayList<String>();
    if (StringUtils.isBlank(value)) {
      return result;
    }
    String[] parts = value.split(",");
    for (String part : parts) {
      String text = part == null ? "" : part.trim();
      if (text.length() > 0) {
        result.add(text);
      }
    }
    return result;
  }

  private String string(Object value) {
    return value == null ? "" : value.toString();
  }

  private Date date(Object value) {
    return value instanceof Date ? (Date) value : null;
  }

  private LocalDate toLocalDate(Date value) {
    return value == null ? null
        : java.time.Instant.ofEpochMilli(value.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
  }

  private BigDecimal decimal(Object value) {
    if (value == null) {
      return ZERO;
    }
    if (value instanceof BigDecimal) {
      return (BigDecimal) value;
    }
    return new BigDecimal(value.toString());
  }

  private BigDecimal positive(BigDecimal value) {
    return value == null ? ZERO : value.max(ZERO);
  }

  private int intValue(Integer value) {
    return value == null ? 0 : value.intValue();
  }

  private BigDecimal money(BigDecimal value) {
    return (value == null ? ZERO : value).setScale(2, RoundingMode.HALF_UP);
  }

  private BigDecimal weight(BigDecimal value) {
    return (value == null ? ZERO : value).setScale(2, RoundingMode.HALF_UP);
  }

  private BigDecimal price(BigDecimal value) {
    return (value == null ? ZERO : value).setScale(6, RoundingMode.HALF_UP);
  }

  private static class CostContext {
    private Map<String, RowAccumulator> rows = new LinkedHashMap<String, RowAccumulator>();
    private List<ErpInventoryCostDetailVo> details = new ArrayList<ErpInventoryCostDetailVo>();
  }

  private static class RowAccumulator {
    private ErpInventoryCostVo row;
    private Integer availableBoxes = 0;
    private BigDecimal availableWeightKg = BigDecimal.ZERO;
    private BigDecimal purchaseAmount = BigDecimal.ZERO;
    private BigDecimal allocatedFeeAmount = BigDecimal.ZERO;
    private Integer detailCount = 0;

    private RowAccumulator(ErpInventoryCostVo row) {
      this.row = row;
    }
  }

  private static class StorageCost {
    private BigDecimal amount;
    private int chargeDays;
    private BigDecimal averageRate;
    private String remark;

    private StorageCost(BigDecimal amount, int chargeDays, BigDecimal averageRate, String remark) {
      this.amount = amount == null ? BigDecimal.ZERO : amount;
      this.chargeDays = chargeDays;
      this.averageRate = averageRate == null ? BigDecimal.ZERO : averageRate;
      this.remark = remark;
    }

    private static StorageCost zero() {
      return new StorageCost(BigDecimal.ZERO, 0, BigDecimal.ZERO, "");
    }
  }

  private static class CostComponent {
    private String costType;
    private String costName;
    private String sourceNo;
    private BigDecimal amount;
    private String remark;

    private CostComponent(String costType, String costName, String sourceNo, BigDecimal amount, String remark) {
      this.costType = costType;
      this.costName = costName;
      this.sourceNo = sourceNo;
      this.amount = amount == null ? BigDecimal.ZERO : amount;
      this.remark = remark;
    }
  }
}
