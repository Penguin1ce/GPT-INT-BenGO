package com.firefly.ragdemo.service.impl;

import com.firefly.ragdemo.entity.DocumentChunk;
import com.firefly.ragdemo.mapper.DocumentChunkMapper;
import com.firefly.ragdemo.service.EmbeddingService;
import com.firefly.ragdemo.service.RagRetrievalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class RagRetrievalServiceImpl implements RagRetrievalService {

    private final DocumentChunkMapper documentChunkMapper;
    private final EmbeddingService embeddingService;

    @Override
    public List<String> retrieveContext(String userId, String query, int topK, int candidateLimit) {
        List<Double> q = embeddingService.embed(query);
        String qJson = toJsonArray(q);
        List<DocumentChunk> chunks = documentChunkMapper.searchTopKByUser(userId, qJson, Math.max(topK, 3));
        List<String> results = new ArrayList<>();
        for (int i = 0; i < Math.min(topK, chunks.size()); i++) {
            results.add(chunks.get(i).getContent());
        }
        return results;
    }

    private String toJsonArray(List<Double> vec) {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < vec.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append(String.format(Locale.US, "%.8f", vec.get(i)));
        }
        sb.append(']');
        return sb.toString();
    }
} 