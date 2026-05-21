package io.renren.modules.erp.service;

import io.renren.modules.erp.vo.ErpRecognizeResultVo;
import org.springframework.web.multipart.MultipartFile;

public interface ErpOcrService {
  ErpRecognizeResultVo recognize(MultipartFile file, String orderTypeHint) throws Exception;
}
