package io.renren.modules.erp.controller;

import io.renren.common.utils.PageUtils;
import io.renren.common.utils.R;
import io.renren.modules.erp.entity.ErpProductEntity;
import io.renren.modules.erp.service.ErpProductService;
import io.renren.modules.sys.controller.AbstractController;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("erp/product")
public class ErpProductController extends AbstractController {
  @Autowired
  private ErpProductService erpProductService;

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
    product.setCreateUserId(getUserId());
    product.setCreateTime(new Date());
    product.setUpdateTime(new Date());
    erpProductService.save(product);
    return R.ok();
  }

  @PostMapping("/update")
  @RequiresPermissions("erp:product:update")
  public R update(@RequestBody ErpProductEntity product) {
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
}
