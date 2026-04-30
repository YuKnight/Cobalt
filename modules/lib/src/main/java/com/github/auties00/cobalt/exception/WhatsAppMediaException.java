package com.github.auties00.cobalt.exception;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;

import java.util.OptionalInt;

/**
 * Thrown for failures during media operations (uploads, downloads,
 * media-server connection bring-up, and the local processing that
 * happens around them).
 *
 * <p>WhatsApp media (images, videos, audio messages, documents,
 * stickers) travels over a separate set of CDN endpoints from the main
 * messaging WebSocket. The flow is: ask the server for a fresh media
 * connection, encrypt and upload the bytes (or fetch and decrypt them
 * during a download), then run any local processing (thumbnailing,
 * format conversion, integrity checks). Each of those steps can fail
 * independently, and each failure is surfaced through one of the four
 * nested subtypes.
 *
 * <p>Media exceptions are never fatal: a failed transfer is local to a
 * single piece of content and the messaging session keeps running.
 * When the failure originated from an HTTP response, the originating
 * status code is preserved on {@link #httpStatusCode()} so the
 * configurable error handler can react differently to a 401 (auth
 * refresh needed) versus a 413 (payload too large) versus a 507
 * (server throttling).
 *
 * @see Connection
 * @see Upload
 * @see Download
 * @see Processing
 */
@WhatsAppWebModule(moduleName = "WAWebMmsClientErrors")
@WhatsAppWebModule(moduleName = "WAWebHttpErrors")
public sealed class WhatsAppMediaException extends WhatsAppException
        permits WhatsAppMediaException.Download,
                WhatsAppMediaException.Processing,
                WhatsAppMediaException.Upload,
                WhatsAppMediaException.Connection {

    /**
     * HTTP {@code 401} returned by the media CDN when the upload or
     * download authentication token has expired.
     */
    @WhatsAppWebExport(moduleName = "WAWebMmsClientErrors", exports = "MMSUnauthorizedError",
                       adaptation = WhatsAppAdaptation.ADAPTED)
    public static final int HTTP_UNAUTHORIZED = 401;

    /**
     * HTTP {@code 403} returned by the media CDN when access to the
     * resource is denied. When the response body indicates an expired
     * URL signature, the server-side classification reclassifies this
     * as a not-found error instead.
     */
    @WhatsAppWebExport(moduleName = "WAWebMmsClientErrors", exports = "MMSForbiddenError",
                       adaptation = WhatsAppAdaptation.ADAPTED)
    public static final int HTTP_FORBIDDEN = 403;

    /**
     * HTTP {@code 404} returned by the media CDN when the file is not
     * available, either because it has been deleted or because the
     * download URL has expired. HTTP {@code 410 Gone} is treated
     * identically.
     */
    @WhatsAppWebExport(moduleName = "WAWebMmsClientErrors", exports = "MediaNotFoundError",
                       adaptation = WhatsAppAdaptation.ADAPTED)
    public static final int HTTP_NOT_FOUND = 404;

    /**
     * HTTP {@code 413} returned by the media CDN when the uploaded
     * payload exceeds the server-side size limit.
     */
    @WhatsAppWebExport(moduleName = "WAWebMmsClientErrors", exports = "MediaTooLargeError",
                       adaptation = WhatsAppAdaptation.ADAPTED)
    public static final int HTTP_TOO_LARGE = 413;

    /**
     * HTTP {@code 415} returned by the media CDN when the media format
     * is invalid or the ciphertext hash does not match the value the
     * client computed.
     */
    @WhatsAppWebExport(moduleName = "WAWebMmsClientErrors", exports = "MediaInvalidError",
                       adaptation = WhatsAppAdaptation.ADAPTED)
    public static final int HTTP_INVALID_MEDIA = 415;

    /**
     * HTTP {@code 507} returned by the media CDN when the server is
     * throttling traffic from this client. WhatsApp explicitly does
     * not retry on this code.
     */
    @WhatsAppWebExport(moduleName = "WAWebMmsClientErrors", exports = "MMSThrottleError",
                       adaptation = WhatsAppAdaptation.ADAPTED)
    public static final int HTTP_THROTTLE = 507;

    /**
     * The HTTP status code returned by the CDN, or {@code -1} when the
     * failure did not originate from an HTTP response.
     */
    private final int httpStatusCode;

    /**
     * Constructs a new media exception with the specified detail message.
     *
     * @param message the detail message describing the media error
     */
    public WhatsAppMediaException(String message) {
        super(message);
        this.httpStatusCode = -1;
    }

    /**
     * Constructs a new media exception with the specified HTTP status code and detail message.
     *
     * @param httpStatusCode the HTTP status code returned by the media CDN
     * @param message        the detail message describing the media error
     */
    @WhatsAppWebExport(moduleName = "WAWebHttpErrors", exports = "HttpStatusCodeError",
                       adaptation = WhatsAppAdaptation.ADAPTED)
    public WhatsAppMediaException(int httpStatusCode, String message) {
        super(message);
        this.httpStatusCode = httpStatusCode;
    }

    /**
     * Constructs a new media exception with the specified detail message and cause.
     *
     * @param message the detail message describing the media error
     * @param cause   the underlying cause of this exception
     */
    public WhatsAppMediaException(String message, Throwable cause) {
        super(message, cause);
        this.httpStatusCode = -1;
    }

    /**
     * Constructs a new media exception wrapping the specified cause.
     *
     * @param cause the underlying cause of this exception
     */
    public WhatsAppMediaException(Throwable cause) {
        super(cause);
        this.httpStatusCode = -1;
    }

    /**
     * Returns whether the failure invalidates the current session.
     *
     * <p>Media operations are isolated from the main messaging
     * channel; their failures never tear the session down.
     *
     * @return {@code false}
     */
    @Override
    public boolean isFatal() {
        return false;
    }

    /**
     * Returns the HTTP status code returned by the media CDN, when
     * the failure originated from an HTTP response.
     *
     * <p>Callers can match the returned value against the
     * {@code HTTP_*} constants on this class to decide how to react
     * (refresh auth on 401, re-request a URL on 404, give up on 413,
     * back off on 507, and so on).
     *
     * @return the HTTP status code wrapped in an {@link OptionalInt},
     *         or empty when the failure was not produced by an HTTP
     *         response
     */
    public OptionalInt httpStatusCode() {
        return httpStatusCode == -1 ? OptionalInt.empty() : OptionalInt.of(httpStatusCode);
    }

    /**
     * Thrown when the media-server connection cannot be established
     * or is no longer usable.
     *
     * <p>WhatsApp serves media through endpoints that are negotiated
     * dynamically and have a limited lifetime. This exception covers
     * the case where the negotiation failed, the connection expired
     * during a transfer, the server returned no usable hosts, or the
     * TLS handshake to the chosen host failed.
     */
    @WhatsAppWebModule(moduleName = "WAWebMediaHostsErrors")
    public static final class Connection extends WhatsAppMediaException {
        /**
         * Constructs a new media connection exception with a default message.
         */
        public Connection() {
            super("Media connection failed");
        }

        /**
         * Constructs a new media connection exception with the specified message.
         *
         * @param message the detail message describing the connection failure
         */
        public Connection(String message) {
            super(message);
        }

        /**
         * Constructs a new media connection exception with the specified message and cause.
         *
         * @param message the detail message describing the connection failure
         * @param cause   the underlying cause of the connection failure
         */
        public Connection(String message, Throwable cause) {
            super(message, cause);
        }

        /**
         * Constructs a new media connection exception wrapping the specified cause.
         *
         * @param cause the underlying cause of the connection failure
         */
        public Connection(Throwable cause) {
            super(cause);
        }
    }

    /**
     * Thrown when fetching a media file from the WhatsApp CDN fails.
     *
     * <p>The transfer can fail because the URL has expired, the
     * network call could not complete, the server returned an error
     * status (commonly {@link #HTTP_NOT_FOUND} for deleted files,
     * {@link #HTTP_UNAUTHORIZED} for an expired auth token, or
     * {@link #HTTP_THROTTLE} when the server is rate-limiting), or
     * because the bytes that did arrive failed integrity validation
     * during decryption.
     */
    @WhatsAppWebModule(moduleName = "WAWebMmsClientErrors")
    @WhatsAppWebModule(moduleName = "WAWebMmsClientMmsDownload")
    public static final class Download extends WhatsAppMediaException {
        /**
         * Constructs a new media download exception with the specified message.
         *
         * @param message the detail message describing the download failure
         */
        public Download(String message) {
            super(message);
        }

        /**
         * Constructs a new media download exception with the specified HTTP status code and message.
         *
         * @param httpStatusCode the HTTP status code returned by the CDN
         * @param message        the detail message describing the download failure
         */
        public Download(int httpStatusCode, String message) {
            super(httpStatusCode, message);
        }

        /**
         * Constructs a new media download exception with the specified message and cause.
         *
         * @param message the detail message describing the download failure
         * @param cause   the underlying cause of the download failure
         */
        @WhatsAppWebExport(moduleName = "WAWebHttpErrors", exports = "HttpNetworkError",
                           adaptation = WhatsAppAdaptation.ADAPTED)
        @WhatsAppWebExport(moduleName = "WAWebHttpErrors", exports = "HttpTimedOutError",
                           adaptation = WhatsAppAdaptation.ADAPTED)
        public Download(String message, Throwable cause) {
            super(message, cause);
        }

        /**
         * Constructs a new media download exception wrapping the specified cause.
         *
         * @param cause the underlying cause of the download failure
         */
        public Download(Throwable cause) {
            super(cause);
        }
    }

    /**
     * Thrown when uploading a media file to the WhatsApp CDN fails.
     *
     * <p>The upload can fail because the media connection is no
     * longer valid, the network call could not complete, or the
     * server rejected the upload. Common server-side rejections are
     * {@link #HTTP_TOO_LARGE} for oversize content,
     * {@link #HTTP_INVALID_MEDIA} for a hash or format mismatch,
     * {@link #HTTP_UNAUTHORIZED} for an expired auth token, and
     * {@link #HTTP_THROTTLE} for rate limiting.
     */
    @WhatsAppWebModule(moduleName = "WAWebMmsClientErrors")
    public static final class Upload extends WhatsAppMediaException {
        /**
         * Constructs a new media upload exception with the specified message.
         *
         * @param message the detail message describing the upload failure
         */
        public Upload(String message) {
            super(message);
        }

        /**
         * Constructs a new media upload exception with the specified HTTP status code and message.
         *
         * @param httpStatusCode the HTTP status code returned by the CDN
         * @param message        the detail message describing the upload failure
         */
        public Upload(int httpStatusCode, String message) {
            super(httpStatusCode, message);
        }

        /**
         * Constructs a new media upload exception with the specified message and cause.
         *
         * @param message the detail message describing the upload failure
         * @param cause   the underlying cause of the upload failure
         */
        public Upload(String message, Throwable cause) {
            super(message, cause);
        }

        /**
         * Constructs a new media upload exception wrapping the specified cause.
         *
         * @param cause the underlying cause of the upload failure
         */
        public Upload(Throwable cause) {
            super(cause);
        }
    }

    /**
     * Thrown when local processing of a media file fails.
     *
     * <p>Processing covers everything that happens around the
     * transfer itself: encryption and decryption, thumbnail and
     * waveform generation, format conversion, metadata extraction,
     * and integrity checks on responses that parsed but contain
     * unusable content.
     */
    @WhatsAppWebModule(moduleName = "WAWebHttpErrors")
    public static final class Processing extends WhatsAppMediaException {
        /**
         * Constructs a new media processing exception with the specified message.
         *
         * @param message the detail message describing the processing failure
         */
        @WhatsAppWebExport(moduleName = "WAWebHttpErrors", exports = "HttpInvalidResponseError",
                           adaptation = WhatsAppAdaptation.ADAPTED)
        @WhatsAppWebExport(moduleName = "WAWebHttpErrors", exports = "MmsDownloadFilehashMismatchError",
                           adaptation = WhatsAppAdaptation.ADAPTED)
        public Processing(String message) {
            super(message);
        }

        /**
         * Constructs a new media processing exception with the specified message and cause.
         *
         * @param message the detail message describing the processing failure
         * @param cause   the underlying cause of the processing failure
         */
        public Processing(String message, Throwable cause) {
            super(message, cause);
        }

        /**
         * Constructs a new media processing exception wrapping the specified cause.
         *
         * @param cause the underlying cause of the processing failure
         */
        public Processing(Throwable cause) {
            super(cause);
        }
    }
}
