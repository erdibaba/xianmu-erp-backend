package io.renren.modules.data.dao;

import io.renren.modules.data.entity.DataImportEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 
 * 
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2023-12-20 18:59:34
 */
@Mapper
public interface DataImportDao extends BaseMapper<DataImportEntity> {

    //批量新增
    void insertBatch(List<DataImportEntity> list);

    void delDataImport(@Param("createUserId")Long createUserId,@Param("date")String date);
}
