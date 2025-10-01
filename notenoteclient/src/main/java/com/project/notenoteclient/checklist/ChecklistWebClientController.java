package com.project.notenoteclient.checklist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.notenoteclient.checklist.DTO.ChecklistDTORequest;
import com.project.notenoteclient.checklist.DTO.ChecklistDTOResponse;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/checklists")
public class ChecklistWebClientController {
    
    @Autowired
    ChecklistWebClientService checklistService;

    @PostMapping("/create")
    public ChecklistDTOResponse createChecklist(
        @RequestBody ChecklistDTORequest request, 
        HttpServletRequest servletRequest
    ){
        String cookieHeader = servletRequest.getHeader("Cookie");
        if (cookieHeader == null) {
            throw new RuntimeException("Access token not found. please login");
        }
        return checklistService.createChecklist(request, cookieHeader).block();
    }

}
