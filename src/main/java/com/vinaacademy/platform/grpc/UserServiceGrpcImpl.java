package com.vinaacademy.platform.grpc;

import com.vinaacademy.grpc.GetUserByIdRequest;
import com.vinaacademy.grpc.GetUserByIdResponse;
import com.vinaacademy.grpc.GetUserByIdsRequest;
import com.vinaacademy.grpc.GetUserByIdsResponse;
import com.vinaacademy.grpc.UserInfo;
import com.vinaacademy.grpc.UserServiceGrpc;
import com.vinaacademy.platform.feature.user.UserRepository;
import com.vinaacademy.platform.feature.user.entity.User;
import com.vinaacademy.platform.feature.user.role.entity.Role;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.grpc.server.service.GrpcService;
import org.springframework.security.access.prepost.PreAuthorize;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class UserServiceGrpcImpl extends UserServiceGrpc.UserServiceImplBase {

  private final UserRepository userRepository;

  /**
   * Retrieves user information by user ID for microservices communication.
   *
   * @param request GetUserByIdRequest containing the user ID
   * @param responseObserver StreamObserver for sending the response
   */
  @PreAuthorize("hasAuthority('SCOPE_api.read')")
  @Override
  public void getUserById(
      GetUserByIdRequest request, StreamObserver<GetUserByIdResponse> responseObserver) {

    log.info("Received getUserById request for user ID: {}", request.getUserId());

    try {
      UUID userId = UUID.fromString(request.getUserId());
      Optional<User> userOptional = userRepository.findById(userId);

      if (userOptional.isPresent()) {
        User user = userOptional.get();

        UserInfo userInfo = convertToUserInfo(user);

        GetUserByIdResponse response =
            GetUserByIdResponse.newBuilder()
                .setSuccess(true)
                .setMessage("User found successfully")
                .setUser(userInfo)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();

        log.info("Successfully returned user information for ID: {}", request.getUserId());
      } else {
        GetUserByIdResponse response =
            GetUserByIdResponse.newBuilder().setSuccess(false).setMessage("User not found").build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();

        log.warn("User not found for ID: {}", request.getUserId());
      }
    } catch (IllegalArgumentException e) {
      log.error("Invalid UUID format: {}", request.getUserId());

      GetUserByIdResponse response =
          GetUserByIdResponse.newBuilder()
              .setSuccess(false)
              .setMessage("Invalid user ID format")
              .build();

      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (Exception e) {
      log.error("Error retrieving user by ID: {}", e.getMessage(), e);

      responseObserver.onError(
          io.grpc.Status.INTERNAL
              .withDescription("Internal server error: " + e.getMessage())
              .withCause(e)
              .asRuntimeException());
    }
  }

  /**
   * Retrieves multiple users information by user IDs for microservices communication.
   *
   * @param request GetUserByIdsRequest containing the list of user IDs
   * @param responseObserver StreamObserver for sending the response
   */
  @PreAuthorize("hasAuthority('SCOPE_api.read')")
  @Override
  public void getUserByIds(
      GetUserByIdsRequest request, StreamObserver<GetUserByIdsResponse> responseObserver) {

    log.info("Received getUserByIds request for {} user IDs",
        Optional.of(request.getUserIdsList().size()));

    try {
      List<String> userIdStrings = request.getUserIdsList();
      List<UUID> userIds = new ArrayList<>();
      List<String> invalidIds = new ArrayList<>();

      // Validate and parse UUIDs
      for (String userIdString : userIdStrings) {
        try {
          userIds.add(UUID.fromString(userIdString));
        } catch (IllegalArgumentException e) {
          invalidIds.add(userIdString);
          log.warn("Invalid UUID format: {}", userIdString);
        }
      }

      // Fetch users from database
      List<User> users = userRepository.findAllById(userIds);
      List<String> notFoundIds = new ArrayList<>();

      // Find which IDs were not found
      List<UUID> foundUserIds = users.stream().map(User::getId).toList();
      for (UUID userId : userIds) {
        if (!foundUserIds.contains(userId)) {
          notFoundIds.add(userId.toString());
        }
      }

      // Add invalid IDs to not found list
      notFoundIds.addAll(invalidIds);

      // Convert users to UserInfo
      List<UserInfo> userInfoList = users.stream()
          .map(this::convertToUserInfo)
          .collect(Collectors.toList());

      GetUserByIdsResponse response = GetUserByIdsResponse.newBuilder()
          .setSuccess(true)
          .setMessage(String.format("Found %d users out of %d requested",
              userInfoList.size(), userIdStrings.size()))
          .addAllUsers(userInfoList)
          .addAllNotFoundIds(notFoundIds)
          .build();

      responseObserver.onNext(response);
      responseObserver.onCompleted();

      log.info("Successfully returned {} users information out of {} requested", 
          userInfoList.size(), userIdStrings.size());

    } catch (Exception e) {
      log.error("Error retrieving users by IDs: {}", e.getMessage(), e);

      responseObserver.onError(
          Status.INTERNAL
              .withDescription("Internal server error: " + e.getMessage())
              .withCause(e)
              .asRuntimeException());
    }
  }

  /**
   * Helper method to convert User entity to UserInfo proto message.
   *
   * @param user User entity to convert
   * @return UserInfo proto message
   */
  private UserInfo convertToUserInfo(User user) {
    return UserInfo.newBuilder()
        .setId(user.getId().toString())
        .setEmail(user.getEmail())
        .setUsername(user.getUsername())
        .setPhone(user.getPhone() != null ? user.getPhone() : "")
        .setAvatarUrl(user.getAvatarUrl() != null ? user.getAvatarUrl() : "")
        .setFullName(user.getFullName() != null ? user.getFullName() : "")
        .setDescription(user.getDescription() != null ? user.getDescription() : "")
        .setIsCollaborator(user.isCollaborator())
        .setBirthday(user.getBirthday() != null ? user.getBirthday().toString() : "")
        .addAllRoles(
            user.getRoles() != null
                ? user.getRoles().stream().map(Role::getCode).collect(Collectors.toList())
                : List.of())
        .setEnabled(user.isEnabled())
        .setIsUsing2Fa(user.isUsing2FA())
        .build();
  }
}
