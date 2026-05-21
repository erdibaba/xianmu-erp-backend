package io.renren.modules.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.renren.common.utils.PageUtils;
import io.renren.modules.erp.entity.ErpProductEntity;
import java.util.List;
import java.util.Map;

public interface ErpProductService extends IService<ErpProductEntity> {
  PageUtils queryPage(Map<String, Object> params);
  List<ErpProductEntity> queryAll();
  PageUtils querySelectPage(Map<String, Object> params);
}
