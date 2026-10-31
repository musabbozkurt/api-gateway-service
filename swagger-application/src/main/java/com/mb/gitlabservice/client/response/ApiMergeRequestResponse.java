package com.mb.gitlabservice.client.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// https://docs.gitlab.com/ee/api/merge_requests.html
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiMergeRequestResponse {

    @JsonProperty("iid")
    private long mergeRequestId;

    @JsonProperty("target_branch")
    private String targetBranch;

    @JsonProperty("source_branch")
    private String sourceBranch;

    @JsonProperty("web_url")
    private String webUrl;
}
