package com.github.auties00.cobalt.model.business.support;

import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

import java.util.Objects;
import java.util.Optional;

/**
 * Input model for the WhatsApp support bug-report submission flow.
 *
 * <p>When a user files an in-app "report a problem" form, the form
 * collects a category for the bug (so support can route the report),
 * a description of what went wrong, and optionally a bundle of extra
 * diagnostic context (attachments, OS metadata, report type). This input
 * model carries the typed surface used by the submission and exposes the
 * remaining server-accepted fields through {@link #additionalContextJson()
 * an opaque JSON pass-through} until they are individually typed.
 *
 * <p>Field shape: {@link #category() category} and {@link #description()
 * description} are inferred from the standard WhatsApp Web bug-report
 * submission flow. Additional optional fields (attachments, OS or build
 * metadata, report type) flow through {@code additionalContextJson} as a
 * JSON-encoded pass-through until they are individually typed from live
 * request captures.
 */
@ProtobufMessage(name = "SupportBugReportSubmissionRequest")
public final class SupportBugReportSubmissionRequest {
    /**
     * Server-defined bug category code that routes the report to the right
     * support queue. Recognised values are listed by
     * {@code WAWebBugReportCategoryTypes.CATEGORY_TO_GRAPHQL} in the
     * WhatsApp Web source. Unset omits the field.
     */
    @ProtobufProperty(index = 1, type = ProtobufType.STRING)
    final String category;

    /**
     * Free-text description of the bug the user is reporting. Carries the
     * user-supplied summary of what went wrong, which support reads to
     * triage the report. Unset omits the field.
     */
    @ProtobufProperty(index = 2, type = ProtobufType.STRING)
    final String description;

    /**
     * Pre-encoded JSON object of additional context fields the typed
     * surface does not yet cover (for example diagnostic attachments,
     * OS or build metadata, report-type discriminators). Each top-level
     * key of the JSON object is merged into the submission input. Unset
     * omits all such fields.
     */
    @ProtobufProperty(index = 3, type = ProtobufType.STRING)
    final String additionalContextJson;

    /**
     * Constructs a new {@code SupportBugReportSubmissionRequest}. Every
     * argument may be {@code null} to omit the corresponding field from
     * the request.
     *
     * @param category              the server-defined bug category code,
     *                              or {@code null}
     * @param description           the free-text bug description, or
     *                              {@code null}
     * @param additionalContextJson the JSON pass-through of extra context,
     *                              or {@code null}
     */
    public SupportBugReportSubmissionRequest(String category, String description, String additionalContextJson) {
        this.category = category;
        this.description = description;
        this.additionalContextJson = additionalContextJson;
    }

    /**
     * Returns the bug category code that routes the report.
     *
     * @return an {@link Optional} carrying the category, or empty when
     *         unset
     */
    public Optional<String> category() {
        return Optional.ofNullable(category);
    }

    /**
     * Returns the free-text description of the bug.
     *
     * @return an {@link Optional} carrying the description, or empty when
     *         unset
     */
    public Optional<String> description() {
        return Optional.ofNullable(description);
    }

    /**
     * Returns the JSON pass-through of extra context fields.
     *
     * @return an {@link Optional} carrying the pre-encoded JSON object, or
     *         empty when unset
     */
    public Optional<String> additionalContextJson() {
        return Optional.ofNullable(additionalContextJson);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (SupportBugReportSubmissionRequest) obj;
        return Objects.equals(category, that.category)
                && Objects.equals(description, that.description)
                && Objects.equals(additionalContextJson, that.additionalContextJson);
    }

    @Override
    public int hashCode() {
        return Objects.hash(category, description, additionalContextJson);
    }

    @Override
    public String toString() {
        return "SupportBugReportSubmissionRequest[" +
                "category=" + category + ", " +
                "description=" + description + ", " +
                "additionalContextJson=" + additionalContextJson + ']';
    }
}
