package com.myAI.myAI.controller;

import com.myAI.myAI.common.BaseResponse;
import com.myAI.myAI.common.ErrorCode;
import com.myAI.myAI.common.ResultUtils;
import com.myAI.myAI.exception.BusinessException;
import com.myAI.myAI.models.vo.FileVO;
import com.myAI.myAI.service.UploadService;
import com.myAI.myAI.utils.MinioFileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;

@RestController
@RequestMapping("/file")
public class FileController {

    @Resource
    private UploadService uploadService;

    @Resource
    private MinioFileUtil minioFileUtil;

    @PostMapping("/uploadPhoto")
    public BaseResponse<FileVO> uploadPhoto(@RequestParam("file") MultipartFile file) throws Exception {

        if (file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件为空");
        }
//        获取原始文件名
        String fileOriginalFilename = file.getOriginalFilename();
//        获取文件后缀名
        String substring = fileOriginalFilename.substring(fileOriginalFilename.lastIndexOf("."));
        if (!".png".equals(substring) && !".jpg".equals(substring) && !".jpeg".equals(substring)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件格式错误");
        }

        FileVO upload = uploadService.upload(file);
        return ResultUtils.success(upload);
    }
}