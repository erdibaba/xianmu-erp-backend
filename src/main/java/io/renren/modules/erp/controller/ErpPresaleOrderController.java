package io.renren.modules.erp.controller;

import io.renren.common.utils.PageUtils;
import io.renren.common.utils.R;
import io.renren.modules.erp.entity.ErpPresaleAttachmentEntity;
import io.renren.modules.erp.entity.ErpPresaleOrderEntity;
import io.renren.modules.erp.service.ErpPresaleOrderService;
import io.renren.modules.erp.vo.ErpRecognizedPackingDraftVo;
import io.renren.modules.erp.vo.ErpRecognizedOrderDraftVo;
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

  @GetMapping("/confirm-list")
  @RequiresPermissions("erp:tradeorder:list")
  public R confirmList(@RequestParam Map<String, Object> params) {
    PageUtils page = erpPresaleOrderService.queryConfirmPage(params);
    return R.ok().put("page", page);
  }

  @GetMapping("/info/{id}")
  @RequiresPermissions("erp:tradeorder:info")
  public R info(@PathVariable("id") Long id,
                @RequestParam(value = "confirmId", required = false) Long confirmId) {
    return R.ok().put("presaleOrder", erpPresaleOrderService.getDetail(id, confirmId));
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
                            @RequestParam(value = "confirmId", required = false) Long confirmId,
                            @RequestParam("attachmentType") String attachmentType,
                            @RequestParam(value = "overwriteExisting", required = false, defaultValue = "false") Boolean overwriteExisting,
                            @RequestParam(value = "confirmedGrossWeight", required = false) BigDecimal confirmedGrossWeight,
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
    ErpPresaleAttachmentEntity attachment = erpPresaleOrderService.uploadAttachment(presaleOrderId, confirmId, normalizedType, file, getUserId(), Boolean.TRUE.equals(overwriteExisting), confirmedGrossWeight);
    return R.ok().put("attachment", attachment);
  }

  @PostMapping("/recognize-customs")
  @RequiresPermissions("erp:tradeorder:save")
  public R recognizeCustoms(@RequestParam("file") MultipartFile file) throws Exception {
    if (file == null || file.isEmpty()) {
      return R.error("请先上传报关单文件");
    }
    return R.ok().put("attachment", erpPresaleOrderService.recognizeCustomsAttachment(file));
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

  @GetMapping("/download/confirm-file/{confirmId}")
  @RequiresPermissions("erp:tradeorder:info")
  public ResponseEntity<byte[]> downloadConfirmFile(@PathVariable("confirmId") Long confirmId) {
    return erpPresaleOrderService.downloadConfirmFileByConfirmId(confirmId);
  }

  @GetMapping("/download/packing/{id}")
  @RequiresPermissions("erp:tradeorder:info")
  public ResponseEntity<byte[]> downloadPacking(@PathVariable("id") Long id) {
    return erpPresaleOrderService.downloadPackingFile(id);
  }

  @GetMapping("/download/packing-file/{confirmId}")
  @RequiresPermissions("erp:tradeorder:info")
  public ResponseEntity<byte[]> downloadPackingFile(@PathVariable("confirmId") Long confirmId) {
    return erpPresaleOrderService.downloadPackingFileByConfirmId(confirmId);
  }

  @GetMapping("/download/customs/{id}")
  @RequiresPermissions("erp:tradeorder:info")
  public ResponseEntity<byte[]> downloadCustoms(@PathVariable("id") Long id) {
    return erpPresaleOrderService.downloadAttachmentFile(id, "CUSTOMS");
  }

  @GetMapping("/download/customs-file/{confirmId}")
  @RequiresPermissions("erp:tradeorder:info")
  public ResponseEntity<byte[]> downloadCustomsFile(@PathVariable("confirmId") Long confirmId) {
    return erpPresaleOrderService.downloadAttachmentFileByConfirmId(confirmId, "CUSTOMS");
  }

  @GetMapping("/download/quarantine/{id}")
  @RequiresPermissions("erp:tradeorder:info")
  public ResponseEntity<byte[]> downloadQuarantine(@PathVariable("id") Long id) {
    return erpPresaleOrderService.downloadAttachmentFile(id, "QUARANTINE");
  }

  @GetMapping("/download/quarantine-file/{confirmId}")
  @RequiresPermissions("erp:tradeorder:info")
  public ResponseEntity<byte[]> downloadQuarantineFile(@PathVariable("confirmId") Long confirmId) {
    return erpPresaleOrderService.downloadAttachmentFileByConfirmId(confirmId, "QUARANTINE");
  }

  @GetMapping("/download/attachment/{attachmentId}")
  @RequiresPermissions("erp:tradeorder:info")
  public ResponseEntity<byte[]> downloadAttachment(@PathVariable("attachmentId") Long attachmentId) {
    return erpPresaleOrderService.downloadAttachmentFileById(attachmentId);
  }
}
