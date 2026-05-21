package io.renren.modules.erp.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.renren.modules.erp.entity.ErpProductEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ErpProductDao extends BaseMapper<ErpProductEntity> {
}
