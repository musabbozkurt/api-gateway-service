package com.mb.notificationservice.util;

import com.mb.notificationservice.queue.dto.AttachmentDto;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

public record AttachmentValidationResult(List<AttachmentDto> validAttachments,
                                         List<AttachmentDto> validInlineAttachments,
                                         List<String> skippedAttachments) {

    public AttachmentValidationResult() {
        this(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    public boolean hasAttachments() {
        return CollectionUtils.isNotEmpty(validAttachments) || CollectionUtils.isNotEmpty(validInlineAttachments);
    }
}
