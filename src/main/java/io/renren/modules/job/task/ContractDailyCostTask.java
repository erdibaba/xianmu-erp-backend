package io.renren.modules.job.task;

import io.renren.modules.erp.service.ErpContractDailyCostService;
import java.time.LocalDate;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 合同成本日报定时生成任务。
 */
@Component("contractDailyCostTask")
public class ContractDailyCostTask implements ITask {
  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Autowired
  private ErpContractDailyCostService erpContractDailyCostService;

  @Override
  public void run(String params) {
    LocalDate costDate = resolveCostDate(params);
    logger.info("合同成本日报生成开始，成本日期：{}", costDate);
    erpContractDailyCostService.generateSnapshot(costDate, null);
    logger.info("合同成本日报生成完成，成本日期：{}", costDate);
  }

  private LocalDate resolveCostDate(String params) {
    String value = StringUtils.defaultString(params).trim();
    if (StringUtils.isBlank(value) || "yesterday".equalsIgnoreCase(value)) {
      return LocalDate.now().minusDays(1);
    }
    if ("today".equalsIgnoreCase(value)) {
      return LocalDate.now();
    }
    return LocalDate.parse(value);
  }
}
