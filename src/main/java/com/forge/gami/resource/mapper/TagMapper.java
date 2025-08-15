package com.forge.gami.resource.mapper;

import com.forge.gami.resource.model.Tag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TagMapper {
    // 查询所有标签
    List<Tag> selectAllTags();

    // 根据ID查询标签
    Tag selectTagById(@Param("id") Integer id);

    // 根据大分类查询标签
    List<Tag> selectTagByCategory(@Param("category") String category);

    // 插入标签（返回自增id）
    int insertTag(Tag tag);

    // 更新标签
    int updateTag(Tag tag);

    // 删除标签
    int deleteTagById(@Param("id") Integer id);
}
