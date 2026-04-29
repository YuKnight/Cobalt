package com.github.auties00.cobalt.node.smax.support;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

/**
 * Sealed family of inbound reply variants.
 */
public sealed interface SmaxContactFormResponse extends SmaxOperation.Response
        permits SmaxContactFormResponse.ContactFormResponseSuccess,
        SmaxContactFormResponse.ContactFormResponseRetryableError,
        SmaxContactFormResponse.ContactFormResponseError {

    /**
     * Tries each {@link SmaxContactFormResponse} variant in priority order and
     * returns the first that parses cleanly.
     *
     * @param node    the inbound IQ stanza; never {@code null}
     * @param request the original outbound stanza; never
     *                {@code null}
     * @return an {@link Optional} carrying the parsed variant
     * @throws NullPointerException if either argument is
     *                              {@code null}
     */
    @WhatsAppWebExport(moduleName = "WASmaxSupportContactFormRPC",
            exports = "sendContactFormRPC",
            adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends SmaxContactFormResponse> of(Node node, Node request) {
        Objects.requireNonNull(node, "node cannot be null");
        Objects.requireNonNull(request, "request cannot be null");
        var success = ContactFormResponseSuccess.of(node, request);
        if (success.isPresent()) {
            return success;
        }
        var retryableError = ContactFormResponseRetryableError.of(node, request);
        if (retryableError.isPresent()) {
            return retryableError;
        }
        return ContactFormResponseError.of(node, request);
    }

    /**
     * The {@code ContactFormResponseSuccess} reply variant — the
     * relay accepted the form, returning the user-visible
     * acknowledgement message, the ticket id (for follow-ups),
     * and the routing group JID.
     *
     * @implNote {@code WASmaxInSupportContactFormResponseSuccess.parseContactFormResponseSuccess}
     *           validates the {@code <iq>} envelope, extracts the
     *           {@code <response status="ok">} child, and projects
     *           its three {@code <message>} / {@code <ticket_id>}
     *           / {@code <group_jid>} grandchildren into the
     *           triple below.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInSupportContactFormResponseSuccess")
    @WhatsAppWebModule(moduleName = "WASmaxInSupportHackBaseIQResultResponseMixin")
    final class ContactFormResponseSuccess implements SmaxContactFormResponse {
        /**
         * The literal {@code "ok"} status string echoed by the
         * relay.
         */
        private final String responseStatus;

        /**
         * The acknowledgement message text shown to the user.
         */
        private final String responseMessageElementValue;

        /**
         * The opaque ticket id used for follow-up correspondence.
         */
        private final String responseTicketIdElementValue;

        /**
         * The routing group JID assigned to the case.
         */
        private final String responseGroupJidElementValue;

        /**
         * Constructs a new successful reply.
         *
         * @param responseStatus              the status; never
         *                                    {@code null}
         * @param responseMessageElementValue the acknowledgement
         *                                    text; never
         *                                    {@code null}
         * @param responseTicketIdElementValue the ticket id; never
         *                                    {@code null}
         * @param responseGroupJidElementValue the routing group
         *                                    JID; never
         *                                    {@code null}
         * @throws NullPointerException if any argument is
         *                              {@code null}
         */
        public ContactFormResponseSuccess(String responseStatus,
                                          String responseMessageElementValue,
                                          String responseTicketIdElementValue,
                                          String responseGroupJidElementValue) {
            this.responseStatus = Objects.requireNonNull(responseStatus, "responseStatus cannot be null");
            this.responseMessageElementValue = Objects.requireNonNull(responseMessageElementValue,
                    "responseMessageElementValue cannot be null");
            this.responseTicketIdElementValue = Objects.requireNonNull(responseTicketIdElementValue,
                    "responseTicketIdElementValue cannot be null");
            this.responseGroupJidElementValue = Objects.requireNonNull(responseGroupJidElementValue,
                    "responseGroupJidElementValue cannot be null");
        }

        /**
         * Returns the response status.
         *
         * @return the status
         */
        public String responseStatus() {
            return responseStatus;
        }

        /**
         * Returns the acknowledgement text.
         *
         * @return the text
         */
        public String responseMessageElementValue() {
            return responseMessageElementValue;
        }

        /**
         * Returns the ticket id.
         *
         * @return the id
         */
        public String responseTicketIdElementValue() {
            return responseTicketIdElementValue;
        }

        /**
         * Returns the routing group JID.
         *
         * @return the JID
         */
        public String responseGroupJidElementValue() {
            return responseGroupJidElementValue;
        }

        /**
         * Tries to parse a {@link ContactFormResponseSuccess}
         * variant.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant
         */
        @WhatsAppWebExport(moduleName = "WASmaxInSupportContactFormResponseSuccess",
                exports = "parseContactFormResponseSuccess",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<ContactFormResponseSuccess> of(Node node, Node request) {
            if (!node.hasDescription("iq")) {
                return Optional.empty();
            }
            if (!node.hasAttribute("type", "result")) {
                return Optional.empty();
            }
            var requestId = request.getAttributeAsString("id").orElse(null);
            if (requestId == null || !node.hasAttribute("id", requestId)) {
                return Optional.empty();
            }
            var responseChild = node.getChild("response").orElse(null);
            if (responseChild == null) {
                return Optional.empty();
            }
            if (!responseChild.hasAttribute("status", "ok")) {
                return Optional.empty();
            }
            var messageNode = responseChild.getChild("message").orElse(null);
            if (messageNode == null) {
                return Optional.empty();
            }
            var messageValue = messageNode.toContentString().orElse(null);
            if (messageValue == null) {
                return Optional.empty();
            }
            var ticketIdNode = responseChild.getChild("ticket_id").orElse(null);
            if (ticketIdNode == null) {
                return Optional.empty();
            }
            var ticketIdValue = ticketIdNode.toContentString().orElse(null);
            if (ticketIdValue == null) {
                return Optional.empty();
            }
            var groupJidNode = responseChild.getChild("group_jid").orElse(null);
            if (groupJidNode == null) {
                return Optional.empty();
            }
            var groupJidValue = groupJidNode.toContentString().orElse(null);
            if (groupJidValue == null) {
                return Optional.empty();
            }
            return Optional.of(new ContactFormResponseSuccess("ok", messageValue,
                    ticketIdValue, groupJidValue));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (ContactFormResponseSuccess) obj;
            return Objects.equals(this.responseStatus, that.responseStatus)
                    && Objects.equals(this.responseMessageElementValue, that.responseMessageElementValue)
                    && Objects.equals(this.responseTicketIdElementValue, that.responseTicketIdElementValue)
                    && Objects.equals(this.responseGroupJidElementValue, that.responseGroupJidElementValue);
        }

        @Override
        public int hashCode() {
            return Objects.hash(responseStatus, responseMessageElementValue,
                    responseTicketIdElementValue, responseGroupJidElementValue);
        }

        @Override
        public String toString() {
            return "SmaxContactFormResponse.ContactFormResponseSuccess[responseStatus="
                    + responseStatus
                    + ", responseMessageElementValue=" + responseMessageElementValue
                    + ", responseTicketIdElementValue=" + responseTicketIdElementValue
                    + ", responseGroupJidElementValue=" + responseGroupJidElementValue + ']';
        }
    }

    /**
     * The {@code ContactFormResponseRetryableError} reply variant
     * — the relay deferred the form (e.g. transient back-pressure)
     * and recommended a retry timestamp.
     *
     * @implNote {@code WASmaxInSupportContactFormResponseRetryableError.parseContactFormResponseRetryableError}
     *           extracts the {@code <response error_code="…"
     *           next_retry_ts="…"/>} child without requiring the
     *           {@code status="ok"} marker.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInSupportContactFormResponseRetryableError")
    final class ContactFormResponseRetryableError implements SmaxContactFormResponse {
        /**
         * The numeric error code projected from the {@code response}
         * child.
         */
        private final int responseErrorCode;

        /**
         * The optional recommended retry-after timestamp (Unix
         * epoch text).
         */
        private final String responseNextRetryTs;

        /**
         * Constructs a new retryable-error reply.
         *
         * @param responseErrorCode    the error code
         * @param responseNextRetryTs  the optional retry timestamp;
         *                             may be {@code null}
         */
        public ContactFormResponseRetryableError(int responseErrorCode, String responseNextRetryTs) {
            this.responseErrorCode = responseErrorCode;
            this.responseNextRetryTs = responseNextRetryTs;
        }

        /**
         * Returns the error code.
         *
         * @return the code
         */
        public int responseErrorCode() {
            return responseErrorCode;
        }

        /**
         * Returns the optional retry timestamp.
         *
         * @return an {@link Optional} carrying the timestamp
         */
        public Optional<String> responseNextRetryTs() {
            return Optional.ofNullable(responseNextRetryTs);
        }

        /**
         * Tries to parse a {@link ContactFormResponseRetryableError}
         * variant.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant
         */
        @WhatsAppWebExport(moduleName = "WASmaxInSupportContactFormResponseRetryableError",
                exports = "parseContactFormResponseRetryableError",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<ContactFormResponseRetryableError> of(Node node, Node request) {
            if (!node.hasDescription("iq")) {
                return Optional.empty();
            }
            if (!node.hasAttribute("type", "result")) {
                return Optional.empty();
            }
            var requestId = request.getAttributeAsString("id").orElse(null);
            if (requestId == null || !node.hasAttribute("id", requestId)) {
                return Optional.empty();
            }
            var responseChild = node.getChild("response").orElse(null);
            if (responseChild == null) {
                return Optional.empty();
            }
            var errorCode = responseChild.getAttributeAsInt("error_code");
            if (errorCode.isEmpty()) {
                return Optional.empty();
            }
            var nextRetryTs = responseChild.getAttributeAsString("next_retry_ts").orElse(null);
            return Optional.of(new ContactFormResponseRetryableError(errorCode.getAsInt(), nextRetryTs));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (ContactFormResponseRetryableError) obj;
            return this.responseErrorCode == that.responseErrorCode
                    && Objects.equals(this.responseNextRetryTs, that.responseNextRetryTs);
        }

        @Override
        public int hashCode() {
            return Objects.hash(responseErrorCode, responseNextRetryTs);
        }

        @Override
        public String toString() {
            return "SmaxContactFormResponse.ContactFormResponseRetryableError[responseErrorCode="
                    + responseErrorCode
                    + ", responseNextRetryTs=" + responseNextRetryTs + ']';
        }
    }

    /**
     * The {@code ContactFormResponseError} reply variant — the
     * relay rejected the form with one of the documented error
     * codes ({@code 400} bad-request, {@code 475} notice-required,
     * {@code 500} internal-server-error). Carries the parsed
     * {@code (code, text)} pair and, for the
     * {@code notice-required} sub-case, the surfaced
     * {@code tos_version}.
     *
     * @implNote {@code WASmaxInSupportContactFormResponseError.parseContactFormResponseError}
     *           extracts the {@code <error/>} child and routes it
     *           through
     *           {@code WASmaxInSupportContactFormError} —
     *           a disjunction over the three sub-mixins
     *           ({@code IQErrorBadRequest},
     *           {@code IQErrorNoticeRequired},
     *           {@code IQErrorInternalServerError}). Cobalt
     *           collapses all three into the single pair below
     *           plus the optional {@code tosVersion}.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInSupportContactFormResponseError")
    @WhatsAppWebModule(moduleName = "WASmaxInSupportContactFormError")
    @WhatsAppWebModule(moduleName = "WASmaxInSupportIQErrorBadRequestMixin")
    @WhatsAppWebModule(moduleName = "WASmaxInSupportIQErrorNoticeRequiredMixin")
    @WhatsAppWebModule(moduleName = "WASmaxInSupportIQErrorInternalServerErrorMixin")
    final class ContactFormResponseError implements SmaxContactFormResponse {
        /**
         * The numeric error code ({@code 400}, {@code 475}, or
         * {@code 500}).
         */
        private final int errorCode;

        /**
         * The error text ({@code "bad-request"} /
         * {@code "notice-required"} /
         * {@code "internal-server-error"}).
         */
        private final String errorText;

        /**
         * The {@code tos_version} carried only by the
         * {@code notice-required} sub-variant ({@code 475}); may
         * be {@code null} for the other two codes.
         */
        private final Integer tosVersion;

        /**
         * Constructs a new error reply.
         *
         * @param errorCode  the numeric code
         * @param errorText  the optional error text; may be
         *                   {@code null}
         * @param tosVersion the optional ToS version; may be
         *                   {@code null}
         */
        public ContactFormResponseError(int errorCode, String errorText, Integer tosVersion) {
            this.errorCode = errorCode;
            this.errorText = errorText;
            this.tosVersion = tosVersion;
        }

        /**
         * Returns the numeric error code.
         *
         * @return the code
         */
        public int errorCode() {
            return errorCode;
        }

        /**
         * Returns the optional error text.
         *
         * @return an {@link Optional} carrying the text
         */
        public Optional<String> errorText() {
            return Optional.ofNullable(errorText);
        }

        /**
         * Returns the optional {@code tos_version}.
         *
         * @return an {@link Optional} carrying the version, or
         *         empty when the error is not {@code notice-required}
         */
        public Optional<Integer> tosVersion() {
            return Optional.ofNullable(tosVersion);
        }

        /**
         * Tries to parse a {@link ContactFormResponseError}
         * variant.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant
         */
        @WhatsAppWebExport(moduleName = "WASmaxInSupportContactFormResponseError",
                exports = "parseContactFormResponseError",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<ContactFormResponseError> of(Node node, Node request) {
            // 4xx → ClientError envelope, 5xx → ServerError envelope.
            var clientEnvelope = SmaxBaseServerErrorMixin.parseClientError(node, request).orElse(null);
            var serverEnvelope = clientEnvelope == null
                    ? SmaxBaseServerErrorMixin.parseServerError(node, request).orElse(null)
                    : null;
            var envelope = clientEnvelope != null ? clientEnvelope : serverEnvelope;
            if (envelope == null) {
                return Optional.empty();
            }
            var code = envelope.code();
            var text = envelope.text();
            // Validate the code/text pair against the documented disjunction.
            Integer tosVersion = null;
            if (code == 400 && "bad-request".equals(text)) {
                // IQErrorBadRequestMixin
            } else if (code == 475 && "notice-required".equals(text)) {
                // IQErrorNoticeRequiredMixin — the <error/> child also carries tos_version.
                var errorChild = node.getChild("error").orElse(null);
                if (errorChild != null) {
                    var tos = errorChild.getAttributeAsInt("tos_version");
                    if (tos.isPresent() && tos.getAsInt() >= 1 && tos.getAsInt() <= 65535) {
                        tosVersion = tos.getAsInt();
                    }
                }
            } else if (code == 500 && "internal-server-error".equals(text)) {
                // IQErrorInternalServerErrorMixin
            } else {
                // Unknown code/text pair — not a documented variant.
                return Optional.empty();
            }
            return Optional.of(new ContactFormResponseError(code, text, tosVersion));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (ContactFormResponseError) obj;
            return this.errorCode == that.errorCode
                    && Objects.equals(this.errorText, that.errorText)
                    && Objects.equals(this.tosVersion, that.tosVersion);
        }

        @Override
        public int hashCode() {
            return Objects.hash(errorCode, errorText, tosVersion);
        }

        @Override
        public String toString() {
            return "SmaxContactFormResponse.ContactFormResponseError[errorCode=" + errorCode
                    + ", errorText=" + errorText
                    + ", tosVersion=" + tosVersion + ']';
        }
    }
}
