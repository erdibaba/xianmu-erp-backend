package io.renren.modules.data.service.impl;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.renren.common.utils.PageUtils;
import io.renren.common.utils.Query;

import io.renren.modules.data.dao.DataImportDao;
import io.renren.modules.data.entity.DataImportEntity;
import io.renren.modules.data.service.DataImportService;


@Service("dataImportService")
public class DataImportServiceImpl extends ServiceImpl<DataImportDao, DataImportEntity> implements DataImportService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<DataImportEntity> page = this.page(
                new Query<DataImportEntity>().getPage(params),
                new QueryWrapper<DataImportEntity>().eq("create_user_id",params.get("createUserId"))
                        .eq("status",1)
        );

        return new PageUtils(page);
    }

    @Override
    public void insertBatch(List<DataImportEntity> list) {
        //开始分为2000个一批插入临时表
        int batchCount = 1999;// 每批commit的个数
        int batchLastIndex = batchCount;// 每批最后一个的下标
        for (int index = 0; index < list.size();) {
            if (batchLastIndex >= list.size()) {
                batchLastIndex = list.size();
                baseMapper.insertBatch(list.subList(index, batchLastIndex));
                break;// 数据插入完毕，退出循环
            } else {
                baseMapper.insertBatch(list.subList(index, batchLastIndex));
                index = batchLastIndex;// 设置下一批下标
                batchLastIndex = index + (batchCount - 1);
            }
        }
    }

    @Override
    public void delDataImport(Long createUserId,String date) {
        baseMapper.delDataImport(createUserId,date);
    }

}