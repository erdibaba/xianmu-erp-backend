package io.renren.modules.erp.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.renren.common.utils.PageUtils;
import io.renren.common.utils.R;
import io.renren.modules.erp.entity.ErpSalespersonEntity;
import io.renren.modules.erp.service.ErpSalespersonService;
import io.renren.modules.sys.controller.AbstractController;
import io.renren.modules.sys.dao.SysUserDao;
import io.renren.modules.sys.entity.SysUserEntity;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
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
@RequestMapping("erp/salesperson")
public class ErpSalespersonController extends AbstractController {
  @Autowired
  private ErpSalespersonService erpSalespersonService;
  @Autowired
  private SysUserDao sysUserDao;

  @GetMapping("/list")
  @RequiresPermissions("erp:salesperson:list")
  public R list(@RequestParam Map<String, Object> params) {
    PageUtils page = erpSalespersonService.queryPage(params);
    return R.ok().put("page", page);
  }

  @GetMapping("/select")
  @RequiresPermissions("erp:salesperson:list")
  public R select(@RequestParam(value = "keyword", required = false) String keyword) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("page", "1");
    params.put("limit", "15");
    params.put("keyword", keyword);
    PageUtils page = erpSalespersonService.queryPage(params);
    return R.ok().put("page", page);
  }

  @GetMapping("/user/select")
  @RequiresPermissions("erp:salesperson:list")
  public R userSelect(@RequestParam(value = "keyword", required = false) String keyword) {
    QueryWrapper<SysUserEntity> wrapper = new QueryWrapper<SysUserEntity>()
        .eq("status", 1)
        .orderByAsc("username")
        .orderByAsc("user_id");
    if (StringUtils.isNotBlank(keyword)) {
      String value = keyword.trim();
      wrapper.and(w -> w.like("username", value).or().like("mobile", value));
    }
    List<SysUserEntity> users = sysUserDao.selectList(wrapper.last("limit 15"));
    List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
    for (SysUserEntity user : users) {
      Map<String, Object> row = new HashMap<String, Object>();
      row.put("userId", user.getUserId());
      row.put("username", user.getUsername());
      row.put("mobile", user.getMobile());
      list.add(row);
    }
    return R.ok().put("list", list);
  }

  @GetMapping("/info/{id}")
  @RequiresPermissions("erp:salesperson:info")
  public R info(@PathVariable("id") Long id) {
    return R.ok().put("salesperson", erpSalespersonService.getById(id));
  }

  @PostMapping("/save")
  @RequiresPermissions("erp:salesperson:save")
  public R save(@RequestBody ErpSalespersonEntity salesperson) {
    erpSalespersonService.saveSalesperson(salesperson, getUserId());
    return R.ok();
  }

  @PostMapping("/update")
  @RequiresPermissions("erp:salesperson:update")
  public R update(@RequestBody ErpSalespersonEntity salesperson) {
    erpSalespersonService.updateSalesperson(salesperson);
    return R.ok();
  }

  @PostMapping("/delete")
  @RequiresPermissions("erp:salesperson:delete")
  public R delete(@RequestBody Long[] ids) {
    erpSalespersonService.removeByIds(Arrays.asList(ids));
    return R.ok();
  }
}
