package io.renren.modules.erp.controller;

import io.renren.common.utils.PageUtils;
import io.renren.common.utils.R;
import io.renren.modules.erp.entity.ErpPresaleAttachmentEntity;
import io.renren.modules.erp.entity.ErpPresaleOrderEntity;
import io.renren.modules.erp.service.ErpPresaleOrderService;
import io.renren.modules.erp.vo.ErpRecognizedPackingDraftVo;
import io.renren.modules.erp.vo.ErpRecognizedOrderDraftVo;
import io.renren.modules.sys.controller.AbstractController;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("erp/presale")
public class ErpPresaleOrderController extends AbstractController {
  @Autowired
  private ErpPresaleOrderService erpPresaleOrderService;

  @GetMapping("/list")
  @RequiresPermissions("erp:tradeorder:list")
  public R list(@RequestParam Map<String, Object> params) {
    PageUtils page = erpPresaleOrderService.queryPage(params);
    return R.ok().put("page", page);
  }

  @GetMapping("/info/{id}")
  @RequiresPermissions("erp:tradeorder:info")
  public R info(@PathVariable("id") Long id) {
    return R.ok().put("presaleOrder", erpPresaleOrderService.getDetail(id));
  }

  @PostMapping("/save")
  @RequiresPermissions("erp:tradeorder:save")
  public R save(@RequestBody ErpPresaleOrderEntity order) {
    erpPresaleOrderService.saveOrder(order, getUserId());
    return R.ok();
  }

  @PostMapping("/update")
  @RequiresPermissions("erp:tradeorder:update")
  public R update(@RequestBody ErpPresaleOrderEntity order) {
    erpPresaleOrderService.updateOrder(order, getUserId());
    return R.ok();
  }

  @PostMapping("/delete")
  @RequiresPermissions("erp:tradeorder:delete")
  public R delete(@RequestBody Long[] ids) {
    erpPresaleOrderService.deleteOrders(ids);
    return R.ok();
  }

  @PostMapping("/sync-confirm-products")
  @RequiresPermissions("erp:tradeorder:update")
  public R syncConfirmProducts(@RequestBody ErpRecognizedOrderDraftVo draft) {
    erpPresaleOrderService.syncConfirmProductMaster(draft);
    return R.ok();
  }

  @PostMapping("/sync-packing-products")
  @RequiresPermissions("erp:tradeorder:update")
  public R syncPackingProducts(@RequestBody ErpRecognizedPackingDraftVo draft) {
    erpPresaleOrderService.syncPackingProductMaster(draft);
    return R.ok();
  }

  @PostMapping("/upload-attachment")
  @RequiresPermissions("erp:tradeorder:save")
  public R uploadAttachment(@RequestParam("presaleOrderId") Long presaleOrderId,
                            @RequestParam("attachmentType") String attachmentType,
                            @RequestParam("file") MultipartFile file) throws Exception {
    if (presaleOrderId == null || presaleOrderId <= 0) {
      return R.error("请先保存预销售单后再上传附件");
    }
    if (file == null || file.isEmpty()) {
      return R.error("请先上传文件");
    }
    String normalizedType = StringUtils.trimToEmpty(attachmentType).toUpperCase();
    if (!"CUSTOMS".equals(normalizedType) && !"QUARANTINE".equals(normalizedType)) {
      return R.error("附件类型不支持");
    }
    ErpPresaleAttachmentEntity attachment = erpPresaleOrderService.uploadAttachment(presaleOrderId, normalizedType, file, getUserId());
    return R.ok().put("attachment", attachment);
  }

  @GetMapping("/download/estimate/{id}")
  @RequiresPermissions("erp:tradeorder:info")
  public ResponseEntity<byte[]> downloadEstimate(@PathVariable("id") Long id) {
    return erpPresaleOrderService.downloadEstimateFile(id);
  }

  @GetMapping("/download/confirm/{id}")
  @RequiresPermissions("erp:tradeorder:info")
  public ResponseEntity<byte[]> downloadConfirm(@PathVariable("id") Long id) {
    return erpPresaleOrderService.downloadConfirmFile(id);
  }

  @GetMapping("/download/packing/{id}")
  @RequiresPermissions("erp:tradeorder:info")
  public ResponseEntity<byte[]> downloadPacking(@PathVariable("id") Long id) {
    return erpPresaleOrderService.downloadPackingFile(id);
  }

  @GetMapping("/download/customs/{id}")
  @RequiresPermissions("erp:tradeorder:info")
  public ResponseEntity<byte[]> downloadCustoms(@PathVariable("id") Long id) {
    return erpPresaleOrderService.downloadAttachmentFile(id, "CUSTOMS");
  }

  @GetMapping("/download/quarantine/{id}")
  @RequiresPermissions("erp:tradeorder:info")
  public ResponseEntity<byte[]> downloadQuarantine(@PathVariable("id") Long id) {
    return erpPresaleOrderService.downloadAttachmentFile(id, "QUARANTINE");
  }
}
