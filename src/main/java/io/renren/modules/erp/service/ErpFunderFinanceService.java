package io.renren.modules.erp.service;

import io.renren.common.utils.PageUtils;
import io.renren.modules.erp.entity.ErpFunderLoanEntity;
import io.renren.modules.erp.entity.ErpFunderLoanRepaymentEntity;
import io.renren.modules.erp.entity.ErpFunderBatchSettlementEntity;
import io.renren.modules.erp.entity.ErpFunderPaymentEntity;
import io.renren.modules.erp.entity.ErpPartnerEntity;
import io.renren.modules.erp.entity.ErpSaleOutboundBatchEntity;
import io.renren.modules.erp.entity.ErpPresaleOrderEntity;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface ErpFunderFinanceService {
  PageUtils queryPaymentPage(Map<String, Object> params);

  ErpFunderPaymentEntity getPaymentDetail(Long id);

  List<ErpPartnerEntity> queryFunderOptions(String keyword);

  List<ErpPartnerEntity> queryInternalPayerOptions(String keyword);

  List<ErpPresaleOrderEntity> queryPresaleOptions(String keyword);

  Map<String, Object> recognizeVoucher(MultipartFile file) throws Exception;

  void confirmPayment(ErpFunderPaymentEntity payment, Long userId);

  PageUtils queryLoanPage(Map<String, Object> params);

  ErpFunderLoanEntity getLoanDetail(Long id);

  ErpFunderLoanRepaymentEntity calculateRepayment(ErpFunderLoanRepaymentEntity repayment);

  void extendLoanDueDate(ErpFunderLoanEntity loan, Long userId);

  void confirmRepayment(ErpFunderLoanRepaymentEntity repayment, Long userId);

  List<ErpSaleOutboundBatchEntity> querySettleableOutboundBatches(String keyword);

  ErpFunderBatchSettlementEntity calculateBatchSettlement(ErpFunderBatchSettlementEntity settlement);

  void confirmBatchSettlement(ErpFunderBatchSettlementEntity settlement, Long userId);

  ResponseEntity<byte[]> downloadPaymentVoucher(Long id);

  ResponseEntity<byte[]> downloadContributionVoucher(Long allocationId);

  ResponseEntity<byte[]> downloadRepaymentVoucher(Long id);
}
