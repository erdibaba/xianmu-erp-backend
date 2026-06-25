package io.renren.modules.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.renren.common.utils.PageUtils;
import io.renren.common.utils.Query;
import io.renren.modules.erp.dao.ErpFunderBatchSettlementDao;
import io.renren.modules.erp.dao.ErpFunderBatchSettlementItemDao;
import io.renren.modules.erp.dao.ErpFunderLoanDao;
import io.renren.modules.erp.dao.ErpFunderLoanRepaymentDao;
import io.renren.modules.erp.dao.ErpFunderPaymentAllocationDao;
import io.renren.modules.erp.dao.ErpFunderPaymentDao;
import io.renren.modules.erp.dao.ErpInboundOrderDao;
import io.renren.modules.erp.dao.ErpPartnerDao;
import io.renren.modules.erp.dao.ErpPresaleAttachmentDao;
import io.renren.modules.erp.dao.ErpPresaleConfirmDao;
import io.renren.modules.erp.dao.ErpPresaleConfirmItemDao;
import io.renren.modules.erp.dao.ErpPresaleOrderDao;
import io.renren.modules.erp.dao.ErpPresalePackingDao;
import io.renren.modules.erp.dao.ErpPresalePackingItemDao;
import io.renren.modules.erp.dao.ErpSaleOrderDao;
import io.renren.modules.erp.dao.ErpSaleOrderItemDao;
import io.renren.modules.erp.dao.ErpSaleOutboundBatchDao;
import io.renren.modules.erp.dao.ErpSaleOutboundPlanItemDao;
import io.renren.modules.erp.dao.ErpSaleOutboundReceiptItemDao;
import io.renren.modules.erp.dao.ErpWarehouseDao;
import io.renren.modules.erp.dao.ErpWarehouseFeeRateDao;
import io.renren.modules.erp.entity.ErpFunderBatchSettlementEntity;
import io.renren.modules.erp.entity.ErpFunderBatchSettlementItemEntity;
import io.renren.modules.erp.entity.ErpFunderLoanEntity;
import io.renren.modules.erp.entity.ErpFunderLoanRepaymentEntity;
import io.renren.modules.erp.entity.ErpFunderPaymentAllocationEntity;
import io.renren.modules.erp.entity.ErpFunderPaymentEntity;
import io.renren.modules.erp.entity.ErpInboundOrderEntity;
import io.renren.modules.erp.entity.ErpPartnerEntity;
import io.renren.modules.erp.entity.ErpPresaleAttachmentEntity;
import io.renren.modules.erp.entity.ErpPresaleConfirmEntity;
import io.renren.modules.erp.entity.ErpPresaleConfirmItemEntity;
import io.renren.modules.erp.entity.ErpPresaleOrderEntity;
import io.renren.modules.erp.entity.ErpPresalePackingEntity;
import io.renren.modules.erp.entity.ErpPresalePackingItemEntity;
import io.renren.modules.erp.entity.ErpSaleOrderEntity;
import io.renren.modules.erp.entity.ErpSaleOrderItemEntity;
import io.renren.modules.erp.entity.ErpSaleOutboundBatchEntity;
import io.renren.modules.erp.entity.ErpSaleOutboundPlanItemEntity;
import io.renren.modules.erp.entity.ErpSaleOutboundReceiptItemEntity;
import io.renren.modules.erp.entity.ErpWarehouseEntity;
import io.renren.modules.erp.entity.ErpWarehouseFeeRateEntity;
import io.renren.modules.erp.service.ErpFunderFinanceService;
import io.renren.modules.erp.service.ErpOcrService;
import io.renren.modules.erp.vo.ErpRecognizeResultVo;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service("erpFunderFinanceService")
public class ErpFunderFinanceServiceImpl implements ErpFunderFinanceService {
  private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
  private static final BigDecimal DAYS_PER_YEAR = new BigDecimal("365");
  private static final BigDecimal DAYS_PER_YEAR_360 = new BigDecimal("360");
  private static final BigDecimal RATE_RUIHEXIANG = new BigDecimal("0.06");
  private static final BigDecimal RATE_CHAOYUE = new BigDecimal("0.06984");
  private static final BigDecimal RATE_WANXIANG = new BigDecimal("0.055");
  private static final int OUTBOUND_BATCH_CONFIRMED = 3;
  private static final String RULE_RUIHEXIANG = "RUIHEXIANG";
  private static final String RULE_CHAOYUE = "CHAOYUE";
  private static final String RULE_WANXIANG = "WANXIANG";
  private static final String REPAYMENT_SOURCE_MANUAL = "MANUAL";
  private static final String REPAYMENT_SOURCE_BATCH = "BATCH";
  private static final int PAYMENT_TYPE_FUNDER = 1;
  private static final int PAYMENT_TYPE_XIANMU = 2;
  private static final Pattern LABEL_AMOUNT_PATTERN = Pattern.compile(
      "(?:交易金额|付款金额|转账金额|打款金额|金额)\\s*[:：]?\\s*(?:CNY|RMB|￥|¥)?\\s*([0-9][0-9,]*(?:\\.[0-9]{1,2})?)",
      Pattern.CASE_INSENSITIVE);
  private static final Pattern CURRENCY_AMOUNT_PATTERN = Pattern.compile(
      "(?:CNY|RMB|￥|¥)\\s*([0-9][0-9,]*(?:\\.[0-9]{1,2})?)",
      Pattern.CASE_INSENSITIVE);
  private static final Pattern DATE_PATTERN = Pattern.compile(
      "(20\\d{2})\\s*[年/\\-.]\\s*(\\d{1,2})\\s*[月/\\-.]\\s*(\\d{1,2})\\s*日?");
  private static final Pattern COMPACT_DATE_PATTERN = Pattern.compile(
      "(20\\d{2})(\\d{2})(\\d{2})(?:\\s+\\d{1,2}\\s*:\\s*\\d{1,2}(?:\\s*:\\s*\\d{1,2})?)?");
  private static final Pattern DECIMAL_AMOUNT_PATTERN = Pattern.compile(
      "(?<!\\d)([0-9]{1,3}(?:,[0-9]{3})+\\.[0-9]{2})(?!\\d)");
  private static final Pattern OCR_DECIMAL_AMOUNT_PATTERN = Pattern.compile(
      "(?<!\\d)([0-9]{1,3}(?:\\s*[,，]\\s*[0-9]{3})+\\s*[.。]\\s*[0-9]{2}|[0-9]{4,12}\\s*[.。]\\s*[0-9]{2})(?!\\d)");

  @Autowired
  private ErpFunderPaymentDao paymentDao;
  @Autowired
  private ErpFunderPaymentAllocationDao allocationDao;
  @Autowired
  private ErpFunderLoanDao loanDao;
  @Autowired
  private ErpFunderLoanRepaymentDao repaymentDao;
  @Autowired
  private ErpFunderBatchSettlementDao batchSettlementDao;
  @Autowired
  private ErpFunderBatchSettlementItemDao batchSettlementItemDao;
  @Autowired
  private ErpPartnerDao partnerDao;
  @Autowired
  private ErpPresaleOrderDao presaleOrderDao;
  @Autowired
  private ErpPresaleConfirmDao presaleConfirmDao;
  @Autowired
  private ErpPresaleConfirmItemDao presaleConfirmItemDao;
  @Autowired
  private ErpPresalePackingDao presalePackingDao;
  @Autowired
  private ErpPresalePackingItemDao presalePackingItemDao;
  @Autowired
  private ErpPresaleAttachmentDao presaleAttachmentDao;
  @Autowired
  private ErpSaleOrderDao saleOrderDao;
  @Autowired
  private ErpSaleOrderItemDao saleOrderItemDao;
  @Autowired
  private ErpSaleOutboundBatchDao outboundBatchDao;
  @Autowired
  private ErpSaleOutboundPlanItemDao outboundPlanItemDao;
  @Autowired
  private ErpSaleOutboundReceiptItemDao outboundReceiptItemDao;
  @Autowired
  private ErpInboundOrderDao inboundOrderDao;
  @Autowired
  private ErpWarehouseDao warehouseDao;
  @Autowired
  private ErpWarehouseFeeRateDao warehouseFeeRateDao;
  @Autowired
  private ErpOcrService ocrService;

  @Override
  public PageUtils queryPaymentPage(Map<String, Object> params) {
    String keyword = stringValue(params.get("keyword"));
    String funderId = stringValue(params.get("funderId"));
    String paymentType = stringValue(params.get("paymentType"));
    QueryWrapper<ErpFunderPaymentEntity> wrapper = new QueryWrapper<ErpFunderPaymentEntity>()
        .orderByDesc("payment_date", "id");
    if (StringUtils.isNotBlank(keyword)) {
      wrapper.and(q -> q.like("payment_no", keyword)
          .or().like("payer_name", keyword)
          .or().like("funder_name", keyword));
    }
    if (StringUtils.isNotBlank(funderId)) {
      wrapper.eq("funder_id", Long.valueOf(funderId));
    }
    if (StringUtils.isNotBlank(paymentType)) {
      wrapper.eq("payment_type", Integer.valueOf(paymentType));
    }
    IPage<ErpFunderPaymentEntity> page = paymentDao.selectPage(
        new Query<ErpFunderPaymentEntity>().getPage(params), wrapper);
    fillSellerContractNos(page.getRecords());
    return new PageUtils(page);
  }

  private void fillSellerContractNos(List<ErpFunderPaymentEntity> paymentList) {
    if (paymentList == null || paymentList.isEmpty()) {
      return;
    }
    List<Long> paymentIds = new ArrayList<Long>();
    for (ErpFunderPaymentEntity payment : paymentList) {
      if (payment != null && payment.getId() != null) {
        paymentIds.add(payment.getId());
      }
    }
    if (paymentIds.isEmpty()) {
      return;
    }
    List<ErpFunderPaymentAllocationEntity> allocationList = allocationDao.selectList(
        new QueryWrapper<ErpFunderPaymentAllocationEntity>()
            .in("payment_id", paymentIds)
            .orderByAsc("id"));
    Map<Long, List<String>> contractMap = new HashMap<Long, List<String>>();
    for (ErpFunderPaymentAllocationEntity allocation : allocationList) {
      String contractNo = firstNonBlank(allocation == null ? null : allocation.getConfirmContractNo(),
          allocation == null ? null : allocation.getSellerContractNo());
      if (allocation == null || allocation.getPaymentId() == null || StringUtils.isBlank(contractNo)) {
        continue;
      }
      List<String> contracts = contractMap.get(allocation.getPaymentId());
      if (contracts == null) {
        contracts = new ArrayList<String>();
        contractMap.put(allocation.getPaymentId(), contracts);
      }
      if (!contracts.contains(contractNo)) {
        contracts.add(contractNo);
      }
    }
    for (ErpFunderPaymentEntity payment : paymentList) {
      List<String> contracts = payment == null ? null : contractMap.get(payment.getId());
      if (contracts != null && !contracts.isEmpty()) {
        payment.setSellerContractNos(StringUtils.join(contracts, ", "));
      }
    }
  }

  @Override
  public ErpFunderPaymentEntity getPaymentDetail(Long id) {
    ErpFunderPaymentEntity payment = paymentDao.selectById(id);
    if (payment != null) {
      List<ErpFunderPaymentAllocationEntity> allocationList = allocationDao.selectList(
          new QueryWrapper<ErpFunderPaymentAllocationEntity>()
              .eq("payment_id", id)
              .orderByAsc("id"));
      fillOrderContractAmount(allocationList);
      payment.setAllocationList(allocationList);
    }
    return payment;
  }

  @Override
  public List<ErpPartnerEntity> queryFunderOptions(String keyword) {
    QueryWrapper<ErpPartnerEntity> wrapper = new QueryWrapper<ErpPartnerEntity>()
        .eq("status", 1)
        .and(q -> q.like("business_role", "FUNDER"))
        .orderByAsc("partner_name")
        .last("limit 15");
    if (StringUtils.isNotBlank(keyword)) {
      wrapper.and(q -> q.like("partner_code", keyword).or().like("partner_name", keyword));
    }
    return partnerDao.selectList(wrapper);
  }

  @Override
  public List<ErpPartnerEntity> queryInternalPayerOptions(String keyword) {
    QueryWrapper<ErpPartnerEntity> wrapper = new QueryWrapper<ErpPartnerEntity>()
        .eq("status", 1)
        .and(q -> q.like("business_role", "INTERNAL"))
        .orderByAsc("partner_name")
        .last("limit 15");
    if (StringUtils.isNotBlank(keyword)) {
      wrapper.and(q -> q.like("partner_code", keyword).or().like("partner_name", keyword));
    }
    return partnerDao.selectList(wrapper);
  }

  @Override
  public List<ErpPresaleOrderEntity> queryPresaleOptions(String keyword) {
    QueryWrapper<ErpPresaleConfirmEntity> confirmWrapper = new QueryWrapper<ErpPresaleConfirmEntity>()
        .notInSql("id", "select confirm_id from erp_funder_payment_allocation where confirm_id is not null")
        .orderByDesc("expected_arrival_date", "id")
        .last("limit 15");
    if (StringUtils.isNotBlank(keyword)) {
      confirmWrapper.and(q -> q.like("contract_no", keyword)
          .or().like("container_no", keyword)
          .or().like("buyer_partner_name", keyword));
    }
    List<ErpPresaleConfirmEntity> confirmList = presaleConfirmDao.selectList(confirmWrapper);
    List<ErpPresaleOrderEntity> list = new ArrayList<ErpPresaleOrderEntity>();
    for (ErpPresaleConfirmEntity confirm : confirmList) {
      if (confirm == null || confirm.getPresaleOrderId() == null) {
        continue;
      }
      ErpPresaleOrderEntity presale = presaleOrderDao.selectById(confirm.getPresaleOrderId());
      if (presale == null) {
        continue;
      }
      presale.setConfirmInfo(confirm);
      list.add(presale);
    }
    return list;
  }

  @Override
  public Map<String, Object> recognizeVoucher(MultipartFile file) throws Exception {
    if (file == null || file.isEmpty()) {
      throw new RuntimeException("请上传银行打款凭证");
    }
    ErpRecognizeResultVo result = ocrService.recognize(file, "bank_payment_voucher");
    String rawText = result == null ? "" : StringUtils.defaultString(result.getRawText());
    Map<String, Object> receipt = extractBankReceipt(rawText);
    Map<String, Object> voucher = new HashMap<String, Object>();
    voucher.put("recognizedAmount", receipt.get("recognizedAmount") == null ? extractAmount(rawText) : receipt.get("recognizedAmount"));
    voucher.put("paymentDate", receipt.get("paymentDate") == null ? extractDate(rawText) : receipt.get("paymentDate"));
    voucher.put("filePath", result == null ? null : result.getSavedFilePath());
    voucher.put("fileName", StringUtils.defaultIfBlank(file.getOriginalFilename(), "银行打款凭证"));
    voucher.put("rawText", rawText);
    voucher.putAll(receipt);
    return voucher;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void confirmPayment(ErpFunderPaymentEntity payment, Long userId) {
    if (payment != null && payment.getId() != null) {
      updateXianmuPayment(payment);
      return;
    }
    validatePayment(payment);
    Date now = new Date();
    Integer paymentType = payment.getPaymentType() == null ? PAYMENT_TYPE_FUNDER : payment.getPaymentType();
    if (paymentType == PAYMENT_TYPE_FUNDER && paymentDao.selectCount(new QueryWrapper<ErpFunderPaymentEntity>()
        .eq("file_path", payment.getFilePath())) > 0) {
      throw new RuntimeException("该资方打款凭证已经确认，请勿重复提交");
    }
    ErpPartnerEntity funder = null;
    ErpPartnerEntity payer;
    if (paymentType == PAYMENT_TYPE_FUNDER) {
      funder = partnerDao.selectById(payment.getFunderId());
      payer = funder;
    } else if (paymentType == PAYMENT_TYPE_XIANMU) {
      payer = partnerDao.selectById(payment.getPayerId());
    } else {
      throw new RuntimeException("请选择正确的付款类型");
    }
    if (paymentType == PAYMENT_TYPE_XIANMU && (payer == null || payer.getStatus() == null || payer.getStatus() != 1
        || !StringUtils.contains(StringUtils.defaultString(payer.getBusinessRole()), "INTERNAL"))) {
      throw new RuntimeException("请选择启用状态的鲜牧付款主体");
    }
    if (paymentType == PAYMENT_TYPE_FUNDER && (funder == null || funder.getStatus() == null || funder.getStatus() != 1
        || !StringUtils.contains(StringUtils.defaultString(funder.getBusinessRole()), "FUNDER"))) {
      throw new RuntimeException("请选择启用状态的资方");
    }
    if (paymentType == PAYMENT_TYPE_FUNDER
        && (funder.getAnnualInterestRate() == null || funder.getAnnualInterestRate().compareTo(BigDecimal.ZERO) <= 0)) {
      throw new RuntimeException("所选资方未维护年利率，请先到往来单位维护资方年利率");
    }

    BigDecimal allocationTotal = BigDecimal.ZERO;
    boolean xianmuInstallmentsComplete = true;
    for (ErpFunderPaymentAllocationEntity allocation : payment.getAllocationList()) {
      ErpPresaleConfirmEntity confirm = loadConfirmForAllocation(allocation);
      if (paymentType == PAYMENT_TYPE_XIANMU) {
        allocation.setAllocationAmount(confirmTotalAmount(confirm.getId()));
      }
      allocationTotal = allocationTotal.add(money(allocation.getAllocationAmount()));
      if (paymentType == PAYMENT_TYPE_FUNDER) {
        validateXianmuContribution(allocation);
      }
    }
    if (paymentType == PAYMENT_TYPE_XIANMU) {
      xianmuInstallmentsComplete = prepareXianmuInstallments(payment, null);
    }
    if (allocationTotal.compareTo(money(payment.getModifiedAmount())) != 0) {
      throw new RuntimeException("预销售单分摊金额合计必须等于修改金额");
    }

    payment.setPaymentNo(number("PP"));
    payment.setPaymentType(paymentType);
    payment.setPayerId(payer.getId());
    payment.setPayerName(payer.getPartnerName());
    payment.setFunderId(funder == null ? null : funder.getId());
    payment.setFunderName(funder == null ? null : funder.getPartnerName());
    payment.setRecognizedAmount(money(payment.getRecognizedAmount()));
    payment.setModifiedAmount(money(payment.getModifiedAmount()));
    payment.setStatus(paymentType == PAYMENT_TYPE_XIANMU && !xianmuInstallmentsComplete ? 0 : 1);
    payment.setCreateUserId(userId);
    payment.setCreateTime(now);
    payment.setUpdateTime(now);
    paymentDao.insert(payment);

    for (ErpFunderPaymentAllocationEntity allocation : payment.getAllocationList()) {
      ErpPresaleConfirmEntity confirm = loadConfirmForAllocation(allocation);
      ErpPresaleOrderEntity presale = presaleOrderDao.selectById(confirm.getPresaleOrderId());
      if (presale == null) {
        throw new RuntimeException("确认函关联的预销售单不存在，请重新选择");
      }
      allocation.setPaymentId(payment.getId());
      allocation.setConfirmId(confirm.getId());
      allocation.setPresaleOrderId(presale.getId());
      allocation.setPresaleOrderNo(presale.getOrderNo());
      allocation.setSellerContractNo(presale.getSellerContractNo());
      allocation.setConfirmContractNo(confirm.getContractNo());
      allocation.setAllocationAmount(money(allocation.getAllocationAmount()));
      allocation.setXianmuContributionRecognizedAmount(money(allocation.getXianmuContributionRecognizedAmount()));
      allocation.setXianmuContributionModifiedAmount(money(allocation.getXianmuContributionModifiedAmount()));
      allocation.setXianmuDepositRecognizedAmount(money(allocation.getXianmuDepositRecognizedAmount()));
      allocation.setXianmuDepositModifiedAmount(money(allocation.getXianmuDepositModifiedAmount()));
      allocation.setXianmuBalanceRecognizedAmount(money(allocation.getXianmuBalanceRecognizedAmount()));
      allocation.setXianmuBalanceModifiedAmount(money(allocation.getXianmuBalanceModifiedAmount()));
      allocation.setCreateTime(now);
      allocation.setUpdateTime(now);
      allocationDao.insert(allocation);

      if (paymentType != PAYMENT_TYPE_FUNDER) {
        continue;
      }
      BigDecimal loanPrincipal = allocation.getAllocationAmount()
          .subtract(money(allocation.getXianmuContributionModifiedAmount()));
      if (loanPrincipal.compareTo(BigDecimal.ZERO) <= 0) {
        continue;
      }
      ErpFunderLoanEntity loan = new ErpFunderLoanEntity();
      loan.setLoanNo(number("FL"));
      loan.setPaymentId(payment.getId());
      loan.setAllocationId(allocation.getId());
      loan.setPresaleOrderId(presale.getId());
      loan.setConfirmId(confirm.getId());
      loan.setPresaleOrderNo(presale.getOrderNo());
      loan.setSellerContractNo(presale.getSellerContractNo());
      loan.setConfirmContractNo(confirm.getContractNo());
      loan.setFunderId(funder.getId());
      loan.setFunderName(funder.getPartnerName());
      loan.setLoanAmount(loanPrincipal);
      loan.setAnnualInterestRate(rate(funder.getAnnualInterestRate()));
      loan.setLoanDate(payment.getPaymentDate());
      loan.setRepaidPrincipal(money(BigDecimal.ZERO));
      loan.setRemainingPrincipal(loanPrincipal);
      loan.setInterestAmount(decimal2(BigDecimal.ZERO));
      loan.setStatus(0);
      loan.setCreateTime(now);
      loan.setUpdateTime(now);
      loanDao.insert(loan);
    }
  }

  @Override
  public PageUtils queryLoanPage(Map<String, Object> params) {
    String keyword = stringValue(params.get("keyword"));
    String status = stringValue(params.get("status"));
    String funderId = stringValue(params.get("funderId"));
    QueryWrapper<ErpFunderLoanEntity> wrapper = new QueryWrapper<ErpFunderLoanEntity>()
        .orderByAsc("status")
        .orderByDesc("loan_date", "id");
    if (StringUtils.isNotBlank(keyword)) {
      wrapper.and(q -> q.like("loan_no", keyword)
          .or().like("presale_order_no", keyword)
          .or().like("seller_contract_no", keyword)
          .or().like("confirm_contract_no", keyword)
          .or().like("funder_name", keyword));
    }
    if (StringUtils.isNotBlank(status)) {
      wrapper.eq("status", Integer.valueOf(status));
    }
    if (StringUtils.isNotBlank(funderId)) {
      wrapper.eq("funder_id", Long.valueOf(funderId));
    }
    IPage<ErpFunderLoanEntity> page = loanDao.selectPage(
        new Query<ErpFunderLoanEntity>().getPage(params), wrapper);
    return new PageUtils(page);
  }

  @Override
  public ErpFunderLoanEntity getLoanDetail(Long id) {
    ErpFunderLoanEntity loan = loanDao.selectById(id);
    if (loan != null) {
      loan.setRepaymentList(repaymentDao.selectList(
          new QueryWrapper<ErpFunderLoanRepaymentEntity>()
              .eq("loan_id", id)
              .orderByAsc("line_no", "id")));
    }
    return loan;
  }

  @Override
  public ErpFunderLoanRepaymentEntity calculateRepayment(ErpFunderLoanRepaymentEntity repayment) {
    if (repayment == null || repayment.getLoanId() == null) {
      throw new RuntimeException("请选择贷款记录");
    }
    ErpFunderLoanEntity loan = loanDao.selectById(repayment.getLoanId());
    if (loan == null) {
      throw new RuntimeException("贷款记录不存在");
    }
    return calculateRepayment(repayment, loan);
  }

  private ErpFunderLoanRepaymentEntity calculateRepayment(ErpFunderLoanRepaymentEntity repayment,
                                                           ErpFunderLoanEntity loan) {
    BigDecimal principal = money(repayment.getRepaymentPrincipal());
    if (principal.compareTo(BigDecimal.ZERO) <= 0) {
      throw new RuntimeException("本次归还本金必须大于0");
    }
    if (principal.compareTo(money(loan.getRemainingPrincipal())) > 0) {
      throw new RuntimeException("本次归还本金不能大于剩余待还本金");
    }
    if (repayment.getRepaymentDate() == null) {
      throw new RuntimeException("请选择还款日期");
    }
    int days = naturalDays(loan.getLoanDate(), repayment.getRepaymentDate());
    BigDecimal interest = principal
        .multiply(rate(loan.getAnnualInterestRate()))
        .divide(ONE_HUNDRED, 16, RoundingMode.HALF_UP)
        .divide(DAYS_PER_YEAR, 16, RoundingMode.HALF_UP)
        .multiply(new BigDecimal(days));
    BigDecimal handlingFee = money(repayment.getHandlingFeeAmount());
    if (handlingFee.compareTo(BigDecimal.ZERO) < 0) {
      throw new RuntimeException("手续费不能小于0");
    }
    if (handlingFee.compareTo(BigDecimal.ZERO) > 0 && StringUtils.isBlank(repayment.getHandlingFeeReason())) {
      throw new RuntimeException("填写手续费时必须填写手续费原因");
    }
    repayment.setRepaymentPrincipal(principal);
    repayment.setAnnualInterestRate(rate(loan.getAnnualInterestRate()));
    repayment.setLoanDays(days);
    repayment.setInterestAmount(decimal2(interest));
    repayment.setHandlingFeeAmount(handlingFee);
    repayment.setHandlingFeeReason(StringUtils.trimToNull(repayment.getHandlingFeeReason()));
    repayment.setExpectedPaymentAmount(decimal2(principal.add(interest).add(handlingFee)));
    if (repayment.getModifiedAmount() != null) {
      repayment.setAmountMatched(money(repayment.getModifiedAmount())
          .compareTo(money(repayment.getExpectedPaymentAmount())) == 0 ? 1 : 0);
    }
    return repayment;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void confirmRepayment(ErpFunderLoanRepaymentEntity repayment, Long userId) {
    if (StringUtils.isBlank(repayment.getFilePath())) {
      throw new RuntimeException("请先上传还款凭证");
    }
    if (repayment.getModifiedAmount() == null || repayment.getModifiedAmount().compareTo(BigDecimal.ZERO) <= 0) {
      throw new RuntimeException("修改金额必须大于0");
    }
    ErpFunderLoanEntity loan = loanDao.selectOne(new QueryWrapper<ErpFunderLoanEntity>()
        .eq("id", repayment.getLoanId())
        .last("for update"));
    if (loan == null) {
      throw new RuntimeException("贷款记录不存在");
    }
    if (loan.getStatus() != null && loan.getStatus() == 1) {
      throw new RuntimeException("该贷款已还款完成");
    }
    repayment = calculateRepayment(repayment, loan);
    Date now = new Date();
    Integer maxLine = repaymentDao.selectList(
        new QueryWrapper<ErpFunderLoanRepaymentEntity>()
            .eq("loan_id", loan.getId())
            .orderByDesc("line_no")
            .last("limit 1"))
        .stream().findFirst().map(ErpFunderLoanRepaymentEntity::getLineNo).orElse(0);
    repayment.setRepaymentNo(number("FR"));
    repayment.setLineNo(maxLine + 1);
    repayment.setRepaymentSource(REPAYMENT_SOURCE_MANUAL);
    repayment.setRecognizedAmount(money(repayment.getRecognizedAmount()));
    repayment.setModifiedAmount(money(repayment.getModifiedAmount()));
    repayment.setHandlingFeeAmount(money(repayment.getHandlingFeeAmount()));
    repayment.setHandlingFeeReason(StringUtils.trimToNull(repayment.getHandlingFeeReason()));
    repayment.setAmountMatched(repayment.getModifiedAmount()
        .compareTo(money(repayment.getExpectedPaymentAmount())) == 0 ? 1 : 0);
    repayment.setStatus(1);
    repayment.setCreateUserId(userId);
    repayment.setCreateTime(now);
    repayment.setUpdateTime(now);
    repaymentDao.insert(repayment);

    BigDecimal repaidPrincipal = money(loan.getRepaidPrincipal()).add(repayment.getRepaymentPrincipal());
    BigDecimal remainingPrincipal = money(loan.getLoanAmount()).subtract(repaidPrincipal);
    if (remainingPrincipal.compareTo(BigDecimal.ZERO) < 0) {
      throw new RuntimeException("累计归还本金不能大于贷款本金");
    }
    loan.setRepaidPrincipal(money(repaidPrincipal));
    loan.setRemainingPrincipal(money(remainingPrincipal));
    loan.setInterestAmount(decimal2(decimal2(loan.getInterestAmount()).add(repayment.getInterestAmount())));
    loan.setStatus(remainingPrincipal.compareTo(BigDecimal.ZERO) == 0 ? 1 : 0);
    loan.setUpdateTime(now);
    loanDao.updateById(loan);
  }

  @Override
  public List<ErpSaleOutboundBatchEntity> querySettleableOutboundBatches(String keyword) {
    QueryWrapper<ErpSaleOutboundBatchEntity> wrapper = new QueryWrapper<ErpSaleOutboundBatchEntity>()
        .and(q -> q.eq("funder_repayment_status", 1).or().like("ownership_name", "资方"))
        .notInSql("id", "select outbound_batch_id from erp_funder_batch_settlement")
        .orderByDesc("create_time", "id")
        .last("limit 80");
    if (StringUtils.isNotBlank(keyword)) {
      wrapper.and(q -> q.like("batch_no", keyword).or().like("ownership_name", keyword));
    }
    List<ErpSaleOutboundBatchEntity> batches = outboundBatchDao.selectList(wrapper);
    List<ErpSaleOutboundBatchEntity> result = new ArrayList<ErpSaleOutboundBatchEntity>();
    for (ErpSaleOutboundBatchEntity batch : batches) {
      ErpSaleOrderEntity order = batch == null ? null : saleOrderDao.selectById(batch.getSaleOrderId());
      if (order != null) {
        batch.setSaleOrderNo(order.getOrderNo());
      }
      String haystack = StringUtils.defaultString(batch == null ? null : batch.getBatchNo()) + " "
          + StringUtils.defaultString(batch == null ? null : batch.getOwnershipName()) + " "
          + StringUtils.defaultString(order == null ? null : order.getOrderNo()) + " "
          + StringUtils.defaultString(order == null ? null : order.getContractNo());
      if (StringUtils.isBlank(keyword) || StringUtils.containsIgnoreCase(haystack, keyword)) {
        result.add(batch);
      }
      if (result.size() >= 15) {
        break;
      }
    }
    return result;
  }

  @Override
  public ErpFunderBatchSettlementEntity calculateBatchSettlement(ErpFunderBatchSettlementEntity request) {
    if (request == null || request.getOutboundBatchId() == null) {
      throw new RuntimeException("请选择已确认的出库批次");
    }
    Date settlementDate = request.getSettlementDate() == null ? new Date() : request.getSettlementDate();
    ErpFunderBatchSettlementEntity settlement = buildBatchSettlement(request.getOutboundBatchId(), settlementDate);
    settlement.setIncludeCodeScanFee(request.getIncludeCodeScanFee() == null ? 0 : request.getIncludeCodeScanFee());
    if (RULE_CHAOYUE.equals(settlement.getRuleType())
        && money(request.getConfirmedPrincipalAmount()).compareTo(BigDecimal.ZERO) > 0) {
      redistributeConfirmedPrincipal(settlement, money(request.getConfirmedPrincipalAmount()));
    } else {
      reapplySettlementRuleAmounts(settlement);
    }
    applySettlementOverrideAmounts(settlement, request);
    recalcSettlementTotals(settlement);
    return settlement;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void confirmBatchSettlement(ErpFunderBatchSettlementEntity request, Long userId) {
    if (request == null || request.getOutboundBatchId() == null) {
      throw new RuntimeException("请选择已确认的出库批次");
    }
    if (batchSettlementDao.selectCount(new QueryWrapper<ErpFunderBatchSettlementEntity>()
        .eq("outbound_batch_id", request.getOutboundBatchId())) > 0) {
      throw new RuntimeException("该出库批次已生成资方结算，请勿重复确认");
    }
    if (request.getConfirmedPaymentAmount() == null
        || money(request.getConfirmedPaymentAmount()).compareTo(BigDecimal.ZERO) <= 0) {
      throw new RuntimeException("确认应付资方金额必须大于0");
    }
    if (StringUtils.isBlank(request.getFilePath())) {
      throw new RuntimeException("请先上传资方还款凭证");
    }
    ErpFunderBatchSettlementEntity settlement = calculateBatchSettlement(request);
    settlement.setConfirmedPaymentAmount(money(request.getConfirmedPaymentAmount()));
    Date now = new Date();
    settlement.setSettlementNo(number("FS"));
    settlement.setStatus(1);
    settlement.setRemark(StringUtils.trimToNull(request.getRemark()));
    settlement.setRecognizedPaymentAmount(money(request.getRecognizedPaymentAmount()));
    settlement.setFilePath(StringUtils.trimToNull(request.getFilePath()));
    settlement.setFileName(StringUtils.trimToNull(request.getFileName()));
    settlement.setRawText(request.getRawText());
    settlement.setCreateUserId(userId);
    settlement.setCreateTime(now);
    settlement.setUpdateTime(now);
    batchSettlementDao.insert(settlement);

    BigDecimal confirmedPaymentLeft = money(settlement.getConfirmedPaymentAmount());
    BigDecimal expectedTotal = money(settlement.getExpectedPaymentAmount());
    int lineNo = 1;
    for (ErpFunderBatchSettlementItemEntity item : settlement.getItemList()) {
      item.setSettlementId(settlement.getId());
      item.setLineNo(lineNo++);
      item.setCreateTime(now);
      item.setUpdateTime(now);
      batchSettlementItemDao.insert(item);

      ErpFunderLoanEntity loan = loanDao.selectOne(new QueryWrapper<ErpFunderLoanEntity>()
          .eq("id", item.getLoanId())
          .last("for update"));
      if (loan == null) {
        throw new RuntimeException("结算明细关联的贷款不存在");
      }
      BigDecimal principal = money(item.getConfirmedPrincipalAmount());
      if (principal.compareTo(money(loan.getRemainingPrincipal())) > 0) {
        throw new RuntimeException("确认还本金不能大于贷款剩余本金，合同号：" + item.getConfirmContractNo());
      }
      BigDecimal expected = money(item.getExpectedPaymentAmount());
      BigDecimal modified = lineNo > settlement.getItemList().size()
          ? confirmedPaymentLeft
          : money(settlement.getConfirmedPaymentAmount()).multiply(expected)
              .divide(expectedTotal.compareTo(BigDecimal.ZERO) <= 0 ? BigDecimal.ONE : expectedTotal, 8, RoundingMode.HALF_UP)
              .setScale(2, RoundingMode.HALF_UP);
      confirmedPaymentLeft = confirmedPaymentLeft.subtract(modified).setScale(2, RoundingMode.HALF_UP);

      ErpFunderLoanRepaymentEntity repayment = new ErpFunderLoanRepaymentEntity();
      repayment.setRepaymentNo(number("FR"));
      repayment.setLoanId(loan.getId());
      repayment.setSettlementId(settlement.getId());
      repayment.setSettlementItemId(item.getId());
      repayment.setOutboundBatchId(settlement.getOutboundBatchId());
      repayment.setOutboundBatchNo(settlement.getBatchNo());
      repayment.setRepaymentSource(REPAYMENT_SOURCE_BATCH);
      repayment.setLineNo(nextRepaymentLineNo(loan.getId()));
      repayment.setRepaymentPrincipal(principal);
      repayment.setAnnualInterestRate(rate(loan.getAnnualInterestRate()));
      repayment.setLoanDays(item.getLoanDays());
      repayment.setInterestAmount(money(item.getInterestAmount()));
      repayment.setHandlingFeeAmount(money(item.getHandlingFeeAmount())
          .add(money(item.getStorageFeeAmount()))
          .add(money(item.getCodeScanFeeAmount()))
          .add(money(item.getStampTaxAmount()))
          .add(money(item.getDepositAmount()))
          .add(money(item.getTaxAdjustAmount()))
          .add(money(item.getGrossWeightFeeAmount()))
          .add(money(item.getOtherFeeAmount())));
      repayment.setHandlingFeeReason("出库批次资方结算：" + settlement.getBatchNo());
      repayment.setExpectedPaymentAmount(expected);
      repayment.setRecognizedAmount(money(settlement.getRecognizedPaymentAmount()));
      repayment.setModifiedAmount(modified);
      repayment.setRepaymentDate(settlement.getSettlementDate());
      repayment.setAmountMatched(settlement.getConfirmedPaymentAmount().compareTo(settlement.getExpectedPaymentAmount()) == 0 ? 1 : 0);
      repayment.setFilePath(settlement.getFilePath());
      repayment.setFileName(settlement.getFileName());
      repayment.setRawText(settlement.getRawText());
      repayment.setStatus(1);
      repayment.setCreateUserId(userId);
      repayment.setCreateTime(now);
      repayment.setUpdateTime(now);
      repaymentDao.insert(repayment);

      BigDecimal repaidPrincipal = money(loan.getRepaidPrincipal()).add(principal);
      BigDecimal remainingPrincipal = money(loan.getLoanAmount()).subtract(repaidPrincipal);
      if (remainingPrincipal.compareTo(BigDecimal.ZERO) < 0) {
        throw new RuntimeException("累计归还本金不能大于贷款本金，合同号：" + item.getConfirmContractNo());
      }
      loan.setRepaidPrincipal(money(repaidPrincipal));
      loan.setRemainingPrincipal(money(remainingPrincipal));
      loan.setInterestAmount(decimal2(decimal2(loan.getInterestAmount()).add(money(item.getInterestAmount()))));
      loan.setStatus(remainingPrincipal.compareTo(BigDecimal.ZERO) == 0 ? 1 : 0);
      loan.setUpdateTime(now);
      loanDao.updateById(loan);
    }
    ErpSaleOutboundBatchEntity batch = outboundBatchDao.selectById(settlement.getOutboundBatchId());
    if (batch != null) {
      batch.setFunderRepaymentStatus(2);
      batch.setUpdateTime(now);
      outboundBatchDao.updateById(batch);
    }
  }

  private ErpFunderBatchSettlementEntity buildBatchSettlement(Long outboundBatchId, Date settlementDate) {
    ErpSaleOutboundBatchEntity batch = outboundBatchDao.selectById(outboundBatchId);
    if (batch == null) {
      throw new RuntimeException("出库批次不存在");
    }
    if (StringUtils.isBlank(batch.getOwnershipName()) || !StringUtils.contains(batch.getOwnershipName(), "资方")) {
      throw new RuntimeException("只有资方货权的出库批次才需要生成资方还款");
    }
    ErpSaleOrderEntity saleOrder = saleOrderDao.selectById(batch.getSaleOrderId());
    if (saleOrder == null) {
      throw new RuntimeException("出库批次关联的销售单不存在");
    }
    List<ErpSaleOutboundPlanItemEntity> planItems = outboundPlanItemDao.selectList(
        new QueryWrapper<ErpSaleOutboundPlanItemEntity>().eq("batch_id", outboundBatchId).orderByAsc("line_no", "id"));
    if (planItems == null || planItems.isEmpty()) {
      throw new RuntimeException("出库批次没有计划货物，不能生成资方还款");
    }

    ErpFunderBatchSettlementEntity settlement = new ErpFunderBatchSettlementEntity();
    settlement.setOutboundBatchId(batch.getId());
    settlement.setSaleOrderId(batch.getSaleOrderId());
    settlement.setBatchNo(batch.getBatchNo());
    settlement.setSaleOrderNo(saleOrder.getOrderNo());
    settlement.setOwnershipName(batch.getOwnershipName());
    settlement.setSettlementDate(settlementDate);
    settlement.setIncludeCodeScanFee(0);
    List<ErpFunderBatchSettlementItemEntity> items = new ArrayList<ErpFunderBatchSettlementItemEntity>();
    Long funderId = null;
    String funderName = null;
    String ruleType = null;
    int lineNo = 1;
    for (ErpSaleOutboundPlanItemEntity plan : planItems) {
      int plannedBoxes = plan.getPlannedBoxes() == null ? 0 : plan.getPlannedBoxes();
      if (plannedBoxes <= 0) {
        continue;
      }
      ErpSaleOrderItemEntity saleItem = saleOrderItemDao.selectById(plan.getSaleOrderItemId());
      if (saleItem == null) {
        throw new RuntimeException("出库计划关联的销售单明细不存在：" + plan.getProductCode());
      }
      ErpInboundOrderEntity inbound = saleItem.getSourceInboundOrderId() == null ? null : inboundOrderDao.selectById(saleItem.getSourceInboundOrderId());
      if (inbound == null || inbound.getConfirmId() == null) {
        throw new RuntimeException("产品" + plan.getProductCode() + "未追溯到客户订单确认函，不能生成资方还款");
      }
      ErpFunderLoanEntity loan = loanDao.selectOne(new QueryWrapper<ErpFunderLoanEntity>()
          .eq("confirm_id", inbound.getConfirmId())
          .eq("status", 0)
          .last("limit 1"));
      if (loan == null) {
        throw new RuntimeException("确认函合同未找到待还资方贷款：" + inbound.getContractNo());
      }
      if (funderId == null) {
        funderId = loan.getFunderId();
        funderName = loan.getFunderName();
        ruleType = resolveFunderRuleType(loan.getFunderId(), loan.getFunderName());
      } else if (!funderId.equals(loan.getFunderId())) {
        throw new RuntimeException("一个出库批次只能结算同一个资方货权");
      }

      ErpPresaleConfirmEntity confirm = presaleConfirmDao.selectById(inbound.getConfirmId());
      ErpPresaleConfirmItemEntity confirmItem = findConfirmItem(inbound.getConfirmId(), plan.getProductId());
      PackingWeight packingWeight = resolvePackingWeight(inbound.getConfirmId(), plan);
      BigDecimal feeWeight = decimal3(packingWeight.avgWeight.multiply(new BigDecimal(plannedBoxes)));
      BigDecimal unitPrice = confirmItem == null ? BigDecimal.ZERO : decimal6(confirmItem.getUnitPriceInclTax());
      BigDecimal costAmount = money(feeWeight.multiply(unitPrice));
      BigDecimal confirmAmount = confirm == null ? BigDecimal.ZERO : money(confirm.getTotalAmount());
      BigDecimal principal = confirmAmount.compareTo(BigDecimal.ZERO) <= 0
          ? BigDecimal.ZERO
          : money(costAmount.multiply(money(loan.getLoanAmount())).divide(confirmAmount, 8, RoundingMode.HALF_UP));
      if (principal.compareTo(money(loan.getRemainingPrincipal())) > 0) {
        principal = money(loan.getRemainingPrincipal());
      }

      ErpFunderBatchSettlementItemEntity item = new ErpFunderBatchSettlementItemEntity();
      item.setOutboundBatchId(outboundBatchId);
      item.setPlanItemId(plan.getId());
      item.setSaleOrderItemId(plan.getSaleOrderItemId());
      item.setLoanId(loan.getId());
      item.setConfirmId(inbound.getConfirmId());
      item.setConfirmContractNo(loan.getConfirmContractNo());
      item.setLineNo(lineNo++);
      item.setProductId(plan.getProductId());
      item.setProductCode(plan.getProductCode());
      item.setProductName(plan.getProductName());
      item.setContainerNo(plan.getContainerNo());
      item.setFactoryNo(plan.getFactoryNo());
      item.setShippedBoxes(plannedBoxes);
      item.setShippedWeight(feeWeight);
      item.setFeeWeight(feeWeight);
      item.setPackingTotalBoxes(packingWeight.totalBoxes);
      item.setPackingTotalWeight(packingWeight.totalWeight);
      item.setPackingAvgWeight(packingWeight.avgWeight);
      item.setUnitPriceInclTax(unitPrice);
      item.setCostAmount(costAmount);
      item.setSystemPrincipalAmount(principal);
      item.setConfirmedPrincipalAmount(principal);
      item.setLoanDays(calcLoanDays(ruleType, loan.getLoanDate(), settlementDate));
      applyRuleAmounts(item, ruleType, loan, confirm, saleItem, settlementDate, false);
      items.add(item);
    }
    if (items.isEmpty()) {
      throw new RuntimeException("出库批次没有可结算的计划出库明细");
    }
    settlement.setFunderId(funderId);
    settlement.setFunderName(funderName);
    settlement.setRuleType(ruleType);
    settlement.setItemList(items);
    recalcSettlementTotals(settlement);
    settlement.setRecognizedPaymentAmount(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
    settlement.setConfirmedPaymentAmount(settlement.getExpectedPaymentAmount());
    return settlement;
  }

  private Map<Long, ActualOutbound> sumActualByPlan(List<ErpSaleOutboundReceiptItemEntity> actualItems) {
    Map<Long, ActualOutbound> result = new HashMap<Long, ActualOutbound>();
    for (ErpSaleOutboundReceiptItemEntity actual : actualItems) {
      if (actual == null || actual.getPlanItemId() == null) {
        continue;
      }
      ActualOutbound summary = result.get(actual.getPlanItemId());
      if (summary == null) {
        summary = new ActualOutbound();
        result.put(actual.getPlanItemId(), summary);
      }
      summary.boxes += actual.getShippedQty() == null ? 0 : actual.getShippedQty();
      summary.weight = summary.weight.add(decimal3(actual.getTotalWeight()));
    }
    return result;
  }

  private ErpPresaleConfirmItemEntity findConfirmItem(Long confirmId, Long productId) {
    if (confirmId == null || productId == null) {
      return null;
    }
    return presaleConfirmItemDao.selectOne(new QueryWrapper<ErpPresaleConfirmItemEntity>()
        .eq("confirm_id", confirmId)
        .eq("product_id", productId)
        .last("limit 1"));
  }

  private PackingWeight resolvePackingWeight(Long confirmId, ErpSaleOutboundPlanItemEntity plan) {
    List<ErpPresalePackingEntity> packings = presalePackingDao.selectList(
        new QueryWrapper<ErpPresalePackingEntity>()
            .eq("confirm_id", confirmId)
            .orderByDesc("id"));
    if (packings == null || packings.isEmpty()) {
      throw new RuntimeException("确认函未上传装箱单，不能计算资方计费重量：" + plan.getProductCode());
    }
    BigDecimal totalWeight = BigDecimal.ZERO;
    int totalBoxes = 0;
    for (ErpPresalePackingEntity packing : packings) {
      QueryWrapper<ErpPresalePackingItemEntity> wrapper = new QueryWrapper<ErpPresalePackingItemEntity>()
          .eq("packing_id", packing.getId())
          .eq("product_id", plan.getProductId());
      if (StringUtils.isNotBlank(plan.getFactoryNo())) {
        wrapper.eq("factory_no", plan.getFactoryNo());
      }
      List<ErpPresalePackingItemEntity> items = presalePackingItemDao.selectList(wrapper);
      for (ErpPresalePackingItemEntity item : items) {
        totalBoxes += item.getTotalBoxes() == null ? 0 : item.getTotalBoxes();
        totalWeight = totalWeight.add(decimal3(item.getTotalWeight()));
      }
    }
    if (totalBoxes <= 0 || totalWeight.compareTo(BigDecimal.ZERO) <= 0) {
      throw new RuntimeException("装箱单未匹配到产品重量/箱数，不能计算资方计费重量：" + plan.getProductCode());
    }
    PackingWeight weight = new PackingWeight();
    weight.totalBoxes = totalBoxes;
    weight.totalWeight = decimal3(totalWeight);
    weight.avgWeight = totalWeight.divide(new BigDecimal(totalBoxes), 6, RoundingMode.HALF_UP);
    return weight;
  }

  private void applyRuleAmounts(ErpFunderBatchSettlementItemEntity item, String ruleType,
                                ErpFunderLoanEntity loan, ErpPresaleConfirmEntity confirm,
                                ErpSaleOrderItemEntity saleItem, Date settlementDate,
                                boolean includeCodeScanFee) {
    BigDecimal principal = money(item.getConfirmedPrincipalAmount());
    BigDecimal interest = BigDecimal.ZERO;
    if (RULE_RUIHEXIANG.equals(ruleType)) {
      interest = principal.multiply(RATE_RUIHEXIANG)
          .divide(DAYS_PER_YEAR, 8, RoundingMode.HALF_UP)
          .multiply(new BigDecimal(item.getLoanDays()));
    } else if (RULE_CHAOYUE.equals(ruleType)) {
      interest = principal.multiply(RATE_CHAOYUE)
          .divide(DAYS_PER_YEAR_360, 8, RoundingMode.HALF_UP)
          .multiply(new BigDecimal(item.getLoanDays()));
    } else if (RULE_WANXIANG.equals(ruleType)) {
      interest = principal.multiply(RATE_WANXIANG)
          .divide(DAYS_PER_YEAR_360, 8, RoundingMode.HALF_UP)
          .multiply(new BigDecimal(item.getLoanDays()));
      applyWanxiangFeeAmounts(item, confirm, saleItem, settlementDate, includeCodeScanFee);
    } else {
      interest = principal.multiply(rate(loan.getAnnualInterestRate()))
          .divide(ONE_HUNDRED, 8, RoundingMode.HALF_UP)
          .divide(DAYS_PER_YEAR, 8, RoundingMode.HALF_UP)
          .multiply(new BigDecimal(item.getLoanDays()));
    }
    if (!RULE_WANXIANG.equals(ruleType)) {
      item.setCodeScanFeeAmount(includeCodeScanFee
          ? calcCodeScanFee(saleItem, item, settlementDate)
          : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
    }
    item.setInterestAmount(money(interest));
    item.setExpectedPaymentAmount(calcItemExpectedPayment(item));
  }

  private void applyWanxiangFeeAmounts(ErpFunderBatchSettlementItemEntity item,
                                       ErpPresaleConfirmEntity confirm,
                                       ErpSaleOrderItemEntity saleItem,
                                       Date settlementDate,
                                       boolean includeCodeScanFee) {
    String coldFreshType = confirm == null ? "" : StringUtils.defaultString(confirm.getColdFreshType());
    boolean frozen = StringUtils.contains(coldFreshType, "冻");
    ErpWarehouseFeeRateEntity rate = loadWarehouseRate(saleItem == null ? null : saleItem.getWarehouseId(), settlementDate);
    BigDecimal weightTon = decimal3(item.getShippedWeight()).divide(new BigDecimal("1000"), 8, RoundingMode.HALF_UP);
    BigDecimal storageRate = rate == null ? BigDecimal.ZERO
        : money(frozen ? rate.getFrozenStorageFee() : rate.getChilledStorageFee());
    BigDecimal handlingRate = rate == null ? BigDecimal.ZERO
        : money(frozen ? rate.getFrozenColdFee() : rate.getChilledColdFee());
    item.setStorageFeeAmount(money(storageRate.multiply(weightTon)));
    item.setHandlingFeeAmount(money(handlingRate.multiply(weightTon)));
    item.setCodeScanFeeAmount(includeCodeScanFee ? calcCodeScanFee(saleItem, item, settlementDate) : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
    item.setStampTaxAmount(money(decimal3(item.getShippedWeight()).multiply(decimal6(item.getUnitPriceInclTax())).multiply(new BigDecimal("0.0006"))));
    item.setDepositAmount(money(item.getCostAmount().multiply(new BigDecimal("0.25"))));
    item.setTaxAdjustAmount(money(item.getTaxAdjustAmount()));
    item.setGrossWeightFeeAmount(calcGrossWeightFee(item, confirm, saleItem, settlementDate));
    item.setOtherFeeAmount(money(item.getOtherFeeAmount()));
  }

  private BigDecimal calcCodeScanFee(ErpSaleOrderItemEntity saleItem,
                                     ErpFunderBatchSettlementItemEntity item,
                                     Date settlementDate) {
    if (saleItem == null || saleItem.getWarehouseId() == null) {
      return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }
    ErpWarehouseEntity warehouse = warehouseDao.selectById(saleItem.getWarehouseId());
    if (warehouse == null || warehouse.getScanFeeEnabled() == null || warehouse.getScanFeeEnabled() != 1) {
      return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }
    ErpWarehouseFeeRateEntity rate = loadWarehouseRate(saleItem.getWarehouseId(), settlementDate);
    if (rate == null || rate.getScanFeeRate() == null || rate.getScanFeeRate().compareTo(BigDecimal.ZERO) <= 0) {
      return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }
    if ("BOX".equals(rate.getScanFeeUnit())) {
      return money(rate.getScanFeeRate().multiply(new BigDecimal(item.getShippedBoxes() == null ? 0 : item.getShippedBoxes())));
    }
    BigDecimal weightTon = decimal3(item.getFeeWeight() == null ? item.getShippedWeight() : item.getFeeWeight())
        .divide(new BigDecimal("1000"), 8, RoundingMode.HALF_UP);
    return money(rate.getScanFeeRate().multiply(weightTon));
  }

  private BigDecimal calcGrossWeightFee(ErpFunderBatchSettlementItemEntity item,
                                        ErpPresaleConfirmEntity confirm,
                                        ErpSaleOrderItemEntity saleItem,
                                        Date settlementDate) {
    if (!shouldCalcGrossWeightFee(saleItem)) {
      item.setCustomsGrossWeight(null);
      item.setGrossDiffWeight(null);
      item.setGrossFeeDays(null);
      item.setGrossFeeRate(null);
      return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }
    BigDecimal customsGrossWeight = loadCustomsGrossWeight(confirm == null ? null : confirm.getId());
    PackingWeight confirmPacking = resolveConfirmPackingWeight(confirm == null ? null : confirm.getId());
    if (customsGrossWeight.compareTo(BigDecimal.ZERO) <= 0) {
      throw new RuntimeException("报关单未维护确认毛重，不能计算毛重费用：" + item.getConfirmContractNo());
    }
    if (confirmPacking.totalWeight.compareTo(BigDecimal.ZERO) <= 0) {
      throw new RuntimeException("装箱单整单总重量为空，不能计算毛重费用：" + item.getConfirmContractNo());
    }
    BigDecimal productNetWeight = decimal3(item.getPackingTotalWeight());
    int productBoxes = item.getPackingTotalBoxes() == null ? 0 : item.getPackingTotalBoxes();
    if (productBoxes <= 0 || productNetWeight.compareTo(BigDecimal.ZERO) <= 0) {
      throw new RuntimeException("装箱单产品总重量/总箱数为空，不能计算毛重费用：" + item.getProductCode());
    }
    BigDecimal productGrossWeight = productNetWeight
        .divide(confirmPacking.totalWeight, 10, RoundingMode.HALF_UP)
        .multiply(customsGrossWeight);
    BigDecimal grossPerBox = productGrossWeight.divide(new BigDecimal(productBoxes), 6, RoundingMode.HALF_UP);
    BigDecimal currentGrossWeight = decimal3(grossPerBox.multiply(new BigDecimal(item.getShippedBoxes() == null ? 0 : item.getShippedBoxes())));
    boolean frozen = isFrozen(confirm == null ? null : confirm.getColdFreshType());
    BigDecimal rate = frozen ? new BigDecimal("2.10") : new BigDecimal("2.80");
    int days = calcGrossFeeDays(saleItem == null ? null : saleItem.getInboundDate(), settlementDate, frozen);
    BigDecimal fee = currentGrossWeight.divide(new BigDecimal("1000"), 8, RoundingMode.HALF_UP)
        .multiply(rate)
        .multiply(new BigDecimal(days));
    item.setCustomsGrossWeight(decimal3(customsGrossWeight));
    item.setGrossDiffWeight(currentGrossWeight);
    item.setGrossFeeDays(days);
    item.setGrossFeeRate(rate);
    return money(fee);
  }

  private boolean shouldCalcGrossWeightFee(ErpSaleOrderItemEntity saleItem) {
    String text = StringUtils.defaultString(saleItem == null ? null : saleItem.getWarehouseName()) + " "
        + StringUtils.defaultString(saleItem == null ? null : saleItem.getContractPortCold());
    return StringUtils.contains(text, "上海万呈") || StringUtils.contains(text, "万纬");
  }

  private boolean isFrozen(String coldFreshType) {
    return StringUtils.contains(StringUtils.defaultString(coldFreshType), "冻");
  }

  private int calcGrossFeeDays(Date inboundDate, Date settlementDate, boolean frozen) {
    if (inboundDate == null || settlementDate == null) {
      throw new RuntimeException("入库日期和结算日期不能为空，不能计算毛重费用");
    }
    LocalDate inbound = inboundDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    LocalDate settlement = settlementDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    int actualDays = Math.toIntExact(ChronoUnit.DAYS.between(inbound, settlement)) + 1;
    if (actualDays < 1) {
      actualDays = 1;
    }
    return Math.max(actualDays, frozen ? 20 : 5);
  }

  private BigDecimal loadCustomsGrossWeight(Long confirmId) {
    if (confirmId == null) {
      return BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
    }
    List<ErpPresaleAttachmentEntity> attachments = presaleAttachmentDao.selectList(
        new QueryWrapper<ErpPresaleAttachmentEntity>()
            .eq("confirm_id", confirmId)
            .eq("attachment_type", "CUSTOMS")
            .orderByDesc("id"));
    BigDecimal total = BigDecimal.ZERO;
    for (ErpPresaleAttachmentEntity attachment : attachments) {
      total = total.add(decimal3(attachment.getConfirmedGrossWeight()));
    }
    return decimal3(total);
  }

  private PackingWeight resolveConfirmPackingWeight(Long confirmId) {
    PackingWeight result = new PackingWeight();
    if (confirmId == null) {
      return result;
    }
    List<ErpPresalePackingEntity> packings = presalePackingDao.selectList(
        new QueryWrapper<ErpPresalePackingEntity>().eq("confirm_id", confirmId));
    for (ErpPresalePackingEntity packing : packings) {
      result.totalBoxes += packing.getTotalBoxes() == null ? 0 : packing.getTotalBoxes();
      result.totalWeight = result.totalWeight.add(decimal3(packing.getTotalWeight()));
    }
    if (result.totalBoxes > 0) {
      result.avgWeight = result.totalWeight.divide(new BigDecimal(result.totalBoxes), 6, RoundingMode.HALF_UP);
    }
    return result;
  }

  private ErpWarehouseFeeRateEntity loadWarehouseRate(Long warehouseId, Date businessDate) {
    if (warehouseId == null) {
      return null;
    }
    return warehouseFeeRateDao.selectOne(new QueryWrapper<ErpWarehouseFeeRateEntity>()
        .eq("warehouse_id", warehouseId)
        .eq("status", 1)
        .le("effective_date", businessDate)
        .orderByDesc("effective_date", "id")
        .last("limit 1"));
  }

  private void redistributeConfirmedPrincipal(ErpFunderBatchSettlementEntity settlement, BigDecimal confirmedTotal) {
    BigDecimal systemTotal = BigDecimal.ZERO;
    for (ErpFunderBatchSettlementItemEntity item : settlement.getItemList()) {
      systemTotal = systemTotal.add(money(item.getSystemPrincipalAmount()));
    }
    if (confirmedTotal.compareTo(systemTotal) > 0) {
      throw new RuntimeException("资方认定回款本金不能大于系统理论还本金");
    }
    BigDecimal allocated = BigDecimal.ZERO;
    List<ErpFunderBatchSettlementItemEntity> items = settlement.getItemList();
    for (int i = 0; i < items.size(); i++) {
      ErpFunderBatchSettlementItemEntity item = items.get(i);
      BigDecimal value = i == items.size() - 1
          ? confirmedTotal.subtract(allocated)
          : confirmedTotal.multiply(money(item.getSystemPrincipalAmount()))
              .divide(systemTotal.compareTo(BigDecimal.ZERO) <= 0 ? BigDecimal.ONE : systemTotal, 8, RoundingMode.HALF_UP)
              .setScale(2, RoundingMode.HALF_UP);
      allocated = allocated.add(value);
      item.setConfirmedPrincipalAmount(money(value));
      ErpFunderLoanEntity loan = loanDao.selectById(item.getLoanId());
      ErpPresaleConfirmEntity confirm = item.getConfirmId() == null ? null : presaleConfirmDao.selectById(item.getConfirmId());
      ErpSaleOrderItemEntity saleItem = item.getSaleOrderItemId() == null ? null : saleOrderItemDao.selectById(item.getSaleOrderItemId());
      applyRuleAmounts(item, settlement.getRuleType(), loan, confirm, saleItem, settlement.getSettlementDate(),
          Integer.valueOf(1).equals(settlement.getIncludeCodeScanFee()));
    }
  }

  private void reapplySettlementRuleAmounts(ErpFunderBatchSettlementEntity settlement) {
    if (settlement == null || settlement.getItemList() == null) {
      return;
    }
    for (ErpFunderBatchSettlementItemEntity item : settlement.getItemList()) {
      ErpFunderLoanEntity loan = loanDao.selectById(item.getLoanId());
      ErpPresaleConfirmEntity confirm = item.getConfirmId() == null ? null : presaleConfirmDao.selectById(item.getConfirmId());
      ErpSaleOrderItemEntity saleItem = item.getSaleOrderItemId() == null ? null : saleOrderItemDao.selectById(item.getSaleOrderItemId());
      applyRuleAmounts(item, settlement.getRuleType(), loan, confirm, saleItem, settlement.getSettlementDate(),
          Integer.valueOf(1).equals(settlement.getIncludeCodeScanFee()));
    }
  }

  private void applySettlementOverrideAmounts(ErpFunderBatchSettlementEntity settlement, ErpFunderBatchSettlementEntity request) {
    settlement.setIncludeCodeScanFee(request.getIncludeCodeScanFee() == null ? 0 : request.getIncludeCodeScanFee());
    settlement.setTaxAdjustAmount(money(request.getTaxAdjustAmount()));
    settlement.setGrossWeightFeeAmount(money(request.getGrossWeightFeeAmount()));
    settlement.setOtherFeeAmount(money(request.getOtherFeeAmount()));
    settlement.setRecognizedPaymentAmount(money(request.getRecognizedPaymentAmount()));
    settlement.setFilePath(StringUtils.trimToNull(request.getFilePath()));
    settlement.setFileName(StringUtils.trimToNull(request.getFileName()));
    settlement.setRawText(request.getRawText());
    if (request.getConfirmedPaymentAmount() != null && money(request.getConfirmedPaymentAmount()).compareTo(BigDecimal.ZERO) > 0) {
      settlement.setConfirmedPaymentAmount(money(request.getConfirmedPaymentAmount()));
    }
  }

  private void recalcSettlementTotals(ErpFunderBatchSettlementEntity settlement) {
    BigDecimal systemPrincipal = BigDecimal.ZERO;
    BigDecimal confirmedPrincipal = BigDecimal.ZERO;
    BigDecimal interest = BigDecimal.ZERO;
    BigDecimal storage = BigDecimal.ZERO;
    BigDecimal handling = BigDecimal.ZERO;
    BigDecimal codeScan = BigDecimal.ZERO;
    BigDecimal stampTax = BigDecimal.ZERO;
    BigDecimal deposit = BigDecimal.ZERO;
    BigDecimal taxAdjust = money(settlement.getTaxAdjustAmount());
    BigDecimal gross = money(settlement.getGrossWeightFeeAmount());
    BigDecimal other = money(settlement.getOtherFeeAmount());
    BigDecimal expected = BigDecimal.ZERO;
    boolean includeCodeScanFee = Integer.valueOf(1).equals(settlement.getIncludeCodeScanFee());
    for (ErpFunderBatchSettlementItemEntity item : settlement.getItemList()) {
      if (!includeCodeScanFee) {
        item.setCodeScanFeeAmount(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
      }
      item.setExpectedPaymentAmount(calcItemExpectedPayment(item));
      systemPrincipal = systemPrincipal.add(money(item.getSystemPrincipalAmount()));
      confirmedPrincipal = confirmedPrincipal.add(money(item.getConfirmedPrincipalAmount()));
      interest = interest.add(money(item.getInterestAmount()));
      storage = storage.add(money(item.getStorageFeeAmount()));
      handling = handling.add(money(item.getHandlingFeeAmount()));
      codeScan = codeScan.add(money(item.getCodeScanFeeAmount()));
      stampTax = stampTax.add(money(item.getStampTaxAmount()));
      deposit = deposit.add(money(item.getDepositAmount()));
      expected = expected.add(money(item.getExpectedPaymentAmount()));
    }
    expected = expected.add(taxAdjust).add(gross).add(other);
    settlement.setSystemPrincipalAmount(money(systemPrincipal));
    settlement.setConfirmedPrincipalAmount(money(confirmedPrincipal));
    settlement.setInterestAmount(money(interest));
    settlement.setStorageFeeAmount(money(storage));
    settlement.setHandlingFeeAmount(money(handling));
    settlement.setCodeScanFeeAmount(money(codeScan));
    settlement.setStampTaxAmount(money(stampTax));
    settlement.setDepositAmount(money(deposit));
    settlement.setTaxAdjustAmount(taxAdjust);
    settlement.setGrossWeightFeeAmount(gross);
    settlement.setOtherFeeAmount(other);
    settlement.setExpectedPaymentAmount(money(expected));
    if (settlement.getConfirmedPaymentAmount() == null || money(settlement.getConfirmedPaymentAmount()).compareTo(BigDecimal.ZERO) <= 0) {
      settlement.setConfirmedPaymentAmount(settlement.getExpectedPaymentAmount());
    }
  }

  private BigDecimal calcItemExpectedPayment(ErpFunderBatchSettlementItemEntity item) {
    return money(item.getConfirmedPrincipalAmount())
        .add(money(item.getInterestAmount()))
        .add(money(item.getStorageFeeAmount()))
        .add(money(item.getHandlingFeeAmount()))
        .add(money(item.getCodeScanFeeAmount()))
        .add(money(item.getStampTaxAmount()))
        .add(money(item.getDepositAmount()))
        .add(money(item.getTaxAdjustAmount()))
        .add(money(item.getGrossWeightFeeAmount()))
        .add(money(item.getOtherFeeAmount()))
        .setScale(2, RoundingMode.HALF_UP);
  }

  private int calcLoanDays(String ruleType, Date loanDate, Date settlementDate) {
    if (loanDate == null || settlementDate == null) {
      throw new RuntimeException("贷款日期和结算日期不能为空");
    }
    LocalDate loan = loanDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    LocalDate settlement = settlementDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    if (settlement.isBefore(loan)) {
      throw new RuntimeException("结算日期不能早于资方下款日期");
    }
    if (RULE_RUIHEXIANG.equals(ruleType) && (loan.getYear() < settlement.getYear() || loan.getMonthValue() < settlement.getMonthValue())) {
      return settlement.getDayOfMonth();
    }
    return Math.toIntExact(ChronoUnit.DAYS.between(loan, settlement)) + 1;
  }

  private int nextRepaymentLineNo(Long loanId) {
    Integer maxLine = repaymentDao.selectList(
        new QueryWrapper<ErpFunderLoanRepaymentEntity>()
            .eq("loan_id", loanId)
            .orderByDesc("line_no")
            .last("limit 1"))
        .stream().findFirst().map(ErpFunderLoanRepaymentEntity::getLineNo).orElse(0);
    return maxLine + 1;
  }

  private String resolveFunderRuleType(Long funderId, String funderName) {
    ErpPartnerEntity partner = funderId == null ? null : partnerDao.selectById(funderId);
    String rule = partner == null ? null : StringUtils.upperCase(StringUtils.trimToEmpty(partner.getFunderFeeRuleType()));
    if (StringUtils.isNotBlank(rule)) {
      return rule;
    }
    String name = StringUtils.defaultString(funderName);
    if (StringUtils.contains(name, "瑞和祥")) {
      return RULE_RUIHEXIANG;
    }
    if (StringUtils.contains(name, "超跃")) {
      return RULE_CHAOYUE;
    }
    if (StringUtils.contains(name, "万翔")) {
      return RULE_WANXIANG;
    }
    return "DEFAULT";
  }

  private BigDecimal decimal3(BigDecimal value) {
    return (value == null ? BigDecimal.ZERO : value).setScale(3, RoundingMode.HALF_UP);
  }

  private BigDecimal decimal6(BigDecimal value) {
    return (value == null ? BigDecimal.ZERO : value).setScale(6, RoundingMode.HALF_UP);
  }

  private static class ActualOutbound {
    private int boxes = 0;
    private BigDecimal weight = BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
  }

  private static class PackingWeight {
    private int totalBoxes = 0;
    private BigDecimal totalWeight = BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
    private BigDecimal avgWeight = BigDecimal.ZERO.setScale(6, RoundingMode.HALF_UP);
  }

  @Override
  public ResponseEntity<byte[]> downloadPaymentVoucher(Long id) {
    ErpFunderPaymentEntity payment = paymentDao.selectById(id);
    return download(payment == null ? null : payment.getFilePath(), payment == null ? null : payment.getFileName());
  }

  @Override
  public ResponseEntity<byte[]> downloadContributionVoucher(Long allocationId) {
    ErpFunderPaymentAllocationEntity allocation = allocationDao.selectById(allocationId);
    return download(allocation == null ? null : allocation.getXianmuContributionFilePath(),
        allocation == null ? null : allocation.getXianmuContributionFileName());
  }

  @Override
  public ResponseEntity<byte[]> downloadRepaymentVoucher(Long id) {
    ErpFunderLoanRepaymentEntity repayment = repaymentDao.selectById(id);
    return download(repayment == null ? null : repayment.getFilePath(), repayment == null ? null : repayment.getFileName());
  }

  private void validatePayment(ErpFunderPaymentEntity payment) {
    if (payment == null) {
      throw new RuntimeException("请填写打款信息");
    }
    Integer paymentType = payment.getPaymentType() == null ? PAYMENT_TYPE_FUNDER : payment.getPaymentType();
    if (paymentType == PAYMENT_TYPE_FUNDER && payment.getFunderId() == null) {
      throw new RuntimeException("请选择资方");
    }
    if (paymentType == PAYMENT_TYPE_XIANMU && payment.getPayerId() == null) {
      throw new RuntimeException("请选择鲜牧付款主体");
    }
    if (paymentType != PAYMENT_TYPE_FUNDER && paymentType != PAYMENT_TYPE_XIANMU) {
      throw new RuntimeException("请选择正确的付款类型");
    }
    if (payment.getModifiedAmount() == null || payment.getModifiedAmount().compareTo(BigDecimal.ZERO) <= 0) {
      throw new RuntimeException("修改金额必须大于0");
    }
    if (paymentType == PAYMENT_TYPE_FUNDER && payment.getPaymentDate() == null) {
      throw new RuntimeException("请选择资方打款日期");
    }
    if (paymentType == PAYMENT_TYPE_FUNDER && StringUtils.isBlank(payment.getFilePath())) {
      throw new RuntimeException("请先上传资方打款凭证");
    }
    if (payment.getAllocationList() == null || payment.getAllocationList().isEmpty()) {
      throw new RuntimeException("请至少选择一张客户订单确认函并填写分摊金额");
    }
    if (paymentType == PAYMENT_TYPE_XIANMU && payment.getAllocationList().size() != 1) {
      throw new RuntimeException("鲜牧全款打款只能选择一张客户订单确认函");
    }
    List<Long> confirmIds = new ArrayList<Long>();
    for (ErpFunderPaymentAllocationEntity allocation : payment.getAllocationList()) {
      if (allocation.getConfirmId() == null || allocation.getAllocationAmount() == null
          || allocation.getAllocationAmount().compareTo(BigDecimal.ZERO) <= 0) {
        throw new RuntimeException("每张客户订单确认函的分摊金额都必须大于0");
      }
      if (confirmIds.contains(allocation.getConfirmId())) {
        throw new RuntimeException("同一张客户订单确认函不能重复分摊");
      }
      Integer existingCount = allocationDao.selectCount(new QueryWrapper<ErpFunderPaymentAllocationEntity>()
          .eq("confirm_id", allocation.getConfirmId()));
      if (existingCount != null && existingCount > 0) {
        throw new RuntimeException("该确认函合同已经进行过资方全款或鲜牧全款打款，不能重复选择");
      }
      confirmIds.add(allocation.getConfirmId());
    }
  }

  private void updateXianmuPayment(ErpFunderPaymentEntity payment) {
    ErpFunderPaymentEntity existing = paymentDao.selectOne(new QueryWrapper<ErpFunderPaymentEntity>()
        .eq("id", payment.getId())
        .last("for update"));
    if (existing == null) {
      throw new RuntimeException("打款记录不存在");
    }
    if (existing.getPaymentType() == null || existing.getPaymentType() != PAYMENT_TYPE_XIANMU) {
      throw new RuntimeException("只有鲜牧全款打款可以补尾款");
    }
    if (existing.getStatus() != null && existing.getStatus() == 1) {
      throw new RuntimeException("该鲜牧全款打款已经确认完成，不能继续修改");
    }
    if (payment.getAllocationList() == null || payment.getAllocationList().isEmpty()) {
      throw new RuntimeException("请保留原合同号分摊明细");
    }
    BigDecimal allocationTotal = BigDecimal.ZERO;
    for (ErpFunderPaymentAllocationEntity allocation : payment.getAllocationList()) {
      ErpPresaleConfirmEntity confirm = loadConfirmForAllocation(allocation);
      allocation.setAllocationAmount(confirmTotalAmount(confirm.getId()));
      if (allocation.getConfirmId() == null || allocation.getAllocationAmount() == null
          || allocation.getAllocationAmount().compareTo(BigDecimal.ZERO) <= 0) {
        throw new RuntimeException("每张客户订单确认函的分摊金额都必须大于0");
      }
      Integer existingAllocationCount = allocationDao.selectCount(new QueryWrapper<ErpFunderPaymentAllocationEntity>()
          .eq("payment_id", existing.getId())
          .eq("confirm_id", allocation.getConfirmId()));
      if (existingAllocationCount == null || existingAllocationCount == 0) {
        throw new RuntimeException("补尾款时不能变更合同号，请重新打开待尾款记录");
      }
      allocationTotal = allocationTotal.add(money(allocation.getAllocationAmount()));
    }
    boolean complete = prepareXianmuInstallments(payment, existing.getId());
    Date now = new Date();
    existing.setPayerId(payment.getPayerId() == null ? existing.getPayerId() : payment.getPayerId());
    existing.setPayerName(payment.getPayerName() == null ? existing.getPayerName() : payment.getPayerName());
    existing.setRecognizedAmount(payment.getRecognizedAmount());
    existing.setModifiedAmount(allocationTotal);
    existing.setPaymentDate(payment.getPaymentDate());
    existing.setFilePath(payment.getFilePath());
    existing.setFileName(payment.getFileName());
    existing.setRawText(payment.getRawText());
    existing.setStatus(complete ? 1 : 0);
    existing.setUpdateTime(now);
    paymentDao.updateById(existing);

    allocationDao.delete(new QueryWrapper<ErpFunderPaymentAllocationEntity>().eq("payment_id", existing.getId()));
    for (ErpFunderPaymentAllocationEntity allocation : payment.getAllocationList()) {
      ErpPresaleConfirmEntity confirm = loadConfirmForAllocation(allocation);
      ErpPresaleOrderEntity presale = presaleOrderDao.selectById(confirm.getPresaleOrderId());
      if (presale == null) {
        throw new RuntimeException("确认函关联的预销售单不存在，请重新选择");
      }
      allocation.setPaymentId(existing.getId());
      allocation.setConfirmId(confirm.getId());
      allocation.setPresaleOrderId(presale.getId());
      allocation.setPresaleOrderNo(presale.getOrderNo());
      allocation.setSellerContractNo(presale.getSellerContractNo());
      allocation.setConfirmContractNo(confirm.getContractNo());
      allocation.setAllocationAmount(money(allocation.getAllocationAmount()));
      allocation.setXianmuDepositRecognizedAmount(money(allocation.getXianmuDepositRecognizedAmount()));
      allocation.setXianmuDepositModifiedAmount(money(allocation.getXianmuDepositModifiedAmount()));
      allocation.setXianmuBalanceRecognizedAmount(money(allocation.getXianmuBalanceRecognizedAmount()));
      allocation.setXianmuBalanceModifiedAmount(money(allocation.getXianmuBalanceModifiedAmount()));
      allocation.setCreateTime(now);
      allocation.setUpdateTime(now);
      allocationDao.insert(allocation);
    }
  }

  private boolean prepareXianmuInstallments(ErpFunderPaymentEntity payment, Long currentPaymentId) {
    boolean complete = true;
    BigDecimal recognizedTotal = BigDecimal.ZERO;
    Date paymentDate = null;
    String firstFilePath = null;
    String firstFileName = null;
    StringBuilder rawText = new StringBuilder();
    for (ErpFunderPaymentAllocationEntity allocation : payment.getAllocationList()) {
      BigDecimal allocationAmount = money(allocation.getAllocationAmount());
      BigDecimal depositAmount = money(allocation.getXianmuDepositModifiedAmount());
      BigDecimal balanceAmount = money(allocation.getXianmuBalanceModifiedAmount());
      if (StringUtils.isBlank(allocation.getXianmuDepositFilePath()) || allocation.getXianmuDepositDate() == null
          || depositAmount.compareTo(BigDecimal.ZERO) <= 0) {
        throw new RuntimeException("鲜牧全款时，每个合同号都必须先上传定金凭证");
      }
      validateUniqueInstallmentFile("xianmu_deposit_file_path", allocation.getXianmuDepositFilePath(), currentPaymentId,
          "该鲜牧定金凭证已经使用，请勿重复提交");
      boolean hasBalance = StringUtils.isNotBlank(allocation.getXianmuBalanceFilePath())
          || allocation.getXianmuBalanceDate() != null
          || balanceAmount.compareTo(BigDecimal.ZERO) > 0;
      if (hasBalance) {
        if (StringUtils.isBlank(allocation.getXianmuBalanceFilePath()) || allocation.getXianmuBalanceDate() == null
            || balanceAmount.compareTo(BigDecimal.ZERO) <= 0) {
          throw new RuntimeException("已填写尾款时，尾款凭证、日期和金额都必须完整");
        }
        validateUniqueInstallmentFile("xianmu_balance_file_path", allocation.getXianmuBalanceFilePath(), currentPaymentId,
            "该鲜牧尾款凭证已经使用，请勿重复提交");
        if (balanceAmount.compareTo(allocationAmount) != 0) {
          throw new RuntimeException("鲜牧尾款金额必须等于客户订单确认函总金额");
        }
      }
      if (!hasBalance) {
        complete = false;
      }
      recognizedTotal = recognizedTotal
          .add(money(allocation.getXianmuDepositRecognizedAmount()))
          .add(money(allocation.getXianmuBalanceRecognizedAmount()));
      if (paymentDate == null || allocation.getXianmuDepositDate().after(paymentDate)) {
        paymentDate = allocation.getXianmuDepositDate();
      }
      if (hasBalance && allocation.getXianmuBalanceDate().after(paymentDate)) {
        paymentDate = allocation.getXianmuBalanceDate();
      }
      if (firstFilePath == null) {
        firstFilePath = allocation.getXianmuDepositFilePath();
        firstFileName = allocation.getXianmuDepositFileName();
      }
      appendRawText(rawText, allocation.getXianmuDepositRawText());
      appendRawText(rawText, allocation.getXianmuBalanceRawText());
    }
    payment.setRecognizedAmount(money(recognizedTotal));
    payment.setPaymentDate(paymentDate);
    payment.setFilePath(firstFilePath);
    payment.setFileName(StringUtils.defaultIfBlank(firstFileName, "鲜牧全款定金/尾款凭证"));
    payment.setRawText(rawText.toString());
    return complete;
  }

  private void validateUniqueInstallmentFile(String columnName, String filePath, Long currentPaymentId, String message) {
    if (StringUtils.isBlank(filePath)) {
      return;
    }
    QueryWrapper<ErpFunderPaymentAllocationEntity> wrapper = new QueryWrapper<ErpFunderPaymentAllocationEntity>()
        .eq(columnName, filePath);
    if (currentPaymentId != null) {
      wrapper.ne("payment_id", currentPaymentId);
    }
    Integer count = allocationDao.selectCount(wrapper);
    if (count != null && count > 0) {
      throw new RuntimeException(message);
    }
  }

  private void appendRawText(StringBuilder builder, String value) {
    if (StringUtils.isBlank(value)) {
      return;
    }
    if (builder.length() > 0) {
      builder.append("\n\n");
    }
    builder.append(value);
  }

  private BigDecimal confirmTotalAmount(Long confirmId) {
    ErpPresaleConfirmEntity confirm = loadConfirmById(confirmId);
    BigDecimal totalAmount = money(confirm.getTotalAmount());
    if (totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new RuntimeException("所选客户订单确认函总金额为空或为0，不能进行打款");
    }
    return totalAmount;
  }

  private ErpPresaleConfirmEntity loadConfirmForAllocation(ErpFunderPaymentAllocationEntity allocation) {
    if (allocation == null || allocation.getConfirmId() == null) {
      throw new RuntimeException("请选择客户订单确认函");
    }
    return loadConfirmById(allocation.getConfirmId());
  }

  private ErpPresaleConfirmEntity loadConfirmById(Long confirmId) {
    if (confirmId == null) {
      throw new RuntimeException("请选择客户订单确认函");
    }
    ErpPresaleConfirmEntity confirm = presaleConfirmDao.selectById(confirmId);
    if (confirm == null) {
      throw new RuntimeException("所选客户订单确认函不存在，请重新选择");
    }
    return confirm;
  }

  private void fillOrderContractAmount(List<ErpFunderPaymentAllocationEntity> allocationList) {
    if (allocationList == null || allocationList.isEmpty()) {
      return;
    }
    for (ErpFunderPaymentAllocationEntity allocation : allocationList) {
      if (allocation.getConfirmId() == null) {
        continue;
      }
      ErpPresaleConfirmEntity confirm = presaleConfirmDao.selectById(allocation.getConfirmId());
      if (confirm != null && confirm.getTotalAmount() != null) {
        allocation.setOrderContractAmount(money(confirm.getTotalAmount()));
        allocation.setConfirmContractNo(confirm.getContractNo());
      }
    }
  }

  private void validateXianmuContribution(ErpFunderPaymentAllocationEntity allocation) {
    if (StringUtils.isBlank(allocation.getXianmuContributionFilePath())) {
      throw new RuntimeException("资方全款时，每个合同号都必须上传鲜牧出资款凭证");
    }
    if (allocation.getXianmuContributionDate() == null) {
      throw new RuntimeException("请选择鲜牧出资款打款日期");
    }
    BigDecimal contributionAmount = money(allocation.getXianmuContributionModifiedAmount());
    if (contributionAmount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new RuntimeException("鲜牧出资款金额必须大于0");
    }
    if (contributionAmount.compareTo(money(allocation.getAllocationAmount())) > 0) {
      throw new RuntimeException("鲜牧出资款不能大于该合同号的资方全款金额");
    }
    Integer count = allocationDao.selectCount(new QueryWrapper<ErpFunderPaymentAllocationEntity>()
        .eq("xianmu_contribution_file_path", allocation.getXianmuContributionFilePath()));
    if (count != null && count > 0) {
      throw new RuntimeException("该鲜牧出资款凭证已经使用，请勿重复提交");
    }
  }

  private int naturalDays(Date startDate, Date endDate) {
    if (startDate == null || endDate == null) {
      throw new RuntimeException("贷款日期和还款日期不能为空");
    }
    LocalDate start = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    LocalDate end = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    long days = ChronoUnit.DAYS.between(start, end);
    if (days < 0) {
      throw new RuntimeException("还款日期不能早于首次资方打款日期");
    }
    return Math.toIntExact(days);
  }

  private BigDecimal extractAmount(String rawText) {
    String text = StringUtils.defaultString(rawText);
    BigDecimal normalSmallAmount = extractAmountNearLabel(text, "小写");
    if (normalSmallAmount != null) {
      return normalSmallAmount;
    }
    BigDecimal ccbAmount = extractAmountNearLabel(text, "小写金额");
    if (ccbAmount != null) {
      return ccbAmount;
    }
    BigDecimal bracketAmount = extractAmountNearLabel(text, "小写");
    if (bracketAmount != null) {
      return bracketAmount;
    }
    BigDecimal chineseAmount = extractChineseRmbAmount(text);
    if (chineseAmount != null) {
      return chineseAmount;
    }
    Matcher matcher = LABEL_AMOUNT_PATTERN.matcher(text);
    if (!matcher.find()) {
      matcher = CURRENCY_AMOUNT_PATTERN.matcher(text);
      if (!matcher.find()) {
        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
      }
    }
    try {
      return money(new BigDecimal(matcher.group(1).replace(",", "")));
    } catch (Exception ignored) {
      return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }
  }

  private Date extractDate(String rawText) {
    String text = StringUtils.defaultString(rawText);
    Matcher matcher = DATE_PATTERN.matcher(text);
    if (matcher.find()) {
      return parseDateParts(matcher.group(1), matcher.group(2), matcher.group(3));
    }
    matcher = COMPACT_DATE_PATTERN.matcher(text);
    if (matcher.find()) {
      return parseDateParts(matcher.group(1), matcher.group(2), matcher.group(3));
    }
    return null;
  }

  private Date parseDateParts(String year, String month, String day) {
    String value = year + "-" + month + "-" + day;
    SimpleDateFormat format = new SimpleDateFormat("yyyy-M-d");
    format.setLenient(false);
    try {
      return format.parse(value);
    } catch (ParseException ignored) {
      return null;
    }
  }

  private BigDecimal extractAmountNearLabel(String rawText, String label) {
    int labelIndex = StringUtils.indexOf(rawText, label);
    if (labelIndex < 0) {
      return null;
    }
    String nearbyText = StringUtils.substring(rawText, labelIndex, Math.min(rawText.length(), labelIndex + 180));
    Matcher matcher = OCR_DECIMAL_AMOUNT_PATTERN.matcher(nearbyText);
    if (!matcher.find()) {
      return null;
    }
    return parseOcrAmount(matcher.group(1));
  }

  private BigDecimal extractFirstDecimalAmount(String rawText) {
    Matcher matcher = OCR_DECIMAL_AMOUNT_PATTERN.matcher(StringUtils.defaultString(rawText));
    if (!matcher.find()) {
      return null;
    }
    return parseOcrAmount(matcher.group(1));
  }

  private BigDecimal parseOcrAmount(String amountText) {
    try {
      String normalized = StringUtils.defaultString(amountText)
          .replaceAll("\\s+", "")
          .replace(",", "")
          .replace("，", "")
          .replace("。", ".");
      return money(new BigDecimal(normalized));
    } catch (Exception ignored) {
      return null;
    }
  }

  private Map<String, Object> extractBankReceipt(String rawText) {
    String text = StringUtils.defaultString(rawText);
    if (StringUtils.contains(text, "建设银行")) {
      return extractCcbReceipt(text);
    }
    if (StringUtils.contains(text, "浦发银行") || StringUtils.containsIgnoreCase(text, "SPDBANK")) {
      return extractSpdReceipt(text);
    }
    if (StringUtils.contains(text, "工商银行") || StringUtils.containsIgnoreCase(text, "ICBC")) {
      return extractIcbcReceipt(text);
    }
    if (StringUtils.contains(text, "兴业银行")) {
      return extractCibReceipt(text);
    }
    if (StringUtils.contains(text, "农业发展银行") || StringUtils.containsIgnoreCase(text, "AGRICULTURAL DEVELOPMENT BANK")) {
      return extractAdbcReceipt(text);
    }
    return new HashMap<String, Object>();
  }

  private Map<String, Object> extractCcbReceipt(String rawText) {
    Map<String, Object> receipt = new HashMap<String, Object>();
    String text = StringUtils.defaultString(rawText);
    if (!StringUtils.contains(text, "中国建设银行网上银行电子回执")) {
      return receipt;
    }
    receipt.put("voucherTemplate", "中国建设银行网上银行电子回执");
    List<String> lines = nonBlankLines(text);
    receipt.put("voucherNo", firstMatchingLine(lines, "^\\d{10,}$", 0));
    receipt.put("transactionNo", firstMatchingLine(lines, "^\\d{2}-[A-Za-z0-9]+$", 0));

    List<String> names = valuesAfterMarker(lines, "全称", 2);
    if (!names.isEmpty()) {
      receipt.put("payerName", names.get(0));
    }
    if (names.size() > 1) {
      receipt.put("payeeName", names.get(1));
    }

    List<String> accounts = numericLinesAfterMarker(lines, "账号", 2);
    if (!accounts.isEmpty()) {
      receipt.put("payerAccount", accounts.get(0));
    }
    if (accounts.size() > 1) {
      receipt.put("payeeAccount", accounts.get(1));
    }

    List<String> banks = valuesAfterMarker(lines, "开户行", 2);
    if (!banks.isEmpty()) {
      receipt.put("payerBank", banks.get(0));
    }
    if (banks.size() > 1) {
      receipt.put("payeeBank", banks.get(1));
    }
    receipt.put("purpose", firstValueAfterMarker(lines, "用途"));
    receipt.put("summary", firstValueAfterMarker(lines, "摘要"));
    return receipt;
  }

  private Map<String, Object> extractSpdReceipt(String rawText) {
    Map<String, Object> receipt = new HashMap<String, Object>();
    String text = StringUtils.defaultString(rawText);
    receipt.put("voucherTemplate", StringUtils.contains(text, "业务凭证/回单")
        ? "上海浦东发展银行业务凭证/回单" : "上海浦东发展银行网上银行电子回单");
    List<String> lines = nonBlankLines(text);
    receipt.put("voucherNo", firstValueAfterAnyMarker(lines, "电子回单编号", "回单编号"));
    receipt.put("transactionNo", firstValueAfterAnyMarker(lines, "交易流水号-传票序号", "交易流水号"));
    putIfNotBlank(receipt, "payerName", firstValueAfterAnyMarker(lines, "付款人户名"));
    putIfNotBlank(receipt, "payeeName", firstValueAfterAnyMarker(lines, "收款人户名"));
    putIfNotBlank(receipt, "payerAccount", firstValueAfterAnyMarker(lines, "付款账号"));
    putIfNotBlank(receipt, "payeeAccount", firstValueAfterAnyMarker(lines, "收款账号"));
    putIfNotBlank(receipt, "payerBank", firstValueAfterAnyMarker(lines, "付款人开户行"));
    putIfNotBlank(receipt, "payeeBank", firstValueAfterAnyMarker(lines, "收款人开户行"));
    if (receipt.get("payerName") == null || receipt.get("payeeName") == null) {
      putPartyValues(receipt, valuesAfterAnyMarker(lines, 2, "账户名称"), "payerName", "payeeName");
    }
    if (receipt.get("payerAccount") == null || receipt.get("payeeAccount") == null) {
      putPartyValues(receipt, valuesAfterAnyMarker(lines, 2, false, "账号"), "payerAccount", "payeeAccount");
    }
    if (receipt.get("payerBank") == null || receipt.get("payeeBank") == null) {
      putPartyValues(receipt, valuesAfterAnyMarker(lines, 2, "开户银行"), "payerBank", "payeeBank");
    }
    receipt.put("purpose", firstValueAfterAnyMarker(lines, "用途", "交易附言"));
    receipt.put("summary", firstValueAfterAnyMarker(lines, "摘要"));
    receipt.put("recognizedAmount", extractAmount(text));
    receipt.put("paymentDate", extractDate(text));
    return receipt;
  }

  private Map<String, Object> extractIcbcReceipt(String rawText) {
    Map<String, Object> receipt = new HashMap<String, Object>();
    String text = StringUtils.defaultString(rawText);
    receipt.put("voucherTemplate", "中国工商银行网上银行电子回单");
    List<String> lines = nonBlankLines(text);
    receipt.put("voucherNo", firstValueAfterAnyMarker(lines, "电子回单号码", "电子回单编号"));
    receipt.put("transactionNo", firstValueAfterAnyMarker(lines, "交易流水号"));
    putPartyValues(receipt, valuesAfterAnyMarker(lines, 2, "户名", "⼾ 名"), "payerName", "payeeName");
    putPartyValues(receipt, valuesAfterAnyMarker(lines, 2, false, "账号", "账 号"), "payerAccount", "payeeAccount");
    putPartyValues(receipt, valuesAfterAnyMarker(lines, 2, "开户银行", "开⼾银⾏"), "payerBank", "payeeBank");
    receipt.put("purpose", firstValueAfterAnyMarker(lines, "用途", "⽤ 途"));
    receipt.put("summary", firstValueAfterAnyMarker(lines, "摘要", "摘 要"));
    receipt.put("recognizedAmount", extractAmount(text));
    receipt.put("paymentDate", extractDate(text));
    return receipt;
  }

  private Map<String, Object> extractCibReceipt(String rawText) {
    Map<String, Object> receipt = new HashMap<String, Object>();
    String text = StringUtils.defaultString(rawText);
    receipt.put("voucherTemplate", "兴业银行汇款回单");
    List<String> lines = nonBlankLines(text);
    receipt.put("voucherNo", firstValueAfterAnyMarker(lines, "唯一流水编号", "回单查询号"));
    putPartyValues(receipt, valuesAfterAnyMarker(lines, 2, "付款人", "收款人", "全称"), "payerName", "payeeName");
    putPartyValues(receipt, accountValuesFromLines(lines), "payerAccount", "payeeAccount");
    putPartyValues(receipt, valuesAfterAnyMarker(lines, 2, "付款银行", "收款银行"), "payerBank", "payeeBank");
    receipt.put("purpose", firstValueAfterAnyMarker(lines, "用途"));
    receipt.put("summary", firstValueAfterAnyMarker(lines, "摘要"));
    BigDecimal amount = extractCibAmount(text);
    receipt.put("recognizedAmount", amount == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : amount);
    receipt.put("paymentDate", extractDate(text));
    return receipt;
  }

  private BigDecimal extractCibAmount(String text) {
    BigDecimal amount = extractAmountNearLabel(text, "小写");
    if (amount != null) {
      return amount;
    }
    amount = extractAmountNearLabel(text, "写");
    if (amount != null) {
      return amount;
    }
    amount = extractChineseRmbAmount(text);
    if (amount != null) {
      return amount;
    }
    return extractFirstDecimalAmount(text);
  }

  private BigDecimal extractChineseRmbAmount(String text) {
    Matcher matcher = Pattern.compile("([零〇壹贰叁肆伍陆柒捌玖一二三四五六七八九十拾佰百仟千万亿]+)元").matcher(StringUtils.defaultString(text));
    if (!matcher.find()) {
      return null;
    }
    Long amount = parseChineseIntegerAmount(matcher.group(1));
    return amount == null ? null : BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP);
  }

  private Long parseChineseIntegerAmount(String value) {
    if (StringUtils.isBlank(value)) {
      return null;
    }
    long total = 0L;
    long section = 0L;
    long number = 0L;
    boolean found = false;
    for (int i = 0; i < value.length(); i++) {
      char ch = value.charAt(i);
      Integer digit = chineseDigit(ch);
      if (digit != null) {
        number = digit;
        found = true;
        continue;
      }
      long unit = chineseUnit(ch);
      if (unit == 0L) {
        continue;
      }
      found = true;
      if (unit < 10000L) {
        section += (number == 0L ? 1L : number) * unit;
      } else {
        section = (section + number) * unit;
        total += section;
        section = 0L;
      }
      number = 0L;
    }
    if (!found) {
      return null;
    }
    return total + section + number;
  }

  private Integer chineseDigit(char ch) {
    switch (ch) {
      case '零':
      case '〇':
        return 0;
      case '壹':
      case '一':
        return 1;
      case '贰':
      case '二':
        return 2;
      case '叁':
      case '三':
        return 3;
      case '肆':
      case '四':
        return 4;
      case '伍':
      case '五':
        return 5;
      case '陆':
      case '六':
        return 6;
      case '柒':
      case '七':
        return 7;
      case '捌':
      case '八':
        return 8;
      case '玖':
      case '九':
        return 9;
      default:
        return null;
    }
  }

  private long chineseUnit(char ch) {
    switch (ch) {
      case '十':
      case '拾':
        return 10L;
      case '佰':
      case '百':
        return 100L;
      case '仟':
      case '千':
        return 1000L;
      case '万':
        return 10000L;
      case '亿':
        return 100000000L;
      default:
        return 0L;
    }
  }

  private Map<String, Object> extractAdbcReceipt(String rawText) {
    Map<String, Object> receipt = new HashMap<String, Object>();
    String text = StringUtils.defaultString(rawText);
    receipt.put("voucherTemplate", "中国农业发展银行客户专用回单");
    List<String> lines = nonBlankLines(text);
    receipt.put("transactionNo", firstValueAfterAnyMarker(lines, "核心流水号", "交易流水号"));
    putPartyValues(receipt, companyNamesBeforeMarker(lines, "户名", 2), "payerName", "payeeName");
    putPartyValues(receipt, valuesAfterAnyMarker(lines, 2, "户名"), "payerName", "payeeName");
    putPartyValues(receipt, valuesAfterAnyMarker(lines, 2, false, "账号"), "payerAccount", "payeeAccount");
    putPartyValues(receipt, valuesAfterAnyMarker(lines, 2, "开户行"), "payerBank", "payeeBank");
    receipt.put("purpose", firstValueAfterAnyMarker(lines, "用途", "附言"));
    receipt.put("summary", firstValueAfterAnyMarker(lines, "摘要", "备注"));
    receipt.put("recognizedAmount", extractAmount(text));
    receipt.put("paymentDate", extractDate(text));
    return receipt;
  }

  private List<String> companyNamesBeforeMarker(List<String> lines, String marker, int maxCount) {
    List<String> values = new ArrayList<String>();
    int markerIndex = -1;
    for (int i = 0; i < lines.size(); i++) {
      if (StringUtils.contains(lines.get(i), marker)) {
        markerIndex = i;
        break;
      }
    }
    if (markerIndex <= 0) {
      return values;
    }
    for (int i = markerIndex - 1; i >= 0 && values.size() < maxCount; i--) {
      String candidate = StringUtils.trimToEmpty(lines.get(i));
      if (StringUtils.isBlank(candidate) || !StringUtils.contains(candidate, "公司")) {
        continue;
      }
      values.add(0, candidate);
    }
    while (values.size() > maxCount) {
      values.remove(0);
    }
    return values;
  }

  private List<String> nonBlankLines(String rawText) {
    List<String> lines = new ArrayList<String>();
    for (String line : StringUtils.split(StringUtils.defaultString(rawText), '\n')) {
      String value = StringUtils.trimToEmpty(line);
      if (StringUtils.isNotBlank(value)) {
        lines.add(value);
      }
    }
    return lines;
  }

  private List<String> valuesAfterMarker(List<String> lines, String marker, int maxCount) {
    return valuesAfterAnyMarker(lines, maxCount, marker);
  }

  private List<String> valuesAfterAnyMarker(List<String> lines, int maxCount, String... markers) {
    return valuesAfterAnyMarker(lines, maxCount, true, markers);
  }

  private List<String> valuesAfterAnyMarker(List<String> lines, int maxCount, boolean skipNumericOnly, String... markers) {
    List<String> values = new ArrayList<String>();
    for (int index = 0; index < lines.size() && values.size() < maxCount; index++) {
      String line = lines.get(index);
      String matchedMarker = matchedMarker(line, markers);
      if (StringUtils.isBlank(matchedMarker)) {
        continue;
      }
      String inlineValue = StringUtils.trimToEmpty(StringUtils.substringAfter(line, matchedMarker));
      if (StringUtils.isNotBlank(inlineValue)) {
        values.add(inlineValue);
      }
      for (int offset = 1; offset <= 6 && index + offset < lines.size() && values.size() < maxCount; offset++) {
        String candidate = lines.get(index + offset);
        if (isReceiptMarker(candidate) || (skipNumericOnly && candidate.matches("^\\d{6,}$"))) {
          continue;
        }
        String nestedMarker = matchedMarker(candidate, markers);
        if (StringUtils.isNotBlank(nestedMarker)) {
          candidate = StringUtils.trimToEmpty(StringUtils.substringAfter(candidate, nestedMarker));
        }
        if (StringUtils.isNotBlank(candidate)) {
          values.add(candidate);
        }
      }
    }
    return values;
  }

  private void putPartyValues(Map<String, Object> receipt, List<String> values, String payerKey, String payeeKey) {
    if (values == null || values.isEmpty()) {
      return;
    }
    if (receipt.get(payerKey) == null) {
      receipt.put(payerKey, values.get(0));
    }
    if (values.size() > 1 && receipt.get(payeeKey) == null) {
      receipt.put(payeeKey, values.get(1));
    }
  }

  private void putIfNotBlank(Map<String, Object> receipt, String key, String value) {
    if (StringUtils.isNotBlank(value)) {
      receipt.put(key, value);
    }
  }

  private List<String> accountValuesFromLines(List<String> lines) {
    List<String> values = new ArrayList<String>();
    Pattern accountPattern = Pattern.compile("^(?:账\\s*)?号[:：]?\\s*([0-9]{10,})$");
    for (String line : lines) {
      Matcher matcher = accountPattern.matcher(StringUtils.trimToEmpty(line));
      if (matcher.find()) {
        values.add(matcher.group(1));
      }
      if (values.size() >= 2) {
        break;
      }
    }
    return values;
  }

  private List<String> numericLinesAfterMarker(List<String> lines, String marker, int maxCount) {
    List<String> values = new ArrayList<String>();
    int firstMarker = lines.indexOf(marker);
    if (firstMarker < 0) {
      return values;
    }
    for (int index = firstMarker + 1; index < lines.size() && values.size() < maxCount; index++) {
      String candidate = lines.get(index);
      if (candidate.matches("^\\d{10,}$")) {
        values.add(candidate);
      }
      if (StringUtils.contains(candidate, "开户行")) {
        break;
      }
    }
    return values;
  }

  private String firstValueAfterMarker(List<String> lines, String marker) {
    return firstValueAfterAnyMarker(lines, marker);
  }

  private String firstValueAfterAnyMarker(List<String> lines, String... markers) {
    for (int index = 0; index < lines.size(); index++) {
      String marker = matchedMarker(lines.get(index), markers);
      if (StringUtils.isBlank(marker)) {
        continue;
      }
      String inlineValue = StringUtils.trimToEmpty(StringUtils.substringAfter(lines.get(index), marker));
      if (StringUtils.isNotBlank(inlineValue)) {
        return inlineValue;
      }
      for (int offset = 1; offset <= 5 && index + offset < lines.size(); offset++) {
        String candidate = lines.get(index + offset);
        if (!isReceiptMarker(candidate)) {
          return candidate;
        }
      }
    }
    return null;
  }

  private String matchedMarker(String line, String... markers) {
    for (String marker : markers) {
      if (StringUtils.isNotBlank(marker) && StringUtils.contains(line, marker)) {
        return marker;
      }
    }
    return null;
  }

  private String firstMatchingLine(List<String> lines, String regex, int skipCount) {
    int count = 0;
    for (String line : lines) {
      if (!line.matches(regex)) {
        continue;
      }
      if (count++ >= skipCount) {
        return line;
      }
    }
    return null;
  }

  private boolean isReceiptMarker(String value) {
    String[] markers = {"全称", "付款人", "收款人", "账号", "开户行", "小写金额", "大写金额",
        "用途", "钞汇标志", "摘要", "币别：", "日期：", "凭证号：", "账户明细编号-交易流水号：",
        "账户名称", "户名", "⼾ 名", "开户银行", "开⼾银⾏", "收款人户名", "付款人户名",
        "收款账号", "付款账号", "收款人开户行", "付款人开户行", "电子回单编号", "回单编号",
        "电子回单号码", "交易流水号", "交易流水号-传票序号", "唯一流水编号", "回单查询号",
        "付款银行", "收款银行", "交易名称", "交易时间", "申请日期", "打印日期", "打印渠道"};
    for (String marker : markers) {
      if (StringUtils.equals(value, marker)) {
        return true;
      }
    }
    return false;
  }

  private ResponseEntity<byte[]> download(String filePath, String fileName) {
    if (StringUtils.isBlank(filePath)) {
      throw new RuntimeException("归档文件不存在");
    }
    try {
      File file = new File(filePath);
      if (!file.exists()) {
        throw new RuntimeException("归档文件不存在");
      }
      return ResponseEntity.ok()
          .contentType(MediaType.APPLICATION_OCTET_STREAM)
          .header(HttpHeaders.CONTENT_DISPOSITION,
              ContentDisposition.attachment()
                  .filename(StringUtils.defaultIfBlank(fileName, file.getName()), StandardCharsets.UTF_8)
                  .build().toString())
          .body(Files.readAllBytes(file.toPath()));
    } catch (Exception ex) {
      throw new RuntimeException("归档文件下载失败: " + ex.getMessage(), ex);
    }
  }

  private String number(String prefix) {
    return prefix + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date())
        + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
  }

  private String stringValue(Object value) {
    return value == null ? "" : StringUtils.trimToEmpty(value.toString());
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

  private BigDecimal money(BigDecimal value) {
    return (value == null ? BigDecimal.ZERO : value).setScale(2, RoundingMode.HALF_UP);
  }

  private BigDecimal rate(BigDecimal value) {
    return (value == null ? BigDecimal.ZERO : value).setScale(10, RoundingMode.HALF_UP);
  }

  private BigDecimal decimal2(BigDecimal value) {
    return (value == null ? BigDecimal.ZERO : value).setScale(2, RoundingMode.HALF_UP);
  }
}
