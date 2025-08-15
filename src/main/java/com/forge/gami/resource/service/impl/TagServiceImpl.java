package com.forge.gami.resource.service.impl;

import com.forge.gami.resource.mapper.TagMapper;
import com.forge.gami.resource.model.Tag;
import com.forge.gami.resource.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TagServiceImpl implements TagService {
    // 注入TagMapper（数据库操作接口）
    private final TagMapper tagMapper;

    // 构造器注入（推荐，便于单元测试和依赖管理）
    @Autowired
    public TagServiceImpl(TagMapper tagMapper) {
        this.tagMapper = tagMapper;
    }

    @Override
    public List<Tag> getAllTags() {
        // 调用mapper查询所有标签
        return tagMapper.selectAllTags();
    }

    @Override
    public Tag getTagById(Integer id) {
        // 调用mapper根据ID查询标签
        return tagMapper.selectTagById(id);
    }

    @Override
    public List<Tag> getTagByCategory(String category) {
        // 调用mapper根据分类查询标签列表
        return tagMapper.selectTagByCategory(category);
    }

    @Override
    public int addTag(Tag tag) {
        // 调用mapper插入标签，返回受影响行数（1=成功，0=失败）
        return tagMapper.insertTag(tag);
    }

    @Override
    public int updateTag(Tag tag) {
        // 调用mapper更新标签，返回受影响行数
        return tagMapper.updateTag(tag);
    }

    @Override
    public int deleteTag(Integer id) {
        // 调用mapper删除标签，返回受影响行数
        return tagMapper.deleteTagById(id);
    }
}
