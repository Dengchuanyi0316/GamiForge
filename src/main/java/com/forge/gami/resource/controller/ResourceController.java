package com.forge.gami.resource.controller;

import com.forge.gami.resource.dto.ResourceDTO;
import com.forge.gami.resource.model.Resource;
import com.forge.gami.resource.model.Tag;
import com.forge.gami.resource.service.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

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
}
