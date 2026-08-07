package com.mb.notificationservice.queue.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * File attachment for email notifications.
 *
 * <p>Example:</p>
 * <pre>{@code
 * {
 *   "filename": "report.xlsx",
 *   "contentType": "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
 *   "contentBase64": "UEsDBBQAAAAI..."
 * }
 * }</pre>
 *
 * <p>Inline image example (referenced in HTML body via {@code cid:contentId}):</p>
 * <pre>{@code
 * {
 *   "filename": "logo.png",
 *   "contentType": "image/png",
 *   "contentBase64": "iVBORw0KGgo...",
 *   "contentId": "company-logo"
 * }
 * }</pre>
 */
@Getter
@Setter
@ToString(exclude = "contentBase64")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Schema(description = "Email file attachment with Base64-encoded content")
public class AttachmentDto {

    /**
     * Original file name including extension.
     * <p>Example: {@code report.xlsx}</p>
     */
    @Schema(description = "Original file name including extension", example = "invoice.pdf")
    private String filename;

    /**
     * MIME content type of the file.
     * <p>Example: {@code application/pdf}</p>
     */
    @Schema(description = "MIME content type of the file", example = "application/pdf")
    private String contentType;

    /**
     * Base64-encoded file content.
     */
    @Schema(description = "Base64-encoded file content", example = "JVBERi0xLjQK...")
    private String contentBase64;

    /**
     * Content ID for inline images embedded in the HTML body.
     * <p>When set, the attachment is embedded inline and must be referenced in the body as
     * {@code &lt;img src="cid:contentId"/&gt;}. Each inline image requires a unique contentId.
     * Only image content types are supported for inline attachments.</p>
     */
    @Schema(description = "Unique content ID for inline images referenced in HTML body via cid:", example = "company-logo")
    private String contentId;
}
