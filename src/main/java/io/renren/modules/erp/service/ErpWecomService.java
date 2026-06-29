package io.renren.modules.erp.service;

import io.renren.modules.erp.entity.ErpShipNoticeEntity;
import io.renren.modules.erp.entity.ErpSaleUploadNoticeEntity;
import io.renren.modules.erp.entity.ErpWecomGroupEntity;
import io.renren.modules.erp.entity.ErpPartnerEntity;
import java.util.Date;
import java.util.List;

public interface ErpWecomService {
  List<ErpWecomGroupEntity> syncGroups();

  List<ErpWecomGroupEntity> selectGroups(String keyword);

  ErpShipNoticeEntity sendShipNotice(Long presaleOrderId, Long partnerId, String content, Long userId);

  List<ErpShipNoticeEntity> sendShipNotice(Long presaleOrderId, List<Long> partnerIds, String content, Long userId);

  List<ErpShipNoticeEntity> autoSendShipNoticeToLinkedFutures(Long presaleOrderId, Long userId);

  List<ErpPartnerEntity> selectArrivalNoticePartners(Long confirmId);

  List<ErpShipNoticeEntity> sendArrivalNotice(Long confirmId, List<Long> partnerIds, Date actualArrivalDate, String content, Long userId);

  ErpSaleUploadNoticeEntity sendSaleUploadNotice(Long saleOrderId, boolean force, Long userId);

  ErpSaleUploadNoticeEntity autoSendSaleUploadNotice(Long saleOrderId, Long userId);
}
