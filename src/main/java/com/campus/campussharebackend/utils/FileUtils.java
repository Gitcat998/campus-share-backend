package com.campus.campussharebackend.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Component
public class FileUtils {

    // 从配置文件读取上传路径（建议配置在application.yml）
    @Value("${file.upload.path}")
    private String uploadPath;

    // 图片访问基础URL（如"http://localhost:8080/images/"）
    @Value("${file.access.url}")
    private String accessUrl;

    /**
     * 上传文件并返回访问URL
     */
    public String uploadFile(MultipartFile file) {
        if (file.isEmpty()) {
            return null;
        }

        try {
            // 1. 生成唯一文件名（避免重名）
            String originalFilename = file.getOriginalFilename();
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = UUID.randomUUID().toString() + suffix;

            // 2. 创建上传目录（若不存在）
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // 3. 保存文件到服务器
            File destFile = new File(uploadPath + File.separator + fileName);
            file.transferTo(destFile);

            // 4. 返回可访问的URL
            return accessUrl + fileName;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}