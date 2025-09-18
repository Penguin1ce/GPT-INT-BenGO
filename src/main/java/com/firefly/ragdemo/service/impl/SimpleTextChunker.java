package com.firefly.ragdemo.service.impl;

import com.firefly.ragdemo.service.TextChunker;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SimpleTextChunker implements TextChunker {

    private static final int CHUNK_SIZE = 800;
    private static final int CHUNK_OVERLAP = 100;

    @Override
    public List<String> split(String text) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isBlank()) return chunks;
        String normalized = text.replaceAll("\r\n?", "\n");
        // 先按段落再做滑窗
        String[] paras = normalized.split("\n\n+");
        StringBuilder buffer = new StringBuilder();
        for (String p : paras) {
            if (buffer.length() + p.length() + 1 <= CHUNK_SIZE) {
                if (buffer.length() > 0) buffer.append('\n');
                buffer.append(p);
            } else {
                if (buffer.length() > 0) {
                    slideAndCollect(chunks, buffer.toString());
                    buffer.setLength(0);
                }
                if (p.length() <= CHUNK_SIZE) {
                    slideAndCollect(chunks, p);
                } else {
                    // 长段落分割
                    for (int i = 0; i < p.length(); i += (CHUNK_SIZE - CHUNK_OVERLAP)) {
                        int end = Math.min(p.length(), i + CHUNK_SIZE);
                        chunks.add(p.substring(i, end));
                    }
                }
            }
        }
        if (buffer.length() > 0) slideAndCollect(chunks, buffer.toString());
        return chunks;
    }

    private void slideAndCollect(List<String> chunks, String text) {
        if (text.length() <= CHUNK_SIZE) {
            chunks.add(text);
            return;
        }
        for (int i = 0; i < text.length(); i += (CHUNK_SIZE - CHUNK_OVERLAP)) {
            int end = Math.min(text.length(), i + CHUNK_SIZE);
            chunks.add(text.substring(i, end));
        }
    }
} 