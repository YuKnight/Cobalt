package com.github.auties00.cobalt.exception;

/**
 * Base exception for media-related errors in the WhatsApp protocol.
 * <p>
 * This sealed class hierarchy represents all failures that can occur during media operations
 * including uploading, downloading, processing, and managing media connections. Media operations
 * in WhatsApp involve separate infrastructure from the main messaging servers, using dedicated
 * media endpoints for file transfer.
 *
 * <h2>Media Architecture</h2>
 * WhatsApp media operations follow this flow:
 * <ol>
 *   <li><b>Connection:</b> Establish connection to media servers using dynamic endpoints</li>
 *   <li><b>Upload:</b> Encrypt media locally, upload to server, receive download URL</li>
 *   <li><b>Download:</b> Fetch encrypted media from URL, decrypt using media keys</li>
 *   <li><b>Processing:</b> Generate thumbnails, extract metadata, validate formats</li>
 * </ol>
 *
 * <h2>Exception Hierarchy</h2>
 * <ul>
 *   <li>{@link Connection} - Media server connection failures</li>
 *   <li>{@link Upload} - Media upload failures</li>
 *   <li>{@link Download} - Media download failures</li>
 *   <li>{@link Processing} - Media processing failures (encryption, decryption, thumbnails)</li>
 * </ul>
 *
 * <h2>Error Recovery</h2>
 * Media exceptions are non-fatal, meaning the client connection remains active. Individual
 * media operations can be retried without affecting the session. Common recovery strategies:
 * <ul>
 *   <li>Retry with exponential backoff for transient network errors</li>
 *   <li>Request new media connection for connection failures</li>
 *   <li>Re-request media URL for expired download links</li>
 * </ul>
 *
 * @see Connection
 * @see Upload
 * @see Download
 * @see Processing
 */
public sealed class WhatsAppMediaException extends WhatsAppException
        permits WhatsAppMediaException.Download,
                WhatsAppMediaException.Processing,
                WhatsAppMediaException.Upload,
                WhatsAppMediaException.Connection {

    /**
     * Constructs a new media exception with the specified detail message.
     *
     * @param message the detail message describing the media error
     */
    public WhatsAppMediaException(String message) {
        super(message);
    }

    /**
     * Constructs a new media exception with the specified detail message and cause.
     *
     * @param message the detail message describing the media error
     * @param cause   the underlying cause of this exception
     */
    public WhatsAppMediaException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new media exception wrapping the specified cause.
     *
     * @param cause the underlying cause of this exception
     */
    public WhatsAppMediaException(Throwable cause) {
        super(cause);
    }

    /**
     * Returns whether this exception represents a fatal error.
     * <p>
     * Media exceptions are never fatal as they affect individual media operations
     * but do not compromise the overall session or connection.
     *
     * @return {@code false} for all media exceptions
     */
    @Override
    public boolean isFatal() {
        return false;
    }

    /**
     * Exception thrown when establishing or maintaining media server connections fails.
     * <p>
     * WhatsApp uses dedicated media servers separate from the main WebSocket connection.
     * Media connections are established dynamically and have a limited lifetime. This
     * exception occurs when:
     * <ul>
     *   <li>Initial connection to media servers fails</li>
     *   <li>Media connection expires during an operation</li>
     *   <li>Authentication with media servers fails</li>
     *   <li>No media hosts are available in the response</li>
     *   <li>TLS/SSL handshake with media server fails</li>
     * </ul>
     *
     * <h2>Recovery</h2>
     * When this exception occurs, the client should:
     * <ol>
     *   <li>Request a new media connection from the server</li>
     *   <li>Wait for the new connection parameters</li>
     *   <li>Retry the media operation with the new connection</li>
     * </ol>
     */
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
     * Exception thrown when media download fails.
     * <p>
     * Media downloads involve fetching encrypted content from WhatsApp's CDN and
     * decrypting it using the media keys provided in the message. This exception
     * occurs when:
     * <ul>
     *   <li>The media URL has expired (typically after 30 days)</li>
     *   <li>Network errors prevent fetching the content</li>
     *   <li>The media server returns an error response</li>
     *   <li>The downloaded content fails integrity validation</li>
     *   <li>Decryption fails due to invalid or missing media keys</li>
     *   <li>The media file has been deleted from servers</li>
     * </ul>
     *
     * <h2>Media Key Structure</h2>
     * Each media file is encrypted with a unique key derived from:
     * <ul>
     *   <li>File encryption key (32 bytes)</li>
     *   <li>File HMAC key (32 bytes)</li>
     *   <li>Initialization vector (16 bytes)</li>
     * </ul>
     *
     * <h2>Recovery</h2>
     * <ul>
     *   <li>For expired URLs: Request a re-upload from the sender</li>
     *   <li>For transient errors: Retry with exponential backoff</li>
     *   <li>For deleted media: Inform user that media is no longer available</li>
     * </ul>
     */
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
         * Constructs a new media download exception with the specified message and cause.
         *
         * @param message the detail message describing the download failure
         * @param cause   the underlying cause of the download failure
         */
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
     * Exception thrown when media upload fails.
     * <p>
     * Media uploads involve encrypting content locally, uploading to WhatsApp's media
     * servers, and receiving a download URL to include in the message. This exception
     * occurs when:
     * <ul>
     *   <li>Media connection is not available or expired</li>
     *   <li>Network errors during upload</li>
     *   <li>Server rejects the upload (size limits, format restrictions)</li>
     *   <li>Authentication token has expired</li>
     *   <li>Upload resumption fails</li>
     *   <li>Server returns unexpected response format</li>
     * </ul>
     *
     * <h2>Upload Process</h2>
     * <ol>
     *   <li>Generate random media keys</li>
     *   <li>Encrypt media content with AES-CBC</li>
     *   <li>Compute file hash for integrity</li>
     *   <li>Upload encrypted content to media server</li>
     *   <li>Receive direct path (download URL) from server</li>
     * </ol>
     *
     * <h2>Recovery</h2>
     * <ul>
     *   <li>For connection errors: Request new media connection and retry</li>
     *   <li>For size limits: Compress media or inform user of limits</li>
     *   <li>For transient errors: Retry with exponential backoff</li>
     * </ul>
     */
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
     * Exception thrown when media processing fails.
     * <p>
     * Media processing encompasses all operations on media content beyond transfer,
     * including encryption, decryption, thumbnail generation, format conversion,
     * and metadata extraction. This exception occurs when:
     * <ul>
     *   <li>Encryption/decryption operations fail</li>
     *   <li>Thumbnail generation fails</li>
     *   <li>Media format is unsupported or invalid</li>
     *   <li>Metadata extraction fails</li>
     *   <li>Content validation fails (corrupted data)</li>
     *   <li>Required codecs are not available</li>
     * </ul>
     *
     * <h2>Processing Operations</h2>
     * <ul>
     *   <li><b>Images:</b> JPEG compression, thumbnail generation, EXIF extraction</li>
     *   <li><b>Videos:</b> H.264 encoding, thumbnail extraction, duration detection</li>
     *   <li><b>Audio:</b> Opus/AAC encoding, waveform generation, duration detection</li>
     *   <li><b>Documents:</b> Preview generation, metadata extraction</li>
     *   <li><b>Stickers:</b> WebP validation, animation detection</li>
     * </ul>
     *
     * <h2>Recovery</h2>
     * <ul>
     *   <li>For format errors: Convert to supported format before retry</li>
     *   <li>For corruption: Request re-send of original content</li>
     *   <li>For codec errors: Use fallback processing or skip optional features</li>
     * </ul>
     */
    public static final class Processing extends WhatsAppMediaException {
        /**
         * Constructs a new media processing exception with the specified message.
         *
         * @param message the detail message describing the processing failure
         */
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
