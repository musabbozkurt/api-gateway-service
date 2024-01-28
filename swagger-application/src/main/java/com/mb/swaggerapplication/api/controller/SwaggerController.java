package com.mb.swaggerapplication.api.controller;

import com.mb.swaggerapplication.api.response.User;
import com.mb.swaggerapplication.enums.EventType;
import com.mb.swaggerapplication.queue.producer.SwaggerEventProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SwaggerController {

    private final SwaggerEventProducer swaggerEventProducer;

    private final List<User> users = new ArrayList<>();

    {
        users.add(new User(1, "Swagger-User1", "ADMIN", "swagger.user1@test.com"));
        users.add(new User(2, "Swagger-User2", "SUPERVISOR", "swagger.user2@test.com"));
        users.add(new User(3, "Swagger-User3", "USER", "swagger.user3@test.com"));
        users.add(new User(4, "Swagger-User4", "USER", "swagger.user4@test.com"));
    }

    @GetMapping(value = "/users")
    public List<User> getUsers() {
        return users;
    }

    @GetMapping(value = "/users/{id}")
    public User getUserById(@PathVariable(value = "id") int id) {
        return users.stream()
                .filter(x -> x.getId() == (id))
                .toList()
                .getFirst();
    }

    @GetMapping(value = "/users/roles/{role}")
    public List<User> getUserByRole(@PathVariable(value = "role") String role) {
        return users.stream()
                .filter(x -> x.getRole().equalsIgnoreCase(role))
                .collect(Collectors.toList());
    }

    @GetMapping(value = "/events")
    public void publishSwaggerEvent() {
        swaggerEventProducer.publishEvent("Publish Swagger Event with EventType SWAGGER_EVENT", EventType.SWAGGER_EVENT);
    }

}
