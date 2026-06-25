package io.renren.modules.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.renren.common.exception.RRException;
import io.renren.common.utils.PageUtils;
import io.renren.common.utils.Query;
import io.renren.modules.erp.dao.ErpManualExpenseDao;
import io.renren.modules.erp.dao.ErpManualExpenseFileDao;
import io.renren.modules.erp.entity.ErpManualExpenseEntity;
import io.renren.modules.erp.entity.ErpManualExpenseFileEntity;
import io.renren.modules.erp.service.ErpManualExpenseService;
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
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service("erpManualExpenseService")
public class ErpManualExpenseServiceImpl extends ServiceImpl<ErpManualExpenseDao, ErpManualExpenseEntity> implements ErpManualExpenseService {
  private static final String UPLOAD_BASE_DIR = "D:\\renren-fast-vue\\renren-fast\\uploads\\manual-expense";

  @Autowired
  private ErpManualExpenseFileDao erpManualExpenseFileDao;

  @Override
  public PageUtils queryPage(Map<String, Object> params) {
    QueryWrapper<ErpManualExpenseEntity> wrapper = new QueryWrapper<ErpManualExpenseEntity>();
    String keyword = stringValue(params.get("keyword"));
    if (StringUtils.isNotBlank(keyword)) {
      wrapper.and(w -> w.like("expense_no", keyword)
          .or().like("expense_type", keyword)
          .or().like("expense_name", keyword)
          .or().like("remark", keyword));
    }
    String expenseType = stringValue(params.get("expenseType"));
    if (StringUtils.isNotBlank(expenseType)) {
      wrapper.eq("expense_type", expenseType);
    }
    String dateStart = stringValue(params.get("dateStart"));
    if (StringUtils.isNotBlank(dateStart)) {
      wrapper.ge("expense_date", dateStart);
    }
    String dateEnd = stringValue(params.get("dateEnd"));
    if (StringUtils.isNotBlank(dateEnd)) {
      wrapper.le("expense_date", dateEnd);
    }
    wrapper.orderByDesc("expense_date").orderByDesc("id");
    IPage<ErpManualExpenseEntity> page = this.page(new Query<ErpManualExpenseEntity>().getPage(params), wrapper);
    List<ErpManualExpenseEntity> list = page.getRecords();
    if (list != null) {
      for (ErpManualExpenseEntity expense : list) {
        expense.setFileList(listFiles(expense.getId()));
      }
    }
    return new PageUtils(page);
  }

  @Override
  public ErpManualExpenseEntity getDetail(Long id) {
    ErpManualExpenseEntity expense = id == null ? null : this.getById(id);
    if (expense == null) {
      throw new RRException("费用记录不存在");
    }
    expense.setFileList(listFiles(expense.getId()));
    return expense;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void saveExpense(ErpManualExpenseEntity expense, Long userId) {
    normalizeAndValidate(expense);
    Date now = new Date();
    expense.setId(null);
    expense.setExpenseNo(buildExpenseNo(now));
    expense.setAmount(money(expense.getAmount()));
    expense.setCreateUserId(userId);
    expense.setCreateTime(now);
    expense.setUpdateTime(now);
    this.save(expense);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void updateExpense(ErpManualExpenseEntity expense) {
    if (expense == null || expense.getId() == null) {
      throw new RRException("费用记录ID不能为空");
    }
    ErpManualExpenseEntity existing = this.getById(expense.getId());
    if (existing == null) {
      throw new RRException("费用记录不存在");
    }
    normalizeAndValidate(expense);
    expense.setExpenseNo(existing.getExpenseNo());
    expense.setCreateUserId(existing.getCreateUserId());
    expense.setCreateTime(existing.getCreateTime());
    expense.setAmount(money(expense.getAmount()));
    expense.setUpdateTime(new Date());
    this.updateById(expense);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void deleteExpenses(Long[] ids) {
    if (ids == null || ids.length == 0) {
      return;
    }
    for (Long id : ids) {
      if (id == null) {
        continue;
      }
      List<ErpManualExpenseFileEntity> files = listFiles(id);
      for (ErpManualExpenseFileEntity file : files) {
        deletePhysicalFile(file.getFilePath());
      }
      erpManualExpenseFileDao.delete(new QueryWrapper<ErpManualExpenseFileEntity>().eq("expense_id", id));
      this.removeById(id);
    }
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public List<ErpManualExpenseFileEntity> uploadFiles(Long expenseId, MultipartFile[] files) throws Exception {
    if (expenseId == null || this.getById(expenseId) == null) {
      throw new RRException("请先保存费用记录");
    }
    if (files == null || files.length == 0) {
      throw new RRException("请先选择附件");
    }
    Date now = new Date();
    int lineNo = nextLineNo(expenseId);
    List<ErpManualExpenseFileEntity> saved = new ArrayList<ErpManualExpenseFileEntity>();
    for (MultipartFile file : files) {
      if (file == null || file.isEmpty()) {
        continue;
      }
      Path target = saveFile(file);
      ErpManualExpenseFileEntity entity = new ErpManualExpenseFileEntity();
      entity.setExpenseId(expenseId);
      entity.setLineNo(lineNo++);
      entity.setFilePath(target.toString());
      entity.setFileName(StringUtils.defaultIfBlank(file.getOriginalFilename(), target.getFileName().toString()));
      entity.setCreateTime(now);
      entity.setUpdateTime(now);
      erpManualExpenseFileDao.insert(entity);
      saved.add(entity);
    }
    return saved;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void deleteFile(Long fileId) {
    ErpManualExpenseFileEntity file = fileId == null ? null : erpManualExpenseFileDao.selectById(fileId);
    if (file == null) {
      throw new RRException("附件不存在");
    }
    deletePhysicalFile(file.getFilePath());
    erpManualExpenseFileDao.deleteById(fileId);
  }

  @Override
  public ResponseEntity<byte[]> downloadFile(Long fileId) {
    ErpManualExpenseFileEntity file = fileId == null ? null : erpManualExpenseFileDao.selectById(fileId);
    if (file == null || StringUtils.isBlank(file.getFilePath())) {
      throw new RRException("附件不存在");
    }
    try {
      File localFile = new File(file.getFilePath());
      if (!localFile.exists()) {
        throw new RRException("附件文件不存在");
      }
      byte[] bytes = Files.readAllBytes(localFile.toPath());
      String downloadName = StringUtils.defaultIfBlank(file.getFileName(), localFile.getName());
      return ResponseEntity.ok()
          .contentType(MediaType.APPLICATION_OCTET_STREAM)
          .header(HttpHeaders.CONTENT_DISPOSITION,
              ContentDisposition.attachment().filename(downloadName, StandardCharsets.UTF_8).build().toString())
          .body(bytes);
    } catch (RRException e) {
      throw e;
    } catch (Exception e) {
      throw new RRException("附件下载失败: " + e.getMessage());
    }
  }

  private void normalizeAndValidate(ErpManualExpenseEntity expense) {
    if (expense == null) {
      throw new RRException("费用记录不能为空");
    }
    expense.setExpenseType(StringUtils.trim(expense.getExpenseType()));
    expense.setExpenseName(StringUtils.trim(expense.getExpenseName()));
    expense.setRemark(StringUtils.trim(expense.getRemark()));
    if (expense.getExpenseDate() == null) {
      throw new RRException("费用日期不能为空");
    }
    if (StringUtils.isBlank(expense.getExpenseName())) {
      throw new RRException("费用名称不能为空");
    }
    if (expense.getAmount() == null || expense.getAmount().compareTo(BigDecimal.ZERO) < 0) {
      throw new RRException("支出金额不能小于0");
    }
  }

  private List<ErpManualExpenseFileEntity> listFiles(Long expenseId) {
    if (expenseId == null) {
      return new ArrayList<ErpManualExpenseFileEntity>();
    }
    List<ErpManualExpenseFileEntity> list = erpManualExpenseFileDao.selectList(new QueryWrapper<ErpManualExpenseFileEntity>()
        .eq("expense_id", expenseId)
        .orderByAsc("line_no")
        .orderByAsc("id"));
    return list == null ? new ArrayList<ErpManualExpenseFileEntity>() : list;
  }

  private int nextLineNo(Long expenseId) {
    ErpManualExpenseFileEntity last = erpManualExpenseFileDao.selectOne(new QueryWrapper<ErpManualExpenseFileEntity>()
        .eq("expense_id", expenseId)
        .orderByDesc("line_no")
        .last("limit 1"));
    return last == null || last.getLineNo() == null ? 1 : last.getLineNo() + 1;
  }

  private Path saveFile(MultipartFile file) throws Exception {
    String dayFolder = new SimpleDateFormat("yyyyMMdd").format(new Date());
    Path dir = Paths.get(UPLOAD_BASE_DIR, dayFolder);
    Files.createDirectories(dir);
    String originalName = StringUtils.defaultString(file.getOriginalFilename(), "expense-file");
    String safeName = originalName.replaceAll("[\\\\/:*?\"<>|\\s]+", "_");
    if (StringUtils.isBlank(safeName)) {
      safeName = "expense-file";
    }
    Path target = dir.resolve(System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + "_" + safeName);
    Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
    return target;
  }

  private void deletePhysicalFile(String filePath) {
    if (StringUtils.isBlank(filePath)) {
      return;
    }
    try {
      Files.deleteIfExists(Paths.get(filePath));
    } catch (Exception ignored) {
      // Physical cleanup failure should not block deleting the business record.
    }
  }

  private String buildExpenseNo(Date now) {
    return "ME" + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(now);
  }

  private BigDecimal money(BigDecimal value) {
    return value == null ? BigDecimal.ZERO : value.setScale(2, RoundingMode.HALF_UP);
  }

  private String stringValue(Object value) {
    return value == null ? null : String.valueOf(value).trim();
  }
}
