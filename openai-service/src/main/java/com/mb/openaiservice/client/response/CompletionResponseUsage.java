package com.mb.openaiservice.client.response;

public record CompletionResponseUsage(int prompt_tokens, int completion_tokens, int total_tokens) {
}
