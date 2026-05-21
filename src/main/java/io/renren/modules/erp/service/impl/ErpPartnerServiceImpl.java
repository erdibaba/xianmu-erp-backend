package io.renren.modules.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.renren.common.utils.PageUtils;
import io.renren.common.utils.Query;
import io.renren.modules.erp.dao.ErpPartnerDao;
import io.renren.modules.erp.entity.ErpPartnerEntity;
import io.renren.modules.erp.service.ErpPartnerService;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

@Service("erpPartnerService")
public class ErpPartnerServiceImpl extends ServiceImpl<ErpPartnerDao, ErpPartnerEntity> implements ErpPartnerService {
  @Override
  public PageUtils queryPage(Map<String, Object> params) {
    String keyword = (String) params.get("keyword");
    String businessRole = params.get("businessRole") == null ? null : params.get("businessRole").toString();
    Integer partnerType = null;
    if (params.get("partnerType") != null) {
      String partnerTypeValue = params.get("partnerType").toString();
      if (StringUtils.isNotBlank(partnerTypeValue)) {
        partnerType = Integer.valueOf(partnerTypeValue);
      }
    }
    QueryWrapper<ErpPartnerEntity> wrapper = new QueryWrapper<ErpPartnerEntity>().orderByDesc("id");
    if (StringUtils.isNotBlank(keyword)) {
      wrapper.and(w -> w.like("partner_code", keyword).or().like("partner_name", keyword).or().like("contact_name", keyword));
    }
    if (partnerType != null) {
      wrapper.eq("partner_type", partnerType);
    }
    if (StringUtils.isNotBlank(businessRole)) {
      wrapper.and(w -> w.like("business_role", businessRole));
    }
    IPage<ErpPartnerEntity> page = this.page(new Query<ErpPartnerEntity>().getPage(params), wrapper);
    return new PageUtils(page);
  }

  @Override
  public List<ErpPartnerEntity> queryAll() {
    return this.list(new QueryWrapper<ErpPartnerEntity>().eq("status", 1).orderByAsc("partner_name"));
  }

  @Override
  public List<ErpPartnerEntity> queryByRole(String businessRole) {
    QueryWrapper<ErpPartnerEntity> wrapper = new QueryWrapper<ErpPartnerEntity>().eq("status", 1).orderByAsc("partner_name");
    if (StringUtils.isNotBlank(businessRole)) {
      wrapper.and(w -> w.like("business_role", businessRole));
    }
    return this.list(wrapper);
  }
}
