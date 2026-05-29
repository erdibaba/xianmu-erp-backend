package io.renren.modules.erp.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.renren.modules.erp.dao.ErpPartnerDao;
import io.renren.modules.erp.dao.ErpPresaleConfirmDao;
import io.renren.modules.erp.dao.ErpPresaleOrderDao;
import io.renren.modules.erp.dao.ErpSaleOrderDao;
import io.renren.modules.erp.dao.ErpSaleUploadNoticeDao;
import io.renren.modules.erp.dao.ErpShipNoticeDao;
import io.renren.modules.erp.dao.ErpWecomGroupDao;
import io.renren.modules.erp.entity.ErpPartnerEntity;
import io.renren.modules.erp.entity.ErpPresaleConfirmEntity;
import io.renren.modules.erp.entity.ErpPresaleOrderEntity;
import io.renren.modules.erp.entity.ErpSaleOrderEntity;
import io.renren.modules.erp.entity.ErpSaleUploadNoticeEntity;
import io.renren.modules.erp.entity.ErpShipNoticeEntity;
import io.renren.modules.erp.entity.ErpWecomGroupEntity;
import io.renren.modules.erp.service.ErpWecomService;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service("erpWecomService")
public class ErpWecomServiceImpl implements ErpWecomService {
  private static final String WECOM_API = "https://qyapi.weixin.qq.com";
  private static final String SALE_CONTRACT_BASE_URL = "http://218.202.240.118:8888/renren-fast/erp/saleorder/contract/";
  private static final String SALE_UPLOAD_PORTAL_BASE_URL = "http://218.202.240.118:3001/#/sale-upload/";

  @Value("${erp.wecom.corp-id:}")
  private String corpId;

  @Value("${erp.wecom.corp-secret:}")
  private String corpSecret;

  @Value("${erp.wecom.default-sender:}")
  private String defaultSender;

  @Autowired
  private ErpWecomGroupDao erpWecomGroupDao;
  @Autowired
  private ErpShipNoticeDao erpShipNoticeDao;
  @Autowired
  private ErpPartnerDao erpPartnerDao;
  @Autowired
  private ErpPresaleOrderDao erpPresaleOrderDao;
  @Autowired
  private ErpPresaleConfirmDao erpPresaleConfirmDao;
  @Autowired
  private ErpSaleOrderDao erpSaleOrderDao;
  @Autowired
  private ErpSaleUploadNoticeDao erpSaleUploadNoticeDao;

  private final RestTemplate restTemplate = new RestTemplate();
  private String cachedAccessToken;
  private long tokenExpireAt;

  @Override
  public List<ErpWecomGroupEntity> syncGroups() {
    List<ErpWecomGroupEntity> groups = new ArrayList<>();
    String cursor = null;
    do {
      JSONObject body = new JSONObject();
      body.put("status_filter", 0);
      body.put("limit", 100);
      if (StringUtils.isNotBlank(cursor)) {
        body.put("cursor", cursor);
      }
      JSONObject response = post("/cgi-bin/externalcontact/groupchat/list", body);
      JSONArray list = response.getJSONArray("group_chat_list");
      if (list != null) {
        for (int i = 0; i < list.size(); i++) {
          JSONObject item = list.getJSONObject(i);
          ErpWecomGroupEntity group = syncGroupDetail(item.getString("chat_id"), item.getInteger("status"));
          if (group != null) {
            groups.add(group);
          }
        }
      }
      cursor = response.getString("next_cursor");
    } while (StringUtils.isNotBlank(cursor));
    return groups;
  }

  @Override
  public List<ErpWecomGroupEntity> selectGroups(String keyword) {
    QueryWrapper<ErpWecomGroupEntity> wrapper = new QueryWrapper<ErpWecomGroupEntity>().orderByDesc("sync_time").last("limit 100");
    if (StringUtils.isNotBlank(keyword)) {
      wrapper.and(w -> w.like("group_name", keyword).or().like("owner", keyword).or().like("chat_id", keyword));
    }
    return erpWecomGroupDao.selectList(wrapper);
  }

  @Override
  public ErpShipNoticeEntity sendShipNotice(Long presaleOrderId, Long partnerId, String content, Long userId) {
    List<Long> partnerIds = new ArrayList<>();
    partnerIds.add(partnerId);
    List<ErpShipNoticeEntity> notices = sendShipNotice(presaleOrderId, partnerIds, content, userId);
    return notices.isEmpty() ? null : notices.get(0);
  }

  @Override
  public List<ErpShipNoticeEntity> sendShipNotice(Long presaleOrderId, List<Long> partnerIds, String content, Long userId) {
    if (presaleOrderId == null || presaleOrderId <= 0) {
      throw new RuntimeException("请选择预销售单");
    }
    if (partnerIds == null || partnerIds.isEmpty()) {
      throw new RuntimeException("请选择二批商");
    }
    ErpPresaleOrderEntity presale = erpPresaleOrderDao.selectById(presaleOrderId);
    if (presale == null) {
      throw new RuntimeException("预销售单不存在");
    }
    ErpPresaleConfirmEntity confirm = erpPresaleConfirmDao.selectOne(new QueryWrapper<ErpPresaleConfirmEntity>()
        .eq("presale_order_id", presaleOrderId).last("limit 1"));
    if (confirm == null || confirm.getExpectedArrivalDate() == null) {
      throw new RuntimeException("请先上传客户订单确认函并维护预计到港时间");
    }
    List<ErpShipNoticeEntity> notices = new ArrayList<>();
    for (Long partnerId : partnerIds) {
      if (partnerId == null || partnerId <= 0) {
        continue;
      }
      notices.add(sendShipNoticeToPartner(presale, confirm, partnerId, content, userId));
    }
    if (notices.isEmpty()) {
      throw new RuntimeException("请选择有效二批商");
    }
    return notices;
  }

  @Override
  public List<ErpShipNoticeEntity> autoSendShipNoticeToLinkedFutures(Long presaleOrderId, Long userId) {
    List<ErpShipNoticeEntity> notices = new ArrayList<>();
    if (presaleOrderId == null || presaleOrderId <= 0) {
      return notices;
    }
    List<ErpSaleOrderEntity> saleOrders = erpSaleOrderDao.selectList(new QueryWrapper<ErpSaleOrderEntity>()
        .eq("sale_type", "FUTURES")
        .eq("source_presale_order_id", presaleOrderId)
        .isNotNull("secondary_partner_id"));
    Set<Long> partnerIds = new LinkedHashSet<>();
    for (ErpSaleOrderEntity saleOrder : saleOrders) {
      if (saleOrder.getSecondaryPartnerId() != null) {
        partnerIds.add(saleOrder.getSecondaryPartnerId());
      }
    }
    for (Long partnerId : partnerIds) {
      try {
        ErpShipNoticeEntity notice = sendShipNotice(presaleOrderId, partnerId, null, userId);
        if (notice != null) {
          notices.add(notice);
        }
      } catch (Exception ignored) {
        // 自动船期通知不能影响确认函保存或销售单关联主流程。
      }
    }
    return notices;
  }

  @Override
  public ErpSaleUploadNoticeEntity sendSaleUploadNotice(Long saleOrderId, boolean force, Long userId) {
    if (saleOrderId == null || saleOrderId <= 0) {
      throw new RuntimeException("请选择销售单");
    }
    ErpSaleOrderEntity order = erpSaleOrderDao.selectById(saleOrderId);
    if (order == null) {
      throw new RuntimeException("销售单不存在");
    }
    if (!force) {
      ErpSaleUploadNoticeEntity existing = findExistingSuccessSaleUploadNotice(order.getId());
      if (existing != null) {
        return existing;
      }
    }
    try {
      return sendSaleUploadNoticeToPartner(order, userId);
    } catch (RuntimeException e) {
      saveFailedSaleUploadNotice(order, e.getMessage(), userId);
      throw e;
    }
  }

  @Override
  public ErpSaleUploadNoticeEntity autoSendSaleUploadNotice(Long saleOrderId, Long userId) {
    try {
      return sendSaleUploadNotice(saleOrderId, false, userId);
    } catch (Exception ignored) {
      return null;
    }
  }

  private ErpShipNoticeEntity sendShipNoticeToPartner(ErpPresaleOrderEntity presale, ErpPresaleConfirmEntity confirm, Long partnerId, String content, Long userId) {
    ErpPartnerEntity partner = erpPartnerDao.selectById(partnerId);
    if (partner == null) {
      throw new RuntimeException("二批商不存在");
    }
    if (StringUtils.isBlank(partner.getWecomChatId())) {
      throw new RuntimeException("该二批商未绑定企业微信客户群");
    }
    String sender = StringUtils.defaultIfBlank(partner.getWecomChatOwner(), defaultSender);
    if (StringUtils.isBlank(sender)) {
      throw new RuntimeException("请配置企业微信群主或默认发送人");
    }
    ErpShipNoticeEntity existing = findExistingSuccessNotice(presale.getId(), partnerId, confirm.getExpectedArrivalDate());
    if (existing != null) {
      return existing;
    }

    String noticeContent = StringUtils.defaultIfBlank(content, buildShipNoticeContent(presale, confirm, partner));
    JSONObject text = new JSONObject();
    text.put("content", noticeContent);
    JSONObject body = new JSONObject();
    body.put("chat_type", "group");
    body.put("sender", sender);
    body.put("allow_select", false);
    JSONArray chatIds = new JSONArray();
    chatIds.add(partner.getWecomChatId());
    body.put("chat_id_list", chatIds);
    body.put("text", text);
    JSONObject response = post("/cgi-bin/externalcontact/add_msg_template", body);

    Date now = new Date();
    ErpShipNoticeEntity notice = new ErpShipNoticeEntity();
    notice.setPresaleOrderId(presale.getId());
    notice.setPartnerId(partnerId);
    notice.setPartnerName(partner.getPartnerName());
    notice.setChatId(partner.getWecomChatId());
    notice.setChatName(partner.getWecomChatName());
    notice.setSender(sender);
    notice.setContractNo(firstNonBlank(confirm.getContractNo(), presale.getSellerContractNo(), presale.getOrderNo()));
    notice.setContainerNo(confirm.getContainerNo());
    notice.setExpectedArrivalDate(confirm.getExpectedArrivalDate());
    notice.setContent(noticeContent);
    notice.setWecomMsgId(response.getString("msgid"));
    notice.setStatus(1);
    notice.setErrorMessage(response.getString("errmsg"));
    notice.setCreateUserId(userId);
    notice.setCreateTime(now);
    notice.setUpdateTime(now);
    erpShipNoticeDao.insert(notice);
    return notice;
  }

  private ErpSaleUploadNoticeEntity sendSaleUploadNoticeToPartner(ErpSaleOrderEntity order, Long userId) {
    if (order.getSecondaryPartnerId() == null) {
      throw new RuntimeException("销售单未关联二批商");
    }
    ErpPartnerEntity partner = erpPartnerDao.selectById(order.getSecondaryPartnerId());
    if (partner == null) {
      throw new RuntimeException("二批商不存在");
    }
    if (StringUtils.isBlank(partner.getWecomChatId())) {
      throw new RuntimeException("该二批商未绑定企业微信客户群");
    }
    String sender = StringUtils.defaultIfBlank(partner.getWecomChatOwner(), defaultSender);
    if (StringUtils.isBlank(sender)) {
      throw new RuntimeException("请配置企业微信群主或默认发送人");
    }
    if (StringUtils.isBlank(order.getContractToken())) {
      throw new RuntimeException("销售单上传链接不存在");
    }
    String portalUrl = SALE_CONTRACT_BASE_URL + order.getContractToken();
    String noticeContent = buildSaleUploadNoticeContent(order, partner, portalUrl);
    JSONObject text = new JSONObject();
    text.put("content", noticeContent);
    JSONObject body = new JSONObject();
    body.put("chat_type", "group");
    body.put("sender", sender);
    body.put("allow_select", false);
    JSONArray chatIds = new JSONArray();
    chatIds.add(partner.getWecomChatId());
    body.put("chat_id_list", chatIds);
    body.put("text", text);
    JSONObject response = post("/cgi-bin/externalcontact/add_msg_template", body);

    Date now = new Date();
    ErpSaleUploadNoticeEntity notice = buildSaleUploadNotice(order, partner, sender, portalUrl, noticeContent, userId, now);
    notice.setWecomMsgId(response.getString("msgid"));
    notice.setStatus(1);
    notice.setErrorMessage(response.getString("errmsg"));
    erpSaleUploadNoticeDao.insert(notice);
    return notice;
  }

  private ErpSaleUploadNoticeEntity buildSaleUploadNotice(ErpSaleOrderEntity order, ErpPartnerEntity partner, String sender, String portalUrl, String content, Long userId, Date now) {
    ErpSaleUploadNoticeEntity notice = new ErpSaleUploadNoticeEntity();
    notice.setSaleOrderId(order.getId());
    notice.setOrderNo(order.getOrderNo());
    notice.setContractNo(order.getContractNo());
    notice.setPartnerId(partner == null ? order.getSecondaryPartnerId() : partner.getId());
    notice.setPartnerName(partner == null ? order.getSecondaryPartnerName() : partner.getPartnerName());
    notice.setChatId(partner == null ? null : partner.getWecomChatId());
    notice.setChatName(partner == null ? null : partner.getWecomChatName());
    notice.setSender(sender);
    notice.setPortalUrl(portalUrl);
    notice.setContent(content);
    notice.setCreateUserId(userId);
    notice.setCreateTime(now);
    notice.setUpdateTime(now);
    return notice;
  }

  private ErpSaleUploadNoticeEntity findExistingSuccessSaleUploadNotice(Long saleOrderId) {
    if (saleOrderId == null) {
      return null;
    }
    return erpSaleUploadNoticeDao.selectOne(new QueryWrapper<ErpSaleUploadNoticeEntity>()
        .eq("sale_order_id", saleOrderId)
        .in("status", 1, 2)
        .orderByDesc("create_time", "id")
        .last("limit 1"));
  }

  private void saveFailedSaleUploadNotice(ErpSaleOrderEntity order, String message, Long userId) {
    if (order == null || order.getId() == null) {
      return;
    }
    ErpPartnerEntity partner = order.getSecondaryPartnerId() == null ? null : erpPartnerDao.selectById(order.getSecondaryPartnerId());
    Date now = new Date();
    String portalUrl = StringUtils.isBlank(order.getContractToken()) ? null : SALE_CONTRACT_BASE_URL + order.getContractToken();
    ErpSaleUploadNoticeEntity notice = buildSaleUploadNotice(order, partner, partner == null ? null : StringUtils.defaultIfBlank(partner.getWecomChatOwner(), defaultSender), portalUrl, null, userId, now);
    notice.setStatus(9);
    notice.setErrorMessage(StringUtils.abbreviate(StringUtils.defaultString(message, "发送失败"), 500));
    erpSaleUploadNoticeDao.insert(notice);
  }

  private String buildSaleUploadNoticeContent(ErpSaleOrderEntity order, ErpPartnerEntity partner, String portalUrl) {
    StringBuilder builder = new StringBuilder();
    builder.append("【鲜牧供应链合同确认】\n");
    builder.append("客户：").append(StringUtils.defaultIfBlank(partner.getPartnerName(), "-")).append("\n");
    builder.append("销售单号：").append(StringUtils.defaultIfBlank(order.getOrderNo(), "-")).append("\n");
    builder.append("合同号：").append(StringUtils.defaultIfBlank(order.getContractNo(), "-")).append("\n");
    builder.append("请点击以下链接下载合同，并按流程上传盖章合同、打款凭证：\n");
    builder.append(portalUrl);
    return builder.toString();
  }

  private ErpShipNoticeEntity findExistingSuccessNotice(Long presaleOrderId, Long partnerId, Date expectedArrivalDate) {
    if (presaleOrderId == null || partnerId == null || expectedArrivalDate == null) {
      return null;
    }
    return erpShipNoticeDao.selectOne(new QueryWrapper<ErpShipNoticeEntity>()
        .eq("presale_order_id", presaleOrderId)
        .eq("partner_id", partnerId)
        .eq("expected_arrival_date", expectedArrivalDate)
        .in("status", 1, 2)
        .last("limit 1"));
  }

  private ErpWecomGroupEntity syncGroupDetail(String chatId, Integer status) {
    if (StringUtils.isBlank(chatId)) {
      return null;
    }
    JSONObject body = new JSONObject();
    body.put("chat_id", chatId);
    body.put("need_name", 1);
    JSONObject response = post("/cgi-bin/externalcontact/groupchat/get", body);
    JSONObject detail = response.getJSONObject("group_chat");
    if (detail == null) {
      return null;
    }
    Date now = new Date();
    ErpWecomGroupEntity group = erpWecomGroupDao.selectOne(new QueryWrapper<ErpWecomGroupEntity>().eq("chat_id", chatId).last("limit 1"));
    boolean create = group == null;
    if (create) {
      group = new ErpWecomGroupEntity();
      group.setChatId(chatId);
      group.setCreateTime(now);
    }
    group.setGroupName(detail.getString("name"));
    group.setOwner(detail.getString("owner"));
    group.setGroupStatus(status == null ? detail.getInteger("status") : status);
    JSONArray members = detail.getJSONArray("member_list");
    group.setMemberCount(members == null ? 0 : members.size());
    Long createTime = detail.getLong("create_time");
    group.setGroupCreateTime(createTime == null ? null : new Date(createTime * 1000));
    group.setSyncTime(now);
    group.setUpdateTime(now);
    if (create) {
      erpWecomGroupDao.insert(group);
    } else {
      erpWecomGroupDao.updateById(group);
    }
    return group;
  }

  private String buildShipNoticeContent(ErpPresaleOrderEntity presale, ErpPresaleConfirmEntity confirm, ErpPartnerEntity partner) {
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    StringBuilder builder = new StringBuilder();
    builder.append("【鲜牧供应链船期通知】\n");
    builder.append("二批商：").append(StringUtils.defaultIfBlank(partner.getPartnerName(), "-")).append("\n");
    builder.append("合同号：").append(firstNonBlank(confirm.getContractNo(), presale.getSellerContractNo(), presale.getOrderNo())).append("\n");
    builder.append("预计到港时间：").append(confirm.getExpectedArrivalDate() == null ? "-" : format.format(confirm.getExpectedArrivalDate())).append("\n");
    if (StringUtils.isNotBlank(confirm.getContainerNo())) {
      builder.append("集装箱号：").append(confirm.getContainerNo()).append("\n");
    }
    builder.append("\n请留意后续到港及提货安排。");
    return builder.toString();
  }

  private JSONObject post(String path, JSONObject body) {
    String url = WECOM_API + path + "?access_token=" + getAccessToken();
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));
    HttpEntity<String> request = new HttpEntity<>(body.toJSONString(), headers);
    String result = restTemplate.postForObject(url, request, String.class);
    JSONObject response = JSONObject.parseObject(result);
    Integer errcode = response == null ? null : response.getInteger("errcode");
    if (errcode == null || errcode != 0) {
      throw new RuntimeException("企业微信接口调用失败：" + (response == null ? "无返回" : response.toJSONString()));
    }
    return response;
  }

  private String getAccessToken() {
    long now = System.currentTimeMillis();
    if (StringUtils.isNotBlank(cachedAccessToken) && tokenExpireAt > now + 60000) {
      return cachedAccessToken;
    }
    if (StringUtils.isBlank(corpId) || StringUtils.isBlank(corpSecret)) {
      throw new RuntimeException("请先配置企业微信 CorpID 和 Secret");
    }
    String url = WECOM_API + "/cgi-bin/gettoken?corpid=" + corpId + "&corpsecret=" + corpSecret;
    String result = restTemplate.getForObject(url, String.class);
    JSONObject response = JSONObject.parseObject(result);
    if (response == null || response.getInteger("errcode") == null || response.getInteger("errcode") != 0) {
      throw new RuntimeException("获取企业微信 access_token 失败：" + (response == null ? "无返回" : response.toJSONString()));
    }
    cachedAccessToken = response.getString("access_token");
    Integer expiresIn = response.getInteger("expires_in");
    tokenExpireAt = now + (expiresIn == null ? 7200 : expiresIn) * 1000L;
    return cachedAccessToken;
  }

  private String firstNonBlank(String... values) {
    if (values == null) {
      return "";
    }
    for (String value : values) {
      if (StringUtils.isNotBlank(value)) {
        return value;
      }
    }
    return "";
  }
}
