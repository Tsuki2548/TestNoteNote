package com.project.notenoteclient.pageResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class pageController {

    @GetMapping("/Notes")
    public String getNotesPage() {
        return "Note";
    }
}
