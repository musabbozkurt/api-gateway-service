package com.mb.gitlabservice.service.impl;

import com.mb.gitlabservice.client.GitlabClient;
import com.mb.gitlabservice.client.config.GitlabConfigProperties;
import com.mb.gitlabservice.client.request.ApiCompareBranchFilter;
import com.mb.gitlabservice.client.request.ApiMergeRequestCreateRequest;
import com.mb.gitlabservice.client.request.ApiMergeRequestFilter;
import com.mb.gitlabservice.client.request.ApiMergeRequestUpdateRequest;
import com.mb.gitlabservice.client.request.ApiProjectFilter;
import com.mb.gitlabservice.client.response.ApiCompareResponse;
import com.mb.gitlabservice.client.response.ApiMergeRequestResponse;
import com.mb.gitlabservice.client.response.ApiProjectResponse;
import com.mb.gitlabservice.service.GitlabService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class GitlabServiceImpl implements GitlabService {

    private static final Set<ApiProjectResponse> GITLAB_PROJECTS = new HashSet<>();
    private static final Integer RESPONSE_PER_PAGE = 20;
    private static final String PRIVATE = "private";

    private final GitlabClient gitlabClient;
    private final GitlabConfigProperties gitlabConfigProperties;

    @Override
    public Set<ApiProjectResponse> getProjects(ApiProjectFilter apiProjectFilter) {
        if (GITLAB_PROJECTS.isEmpty()) {
            int pageNumber = 1;
            // https://stackoverflow.com/a/45087988
            Set<ApiProjectResponse> projects = gitlabClient.getProjects(RESPONSE_PER_PAGE, pageNumber, apiProjectFilter);
            filterProjects(projects);
            while (projects.size() >= RESPONSE_PER_PAGE) {
                Set<ApiProjectResponse> gitlabProjects = gitlabClient.getProjects(RESPONSE_PER_PAGE, pageNumber++, apiProjectFilter);
                filterProjects(gitlabProjects);
                projects = gitlabProjects;
                if (GITLAB_PROJECTS.size() >= gitlabConfigProperties.getMaxNumberOfProject()) {
                    return GITLAB_PROJECTS;
                }
            }
        }
        return GITLAB_PROJECTS;
    }

    private void filterProjects(Set<ApiProjectResponse> projects) {
        if (StringUtils.isNotBlank(gitlabConfigProperties.getPathWithNamespace())) {
            GITLAB_PROJECTS.addAll(projects
                    .stream()
                    .filter(apiProjectResponse -> apiProjectResponse.getPathWithNamespace().startsWith(gitlabConfigProperties.getPathWithNamespace()))
                    .collect(Collectors.toSet()));
        } else {
            GITLAB_PROJECTS.addAll(projects);
        }
    }

    @Override
    public List<ApiProjectResponse> compareBranches(ApiCompareBranchFilter gitlabFilter) {
        List<ApiProjectResponse> apiProjectResponses = new ArrayList<>();
        getProjects(ApiProjectFilter.builder().visibility(PRIVATE).build()).forEach(apiProjectResponse -> {
            ApiCompareResponse apiCompareResponse;
            try {
                apiCompareResponse = gitlabClient.compareBranches(apiProjectResponse.getId(), gitlabFilter);
            } catch (Exception e) {
                log.error("Exception occurred while branches are being compared. Exception: {}", ExceptionUtils.getStackTrace(e));
                apiCompareResponse = new ApiCompareResponse();
            }
            if (Objects.nonNull(apiCompareResponse.getDiffs()) && !apiCompareResponse.getDiffs().isEmpty()) {
                apiProjectResponses.add(apiProjectResponse);
            }
        });
        return apiProjectResponses;
    }

    @Override
    public List<ApiMergeRequestResponse> getMergeRequests(ApiMergeRequestFilter gitlabFilter) {
        List<ApiMergeRequestResponse> mergeRequests = new ArrayList<>();
        getProjects(ApiProjectFilter.builder().visibility(PRIVATE).build()).forEach(apiProjectResponse -> {
            try {
                mergeRequests.addAll(gitlabClient.getMergeRequests(apiProjectResponse.getId(), gitlabFilter));
            } catch (Exception e) {
                log.error("Exception occurred while fetching merge requests. Exception: {}", ExceptionUtils.getStackTrace(e));
            }
        });
        mergeRequests.stream().map(ApiMergeRequestResponse::getWebUrl).forEach(log::info);
        return mergeRequests;
    }

    @Override
    public List<ApiMergeRequestResponse> createMergeRequests(ApiMergeRequestCreateRequest apiMergeRequestCreateRequest) {
        List<ApiMergeRequestResponse> mergeRequests = new ArrayList<>();
        compareBranches(new ApiCompareBranchFilter(apiMergeRequestCreateRequest.getSourceBranch(), apiMergeRequestCreateRequest.getTargetBranch(), true))
                .forEach(apiProjectResponse -> {
                    try {
                        mergeRequests.add(gitlabClient.createMergeRequest(apiProjectResponse.getId(), apiMergeRequestCreateRequest));
                    } catch (Exception e) {
                        log.error("Exception occurred while creating merge requests. Exception: {}", ExceptionUtils.getStackTrace(e));
                    }
                });
        return mergeRequests;
    }

    @Override
    public List<ApiMergeRequestResponse> updateMergeRequests(ApiMergeRequestUpdateRequest apiMergeRequestUpdateRequest) {
        List<ApiMergeRequestResponse> mergeRequests = new ArrayList<>();
        getProjects(ApiProjectFilter.builder().visibility(PRIVATE).build()).forEach(apiProjectResponse -> {
            try {
                List<ApiMergeRequestResponse> openedMergeRequests = getMergeRequests(ApiMergeRequestFilter.builder().state("opened").build());
                openedMergeRequests
                        .forEach(apiMergeRequestResponse -> mergeRequests.add(gitlabClient.updateMergeRequest(apiProjectResponse.getId(), apiMergeRequestResponse.getMergeRequestId(), apiMergeRequestUpdateRequest)));
            } catch (Exception e) {
                log.error("Exception occurred while updating merge requests. Exception: {}", ExceptionUtils.getStackTrace(e));
            }
        });
        return mergeRequests;
    }
}
