package com.mb.notificationservice.util;

import com.mb.notificationservice.queue.dto.AttachmentDto;

import java.util.ArrayList;
import java.util.List;

public record AttachmentValidationResult(List<AttachmentDto> validAttachments, List<String> skippedAttachments) {

    public AttachmentValidationResult() {
        this(new ArrayList<>(), new ArrayList<>());
    }

    public boolean hasAttachments() {
        return !validAttachments.isEmpty();
    }
}
