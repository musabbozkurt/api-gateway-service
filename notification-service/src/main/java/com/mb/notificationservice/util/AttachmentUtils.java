package com.mb.notificationservice.util;

import com.mb.notificationservice.config.EmailAttachmentProperties;
import com.mb.notificationservice.queue.dto.AttachmentDto;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AttachmentUtils {

    public static AttachmentValidationResult partition(List<AttachmentDto> attachments, EmailAttachmentProperties properties) {
        AttachmentValidationResult attachmentValidationResult = new AttachmentValidationResult();

        if (CollectionUtils.isEmpty(attachments)) {
            return attachmentValidationResult;
        }

        long totalSize = 0L;
        Set<String> usedContentIds = new HashSet<>();

        for (AttachmentDto attachment : attachments) {
            int validCount = attachmentValidationResult.validAttachments().size() + attachmentValidationResult.validInlineAttachments().size();
            String skipReason = getSkipReason(attachment, properties, validCount, totalSize, usedContentIds);
            if (StringUtils.isNotBlank(skipReason)) {
                attachmentValidationResult.skippedAttachments().add(skipReason);
            } else {
                long decodedSize = estimateDecodedSize(attachment.getContentBase64());
                if (isInline(attachment)) {
                    attachmentValidationResult.validInlineAttachments().add(attachment);
                    usedContentIds.add(attachment.getContentId().trim());
                } else {
                    attachmentValidationResult.validAttachments().add(attachment);
                }
                totalSize += decodedSize;
            }
        }

        return attachmentValidationResult;
    }

    public static String appendSkippedAttachmentsNotice(String body, List<String> skippedAttachments, boolean isHtml) {
        if (CollectionUtils.isEmpty(skippedAttachments)) {
            return body;
        }

        String notice = "The following attachments could not be included: " + String.join(", ", skippedAttachments);
        if (isHtml) {
            return body + "<br><br><p><strong>Note:</strong> " + notice + "</p>";
        }
        return body + "\n\nNote: " + notice;
    }

    public static byte[] decode(AttachmentDto attachment) {
        try {
            return Base64.getDecoder().decode(stripWhitespace(attachment.getContentBase64()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid Base64 content for attachment '%s'".formatted(attachment.getFilename()), e);
        }
    }

    private static String getSkipReason(AttachmentDto attachment, EmailAttachmentProperties properties, int validCount, long totalSize, Set<String> usedContentIds) {
        String rejectionReason = getRejectionReason(attachment, properties, usedContentIds);
        if (StringUtils.isNotBlank(rejectionReason)) {
            return rejectionReason;
        }

        String filename = attachment.getFilename();
        long decodedSize = estimateDecodedSize(attachment.getContentBase64());

        if (decodedSize > properties.getMaxFileSizeBytes()) {
            log.error("Attachment '{}' exceeds max file size of {} bytes", filename, properties.getMaxFileSizeBytes());
            return "%s (exceeds max file size of %d bytes)".formatted(filename, properties.getMaxFileSizeBytes());
        }

        if (!isValidBase64(attachment.getContentBase64())) {
            log.error("Invalid Base64 content for attachment '{}'", filename);
            return "%s (invalid Base64 content)".formatted(filename);
        }

        if (validCount >= properties.getMaxCount()) {
            log.error("Attachment count limit of {} exceeded, skipping '{}'", properties.getMaxCount(), filename);
            return "%s (attachment count limit of %d exceeded)".formatted(filename, properties.getMaxCount());
        }

        if (totalSize + decodedSize > properties.getMaxTotalSizeBytes()) {
            log.error("Total attachment size limit of {} bytes exceeded, skipping '{}'", properties.getMaxTotalSizeBytes(), filename);
            return "%s (total attachment size limit of %d bytes exceeded)".formatted(filename, properties.getMaxTotalSizeBytes());
        }

        return null;
    }

    private static long estimateDecodedSize(String contentBase64) {
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

    private static boolean isInline(AttachmentDto attachment) {
        return StringUtils.isNotBlank(attachment.getContentId());
    }

    private static String stripWhitespace(String value) {
        return value.replaceAll("\\s", "");
    }

    private static String getRejectionReason(AttachmentDto attachment, EmailAttachmentProperties properties, Set<String> usedContentIds) {
        if (attachment == null) {
            log.error("Attachment must not be null");
            return "unknown attachment (attachment must not be null)";
        }

        String filename = attachment.getFilename();
        String displayName = StringUtils.isBlank(filename) ? "unknown" : filename;

        if (StringUtils.isBlank(filename)) {
            log.error("Attachment filename must not be blank");
            return displayName + " (filename must not be blank)";
        }
        if (StringUtils.isBlank(attachment.getContentType())) {
            log.error("Attachment contentType must not be blank for '{}'", filename);
            return displayName + " (content type must not be blank)";
        }
        if (StringUtils.isBlank(attachment.getContentBase64())) {
            log.error("Attachment contentBase64 must not be blank for '{}'", filename);
            return displayName + " (content must not be blank)";
        }

        String contentType = attachment.getContentType().trim().toLowerCase(Locale.ROOT);
        if (!properties.getAllowedContentTypes().contains(contentType)) {
            log.error("Attachment contentType '{}' is not allowed for '{}'", attachment.getContentType(), filename);
            return displayName + " (content type '%s' is not allowed)".formatted(attachment.getContentType());
        }

        if (isInline(attachment)) {
            String contentId = attachment.getContentId().trim();
            if (usedContentIds.contains(contentId)) {
                log.error("Duplicate inline contentId '{}' for '{}'", contentId, filename);
                return displayName + " (duplicate contentId '%s')".formatted(contentId);
            }
            if (!contentType.startsWith("image/")) {
                log.error("Inline attachment '{}' must be an image, got '{}'", filename, attachment.getContentType());
                return displayName + " (inline attachments must be images)";
            }
        }

        return null;
    }

    private static boolean isValidBase64(String contentBase64) {
        try {
            Base64.getDecoder().decode(stripWhitespace(contentBase64));
            return true;
        } catch (IllegalArgumentException _) {
            return false;
        }
    }
}
