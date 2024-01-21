package com.mb.openaiservice.client.response;

public record CompletionResponseChoice(String text, int index, Object logprobs, String finish_reason) {
}
