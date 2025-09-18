package com.firefly.ragdemo.service;

import java.util.List;

public interface RagRetrievalService {

    List<String> retrieveContext(String userId, String query, int topK, int candidateLimit);
} 