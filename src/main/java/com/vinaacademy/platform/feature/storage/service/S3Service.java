package com.vinaacademy.platform.feature.storage.service;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

public interface S3Service {
    
    /**
     * Upload a file to S3/MinIO
     * @param key the object key (path in bucket)
     * @param filePath local file path to upload
     * @param contentType MIME type of the file
     * @return the public URL of the uploaded file
     */
    String uploadFile(String key, Path filePath, String contentType);
    
    /**
     * Upload a file to S3/MinIO using InputStream
     * @param key the object key (path in bucket)
     * @param inputStream the input stream of the file
     * @param contentLength the content length
     * @param contentType MIME type of the file
     * @return the public URL of the uploaded file
     */
    String uploadFile(String key, InputStream inputStream, long contentLength, String contentType);
    
    /**
     * Upload multiple files in a directory to S3/MinIO
     * @param keyPrefix the prefix for all object keys
     * @param directoryPath the local directory path
     * @return list of uploaded file URLs
     */
    List<String> uploadDirectory(String keyPrefix, Path directoryPath);
    
    /**
     * Download a file from S3/MinIO as InputStream
     * @param key the object key
     * @return InputStream of the file
     */
    InputStream downloadFile(String key);
    
    /**
     * Generate a presigned URL for streaming
     * @param key the object key
     * @param expirationInSeconds expiration time in seconds
     * @return presigned URL
     */
    String generatePresignedUrl(String key, int expirationInSeconds);
    
    /**
     * Delete a file from S3/MinIO
     * @param key the object key
     */
    void deleteFile(String key);
    
    /**
     * Delete multiple files with the same prefix
     * @param keyPrefix the prefix of object keys to delete
     */
    void deleteDirectory(String keyPrefix);
    
    /**
     * Check if a file exists in S3/MinIO
     * @param key the object key
     * @return true if exists, false otherwise
     */
    boolean fileExists(String key);
}
