package com.mb.gitlabservice.client.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiCompareBranchFilter {

    @NotNull
    @Schema(description = "Source branch name.")
    private String from;

    @NotNull
    @Schema(description = "Target branch name.")
    private String to;

    private boolean straight;
}
