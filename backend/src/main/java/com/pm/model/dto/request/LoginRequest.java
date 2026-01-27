package com.pm.model.dto.request;

import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
	@NotBlank
	private String email;

	@NotBlank
	private String password;
}
