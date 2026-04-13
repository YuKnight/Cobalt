package com.github.auties00.cobalt.exception;

import java.util.Optional;

/**
 * Exception thrown when mobile phone number registration with the WhatsApp API fails.
 * <p>
 * This exception represents failures during the mobile registration process, which is the
 * method used by WhatsApp mobile apps to register and verify phone numbers. Registration
 * involves multiple API calls to WhatsApp's registration servers.
 *
 * <h2>Registration Flow</h2>
 * Mobile registration typically follows these steps:
 * <ol>
 *   <li><b>Request code:</b> Send phone number to receive verification code via SMS or call</li>
 *   <li><b>Receive code:</b> User receives 6-digit verification code</li>
 *   <li><b>Register code:</b> Submit the verification code to complete registration</li>
 *   <li><b>Receive credentials:</b> Server returns authentication credentials on success</li>
 * </ol>
 *
 * <h2>Possible Causes</h2>
 * <ul>
 *   <li><b>Invalid phone number:</b> The phone number format is incorrect or not supported</li>
 *   <li><b>Rate limiting:</b> Too many registration attempts in a short period</li>
 *   <li><b>Anti-spam blocking:</b> WhatsApp's systems detected suspicious activity</li>
 *   <li><b>Invalid code:</b> The verification code entered was incorrect or expired</li>
 *   <li><b>Network errors:</b> Communication with registration servers failed</li>
 *   <li><b>Unsupported configuration:</b> The device or platform is not supported</li>
 *   <li><b>Account banned:</b> The phone number has been permanently banned</li>
 * </ul>
 *
 * <h2>API Response</h2>
 * When available, the raw JSON response from the registration API is captured and can
 * be retrieved via {@link #erroneousResponse()}. This response often contains:
 * <ul>
 *   <li>Error codes indicating the specific failure reason</li>
 *   <li>Retry-after timestamps for rate limiting</li>
 *   <li>Human-readable error messages</li>
 *   <li>Additional metadata for debugging</li>
 * </ul>
 *
 * <h2>Fatality</h2>
 * Registration exceptions are fatal as they prevent the client from completing
 * authentication. The user must address the underlying issue before retrying.
 */
public final class WhatsAppRegistrationException extends WhatsAppException {

    /**
     * The raw API response that caused this exception, if available.
     * May be null if the exception occurred before or without server communication.
     */
    private final String erroneousResponse;

    /**
     * Constructs a new registration exception with a message and API response.
     * <p>
     * This constructor should be used when a registration failure occurs and the WhatsApp API
     * returns an error response that may contain additional diagnostic information.
     *
     * @param message           a descriptive error message explaining the registration failure
     * @param erroneousResponse the raw response from the WhatsApp registration API (typically JSON);
     *                          may be null if no response is available
     */
    public WhatsAppRegistrationException(String message, String erroneousResponse) {
        super(message);
        this.erroneousResponse = erroneousResponse;
    }

    /**
     * Constructs a new registration exception with a descriptive message.
     * <p>
     * This constructor should be used for registration failures that occur before or without
     * communication with the WhatsApp API, such as validation errors or unsupported configurations.
     *
     * @param message a descriptive error message explaining the registration failure
     */
    public WhatsAppRegistrationException(String message) {
        super(message);
        this.erroneousResponse = null;
    }

    /**
     * Constructs a new registration exception that wraps an underlying cause.
     * <p>
     * This constructor should be used when a registration failure is caused by an underlying
     * exception, such as network errors, I/O failures, or interrupted operations.
     *
     * @param cause the underlying exception that caused the registration to fail
     */
    public WhatsAppRegistrationException(Throwable cause) {
        super(cause);
        this.erroneousResponse = null;
    }

    /**
     * Returns the raw API response that caused this exception, if available.
     * <p>
     * The response, when present, typically contains a JSON-formatted error message from the
     * WhatsApp registration API with details such as:
     * <ul>
     *   <li><b>status:</b> Error status code (e.g., "fail", "error")</li>
     *   <li><b>reason:</b> Machine-readable reason code (e.g., "too_recent", "blocked")</li>
     *   <li><b>retry_after:</b> Timestamp indicating when to retry (for rate limiting)</li>
     *   <li><b>param:</b> Additional parameters related to the error</li>
     * </ul>
     *
     * <h2>Example Response</h2>
     * <pre>{@code
     * {
     *   "status": "fail",
     *   "reason": "too_recent",
     *   "retry_after": 1234567890
     * }
     * }</pre>
     *
     * @return an {@link Optional} containing the erroneous API response,
     *         or empty if no response is available
     */
    public Optional<String> erroneousResponse() {
        return Optional.ofNullable(erroneousResponse);
    }

    /**
     * Returns whether this exception represents a fatal error.
     * <p>
     * Registration exceptions are always fatal as they prevent the client from
     * completing the authentication process.
     *
     * @return {@code true}
     */
    @Override
    public boolean isFatal() {
        return true;
    }
}
