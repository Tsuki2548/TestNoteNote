package com.project.notenoteclient.label;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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

}
