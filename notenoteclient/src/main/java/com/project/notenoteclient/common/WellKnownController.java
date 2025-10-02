package com.project.notenoteclient.common;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Prevent Thymeleaf from attempting to resolve templates for Chrome/clients
 * probing well-known endpoints like ".well-known/appspecific/com.chrome.devtools".
 * We simply return 204 No Content for GET/HEAD under /.well-known/**.
 */
@Controller
public class WellKnownController {

    @RequestMapping(value = {"/.well-known", "/.well-known/**"}, method = {RequestMethod.GET, RequestMethod.HEAD})
    public ResponseEntity<Void> handleWellKnown() {
        return ResponseEntity.noContent().build();
    }
}
