package com.myAI.myAI.service.Impl;

import com.myAI.myAI.common.ErrorCode;
import com.myAI.myAI.config.MinioConfig;
import com.myAI.myAI.exception.BusinessException;
import com.myAI.myAI.models.vo.FileVO;
import com.myAI.myAI.service.UploadService;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class MinioSysFileServiceImpl implements UploadService {

    // 注入minio配置文件
    @Autowired
    private MinioConfig minioConfig;

    // 注入minio client
    @Autowired
    private MinioClient client;

    @Override
    public FileVO upload(MultipartFile file) {
        FileVO fileVO = new FileVO();
        try {
            // 获取文件真实名称
            String originalFilename = file.getOriginalFilename();
            // 获取文件的扩展名 例如.jpg .doc
            String extname = originalFilename.substring(originalFilename.lastIndexOf("."));
//            // 生成新的文件名 防止文件覆盖
            String newFileName = System.currentTimeMillis() + extname;
            // 构建文件上传相关信息
            PutObjectArgs args = PutObjectArgs.builder().bucket(minioConfig.getBucketName()).object(newFileName).stream(file.getInputStream(), file.getSize(), -1).contentType(file.getContentType()).build();
            // 将文件上传到minio服务器
            client.putObject(args);
            log.info("文件上传成功");
            // 组装文件信息，返回前端 或者存入数据路
            String url = minioConfig.getUrl() + "/" + minioConfig.getBucketName() + "/" + newFileName;
            fileVO.setUrl(url);
            fileVO.setSize(file.getSize());
            fileVO.setFileName(newFileName);
            fileVO.setExtname(extname);
        } catch (Exception e) {
            String errorMessage = "文件上传异常";
            if (e.getCause() != null) {
                errorMessage += ": " + e.getCause().toString();
            }
            throw new BusinessException(ErrorCode.OPERATION_ERROR, errorMessage);
        }
        return fileVO;
    }
}