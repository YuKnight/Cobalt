package com.github.auties00.cobalt.exception;

import com.github.auties00.cobalt.node.Node;

import java.util.Objects;

/**
 * Exception thrown when an error occurs in the WhatsApp protocol stream.
 * <p>
 * This sealed class hierarchy represents errors at the protocol stream level, which sits
 * above the session layer but below application-level message handling. Stream errors
 * typically indicate problems with the XMPP-like node (stanza) structure or protocol
 * timing issues.
 *
 * <h2>Stream Architecture</h2>
 * The WhatsApp stream layer handles:
 * <ul>
 *   <li>Binary node encoding and decoding</li>
 *   <li>Request/response correlation via message IDs</li>
 *   <li>Timeout handling for pending requests</li>
 *   <li>Node structure validation</li>
 * </ul>
 *
 * <h2>Exception Hierarchy</h2>
 * <ul>
 *   <li>{@link MalformedNode} - Node structure is invalid or malformed</li>
 *   <li>{@link NodeTimeout} - Node request did not receive a response in time</li>
 * </ul>
 *
 * <h2>Fatality</h2>
 * All stream exceptions are fatal because they indicate fundamental protocol issues
 * that cannot be recovered from without re-establishing the connection.
 *
 * @see MalformedNode
 * @see NodeTimeout
 */
public sealed class WhatsAppStreamException extends WhatsAppException
        permits WhatsAppStreamException.MalformedNode, WhatsAppStreamException.NodeTimeout {

    /**
     * Constructs a new stream exception with no detail message.
     */
    public WhatsAppStreamException() {
        super();
    }

    /**
     * Constructs a new stream exception with the specified detail message.
     *
     * @param message the detail message describing the stream error
     */
    public WhatsAppStreamException(String message) {
        super(message);
    }

    /**
     * Constructs a new stream exception with the specified detail message and cause.
     *
     * @param message the detail message describing the stream error
     * @param cause   the underlying cause of this exception
     */
    public WhatsAppStreamException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new stream exception wrapping the specified cause.
     *
     * @param cause the underlying cause of this exception
     */
    public WhatsAppStreamException(Throwable cause) {
        super(cause);
    }

    /**
     * Returns whether this exception represents a fatal error.
     * <p>
     * Stream exceptions are always fatal as they indicate protocol-level failures
     * that cannot be recovered from within the current session.
     *
     * @return {@code true} for all stream exceptions
     */
    @Override
    public boolean isFatal() {
        return true;
    }

    /**
     * Exception thrown when a malformed or invalid node is encountered in the protocol stream.
     * <p>
     * Nodes in WhatsApp are XMPP-like structures with a tag name, attributes, and content.
     * This exception is thrown when a received node does not conform to the expected structure.
     *
     * <h2>Node Structure</h2>
     * A valid node consists of:
     * <ul>
     *   <li><b>Tag:</b> A string identifier (e.g., "message", "receipt", "iq")</li>
     *   <li><b>Attributes:</b> Key-value pairs with string values</li>
     *   <li><b>Content:</b> Either child nodes, binary data, or text</li>
     * </ul>
     *
     * <h2>Possible Causes</h2>
     * <ul>
     *   <li><b>Decoding error:</b> The binary node format could not be parsed</li>
     *   <li><b>Missing required fields:</b> A node is missing required attributes or children</li>
     *   <li><b>Type mismatch:</b> An attribute or content has an unexpected type</li>
     *   <li><b>Truncation:</b> The node data was cut off mid-stream</li>
     *   <li><b>Protocol violation:</b> The node structure violates protocol requirements</li>
     * </ul>
     *
     * <h2>Examples</h2>
     * <ul>
     *   <li>A message node missing the "from" attribute</li>
     *   <li>An IQ response with an invalid "type" value</li>
     *   <li>A receipt node with malformed timestamp</li>
     * </ul>
     */
    public static final class MalformedNode extends WhatsAppStreamException {
        /**
         * Constructs a new malformed node exception with no detail message.
         * <p>
         * This constructor is used when the error context is self-evident from the call stack
         * or when additional details are not available.
         */
        public MalformedNode() {
            super();
        }

        /**
         * Constructs a new malformed node exception with the specified message.
         *
         * @param message the detail message describing why the node is malformed
         */
        public MalformedNode(String message) {
            super(message);
        }

        /**
         * Constructs a new malformed node exception with the specified message and cause.
         *
         * @param message the detail message describing why the node is malformed
         * @param cause   the underlying cause (e.g., parsing exception)
         */
        public MalformedNode(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Exception thrown when a WhatsApp protocol node request does not receive a response
     * within the expected timeout period.
     * <p>
     * The WhatsApp protocol uses a request-response pattern for many operations, where
     * nodes are sent to the server and responses are correlated by message ID. This
     * exception occurs when the expected response is not received within the configured
     * timeout (typically 60 seconds).
     *
     * <h2>Timeout Architecture</h2>
     * <ul>
     *   <li>Each request is assigned a unique message ID</li>
     *   <li>Responses are matched by ID and delivered to waiting handlers</li>
     *   <li>If no response arrives within the timeout, this exception is thrown</li>
     * </ul>
     *
     * <h2>Possible Causes</h2>
     * <ul>
     *   <li><b>Network issues:</b> Connectivity problems preventing server communication</li>
     *   <li><b>Server unavailability:</b> The server is overloaded or unreachable</li>
     *   <li><b>Invalid request:</b> The request was malformed and the server ignores it</li>
     *   <li><b>Authentication issues:</b> Session problems causing the server to ignore requests</li>
     *   <li><b>Rate limiting:</b> Too many requests caused the server to stop responding</li>
     * </ul>
     *
     * <h2>Captured Information</h2>
     * The exception captures the original node that timed out, which is valuable for:
     * <ul>
     *   <li>Debugging which operation failed</li>
     *   <li>Logging for later analysis</li>
     *   <li>Potential retry logic (with appropriate backoff)</li>
     * </ul>
     *
     * @see Node
     */
    public static final class NodeTimeout extends WhatsAppStreamException {
        /**
         * The WhatsApp protocol node that did not receive a response in time.
         */
        private final Node node;

        /**
         * Constructs a new node timeout exception with the node that timed out.
         *
         * @param node the WhatsApp protocol node that did not receive a response in time;
         *             must not be {@code null}
         * @throws NullPointerException if node is null
         */
        public NodeTimeout(Node node) {
            super("Node timeout: " + Objects.requireNonNull(node, "node cannot be null"));
            this.node = node;
        }

        /**
         * Constructs a new node timeout exception with a custom message and the timed-out node.
         *
         * @param message the detail message describing the timeout condition
         * @param node    the WhatsApp protocol node that did not receive a response in time;
         *                must not be {@code null}
         * @throws NullPointerException if node is null
         */
        public NodeTimeout(String message, Node node) {
            super(message);
            this.node = Objects.requireNonNull(node, "node cannot be null");
        }

        /**
         * Constructs a new node timeout exception with a message, cause, and the timed-out node.
         *
         * @param message the detail message describing the timeout condition
         * @param node    the WhatsApp protocol node that did not receive a response in time;
         *                must not be {@code null}
         * @param cause   the underlying cause of the timeout
         * @throws NullPointerException if node is null
         */
        public NodeTimeout(String message, Node node, Throwable cause) {
            super(message, cause);
            this.node = Objects.requireNonNull(node, "node cannot be null");
        }

        /**
         * Returns the WhatsApp protocol node that did not receive a response within the timeout period.
         * <p>
         * This node can be examined to understand which operation failed and potentially
         * retry the operation after reconnecting.
         *
         * @return the node that timed out; never {@code null}
         */
        public Node node() {
            return node;
        }
    }
}
