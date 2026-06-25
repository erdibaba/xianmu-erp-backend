package io.renren.modules.erp.service;

import io.renren.common.utils.PageUtils;
import io.renren.modules.erp.entity.ErpPresaleAttachmentEntity;
import io.renren.modules.erp.entity.ErpPresaleOrderEntity;
import io.renren.modules.erp.vo.ErpRecognizedPackingDraftVo;
import io.renren.modules.erp.vo.ErpRecognizedOrderDraftVo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.Map;

public interface ErpPresaleOrderService {
  PageUtils queryPage(Map<String, Object> params);

  PageUtils queryConfirmPage(Map<String, Object> params);

  ErpPresaleOrderEntity getDetail(Long id);

  ErpPresaleOrderEntity getDetail(Long id, Long confirmId);

  void saveOrder(ErpPresaleOrderEntity order, Long userId);

  void updateOrder(ErpPresaleOrderEntity order, Long userId);

  void deleteOrders(Long[] ids);

  void syncConfirmProductMaster(ErpRecognizedOrderDraftVo draft);

  void syncPackingProductMaster(ErpRecognizedPackingDraftVo draft);

  ErpPresaleAttachmentEntity recognizeCustomsAttachment(MultipartFile file) throws Exception;

  ErpPresaleAttachmentEntity uploadAttachment(Long presaleOrderId, Long confirmId, String attachmentType, MultipartFile file, Long userId, boolean overwriteExisting, BigDecimal confirmedGrossWeight) throws Exception;

  ResponseEntity<byte[]> downloadEstimateFile(Long id);

  ResponseEntity<byte[]> downloadConfirmFile(Long id);

  ResponseEntity<byte[]> downloadConfirmFileByConfirmId(Long confirmId);

  ResponseEntity<byte[]> downloadPackingFile(Long id);

  ResponseEntity<byte[]> downloadPackingFileByConfirmId(Long confirmId);

  ResponseEntity<byte[]> downloadAttachmentFile(Long id, String attachmentType);

  ResponseEntity<byte[]> downloadAttachmentFileByConfirmId(Long confirmId, String attachmentType);

  ResponseEntity<byte[]> downloadAttachmentFileById(Long attachmentId);
}
