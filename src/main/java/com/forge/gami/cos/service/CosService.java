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
}
