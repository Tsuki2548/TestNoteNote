package com.project.notenoteclient.board;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.project.notenoteclient.board.DTO.BoardDTORequest;
import com.project.notenoteclient.board.DTO.BoardDTOResponse;

import jakarta.servlet.http.HttpServletRequest;


@Controller
@RequestMapping("/boards")
public class BoardWebClientController {
    @Autowired
    private BoardWebClientService boardService;

    @PostMapping("/create")
    public BoardDTOResponse createBoard(
        @RequestBody BoardDTORequest request, 
        HttpServletRequest servletRequest
    ){
         String cookieHeader = servletRequest.getHeader("Cookie");
         if (cookieHeader == null) {
             throw new RuntimeException("Access token not found. please login");
         }
        return boardService.createBoard(request, cookieHeader).block();
    }

    @PutMapping("/update/{boardId}")
    public BoardDTOResponse updateBoard(
        @PathVariable Long boardId, 
        @RequestBody BoardDTORequest request, 
        HttpServletRequest servletRequest
    ){
         String cookieHeader = servletRequest.getHeader("Cookie");
         if (cookieHeader == null) {
             throw new RuntimeException("Access token not found. please login");
         }
        return boardService.updateBoard(boardId, request, cookieHeader).block();
    }

    @DeleteMapping("/delete/{boardId}")
    public BoardDTOResponse deleteBoard(
        @PathVariable Long boardId, 
        HttpServletRequest servletRequest
    ){
         String cookieHeader = servletRequest.getHeader("Cookie");
         if (cookieHeader == null) {
             throw new RuntimeException("Access token not found. please login");
         }
        return boardService.deleteBoard(boardId, cookieHeader).block();
    }

}
