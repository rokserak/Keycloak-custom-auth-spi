package com.example.keycloak.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoleDTO {

    @JsonProperty("id")
    public Long id;
    @JsonProperty("name")
    private String name;

}
