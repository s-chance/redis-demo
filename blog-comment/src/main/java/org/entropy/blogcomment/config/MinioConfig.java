package org.entropy.blogcomment.config;

import io.minio.MinioClient;
import io.minio.SetBucketPolicyArgs;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "minio")
@Data
public class MinioConfig {
    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String bucketName;

    @Bean
    public MinioClient minioClient() {
        MinioClient minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
        String config = """
                {
                     "Statement": [
                         {
                             "Action": [
                                 "s3:GetBucketLocation",
                                 "s3:ListBucket"
                             ],
                             "Effect": "Allow",
                             "Principal": "*",
                             "Resource": "arn:aws:s3:::%s"
                         },
                         {
                             "Action": "s3:GetObject",
                             "Effect": "Allow",
                             "Principal": "*",
                             "Resource": "arn:aws:s3:::%s/**"
                         }
                     ],
                     "Version": "2012-10-17"
                 }
                """.formatted(bucketName, bucketName);
        try {
            minioClient.setBucketPolicy(SetBucketPolicyArgs.builder().bucket(bucketName).config(config).build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return minioClient;
    }
}
