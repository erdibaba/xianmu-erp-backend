package io.renren.modules.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.renren.common.utils.PageUtils;
import io.renren.modules.erp.entity.ErpExpenseEntity;
import io.renren.modules.erp.entity.ErpInboundOrderEntity;
import java.util.List;
import java.util.Map;

public interface ErpExpenseService extends IService<ErpExpenseEntity> {
  String TYPE_INBOUND_HANDLING = "INBOUND_HANDLING";
  String TYPE_OUTBOUND_STORAGE = "OUTBOUND_STORAGE";
  String SOURCE_INBOUND_ORDER = "INBOUND_ORDER";
  String SOURCE_OUTBOUND_BATCH = "OUTBOUND_BATCH";

  PageUtils queryPage(Map<String, Object> params);

  List<ErpExpenseEntity> listBySource(String sourceType, Long sourceId);

  void regenerateInboundHandlingExpense(ErpInboundOrderEntity order, Long userId);

  void regenerateOutboundStorageExpense(Long outboundBatchId, Long userId);

  void deleteBySource(String expenseType, String sourceType, Long sourceId);
}
