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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
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

    @Value("${server.base.url}")
    private String serverBaseUrl;

    private final Map<String, TokenInfo> tempTokens = new ConcurrentHashMap<>();

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

    @Override
    public String generateFilePreviewHtml(Integer resourceId) throws Exception {
        // 1. 根据资源ID获取资源信息
        Resource resource = getResourceById(resourceId);
        if (resource == null) {
            throw new IllegalArgumentException("资源不存在，ID: " + resourceId);
        }

        // 2. 处理文件路径，获取实际物理路径
        String dbPath = resource.getFilePath();
        String relativePath = dbPath.replace("/files/", "");
        Path resourceDir = Paths.get(baseUploadPath, relativePath);

        // 3. 验证目录是否存在
        if (!Files.exists(resourceDir) || !Files.isDirectory(resourceDir)) {
            throw new IOException("资源目录不存在: " + resourceDir);
        }

        // 4. 生成HTML页面
        StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append("<!DOCTYPE html>");
        htmlBuilder.append("<html lang=\"zh-CN\">");
        htmlBuilder.append("<head>");
        htmlBuilder.append("<meta charset=\"UTF-8\">");
        htmlBuilder.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
        htmlBuilder.append("<title>资源文件预览 - ID: ").append(resourceId).append("</title>");
        htmlBuilder.append("<style>");
        htmlBuilder.append("  body { font-family: 'Segoe UI', Arial, sans-serif; max-width: 1200px; margin: 0 auto; padding: 20px; background-color: #f5f7fa; }");
        htmlBuilder.append("  .container { background-color: white; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); padding: 30px; }");
        htmlBuilder.append("  h1 { color: #333; border-bottom: 2px solid #007bff; padding-bottom: 10px; }");
        htmlBuilder.append("  .file-list { list-style: none; padding: 0; margin-top: 20px; }");
        htmlBuilder.append("  .file-item { padding: 12px 15px; border-bottom: 1px solid #eee; display: flex; align-items: center; }");
        htmlBuilder.append("  .file-item:last-child { border-bottom: none; }");
        htmlBuilder.append("  .file-item:hover { background-color: #f8f9fa; }");
        htmlBuilder.append("  .file-icon { margin-right: 10px; color: #6c757d; }");
        htmlBuilder.append("  .file-link { text-decoration: none; color: #007bff; font-weight: 500; }");
        htmlBuilder.append("  .file-link:hover { text-decoration: underline; }");
        htmlBuilder.append("  .expiry-note { color: #6c757d; font-size: 0.9em; margin-top: 20px; padding-top: 15px; border-top: 1px dashed #ddd; }");
        htmlBuilder.append("</style>");
        htmlBuilder.append("</head>");
        htmlBuilder.append("<body>");
        htmlBuilder.append("<div class=\"container\">");
        htmlBuilder.append("  <h1>资源文件预览 (ID: ").append(resourceId).append(")</h1>");
        htmlBuilder.append("  <ul class=\"file-list\">");

        // 遍历资源目录下的文件
        Files.walk(resourceDir, 1)
                .filter(Files::isRegularFile)
                .forEach(filePath -> {
                    String fileName = filePath.getFileName().toString();
                    String fileIcon = getFileIcon(fileName);
                    String tempToken = generateTempToken(filePath.toString());
                    String fileUrl = serverBaseUrl + "/api/resources/preview?token=" + tempToken;

                    htmlBuilder.append("    <li class=\"file-item\">");
                    htmlBuilder.append("      <span class=\"file-icon\">").append(fileIcon).append("</span>");
                    htmlBuilder.append("      <a class=\"file-link\" href=\"").append(fileUrl).append("\" target=\"_blank\">").append(fileName).append("</a>");
                    htmlBuilder.append("    </li>");
                });

        htmlBuilder.append("  </ul>");
        htmlBuilder.append("  <p class=\"expiry-note\">文件链接 30 分钟后过期，请及时下载。</p>");
        htmlBuilder.append("</div>");
        htmlBuilder.append("</body>");
        htmlBuilder.append("</html>");

        return htmlBuilder.toString();
    }

    /**
     * 生成临时访问令牌
     */
    private String generateTempToken(String filePath) {
        String token = UUID.randomUUID().toString();
        long expiryTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(30); // 30分钟过期
        tempTokens.put(token, new TokenInfo(filePath, expiryTime));
        return token;
    }

    /**
     * 根据文件名获取文件图标
     */
    private String getFileIcon(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        switch (extension) {
            case "pdf": return "📄 ";
            case "doc":
            case "docx": return "📝 ";
            case "xls":
            case "xlsx": return "📊 ";
            case "ppt":
            case "pptx": return "📑 ";
            case "jpg":
            case "jpeg":
            case "png":
            case "gif": return "🖼️ ";
            case "zip":
            case "rar":
            case "7z": return "🗜️ ";
            case "txt": return "📄 ";
            case "java":
            case "py":
            case "js": return "💻 ";
            default: return "📄 ";
        }
    }

    /**
     * 令牌信息内部类
     */
    private static class TokenInfo {
        String filePath;
        long expiryTime;

        TokenInfo(String filePath, long expiryTime) {
            this.filePath = filePath;
            this.expiryTime = expiryTime;
        }
    }

    @Override
    public String validateTempToken(String token) {
        TokenInfo tokenInfo = tempTokens.get(token);
        if (tokenInfo != null && System.currentTimeMillis() < tokenInfo.expiryTime) {
            return tokenInfo.filePath;
        }
        tempTokens.remove(token);
        return null;
    }

    @Override
    public FileSystemResource getFileResource(String filePath) {
        File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            return new FileSystemResource(file);
        }
        return null;
    }

    @Override
    public List<Resource> getResourcesByTagIds(List<Integer> tagIds) {
        return resourceMapper.selectResourcesByTagIds(tagIds);
    }


}
