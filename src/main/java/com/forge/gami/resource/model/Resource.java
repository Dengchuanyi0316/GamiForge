package com.forge.gami.resource.model;

import java.time.LocalDateTime;
import java.util.List;

public class Resource {
    private Integer id;              // 唯一ID
    private String name;             // 资源名称
    private String category;         // 大分类
    private String filePath;         // 文件路径
    private String thumbnailPath;    // 缩略图路径
    private String description;      // 描述
    private String state;            // 状态（ temp 临时/ active 已完成）
    private LocalDateTime addedAt;   // 添加时间
    private LocalDateTime updatedAt; // 更新时间

    // 可选：标签列表
    private List<Tag> tags;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(LocalDateTime addedAt) {
        this.addedAt = addedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "Resource{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", filePath='" + filePath + '\'' +
                ", thumbnailPath='" + thumbnailPath + '\'' +
                ", state='" + state + '\'' +
                ", description='" + description + '\'' +
                ", addedAt=" + addedAt +
                ", updatedAt=" + updatedAt +
                ", tags=" + tags +
                '}';
    }
    public Resource toResource() {
        Resource resource = new Resource();
        resource.setName(this.name);
        resource.setDescription(this.description);
        resource.setCategory(this.category);
        resource.setFilePath(this.filePath);
        return resource;
    }
}
