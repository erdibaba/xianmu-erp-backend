package io.renren.modules.erp.controller;

import io.renren.common.utils.PageUtils;
import io.renren.common.utils.R;
import io.renren.modules.erp.entity.ErpDriverEntity;
import io.renren.modules.erp.entity.ErpInboundOrderEntity;
import io.renren.modules.erp.service.ErpDriverService;
import io.renren.modules.erp.service.ErpInboundOrderService;
import io.renren.modules.erp.vo.ErpRecognizedInboundResultVo;
import io.renren.modules.sys.controller.AbstractController;
import java.math.BigDecimal;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("erp/inbound")
public class ErpInboundOrderController extends AbstractController {
  @Autowired
  private ErpInboundOrderService erpInboundOrderService;
  @Autowired
  private ErpDriverService erpDriverService;

  @GetMapping("/list")
  @RequiresPermissions("erp:tradeorder:list")
  public R list(@RequestParam Map<String, Object> params) {
    PageUtils page = erpInboundOrderService.queryPage(params);
    return R.ok().put("page", page);
  }

  @GetMapping("/info/{presaleOrderId}")
  @RequiresPermissions("erp:tradeorder:info")
  public R info(@PathVariable("presaleOrderId") Long presaleOrderId,
                @RequestParam(value = "confirmId", required = false) Long confirmId) {
    return R.ok().put("inboundOrder", erpInboundOrderService.getDetail(presaleOrderId, confirmId));
  }

  @GetMapping("/packing-boxes/{presaleOrderId}")
  @RequiresPermissions("erp:tradeorder:info")
  public R packingBoxes(@PathVariable("presaleOrderId") Long presaleOrderId,
                        @RequestParam(value = "confirmId", required = false) Long confirmId) {
    return R.ok().put("packingBoxMap", erpInboundOrderService.getPackingBoxMap(presaleOrderId, confirmId));
  }

  @GetMapping("/driver/select")
  @RequiresPermissions("erp:tradeorder:info")
  public R driverSelect(@RequestParam(value = "keyword", required = false) String keyword) {
    Map<String, Object> params = new java.util.HashMap<String, Object>();
    params.put("page", "1");
    params.put("limit", "15");
    params.put("keyword", keyword);
    return R.ok().put("page", erpDriverService.queryPage(params));
  }

  @PostMapping("/driver/save")
  @RequiresPermissions("erp:tradeorder:save")
  public R driverSave(@RequestBody ErpDriverEntity driver) {
    erpDriverService.saveDriver(driver, getUserId());
    return R.ok().put("driver", driver);
  }

  @PostMapping("/save")
  @RequiresPermissions("erp:tradeorder:save")
  public R save(@RequestBody ErpInboundOrderEntity order) {
    erpInboundOrderService.saveOrder(order, getUserId());
    return R.ok();
  }

  @PostMapping("/update")
  @RequiresPermissions("erp:tradeorder:update")
  public R update(@RequestBody ErpInboundOrderEntity order) {
    erpInboundOrderService.updateOrder(order, getUserId());
    return R.ok();
  }

  @PostMapping("/item/damage")
  @RequiresPermissions("erp:tradeorder:update")
  public R saveItemDamage(@RequestBody Map<String, Object> params) {
    Long itemId = params.get("itemId") == null ? null : Long.valueOf(String.valueOf(params.get("itemId")));
    BigDecimal damageWeightKg = params.get("damageWeightKg") == null || StringUtils.isBlank(String.valueOf(params.get("damageWeightKg")))
        ? null : new BigDecimal(String.valueOf(params.get("damageWeightKg")));
    String damageReason = params.get("damageReason") == null ? null : String.valueOf(params.get("damageReason"));
    erpInboundOrderService.saveItemDamage(itemId, damageWeightKg, damageReason);
    return R.ok();
  }

  @PostMapping("/recognize")
  @RequiresPermissions("erp:tradeorder:save")
  public R recognize(@RequestParam("presaleOrderId") Long presaleOrderId,
                     @RequestParam(value = "confirmId", required = false) Long confirmId,
                     @RequestParam("files") MultipartFile[] files) throws Exception {
    if (presaleOrderId == null || presaleOrderId <= 0) {
      return R.error("请先选择预销售单");
    }
    if (files == null || files.length == 0) {
      return R.error("请先上传入库单文件");
    }
    for (MultipartFile file : files) {
      String filename = StringUtils.defaultString(file.getOriginalFilename()).toLowerCase();
      if (!(filename.endsWith(".jpg") || filename.endsWith(".jpeg") || filename.endsWith(".png")
          || filename.endsWith(".jfif") || filename.endsWith(".bmp") || filename.endsWith(".pdf"))) {
        return R.error("仅支持 jpg/jpeg/png/jfif/bmp/pdf");
      }
    }
    ErpRecognizedInboundResultVo result = erpInboundOrderService.recognize(presaleOrderId, confirmId, files);
    return R.ok().put("result", result);
  }

  @GetMapping("/download/file/{fileId}")
  @RequiresPermissions("erp:tradeorder:info")
  public ResponseEntity<byte[]> downloadFile(@PathVariable("fileId") Long fileId) {
    return erpInboundOrderService.downloadFile(fileId);
  }
}
