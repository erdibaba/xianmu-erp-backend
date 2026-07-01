package io.renren.modules.erp.service.impl;

import io.renren.common.utils.PageUtils;
import io.renren.modules.erp.dao.ErpInventoryDao;
import io.renren.modules.erp.entity.ErpWarehouseFeeRateEntity;
import io.renren.modules.erp.service.ErpContractDailyCostService;
import io.renren.modules.erp.service.ErpWarehouseFeeRateService;
import io.renren.modules.erp.vo.ErpInventoryBatchVo;
import io.renren.modules.erp.vo.ErpSpotInventoryVo;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("erpContractDailyCostService")
public class ErpContractDailyCostServiceImpl implements ErpContractDailyCostService {
  private static final BigDecimal ZERO = BigDecimal.ZERO;
  private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
  private static final BigDecimal DAYS_PER_YEAR = new BigDecimal("365");
  private static final BigDecimal KG_PER_TON = new BigDecimal("1000");

  @Autowired
  private JdbcTemplate jdbcTemplate;
  @Autowired
  private ErpInventoryDao erpInventoryDao;
  @Autowired
  private ErpWarehouseFeeRateService erpWarehouseFeeRateService;

  @Override
  @Transactional
  public PageUtils queryPage(Map<String, Object> params) {
    DateRange range = dateRange(params);
    String contractNo = text(params, "contractNo");
    ensureSnapshots(contractNo, range.start, range.end, false, null);

    int page = intParam(params, "page", 1);
    int limit = intParam(params, "limit", 10);
    int offset = (page - 1) * limit;
    List<Object> args = new ArrayList<Object>();
    String where = whereSql(contractNo, range, args);
    Integer total = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM erp_contract_daily_cost " + where,
        args.toArray(), Integer.class);
    List<Object> listArgs = new ArrayList<Object>(args);
    listArgs.add(offset);
    listArgs.add(limit);
    List<Map<String, Object>> list = jdbcTemplate.queryForList(
        "SELECT * FROM erp_contract_daily_cost " + where
            + " ORDER BY cost_date DESC, contract_no ASC LIMIT ?, ?",
        listArgs.toArray());
    return new PageUtils(list, total == null ? 0 : total, limit, page);
  }

  @Override
  public List<Map<String, Object>> queryDetails(Long dailyCostId) {
    return jdbcTemplate.queryForList(
        "SELECT * FROM erp_contract_daily_cost_detail WHERE daily_cost_id = ? ORDER BY line_no ASC, id ASC",
        dailyCostId);
  }

  @Override
  @Transactional
  public void refresh(Map<String, Object> params, Long userId) {
    DateRange range = dateRange(params);
    String contractNo = text(params, "contractNo");
    ensureSnapshots(contractNo, range.start, range.end, true, userId);
  }

  @Override
  @Transactional
  public void generateSnapshot(LocalDate costDate, Long userId) {
    if (costDate == null) {
      costDate = LocalDate.now().minusDays(1);
    }
    ensureSnapshots(null, costDate, costDate, true, userId);
  }

  private void ensureSnapshots(String contractNoKeyword, LocalDate start, LocalDate end, boolean force, Long userId) {
    List<String> contracts = queryContracts(contractNoKeyword);
    for (String contractNo : contracts) {
      LocalDate cursor = start;
      while (!cursor.isAfter(end)) {
        if (force || !existsSnapshot(contractNo, cursor)) {
          regenerateOne(contractNo, cursor, userId);
        }
        cursor = cursor.plusDays(1);
      }
    }
  }

  private List<String> queryContracts(String contractNoKeyword) {
    List<Object> args = new ArrayList<Object>();
    StringBuilder sql = new StringBuilder();
    sql.append("SELECT DISTINCT contract_no FROM (")
        .append(" SELECT contract_no FROM erp_presale_confirm")
        .append(" UNION ALL SELECT confirm_contract_no AS contract_no FROM erp_funder_loan")
        .append(" UNION ALL SELECT contract_no FROM erp_inbound_order")
        .append(") t WHERE contract_no IS NOT NULL AND contract_no <> ''");
    if (StringUtils.isNotBlank(contractNoKeyword)) {
      sql.append(" AND contract_no LIKE ?");
      args.add("%" + contractNoKeyword.trim() + "%");
    }
    sql.append(" ORDER BY contract_no ASC");
    List<String> rows = jdbcTemplate.queryForList(sql.toString(), args.toArray(), String.class);
    Set<String> result = new LinkedHashSet<String>();
    for (String row : rows) {
      if (StringUtils.isNotBlank(row)) {
        result.add(row.trim());
      }
    }
    return new ArrayList<String>(result);
  }

  private boolean existsSnapshot(String contractNo, LocalDate date) {
    Integer count = jdbcTemplate.queryForObject(
        "SELECT COUNT(1) FROM erp_contract_daily_cost WHERE contract_no = ? AND cost_date = ?",
        new Object[] { contractNo, Date.valueOf(date) }, Integer.class);
    return count != null && count > 0;
  }

  private void regenerateOne(String contractNo, LocalDate date, Long userId) {
    List<Long> oldIds = jdbcTemplate.queryForList(
        "SELECT id FROM erp_contract_daily_cost WHERE contract_no = ? AND cost_date = ?",
        new Object[] { contractNo, Date.valueOf(date) }, Long.class);
    if (!oldIds.isEmpty()) {
      String placeholders = placeholders(oldIds.size());
      jdbcTemplate.update("DELETE FROM erp_contract_daily_cost_detail WHERE daily_cost_id IN (" + placeholders + ")",
          oldIds.toArray());
      jdbcTemplate.update("DELETE FROM erp_contract_daily_cost WHERE id IN (" + placeholders + ")",
          oldIds.toArray());
    }

    CostDay day = calculate(contractNo, date);
    Long dailyCostId = insertDailyCost(contractNo, date, day, userId);
    int lineNo = 1;
    for (CostLine line : day.lines) {
      insertDailyCostDetail(dailyCostId, lineNo++, line);
    }
  }

  private CostDay calculate(String contractNo, LocalDate date) {
    CostDay day = new CostDay();
    calculateInterest(contractNo, date, day);
    calculateStorage(contractNo, date, day);
    day.totalAmount = day.interestAmount.add(day.storageAmount).add(day.otherAmount);
    return day;
  }

  private void calculateInterest(String contractNo, LocalDate date, CostDay day) {
    List<Map<String, Object>> loans = jdbcTemplate.queryForList(
        "SELECT id, loan_no, funder_name, loan_amount, annual_interest_rate, loan_date "
            + "FROM erp_funder_loan WHERE confirm_contract_no = ? AND loan_date <= ? "
            + "AND COALESCE(loan_amount, 0) > 0",
        contractNo, Date.valueOf(date));
    for (Map<String, Object> loan : loans) {
      Long loanId = longValue(loan.get("id"));
      BigDecimal principal = decimal(loan.get("loan_amount")).subtract(repaidBefore(loanId, date)).max(ZERO);
      BigDecimal annualRate = decimal(loan.get("annual_interest_rate"));
      if (principal.compareTo(ZERO) <= 0 || annualRate.compareTo(ZERO) <= 0) {
        continue;
      }
      BigDecimal amount = principal.multiply(annualRate)
          .divide(ONE_HUNDRED, 16, RoundingMode.HALF_UP)
          .divide(DAYS_PER_YEAR, 16, RoundingMode.HALF_UP)
          .setScale(10, RoundingMode.HALF_UP);
      String funderName = string(loan.get("funder_name"));
      day.funderNames.add(funderName);
      day.interestAmount = day.interestAmount.add(amount);
      String formula = "当日利息 = 当日未还本金" + moneyText(principal)
          + " × 年利率" + decimalText(annualRate) + "% ÷ 365 = " + decimalText(amount);
      day.lines.add(new CostLine("资金成本", "资方每日利息", string(loan.get("loan_no")), funderName,
          principal, amount, formula, "还款日当天仍计息；已还本金按还款日期次日开始扣减"));
    }
  }

  private BigDecimal repaidBefore(Long loanId, LocalDate date) {
    BigDecimal result = jdbcTemplate.queryForObject(
        "SELECT COALESCE(SUM(COALESCE(repayment_principal, 0)), 0) "
            + "FROM erp_funder_loan_repayment WHERE loan_id = ? AND status = 1 AND repayment_date < ?",
        new Object[] { loanId, Date.valueOf(date) }, BigDecimal.class);
    return decimal(result);
  }

  private void calculateStorage(String contractNo, LocalDate date, CostDay day) {
    Map<Long, List<ErpWarehouseFeeRateEntity>> rateCache = new HashMap<Long, List<ErpWarehouseFeeRateEntity>>();
    List<ErpSpotInventoryVo> rows = erpInventoryDao.querySpot(null, null, new ArrayList<String>(), null, 1);
    for (ErpSpotInventoryVo row : rows) {
      List<ErpInventoryBatchVo> batches = erpInventoryDao.querySpotBatches(row.getProductId(), null,
          new ArrayList<String>(), row.getOwnershipName(), null, 1);
      for (ErpInventoryBatchVo batch : batches) {
        if (!StringUtils.equals(contractNo, batch.getContractNo())) {
          continue;
        }
        LocalDate inboundDate = toLocalDate(batch.getInboundDate());
        if (inboundDate == null || inboundDate.isAfter(date)) {
          continue;
        }
        BigDecimal weightKg = decimal(batch.getAvailableWeightKg());
        if (weightKg.compareTo(ZERO) <= 0 || batch.getWarehouseId() == null) {
          continue;
        }
        ErpWarehouseFeeRateEntity rate = effectiveRate(batch.getWarehouseId(), date, rateCache);
        if (rate == null) {
          continue;
        }
        BigDecimal unitRate = storageRate(rate, batch.getTemperatureZone());
        if (unitRate.compareTo(ZERO) <= 0) {
          continue;
        }
        BigDecimal weightTon = weightKg.divide(KG_PER_TON, 10, RoundingMode.HALF_UP);
        BigDecimal amount = weightTon.multiply(unitRate).setScale(10, RoundingMode.HALF_UP);
        day.storageAmount = day.storageAmount.add(amount);
        String formula = "当日仓储费 = 当前可售重量" + decimalText(weightKg) + "KG ÷ 1000 × 当日费率"
            + decimalText(unitRate) + "元/吨/天 = " + decimalText(amount);
        String sourceNo = StringUtils.defaultString(batch.getWarehouseName()) + "/"
            + StringUtils.defaultString(batch.getContainerNo());
        day.lines.add(new CostLine("仓储费用", "每日仓储费", sourceNo, batch.getWarehouseName(),
            weightKg, amount, formula, "费率按仓库费用历史取" + date + "当天生效价；温区：" + StringUtils.defaultString(batch.getTemperatureZone())));
      }
    }
  }

  private ErpWarehouseFeeRateEntity effectiveRate(Long warehouseId, LocalDate date,
                                                   Map<Long, List<ErpWarehouseFeeRateEntity>> cache) {
    List<ErpWarehouseFeeRateEntity> rates = cache.get(warehouseId);
    if (rates == null) {
      rates = erpWarehouseFeeRateService.listByWarehouseId(warehouseId);
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
          return left.getEffectiveDate().compareTo(right.getEffectiveDate());
        }
      });
      rates = enabled;
      cache.put(warehouseId, rates);
    }
    ErpWarehouseFeeRateEntity matched = null;
    for (ErpWarehouseFeeRateEntity rate : rates) {
      LocalDate effectiveDate = toLocalDate(rate.getEffectiveDate());
      if (effectiveDate != null && !effectiveDate.isAfter(date)) {
        matched = rate;
      }
      if (effectiveDate != null && effectiveDate.isAfter(date)) {
        break;
      }
    }
    return matched;
  }

  private BigDecimal storageRate(ErpWarehouseFeeRateEntity rate, String temperatureZone) {
    if (rate == null) {
      return ZERO;
    }
    String text = StringUtils.defaultString(temperatureZone).toUpperCase();
    boolean frozen = text.contains("冻") || text.contains("FROZEN");
    return decimal(frozen ? rate.getFrozenStorageFee() : rate.getChilledStorageFee());
  }

  private Long insertDailyCost(final String contractNo, final LocalDate date, final CostDay day, final Long userId) {
    final String funderNames = StringUtils.join(day.funderNames.toArray(), "，");
    final String summary = "成本合计 = 资金利息" + decimalText(day.interestAmount)
        + " + 仓储费" + decimalText(day.storageAmount)
        + " + 其他费用" + decimalText(day.otherAmount)
        + " = " + decimalText(day.totalAmount);
    KeyHolder keyHolder = new GeneratedKeyHolder();
    jdbcTemplate.update((Connection connection) -> {
      PreparedStatement ps = connection.prepareStatement(
          "INSERT INTO erp_contract_daily_cost "
              + "(contract_no, cost_date, funder_names, interest_amount, storage_amount, other_amount, total_amount, calculation_summary, create_user_id) "
              + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
          Statement.RETURN_GENERATED_KEYS);
      ps.setString(1, contractNo);
      ps.setDate(2, Date.valueOf(date));
      ps.setString(3, funderNames);
      ps.setBigDecimal(4, decimal10(day.interestAmount));
      ps.setBigDecimal(5, decimal10(day.storageAmount));
      ps.setBigDecimal(6, decimal10(day.otherAmount));
      ps.setBigDecimal(7, decimal10(day.totalAmount));
      ps.setString(8, summary);
      if (userId == null) {
        ps.setObject(9, null);
      } else {
        ps.setLong(9, userId);
      }
      return ps;
    }, keyHolder);
    return keyHolder.getKey().longValue();
  }

  private void insertDailyCostDetail(Long dailyCostId, int lineNo, CostLine line) {
    jdbcTemplate.update(
        "INSERT INTO erp_contract_daily_cost_detail "
            + "(daily_cost_id, line_no, cost_type, cost_name, source_no, related_name, basis_amount, amount, formula, remark) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
        dailyCostId, lineNo, line.costType, line.costName, line.sourceNo, line.relatedName,
        decimal10(line.basisAmount), decimal10(line.amount), line.formula, line.remark);
  }

  private String whereSql(String contractNo, DateRange range, List<Object> args) {
    StringBuilder where = new StringBuilder("WHERE cost_date BETWEEN ? AND ?");
    args.add(Date.valueOf(range.start));
    args.add(Date.valueOf(range.end));
    if (StringUtils.isNotBlank(contractNo)) {
      where.append(" AND contract_no LIKE ?");
      args.add("%" + contractNo.trim() + "%");
    }
    return where.toString();
  }

  private DateRange dateRange(Map<String, Object> params) {
    LocalDate today = LocalDate.now();
    LocalDate start = parseDate(text(params, "dateStart"), today.withDayOfMonth(1));
    LocalDate end = parseDate(text(params, "dateEnd"), today);
    if (end.isBefore(start)) {
      LocalDate tmp = start;
      start = end;
      end = tmp;
    }
    return new DateRange(start, end);
  }

  private LocalDate parseDate(String text, LocalDate defaultValue) {
    if (StringUtils.isBlank(text)) {
      return defaultValue;
    }
    return LocalDate.parse(text.trim());
  }

  private String placeholders(int size) {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < size; i++) {
      if (i > 0) {
        builder.append(',');
      }
      builder.append('?');
    }
    return builder.toString();
  }

  private String text(Map<String, Object> params, String key) {
    Object value = params == null ? null : params.get(key);
    return value == null ? "" : value.toString().trim();
  }

  private int intParam(Map<String, Object> params, String key, int defaultValue) {
    String value = text(params, key);
    return StringUtils.isBlank(value) ? defaultValue : Integer.parseInt(value);
  }

  private LocalDate toLocalDate(java.util.Date value) {
    return value == null ? null : value.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
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

  private BigDecimal decimal10(BigDecimal value) {
    return (value == null ? ZERO : value).setScale(10, RoundingMode.HALF_UP);
  }

  private Long longValue(Object value) {
    return value == null ? null : Long.valueOf(value.toString());
  }

  private String string(Object value) {
    return value == null ? "" : value.toString();
  }

  private String decimalText(BigDecimal value) {
    return decimal10(value).stripTrailingZeros().toPlainString();
  }

  private String moneyText(BigDecimal value) {
    return (value == null ? ZERO : value).setScale(2, RoundingMode.HALF_UP).toPlainString();
  }

  private static class DateRange {
    private LocalDate start;
    private LocalDate end;

    private DateRange(LocalDate start, LocalDate end) {
      this.start = start;
      this.end = end;
    }
  }

  private static class CostDay {
    private BigDecimal interestAmount = BigDecimal.ZERO;
    private BigDecimal storageAmount = BigDecimal.ZERO;
    private BigDecimal otherAmount = BigDecimal.ZERO;
    private BigDecimal totalAmount = BigDecimal.ZERO;
    private Set<String> funderNames = new LinkedHashSet<String>();
    private List<CostLine> lines = new ArrayList<CostLine>();
  }

  private static class CostLine {
    private String costType;
    private String costName;
    private String sourceNo;
    private String relatedName;
    private BigDecimal basisAmount;
    private BigDecimal amount;
    private String formula;
    private String remark;

    private CostLine(String costType, String costName, String sourceNo, String relatedName,
                     BigDecimal basisAmount, BigDecimal amount, String formula, String remark) {
      this.costType = costType;
      this.costName = costName;
      this.sourceNo = sourceNo;
      this.relatedName = relatedName;
      this.basisAmount = basisAmount == null ? BigDecimal.ZERO : basisAmount;
      this.amount = amount == null ? BigDecimal.ZERO : amount;
      this.formula = formula;
      this.remark = remark;
    }
  }
}
