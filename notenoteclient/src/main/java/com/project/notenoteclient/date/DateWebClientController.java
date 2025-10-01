package com.project.notenoteclient.date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.notenoteclient.date.DTO.DateDTORequest;
import com.project.notenoteclient.date.DTO.DateDTOResponse;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/dates")
public class DateWebClientController {
    @Autowired
    private DateWebClientService dateService;

    @PostMapping("/create")
    public DateDTOResponse createDate(
        @RequestBody DateDTORequest request, 
        HttpServletRequest servletRequest
    ){
        String cookieHeader = servletRequest.getHeader("Cookie");
        if (cookieHeader == null) {
            throw new RuntimeException("Access token not found. please login");
        }
        return dateService.createDate(request, cookieHeader).block();
    }

    @PutMapping("/update/{dateId}")
    public DateDTOResponse updateDate (
        @PathVariable Long dateId,
        @RequestBody DateDTORequest request,
        HttpServletRequest servletRequest
    ){
        String cookieHeader = servletRequest.getHeader("Cookie");
        if (cookieHeader == null) {
            throw new RuntimeException("Access token not found. please login");
        }
        return dateService.updateDate(dateId, request, cookieHeader).block();
    }

    @DeleteMapping("/delete/{dateId}")
    public DateDTOResponse deleteDate(
        @PathVariable Long dateId,
        HttpServletRequest servletRequest
    ){
        String cookieHeader = servletRequest.getHeader("Cookie");
        if (cookieHeader == null) {
            throw new RuntimeException("Access token not found. please login");
        }
        return dateService.deleteDate(dateId, cookieHeader).block();
    }

}
