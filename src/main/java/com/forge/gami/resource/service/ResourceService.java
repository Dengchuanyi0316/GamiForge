package com.forge.gami.resource.service;

import com.forge.gami.resource.model.Resource;
import com.forge.gami.resource.model.Tag;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface ResourceService {
    // 查询单个资源及其标签
    Resource getResourceById(Integer id);

    // 查询所有资源及其标签
    List<Resource> getAllResources();

    // 根据分类查询资源
    List<Resource> getResourcesByCategory(String category);

    // 资源文件上传
    Map<String, String> uploadResourceFile(MultipartFile file, String folder,String fixedFileName);


    // 新增资源（可同时关联标签）
    int addResource(Resource resource, List<Tag> tags);

    // 更新资源信息（可更新标签）
    int updateResource(Resource resource, List<Integer> tagIds);

    // 删除资源及其标签关联
    int deleteResource(Integer id);
}
