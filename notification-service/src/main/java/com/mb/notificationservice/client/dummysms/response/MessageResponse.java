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
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"MSGID", "EID", "CNO", "RESULT"})
public class MessageResponse {

    @JsonProperty("MSGID")
    private String messageId;

    @JsonProperty("EID")
    private String clientId;

    @JsonProperty("CNO")
    private String cno;

    @JsonProperty("RESULT")
    private String result;
}
