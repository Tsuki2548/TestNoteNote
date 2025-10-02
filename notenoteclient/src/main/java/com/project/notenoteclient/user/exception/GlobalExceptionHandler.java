package com.project.notenoteclient.user.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.reactive.result.view.RedirectView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;


@ControllerAdvice
public class GlobalExceptionHandler {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private boolean isApiRequest(HttpServletRequest request) {
        if (request == null) return false;
        String uri = request.getRequestURI();
        String accept = request.getHeader("Accept");
        // Treat as API if:
        // - Path under /api/* (main proxy controllers)
        // - Path starts with /dates (date proxy lives outside /api)
        // - Client indicates it expects JSON
        boolean uriLooksApi = uri != null && (uri.startsWith("/api/") || uri.startsWith("/dates"));
        boolean expectsJson = accept != null && accept.toLowerCase().contains("application/json");
        return uriLooksApi || expectsJson;
    }

    private ResponseEntity<Map<String, Object>> jsonError(int status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", message);
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);
    }

    @ExceptionHandler(ForbiddenException.class)
    public Object handleForbidden(ForbiddenException ex, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        log.error("Forbidden error: {}", ex.getMessage());
        if (isApiRequest(request)) {
            return jsonError(403, ex.getMessage() != null ? ex.getMessage() : "Forbidden");
        }
        redirectAttributes.addFlashAttribute("messageType", "error");
        redirectAttributes.addFlashAttribute("error", "Session expired. Please login again.");
        redirectAttributes.addFlashAttribute("errorDetails", ex.getMessage());
        return new RedirectView("/login");
    }

    @ExceptionHandler(ClientErrorException.class)
    public Object handleClientError(ClientErrorException ex, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        log.error("Client error: {}", ex.getMessage());
        if (isApiRequest(request)) {
            return jsonError(400, ex.getMessage() != null ? ex.getMessage() : "Bad Request");
        }
        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        return new RedirectView("/welcome");
    }

    @ExceptionHandler(ServerErrorException.class)
    public Object handleServerError(ServerErrorException ex, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        log.error("Server error: {}", ex.getMessage());
        if (isApiRequest(request)) {
            return jsonError(502, ex.getMessage() != null ? ex.getMessage() : "Server error");
        }
        redirectAttributes.addFlashAttribute("error", "Server error occured. Please try again later.");
        return new RedirectView("/welcome");
    }

    @ExceptionHandler(NetworkErrorException.class)
    public Object handleNetworkError(NetworkErrorException ex, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        log.error("Network error: {}", ex.getMessage());
        if (isApiRequest(request)) {
            return jsonError(503, ex.getMessage() != null ? ex.getMessage() : "Network error");
        }
        redirectAttributes.addFlashAttribute("error", "Network error. Please check your connect.");
        return new RedirectView("/welcome");
    }

    @ExceptionHandler(Exception.class)
    public Object handleUnexpectError(Exception ex, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        log.error("Unexpect error: {}", ex.getMessage());
        if (isApiRequest(request)) {
            return jsonError(500, "An unexpected error occured. Please try again.");
        }
        redirectAttributes.addFlashAttribute("error", "An unexpected error occured. Please try again.");
        return new RedirectView("/welcome");
    }
}
