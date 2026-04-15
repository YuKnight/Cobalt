package com.github.auties00.cobalt.socket.threading;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * An unbounded thread-safe queue of {@link ByteBuffer} instances designed
 * for multiple-producer, single-consumer (MPSC) use.
 *
 * <p>This queue orders elements FIFO with respect to each individual
 * producer.  The consumer retrieves elements in the global order in
 * which producers' stores became visible.
 *
 * <p>The {@link #offer(ByteBuffer)} method is lock-free.  The common
 * path executes a single atomic increment and a single release-store
 * with no allocation.
 *
 * <p><em>Memory consistency effects:</em> actions in a producer thread
 * prior to placing a buffer into the queue <i>happen-before</i> actions
 * subsequent to the retrieval of that buffer via {@link #claim()} in
 * the consumer thread.
 */
public final class SocketClientPendingWrites {
    private static final VarHandle ELEMENT;
    private static final VarHandle CHUNK_INDEX;
    private static final VarHandle CHUNK_NEXT;
    private static final VarHandle PRODUCER_CHUNK;

    static {
        try {
            var lookup = MethodHandles.lookup();
            ELEMENT = MethodHandles.arrayElementVarHandle(ByteBuffer[].class);
            CHUNK_INDEX = lookup.findVarHandle(Chunk.class, "index", int.class);
            CHUNK_NEXT = lookup.findVarHandle(Chunk.class, "next", Chunk.class);
            PRODUCER_CHUNK = lookup.findVarHandle(
                    SocketClientPendingWrites.class, "producerChunk", Chunk.class
            );
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * A fixed-size array segment in the linked chain.
     */
    static final class Chunk {
        final ByteBuffer[] data;
        @SuppressWarnings("unused") volatile int index;
        @SuppressWarnings("unused") volatile Chunk next;

        Chunk(int capacity) {
            this.data = new ByteBuffer[capacity];
        }
    }

    /**
     * A zero-copy view into a chunk's backing array returned by
     * {@link SocketClientPendingWrites#claim()}.
     *
     * @param array  the chunk's internal array, not a copy
     * @param offset index of the first available element
     * @param count  number of available elements starting at offset
     */
    public record Claim(ByteBuffer[] array, int offset, int count) {
        private static final Claim EMPTY = new Claim(new ByteBuffer[0], 0, 0);

        /**
         * Returns whether this claim contains no elements.
         *
         * @return {@code true} if empty
         */
        public boolean isEmpty() {
            return count == 0;
        }
    }

    private final int chunkCapacity;
    private final int maxPending;
    private final AtomicInteger pendingCount;

    @SuppressWarnings({"unused", "FieldMayBeFinal"})
    private volatile Chunk producerChunk;

    private Chunk consumerChunk;
    private int consumerOffset;

    /**
     * Creates a queue with the specified chunk capacity and a default
     * backpressure limit of {@code chunkCapacity * 32}.
     *
     * @param chunkCapacity the number of elements per chunk, must be a
     *                      power of two
     * @throws IllegalArgumentException if chunkCapacity is not a power of two
     */
    public SocketClientPendingWrites(int chunkCapacity) {
        this(chunkCapacity, chunkCapacity * 32);
    }

    /**
     * Creates a queue with the specified chunk capacity and backpressure
     * limit.
     *
     * @param chunkCapacity the number of elements per chunk, must be a
     *                      power of two
     * @param maxPending    the maximum number of pending buffers before
     *                      {@link #offer(ByteBuffer)} returns {@code false}
     * @throws IllegalArgumentException if chunkCapacity is not a power of two
     */
    public SocketClientPendingWrites(int chunkCapacity, int maxPending) {
        if (Integer.bitCount(chunkCapacity) != 1) {
            throw new IllegalArgumentException("Chunk capacity must be a power of 2");
        }
        this.chunkCapacity = chunkCapacity;
        this.maxPending = maxPending;
        this.pendingCount = new AtomicInteger();
        var initial = new Chunk(chunkCapacity);
        this.producerChunk = initial;
        this.consumerChunk = initial;
    }

    /**
     * Inserts the specified buffer at the tail of this queue.
     *
     * <p>If the number of pending (not yet released) buffers would
     * exceed the configured maximum, the buffer is rejected and this
     * method returns {@code false}.
     *
     * @param buffer the buffer to insert
     * @return {@code true} if the buffer was enqueued, {@code false} if
     *         the queue is at capacity
     */
    public boolean offer(ByteBuffer buffer) {
        var current = pendingCount.get();
        while (true) {
            if (current >= maxPending) {
                return false;
            }
            var witness = pendingCount.compareAndExchange(current, current + 1);
            if (witness == current) {
                break;
            }
            current = witness;
        }

        while (true) {
            var chunk = producerChunk;
            var idx = (int) CHUNK_INDEX.getAndAdd(chunk, 1);

            if (idx < chunkCapacity) {
                ELEMENT.setRelease(chunk.data, idx, buffer);
                return true;
            }

            if (chunk.next == null) {
                CHUNK_NEXT.compareAndSet(chunk, null, new Chunk(chunkCapacity));
            }

            PRODUCER_CHUNK.compareAndSet(this, chunk, chunk.next);
        }
    }

    /**
     * Returns {@code true} if this queue contains no elements available
     * for consumption.
     *
     * @return {@code true} if no elements are currently available
     */
    public boolean isEmpty() {
        var chunk = consumerChunk;
        var produced = Math.min((int) CHUNK_INDEX.getAcquire(chunk), chunkCapacity);
        return consumerOffset >= produced && chunk.next == null;
    }

    /**
     * Claims a contiguous slice of the current chunk's backing array.
     *
     * @return a zero-copy view into the current chunk
     */
    public Claim claim() {
        while (true) {
            var chunk = consumerChunk;
            var produced = Math.min((int) CHUNK_INDEX.getAcquire(chunk), chunkCapacity);

            if (consumerOffset < produced) {
                for (var i = consumerOffset; i < produced; i++) {
                    while (ELEMENT.getAcquire(chunk.data, i) == null) {
                        Thread.onSpinWait();
                    }
                }
                return new Claim(chunk.data, consumerOffset, produced - consumerOffset);
            }

            if (consumerOffset == chunkCapacity && chunk.next != null) {
                consumerChunk = chunk.next;
                consumerOffset = 0;
                continue;
            }

            return Claim.EMPTY;
        }
    }

    /**
     * Releases consumed elements from the head of the last claimed
     * region.
     *
     * @param consumed the number of elements to release
     */
    public void release(int consumed) {
        for (var i = consumerOffset; i < consumerOffset + consumed; i++) {
            consumerChunk.data[i] = null;
        }
        consumerOffset += consumed;
        pendingCount.addAndGet(-consumed);

        if (consumerOffset == chunkCapacity && consumerChunk.next != null) {
            consumerChunk = consumerChunk.next;
            consumerOffset = 0;
        }
    }
}
