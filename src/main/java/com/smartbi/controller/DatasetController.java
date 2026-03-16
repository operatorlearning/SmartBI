package com.smartbi.controller;

import com.smartbi.annotation.OperLog;
import com.smartbi.common.PageResult;
import com.smartbi.common.Result;
import com.smartbi.dto.DatasetQueryDTO;
import com.smartbi.service.DatasetService;
import com.smartbi.vo.DatasetVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 数据集接口 — Excel 上传与管理
 */
@Tag(name = "数据集管理")
@RestController
@RequestMapping("/dataset")
@RequiredArgsConstructor
public class DatasetController {

    private final DatasetService datasetService;

    @PostMapping("/upload")
    @Operation(summary = "上传Excel文件创建数据集")
    @OperLog("上传数据集")
    public Result<DatasetVO> upload(@RequestParam("file") MultipartFile file,
                                    @RequestParam(required = false) String name,
                                    @RequestParam(required = false) String description) {
        return Result.success(datasetService.uploadExcel(file, name, description));
    }

    @GetMapping("/list")
    @Operation(summary = "分页查询我的数据集")
    public Result<PageResult<DatasetVO>> list(DatasetQueryDTO dto) {
        return Result.success(datasetService.listMyDatasets(dto));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取数据集详情")
    public Result<DatasetVO> detail(@PathVariable Long id) {
        return Result.success(datasetService.getDatasetDetail(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除数据集")
    @OperLog("删除数据集")
    public Result<Void> delete(@PathVariable Long id) {
        datasetService.deleteDataset(id);
        return Result.success();
    }
}
