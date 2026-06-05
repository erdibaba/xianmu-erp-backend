package io.renren.modules.erp.controller;

import io.renren.common.utils.R;
import io.renren.modules.erp.entity.ErpFunderLoanRepaymentEntity;
import io.renren.modules.erp.entity.ErpFunderPaymentEntity;
import io.renren.modules.erp.service.ErpFunderFinanceService;
import io.renren.modules.sys.controller.AbstractController;
import java.util.Map;
import org.apache.shiro.authz.annotation.Logical;
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
@RequestMapping("erp/funder-finance")
public class ErpFunderFinanceController extends AbstractController {
  @Autowired
  private ErpFunderFinanceService funderFinanceService;

  @GetMapping("/payment/list")
  @RequiresPermissions("erp:funderpayment:list")
  public R paymentList(@RequestParam Map<String, Object> params) {
    return R.ok().put("page", funderFinanceService.queryPaymentPage(params));
  }

  @GetMapping("/payment/info/{id}")
  @RequiresPermissions("erp:funderpayment:list")
  public R paymentInfo(@PathVariable("id") Long id) {
    return R.ok().put("payment", funderFinanceService.getPaymentDetail(id));
  }

  @GetMapping("/funder-options")
  public R funderOptions(@RequestParam(value = "keyword", required = false) String keyword) {
    return R.ok().put("list", funderFinanceService.queryFunderOptions(keyword));
  }

  @GetMapping("/presale-options")
  public R presaleOptions(@RequestParam(value = "keyword", required = false) String keyword) {
    return R.ok().put("list", funderFinanceService.queryPresaleOptions(keyword));
  }

  @PostMapping("/voucher/recognize")
  @RequiresPermissions(value = {"erp:funderpayment:save", "erp:funderloan:update"}, logical = Logical.OR)
  public R recognizeVoucher(@RequestParam("file") MultipartFile file) throws Exception {
    return R.ok().put("voucher", funderFinanceService.recognizeVoucher(file));
  }

  @PostMapping("/payment/confirm")
  @RequiresPermissions("erp:funderpayment:save")
  public R confirmPayment(@RequestBody ErpFunderPaymentEntity payment) {
    funderFinanceService.confirmPayment(payment, getUserId());
    return R.ok();
  }

  @GetMapping("/loan/list")
  @RequiresPermissions("erp:funderloan:list")
  public R loanList(@RequestParam Map<String, Object> params) {
    return R.ok().put("page", funderFinanceService.queryLoanPage(params));
  }

  @GetMapping("/loan/info/{id}")
  @RequiresPermissions("erp:funderloan:list")
  public R loanInfo(@PathVariable("id") Long id) {
    return R.ok().put("loan", funderFinanceService.getLoanDetail(id));
  }

  @PostMapping("/loan/repayment/calculate")
  @RequiresPermissions("erp:funderloan:update")
  public R calculateRepayment(@RequestBody ErpFunderLoanRepaymentEntity repayment) {
    return R.ok().put("repayment", funderFinanceService.calculateRepayment(repayment));
  }

  @PostMapping("/loan/repayment/confirm")
  @RequiresPermissions("erp:funderloan:update")
  public R confirmRepayment(@RequestBody ErpFunderLoanRepaymentEntity repayment) {
    funderFinanceService.confirmRepayment(repayment, getUserId());
    return R.ok();
  }

  @GetMapping("/payment/download/{id}")
  @RequiresPermissions("erp:funderpayment:list")
  public ResponseEntity<byte[]> downloadPayment(@PathVariable("id") Long id) {
    return funderFinanceService.downloadPaymentVoucher(id);
  }

  @GetMapping("/repayment/download/{id}")
  @RequiresPermissions("erp:funderloan:list")
  public ResponseEntity<byte[]> downloadRepayment(@PathVariable("id") Long id) {
    return funderFinanceService.downloadRepaymentVoucher(id);
  }
}
