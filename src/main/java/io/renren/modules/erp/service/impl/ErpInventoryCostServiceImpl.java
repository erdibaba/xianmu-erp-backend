package io.renren.modules.erp.service.impl;

import io.renren.modules.erp.dao.ErpInventoryDao;
import io.renren.modules.erp.service.ErpInventoryCostService;
import io.renren.modules.erp.vo.ErpInventoryBatchVo;
import io.renren.modules.erp.vo.ErpInventoryCostDetailVo;
import io.renren.modules.erp.vo.ErpInventoryCostVo;
import io.renren.modules.erp.vo.ErpSpotInventoryVo;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
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

  @Autowired
  private ErpInventoryDao erpInventoryDao;

  @Autowired
  private JdbcTemplate jdbcTemplate;

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
      RowAccumulator accumulator = getAccumulator(context.rows, row);
      for (ErpInventoryBatchVo batch : batches) {
        BigDecimal availableWeight = positive(batch.getAvailableWeightKg());
        if (availableWeight.compareTo(ZERO) <= 0) {
          continue;
        }
        accumulator.availableWeightKg = accumulator.availableWeightKg.add(availableWeight);
        accumulator.availableBoxes += intValue(batch.getAvailableBoxes());

        BigDecimal purchaseUnitPrice = getPurchaseUnitPrice(batch.getInboundItemId(), purchaseUnitPriceCache);
        BigDecimal purchaseAmount = money(availableWeight.multiply(purchaseUnitPrice));
        accumulator.purchaseAmount = accumulator.purchaseAmount.add(purchaseAmount);
        if (purchaseAmount.compareTo(ZERO) > 0) {
          accumulator.detailCount++;
          context.details.add(detail("采购成本", "订单确认函采购价", batch.getContractNo(), batch.getContractNo(),
              batch.getContainerNo(), batch.getFactoryNo(), purchaseAmount, purchaseAmount, availableWeight,
              availableWeight, "采购单价按确认函产品行带出，金额按当前剩余重量计算"));
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
              batch.getContainerNo(), batch.getFactoryNo(), component.amount, allocated, availableWeight,
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
    String loanSql = "SELECT confirm_contract_no, loan_no, interest_amount "
        + "FROM erp_funder_loan WHERE confirm_contract_no IN (" + placeholders + ") "
        + "AND COALESCE(interest_amount, 0) <> 0";
    for (Map<String, Object> row : jdbcTemplate.queryForList(loanSql, args)) {
      addComponent(result, string(row.get("confirm_contract_no")), new CostComponent(
          "资金成本", "资方贷款利息", string(row.get("loan_no")), decimal(row.get("interest_amount")),
          "按资方贷款明细已计算利息分摊"));
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

  private RowAccumulator getAccumulator(Map<String, RowAccumulator> rows, ErpSpotInventoryVo source) {
    String key = source.getProductId() + "|" + StringUtils.defaultString(source.getOwnershipName());
    RowAccumulator existing = rows.get(key);
    if (existing != null) {
      return existing;
    }
    ErpInventoryCostVo row = new ErpInventoryCostVo();
    row.setProductId(source.getProductId());
    row.setProductCode(source.getProductCode());
    row.setProductName(source.getProductName());
    row.setProductNameEn(source.getProductNameEn());
    row.setOwnershipName(source.getOwnershipName());
    RowAccumulator accumulator = new RowAccumulator(row);
    rows.put(key, accumulator);
    return accumulator;
  }

  private ErpInventoryCostDetailVo detail(String costType, String costName, String sourceNo, String contractNo,
                                          String containerNo, String factoryNo, BigDecimal sourceAmount,
                                          BigDecimal allocatedAmount, BigDecimal basisWeightKg,
                                          BigDecimal totalBasisWeightKg, String remark) {
    ErpInventoryCostDetailVo detail = new ErpInventoryCostDetailVo();
    detail.setCostType(costType);
    detail.setCostName(costName);
    detail.setSourceNo(sourceNo);
    detail.setContractNo(contractNo);
    detail.setContainerNo(containerNo);
    detail.setFactoryNo(factoryNo);
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
