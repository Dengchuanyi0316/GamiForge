package com.forge.gami.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Value("${upload.base.path}")
    private String uploadBasePath;
    // 配置 CORS
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 所有路径
                .allowedOriginPatterns("*") // 允许所有源（可指定 http://localhost:5173）
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowCredentials(true) // 允许携带 cookie
                .allowedHeaders("*")
                .maxAge(3600);
    }
    // 配置静态资源映射
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 把 /files/** 映射到 D:/file-self/ 目录
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:"+uploadBasePath + "/");
        // 添加音乐文件映射: /music/ -> D:/file-self/music/
        registry.addResourceHandler("/music/**")
                .addResourceLocations("file:"+uploadBasePath + "/music/");
    }

}
