package com.github.auties00.cobalt.exception;

/**
 * Exception thrown when a reconnection attempt to the WhatsApp server fails.
 * <p>
 * This exception is thrown when the client attempts to re-establish a connection after
 * a previous disconnection and fails. It differs from {@link WhatsAppConnectionException}
 * in that it represents a retry scenario where the client had previously connected successfully.
 *
 * <h2>Reconnection Architecture</h2>
 * WhatsApp uses a sophisticated reconnection strategy with:
 * <ul>
 *   <li><b>Fibonacci backoff:</b> Delays between attempts follow a Fibonacci-like sequence</li>
 *   <li><b>Jitter:</b> Random variation to prevent thundering herd problems</li>
 *   <li><b>Maximum attempts:</b> Configurable limit on reconnection attempts</li>
 *   <li><b>Session resumption:</b> Attempt to resume the previous session when possible</li>
 * </ul>
 *
 * <h2>Possible Causes</h2>
 * <ul>
 *   <li><b>Temporary network instability:</b> Brief network outages or WiFi switching</li>
 *   <li><b>Server maintenance:</b> WhatsApp servers performing rolling updates</li>
 *   <li><b>Load balancing:</b> Server redirects causing connection churn</li>
 *   <li><b>Rate limiting:</b> Too many connection attempts in a short period</li>
 *   <li><b>Session invalidation:</b> The previous session is no longer valid</li>
 * </ul>
 *
 * <h2>Attempt Tracking</h2>
 * The exception tracks the number of reconnection attempts made, enabling:
 * <ul>
 *   <li>Exponential/Fibonacci backoff calculations</li>
 *   <li>Maximum retry limit enforcement</li>
 *   <li>User notification after multiple failures</li>
 *   <li>Diagnostics and logging</li>
 * </ul>
 *
 * <h2>Recovery</h2>
 * When handling this exception:
 * <ol>
 *   <li>Check if maximum retry attempts have been exceeded</li>
 *   <li>Calculate appropriate backoff delay using the attempt count</li>
 *   <li>Wait for the delay period</li>
 *   <li>Attempt reconnection again</li>
 *   <li>If maximum attempts exceeded, consider notifying the user</li>
 * </ol>
 *
 * @see WhatsAppConnectionException for initial connection failures
 */
public final class WhatsAppReconnectionException extends WhatsAppException {

    /**
     * The number of reconnection attempts made before this failure.
     */
    private final int attempts;

    /**
     * Constructs a new reconnection exception with the specified message and attempt count.
     *
     * @param message  a descriptive message explaining the reconnection failure
     * @param attempts the number of reconnection attempts made before this failure;
     *                 must be non-negative
     */
    public WhatsAppReconnectionException(String message, int attempts) {
        super(message);
        this.attempts = attempts;
    }

    /**
     * Constructs a new reconnection exception with a message, attempt count, and cause.
     *
     * @param message  a descriptive message explaining the reconnection failure
     * @param attempts the number of reconnection attempts made before this failure;
     *                 must be non-negative
     * @param cause    the underlying cause of the reconnection failure
     */
    public WhatsAppReconnectionException(String message, int attempts, Throwable cause) {
        super(message, cause);
        this.attempts = attempts;
    }

    /**
     * Returns the number of reconnection attempts made before this failure.
     * <p>
     * This count is useful for:
     * <ul>
     *   <li>Determining if the maximum retry limit has been reached</li>
     *   <li>Calculating the appropriate backoff delay for the next attempt</li>
     *   <li>Logging reconnection statistics for debugging and monitoring</li>
     *   <li>Deciding when to notify the user about connectivity issues</li>
     * </ul>
     *
     * <h2>Backoff Calculation Example</h2>
     * <pre>{@code
     * // Fibonacci-based backoff with jitter
     * int fib = fibonacci(exception.attempts());
     * int delay = fib * 1000 + random.nextInt(500);
     * }</pre>
     *
     * @return the number of reconnection attempts; always non-negative
     */
    public int attempts() {
        return attempts;
    }

    /**
     * Returns whether this exception represents a fatal error.
     * <p>
     * Reconnection exceptions are always fatal as they indicate the current
     * reconnection attempt has failed. However, the client may choose to
     * retry again with appropriate backoff.
     *
     * @return {@code true}
     */
    @Override
    public boolean isFatal() {
        return true;
    }
}
