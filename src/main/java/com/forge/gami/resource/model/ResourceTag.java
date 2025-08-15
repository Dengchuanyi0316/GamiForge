package com.forge.gami.resource.model;

public class ResourceTag {
    private Integer resourceId; // 资源ID
    private Integer tagId;      // 标签ID

    public ResourceTag(Integer resourceId, Integer tagId) {
        this.resourceId = resourceId;
        this.tagId = tagId;
    }

    public Integer getResourceId() {
        return resourceId;
    }

    public void setResourceId(Integer resourceId) {
        this.resourceId = resourceId;
    }

    public Integer getTagId() {
        return tagId;
    }

    public void setTagId(Integer tagId) {
        this.tagId = tagId;
    }

    @Override
    public String toString() {
        return "ResourceTag{" +
                "resourceId=" + resourceId +
                ", tagId=" + tagId +
                '}';
    }
}
