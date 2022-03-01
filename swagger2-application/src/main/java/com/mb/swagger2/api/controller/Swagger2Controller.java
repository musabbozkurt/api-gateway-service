package com.mb.swagger2.api.controller;

import com.mb.swagger2.api.response.User;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Api(value = "Swagger2 Rest Controller")
@RequestMapping("/api")
@RestController
public class Swagger2Controller {

    List<User> users = new ArrayList<>();

    {
        users.add(new User(1, "Swagger2-User1", "ADMIN", "swagger2.user1@test.com"));
        users.add(new User(2, "Swagger2-User2", "SUPERVISOR", "swagger2.user2@test.com"));
        users.add(new User(3, "Swagger2-User3", "USER", "swagger2.user3@test.com"));
        users.add(new User(4, "Swagger2-User4", "USER", "swagger2.user4@test.com"));
    }

    @ApiOperation(value = "Get Users", response = Iterable.class, tags = "getUsers")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success|OK"),
            @ApiResponse(code = 401, message = "Not Authorized!"),
            @ApiResponse(code = 403, message = "Forbidden!"),
            @ApiResponse(code = 404, message = "Not Found!")})

    @RequestMapping(value = "/users")
    public List<User> getUsers() {
        return users;
    }

    @ApiOperation(value = "Get User by User Id ", response = User.class, tags = "getUserById")
    @RequestMapping(value = "/user/{id}")
    public User getUserById(@PathVariable(value = "id") int id) {
        return users.stream()
                .filter(x -> x.getId() == (id))
                .collect(Collectors.toList())
                .get(0);
    }

    @ApiOperation(value = "Get User by role ", response = User.class, tags = "getUserByRole")
    @RequestMapping(value = "/user/role/{role}")
    public List<User> getUserByRole(@PathVariable(value = "role") String role) {
        return users.stream()
                .filter(x -> x.getRole().equalsIgnoreCase(role))
                .collect(Collectors.toList());
    }

}
