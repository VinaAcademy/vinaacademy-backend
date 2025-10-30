package com.vinaacademy.platform.exception;

import com.vinaacademy.platform.feature.common.exception.ResourceNotFoundException;
import com.vinaacademy.platform.feature.common.response.ApiResponse;
import com.vinaacademy.platform.feature.common.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;

import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

/**
 * Global exception handler with internationalization support.
 * Maps exceptions to ApiResponse with localized error messages.
 */
@ControllerAdvice
@ResponseBody
@Order(HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private final MessageService messageService;

    /**
     * Handle BadRequestException with i18n support
     */
    @ExceptionHandler({BadRequestException.class})
    public ResponseEntity<Object> handleBadRequest(BadRequestException e) {
        logger.error("BadRequestException: " + e.getMessage(), e);
        
        String message = resolveMessage(e.getMessageKey(), e.getMessageArgs(), e.getMessage());
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getCode() != null ? e.getCode() : 400, message));
    }

    /**
     * Handle ResourceNotFoundException with i18n support
     */
    @ExceptionHandler({ResourceNotFoundException.class})
    public ResponseEntity<Object> handleResourceNotFound(ResourceNotFoundException e) {
        logger.error("ResourceNotFoundException: " + e.getMessage(), e);
        
        String message = resolveMessage(e.getMessageKey(), e.getMessageArgs(), e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(404, message));
    }

    /**
     * Handle custom AccessDeniedException with i18n support
     */
    @ExceptionHandler({AccessDeniedException.class})
    public ResponseEntity<Object> handleCustomAccessDenied(AccessDeniedException e) {
        logger.error("AccessDeniedException: " + e.getMessage(), e);
        
        String message = resolveMessage(e.getMessageKey(), e.getMessageArgs(), e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(403, message));
    }

    /**
     * Handle Spring Security AccessDeniedException
     */
    @ExceptionHandler({org.springframework.security.access.AccessDeniedException.class})
    public ResponseEntity<Object> handleSpringAccessDenied(org.springframework.security.access.AccessDeniedException e) {
        logger.error("Spring AccessDeniedException: " + e.getMessage(), e);
        
        String message = messageService.getMessage("access.denied", null, e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(403, message));
    }

    /**
     * Handle validation errors with i18n support
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException e,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        logger.error("MethodArgumentNotValidException: " + e.getMessage(), e);

        List<String> errors = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> {
                    String defaultMessage = fieldError.getDefaultMessage();
                    // Try to resolve as message key first, fallback to default message
                    return messageService.getMessage(defaultMessage, null, defaultMessage);
                })
                .toList();

        String message = String.join(", ", errors);
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(400, message));
    }

    /**
     * Handle authentication exceptions
     */
    @ExceptionHandler({
            UnauthorizedException.class,
            AuthenticationException.class,
            InternalAuthenticationServiceException.class,
            BadCredentialsException.class
    })
    public ResponseEntity<Object> handleUnauthorized(Exception e) {
        logger.error("AuthenticationException: " + e.getMessage(), e);
        
        String messageKey = "unauthorized.access";
        String fallbackMessage = e.getCause() instanceof UsernameNotFoundException ?
                e.getCause().getMessage() : e.getMessage();
        
        String message = messageService.getMessage(messageKey, null, fallbackMessage);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(401, message));
    }

    /**
     * Handle generic exceptions
     */
    @ExceptionHandler({Exception.class})
    public ResponseEntity<Object> handleGenericException(Exception e) {
        logger.error("Generic Exception: " + e.getMessage(), e);

        String message = messageService.getMessage("operation.failed", null, e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(500, message));
    }

    /**
     * Resolve message using MessageService
     */
    private String resolveMessage(String messageKey, Object[] args, String fallback) {
        if (messageKey != null) {
            return messageService.getMessage(messageKey, args, fallback);
        }
        return fallback != null ? fallback : "Unknown error";
    }
}
