package com.mb.notificationservice.client.dummysms.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonPropertyOrder({"SENDSMSRSP"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DummySmsOtpResponse {

    @JsonProperty("SENDSMSRSP")
    private SendSmsResponse sendSmsResponse;
}
