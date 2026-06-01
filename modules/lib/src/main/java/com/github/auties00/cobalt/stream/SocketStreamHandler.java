package com.github.auties00.cobalt.stream;

import com.github.auties00.cobalt.node.Node;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Defines the contract for every per-tag stanza handler registered on a {@link NodeStreamService}.
 *
 * <p>A handler is selected by the inbound stanza's tag and scheduled by {@link NodeStreamService#handle(Node)}
 * through {@link #handleAsync(Node)}. The two permitted shapes own that scheduling: a {@link Concurrent}
 * runs each of its stanzas on its own fresh virtual thread, whereas an {@link Ordered} serialises its
 * stanzas per ordering key so a handler whose correctness depends on stanza arrival order is never raced.
 * Both shapes invoke the subclass's {@code handle} work method off the socket-reader thread and absorb any
 * failure so a single bad stanza never tears the socket down.
 *
 * @see Concurrent
 * @see Ordered
 */
public sealed interface SocketStreamHandler permits SocketStreamHandler.Concurrent, SocketStreamHandler.Ordered {
    /**
     * The system logger used to report handler failures that escape a handler's work method.
     */
    System.Logger LOGGER = System.getLogger(SocketStreamHandler.class.getName());

    /**
     * Schedules handling of the given inbound stanza off the socket-reader thread.
     *
     * <p>Called by {@link NodeStreamService#handle(Node)} for the handler registered under the stanza's tag.
     * The two shapes differ only in how they schedule the work: {@link Concurrent} on a fresh virtual
     * thread, {@link Ordered} on a per-key serial chain.
     *
     * @param node the inbound stanza
     */
    void handleAsync(Node node);

    /**
     * Clears any per-connection state so the handler is safe to reuse after a reconnection.
     *
     * @implSpec
     * The default implementation does nothing. Subclasses that hold one-shot bootstrap guards, rotation
     * timers, or accumulated counters override this method to clear them; an {@link Ordered} additionally
     * drops its in-flight per-key chains.
     *
     * @implNote
     * This implementation is a no-op; the dispatcher iterates every registered handler unconditionally so
     * stateless handlers do not need to override.
     */
    default void reset() {

    }

    /**
     * Invokes the given handler's work method, catching any {@link Throwable} so a failure never escapes
     * onto the scheduling thread's uncaught path.
     *
     * <p>Shared by both shapes so the failure-logging policy lives in one place.
     *
     * @param handler the handler whose {@code handle} method runs
     * @param node    the stanza to pass to the work method
     */
    private static void runHandle(SocketStreamHandler handler, Node node) {
        try {
            switch (handler) {
                case Concurrent concurrent -> concurrent.handle(node);
                case Ordered ordered -> ordered.handle(node);
            }
        } catch (Throwable throwable) {
            LOGGER.log(System.Logger.Level.WARNING,
                    "Handler {0} failed for stanza {1}: {2}",
                    handler.getClass().getSimpleName(),
                    node.description(),
                    throwable.getMessage());
        }
    }

    /**
     * A {@link SocketStreamHandler} whose stanzas carry no ordering dependency and are each processed on
     * their own fresh virtual thread.
     *
     * <p>Independent stanzas make progress in parallel and a slow handler cannot stall the socket reader.
     * This is the shape for every handler other than the per-chat ordered message handler.
     */
    abstract non-sealed class Concurrent implements SocketStreamHandler {
        /**
         * {@inheritDoc}
         *
         * @implNote
         * This implementation starts a fresh virtual thread that invokes {@link #handle(Node)} through
         * {@link SocketStreamHandler#runHandle(SocketStreamHandler, Node)}.
         */
        @Override
        public final void handleAsync(Node node) {
            Thread.startVirtualThread(() -> runHandle(this, node));
        }

        /**
         * Handles the given inbound stanza.
         *
         * <p>Public so a composing dispatcher in another package can invoke a sub-handler's work method
         * directly (synchronously, on the dispatcher's own thread) instead of scheduling a second
         * virtual thread through {@link #handleAsync(Node)}.
         *
         * @implSpec
         * Implementations complete on the calling virtual thread; any required asynchrony uses plain
         * blocking calls. Implementations may throw {@link IOException} to signal a blocking I/O failure;
         * any other {@link Throwable} is also caught and logged by {@link #handleAsync(Node)}.
         *
         * @param node the inbound stanza
         * @throws IOException if a blocking I/O operation fails
         */
        public abstract void handle(Node node) throws IOException;
    }

    /**
     * A {@link SocketStreamHandler} whose stanzas must be processed in arrival order within an ordering
     * key, so stanzas sharing a key are never handled concurrently.
     *
     * <p>Stanzas of one key run on a single chain that preserves wire arrival order, while stanzas of
     * different keys still run in parallel. The prime case is the {@code <message>} handler keyed by chat
     * JID: a group's sender-key distribution message must be applied before the sender-key messages that
     * depend on it, which concurrent dispatch would otherwise race into an avoidable retry.
     */
    abstract non-sealed class Ordered implements SocketStreamHandler {
        /**
         * Holds one in-flight chain per ordering key; a new stanza for a key chains onto its tail so the
         * two never run concurrently. Holds at most one (constantly replaced) entry per key and is cleared
         * by {@link #reset()} between connections.
         */
        private final ConcurrentMap<String, CompletableFuture<Void>> chains = new ConcurrentHashMap<>();

        /**
         * Holds the virtual-thread executor that runs the per-key chains in {@link #chains}, so each
         * stanza still runs off the socket-reader thread while the chain enforces per-key order.
         */
        private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

        /**
         * {@inheritDoc}
         *
         * @implNote
         * This implementation chains the stanza onto its key's tail through
         * {@link CompletableFuture#handleAsync}; {@code handleAsync} (rather than {@code thenRunAsync})
         * keeps the chain alive when a work invocation fails. Enqueueing runs on the single socket-reader
         * thread that calls {@link NodeStreamService#handle(Node)}, so the chain order matches wire arrival
         * order.
         */
        @Override
        public final void handleAsync(Node node) {
            var key = orderingKey(node);
            chains.compute(key, (ignoredKey, previous) -> {
                var base = previous != null ? previous : CompletableFuture.<Void>completedFuture(null);
                return base.handleAsync((ignoredResult, ignoredError) -> {
                    runHandle(this, node);
                    return null;
                }, executor);
            });
        }

        /**
         * {@inheritDoc}
         *
         * @implNote
         * This implementation additionally drops the in-flight per-key chains so the next connection
         * starts with none.
         */
        @Override
        public void reset() {
            chains.clear();
        }

        /**
         * Handles the given inbound stanza on its key's ordered chain.
         *
         * @implSpec
         * Behaves as {@link Concurrent#handle(Node)} but is invoked in arrival order per
         * {@link #orderingKey(Node)}.
         *
         * @param node the inbound stanza
         * @throws IOException if a blocking I/O operation fails
         */
        public abstract void handle(Node node) throws IOException;

        /**
         * Returns the key on which this handler's stanzas are serialised.
         *
         * <p>Stanzas whose keys are equal are processed one at a time in arrival order; stanzas with
         * distinct keys run concurrently.
         *
         * @param node the inbound stanza
         * @return the ordering key, never {@code null}
         */
        protected abstract String orderingKey(Node node);
    }
}
