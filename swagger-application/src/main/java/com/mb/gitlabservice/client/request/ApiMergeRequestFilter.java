package com.mb.gitlabservice.client.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// https://docs.gitlab.com/ee/api/merge_requests.html
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiMergeRequestFilter {

    @Schema(description = "Return all merge requests or just those that are opened, closed, locked, or merged.")
    private String state;
}
