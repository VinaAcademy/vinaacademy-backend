package com.vinaacademy.platform.feature.storage.service;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

public interface S3Service {
    
    /**
 * Uploads a local file to S3/MinIO and returns its public URL.
 *
 * @param key the destination object key (path) to store the file under in the bucket
 * @param filePath the local file system path of the file to upload
 * @param contentType the MIME type of the file being uploaded
 * @return the public URL of the uploaded object
 */
    String uploadFile(String key, Path filePath, String contentType);
    
    /**
 * Uploads data from an InputStream to S3/MinIO and returns the public URL of the stored object.
 *
 * @param key the object key (path within the bucket)
 * @param inputStream stream providing the object's bytes; must be positioned at the start of the content
 * @param contentLength length of the content in bytes
 * @param contentType MIME type of the uploaded object (e.g., "image/png", "application/pdf")
 * @return the public URL where the uploaded object can be accessed
 */
    String uploadFile(String key, InputStream inputStream, long contentLength, String contentType);
    
    /**
 * Uploads all files found under the given local directory to S3/MinIO using the provided key prefix.
 *
 * The implementation is expected to iterate the directory (including subdirectories), create object keys by
 * joining {@code keyPrefix} with each file's relative path, and upload each file, returning their public URLs.
 *
 * @param keyPrefix     prefix to prepend to each object's key in the bucket (may be empty)
 * @param directoryPath local directory whose files will be uploaded
 * @return              a list of public URLs for the uploaded files
 */
    List<String> uploadDirectory(String keyPrefix, Path directoryPath);
    
    /**
 * Returns an InputStream to read the object identified by the given key from S3/MinIO.
 *
 * The returned stream provides the object's raw bytes and must be closed by the caller when
 * finished to release underlying resources.
 *
 * @param key the object key (path/name) in the storage bucket
 * @return an InputStream for reading the object's data
 */
    InputStream downloadFile(String key);
    
    /**
 * Creates a presigned URL that grants time-limited, streamable access to the object identified by the given key.
 *
 * The returned URL is valid for the specified number of seconds and can be used to stream or download the object
 * without requiring direct credentials.
 *
 * @param key the object key in the bucket
 * @param expirationInSeconds duration in seconds that the presigned URL remains valid
 * @return a presigned HTTP URL for accessing the object
 */
    String generatePresignedUrl(String key, int expirationInSeconds);
    
    /**
 * Delete the object identified by the given key from S3/MinIO.
 *
 * <p>The key is the object's S3 path within the configured bucket (for example
 * "folder/subfolder/file.txt"). Implementations perform the removal operation
 * against the configured storage backend.
 *
 * @param key the S3 object key (path) to delete
 */
    void deleteFile(String key);
    
    /**
 * Delete all objects whose keys start with the given prefix.
 *
 * <p>If no objects match the prefix the method performs no action.</p>
 *
 * @param keyPrefix prefix of object keys to delete (all objects with keys that begin with this value)
 */
    void deleteDirectory(String keyPrefix);
    
    /**
 * Returns true if an object with the given S3/MinIO key exists.
 *
 * @param key the object's storage key (path) to check
 * @return true if the object exists, false otherwise
 */
    boolean fileExists(String key);
}
