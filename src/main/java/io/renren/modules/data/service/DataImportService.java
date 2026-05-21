package io.renren.modules.data.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.renren.common.utils.PageUtils;
import io.renren.modules.data.entity.DataImportEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 
 *
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2023-12-20 18:59:34
 */
public interface DataImportService extends IService<DataImportEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void insertBatch(List<DataImportEntity> dataImportList);

    void delDataImport(Long createUserId,String date);
}

