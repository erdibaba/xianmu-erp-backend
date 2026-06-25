package io.renren.modules.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.renren.common.utils.PageUtils;
import io.renren.modules.erp.entity.ErpManualExpenseEntity;
import io.renren.modules.erp.entity.ErpManualExpenseFileEntity;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface ErpManualExpenseService extends IService<ErpManualExpenseEntity> {
  PageUtils queryPage(Map<String, Object> params);

  ErpManualExpenseEntity getDetail(Long id);

  void saveExpense(ErpManualExpenseEntity expense, Long userId);

  void updateExpense(ErpManualExpenseEntity expense);

  void deleteExpenses(Long[] ids);

  List<ErpManualExpenseFileEntity> uploadFiles(Long expenseId, MultipartFile[] files) throws Exception;

  void deleteFile(Long fileId);

  ResponseEntity<byte[]> downloadFile(Long fileId);
}
