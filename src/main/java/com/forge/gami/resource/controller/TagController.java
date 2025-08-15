package com.forge.gami.resource.controller;

import com.forge.gami.resource.model.Tag;
import com.forge.gami.resource.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
public class TagController {
    private final TagService tagService;

    // 构造器注入TagService（推荐依赖注入方式）
    @Autowired
    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    // 1. 查询所有标签
    @GetMapping
    public List<Tag> getAllTags() {
        return tagService.getAllTags();
    }

    // 2. 根据ID查询单个标签
    @GetMapping("/{id}")
    public Tag getTagById(@PathVariable Integer id) {
        return tagService.getTagById(id);
    }

    // 3. 根据大分类查询标签列表
    @GetMapping("/category")
    public List<Tag> getTagByCategory(@RequestParam String category) {
        return tagService.getTagByCategory(category);
    }

    // 4. 新增标签
    @PostMapping
    public String addTag(@RequestBody Tag tag) {
        int rows = tagService.addTag(tag);
        return rows > 0 ? "标签添加成功" : "标签添加失败";
    }

    // 5. 更新标签
    @PutMapping("/{id}")
    public String updateTag(@PathVariable Integer id, @RequestBody Tag tag) {
        tag.setId(id); // 确保更新的是路径参数指定的标签ID
        int rows = tagService.updateTag(tag);
        return rows > 0 ? "标签更新成功" : "标签更新失败";
    }

    // 6. 删除标签
    @DeleteMapping("/{id}")
    public String deleteTag(@PathVariable Integer id) {
        int rows = tagService.deleteTag(id);
        return rows > 0 ? "标签删除成功" : "标签删除失败";
    }
}
