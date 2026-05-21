package io.renren.modules.erp.controller;

import io.renren.common.utils.PageUtils;
import io.renren.common.utils.R;
import io.renren.modules.erp.entity.ErpProductPriceEntity;
import io.renren.modules.erp.service.ErpProductPriceService;
import io.renren.modules.sys.controller.AbstractController;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("erp/productprice")
public class ErpProductPriceController extends AbstractController {
  @Autowired
  private ErpProductPriceService erpProductPriceService;

  @GetMapping("/list")
  @RequiresPermissions("erp:productprice:list")
  public R list(@RequestParam Map<String, Object> params) {
    PageUtils page = erpProductPriceService.queryPage(params);
    return R.ok().put("page", page);
  }

  @GetMapping("/info/{id}")
  @RequiresPermissions("erp:productprice:info")
  public R info(@PathVariable("id") Long id) {
    return R.ok().put("productPrice", erpProductPriceService.getById(id));
  }

  @PostMapping("/save")
  @RequiresPermissions("erp:productprice:save")
  public R save(@RequestBody ErpProductPriceEntity entity) {
    entity.setCreateUserId(getUserId());
    entity.setCreateTime(new Date());
    entity.setUpdateTime(new Date());
    erpProductPriceService.save(entity);
    return R.ok();
  }

  @PostMapping("/update")
  @RequiresPermissions("erp:productprice:update")
  public R update(@RequestBody ErpProductPriceEntity entity) {
    entity.setUpdateTime(new Date());
    erpProductPriceService.updateById(entity);
    return R.ok();
  }

  @PostMapping("/delete")
  @RequiresPermissions("erp:productprice:delete")
  public R delete(@RequestBody Long[] ids) {
    erpProductPriceService.removeByIds(Arrays.asList(ids));
    return R.ok();
  }
}
