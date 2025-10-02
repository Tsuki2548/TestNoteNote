package com.project.notenoteclient.common;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Prevent Thymeleaf from trying to resolve a template named "favicon" by
 * short-circuiting favicon requests. You can replace this with serving a real
 * favicon from src/main/resources/static/favicon.ico later.
 */
@Controller
public class FaviconController {

    @GetMapping({"/favicon.ico", "/favicon"})
    public ResponseEntity<Void> favicon() {
        // Return 204 No Content to avoid template resolution errors
        return ResponseEntity.noContent().build();
    }
}
