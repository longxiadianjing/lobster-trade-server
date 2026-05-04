package com.lobster.trade.controller;

import com.lobster.trade.model.response.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
public class FileUploadController {

    @Value("${upload.path:D:/uploads/lobster}")
    private String uploadPath;

    @Value("${upload.base-url:http://localhost:8080/uploads}")
    private String baseUrl;

    @PostMapping
    public ApiResponse<String> upload(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ApiResponse.fail("请选择文件");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ApiResponse.fail("只能上传图片文件");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            return ApiResponse.fail("文件大小不能超过 5MB");
        }

        String dateDir = new SimpleDateFormat("yyyyMMdd").format(new Date());
        Path dirPath = Paths.get(uploadPath, dateDir);
        Files.createDirectories(dirPath);

        String originalFilename = file.getOriginalFilename();
        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String fileName = UUID.randomUUID().toString().replace("-", "") + ext;

        Path filePath = dirPath.resolve(fileName);
        file.transferTo(filePath.toFile());

        String url = baseUrl + "/" + dateDir + "/" + fileName;
        return new ApiResponse<>(200, "上传成功", url);
    }
}
