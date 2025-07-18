package com.vinaacademy.platform.feature.user.auth.aspect;

import com.vinaacademy.platform.exception.ValidationException;
import com.vinaacademy.platform.feature.user.auth.annotation.RequiresResourcePermission;
import com.vinaacademy.platform.feature.user.auth.service.AuthorizationService;
import com.vinaacademy.platform.feature.user.constant.ResourceConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.UUID;

/**
 * Aspect that handles authorization checks based on the RequiresResourcePermission annotation.
 * This centralizes resource-based permission checks in one place.
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuthorizationCheckerAspect {

    private final AuthorizationService authorizationService;

    @Before("@annotation(com.vinaacademy.platform.feature.user.auth.annotation.RequiresResourcePermission)")
    public void checkResourcePermission(JoinPoint joinPoint) {
        // Get the method signature
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // Get the annotation
        RequiresResourcePermission annotation = method.getAnnotation(RequiresResourcePermission.class);

        // Extract resource ID
        UUID resourceId = extractResourceId(joinPoint, signature, annotation);

        // Check permission based on resource type and permission type
        boolean hasPermission = checkPermission(resourceId, annotation.resourceType(), annotation.permission());

        // If no permission, throw an exception
        if (!hasPermission) {
            throw new AccessDeniedException("User does not have permission to " +
                    annotation.permission() + " this " + annotation.resourceType());
        }
    }

    private UUID extractResourceId(JoinPoint joinPoint, MethodSignature signature, RequiresResourcePermission annotation) {
        Object[] args = joinPoint.getArgs();
        Parameter[] parameters = signature.getMethod().getParameters();

        // If idParam is specified, look for a parameter with that name
        if (!annotation.idParam().isEmpty()) {
            String idParam = annotation.idParam();

            for (int i = 0; i < parameters.length; i++) {
                String paramName = parameters[i].getName();

                if (idParam.contains(".")) {
                    String[] parts = idParam.split("\\.");

                    if (parts.length != 2 || !paramName.equals(parts[0])) continue;

                    Object arg = args[i];
                    if (arg == null) continue;

                    try {
                        Object value = PropertyUtils.getProperty(arg, parts[1]);
                        if (value instanceof UUID uuid) {
                            return uuid;
                        }
                    } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                        log.error("Error extracting resource ID from parameter: {}", e.getMessage());
                    }

                } else if (paramName.equals(idParam)) {
                    Object arg = args[i];
                    if (arg instanceof UUID uuid) {
                        return uuid;
                    }
                }
            }
        }

        // Otherwise, find the first UUID parameter
        for (Object arg : args) {
            if (arg instanceof UUID) {
                return (UUID) arg;
            }
        }

        throw new ValidationException("Could not find resource ID in method parameters");
    }

    private boolean checkPermission(UUID resourceId, String resourceType, String permission) {
        // Delegate to appropriate method in AuthorizationService based on resource type and permission
        return switch (resourceType) {
            case ResourceConstants.LESSON -> switch (permission) {
                case ResourceConstants.VIEW -> authorizationService.canAccessLesson(resourceId);
                case ResourceConstants.CREATE, ResourceConstants.EDIT,
                     ResourceConstants.DELETE, ResourceConstants.VIEW_OWN ->
                        authorizationService.canModifyLesson(resourceId);
                default -> false;
            };
            case ResourceConstants.COURSE -> switch (permission) {
                case ResourceConstants.VIEW -> authorizationService.canAccessCourse(resourceId);
                case ResourceConstants.CREATE, ResourceConstants.EDIT,
                     ResourceConstants.DELETE, ResourceConstants.VIEW_OWN ->
                        authorizationService.canModifyCourse(resourceId);
                default -> false;
            };
            case ResourceConstants.SECTION -> switch (permission) {
                case ResourceConstants.VIEW -> authorizationService.canAccessSection(resourceId);
                case ResourceConstants.CREATE, ResourceConstants.EDIT,
                     ResourceConstants.DELETE, ResourceConstants.VIEW_OWN ->
                        authorizationService.canModifySection(resourceId);
                default -> false;
            };
            default -> false;
        };
    }
}