package com.forge.gami.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.nio.charset.StandardCharsets;

@Service
public class CosSignatureService {

    // 从配置文件获取
    @Value("${cos.secretId}")
    private String secretId;

    @Value("${cos.secretKey}")
    private String secretKey;

    @Value("${cos.bucket}")
    private String bucket;

    @Value("${cos.region}")
    private String region;

    @Value("${server.base.url}")
    private String serverBaseUrl;

    /**
     * 生成 COS 临时上传签名
     */
    public Map<String, String> generateCosUploadSignature(String filePath) throws Exception {
        long currentTime = Instant.now().getEpochSecond();
        long expireTime = currentTime + 30 * 60; // 签名有效期 30 分钟

        String policy = "{\n" +
                "  \"expiration\": \"" + Instant.ofEpochSecond(expireTime) + "\",\n" +
                "  \"conditions\": [\n" +
                "    [\"starts-with\", \"$key\", \"" + filePath + "\"]\n" +
                "  ]\n" +
                "}";

        String policyBase64 = Base64.getEncoder().encodeToString(policy.getBytes(StandardCharsets.UTF_8));

        // HMAC-SHA1 签名
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA1"));
        String signature = Base64.getEncoder().encodeToString(mac.doFinal(policyBase64.getBytes(StandardCharsets.UTF_8)));

        Map<String, String> result = new HashMap<>();
        result.put("policy", policyBase64);
        result.put("signature", signature);
        result.put("key", filePath);
        result.put("cosUrl", "https://" + bucket + ".cos." + region + ".myqcloud.com");

        return result;
    }

    /**
     * 获取临时文件路径示例
     */
    public String generateTempFilePath(Integer resourceId, String originalFilename) {
        String ext = "";
        if (originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return "uploads/" + resourceId + "/" + UUID.randomUUID() + ext;
    }
}
