package com.mb.modulith.localstack.client.third_party_api.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ThirdPartyApiResponse {

    private List<ThirdPartyApiObject> objects;
}
