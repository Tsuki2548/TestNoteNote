package com.project.notenoteclient.pageResponse;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.project.notenoteclient.note.NoteWebClientService;
import com.project.notenoteclient.note.DTO.NoteDTOResponse;
import com.project.notenoteclient.user.UsersWebClientService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class pageController {

    @Autowired
    private UsersWebClientService userService;

    @Autowired
    private NoteWebClientService noteService;

    @GetMapping("/Notes")
    public String getNotesPage(HttpServletRequest servletRequest,Model model) {
        String cookieHeader = servletRequest.getHeader("Cookie");
        String accessToken = null;
        if (cookieHeader != null) {
        for (String cookie : cookieHeader.split(";")) {
            String[] parts = cookie.trim().split("=", 2);
            if (parts.length == 2 && parts[0].equals("ACCESS_TOKEN")) {
                accessToken = parts[1];
                break;
            }
            }
        }
        String username = userService.getUsername(accessToken).block();
        System.out.println("username = " + username);

        List<NoteDTOResponse> notes = noteService.getNoteByUsername(username, cookieHeader).collectList().block();

        model.addAttribute("notes", notes);
        return "Note";
    }
}
