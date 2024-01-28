package com.mb.swaggerapplication.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
public class User {

    private int id;

    private String name;

    private String role;

    private String email;
}
