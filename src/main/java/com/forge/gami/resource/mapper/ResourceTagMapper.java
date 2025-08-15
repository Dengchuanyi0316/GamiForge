package com.forge.gami.resource.mapper;

import com.forge.gami.resource.model.ResourceTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ResourceTagMapper {
    // 插入单个关联
    int insertResourceTag(ResourceTag resourceTag);

    // 批量插入关联
    int insertResourceTags(@Param("list") List<ResourceTag> resourceTags);

    // 删除某资源的所有标签关联
    int deleteTagsByResourceId(@Param("resourceId") Integer resourceId);

    // 删除单个资源-标签关联
    int deleteTag(@Param("resourceId") Integer resourceId, @Param("tagId") Integer tagId);
}
