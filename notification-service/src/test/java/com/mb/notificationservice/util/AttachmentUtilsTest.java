package com.mb.notificationservice.util;

import com.mb.notificationservice.config.EmailAttachmentProperties;
import com.mb.notificationservice.queue.dto.AttachmentDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
    void partition_ShouldReturnEmptyLists_WhenAttachmentsAreEmptyOrNull() {
        // Arrange
        List<AttachmentDto> empty = List.of();

        // Act
        AttachmentValidationResult nullResult = AttachmentUtils.partition(null, properties);
        AttachmentValidationResult emptyResult = AttachmentUtils.partition(empty, properties);

        // Assertions
        assertTrue(nullResult.validAttachments().isEmpty());
        assertTrue(nullResult.skippedAttachments().isEmpty());
        assertTrue(emptyResult.validAttachments().isEmpty());
        assertTrue(emptyResult.skippedAttachments().isEmpty());
        assertFalse(nullResult.hasAttachments());
    }

    @Test
    void partition_ShouldReturnAllAttachments_WhenAttachmentsAreWithinLimits() {
        // Arrange
        AttachmentDto attachment = createAttachment("a.pdf", "application/pdf", "hello");

        // Act
        AttachmentValidationResult result = AttachmentUtils.partition(List.of(attachment), properties);

        // Assertions
        assertEquals(1, result.validAttachments().size());
        assertTrue(result.skippedAttachments().isEmpty());
        assertTrue(result.hasAttachments());
    }

    @Test
    void partition_ShouldAttachValidOnesOnly_WhenSomeAttachmentsAreInvalid() {
        // Arrange
        AttachmentDto valid = createAttachment("valid.pdf", "application/pdf", "hello");
        AttachmentDto invalid = createAttachment("invalid.bin", "application/octet-stream", "hello");

        // Act
        AttachmentValidationResult result = AttachmentUtils.partition(List.of(valid, invalid), properties);

        // Assertions
        assertEquals(1, result.validAttachments().size());
        assertEquals("valid.pdf", result.validAttachments().getFirst().getFilename());
        assertEquals(1, result.skippedAttachments().size());
        assertTrue(result.skippedAttachments().getFirst().contains("invalid.bin"));
    }

    @Test
    void partition_ShouldAttachOnlyUpToMaxCount_WhenCountExceedsLimit() {
        // Arrange
        List<AttachmentDto> attachments = List.of(
                createAttachment("a.pdf", "application/pdf", "a"),
                createAttachment("b.pdf", "application/pdf", "b"),
                createAttachment("c.pdf", "application/pdf", "c")
        );

        // Act
        AttachmentValidationResult result = AttachmentUtils.partition(attachments, properties);

        // Assertions
        assertEquals(2, result.validAttachments().size());
        assertEquals(1, result.skippedAttachments().size());
        assertTrue(result.skippedAttachments().getFirst().contains("c.pdf"));
    }

    @Test
    void partition_ShouldSkipAttachment_WhenContentTypeIsNotAllowed() {
        // Arrange
        AttachmentDto attachment = createAttachment("a.bin", "application/octet-stream", "hello");

        // Act
        AttachmentValidationResult result = AttachmentUtils.partition(List.of(attachment), properties);

        // Assertions
        assertTrue(result.validAttachments().isEmpty());
        assertEquals(1, result.skippedAttachments().size());
        assertTrue(result.skippedAttachments().getFirst().contains("is not allowed"));
    }

    @Test
    void partition_ShouldSkipAttachment_WhenFilenameIsBlank() {
        // Arrange
        AttachmentDto attachment = createAttachment(" ", "application/pdf", "hello");

        // Act
        AttachmentValidationResult result = AttachmentUtils.partition(List.of(attachment), properties);

        // Assertions
        assertTrue(result.validAttachments().isEmpty());
        assertEquals(1, result.skippedAttachments().size());
    }

    @Test
    void partition_ShouldSkipAttachment_WhenFileSizeExceedsLimit() {
        // Arrange
        properties.setMaxFileSizeBytes(4);
        AttachmentDto attachment = createAttachment("a.pdf", "application/pdf", "hello world");

        // Act
        AttachmentValidationResult result = AttachmentUtils.partition(List.of(attachment), properties);

        // Assertions
        assertTrue(result.validAttachments().isEmpty());
        assertEquals(1, result.skippedAttachments().size());
        assertTrue(result.skippedAttachments().getFirst().contains("exceeds max file size"));
    }

    @Test
    void partition_ShouldAttachFirstAndSkipSecond_WhenTotalSizeExceedsLimit() {
        // Arrange
        properties.setMaxFileSizeBytes(100);
        properties.setMaxTotalSizeBytes(10);
        List<AttachmentDto> attachments = List.of(
                createAttachment("a.pdf", "application/pdf", "hello1"),
                createAttachment("b.pdf", "application/pdf", "hello2")
        );

        // Act
        AttachmentValidationResult result = AttachmentUtils.partition(attachments, properties);

        // Assertions
        assertEquals(1, result.validAttachments().size());
        assertEquals("a.pdf", result.validAttachments().getFirst().getFilename());
        assertEquals(1, result.skippedAttachments().size());
        assertTrue(result.skippedAttachments().getFirst().contains("b.pdf"));
    }

    @Test
    void partition_ShouldSkipAttachment_WhenAttachmentIsNullInList() {
        // Arrange
        List<AttachmentDto> attachments = new ArrayList<>();
        attachments.add(null);

        // Act
        AttachmentValidationResult result = AttachmentUtils.partition(attachments, properties);

        // Assertions
        assertTrue(result.validAttachments().isEmpty());
        assertEquals(1, result.skippedAttachments().size());
        assertTrue(result.skippedAttachments().getFirst().contains("unknown attachment"));
    }

    @Test
    void partition_ShouldSkipAttachment_WhenContentTypeIsBlank() {
        // Arrange
        AttachmentDto attachment = createAttachment("a.pdf", "application/pdf", "hello");
        attachment.setContentType(" ");

        // Act
        AttachmentValidationResult result = AttachmentUtils.partition(List.of(attachment), properties);

        // Assertions
        assertTrue(result.validAttachments().isEmpty());
        assertEquals(1, result.skippedAttachments().size());
        assertTrue(result.skippedAttachments().getFirst().contains("content type must not be blank"));
    }

    @Test
    void partition_ShouldSkipAttachment_WhenContentBase64IsBlank() {
        // Arrange
        AttachmentDto attachment = createAttachment("a.pdf", "application/pdf", "hello");
        attachment.setContentBase64(" ");

        // Act
        AttachmentValidationResult result = AttachmentUtils.partition(List.of(attachment), properties);

        // Assertions
        assertTrue(result.validAttachments().isEmpty());
        assertEquals(1, result.skippedAttachments().size());
        assertTrue(result.skippedAttachments().getFirst().contains("content must not be blank"));
    }

    @Test
    void partition_ShouldSkipAttachment_WhenBase64IsInvalid() {
        // Arrange
        AttachmentDto attachment = new AttachmentDto();
        attachment.setFilename("a.pdf");
        attachment.setContentType("application/pdf");
        attachment.setContentBase64("!!!not-base64!!!");

        // Act
        AttachmentValidationResult result = AttachmentUtils.partition(List.of(attachment), properties);

        // Assertions
        assertTrue(result.validAttachments().isEmpty());
        assertEquals(1, result.skippedAttachments().size());
        assertTrue(result.skippedAttachments().getFirst().contains("invalid Base64 content"));
    }

    @Test
    void partition_ShouldAcceptAttachment_WhenContentTypeHasDifferentCase() {
        // Arrange
        AttachmentDto attachment = createAttachment("a.pdf", "APPLICATION/PDF", "hello");

        // Act
        AttachmentValidationResult result = AttachmentUtils.partition(List.of(attachment), properties);

        // Assertions
        assertEquals(1, result.validAttachments().size());
        assertTrue(result.skippedAttachments().isEmpty());
    }

    @Test
    void partition_ShouldAcceptAttachment_WhenContentTypeHasWhitespace() {
        // Arrange
        AttachmentDto attachment = createAttachment("a.pdf", " application/pdf ", "hello");

        // Act
        AttachmentValidationResult result = AttachmentUtils.partition(List.of(attachment), properties);

        // Assertions
        assertEquals(1, result.validAttachments().size());
        assertTrue(result.skippedAttachments().isEmpty());
    }

    @Test
    void partition_ShouldAcceptAttachment_WhenBase64HasWhitespace() {
        // Arrange
        AttachmentDto attachment = new AttachmentDto();
        attachment.setFilename("a.pdf");
        attachment.setContentType("application/pdf");
        String base64 = Base64.getEncoder().encodeToString("hello".getBytes(StandardCharsets.UTF_8));
        attachment.setContentBase64(base64.substring(0, 4) + " " + base64.substring(4));

        // Act
        AttachmentValidationResult result = AttachmentUtils.partition(List.of(attachment), properties);

        // Assertions
        assertEquals(1, result.validAttachments().size());
        assertTrue(result.skippedAttachments().isEmpty());
    }

    @Test
    void partition_ShouldSkipAllAttachments_WhenAllAreInvalid() {
        // Arrange
        List<AttachmentDto> attachments = List.of(
                createAttachment("bad.bin", "application/octet-stream", "a"),
                createAttachment(" ", "application/pdf", "b")
        );

        // Act
        AttachmentValidationResult result = AttachmentUtils.partition(attachments, properties);

        // Assertions
        assertTrue(result.validAttachments().isEmpty());
        assertEquals(2, result.skippedAttachments().size());
        assertFalse(result.hasAttachments());
    }

    @Test
    void partition_ShouldHandleMixedFailures_WhenValidInvalidAndCountLimitApply() {
        // Arrange
        properties.setMaxCount(1);
        List<AttachmentDto> attachments = List.of(
                createAttachment("valid.pdf", "application/pdf", "valid"),
                createAttachment("invalid.bin", "application/octet-stream", "bad"),
                createAttachment("overflow.pdf", "application/pdf", "overflow")
        );

        // Act
        AttachmentValidationResult result = AttachmentUtils.partition(attachments, properties);

        // Assertions
        assertEquals(1, result.validAttachments().size());
        assertEquals("valid.pdf", result.validAttachments().getFirst().getFilename());
        assertEquals(2, result.skippedAttachments().size());
        assertTrue(result.skippedAttachments().stream().anyMatch(reason -> reason.contains("invalid.bin")));
        assertTrue(result.skippedAttachments().stream().anyMatch(reason -> reason.contains("overflow.pdf")));
    }

    @Test
    void appendSkippedAttachmentsNotice_ShouldReturnOriginalBody_WhenSkippedListIsEmptyOrNull() {
        // Arrange
        String body = "Test Body";
        List<String> empty = List.of();

        // Act
        String nullResult = AttachmentUtils.appendSkippedAttachmentsNotice(body, null, false);
        String emptyResult = AttachmentUtils.appendSkippedAttachmentsNotice(body, empty, true);

        // Assertions
        assertEquals(body, nullResult);
        assertEquals(body, emptyResult);
    }

    @Test
    void appendSkippedAttachmentsNotice_ShouldJoinMultipleSkippedAttachments_WhenMultipleAreSkipped() {
        // Arrange
        String body = "Test Body";
        List<String> skipped = List.of(
                "a.exe (content type is not allowed)",
                "b.pdf (exceeds max file size)"
        );

        // Act
        String result = AttachmentUtils.appendSkippedAttachmentsNotice(body, skipped, false);

        // Assertions
        assertTrue(result.contains("a.exe (content type is not allowed), b.pdf (exceeds max file size)"));
    }

    @Test
    void decode_ShouldReturnOriginalBytes_WhenBase64HasWhitespace() {
        // Arrange
        byte[] original = "hello-attachment".getBytes(StandardCharsets.UTF_8);
        String base64 = Base64.getEncoder().encodeToString(original);
        AttachmentDto attachment = new AttachmentDto();
        attachment.setFilename("a.txt");
        attachment.setContentType("text/plain");
        attachment.setContentBase64(base64.substring(0, 4) + "\n" + base64.substring(4));

        // Act
        byte[] decoded = AttachmentUtils.decode(attachment);

        // Assertions
        assertArrayEquals(original, decoded);
    }

    @Test
    void decode_ShouldIncludeFilename_WhenBase64IsInvalid() {
        // Arrange
        AttachmentDto attachment = new AttachmentDto();
        attachment.setFilename("report.pdf");
        attachment.setContentType("application/pdf");
        attachment.setContentBase64("!!!not-base64!!!");

        // Act
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> AttachmentUtils.decode(attachment));

        // Assertions
        assertTrue(exception.getMessage().contains("report.pdf"));
    }

    @Test
    void appendSkippedAttachmentsNotice_ShouldAppendPlainTextLine_WhenBodyIsNotHtml() {
        // Arrange
        String body = "Test Body";
        List<String> skipped = List.of("bad.exe (content type is not allowed)");

        // Act
        String result = AttachmentUtils.appendSkippedAttachmentsNotice(body, skipped, false);

        // Assertions
        assertTrue(result.contains("Test Body"));
        assertTrue(result.contains("Note: The following attachments could not be included: bad.exe"));
    }

    @Test
    void appendSkippedAttachmentsNotice_ShouldAppendHtmlLine_WhenBodyIsHtml() {
        // Arrange
        String body = "<p>Test Body</p>";
        List<String> skipped = List.of("bad.exe (content type is not allowed)");

        // Act
        String result = AttachmentUtils.appendSkippedAttachmentsNotice(body, skipped, true);

        // Assertions
        assertTrue(result.contains("<p>Test Body</p>"));
        assertTrue(result.contains("<strong>Note:</strong>"));
        assertTrue(result.contains("bad.exe"));
    }

    @Test
    void decode_ShouldReturnOriginalBytes_WhenContentIsValidBase64() {
        // Arrange
        byte[] original = "hello-attachment".getBytes(StandardCharsets.UTF_8);
        AttachmentDto attachment = new AttachmentDto();
        attachment.setFilename("a.txt");
        attachment.setContentType("text/plain");
        attachment.setContentBase64(Base64.getEncoder().encodeToString(original));

        // Act
        byte[] decoded = AttachmentUtils.decode(attachment);

        // Assertions
        assertArrayEquals(original, decoded);
    }

    @Test
    void decode_ShouldFail_WhenBase64IsInvalid() {
        // Arrange
        AttachmentDto attachment = new AttachmentDto();
        attachment.setFilename("a.pdf");
        attachment.setContentType("application/pdf");
        attachment.setContentBase64("!!!not-base64!!!");

        // Act
        // Assertions
        assertThrows(IllegalArgumentException.class, () -> AttachmentUtils.decode(attachment));
    }

    @Test
    void estimateDecodedSize_ShouldReturnZero_WhenContentIsEmpty() {
        // Arrange
        String contentBase64 = "";

        // Act
        long decodedSize = invokeEstimateDecodedSize(contentBase64);

        // Assertions
        assertEquals(0L, decodedSize);
    }

    @Test
    void estimateDecodedSize_ShouldReturnZero_WhenContentIsWhitespaceOnly() {
        // Arrange
        String contentBase64 = "  \n\t  ";

        // Act
        long decodedSize = invokeEstimateDecodedSize(contentBase64);

        // Assertions
        assertEquals(0L, decodedSize);
    }

    @Test
    void estimateDecodedSize_ShouldReturnDecodedLength_WhenBase64HasNoPadding() {
        // Arrange
        String contentBase64 = Base64.getEncoder().encodeToString("abc".getBytes(StandardCharsets.UTF_8));

        // Act
        long decodedSize = invokeEstimateDecodedSize(contentBase64);

        // Assertions
        assertEquals(3L, decodedSize);
    }

    @Test
    void estimateDecodedSize_ShouldReturnDecodedLength_WhenBase64HasSinglePadding() {
        // Arrange
        String contentBase64 = Base64.getEncoder().encodeToString("ab".getBytes(StandardCharsets.UTF_8));

        // Act
        long decodedSize = invokeEstimateDecodedSize(contentBase64);

        // Assertions
        assertEquals(2L, decodedSize);
        assertTrue(contentBase64.endsWith("="));
        assertFalse(contentBase64.endsWith("=="));
    }

    @Test
    void estimateDecodedSize_ShouldReturnDecodedLength_WhenBase64HasDoublePadding() {
        // Arrange
        String contentBase64 = Base64.getEncoder().encodeToString("a".getBytes(StandardCharsets.UTF_8));

        // Act
        long decodedSize = invokeEstimateDecodedSize(contentBase64);

        // Assertions
        assertEquals(1L, decodedSize);
        assertTrue(contentBase64.endsWith("=="));
    }

    private AttachmentDto createAttachment(String filename, String contentType, String content) {
        AttachmentDto attachment = new AttachmentDto();
        attachment.setFilename(filename);
        attachment.setContentType(contentType);
        attachment.setContentBase64(Base64.getEncoder().encodeToString(content.getBytes(StandardCharsets.UTF_8)));
        return attachment;
    }

    private long invokeEstimateDecodedSize(String contentBase64) {
        Long decodedSize = ReflectionTestUtils.invokeMethod(AttachmentUtils.class, "estimateDecodedSize", contentBase64);
        return Objects.requireNonNull(decodedSize, "estimateDecodedSize returned null");
    }
}
