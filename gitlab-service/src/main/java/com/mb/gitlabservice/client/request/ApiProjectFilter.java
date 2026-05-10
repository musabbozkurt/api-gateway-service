package com.mb.gitlabservice.client.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiProjectFilter {

    @Schema(description = "Limit by visibility public, internal, or private.")
    private String visibility;

    @Schema(description = "Limit by projects explicitly owned by the current user.")
    private Boolean owned;
}
