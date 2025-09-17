package com.firefly.ragdemo.service;

import com.firefly.ragdemo.VO.FileVO;
import com.firefly.ragdemo.VO.PaginationVO;
import com.firefly.ragdemo.entity.UploadedFile;
import com.firefly.ragdemo.entity.User;
import com.firefly.ragdemo.mapper.UploadedFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileService {

    private final UploadedFileRepository uploadedFileRepository;

    @Value("${app.file.upload-dir:uploads}")
    private String uploadDir;

    private final List<String> ALLOWED_EXTENSIONS = Arrays.asList("txt", "md", "pdf", "docx");
    private final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    @Transactional
    public FileVO uploadFile(MultipartFile file, User user) throws IOException {
        // 验证文件
        validateFile(file);

        // 创建上传目录
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String newFilename = UUID.randomUUID().toString() + "." + extension;
        Path filePath = uploadPath.resolve(newFilename);

        // 保存文件
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // 保存文件记录到数据库
        UploadedFile uploadedFile = UploadedFile.builder()
                .user(user)
                .filename(originalFilename)
                .filePath(filePath.toString())
                .fileSize(file.getSize())
                .fileType(extension)
                .status(UploadedFile.FileStatus.PROCESSING)
                .build();

        UploadedFile savedFile = uploadedFileRepository.save(uploadedFile);

        // 异步处理文件（这里可以添加文件处理逻辑，如向量化等）
        processFileAsync(savedFile.getId());

        return FileVO.builder()
                .id(savedFile.getId())
                .filename(savedFile.getFilename())
                .fileSize(savedFile.getFileSize())
                .fileType(savedFile.getFileType())
                .uploadTime(savedFile.getUploadTime())
                .status(savedFile.getStatus())
                .build();
    }

    @Transactional(readOnly = true)
    public Page<FileVO> getUserFiles(String userId, int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit); // Spring Data JPA使用0索引
        Page<UploadedFile> files = uploadedFileRepository.findByUserIdOrderByUploadTimeDesc(userId, pageable);

        return files.map(file -> FileVO.builder()
                .id(file.getId())
                .filename(file.getFilename())
                .fileSize(file.getFileSize())
                .fileType(file.getFileType())
                .uploadTime(file.getUploadTime())
                .status(file.getStatus())
                .build());
    }

    public PaginationVO createPagination(Page<?> page) {
        return PaginationVO.builder()
                .page(page.getNumber() + 1) // 转换为1索引
                .limit(page.getSize())
                .total(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }

    @Transactional(readOnly = true)
    public Optional<UploadedFile> findById(String fileId) {
        return uploadedFileRepository.findById(fileId);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("文件大小不能超过10MB");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new IllegalArgumentException("文件名不能为空");
        }

        String extension = getFileExtension(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("不支持的文件类型，支持的类型: " + String.join(", ", ALLOWED_EXTENSIONS));
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }

        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex + 1);
        }

        return "";
    }

    private void processFileAsync(String fileId) {
        // 这里可以添加异步文件处理逻辑
        // 例如：文档解析、向量化、索引等
        log.info("开始异步处理文件: {}", fileId);

        // 模拟处理完成
        new Thread(() -> {
            try {
                Thread.sleep(2000); // 模拟处理时间

                // 更新文件状态为已完成
                Optional<UploadedFile> fileOpt = uploadedFileRepository.findById(fileId);
                if (fileOpt.isPresent()) {
                    UploadedFile file = fileOpt.get();
                    file.setStatus(UploadedFile.FileStatus.COMPLETED);
                    uploadedFileRepository.save(file);
                    log.info("文件处理完成: {}", fileId);
                }
            } catch (Exception e) {
                log.error("文件处理失败: {}", fileId, e);

                // 更新文件状态为失败
                Optional<UploadedFile> fileOpt = uploadedFileRepository.findById(fileId);
                if (fileOpt.isPresent()) {
                    UploadedFile file = fileOpt.get();
                    file.setStatus(UploadedFile.FileStatus.FAILED);
                    uploadedFileRepository.save(file);
                }
            }
        }).start();
    }
}
