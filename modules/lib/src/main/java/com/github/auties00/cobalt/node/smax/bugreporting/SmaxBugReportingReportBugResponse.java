package com.github.auties00.cobalt.node.smax.bugreporting;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqResultResponseMixin;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Sealed family of inbound reply variants.
 *
 * @implNote {@code WASmaxBugReportingReportBugRPC.sendReportBugRPC}
 *           tries {@code Success} → {@code Error}.
 */
public sealed interface SmaxBugReportingReportBugResponse extends SmaxOperation.Response
        permits SmaxBugReportingReportBugResponse.Success, SmaxBugReportingReportBugResponse.Error {

    /**
     * Tries each {@link SmaxBugReportingReportBugResponse} variant in priority order.
     *
     * @param node    the inbound IQ stanza. Never {@code null}
     * @param request the original outbound stanza. Never
     *                {@code null}
     * @return an {@link Optional} carrying the parsed variant
     * @throws NullPointerException if either argument is
     *                              {@code null}
     */
    @WhatsAppWebExport(moduleName = "WASmaxBugReportingReportBugRPC",
            exports = "sendReportBugRPC", adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends SmaxBugReportingReportBugResponse> of(Node node, Node request) {
        Objects.requireNonNull(node, "node cannot be null");
        Objects.requireNonNull(request, "request cannot be null");
        var success = Success.of(node, request);
        if (success.isPresent()) {
            return success;
        }
        return Error.of(node, request);
    }

    /**
     * The {@code Success} reply variant. The relay accepted the
     * report and returned a backend-side {@code <task_id/>}.
     *
     * @implNote {@code WASmaxInBugReportingReportBugResponseSuccess.parseReportBugResponseSuccess}
     *           validates the IQ-result envelope (via
     *           {@code WASmaxInBugReportingHackBaseIQResultResponseMixin}
     *           which permits an optional {@code to=USER_JID}
     *           echo on the reply), then extracts the
     *           {@code <task_id>{content}</task_id>} child.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInBugReportingReportBugResponseSuccess")
    @WhatsAppWebModule(moduleName = "WASmaxInBugReportingHackBaseIQResultResponseMixin")
    final class Success implements SmaxBugReportingReportBugResponse {
        /**
         * The optional {@code to} attribute echoed on the reply (a
         * user JID. When the request set {@code from=USER_JID(t)}
         * the reply echoes that JID back into the {@code to}
         * attribute).
         */
        private final Jid replyTo;

        /**
         * The backend-side task id assigned to the report.
         */
        private final String taskIdElementValue;

        /**
         * Constructs a new success reply.
         *
         * @param replyTo            the optional reply-to JID. May
         *                           be {@code null}
         * @param taskIdElementValue the task id. Never {@code null}
         * @throws NullPointerException if {@code taskIdElementValue}
         *                              is {@code null}
         */
        public Success(Jid replyTo, String taskIdElementValue) {
            this.replyTo = replyTo;
            this.taskIdElementValue = Objects.requireNonNull(taskIdElementValue,
                    "taskIdElementValue cannot be null");
        }

        /**
         * Returns the optional reply-to JID.
         *
         * @return an {@link Optional} carrying the JID
         */
        public Optional<Jid> replyTo() {
            return Optional.ofNullable(replyTo);
        }

        /**
         * Returns the backend-side task id.
         *
         * @return the task id. Never {@code null}
         */
        public String taskIdElementValue() {
            return taskIdElementValue;
        }

        /**
         * Tries to parse a {@link Success} variant.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant
         */
        @WhatsAppWebExport(moduleName = "WASmaxInBugReportingReportBugResponseSuccess",
                exports = "parseReportBugResponseSuccess",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Success> of(Node node, Node request) {
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            var taskIdNode = node.getChild("task_id").orElse(null);
            if (taskIdNode == null) {
                return Optional.empty();
            }
            var taskId = taskIdNode.toContentString().orElse(null);
            if (taskId == null) {
                return Optional.empty();
            }
            var replyTo = node.getAttributeAsJid("to").orElse(null);
            return Optional.of(new Success(replyTo, taskId));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (Success) obj;
            return Objects.equals(this.replyTo, that.replyTo)
                    && Objects.equals(this.taskIdElementValue, that.taskIdElementValue);
        }

        @Override
        public int hashCode() {
            return Objects.hash(replyTo, taskIdElementValue);
        }

        @Override
        public String toString() {
            return "SmaxBugReportingReportBugResponse.Success[replyTo=" + replyTo
                    + ", taskIdElementValue=" + taskIdElementValue + ']';
        }
    }

    /**
     * The {@code Error} reply variant. The relay rejected the
     * report with one of two documented codes:
     * {@code 400} bad-request or {@code 500} internal-server-error.
     *
     * @implNote {@code WASmaxInBugReportingReportBugResponseError.parseReportBugResponseError}
     *           validates the IQ-error envelope (via
     *           {@code WASmaxInBugReportingHackBaseIQErrorResponseMixin}
     *           which permits an optional {@code to=USER_JID}
     *           echo on the reply) and routes the {@code <error/>}
     *           child through {@code WASmaxInBugReportingReportBugErrors},
     *           a disjunction over {@code IQErrorBadRequest}
     *           ({@code 400}) and {@code IQErrorInternalServerError}
     *           ({@code 500}). Cobalt collapses the two sub-mixins
     *           into the single {@code (errorCode, errorText)} pair
     *           below.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInBugReportingReportBugResponseError")
    @WhatsAppWebModule(moduleName = "WASmaxInBugReportingHackBaseIQErrorResponseMixin")
    @WhatsAppWebModule(moduleName = "WASmaxInBugReportingReportBugErrors")
    @WhatsAppWebModule(moduleName = "WASmaxInBugReportingIQErrorBadRequestMixin")
    @WhatsAppWebModule(moduleName = "WASmaxInBugReportingIQErrorInternalServerErrorMixin")
    final class Error implements SmaxBugReportingReportBugResponse {
        /**
         * The optional {@code to} attribute echoed on the reply.
         */
        private final Jid replyTo;

        /**
         * The numeric error code. One of {@code 400} or
         * {@code 500}.
         */
        private final int errorCode;

        /**
         * The error text. One of {@code "bad-request"} or
         * {@code "internal-server-error"}.
         */
        private final String errorText;

        /**
         * Constructs a new error reply.
         *
         * @param replyTo   the optional reply-to JID. May be
         *                  {@code null}
         * @param errorCode the numeric error code
         * @param errorText the optional error text. May be
         *                  {@code null}
         */
        public Error(Jid replyTo, int errorCode, String errorText) {
            this.replyTo = replyTo;
            this.errorCode = errorCode;
            this.errorText = errorText;
        }

        /**
         * Returns the optional reply-to JID.
         *
         * @return an {@link Optional} carrying the JID
         */
        public Optional<Jid> replyTo() {
            return Optional.ofNullable(replyTo);
        }

        /**
         * Returns the numeric error code.
         *
         * @return the error code
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
         * Tries to parse an {@link Error} variant.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant
         */
        @WhatsAppWebExport(moduleName = "WASmaxInBugReportingReportBugResponseError",
                exports = "parseReportBugResponseError",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Error> of(Node node, Node request) {
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
            if (code == 400 && "bad-request".equals(text)) {
                // IQErrorBadRequestMixin
            } else if (code == 500 && "internal-server-error".equals(text)) {
                // IQErrorInternalServerErrorMixin
            } else {
                // Unknown code/text pair. Not a documented variant.
                return Optional.empty();
            }
            var replyTo = node.getAttributeAsJid("to").orElse(null);
            return Optional.of(new Error(replyTo, code, text));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (Error) obj;
            return this.errorCode == that.errorCode
                    && Objects.equals(this.replyTo, that.replyTo)
                    && Objects.equals(this.errorText, that.errorText);
        }

        @Override
        public int hashCode() {
            return Objects.hash(replyTo, errorCode, errorText);
        }

        @Override
        public String toString() {
            return "SmaxBugReportingReportBugResponse.Error[replyTo=" + replyTo
                    + ", errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }
}
