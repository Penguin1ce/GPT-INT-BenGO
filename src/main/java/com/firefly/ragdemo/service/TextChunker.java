package com.firefly.ragdemo.service;

import java.util.List;

public interface TextChunker {

    List<String> split(String text);
} 