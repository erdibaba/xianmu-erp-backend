package io.renren.modules.erp.controller;

import io.renren.common.utils.R;
import io.renren.modules.erp.service.ErpInventoryCostService;
import io.renren.modules.sys.controller.AbstractController;
import java.util.Map;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("erp/inventory-cost")
public class ErpInventoryCostController extends AbstractController {
  @Autowired
  private ErpInventoryCostService erpInventoryCostService;

  @GetMapping("/spot")
  @RequiresPermissions("erp:inventory-cost:list")
  public R spot(@RequestParam Map<String, Object> params) {
    return R.ok().put("list", erpInventoryCostService.querySpotCost(params));
  }

  @GetMapping("/spot/details")
  @RequiresPermissions("erp:inventory-cost:list")
  public R spotDetails(@RequestParam Map<String, Object> params) {
    return R.ok().put("list", erpInventoryCostService.querySpotCostDetails(params));
  }
}
