package com.mb.swagger2.api.controller;

import com.mb.swagger2.api.response.User;
import com.mb.swagger2.enums.EventType;
import com.mb.swagger2.queue.producer.Swagger2EventProducer;
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
public class Swagger2Controller {

    private final Swagger2EventProducer swagger2EventProducer;

    private final List<User> users = new ArrayList<>();

    {
        users.add(new User(1, "Swagger2-User1", "ADMIN", "swagger2.user1@test.com"));
        users.add(new User(2, "Swagger2-User2", "SUPERVISOR", "swagger2.user2@test.com"));
        users.add(new User(3, "Swagger2-User3", "USER", "swagger2.user3@test.com"));
        users.add(new User(4, "Swagger2-User4", "USER", "swagger2.user4@test.com"));
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
    public void publishSwagger2Event() {
        swagger2EventProducer.publishEvent("Publish Swagger2 Event with EventType SWAGGER2_EVENT", EventType.SWAGGER2_EVENT);
    }

}
