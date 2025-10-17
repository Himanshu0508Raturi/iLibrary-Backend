package com.DBMS.iLibrary.entity;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class Userdto {
    @NotBlank(message = "Username cannot be blank")
    private String username;
    private String password;
    private String email;
}
