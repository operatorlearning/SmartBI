package com.smartbi.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartbi.common.BusinessException;
import com.smartbi.common.ErrorCode;
import com.smartbi.common.PageResult;
import com.smartbi.dto.DatasetQueryDTO;
import com.smartbi.entity.Dataset;
import com.smartbi.mapper.DatasetMapper;
import com.smartbi.service.DatasetService;
import com.smartbi.service.UserService;
import com.smartbi.util.ExcelUtils;
import com.smartbi.vo.DatasetVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 数据集服务实现
 * <p>
 * 技术亮点：EasyExcel SAX 模式流式解析百万级数据
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DatasetServiceImpl extends ServiceImpl<DatasetMapper, Dataset> implements DatasetService {

    private final UserService userService;

    @Override
    public DatasetVO uploadExcel(MultipartFile file, String name, String description) {
        // 1. 校验文件
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请选择文件");
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null ||
                (!originalFilename.endsWith(".xlsx") && !originalFilename.endsWith(".xls") && !originalFilename.endsWith(".csv"))) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "仅支持 Excel(.xlsx/.xls) 和 CSV 文件");
        }

        // 2. 使用 EasyExcel SAX 模式解析
        ExcelUtils.ExcelParseResult parseResult;
        try {
            parseResult = ExcelUtils.parseExcel(file);
        } catch (Exception e) {
            log.error("Excel解析失败: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.DATASET_PARSE_ERROR, "文件解析失败: " + e.getMessage());
        }

        if (parseResult.getRows().isEmpty()) {
            throw new BusinessException(ErrorCode.DATASET_EMPTY);
        }

        // 3. 转成 CSV 格式存储
        String csvData = ExcelUtils.toCsv(parseResult.getHeaders(), parseResult.getRows());
        String columnsInfo = JSONUtil.toJsonStr(parseResult.getColumnInfoList());

        // 4. 保存数据集记录
        Long userId = userService.getCurrentUserId();
        Dataset dataset = new Dataset();
        dataset.setUserId(userId);
        dataset.setName(StringUtils.hasText(name) ? name : originalFilename);
        dataset.setDescription(description);
        dataset.setFileName(originalFilename);
        dataset.setColumnsInfo(columnsInfo);
        dataset.setRowCount(parseResult.getRows().size());
        dataset.setRawData(csvData);
        dataset.setSize(file.getSize());
        dataset.setStatus(1);
        save(dataset);

        log.info("数据集上传成功: id={}, name={}, rows={}", dataset.getId(), dataset.getName(), dataset.getRowCount());

        // 5. 返回 VO
        DatasetVO vo = new DatasetVO();
        BeanUtils.copyProperties(dataset, vo);
        return vo;
    }

    @Override
    public PageResult<DatasetVO> listMyDatasets(DatasetQueryDTO dto) {
        Long userId = userService.getCurrentUserId();

        LambdaQueryWrapper<Dataset> wrapper = new LambdaQueryWrapper<Dataset>()
                .eq(Dataset::getUserId, userId)
                .like(StringUtils.hasText(dto.getName()), Dataset::getName, dto.getName())
                .orderByDesc(Dataset::getCreateTime);

        Page<Dataset> page = page(new Page<>(dto.getCurrent(), dto.getPageSize()), wrapper);

        // 转 VO（不返回 rawData，减少传输量）
        Page<DatasetVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        List<DatasetVO> voList = page.getRecords().stream().map(d -> {
            DatasetVO vo = new DatasetVO();
            BeanUtils.copyProperties(d, vo);
            return vo;
        }).toList();
        voPage.setRecords(voList);

        return PageResult.of(voPage);
    }

    @Override
    public DatasetVO getDatasetDetail(Long id) {
        Dataset dataset = getById(id);
        if (dataset == null) {
            throw new BusinessException(ErrorCode.DATASET_NOT_FOUND);
        }
        // 校验权限（只能查看自己的数据集）
        Long userId = userService.getCurrentUserId();
        if (!dataset.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        DatasetVO vo = new DatasetVO();
        BeanUtils.copyProperties(dataset, vo);
        return vo;
    }

    @Override
    public void deleteDataset(Long id) {
        Dataset dataset = getById(id);
        if (dataset == null) {
            throw new BusinessException(ErrorCode.DATASET_NOT_FOUND);
        }
        Long userId = userService.getCurrentUserId();
        if (!dataset.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        removeById(id);
        log.info("数据集删除: id={}", id);
    }
}
