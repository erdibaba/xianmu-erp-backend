package io.renren.modules.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.renren.common.exception.RRException;
import io.renren.common.utils.PageUtils;
import io.renren.common.utils.Query;
import io.renren.modules.erp.dao.ErpSalespersonDao;
import io.renren.modules.erp.entity.ErpSalespersonEntity;
import io.renren.modules.erp.service.ErpSalespersonService;
import io.renren.modules.sys.dao.SysUserDao;
import io.renren.modules.sys.entity.SysUserEntity;
import java.util.Date;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("erpSalespersonService")
public class ErpSalespersonServiceImpl extends ServiceImpl<ErpSalespersonDao, ErpSalespersonEntity> implements ErpSalespersonService {
  @Autowired
  private SysUserDao sysUserDao;

  @Override
  public PageUtils queryPage(Map<String, Object> params) {
    String keyword = (String) params.get("keyword");
    QueryWrapper<ErpSalespersonEntity> wrapper = new QueryWrapper<ErpSalespersonEntity>()
        .orderByDesc("status")
        .orderByAsc("sales_name")
        .orderByAsc("id");
    if (StringUtils.isNotBlank(keyword)) {
      String value = keyword.trim();
      wrapper.and(w -> w.like("sales_name", value).or().like("mobile", value).or().like("sys_username", value));
    }
    IPage<ErpSalespersonEntity> page = this.page(new Query<ErpSalespersonEntity>().getPage(params), wrapper);
    return new PageUtils(page);
  }

  @Override
  public void saveSalesperson(ErpSalespersonEntity salesperson, Long userId) {
    normalizeAndValidate(salesperson, null);
    Date now = new Date();
    salesperson.setId(null);
    salesperson.setStatus(salesperson.getStatus() == null ? 1 : salesperson.getStatus());
    salesperson.setCreateUserId(userId);
    salesperson.setCreateTime(now);
    salesperson.setUpdateTime(now);
    this.save(salesperson);
  }

  @Override
  public void updateSalesperson(ErpSalespersonEntity salesperson) {
    if (salesperson.getId() == null) {
      throw new RRException("销售信息ID不能为空");
    }
    normalizeAndValidate(salesperson, salesperson.getId());
    ErpSalespersonEntity existing = this.getById(salesperson.getId());
    if (existing != null) {
      salesperson.setCreateUserId(existing.getCreateUserId());
      salesperson.setCreateTime(existing.getCreateTime());
    }
    salesperson.setUpdateTime(new Date());
    this.updateById(salesperson);
  }

  private void normalizeAndValidate(ErpSalespersonEntity salesperson, Long excludeId) {
    if (salesperson == null) {
      throw new RRException("销售信息不能为空");
    }
    salesperson.setSalesName(StringUtils.trim(salesperson.getSalesName()));
    salesperson.setMobile(StringUtils.trim(salesperson.getMobile()));
    salesperson.setSysUsername(StringUtils.trim(salesperson.getSysUsername()));
    salesperson.setRemark(StringUtils.trim(salesperson.getRemark()));
    if (StringUtils.isBlank(salesperson.getSalesName())) {
      throw new RRException("销售姓名不能为空");
    }
    fillUserSnapshot(salesperson);
    QueryWrapper<ErpSalespersonEntity> wrapper = new QueryWrapper<ErpSalespersonEntity>().eq("sales_name", salesperson.getSalesName());
    if (excludeId != null) {
      wrapper.ne("id", excludeId);
    }
    if (this.count(wrapper) > 0) {
      throw new RRException("销售姓名已存在");
    }
  }

  private void fillUserSnapshot(ErpSalespersonEntity salesperson) {
    if (salesperson.getSysUserId() == null) {
      salesperson.setSysUsername(null);
      return;
    }
    SysUserEntity user = sysUserDao.selectById(salesperson.getSysUserId());
    if (user == null) {
      throw new RRException("绑定登录账户不存在");
    }
    salesperson.setSysUsername(user.getUsername());
  }
}
