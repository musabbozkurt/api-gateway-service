package com.mb.notificationservice.config;

import com.mb.notificationservice.client.dummysms.DummySmsClient;
import com.mb.notificationservice.client.dummysms.request.DummySmsOtpRequest;
import com.mb.notificationservice.exception.BaseException;
import com.mb.notificationservice.exception.NotificationErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class DummySmsClientConfigTest {

    private DummySmsClient dummySmsClient;
    private MockRestServiceServer mockServer;
    private DummySmsOtpRequest request;

    @BeforeEach
    void setUp() {
        request = new DummySmsOtpRequest("5554443322", "client", "msg");
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("http://localhost/sms")
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .defaultStatusHandler(status -> !status.is2xxSuccessful(), (_, _) -> {
                    throw new BaseException(NotificationErrorCode.DUMMY_SMS_ERROR);
                });

        mockServer = MockRestServiceServer.bindTo(builder).build();

        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(builder.build()))
                .build();

        dummySmsClient = factory.createClient(DummySmsClient.class);
    }

    @Test
    void sendOtp_ShouldReturnResponse_WhenStatusIsOk() {
        mockServer.expect(requestTo("http://localhost/sms"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "test-auth"))
                .andExpect(header("dt", "test-date"))
                .andRespond(withSuccess("""
                        {
                            "SENDSMSRSP":
                            {
                                "SUCCESSCOUNT": 1
                             }
                        }
                        """, MediaType.APPLICATION_JSON));

        var response = dummySmsClient.sendOtp("test-auth", "test-date", request);

        assertNotNull(response);
        mockServer.verify();
    }

    @Test
    void sendOtp_ShouldThrowException_WhenStatusIsBadRequest() {
        mockServer.expect(requestTo("http://localhost/sms"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withBadRequest());

        BaseException exception = assertThrows(BaseException.class, () -> dummySmsClient.sendOtp("auth", "date", request));

        assertEquals(NotificationErrorCode.DUMMY_SMS_ERROR, exception.getErrorCode());
        mockServer.verify();
    }

    @Test
    void sendOtp_ShouldThrowException_WhenStatusIsServerError() {
        mockServer.expect(requestTo("http://localhost/sms"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withServerError());

        BaseException exception = assertThrows(BaseException.class, () -> dummySmsClient.sendOtp("auth", "date", request));

        assertEquals(NotificationErrorCode.DUMMY_SMS_ERROR, exception.getErrorCode());
        mockServer.verify();
    }
}
