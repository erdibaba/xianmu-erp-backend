package io.renren.modules.erp.controller;

import io.renren.common.utils.PageUtils;
import io.renren.common.utils.R;
import io.renren.modules.erp.entity.ErpPartnerEntity;
import io.renren.modules.erp.service.ErpPartnerService;
import io.renren.modules.sys.controller.AbstractController;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("erp/partner")
public class ErpPartnerController extends AbstractController {
  @Autowired
  private ErpPartnerService erpPartnerService;

  @GetMapping("/list")
  @RequiresPermissions("erp:partner:list")
  public R list(@RequestParam Map<String, Object> params) {
    PageUtils page = erpPartnerService.queryPage(params);
    return R.ok().put("page", page);
  }

  @GetMapping("/select")
  public R select(@RequestParam(value = "businessRole", required = false) String businessRole) {
    return R.ok().put("list", erpPartnerService.queryByRole(businessRole));
  }

  @GetMapping("/info/{id}")
  @RequiresPermissions("erp:partner:info")
  public R info(@PathVariable("id") Long id) {
    return R.ok().put("partner", erpPartnerService.getById(id));
  }

  @PostMapping("/save")
  @RequiresPermissions("erp:partner:save")
  public R save(@RequestBody ErpPartnerEntity partner) {
    normalizeColdStorageFreeDays(partner);
    normalizeRiskLevel(partner);
    partner.setCreateUserId(getUserId());
    partner.setCreateTime(new Date());
    partner.setUpdateTime(new Date());
    erpPartnerService.save(partner);
    return R.ok();
  }

  @PostMapping("/update")
  @RequiresPermissions("erp:partner:update")
  public R update(@RequestBody ErpPartnerEntity partner) {
    normalizeColdStorageFreeDays(partner);
    normalizeRiskLevel(partner);
    partner.setUpdateTime(new Date());
    erpPartnerService.updateById(partner);
    return R.ok();
  }

  @PostMapping("/delete")
  @RequiresPermissions("erp:partner:delete")
  public R delete(@RequestBody Long[] ids) {
    erpPartnerService.removeByIds(Arrays.asList(ids));
    return R.ok();
  }

  private void normalizeColdStorageFreeDays(ErpPartnerEntity partner) {
    if (partner.getColdStorageFreeDays() == null || partner.getColdStorageFreeDays() <= 0) {
      partner.setColdStorageFreeDays(7);
    }
  }

  private void normalizeRiskLevel(ErpPartnerEntity partner) {
    if (partner.getRiskLevel() == null || partner.getRiskLevel().trim().isEmpty()) {
      partner.setRiskLevel("NORMAL");
    }
    if ("NORMAL".equals(partner.getRiskLevel())) {
      partner.setRiskRemark(null);
      partner.setRiskMarkDate(null);
    } else if (partner.getRiskMarkDate() == null) {
      partner.setRiskMarkDate(new Date());
    }
  }
}
