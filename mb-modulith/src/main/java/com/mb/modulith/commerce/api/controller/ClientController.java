package com.mb.modulith.commerce.api.controller;

import com.mb.modulith.commerce.api.model.request.ApiClientRequest;
import com.mb.modulith.commerce.api.model.response.ApiClientResponse;
import com.mb.modulith.commerce.mapping.ClientMapper;
import com.mb.modulith.commerce.service.ClientService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/client")
public class ClientController {

    private final ClientService clientService;
    private final ClientMapper clientMapper;

    /**
     * Creates client.
     *
     * @param apiClientRequest to create client.
     */
    @PostMapping
    @PreAuthorize("hasRole('CREATE_CLIENT')")
    @Operation(summary = "Creates client.")
    public ResponseEntity<ApiClientResponse> createClient(@RequestBody ApiClientRequest apiClientRequest) {
        log.info("Received a request to create Client. ApiAddressRequest: {}", apiClientRequest);
        return ResponseEntity.ok().body(clientMapper.map(clientService.createClient(clientMapper.map(apiClientRequest))));
    }

    /**
     * returns client by Id
     *
     * @param id client id.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('GET_CLIENT')")
    @Operation(summary = "returns client by Id.")
    public ResponseEntity<ApiClientResponse> getClientById(@PathVariable UUID id) {
        return ResponseEntity.ok().body(clientMapper.map(clientService.getClientById(id)));
    }

    /**
     * Updates client.
     *
     * @param id               client id.
     * @param apiClientRequest to update client.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('UPDATE_CLIENT')")
    @Operation(summary = "Updates client.")
    public ResponseEntity<ApiClientResponse> updateClient(@PathVariable UUID id, @RequestBody ApiClientRequest apiClientRequest) {
        log.info("Received a request to update Client. ApiAddressRequest: {}", apiClientRequest);
        return ResponseEntity.ok().body(clientMapper.map(clientService.updateClient(id, clientMapper.map(apiClientRequest))));
    }

    /**
     * delete client by id
     *
     * @param id client id.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('DELETE_CLIENT')")
    @Operation(summary = "deletes client by id")
    public void deleteClientById(@PathVariable UUID id) {
        log.info("Received a request to delete client. id: {}", id);
        clientService.deleteClientById(id);
    }
}
