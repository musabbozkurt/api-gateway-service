package com.mb.modulith.localstack.integration_test;

import com.mb.modulith.localstack.client.third_party_api.ThirdPartyApiClient;
import com.mb.modulith.localstack.client.third_party_api.response.ThirdPartyApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.BDDAssertions.then;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
class ThirdPartyApiClientTest {

    @Autowired
    private ThirdPartyApiClient thirdPartyApiClient;

    @Test
    @Disabled("Requires a reachable third party API and valid OAuth2 client credentials")
    void getResult_ShouldReturn200AndNonEmptyObjects_WhenPathVariableIsValid() {
        // Arrange
        // Act
        ResponseEntity<ThirdPartyApiResponse> responseEntity = thirdPartyApiClient.getResult("43153596");

        ThirdPartyApiResponse thirdPartyApiResponse = responseEntity.getBody();

        log.info("getResult - thirdPartyApiResponse: {}", thirdPartyApiResponse);

        // Assertions
        then(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        then(thirdPartyApiResponse).isNotNull();
        then(thirdPartyApiResponse.getObjects()).isNotEmpty();
    }
}
