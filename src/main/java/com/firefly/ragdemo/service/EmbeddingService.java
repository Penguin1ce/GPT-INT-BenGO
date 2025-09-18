package com.firefly.ragdemo.service;

import java.util.List;

public interface EmbeddingService {

    List<Double> embed(String text);

    List<List<Double>> embedBatch(List<String> texts);
} 