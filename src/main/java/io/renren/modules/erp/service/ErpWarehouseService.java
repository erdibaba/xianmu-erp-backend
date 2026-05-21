package io.renren.modules.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.renren.common.utils.PageUtils;
import io.renren.modules.erp.entity.ErpWarehouseEntity;
import java.util.List;
import java.util.Map;

public interface ErpWarehouseService extends IService<ErpWarehouseEntity> {
  PageUtils queryPage(Map<String, Object> params);
  List<ErpWarehouseEntity> queryAll();
}
