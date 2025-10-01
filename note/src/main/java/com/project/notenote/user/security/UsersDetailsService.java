package com.project.notenote.user.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.project.notenote.user.Users;
import com.project.notenote.user.UsersRepository;

@Component
public class UsersDetailsService implements UserDetailsService {

    private final UsersRepository usersRepository;

    private UsersDetailsService(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String loginInput) throws UsernameNotFoundException {
        Users user = usersRepository.findByUsernameOrEmail(loginInput, loginInput)
                        .orElseThrow(() -> new UsernameNotFoundException("Username or Email: " +loginInput + " not found"));
        return new UsersDetails(user);
    }
}
