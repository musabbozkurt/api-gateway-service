package com.mb.notificationservice.client.dummysms.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({"GSM", "EID", "MSG"})
public class DummySmsOtpRequest {

    @JsonProperty("GSM")
    private String gsm;

    @JsonProperty("EID")
    private String clientId;

    @JsonProperty("MSG")
    private String message;
}
