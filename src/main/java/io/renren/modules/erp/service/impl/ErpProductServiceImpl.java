package io.renren.modules.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.renren.common.utils.PageUtils;
import io.renren.common.utils.Query;
import io.renren.modules.erp.dao.ErpProductDao;
import io.renren.modules.erp.entity.ErpProductEntity;
import io.renren.modules.erp.service.ErpProductService;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

@Service("erpProductService")
public class ErpProductServiceImpl extends ServiceImpl<ErpProductDao, ErpProductEntity> implements ErpProductService {
  @Override
  public PageUtils queryPage(Map<String, Object> params) {
    QueryWrapper<ErpProductEntity> wrapper = buildKeywordWrapper((String) params.get("keyword")).orderByDesc("id");
    IPage<ErpProductEntity> page = this.page(new Query<ErpProductEntity>().getPage(params), wrapper);
    return new PageUtils(page);
  }

  @Override
  public List<ErpProductEntity> queryAll() {
    return this.list(new QueryWrapper<ErpProductEntity>().eq("status", 1).orderByAsc("product_name"));
  }

  @Override
  public PageUtils querySelectPage(Map<String, Object> params) {
    QueryWrapper<ErpProductEntity> wrapper = buildKeywordWrapper((String) params.get("keyword"))
        .eq("status", 1)
        .orderByAsc("product_code");
    IPage<ErpProductEntity> page = this.page(new Query<ErpProductEntity>().getPage(params), wrapper);
    return new PageUtils(page);
  }

  private QueryWrapper<ErpProductEntity> buildKeywordWrapper(String keyword) {
    QueryWrapper<ErpProductEntity> wrapper = new QueryWrapper<>();
    if (StringUtils.isNotBlank(keyword)) {
      wrapper.and(w -> w.like("product_code", keyword)
          .or().like("product_name", keyword)
          .or().like("product_name_en", keyword)
          .or().like("brand", keyword));
    }
    return wrapper;
  }
}
