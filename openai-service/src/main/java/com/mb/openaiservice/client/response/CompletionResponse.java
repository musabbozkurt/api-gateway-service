package com.mb.openaiservice.client.response;

import java.util.List;

public record CompletionResponse(String id,
                                 String object,
                                 int created,
                                 String model,
                                 List<CompletionResponseChoice> choices,
                                 CompletionResponseUsage usage) {
}
