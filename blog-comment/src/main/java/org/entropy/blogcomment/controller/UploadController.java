package org.entropy.blogcomment.controller;

import org.entropy.blogcomment.pojo.Result;
import org.entropy.blogcomment.utils.MinioUtil;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/upload")
public class UploadController {

    private final MinioUtil minioUtil;

    public UploadController(MinioUtil minioUtil) {
        this.minioUtil = minioUtil;
    }

    @PostMapping("/image")
    public Result<?> uploadImage(@RequestParam("file") MultipartFile file) {
        return minioUtil.upload(file);
    }
}
