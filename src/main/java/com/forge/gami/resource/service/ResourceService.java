package com.forge.gami.resource.service;

import com.forge.gami.resource.model.Resource;
import com.forge.gami.resource.model.Tag;
import org.springframework.core.io.FileSystemResource;
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

    // 根据资源ID查询文件数量及总大小
    Map<String, Object> getFileInfoByResourceId(Integer resourceId);

    // 提供压缩文件夹下载
    org.springframework.core.io.Resource getResourceZipById(Integer resourceId);

    // 文件预览
    String generateFilePreviewHtml(Integer resourceId) throws Exception;

    /**
     * 验证临时令牌并返回文件路径
     * @param token 临时令牌
     * @return 文件路径，若令牌无效则返回 null
     */
    String validateTempToken(String token);

    /**
     * 根据文件路径获取文件资源
     *
     * @param filePath 文件路径
     * @return 文件资源
     */
    FileSystemResource getFileResource(String filePath);

    /**
     * 根据标签 ID 数组查询资源
     * @param tagIds 标签 ID 数组
     * @return 符合条件的资源列表
     */
    List<Resource> getResourcesByTagIds(List<Integer> tagIds);
}
