package com.example.keycloak.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class User {
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String token;
    private List<String> roles;
    public User() {
    }
    public User(String username, String email, String firstName, String lastName, String token, List<RoleDTO> roles) {
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.token = token;
        this.roles = roles.stream().map(RoleDTO::getName).collect(Collectors.toList());
    }
}
