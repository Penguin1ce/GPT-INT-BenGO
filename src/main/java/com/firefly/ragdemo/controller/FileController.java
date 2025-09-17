package com.firefly.ragdemo.controller;

import com.firefly.ragdemo.VO.ApiResponse;
import com.firefly.ragdemo.VO.FileVO;
import com.firefly.ragdemo.entity.User;
import com.firefly.ragdemo.secutiry.CustomUserPrincipal;
import com.firefly.ragdemo.service.FileService;
import com.firefly.ragdemo.util.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<FileVO>> uploadFile(@RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("请选择要上传的文件", 400));
        }

        try {
            User user = principal.getUser();
            FileVO fileVO = fileService.uploadFile(file, user);

            ApiResponse<FileVO> response = ApiResponse.success("文件上传成功，开始处理", fileVO);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("文件上传验证失败 for user {}: {}", principal.getUserId(), e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), 400));

        } catch (Exception e) {
            log.error("文件上传失败 for user {}: {}", principal.getUserId(), e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("文件上传失败"));
        }
    }

    @GetMapping("/files")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getFiles(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        try {
            String userId = principal.getUserId();
            PageResult<FileVO> pageResult = fileService.getUserFiles(userId, page, limit);

            List<FileVO> files = pageResult.getItems();

            Map<String, Object> pagination = new HashMap<>();
            pagination.put("page", pageResult.getPage());
            pagination.put("limit", pageResult.getLimit());
            pagination.put("total", pageResult.getTotal());
            pagination.put("totalPages", pageResult.getTotalPages());

            Map<String, Object> data = new HashMap<>();
            data.put("files", files);
            data.put("pagination", pagination);

            ApiResponse<Map<String, Object>> response = ApiResponse.success("获取文件列表成功", data);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("获取文件列表失败 for user {}: {}", principal.getUserId(), e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("获取文件列表失败"));
        }
    }
}
