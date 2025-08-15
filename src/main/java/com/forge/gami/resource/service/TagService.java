package com.forge.gami.resource.service;

import com.forge.gami.resource.model.Tag;

import java.util.List;

public interface TagService {
    // 查询所有标签
    List<Tag> getAllTags();

    // 根据ID查询标签
    Tag getTagById(Integer id);

    // 根据大分类查询标签
    List<Tag> getTagByCategory(String category);

    // 新增标签
    int addTag(Tag tag);

    // 更新标签
    int updateTag(Tag tag);

    // 删除标签
    int deleteTag(Integer id);
}
