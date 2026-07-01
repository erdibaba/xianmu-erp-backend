package io.renren.modules.erp.controller;

import io.renren.common.utils.PageUtils;
import io.renren.common.utils.R;
import io.renren.modules.erp.service.ErpContractDailyCostService;
import io.renren.modules.sys.controller.AbstractController;
import java.util.Map;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("erp/contract-daily-cost")
public class ErpContractDailyCostController extends AbstractController {
  @Autowired
  private ErpContractDailyCostService erpContractDailyCostService;

  @GetMapping("/list")
  @RequiresPermissions("erp:contract-daily-cost:list")
  public R list(@RequestParam Map<String, Object> params) {
    PageUtils page = erpContractDailyCostService.queryPage(params);
    return R.ok().put("page", page);
  }

  @GetMapping("/details/{id}")
  @RequiresPermissions("erp:contract-daily-cost:list")
  public R details(@PathVariable("id") Long id) {
    return R.ok().put("list", erpContractDailyCostService.queryDetails(id));
  }

  @PostMapping("/refresh")
  @RequiresPermissions("erp:contract-daily-cost:refresh")
  public R refresh(@RequestParam Map<String, Object> params) {
    erpContractDailyCostService.refresh(params, getUserId());
    return R.ok();
  }
}
