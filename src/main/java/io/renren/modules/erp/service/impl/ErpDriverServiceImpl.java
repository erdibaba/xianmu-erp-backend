package io.renren.modules.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.renren.common.exception.RRException;
import io.renren.common.utils.PageUtils;
import io.renren.common.utils.Query;
import io.renren.modules.erp.dao.ErpDriverDao;
import io.renren.modules.erp.entity.ErpDriverEntity;
import io.renren.modules.erp.service.ErpDriverService;
import java.util.Date;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

@Service("erpDriverService")
public class ErpDriverServiceImpl extends ServiceImpl<ErpDriverDao, ErpDriverEntity> implements ErpDriverService {
  @Override
  public PageUtils queryPage(Map<String, Object> params) {
    String keyword = (String) params.get("keyword");
    QueryWrapper<ErpDriverEntity> wrapper = new QueryWrapper<ErpDriverEntity>()
        .orderByDesc("status")
        .orderByAsc("driver_name")
        .orderByAsc("id");
    if (StringUtils.isNotBlank(keyword)) {
      String value = keyword.trim();
      wrapper.and(w -> w.like("driver_name", value).or().like("plate_no", value).or().like("mobile", value).or().like("id_card_no", value));
    }
    IPage<ErpDriverEntity> page = this.page(new Query<ErpDriverEntity>().getPage(params), wrapper);
    return new PageUtils(page);
  }

  @Override
  public void saveDriver(ErpDriverEntity driver, Long userId) {
    normalizeAndValidate(driver, null);
    Date now = new Date();
    driver.setId(null);
    driver.setStatus(driver.getStatus() == null ? 1 : driver.getStatus());
    driver.setCreateUserId(userId);
    driver.setCreateTime(now);
    driver.setUpdateTime(now);
    this.save(driver);
  }

  @Override
  public void updateDriver(ErpDriverEntity driver) {
    if (driver.getId() == null) {
      throw new RRException("司机信息ID不能为空");
    }
    normalizeAndValidate(driver, driver.getId());
    driver.setUpdateTime(new Date());
    this.updateById(driver);
  }

  private void normalizeAndValidate(ErpDriverEntity driver, Long excludeId) {
    if (driver == null) {
      throw new RRException("司机信息不能为空");
    }
    driver.setDriverName(StringUtils.trim(driver.getDriverName()));
    driver.setPlateNo(StringUtils.upperCase(StringUtils.trim(driver.getPlateNo())));
    driver.setMobile(StringUtils.trim(driver.getMobile()));
    driver.setIdCardNo(StringUtils.trim(driver.getIdCardNo()));
    if (StringUtils.isBlank(driver.getDriverName())) {
      throw new RRException("司机姓名不能为空");
    }
    if (StringUtils.isBlank(driver.getPlateNo())) {
      throw new RRException("车牌号不能为空");
    }
    if (StringUtils.isBlank(driver.getMobile())) {
      throw new RRException("手机号不能为空");
    }
    QueryWrapper<ErpDriverEntity> wrapper = new QueryWrapper<ErpDriverEntity>().eq("plate_no", driver.getPlateNo());
    if (excludeId != null) {
      wrapper.ne("id", excludeId);
    }
    if (this.count(wrapper) > 0) {
      throw new RRException("车牌号已存在");
    }
  }
}
