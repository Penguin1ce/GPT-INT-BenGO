package com.firefly.ragdemo.service;

import com.firefly.ragdemo.VO.FileVO;
import com.firefly.ragdemo.entity.UploadedFile;
import com.firefly.ragdemo.entity.User;
import com.firefly.ragdemo.util.PageResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

public interface FileService {

    FileVO uploadFile(MultipartFile file, User user) throws IOException;

    PageResult<FileVO> getUserFiles(String userId, int page, int limit);

    Optional<UploadedFile> findById(String fileId);
}
