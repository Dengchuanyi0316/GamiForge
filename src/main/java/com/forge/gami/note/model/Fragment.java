package com.forge.gami.note.model;

import java.time.LocalDateTime;
import java.util.Date;

public class Fragment {
    private Long id; // 主键ID
    private String filename; // 原始文件名
    private String filePath; // 文件相对路径
    private String fileType; // MIME类型
    private Long fileSize; // 文件大小（字节）
    private Long noteId; // 关联笔记ID（可为 null）
    private String description; // 用户备注
    private LocalDateTime createdAt; // 创建时间
    private LocalDateTime updatedAt; // 更新时间

    private Integer sortOrder;
    private String type;

    public Fragment() {}

    // 全参构造（可选）
    public Fragment(Long id, String filename, String filePath, String fileType, Long fileSize,
                    Long noteId, String description, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.filename = filename;
        this.filePath = filePath;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.noteId = noteId;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getter / Setter 省略可使用 Lombok

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getFilename() { return filename; }

    public void setFilename(String filename) { this.filename = filename; }

    public String getFilePath() { return filePath; }

    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getFileType() { return fileType; }

    public void setFileType(String fileType) { this.fileType = fileType; }

    public Long getFileSize() { return fileSize; }

    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public Long getNoteId() { return noteId; }

    public void setNoteId(Long noteId) { this.noteId = noteId; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    @Override
    public String toString() {
        return "Fragment{" +
                "id=" + id +
                ", filename='" + filename + '\'' +
                ", filePath='" + filePath + '\'' +
                ", fileType='" + fileType + '\'' +
                ", fileSize=" + fileSize +
                ", noteId=" + noteId +
                ", sortOrder=" + sortOrder +
                ", type='" + type + '\'' +
                ", description='" + description + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
