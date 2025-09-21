package com.vinaacademy.platform.grpc;

import com.vinaacademy.grpc.JwtServiceGrpc;
import com.vinaacademy.grpc.TokenRequest;
import com.vinaacademy.grpc.ValidateTokenResponse;
import com.vinaacademy.platform.feature.user.auth.service.JwtService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.grpc.server.service.GrpcService;
import org.springframework.security.access.prepost.PreAuthorize;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class JwtServiceGrpcImpl extends JwtServiceGrpc.JwtServiceImplBase {
  private final JwtService jwtService;


  /**
   * Validates a JWT from the incoming TokenRequest and returns a detailed
   * ValidateTokenResponse via the provided gRPC response observer.
   *
   * <p>The response includes a boolean validity flag, a human-readable message,
   * and token-derived claims: userId, email, roles, avatarUrl, and fullName.
   *
   * @param request TokenRequest containing the JWT to validate (request.getToken()).
   */
  @PreAuthorize("hasAuthority('SCOPE_api.read')")
  @Override
  public void validateToken(
      TokenRequest request, StreamObserver<ValidateTokenResponse> responseObserver) {
    log.info("Received validate request with token: {}", request.getToken());
    try {
      boolean isValid = jwtService.isValidToken(request.getToken());

      ValidateTokenResponse response =
          ValidateTokenResponse.newBuilder()
              .setIsValid(isValid)
              .setMessage(isValid ? "Token is valid" : "Token is invalid")
              .setUserId(jwtService.extractUserId(request.getToken()))
              .setEmail(jwtService.extractEmail(request.getToken()))
              .setRoles(jwtService.extractRoles(request.getToken()))
              .setAvatarUrl(jwtService.extractAvatarUrl(request.getToken()))
              .setFullName(jwtService.extractFullName(request.getToken()))
              .build();

      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (Exception e) {
      log.error("Error validating token: {}", e.getMessage());
      responseObserver.onError(
          io.grpc.Status.INTERNAL
              .withDescription(e.getMessage())
              .withCause(e)
              .asRuntimeException());
    }
  }
}
