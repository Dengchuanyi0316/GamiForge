package com.forge.gami.resource.service;

import com.forge.gami.resource.model.ResourceTag;

import java.util.List;

public interface ResourceTagService {
    // 为资源添加标签
    int addResourceTag(ResourceTag resourceTag);

    // 批量添加资源标签
    int addResourceTags(List<ResourceTag> resourceTags);

    // 删除某资源的所有标签关联
    int removeTagsByResourceId(Integer resourceId);

    // 删除单个资源-标签关联
    int removeTag(Integer resourceId, Integer tagId);
}
