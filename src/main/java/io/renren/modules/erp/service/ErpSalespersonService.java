package io.renren.modules.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.renren.common.utils.PageUtils;
import io.renren.modules.erp.entity.ErpSalespersonEntity;
import java.util.Map;

public interface ErpSalespersonService extends IService<ErpSalespersonEntity> {
  PageUtils queryPage(Map<String, Object> params);

  void saveSalesperson(ErpSalespersonEntity salesperson, Long userId);

  void updateSalesperson(ErpSalespersonEntity salesperson);
}
