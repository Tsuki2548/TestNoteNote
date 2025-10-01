package com.project.notenoteclient.user.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.reactive.result.view.RedirectView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(ForbiddenException.class)
    public RedirectView handleForbidden(ForbiddenException ex, RedirectAttributes redirectAttributes) {
        log.error("Forbidden error: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("messageType", "error");
        redirectAttributes.addFlashAttribute("error", "Session expired. Please login again.");
        redirectAttributes.addFlashAttribute("errorDetails", ex.getMessage());
        return new RedirectView("/login");
    }

    @ExceptionHandler(ClientErrorException.class)
    public RedirectView handleClientError(ClientErrorException ex, RedirectAttributes redirectAttributes) {
        log.error("Client error: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        return new RedirectView("/welcome");
    }

    @ExceptionHandler(ServerErrorException.class)
    public RedirectView handleServerError(ServerErrorException ex, RedirectAttributes redirectAttributes) {
        log.error("Server error: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("error", "Server error occured. Please try again later.");
        return new RedirectView("/welcome");
    }

    @ExceptionHandler(NetworkErrorException.class)
    public RedirectView handleNetworkError(NetworkErrorException ex, RedirectAttributes redirectAttributes) {
        log.error("Network error: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("error", "Network error. Please check your connect.");
        return new RedirectView("/welcome");
    }

    @ExceptionHandler(Exception.class)
    public RedirectView handleUnexpectError(Exception ex, RedirectAttributes redirectAttributes) {
        log.error("Unexpect error: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("error", "An unexpected error occured. Please try again.");
        return new RedirectView("/welcome");
    }
}
