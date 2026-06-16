package io.renren.modules.erp.controller;

import io.renren.common.utils.R;
import io.renren.modules.erp.service.ErpOcrService;
import io.renren.modules.erp.vo.ErpRecognizeResultVo;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("erp/ocr")
public class ErpOcrController {
  @Autowired
  private ErpOcrService erpOcrService;

  @PostMapping("/recognize")
  @RequiresPermissions("erp:tradeorder:save")
  public R recognize(@RequestParam("file") MultipartFile file,
                     @RequestParam(value = "orderTypeHint", required = false) String orderTypeHint) throws Exception {
    if (file == null || file.isEmpty()) {
      return R.error("\u8bf7\u4e0a\u4f20\u5355\u636e\u56fe\u7247\u6216 PDF");
    }
    String filename = StringUtils.defaultString(file.getOriginalFilename()).toLowerCase();
    if (!(filename.endsWith(".jpg") || filename.endsWith(".jpeg") || filename.endsWith(".png")
        || filename.endsWith(".jfif") || filename.endsWith(".bmp") || filename.endsWith(".pdf"))) {
      return R.error("\u4ec5\u652f\u6301 jpg/jpeg/png/jfif/bmp/pdf");
    }
    ErpRecognizeResultVo result = erpOcrService.recognize(file, orderTypeHint);
    if (Boolean.FALSE.equals(result.getSuccess())) {
      return R.error(StringUtils.defaultIfBlank(result.getMessage(), "\u8bc6\u522b\u5931\u8d25"));
    }
    return R.ok().put("result", result);
  }
}
