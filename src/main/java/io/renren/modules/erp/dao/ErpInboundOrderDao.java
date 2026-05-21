package io.renren.modules.erp.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.renren.modules.erp.entity.ErpInboundOrderEntity;
import io.renren.modules.erp.vo.ErpInboundListVo;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ErpInboundOrderDao extends BaseMapper<ErpInboundOrderEntity> {
  List<ErpInboundListVo> queryReadyPresaleList(IPage<ErpInboundListVo> page, @Param("keyword") String keyword);
}
