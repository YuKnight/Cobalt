package com.github.auties00.cobalt.node.smax.pushconfig;

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
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Sealed family of inbound reply variants.
 *
 * @implNote {@code WASmaxPushConfigSetRPC.sendSetRPC} tries
 *           {@code Success} → {@code InternalServerError} →
 *           {@code Conflict}.
 */
public sealed interface SmaxPushConfigSetResponse extends SmaxOperation.Response
        permits SmaxPushConfigSetResponse.Success, SmaxPushConfigSetResponse.InternalServerError, SmaxPushConfigSetResponse.Conflict {

    /**
     * Tries each {@link SmaxPushConfigSetResponse} variant in priority order.
     *
     * @param node    the inbound IQ stanza. Never {@code null}
     * @param request the original outbound stanza. Never
     *                {@code null}
     * @return an {@link Optional} carrying the parsed variant
     * @throws NullPointerException if either argument is
     *                              {@code null}
     */
    @WhatsAppWebExport(moduleName = "WASmaxPushConfigSetRPC",
            exports = "sendSetRPC", adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends SmaxPushConfigSetResponse> of(Node node, Node request) {
        Objects.requireNonNull(node, "node cannot be null");
        Objects.requireNonNull(request, "request cannot be null");
        var success = Success.of(node, request);
        if (success.isPresent()) {
            return success;
        }
        var internalServerError = InternalServerError.of(node, request);
        if (internalServerError.isPresent()) {
            return internalServerError;
        }
        return Conflict.of(node, request);
    }

    /**
     * The {@code Success} reply variant. The relay accepted the
     * push-config change.
     *
     * <p>Carries no payload beyond the envelope echo.
     *
     * @implNote {@code WASmaxInPushConfigSetResponseSuccess.parseSetResponseSuccess}
     *           delegates to
     *           {@code WASmaxInPushConfigIQResultResponseMixin.parseIQResultResponseMixin}.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInPushConfigSetResponseSuccess")
    @WhatsAppWebModule(moduleName = "WASmaxInPushConfigIQResultResponseMixin")
    final class Success implements SmaxPushConfigSetResponse {
        /**
         * Constructs a new successful reply.
         */
        public Success() {
        }

        /**
         * Tries to parse a {@link Success} variant.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant
         */
        @WhatsAppWebExport(moduleName = "WASmaxInPushConfigSetResponseSuccess",
                exports = "parseSetResponseSuccess",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Success> of(Node node, Node request) {
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            return Optional.of(new Success());
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            return obj != null && obj.getClass() == this.getClass();
        }

        @Override
        public int hashCode() {
            return Success.class.hashCode();
        }

        @Override
        public String toString() {
            return "SmaxPushConfigSetResponse.Success[]";
        }
    }

    /**
     * The {@code InternalServerError} reply variant carrying
     * {@code (500, "internal-server-error")}.
     *
     * @implNote {@code WASmaxInPushConfigSetResponseInternalServerError.parseSetResponseInternalServerError}
     *           projects the {@code <error/>} child through
     *           {@code WASmaxInPushConfigIQErrorInternalServerErrorMixin}
     *           which asserts the literal {@code (500,
     *           "internal-server-error")} pair.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInPushConfigSetResponseInternalServerError")
    @WhatsAppWebModule(moduleName = "WASmaxInPushConfigIQErrorInternalServerErrorMixin")
    final class InternalServerError implements SmaxPushConfigSetResponse {
        /**
         * Constructs a new internal-server-error reply. The shape
         * carries no payload beyond the asserted
         * {@code (500, "internal-server-error")} pair.
         */
        public InternalServerError() {
        }

        /**
         * Returns the numeric error code. Always {@code 500}.
         *
         * @return the code
         */
        public int errorCode() {
            return 500;
        }

        /**
         * Returns the error text. Always
         * {@code "internal-server-error"}.
         *
         * @return the text
         */
        public String errorText() {
            return "internal-server-error";
        }

        /**
         * Tries to parse an {@link InternalServerError} variant.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant
         */
        @WhatsAppWebExport(moduleName = "WASmaxInPushConfigSetResponseInternalServerError",
                exports = "parseSetResponseInternalServerError",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<InternalServerError> of(Node node, Node request) {
            var envelope = SmaxBaseServerErrorMixin.parseServerError(node, request).orElse(null);
            if (envelope == null) {
                return Optional.empty();
            }
            if (envelope.code() != 500 || !"internal-server-error".equals(envelope.text())) {
                return Optional.empty();
            }
            return Optional.of(new InternalServerError());
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            return obj != null && obj.getClass() == this.getClass();
        }

        @Override
        public int hashCode() {
            return InternalServerError.class.hashCode();
        }

        @Override
        public String toString() {
            return "SmaxPushConfigSetResponse.InternalServerError[]";
        }
    }

    /**
     * The {@code Conflict} reply variant. {@code (409, "conflict")}
     * thrown when the requested registration collides with an
     * existing one (e.g., multiple devices vying for the same push
     * token).
     *
     * @implNote {@code WASmaxInPushConfigSetResponseConflict.parseSetResponseConflict}
     *           projects the {@code <error/>} child through
     *           {@code WASmaxInPushConfigIQErrorConflictMixin}
     *           which asserts the literal
     *           {@code (409, "conflict")} pair.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInPushConfigSetResponseConflict")
    @WhatsAppWebModule(moduleName = "WASmaxInPushConfigIQErrorConflictMixin")
    final class Conflict implements SmaxPushConfigSetResponse {
        /**
         * Constructs a new conflict reply. The shape carries no
         * payload beyond the asserted {@code (409, "conflict")}
         * pair.
         */
        public Conflict() {
        }

        /**
         * Returns the numeric error code. Always {@code 409}.
         *
         * @return the code
         */
        public int errorCode() {
            return 409;
        }

        /**
         * Returns the error text. Always {@code "conflict"}.
         *
         * @return the text
         */
        public String errorText() {
            return "conflict";
        }

        /**
         * Tries to parse a {@link Conflict} variant.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant
         */
        @WhatsAppWebExport(moduleName = "WASmaxInPushConfigSetResponseConflict",
                exports = "parseSetResponseConflict",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Conflict> of(Node node, Node request) {
            var envelope = SmaxBaseServerErrorMixin.parseClientError(node, request).orElse(null);
            if (envelope == null) {
                return Optional.empty();
            }
            if (envelope.code() != 409 || !"conflict".equals(envelope.text())) {
                return Optional.empty();
            }
            return Optional.of(new Conflict());
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            return obj != null && obj.getClass() == this.getClass();
        }

        @Override
        public int hashCode() {
            return Conflict.class.hashCode();
        }

        @Override
        public String toString() {
            return "SmaxPushConfigSetResponse.Conflict[]";
        }
    }
}
