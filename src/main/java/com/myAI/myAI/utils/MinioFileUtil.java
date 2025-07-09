package com.myAI.myAI.utils;

import com.myAI.myAI.common.ErrorCode;
import com.myAI.myAI.config.MinioConfig;
import com.myAI.myAI.exception.BusinessException;
import io.minio.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;

@Configuration
@Slf4j
public class MinioFileUtil {

    @Resource
    MinioConfig minioConfig;

    @Resource
    MinioClient minioClient;

    /**
     * 创建桶
     *
     * @param bucketName 桶名称
     */
    public void createBucket(String bucketName) throws Exception {
        if (!StringUtils.hasLength(bucketName)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "桶名不能为");
        }

        minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
    }

    /**
     * 创建文件夹
     *
     * @param bucketName 桶名
     * @param folderName 文件夹名称
     * @return
     * @throws Exception
     */
    public ObjectWriteResponse createBucketFolder(String bucketName, String folderName) throws Exception {

        if (!checkBucketExist(bucketName)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "桶不存在，无法创建文件夹");
        }
        if (!StringUtils.hasLength(folderName)) {
            throw new RuntimeException("创建的文件夹名不能为空");
        }
        PutObjectArgs putObjectArgs = PutObjectArgs.builder().bucket(bucketName).object(folderName + "/").stream(new ByteArrayInputStream(new byte[0]), 0, 0).build();
        ObjectWriteResponse objectWriteResponse = minioClient.putObject(putObjectArgs);


        return objectWriteResponse;
    }


    /**
     * 检查桶是否存在
     *
     * @param bucketName 桶名称
     * @return boolean true-存在 false-不存在
     */
    public boolean checkBucketExist(String bucketName) throws Exception {
        if (!StringUtils.hasLength(bucketName)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "桶名不能为空");
        }

        return minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
    }

    /**
     * 删除文件
     *
     * @param bucketName 桶名
     * @param objectName 文件所在的路径 + 文件名称  例如100000/im/750510573649620992.xlsx
     * @return
     */
    public Boolean deleteBucketFile(String bucketName, String objectName) {
        if (!StringUtils.hasLength(bucketName) || !StringUtils.hasLength(objectName)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "桶名或文件名不能为空");

        }
        try {
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(objectName).build());
            return true;
        } catch (Exception e) {
            log.info("删除文件失败");
            return false;
        }
    }


}