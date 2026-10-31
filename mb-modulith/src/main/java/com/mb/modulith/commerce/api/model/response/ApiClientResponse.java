package com.mb.modulith.commerce.api.model.response;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
public class ApiClientResponse {
    private UUID id;
    private Date createdDateTime;
    private boolean deleted;
    private String firstName;
    private String lastName;
    private String telephone;
    private String username;
    private String email;
}
