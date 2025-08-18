package com.forge.gami.resource.mapper;

import com.forge.gami.resource.model.Resource;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ResourceMapper {
    // 查询单个资源及标签
    Resource selectResourceById(@Param("id") Integer id);

    // 查询所有资源及标签
    List<Resource> selectAllResources();

    // 根据分类查询资源
    List<Resource> selectResourcesByCategory(@Param("category") String category);

    // 插入资源（返回自增id）
    int insertResource(Resource resource);

    // 更新资源
    int updateResource(Resource resource);

    // 删除资源
    int deleteResourceById(@Param("id") Integer id);
}
