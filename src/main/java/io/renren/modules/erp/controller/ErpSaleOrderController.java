package io.renren.modules.erp.controller;

import io.renren.common.utils.PageUtils;
import io.renren.common.utils.R;
import io.renren.modules.erp.entity.ErpSaleOrderEntity;
import io.renren.modules.erp.entity.ErpSaleOrderItemEntity;
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
    erpSaleOrderService.confirmInternalStep(saleOrderId, fileType, getUserId());
    return R.ok();
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
