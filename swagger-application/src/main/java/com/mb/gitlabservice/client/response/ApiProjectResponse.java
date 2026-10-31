package com.mb.gitlabservice.client.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ApiProjectResponse {

    @Schema(description = "Gitlab project id. Example: 1234")
    private long id;

    @Schema(description = "Gitlab project name.")
    private String name;

    @JsonProperty("path_with_namespace")
    @Schema(description = "Path with namespace.")
    private String pathWithNamespace;
}
