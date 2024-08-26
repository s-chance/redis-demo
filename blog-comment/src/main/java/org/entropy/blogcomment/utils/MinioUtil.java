package org.entropy.blogcomment.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.UUID;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.apache.commons.lang3.StringUtils;
import org.entropy.blogcomment.config.MinioConfig;
import org.entropy.blogcomment.pojo.Result;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Component
public class MinioUtil {

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;

    public MinioUtil(MinioClient minioClient, MinioConfig minioConfig) {
        this.minioClient = minioClient;
        this.minioConfig = minioConfig;
    }

    public Result<?> upload(MultipartFile file) {
        try {
            String bucketName = minioConfig.getBucketName();
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }

            String originalFilename = file.getOriginalFilename();
            if (StringUtils.isBlank(originalFilename)) {
                return Result.failure("文件不能为空");
            }

            String dir = DateUtil.format(LocalDateTime.now(), "yyyy-MM-dd");
            String uuid = UUID.randomUUID().toString(true);
            String objectName = dir + "/" + uuid + "-" + originalFilename;

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            return Result.success("上传成功",
                    minioConfig.getEndpoint() + "/" + minioConfig.getBucketName() + "/" + objectName);

        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }
}
