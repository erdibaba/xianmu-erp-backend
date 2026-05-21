package io.renren.modules.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.renren.common.utils.PageUtils;
import io.renren.common.utils.Query;
import io.renren.modules.erp.dao.ErpWarehouseDao;
import io.renren.modules.erp.entity.ErpWarehouseEntity;
import io.renren.modules.erp.service.ErpWarehouseService;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

@Service("erpWarehouseService")
public class ErpWarehouseServiceImpl extends ServiceImpl<ErpWarehouseDao, ErpWarehouseEntity> implements ErpWarehouseService {
  @Override
  public PageUtils queryPage(Map<String, Object> params) {
    String keyword = (String) params.get("keyword");
    QueryWrapper<ErpWarehouseEntity> wrapper = new QueryWrapper<ErpWarehouseEntity>().orderByAsc("warehouse_name", "id");
    if (StringUtils.isNotBlank(keyword)) {
      wrapper.and(w -> w.like("warehouse_code", keyword).or().like("warehouse_name", keyword).or().like("address", keyword));
    }
    IPage<ErpWarehouseEntity> page = this.page(new Query<ErpWarehouseEntity>().getPage(params), wrapper);
    return new PageUtils(page);
  }

  @Override
  public List<ErpWarehouseEntity> queryAll() {
    return this.list(new QueryWrapper<ErpWarehouseEntity>().eq("status", 1).orderByAsc("warehouse_name", "id"));
  }
}
