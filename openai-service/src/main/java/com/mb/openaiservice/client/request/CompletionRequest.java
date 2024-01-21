package com.mb.openaiservice.client.request;

public record CompletionRequest(String model, String prompt, int temperature, int max_tokens) {
}
