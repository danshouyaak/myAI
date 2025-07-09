package com.myAI.myAI.service;

import com.myAI.myAI.models.vo.FileVO;
import org.springframework.web.multipart.MultipartFile;

public interface UploadService {
    FileVO upload(MultipartFile file);
}
