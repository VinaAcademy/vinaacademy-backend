package com.vinaacademy.platform.feature.storage.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.AbortableInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.transfer.s3.S3TransferManager;
import software.amazon.awssdk.transfer.s3.model.CompletedDirectoryUpload;
import software.amazon.awssdk.transfer.s3.model.UploadDirectoryRequest;

@ExtendWith(MockitoExtension.class)
class S3ServiceImplTest {

  @Mock private S3Client s3Client;
  @Mock private S3Presigner s3Presigner;
  private S3TransferManager s3TransferManager;

  private S3ServiceImpl service;

  @BeforeEach
  void setUp() {
    s3TransferManager = Mockito.mock(S3TransferManager.class, Mockito.RETURNS_DEEP_STUBS);
    service = new S3ServiceImpl(s3Client, s3Presigner, s3TransferManager);
    ReflectionTestUtils.setField(service, "bucketName", "test-bucket");
    ReflectionTestUtils.setField(service, "endpoint", "http://localhost:9000");
    ReflectionTestUtils.setField(service, "pathStyle", true);
  }

  @AfterEach
  void tearDown() {}

  @Test
  void uploadFile_withPath_success() throws IOException {
    Path tempFile = Files.createTempFile("test", ".txt");
    Files.writeString(tempFile, "hello", StandardCharsets.UTF_8);

    ArgumentCaptor<PutObjectRequest> reqCap = ArgumentCaptor.forClass(PutObjectRequest.class);
    when(s3Client.putObject(reqCap.capture(), any(RequestBody.class))).thenReturn(PutObjectResponse.builder().build());

    String url = service.uploadFile("folder/file.txt", tempFile, "text/plain");

    assertEquals("http://localhost:9000/test-bucket/folder/file.txt", url);
    PutObjectRequest req = reqCap.getValue();
    assertEquals("test-bucket", req.bucket());
    assertEquals("folder/file.txt", req.key());
    assertEquals("text/plain", req.contentType());

    Files.deleteIfExists(tempFile);
  }

  @Test
  void uploadFile_withStream_success() {
    byte[] data = "stream data".getBytes(StandardCharsets.UTF_8);
    InputStream is = new ByteArrayInputStream(data);

    ArgumentCaptor<PutObjectRequest> reqCap = ArgumentCaptor.forClass(PutObjectRequest.class);
    when(s3Client.putObject(reqCap.capture(), any(RequestBody.class)))
        .thenReturn(PutObjectResponse.builder().build());

    String url = service.uploadFile("folder/stream.bin", is, data.length, "application/octet-stream");

    assertEquals("http://localhost:9000/test-bucket/folder/stream.bin", url);
    PutObjectRequest req = reqCap.getValue();
    assertEquals("test-bucket", req.bucket());
    assertEquals("folder/stream.bin", req.key());
    assertEquals("application/octet-stream", req.contentType());
    assertEquals((Long) (long) data.length, req.contentLength());
  }

  @Test
  void downloadFile_success() throws Exception {
    byte[] data = "download".getBytes(StandardCharsets.UTF_8);
    ResponseInputStream<GetObjectResponse> respStream =
        new ResponseInputStream<>(
            GetObjectResponse.builder().build(),
            AbortableInputStream.create(new ByteArrayInputStream(data)));

    when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(respStream);

    try (InputStream got = service.downloadFile("a/b.txt")) {
      byte[] gotBytes = got.readAllBytes();
      assertThat(gotBytes).isEqualTo(data);
    }
  }

  @Test
  void generatePresignedUrl_success() throws Exception {
    PresignedGetObjectRequest presigned = org.mockito.Mockito.mock(PresignedGetObjectRequest.class);
    when(presigned.url()).thenReturn(new URL("http://localhost:9000/test-bucket/a/b.txt"));
    when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(presigned);

    String url = service.generatePresignedUrl("a/b.txt", 60);

    assertEquals("http://localhost:9000/test-bucket/a/b.txt", url);
  }

  @Test
  void deleteFile_success() {
    when(s3Client.deleteObject(any(DeleteObjectRequest.class)))
        .thenReturn(DeleteObjectResponse.builder().build());

    service.deleteFile("x/y.txt");

    verify(s3Client, times(1)).deleteObject(any(DeleteObjectRequest.class));
  }

  @Test
  void deleteDirectory_whenEmpty_nothingHappens() {
    ListObjectsV2Response listEmpty = ListObjectsV2Response.builder().contents(new ArrayList<>()).build();
    when(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(listEmpty);

    service.deleteDirectory("prefix/");

    verify(s3Client, times(0)).deleteObjects(any(DeleteObjectsRequest.class));
  }

  @Test
  void deleteDirectory_whenHasObjects_deletesAll() {
    S3Object o1 = S3Object.builder().key("prefix/a.txt").build();
    S3Object o2 = S3Object.builder().key("prefix/b.txt").build();
    ListObjectsV2Response listResp = ListObjectsV2Response.builder().contents(o1, o2).build();
    when(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(listResp);
    when(s3Client.deleteObjects(any(DeleteObjectsRequest.class)))
        .thenReturn(DeleteObjectsResponse.builder().build());

    service.deleteDirectory("prefix/");

    verify(s3Client, times(1)).deleteObjects(any(DeleteObjectsRequest.class));
  }

  @Test
  void fileExists_true() {
    when(s3Client.headObject(any(HeadObjectRequest.class))).thenReturn(HeadObjectResponse.builder().build());
    assertThat(service.fileExists("a/b")).isTrue();
  }

  @Test
  void fileExists_false_noSuchKey() {
    when(s3Client.headObject(any(HeadObjectRequest.class))).thenThrow(NoSuchKeyException.builder().build());
    assertThat(service.fileExists("a/b")).isFalse();
  }

  @Test
  void uploadDirectory_success_withPathStyleUrl() throws IOException {
    // Prepare temp directory with nested files
    Path dir = Files.createTempDirectory("upload-dir");
    Path sub = Files.createDirectories(dir.resolve("sub"));
    Files.writeString(dir.resolve("a.txt"), "A", StandardCharsets.UTF_8);
    Files.writeString(sub.resolve("b.txt"), "B", StandardCharsets.UTF_8);

    // Stub transfer manager deep chain: uploadDirectory(...).completionFuture() -> completed
    CompletedDirectoryUpload completed = Mockito.mock(CompletedDirectoryUpload.class);
    when(completed.failedTransfers()).thenReturn(List.of());
    when(s3TransferManager.uploadDirectory(any(UploadDirectoryRequest.class)).completionFuture())
        .thenReturn(CompletableFuture.completedFuture(completed));

    List<String> urls = service.uploadDirectory("videos/", dir);

    // Expect 2 files
    assertThat(urls).hasSize(2);
    assertThat(urls)
        .contains(
            "http://localhost:9000/test-bucket/videos/a.txt",
            "http://localhost:9000/test-bucket/videos/sub/b.txt");

    // cleanup
    Files.deleteIfExists(sub.resolve("b.txt"));
    Files.deleteIfExists(dir.resolve("a.txt"));
    Files.deleteIfExists(sub);
    Files.deleteIfExists(dir);
  }
}
