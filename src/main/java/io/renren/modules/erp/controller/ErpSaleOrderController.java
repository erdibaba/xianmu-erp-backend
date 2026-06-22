package io.renren.modules.erp.controller;

import io.renren.common.utils.PageUtils;
import io.renren.common.utils.R;
import io.renren.modules.erp.entity.ErpSaleOrderEntity;
import io.renren.modules.erp.entity.ErpSaleOrderItemEntity;
import io.renren.modules.erp.entity.ErpSaleOutboundBatchEntity;
import io.renren.modules.erp.entity.ErpSaleOutboundReceiptEntity;
import io.renren.modules.erp.entity.ErpDriverEntity;
import io.renren.modules.erp.service.ErpDriverService;
import io.renren.modules.erp.service.ErpSaleOrderService;
import io.renren.modules.erp.vo.ErpSalePresaleItemVo;
import io.renren.modules.erp.vo.ErpSalePresaleOrderVo;
import io.renren.modules.sys.controller.AbstractController;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("erp/saleorder")
public class ErpSaleOrderController extends AbstractController {
  @Autowired
  private ErpSaleOrderService erpSaleOrderService;
  @Autowired
  private ErpDriverService erpDriverService;

  @GetMapping("/list")
  @RequiresPermissions("erp:tradeorder:list")
  public R list(@RequestParam Map<String, Object> params) {
    PageUtils page = erpSaleOrderService.queryPage(params);
    return R.ok().put("page", page);
  }

  @GetMapping("/info/{id}")
  @RequiresPermissions("erp:tradeorder:info")
  public R info(@PathVariable("id") Long id) {
    return R.ok().put("saleOrder", erpSaleOrderService.getDetail(id));
  }

  @PostMapping("/save")
  @RequiresPermissions("erp:tradeorder:save")
  public R save(@RequestBody ErpSaleOrderEntity order) {
    try {
      erpSaleOrderService.saveOrder(order, getUserId());
      return R.ok();
    } catch (RuntimeException e) {
      return R.error(e.getMessage());
    }
  }

  @PostMapping("/update")
  @RequiresPermissions("erp:tradeorder:update")
  public R update(@RequestBody ErpSaleOrderEntity order) {
    try {
      erpSaleOrderService.updateOrder(order, getUserId());
      return R.ok();
    } catch (RuntimeException e) {
      return R.error(e.getMessage());
    }
  }

  @PostMapping("/presale-link/update")
  @RequiresPermissions("erp:tradeorder:update")
  public R updatePresaleLink(@RequestBody Map<String, Object> params) {
    try {
      Long saleOrderId = params.get("saleOrderId") == null ? null : Long.valueOf(String.valueOf(params.get("saleOrderId")));
      Long presaleOrderId = params.get("presaleOrderId") == null ? null : Long.valueOf(String.valueOf(params.get("presaleOrderId")));
      erpSaleOrderService.updatePresaleLink(saleOrderId, presaleOrderId, getUserId());
      return R.ok();
    } catch (RuntimeException e) {
      return R.error(e.getMessage());
    }
  }

  @PostMapping("/presale-link/confirm")
  @RequiresPermissions("erp:tradeorder:update")
  public R confirmPresaleLink(@RequestBody Map<String, Object> params) {
    try {
      Long saleOrderId = params.get("saleOrderId") == null ? null : Long.valueOf(String.valueOf(params.get("saleOrderId")));
      erpSaleOrderService.confirmPresaleLink(saleOrderId, getUserId());
      return R.ok();
    } catch (RuntimeException e) {
      return R.error(e.getMessage());
    }
  }

  @PostMapping("/preview-allocation")
  @RequiresPermissions("erp:tradeorder:save")
  public R previewAllocation(@RequestBody ErpSaleOrderEntity order) {
    try {
      List<ErpSaleOrderItemEntity> list = erpSaleOrderService.previewAllocation(order);
      return R.ok().put("list", list);
    } catch (RuntimeException e) {
      return R.error(e.getMessage());
    }
  }

  @PostMapping("/delete")
  @RequiresPermissions("erp:tradeorder:delete")
  public R delete(@RequestBody Long[] ids) {
    erpSaleOrderService.deleteOrders(ids);
    return R.ok();
  }

  @GetMapping("/presale-orders")
  @RequiresPermissions("erp:tradeorder:info")
  public R presaleOrders(@RequestParam(value = "keyword", required = false) String keyword) {
    List<ErpSalePresaleOrderVo> list = erpSaleOrderService.queryPresaleOrders(keyword);
    return R.ok().put("list", list);
  }

  @GetMapping("/presale-items")
  @RequiresPermissions("erp:tradeorder:info")
  public R presaleItems(@RequestParam(value = "productId", required = false) Long productId,
                        @RequestParam(value = "keyword", required = false) String keyword) {
    List<ErpSalePresaleItemVo> list = erpSaleOrderService.queryPresaleItems(productId, keyword);
    return R.ok().put("list", list);
  }

  @PostMapping("/upload")
  @RequiresPermissions("erp:tradeorder:update")
  public R upload(@RequestParam("saleOrderId") Long saleOrderId,
                  @RequestParam("fileType") String fileType,
                  @RequestParam("files") MultipartFile[] files) throws Exception {
    if (saleOrderId == null || saleOrderId <= 0) {
      return R.error("请先保存销售单");
    }
    if (StringUtils.isBlank(fileType)) {
      return R.error("文件类型不能为空");
    }
    if (files == null || files.length == 0) {
      return R.error("请先选择文件");
    }
    return R.ok().put("list", erpSaleOrderService.uploadFiles(saleOrderId, fileType, files, getUserId()));
  }

  @PostMapping("/outbound/receipt/recognize")
  @RequiresPermissions("erp:tradeorder:update")
  public R recognizeOutboundReceipt(@RequestParam("saleOrderId") Long saleOrderId,
                                    @RequestParam(value = "batchId", required = false) Long batchId,
                                    @RequestParam("files") MultipartFile[] files) throws Exception {
    if (saleOrderId == null || saleOrderId <= 0) {
      return R.error("请先保存销售单");
    }
    if (files == null || files.length == 0) {
      return R.error("请先选择出库回单文件");
    }
    try {
      return R.ok().put("receipt", erpSaleOrderService.recognizeOutboundReceipt(saleOrderId, batchId, files, getUserId()));
    } catch (RuntimeException e) {
      return R.error(e.getMessage());
    }
  }

  @PostMapping("/outbound/receipt/save")
  @RequiresPermissions("erp:tradeorder:update")
  public R saveOutboundReceipt(@RequestBody ErpSaleOutboundReceiptEntity receipt) {
    try {
      return R.ok().put("receipt", erpSaleOrderService.saveOutboundReceipt(receipt, getUserId()));
    } catch (RuntimeException e) {
      return R.error(e.getMessage());
    }
  }

  @GetMapping("/outbound/batch/list")
  @RequiresPermissions("erp:tradeorder:info")
  public R outboundBatchList(@RequestParam("saleOrderId") Long saleOrderId) {
    return R.ok().put("list", erpSaleOrderService.queryOutboundBatches(saleOrderId));
  }

  @PostMapping("/outbound/batch/create")
  @RequiresPermissions("erp:tradeorder:update")
  public R createOutboundBatch(@RequestBody ErpSaleOutboundBatchEntity batch) {
    if (batch == null || batch.getSaleOrderId() == null || batch.getSaleOrderId() <= 0) {
      return R.error("请先选择销售单");
    }
    try {
      return R.ok().put("batch", erpSaleOrderService.createOutboundBatch(batch, getUserId()));
    } catch (RuntimeException e) {
      return R.error(e.getMessage());
    }
  }

  @GetMapping("/outbound/driver/select")
  @RequiresPermissions("erp:tradeorder:update")
  public R outboundDriverSelect(@RequestParam(value = "keyword", required = false) String keyword) {
    Map<String, Object> params = new java.util.HashMap<String, Object>();
    params.put("page", "1");
    params.put("limit", "15");
    params.put("keyword", keyword);
    return R.ok().put("page", erpDriverService.queryPage(params));
  }

  @PostMapping("/outbound/driver/save")
  @RequiresPermissions("erp:tradeorder:update")
  public R outboundDriverSave(@RequestBody ErpDriverEntity driver) {
    try {
      erpDriverService.saveDriver(driver, getUserId());
      return R.ok().put("driver", driver);
    } catch (RuntimeException e) {
      return R.error(e.getMessage());
    }
  }

  @PostMapping("/outbound/batch/bank-slip/upload")
  @RequiresPermissions("erp:tradeorder:update")
  public R uploadOutboundBatchBankSlip(@RequestParam("saleOrderId") Long saleOrderId,
                                       @RequestParam("batchId") Long batchId,
                                       @RequestParam("files") MultipartFile[] files) throws Exception {
    if (saleOrderId == null || saleOrderId <= 0) {
      return R.error("请先选择销售单");
    }
    if (batchId == null || batchId <= 0) {
      return R.error("请先选择出库批次");
    }
    if (files == null || files.length == 0) {
      return R.error("请先选择二批来款水单");
    }
    try {
      return R.ok().put("batch", erpSaleOrderService.uploadOutboundBatchBankSlip(saleOrderId, batchId, files, getUserId()));
    } catch (RuntimeException e) {
      return R.error(e.getMessage());
    }
  }

  @PostMapping("/outbound/batch/bank-slip/save")
  @RequiresPermissions("erp:tradeorder:update")
  public R saveOutboundBatchBankSlip(@RequestBody ErpSaleOutboundBatchEntity batch) {
    if (batch == null || batch.getId() == null || batch.getId() <= 0) {
      return R.error("请选择出库批次");
    }
    try {
      return R.ok().put("batch", erpSaleOrderService.saveOutboundBatchBankSlip(batch, getUserId()));
    } catch (RuntimeException e) {
      return R.error(e.getMessage());
    }
  }

  @PostMapping("/outbound/batch/scan-link/bind")
  @RequiresPermissions("erp:tradeorder:update")
  public R bindOutboundBatchScanLink(@RequestBody Map<String, Object> params) throws Exception {
    Long saleOrderId = params.get("saleOrderId") == null ? null : Long.valueOf(String.valueOf(params.get("saleOrderId")));
    Long batchId = params.get("batchId") == null ? null : Long.valueOf(String.valueOf(params.get("batchId")));
    String scanUrl = params.get("scanUrl") == null ? null : String.valueOf(params.get("scanUrl"));
    if (saleOrderId == null || saleOrderId <= 0) {
      return R.error("请先选择销售单");
    }
    if (batchId == null || batchId <= 0) {
      return R.error("请先选择出库批次");
    }
    if (StringUtils.isBlank(scanUrl)) {
      return R.error("请先填写扫码链接");
    }
    try {
      return R.ok().put("batch", erpSaleOrderService.bindOutboundBatchScanLink(saleOrderId, batchId, scanUrl, getUserId()));
    } catch (RuntimeException e) {
      return R.error(e.getMessage());
    }
  }

  @PostMapping("/outbound/batch/confirm")
  @RequiresPermissions("erp:tradeorder:update")
  public R confirmOutboundBatch(@RequestBody Map<String, Object> params) {
    Long batchId = params.get("batchId") == null ? null : Long.valueOf(String.valueOf(params.get("batchId")));
    if (batchId == null || batchId <= 0) {
      return R.error("请先选择出库批次");
    }
    try {
      return R.ok().put("batch", erpSaleOrderService.confirmOutboundBatch(batchId, getUserId()));
    } catch (RuntimeException e) {
      return R.error(e.getMessage());
    }
  }

  @PostMapping("/outbound/batch/void")
  @RequiresPermissions("erp:tradeorder:update")
  public R voidOutboundBatch(@RequestBody Map<String, Object> params) {
    Long batchId = params.get("batchId") == null ? null : Long.valueOf(String.valueOf(params.get("batchId")));
    if (batchId == null || batchId <= 0) {
      return R.error("请先选择出库批次");
    }
    try {
      erpSaleOrderService.voidOutboundBatch(batchId, getUserId());
      return R.ok();
    } catch (RuntimeException e) {
      return R.error(e.getMessage());
    }
  }

  @PostMapping("/confirm")
  @RequiresPermissions("erp:tradeorder:update")
  public R confirm(@RequestBody Map<String, Object> params) {
    Long saleOrderId = params.get("saleOrderId") == null ? null : Long.valueOf(String.valueOf(params.get("saleOrderId")));
    String fileType = params.get("fileType") == null ? null : String.valueOf(params.get("fileType"));
    if (saleOrderId == null || saleOrderId <= 0) {
      return R.error("请先选择销售单");
    }
    if (StringUtils.isBlank(fileType)) {
      return R.error("请先指定确认节点");
    }
    try {
      erpSaleOrderService.confirmInternalStep(saleOrderId, fileType, getUserId());
      return R.ok();
    } catch (RuntimeException e) {
      return R.error(e.getMessage());
    }
  }

  @GetMapping("/download/file/{fileId}")
  @RequiresPermissions("erp:tradeorder:info")
  public ResponseEntity<byte[]> downloadFile(@PathVariable("fileId") Long fileId) {
    return erpSaleOrderService.downloadFile(fileId);
  }

  @PostMapping("/delete/file/{fileId}")
  @RequiresPermissions("erp:tradeorder:update")
  public R deleteFile(@PathVariable("fileId") Long fileId) {
    erpSaleOrderService.deleteFile(fileId, getUserId());
    return R.ok();
  }

  @GetMapping("/portal/info/{token}")
  public R portalInfo(@PathVariable("token") String token) {
    return R.ok().put("saleOrder", erpSaleOrderService.getPortalDetail(token, getUserId()));
  }

  @PostMapping("/portal/upload")
  public R portalUpload(@RequestParam("token") String token,
                        @RequestParam("fileType") String fileType,
                        @RequestParam("files") MultipartFile[] files) throws Exception {
    if (StringUtils.isBlank(token)) {
      return R.error("链接令牌不能为空");
    }
    if (StringUtils.isBlank(fileType)) {
      return R.error("文件类型不能为空");
    }
    if (files == null || files.length == 0) {
      return R.error("请先选择文件");
    }
    return R.ok().put("list", erpSaleOrderService.uploadPortalFiles(token, fileType, files, getUserId()));
  }

  @PostMapping("/portal/confirm")
  public R portalConfirm(@RequestBody Map<String, Object> params) {
    String token = params.get("token") == null ? null : String.valueOf(params.get("token"));
    String fileType = params.get("fileType") == null ? null : String.valueOf(params.get("fileType"));
    if (StringUtils.isBlank(token)) {
      return R.error("链接令牌不能为空");
    }
    if (StringUtils.isBlank(fileType)) {
      return R.error("请先指定确认节点");
    }
    erpSaleOrderService.confirmPortalStep(token, fileType, getUserId());
    return R.ok();
  }

  @GetMapping("/portal/download/file/{fileId}")
  public ResponseEntity<byte[]> portalDownloadFile(@PathVariable("fileId") Long fileId) {
    return erpSaleOrderService.downloadPortalFile(fileId, getUserId());
  }

  @PostMapping("/portal/delete/file/{fileId}")
  public R portalDeleteFile(@PathVariable("fileId") Long fileId) {
    erpSaleOrderService.deletePortalFile(fileId, getUserId());
    return R.ok();
  }

  @GetMapping(value = "/contract/{token}", produces = MediaType.TEXT_HTML_VALUE)
  public ResponseEntity<byte[]> contract(@PathVariable("token") String token) {
    String html = erpSaleOrderService.buildContractHtml(token);
    return ResponseEntity.ok()
        .contentType(new MediaType("text", "html", StandardCharsets.UTF_8))
        .body(html.getBytes(StandardCharsets.UTF_8));
  }

  @GetMapping("/contract/pdf/{token}")
  public ResponseEntity<byte[]> contractPdf(@PathVariable("token") String token) {
    return erpSaleOrderService.downloadContractPdf(token);
  }
}
