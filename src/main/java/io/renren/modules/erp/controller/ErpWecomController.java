package io.renren.modules.erp.controller;

import io.renren.common.utils.R;
import io.renren.modules.erp.service.ErpWecomService;
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

@RestController
@RequestMapping("erp/wecom")
public class ErpWecomController extends AbstractController {
  @Autowired
  private ErpWecomService erpWecomService;

  @PostMapping("/groups/sync")
  @RequiresPermissions("erp:partner:update")
  public R syncGroups() {
    return R.ok().put("list", erpWecomService.syncGroups());
  }

  @GetMapping("/groups/select")
  public R selectGroups(@RequestParam(value = "keyword", required = false) String keyword) {
    return R.ok().put("list", erpWecomService.selectGroups(keyword));
  }

  @PostMapping("/ship-notice/send")
  @RequiresPermissions("erp:tradeorder:update")
  public R sendShipNotice(@RequestBody Map<String, Object> params) {
    Long presaleOrderId = toLong(params.get("presaleOrderId"));
    Long partnerId = toLong(params.get("partnerId"));
    String content = params.get("content") == null ? null : params.get("content").toString();
    return R.ok().put("notice", erpWecomService.sendShipNotice(presaleOrderId, partnerId, content, getUserId()));
  }

  private Long toLong(Object value) {
    if (value == null || "".equals(value.toString())) {
      return null;
    }
    return Long.valueOf(value.toString());
  }
}
