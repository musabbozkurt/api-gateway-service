package com.mb.notificationservice.client.dummysms;

import com.mb.notificationservice.client.dummysms.request.DummySmsOtpRequest;
import com.mb.notificationservice.client.dummysms.response.DummySmsOtpResponse;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange
public interface DummySmsClient {

    @PostExchange
    DummySmsOtpResponse sendOtp(@RequestHeader("Authorization") String authorization,
                                @RequestHeader("dt") String date,
                                @RequestBody DummySmsOtpRequest request);
}
