package io.renren.modules.erp.service;

import io.renren.modules.erp.entity.ErpShipNoticeEntity;
import io.renren.modules.erp.entity.ErpWecomGroupEntity;
import java.util.List;

public interface ErpWecomService {
  List<ErpWecomGroupEntity> syncGroups();

  List<ErpWecomGroupEntity> selectGroups(String keyword);

  ErpShipNoticeEntity sendShipNotice(Long presaleOrderId, Long partnerId, String content, Long userId);

  List<ErpShipNoticeEntity> sendShipNotice(Long presaleOrderId, List<Long> partnerIds, String content, Long userId);

  List<ErpShipNoticeEntity> autoSendShipNoticeToLinkedFutures(Long presaleOrderId, Long userId);
}
