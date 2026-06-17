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
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.renren.common.utils.PageUtils;
import io.renren.common.utils.Query;
import io.renren.modules.erp.dao.ErpInboundOrderDao;
import io.renren.modules.erp.dao.ErpInboundOrderItemDao;
import io.renren.modules.erp.dao.ErpFunderLoanDao;
import io.renren.modules.erp.dao.ErpFunderPaymentAllocationDao;
import io.renren.modules.erp.dao.ErpFunderPaymentDao;
import io.renren.modules.erp.dao.ErpPartnerDao;
import io.renren.modules.erp.dao.ErpPresaleOrderDao;
import io.renren.modules.erp.dao.ErpPresaleOrderItemDao;
import io.renren.modules.erp.dao.ErpPresalePackingDao;
import io.renren.modules.erp.dao.ErpPresalePackingItemDao;
import io.renren.modules.erp.dao.ErpProductDao;
import io.renren.modules.erp.dao.ErpSaleOrderDao;
import io.renren.modules.erp.dao.ErpSaleOrderFileDao;
import io.renren.modules.erp.dao.ErpSaleOrderItemDao;
import io.renren.modules.erp.dao.ErpSaleOutboundBatchDao;
import io.renren.modules.erp.dao.ErpSaleOutboundReceiptDao;
import io.renren.modules.erp.dao.ErpSaleOutboundReceiptItemDao;
import io.renren.modules.erp.dao.ErpSaleOutboundScanDao;
import io.renren.modules.erp.dao.ErpSaleOutboundScanItemDao;
import io.renren.modules.erp.dao.ErpSaleUploadNoticeDao;
import io.renren.modules.erp.dao.ErpStockLedgerDao;
import io.renren.modules.erp.dao.ErpWarehouseDao;
import io.renren.modules.erp.entity.ErpInboundOrderEntity;
import io.renren.modules.erp.entity.ErpInboundOrderItemEntity;
import io.renren.modules.erp.entity.ErpFunderLoanEntity;
import io.renren.modules.erp.entity.ErpFunderPaymentAllocationEntity;
import io.renren.modules.erp.entity.ErpFunderPaymentEntity;
import io.renren.modules.erp.entity.ErpPartnerEntity;
import io.renren.modules.erp.entity.ErpPresaleOrderEntity;
import io.renren.modules.erp.entity.ErpPresaleOrderItemEntity;
import io.renren.modules.erp.entity.ErpPresalePackingEntity;
import io.renren.modules.erp.entity.ErpPresalePackingItemEntity;
import io.renren.modules.erp.entity.ErpProductEntity;
import io.renren.modules.erp.entity.ErpSaleOrderEntity;
import io.renren.modules.erp.entity.ErpSaleOrderFileEntity;
import io.renren.modules.erp.entity.ErpSaleOrderItemEntity;
import io.renren.modules.erp.entity.ErpSaleOutboundBatchEntity;
import io.renren.modules.erp.entity.ErpSaleOutboundReceiptEntity;
import io.renren.modules.erp.entity.ErpSaleOutboundReceiptItemEntity;
import io.renren.modules.erp.entity.ErpSaleOutboundScanEntity;
import io.renren.modules.erp.entity.ErpSaleOutboundScanItemEntity;
import io.renren.modules.erp.entity.ErpSaleUploadNoticeEntity;
import io.renren.modules.erp.entity.ErpStockLedgerEntity;
import io.renren.modules.erp.entity.ErpWarehouseEntity;
import io.renren.modules.erp.service.ErpSaleOrderService;
import io.renren.modules.erp.service.ErpExpenseService;
import io.renren.modules.erp.service.ErpOcrService;
import io.renren.modules.erp.service.ErpWecomService;
import io.renren.modules.erp.vo.ErpRecognizeResultVo;
import io.renren.modules.erp.vo.ErpSalePresaleItemVo;
import io.renren.modules.erp.vo.ErpSalePresaleOrderVo;
import io.renren.modules.sys.entity.SysUserEntity;
import io.renren.modules.sys.service.SysUserService;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
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
    private static final String FILE_TYPE_OUTBOUND_RECEIPT = "OUTBOUND_RECEIPT";
    private static final String FILE_TYPE_OUTBOUND_ATTACHMENT = "OUTBOUND_ATTACHMENT";
    private static final String FILE_TYPE_OUTBOUND_BATCH_BANK = "OUTBOUND_BATCH_BANK_SLIP";
    private static final int OUTBOUND_BATCH_PENDING_RECEIPT = 0;
    private static final int OUTBOUND_BATCH_PENDING_BANK = 1;
    private static final int OUTBOUND_BATCH_PENDING_CONFIRM = 2;
    private static final int OUTBOUND_BATCH_CONFIRMED = 3;
    private static final int OUTBOUND_BATCH_VOID = 9;
    private static final String CONTRACT_BASE_URL = "http://218.202.240.118:8888/renren-fast/erp/saleorder/contract/";
    private static final String PORTAL_BASE_URL = "http://218.202.240.118:3001/#/sale-upload/";
    private static final String UPLOAD_BASE_DIR = "D:\\renren-fast-vue\\renren-fast\\uploads\\saleorder";
    private static final String CONTRACT_TEMPLATE_PATH = "D:\\renren-fast-vue\\renren-fast\\src\\main\\resources\\templates\\sale-contract-template.xlsx";
    private static final String CONTRACT_RENDER_SCRIPT_PATH = "D:\\renren-fast-vue\\renren-fast\\scripts\\render-sale-contract.ps1";
    private static final String CONTRACT_STAMP_PATH = "D:\\renren-fast-vue\\renren-fast\\src\\main\\resources\\templates\\sale-contract-party-a-stamp.png";
    @Autowired
    private ErpSaleOrderItemDao erpSaleOrderItemDao;
    @Autowired
    private ErpSaleOrderFileDao erpSaleOrderFileDao;
    @Autowired
    private ErpSaleOutboundBatchDao erpSaleOutboundBatchDao;
    @Autowired
    private ErpSaleOutboundReceiptDao erpSaleOutboundReceiptDao;
    @Autowired
    private ErpSaleOutboundReceiptItemDao erpSaleOutboundReceiptItemDao;
    @Autowired
    private ErpSaleOutboundScanDao erpSaleOutboundScanDao;
    @Autowired
    private ErpSaleOutboundScanItemDao erpSaleOutboundScanItemDao;
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
    private ErpPresalePackingDao erpPresalePackingDao;
    @Autowired
    private ErpPresalePackingItemDao erpPresalePackingItemDao;
    @Autowired
    private ErpInboundOrderDao erpInboundOrderDao;
    @Autowired
    private ErpInboundOrderItemDao erpInboundOrderItemDao;
    @Autowired
    private ErpStockLedgerDao erpStockLedgerDao;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private ErpWecomService erpWecomService;
    @Autowired
    private ErpSaleUploadNoticeDao erpSaleUploadNoticeDao;
    @Autowired
    private ErpOcrService erpOcrService;
    @Autowired
    private ErpExpenseService erpExpenseService;
    @Autowired
    private ErpFunderPaymentAllocationDao erpFunderPaymentAllocationDao;
    @Autowired
    private ErpFunderPaymentDao erpFunderPaymentDao;
    @Autowired
    private ErpFunderLoanDao erpFunderLoanDao;

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
    public void updatePresaleLink(Long saleOrderId, Long presaleOrderId, Long userId) {
        if (saleOrderId == null) {
            throw new RuntimeException("请先选择销售单");
        }
        ErpSaleOrderEntity order = (ErpSaleOrderEntity)this.getById(saleOrderId);
        if (order == null) {
            throw new RuntimeException("销售单不存在");
        }
        if (!SALE_TYPE_FUTURES.equals(order.getSaleType())) {
            throw new RuntimeException("只有期货单可以关联预销售单");
        }
        if (this.defaultFlag(order.getPresaleLinkConfirmed()) == 1) {
            throw new RuntimeException("关联预销售单已确认，不能修改");
        }
        if (presaleOrderId == null) {
            throw new RuntimeException("请选择关联预销售单");
        }
        ErpPresaleOrderEntity presaleOrder = null;
        Map<Long, ErpPresaleOrderItemEntity> presaleItemMap = new HashMap<Long, ErpPresaleOrderItemEntity>();
        if (presaleOrderId != null) {
            presaleOrder = (ErpPresaleOrderEntity)this.erpPresaleOrderDao.selectById(presaleOrderId);
            if (presaleOrder == null) {
                throw new RuntimeException("关联预销售单不存在");
            }
            List<ErpPresaleOrderItemEntity> presaleItems = this.erpPresaleOrderItemDao.selectList((Wrapper)((QueryWrapper)new QueryWrapper().eq((Object)"presale_order_id", (Object)presaleOrder.getId())).orderByAsc((Object[])new String[]{"line_no", "id"}));
            for (ErpPresaleOrderItemEntity presaleItem : presaleItems) {
                if (presaleItem.getProductId() == null || presaleItemMap.containsKey(presaleItem.getProductId())) continue;
                presaleItemMap.put(presaleItem.getProductId(), presaleItem);
            }
        }
        List<ErpSaleOrderItemEntity> items = this.erpSaleOrderItemDao.selectList((Wrapper)((QueryWrapper)new QueryWrapper().eq((Object)"sale_order_id", (Object)saleOrderId)).orderByAsc((Object[])new String[]{"line_no", "id"}));
        String presaleOrderNo = presaleOrder == null ? null : this.firstNonBlank(presaleOrder.getSellerContractNo(), presaleOrder.getOrderNo());
        for (ErpSaleOrderItemEntity item : items) {
            ErpPresaleOrderItemEntity presaleItem = presaleOrder == null ? null : (ErpPresaleOrderItemEntity)presaleItemMap.get(item.getProductId());
            if (presaleOrder != null && presaleItem == null) {
                throw new RuntimeException("产品" + StringUtils.defaultString((String)item.getProductCode(), (String)"-") + "未在关联预销售单中找到");
            }
            item.setSourcePresaleOrderId(presaleOrder == null ? null : presaleOrder.getId());
            item.setSourcePresaleOrderNo(presaleOrderNo);
            item.setSourcePresaleOrderItemId(presaleItem == null ? null : presaleItem.getId());
            item.setUpdateTime(new Date());
            this.erpSaleOrderItemDao.updateById(item);
        }
        order.setSourcePresaleOrderId(presaleOrder == null ? null : presaleOrder.getId());
        order.setSourcePresaleOrderNo(presaleOrderNo);
        order.setUpdateTime(new Date());
        this.updateById(order);
    }

    @Override
    @Transactional(rollbackFor={Exception.class})
    public void confirmPresaleLink(Long saleOrderId, Long userId) {
        if (saleOrderId == null) {
            throw new RuntimeException("请先选择销售单");
        }
        ErpSaleOrderEntity order = (ErpSaleOrderEntity)this.getById(saleOrderId);
        if (order == null) {
            throw new RuntimeException("销售单不存在");
        }
        if (!SALE_TYPE_FUTURES.equals(order.getSaleType())) {
            throw new RuntimeException("只有期货单可以确认预销售单关联");
        }
        if (this.defaultFlag(order.getPresaleLinkConfirmed()) == 1) {
            throw new RuntimeException("关联预销售单已确认");
        }
        if (order.getSourcePresaleOrderId() == null) {
            this.loadChildren(order);
        }
        if (order.getSourcePresaleOrderId() == null) {
            throw new RuntimeException("请先选择关联预销售单");
        }
        order.setPresaleLinkConfirmed(1);
        order.setUpdateTime(new Date());
        this.updateById(order);
        this.autoSendShipNoticeForConfirmedPresaleLink(order, userId);
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

    @Override
    @Transactional(rollbackFor={Exception.class})
    public ErpSaleOutboundReceiptEntity recognizeOutboundReceipt(Long saleOrderId, MultipartFile[] files, Long userId) throws Exception {
        ErpSaleOutboundBatchEntity batch = this.ensureOpenOutboundBatch(saleOrderId, userId);
        return this.recognizeOutboundReceipt(saleOrderId, batch.getId(), files, userId);
    }

    @Override
    @Transactional(rollbackFor={Exception.class})
    public ErpSaleOutboundReceiptEntity recognizeOutboundReceipt(Long saleOrderId, Long batchId, MultipartFile[] files, Long userId) throws Exception {
        if (files == null || files.length == 0) {
            throw new RuntimeException("请先选择出库回单文件");
        }
        ErpSaleOutboundBatchEntity batch = this.requireEditableOutboundBatch(saleOrderId, batchId);
        int previousMaxLineNo = this.nextFileLineNo(saleOrderId, FILE_TYPE_OUTBOUND_RECEIPT, batch.getId()) - 1;
        List<ErpSaleOrderFileEntity> allFiles = this.doUploadFiles(saleOrderId, FILE_TYPE_OUTBOUND_RECEIPT, files, userId, false, batch.getId());
        List<ErpSaleOrderFileEntity> currentFiles = new ArrayList<ErpSaleOrderFileEntity>();
        for (ErpSaleOrderFileEntity file : allFiles) {
            if (file != null && this.defaultInt(file.getLineNo()) > previousMaxLineNo) {
                currentFiles.add(file);
            }
        }
        if (this.loadOutboundScanByBatch(batch.getId()) != null) {
            this.refreshOutboundBatchStatus(batch.getId());
            return this.loadOutboundReceiptByBatch(batch.getId());
        }
        JSONObject recognized = this.runOutboundReceiptOcr(currentFiles);
        ErpSaleOutboundReceiptEntity receipt = this.buildOutboundReceiptFromOcr(saleOrderId, recognized);
        receipt.setBatchId(batch.getId());
        ErpSaleOutboundReceiptEntity saved = this.appendOutboundReceiptRecognition(receipt, userId);
        this.refreshOutboundBatchStatus(batch.getId());
        return saved;
    }

    @Override
    @Transactional(rollbackFor={Exception.class})
    public ErpSaleOutboundReceiptEntity saveOutboundReceipt(ErpSaleOutboundReceiptEntity receipt, Long userId) {
        if (receipt == null || receipt.getSaleOrderId() == null) {
            throw new RuntimeException("出库回单数据不能为空");
        }
        ErpSaleOrderEntity order = (ErpSaleOrderEntity)this.getById(receipt.getSaleOrderId());
        if (order == null) {
            throw new RuntimeException("销售单不存在");
        }
        if (this.defaultFlag(order.getOutboundReceiptConfirmed()) == 1) {
            throw new RuntimeException("出库回单已确认，不能修改");
        }
        if (receipt.getBatchId() != null) {
            this.requireEditableOutboundBatch(receipt.getSaleOrderId(), receipt.getBatchId());
        }
        this.validateOutboundReceiptRequiredItems(receipt);
        ErpSaleOutboundReceiptEntity saved = this.upsertOutboundReceipt(receipt, userId);
        if (receipt.getBatchId() != null) {
            this.refreshOutboundBatchStatus(receipt.getBatchId());
        }
        return saved;
    }

    @Override
    public List<ErpSaleOutboundBatchEntity> queryOutboundBatches(Long saleOrderId) {
        if (saleOrderId == null || saleOrderId <= 0) {
            return new ArrayList<ErpSaleOutboundBatchEntity>();
        }
        return this.loadOutboundBatches(saleOrderId);
    }

    @Override
    @Transactional(rollbackFor={Exception.class})
    public ErpSaleOutboundBatchEntity createOutboundBatch(Long saleOrderId, Long userId) {
        ErpSaleOrderEntity order = (ErpSaleOrderEntity)this.getById(saleOrderId);
        if (order == null) {
            throw new RuntimeException("销售单不存在");
        }
        if (this.defaultFlag(order.getOutboundReceiptConfirmed()) == 1) {
            throw new RuntimeException("销售单出库流程已完成，不能新增出库批次");
        }
        ErpSaleOutboundBatchEntity openBatch = this.findOpenOutboundBatch(saleOrderId);
        if (openBatch != null) {
            throw new RuntimeException("存在未完成的出库批次，请先确认完成或删除后再新增");
        }
        Date now = new Date();
        ErpSaleOutboundBatchEntity batch = new ErpSaleOutboundBatchEntity();
        batch.setSaleOrderId(saleOrderId);
        batch.setBatchNo(this.nextOutboundBatchNo(saleOrderId));
        batch.setStatus(OUTBOUND_BATCH_PENDING_RECEIPT);
        batch.setReceiptCount(0);
        batch.setShippedTotalBoxes(0);
        batch.setShippedTotalWeight(BigDecimal.ZERO);
        batch.setCreateUserId(userId);
        batch.setCreateTime(now);
        batch.setUpdateTime(now);
        this.erpSaleOutboundBatchDao.insert(batch);
        return this.loadOutboundBatch(batch.getId());
    }

    @Override
    @Transactional(rollbackFor={Exception.class})
    public ErpSaleOutboundBatchEntity uploadOutboundBatchBankSlip(Long saleOrderId, Long batchId, MultipartFile[] files, Long userId) throws Exception {
        if (files == null || files.length != 1) {
            throw new RuntimeException("每个出库批次只能上传一张二批来款水单");
        }
        byte[] fileBytes = files[0].getBytes();
        MultipartFile archiveFile = new ByteArrayMultipartFile(files[0], fileBytes);
        MultipartFile ocrFile = new ByteArrayMultipartFile(files[0], fileBytes);
        ErpSaleOutboundBatchEntity batch = this.requireEditableOutboundBatch(saleOrderId, batchId);
        if (batch.getBankSlipFileId() != null) {
            ErpSaleOrderFileEntity oldFile = (ErpSaleOrderFileEntity)this.erpSaleOrderFileDao.selectById(batch.getBankSlipFileId());
            if (oldFile != null) {
                this.deleteFileRecord(oldFile);
            }
            batch.setBankSlipFileId(null);
            this.clearOutboundBatchBankSlip(batch);
        }
        this.fillOutboundBatchBankSlipRecognition(batch, ocrFile);
        List<ErpSaleOrderFileEntity> savedFiles = this.doUploadFiles(saleOrderId, FILE_TYPE_OUTBOUND_BATCH_BANK, new MultipartFile[]{archiveFile}, userId, false, batchId);
        ErpSaleOrderFileEntity latest = savedFiles == null || savedFiles.isEmpty() ? null : savedFiles.get(savedFiles.size() - 1);
        if (latest == null) {
            throw new RuntimeException("二批来款水单上传失败");
        }
        batch.setBankSlipFileId(latest.getId());
        this.refreshOutboundBatchBankAmountDiff(batch);
        batch.setUpdateTime(new Date());
        this.erpSaleOutboundBatchDao.updateById(batch);
        return this.refreshOutboundBatchStatus(batchId);
    }

    @Override
    @Transactional(rollbackFor={Exception.class})
    public ErpSaleOutboundBatchEntity saveOutboundBatchBankSlip(ErpSaleOutboundBatchEntity request, Long userId) {
        ErpSaleOutboundBatchEntity batch = this.requireEditableOutboundBatch(request == null ? null : request.getSaleOrderId(), request == null ? null : request.getId());
        if (batch.getBankSlipFileId() == null) {
            throw new RuntimeException("请先上传二批来款水单");
        }
        batch.setBankPayerNameModified(StringUtils.trimToNull(request.getBankPayerNameModified()));
        batch.setBankPayeeNameModified(StringUtils.trimToNull(request.getBankPayeeNameModified()));
        batch.setBankAmountModified(this.defaultDecimal(request.getBankAmountModified()).setScale(2, RoundingMode.HALF_UP));
        batch.setBankPaymentDateModified(request.getBankPaymentDateModified());
        batch.setBankSerialNoModified(StringUtils.trimToNull(request.getBankSerialNoModified()));
        this.refreshOutboundBatchBankAmountDiff(batch);
        batch.setUpdateTime(new Date());
        this.erpSaleOutboundBatchDao.updateById(batch);
        return this.loadOutboundBatch(batch.getId());
    }

    @Override
    @Transactional(rollbackFor={Exception.class})
    public ErpSaleOutboundBatchEntity bindOutboundBatchScanLink(Long saleOrderId, Long batchId, String scanUrl, Long userId) throws Exception {
        ErpSaleOutboundBatchEntity batch = this.requireEditableOutboundBatch(saleOrderId, batchId);
        if (StringUtils.isBlank((String)scanUrl)) {
            throw new RuntimeException("请先填写扫码链接");
        }
        Map<String, String> query = this.parseQueryParams(scanUrl);
        String orderNum = query.get("orderNum");
        String imei = query.get("imei");
        if (StringUtils.isBlank((String)orderNum) || StringUtils.isBlank((String)imei)) {
            throw new RuntimeException("扫码链接缺少orderNum或imei参数");
        }
        JSONObject response = this.fetchShunshiOrder(orderNum, imei);
        if (response == null || response.getIntValue("code") != 200 || response.getJSONObject("data") == null) {
            throw new RuntimeException(response == null ? "顺势云数据抓取失败" : this.firstNonBlank(response.getString("msg"), "顺势云数据抓取失败"));
        }
        JSONObject data = response.getJSONObject("data");
        Date now = new Date();
        this.deleteOutboundScanData(batchId);
        ErpSaleOutboundScanEntity scan = new ErpSaleOutboundScanEntity();
        scan.setSaleOrderId(saleOrderId);
        scan.setBatchId(batchId);
        scan.setScanUrl(scanUrl);
        scan.setOrderNum(this.firstNonBlank(data.getString("orderNum"), orderNum));
        scan.setImei(imei);
        scan.setScanOrderTime(this.parseDateTime(data.getString("orderTime")));
        scan.setCustomerName(StringUtils.trimToEmpty((String)data.getString("orderCustomerName")));
        scan.setTotalBoxes(this.parseInteger(data.getString("orderTotalNum")));
        scan.setTotalWeight(this.parseDecimal(data.getString("orderTotalWeight")));
        scan.setRawJson(response.toJSONString());
        scan.setCreateTime(now);
        scan.setUpdateTime(now);
        this.erpSaleOutboundScanDao.insert(scan);

        ErpSaleOutboundReceiptEntity receipt = new ErpSaleOutboundReceiptEntity();
        receipt.setSaleOrderId(saleOrderId);
        receipt.setBatchId(batchId);
        receipt.setOutboundOrderNo(scan.getOrderNum());
        receipt.setCustomerName(scan.getCustomerName());
        receipt.setRawText(response.toJSONString());
        List<ErpSaleOutboundReceiptItemEntity> receiptItems = new ArrayList<ErpSaleOutboundReceiptItemEntity>();
        JSONArray itemArray = data.getJSONArray("commodityVoList");
        if (itemArray != null) {
            for (int i = 0; i < itemArray.size(); i++) {
                JSONObject itemJson = itemArray.getJSONObject(i);
                ErpSaleOutboundScanItemEntity scanItem = this.buildOutboundScanItem(scan, itemJson, i + 1, now);
                this.erpSaleOutboundScanItemDao.insert(scanItem);
                receiptItems.add(this.buildOutboundReceiptItemFromScan(scan, scanItem));
            }
        }
        if (receiptItems.isEmpty()) {
            throw new RuntimeException("顺势云未返回产品明细，请核对链接");
        }
        receipt.setItemList(receiptItems);
        ErpSaleOutboundReceiptEntity saved = this.upsertOutboundReceipt(receipt, userId);
        this.refreshOutboundBatchStatus(batch.getId());
        return this.loadOutboundBatch(batch.getId());
    }

    @Override
    @Transactional(rollbackFor={Exception.class})
    public ErpSaleOutboundBatchEntity confirmOutboundBatch(Long batchId, Long userId) {
        ErpSaleOutboundBatchEntity batch = this.requireEditableOutboundBatch(null, batchId);
        ErpSaleOutboundReceiptEntity receipt = this.loadOutboundReceiptByBatch(batchId);
        if (receipt == null || receipt.getItemList() == null || receipt.getItemList().isEmpty()) {
            throw new RuntimeException("请先上传并保存出库回单后再确认批次");
        }
        if (!this.hasOutboundReceiptFile(batch.getSaleOrderId(), batchId)) {
            throw new RuntimeException("请先上传出库回单原件");
        }
        this.validateOutboundReceiptRequiredItems(receipt);
        if (batch.getBankSlipFileId() == null) {
            throw new RuntimeException("请先上传二批来款水单后再确认批次");
        }
        Date now = new Date();
        batch.setStatus(OUTBOUND_BATCH_CONFIRMED);
        batch.setConfirmUserId(userId);
        batch.setConfirmTime(now);
        batch.setUpdateTime(now);
        this.erpSaleOutboundBatchDao.updateById(batch);
        this.erpExpenseService.regenerateOutboundStorageExpense(batch.getId(), userId);
        this.refreshOutboundReceiptOrderConfirmed(batch.getSaleOrderId());
        this.refreshOrderStatus(batch.getSaleOrderId());
        return this.loadOutboundBatch(batchId);
    }

    @Override
    @Transactional(rollbackFor={Exception.class})
    public void voidOutboundBatch(Long batchId, Long userId) {
        ErpSaleOutboundBatchEntity batch = (ErpSaleOutboundBatchEntity)this.erpSaleOutboundBatchDao.selectById(batchId);
        if (batch == null) {
            throw new RuntimeException("出库批次不存在");
        }
        this.deleteOutboundBatchData(batch);
        this.refreshOutboundReceiptOrderConfirmed(batch.getSaleOrderId());
        this.refreshOrderStatus(batch.getSaleOrderId());
    }

    private List<ErpSaleOrderFileEntity> doUploadFiles(Long saleOrderId, String fileType, MultipartFile[] files, Long userId, boolean portalMode) throws Exception {
        return this.doUploadFiles(saleOrderId, fileType, files, userId, portalMode, null);
    }

    private List<ErpSaleOrderFileEntity> doUploadFiles(Long saleOrderId, String fileType, MultipartFile[] files, Long userId, boolean portalMode, Long batchId) throws Exception {
        ErpSaleOrderEntity order = (ErpSaleOrderEntity)this.getById(saleOrderId);
        if (order == null) {
            throw new RuntimeException("销售单不存在");
        }
        this.validateUploadAccess(order, fileType, userId, portalMode);
        int lineNo = this.nextFileLineNo(saleOrderId, fileType, batchId);
        Date now = new Date();
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) continue;
            Path savedPath = this.saveUploadFile(fileType, file);
            ErpSaleOrderFileEntity entity = new ErpSaleOrderFileEntity();
            entity.setSaleOrderId(saleOrderId);
            entity.setBatchId(batchId);
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
        QueryWrapper<ErpSaleOrderFileEntity> wrapper = (QueryWrapper<ErpSaleOrderFileEntity>)((QueryWrapper)((QueryWrapper)new QueryWrapper().eq((Object)"sale_order_id", (Object)saleOrderId)).eq((Object)"file_type", (Object)fileType));
        if (batchId == null) {
            wrapper.isNull("batch_id");
        } else {
            wrapper.eq("batch_id", batchId);
        }
        return this.erpSaleOrderFileDao.selectList((Wrapper)wrapper.orderByAsc("line_no", "id"));
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
        this.validateBatchFileEditable(file);
        this.deleteFileRecord(file);
        if (file.getBatchId() != null) {
            ErpSaleOutboundBatchEntity batch = (ErpSaleOutboundBatchEntity)this.erpSaleOutboundBatchDao.selectById(file.getBatchId());
            if (batch != null && file.getId() != null && file.getId().equals(batch.getBankSlipFileId())) {
                batch.setBankSlipFileId(null);
                this.clearOutboundBatchBankSlip(batch);
                batch.setUpdateTime(new Date());
                this.erpSaleOutboundBatchDao.updateById(batch);
            }
            this.refreshOutboundBatchStatus(file.getBatchId());
        }
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
        this.validateBatchFileEditable(file);
        this.deleteFileRecord(file);
        if (file.getBatchId() != null) {
            this.refreshOutboundBatchStatus(file.getBatchId());
        }
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
        html.append("<html><head><meta charset='UTF-8'><title>销售合同</title>").append("<style>body{font-family:Arial,'Microsoft YaHei';padding:24px;color:#222;}table{width:100%;min-width:1320px;border-collapse:collapse;margin-top:16px;}th,td{border:1px solid #dcdfe6;padding:8px;font-size:14px;}th{background:#f5f7fa;}h2{margin:0 0 12px;} .meta{line-height:28px;}.table-wrap{overflow-x:auto;}.num{text-align:right;white-space:nowrap;}</style>").append("</head><body>");
        html.append("<h2>销售合同</h2>");
        html.append("<div class='meta'>合同号：").append(this.escapeHtml(order.getContractNo())).append("</div>");
        html.append("<div class='meta'>销售单号：").append(this.escapeHtml(order.getOrderNo())).append("</div>");
        html.append("<div class='meta'>类型：").append(SALE_TYPE_SPOT.equals(order.getSaleType()) ? "现货单" : "期货单").append("</div>");
        html.append("<div class='meta'>二批商：").append(this.escapeHtml(order.getSecondaryPartnerName())).append("</div>");
        html.append("<div class='meta'>仓库：").append(this.escapeHtml(StringUtils.defaultString((String)order.getWarehouseName(), (String)"-"))).append("</div>");
        html.append("<div class='table-wrap'><table><thead><tr><th>序号</th><th>合同号</th><th>产品编码</th><th>中文名称</th><th>英文名称</th><th>柜号</th><th>厂号</th><th>箱数</th><th>数量/千克</th><th>销售价（元/千克）</th><th>货款金额</th><th>税款金额</th><th>金额（元）</th></tr></thead><tbody>");
        int index = 1;
        for (ErpSaleOrderItemEntity item : displayItems) {
            BigDecimal quantityKg = this.defaultDecimal(item.getContractQuantityKg());
            BigDecimal salePriceKg = this.defaultDecimal(item.getSalePriceKg());
            BigDecimal totalAmount = quantityKg.multiply(salePriceKg).setScale(2, RoundingMode.HALF_UP);
            BigDecimal goodsAmount = totalAmount.divide(new BigDecimal("1.09"), 2, RoundingMode.HALF_UP);
            BigDecimal taxAmount = goodsAmount.multiply(new BigDecimal("0.09")).setScale(2, RoundingMode.HALF_UP);
            html.append("<tr>").append("<td>").append(index++).append("</td>").append("<td>").append(this.escapeHtml(this.firstNonBlank(order.getContractNo(), order.getOrderNo(), "-"))).append("</td>").append("<td>").append(this.escapeHtml(item.getProductCode())).append("</td>").append("<td>").append(this.escapeHtml(item.getProductName())).append("</td>").append("<td>").append(this.escapeHtml(item.getProductNameEn())).append("</td>").append("<td>").append(this.escapeHtml(this.firstNonBlank(item.getSourceContainerNo(), "-"))).append("</td>").append("<td>").append(this.escapeHtml(this.firstNonBlank(item.getContractFactoryNo(), "-"))).append("</td>").append("<td class='num'>").append(this.defaultInt(item.getBoxes())).append("</td>").append("<td class='num'>").append(this.formatDecimal(quantityKg)).append("</td>").append("<td class='num'>").append(this.formatDecimal(salePriceKg)).append("</td>").append("<td class='num'>").append(this.formatDecimal(goodsAmount)).append("</td>").append("<td class='num'>").append(this.formatDecimal(taxAmount)).append("</td>").append("<td class='num'>").append(this.formatDecimal(totalAmount)).append("</td>").append("</tr>");
        }
        html.append("</tbody></table></div>");
        html.append("<div style='margin-top:24px;'>请先下载PDF合同并盖章，回传后由内部继续处理出库批次和来款水单。</div>");
        html.append("<div style='margin-top:16px;display:flex;gap:12px;flex-wrap:wrap;'>");
        html.append("<a href='").append(CONTRACT_BASE_URL).append("pdf/").append(this.escapeHtml(order.getContractToken())).append("' style='display:inline-block;padding:10px 16px;background:#0B1457;color:#fff;text-decoration:none;border-radius:4px;'>下载PDF合同</a>");
        html.append("<a href='").append(PORTAL_BASE_URL).append(this.escapeHtml(order.getContractToken())).append("' style='display:inline-block;padding:10px 16px;background:#0B1457;color:#fff;text-decoration:none;border-radius:4px;'>登录上传盖章合同</a>");
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
            payload.put("coldStorageFreeDays", this.resolveColdStorageFreeDays(order));
            payload.put("rows", rows);
            Files.write(payloadPath, JSON.toJSONString(payload, SerializerFeature.WriteMapNullValue).getBytes(StandardCharsets.UTF_8));
            this.runContractRenderScript(templateFile, renderScriptFile, payloadPath.toFile(), outputXlsx.toFile(), outputPdf.toFile(), new File(CONTRACT_STAMP_PATH));
            if (!Files.exists(outputXlsx) || !Files.exists(outputPdf)) {
                throw new RuntimeException("合同文件未生成成功");
            }
            return outputPdf;
        }
        finally {
            Files.deleteIfExists(payloadPath);
        }
    }

    private void runContractRenderScript(File templateFile, File renderScriptFile, File payloadFile, File outputXlsx, File outputPdf, File stampFile) throws Exception {
        ProcessBuilder builder = new ProcessBuilder(new String[]{"powershell", "-ExecutionPolicy", "Bypass", "-File", renderScriptFile.getAbsolutePath(), "-TemplatePath", templateFile.getAbsolutePath(), "-PayloadPath", payloadFile.getAbsolutePath(), "-OutputXlsx", outputXlsx.getAbsolutePath(), "-OutputPdf", outputPdf.getAbsolutePath(), "-StampPath", stampFile.getAbsolutePath()});
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
            row.put("productCode", this.firstNonBlank(item.getProductCode(), ""));
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

    private int resolveColdStorageFreeDays(ErpSaleOrderEntity order) {
        if (order == null || order.getSecondaryPartnerId() == null) {
            return 7;
        }
        ErpPartnerEntity partner = (ErpPartnerEntity)this.erpPartnerDao.selectById(order.getSecondaryPartnerId());
        if (partner == null || partner.getColdStorageFreeDays() == null || partner.getColdStorageFreeDays() <= 0) {
            return 7;
        }
        return partner.getColdStorageFreeDays();
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
        if (create && SALE_TYPE_FUTURES.equals(order.getSaleType()) && order.getSourcePresaleOrderId() != null) {
            order.setPresaleLinkConfirmed(1);
        }
        if (create) {
            this.save(order);
        } else {
            this.updateById(order);
            this.erpSaleOrderItemDao.delete((Wrapper)new QueryWrapper().eq((Object)"sale_order_id", (Object)order.getId()));
            this.erpStockLedgerDao.delete((Wrapper)((QueryWrapper)new QueryWrapper().eq((Object)"order_id", (Object)order.getId())).eq((Object)"order_type", (Object)"SALE_ORDER"));
        }
        this.saveItems(order, preparedItems, now);
        this.refreshOrderStatus(order.getId());
        if (create) {
            this.erpWecomService.autoSendSaleUploadNotice(order.getId(), userId);
        }
        this.autoSendShipNoticeForConfirmedPresaleLink(order, userId);
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
            order.setPresaleLinkConfirmed(0);
            order.setOutboundReceiptConfirmed(0);
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
            order.setPresaleLinkConfirmed(this.defaultFlag(existing.getPresaleLinkConfirmed()));
            order.setOutboundReceiptConfirmed(this.defaultFlag(existing.getOutboundReceiptConfirmed()));
        }
        order.setUpdateTime(now);
    }

    private void autoSendShipNoticeForConfirmedPresaleLink(ErpSaleOrderEntity order, Long userId) {
        if (order == null || !SALE_TYPE_FUTURES.equals(order.getSaleType())) {
            return;
        }
        if (this.defaultFlag(order.getPresaleLinkConfirmed()) != 1 || order.getSourcePresaleOrderId() == null) {
            return;
        }
        try {
            this.erpWecomService.autoSendShipNoticeToLinkedFutures(order.getSourcePresaleOrderId(), userId);
        } catch (Exception ignored) {
            // 船期自动通知失败不影响销售单保存或预售关联确认，用户仍可在预销售单列表手动重发。
        }
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
        order.setSourcePresaleOrderId(presaleOrder == null ? null : presaleOrder.getId());
        order.setSourcePresaleOrderNo(presaleOrder == null ? null : this.firstNonBlank(presaleOrder.getSellerContractNo(), presaleOrder.getOrderNo()));
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
                allocated.setOwnershipName(candidate.ownershipName);
                allocated.setInboundDate(candidate.inboundOrder.getCreateTime());
                allocated.setProductionDate(candidate.inboundItem.getProductionDate());
                allocated.setExpiryDate(candidate.inboundItem.getExpiryDate());
                allocated.setSpecWeight(candidate.inboundItem.getSpecWeight());
                allocated.setSalePriceKg(requestItem.getSalePriceKg().setScale(2, RoundingMode.HALF_UP));
                String defaultFactoryNo = this.findFactoryNoForInboundItem(candidate.inboundOrder, candidate.inboundItem);
                String defaultPortCold = StringUtils.trimToEmpty((String)candidate.inboundOrder.getWarehouseName());
                ErpSaleOrderItemEntity inputAllocation = (ErpSaleOrderItemEntity)inputAllocationMap.get(candidate.inboundItem.getId());
                if (inputAllocation != null) {
                    allocated.setContractQuantityKg(inputAllocation.getContractQuantityKg());
                    allocated.setContractFactoryNo(this.firstNonBlank(inputAllocation.getContractFactoryNo(), defaultFactoryNo));
                    allocated.setContractPortCold(this.firstNonBlank(inputAllocation.getContractPortCold(), defaultPortCold));
                } else {
                    allocated.setContractFactoryNo(defaultFactoryNo);
                    allocated.setContractPortCold(defaultPortCold);
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

    private String findFactoryNoForInboundItem(ErpInboundOrderEntity inboundOrder, ErpInboundOrderItemEntity inboundItem) {
        if (inboundOrder == null || inboundItem == null || inboundOrder.getPresaleOrderId() == null || inboundItem.getProductId() == null) {
            return "";
        }
        QueryWrapper<ErpPresalePackingEntity> packingWrapper = new QueryWrapper<ErpPresalePackingEntity>()
            .eq("presale_order_id", inboundOrder.getPresaleOrderId())
            .orderByDesc("id");
        List<ErpPresalePackingEntity> packingList = this.erpPresalePackingDao.selectList((Wrapper)packingWrapper);
        if (packingList == null || packingList.isEmpty()) {
            return "";
        }
        String containerNo = StringUtils.trimToEmpty((String)inboundOrder.getContainerNo());
        for (ErpPresalePackingEntity packing : packingList) {
            if (packing == null) continue;
            String packingContainerNo = StringUtils.trimToEmpty((String)packing.getContainerNo());
            if (StringUtils.isNotBlank((String)containerNo) && StringUtils.isNotBlank((String)packingContainerNo) && !containerNo.equalsIgnoreCase(packingContainerNo)) {
                continue;
            }
            ErpPresalePackingItemEntity packingItem = this.erpPresalePackingItemDao.selectOne((Wrapper)new QueryWrapper<ErpPresalePackingItemEntity>()
                .eq("packing_id", packing.getId())
                .eq("product_id", inboundItem.getProductId())
                .last("limit 1"));
            if (packingItem != null && StringUtils.isNotBlank((String)packingItem.getFactoryNo())) {
                return StringUtils.trimToEmpty((String)packingItem.getFactoryNo());
            }
        }
        return "";
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
        ArrayList<Long> presaleOrderIds = new ArrayList<Long>();
        ArrayList<Long> confirmIds = new ArrayList<Long>();
        for (ErpInboundOrderEntity order : inboundOrderMap.values()) {
            if (order.getPresaleOrderId() != null && !presaleOrderIds.contains(order.getPresaleOrderId())) {
                presaleOrderIds.add(order.getPresaleOrderId());
            }
            if (order.getConfirmId() != null && !confirmIds.contains(order.getConfirmId())) {
                confirmIds.add(order.getConfirmId());
            }
        }
        Map<Long, String> ownershipMap = this.loadOwnershipNameMap(presaleOrderIds, confirmIds);
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
            candidate.ownershipName = this.resolveOwnershipNameByConfirm(inboundOrder, ownershipMap);
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

    private Map<Long, String> loadOwnershipNameMap(List<Long> presaleOrderIds, List<Long> confirmIds) {
        HashMap<Long, String> result = new HashMap<Long, String>();
        boolean hasPresaleIds = presaleOrderIds != null && !presaleOrderIds.isEmpty();
        boolean hasConfirmIds = confirmIds != null && !confirmIds.isEmpty();
        if (!hasPresaleIds && !hasConfirmIds) {
            return result;
        }
        ArrayList<ErpFunderPaymentAllocationEntity> allocations = new ArrayList<ErpFunderPaymentAllocationEntity>();
        if (hasConfirmIds) {
            allocations.addAll(this.erpFunderPaymentAllocationDao.selectList((Wrapper)new QueryWrapper<ErpFunderPaymentAllocationEntity>()
                    .in("confirm_id", confirmIds)));
        }
        if (hasPresaleIds) {
            allocations.addAll(this.erpFunderPaymentAllocationDao.selectList((Wrapper)new QueryWrapper<ErpFunderPaymentAllocationEntity>()
                    .in("presale_order_id", presaleOrderIds)
                    .isNull("confirm_id")));
        }
        if (allocations == null || allocations.isEmpty()) {
            return result;
        }
        ArrayList<Long> paymentIds = new ArrayList<Long>();
        ArrayList<Long> allocationIds = new ArrayList<Long>();
        for (ErpFunderPaymentAllocationEntity allocation : allocations) {
            if (allocation == null) continue;
            if (allocation.getPaymentId() != null && !paymentIds.contains(allocation.getPaymentId())) {
                paymentIds.add(allocation.getPaymentId());
            }
            if (allocation.getId() != null) {
                allocationIds.add(allocation.getId());
            }
        }
        HashMap<Long, ErpFunderPaymentEntity> paymentMap = new HashMap<Long, ErpFunderPaymentEntity>();
        if (!paymentIds.isEmpty()) {
            for (ErpFunderPaymentEntity payment : this.erpFunderPaymentDao.selectBatchIds(paymentIds)) {
                paymentMap.put(payment.getId(), payment);
            }
        }
        HashMap<Long, List<ErpFunderLoanEntity>> loanMap = new HashMap<Long, List<ErpFunderLoanEntity>>();
        if (!allocationIds.isEmpty()) {
            List<ErpFunderLoanEntity> loans = this.erpFunderLoanDao.selectList((Wrapper)new QueryWrapper<ErpFunderLoanEntity>().in("allocation_id", allocationIds));
            for (ErpFunderLoanEntity loan : loans) {
                if (loan == null || loan.getAllocationId() == null) continue;
                List<ErpFunderLoanEntity> list = loanMap.get(loan.getAllocationId());
                if (list == null) {
                    list = new ArrayList<ErpFunderLoanEntity>();
                    loanMap.put(loan.getAllocationId(), list);
                }
                list.add(loan);
            }
        }
        HashMap<Long, OwnershipState> stateMap = new HashMap<Long, OwnershipState>();
        for (ErpFunderPaymentAllocationEntity allocation : allocations) {
            if (allocation == null || allocation.getPresaleOrderId() == null) continue;
            ErpFunderPaymentEntity payment = paymentMap.get(allocation.getPaymentId());
            if (payment == null) continue;
            Long ownershipKey = this.ownershipKey(allocation.getPresaleOrderId(), allocation.getConfirmId());
            OwnershipState state = stateMap.get(ownershipKey);
            if (state == null) {
                state = new OwnershipState();
                stateMap.put(ownershipKey, state);
            }
            Integer paymentType = payment.getPaymentType();
            if (paymentType != null && (paymentType == 1 || paymentType == 2)) {
                state.hasPayment = true;
            }
            if (paymentType != null && paymentType == 1 && this.hasUnsettledFunderLoan(loanMap.get(allocation.getId()))) {
                state.hasUnsettledFunder = true;
                state.funderName = this.firstNonBlank(this.firstUnsettledFunderName(loanMap.get(allocation.getId())), payment.getFunderName(), "未知资方");
            }
        }
        for (Map.Entry<Long, OwnershipState> entry : stateMap.entrySet()) {
            OwnershipState state = entry.getValue();
            if (state.hasUnsettledFunder) {
                result.put(entry.getKey(), "资方-" + this.firstNonBlank(state.funderName, "未知资方"));
            } else if (state.hasPayment) {
                result.put(entry.getKey(), "鲜牧");
            }
        }
        return result;
    }

    private boolean hasUnsettledFunderLoan(List<ErpFunderLoanEntity> loans) {
        if (loans == null || loans.isEmpty()) {
            return true;
        }
        for (ErpFunderLoanEntity loan : loans) {
            if (loan == null || loan.getStatus() == null || loan.getStatus() != 1) {
                return true;
            }
        }
        return false;
    }

    private String firstUnsettledFunderName(List<ErpFunderLoanEntity> loans) {
        if (loans == null) {
            return "";
        }
        for (ErpFunderLoanEntity loan : loans) {
            if (loan == null || loan.getStatus() != null && loan.getStatus() == 1) continue;
            if (StringUtils.isNotBlank((String)loan.getFunderName())) {
                return loan.getFunderName();
            }
        }
        return "";
    }

    private String resolveOwnershipNameByConfirm(ErpInboundOrderEntity inboundOrder, Map<Long, String> ownershipMap) {
        if (inboundOrder == null || inboundOrder.getPresaleOrderId() == null || ownershipMap == null) {
            return "\u672a\u786e\u8ba4";
        }
        String ownershipName = ownershipMap.get(this.ownershipKey(inboundOrder.getPresaleOrderId(), inboundOrder.getConfirmId()));
        if (StringUtils.isNotBlank(ownershipName)) {
            return ownershipName;
        }
        return this.firstNonBlank(ownershipMap.get(inboundOrder.getPresaleOrderId()), "\u672a\u786e\u8ba4");
    }

    private Long ownershipKey(Long presaleOrderId, Long confirmId) {
        return confirmId != null && confirmId > 0L ? -confirmId : presaleOrderId;
    }

    private String resolveOwnershipName(ErpInboundOrderEntity inboundOrder, Map<Long, String> ownershipMap) {
        if (inboundOrder == null || inboundOrder.getPresaleOrderId() == null || ownershipMap == null) {
            return "未确认";
        }
        return this.firstNonBlank(ownershipMap.get(inboundOrder.getPresaleOrderId()), "未确认");
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

    private JSONObject runOutboundReceiptOcr(List<ErpSaleOrderFileEntity> files) throws Exception {
        if (files == null || files.isEmpty()) {
            throw new RuntimeException("请先上传出库回单文件");
        }
        Path listFile = Files.createTempFile("erp-outbound-paths-", ".txt", new FileAttribute[0]);
        try {
            List<String> pathLines = new ArrayList<String>();
            for (ErpSaleOrderFileEntity file : files) {
                if (file != null && StringUtils.isNotBlank((String)file.getFilePath())) {
                    pathLines.add(file.getFilePath());
                }
            }
            if (pathLines.isEmpty()) {
                throw new RuntimeException("出库回单文件路径为空");
            }
            Files.write(listFile, pathLines, StandardCharsets.UTF_8);
            File scriptFile = this.ensureOutboundOcrScriptFile();
            ProcessBuilder processBuilder = new ProcessBuilder(new String[]{"python", "-X", "utf8", scriptFile.getAbsolutePath(), listFile.toAbsolutePath().toString()});
            processBuilder.environment().put("PYTHONIOENCODING", "UTF-8");
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            String output;
            try (InputStream inputStream = process.getInputStream();
                 ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
                byte[] bytes = new byte[4096];
                int len;
                while ((len = inputStream.read(bytes)) != -1) {
                    buffer.write(bytes, 0, len);
                }
                output = new String(buffer.toByteArray(), StandardCharsets.UTF_8).trim();
            }
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("出库回单OCR识别失败: " + output);
            }
            JSONObject result = JSON.parseObject(output);
            if (result == null || result.getJSONObject("receipt") == null) {
                throw new RuntimeException("出库回单OCR结果为空");
            }
            return result;
        }
        finally {
            Files.deleteIfExists(listFile);
        }
    }

    private File ensureOutboundOcrScriptFile() throws Exception {
        File target = new File(System.getProperty("java.io.tmpdir"), "erp_outbound_ocr.py");
        this.copyOcrScript("ocr/erp_outbound_ocr.py", target);
        this.copyOcrScript("ocr/erp_ocr.py", new File(System.getProperty("java.io.tmpdir"), "erp_ocr.py"));
        return target;
    }

    private void copyOcrScript(String resourcePath, File target) throws Exception {
        Path sourcePath = Paths.get("D:\\renren-fast-vue\\renren-fast\\src\\main\\resources", resourcePath);
        if (Files.exists(sourcePath)) {
            Files.copy(sourcePath, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return;
        }
        this.copyClasspathOcrScript(resourcePath, target);
    }

    private void copyClasspathOcrScript(String resourcePath, File target) throws Exception {
        ClassPathResource resource = new ClassPathResource(resourcePath);
        try (InputStream inputStream = resource.getInputStream()) {
            Files.copy(inputStream, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private ErpSaleOutboundReceiptEntity buildOutboundReceiptFromOcr(Long saleOrderId, JSONObject recognized) {
        JSONObject receiptJson = recognized.getJSONObject("receipt");
        ErpSaleOutboundReceiptEntity receipt = new ErpSaleOutboundReceiptEntity();
        receipt.setSaleOrderId(saleOrderId);
        receipt.setWmsOrderNo(StringUtils.trimToEmpty((String)receiptJson.getString("wmsOrderNo")));
        receipt.setOutboundOrderNo(StringUtils.trimToEmpty((String)receiptJson.getString("outboundOrderNo")));
        receipt.setCustomerCode(StringUtils.trimToEmpty((String)receiptJson.getString("customerCode")));
        receipt.setCustomerName(StringUtils.trimToEmpty((String)receiptJson.getString("customerName")));
        receipt.setRawText(recognized.getString("rawText"));
        JSONArray itemArray = receiptJson.getJSONArray("itemList");
        ArrayList<ErpSaleOutboundReceiptItemEntity> items = new ArrayList<ErpSaleOutboundReceiptItemEntity>();
        if (itemArray != null) {
            int lineNo = 1;
            for (int i = 0; i < itemArray.size(); i++) {
                JSONObject itemJson = itemArray.getJSONObject(i);
                ErpSaleOutboundReceiptItemEntity item = new ErpSaleOutboundReceiptItemEntity();
                item.setLineNo(lineNo++);
                item.setSaleOrderId(saleOrderId);
                item.setWmsOrderNo(this.firstNonBlank(itemJson.getString("wmsOrderNo"), receiptJson.getString("wmsOrderNo")));
                item.setOutboundOrderNo(this.firstNonBlank(itemJson.getString("outboundOrderNo"), receiptJson.getString("outboundOrderNo")));
                item.setCustomerCode(this.firstNonBlank(itemJson.getString("customerCode"), receiptJson.getString("customerCode")));
                item.setCustomerName(this.firstNonBlank(itemJson.getString("customerName"), receiptJson.getString("customerName")));
                item.setRecognizedProductCode(StringUtils.trimToEmpty((String)itemJson.getString("recognizedProductCode")));
                item.setProductSpec(StringUtils.trimToEmpty((String)itemJson.getString("productSpec")));
                item.setUnit(StringUtils.trimToEmpty((String)itemJson.getString("unit")));
                item.setOrderQty(itemJson.getInteger("orderQty"));
                item.setShippedQty(itemJson.getInteger("shippedQty"));
                item.setContainerNo(StringUtils.trimToEmpty((String)itemJson.getString("containerNo")));
                item.setFactoryNo(StringUtils.trimToEmpty((String)itemJson.getString("factoryNo")));
                item.setAvgWeight(itemJson.getBigDecimal("avgWeight"));
                item.setTotalWeight(itemJson.getBigDecimal("totalWeight"));
                String recognizedName = StringUtils.trimToEmpty((String)itemJson.getString("recognizedProductName"));
                this.enrichOutboundReceiptItemProduct(item, recognizedName);
                items.add(item);
            }
        }
        receipt.setItemList(items);
        return receipt;
    }

    private ErpSaleOutboundReceiptEntity upsertOutboundReceipt(ErpSaleOutboundReceiptEntity receipt, Long userId) {
        ErpSaleOrderEntity order = (ErpSaleOrderEntity)this.getById(receipt.getSaleOrderId());
        if (order == null) {
            throw new RuntimeException("销售单不存在");
        }
        if (this.defaultFlag(order.getOutboundReceiptConfirmed()) == 1) {
            throw new RuntimeException("出库回单已确认，不能修改");
        }
        Date now = new Date();
        ErpSaleOutboundReceiptEntity existing = this.selectOutboundReceiptForSave(receipt);
        receipt.setSaleTotalBoxes(this.calcSaleTotalBoxes(receipt.getSaleOrderId()));
        receipt.setShippedTotalBoxes(this.calcOutboundShippedBoxes(receipt.getItemList()));
        boolean matched = this.defaultInt(receipt.getSaleTotalBoxes()) == this.defaultInt(receipt.getShippedTotalBoxes());
        receipt.setMatched(matched ? 1 : 0);
        receipt.setMatchMessage(matched ? "销售单箱数与出库回单发货数一致" : "销售单箱数" + this.defaultInt(receipt.getSaleTotalBoxes()) + "箱，出库回单发货数" + this.defaultInt(receipt.getShippedTotalBoxes()) + "箱，请核对");
        receipt.setUpdateTime(now);
        if (existing == null) {
            receipt.setCreateTime(now);
            this.erpSaleOutboundReceiptDao.insert(receipt);
        } else {
            receipt.setId(existing.getId());
            receipt.setCreateTime(existing.getCreateTime());
            this.erpSaleOutboundReceiptDao.updateById(receipt);
            this.erpSaleOutboundReceiptItemDao.delete((Wrapper)new QueryWrapper().eq((Object)"receipt_id", (Object)existing.getId()));
        }
        int lineNo = 1;
        for (ErpSaleOutboundReceiptItemEntity item : receipt.getItemList()) {
            if (item == null) continue;
            item.setId(null);
            item.setReceiptId(receipt.getId());
            item.setSaleOrderId(receipt.getSaleOrderId());
            item.setBatchId(receipt.getBatchId());
            item.setLineNo(lineNo++);
            item.setCreateTime(now);
            item.setUpdateTime(now);
            this.enrichOutboundReceiptItemProduct(item, item.getProductName());
            this.erpSaleOutboundReceiptItemDao.insert(item);
        }
        return receipt.getBatchId() == null ? this.loadOutboundReceipt(receipt.getSaleOrderId()) : this.loadOutboundReceiptByBatch(receipt.getBatchId());
    }

    private ErpSaleOutboundReceiptEntity appendOutboundReceiptRecognition(ErpSaleOutboundReceiptEntity receipt, Long userId) {
        ErpSaleOrderEntity order = (ErpSaleOrderEntity)this.getById(receipt.getSaleOrderId());
        if (order == null) {
            throw new RuntimeException("销售单不存在");
        }
        if (this.defaultFlag(order.getOutboundReceiptConfirmed()) == 1) {
            throw new RuntimeException("出库回单已确认，不能修改");
        }
        Date now = new Date();
        ErpSaleOutboundReceiptEntity existing = this.selectOutboundReceiptForSave(receipt);
        if (existing == null) {
            receipt.setSaleTotalBoxes(this.calcSaleTotalBoxes(receipt.getSaleOrderId()));
            receipt.setShippedTotalBoxes(0);
            receipt.setMatched(0);
            receipt.setMatchMessage("出库回单待核对");
            receipt.setCreateTime(now);
            receipt.setUpdateTime(now);
            this.erpSaleOutboundReceiptDao.insert(receipt);
            existing = receipt;
        } else {
            existing.setWmsOrderNo(this.firstNonBlank(existing.getWmsOrderNo(), receipt.getWmsOrderNo()));
            existing.setOutboundOrderNo(this.firstNonBlank(existing.getOutboundOrderNo(), receipt.getOutboundOrderNo()));
            existing.setCustomerCode(this.firstNonBlank(existing.getCustomerCode(), receipt.getCustomerCode()));
            existing.setCustomerName(this.firstNonBlank(existing.getCustomerName(), receipt.getCustomerName()));
            existing.setRawText(this.combineText(existing.getRawText(), receipt.getRawText()));
            existing.setUpdateTime(now);
            this.erpSaleOutboundReceiptDao.updateById(existing);
        }
        int lineNo = this.nextOutboundReceiptItemLineNo(existing.getId());
        if (receipt.getItemList() != null) {
            for (ErpSaleOutboundReceiptItemEntity item : receipt.getItemList()) {
                if (item == null) continue;
                item.setId(null);
                item.setReceiptId(existing.getId());
                item.setSaleOrderId(receipt.getSaleOrderId());
                item.setBatchId(receipt.getBatchId());
                item.setLineNo(lineNo++);
                item.setCreateTime(now);
                item.setUpdateTime(now);
                this.fillOutboundReceiptItemHeaderFallback(item, existing);
                this.enrichOutboundReceiptItemProduct(item, item.getProductName());
                this.erpSaleOutboundReceiptItemDao.insert(item);
            }
        }
        this.refreshOutboundReceiptMatch(existing);
        return receipt.getBatchId() == null ? this.loadOutboundReceipt(receipt.getSaleOrderId()) : this.loadOutboundReceiptByBatch(receipt.getBatchId());
    }

    private void refreshOutboundReceiptMatch(ErpSaleOutboundReceiptEntity receipt) {
        if (receipt == null || receipt.getSaleOrderId() == null) {
            return;
        }
        QueryWrapper<ErpSaleOutboundReceiptItemEntity> wrapper = new QueryWrapper<ErpSaleOutboundReceiptItemEntity>().eq("sale_order_id", receipt.getSaleOrderId());
        if (receipt.getBatchId() != null) {
            wrapper.eq("batch_id", receipt.getBatchId());
        }
        List<ErpSaleOutboundReceiptItemEntity> items = this.erpSaleOutboundReceiptItemDao.selectList((Wrapper)wrapper);
        int saleTotalBoxes = this.calcSaleTotalBoxes(receipt.getSaleOrderId());
        int shippedTotalBoxes = this.calcOutboundShippedBoxes(items);
        boolean matched = saleTotalBoxes == shippedTotalBoxes;
        receipt.setSaleTotalBoxes(saleTotalBoxes);
        receipt.setShippedTotalBoxes(shippedTotalBoxes);
        receipt.setMatched(matched ? 1 : 0);
        receipt.setMatchMessage(matched ? "销售单箱数与出库回单发货数一致" : "销售单箱数" + saleTotalBoxes + "箱，出库回单发货数" + shippedTotalBoxes + "箱，请核对");
        receipt.setUpdateTime(new Date());
        this.erpSaleOutboundReceiptDao.updateById(receipt);
    }

    private int nextOutboundReceiptItemLineNo(Long receiptId) {
        if (receiptId == null) {
            return 1;
        }
        List items = this.erpSaleOutboundReceiptItemDao.selectList((Wrapper)((QueryWrapper)((QueryWrapper)new QueryWrapper().eq((Object)"receipt_id", (Object)receiptId)).orderByDesc((Object[])new String[]{"line_no", "id"})).last("limit 1"));
        if (items == null || items.isEmpty() || ((ErpSaleOutboundReceiptItemEntity)items.get(0)).getLineNo() == null) {
            return 1;
        }
        return ((ErpSaleOutboundReceiptItemEntity)items.get(0)).getLineNo() + 1;
    }

    private String combineText(String existingText, String newText) {
        if (StringUtils.isBlank((String)existingText)) {
            return newText;
        }
        if (StringUtils.isBlank((String)newText)) {
            return existingText;
        }
        return existingText + "\n\n" + newText;
    }

    private ErpSaleOutboundReceiptEntity selectOutboundReceiptForSave(ErpSaleOutboundReceiptEntity receipt) {
        QueryWrapper<ErpSaleOutboundReceiptEntity> wrapper = new QueryWrapper<ErpSaleOutboundReceiptEntity>()
                .eq("sale_order_id", receipt.getSaleOrderId());
        if (receipt.getBatchId() == null) {
            wrapper.isNull("batch_id");
        } else {
            wrapper.eq("batch_id", receipt.getBatchId());
        }
        return this.erpSaleOutboundReceiptDao.selectOne((Wrapper)wrapper.last("limit 1"));
    }

    private ErpSaleOutboundBatchEntity ensureOpenOutboundBatch(Long saleOrderId, Long userId) {
        ErpSaleOutboundBatchEntity openBatch = this.findOpenOutboundBatch(saleOrderId);
        if (openBatch != null) {
            return openBatch;
        }
        return this.createOutboundBatch(saleOrderId, userId);
    }

    private ErpSaleOutboundBatchEntity findOpenOutboundBatch(Long saleOrderId) {
        QueryWrapper<ErpSaleOutboundBatchEntity> wrapper = new QueryWrapper<ErpSaleOutboundBatchEntity>();
        wrapper.eq("sale_order_id", saleOrderId);
        wrapper.ne("status", OUTBOUND_BATCH_CONFIRMED);
        wrapper.ne("status", OUTBOUND_BATCH_VOID);
        wrapper.orderByDesc("id");
        return this.erpSaleOutboundBatchDao.selectOne((Wrapper)wrapper.last("limit 1"));
    }

    private ErpSaleOutboundBatchEntity requireEditableOutboundBatch(Long saleOrderId, Long batchId) {
        if (batchId == null || batchId <= 0) {
            throw new RuntimeException("请先选择出库批次");
        }
        ErpSaleOutboundBatchEntity batch = (ErpSaleOutboundBatchEntity)this.erpSaleOutboundBatchDao.selectById(batchId);
        if (batch == null) {
            throw new RuntimeException("出库批次不存在");
        }
        if (saleOrderId != null && !saleOrderId.equals(batch.getSaleOrderId())) {
            throw new RuntimeException("出库批次与销售单不匹配");
        }
        int status = this.defaultInt(batch.getStatus());
        if (status == OUTBOUND_BATCH_CONFIRMED) {
            throw new RuntimeException("出库批次已确认，如需调整请走后续冲正流程");
        }
        if (status == OUTBOUND_BATCH_VOID) {
            throw new RuntimeException("出库批次已作废，不能继续操作");
        }
        return batch;
    }

    private String nextOutboundBatchNo(Long saleOrderId) {
        Number count = (Number)this.erpSaleOutboundBatchDao.selectCount((Wrapper)new QueryWrapper().eq((Object)"sale_order_id", (Object)saleOrderId));
        int next = (count == null ? 0 : count.intValue()) + 1;
        return "CK-" + saleOrderId + "-" + String.format("%02d", next);
    }

    private void fillOutboundBatchBankSlipRecognition(ErpSaleOutboundBatchEntity batch, MultipartFile file) throws Exception {
        ErpRecognizeResultVo result = this.erpOcrService.recognize(file, "bank_payment_voucher");
        String rawText = result == null ? "" : StringUtils.defaultString(result.getRawText());
        Map<String, Object> receipt = this.extractOutboundBankReceipt(rawText);
        String payerName = this.stringValue(receipt.get("payerName"));
        String payeeName = this.stringValue(receipt.get("payeeName"));
        String serialNo = this.stringValue(receipt.get("serialNo"));
        BigDecimal amount = receipt.get("amount") instanceof BigDecimal ? (BigDecimal)receipt.get("amount") : this.extractOutboundBankAmount(rawText);
        Date paymentDate = receipt.get("paymentDate") instanceof Date ? (Date)receipt.get("paymentDate") : this.extractOutboundBankDate(rawText);
        batch.setBankVoucherTemplate(this.firstNonBlank(this.stringValue(receipt.get("voucherTemplate")), "银行电子回单"));
        batch.setBankPayerNameRecognized(payerName);
        batch.setBankPayeeNameRecognized(payeeName);
        batch.setBankAmountRecognized(this.defaultDecimal(amount).setScale(2, RoundingMode.HALF_UP));
        batch.setBankPaymentDateRecognized(paymentDate);
        batch.setBankSerialNoRecognized(serialNo);
        batch.setBankPayerNameModified(payerName);
        batch.setBankPayeeNameModified(payeeName);
        batch.setBankAmountModified(this.defaultDecimal(amount).setScale(2, RoundingMode.HALF_UP));
        batch.setBankPaymentDateModified(paymentDate);
        batch.setBankSerialNoModified(serialNo);
        batch.setBankReceiptRawText(rawText);
    }

    private Map<String, Object> extractOutboundBankReceipt(String rawText) {
        Map<String, Object> receipt = new HashMap<String, Object>();
        String text = StringUtils.defaultString(rawText);
        if (StringUtils.contains(text, "农业发展银行") || StringUtils.containsIgnoreCase(text, "AGRICULTURAL DEVELOPMENT BANK")) {
            receipt.put("voucherTemplate", "中国农业发展银行客户专用回单");
            receipt.put("serialNo", this.extractValueAfterRegex(text, "(?:核心流水号|交易流水号)\\s*[:：]?\\s*([A-Za-z0-9\\-]+)"));
            this.fillAdbcPartyNames(receipt, text);
            receipt.put("amount", this.extractOutboundBankAmount(text));
            receipt.put("paymentDate", this.extractOutboundBankDate(text));
            return receipt;
        }
        receipt.put("voucherTemplate", "银行电子回单");
        receipt.put("serialNo", this.extractValueAfterRegex(text, "(?:核心流水号|交易流水号|唯一流水编号|电子回单编号|电子回单号码|凭证号)\\s*[:：]?\\s*([A-Za-z0-9\\-]+)"));
        receipt.put("payerName", this.extractGenericBankPartyName(text, "付款"));
        receipt.put("payeeName", this.extractGenericBankPartyName(text, "收款"));
        receipt.put("amount", this.extractOutboundBankAmount(text));
        receipt.put("paymentDate", this.extractOutboundBankDate(text));
        return receipt;
    }

    private String extractBankPartyName(String rawText, String startMarker, String endMarker) {
        String text = StringUtils.defaultString(rawText);
        int start = text.indexOf(startMarker);
        if (start < 0) {
            return null;
        }
        int end = StringUtils.isBlank(endMarker) ? -1 : text.indexOf(endMarker, start + startMarker.length());
        String block = end > start ? text.substring(start, end) : text.substring(start);
        String name = this.extractValueAfterRegex(block, "(?:户名|账户名称|全称)\\s*[:：]?\\s*([^\\r\\n]+)");
        if (StringUtils.isBlank(name)) {
            List<String> names = this.extractValuesAfterMarker(this.bankNonBlankLines(rawText), "户名", 2);
            if ("付款人".equals(startMarker) && !names.isEmpty()) {
                name = names.get(0);
            } else if ("收款人".equals(startMarker) && names.size() > 1) {
                name = names.get(1);
            }
        }
        return this.cleanBankTextValue(name);
    }

    private void fillAdbcPartyNames(Map<String, Object> receipt, String rawText) {
        List<String> companyNames = this.extractCompanyNamesBeforeMarker(this.bankNonBlankLines(rawText), "户名", 2);
        if (!companyNames.isEmpty()) {
            receipt.put("payerName", companyNames.get(0));
        }
        if (companyNames.size() > 1) {
            receipt.put("payeeName", companyNames.get(1));
        }
        if (receipt.get("payerName") == null) {
            receipt.put("payerName", this.extractBankPartyName(rawText, "付款人", "收款人"));
        }
        if (receipt.get("payeeName") == null) {
            receipt.put("payeeName", this.extractBankPartyName(rawText, "收款人", "交易渠道"));
        }
    }

    private List<String> extractCompanyNamesBeforeMarker(List<String> lines, String marker, int maxCount) {
        List<String> values = new ArrayList<String>();
        int markerIndex = -1;
        for (int i = 0; i < lines.size(); i++) {
            if (StringUtils.contains(lines.get(i), marker)) {
                markerIndex = i;
                break;
            }
        }
        if (markerIndex <= 0) {
            return values;
        }
        for (int i = markerIndex - 1; i >= 0 && values.size() < maxCount; i--) {
            String candidate = this.cleanBankTextValue(lines.get(i));
            if (StringUtils.isBlank(candidate) || !StringUtils.contains(candidate, "公司")) {
                continue;
            }
            values.add(0, candidate);
        }
        while (values.size() > maxCount) {
            values.remove(0);
        }
        return values;
    }

    private String extractGenericBankPartyName(String rawText, String partyMarker) {
        String name = this.extractValueAfterRegex(rawText, partyMarker + "[^\\r\\n]{0,8}(?:户名|账户名称|全称)\\s*[:：]?\\s*([^\\r\\n]+)");
        if (StringUtils.isNotBlank(name)) {
            return this.cleanBankTextValue(name);
        }
        List<String> names = this.extractValuesAfterMarker(this.bankNonBlankLines(rawText), "户名", 2);
        if ("付款".equals(partyMarker) && !names.isEmpty()) {
            return this.cleanBankTextValue(names.get(0));
        }
        if ("收款".equals(partyMarker) && names.size() > 1) {
            return this.cleanBankTextValue(names.get(1));
        }
        return null;
    }

    private BigDecimal extractOutboundBankAmount(String rawText) {
        String text = StringUtils.defaultString(rawText);
        BigDecimal amount = this.extractOutboundAmountNearLabel(text, "小写");
        if (amount != null) {
            return amount;
        }
        amount = this.extractOutboundAmountNearLabel(text, "金额");
        if (amount != null) {
            return amount;
        }
        Matcher matcher = Pattern.compile("(?:￥|¥)?\\s*([0-9]{1,3}(?:,[0-9]{3})*(?:\\.[0-9]{1,2})|[0-9]+\\.[0-9]{1,2})\\s*(?:元)?").matcher(text);
        BigDecimal result = BigDecimal.ZERO;
        while (matcher.find()) {
            result = this.parseDecimal(matcher.group(1)).setScale(2, RoundingMode.HALF_UP);
        }
        return result;
    }

    private BigDecimal extractOutboundAmountNearLabel(String rawText, String label) {
        int index = StringUtils.indexOf(rawText, label);
        if (index < 0) {
            return null;
        }
        String nearby = StringUtils.substring(rawText, index, Math.min(rawText.length(), index + 160));
        Matcher matcher = Pattern.compile("([0-9]{1,3}(?:,[0-9]{3})*(?:\\.[0-9]{1,2})|[0-9]+\\.[0-9]{1,2})").matcher(nearby);
        if (!matcher.find()) {
            return null;
        }
        return this.parseDecimal(matcher.group(1)).setScale(2, RoundingMode.HALF_UP);
    }

    private Date extractOutboundBankDate(String rawText) {
        String text = StringUtils.defaultString(rawText);
        String compact = this.extractValueAfterRegex(text, "(?:交易日期|日期|打印日期)\\s*[:：]?\\s*(20\\d{6})");
        if (StringUtils.isNotBlank(compact)) {
            return this.parseDateTime(compact.substring(0, 4) + "-" + compact.substring(4, 6) + "-" + compact.substring(6, 8));
        }
        Matcher matcher = Pattern.compile("(20\\d{2})[年\\-/\\.](\\d{1,2})[月\\-/\\.](\\d{1,2})").matcher(text);
        if (matcher.find()) {
            return this.parseDateTime(matcher.group(1) + "-" + matcher.group(2) + "-" + matcher.group(3));
        }
        return null;
    }

    private void refreshOutboundBatchBankAmountDiff(ErpSaleOutboundBatchEntity batch) {
        if (batch == null || batch.getId() == null) {
            return;
        }
        BigDecimal expectedAmount = this.calcOutboundBatchReceivableAmount(batch.getId());
        BigDecimal modifiedAmount = batch.getBankAmountModified() == null ? batch.getBankAmountRecognized() : batch.getBankAmountModified();
        batch.setBankExpectedAmount(expectedAmount.setScale(2, RoundingMode.HALF_UP));
        batch.setBankAmountDiff(this.defaultDecimal(modifiedAmount).subtract(expectedAmount).setScale(2, RoundingMode.HALF_UP));
    }

    private void clearOutboundBatchBankSlip(ErpSaleOutboundBatchEntity batch) {
        if (batch == null) {
            return;
        }
        batch.setBankVoucherTemplate(null);
        batch.setBankPayerNameRecognized(null);
        batch.setBankPayerNameModified(null);
        batch.setBankPayeeNameRecognized(null);
        batch.setBankPayeeNameModified(null);
        batch.setBankAmountRecognized(null);
        batch.setBankAmountModified(null);
        batch.setBankPaymentDateRecognized(null);
        batch.setBankPaymentDateModified(null);
        batch.setBankSerialNoRecognized(null);
        batch.setBankSerialNoModified(null);
        batch.setBankExpectedAmount(null);
        batch.setBankAmountDiff(null);
        batch.setBankReceiptRawText(null);
    }

    private BigDecimal calcOutboundBatchReceivableAmount(Long batchId) {
        ErpSaleOutboundReceiptEntity receipt = this.loadOutboundReceiptByBatch(batchId);
        if (receipt == null || receipt.getItemList() == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal total = BigDecimal.ZERO;
        for (ErpSaleOutboundReceiptItemEntity item : receipt.getItemList()) {
            if (item == null) {
                continue;
            }
            total = total.add(this.defaultDecimal(item.getTotalWeight()).multiply(this.defaultDecimal(item.getSalePriceKg())));
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    private String extractValueAfterRegex(String rawText, String regex) {
        Matcher matcher = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL).matcher(StringUtils.defaultString(rawText));
        return matcher.find() ? this.cleanBankTextValue(matcher.group(1)) : null;
    }

    private List<String> bankNonBlankLines(String rawText) {
        List<String> lines = new ArrayList<String>();
        for (String line : StringUtils.split(StringUtils.defaultString(rawText), '\n')) {
            String value = StringUtils.trimToEmpty(line);
            if (StringUtils.isNotBlank(value)) {
                lines.add(value);
            }
        }
        return lines;
    }

    private List<String> extractValuesAfterMarker(List<String> lines, String marker, int maxCount) {
        List<String> values = new ArrayList<String>();
        for (int index = 0; index < lines.size() && values.size() < maxCount; index++) {
            String line = lines.get(index);
            if (!StringUtils.contains(line, marker)) {
                continue;
            }
            String inlineValue = this.cleanBankTextValue(StringUtils.substringAfter(line, marker));
            if (StringUtils.isNotBlank(inlineValue)) {
                values.add(inlineValue);
            }
            for (int offset = 1; offset <= 4 && index + offset < lines.size() && values.size() < maxCount; offset++) {
                String candidate = this.cleanBankTextValue(lines.get(index + offset));
                if (StringUtils.isNotBlank(candidate) && !candidate.matches("^\\d{6,}$")) {
                    values.add(candidate);
                }
            }
        }
        return values;
    }

    private String cleanBankTextValue(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        String result = StringUtils.trimToEmpty(value).replaceAll("^[：:：\\s]+", "").trim();
        result = result.replaceAll("\\s{2,}.*$", "").trim();
        return StringUtils.isBlank(result) ? null : result;
    }

    private ErpSaleOutboundBatchEntity refreshOutboundBatchStatus(Long batchId) {
        ErpSaleOutboundBatchEntity batch = (ErpSaleOutboundBatchEntity)this.erpSaleOutboundBatchDao.selectById(batchId);
        if (batch == null) {
            return null;
        }
        if (this.defaultInt(batch.getStatus()) == OUTBOUND_BATCH_CONFIRMED || this.defaultInt(batch.getStatus()) == OUTBOUND_BATCH_VOID) {
            return this.loadOutboundBatch(batchId);
        }
        ErpSaleOutboundReceiptEntity receipt = this.loadOutboundReceiptByBatch(batchId);
        int receiptCount = receipt == null || receipt.getItemList() == null ? 0 : receipt.getItemList().size();
        int shippedBoxes = receipt == null ? 0 : this.calcOutboundShippedBoxes(receipt.getItemList());
        BigDecimal shippedWeight = receipt == null ? BigDecimal.ZERO : this.calcOutboundShippedWeight(receipt.getItemList());
        int status = receiptCount <= 0 ? OUTBOUND_BATCH_PENDING_RECEIPT : (batch.getBankSlipFileId() == null ? OUTBOUND_BATCH_PENDING_BANK : OUTBOUND_BATCH_PENDING_CONFIRM);
        batch.setReceiptCount(receiptCount);
        batch.setShippedTotalBoxes(shippedBoxes);
        batch.setShippedTotalWeight(shippedWeight);
        batch.setStatus(status);
        this.refreshOutboundBatchBankAmountDiff(batch);
        batch.setUpdateTime(new Date());
        this.erpSaleOutboundBatchDao.updateById(batch);
        return this.loadOutboundBatch(batchId);
    }

    private boolean hasOutboundReceiptFile(Long saleOrderId, Long batchId) {
        if (saleOrderId == null || batchId == null) {
            return false;
        }
        Number count = (Number)this.erpSaleOrderFileDao.selectCount((Wrapper)((QueryWrapper)((QueryWrapper)new QueryWrapper()
                .eq((Object)"sale_order_id", (Object)saleOrderId))
                .eq((Object)"batch_id", (Object)batchId))
                .eq((Object)"file_type", (Object)FILE_TYPE_OUTBOUND_RECEIPT));
        return count != null && count.intValue() > 0;
    }

    private List<ErpSaleOutboundBatchEntity> loadOutboundBatches(Long saleOrderId) {
        List<ErpSaleOutboundBatchEntity> batches = this.erpSaleOutboundBatchDao.selectList((Wrapper)((QueryWrapper)new QueryWrapper()
                .eq((Object)"sale_order_id", (Object)saleOrderId))
                .orderByAsc((Object[])new String[]{"id"}));
        if (batches == null) {
            return new ArrayList<ErpSaleOutboundBatchEntity>();
        }
        for (ErpSaleOutboundBatchEntity batch : batches) {
            this.fillOutboundBatchChildren(batch);
        }
        return batches;
    }

    private ErpSaleOutboundBatchEntity loadOutboundBatch(Long batchId) {
        ErpSaleOutboundBatchEntity batch = (ErpSaleOutboundBatchEntity)this.erpSaleOutboundBatchDao.selectById(batchId);
        if (batch != null) {
            this.fillOutboundBatchChildren(batch);
        }
        return batch;
    }

    private void deleteOutboundBatchData(ErpSaleOutboundBatchEntity batch) {
        if (batch == null || batch.getId() == null) {
            return;
        }
        Long batchId = batch.getId();
        this.deleteOutboundScanData(batchId);
        ErpSaleOutboundReceiptEntity receipt = this.loadOutboundReceiptByBatch(batchId);
        if (receipt != null && receipt.getId() != null) {
            this.erpSaleOutboundReceiptItemDao.delete((Wrapper)new QueryWrapper().eq((Object)"receipt_id", (Object)receipt.getId()));
            this.erpSaleOutboundReceiptDao.deleteById(receipt.getId());
        }
        List<ErpSaleOrderFileEntity> files = this.erpSaleOrderFileDao.selectList((Wrapper)new QueryWrapper().eq((Object)"batch_id", (Object)batchId));
        if (files != null) {
            for (ErpSaleOrderFileEntity file : files) {
                this.deleteFileRecord(file);
            }
        }
        this.erpExpenseService.deleteBySource(ErpExpenseService.TYPE_OUTBOUND_STORAGE, ErpExpenseService.SOURCE_OUTBOUND_BATCH, batchId);
        this.erpSaleOutboundBatchDao.deleteById(batchId);
    }

    private void fillOutboundBatchChildren(ErpSaleOutboundBatchEntity batch) {
        if (batch == null) {
            return;
        }
        batch.setReceipt(this.loadOutboundReceiptByBatch(batch.getId()));
        batch.setScan(this.loadOutboundScanByBatch(batch.getId()));
        if (batch.getBankSlipFileId() != null) {
            batch.setBankSlipFile((ErpSaleOrderFileEntity)this.erpSaleOrderFileDao.selectById(batch.getBankSlipFileId()));
        }
        QueryWrapper<ErpSaleOrderFileEntity> fileWrapper = new QueryWrapper<ErpSaleOrderFileEntity>();
        fileWrapper.eq("sale_order_id", batch.getSaleOrderId());
        fileWrapper.eq("batch_id", batch.getId());
        fileWrapper.eq("file_type", FILE_TYPE_OUTBOUND_RECEIPT);
        fileWrapper.orderByAsc("line_no", "id");
        List<ErpSaleOrderFileEntity> receiptFiles = this.erpSaleOrderFileDao.selectList((Wrapper)fileWrapper);
        batch.setReceiptFileList(receiptFiles == null ? new ArrayList<ErpSaleOrderFileEntity>() : receiptFiles);
        batch.setExpenseList(this.erpExpenseService.listBySource(ErpExpenseService.SOURCE_OUTBOUND_BATCH, batch.getId()));
    }

    private ErpSaleOutboundScanEntity loadOutboundScanByBatch(Long batchId) {
        if (batchId == null) {
            return null;
        }
        ErpSaleOutboundScanEntity scan = (ErpSaleOutboundScanEntity)this.erpSaleOutboundScanDao.selectOne((Wrapper)((QueryWrapper)new QueryWrapper().eq((Object)"batch_id", (Object)batchId)).last("limit 1"));
        if (scan == null) {
            return null;
        }
        List items = this.erpSaleOutboundScanItemDao.selectList((Wrapper)((QueryWrapper)new QueryWrapper().eq((Object)"scan_id", (Object)scan.getId())).orderByAsc((Object[])new String[]{"line_no", "id"}));
        scan.setItemList(items);
        return scan;
    }

    private void deleteOutboundScanData(Long batchId) {
        if (batchId == null) {
            return;
        }
        List<ErpSaleOutboundScanEntity> scans = this.erpSaleOutboundScanDao.selectList((Wrapper)new QueryWrapper().eq((Object)"batch_id", (Object)batchId));
        if (scans == null) {
            return;
        }
        for (ErpSaleOutboundScanEntity scan : scans) {
            if (scan == null || scan.getId() == null) continue;
            this.erpSaleOutboundScanItemDao.delete((Wrapper)new QueryWrapper().eq((Object)"scan_id", (Object)scan.getId()));
            this.erpSaleOutboundScanDao.deleteById(scan.getId());
        }
    }

    private ErpSaleOutboundScanItemEntity buildOutboundScanItem(ErpSaleOutboundScanEntity scan, JSONObject itemJson, int lineNo, Date now) {
        ErpSaleOutboundScanItemEntity item = new ErpSaleOutboundScanItemEntity();
        item.setScanId(scan.getId());
        item.setSaleOrderId(scan.getSaleOrderId());
        item.setBatchId(scan.getBatchId());
        item.setLineNo(lineNo);
        item.setScanProductName(StringUtils.trimToEmpty((String)itemJson.getString("commodityChaocodeName")));
        item.setRecognizedProductCode(this.extractScanProductCode(item.getScanProductName()));
        item.setBoxes(this.parseInteger(itemJson.getString("commodityAmount")));
        item.setTotalWeight(this.parseDecimal(itemJson.getString("commodityTotalWeight")));
        JSONArray weightList = itemJson.getJSONArray("weightList");
        item.setWeightListJson(weightList == null ? "[]" : weightList.toJSONString());
        ErpProductEntity product = this.findProductByRecognizedCode(item.getRecognizedProductCode());
        if (product != null) {
            item.setProductId(product.getId());
            item.setProductCode(product.getProductCode());
            item.setProductName(product.getProductName());
            item.setProductNameEn(product.getProductNameEn());
        } else {
            item.setProductName(item.getScanProductName());
        }
        item.setCreateTime(now);
        item.setUpdateTime(now);
        return item;
    }

    private ErpSaleOutboundReceiptItemEntity buildOutboundReceiptItemFromScan(ErpSaleOutboundScanEntity scan, ErpSaleOutboundScanItemEntity scanItem) {
        ErpSaleOutboundReceiptItemEntity item = new ErpSaleOutboundReceiptItemEntity();
        item.setSaleOrderId(scan.getSaleOrderId());
        item.setBatchId(scan.getBatchId());
        item.setLineNo(scanItem.getLineNo());
        item.setOutboundOrderNo(scan.getOrderNum());
        item.setCustomerName(scan.getCustomerName());
        item.setProductId(scanItem.getProductId());
        item.setProductCode(scanItem.getProductCode());
        item.setRecognizedProductCode(scanItem.getRecognizedProductCode());
        item.setProductName(scanItem.getProductName());
        item.setProductNameEn(scanItem.getProductNameEn());
        item.setUnit("箱");
        item.setShippedQty(scanItem.getBoxes());
        item.setTotalWeight(scanItem.getTotalWeight());
        if (this.defaultInt(scanItem.getBoxes()) > 0) {
            item.setAvgWeight(this.defaultDecimal(scanItem.getTotalWeight()).divide(new BigDecimal(this.defaultInt(scanItem.getBoxes())), 4, RoundingMode.HALF_UP));
        }
        return item;
    }

    private JSONObject fetchShunshiOrder(String orderNum, String imei) throws Exception {
        String url = "http://shunshiyun.com/prod-api/order/list?orderNum=" + URLEncoder.encode(orderNum, StandardCharsets.UTF_8.name()) + "&imei=" + URLEncoder.encode(imei, StandardCharsets.UTF_8.name());
        HttpURLConnection connection = (HttpURLConnection)new URL(url).openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(20000);
        connection.setRequestProperty("Accept", "application/json,text/plain,*/*");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
        int status = connection.getResponseCode();
        InputStream inputStream = status >= 200 && status < 300 ? connection.getInputStream() : connection.getErrorStream();
        String body = new String(this.readAllBytes(inputStream), StandardCharsets.UTF_8);
        if (status < 200 || status >= 300) {
            throw new RuntimeException("顺势云接口请求失败：" + status);
        }
        return JSON.parseObject(body);
    }

    private byte[] readAllBytes(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return new byte[0];
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            output.write(buffer, 0, length);
        }
        return output.toByteArray();
    }

    private Map<String, String> parseQueryParams(String url) throws Exception {
        Map<String, String> result = new HashMap<String, String>();
        String query = new URI(url).getRawQuery();
        if (StringUtils.isBlank((String)query)) {
            return result;
        }
        for (String pair : query.split("&")) {
            if (StringUtils.isBlank((String)pair)) continue;
            int index = pair.indexOf('=');
            String key = index >= 0 ? pair.substring(0, index) : pair;
            String value = index >= 0 ? pair.substring(index + 1) : "";
            result.put(URLDecoder.decode(key, StandardCharsets.UTF_8.name()), URLDecoder.decode(value, StandardCharsets.UTF_8.name()));
        }
        return result;
    }

    private String extractScanProductCode(String text) {
        if (StringUtils.isBlank((String)text)) {
            return null;
        }
        java.util.regex.Matcher matcher = Pattern.compile("C\\s*(\\d{4,6})", Pattern.CASE_INSENSITIVE).matcher(text);
        return matcher.find() ? "C" + matcher.group(1) : null;
    }

    private ErpProductEntity findProductByRecognizedCode(String recognizedCode) {
        String mainCode = this.normalizeMainProductCode(recognizedCode);
        if (StringUtils.isBlank((String)mainCode)) {
            return null;
        }
        ErpProductEntity product = (ErpProductEntity)this.erpProductDao.selectOne((Wrapper)((QueryWrapper)new QueryWrapper().eq((Object)"product_code", (Object)mainCode)).last("limit 1"));
        if (product != null) {
            return product;
        }
        List<ErpProductEntity> products = this.erpProductDao.selectList((Wrapper)((QueryWrapper)new QueryWrapper().like((Object)"alias_codes", (Object)mainCode)).last("limit 20"));
        if (products == null) {
            return null;
        }
        for (ErpProductEntity candidate : products) {
            if (candidate == null || StringUtils.isBlank((String)candidate.getAliasCodes())) continue;
            List<String> aliases = Arrays.asList(candidate.getAliasCodes().split("[,，;；\\s]+"));
            if (aliases.contains(mainCode) || aliases.contains(recognizedCode)) {
                return candidate;
            }
        }
        return null;
    }

    private ErpSaleOutboundReceiptEntity loadOutboundReceipt(Long saleOrderId) {
        if (saleOrderId == null) {
            return null;
        }
        ErpSaleOutboundReceiptEntity receipt = this.erpSaleOutboundReceiptDao.selectOne((Wrapper)((QueryWrapper)new QueryWrapper().eq((Object)"sale_order_id", (Object)saleOrderId)).last("limit 1"));
        if (receipt == null) {
            return null;
        }
        List items = this.erpSaleOutboundReceiptItemDao.selectList((Wrapper)((QueryWrapper)new QueryWrapper().eq((Object)"receipt_id", (Object)receipt.getId())).orderByAsc((Object[])new String[]{"line_no", "id"}));
        receipt.setItemList(items);
        this.enrichOutboundReceiptAdjustment(receipt);
        return receipt;
    }

    private ErpSaleOutboundReceiptEntity loadOutboundReceiptByBatch(Long batchId) {
        if (batchId == null) {
            return null;
        }
        ErpSaleOutboundReceiptEntity receipt = this.erpSaleOutboundReceiptDao.selectOne((Wrapper)((QueryWrapper)new QueryWrapper().eq((Object)"batch_id", (Object)batchId)).last("limit 1"));
        if (receipt == null) {
            return null;
        }
        List items = this.erpSaleOutboundReceiptItemDao.selectList((Wrapper)((QueryWrapper)new QueryWrapper().eq((Object)"receipt_id", (Object)receipt.getId())).orderByAsc((Object[])new String[]{"line_no", "id"}));
        receipt.setItemList(items);
        this.enrichOutboundReceiptAdjustment(receipt);
        return receipt;
    }

    private void enrichOutboundReceiptAdjustment(ErpSaleOutboundReceiptEntity receipt) {
        if (receipt == null || receipt.getSaleOrderId() == null || receipt.getItemList() == null) {
            return;
        }
        ErpSaleOrderEntity order = (ErpSaleOrderEntity)this.getById(receipt.getSaleOrderId());
        QueryWrapper<ErpSaleOrderItemEntity> saleItemWrapper = new QueryWrapper<ErpSaleOrderItemEntity>();
        saleItemWrapper.eq("sale_order_id", receipt.getSaleOrderId()).orderByAsc("line_no", "id");
        List<ErpSaleOrderItemEntity> saleItems = this.erpSaleOrderItemDao.selectList((Wrapper)saleItemWrapper);
        Map<String, List<ErpSaleOrderItemEntity>> byProductContainer = new HashMap<String, List<ErpSaleOrderItemEntity>>();
        Map<String, List<ErpSaleOrderItemEntity>> byProduct = new HashMap<String, List<ErpSaleOrderItemEntity>>();
        for (ErpSaleOrderItemEntity saleItem : saleItems) {
            if (saleItem == null || saleItem.getProductId() == null) {
                continue;
            }
            this.addSaleItemMatch(byProduct, String.valueOf(saleItem.getProductId()), saleItem);
            String container = this.normalizeContainerNo(saleItem.getSourceContainerNo());
            if (StringUtils.isNotBlank((String)container)) {
                this.addSaleItemMatch(byProductContainer, saleItem.getProductId() + "|" + container, saleItem);
            }
        }
        for (ErpSaleOutboundReceiptItemEntity item : receipt.getItemList()) {
            if (item == null) {
                continue;
            }
            this.fillOutboundReceiptItemHeaderFallback(item, receipt);
            ErpSaleOrderItemEntity saleItem = this.pollMatchedSaleItem(byProductContainer, byProduct, item);
            this.fillOutboundReceiptAdjustment(item, order, saleItem);
        }
    }

    private void fillOutboundReceiptItemHeaderFallback(ErpSaleOutboundReceiptItemEntity item, ErpSaleOutboundReceiptEntity receipt) {
        if (item == null || receipt == null) {
            return;
        }
        item.setWmsOrderNo(this.firstNonBlank(item.getWmsOrderNo(), receipt.getWmsOrderNo()));
        item.setOutboundOrderNo(this.firstNonBlank(item.getOutboundOrderNo(), receipt.getOutboundOrderNo()));
        item.setCustomerCode(this.firstNonBlank(item.getCustomerCode(), receipt.getCustomerCode()));
        item.setCustomerName(this.firstNonBlank(item.getCustomerName(), receipt.getCustomerName()));
    }

    private void addSaleItemMatch(Map<String, List<ErpSaleOrderItemEntity>> map, String key, ErpSaleOrderItemEntity item) {
        if (StringUtils.isBlank((String)key)) {
            return;
        }
        List<ErpSaleOrderItemEntity> values = map.get(key);
        if (values == null) {
            values = new ArrayList<ErpSaleOrderItemEntity>();
            map.put(key, values);
        }
        values.add(item);
    }

    private ErpSaleOrderItemEntity pollMatchedSaleItem(Map<String, List<ErpSaleOrderItemEntity>> byProductContainer, Map<String, List<ErpSaleOrderItemEntity>> byProduct, ErpSaleOutboundReceiptItemEntity item) {
        if (item == null || item.getProductId() == null) {
            return null;
        }
        String container = this.normalizeContainerNo(item.getContainerNo());
        if (StringUtils.isNotBlank((String)container)) {
            ErpSaleOrderItemEntity matched = this.pollFirst(byProductContainer.get(item.getProductId() + "|" + container));
            if (matched != null) {
                return matched;
            }
        }
        return this.pollFirst(byProduct.get(String.valueOf(item.getProductId())));
    }

    private ErpSaleOrderItemEntity pollFirst(List<ErpSaleOrderItemEntity> items) {
        if (items == null || items.isEmpty()) {
            return null;
        }
        return items.remove(0);
    }

    private void fillOutboundReceiptAdjustment(ErpSaleOutboundReceiptItemEntity item, ErpSaleOrderEntity order, ErpSaleOrderItemEntity saleItem) {
        if (item == null) {
            return;
        }
        item.setContractNo(order == null ? null : this.firstNonBlank(order.getContractNo(), order.getOrderNo()));
        if (saleItem == null) {
            item.setExpectedBoxes(0);
            item.setExpectedWeight(BigDecimal.ZERO);
            item.setSalePriceKg(BigDecimal.ZERO);
        } else {
            item.setExpectedFactoryNo(saleItem.getContractFactoryNo());
            item.setExpectedContainerNo(saleItem.getSourceContainerNo());
            item.setExpectedBoxes(this.defaultInt(saleItem.getBoxes()));
            item.setExpectedWeight(this.defaultDecimal(saleItem.getContractQuantityKg()).setScale(3, RoundingMode.HALF_UP));
            item.setSalePriceKg(this.defaultDecimal(saleItem.getSalePriceKg()).setScale(2, RoundingMode.HALF_UP));
            if (StringUtils.isBlank((String)item.getFactoryNo())) {
                item.setFactoryNo(saleItem.getContractFactoryNo());
            }
        }
        int diffBoxes = this.defaultInt(item.getExpectedBoxes()) - this.defaultInt(item.getShippedQty());
        BigDecimal diffWeight = this.defaultDecimal(item.getExpectedWeight()).subtract(this.defaultDecimal(item.getTotalWeight())).setScale(3, RoundingMode.HALF_UP);
        BigDecimal amount = diffWeight.multiply(this.defaultDecimal(item.getSalePriceKg())).setScale(2, RoundingMode.HALF_UP);
        item.setDiffBoxes(diffBoxes);
        item.setDiffWeight(diffWeight);
        item.setAdjustmentAmount(amount);
    }

    private List<ErpSaleOutboundReceiptItemEntity> buildOutboundSummary(Long saleOrderId) {
        LinkedHashMap<Long, ErpSaleOutboundReceiptItemEntity> summaryMap = new LinkedHashMap<Long, ErpSaleOutboundReceiptItemEntity>();
        List<ErpSaleOrderItemEntity> saleItems = this.erpSaleOrderItemDao.selectList((Wrapper)((QueryWrapper)new QueryWrapper()
                .eq((Object)"sale_order_id", (Object)saleOrderId)).orderByAsc((Object[])new String[]{"line_no", "id"}));
        for (ErpSaleOrderItemEntity saleItem : saleItems) {
            if (saleItem == null || saleItem.getProductId() == null) {
                continue;
            }
            ErpSaleOutboundReceiptItemEntity summary = summaryMap.get(saleItem.getProductId());
            if (summary == null) {
                summary = new ErpSaleOutboundReceiptItemEntity();
                summary.setSaleOrderId(saleOrderId);
                summary.setProductId(saleItem.getProductId());
                summary.setProductCode(saleItem.getProductCode());
                summary.setProductName(saleItem.getProductName());
                summary.setProductNameEn(saleItem.getProductNameEn());
                summary.setExpectedBoxes(0);
                summary.setExpectedWeight(BigDecimal.ZERO);
                summary.setShippedQty(0);
                summary.setTotalWeight(BigDecimal.ZERO);
                summary.setSalePriceKg(this.defaultDecimal(saleItem.getSalePriceKg()).setScale(2, RoundingMode.HALF_UP));
                summaryMap.put(saleItem.getProductId(), summary);
            }
            summary.setExpectedBoxes(this.defaultInt(summary.getExpectedBoxes()) + this.defaultInt(saleItem.getBoxes()));
            summary.setExpectedWeight(this.defaultDecimal(summary.getExpectedWeight()).add(this.defaultDecimal(saleItem.getContractQuantityKg())).setScale(3, RoundingMode.HALF_UP));
            if (this.defaultDecimal(summary.getSalePriceKg()).compareTo(BigDecimal.ZERO) == 0 && saleItem.getSalePriceKg() != null) {
                summary.setSalePriceKg(this.defaultDecimal(saleItem.getSalePriceKg()).setScale(2, RoundingMode.HALF_UP));
            }
        }
        List<ErpSaleOutboundReceiptItemEntity> outboundItems = this.loadActiveOutboundItems(saleOrderId);
        for (ErpSaleOutboundReceiptItemEntity outboundItem : outboundItems) {
            if (outboundItem == null || outboundItem.getProductId() == null) {
                continue;
            }
            ErpSaleOutboundReceiptItemEntity summary = summaryMap.get(outboundItem.getProductId());
            if (summary == null) {
                summary = new ErpSaleOutboundReceiptItemEntity();
                summary.setSaleOrderId(saleOrderId);
                summary.setProductId(outboundItem.getProductId());
                summary.setProductCode(outboundItem.getProductCode());
                summary.setProductName(outboundItem.getProductName());
                summary.setProductNameEn(outboundItem.getProductNameEn());
                summary.setExpectedBoxes(0);
                summary.setExpectedWeight(BigDecimal.ZERO);
                summary.setShippedQty(0);
                summary.setTotalWeight(BigDecimal.ZERO);
                summary.setSalePriceKg(BigDecimal.ZERO);
                summaryMap.put(outboundItem.getProductId(), summary);
            }
            summary.setShippedQty(this.defaultInt(summary.getShippedQty()) + this.defaultInt(outboundItem.getShippedQty()));
            summary.setTotalWeight(this.defaultDecimal(summary.getTotalWeight()).add(this.defaultDecimal(outboundItem.getTotalWeight())).setScale(3, RoundingMode.HALF_UP));
        }
        List<ErpSaleOutboundReceiptItemEntity> result = new ArrayList<ErpSaleOutboundReceiptItemEntity>();
        int lineNo = 1;
        for (ErpSaleOutboundReceiptItemEntity summary : summaryMap.values()) {
            summary.setLineNo(lineNo++);
            int diffBoxes = this.defaultInt(summary.getExpectedBoxes()) - this.defaultInt(summary.getShippedQty());
            BigDecimal diffWeight = this.defaultDecimal(summary.getExpectedWeight()).subtract(this.defaultDecimal(summary.getTotalWeight())).setScale(3, RoundingMode.HALF_UP);
            BigDecimal amount = diffWeight.multiply(this.defaultDecimal(summary.getSalePriceKg())).setScale(2, RoundingMode.HALF_UP);
            summary.setDiffBoxes(diffBoxes);
            summary.setDiffWeight(diffWeight);
            summary.setAdjustmentAmount(amount);
            result.add(summary);
        }
        return result;
    }

    private List<ErpSaleOutboundReceiptItemEntity> loadActiveOutboundItems(Long saleOrderId) {
        List<ErpSaleOutboundBatchEntity> activeBatches = this.erpSaleOutboundBatchDao.selectList((Wrapper)new QueryWrapper<ErpSaleOutboundBatchEntity>()
                .eq("sale_order_id", saleOrderId)
                .ne("status", OUTBOUND_BATCH_VOID));
        if (activeBatches == null || activeBatches.isEmpty()) {
            return new ArrayList<ErpSaleOutboundReceiptItemEntity>();
        }
        List<Long> batchIds = new ArrayList<Long>();
        for (ErpSaleOutboundBatchEntity batch : activeBatches) {
            if (batch != null && batch.getId() != null) {
                batchIds.add(batch.getId());
            }
        }
        if (batchIds.isEmpty()) {
            return new ArrayList<ErpSaleOutboundReceiptItemEntity>();
        }
        QueryWrapper<ErpSaleOutboundReceiptItemEntity> itemWrapper = new QueryWrapper<ErpSaleOutboundReceiptItemEntity>();
        itemWrapper.eq("sale_order_id", saleOrderId);
        itemWrapper.in("batch_id", batchIds);
        itemWrapper.orderByAsc("batch_id", "line_no", "id");
        return this.erpSaleOutboundReceiptItemDao.selectList((Wrapper)itemWrapper);
    }

    private String normalizeContainerNo(String value) {
        return StringUtils.trimToEmpty((String)value).replaceAll("\\s+", "").toUpperCase();
    }

    private void enrichOutboundReceiptItemProduct(ErpSaleOutboundReceiptItemEntity item, String fallbackName) {
        if (item == null) {
            return;
        }
        ErpProductEntity product = null;
        if (item.getProductId() != null) {
            product = (ErpProductEntity)this.erpProductDao.selectById(item.getProductId());
        }
        if (product == null) {
            String mainCode = this.normalizeMainProductCode(this.firstNonBlank(item.getRecognizedProductCode(), item.getProductCode()));
            if (StringUtils.isNotBlank((String)mainCode)) {
                product = (ErpProductEntity)this.erpProductDao.selectOne((Wrapper)((QueryWrapper)new QueryWrapper().eq((Object)"product_code", (Object)mainCode)).last("limit 1"));
            }
        }
        if (product != null) {
            item.setProductId(product.getId());
            item.setProductCode(product.getProductCode());
            item.setProductName(product.getProductName());
            item.setProductNameEn(product.getProductNameEn());
            if (StringUtils.isBlank((String)item.getProductSpec())) {
                item.setProductSpec(product.getProductSpec());
            }
            if (StringUtils.isBlank((String)item.getUnit())) {
                item.setUnit(product.getUnit());
            }
        } else if (StringUtils.isBlank((String)item.getProductName())) {
            item.setProductName(StringUtils.trimToEmpty((String)fallbackName));
        }
    }

    private String normalizeMainProductCode(String value) {
        if (StringUtils.isBlank((String)value)) {
            return null;
        }
        java.util.regex.Matcher matcher = Pattern.compile("(\\d{5})").matcher(value);
        return matcher.find() ? matcher.group(1) : StringUtils.trimToEmpty((String)value);
    }

    private int calcSaleTotalBoxes(Long saleOrderId) {
        List<ErpSaleOrderItemEntity> items = this.erpSaleOrderItemDao.selectList((Wrapper)new QueryWrapper().eq((Object)"sale_order_id", (Object)saleOrderId));
        int total = 0;
        for (ErpSaleOrderItemEntity item : items) {
            total += this.defaultInt(item.getBoxes());
        }
        return total;
    }

    private int calcOutboundShippedBoxes(List<ErpSaleOutboundReceiptItemEntity> items) {
        int total = 0;
        if (items == null) {
            return total;
        }
        for (ErpSaleOutboundReceiptItemEntity item : items) {
            total += item == null ? 0 : this.defaultInt(item.getShippedQty());
        }
        return total;
    }

    private BigDecimal calcOutboundShippedWeight(List<ErpSaleOutboundReceiptItemEntity> items) {
        BigDecimal total = BigDecimal.ZERO;
        if (items == null) {
            return total;
        }
        for (ErpSaleOutboundReceiptItemEntity item : items) {
            if (item == null) {
                continue;
            }
            total = total.add(this.defaultDecimal(item.getTotalWeight()));
        }
        return total.setScale(3, RoundingMode.HALF_UP);
    }

    private void refreshOutboundReceiptOrderConfirmed(Long saleOrderId) {
        ErpSaleOrderEntity order = (ErpSaleOrderEntity)this.getById(saleOrderId);
        if (order == null) {
            return;
        }
        order.setOutboundReceiptConfirmed(this.isOutboundBoxesCompleted(saleOrderId) ? 1 : 0);
        order.setUpdateTime(new Date());
        this.updateById(order);
    }

    private boolean isOutboundBoxesCompleted(Long saleOrderId) {
        List<ErpSaleOutboundReceiptItemEntity> summaryList = this.buildOutboundSummary(saleOrderId);
        if (summaryList == null || summaryList.isEmpty()) {
            return false;
        }
        for (ErpSaleOutboundReceiptItemEntity summary : summaryList) {
            if (this.defaultInt(summary.getExpectedBoxes()) <= 0) {
                return false;
            }
            if (this.defaultInt(summary.getExpectedBoxes()) != this.defaultInt(summary.getShippedQty())) {
                return false;
            }
        }
        return true;
    }

    private void validateOutboundReceiptRequiredItems(ErpSaleOutboundReceiptEntity receipt) {
        if (receipt == null || receipt.getItemList() == null || receipt.getItemList().isEmpty()) {
            throw new RuntimeException("出库回单明细不能为空");
        }
        int index = 1;
        for (ErpSaleOutboundReceiptItemEntity item : receipt.getItemList()) {
            if (item == null) {
                throw new RuntimeException("第" + index + "行出库回单明细不能为空");
            }
            if (item.getProductId() == null) {
                throw new RuntimeException("第" + index + "行系统编码不能为空");
            }
            if (item.getShippedQty() == null || item.getShippedQty() <= 0) {
                throw new RuntimeException("第" + index + "行实际箱数必须大于0");
            }
            if (item.getTotalWeight() == null || item.getTotalWeight().compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("第" + index + "行实际重量必须大于0");
            }
            index++;
        }
    }

    private void validateOutboundReceiptMatched(Long saleOrderId) {
        ErpSaleOutboundReceiptEntity receipt = this.loadOutboundReceipt(saleOrderId);
        if (receipt == null) {
            throw new RuntimeException("请先上传并保存出库回单");
        }
        int saleTotalBoxes = this.calcSaleTotalBoxes(saleOrderId);
        int shippedTotalBoxes = this.calcOutboundShippedBoxes(receipt.getItemList());
        boolean matched = saleTotalBoxes == shippedTotalBoxes;
        if (!matched) {
            throw new RuntimeException("销售单箱数" + saleTotalBoxes + "箱，出库回单发货数" + shippedTotalBoxes + "箱，请核对");
        }
        if (this.defaultInt(receipt.getSaleTotalBoxes()) != saleTotalBoxes || this.defaultInt(receipt.getShippedTotalBoxes()) != shippedTotalBoxes || this.defaultFlag(receipt.getMatched()) != 1) {
            receipt.setSaleTotalBoxes(saleTotalBoxes);
            receipt.setShippedTotalBoxes(shippedTotalBoxes);
            receipt.setMatched(1);
            receipt.setMatchMessage("销售单箱数与出库回单发货数一致");
            receipt.setUpdateTime(new Date());
            this.erpSaleOutboundReceiptDao.updateById(receipt);
        }
    }

    private void loadChildren(ErpSaleOrderEntity order) {
        List items = this.erpSaleOrderItemDao.selectList((Wrapper)((QueryWrapper)new QueryWrapper().eq((Object)"sale_order_id", (Object)order.getId())).orderByAsc((Object[])new String[]{"line_no", "id"}));
        List files = this.erpSaleOrderFileDao.selectList((Wrapper)((QueryWrapper)new QueryWrapper().eq((Object)"sale_order_id", (Object)order.getId())).orderByAsc((Object[])new String[]{"file_type", "line_no", "id"}));
        order.setAllocationItemList(new ArrayList<ErpSaleOrderItemEntity>());
        if (SALE_TYPE_SPOT.equals(order.getSaleType())) {
            this.enrichSaleItemOwnership(items);
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
        order.setOutboundReceipt(this.loadOutboundReceipt(order.getId()));
        order.setOutboundBatchList(this.loadOutboundBatches(order.getId()));
        order.setOutboundSummaryList(this.buildOutboundSummary(order.getId()));
    }

    private void enrichSaleItemOwnership(List<ErpSaleOrderItemEntity> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        ArrayList<Long> inboundOrderIds = new ArrayList<Long>();
        for (ErpSaleOrderItemEntity item : items) {
            if (item != null && item.getSourceInboundOrderId() != null && !inboundOrderIds.contains(item.getSourceInboundOrderId())) {
                inboundOrderIds.add(item.getSourceInboundOrderId());
            }
        }
        if (inboundOrderIds.isEmpty()) {
            return;
        }
        HashMap<Long, ErpInboundOrderEntity> inboundOrderMap = new HashMap<Long, ErpInboundOrderEntity>();
        ArrayList<Long> presaleOrderIds = new ArrayList<Long>();
        ArrayList<Long> confirmIds = new ArrayList<Long>();
        for (ErpInboundOrderEntity inboundOrder : this.erpInboundOrderDao.selectBatchIds(inboundOrderIds)) {
            inboundOrderMap.put(inboundOrder.getId(), inboundOrder);
            if (inboundOrder.getPresaleOrderId() != null && !presaleOrderIds.contains(inboundOrder.getPresaleOrderId())) {
                presaleOrderIds.add(inboundOrder.getPresaleOrderId());
            }
            if (inboundOrder.getConfirmId() != null && !confirmIds.contains(inboundOrder.getConfirmId())) {
                confirmIds.add(inboundOrder.getConfirmId());
            }
        }
        Map<Long, String> ownershipMap = this.loadOwnershipNameMap(presaleOrderIds, confirmIds);
        for (ErpSaleOrderItemEntity item : items) {
            if (item == null) continue;
            item.setOwnershipName(this.resolveOwnershipNameByConfirm(inboundOrderMap.get(item.getSourceInboundOrderId()), ownershipMap));
        }
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
        order.setOutboundReceiptUploaded(this.hasFileType(files, FILE_TYPE_OUTBOUND_RECEIPT) ? 1 : 0);
        order.setOutboundAttachmentUploaded(this.hasFileType(files, FILE_TYPE_OUTBOUND_ATTACHMENT) ? 1 : 0);
        order.setStatus(this.computeStatus(order, files));
        order.setContractUrl(CONTRACT_BASE_URL + order.getContractToken());
        order.setBuyerPortalUrl(PORTAL_BASE_URL + order.getContractToken());
        this.enrichUploadNoticeDisplay(order);
    }

    private void enrichUploadNoticeDisplay(ErpSaleOrderEntity order) {
        QueryWrapper<ErpSaleUploadNoticeEntity> wrapper = new QueryWrapper<ErpSaleUploadNoticeEntity>()
                .eq("sale_order_id", order.getId())
                .orderByDesc("create_time", "id")
                .last("limit 1");
        ErpSaleUploadNoticeEntity notice = this.erpSaleUploadNoticeDao.selectOne(wrapper);
        if (notice == null) {
            order.setUploadNoticeStatus(0);
            order.setUploadNoticeStatusText("未发送");
            order.setUploadNoticeTime(null);
            order.setUploadNoticeErrorMessage(null);
            return;
        }
        order.setUploadNoticeStatus(this.defaultFlag(notice.getStatus()));
        order.setUploadNoticeTime(notice.getCreateTime());
        order.setUploadNoticeErrorMessage(notice.getErrorMessage());
        if (this.defaultFlag(notice.getStatus()) == 9) {
            order.setUploadNoticeStatusText("发送失败");
        } else {
            order.setUploadNoticeStatusText("已发送");
        }
    }

    private int computeStatus(ErpSaleOrderEntity order, List<ErpSaleOrderFileEntity> files) {
        if (this.defaultFlag(order.getSignedContractConfirmed()) == 0) {
            return 1;
        }
        if (this.defaultFlag(order.getOutboundReceiptConfirmed()) == 0) {
            return 5;
        }
        return 6;
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
            if (this.defaultFlag(order.getSignedContractConfirmed()) == 1) {
                throw new RuntimeException("盖章合同已确认，不能重复上传");
            }
            return;
        }
        if (FILE_TYPE_BUYER_PAYMENT.equals(normalizedType)) {
            if (portalMode) {
                throw new RuntimeException("二批打款凭证已移至内部出库批次来款水单流程");
            }
            if (this.defaultFlag(order.getSignedContractConfirmed()) == 0) {
                throw new RuntimeException("请先完成盖章合同确认");
            }
            if (this.defaultFlag(order.getBuyerPaymentConfirmed()) == 1) {
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
        if (FILE_TYPE_OUTBOUND_RECEIPT.equals(normalizedType)) {
            if (portalMode) {
                throw new RuntimeException("出库回单仅支持内部人员上传");
            }
            if (this.defaultFlag(order.getOutboundReceiptConfirmed()) == 1) {
                throw new RuntimeException("出库回单已确认，不能重复上传");
            }
            return;
        }
        if (FILE_TYPE_OUTBOUND_ATTACHMENT.equals(normalizedType)) {
            if (portalMode) {
                throw new RuntimeException("出库附件仅支持内部人员上传");
            }
            return;
        }
        if (FILE_TYPE_OUTBOUND_BATCH_BANK.equals(normalizedType)) {
            if (portalMode) {
                throw new RuntimeException("二批来款水单仅支持内部人员上传");
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
        if (FILE_TYPE_OUTBOUND_RECEIPT.equals(normalizedType)) {
            if (portalMode) {
                throw new RuntimeException("出库回单仅支持内部删除");
            }
            if (this.defaultFlag(order.getOutboundReceiptConfirmed()) == 1) {
                throw new RuntimeException("出库回单已确认，不能删除");
            }
            return;
        }
        if (FILE_TYPE_OUTBOUND_ATTACHMENT.equals(normalizedType)) {
            if (portalMode) {
                throw new RuntimeException("出库附件仅支持内部删除");
            }
            return;
        }
        if (FILE_TYPE_OUTBOUND_BATCH_BANK.equals(normalizedType)) {
            if (portalMode) {
                throw new RuntimeException("二批来款水单仅支持内部删除");
            }
            return;
        }
        throw new RuntimeException("不支持的附件类型");
    }

    private void validateBatchFileEditable(ErpSaleOrderFileEntity file) {
        if (file == null || file.getBatchId() == null) {
            return;
        }
        ErpSaleOutboundBatchEntity batch = (ErpSaleOutboundBatchEntity)this.erpSaleOutboundBatchDao.selectById(file.getBatchId());
        if (batch == null) {
            return;
        }
        if (this.defaultInt(batch.getStatus()) == OUTBOUND_BATCH_CONFIRMED) {
            throw new RuntimeException("出库批次已确认，不能删除批次附件");
        }
        if (this.defaultInt(batch.getStatus()) == OUTBOUND_BATCH_VOID) {
            throw new RuntimeException("出库批次已作废，不能删除批次附件");
        }
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
        } else if (FILE_TYPE_OUTBOUND_RECEIPT.equals(normalizedType)) {
            this.validateOutboundReceiptMatched(order.getId());
            order.setOutboundReceiptConfirmed(1);
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
            if (portalMode) {
                throw new RuntimeException("二批打款凭证已移至内部出库批次来款水单流程");
            }
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
        if (FILE_TYPE_OUTBOUND_RECEIPT.equals(fileType)) {
            if (portalMode) {
                throw new RuntimeException("出库回单仅支持内部确认");
            }
            if (this.defaultFlag(order.getOutboundReceiptConfirmed()) == 1) {
                throw new RuntimeException("出库回单已确认");
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
        } else if (FILE_TYPE_OUTBOUND_RECEIPT.equals(normalizedType) && this.defaultFlag(order.getOutboundReceiptConfirmed()) != 0) {
            order.setOutboundReceiptConfirmed(0);
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
        return this.nextFileLineNo(saleOrderId, fileType, null);
    }

    private int nextFileLineNo(Long saleOrderId, String fileType, Long batchId) {
        QueryWrapper<ErpSaleOrderFileEntity> wrapper = (QueryWrapper<ErpSaleOrderFileEntity>)((QueryWrapper)((QueryWrapper)new QueryWrapper().eq((Object)"sale_order_id", (Object)saleOrderId)).eq((Object)"file_type", (Object)fileType));
        if (batchId == null) {
            wrapper.isNull("batch_id");
        } else {
            wrapper.eq("batch_id", batchId);
        }
        List files = this.erpSaleOrderFileDao.selectList((Wrapper)wrapper.orderByDesc("line_no", "id").last("limit 1"));
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

    private Integer parseInteger(String value) {
        if (StringUtils.isBlank((String)value)) {
            return 0;
        }
        try {
            return new BigDecimal(value.replace(",", "").trim()).intValue();
        }
        catch (Exception ex) {
            return 0;
        }
    }

    private BigDecimal parseDecimal(String value) {
        if (StringUtils.isBlank((String)value)) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(value.replace(",", "").trim());
        }
        catch (Exception ex) {
            return BigDecimal.ZERO;
        }
    }

    private Date parseDateTime(String value) {
        if (StringUtils.isBlank((String)value)) {
            return null;
        }
        for (String pattern : new String[]{"yyyy-MM-dd HH:mm:ss", "yyyy/MM/dd HH:mm:ss", "yyyy-MM-dd"}) {
            try {
                return new SimpleDateFormat(pattern).parse(value.trim());
            }
            catch (Exception ex) {
                // try next pattern
            }
        }
        return null;
    }

    private int defaultFlag(Integer value) {
        return value == null ? 0 : value;
    }

    private BigDecimal defaultDecimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
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

    private String formatDecimal(BigDecimal value) {
        return this.defaultDecimal(value).setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private String formatDate(Date date) {
        if (date == null) {
            return "-";
        }
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }

    private static class ByteArrayMultipartFile implements MultipartFile {
        private final String name;
        private final String originalFilename;
        private final String contentType;
        private final byte[] content;

        private ByteArrayMultipartFile(MultipartFile source, byte[] content) {
            this.name = source == null ? "file" : source.getName();
            this.originalFilename = source == null ? null : source.getOriginalFilename();
            this.contentType = source == null ? null : source.getContentType();
            this.content = content == null ? new byte[0] : content;
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public String getOriginalFilename() {
            return this.originalFilename;
        }

        @Override
        public String getContentType() {
            return this.contentType;
        }

        @Override
        public boolean isEmpty() {
            return this.content.length == 0;
        }

        @Override
        public long getSize() {
            return this.content.length;
        }

        @Override
        public byte[] getBytes() {
            return this.content;
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(this.content);
        }

        @Override
        public void transferTo(File dest) throws IOException {
            Files.write(dest.toPath(), this.content);
        }
    }

    private static class StockCandidate {
        private ErpInboundOrderEntity inboundOrder;
        private ErpInboundOrderItemEntity inboundItem;
        private ErpProductEntity product;
        private int availableBoxes;
        private String ownershipName;

        private StockCandidate() {
        }
    }

    private static class OwnershipState {
        private boolean hasPayment;
        private boolean hasUnsettledFunder;
        private String funderName;

        private OwnershipState() {
        }
    }
}
