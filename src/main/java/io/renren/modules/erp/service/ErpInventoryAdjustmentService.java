package io.renren.modules.erp.service;

import io.renren.modules.erp.vo.ErpInventoryAdjustmentRequest;
import io.renren.modules.erp.vo.ErpInventoryAdjustmentRecognizeVo;
import io.renren.modules.erp.vo.ErpInventoryBatchVo;
import java.util.List;
import java.util.Map;
import org.springframework.web.multipart.MultipartFile;

public interface ErpInventoryAdjustmentService {
  List<ErpInventoryBatchVo> queryAvailableLots(Map<String, Object> params);

  ErpInventoryAdjustmentRecognizeVo recognizeAdjustment(String adjustmentType, MultipartFile[] files) throws Exception;

  void saveAdjustment(ErpInventoryAdjustmentRequest request, Long userId);
}
