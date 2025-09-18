package com.firefly.ragdemo.mapper;

import com.firefly.ragdemo.entity.DocumentChunk;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DocumentChunkMapper {

    int insertBatch(@Param("chunks") List<DocumentChunk> chunks);

    List<DocumentChunk> searchTopKByUser(@Param("userId") String userId,
                                         @Param("queryEmbeddingJson") String queryEmbeddingJson,
                                         @Param("topK") int topK);

    int deleteByFileIdAndUser(@Param("fileId") String fileId, @Param("userId") String userId);
} 