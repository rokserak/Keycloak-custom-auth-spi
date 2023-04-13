package com.example.keycloak.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthRequestDTO {
	
	public String username;
	public String password;

	public AuthRequestDTO(String username, String password) {
		super();
		this.username = username;
		this.password = password;
	}
}