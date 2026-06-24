package com.github.auties00.cobalt.exception;

import com.github.auties00.cobalt.client.linked.WhatsAppLinkedClientErrorResult;
import com.github.auties00.cobalt.client.linked.WhatsAppLinkedClientErrorHandler;
import com.github.auties00.cobalt.model.cloud.CloudApiVersion;

import java.util.Optional;
import java.util.OptionalInt;

/**
 * Sealed root for failures encountered while talking to Meta's WhatsApp Cloud API.
 *
 * <p>The Cloud transport reaches {@code graph.facebook.com} over HTTPS for outbound traffic and
 * receives inbound traffic through a webhook the integrator hosts. Each layer has its own failure
 * mode, enumerated by the nested subtypes: the graph endpoint rejected a request with a structured
 * error envelope ({@link CloudApiException}), the configured access token is missing, invalid, or
 * expired so no request can succeed ({@link CloudAuthException}), an inbound webhook delivery failed
 * its signature check or could not be parsed ({@link CloudWebhookException}), or an operation requires
 * a newer Cloud API version than the client is configured to target
 * ({@link CloudUnsupportedVersionException}).
 *
 * @apiNote
 * Embedders typically observe these through the configured
 * {@link WhatsAppLinkedClientErrorHandler} rather than around individual
 * calls; pattern-match the concrete subtype to react to a specific failure mode.
 *
 * @see CloudApiException
 * @see CloudAuthException
 * @see CloudWebhookException
 * @see CloudUnsupportedVersionException
 */
public sealed abstract class WhatsAppCloudException extends WhatsAppException
        permits WhatsAppCloudException.CloudApiException,
                WhatsAppCloudException.CloudAuthException,
                WhatsAppCloudException.CloudUnsupportedVersionException,
                WhatsAppCloudException.CloudWebhookException {

    /**
     * Constructs a new Cloud exception with the specified detail message.
     *
     * @param message the detail message describing the Cloud failure
     */
    protected WhatsAppCloudException(String message) {
        super(message);
    }

    /**
     * Constructs a new Cloud exception with the specified detail message and cause.
     *
     * @param message the detail message describing the Cloud failure
     * @param cause   the underlying cause of this exception
     */
    protected WhatsAppCloudException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * {@inheritDoc}
     *
     * @implNote
     * This implementation returns {@link WhatsAppLinkedClientErrorResult#DISCARD}: a single Cloud failure
     * leaves the access token and the rest of the Cloud session usable. {@link CloudAuthException}
     * overrides this because a credential failure makes the whole session unusable.
     */
    @Override
    public WhatsAppLinkedClientErrorResult toErrorResult() {
        return WhatsAppLinkedClientErrorResult.DISCARD;
    }

    /**
     * Thrown when the graph endpoint rejected a Cloud API request with a structured error envelope.
     *
     * <p>Carries the HTTP status and the {@code error} object Meta returns ({@code code},
     * {@code error_subcode}, {@code message}, and the {@code fbtrace_id} support correlation token),
     * so callers can classify the rejection (a rate limit, an invalid parameter, a re-engagement
     * window violation) and log the trace id when escalating to Meta.
     */
    public static final class CloudApiException extends WhatsAppCloudException {
        /**
         * The HTTP status code returned by the graph endpoint, or {@code null} when the failure
         * occurred before a response was received.
         */
        private final Integer httpStatus;

        /**
         * The Meta {@code error.code} value, or {@code null} when the response carried none.
         */
        private final Integer code;

        /**
         * The Meta {@code error.error_subcode} value, or {@code null} when the response carried
         * none.
         */
        private final Integer subcode;

        /**
         * The Meta {@code error.fbtrace_id} support correlation token, or {@code null} when absent.
         */
        private final String fbtraceId;

        /**
         * Constructs a new Cloud API exception with no transport-level detail.
         *
         * <p>Used when the failure occurred before a structured graph response was available, for
         * example a transport I/O error or an interrupted send, so the HTTP status, error code,
         * subcode, and trace id are all absent.
         *
         * @param message the detail message describing the failure
         */
        public CloudApiException(String message) {
            super(message);
            this.httpStatus = null;
            this.code = null;
            this.subcode = null;
            this.fbtraceId = null;
        }

        /**
         * Constructs a new Cloud API exception.
         *
         * @param httpStatus the HTTP status returned by the graph endpoint
         * @param code       the Meta {@code error.code}, or {@code 0} when absent
         * @param subcode    the Meta {@code error.error_subcode}, or {@code 0} when absent
         * @param message    the Meta {@code error.message}, or a synthesised description
         * @param fbtraceId  the Meta {@code error.fbtrace_id}, or {@code null} when absent
         */
        public CloudApiException(int httpStatus, int code, int subcode, String message, String fbtraceId) {
            super("Cloud API request failed: status=" + httpStatus + ", code=" + code
                    + ", subcode=" + subcode + ", fbtrace_id=" + fbtraceId + ", message=" + message);
            this.httpStatus = httpStatus;
            this.code = code == 0 ? null : code;
            this.subcode = subcode == 0 ? null : subcode;
            this.fbtraceId = fbtraceId;
        }

        /**
         * Returns the HTTP status returned by the graph endpoint.
         *
         * @return an {@link OptionalInt} carrying the HTTP status code, or empty when the failure
         *         occurred before a response was received
         */
        public OptionalInt httpStatus() {
            return httpStatus == null ? OptionalInt.empty() : OptionalInt.of(httpStatus);
        }

        /**
         * Returns the Meta {@code error.code} value.
         *
         * @return an {@link OptionalInt} carrying the error code, or empty when the response
         *         carried none
         */
        public OptionalInt code() {
            return code == null ? OptionalInt.empty() : OptionalInt.of(code);
        }

        /**
         * Returns the Meta {@code error.error_subcode} value.
         *
         * @return an {@link OptionalInt} carrying the error subcode, or empty when the response
         *         carried none
         */
        public OptionalInt subcode() {
            return subcode == null ? OptionalInt.empty() : OptionalInt.of(subcode);
        }

        /**
         * Returns the Meta {@code error.fbtrace_id} support correlation token.
         *
         * @return an {@link Optional} carrying the trace id, or empty when the response carried
         *         none
         */
        public Optional<String> fbtraceId() {
            return Optional.ofNullable(fbtraceId);
        }
    }

    /**
     * Thrown when the configured access token is missing, malformed, invalid, or expired.
     *
     * <p>Unlike a {@link CloudApiException}, which scopes to one request, an authentication failure
     * means no request can succeed until the integrator supplies fresh credentials, so the whole
     * Cloud session is unusable.
     */
    public static final class CloudAuthException extends WhatsAppCloudException {
        /**
         * Constructs a new Cloud authentication exception with the specified detail message.
         *
         * @param message the detail message describing the authentication failure
         */
        public CloudAuthException(String message) {
            super(message);
        }

        /**
         * Constructs a new Cloud authentication exception with the specified detail message and
         * cause.
         *
         * @param message the detail message describing the authentication failure
         * @param cause   the underlying cause of this exception
         */
        public CloudAuthException(String message, Throwable cause) {
            super(message, cause);
        }

        /**
         * {@inheritDoc}
         *
         * @implNote
         * This implementation returns {@link WhatsAppLinkedClientErrorResult#DISCONNECT}: with no valid
         * access token the Cloud client cannot send or receive anything, so the session is unusable
         * until it is re-credentialed.
         */
        @Override
        public WhatsAppLinkedClientErrorResult toErrorResult() {
            return WhatsAppLinkedClientErrorResult.DISCONNECT;
        }
    }

    /**
     * Thrown when an inbound webhook delivery fails verification or cannot be parsed.
     *
     * <p>Raised when the {@code X-Hub-Signature-256} header does not match the HMAC computed over the
     * raw request body, or when the {@code object=whatsapp_business_account} envelope is malformed.
     * The failure is scoped to a single delivery and does not affect the rest of the session.
     */
    public static final class CloudWebhookException extends WhatsAppCloudException {
        /**
         * Constructs a new Cloud webhook exception with the specified detail message.
         *
         * @param message the detail message describing the webhook failure
         */
        public CloudWebhookException(String message) {
            super(message);
        }

        /**
         * Constructs a new Cloud webhook exception with the specified detail message and cause.
         *
         * @param message the detail message describing the webhook failure
         * @param cause   the underlying cause of this exception
         */
        public CloudWebhookException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Thrown when an operation requires a newer Cloud API version than the one the client is configured
     * to target.
     *
     * <p>Some Cloud API operations were introduced in a specific Graph API version and cannot be served
     * by an older versioned graph base. When the client targets a version older than an operation's
     * minimum, the call is rejected locally with this exception before any request is sent, carrying the
     * rejected operation's name together with the required and configured versions. The failure is scoped
     * to the single call; every operation available at the configured version stays usable.
     */
    public static final class CloudUnsupportedVersionException extends WhatsAppCloudException {
        /**
         * The name of the operation that was rejected.
         */
        private final String operation;

        /**
         * The minimum version the operation requires.
         */
        private final CloudApiVersion requiredVersion;

        /**
         * The version the client is configured to target.
         */
        private final CloudApiVersion configuredVersion;

        /**
         * Constructs a new unsupported-version exception.
         *
         * @param operation         the name of the rejected operation
         * @param requiredVersion   the minimum version the operation requires
         * @param configuredVersion the version the client is configured to target
         */
        public CloudUnsupportedVersionException(String operation, CloudApiVersion requiredVersion,
                                                CloudApiVersion configuredVersion) {
            super("operation '" + operation + "' requires Cloud API version " + requiredVersion.version()
                    + " but the client is configured for " + configuredVersion.version());
            this.operation = operation;
            this.requiredVersion = requiredVersion;
            this.configuredVersion = configuredVersion;
        }

        /**
         * Returns the name of the operation that was rejected.
         *
         * @return the operation name
         */
        public String operation() {
            return operation;
        }

        /**
         * Returns the minimum version the operation requires.
         *
         * @return the required version
         */
        public CloudApiVersion requiredVersion() {
            return requiredVersion;
        }

        /**
         * Returns the version the client is configured to target.
         *
         * @return the configured version
         */
        public CloudApiVersion configuredVersion() {
            return configuredVersion;
        }
    }
}
