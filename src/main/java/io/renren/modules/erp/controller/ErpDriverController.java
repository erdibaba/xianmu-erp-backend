package io.renren.modules.erp.controller;

import io.renren.common.utils.PageUtils;
import io.renren.common.utils.R;
import io.renren.modules.erp.entity.ErpDriverEntity;
import io.renren.modules.erp.service.ErpDriverService;
import io.renren.modules.sys.controller.AbstractController;
import java.util.Arrays;
import java.util.Map;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("erp/driver")
public class ErpDriverController extends AbstractController {
  @Autowired
  private ErpDriverService erpDriverService;

  @GetMapping("/list")
  @RequiresPermissions("erp:driver:list")
  public R list(@RequestParam Map<String, Object> params) {
    PageUtils page = erpDriverService.queryPage(params);
    return R.ok().put("page", page);
  }

  @GetMapping("/info/{id}")
  @RequiresPermissions("erp:driver:info")
  public R info(@PathVariable("id") Long id) {
    return R.ok().put("driver", erpDriverService.getById(id));
  }

  @PostMapping("/save")
  @RequiresPermissions("erp:driver:save")
  public R save(@RequestBody ErpDriverEntity driver) {
    erpDriverService.saveDriver(driver, getUserId());
    return R.ok();
  }

  @PostMapping("/update")
  @RequiresPermissions("erp:driver:update")
  public R update(@RequestBody ErpDriverEntity driver) {
    erpDriverService.updateDriver(driver);
    return R.ok();
  }

  @PostMapping("/delete")
  @RequiresPermissions("erp:driver:delete")
  public R delete(@RequestBody Long[] ids) {
    erpDriverService.removeByIds(Arrays.asList(ids));
    return R.ok();
  }
}
