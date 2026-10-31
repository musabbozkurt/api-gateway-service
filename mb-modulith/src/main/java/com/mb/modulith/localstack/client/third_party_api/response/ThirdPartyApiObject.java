package com.mb.modulith.localstack.client.third_party_api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ThirdPartyApiObject {

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;
}
