/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.baomidou.mybatisplus.core.conditions.Wrapper
 *  com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
 *  com.baomidou.mybatisplus.core.metadata.IPage
 *  com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
 *  org.apache.commons.lang.StringUtils
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.http.ContentDisposition
 *  org.springframework.http.MediaType
 *  org.springframework.http.ResponseEntity
 *  org.springframework.http.ResponseEntity$BodyBuilder
 *  org.springframework.stereotype.Service
 *  org.springframework.transaction.annotation.Transactional
 *  org.springframework.web.multipart.MultipartFile
 */
package io.renren.modules.erp.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.renren.common.utils.PageUtils;
import io.renren.common.utils.Query;
import io.renren.modules.erp.dao.ErpInboundOrderDao;
import io.renren.modules.erp.dao.ErpInboundOrderItemDao;
import io.renren.modules.erp.dao.ErpPartnerDao;
import io.renren.modules.erp.dao.ErpPresaleOrderDao;
import io.renren.modules.erp.dao.ErpPresaleOrderItemDao;
import io.renren.modules.erp.dao.ErpProductDao;
import io.renren.modules.erp.dao.ErpSaleOrderDao;
import io.renren.modules.erp.dao.ErpSaleOrderFileDao;
import io.renren.modules.erp.dao.ErpSaleOrderItemDao;
import io.renren.modules.erp.dao.ErpStockLedgerDao;
import io.renren.modules.erp.dao.ErpWarehouseDao;
import io.renren.modules.erp.entity.ErpInboundOrderEntity;
import io.renren.modules.erp.entity.ErpInboundOrderItemEntity;
import io.renren.modules.erp.entity.ErpPartnerEntity;
import io.renren.modules.erp.entity.ErpPresaleOrderEntity;
import io.renren.modules.erp.entity.ErpPresaleOrderItemEntity;
import io.renren.modules.erp.entity.ErpProductEntity;
import io.renren.modules.erp.entity.ErpSaleOrderEntity;
import io.renren.modules.erp.entity.ErpSaleOrderFileEntity;
import io.renren.modules.erp.entity.ErpSaleOrderItemEntity;
import io.renren.modules.erp.entity.ErpStockLedgerEntity;
import io.renren.modules.erp.entity.ErpWarehouseEntity;
import io.renren.modules.erp.service.ErpSaleOrderService;
import io.renren.modules.erp.vo.ErpSalePresaleItemVo;
import io.renren.modules.erp.vo.ErpSalePresaleOrderVo;
import io.renren.modules.sys.entity.SysUserEntity;
import io.renren.modules.sys.service.SysUserService;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service(value="erpSaleOrderService")
public class ErpSaleOrderServiceImpl
extends ServiceImpl<ErpSaleOrderDao, ErpSaleOrderEntity>
implements ErpSaleOrderService {
    private static final Logger logger = LoggerFactory.getLogger(ErpSaleOrderServiceImpl.class);
    private static final String SALE_TYPE_FUTURES = "FUTURES";
    private static final String SALE_TYPE_SPOT = "SPOT";
    private static final String FILE_TYPE_SIGNED_CONTRACT = "SIGNED_CONTRACT";
    private static final String FILE_TYPE_BUYER_PAYMENT = "BUYER_PAYMENT_PROOF";
    private static final String FILE_TYPE_BUYER_BANK = "BUYER_BANK_SLIP";
    private static final String FILE_TYPE_FUNDER_PAYMENT = "FUNDER_PAYMENT_PROOF";
    private static final String CONTRACT_BASE_URL = "http://192.168.0.36:8080/renren-fast/erp/saleorder/contract/";
    private static final String PORTAL_BASE_URL = "http://192.168.0.36:8001/#/sale-upload/";
    private static final String UPLOAD_BASE_DIR = "D:\\renren-fast-vue\\renren-fast\\uploads\\saleorder";
    private static final String CONTRACT_TEMPLATE_PATH = "D:\\renren-fast-vue\\renren-fast\\src\\main\\resources\\templates\\sale-contract-template.xlsx";
    private static final String CONTRACT_RENDER_SCRIPT_PATH = "D:\\renren-fast-vue\\renren-fast\\scripts\\render-sale-contract.ps1";
    @Autowired
    private ErpSaleOrderItemDao erpSaleOrderItemDao;
    @Autowired
    private ErpSaleOrderFileDao erpSaleOrderFileDao;
    @Autowired
    private ErpPartnerDao erpPartnerDao;
    @Autowired
    private ErpWarehouseDao erpWarehouseDao;
    @Autowired
    private ErpProductDao erpProductDao;
    @Autowired
    private ErpPresaleOrderDao erpPresaleOrderDao;
    @Autowired
    private ErpPresaleOrderItemDao erpPresaleOrderItemDao;
    @Autowired
    private ErpInboundOrderDao erpInboundOrderDao;
    @Autowired
    private ErpInboundOrderItemDao erpInboundOrderItemDao;
    @Autowired
    private ErpStockLedgerDao erpStockLedgerDao;
    @Autowired
    private SysUserService sysUserService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<ErpSaleOrderEntity> wrapper = new QueryWrapper<ErpSaleOrderEntity>().orderByDesc(new String[]{"update_time", "id"});
        String keyword = this.stringValue(params.get("keyword"));
        String saleType = this.stringValue(params.get("saleType"));
        String status = this.stringValue(params.get("status"));
        if (StringUtils.isNotBlank((String)keyword)) {
            wrapper.and(w -> w.like("order_no", keyword).or().like("contract_no", keyword).or().like("secondary_partner_name", keyword).or().like("warehouse_name", keyword));
        }
        if (StringUtils.isNotBlank((String)saleType)) {
            wrapper.eq("sale_type", saleType);
        }
        if (StringUtils.isNotBlank((String)status)) {
            wrapper.eq("status", Integer.valueOf(status));
        }
        IPage<ErpSaleOrderEntity> page = this.page(new Query<ErpSaleOrderEntity>().getPage(params), wrapper);
        for (ErpSaleOrderEntity order : page.getRecords()) {
            this.enrichOrderDisplay(order, false);
        }
        return new PageUtils(page);
    }

    @Override
    public ErpSaleOrderEntity getDetail(Long id) {
        ErpSaleOrderEntity order = (ErpSaleOrderEntity)this.getById(id);
        if (order == null) {
            return null;
        }
        this.loadChildren(order);
        this.enrichOrderDisplay(order, true);
        return order;
    }

    @Override
    @Transactional(rollbackFor={Exception.class})
    public void saveOrder(ErpSaleOrderEntity order, Long userId) {
        this.upsertOrder(order, userId, true);
    }

    @Override
    @Transactional(rollbackFor={Exception.class})
    public void updateOrder(ErpSaleOrderEntity order, Long userId) {
        throw new RuntimeException("销售单创建后不允许修改，请删除后重新创建");
    }

    @Override
    @Transactional(rollbackFor={Exception.class})
    public void deleteOrders(Long[] ids) {
        if (ids == null || ids.length == 0) {
            return;
        }
        for (Long id : ids) {
            long fileCount = this.erpSaleOrderFileDao.selectCount((Wrapper)new QueryWrapper().eq((Object)"sale_order_id", (Object)id)).intValue();
            if (fileCount > 0L) {
                throw new RuntimeException("销售单已上传附件，不能删除");
            }
            this.erpSaleOrderItemDao.delete((Wrapper)new QueryWrapper().eq((Object)"sale_order_id", (Object)id));
            this.erpSaleOrderFileDao.delete((Wrapper)new QueryWrapper().eq((Object)"sale_order_id", (Object)id));
            this.erpStockLedgerDao.delete((Wrapper)((QueryWrapper)new QueryWrapper().eq((Object)"order_id", (Object)id)).eq((Object)"order_type", (Object)"SALE_ORDER"));
        }
        this.removeByIds(Arrays.asList(ids));
    }

    @Override
    public List<ErpSalePresaleOrderVo> queryPresaleOrders(String keyword) {
        QueryWrapper<ErpPresaleOrderEntity> wrapper = new QueryWrapper<ErpPresaleOrderEntity>().orderByDesc(new String[]{"order_date", "id"});
        if (StringUtils.isNotBlank((String)keyword)) {
            wrapper.and(w -> w.like("seller_contract_no", keyword).or().like("order_no", keyword).or().like("customer_reference", keyword).or().like("brand_name", keyword));
        }
        List<ErpPresaleOrderEntity> orders = this.erpPresaleOrderDao.selectList((Wrapper)wrapper.last("limit 15"));
        ArrayList<ErpSalePresaleOrderVo> result = new ArrayList<ErpSalePresaleOrderVo>();
        for (ErpPresaleOrderEntity order : orders) {
            ErpSalePresaleOrderVo vo = new ErpSalePresaleOrderVo();
            vo.setPresaleOrderId(order.getId());
            vo.setPresaleOrderNo(order.getOrderNo());
            vo.setSellerContractNo(order.getSellerContractNo());
            vo.setCustomerReference(order.getCustomerReference());
            vo.setBrandName(order.getBrandName());
            vo.setOrderDate(order.getOrderDate());
            result.add(vo);
        }
        return result;
    }

    @Override
    public List<ErpSalePresaleItemVo> queryPresaleItems(Long productId, String keyword) {
        QueryWrapper<ErpPresaleOrderItemEntity> itemWrapper = new QueryWrapper<ErpPresaleOrderItemEntity>().orderByDesc("id");
        if (productId != null && productId > 0L) {
            itemWrapper.eq("product_id", productId);
        }
        if (StringUtils.isNotBlank((String)keyword)) {
            itemWrapper.and(w -> w.like("product_code", keyword).or().like("product_name", keyword).or().like("product_name_en", keyword));
        }
        List<ErpPresaleOrderItemEntity> items = this.erpPresaleOrderItemDao.selectList((Wrapper)itemWrapper.last("limit 15"));
        ArrayList<ErpSalePresaleItemVo> result = new ArrayList<ErpSalePresaleItemVo>();
        for (ErpPresaleOrderItemEntity item : items) {
            ErpPresaleOrderEntity order = (ErpPresaleOrderEntity)this.erpPresaleOrderDao.selectById(item.getPresaleOrderId());
            if (order == null) continue;
            ErpSalePresaleItemVo vo = new ErpSalePresaleItemVo();
            vo.setPresaleOrderId(order.getId());
            vo.setPresaleOrderItemId(item.getId());
            vo.setPresaleOrderNo(order.getOrderNo());
            vo.setSellerContractNo(order.getSellerContractNo());
            vo.setCustomerReference(order.getCustomerReference());
            vo.setProductId(item.getProductId());
            vo.setProductCode(item.getProductCode());
            vo.setProductName(item.getProductName());
            vo.setProductNameEn(item.getProductNameEn());
            vo.setQuantityTon(item.getQuantityTon());
            vo.setQuantityKg(item.getQuantityKg());
            vo.setOrderDate(order.getOrderDate());
            result.add(vo);
        }
        return result;
    }

    @Override
    public List<ErpSaleOrderItemEntity> previewAllocation(ErpSaleOrderEntity order) {
        if (order == null) {
            throw new RuntimeException("销售单不能为空");
        }
        order.setSaleType(StringUtils.upperCase((String)StringUtils.trim((String)order.getSaleType())));
        if (!SALE_TYPE_SPOT.equals(order.getSaleType())) {
            throw new RuntimeException("只有现货单支持预览分配");
        }
        if (order.getWarehouseId() == null) {
            throw new RuntimeException("请先选择仓库");
        }
        if (order.getItemList() == null || order.getItemList().isEmpty()) {
            throw new RuntimeException("请先录入现货产品明细");
        }
        this.validateSpotRequestItems(order.getItemList(), false);
        return this.buildSpotAllocationItems(order, null, false);
    }

    @Override
    @Transactional(rollbackFor={Exception.class})
    public List<ErpSaleOrderFileEntity> uploadFiles(Long saleOrderId, String fileType, MultipartFile[] files) throws Exception {
        return this.doUploadFiles(saleOrderId, fileType, files, null, false);
    }

    @Override
    @Transactional(rollbackFor={Exception.class})
    public List<ErpSaleOrderFileEntity> uploadFiles(Long saleOrderId, String fileType, MultipartFile[] files, Long userId) throws Exception {
        return this.doUploadFiles(saleOrderId, fileType, files, userId, false);
    }

    private List<ErpSaleOrderFileEntity> doUploadFiles(Long saleOrderId, String fileType, MultipartFile[] files, Long userId, boolean portalMode) throws Exception {
        ErpSaleOrderEntity order = (ErpSaleOrderEntity)this.getById(saleOrderId);
        if (order == null) {
            throw new RuntimeException("销售单不存在");
        }
        this.validateUploadAccess(order, fileType, userId, portalMode);
        int lineNo = this.nextFileLineNo(saleOrderId, fileType);
        Date now = new Date();
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) continue;
            Path savedPath = this.saveUploadFile(fileType, file);
            ErpSaleOrderFileEntity entity = new ErpSaleOrderFileEntity();
            entity.setSaleOrderId(saleOrderId);
            entity.setFileType(fileType);
            entity.setLineNo(lineNo++);
            entity.setFilePath(savedPath.toAbsolutePath().toString());
            entity.setFileName(savedPath.getFileName().toString());
            entity.setCreateTime(now);
            entity.setUpdateTime(now);
            this.erpSaleOrderFileDao.insert(entity);
        }
        this.resetConfirmFlag(order, fileType);
        this.refreshOrderStatus(saleOrderId);
        return this.erpSaleOrderFileDao.selectList((Wrapper)((QueryWrapper)((QueryWrapper)new QueryWrapper().eq((Object)"sale_order_id", (Object)saleOrderId)).eq((Object)"file_type", (Object)fileType)).orderByAsc((Object[])new String[]{"line_no", "id"}));
    }

    @Override
    public ResponseEntity<byte[]> downloadFile(Long fileId) {
        ErpSaleOrderFileEntity file = (ErpSaleOrderFileEntity)this.erpSaleOrderFileDao.selectById(fileId);
        if (file == null || StringUtils.isBlank((String)file.getFilePath())) {
            throw new RuntimeException("文件不存在");
        }
        try {
            File target = new File(file.getFilePath());
            if (!target.exists()) {
                throw new RuntimeException("文件不存在");
            }
            byte[] bytes = Files.readAllBytes(target.toPath());
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).header("Content-Disposition", ContentDisposition.attachment().filename(file.getFileName(), StandardCharsets.UTF_8).build().toString()).body(bytes);
        }
        catch (Exception ex) {
            throw new RuntimeException("文件下载失败: " + ex.getMessage(), ex);
        }
    }

    @Override
    @Transactional(rollbackFor={Exception.class})
    public void deleteFile(Long fileId, Long userId) {
        ErpSaleOrderFileEntity file = (ErpSaleOrderFileEntity)this.erpSaleOrderFileDao.selectById(fileId);
        if (file == null) {
            throw new RuntimeException("附件不存在");
        }
        ErpSaleOrderEntity order = (ErpSaleOrderEntity)this.getById(file.getSaleOrderId());
        if (order == null) {
            throw new RuntimeException("销售单不存在");
        }
        this.validateDeleteAccess(order, file.getFileType(), false);
        this.deleteFileRecord(file);
        this.refreshOrderStatus(order.getId());
    }

    @Override
    public ResponseEntity<byte[]> downloadPortalFile(Long fileId, Long userId) {
        ErpSaleOrderFileEntity file = (ErpSaleOrderFileEntity)this.erpSaleOrderFileDao.selectById(fileId);
        if (file == null) {
            throw new RuntimeException("附件不存在");
        }
        ErpSaleOrderEntity order = (ErpSaleOrderEntity)this.getById(file.getSaleOrderId());
        this.ensurePortalOwner(order, userId);
        return this.downloadFile(fileId);
    }

    @Override
    public ErpSaleOrderEntity getPortalDetail(String token, Long userId) {
        ErpSaleOrderEntity order = this.getOrderByToken(token);
        this.ensurePortalOwner(order, userId);
        this.loadChildren(order);
        this.enrichOrderDisplay(order, true);
        return order;
    }

    @Override
    @Transactional(rollbackFor={Exception.class})
    public List<ErpSaleOrderFileEntity> uploadPortalFiles(String token, String fileType, MultipartFile[] files, Long userId) throws Exception {
        ErpSaleOrderEntity order = this.getOrderByToken(token);
        this.ensurePortalOwner(order, userId);
        return this.doUploadFiles(order.getId(), fileType, files, userId, true);
    }

    @Override
    @Transactional(rollbackFor={Exception.class})
    public void deletePortalFile(Long fileId, Long userId) {
        ErpSaleOrderFileEntity file = (ErpSaleOrderFileEntity)this.erpSaleOrderFileDao.selectById(fileId);
        if (file == null) {
            throw new RuntimeException("附件不存在");
        }
        ErpSaleOrderEntity order = (ErpSaleOrderEntity)this.getById(file.getSaleOrderId());
        this.ensurePortalOwner(order, userId);
        this.validateDeleteAccess(order, file.getFileType(), true);
        this.deleteFileRecord(file);
        this.refreshOrderStatus(order.getId());
    }

    @Override
    @Transactional(rollbackFor={Exception.class})
    public void confirmPortalStep(String token, String fileType, Long userId) {
        ErpSaleOrderEntity order = this.getOrderByToken(token);
        this.ensurePortalOwner(order, userId);
        this.confirmStep(order, fileType, userId, true);
    }

    @Override
    @Transactional(rollbackFor={Exception.class})
    public void confirmInternalStep(Long saleOrderId, String fileType, Long userId) {
        ErpSaleOrderEntity order = (ErpSaleOrderEntity)this.getById(saleOrderId);
        if (order == null) {
            throw new RuntimeException("销售单不存在");
        }
        this.confirmStep(order, fileType, userId, false);
    }

    @Override
    public String buildContractHtml(String token) {
        if (StringUtils.isBlank((String)token)) {
            return "<html><body><h3>合同链接无效</h3></body></html>";
        }
        ErpSaleOrderEntity order = (ErpSaleOrderEntity)this.getOne((Wrapper)((QueryWrapper)new QueryWrapper().eq((Object)"contract_token", (Object)token)).last("limit 1"));
        if (order == null) {
            return "<html><body><h3>合同不存在或链接已失效</h3></body></html>";
        }
        this.loadChildren(order);
        this.enrichOrderDisplay(order, true);
        List<ErpSaleOrderItemEntity> displayItems = SALE_TYPE_SPOT.equals(order.getSaleType()) ? order.getAllocationItemList() : order.getItemList();
        StringBuilder html = new StringBuilder();
        html.append("<html><head><meta charset='UTF-8'><title>销售合同</title>").append("<style>body{font-family:Arial,'Microsoft YaHei';padding:24px;color:#222;}table{width:100%;border-collapse:collapse;margin-top:16px;}th,td{border:1px solid #dcdfe6;padding:8px;font-size:14px;}th{background:#f5f7fa;}h2{margin:0 0 12px;} .meta{line-height:28px;}</style>").append("</head><body>");
        html.append("<h2>销售合同</h2>");
        html.append("<div class='meta'>合同号：").append(this.escapeHtml(order.getContractNo())).append("</div>");
        html.append("<div class='meta'>销售单号：").append(this.escapeHtml(order.getOrderNo())).append("</div>");
        html.append("<div class='meta'>类型：").append(SALE_TYPE_SPOT.equals(order.getSaleType()) ? "现货单" : "期货单").append("</div>");
        html.append("<div class='meta'>二批商：").append(this.escapeHtml(order.getSecondaryPartnerName())).append("</div>");
        html.append("<div class='meta'>仓库：").append(this.escapeHtml(StringUtils.defaultString((String)order.getWarehouseName(), (String)"-"))).append("</div>");
        html.append("<table><thead><tr><th>序号</th><th>产品编码</th><th>中文名称</th><th>英文名称</th><th>柜号</th><th>箱数</th></tr></thead><tbody>");
        int index = 1;
        for (ErpSaleOrderItemEntity item : displayItems) {
            html.append("<tr>").append("<td>").append(index++).append("</td>").append("<td>").append(this.escapeHtml(item.getProductCode())).append("</td>").append("<td>").append(this.escapeHtml(item.getProductName())).append("</td>").append("<td>").append(this.escapeHtml(item.getProductNameEn())).append("</td>").append("<td>").append(this.escapeHtml(this.firstNonBlank(item.getSourceContainerNo(), "-"))).append("</td>").append("<td>").append(this.defaultInt(item.getBoxes())).append("</td>").append("</tr>");
        }
        html.append("</tbody></table>");
        html.append("<div style='margin-top:24px;'>请先下载PDF合同并盖章，回传后再进入付款凭证上传流程。</div>");
        html.append("<div style='margin-top:16px;display:flex;gap:12px;flex-wrap:wrap;'>");
        html.append("<a href='").append(CONTRACT_BASE_URL).append("pdf/").append(this.escapeHtml(order.getContractToken())).append("' style='display:inline-block;padding:10px 16px;background:#0B1457;color:#fff;text-decoration:none;border-radius:4px;'>下载PDF合同</a>");
        html.append("<a href='").append(PORTAL_BASE_URL).append(this.escapeHtml(order.getContractToken())).append("' style='display:inline-block;padding:10px 16px;background:#0B1457;color:#fff;text-decoration:none;border-radius:4px;'>登录上传盖章合同与付款凭证</a>");
        html.append("</div>");
        html.append("</body></html>");
        return html.toString();
    }

    @Override
    public ResponseEntity<byte[]> downloadContractPdf(String token) {
        ErpSaleOrderEntity order = this.getOrderByToken(token);
        this.loadChildren(order);
        this.enrichOrderDisplay(order, true);
        try {
            Path pdfPath = this.renderContractPdf(order);
            byte[] bytes = Files.readAllBytes(pdfPath);
            String fileName = this.buildContractFileName(order, ".pdf");
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).header("Content-Disposition", ContentDisposition.attachment().filename(fileName, StandardCharsets.UTF_8).build().toString()).body(bytes);
        }
        catch (Exception ex) {
            throw new RuntimeException("生成PDF合同失败: " + ex.getMessage(), ex);
        }
    }

    private Path renderContractPdf(ErpSaleOrderEntity order) throws Exception {
        File templateFile = new File(CONTRACT_TEMPLATE_PATH);
        File renderScriptFile = new File(CONTRACT_RENDER_SCRIPT_PATH);
        if (!templateFile.exists()) {
            throw new RuntimeException("合同模板不存在: " + CONTRACT_TEMPLATE_PATH);
        }
        if (!renderScriptFile.exists()) {
            throw new RuntimeException("合同渲染脚本不存在: " + CONTRACT_RENDER_SCRIPT_PATH);
        }
        List<Map<String, Object>> rows = this.buildContractRows(order);
        if (rows.isEmpty()) {
            throw new RuntimeException("销售单暂无可生成合同的明细");
        }
        Path outputDir = Paths.get(UPLOAD_BASE_DIR, "contracts", new SimpleDateFormat("yyyyMMdd").format(new Date()));
        Files.createDirectories(outputDir, new FileAttribute[0]);
        String fileBaseName = this.buildContractFileName(order, "");
        String renderSuffix = "_" + System.currentTimeMillis();
        Path outputXlsx = outputDir.resolve(fileBaseName + renderSuffix + ".xlsx");
        Path outputPdf = outputDir.resolve(fileBaseName + renderSuffix + ".pdf");
        Path payloadPath = Files.createTempFile("sale-contract-", ".json", new FileAttribute[0]);
        try {
            Map<String, Object> payload = new LinkedHashMap<String, Object>();
            payload.put("signDateText", new SimpleDateFormat("yyyy/M/d").format(order.getContractSignDate()));
            payload.put("secondaryPartnerName", this.firstNonBlank(order.getSecondaryPartnerName(), "-"));
            payload.put("contractNo", this.firstNonBlank(order.getContractNo(), order.getOrderNo(), "-"));
            payload.put("rows", rows);
            Files.write(payloadPath, JSON.toJSONString(payload, SerializerFeature.WriteMapNullValue).getBytes(StandardCharsets.UTF_8));
            this.runContractRenderScript(templateFile, renderScriptFile, payloadPath.toFile(), outputXlsx.toFile(), outputPdf.toFile());
            if (!Files.exists(outputXlsx) || !Files.exists(outputPdf)) {
                throw new RuntimeException("合同文件未生成成功");
            }
            return outputPdf;
        }
        finally {
            Files.deleteIfExists(payloadPath);
        }
    }

    private void runContractRenderScript(File templateFile, File renderScriptFile, File payloadFile, File outputXlsx, File outputPdf) throws Exception {
        ProcessBuilder builder = new ProcessBuilder(new String[]{"powershell", "-ExecutionPolicy", "Bypass", "-File", renderScriptFile.getAbsolutePath(), "-TemplatePath", templateFile.getAbsolutePath(), "-PayloadPath", payloadFile.getAbsolutePath(), "-OutputXlsx", outputXlsx.getAbsolutePath(), "-OutputPdf", outputPdf.getAbsolutePath()});
        builder.redirectErrorStream(true);
        Process process = builder.start();
        String log = new String(Files.readAllBytes(this.copyProcessOutput(process)), StandardCharsets.UTF_8);
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException(StringUtils.defaultIfEmpty((String)StringUtils.trim((String)log), (String)"合同渲染脚本执行失败"));
        }
    }

    private Path copyProcessOutput(Process process) throws IOException {
        Path tempLog = Files.createTempFile("sale-contract-render-", ".log", new FileAttribute[0]);
        Files.copy(process.getInputStream(), tempLog, StandardCopyOption.REPLACE_EXISTING);
        return tempLog;
    }

    private String buildContractFileName(ErpSaleOrderEntity order, String suffix) {
        String baseName = this.firstNonBlank(order.getContractNo(), order.getOrderNo(), "sale-contract");
        String safeName = baseName.replaceAll("[\\\\/:*?\"<>|\\s]+", "_");
        if (StringUtils.isBlank((String)safeName)) {
            safeName = "sale-contract";
        }
        return safeName + suffix;
    }

    private List<Map<String, Object>> buildContractRows(ErpSaleOrderEntity order) {
        List<ErpSaleOrderItemEntity> sourceItems = SALE_TYPE_SPOT.equals(order.getSaleType()) ? order.getAllocationItemList() : order.getItemList();
        ArrayList<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        int rowNo = 1;
        for (ErpSaleOrderItemEntity item : sourceItems) {
            if (item == null) continue;
            if (item.getBoxes() == null || item.getBoxes() <= 0) {
                throw new RuntimeException("合同第" + rowNo + "行件数不能为空");
            }
            if (item.getContractQuantityKg() == null || item.getContractQuantityKg().compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("合同第" + rowNo + "行数量/千克必须大于0");
            }
            if (item.getSalePriceKg() == null || item.getSalePriceKg().compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("合同第" + rowNo + "行销售价（元/千克）必须大于0");
            }
            if (StringUtils.isBlank((String)item.getContractFactoryNo())) {
                throw new RuntimeException("合同第" + rowNo + "行厂号不能为空");
            }
            if (StringUtils.isBlank((String)item.getContractPortCold())) {
                throw new RuntimeException("合同第" + rowNo + "行港口/冷库不能为空");
            }
            Map<String, Object> row = new LinkedHashMap<String, Object>();
            row.put("contractNo", this.firstNonBlank(order.getContractNo(), order.getOrderNo(), "-"));
            row.put("factoryNo", StringUtils.trimToEmpty((String)item.getContractFactoryNo()));
            row.put("containerNo", SALE_TYPE_SPOT.equals(order.getSaleType()) ? this.firstNonBlank(item.getSourceContainerNo(), "") : "");
            row.put("productName", this.firstNonBlank(item.getProductName(), item.getProductNameEn(), "-"));
            row.put("boxes", item.getBoxes());
            row.put("quantityKg", item.getContractQuantityKg().setScale(2, RoundingMode.HALF_UP));
            row.put("salePriceKg", item.getSalePriceKg().setScale(2, RoundingMode.HALF_UP));
            row.put("portCold", StringUtils.trimToEmpty((String)item.getContractPortCold()));
            row.put("arrivalText", "证件齐全");
            rows.add(row);
            rowNo++;
        }
        return rows;
    }

    @Transactional(rollbackFor={Exception.class})
    protected void upsertOrder(ErpSaleOrderEntity order, Long userId, boolean create) {
        List<ErpSaleOrderItemEntity> preparedItems;
        ErpSaleOrderEntity existing;
        if (order == null) {
            throw new RuntimeException("销售单不能为空");
        }
        ErpSaleOrderEntity erpSaleOrderEntity = existing = create ? null : (ErpSaleOrderEntity)this.getById(order.getId());
        if (!create && existing == null) {
            throw new RuntimeException("销售单不存在");
        }
        Date now = new Date();
        this.normalizeOrder(order, existing, userId, now, create);
        List<ErpSaleOrderItemEntity> list = preparedItems = SALE_TYPE_SPOT.equals(order.getSaleType()) ? this.buildSpotAllocationItems(order, existing, true) : this.buildFuturesItems(order);
        if (create) {
            this.save(order);
        } else {
            this.updateById(order);
            this.erpSaleOrderItemDao.delete((Wrapper)new QueryWrapper().eq((Object)"sale_order_id", (Object)order.getId()));
            this.erpStockLedgerDao.delete((Wrapper)((QueryWrapper)new QueryWrapper().eq((Object)"order_id", (Object)order.getId())).eq((Object)"order_type", (Object)"SALE_ORDER"));
        }
        this.saveItems(order, preparedItems, now);
        this.refreshOrderStatus(order.getId());
    }

    private void normalizeOrder(ErpSaleOrderEntity order, ErpSaleOrderEntity existing, Long userId, Date now, boolean create) {
        order.setSaleType(StringUtils.upperCase((String)StringUtils.trim((String)order.getSaleType())));
        this.validateOrder(order);
        if (StringUtils.isBlank((String)order.getOrderNo())) {
            order.setOrderNo(this.generateOrderNo(order.getSaleType()));
        }
        if (StringUtils.isBlank((String)order.getContractNo())) {
            order.setContractNo(order.getOrderNo());
        }
        if (StringUtils.isBlank((String)order.getContractToken())) {
            order.setContractToken(UUID.randomUUID().toString().replace("-", ""));
        }
        this.fillPartnerSnapshot(order);
        this.fillWarehouseSnapshot(order);
        if (create) {
            order.setCreateUserId(userId);
            order.setCreateTime(now);
            order.setSignedContractConfirmed(0);
            order.setBuyerPaymentConfirmed(0);
            order.setBuyerBankConfirmed(0);
            order.setFunderPaymentConfirmed(0);
        } else {
            order.setCreateUserId(existing.getCreateUserId());
            order.setCreateTime(existing.getCreateTime());
            if (StringUtils.isBlank((String)order.getContractToken())) {
                order.setContractToken(existing.getContractToken());
            }
            if (StringUtils.isBlank((String)order.getOrderNo())) {
                order.setOrderNo(existing.getOrderNo());
            }
            if (StringUtils.isBlank((String)order.getContractNo())) {
                order.setContractNo(existing.getContractNo());
            }
            order.setSignedContractConfirmed(this.defaultFlag(existing.getSignedContractConfirmed()));
            order.setBuyerPaymentConfirmed(this.defaultFlag(existing.getBuyerPaymentConfirmed()));
            order.setBuyerBankConfirmed(this.defaultFlag(existing.getBuyerBankConfirmed()));
            order.setFunderPaymentConfirmed(this.defaultFlag(existing.getFunderPaymentConfirmed()));
        }
        order.setUpdateTime(now);
    }

    private void validateOrder(ErpSaleOrderEntity order) {
        if (!SALE_TYPE_FUTURES.equals(order.getSaleType()) && !SALE_TYPE_SPOT.equals(order.getSaleType())) {
            throw new RuntimeException("销售类型不能为空");
        }
        if (order.getSecondaryPartnerId() == null) {
            throw new RuntimeException("请选择二批商");
        }
        if (SALE_TYPE_SPOT.equals(order.getSaleType()) && order.getWarehouseId() == null) {
            throw new RuntimeException("现货单必须选择仓库");
        }
        if (order.getItemList() == null || order.getItemList().isEmpty()) {
            throw new RuntimeException("销售明细不能为空");
        }
        if (order.getContractSignDate() == null) {
            throw new RuntimeException("请填写签订日期");
        }
        if (SALE_TYPE_SPOT.equals(order.getSaleType())) {
            this.validateSpotRequestItems(order.getItemList(), true);
        }
    }

    private void fillPartnerSnapshot(ErpSaleOrderEntity order) {
        ErpPartnerEntity partner;
        ErpPartnerEntity erpPartnerEntity = partner = order.getSecondaryPartnerId() == null ? null : (ErpPartnerEntity)this.erpPartnerDao.selectById(order.getSecondaryPartnerId());
        if (partner != null) {
            order.setSecondaryPartnerName(partner.getPartnerName());
        }
    }

    private void fillWarehouseSnapshot(ErpSaleOrderEntity order) {
        if (order.getWarehouseId() == null) {
            order.setWarehouseName(null);
            return;
        }
        ErpWarehouseEntity warehouse = (ErpWarehouseEntity)this.erpWarehouseDao.selectById(order.getWarehouseId());
        if (warehouse != null) {
            order.setWarehouseName(warehouse.getWarehouseName());
        }
    }

    private List<ErpSaleOrderItemEntity> buildFuturesItems(ErpSaleOrderEntity order) {
        ArrayList<ErpSaleOrderItemEntity> result = new ArrayList<ErpSaleOrderItemEntity>();
        HashMap<Long, ErpPresaleOrderItemEntity> presaleItemMap = new HashMap<Long, ErpPresaleOrderItemEntity>();
        ErpPresaleOrderEntity presaleOrder = null;
        if (order.getSourcePresaleOrderId() != null) {
            presaleOrder = (ErpPresaleOrderEntity)this.erpPresaleOrderDao.selectById(order.getSourcePresaleOrderId());
            if (presaleOrder == null) {
                throw new RuntimeException("关联预销售单不存在");
            }
            List<ErpPresaleOrderItemEntity> presaleItems = this.erpPresaleOrderItemDao.selectList((Wrapper)((QueryWrapper)new QueryWrapper().eq((Object)"presale_order_id", (Object)presaleOrder.getId())).orderByAsc((Object[])new String[]{"line_no", "id"}));
            for (ErpPresaleOrderItemEntity presaleItem : presaleItems) {
                if (presaleItem.getProductId() == null || presaleItemMap.containsKey(presaleItem.getProductId())) continue;
                presaleItemMap.put(presaleItem.getProductId(), presaleItem);
            }
        }
        int lineNo = 1;
        for (ErpSaleOrderItemEntity item : order.getItemList()) {
            if (item == null) continue;
            if (item.getProductId() == null) {
                throw new RuntimeException("期货单第" + lineNo + "行产品不能为空");
            }
            if (item.getBoxes() == null || item.getBoxes() <= 0) {
                throw new RuntimeException("期货单第" + lineNo + "行箱数必须大于0");
            }
            if (item.getSalePriceKg() == null || item.getSalePriceKg().compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("期货单第" + lineNo + "行销售价（元/千克）必须大于0");
            }
            if (item.getContractQuantityKg() == null || item.getContractQuantityKg().compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("期货单第" + lineNo + "行数量/千克必须大于0");
            }
            if (StringUtils.isBlank((String)item.getContractFactoryNo())) {
                throw new RuntimeException("期货单第" + lineNo + "行厂号不能为空");
            }
            if (StringUtils.isBlank((String)item.getContractPortCold())) {
                throw new RuntimeException("期货单第" + lineNo + "行港口/冷库不能为空");
            }
            ErpProductEntity product = (ErpProductEntity)this.erpProductDao.selectById(item.getProductId());
            ErpPresaleOrderItemEntity presaleItem = (ErpPresaleOrderItemEntity)presaleItemMap.get(item.getProductId());
            if (product == null) {
                throw new RuntimeException("期货单第" + lineNo + "行产品不存在");
            }
            if (presaleOrder != null && presaleItem == null) {
                throw new RuntimeException("期货单第" + lineNo + "行产品未在关联预销售单中找到");
            }
            ErpSaleOrderItemEntity saved = new ErpSaleOrderItemEntity();
            saved.setLineNo(lineNo++);
            saved.setSaleType(SALE_TYPE_FUTURES);
            saved.setProductId(product.getId());
            saved.setProductCode(product.getProductCode());
            saved.setProductName(product.getProductName());
            saved.setProductNameEn(product.getProductNameEn());
            saved.setProductSpec(product.getProductSpec());
            saved.setUnit(product.getUnit());
            saved.setBoxes(item.getBoxes());
            saved.setSourcePresaleOrderId(presaleOrder == null ? null : presaleOrder.getId());
            saved.setSourcePresaleOrderNo(presaleOrder == null ? null : this.firstNonBlank(presaleOrder.getSellerContractNo(), presaleOrder.getOrderNo()));
            saved.setSourcePresaleOrderItemId(presaleItem == null ? null : presaleItem.getId());
            saved.setSalePriceKg(item.getSalePriceKg().setScale(2, RoundingMode.HALF_UP));
            saved.setContractQuantityKg(item.getContractQuantityKg().setScale(2, RoundingMode.HALF_UP));
            saved.setContractFactoryNo(StringUtils.trimToEmpty((String)item.getContractFactoryNo()));
            saved.setContractPortCold(StringUtils.trimToEmpty((String)item.getContractPortCold()));
            saved.setRemark(item.getRemark());
            result.add(saved);
        }
        return result;
    }

    private List<ErpSaleOrderItemEntity> buildSpotAllocationItems(ErpSaleOrderEntity order, ErpSaleOrderEntity existing, boolean requireContractFields) {
        Map<Long, List<StockCandidate>> stockMap = this.loadSpotStockMap(order.getWarehouseId(), order.getId());
        Map<Long, ErpSaleOrderItemEntity> inputAllocationMap = this.buildSpotAllocationInputMap(order.getAllocationItemList());
        ArrayList<ErpSaleOrderItemEntity> result = new ArrayList<ErpSaleOrderItemEntity>();
        int lineNo = 1;
        for (ErpSaleOrderItemEntity requestItem : order.getItemList()) {
            if (requestItem == null) continue;
            if (requestItem.getProductId() == null) {
                throw new RuntimeException("现货单第" + lineNo + "行产品不能为空");
            }
            if (requestItem.getBoxes() == null || requestItem.getBoxes() <= 0) {
                throw new RuntimeException("现货单第" + lineNo + "行箱数必须大于0");
            }
            if (requestItem.getSalePriceKg() == null || requestItem.getSalePriceKg().compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("现货单第" + lineNo + "行销售价（元/千克）必须大于0");
            }
            List<StockCandidate> candidates = stockMap.get(requestItem.getProductId());
            if (candidates == null || candidates.isEmpty()) {
                throw new RuntimeException("产品" + StringUtils.defaultString((String)requestItem.getProductCode(), (String)"-") + "在所选仓库没有可用库存");
            }
            int remaining = requestItem.getBoxes();
            for (StockCandidate candidate : candidates) {
                if (remaining <= 0) break;
                if (candidate.availableBoxes <= 0) continue;
                int allocate = Math.min(candidate.availableBoxes, remaining);
                remaining -= allocate;
                StockCandidate stockCandidate = candidate;
                stockCandidate.availableBoxes = stockCandidate.availableBoxes - allocate;
                ErpSaleOrderItemEntity allocated = new ErpSaleOrderItemEntity();
                allocated.setLineNo(lineNo++);
                allocated.setSaleType(SALE_TYPE_SPOT);
                allocated.setProductId(candidate.product.getId());
                allocated.setProductCode(candidate.product.getProductCode());
                allocated.setProductName(candidate.product.getProductName());
                allocated.setProductNameEn(candidate.product.getProductNameEn());
                allocated.setProductSpec(candidate.product.getProductSpec());
                allocated.setUnit(candidate.product.getUnit());
                allocated.setBoxes(allocate);
                allocated.setSourceInboundOrderId(candidate.inboundOrder.getId());
                allocated.setSourceInboundItemId(candidate.inboundItem.getId());
                allocated.setSourceContainerNo(candidate.inboundOrder.getContainerNo());
                allocated.setWarehouseId(candidate.inboundOrder.getWarehouseId());
                allocated.setWarehouseName(candidate.inboundOrder.getWarehouseName());
                allocated.setBrandId(candidate.inboundOrder.getBrandId());
                allocated.setBrandName(candidate.inboundOrder.getBrandName());
                allocated.setInboundDate(candidate.inboundOrder.getCreateTime());
                allocated.setProductionDate(candidate.inboundItem.getProductionDate());
                allocated.setExpiryDate(candidate.inboundItem.getExpiryDate());
                allocated.setSpecWeight(candidate.inboundItem.getSpecWeight());
                allocated.setSalePriceKg(requestItem.getSalePriceKg().setScale(2, RoundingMode.HALF_UP));
                ErpSaleOrderItemEntity inputAllocation = (ErpSaleOrderItemEntity)inputAllocationMap.get(candidate.inboundItem.getId());
                if (inputAllocation != null) {
                    allocated.setContractQuantityKg(inputAllocation.getContractQuantityKg());
                    allocated.setContractFactoryNo(StringUtils.trimToEmpty((String)inputAllocation.getContractFactoryNo()));
                    allocated.setContractPortCold(StringUtils.trimToEmpty((String)inputAllocation.getContractPortCold()));
                }
                allocated.setRemark(requestItem.getRemark());
                result.add(allocated);
            }
            if (remaining <= 0) continue;
            throw new RuntimeException("产品" + StringUtils.defaultString((String)requestItem.getProductCode(), (String)"-") + "库存不足，还缺少" + remaining + "箱");
        }
        if (requireContractFields) {
            int allocationRowNo = 1;
            for (ErpSaleOrderItemEntity allocated : result) {
                if (allocated.getContractQuantityKg() == null || allocated.getContractQuantityKg().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new RuntimeException("现货单第" + allocationRowNo + "条分配明细数量/千克必须大于0");
                }
                if (StringUtils.isBlank((String)allocated.getContractFactoryNo())) {
                    throw new RuntimeException("现货单第" + allocationRowNo + "条分配明细厂号不能为空");
                }
                if (StringUtils.isBlank((String)allocated.getContractPortCold())) {
                    throw new RuntimeException("现货单第" + allocationRowNo + "条分配明细港口/冷库不能为空");
                }
                allocated.setContractQuantityKg(allocated.getContractQuantityKg().setScale(2, RoundingMode.HALF_UP));
                allocationRowNo++;
            }
        }
        return result;
    }

    private Map<Long, List<StockCandidate>> loadSpotStockMap(Long warehouseId, Long excludeOrderId) {
        QueryWrapper<ErpInboundOrderItemEntity> itemWrapper = new QueryWrapper<ErpInboundOrderItemEntity>().isNotNull("product_id").orderByAsc(new String[]{"expiry_date", "id"});
        List<ErpInboundOrderItemEntity> inboundItems = this.erpInboundOrderItemDao.selectList((Wrapper)itemWrapper);
        if (inboundItems.isEmpty()) {
            return Collections.emptyMap();
        }
        ArrayList<Long> inboundOrderIds = new ArrayList<Long>();
        ArrayList<Long> inboundItemIds = new ArrayList<Long>();
        for (ErpInboundOrderItemEntity item : inboundItems) {
            inboundOrderIds.add(item.getInboundOrderId());
            inboundItemIds.add(item.getId());
        }
        HashMap<Long, ErpInboundOrderEntity> inboundOrderMap = new HashMap<Long, ErpInboundOrderEntity>();
        for (ErpInboundOrderEntity order : this.erpInboundOrderDao.selectBatchIds(inboundOrderIds)) {
            if (order.getWarehouseId() == null || !order.getWarehouseId().equals(warehouseId)) continue;
            inboundOrderMap.put(order.getId(), order);
        }
        if (inboundOrderMap.isEmpty()) {
            return Collections.emptyMap();
        }
        QueryWrapper saleItemWrapper = (QueryWrapper)((QueryWrapper)new QueryWrapper().eq((Object)"sale_type", (Object)SALE_TYPE_SPOT)).in((Object)"source_inbound_item_id", inboundItemIds);
        if (excludeOrderId != null && excludeOrderId > 0L) {
            saleItemWrapper.ne((Object)"sale_order_id", (Object)excludeOrderId);
        }
        List<ErpSaleOrderItemEntity> soldItems = this.erpSaleOrderItemDao.selectList((Wrapper)saleItemWrapper);
        HashMap<Long, Integer> consumedMap = new HashMap<Long, Integer>();
        for (ErpSaleOrderItemEntity soldItem : soldItems) {
            if (soldItem.getSourceInboundItemId() == null) continue;
            consumedMap.put(soldItem.getSourceInboundItemId(), this.defaultInt((Integer)consumedMap.get(soldItem.getSourceInboundItemId())) + this.defaultInt(soldItem.getBoxes()));
        }
        LinkedHashMap<Long, List<StockCandidate>> result = new LinkedHashMap<Long, List<StockCandidate>>();
        for (ErpInboundOrderItemEntity inboundItem : inboundItems) {
            ErpProductEntity product;
            int available;
            ErpInboundOrderEntity inboundOrder = (ErpInboundOrderEntity)inboundOrderMap.get(inboundItem.getInboundOrderId());
            if (inboundOrder == null || inboundItem.getProductId() == null || (available = this.defaultInt(inboundItem.getActualQty()) - this.defaultInt((Integer)consumedMap.get(inboundItem.getId()))) <= 0 || (product = (ErpProductEntity)this.erpProductDao.selectById(inboundItem.getProductId())) == null) continue;
            StockCandidate candidate = new StockCandidate();
            candidate.inboundOrder = inboundOrder;
            candidate.inboundItem = inboundItem;
            candidate.product = product;
            candidate.availableBoxes = available;
            ArrayList<StockCandidate> list = (ArrayList<StockCandidate>)result.get(product.getId());
            if (list == null) {
                list = new ArrayList<StockCandidate>();
                result.put(product.getId(), list);
            }
            list.add(candidate);
        }
        Comparator<StockCandidate> comparator = new Comparator<StockCandidate>(){

            @Override
            public int compare(StockCandidate left, StockCandidate right) {
                int expiryCompare = ErpSaleOrderServiceImpl.this.compareDate(left.inboundItem.getExpiryDate(), right.inboundItem.getExpiryDate());
                if (expiryCompare != 0) {
                    return expiryCompare;
                }
                int inboundCompare = ErpSaleOrderServiceImpl.this.compareDate(left.inboundOrder.getCreateTime(), right.inboundOrder.getCreateTime());
                if (inboundCompare != 0) {
                    return inboundCompare;
                }
                return StringUtils.defaultString((String)left.inboundOrder.getContainerNo()).compareToIgnoreCase(StringUtils.defaultString((String)right.inboundOrder.getContainerNo()));
            }
        };
        for (List list : result.values()) {
            Collections.sort(list, comparator);
        }
        return result;
    }

    private void validateSpotRequestItems(List<ErpSaleOrderItemEntity> itemList, boolean requireSalePrice) {
        HashMap<Long, Integer> duplicateCounter = new HashMap<Long, Integer>();
        int lineNo = 1;
        for (ErpSaleOrderItemEntity item : itemList) {
            if (item == null) {
                lineNo++;
                continue;
            }
            if (item.getProductId() == null) {
                throw new RuntimeException("现货单第" + lineNo + "行产品不能为空");
            }
            if (item.getBoxes() == null || item.getBoxes() <= 0) {
                throw new RuntimeException("现货单第" + lineNo + "行箱数必须大于0");
            }
            if (requireSalePrice && (item.getSalePriceKg() == null || item.getSalePriceKg().compareTo(BigDecimal.ZERO) <= 0)) {
                throw new RuntimeException("现货单第" + lineNo + "行销售价（元/千克）必须大于0");
            }
            if (duplicateCounter.containsKey(item.getProductId())) {
                throw new RuntimeException("现货单不支持重复录入同一产品，请合并产品" + StringUtils.defaultString((String)item.getProductCode(), (String)"-") + "的箱数");
            }
            duplicateCounter.put(item.getProductId(), 1);
            lineNo++;
        }
    }

    private Map<Long, ErpSaleOrderItemEntity> buildSpotAllocationInputMap(List<ErpSaleOrderItemEntity> allocationItemList) {
        HashMap<Long, ErpSaleOrderItemEntity> result = new HashMap<Long, ErpSaleOrderItemEntity>();
        if (allocationItemList == null) {
            return result;
        }
        for (ErpSaleOrderItemEntity item : allocationItemList) {
            if (item == null || item.getSourceInboundItemId() == null) continue;
            result.put(item.getSourceInboundItemId(), item);
        }
        return result;
    }

    private void saveItems(ErpSaleOrderEntity order, List<ErpSaleOrderItemEntity> items, Date now) {
        for (ErpSaleOrderItemEntity item : items) {
            item.setSaleOrderId(order.getId());
            item.setCreateTime(now);
            item.setUpdateTime(now);
            this.erpSaleOrderItemDao.insert(item);
            if (!SALE_TYPE_SPOT.equals(order.getSaleType())) continue;
            this.erpStockLedgerDao.insert(this.buildStockLedger(order, item));
        }
    }

    private ErpStockLedgerEntity buildStockLedger(ErpSaleOrderEntity order, ErpSaleOrderItemEntity item) {
        ErpStockLedgerEntity ledger = new ErpStockLedgerEntity();
        ledger.setOrderId(order.getId());
        ledger.setOrderItemId(item.getId());
        ledger.setOrderType("SALE_ORDER");
        ledger.setOrderNo(order.getOrderNo());
        ledger.setRelatedOrderNo(item.getSourceContainerNo());
        ledger.setBizType("SPOT_SALE");
        ledger.setProductId(item.getProductId());
        ledger.setProductCode(item.getProductCode());
        ledger.setProductName(item.getProductName());
        ledger.setProductSpec(item.getProductSpec());
        ledger.setWarehouseId(item.getWarehouseId());
        ledger.setWarehouseName(item.getWarehouseName());
        ledger.setInQuantity(BigDecimal.ZERO);
        ledger.setOutQuantity(this.calcWeight(item));
        ledger.setInPieces(BigDecimal.ZERO);
        ledger.setOutPieces(BigDecimal.valueOf(this.defaultInt(item.getBoxes())));
        ledger.setLossWeight(BigDecimal.ZERO);
        ledger.setUnitPrice(BigDecimal.ZERO);
        ledger.setBizDate(new Date());
        ledger.setExpiryDate(item.getExpiryDate());
        ledger.setCreateUserId(order.getCreateUserId());
        ledger.setCreateTime(new Date());
        return ledger;
    }

    private BigDecimal calcWeight(ErpSaleOrderItemEntity item) {
        if (item.getContractQuantityKg() != null && item.getContractQuantityKg().compareTo(BigDecimal.ZERO) > 0) {
            return item.getContractQuantityKg().setScale(2, RoundingMode.HALF_UP);
        }
        if (item.getSpecWeight() == null || item.getBoxes() == null) {
            return BigDecimal.ZERO;
        }
        return item.getSpecWeight().multiply(BigDecimal.valueOf(item.getBoxes().intValue())).setScale(2, RoundingMode.HALF_UP);
    }

    private void loadChildren(ErpSaleOrderEntity order) {
        List items = this.erpSaleOrderItemDao.selectList((Wrapper)((QueryWrapper)new QueryWrapper().eq((Object)"sale_order_id", (Object)order.getId())).orderByAsc((Object[])new String[]{"line_no", "id"}));
        List files = this.erpSaleOrderFileDao.selectList((Wrapper)((QueryWrapper)new QueryWrapper().eq((Object)"sale_order_id", (Object)order.getId())).orderByAsc((Object[])new String[]{"file_type", "line_no", "id"}));
        order.setAllocationItemList(new ArrayList<ErpSaleOrderItemEntity>());
        if (SALE_TYPE_SPOT.equals(order.getSaleType())) {
            order.setAllocationItemList(items);
            order.setItemList(this.buildSpotRequestItems(items));
        } else {
            order.setItemList(items);
            if (items != null && !items.isEmpty()) {
                ErpSaleOrderItemEntity first = (ErpSaleOrderItemEntity)items.get(0);
                order.setSourcePresaleOrderId(first.getSourcePresaleOrderId());
                order.setSourcePresaleOrderNo(this.firstNonBlank(first.getSourcePresaleOrderNo(), order.getSourcePresaleOrderNo()));
            }
        }
        order.setFileList(files);
    }

    private List<ErpSaleOrderItemEntity> buildSpotRequestItems(List<ErpSaleOrderItemEntity> items) {
        LinkedHashMap<String, ErpSaleOrderItemEntity> grouped = new LinkedHashMap<String, ErpSaleOrderItemEntity>();
        for (ErpSaleOrderItemEntity item : items) {
            String key = this.firstNonBlank(item.getProductCode(), String.valueOf(item.getProductId()));
            ErpSaleOrderItemEntity groupedItem = (ErpSaleOrderItemEntity)grouped.get(key);
            if (groupedItem == null) {
                groupedItem = new ErpSaleOrderItemEntity();
                groupedItem.setProductId(item.getProductId());
                groupedItem.setProductCode(item.getProductCode());
                groupedItem.setProductName(item.getProductName());
                groupedItem.setProductNameEn(item.getProductNameEn());
                groupedItem.setProductSpec(item.getProductSpec());
                groupedItem.setUnit(item.getUnit());
                groupedItem.setBoxes(0);
                groupedItem.setSalePriceKg(item.getSalePriceKg());
                grouped.put(key, groupedItem);
            }
            groupedItem.setBoxes(this.defaultInt(groupedItem.getBoxes()) + this.defaultInt(item.getBoxes()));
        }
        return new ArrayList<ErpSaleOrderItemEntity>(grouped.values());
    }

    private void enrichOrderDisplay(ErpSaleOrderEntity order, boolean detail) {
        List files = detail && order.getFileList() != null ? order.getFileList() : this.erpSaleOrderFileDao.selectList((Wrapper)new QueryWrapper().eq((Object)"sale_order_id", (Object)order.getId()));
        order.setSignedContractUploaded(this.hasFileType(files, FILE_TYPE_SIGNED_CONTRACT) ? 1 : 0);
        order.setBuyerPaymentUploaded(this.hasFileType(files, FILE_TYPE_BUYER_PAYMENT) ? 1 : 0);
        order.setBuyerBankSlipUploaded(this.hasFileType(files, FILE_TYPE_BUYER_BANK) ? 1 : 0);
        order.setFunderPaymentUploaded(this.hasFileType(files, FILE_TYPE_FUNDER_PAYMENT) ? 1 : 0);
        order.setStatus(this.computeStatus(order, files));
        order.setContractUrl(CONTRACT_BASE_URL + order.getContractToken());
        order.setBuyerPortalUrl(PORTAL_BASE_URL + order.getContractToken());
    }

    private int computeStatus(ErpSaleOrderEntity order, List<ErpSaleOrderFileEntity> files) {
        if (this.defaultFlag(order.getSignedContractConfirmed()) == 0) {
            return 1;
        }
        if (this.defaultFlag(order.getBuyerPaymentConfirmed()) == 0) {
            return 2;
        }
        if (this.defaultFlag(order.getBuyerBankConfirmed()) == 0) {
            return 3;
        }
        if (this.defaultFlag(order.getFunderPaymentConfirmed()) == 0) {
            return 4;
        }
        return 5;
    }

    private boolean hasFileType(List<ErpSaleOrderFileEntity> files, String fileType) {
        if (files == null) {
            return false;
        }
        for (ErpSaleOrderFileEntity file : files) {
            if (!fileType.equals(file.getFileType())) continue;
            return true;
        }
        return false;
    }

    private void refreshOrderStatus(Long saleOrderId) {
        ErpSaleOrderEntity order = (ErpSaleOrderEntity)this.getById(saleOrderId);
        if (order == null) {
            return;
        }
        order.setStatus(this.computeStatus(order, null));
        order.setUpdateTime(new Date());
        this.updateById(order);
    }

    private ErpSaleOrderEntity getOrderByToken(String token) {
        if (StringUtils.isBlank((String)token)) {
            throw new RuntimeException("链接令牌不能为空");
        }
        ErpSaleOrderEntity order = (ErpSaleOrderEntity)this.getOne((Wrapper)((QueryWrapper)new QueryWrapper().eq((Object)"contract_token", (Object)token)).last("limit 1"));
        if (order == null) {
            throw new RuntimeException("销售单不存在或链接已失效");
        }
        return order;
    }

    private void ensurePortalOwner(ErpSaleOrderEntity order, Long userId) {
        if (order == null) {
            throw new RuntimeException("销售单不存在");
        }
        if (userId == null || userId <= 0L) {
            throw new RuntimeException("请先登录后再操作");
        }
        SysUserEntity user = (SysUserEntity)this.sysUserService.getById(userId);
        if (user == null) {
            throw new RuntimeException("登录用户不存在");
        }
        if (user.getSecondaryPartnerId() == null) {
            throw new RuntimeException("当前账号未绑定二批主体");
        }
        if (order.getSecondaryPartnerId() == null || !order.getSecondaryPartnerId().equals(user.getSecondaryPartnerId())) {
            throw new RuntimeException("当前账号无权访问该销售单");
        }
    }

    private void validateUploadAccess(ErpSaleOrderEntity order, String fileType, Long userId, boolean portalMode) {
        String normalizedType = StringUtils.upperCase((String)StringUtils.trim((String)fileType));
        if (FILE_TYPE_SIGNED_CONTRACT.equals(normalizedType)) {
            if (portalMode && this.defaultFlag(order.getSignedContractConfirmed()) == 1) {
                throw new RuntimeException("盖章合同已确认，不能重复上传");
            }
            return;
        }
        if (FILE_TYPE_BUYER_PAYMENT.equals(normalizedType)) {
            if (this.defaultFlag(order.getSignedContractConfirmed()) == 0) {
                throw new RuntimeException("请先完成盖章合同确认");
            }
            if (portalMode && this.defaultFlag(order.getBuyerPaymentConfirmed()) == 1) {
                throw new RuntimeException("二批打款凭证已确认，不能重复上传");
            }
            return;
        }
        if (FILE_TYPE_BUYER_BANK.equals(normalizedType)) {
            if (portalMode) {
                throw new RuntimeException("该附件仅支持内部人员上传");
            }
            if (this.defaultFlag(order.getBuyerPaymentConfirmed()) == 0) {
                throw new RuntimeException("请先完成二批打款凭证确认");
            }
            if (this.defaultFlag(order.getBuyerBankConfirmed()) == 1) {
                throw new RuntimeException("二批来款水单已确认，不能重复上传");
            }
            return;
        }
        if (FILE_TYPE_FUNDER_PAYMENT.equals(normalizedType)) {
            if (portalMode) {
                throw new RuntimeException("该附件仅支持内部人员上传");
            }
            if (this.defaultFlag(order.getBuyerBankConfirmed()) == 0) {
                throw new RuntimeException("请先完成二批来款水单确认");
            }
            if (this.defaultFlag(order.getFunderPaymentConfirmed()) == 1) {
                throw new RuntimeException("资方打款凭证已确认，不能重复上传");
            }
            return;
        }
        throw new RuntimeException("不支持的附件类型");
    }

    private void validateDeleteAccess(ErpSaleOrderEntity order, String fileType, boolean portalMode) {
        String normalizedType = StringUtils.upperCase((String)StringUtils.trim((String)fileType));
        if (FILE_TYPE_SIGNED_CONTRACT.equals(normalizedType)) {
            if (this.defaultFlag(order.getSignedContractConfirmed()) == 1) {
                throw new RuntimeException("盖章合同已确认，不能删除");
            }
            return;
        }
        if (FILE_TYPE_BUYER_PAYMENT.equals(normalizedType)) {
            if (this.defaultFlag(order.getBuyerPaymentConfirmed()) == 1) {
                throw new RuntimeException("二批打款凭证已确认，不能删除");
            }
            return;
        }
        if (FILE_TYPE_BUYER_BANK.equals(normalizedType)) {
            if (portalMode) {
                throw new RuntimeException("该附件仅支持内部删除");
            }
            if (this.defaultFlag(order.getBuyerBankConfirmed()) == 1) {
                throw new RuntimeException("二批来款水单已确认，不能删除");
            }
            return;
        }
        if (FILE_TYPE_FUNDER_PAYMENT.equals(normalizedType)) {
            if (portalMode) {
                throw new RuntimeException("该附件仅支持内部删除");
            }
            if (this.defaultFlag(order.getFunderPaymentConfirmed()) == 1) {
                throw new RuntimeException("资方打款凭证已确认，不能删除");
            }
            return;
        }
        throw new RuntimeException("不支持的附件类型");
    }

    private void confirmStep(ErpSaleOrderEntity order, String fileType, Long userId, boolean portalMode) {
        String normalizedType = StringUtils.upperCase((String)StringUtils.trim((String)fileType));
        this.validateConfirmAccess(order, normalizedType, portalMode);
        List files = this.erpSaleOrderFileDao.selectList((Wrapper)((QueryWrapper)new QueryWrapper().eq((Object)"sale_order_id", (Object)order.getId())).eq((Object)"file_type", (Object)normalizedType));
        if (files == null || files.isEmpty()) {
            throw new RuntimeException("请先上传附件后再确认");
        }
        if (FILE_TYPE_SIGNED_CONTRACT.equals(normalizedType)) {
            order.setSignedContractConfirmed(1);
        } else if (FILE_TYPE_BUYER_PAYMENT.equals(normalizedType)) {
            order.setBuyerPaymentConfirmed(1);
        } else if (FILE_TYPE_BUYER_BANK.equals(normalizedType)) {
            order.setBuyerBankConfirmed(1);
        } else if (FILE_TYPE_FUNDER_PAYMENT.equals(normalizedType)) {
            order.setFunderPaymentConfirmed(1);
        } else {
            throw new RuntimeException("不支持的确认节点");
        }
        order.setUpdateTime(new Date());
        order.setStatus(this.computeStatus(order, files));
        this.updateById(order);
    }

    private void validateConfirmAccess(ErpSaleOrderEntity order, String fileType, boolean portalMode) {
        if (FILE_TYPE_SIGNED_CONTRACT.equals(fileType)) {
            if (this.defaultFlag(order.getSignedContractConfirmed()) == 1) {
                throw new RuntimeException("盖章合同已确认");
            }
            return;
        }
        if (FILE_TYPE_BUYER_PAYMENT.equals(fileType)) {
            if (this.defaultFlag(order.getSignedContractConfirmed()) == 0) {
                throw new RuntimeException("请先确认盖章合同");
            }
            if (this.defaultFlag(order.getBuyerPaymentConfirmed()) == 1) {
                throw new RuntimeException("二批打款凭证已确认");
            }
            return;
        }
        if (FILE_TYPE_BUYER_BANK.equals(fileType)) {
            if (portalMode) {
                throw new RuntimeException("该节点仅支持内部确认");
            }
            if (this.defaultFlag(order.getBuyerPaymentConfirmed()) == 0) {
                throw new RuntimeException("请先确认二批打款凭证");
            }
            if (this.defaultFlag(order.getBuyerBankConfirmed()) == 1) {
                throw new RuntimeException("二批来款水单已确认");
            }
            return;
        }
        if (FILE_TYPE_FUNDER_PAYMENT.equals(fileType)) {
            if (portalMode) {
                throw new RuntimeException("该节点仅支持内部确认");
            }
            if (this.defaultFlag(order.getBuyerBankConfirmed()) == 0) {
                throw new RuntimeException("请先确认二批来款水单");
            }
            if (this.defaultFlag(order.getFunderPaymentConfirmed()) == 1) {
                throw new RuntimeException("资方打款凭证已确认");
            }
            return;
        }
        throw new RuntimeException("不支持的确认节点");
    }

    private void resetConfirmFlag(ErpSaleOrderEntity order, String fileType) {
        String normalizedType = StringUtils.upperCase((String)StringUtils.trim((String)fileType));
        boolean changed = false;
        if (FILE_TYPE_SIGNED_CONTRACT.equals(normalizedType) && this.defaultFlag(order.getSignedContractConfirmed()) != 0) {
            order.setSignedContractConfirmed(0);
            changed = true;
        } else if (FILE_TYPE_BUYER_PAYMENT.equals(normalizedType) && this.defaultFlag(order.getBuyerPaymentConfirmed()) != 0) {
            order.setBuyerPaymentConfirmed(0);
            changed = true;
        } else if (FILE_TYPE_BUYER_BANK.equals(normalizedType) && this.defaultFlag(order.getBuyerBankConfirmed()) != 0) {
            order.setBuyerBankConfirmed(0);
            changed = true;
        } else if (FILE_TYPE_FUNDER_PAYMENT.equals(normalizedType) && this.defaultFlag(order.getFunderPaymentConfirmed()) != 0) {
            order.setFunderPaymentConfirmed(0);
            changed = true;
        }
        if (changed) {
            order.setUpdateTime(new Date());
            this.updateById(order);
        }
    }

    private void deleteFileRecord(ErpSaleOrderFileEntity file) {
        this.erpSaleOrderFileDao.deleteById(file.getId());
        if (StringUtils.isBlank((String)file.getFilePath())) {
            return;
        }
        try {
            Files.deleteIfExists(Paths.get(file.getFilePath()));
        }
        catch (Exception ex) {
            logger.warn("delete sale order attachment failed: {}", file.getFilePath(), ex);
        }
    }

    private int nextFileLineNo(Long saleOrderId, String fileType) {
        List files = this.erpSaleOrderFileDao.selectList((Wrapper)((QueryWrapper)((QueryWrapper)((QueryWrapper)new QueryWrapper().eq((Object)"sale_order_id", (Object)saleOrderId)).eq((Object)"file_type", (Object)fileType)).orderByDesc((Object[])new String[]{"line_no", "id"})).last("limit 1"));
        if (files.isEmpty() || ((ErpSaleOrderFileEntity)files.get(0)).getLineNo() == null) {
            return 1;
        }
        return ((ErpSaleOrderFileEntity)files.get(0)).getLineNo() + 1;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private Path saveUploadFile(String fileType, MultipartFile file) throws Exception {
        String suffix = this.getSuffix(file.getOriginalFilename());
        String originalName = StringUtils.defaultString((String)file.getOriginalFilename(), (String)"sale-file").replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">");
        String safeName = originalName.replaceAll("[\\\\/:*?\"<>|\\s]+", "_");
        if (StringUtils.isBlank((String)safeName)) {
            safeName = "sale-file" + suffix;
        }
        if (!safeName.toLowerCase().endsWith(suffix.toLowerCase())) {
            safeName = safeName + suffix;
        }
        Path dir = Paths.get(UPLOAD_BASE_DIR, fileType.toLowerCase(), new SimpleDateFormat("yyyyMMdd").format(new Date()));
        Files.createDirectories(dir, new FileAttribute[0]);
        Path target = dir.resolve(System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + "_" + safeName);
        Path tempFile = Files.createTempFile("erp-sale-order-", suffix, new FileAttribute[0]);
        try {
            file.transferTo(tempFile.toFile());
            Files.copy(tempFile, target, StandardCopyOption.REPLACE_EXISTING);
        }
        finally {
            Files.deleteIfExists(tempFile);
        }
        return target;
    }

    private String generateOrderNo(String saleType) {
        String prefix = SALE_TYPE_SPOT.equals(saleType) ? "XH" : "QH";
        return prefix + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
    }

    private String getSuffix(String filename) {
        if (StringUtils.isBlank((String)filename) || filename.lastIndexOf(".") < 0) {
            return ".tmp";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    private int compareDate(Date left, Date right) {
        if (left == null && right == null) {
            return 0;
        }
        if (left == null) {
            return 1;
        }
        if (right == null) {
            return -1;
        }
        return left.compareTo(right);
    }

    private int defaultInt(Integer value) {
        return value == null ? 0 : value;
    }

    private int defaultFlag(Integer value) {
        return value == null ? 0 : value;
    }

    private String stringValue(Object value) {
        return value == null ? null : value.toString();
    }

    private String firstNonBlank(String ... values) {
        for (String value : values) {
            if (!StringUtils.isNotBlank((String)value)) continue;
            return value;
        }
        return null;
    }

    private String escapeHtml(String value) {
        String text = StringUtils.defaultString((String)value);
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private String formatDate(Date date) {
        if (date == null) {
            return "-";
        }
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }

    private static class StockCandidate {
        private ErpInboundOrderEntity inboundOrder;
        private ErpInboundOrderItemEntity inboundItem;
        private ErpProductEntity product;
        private int availableBoxes;

        private StockCandidate() {
        }
    }
}
