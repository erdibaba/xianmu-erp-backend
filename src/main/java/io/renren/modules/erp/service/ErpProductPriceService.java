package io.renren.modules.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.renren.common.utils.PageUtils;
import io.renren.modules.erp.entity.ErpProductPriceEntity;
import java.util.Map;

public interface ErpProductPriceService extends IService<ErpProductPriceEntity> {
  PageUtils queryPage(Map<String, Object> params);
}
