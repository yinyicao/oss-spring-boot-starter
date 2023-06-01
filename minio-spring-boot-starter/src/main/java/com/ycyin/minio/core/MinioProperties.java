package com.ycyin.minio.core;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @Author
 * @Description MINIO配置类
 * @ClassName MinioProperties
 * @Date 2023/3/18 17:51
 **/
@Data
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {
    /**
     * 对象存储服务的URL
     */
    private String endpoint;

    /**
     * 区域
     */
    private String region;

    /**
     * Access key
     */
    private String accessKey;

    /**
     * Secret key
     */
    private String secretKey;

    /**
     * Bucket name for the application. The bucket must already exists on Minio.
     */
    private String bucket;

    /**
     * 最大线程数，默认： 100
     */
    private Integer maxConnections = 100;
}
