package io.renren.modules.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.renren.common.exception.RRException;
import io.renren.modules.erp.dao.ErpWarehouseFeeRateDao;
import io.renren.modules.erp.entity.ErpWarehouseEntity;
import io.renren.modules.erp.entity.ErpWarehouseFeeRateEntity;
import io.renren.modules.erp.service.ErpWarehouseFeeRateService;
import io.renren.modules.erp.service.ErpWarehouseService;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("erpWarehouseFeeRateService")
public class ErpWarehouseFeeRateServiceImpl extends ServiceImpl<ErpWarehouseFeeRateDao, ErpWarehouseFeeRateEntity> implements ErpWarehouseFeeRateService {
  @Autowired
  private ErpWarehouseService erpWarehouseService;

  @Override
  public List<ErpWarehouseFeeRateEntity> listByWarehouseId(Long warehouseId) {
    return this.list(new QueryWrapper<ErpWarehouseFeeRateEntity>()
        .eq("warehouse_id", warehouseId)
        .orderByDesc("effective_date")
        .orderByDesc("id"));
  }

  @Override
  public ErpWarehouseFeeRateEntity getEffectiveRate(Long warehouseId, Date businessDate) {
    Date date = businessDate == null ? new Date() : businessDate;
    return this.getOne(new QueryWrapper<ErpWarehouseFeeRateEntity>()
        .eq("warehouse_id", warehouseId)
        .eq("status", 1)
        .le("effective_date", date)
        .orderByDesc("effective_date")
        .orderByDesc("id")
        .last("limit 1"));
  }

  @Override
  public void saveRate(ErpWarehouseFeeRateEntity rate, Long userId) {
    validateRate(rate, null);
    fillWarehouseName(rate);
    rate.setCreateUserId(userId);
    rate.setCreateTime(new Date());
    rate.setUpdateTime(new Date());
    this.save(rate);
  }

  @Override
  public void updateRate(ErpWarehouseFeeRateEntity rate) {
    if (rate.getId() == null) {
      throw new RRException("费用记录ID不能为空");
    }
    validateRate(rate, rate.getId());
    fillWarehouseName(rate);
    rate.setUpdateTime(new Date());
    this.updateById(rate);
  }

  private void validateRate(ErpWarehouseFeeRateEntity rate, Long excludeId) {
    if (rate.getWarehouseId() == null) {
      throw new RRException("仓库不能为空");
    }
    if (rate.getEffectiveDate() == null) {
      throw new RRException("生效日期不能为空");
    }
    if (StringUtils.isBlank(rate.getScanFeeUnit())) {
      rate.setScanFeeUnit("TON");
    }
    if (!"BOX".equals(rate.getScanFeeUnit()) && !"TON".equals(rate.getScanFeeUnit())) {
      throw new RRException("扫码费计费方式不正确");
    }
    if (rate.getScanFeeRate() == null) {
      rate.setScanFeeRate(BigDecimal.ZERO);
    }
    QueryWrapper<ErpWarehouseFeeRateEntity> wrapper = new QueryWrapper<ErpWarehouseFeeRateEntity>()
        .eq("warehouse_id", rate.getWarehouseId())
        .eq("effective_date", rate.getEffectiveDate());
    if (excludeId != null) {
      wrapper.ne("id", excludeId);
    }
    if (this.count(wrapper) > 0) {
      throw new RRException("同一仓库同一生效日期已存在费用记录");
    }
  }

  private void fillWarehouseName(ErpWarehouseFeeRateEntity rate) {
    ErpWarehouseEntity warehouse = erpWarehouseService.getById(rate.getWarehouseId());
    if (warehouse == null) {
      throw new RRException("仓库不存在");
    }
    rate.setWarehouseName(warehouse.getWarehouseName());
  }
}
