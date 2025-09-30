package com.forge.gami.cos.service;

import com.alibaba.fastjson.JSONObject;
import com.forge.gami.cos.config.CosConfig;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.http.HttpMethodName;
import com.qcloud.cos.model.*;
import com.qcloud.cos.region.Region;
import com.qcloud.cos.ClientConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class CosService {

    @Autowired
    private CosConfig cosConfig;

    // 生成临时密钥和上传签名
    public Map<String, String> generateUploadSignature(String fileName, String fileType, long fileSize) {
        // 生成唯一的文件路径
        String dateDir = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String key = cosConfig.getBasePath() + '/' + dateDir + '/' + UUID.randomUUID() + '_' + fileName;



        try {
            // 调用通用方法生成签名URL
            URL url = generatePresignedUrl(cosConfig.getBucket(), key, HttpMethodName.PUT, cosConfig.getTempExpire());


            // 准备返回的签名信息
            Map<String, String> signatureInfo = new HashMap<>();
            signatureInfo.put("key", key);
            signatureInfo.put("signature", url.toString());
            signatureInfo.put("token", ""); // 临时密钥模式下使用，这里简化处理
            signatureInfo.put("cosUrl", "https://" + cosConfig.getBucket() + ".cos." + cosConfig.getRegion() + ".myqcloud.com");

            return signatureInfo;
        } catch (Exception e) {
            throw new RuntimeException("生成上传签名失败", e);
        }
    }

    // 上传文件到COS
    public String uploadFile(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        String dateDir = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String key = cosConfig.getBasePath() + '/' + dateDir + '/' + UUID.randomUUID() + '_' + fileName;

        COSCredentials cred = new BasicCOSCredentials(cosConfig.getSecretId(), cosConfig.getSecretKey());
        ClientConfig clientConfig = new ClientConfig(new Region(cosConfig.getRegion()));
        COSClient cosClient = new COSClient(cred, clientConfig);

        try {
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(file.getSize());
            objectMetadata.setContentType(file.getContentType());

            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    cosConfig.getBucket(), key, file.getInputStream(), objectMetadata);

            PutObjectResult putObjectResult = cosClient.putObject(putObjectRequest);

            // 返回文件在COS中的路径
            return key;
        } finally {
            cosClient.shutdown();
        }
    }

    // 从COS下载文件
    public void downloadFile(String fileId, HttpServletResponse response) throws IOException {
        COSCredentials cred = new BasicCOSCredentials(cosConfig.getSecretId(), cosConfig.getSecretKey());
        ClientConfig clientConfig = new ClientConfig(new Region(cosConfig.getRegion()));
        COSClient cosClient = new COSClient(cred, clientConfig);

        try {
            GetObjectRequest getObjectRequest = new GetObjectRequest(cosConfig.getBucket(), fileId);
            COSObject cosObject = cosClient.getObject(getObjectRequest);

            COSObjectInputStream cosObjectInput = cosObject.getObjectContent();

            // 设置响应头
            ObjectMetadata metadata = cosObject.getObjectMetadata();
            response.setContentType(metadata.getContentType());
            response.setHeader("Content-Disposition", "attachment; filename=\"" + extractFileName(fileId) + "\"");

            // 写入响应流
            byte[] buffer = new byte[4096];
            int bytesRead;
            OutputStream outputStream = response.getOutputStream();

            while ((bytesRead = cosObjectInput.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.flush();
        } finally {
            cosClient.shutdown();
        }
    }

    // 获取文件预览URL
    public String getFilePreviewUrl(String fileId) {
        COSCredentials cred = new BasicCOSCredentials(cosConfig.getSecretId(), cosConfig.getSecretKey());
        ClientConfig clientConfig = new ClientConfig(new Region(cosConfig.getRegion()));
        COSClient cosClient = new COSClient(cred, clientConfig);

        try {
            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(
                    cosConfig.getBucket(), fileId, HttpMethodName.GET);
            request.setExpiration(new Date(System.currentTimeMillis() + cosConfig.getTempExpire() * 1000));

            URL url = cosClient.generatePresignedUrl(request);
            return url.toString();
        } finally {
            cosClient.shutdown();
        }
    }

    // 删除文件
    public boolean deleteFile(String fileId) {
        COSCredentials cred = new BasicCOSCredentials(cosConfig.getSecretId(), cosConfig.getSecretKey());
        ClientConfig clientConfig = new ClientConfig(new Region(cosConfig.getRegion()));
        COSClient cosClient = new COSClient(cred, clientConfig);

        try {
            cosClient.deleteObject(cosConfig.getBucket(), fileId);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            cosClient.shutdown();
        }
    }

    // 获取文件信息
    public Map<String, Object> getFileInfo(String fileId) {
        COSCredentials cred = new BasicCOSCredentials(cosConfig.getSecretId(), cosConfig.getSecretKey());
        ClientConfig clientConfig = new ClientConfig(new Region(cosConfig.getRegion()));
        COSClient cosClient = new COSClient(cred, clientConfig);

        try {
            ObjectMetadata metadata = cosClient.getObjectMetadata(cosConfig.getBucket(), fileId);

            Map<String, Object> fileInfo = new HashMap<>();
            fileInfo.put("fileId", fileId);
            fileInfo.put("fileName", extractFileName(fileId));
            fileInfo.put("contentType", metadata.getContentType());
            fileInfo.put("contentLength", metadata.getContentLength());
            fileInfo.put("lastModified", metadata.getLastModified());

            return fileInfo;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            cosClient.shutdown();
        }
    }

    // 列出文件
    public List<Map<String, Object>> listFiles(String prefix, int maxKeys) {
        COSCredentials cred = new BasicCOSCredentials(cosConfig.getSecretId(), cosConfig.getSecretKey());
        ClientConfig clientConfig = new ClientConfig(new Region(cosConfig.getRegion()));
        COSClient cosClient = new COSClient(cred, clientConfig);

        try {
            ListObjectsRequest listObjectsRequest = new ListObjectsRequest();
            listObjectsRequest.setBucketName(cosConfig.getBucket());
            if (StringUtils.hasText(prefix)) {
                listObjectsRequest.setPrefix(prefix);
            }
            listObjectsRequest.setMaxKeys(maxKeys > 0 ? maxKeys : 100);

            ObjectListing objectListing = cosClient.listObjects(listObjectsRequest);
            List<COSObjectSummary> objectSummaries = objectListing.getObjectSummaries();

            List<Map<String, Object>> fileList = new ArrayList<>();
            for (COSObjectSummary summary : objectSummaries) {
                Map<String, Object> fileInfo = new HashMap<>();
                fileInfo.put("fileId", summary.getKey());
                fileInfo.put("fileName", extractFileName(summary.getKey()));
                fileInfo.put("contentLength", summary.getSize());
                fileInfo.put("lastModified", summary.getLastModified());

                fileList.add(fileInfo);
            }

            return fileList;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            cosClient.shutdown();
        }
    }

    // 辅助方法：从文件路径中提取文件名
    private String extractFileName(String filePath) {
        int lastSlashIndex = filePath.lastIndexOf('/');
        if (lastSlashIndex >= 0 && lastSlashIndex < filePath.length() - 1) {
            String fileNameWithUuid = filePath.substring(lastSlashIndex + 1);
            int uuidUnderscoreIndex = fileNameWithUuid.indexOf('_');
            if (uuidUnderscoreIndex > 0 && uuidUnderscoreIndex < fileNameWithUuid.length() - 1) {
                return fileNameWithUuid.substring(uuidUnderscoreIndex + 1);
            }
            return fileNameWithUuid;
        }
        return filePath;
    }

    // 添加通用的签名生成方法
    private URL generatePresignedUrl(String bucket, String key, HttpMethodName method, long expirationSeconds) {
        COSCredentials cred = new BasicCOSCredentials(cosConfig.getSecretId(), cosConfig.getSecretKey());
        ClientConfig clientConfig = new ClientConfig(new Region(cosConfig.getRegion()));
        COSClient cosClient = new COSClient(cred, clientConfig);

        try {
            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucket, key, method);
            request.setExpiration(new Date(System.currentTimeMillis() + expirationSeconds * 1000));
            return cosClient.generatePresignedUrl(request);
        } finally {
            cosClient.shutdown();
        }
    }

    /**
     * 初始化分片上传
     */
    public Map<String, String> initMultipartUpload(String fileName) {
        // 生成唯一的文件路径
        String dateDir = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String key = cosConfig.getBasePath() + '/' + dateDir + '/' + UUID.randomUUID() + '_' + fileName;

        COSCredentials cred = new BasicCOSCredentials(cosConfig.getSecretId(), cosConfig.getSecretKey());
        ClientConfig clientConfig = new ClientConfig(new Region(cosConfig.getRegion()));
        COSClient cosClient = new COSClient(cred, clientConfig);

        try {
            InitiateMultipartUploadRequest request = new InitiateMultipartUploadRequest(
                    cosConfig.getBucket(), key);

            // 设置Content-Type（可选）
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentType(getContentType(fileName));
            request.setObjectMetadata(objectMetadata);

            InitiateMultipartUploadResult result = cosClient.initiateMultipartUpload(request);

            // 准备返回结果
            Map<String, String> uploadInfo = new HashMap<>();
            uploadInfo.put("key", key);
            uploadInfo.put("uploadId", result.getUploadId());
            uploadInfo.put("cosUrl", "https://" + cosConfig.getBucket() + ".cos." + cosConfig.getRegion() + ".myqcloud.com");

            return uploadInfo;
        } catch (Exception e) {
            throw new RuntimeException("初始化分片上传失败", e);
        } finally {
            cosClient.shutdown();
        }
    }

    /**
     * 为分片生成签名URL
     */
    public String generatePartUploadUrl(String key, String uploadId, int partNumber) {
        try {
            // 使用现有的generatePresignedUrl方法生成签名URL
            URL url = generatePresignedUrlForPart(cosConfig.getBucket(), key, uploadId, partNumber);
            return url.toString();
        } catch (Exception e) {
            throw new RuntimeException("生成分片上传签名URL失败", e);
        }
    }

    /**
     * 完成分片上传
     */
    public Map<String, Object> completeMultipartUpload(String key, String uploadId, List<Map<String, Object>> parts) {
        COSCredentials cred = new BasicCOSCredentials(cosConfig.getSecretId(), cosConfig.getSecretKey());
        ClientConfig clientConfig = new ClientConfig(new Region(cosConfig.getRegion()));
        COSClient cosClient = new COSClient(cred, clientConfig);

        try {
            // 构建分片列表
            List<PartETag> partETags = new ArrayList<>();
            for (Map<String, Object> part : parts) {
                Integer partNumber = (Integer) part.get("PartNumber");
                String eTag = (String) part.get("ETag");
                partETags.add(new PartETag(partNumber, eTag));
            }

            // 完成分片上传请求
            CompleteMultipartUploadRequest request = new CompleteMultipartUploadRequest(
                    cosConfig.getBucket(), key, uploadId, partETags);

            CompleteMultipartUploadResult result = cosClient.completeMultipartUpload(request);

            // 准备返回结果
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("location", result.getLocation());
            resultMap.put("bucket", result.getBucketName());
            resultMap.put("key", result.getKey());
            resultMap.put("etag", result.getETag());

            return resultMap;
        } catch (Exception e) {
            throw new RuntimeException("完成分片上传失败", e);
        } finally {
            cosClient.shutdown();
        }
    }

    /**
     * 取消分片上传
     */
    public void abortMultipartUpload(String key, String uploadId) {
        COSCredentials cred = new BasicCOSCredentials(cosConfig.getSecretId(), cosConfig.getSecretKey());
        ClientConfig clientConfig = new ClientConfig(new Region(cosConfig.getRegion()));
        COSClient cosClient = new COSClient(cred, clientConfig);

        try {
            AbortMultipartUploadRequest request = new AbortMultipartUploadRequest(
                    cosConfig.getBucket(), key, uploadId);
            cosClient.abortMultipartUpload(request);
        } catch (Exception e) {
            throw new RuntimeException("取消分片上传失败", e);
        } finally {
            cosClient.shutdown();
        }
    }

    /**
     * 为分片上传生成签名URL的专用方法
     */
    private URL generatePresignedUrlForPart(String bucket, String key, String uploadId, int partNumber) {
        COSCredentials cred = new BasicCOSCredentials(cosConfig.getSecretId(), cosConfig.getSecretKey());
        ClientConfig clientConfig = new ClientConfig(new Region(cosConfig.getRegion()));
        COSClient cosClient = new COSClient(cred, clientConfig);

        try {
            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucket, key, HttpMethodName.PUT);
            request.setExpiration(new Date(System.currentTimeMillis() + cosConfig.getTempExpire() * 1000));
            request.addRequestParameter("partNumber", String.valueOf(partNumber));
            request.addRequestParameter("uploadId", uploadId);
            return cosClient.generatePresignedUrl(request);
        } finally {
            cosClient.shutdown();
        }
    }

    /**
     * 根据文件名获取Content-Type
     */
    private String getContentType(String fileName) {
        String extension = StringUtils.getFilenameExtension(fileName);
        if (extension == null) {
            return "application/octet-stream";
        }

        switch (extension.toLowerCase()) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "bmp":
                return "image/bmp";
            case "pdf":
                return "application/pdf";
            case "doc":
            case "docx":
                return "application/msword";
            case "xls":
            case "xlsx":
                return "application/vnd.ms-excel";
            case "ppt":
            case "pptx":
                return "application/vnd.ms-powerpoint";
            case "zip":
                return "application/zip";
            case "txt":
                return "text/plain";
            case "html":
            case "htm":
                return "text/html";
            case "css":
                return "text/css";
            case "js":
                return "application/javascript";
            default:
                return "application/octet-stream";
        }
    }
}
