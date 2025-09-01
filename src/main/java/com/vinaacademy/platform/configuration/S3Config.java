package com.vinaacademy.platform.configuration;

import java.net.URI;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.checksums.RequestChecksumCalculation;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.transfer.s3.S3TransferManager;

@Configuration
public class S3Config {

  @Value("${minio.endpoint}")
  private String endpoint; // e.g. http://localhost:9000 or https://minio.mycorp.com

  @Value("${minio.access-key}")
  private String accessKey;

  @Value("${minio.secret-key}")
  private String secretKey;

  @Value("${minio.region:us-east-1}")
  private String region;

  @Value("${minio.path-style:true}")
  private boolean pathStyle;

  private S3Configuration s3Cfg() {
    return S3Configuration.builder().pathStyleAccessEnabled(pathStyle).build();
  }

  private StaticCredentialsProvider creds() {
    return StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));
  }

  /** Synchronous S3 client (good for metadata, small ops) */
  @Bean
  public S3Client s3Client() {
    return S3Client.builder()
        .endpointOverride(URI.create(endpoint))
        .region(Region.of(region))
        .serviceConfiguration(s3Cfg())
        .credentialsProvider(creds())
        // Avoid sending optional checksum headers that some S3-compatible stores may not accept
        .requestChecksumCalculation(RequestChecksumCalculation.WHEN_REQUIRED)
        .overrideConfiguration(
            ClientOverrideConfiguration.builder()
                .apiCallTimeout(Duration.ofMinutes(2))
                .apiCallAttemptTimeout(Duration.ofSeconds(30))
                .build())
        .build();
  }

  /** Async client (required for Transfer Manager, best performance for batch) */
  @Bean
  public S3AsyncClient s3AsyncClient() {
    // Use the default netty async client; CRT works too, but start simple with MinIO.
    return S3AsyncClient.builder()
        .endpointOverride(URI.create(endpoint))
        .region(Region.of(region))
        .serviceConfiguration(s3Cfg())
        .credentialsProvider(creds())
        .requestChecksumCalculation(RequestChecksumCalculation.WHEN_REQUIRED)
        .overrideConfiguration(
            ClientOverrideConfiguration.builder()
                .apiCallTimeout(Duration.ofMinutes(5))
                .apiCallAttemptTimeout(Duration.ofSeconds(30))
                .build())
        .build();
  }

  /** High-throughput parallel uploads/downloads */
  @Bean
  public S3TransferManager s3TransferManager(S3AsyncClient async) {
    return S3TransferManager.builder().s3Client(async).build();
  }

  /** Presigner for GET/PUT pre-signed URLs that target MinIO */
  @Bean
  public S3Presigner s3Presigner() {
    return S3Presigner.builder()
        .endpointOverride(URI.create(endpoint))
        .region(Region.of(region))
        .credentialsProvider(creds())
        .serviceConfiguration(s3Cfg())
        .build();
  }
}
