package io.renren.modules.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.renren.common.utils.PageUtils;
import io.renren.modules.erp.entity.ErpDriverEntity;
import java.util.Map;

public interface ErpDriverService extends IService<ErpDriverEntity> {
  PageUtils queryPage(Map<String, Object> params);

  void saveDriver(ErpDriverEntity driver, Long userId);

  void updateDriver(ErpDriverEntity driver);
}
