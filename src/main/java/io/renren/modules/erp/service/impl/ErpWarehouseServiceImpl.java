package io.renren.modules.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.renren.common.utils.PageUtils;
import io.renren.common.utils.Query;
import io.renren.modules.erp.dao.ErpWarehouseFeeRateDao;
import io.renren.modules.erp.dao.ErpWarehouseDao;
import io.renren.modules.erp.entity.ErpWarehouseEntity;
import io.renren.modules.erp.entity.ErpWarehouseFeeRateEntity;
import io.renren.modules.erp.service.ErpWarehouseService;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("erpWarehouseService")
public class ErpWarehouseServiceImpl extends ServiceImpl<ErpWarehouseDao, ErpWarehouseEntity> implements ErpWarehouseService {
  @Autowired
  private ErpWarehouseFeeRateDao erpWarehouseFeeRateDao;

  @Override
  public PageUtils queryPage(Map<String, Object> params) {
    String keyword = (String) params.get("keyword");
    QueryWrapper<ErpWarehouseEntity> wrapper = new QueryWrapper<ErpWarehouseEntity>().orderByAsc("warehouse_name", "id");
    if (StringUtils.isNotBlank(keyword)) {
      wrapper.and(w -> w.like("warehouse_code", keyword).or().like("warehouse_name", keyword).or().like("address", keyword));
    }
    IPage<ErpWarehouseEntity> page = this.page(new Query<ErpWarehouseEntity>().getPage(params), wrapper);
    fillEffectiveRates(page.getRecords());
    return new PageUtils(page);
  }

  @Override
  public List<ErpWarehouseEntity> queryAll() {
    List<ErpWarehouseEntity> list = this.list(new QueryWrapper<ErpWarehouseEntity>().eq("status", 1).orderByAsc("warehouse_name", "id"));
    fillEffectiveRates(list);
    return list;
  }

  private void fillEffectiveRates(List<ErpWarehouseEntity> warehouses) {
    if (warehouses == null || warehouses.isEmpty()) {
      return;
    }
    Date today = new Date();
    for (ErpWarehouseEntity warehouse : warehouses) {
      ErpWarehouseFeeRateEntity rate = erpWarehouseFeeRateDao.selectOne(new QueryWrapper<ErpWarehouseFeeRateEntity>()
          .eq("warehouse_id", warehouse.getId())
          .eq("status", 1)
          .le("effective_date", today)
          .orderByDesc("effective_date")
          .orderByDesc("id")
          .last("limit 1"));
      if (rate != null) {
        warehouse.setFrozenStorageFee(rate.getFrozenStorageFee());
        warehouse.setChilledStorageFee(rate.getChilledStorageFee());
        warehouse.setFrozenColdFee(rate.getFrozenColdFee());
        warehouse.setChilledColdFee(rate.getChilledColdFee());
        warehouse.setFeeUnit("元/吨");
      }
    }
  }
}
