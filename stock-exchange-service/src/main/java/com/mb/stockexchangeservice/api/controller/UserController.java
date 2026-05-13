package com.mb.stockexchangeservice.api.controller;

import com.mb.stockexchangeservice.api.request.ApiUserRequest;
import com.mb.stockexchangeservice.api.response.ApiUserResponse;
import com.mb.stockexchangeservice.mapper.UserMapper;
import com.mb.stockexchangeservice.service.UserService;
import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @GetMapping("/")
    @Observed(name = "getUsers")
    @Operation(description = "Get all users.")
    public ResponseEntity<Page<ApiUserResponse>> getUsers(Pageable pageable) {
        log.info("Received a request to get all users. getAllUsers.");
        return new ResponseEntity<>(userMapper.map(userService.getAllUsers(pageable)), HttpStatus.OK);
    }

    @PostMapping("/")
    @Observed(name = "createUser")
    @Operation(description = "Create user.")
    public ResponseEntity<ApiUserResponse> createUser(@RequestBody ApiUserRequest apiUserRequest) {
        log.info("Received a request to create user. createUser - apiUserRequest: {}", apiUserRequest);
        return new ResponseEntity<>(userMapper.map(userService.createUser(userMapper.map(apiUserRequest))), HttpStatus.CREATED);
    }

    @GetMapping("/{userId}")
    @Observed(name = "getUserById")
    @Operation(description = "Get user by id.")
    public ResponseEntity<ApiUserResponse> getUserById(@PathVariable Long userId) {
        log.info("Received a request to get user by id. getUserById - userId: {}", userId);
        return new ResponseEntity<>(userMapper.map(userService.getUserById(userId)), HttpStatus.OK);
    }

    @PutMapping("/{userId}")
    @Observed(name = "updateUserById")
    @Operation(description = "Update user.")
    public ResponseEntity<ApiUserResponse> updateUserById(@PathVariable Long userId, @RequestBody ApiUserRequest apiUserRequest) {
        log.info("Received a request to update user by id. updateUserById - userId: {}, apiUserRequest: {}", userId, apiUserRequest);
        return new ResponseEntity<>(userMapper.map(userService.updateUserById(userId, userMapper.map(apiUserRequest))), HttpStatus.OK);
    }

    @DeleteMapping("/{userId}")
    @Observed(name = "deleteUserById")
    @Operation(description = "Delete user by id.")
    public ResponseEntity<String> deleteUserById(@PathVariable Long userId) {
        log.info("Received a request to delete user by id. deleteUserById - userId: {}", userId);
        userService.deleteUserById(userId);
        return new ResponseEntity<>("User deleted successfully.", HttpStatus.OK);
    }
}
