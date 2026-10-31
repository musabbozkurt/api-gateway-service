package com.mb.modulith.commerce.api.controller;

import com.mb.modulith.commerce.api.model.request.ApiAddressRequest;
import com.mb.modulith.commerce.api.model.request.ApiUpdateAddressRequest;
import com.mb.modulith.commerce.api.model.response.ApiClientAddressResponse;
import com.mb.modulith.commerce.data.model.Address;
import com.mb.modulith.commerce.mapping.CustomAddressMapper;
import com.mb.modulith.commerce.service.AddressService;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/client/address")
@OpenAPIDefinition(
        info = @Info(
                title = "MB Backend Technical Test API",
                description = "API for MB Backend Technical Test",
                version = "2.0",
                contact = @Contact(
                        name = "Musab Bozkurt",
                        email = "https://github.com/musabbozkurt",
                        url = "https://github.com/musabbozkurt"
                ),
                license = @License(
                        name = "MIT Licence",
                        url = "https://opensource.org/licenses/mit-license.php"
                )
        ))
public class AddressController {

    private final CustomAddressMapper customMapper;
    private final AddressService addressService;

    /**
     * Creates address.
     *
     * @param apiAddressRequest to create address.
     */
    @PostMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Creates address.", description = "Creates address in database.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Address is created", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Address.class)))}),
            @ApiResponse(responseCode = "404", description = "Can not create address", content = @Content)})
    public ResponseEntity<ApiClientAddressResponse> createAddress(@RequestBody ApiAddressRequest apiAddressRequest) {
        log.info("Received a request to create address. ApiAddressRequest: {}", apiAddressRequest);
        return ResponseEntity.ok().body(customMapper.map(addressService.createAddress(apiAddressRequest.getClientId(), customMapper.map(apiAddressRequest))));
    }

    /**
     * returns address by Id
     *
     * @param id address id.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "returns address by Id.")
    public ResponseEntity<ApiClientAddressResponse> getAddressById(@PathVariable UUID id) {
        return ResponseEntity.ok().body(customMapper.map(addressService.getAddressById(id)));
    }

    /**
     * Updates address.
     *
     * @param id                      address id.
     * @param apiUpdateAddressRequest to update address.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Updates address.")
    public ResponseEntity<ApiClientAddressResponse> updateAddress(@PathVariable UUID id, @RequestBody ApiUpdateAddressRequest apiUpdateAddressRequest) {
        log.info("Received a request to update address. ApiAddressRequest: {}", apiUpdateAddressRequest);
        return ResponseEntity.ok().body(customMapper.map(addressService.updateAddress(id, customMapper.map(apiUpdateAddressRequest))));
    }

    /**
     * delete address by id
     *
     * @param id address id.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "deletes address by id")
    public void deleteAddressById(@PathVariable UUID id) {
        log.info("Received a request to delete address. id: {}", id);
        addressService.deleteAddressById(id);
    }

    /**
     * returns address by clientId
     *
     * @param clientId client id.
     */
    @GetMapping("/{clientId}/address")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "returns address by clientId.")
    public ResponseEntity<List<ApiClientAddressResponse>> getAddresssByClientId(@PathVariable UUID clientId) {
        return ResponseEntity.ok().body(customMapper.map(addressService.getAddresssByClientId(clientId)));
    }

}
