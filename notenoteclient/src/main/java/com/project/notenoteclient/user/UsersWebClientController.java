package com.project.notenoteclient.user;

import java.time.Duration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.project.notenoteclient.user.dto.LoginRequest;
import com.project.notenoteclient.user.dto.LoginResponse;
import com.project.notenoteclient.user.exception.ClientErrorException;
import com.project.notenoteclient.user.exception.ForbiddenException;
import com.project.notenoteclient.user.exception.NetworkErrorException;
import com.project.notenoteclient.user.exception.ServerErrorException;

import jakarta.servlet.http.HttpServletResponse;
import reactor.core.publisher.Mono;

import org.springframework.ui.Model;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Cookie;

@Controller
public class UsersWebClientController {
    private Logger log = LoggerFactory.getLogger(getClass());
    @Autowired
    private UsersWebClientService usersWebClientService;

    @GetMapping("/")
    public String index(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                log.info("Cookie from browser - name: {}, value: {}", 
                    cookie.getName(), cookie.getValue());
            }
        } else {
            log.warn("No cookies received from browser");
        }
        return "redirect:/welcome";
    }

    @GetMapping("/login")
    public String loginPage() {
        
        return "Users/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new Users());
        return "Users/register";
    }

    @GetMapping("/welcome")
    public String welcomePage() {
        return "Users/welcome";
    }

    @PostMapping("/register")
    public Mono<String> register(@ModelAttribute Users users, RedirectAttributes redirectAttributes) {
        return usersWebClientService.register(users)
            .map(user -> {
                redirectAttributes.addFlashAttribute("success", "Register successful!");
                return "redirect:/login";
            })
            .onErrorResume(ClientErrorException.class, ex -> {
                redirectAttributes.addFlashAttribute("error", ex.getMessage());
                return Mono.just("redirect:/register?error=Register failed");
            })
            .onErrorResume(ServerErrorException.class, ex -> {
                redirectAttributes.addFlashAttribute("error", ex.getMessage());
                return Mono.just("redirect:/register?error=Register failed");
            })
            .onErrorResume(NetworkErrorException.class, ex -> {
                redirectAttributes.addFlashAttribute("error", ex.getMessage());
                return Mono.just("redirect:/register?error=Register failed");
            });
    }

    @PostMapping("/login")
    public String login(@ModelAttribute LoginRequest request, HttpServletResponse httpServletResponse, Model model) {

        LoginResponse loginResponse = usersWebClientService.login(request).block();

        if (loginResponse != null && loginResponse.isSuccess()) {
            loginResponse.getCookies().forEach(cookie -> {
                httpServletResponse.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
            });
            return "redirect:/welcome";
        } else {
            model.addAttribute("errorMessage", 
                loginResponse != null ? loginResponse.getErrorMessage() : "Login failed");
            return "redirect:/login";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpServletResponse response) {
        log.info("logout processing");
        try {
            // เรียก API logout ที่ server
            Boolean logoutSuccess = usersWebClientService.logout()
                .block();

            if (Boolean.TRUE.equals(logoutSuccess)) {
                // ถ้า logout สำเร็จ จึงลบ cookies
                ResponseCookie accessTokenCookie = ResponseCookie.from("ACCESS_TOKEN", "")
                    .path("/")
                    .maxAge(0)
                    .httpOnly(true)
                    .build();
                
                ResponseCookie refreshTokenCookie = ResponseCookie.from("REFRESH_TOKEN", "")
                    .path("/")
                    .maxAge(0)
                    .httpOnly(true)
                    .build();
                    
                response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
                response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
                
                return "redirect:/login";
            }
            
            // ถ้า logout ไม่สำเร็จ
            log.error("error");
            return "redirect:/welcome?error=Logout failed";
            
        } catch (Exception e) {
            // จัดการ error โดย GlobalExceptionHandler
            throw e;
        }
    }
}
