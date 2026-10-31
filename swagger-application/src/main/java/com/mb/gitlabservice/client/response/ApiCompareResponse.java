package com.mb.gitlabservice.client.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class ApiCompareResponse {

    @Schema(description = "List of differences")
    private List<Object> diffs;
}
