package com.mb.gitlabservice.api;

import com.mb.gitlabservice.client.request.ApiCompareBranchFilter;
import com.mb.gitlabservice.client.request.ApiMergeRequestCreateRequest;
import com.mb.gitlabservice.client.request.ApiMergeRequestFilter;
import com.mb.gitlabservice.client.request.ApiMergeRequestUpdateRequest;
import com.mb.gitlabservice.client.request.ApiProjectFilter;
import com.mb.gitlabservice.client.response.ApiMergeRequestResponse;
import com.mb.gitlabservice.client.response.ApiProjectResponse;
import com.mb.gitlabservice.service.GitlabService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/gitlab")
public class GitlabController {

    private final GitlabService gitlabService;

    /**
     * returns Gitlab Projects
     */
    @GetMapping("/projects")
    @Operation(summary = "Get Gitlab Projects.")
    public ResponseEntity<Set<ApiProjectResponse>> getProjects(ApiProjectFilter gitlabFilter) {
        log.info("Received a request to get Gitlab projects. getProjects - ApiProjectFilter: {}", gitlabFilter);
        return ResponseEntity.ok().body(gitlabService.getProjects(gitlabFilter));
    }

    /**
     * returns result of compared branches
     */
    @GetMapping("/compare-branches")
    @Operation(summary = "Compare branches.")
    public ResponseEntity<List<ApiProjectResponse>> compareBranches(ApiCompareBranchFilter gitlabFilter) {
        log.info("Received a request to compare branches. compareBranches - ApiGitlabFilter: {}", gitlabFilter);
        return ResponseEntity.ok().body(gitlabService.compareBranches(gitlabFilter));
    }

    /**
     * returns merge requests
     */
    @GetMapping("/merge-requests")
    @Operation(summary = "Get all merge requests.")
    public ResponseEntity<List<ApiMergeRequestResponse>> getMergeRequests(ApiMergeRequestFilter gitlabFilter) {
        log.info("Received a request to get merge requests. getMergeRequests - ApiMergeRequestFilter: {}", gitlabFilter);
        return ResponseEntity.ok().body(gitlabService.getMergeRequests(gitlabFilter));
    }

    /**
     * creates merge requests
     */
    @PostMapping("/merge-requests")
    @Operation(summary = "Create merge requests.")
    public ResponseEntity<List<ApiMergeRequestResponse>> createMergeRequests(@RequestBody ApiMergeRequestCreateRequest apiMergeRequestCreateRequest) {
        log.info("Received a request to create merge requests. createMergeRequests - ApiMergeRequest: {}", apiMergeRequestCreateRequest);
        return ResponseEntity.ok().body(gitlabService.createMergeRequests(apiMergeRequestCreateRequest));
    }

    /**
     * updates merge requests
     */
    @PutMapping("/merge-requests")
    @Operation(summary = "Update merge requests.")
    public ResponseEntity<List<ApiMergeRequestResponse>> updateMergeRequests(@RequestBody ApiMergeRequestUpdateRequest apiMergeRequestUpdateRequest) {
        log.info("Received a request to update merge requests. updateMergeRequests - ApiMergeRequestUpdateRequest: {}", apiMergeRequestUpdateRequest);
        return ResponseEntity.ok().body(gitlabService.updateMergeRequests(apiMergeRequestUpdateRequest));
    }
}
