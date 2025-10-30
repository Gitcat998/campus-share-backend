package com.campus.campussharebackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    // 注入文件上传路径（与FileUtils和application.yml保持一致）
    @Value("${file.upload.path}")
    private String uploadPath;

    // 配置静态资源映射（图片访问）
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射URL路径"/images/**"到本地文件系统的上传目录
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + uploadPath + "/");
    }


}