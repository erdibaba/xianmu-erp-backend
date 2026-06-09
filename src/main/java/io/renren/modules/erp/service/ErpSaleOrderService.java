package io.renren.modules.erp.service;

import io.renren.common.utils.PageUtils;
import io.renren.modules.erp.entity.ErpSaleOrderFileEntity;
import io.renren.modules.erp.entity.ErpSaleOrderEntity;
import io.renren.modules.erp.entity.ErpSaleOrderItemEntity;
import io.renren.modules.erp.entity.ErpSaleOutboundBatchEntity;
import io.renren.modules.erp.entity.ErpSaleOutboundReceiptEntity;
import io.renren.modules.erp.vo.ErpSalePresaleItemVo;
import io.renren.modules.erp.vo.ErpSalePresaleOrderVo;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface ErpSaleOrderService {
  PageUtils queryPage(Map<String, Object> params);

  ErpSaleOrderEntity getDetail(Long id);

  void saveOrder(ErpSaleOrderEntity order, Long userId);

  void updateOrder(ErpSaleOrderEntity order, Long userId);

  void updatePresaleLink(Long saleOrderId, Long presaleOrderId, Long userId);

  void confirmPresaleLink(Long saleOrderId, Long userId);

  void deleteOrders(Long[] ids);

  List<ErpSalePresaleOrderVo> queryPresaleOrders(String keyword);

  List<ErpSalePresaleItemVo> queryPresaleItems(Long productId, String keyword);

  List<ErpSaleOrderItemEntity> previewAllocation(ErpSaleOrderEntity order);

  List<ErpSaleOrderFileEntity> uploadFiles(Long saleOrderId, String fileType, MultipartFile[] files) throws Exception;

  List<ErpSaleOrderFileEntity> uploadFiles(Long saleOrderId, String fileType, MultipartFile[] files, Long userId) throws Exception;

  ErpSaleOutboundReceiptEntity recognizeOutboundReceipt(Long saleOrderId, MultipartFile[] files, Long userId) throws Exception;

  ErpSaleOutboundReceiptEntity recognizeOutboundReceipt(Long saleOrderId, Long batchId, MultipartFile[] files, Long userId) throws Exception;

  ErpSaleOutboundReceiptEntity saveOutboundReceipt(ErpSaleOutboundReceiptEntity receipt, Long userId);

  List<ErpSaleOutboundBatchEntity> queryOutboundBatches(Long saleOrderId);

  ErpSaleOutboundBatchEntity createOutboundBatch(Long saleOrderId, Long userId);

  ErpSaleOutboundBatchEntity uploadOutboundBatchBankSlip(Long saleOrderId, Long batchId, MultipartFile[] files, Long userId) throws Exception;

  ErpSaleOutboundBatchEntity bindOutboundBatchScanLink(Long saleOrderId, Long batchId, String scanUrl, Long userId) throws Exception;

  ErpSaleOutboundBatchEntity confirmOutboundBatch(Long batchId, Long userId);

  void voidOutboundBatch(Long batchId, Long userId);

  ResponseEntity<byte[]> downloadFile(Long fileId);

  void deleteFile(Long fileId, Long userId);

  ResponseEntity<byte[]> downloadPortalFile(Long fileId, Long userId);

  String buildContractHtml(String token);

  ResponseEntity<byte[]> downloadContractPdf(String token);

  ErpSaleOrderEntity getPortalDetail(String token, Long userId);

  List<ErpSaleOrderFileEntity> uploadPortalFiles(String token, String fileType, MultipartFile[] files, Long userId) throws Exception;

  void deletePortalFile(Long fileId, Long userId);

  void confirmPortalStep(String token, String fileType, Long userId);

  void confirmInternalStep(Long saleOrderId, String fileType, Long userId);
}
