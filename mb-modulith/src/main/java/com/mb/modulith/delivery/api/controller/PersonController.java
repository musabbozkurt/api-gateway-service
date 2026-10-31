package com.mb.modulith.delivery.api.controller;

import com.mb.modulith.delivery.api.request.ApiPersonRequest;
import com.mb.modulith.delivery.api.response.ApiPersonResponse;
import com.mb.modulith.delivery.data.model.Person;
import com.mb.modulith.delivery.mapper.PersonMapper;
import com.mb.modulith.delivery.service.PersonService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PersonController {

    private final PersonService personService;
    private final PersonMapper personMapper;

    @PostMapping(path = "/api/person")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Register a customer or delivery agent")
    public ResponseEntity<ApiPersonResponse> register(@RequestBody ApiPersonRequest apiPersonRequest) {
        return ResponseEntity.ok(personMapper.map(personService.save(personMapper.map(apiPersonRequest))));
    }

    @GetMapping(path = "/api/person")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "List registered participants")
    public ResponseEntity<List<ApiPersonResponse>> getAllPersons() {
        return ResponseEntity.ok(personMapper.map(personService.getAll()));
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(path = "/api/person/{id}")
    @Operation(summary = "Get participant by id")
    public ResponseEntity<Person> getPersonById(@PathVariable Long id) {
        Person person = personService.findById(id);
        if (person != null) {
            return ResponseEntity.ok(person);
        }
        return ResponseEntity.notFound().build();
    }
}
