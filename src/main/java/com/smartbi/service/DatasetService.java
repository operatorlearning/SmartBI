package com.smartbi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartbi.common.PageResult;
import com.smartbi.dto.DatasetQueryDTO;
import com.smartbi.entity.Dataset;
import com.smartbi.vo.DatasetVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 数据集服务接口
 */
public interface DatasetService extends IService<Dataset> {

    /**
     * 上传 Excel 并解析为数据集
     */
    DatasetVO uploadExcel(MultipartFile file, String name, String description);

    /**
     * 分页查询当前用户的数据集
     */
    PageResult<DatasetVO> listMyDatasets(DatasetQueryDTO dto);

    /**
     * 获取数据集详情
     */
    DatasetVO getDatasetDetail(Long id);

    /**
     * 删除数据集
     */
    void deleteDataset(Long id);
}
