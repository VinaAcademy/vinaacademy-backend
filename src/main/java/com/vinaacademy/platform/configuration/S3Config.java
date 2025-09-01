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

  /**
   * Create an S3Configuration with path-style addressing set from the configured flag.
   *
   * This configures S3 clients to use path-style access when the injected `pathStyle` property
   * is true (required for many MinIO deployments).
   *
   * @return an S3Configuration with pathStyleAccessEnabled set to the `pathStyle` field
   */
  private S3Configuration s3Cfg() {
    return S3Configuration.builder().pathStyleAccessEnabled(pathStyle).build();
  }

  /**
   * Creates a StaticCredentialsProvider from the configured access and secret keys.
   *
   * The provider wraps AwsBasicCredentials constructed from this class's
   * accessKey and secretKey fields.
   *
   * @return a StaticCredentialsProvider supplying AwsBasicCredentials for S3 clients
   */
  private StaticCredentialsProvider creds() {
    return StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));
  }

  /**
   * Creates and configures a synchronous S3Client for blocking or short-lived S3 operations.
   *
   * <p>The client is configured with the configured endpoint and region, path-style access
   * (from s3Cfg()), static credentials (from creds()), request checksum calculation set to
   * WHEN_REQUIRED, and client override timeouts (apiCallTimeout = 2 minutes,
   * apiCallAttemptTimeout = 30 seconds). Suitable for metadata operations and small S3 requests.
   *
   * @return a fully configured S3Client
   */
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

  /**
   * Creates and configures a non-blocking S3AsyncClient for high-throughput and
   * asynchronous S3 operations (required by the Transfer Manager).
   *
   * The client is configured to target the configured endpoint and region, use
   * the shared S3 service configuration (including path-style addressing), and
   * the static credentials provider. Request checksum calculation is enabled
   * when required. API call timeouts are set to support long-running transfers
   * (apiCallTimeout = 5 minutes, apiCallAttemptTimeout = 30 seconds).
   *
   * @return a configured S3AsyncClient suitable for parallel/non-blocking S3 transfers
   */
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

  /**
   * Creates an S3TransferManager for high-throughput, parallel S3 transfers using the provided async client.
   *
   * The returned manager performs multipart and parallel uploads/downloads and should be closed when no longer needed.
   *
   * @return a configured S3TransferManager backed by the provided S3AsyncClient
   */
  @Bean
  public S3TransferManager s3TransferManager(S3AsyncClient async) {
    return S3TransferManager.builder().s3Client(async).build();
  }

  /**
   * Creates an S3Presigner configured for the application's MinIO/S3 endpoint.
   *
   * <p>The presigner is configured with the injected endpoint, region, credentials, and
   * S3 service configuration (including path-style addressing). Use this bean to
   * generate pre-signed GET/PUT URLs that are compatible with MinIO and other S3-compatible
   * services.
   *
   * @return an S3Presigner ready to build pre-signed requests against the configured endpoint
   */
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
