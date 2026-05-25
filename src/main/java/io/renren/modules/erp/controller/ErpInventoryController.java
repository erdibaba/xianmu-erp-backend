package io.renren.modules.erp.controller;

import io.renren.common.utils.R;
import io.renren.modules.erp.service.ErpInventoryService;
import io.renren.modules.sys.controller.AbstractController;
import java.util.Map;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("erp/inventory")
public class ErpInventoryController extends AbstractController {
  @Autowired
  private ErpInventoryService erpInventoryService;

  @GetMapping("/summary")
  @RequiresPermissions("erp:inventory:list")
  public R summary(@RequestParam Map<String, Object> params) {
    return R.ok().put("list", erpInventoryService.querySummary(params));
  }

  @GetMapping("/spot")
  @RequiresPermissions("erp:inventory:list")
  public R spot(@RequestParam Map<String, Object> params) {
    return R.ok().put("list", erpInventoryService.querySpot(params));
  }

  @GetMapping("/futures")
  @RequiresPermissions("erp:inventory:list")
  public R futures(@RequestParam Map<String, Object> params) {
    return R.ok().put("list", erpInventoryService.queryFutures(params));
  }
}
