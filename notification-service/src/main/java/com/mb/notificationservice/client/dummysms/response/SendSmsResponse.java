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
@JsonPropertyOrder({"RTCD", "EXP", "TOTALCOUNT", "SUCCESSCOUNT", "FAILEDCOUNT", "RSP_LIST"})
public class SendSmsResponse {

    @JsonProperty("RTCD")
    private Integer rtcd;

    @JsonProperty("EXP")
    private String exp;

    @JsonProperty("TOTALCOUNT")
    private Integer totalCount;

    @JsonProperty("SUCCESSCOUNT")
    private Integer successCount;

    @JsonProperty("FAILEDCOUNT")
    private Integer failedCount;

    @JsonProperty("RSP_LIST")
    private MessageResponseList messageResponseList;
}
