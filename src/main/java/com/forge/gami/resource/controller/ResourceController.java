package com.forge.gami.resource.controller;

import com.forge.gami.resource.dto.ResourceDTO;
import com.forge.gami.resource.model.Resource;
import com.forge.gami.resource.model.Tag;
import com.forge.gami.resource.service.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.nio.file.Files;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/resources")
public class ResourceController {
    @Autowired
    private ResourceService resourceService;

    // 查询所有资源
    @GetMapping
    public List<Resource> getAllResources() {
        return resourceService.getAllResources();
    }

    // 根据ID查询单个资源
    @GetMapping("/{id}")
    public Resource getResource(@PathVariable Integer id) {
        return resourceService.getResourceById(id);
    }

    // 根据分类查询资源
    @GetMapping("/category/{category}")
    public List<Resource> getResourcesByCategory(@PathVariable String category) {
        return resourceService.getResourcesByCategory(category);
    }

    // 资源文件上传接口
    @PostMapping("/upload")
    public Map<String, String> uploadResourceFile(@RequestParam("file") MultipartFile file,
                                                  @RequestParam("folder") String folder) {
        Map<String, String> result = resourceService.uploadResourceFile(file, folder, "resource.jpg");
        System.out.println("资源文件上传返回结果: " + result); // 打印返回结果
        return result;
    }

    // 缩略图上传接口
    @PostMapping("/upload/thumbnail")
    public Map<String, String> uploadThumbnail(@RequestParam("file") MultipartFile file,
                                               @RequestParam("folder") String folder) {
        Map<String, String> result = resourceService.uploadResourceFile(file, folder, "thumbnail.jpg");
        System.out.println("缩略图上传返回结果: " + result); // 打印返回结果
        return result;
    }

    // 新增资源，同时关联标签
    @PostMapping
    public String addResource(@RequestBody Resource resource) {

        // 获取标签 ID 列表
        List<Tag> tags = resource.getTags();

        // 打印传递给 service 方法的参数
        System.out.println("传递给 resourceService.addResource 的 Resource 对象: " + resource);
        System.out.println("传递给 resourceService.addResource 的标签 ID 列表: " + tags);

        int rows = resourceService.addResource(resource, tags);
        return rows > 0 ? "添加成功" : "添加失败";
    }

    // 更新资源信息，同时更新标签
    @PutMapping("/{id}")
    public String updateResource(@PathVariable Integer id, @RequestBody ResourceDTO resourceDTO) {
        Resource resource = resourceDTO.toResource();
        resource.setId(id); // 设置资源ID
        int rows = resourceService.updateResource(resource, resourceDTO.getTagIds());
        return rows > 0 ? "更新成功" : "更新失败";
    }

    // 删除资源
    @DeleteMapping("/{id}")
    public String deleteResource(@PathVariable Integer id) {
        int rows = resourceService.deleteResource(id);
        return rows > 0 ? "删除成功" : "删除失败";
    }

    // 根据资源ID查询文件数量及总大小
    @GetMapping("/{resourceId}/file-info")
    public Map<String, Object> getFileInfo(@PathVariable Integer resourceId) {
        return resourceService.getFileInfoByResourceId(resourceId);
    }

    // 提供压缩文件夹下载
    @GetMapping("/{resourceId}/download-zip")
    public ResponseEntity<org.springframework.core.io.Resource> downloadZip(@PathVariable Integer resourceId) throws IOException {
        org.springframework.core.io.Resource zipFile = resourceService.getResourceZipById(resourceId);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"resource-" + resourceId + ".zip\"");
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentLength(zipFile.contentLength());
        return ResponseEntity.ok()
                .headers(headers)
                .body(zipFile);
    }
    /**
     * 文件预览接口
     */
    @GetMapping(value = "/{resourceId}/preview", produces = MediaType.TEXT_HTML_VALUE)
    public String previewFiles(@PathVariable Integer resourceId) throws Exception {
        return resourceService.generateFilePreviewHtml(resourceId);
    }

    /**
     * 根据临时令牌预览文件
     * @param token 临时令牌
     * @return 文件响应实体
     * @throws  IOException 文件操作异常
     */
    @GetMapping("/preview")
    public ResponseEntity<org.springframework.core.io.Resource> previewFile(@RequestParam("token") String token) throws IOException {
        try {
            String filePath = resourceService.validateTempToken(token);
            if (filePath == null) {
                return ResponseEntity.notFound().build();
            }

            FileSystemResource fileResource = resourceService.getFileResource(filePath);
            if (fileResource == null || !fileResource.exists()) {
                return ResponseEntity.notFound().build();
            }

            // 根据文件类型设置 Content-Type
            String contentType = Files.probeContentType(Paths.get(filePath));
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            HttpHeaders headers = new HttpHeaders();
            // 设置为 inline 以实现预览
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileResource.getFilename() + "\"");
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentLength(fileResource.contentLength());

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(fileResource);
        } catch (Exception e) {
            System.err.println("预览文件时出现异常: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 根据标签 ID 数组查询资源
     * @param tagIds 标签 ID 数组
     * @return 符合条件的资源列表
     */
    @GetMapping("/by-tag-ids")
    public List<Resource> getResourcesByTagIds(@RequestParam List<Integer> tagIds) {
        return resourceService.getResourcesByTagIds(tagIds);
    }

    /**
     * 获取 COS 上传签名
     * @param fileKey 上传文件在 COS 上的路径，例如 "uploads/example.png"
     * @return 包含签名的 JSON 对象
     */
    @GetMapping("/cos-signature")
    public Map<String, String> getCosSignature(@RequestParam String fileKey) {
        String signature = resourceService.generateCosSignature(fileKey);
        Map<String, String> result = new HashMap<>();
        result.put("signature", signature);
        result.put("fileKey", fileKey);
        return result;
    }
}
