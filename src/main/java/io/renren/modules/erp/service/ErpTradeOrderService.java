package io.renren.modules.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.renren.common.utils.PageUtils;
import io.renren.modules.erp.entity.ErpTradeOrderEntity;
import java.util.Map;
import org.springframework.http.ResponseEntity;

public interface ErpTradeOrderService extends IService<ErpTradeOrderEntity> {
  PageUtils queryPage(Map<String, Object> params, String orderType);
  ErpTradeOrderEntity getDetail(Long id);
  void saveOrder(ErpTradeOrderEntity order, Long userId);
  void updateOrder(ErpTradeOrderEntity order, Long userId);
  void deleteOrders(Long[] ids);
  ResponseEntity<byte[]> exportFinanceStatement(Map<String, Object> params, String orderType);
}
