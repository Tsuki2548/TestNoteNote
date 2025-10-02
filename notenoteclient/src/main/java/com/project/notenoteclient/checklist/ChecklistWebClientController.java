package com.project.notenoteclient.checklist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    // Read-through: get checklists by card id
    @GetMapping("/byCardId/{cardId}")
    public java.util.List<ChecklistDTOResponse> getByCardId(@PathVariable Long cardId, HttpServletRequest servletRequest){
        String cookieHeader = servletRequest.getHeader("Cookie");
        if (cookieHeader == null) throw new RuntimeException("Access token not found. please login");
        return checklistService.getChecklistByCardId(cardId, cookieHeader).collectList().block();
    }

    // Write-through: update checklist title/card binding
    @PutMapping("/update/{checklistId}")
    public ChecklistDTOResponse updateChecklist(@PathVariable Long checklistId, @RequestBody ChecklistDTORequest request, HttpServletRequest servletRequest){
        String cookieHeader = servletRequest.getHeader("Cookie");
        if (cookieHeader == null) throw new RuntimeException("Access token not found. please login");
        return checklistService.updateChecklist(checklistId, request, cookieHeader).block();
    }

    // Delete checklist by id
    @DeleteMapping("/delete/{checklistId}")
    public ChecklistDTOResponse deleteChecklist(@PathVariable Long checklistId, HttpServletRequest servletRequest){
        String cookieHeader = servletRequest.getHeader("Cookie");
        if (cookieHeader == null) throw new RuntimeException("Access token not found. please login");
        return checklistService.deleteChecklist(checklistId, cookieHeader).block();
    }
}
