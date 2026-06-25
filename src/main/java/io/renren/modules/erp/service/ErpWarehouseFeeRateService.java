package io.renren.modules.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.renren.modules.erp.entity.ErpWarehouseFeeRateEntity;
import java.util.Date;
import java.util.List;

public interface ErpWarehouseFeeRateService extends IService<ErpWarehouseFeeRateEntity> {
  List<ErpWarehouseFeeRateEntity> listByWarehouseId(Long warehouseId);

  ErpWarehouseFeeRateEntity getEffectiveRate(Long warehouseId, Date businessDate);

  void saveRate(ErpWarehouseFeeRateEntity rate, Long userId);

  void updateRate(ErpWarehouseFeeRateEntity rate);

  void deleteRate(Long id);
}
