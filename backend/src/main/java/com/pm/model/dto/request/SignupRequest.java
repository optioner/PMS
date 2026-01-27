package com.pm.model.dto.request;

import java.util.Set;

import javax.validation.constraints.*;
import lombok.Data;

@Data
public class SignupRequest {
    @NotBlank
    @Size(max = 100)
    private String fullName;

    @NotBlank
    @Size(max = 100)
    @Email
    private String email;

    @NotBlank
    @Size(min = 6, max = 40)
    private String password;
    
    private Set<String> role;
    
    private String position;
}
