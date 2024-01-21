package com.mb.openaiservice.client.response;

public record CompletionResponse(String id, String object, int created, String model,
                                 CompletionResponseChoice[] choices,
                                 CompletionResponseUsage usage) {
}
