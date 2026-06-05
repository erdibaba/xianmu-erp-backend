package io.renren.modules.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.renren.common.utils.PageUtils;
import io.renren.common.utils.Query;
import io.renren.modules.erp.dao.ErpFunderLoanDao;
import io.renren.modules.erp.dao.ErpFunderLoanRepaymentDao;
import io.renren.modules.erp.dao.ErpFunderPaymentAllocationDao;
import io.renren.modules.erp.dao.ErpFunderPaymentDao;
import io.renren.modules.erp.dao.ErpPartnerDao;
import io.renren.modules.erp.dao.ErpPresaleOrderDao;
import io.renren.modules.erp.entity.ErpFunderLoanEntity;
import io.renren.modules.erp.entity.ErpFunderLoanRepaymentEntity;
import io.renren.modules.erp.entity.ErpFunderPaymentAllocationEntity;
import io.renren.modules.erp.entity.ErpFunderPaymentEntity;
import io.renren.modules.erp.entity.ErpPartnerEntity;
import io.renren.modules.erp.entity.ErpPresaleOrderEntity;
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
  private static final Pattern LABEL_AMOUNT_PATTERN = Pattern.compile(
      "(?:交易金额|付款金额|转账金额|打款金额|金额|人民币)\\s*[:：]?\\s*(?:CNY|RMB|￥|¥)?\\s*([0-9][0-9,]*(?:\\.[0-9]{1,2})?)",
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

  @Autowired
  private ErpFunderPaymentDao paymentDao;
  @Autowired
  private ErpFunderPaymentAllocationDao allocationDao;
  @Autowired
  private ErpFunderLoanDao loanDao;
  @Autowired
  private ErpFunderLoanRepaymentDao repaymentDao;
  @Autowired
  private ErpPartnerDao partnerDao;
  @Autowired
  private ErpPresaleOrderDao presaleOrderDao;
  @Autowired
  private ErpOcrService ocrService;

  @Override
  public PageUtils queryPaymentPage(Map<String, Object> params) {
    String keyword = stringValue(params.get("keyword"));
    String funderId = stringValue(params.get("funderId"));
    QueryWrapper<ErpFunderPaymentEntity> wrapper = new QueryWrapper<ErpFunderPaymentEntity>()
        .orderByDesc("payment_date", "id");
    if (StringUtils.isNotBlank(keyword)) {
      wrapper.and(q -> q.like("payment_no", keyword).or().like("funder_name", keyword));
    }
    if (StringUtils.isNotBlank(funderId)) {
      wrapper.eq("funder_id", Long.valueOf(funderId));
    }
    IPage<ErpFunderPaymentEntity> page = paymentDao.selectPage(
        new Query<ErpFunderPaymentEntity>().getPage(params), wrapper);
    return new PageUtils(page);
  }

  @Override
  public ErpFunderPaymentEntity getPaymentDetail(Long id) {
    ErpFunderPaymentEntity payment = paymentDao.selectById(id);
    if (payment != null) {
      payment.setAllocationList(allocationDao.selectList(
          new QueryWrapper<ErpFunderPaymentAllocationEntity>()
              .eq("payment_id", id)
              .orderByAsc("id")));
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
  public List<ErpPresaleOrderEntity> queryPresaleOptions(String keyword) {
    QueryWrapper<ErpPresaleOrderEntity> wrapper = new QueryWrapper<ErpPresaleOrderEntity>()
        .orderByDesc("order_date", "id")
        .last("limit 15");
    if (StringUtils.isNotBlank(keyword)) {
      wrapper.and(q -> q.like("order_no", keyword)
          .or().like("seller_contract_no", keyword)
          .or().like("customer_reference", keyword));
    }
    return presaleOrderDao.selectList(wrapper);
  }

  @Override
  public Map<String, Object> recognizeVoucher(MultipartFile file) throws Exception {
    if (file == null || file.isEmpty()) {
      throw new RuntimeException("请上传银行打款凭证");
    }
    ErpRecognizeResultVo result = ocrService.recognize(file, "bank_payment_voucher");
    String rawText = result == null ? "" : StringUtils.defaultString(result.getRawText());
    Map<String, Object> voucher = new HashMap<String, Object>();
    voucher.put("recognizedAmount", extractAmount(rawText));
    voucher.put("paymentDate", extractDate(rawText));
    voucher.put("filePath", result == null ? null : result.getSavedFilePath());
    voucher.put("fileName", StringUtils.defaultIfBlank(file.getOriginalFilename(), "银行打款凭证"));
    voucher.put("rawText", rawText);
    voucher.putAll(extractCcbReceipt(rawText));
    return voucher;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void confirmPayment(ErpFunderPaymentEntity payment, Long userId) {
    validatePayment(payment);
    if (paymentDao.selectCount(new QueryWrapper<ErpFunderPaymentEntity>()
        .eq("file_path", payment.getFilePath())) > 0) {
      throw new RuntimeException("该资方打款凭证已经确认，请勿重复提交");
    }
    Date now = new Date();
    ErpPartnerEntity funder = partnerDao.selectById(payment.getFunderId());
    if (funder == null || funder.getStatus() == null || funder.getStatus() != 1
        || !StringUtils.contains(StringUtils.defaultString(funder.getBusinessRole()), "FUNDER")) {
      throw new RuntimeException("请选择启用状态的资方");
    }
    if (funder.getAnnualInterestRate() == null || funder.getAnnualInterestRate().compareTo(BigDecimal.ZERO) <= 0) {
      throw new RuntimeException("所选资方未维护年利率，请先到往来单位维护资方年利率");
    }

    BigDecimal allocationTotal = BigDecimal.ZERO;
    for (ErpFunderPaymentAllocationEntity allocation : payment.getAllocationList()) {
      allocationTotal = allocationTotal.add(money(allocation.getAllocationAmount()));
    }
    if (allocationTotal.compareTo(money(payment.getModifiedAmount())) != 0) {
      throw new RuntimeException("预销售单分摊金额合计必须等于修改金额");
    }

    payment.setPaymentNo(number("FP"));
    payment.setFunderName(funder.getPartnerName());
    payment.setRecognizedAmount(money(payment.getRecognizedAmount()));
    payment.setModifiedAmount(money(payment.getModifiedAmount()));
    payment.setStatus(1);
    payment.setCreateUserId(userId);
    payment.setCreateTime(now);
    payment.setUpdateTime(now);
    paymentDao.insert(payment);

    for (ErpFunderPaymentAllocationEntity allocation : payment.getAllocationList()) {
      ErpPresaleOrderEntity presale = presaleOrderDao.selectById(allocation.getPresaleOrderId());
      if (presale == null) {
        throw new RuntimeException("存在已删除或无效的预销售单，请重新选择");
      }
      allocation.setPaymentId(payment.getId());
      allocation.setPresaleOrderNo(presale.getOrderNo());
      allocation.setSellerContractNo(presale.getSellerContractNo());
      allocation.setAllocationAmount(money(allocation.getAllocationAmount()));
      allocation.setCreateTime(now);
      allocation.setUpdateTime(now);
      allocationDao.insert(allocation);

      ErpFunderLoanEntity loan = new ErpFunderLoanEntity();
      loan.setLoanNo(number("FL"));
      loan.setPaymentId(payment.getId());
      loan.setAllocationId(allocation.getId());
      loan.setPresaleOrderId(presale.getId());
      loan.setPresaleOrderNo(presale.getOrderNo());
      loan.setSellerContractNo(presale.getSellerContractNo());
      loan.setFunderId(funder.getId());
      loan.setFunderName(funder.getPartnerName());
      loan.setLoanAmount(allocation.getAllocationAmount());
      loan.setAnnualInterestRate(rate(funder.getAnnualInterestRate()));
      loan.setLoanDate(payment.getPaymentDate());
      loan.setRepaidPrincipal(money(BigDecimal.ZERO));
      loan.setRemainingPrincipal(allocation.getAllocationAmount());
      loan.setInterestAmount(decimal10(BigDecimal.ZERO));
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
    repayment.setRepaymentPrincipal(principal);
    repayment.setAnnualInterestRate(rate(loan.getAnnualInterestRate()));
    repayment.setLoanDays(days);
    repayment.setInterestAmount(decimal10(interest));
    repayment.setExpectedPaymentAmount(decimal10(principal.add(interest)));
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
    repayment.setRecognizedAmount(money(repayment.getRecognizedAmount()));
    repayment.setModifiedAmount(money(repayment.getModifiedAmount()));
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
    loan.setInterestAmount(decimal10(decimal10(loan.getInterestAmount()).add(repayment.getInterestAmount())));
    loan.setStatus(remainingPrincipal.compareTo(BigDecimal.ZERO) == 0 ? 1 : 0);
    loan.setUpdateTime(now);
    loanDao.updateById(loan);
  }

  @Override
  public ResponseEntity<byte[]> downloadPaymentVoucher(Long id) {
    ErpFunderPaymentEntity payment = paymentDao.selectById(id);
    return download(payment == null ? null : payment.getFilePath(), payment == null ? null : payment.getFileName());
  }

  @Override
  public ResponseEntity<byte[]> downloadRepaymentVoucher(Long id) {
    ErpFunderLoanRepaymentEntity repayment = repaymentDao.selectById(id);
    return download(repayment == null ? null : repayment.getFilePath(), repayment == null ? null : repayment.getFileName());
  }

  private void validatePayment(ErpFunderPaymentEntity payment) {
    if (payment == null || payment.getFunderId() == null) {
      throw new RuntimeException("请选择资方");
    }
    if (payment.getModifiedAmount() == null || payment.getModifiedAmount().compareTo(BigDecimal.ZERO) <= 0) {
      throw new RuntimeException("修改金额必须大于0");
    }
    if (payment.getPaymentDate() == null) {
      throw new RuntimeException("请选择资方打款日期");
    }
    if (StringUtils.isBlank(payment.getFilePath())) {
      throw new RuntimeException("请先上传资方打款凭证");
    }
    if (payment.getAllocationList() == null || payment.getAllocationList().isEmpty()) {
      throw new RuntimeException("请至少选择一张预销售单并填写分摊金额");
    }
    List<Long> presaleIds = new ArrayList<Long>();
    for (ErpFunderPaymentAllocationEntity allocation : payment.getAllocationList()) {
      if (allocation.getPresaleOrderId() == null || allocation.getAllocationAmount() == null
          || allocation.getAllocationAmount().compareTo(BigDecimal.ZERO) <= 0) {
        throw new RuntimeException("每张预销售单的分摊金额都必须大于0");
      }
      if (presaleIds.contains(allocation.getPresaleOrderId())) {
        throw new RuntimeException("同一张预销售单不能重复分摊");
      }
      presaleIds.add(allocation.getPresaleOrderId());
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
    BigDecimal ccbAmount = extractAmountNearLabel(text, "小写金额");
    if (ccbAmount != null) {
      return ccbAmount;
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
    Matcher matcher = DECIMAL_AMOUNT_PATTERN.matcher(nearbyText);
    if (!matcher.find()) {
      return null;
    }
    try {
      return money(new BigDecimal(matcher.group(1).replace(",", "")));
    } catch (Exception ignored) {
      return null;
    }
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
    List<String> values = new ArrayList<String>();
    for (int index = 0; index < lines.size() && values.size() < maxCount; index++) {
      String line = lines.get(index);
      if (!StringUtils.contains(line, marker)) {
        continue;
      }
      String inlineValue = StringUtils.trimToEmpty(StringUtils.substringAfter(line, marker));
      if (StringUtils.isNotBlank(inlineValue)) {
        values.add(inlineValue);
      }
      for (int offset = 1; offset <= 6 && index + offset < lines.size() && values.size() < maxCount; offset++) {
        String candidate = lines.get(index + offset);
        if (isReceiptMarker(candidate) || candidate.matches("^\\d{6,}$")) {
          continue;
        }
        if (StringUtils.contains(candidate, marker)) {
          candidate = StringUtils.trimToEmpty(StringUtils.substringAfter(candidate, marker));
        }
        if (StringUtils.isNotBlank(candidate)) {
          values.add(candidate);
        }
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
    for (int index = 0; index < lines.size(); index++) {
      if (!StringUtils.equals(lines.get(index), marker)) {
        continue;
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
        "用途", "钞汇标志", "摘要", "币别：", "日期：", "凭证号：", "账户明细编号-交易流水号："};
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

  private BigDecimal money(BigDecimal value) {
    return (value == null ? BigDecimal.ZERO : value).setScale(2, RoundingMode.HALF_UP);
  }

  private BigDecimal rate(BigDecimal value) {
    return (value == null ? BigDecimal.ZERO : value).setScale(10, RoundingMode.HALF_UP);
  }

  private BigDecimal decimal10(BigDecimal value) {
    return (value == null ? BigDecimal.ZERO : value).setScale(10, RoundingMode.HALF_UP);
  }
}
