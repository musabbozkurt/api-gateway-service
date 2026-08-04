package com.mb.notificationservice.util;

import com.mb.notificationservice.config.EmailAttachmentProperties;
import com.mb.notificationservice.queue.dto.AttachmentDto;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Base64;
import java.util.List;
import java.util.Locale;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AttachmentUtils {

    public static void validate(List<AttachmentDto> attachments, EmailAttachmentProperties properties) {
        if (CollectionUtils.isEmpty(attachments)) {
            return;
        }

        if (attachments.size() > properties.getMaxCount()) {
            throw new IllegalArgumentException("Attachment count exceeds limit of %d".formatted(properties.getMaxCount()));
        }

        long totalSize = 0L;
        for (AttachmentDto attachment : attachments) {
            validateAttachment(attachment, properties);
            long decodedSize = estimateDecodedSize(attachment.getContentBase64());
            if (decodedSize > properties.getMaxFileSizeBytes()) {
                throw new IllegalArgumentException("Attachment '%s' exceeds max file size of %d bytes".formatted(attachment.getFilename(), properties.getMaxFileSizeBytes()));
            }
            totalSize += decodedSize;
            if (totalSize > properties.getMaxTotalSizeBytes()) {
                throw new IllegalArgumentException("Total attachment size exceeds limit of %d bytes".formatted(properties.getMaxTotalSizeBytes()));
            }
        }
    }

    public static byte[] decode(AttachmentDto attachment) {
        try {
            return Base64.getDecoder().decode(stripWhitespace(attachment.getContentBase64()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid Base64 content for attachment '" + attachment.getFilename() + "'", e);
        }
    }

    private static void validateAttachment(AttachmentDto attachment, EmailAttachmentProperties properties) {
        if (attachment == null) {
            throw new IllegalArgumentException("Attachment must not be null");
        }
        if (StringUtils.isBlank(attachment.getFilename())) {
            throw new IllegalArgumentException("Attachment filename must not be blank");
        }
        if (StringUtils.isBlank(attachment.getContentType())) {
            throw new IllegalArgumentException("Attachment contentType must not be blank for '%s'".formatted(attachment.getFilename()));
        }
        if (StringUtils.isBlank(attachment.getContentBase64())) {
            throw new IllegalArgumentException("Attachment contentBase64 must not be blank for '" + attachment.getFilename() + "'");
        }

        String contentType = attachment.getContentType().trim().toLowerCase(Locale.ROOT);
        if (!properties.getAllowedContentTypes().contains(contentType)) {
            throw new IllegalArgumentException("Attachment contentType '%s' is not allowed for '%s'".formatted(attachment.getContentType(), attachment.getFilename()));
        }
    }

    /**
     * Estimates decoded byte length from Base64 without allocating the decoded array.
     * Formula: floor(base64Length * 3 / 4) after removing whitespace and accounting for padding.
     */
    public static long estimateDecodedSize(String contentBase64) {
        String normalized = stripWhitespace(contentBase64);
        int length = normalized.length();
        if (length == 0) {
            return 0L;
        }

        int padding = 0;
        if (normalized.charAt(length - 1) == '=') {
            padding++;
            if (length > 1 && normalized.charAt(length - 2) == '=') {
                padding++;
            }
        }
        return ((long) length * 3 / 4) - padding;
    }

    private static String stripWhitespace(String value) {
        return value.replaceAll("\\s", "");
    }
}
