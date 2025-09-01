package com.vinaacademy.platform.feature.storage.service.impl;

import com.vinaacademy.platform.feature.storage.service.S3Service;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.transfer.s3.S3TransferManager;
import software.amazon.awssdk.transfer.s3.model.CompletedDirectoryUpload;
import software.amazon.awssdk.transfer.s3.model.UploadDirectoryRequest;
import software.amazon.awssdk.transfer.s3.model.UploadFileRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {

  private final S3Client s3Client;
  private final S3Presigner s3Presigner;
  private final S3TransferManager s3TransferManager;

  @Value("${minio.bucket}")
  private String bucketName;

  @Value("${minio.endpoint}")
  private String endpoint;

  @Value("${minio.path-style:true}")
  private boolean pathStyle;

  /**
   * Uploads a local file to the configured S3 bucket and returns its public URL.
   *
   * @param key         the object key to use in the bucket (including any desired prefix)
   * @param filePath    path to the local file to upload
   * @param contentType the MIME content type to set for the uploaded object
   * @return the public URL of the uploaded object (constructed from the configured endpoint, bucket, and key)
   * @throws RuntimeException if the upload fails
   */
  @Override
  public String uploadFile(String key, Path filePath, String contentType) {
    try {
      log.debug("Uploading file to S3: bucket={}, key={}, filePath={}", bucketName, key, filePath);
      PutObjectRequest putObjectRequest =
          PutObjectRequest.builder().bucket(bucketName).key(key).contentType(contentType).build();
      s3Client.putObject(putObjectRequest, RequestBody.fromFile(filePath));

      String publicUrl = String.format("%s/%s/%s", endpoint, bucketName, key);
      log.debug("Successfully uploaded file to S3: {}", publicUrl);
      return publicUrl;
    } catch (Exception e) {
      log.error(
          "Failed to upload file to S3: bucket={}, key={}, filePath={}, error={}",
          bucketName,
          key,
          filePath,
          e.getMessage(),
          e);
      throw new RuntimeException("Failed to upload file to S3", e);
    }
  }

  /**
   * Uploads data from an InputStream to the configured S3 bucket and returns a public URL for the stored object.
   *
   * <p>The provided stream is uploaded as an object identified by {@code key}. {@code contentLength} must be the
   * precise number of bytes to read from the stream and is used to build the request. {@code contentType} is set as
   * the object's MIME type (may be {@code null}).
   *
   * @param key destination object key (path) within the bucket
   * @param inputStream source stream containing the object data
   * @param contentLength exact length in bytes of the stream to upload
   * @param contentType MIME type to assign to the stored object; may be {@code null}
   * @return a publicly accessible URL for the uploaded object
   * @throws RuntimeException if the upload fails
   */
  @Override
  public String uploadFile(
      String key, InputStream inputStream, long contentLength, String contentType) {
    try {
      log.debug(
          "Uploading stream to S3: bucket={}, key={}, contentLength={}",
          bucketName,
          key,
          contentLength);

      PutObjectRequest putObjectRequest =
          PutObjectRequest.builder()
              .bucket(bucketName)
              .key(key)
              .contentType(contentType)
              .contentLength(contentLength)
              .build();

      s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, contentLength));

      String publicUrl = String.format("%s/%s/%s", endpoint, bucketName, key);
      log.debug("Successfully uploaded stream to S3: {}", publicUrl);
      return publicUrl;

    } catch (Exception e) {
      log.error(
          "Failed to upload stream to S3: bucket={}, key={}, error={}",
          bucketName,
          key,
          e.getMessage(),
          e);
      throw new RuntimeException("Failed to upload stream to S3", e);
    }
  }

  /**
   * Uploads all files under a local directory to the configured S3 bucket and returns the public URLs of the uploaded objects.
   *
   * The method normalizes the provided key prefix (ensuring it ends with '/'), uses the S3 Transfer Manager to perform
   * a parallel, multipart upload of the directory contents, sets a safe `Content-Type` for each file, waits for completion,
   * and logs any failed transfers. The returned URLs are constructed from the configured endpoint, bucket, and the uploaded
   * objects' relative paths (using forward slashes).
   *
   * @param keyPrefix     S3 key prefix under which files will be uploaded (a trailing slash will be appended if missing)
   * @param directoryPath local directory to upload (all regular files beneath this path will be uploaded, recursion enabled)
   * @return a list of public URLs for the uploaded objects, preserving the directory's relative structure
   * @throws RuntimeException if walking the local directory fails (wraps the underlying IOException)
   */
  @Override
  public List<String> uploadDirectory(String keyPrefix, Path directoryPath) {
    // Chuẩn hoá prefix
    final String prefix = keyPrefix.endsWith("/") ? keyPrefix : keyPrefix + "/";

    // 1) Dùng Transfer Manager để upload cả thư mục (song song + multipart)
    UploadDirectoryRequest req =
        UploadDirectoryRequest.builder()
            .bucket(bucketName)
            .source(directoryPath) // cần upload
            .s3Prefix(prefix) // TM tự ghép prefix + relativePath
            .maxDepth(Integer.MAX_VALUE)
            .followSymbolicLinks(false)
            // set Content-Type cho từng file khi putObject
            .uploadFileRequestTransformer(
                uploadBuilder -> {
                  UploadFileRequest uploadRequest = uploadBuilder.build();
                  Path file = uploadRequest.source();
                  String contentType = determineContentTypeSafe(file);
                  PutObjectRequest putObjectRequest =
                      uploadRequest.putObjectRequest().toBuilder().contentType(contentType).build();

                  uploadBuilder.putObjectRequest(putObjectRequest);
                })
            .build();

    CompletedDirectoryUpload result =
        s3TransferManager
            .uploadDirectory(req) // bean S3TransferManager đã cấu hình cho MinIO
            .completionFuture()
            .join();

    // 2) Log lỗi (nếu có)
    result
        .failedTransfers()
        .forEach(
            f ->
                log.error(
                    "Failed {}: {}",
                    f.request().source(),
                    f.exception().getMessage(),
                    f.exception()));

    // 3) Trả về danh sách URL các object đã up (dựa trên relative path)
    try (Stream<Path> paths = Files.walk(directoryPath)) {
      return paths
          .filter(Files::isRegularFile)
          .map(p -> directoryPath.relativize(p).toString().replace("\\", "/"))
          .map(rel -> buildObjectUrl(prefix + rel))
          .toList();
    } catch (IOException e) {
      throw new RuntimeException("Failed to walk directory: " + directoryPath, e);
    }
  }

  /**
   * Returns a safe MIME content type for the given file path, falling back to
   * "application/octet-stream" when the type is unknown or cannot be determined.
   *
   * @param p the file system path to inspect
   * @return the determined content type, or "application/octet-stream" if probing
   *         fails or yields no usable result
   */
  private static String determineContentTypeSafe(Path p) {
    try {
      String ct = Files.probeContentType(p);
      return (ct == null || ct.isBlank()) ? "application/octet-stream" : ct;
    } catch (IOException e) {
      return "application/octet-stream";
    }
  }

  /**
   * Build the public URL for an object key using the configured endpoint and addressing style.
   *
   * <p>If path-style addressing is enabled, the URL will be:
   *   <code>{endpoint}/{bucket}/{key}</code>.
   * If virtual-host (bucket-in-host) addressing is used, the URL will be:
   *   <code>{scheme}://{bucket}.{host}{:port}/{key}</code>.
   *
   * <p>Assumes the configured endpoint has the form <code>scheme://host[:port]</code> with no
   * trailing slash. Virtual-host style requires DNS/host routing that resolves
   * <code>{bucket}.{host}</code> to the same S3-compatible service.
   *
   * @param key object key/path to append to the generated URL
   * @return fully qualified HTTP(S) URL for the object
   */
  private String buildObjectUrl(String key) {
    // endpoint dạng https://host:port (không có trailing slash)
    if (pathStyle) {
      return String.format("%s/%s/%s", endpoint, bucketName, key);
    } else {
      // virtual-host style (cần DNS/host phù hợp)
      // https://bucket.minio.local:9000/key
      URI ep = URI.create(endpoint);
      String host = ep.getHost();
      String schemeAndPort =
          ep.getScheme() + "://" + (ep.getPort() > 0 ? host + ":" + ep.getPort() : host);
      return String.format("%s/%s", schemeAndPort.replace(host, bucketName + "." + host), key);
    }
  }

  /**
   * Downloads an object from the configured bucket and returns its content as an InputStream.
   *
   * @param key the object key (path) within the bucket to download
   * @return an InputStream for the object's data; the caller is responsible for closing the stream
   * @throws RuntimeException if the object cannot be retrieved
   */
  @Override
  public InputStream downloadFile(String key) {
    try {
      log.debug("Downloading file from S3: bucket={}, key={}", bucketName, key);

      GetObjectRequest getObjectRequest =
          GetObjectRequest.builder().bucket(bucketName).key(key).build();

      return s3Client.getObject(getObjectRequest);

    } catch (Exception e) {
      log.error(
          "Failed to download file from S3: bucket={}, key={}, error={}",
          bucketName,
          key,
          e.getMessage(),
          e);
      throw new RuntimeException("Failed to download file from S3", e);
    }
  }

  /**
   * Generate a presigned GET URL for an object stored in the configured bucket.
   *
   * The returned URL grants temporary, time-limited access to the object identified
   * by `key`. The URL will expire after `expirationInSeconds` seconds.
   *
   * @param key the object key (path) in the bucket
   * @param expirationInSeconds lifetime of the presigned URL in seconds
   * @return a presigned GET URL as a string
   * @throws RuntimeException if presigned URL generation fails
   */
  @Override
  public String generatePresignedUrl(String key, int expirationInSeconds) {
    try {
      log.debug(
          "Generating presigned URL: bucket={}, key={}, expiration={}",
          bucketName,
          key,
          expirationInSeconds);

      GetObjectRequest getObjectRequest =
          GetObjectRequest.builder().bucket(bucketName).key(key).build();

      GetObjectPresignRequest presignRequest =
          GetObjectPresignRequest.builder()
              .signatureDuration(Duration.ofSeconds(expirationInSeconds))
              .getObjectRequest(getObjectRequest)
              .build();

      PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
      String url = presignedRequest.url().toString();

      log.debug("Generated presigned URL: {}", url);
      return url;

    } catch (Exception e) {
      log.error(
          "Failed to generate presigned URL: bucket={}, key={}, error={}",
          bucketName,
          key,
          e.getMessage(),
          e);
      throw new RuntimeException("Failed to generate presigned URL", e);
    }
  }

  /**
   * Delete the object identified by the given key from the configured S3 bucket.
   *
   * @param key the object key (path) in the bucket to delete
   * @throws RuntimeException if the deletion fails
   */
  @Override
  public void deleteFile(String key) {
    try {
      log.debug("Deleting file from S3: bucket={}, key={}", bucketName, key);

      DeleteObjectRequest deleteObjectRequest =
          DeleteObjectRequest.builder().bucket(bucketName).key(key).build();

      s3Client.deleteObject(deleteObjectRequest);
      log.debug("Successfully deleted file from S3: {}", key);

    } catch (Exception e) {
      log.error(
          "Failed to delete file from S3: bucket={}, key={}, error={}",
          bucketName,
          key,
          e.getMessage(),
          e);
      throw new RuntimeException("Failed to delete file from S3", e);
    }
  }

  /**
   * Deletes all objects in the configured bucket that have keys starting with the provided prefix.
   *
   * The method lists objects matching the prefix and issues a single bulk delete request for those
   * objects. If no objects are found for the prefix, the method returns without error.
   *
   * @param keyPrefix prefix of object keys to remove (treated as a path-like prefix)
   * @throws RuntimeException if listing or deletion from the S3 bucket fails
   */
  @Override
  public void deleteDirectory(String keyPrefix) {
    try {
      log.debug("Deleting directory from S3: bucket={}, keyPrefix={}", bucketName, keyPrefix);

      // List all objects with the prefix
      ListObjectsV2Request listRequest =
          ListObjectsV2Request.builder().bucket(bucketName).prefix(keyPrefix).build();

      ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);

      if (listResponse.contents().isEmpty()) {
        log.debug("No objects found with prefix: {}", keyPrefix);
        return;
      }

      // Create delete request
      List<ObjectIdentifier> objectsToDelete =
          listResponse.contents().stream()
              .map(s3Object -> ObjectIdentifier.builder().key(s3Object.key()).build())
              .toList();

      Delete delete = Delete.builder().objects(objectsToDelete).build();

      DeleteObjectsRequest deleteRequest =
          DeleteObjectsRequest.builder().bucket(bucketName).delete(delete).build();

      s3Client.deleteObjects(deleteRequest);
      log.debug(
          "Successfully deleted {} objects with prefix: {}", objectsToDelete.size(), keyPrefix);

    } catch (Exception e) {
      log.error(
          "Failed to delete directory from S3: bucket={}, keyPrefix={}, error={}",
          bucketName,
          keyPrefix,
          e.getMessage(),
          e);
      throw new RuntimeException("Failed to delete directory from S3", e);
    }
  }

  /**
   * Checks whether an object with the given key exists in the configured bucket.
   *
   * @param key the object key to check
   * @return true if the object exists; false if the object does not exist
   * @throws RuntimeException if an unexpected error occurs while checking existence
   */
  @Override
  public boolean fileExists(String key) {
    try {
      HeadObjectRequest headObjectRequest =
          HeadObjectRequest.builder().bucket(bucketName).key(key).build();

      s3Client.headObject(headObjectRequest);
      return true;

    } catch (NoSuchKeyException e) {
      return false;
    } catch (Exception e) {
      log.error(
          "Failed to check file existence: bucket={}, key={}, error={}",
          bucketName,
          key,
          e.getMessage(),
          e);
      throw new RuntimeException("Failed to check file existence", e);
    }
  }
}
