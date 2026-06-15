package io.renren.modules.erp.controller;

import io.renren.common.utils.PageUtils;
import io.renren.common.utils.R;
import io.renren.modules.erp.service.ErpExpenseService;
import io.renren.modules.sys.controller.AbstractController;
import java.util.Map;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("erp/expense")
public class ErpExpenseController extends AbstractController {
  @Autowired
  private ErpExpenseService erpExpenseService;

  @GetMapping("/list")
  @RequiresPermissions("erp:expense:list")
  public R list(@RequestParam Map<String, Object> params) {
    PageUtils page = erpExpenseService.queryPage(params);
    return R.ok().put("page", page);
  }
}
