package com.mb.modulith.localstack.client.third_party_api;

import com.mb.modulith.localstack.client.third_party_api.response.ThirdPartyApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

public interface ThirdPartyApiClient {

    @GetMapping(value = "/{pathVar}")
    ResponseEntity<ThirdPartyApiResponse> getResult(@PathVariable String pathVar);
}
