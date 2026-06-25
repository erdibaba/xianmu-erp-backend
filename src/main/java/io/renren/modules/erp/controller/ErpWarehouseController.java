package io.renren.modules.erp.controller;

import io.renren.common.utils.PageUtils;
import io.renren.common.utils.R;
import io.renren.modules.erp.entity.ErpWarehouseEntity;
import io.renren.modules.erp.entity.ErpWarehouseFeeRateEntity;
import io.renren.modules.erp.service.ErpWarehouseFeeRateService;
import io.renren.modules.erp.service.ErpWarehouseService;
import io.renren.modules.sys.controller.AbstractController;
import java.util.Arrays;
import java.util.Date;
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
@RequestMapping("erp/warehouse")
public class ErpWarehouseController extends AbstractController {
  @Autowired
  private ErpWarehouseService erpWarehouseService;
  @Autowired
  private ErpWarehouseFeeRateService erpWarehouseFeeRateService;

  @GetMapping("/list")
  @RequiresPermissions("erp:warehouse:list")
  public R list(@RequestParam Map<String, Object> params) {
    PageUtils page = erpWarehouseService.queryPage(params);
    return R.ok().put("page", page);
  }

  @GetMapping("/select")
  public R select() {
    return R.ok().put("list", erpWarehouseService.queryAll());
  }

  @GetMapping("/info/{id}")
  @RequiresPermissions("erp:warehouse:info")
  public R info(@PathVariable("id") Long id) {
    return R.ok().put("warehouse", erpWarehouseService.getById(id));
  }

  @PostMapping("/save")
  @RequiresPermissions("erp:warehouse:save")
  public R save(@RequestBody ErpWarehouseEntity warehouse) {
    warehouse.setCreateUserId(getUserId());
    warehouse.setCreateTime(new Date());
    warehouse.setUpdateTime(new Date());
    erpWarehouseService.save(warehouse);
    return R.ok();
  }

  @PostMapping("/update")
  @RequiresPermissions("erp:warehouse:update")
  public R update(@RequestBody ErpWarehouseEntity warehouse) {
    warehouse.setUpdateTime(new Date());
    erpWarehouseService.updateById(warehouse);
    return R.ok();
  }

  @PostMapping("/delete")
  @RequiresPermissions("erp:warehouse:delete")
  public R delete(@RequestBody Long[] ids) {
    erpWarehouseService.removeByIds(Arrays.asList(ids));
    return R.ok();
  }

  @GetMapping("/rate/list/{warehouseId}")
  @RequiresPermissions("erp:warehouse:list")
  public R rateList(@PathVariable("warehouseId") Long warehouseId) {
    return R.ok().put("list", erpWarehouseFeeRateService.listByWarehouseId(warehouseId));
  }

  @PostMapping("/rate/save")
  @RequiresPermissions("erp:warehouse:update")
  public R rateSave(@RequestBody ErpWarehouseFeeRateEntity rate) {
    erpWarehouseFeeRateService.saveRate(rate, getUserId());
    return R.ok();
  }

  @PostMapping("/rate/update")
  @RequiresPermissions("erp:warehouse:update")
  public R rateUpdate(@RequestBody ErpWarehouseFeeRateEntity rate) {
    erpWarehouseFeeRateService.updateRate(rate);
    return R.ok();
  }

  @PostMapping("/rate/delete/{id}")
  @RequiresPermissions("erp:warehouse:update")
  public R rateDelete(@PathVariable("id") Long id) {
    erpWarehouseFeeRateService.deleteRate(id);
    return R.ok();
  }
}
