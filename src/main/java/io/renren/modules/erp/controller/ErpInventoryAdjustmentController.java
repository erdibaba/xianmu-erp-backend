package io.renren.modules.erp.controller;

import io.renren.common.utils.R;
import io.renren.modules.erp.service.ErpInventoryAdjustmentService;
import io.renren.modules.erp.vo.ErpInventoryAdjustmentRequest;
import io.renren.modules.sys.controller.AbstractController;
import java.util.Map;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("erp/inventory-adjustment")
public class ErpInventoryAdjustmentController extends AbstractController {
  @Autowired
  private ErpInventoryAdjustmentService erpInventoryAdjustmentService;

  @GetMapping("/lots")
  @RequiresPermissions("erp:inventory-adjustment:list")
  public R lots(@RequestParam Map<String, Object> params) {
    return R.ok().put("list", erpInventoryAdjustmentService.queryAvailableLots(params));
  }

  @GetMapping("/containers")
  @RequiresPermissions("erp:inventory-adjustment:list")
  public R containers(@RequestParam Map<String, Object> params) {
    return R.ok().put("list", erpInventoryAdjustmentService.queryContainerOptions(params));
  }

  @PostMapping("/recognize")
  @RequiresPermissions("erp:inventory-adjustment:save")
  public R recognize(@RequestParam("adjustmentType") String adjustmentType, @RequestParam("files") MultipartFile[] files) throws Exception {
    return R.ok().put("result", erpInventoryAdjustmentService.recognizeAdjustment(adjustmentType, files));
  }

  @PostMapping("/save")
  @RequiresPermissions("erp:inventory-adjustment:save")
  public R save(@RequestBody ErpInventoryAdjustmentRequest request) {
    erpInventoryAdjustmentService.saveAdjustment(request, getUserId());
    return R.ok();
  }
}
