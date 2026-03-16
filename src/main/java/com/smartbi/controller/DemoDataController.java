package com.smartbi.controller;

import com.smartbi.common.Result;
import com.smartbi.service.DatasetService;
import com.smartbi.vo.DatasetVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 示例数据集接口 — 提供预置的Excel示例文件供用户体验
 */
@Tag(name = "示例数据")
@Slf4j
@RestController
@RequestMapping("/demo")
@RequiredArgsConstructor
public class DemoDataController {

    private final DatasetService datasetService;

    /** 示例文件元信息 */
    private static final List<Map<String, String>> DEMO_META = List.of(
            Map.of("fileName", "电商平台月度销售数据.xlsx",
                    "name", "电商月度销售数据",
                    "description", "2025全年5大品类月度销售量、销售额及退货率，适合折线图/柱状图/饼图分析",
                    "icon", "🛒", "color", "#f56c6c",
                    "tags", "电商,销售,趋势",
                    "rows", "60", "cols", "5"),
            Map.of("fileName", "公司员工薪资与绩效分析.xlsx",
                    "name", "员工薪资绩效分析",
                    "description", "80名员工的部门、职级、薪资与绩效评分，适合散点图/柱状图分析",
                    "icon", "👥", "color", "#409eff",
                    "tags", "HR,薪资,绩效",
                    "rows", "80", "cols", "8"),
            Map.of("fileName", "网站流量与用户行为数据.xlsx",
                    "name", "网站流量分析",
                    "description", "90天PV/UV/新用户/跳出率/下单数据，适合面积图/折线图分析",
                    "icon", "🌐", "color", "#67c23a",
                    "tags", "流量,用户,转化",
                    "rows", "90", "cols", "7"),
            Map.of("fileName", "各城市空气质量指数AQI.xlsx",
                    "name", "城市AQI空气质量",
                    "description", "10大城市12个月AQI/PM2.5/PM10数据，适合热力图/雷达图分析",
                    "icon", "🏙️", "color", "#e6a23c",
                    "tags", "环境,AQI,城市",
                    "rows", "120", "cols", "7"),
            Map.of("fileName", "学生成绩分析数据.xlsx",
                    "name", "学生成绩分析",
                    "description", "3个班级120名学生五科成绩，适合雷达图/箱线图/柱状图分析",
                    "icon", "🎓", "color", "#9b59b6",
                    "tags", "教育,成绩,班级",
                    "rows", "120", "cols", "10")
    );

    @GetMapping("/list")
    @Operation(summary = "获取示例数据集列表")
    public Result<List<Map<String, String>>> list() {
        // 过滤掉文件不存在的
        List<Map<String, String>> result = DEMO_META.stream()
                .filter(m -> getDemoFile(m.get("fileName")).exists())
                .collect(Collectors.toList());
        return Result.success(result);
    }

    @GetMapping("/download/{fileName}")
    @Operation(summary = "下载示例Excel文件")
    public ResponseEntity<Resource> download(@PathVariable String fileName) throws IOException {
        File file = getDemoFile(fileName);
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }
        String encodedName = URLEncoder.encode(file.getName(), StandardCharsets.UTF_8).replace("+", "%20");
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedName)
                .body(new FileSystemResource(file));
    }

    @PostMapping("/import/{fileName}")
    @Operation(summary = "将示例数据导入为我的数据集")
    public Result<DatasetVO> importAsDataset(@PathVariable String fileName) throws IOException {
        File file = getDemoFile(fileName);
        if (!file.exists()) {
            return Result.error(404, "示例文件不存在: " + fileName);
        }
        // 构造 MultipartFile 调用已有上传逻辑
        byte[] content = Files.readAllBytes(file.toPath());
        MultipartFile multipartFile = new InMemoryMultipartFile(
                "file", file.getName(),
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                content);
            // 找到对应的 name 和 description
            String name = DEMO_META.stream()
                    .filter(m -> m.get("fileName").equals(fileName))
                    .map(m -> m.get("name"))
                    .findFirst().orElse(fileName);
            String desc = DEMO_META.stream()
                    .filter(m -> m.get("fileName").equals(fileName))
                    .map(m -> m.get("description"))
                    .findFirst().orElse("");
            DatasetVO vo = datasetService.uploadExcel(multipartFile, name, desc);
            return Result.success(vo);
    }

    /** 获取 demo-data 目录下的文件 */
    private File getDemoFile(String fileName) {
        // 优先查找项目根目录/demo-data（开发环境）
        File f = new File("demo-data", fileName);
        if (f.exists()) return f;
        // 其次查找 classpath:demo-data/（jar部署时）
        String userDir = System.getProperty("user.dir");
        return new File(userDir + "/demo-data", fileName);
    }

    /** 简易 MultipartFile 内存实现，用于将本地文件转为 MultipartFile 供 Service 调用 */
    private static class InMemoryMultipartFile implements MultipartFile {
        private final String name;
        private final String originalFilename;
        private final String contentType;
        private final byte[] content;

        InMemoryMultipartFile(String name, String originalFilename, String contentType, byte[] content) {
            this.name = name;
            this.originalFilename = originalFilename;
            this.contentType = contentType;
            this.content = content;
        }

        @Override public String getName() { return name; }
        @Override public String getOriginalFilename() { return originalFilename; }
        @Override public String getContentType() { return contentType; }
        @Override public boolean isEmpty() { return content.length == 0; }
        @Override public long getSize() { return content.length; }
        @Override public byte[] getBytes() { return content; }
        @Override public InputStream getInputStream() { return new ByteArrayInputStream(content); }
        @Override public void transferTo(File dest) throws IOException {
            Files.write(dest.toPath(), content);
        }
    }
}
