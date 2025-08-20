package com.forge.gami.resource.service.impl;

import com.forge.gami.resource.mapper.ResourceMapper;
import com.forge.gami.resource.mapper.ResourceTagMapper;
import com.forge.gami.resource.mapper.TagMapper;
import com.forge.gami.resource.model.Resource;
import com.forge.gami.resource.model.ResourceTag;
import com.forge.gami.resource.model.Tag;
import com.forge.gami.resource.service.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class ResourceServiceImpl implements ResourceService {
    @Autowired
    private ResourceMapper resourceMapper;

    @Autowired
    private TagMapper tagMapper;

    @Autowired
    private ResourceTagMapper resourceTagMapper;


    // 从配置文件读取基础上传目录
    @Value("${upload.base.path}")
    private String baseUploadPath;

    @Override
    public Resource getResourceById(Integer id) {
        Resource resource = resourceMapper.selectResourceById(id);
        if (resource != null) {
            // 查询该资源对应的标签
            // 这里假设你在 Resource 实体类里有 List<Tag> tags;
            // 你可能需要写一个 Mapper 方法获取标签列表
        }
        return resource;
    }

    @Override
    public List<Resource> getAllResources() {
        List<Resource> resources = resourceMapper.selectAllResources();
        // 可选：批量查询并填充标签
        return resources;
    }

    @Override
    public List<Resource> getResourcesByCategory(String category) {
        return resourceMapper.selectResourcesByCategory(category);
    }

    private Map<String, String> saveFileToTimestampFolder(MultipartFile file, String baseUploadPath, String fileTypeName, String folder,String fixedFileName) {
        Map<String, String> result = new HashMap<>();

        if (file.isEmpty()) {
            result.put("success", "false");
            result.put("message", fileTypeName + "文件不能为空");
            return result;
        }

        try {
            // 1. 前端传来时间戳
            String timestampFolder = folder;

            //今天日期
            String today = java.time.LocalDate.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));

            // 2. 创建物理路径
            File uploadDir = Paths.get(baseUploadPath, today, timestampFolder).toFile();
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // 3. 唯一文件名
            String fileName;
            if (fixedFileName.equals("thumbnail.jpg")) {
                fileName = fixedFileName; // 缩略图固定名
            } else {
                String originalFilename = file.getOriginalFilename();
                String fileExt = originalFilename.substring(originalFilename.lastIndexOf("."));
                fileName = UUID.randomUUID().toString() + fileExt;
            }

            // 4. 保存文件
            File destFile = new File(uploadDir, fileName);
            file.transferTo(destFile);

            // 5. 返回文件夹路径
            String folderUrl = "/files/" + today + "/" + timestampFolder + "/";
            if (fixedFileName.equals("thumbnail.jpg")){
                folderUrl = folderUrl + fixedFileName;
            }
            result.put("success", "true");
            result.put("folderPath", folderUrl);
            result.put("message", fileTypeName + "上传成功");

        } catch (IOException e) {
            result.put("success", "false");
            result.put("message", fileTypeName + "上传失败: " + e.getMessage());
        }

        return result;
    }

    @Override
    public Map<String, String> uploadResourceFile(MultipartFile file, String folder,String fixedFileName) {
        return saveFileToTimestampFolder(file, baseUploadPath, "资源文件", folder,fixedFileName);
    }


    @Override
    @Transactional
    public int addResource(Resource resource, List<Tag> tags) {
        // 设置资源添加时间为当前时间
        resource.setAddedAt(java.time.LocalDateTime.now());
        // 设置资源更新时间为当前时间
        resource.setUpdatedAt(java.time.LocalDateTime.now());
        int rows = resourceMapper.insertResource(resource); // 插入资源，自动生成id
        Integer resourceId = resource.getId(); // 获取自增ID

        if (tags != null && !tags.isEmpty()) {
            List<ResourceTag> relations = new ArrayList<>();
            for (Tag tag : tags) {
                relations.add(new ResourceTag(resourceId, tag.getId()));
            }
            resourceTagMapper.insertResourceTags(relations);
        }

        return rows;
    }

    @Override
    @Transactional
    public int updateResource(Resource resource, List<Integer> tagIds) {
        int rows = resourceMapper.updateResource(resource);

        Integer resourceId = resource.getId();
        // 删除原有标签关联
        resourceTagMapper.deleteTagsByResourceId(resourceId);

        // 插入新的标签关联
        if (tagIds != null && !tagIds.isEmpty()) {
            List<ResourceTag> relations = new ArrayList<>();
            for (Integer tagId : tagIds) {
                relations.add(new ResourceTag(resourceId, tagId));
            }
            resourceTagMapper.insertResourceTags(relations);
        }

        return rows;
    }

    @Override
    @Transactional
    public int deleteResource(Integer id) {
        // 先删除关联的标签
        resourceTagMapper.deleteTagsByResourceId(id);
        // 再删除资源
        return resourceMapper.deleteResourceById(id);
    }

    @Override
    public Map<String, Object> getFileInfoByResourceId(Integer resourceId) {
        Map<String, Object> fileInfo = new HashMap<>();
        Resource resource = getResourceById(resourceId);
        if (resource == null) {
            throw new RuntimeException("资源不存在");
        }

        // 移除数据库路径中的"/files/"前缀，并拼接实际存储路径
        String dbPath = resource.getFilePath();
        String relativePath = dbPath.replace("/files/", "");
        Path directoryPath = Paths.get(baseUploadPath, relativePath);
        System.out.println("===========文件夹路径============" + directoryPath);

        File directory = directoryPath.toFile();
        if (!directory.exists() || !directory.isDirectory()) {
            throw new RuntimeException("资源文件路径不存在或不是目录");
        }

        // 计算文件数量和总大小
        long fileCount = countFiles(directory);
        long totalSize = calculateTotalSize(directory);

        fileInfo.put("fileCount", fileCount);
        fileInfo.put("totalSize", totalSize);
        fileInfo.put("unit", "bytes");

        return fileInfo;
    }

    @Override
    public org.springframework.core.io.Resource getResourceZipById(Integer resourceId) {
        Resource resource = getResourceById(resourceId);
        if (resource == null) {
            throw new RuntimeException("资源不存在");
        }

        String dbPath = resource.getFilePath();
        String relativePath = dbPath.replace("/files/", "");
        Path sourcePath = Paths.get(baseUploadPath, relativePath);

        String zipFileName = "resource-" + resourceId + ".zip";
        Path zipFilePath = Paths.get(baseUploadPath, "temp", zipFileName);
        File zipFile = zipFilePath.toFile();

        // 创建临时目录
        File tempDir = zipFilePath.getParent().toFile();
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }

        try {
            // 压缩文件夹
            zipDirectory(sourcePath.toFile(), zipFile);
            return new FileSystemResource(zipFile); // 返回Spring的Resource实现类
        } catch (IOException e) {
            throw new RuntimeException("压缩文件失败: " + e.getMessage());
        }
    }

    // 辅助方法：计算目录下文件总数
    private long countFiles(File directory) {
        File[] files = directory.listFiles();
        if (files == null) return 0;

        long count = 0;
        for (File file : files) {
            if (file.isDirectory()) {
                count += countFiles(file);
            } else {
                count++;
            }
        }
        return count;
    }

    // 辅助方法：计算目录下文件总大小
    private long calculateTotalSize(File directory) {
        File[] files = directory.listFiles();
        if (files == null) return 0;

        long totalSize = 0;
        for (File file : files) {
            if (file.isDirectory()) {
                totalSize += calculateTotalSize(file);
            } else {
                totalSize += file.length();
            }
        }
        return totalSize;
    }

    // 辅助方法：压缩目录
    private void zipDirectory(File sourceDir, File zipFile) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            Path sourcePath = sourceDir.toPath();
            Files.walk(sourcePath)
                    .filter(Files::isRegularFile)
                    .forEach(path -> {
                        ZipEntry zipEntry = new ZipEntry(sourcePath.relativize(path).toString());
                        try {
                            zos.putNextEntry(zipEntry);
                            Files.copy(path, zos);
                            zos.closeEntry();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }
    }

}
