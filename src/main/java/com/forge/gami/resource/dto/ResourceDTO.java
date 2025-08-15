package com.forge.gami.resource.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.forge.gami.resource.model.Resource;

import java.util.List;

public class ResourceDTO {
    private String name;
    private String description;
    private String category;
    @JsonProperty("file_path")
    private String filePath;
    private List<Integer> tagIds;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public List<Integer> getTagIds() {
        return tagIds;
    }

    public void setTagIds(List<Integer> tagIds) {
        this.tagIds = tagIds;
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
