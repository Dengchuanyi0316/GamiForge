package com.forge.gami.cos.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CosConfig {

    @Value("${cos.secret-id}")
    private String secretId;
    
    @Value("${cos.secret-key}")
    private String secretKey;
    
    @Value("${cos.region}")
    private String region;
    
    @Value("${cos.bucket}")
    private String bucket;
    
    @Value("${cos.temp-expire}")
    private long tempExpire;
    
    @Value("${cos.cdn-domain}")
    private String cdnDomain;
    
    @Value("${cos.base-path}")
    private String basePath;

    public String getSecretId() {
        return secretId;
    }

    public void setSecretId(String secretId) {
        this.secretId = secretId;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public long getTempExpire() {
        return tempExpire;
    }

    public void setTempExpire(long tempExpire) {
        this.tempExpire = tempExpire;
    }

    public String getCdnDomain() {
        return cdnDomain;
    }

    public void setCdnDomain(String cdnDomain) {
        this.cdnDomain = cdnDomain;
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }
}
