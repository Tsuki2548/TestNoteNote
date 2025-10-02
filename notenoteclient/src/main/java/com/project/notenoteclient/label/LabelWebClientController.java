package com.project.notenoteclient.label;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.notenoteclient.label.DTO.LabelDTORequest;
import com.project.notenoteclient.label.DTO.LabelDTOResponse;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/labels")
public class LabelWebClientController {
    @Autowired
    private LabelWebClientService labelService;

    @PostMapping("/create")
    public LabelDTOResponse createLabel(
        @RequestBody LabelDTORequest request, 
        HttpServletRequest servletRequest
    ){
        String cookieHeader = servletRequest.getHeader("Cookie");
        if (cookieHeader == null) {
            throw new RuntimeException("Access token not found. please login");
        }
        return labelService.createLabel(request, cookieHeader).block();
    }

    @PutMapping("/update/{labelId}")
    public LabelDTOResponse updateLabel(
        @PathVariable Long labelId,
        @RequestBody LabelDTORequest request,
        HttpServletRequest servletRequest
    ){
        String cookieHeader = servletRequest.getHeader("Cookie");
        if (cookieHeader == null) {
            throw new RuntimeException("Access token not found. please login");
        }
        return labelService.updateLabel(labelId, request, cookieHeader).block();
    }

    @DeleteMapping("/delete/{labelId}")
    public LabelDTOResponse deleteLabel(
        @PathVariable Long labelId,
        HttpServletRequest servletRequest
    ){
        String cookieHeader = servletRequest.getHeader("Cookie");
        if (cookieHeader == null) {
            throw new RuntimeException("Access token not found. please login");
        }
        return labelService.deleteLabel(labelId, cookieHeader).block();
    }

    // Read-through endpoints for client-side fetches
    @GetMapping("/byCardId/{cardId}")
    public java.util.List<LabelDTOResponse> getLabelsByCardId(@PathVariable Long cardId, HttpServletRequest servletRequest){
        String cookieHeader = servletRequest.getHeader("Cookie");
        if (cookieHeader == null) throw new RuntimeException("Access token not found. please login");
        return labelService.getLabelsByCardId(cardId, cookieHeader).collectList().block();
    }
    @GetMapping("/batch")
    public java.util.List<LabelDTOResponse> getLabelsBatch(@RequestParam("ids") String idsCsv, HttpServletRequest servletRequest){
        String cookieHeader = servletRequest.getHeader("Cookie");
        if (cookieHeader == null) throw new RuntimeException("Access token not found. please login");
        return labelService.getLabelsBatch(idsCsv, cookieHeader).collectList().block();
    }

    // Write-through endpoints for client-side assign/remove/create-assign
    @PostMapping("/assign/{cardId}/{labelId}")
    public String assignLabelToCard(@PathVariable Long cardId, @PathVariable Long labelId, HttpServletRequest servletRequest){
        String cookieHeader = servletRequest.getHeader("Cookie");
        if (cookieHeader == null) throw new RuntimeException("Access token not found. please login");
        return labelService.assignLabelToCard(cardId, labelId, cookieHeader).block();
    }
    @DeleteMapping("/remove/{cardId}/{labelId}")
    public String removeLabelFromCard(@PathVariable Long cardId, @PathVariable Long labelId, HttpServletRequest servletRequest){
        String cookieHeader = servletRequest.getHeader("Cookie");
        if (cookieHeader == null) throw new RuntimeException("Access token not found. please login");
        return labelService.removeLabelFromCard(cardId, labelId, cookieHeader).block();
    }
    @PostMapping("/create-assign/{cardId}")
    public String createAssignLabel(@PathVariable Long cardId, @RequestBody com.project.notenoteclient.label.DTO.LabelDTORequest request, HttpServletRequest servletRequest){
        String cookieHeader = servletRequest.getHeader("Cookie");
        if (cookieHeader == null) throw new RuntimeException("Access token not found. please login");
        return labelService.createAssignLabel(cardId, request, cookieHeader).block();
    }

    // Get labels for a specific note (user-scoped)
    @GetMapping("/byNoteId/{noteId}")
    public java.util.List<LabelDTOResponse> getLabelsByNoteId(@PathVariable Long noteId, HttpServletRequest servletRequest){
        String cookieHeader = servletRequest.getHeader("Cookie");
        if (cookieHeader == null) throw new RuntimeException("Access token not found. please login");
        return labelService.getLabelsByNoteId(noteId, cookieHeader).collectList().block();
    }

}
