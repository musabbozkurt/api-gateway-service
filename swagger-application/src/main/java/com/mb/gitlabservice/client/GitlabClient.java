package com.mb.gitlabservice.client;

import com.mb.gitlabservice.client.config.GitlabFeignConfig;
import com.mb.gitlabservice.client.request.ApiCompareBranchFilter;
import com.mb.gitlabservice.client.request.ApiMergeRequestCreateRequest;
import com.mb.gitlabservice.client.request.ApiMergeRequestFilter;
import com.mb.gitlabservice.client.request.ApiMergeRequestUpdateRequest;
import com.mb.gitlabservice.client.request.ApiProjectFilter;
import com.mb.gitlabservice.client.response.ApiCompareResponse;
import com.mb.gitlabservice.client.response.ApiMergeRequestResponse;
import com.mb.gitlabservice.client.response.ApiProjectResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Set;

@FeignClient(value = "gitlab-client", url = "${feign.services.gitlab-client.url}", configuration = GitlabFeignConfig.class)
public interface GitlabClient {

    @GetMapping("/projects")
    Set<ApiProjectResponse> getProjects(@RequestParam("per_page") long perPage,
                                        @RequestParam long page,
                                        @SpringQueryMap ApiProjectFilter gitlabFilter);

    @GetMapping("/projects/{id}/repository/compare")
    ApiCompareResponse compareBranches(@PathVariable long id,
                                       @SpringQueryMap ApiCompareBranchFilter gitlabFilter);

    @GetMapping("/projects/{id}/merge_requests")
    List<ApiMergeRequestResponse> getMergeRequests(@PathVariable long id,
                                                   @SpringQueryMap ApiMergeRequestFilter gitlabFilter);

    @PostMapping("/projects/{id}/merge_requests")
    ApiMergeRequestResponse createMergeRequest(@PathVariable long id,
                                               @SpringQueryMap ApiMergeRequestCreateRequest mergeRequest);

    @PutMapping("/projects/{id}/merge_requests/{merge_request_iid}")
    ApiMergeRequestResponse updateMergeRequest(@PathVariable long id,
                                               @PathVariable("merge_request_iid") long mergeRequestId,
                                               @SpringQueryMap ApiMergeRequestUpdateRequest mergeRequest);
}
