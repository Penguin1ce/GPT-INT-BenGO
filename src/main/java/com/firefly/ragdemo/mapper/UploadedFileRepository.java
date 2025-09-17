package com.firefly.ragdemo.mapper;

import com.firefly.ragdemo.entity.UploadedFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UploadedFileRepository extends JpaRepository<UploadedFile, String> {

    @Query("SELECT uf FROM UploadedFile uf WHERE uf.user.id = :userId ORDER BY uf.uploadTime DESC")
    Page<UploadedFile> findByUserIdOrderByUploadTimeDesc(@Param("userId") String userId, Pageable pageable);

    long countByUserId(String userId);
}
