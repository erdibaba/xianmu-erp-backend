package io.renren.modules.erp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.renren.common.utils.PageUtils;
import io.renren.common.utils.Query;
import io.renren.modules.erp.dao.ErpProductPriceDao;
import io.renren.modules.erp.entity.ErpProductPriceEntity;
import io.renren.modules.erp.service.ErpProductPriceService;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service("erpProductPriceService")
public class ErpProductPriceServiceImpl extends ServiceImpl<ErpProductPriceDao, ErpProductPriceEntity> implements ErpProductPriceService {
  @Override
  public PageUtils queryPage(Map<String, Object> params) {
    String priceType = (String) params.get("priceType");
    Long productId = params.get("productId") == null || "".equals(params.get("productId")) ? null : Long.valueOf(params.get("productId").toString());
    QueryWrapper<ErpProductPriceEntity> wrapper = new QueryWrapper<ErpProductPriceEntity>().orderByDesc("effective_date", "id");
    if (priceType != null && !"".equals(priceType)) {
      wrapper.eq("price_type", Integer.valueOf(priceType));
    }
    if (productId != null) {
      wrapper.eq("product_id", productId);
    }
    IPage<ErpProductPriceEntity> page = this.page(new Query<ErpProductPriceEntity>().getPage(params), wrapper);
    return new PageUtils(page);
  }
}
