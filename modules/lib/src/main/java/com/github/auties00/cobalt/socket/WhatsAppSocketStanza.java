package com.github.auties00.cobalt.socket;

import com.github.auties00.cobalt.exception.WhatsAppStreamException;
import com.github.auties00.cobalt.node.Node;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Function;

/**
 * Tracks a single outbound stanza and the response that completes it.
 *
 * <p>The sender thread parks on {@link #waitForResponse()} until the
 * inbound dispatcher hands the matching reply to {@link #complete(Node)}.
 * A user-supplied filter decides whether an arriving node is the
 * expected reply, which keeps unrelated traffic from waking up the
 * waiter early.
 */
public final class WhatsAppSocketStanza {
    /**
     * Default upper bound on how long {@link #waitForResponse()} blocks
     * before throwing {@link WhatsAppStreamException.NodeTimeout}.
     */
    private static final Duration TIMEOUT = Duration.ofSeconds(60);

    /**
     * The outgoing node, retained so timeout exceptions can identify
     * which stanza expired.
     */
    private final Node body;

    /**
     * Predicate consulted to decide whether an arriving node satisfies
     * this stanza, or {@code null} to accept any node.
     */
    private final Function<Node, Boolean> filter;

    /**
     * The accepted response, set by {@link #complete(Node)} and read by
     * the waiter under {@code this} monitor.
     */
    private volatile Node response;

    /**
     * Creates a stanza tracker for the given outbound node.
     *
     * @param body   the node that has been or is about to be sent
     * @param filter predicate that returns {@code true} when an inbound
     *               node is the matching response, or {@code null} to
     *               accept the first node offered
     */
    public WhatsAppSocketStanza(Node body, Function<Node, Boolean> filter) {
        this.body = body;
        this.filter = filter;
    }

    /**
     * Offers an inbound node as the response for this stanza.
     *
     * <p>If the filter accepts the node (or no filter is configured),
     * the response is recorded and any thread blocked in
     * {@link #waitForResponse()} is woken up.
     *
     * @param response the candidate response, or {@code null} to
     *                 unconditionally complete the stanza
     * @return {@code true} if the response was accepted
     */
    public boolean complete(Node response) {
        var acceptable = response == null
                || filter == null
                || filter.apply(response);
        if (acceptable) {
            synchronized (this) {
                this.response = response;
                notifyAll();
            }
        }
        return acceptable;
    }

    /**
     * Blocks the calling virtual thread until a response arrives or the
     * default timeout elapses.
     *
     * @return the accepted response
     * @throws WhatsAppStreamException.NodeTimeout if no acceptable
     *         response arrives within the default timeout
     */
    public Node waitForResponse() {
        return waitForResponse(TIMEOUT);
    }

    /**
     * Blocks the calling virtual thread until a response arrives or the
     * supplied timeout elapses.
     *
     * @param timeout the maximum amount of time to wait
     * @return the accepted response
     * @throws WhatsAppStreamException.NodeTimeout if no acceptable
     *         response arrives within {@code timeout} or the wait is
     *         interrupted
     */
    public Node waitForResponse(Duration timeout) {
        synchronized (this) {
            var end = Instant.now().plus(timeout);
            while (response == null) {
                var remainingMs = Duration.between(Instant.now(), end).toMillis();
                if (remainingMs <= 0) {
                    throw new WhatsAppStreamException.NodeTimeout(body);
                }
                try {
                    wait(remainingMs);
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    throw new WhatsAppStreamException.NodeTimeout(body);
                }
            }
            return response;
        }
    }
}
