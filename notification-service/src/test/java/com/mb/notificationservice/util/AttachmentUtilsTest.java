package com.mb.notificationservice.util;

import com.mb.notificationservice.config.EmailAttachmentProperties;
import com.mb.notificationservice.queue.dto.AttachmentDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AttachmentUtilsTest {

    private EmailAttachmentProperties properties;

    @BeforeEach
    void setUp() {
        properties = new EmailAttachmentProperties();
        properties.setMaxCount(2);
        properties.setMaxFileSizeBytes(100);
        properties.setMaxTotalSizeBytes(150);
    }

    @Test
    void validate_ShouldPass_WhenAttachmentsAreEmptyOrNull() {
        assertDoesNotThrow(() -> AttachmentUtils.validate(null, properties));
        assertDoesNotThrow(() -> AttachmentUtils.validate(List.of(), properties));
    }

    @Test
    void validate_ShouldPass_WhenAttachmentsAreWithinLimits() {
        AttachmentDto attachment = createAttachment("a.pdf", "application/pdf", "hello");

        assertDoesNotThrow(() -> AttachmentUtils.validate(List.of(attachment), properties));
    }

    @Test
    void validate_ShouldFail_WhenCountExceedsLimit() {
        List<AttachmentDto> attachments = List.of(
                createAttachment("a.pdf", "application/pdf", "a"),
                createAttachment("b.pdf", "application/pdf", "b"),
                createAttachment("c.pdf", "application/pdf", "c")
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> AttachmentUtils.validate(attachments, properties));
        assertTrue(ex.getMessage().contains("Attachment count exceeds limit"));
    }

    @Test
    void validate_ShouldFail_WhenContentTypeIsNotAllowed() {
        AttachmentDto attachment = createAttachment("a.bin", "application/octet-stream", "hello");
        List<AttachmentDto> attachments = List.of(attachment);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> AttachmentUtils.validate(attachments, properties));
        assertTrue(ex.getMessage().contains("is not allowed"));
    }

    @Test
    void validate_ShouldFail_WhenFilenameIsBlank() {
        AttachmentDto attachment = createAttachment(" ", "application/pdf", "hello");
        List<AttachmentDto> attachments = List.of(attachment);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> AttachmentUtils.validate(attachments, properties));
        assertTrue(exception.getMessage().contains("filename must not be blank"));
    }

    @Test
    void validate_ShouldFail_WhenFileSizeExceedsLimit() {
        properties.setMaxFileSizeBytes(4);
        AttachmentDto attachment = createAttachment("a.pdf", "application/pdf", "hello world");
        List<AttachmentDto> attachments = List.of(attachment);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> AttachmentUtils.validate(attachments, properties));
        assertTrue(ex.getMessage().contains("exceeds max file size"));
    }

    @Test
    void validate_ShouldFail_WhenTotalSizeExceedsLimit() {
        properties.setMaxFileSizeBytes(100);
        properties.setMaxTotalSizeBytes(10);
        List<AttachmentDto> attachments = List.of(
                createAttachment("a.pdf", "application/pdf", "hello1"),
                createAttachment("b.pdf", "application/pdf", "hello2")
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> AttachmentUtils.validate(attachments, properties));
        assertTrue(ex.getMessage().contains("Total attachment size exceeds limit"));
    }

    @Test
    void decode_ShouldReturnOriginalBytes() {
        byte[] original = "hello-attachment".getBytes(StandardCharsets.UTF_8);
        AttachmentDto attachment = new AttachmentDto();
        attachment.setFilename("a.txt");
        attachment.setContentType("text/plain");
        attachment.setContentBase64(Base64.getEncoder().encodeToString(original));

        assertArrayEquals(original, AttachmentUtils.decode(attachment));
    }

    @Test
    void decode_ShouldFail_WhenBase64IsInvalid() {
        AttachmentDto attachment = new AttachmentDto();
        attachment.setFilename("a.pdf");
        attachment.setContentType("application/pdf");
        attachment.setContentBase64("!!!not-base64!!!");

        assertThrows(IllegalArgumentException.class, () -> AttachmentUtils.decode(attachment));
    }

    @Test
    void estimateDecodedSize_ShouldMatchActualDecodedLength() {
        String payload = "sample-content-for-size";
        String base64 = Base64.getEncoder().encodeToString(payload.getBytes(StandardCharsets.UTF_8));

        assertEquals(payload.length(), AttachmentUtils.estimateDecodedSize(base64));
    }

    @Test
    void validate_ShouldFail_WhenAttachmentIsNullInList() {
        List<AttachmentDto> attachments = new ArrayList<>();
        attachments.add(null);

        assertThrows(IllegalArgumentException.class, () -> AttachmentUtils.validate(attachments, properties));
    }

    private AttachmentDto createAttachment(String filename, String contentType, String content) {
        AttachmentDto attachment = new AttachmentDto();
        attachment.setFilename(filename);
        attachment.setContentType(contentType);
        attachment.setContentBase64(Base64.getEncoder().encodeToString(content.getBytes(StandardCharsets.UTF_8)));
        return attachment;
    }
}
