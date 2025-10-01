package com.project.notenote.user;

import java.util.DuplicateFormatFlagsException;
import java.util.List;
import java.util.Map;

import javax.management.RuntimeErrorException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.notenote.user.dto.UsersRequest;
import com.project.notenote.user.dto.UsersResponse;
import com.project.notenote.user.exception.DuplicateUserException;
import com.project.notenote.user.exception.UsersNotFoundException;
import com.project.notenote.user.security.JwtUtils;
import com.project.notenote.user.security.UsersDetailsService;

import lombok.AllArgsConstructor;

@Service
@Transactional
@AllArgsConstructor
public class UsersService {
    private final Logger log = LoggerFactory.getLogger(getClass());
    @Autowired
    private final JwtUtils jwtUtils;
    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UsersDetailsService usersDetailsService;
    
    // @Override
    // public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    //     Optional<Users> user = usersRepository.findByUsername(username);
    //     if (user.isPresent()) {
    //         var userObj = user.get();
    //         return User.builder()
    //             .username(userObj.getUsername())
    //             .password(userObj.getPassword())
    //             .build();
    //     }
    //     else {
    //         throw new UsernameNotFoundException(username);
    //     }
    // }
    // @Autowired
    // usersRepositorysitory usersRepository;

    private UsersResponse mapToResponse(Users user) {
        return new UsersResponse(
                // user.getid(),
                user.getUsername(), 
                user.getEmail()
            );
    }

    private Users mapToEntity(UsersRequest request) {
        Users user = new Users();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        return user;
    }

    public List<UsersResponse> getUserList() {
        List<Users> users = (List<Users>) usersRepository.findAll();
        return users.stream()
            .map(u -> mapToResponse(u))
            .toList();
    }

    public UsersResponse getUserById(long id) {
        Users user = usersRepository.findById(id).orElseThrow(() -> new UsersNotFoundException(id));
        return mapToResponse(user);
    }

    public UsersResponse save(UsersRequest request) {
        Users user = mapToEntity(request);
        usersRepository.save(user);
        return mapToResponse(user);
    }

    @Transactional
    public UsersResponse addUser(UsersRequest request) {
        usersRepository.findByUsernameOrEmail(request.getUsername(), request.getEmail())
            .ifPresent(u -> {throw new DuplicateUserException("Username or Email already exist.");});

        try {
            Users user = mapToEntity(request);
    
            user.setPassword(passwordEncoder.encode(user.getPassword()));
    
            Users newUser = usersRepository.save(user);
            log.info("User created: {}", newUser.getUsername());
            return mapToResponse(newUser);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateUserException("Username or Email already exist.");
        } catch (Exception e) {
            log.error("Unexpected error while saving user", e);
            throw new RuntimeException("Unexpected error while saving user: " + e.getMessage());
        }
    }

    public UsersResponse updateUser(long id, UsersRequest request) {
        Users user = usersRepository.findById(id).orElseThrow(() -> new UsersNotFoundException(id));
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user = usersRepository.save(user);
        return mapToResponse(user);
    }

    public void deleteUserById(long id) {
        Users user = usersRepository.findById(id).orElseThrow(() -> new UsersNotFoundException(id));
        usersRepository.delete(user);
    }

    public Map<String, String> authenticate(String username, String password) {
        UserDetails user = usersDetailsService.loadUserByUsername(username);
        if (passwordEncoder.matches(password, user.getPassword())) {
            return Map.of(
                "ACCESS_TOKEN", jwtUtils.generateAccessToken(username),
                "REFRESH_TOKEN", jwtUtils.generateRefreshToken(username)
            );
            // return jwtUtils.generateToken(username);
        }
        else {
            log.error("error: username or password invalid");
            throw new RuntimeException("Invalid credentials");
        }
    }

    public Boolean validateAccessToken(String accessToken) {
        return jwtUtils.validateAccessToken(accessToken);
    }

    public String refreshAccessToken(String refreshToken) {
        return jwtUtils.refreshAccessToken(refreshToken);
    }

    public String getUsernameByToken(String accessToken) {
        if (jwtUtils.validateAccessToken(accessToken)) {
            return jwtUtils.extractUsername(accessToken, true);
        }
        else throw new RuntimeException("Invalid or expired access token.");
    }

    public Users getUserByUsername(String username) {
        return usersRepository.findByUsername(username).orElseThrow(() -> new UsersNotFoundException(username));
    }

}
