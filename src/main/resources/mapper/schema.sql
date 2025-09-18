-- uploaded_files 表（若不存在）
CREATE TABLE IF NOT EXISTS uploaded_files (
    id VARCHAR(64) PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL,
    filename TEXT NOT NULL,
    file_path TEXT NOT NULL,
    file_size BIGINT,
    file_type VARCHAR(32),
    upload_time TIMESTAMP,
    status VARCHAR(32)
);

-- 文档分块表（RAG）
CREATE TABLE IF NOT EXISTS document_chunks (
    id VARCHAR(64) PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL,
    file_id VARCHAR(64) NOT NULL,
    chunk_index INT NOT NULL,
    content TEXT,
    embedding JSONB,
    created_at TIMESTAMP,
    CONSTRAINT fk_file FOREIGN KEY (file_id) REFERENCES uploaded_files (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_document_chunks_user ON document_chunks(user_id);
CREATE INDEX IF NOT EXISTS idx_document_chunks_file ON document_chunks(file_id); 