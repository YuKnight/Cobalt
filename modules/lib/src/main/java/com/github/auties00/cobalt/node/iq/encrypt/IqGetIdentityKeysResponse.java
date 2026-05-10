package com.github.auties00.cobalt.node.iq.encrypt;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.iq.IqOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqResultResponseMixin;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Sealed family of inbound reply variants produced by the relay in
 * response to an {@link IqGetIdentityKeysRequest}.
 */
@WhatsAppWebModule(moduleName = "WAWebGetIdentityKeysJob")
public sealed interface IqGetIdentityKeysResponse extends IqOperation.Response
        permits IqGetIdentityKeysResponse.Success, IqGetIdentityKeysResponse.ClientError, IqGetIdentityKeysResponse.ServerError {

    /**
     * Tries each {@link IqGetIdentityKeysResponse} variant in priority order and returns
     * the first that parses cleanly.
     *
     * @param node    the inbound IQ stanza received from the relay;
     *                never {@code null}
     * @param request the original outbound stanza — used to validate
     *                echoed identifiers; never {@code null}
     * @return an {@link Optional} carrying the parsed variant, or
     *         {@link Optional#empty()} when no documented variant
     *         matched the stanza shape
     * @throws NullPointerException if either argument is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WAWebGetIdentityKeysJob",
            exports = "getAndStoreIdentityKeys", adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends IqGetIdentityKeysResponse> of(Node node, Node request) {
        Objects.requireNonNull(node, "node cannot be null");
        Objects.requireNonNull(request, "request cannot be null");
        var success = Success.of(node, request);
        if (success.isPresent()) {
            return success;
        }
        var clientError = ClientError.of(node, request);
        if (clientError.isPresent()) {
            return clientError;
        }
        return ServerError.of(node, request);
    }

    /**
     * The {@code Success} reply variant — the relay returned a
     * mixed list of resolved identity entries and per-device
     * client-error envelopes.
     */
    @WhatsAppWebModule(moduleName = "WAWebGetIdentityKeysJob")
    final class Success implements IqGetIdentityKeysResponse {
        /**
         * The list of per-device identity entries returned by the
         * relay — interleaved {@link IdentityEntry.Resolved} and
         * {@link IdentityEntry.Failure} variants.
         */
        private final List<IdentityEntry> entries;

        /**
         * Constructs a new successful reply.
         *
         * @param entries the per-device identity entries; never
         *                {@code null}
         * @throws NullPointerException if {@code entries} is
         *                              {@code null}
         */
        public Success(List<IdentityEntry> entries) {
            Objects.requireNonNull(entries, "entries cannot be null");
            this.entries = List.copyOf(entries);
        }

        /**
         * Returns the unmodifiable list of identity entries.
         *
         * @return the entries; never {@code null}
         */
        public List<IdentityEntry> entries() {
            return entries;
        }

        /**
         * Tries to parse a {@link Success} variant from the given
         * inbound stanza.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant, or
         *         empty when the stanza does not match the success
         *         schema
         */
        @WhatsAppWebExport(moduleName = "WAWebGetIdentityKeysJob",
                exports = "identityKeysParser", adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Success> of(Node node, Node request) {
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            var listNode = node.getChild("list").orElse(null);
            if (listNode == null) {
                return Optional.empty();
            }
            var entries = new ArrayList<IdentityEntry>();
            for (var userNode : listNode.children()) {
                var deviceJid = userNode.getAttributeAsJid("jid").orElse(null);
                if (deviceJid == null) {
                    return Optional.empty();
                }
                var errorChild = userNode.getChild("error").orElse(null);
                if (errorChild != null) {
                    var errorCode = errorChild.getAttributeAsInt("code").orElse(0);
                    var errorText = errorChild.getAttributeAsString("text").orElse(null);
                    entries.add(new IdentityEntry.Failure(deviceJid, errorCode, errorText));
                    continue;
                }
                var typeChild = userNode.getChild("type").orElse(null);
                var identityChild = userNode.getChild("identity").orElse(null);
                if (typeChild == null || identityChild == null) {
                    return Optional.empty();
                }
                var typeBytes = typeChild.toContentBytes().orElse(null);
                var identityBytes = identityChild.toContentBytes().orElse(null);
                if (typeBytes == null || typeBytes.length < 1 || identityBytes == null) {
                    return Optional.empty();
                }
                entries.add(new IdentityEntry.Resolved(deviceJid, typeBytes[0], identityBytes));
            }
            return Optional.of(new Success(entries));
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
            return Objects.equals(this.entries, that.entries);
        }

        @Override
        public int hashCode() {
            return Objects.hash(entries);
        }

        @Override
        public String toString() {
            return "IqGetIdentityKeysResponse.Success[entries=" + entries + ']';
        }
    }

    /**
     * Per-device identity entry — sealed sub-family carrying either
     * a successfully-resolved {@link Resolved} payload or a
     * relay-rejected {@link Failure} envelope for one of the
     * requested device JIDs.
     */
    @WhatsAppWebModule(moduleName = "WAWebGetIdentityKeysJob")
    sealed interface IdentityEntry permits IdentityEntry.Resolved, IdentityEntry.Failure {

        /**
         * Returns the device JID this entry corresponds to.
         *
         * @return the JID; never {@code null}
         */
        Jid deviceJid();

        /**
         * Successfully-resolved identity entry — carries the
         * per-device long-term identity public key and the
         * single-byte type marker.
         */
        @WhatsAppWebModule(moduleName = "WAWebGetIdentityKeysJob")
        final class Resolved implements IdentityEntry {
            /**
             * The device JID this entry corresponds to.
             */
            private final Jid deviceJid;

            /**
             * The single-byte Signal key-bundle type marker carried
             * by the {@code <type/>} grandchild.
             */
            private final byte keyBundleType;

            /**
             * The device's long-term identity public key bytes
             * carried by the {@code <identity/>} grandchild —
             * thirty-two bytes.
             */
            private final byte[] identityPublicKey;

            /**
             * Constructs a new resolved entry.
             *
             * @param deviceJid         the device JID; never
             *                          {@code null}
             * @param keyBundleType     the type marker
             * @param identityPublicKey the identity public key
             *                          bytes; never {@code null}
             * @throws NullPointerException if any reference argument
             *                              is {@code null}
             */
            public Resolved(Jid deviceJid, byte keyBundleType, byte[] identityPublicKey) {
                this.deviceJid = Objects.requireNonNull(deviceJid, "deviceJid cannot be null");
                this.keyBundleType = keyBundleType;
                this.identityPublicKey = Objects.requireNonNull(identityPublicKey, "identityPublicKey cannot be null");
            }

            @Override
            public Jid deviceJid() {
                return deviceJid;
            }

            /**
             * Returns the type marker.
             *
             * @return the type marker
             */
            public byte keyBundleType() {
                return keyBundleType;
            }

            /**
             * Returns the identity public key bytes.
             *
             * @return the bytes; never {@code null}
             */
            public byte[] identityPublicKey() {
                return identityPublicKey;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) {
                    return true;
                }
                if (obj == null || obj.getClass() != this.getClass()) {
                    return false;
                }
                var that = (Resolved) obj;
                return this.keyBundleType == that.keyBundleType
                        && Objects.equals(this.deviceJid, that.deviceJid)
                        && Arrays.equals(this.identityPublicKey, that.identityPublicKey);
            }

            @Override
            public int hashCode() {
                var result = Objects.hash(deviceJid, keyBundleType);
                result = 31 * result + Arrays.hashCode(identityPublicKey);
                return result;
            }

            @Override
            public String toString() {
                return "IqGetIdentityKeysResponse.IdentityEntry.Resolved[deviceJid=" + deviceJid
                        + ", keyBundleType=" + keyBundleType
                        + ", identityPublicKey=" + Arrays.toString(identityPublicKey) + ']';
            }
        }

        /**
         * Per-device failure entry — the relay could not resolve
         * the identity for this particular device JID.
         */
        @WhatsAppWebModule(moduleName = "WAWebGetIdentityKeysJob")
        final class Failure implements IdentityEntry {
            /**
             * The device JID this failure corresponds to.
             */
            private final Jid deviceJid;

            /**
             * The numeric per-device error code.
             */
            private final int errorCode;

            /**
             * The human-readable per-device error text, when the
             * relay supplied one.
             */
            private final String errorText;

            /**
             * Constructs a new failure entry.
             *
             * @param deviceJid the device JID; never {@code null}
             * @param errorCode the numeric error code
             * @param errorText the optional human-readable text;
             *                  may be {@code null}
             * @throws NullPointerException if {@code deviceJid} is
             *                              {@code null}
             */
            public Failure(Jid deviceJid, int errorCode, String errorText) {
                this.deviceJid = Objects.requireNonNull(deviceJid, "deviceJid cannot be null");
                this.errorCode = errorCode;
                this.errorText = errorText;
            }

            @Override
            public Jid deviceJid() {
                return deviceJid;
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
             * Returns the optional human-readable error text.
             *
             * @return an {@link Optional} carrying the error text,
             *         or empty when the relay omitted it
             */
            public Optional<String> errorText() {
                return Optional.ofNullable(errorText);
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) {
                    return true;
                }
                if (obj == null || obj.getClass() != this.getClass()) {
                    return false;
                }
                var that = (Failure) obj;
                return this.errorCode == that.errorCode
                        && Objects.equals(this.deviceJid, that.deviceJid)
                        && Objects.equals(this.errorText, that.errorText);
            }

            @Override
            public int hashCode() {
                return Objects.hash(deviceJid, errorCode, errorText);
            }

            @Override
            public String toString() {
                return "IqGetIdentityKeysResponse.IdentityEntry.Failure[deviceJid=" + deviceJid
                        + ", errorCode=" + errorCode
                        + ", errorText=" + errorText + ']';
            }
        }
    }

    /**
     * The {@code ClientError} reply variant — the relay rejected the
     * whole bulk-fetch as malformed or unauthorised.
     */
    @WhatsAppWebModule(moduleName = "WAWebGetIdentityKeysJob")
    final class ClientError implements IqGetIdentityKeysResponse {
        /**
         * The numeric server-side error code.
         */
        private final int errorCode;

        /**
         * The human-readable error text, when the relay supplied one.
         */
        private final String errorText;

        /**
         * Constructs a new client-error reply.
         *
         * @param errorCode the numeric error code
         * @param errorText the optional human-readable text; may be
         *                  {@code null}
         */
        public ClientError(int errorCode, String errorText) {
            this.errorCode = errorCode;
            this.errorText = errorText;
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
         * Returns the optional human-readable error text.
         *
         * @return an {@link Optional} carrying the error text, or empty
         *         when the relay omitted it
         */
        public Optional<String> errorText() {
            return Optional.ofNullable(errorText);
        }

        /**
         * Tries to parse a {@link ClientError} variant from the given
         * inbound stanza.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant, or
         *         empty when the stanza does not match the
         *         client-error schema
         */
        @WhatsAppWebExport(moduleName = "WAWebGetIdentityKeysJob",
                exports = "getAndStoreIdentityKeys", adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<ClientError> of(Node node, Node request) {
            var envelope = SmaxBaseServerErrorMixin.parseClientError(node, request).orElse(null);
            if (envelope == null) {
                return Optional.empty();
            }
            return Optional.of(new ClientError(envelope.code(), envelope.text()));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (ClientError) obj;
            return this.errorCode == that.errorCode
                    && Objects.equals(this.errorText, that.errorText);
        }

        @Override
        public int hashCode() {
            return Objects.hash(errorCode, errorText);
        }

        @Override
        public String toString() {
            return "IqGetIdentityKeysResponse.ClientError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }

    /**
     * The {@code ServerError} reply variant — the relay encountered a
     * transient internal failure while processing the bulk-fetch.
     */
    @WhatsAppWebModule(moduleName = "WAWebGetIdentityKeysJob")
    final class ServerError implements IqGetIdentityKeysResponse {
        /**
         * The numeric server-side error code.
         */
        private final int errorCode;

        /**
         * The human-readable error text, when the relay supplied one.
         */
        private final String errorText;

        /**
         * Constructs a new server-error reply.
         *
         * @param errorCode the numeric error code
         * @param errorText the optional human-readable text; may be
         *                  {@code null}
         */
        public ServerError(int errorCode, String errorText) {
            this.errorCode = errorCode;
            this.errorText = errorText;
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
         * Returns the optional human-readable error text.
         *
         * @return an {@link Optional} carrying the error text, or empty
         *         when the relay omitted it
         */
        public Optional<String> errorText() {
            return Optional.ofNullable(errorText);
        }

        /**
         * Tries to parse a {@link ServerError} variant from the given
         * inbound stanza.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant, or
         *         empty when the stanza does not match the
         *         server-error schema
         */
        @WhatsAppWebExport(moduleName = "WAWebGetIdentityKeysJob",
                exports = "getAndStoreIdentityKeys", adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<ServerError> of(Node node, Node request) {
            var envelope = SmaxBaseServerErrorMixin.parseServerError(node, request).orElse(null);
            if (envelope == null) {
                return Optional.empty();
            }
            return Optional.of(new ServerError(envelope.code(), envelope.text()));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (ServerError) obj;
            return this.errorCode == that.errorCode
                    && Objects.equals(this.errorText, that.errorText);
        }

        @Override
        public int hashCode() {
            return Objects.hash(errorCode, errorText);
        }

        @Override
        public String toString() {
            return "IqGetIdentityKeysResponse.ServerError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }
}
