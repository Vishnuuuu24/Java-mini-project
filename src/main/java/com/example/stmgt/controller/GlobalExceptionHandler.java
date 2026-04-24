package com.example.stmgt.controller;

import com.example.stmgt.service.exception.DomainException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.support.RequestContextUtils;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice(basePackages = "com.example.stmgt.controller")
public class GlobalExceptionHandler {

    @ExceptionHandler(DomainException.class)
    public Object handleDomainException(DomainException exception, HttpServletRequest request) {
        if (isJsonRequest(request)) {
            return ResponseEntity.badRequest().body(errorPayload(exception.getMessage()));
        }

        FlashMap flashMap = RequestContextUtils.getOutputFlashMap(request);
        flashMap.put("error", exception.getMessage());
        return "redirect:" + resolveRedirectPath(request);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public Object handleValidationException(Exception exception, HttpServletRequest request) {
        String message = extractValidationMessage(exception);
        if (isJsonRequest(request)) {
            return ResponseEntity.badRequest().body(errorPayload(message));
        }

        FlashMap flashMap = RequestContextUtils.getOutputFlashMap(request);
        flashMap.put("error", message);
        return "redirect:" + resolveRedirectPath(request);
    }

    private Map<String, Object> errorPayload(String message) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("success", false);
        payload.put("error", message == null ? "An unexpected error occurred." : message);
        return payload;
    }

    private boolean isJsonRequest(HttpServletRequest request) {
        String requestedWith = request.getHeader("X-Requested-With");
        if ("XMLHttpRequest".equalsIgnoreCase(requestedWith)) {
            return true;
        }

        String accept = request.getHeader("Accept");
        if (accept != null && accept.toLowerCase().contains(MediaType.APPLICATION_JSON_VALUE)) {
            return true;
        }

        String contentType = request.getContentType();
        return contentType != null && contentType.toLowerCase().contains(MediaType.APPLICATION_JSON_VALUE);
    }

    private String resolveRedirectPath(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        if (referer == null || referer.isBlank()) {
            return "/users/login/";
        }

        try {
            URI uri = URI.create(referer);
            String path = uri.getPath();
            return (path == null || path.isBlank()) ? "/users/login/" : path;
        } catch (IllegalArgumentException ignored) {
            return "/users/login/";
        }
    }

    private String extractValidationMessage(Exception exception) {
        if (exception instanceof MethodArgumentNotValidException methodException) {
            FieldError fieldError = methodException.getBindingResult().getFieldError();
            return fieldError == null ? "Invalid input." : fieldError.getDefaultMessage();
        }

        if (exception instanceof BindException bindException) {
            FieldError fieldError = bindException.getBindingResult().getFieldError();
            return fieldError == null ? "Invalid input." : fieldError.getDefaultMessage();
        }

        return "Invalid input.";
    }
}
