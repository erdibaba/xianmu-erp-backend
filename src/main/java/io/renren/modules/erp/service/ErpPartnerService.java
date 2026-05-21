package io.renren.modules.erp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.renren.common.utils.PageUtils;
import io.renren.modules.erp.entity.ErpPartnerEntity;
import java.util.List;
import java.util.Map;

public interface ErpPartnerService extends IService<ErpPartnerEntity> {
  PageUtils queryPage(Map<String, Object> params);
  List<ErpPartnerEntity> queryAll();
  List<ErpPartnerEntity> queryByRole(String businessRole);
}
