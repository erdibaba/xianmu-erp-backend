package io.renren.modules.erp.controller;

import io.renren.common.utils.PageUtils;
import io.renren.common.utils.R;
import io.renren.modules.erp.entity.ErpTradeOrderEntity;
import io.renren.modules.erp.service.ErpTradeOrderService;
import io.renren.modules.sys.controller.AbstractController;
import java.util.Map;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("erp/tradeorder")
public class ErpTradeOrderController extends AbstractController {
  @Autowired
  private ErpTradeOrderService erpTradeOrderService;

  @GetMapping("/list/{orderType}")
  @RequiresPermissions("erp:tradeorder:list")
  public R list(@PathVariable("orderType") String orderType, @RequestParam Map<String, Object> params) {
    PageUtils page = erpTradeOrderService.queryPage(params, orderType);
    return R.ok().put("page", page);
  }

  @GetMapping("/info/{id}")
  @RequiresPermissions("erp:tradeorder:info")
  public R info(@PathVariable("id") Long id) {
    return R.ok().put("tradeOrder", erpTradeOrderService.getDetail(id));
  }

  @PostMapping("/save")
  @RequiresPermissions("erp:tradeorder:save")
  public R save(@RequestBody ErpTradeOrderEntity order) {
    erpTradeOrderService.saveOrder(order, getUserId());
    return R.ok();
  }

  @PostMapping("/update")
  @RequiresPermissions("erp:tradeorder:update")
  public R update(@RequestBody ErpTradeOrderEntity order) {
    erpTradeOrderService.updateOrder(order, getUserId());
    return R.ok();
  }

  @PostMapping("/delete")
  @RequiresPermissions("erp:tradeorder:delete")
  public R delete(@RequestBody Long[] ids) {
    erpTradeOrderService.deleteOrders(ids);
    return R.ok();
  }

  @GetMapping("/export/finance/{orderType}")
  @RequiresPermissions("erp:tradeorder:list")
  public ResponseEntity<byte[]> exportFinance(@PathVariable("orderType") String orderType, @RequestParam Map<String, Object> params) {
    return erpTradeOrderService.exportFinanceStatement(params, orderType);
  }
}
