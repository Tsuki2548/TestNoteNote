package com.project.notenoteclient.checkbox;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.notenoteclient.checkbox.DTO.CheckboxDTORequest;
import com.project.notenoteclient.checkbox.DTO.CheckboxDTOResponse;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/checkboxs")
public class CheckboxWebClientController {
    @Autowired
    private CheckboxWebClientService checkboxService;

    @PostMapping("/create")
    public CheckboxDTOResponse createCheckbox(
        @RequestBody CheckboxDTORequest request, 
        HttpServletRequest servletRequest
    ){
        String cookieHeader = servletRequest.getHeader("Cookie");
        if (cookieHeader == null) {
            throw new RuntimeException("Access token not found. please login");
        }
        return checkboxService.createCheckbox(request, cookieHeader).block();
    }

    @PutMapping("/update/{checkboxId}")
    public CheckboxDTOResponse updateCheckbox(
        @PathVariable Long checkboxId, 
        @RequestBody CheckboxDTORequest request,
        HttpServletRequest servletRequest
    ){
        String cookieHeader = servletRequest.getHeader("Cookie");
        if (cookieHeader == null) {
            throw new RuntimeException("Access token not found. please login");
        }
        return checkboxService.updateCheckbox(checkboxId, request, cookieHeader).block();
    }

    @DeleteMapping("/delete/{checkboxId}")
    public CheckboxDTOResponse deleteCheckbox(
        @PathVariable Long checkboxId, 
        HttpServletRequest servletRequest
    ){
        String cookieHeader = servletRequest.getHeader("Cookie");
        if (cookieHeader == null) {
            throw new RuntimeException("Access token not found. please login");
        }
        return checkboxService.deleteCheckbox(checkboxId, cookieHeader).block();
    }

}
