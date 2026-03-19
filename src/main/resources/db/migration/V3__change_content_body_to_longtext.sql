-- V3: Change content_body to LONGTEXT for storing rich HTML content
ALTER TABLE articles MODIFY COLUMN content_body LONGTEXT;
