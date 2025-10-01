package com.project.notenote.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UsersResponse(
    // @NotNull long id, 
    @NotBlank String username,
    @Email String email
) {

}
