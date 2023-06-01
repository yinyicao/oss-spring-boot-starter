package com.ycyin.minio;

import com.ycyin.minio.core.MinioProperties;
import com.ycyin.minio.core.MinioTemplate;
import com.ycyin.minio.server.MinioTemplateImpl;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author ycyin
 * @Description Minio配置bean
 * @ClassName MinioAutoConfiguration
 * @Date 2023/3/18 18:23
 **/
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(MinioProperties.class)
public class MinioAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MinioClient ossClient(MinioProperties minioProperties) {
        return MinioClient.builder()
                .endpoint(minioProperties.getEndpoint())
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                .build();
    }

    @Bean
    @ConditionalOnBean(MinioClient.class)
    public MinioTemplate ossTemplate(MinioClient minioClient,MinioProperties minioProperties){
        return new MinioTemplateImpl(minioClient,minioProperties);
    }
}
