package com.mb.swagger2.api.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
public class User {

    @ApiModelProperty(notes = "User Id", name = "id", required = true, value = "1")
    private int id;

    @ApiModelProperty(notes = "User Name", name = "name", required = true, value = "test name")
    private String name;

    @ApiModelProperty(notes = "User Role", name = "role", required = true, value = "test role")
    private String role;

    @ApiModelProperty(notes = "User Email Id", name = "email", required = true, value = "test email")
    private String email;
}
