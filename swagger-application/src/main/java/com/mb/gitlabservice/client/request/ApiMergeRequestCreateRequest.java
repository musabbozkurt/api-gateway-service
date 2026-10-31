package com.mb.gitlabservice.client.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// https://docs.gitlab.com/ee/api/merge_requests.html#create-mr
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiMergeRequestCreateRequest {

    @NotNull
    @JsonProperty("source_branch")
    @Schema(description = "Source branch name.")
    private String sourceBranch;

    @NotNull
    @JsonProperty("target_branch")
    @Schema(description = "Target branch name.")
    private String targetBranch;

    @NotNull
    private String title;

    @JsonProperty("assignee_id")
    @Schema(description = "Assignee id.")
    private String assigneeId;
}
