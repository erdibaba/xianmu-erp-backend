package io.renren.modules.erp.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.renren.common.utils.PageUtils;
import io.renren.common.utils.R;
import io.renren.modules.erp.dao.ErpPartnerDao;
import io.renren.modules.erp.entity.ErpPartnerEntity;
import io.renren.modules.erp.entity.ErpProductEntity;
import io.renren.modules.erp.service.ErpProductService;
import io.renren.modules.sys.controller.AbstractController;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("erp/product")
public class ErpProductController extends AbstractController {
  @Autowired
  private ErpProductService erpProductService;
  @Autowired
  private ErpPartnerDao erpPartnerDao;

  @GetMapping("/list")
  @RequiresPermissions("erp:product:list")
  public R list(@RequestParam Map<String, Object> params) {
    PageUtils page = erpProductService.queryPage(params);
    return R.ok().put("page", page);
  }

  @GetMapping("/select")
  public R select() {
    return R.ok().put("list", erpProductService.queryAll());
  }

  @GetMapping("/selectPage")
  public R selectPage(@RequestParam Map<String, Object> params) {
    PageUtils page = erpProductService.querySelectPage(params);
    return R.ok().put("page", page);
  }

  @GetMapping("/info/{id}")
  @RequiresPermissions("erp:product:info")
  public R info(@PathVariable("id") Long id) {
    return R.ok().put("product", erpProductService.getById(id));
  }

  @PostMapping("/save")
  @RequiresPermissions("erp:product:save")
  public R save(@RequestBody ErpProductEntity product) {
    fillBrandAssociation(product);
    product.setCreateUserId(getUserId());
    product.setCreateTime(new Date());
    product.setUpdateTime(new Date());
    erpProductService.save(product);
    return R.ok();
  }

  @PostMapping("/update")
  @RequiresPermissions("erp:product:update")
  public R update(@RequestBody ErpProductEntity product) {
    fillBrandAssociation(product);
    product.setUpdateTime(new Date());
    erpProductService.updateById(product);
    return R.ok();
  }

  @PostMapping("/delete")
  @RequiresPermissions("erp:product:delete")
  public R delete(@RequestBody Long[] ids) {
    erpProductService.removeByIds(Arrays.asList(ids));
    return R.ok();
  }

  private void fillBrandAssociation(ErpProductEntity product) {
    ErpPartnerEntity brand = product.getBrandId() == null ? null : erpPartnerDao.selectById(product.getBrandId());
    if (brand == null && StringUtils.isNotBlank(product.getBrand())) {
      brand = erpPartnerDao.selectOne(new QueryWrapper<ErpPartnerEntity>()
          .eq("partner_name", product.getBrand())
          .apply("FIND_IN_SET('BRAND', business_role) > 0")
          .last("limit 1"));
    }
    if (brand == null || !StringUtils.contains(StringUtils.defaultString(brand.getBusinessRole()), "BRAND")) {
      throw new RuntimeException("请选择有效的品牌方主体");
    }
    product.setBrandId(brand.getId());
    product.setBrand(brand.getPartnerName());
  }
}
