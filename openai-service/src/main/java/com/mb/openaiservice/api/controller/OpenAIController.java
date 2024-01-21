package com.mb.openaiservice.api.controller;

import com.mb.openaiservice.client.OpenAIClient;
import com.mb.openaiservice.client.request.CompletionRequest;
import com.mb.openaiservice.client.response.CompletionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/openai")
public class OpenAIController {

    private final OpenAIClient openAIClient;

    /**
     * returns CompletionResponse
     */
    @PostMapping("/completions")
    public ResponseEntity<CompletionResponse> getCompletions(@RequestBody CompletionRequest completionRequest) {
        log.info("Received a request to get completions. getCompletions - CompletionRequest: {}", completionRequest);
        return ResponseEntity.ok().body(openAIClient.getCompletions(completionRequest));
    }
}