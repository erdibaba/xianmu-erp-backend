package io.renren.modules.erp.controller;

import io.renren.common.utils.PageUtils;
import io.renren.common.utils.R;
import io.renren.modules.erp.entity.ErpManualExpenseEntity;
import io.renren.modules.erp.service.ErpManualExpenseService;
import io.renren.modules.sys.controller.AbstractController;
import java.util.Map;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("erp/manual-expense")
public class ErpManualExpenseController extends AbstractController {
  @Autowired
  private ErpManualExpenseService erpManualExpenseService;

  @GetMapping("/list")
  @RequiresPermissions("erp:manual-expense:list")
  public R list(@RequestParam Map<String, Object> params) {
    PageUtils page = erpManualExpenseService.queryPage(params);
    return R.ok().put("page", page);
  }

  @GetMapping("/info/{id}")
  @RequiresPermissions("erp:manual-expense:info")
  public R info(@PathVariable("id") Long id) {
    return R.ok().put("expense", erpManualExpenseService.getDetail(id));
  }

  @PostMapping("/save")
  @RequiresPermissions("erp:manual-expense:save")
  public R save(@RequestBody ErpManualExpenseEntity expense) {
    erpManualExpenseService.saveExpense(expense, getUserId());
    return R.ok().put("expense", expense);
  }

  @PostMapping("/update")
  @RequiresPermissions("erp:manual-expense:update")
  public R update(@RequestBody ErpManualExpenseEntity expense) {
    erpManualExpenseService.updateExpense(expense);
    return R.ok().put("expense", expense);
  }

  @PostMapping("/delete")
  @RequiresPermissions("erp:manual-expense:delete")
  public R delete(@RequestBody Long[] ids) {
    erpManualExpenseService.deleteExpenses(ids);
    return R.ok();
  }

  @PostMapping("/upload")
  @RequiresPermissions("erp:manual-expense:update")
  public R upload(@RequestParam("expenseId") Long expenseId,
                  @RequestParam("files") MultipartFile[] files) throws Exception {
    return R.ok().put("list", erpManualExpenseService.uploadFiles(expenseId, files));
  }

  @PostMapping("/delete/file/{fileId}")
  @RequiresPermissions("erp:manual-expense:update")
  public R deleteFile(@PathVariable("fileId") Long fileId) {
    erpManualExpenseService.deleteFile(fileId);
    return R.ok();
  }

  @GetMapping("/download/file/{fileId}")
  @RequiresPermissions("erp:manual-expense:info")
  public ResponseEntity<byte[]> downloadFile(@PathVariable("fileId") Long fileId) {
    return erpManualExpenseService.downloadFile(fileId);
  }
}
