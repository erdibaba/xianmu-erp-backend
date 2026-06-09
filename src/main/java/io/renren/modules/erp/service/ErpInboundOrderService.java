package io.renren.modules.erp.service;

import io.renren.common.utils.PageUtils;
import io.renren.modules.erp.entity.ErpInboundOrderEntity;
import io.renren.modules.erp.vo.ErpRecognizedInboundResultVo;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface ErpInboundOrderService {
  PageUtils queryPage(Map<String, Object> params);

  ErpInboundOrderEntity getDetail(Long presaleOrderId);

  Map<String, Integer> getPackingBoxMap(Long presaleOrderId);

  void saveOrder(ErpInboundOrderEntity order, Long userId);

  void updateOrder(ErpInboundOrderEntity order, Long userId);

  void saveItemDamage(Long itemId, java.math.BigDecimal damageWeightKg, String damageReason);

  ErpRecognizedInboundResultVo recognize(Long presaleOrderId, MultipartFile[] files) throws Exception;

  ResponseEntity<byte[]> downloadFile(Long fileId);
}
