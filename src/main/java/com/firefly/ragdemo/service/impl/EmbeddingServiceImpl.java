package com.firefly.ragdemo.service.impl;

import com.firefly.ragdemo.service.EmbeddingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmbeddingServiceImpl implements EmbeddingService {

    private final OpenAiEmbeddingModel embeddingModel;

    @Override
    public List<Double> embed(String text) {
        try {
            float[] embedding = embeddingModel.embed(text);
            return java.util.stream.IntStream.range(0, embedding.length)
                    .mapToDouble(i -> embedding[i])
                    .boxed()
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Embedding failed", e);
            throw new RuntimeException("Embedding失败: " + e.getMessage());
        }
    }

    @Override
    public List<List<Double>> embedBatch(List<String> texts) {
        List<List<Double>> result = new ArrayList<>();
        for (String t : texts) {
            result.add(embed(t));
        }
        return result;
    }
} 