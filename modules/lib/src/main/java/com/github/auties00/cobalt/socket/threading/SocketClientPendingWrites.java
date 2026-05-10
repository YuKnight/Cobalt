package com.github.auties00.cobalt.socket.threading;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unbounded thread-safe queue of {@link ByteBuffer} instances designed
 * for the multiple-producer single-consumer pattern that the socket
 * stack uses for outbound writes.
 *
 * <p>Elements are FIFO with respect to each individual producer; the
 * consumer retrieves elements in the global order in which producer
 * stores became visible.
 *
 * <p>{@link #offer(ByteBuffer)} is lock-free; the common path executes
 * a single atomic increment and one release store with no allocation.
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
     * One fixed-size segment in the linked chain of chunks.
     */
    static final class Chunk {
        /**
         * Backing array for this chunk's slots; entries are populated
         * by producers via the {@link #ELEMENT} VarHandle and cleared
         * by the consumer in {@link #release(int)}.
         */
        final ByteBuffer[] data;

        /**
         * Producer claim counter, incremented atomically by
         * {@link #offer(ByteBuffer)} to reserve the next slot.
         */
        @SuppressWarnings("unused") volatile int index;

        /**
         * Forward pointer to the next chunk, lazily allocated when
         * {@link #index} reaches the chunk capacity.
         */
        @SuppressWarnings("unused") volatile Chunk next;

        /**
         * Creates a chunk with capacity {@code capacity}.
         *
         * @param capacity the number of slots in this chunk
         */
        Chunk(int capacity) {
            this.data = new ByteBuffer[capacity];
        }
    }

    /**
     * Zero-copy view into a chunk's backing array returned by
     * {@link SocketClientPendingWrites#claim()}.
     *
     * @param array  the chunk's internal array, shared with the queue
     * @param offset index of the first available element
     * @param count  number of available elements starting at offset
     */
    public record Claim(ByteBuffer[] array, int offset, int count) {
        /**
         * Sentinel claim used when the queue has nothing to deliver.
         */
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

    /**
     * Number of slots per chunk, fixed at construction time.
     */
    private final int chunkCapacity;

    /**
     * Backpressure threshold; {@link #offer(ByteBuffer)} returns
     * {@code false} once the queue holds this many unreleased buffers.
     */
    private final int maxPending;

    /**
     * Counter of buffers currently in flight, used to enforce
     * {@link #maxPending}.
     */
    private final AtomicInteger pendingCount;

    /**
     * Tail chunk, where producers reserve slots; updated atomically
     * via {@link #PRODUCER_CHUNK} when a chunk fills up.
     */
    @SuppressWarnings({"unused", "FieldMayBeFinal"})
    private volatile Chunk producerChunk;

    /**
     * Head chunk, owned by the consumer thread.
     */
    private Chunk consumerChunk;

    /**
     * Index of the next element the consumer will read from
     * {@link #consumerChunk}.
     */
    private int consumerOffset;

    /**
     * Creates a queue with the given chunk capacity and a default
     * backpressure limit of {@code chunkCapacity * 32}.
     *
     * @param chunkCapacity the number of elements per chunk; must be a
     *                      power of two
     * @throws IllegalArgumentException if {@code chunkCapacity} is not
     *         a power of two
     */
    public SocketClientPendingWrites(int chunkCapacity) {
        this(chunkCapacity, chunkCapacity * 32);
    }

    /**
     * Creates a queue with the given chunk capacity and backpressure
     * limit.
     *
     * @param chunkCapacity the number of elements per chunk; must be a
     *                      power of two
     * @param maxPending    the maximum number of pending buffers
     *                      before {@link #offer(ByteBuffer)} returns
     *                      {@code false}
     * @throws IllegalArgumentException if {@code chunkCapacity} is not
     *         a power of two
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
     * Inserts {@code buffer} at the tail of this queue.
     *
     * <p>Returns {@code false} when accepting the buffer would push
     * the number of unreleased entries above {@link #maxPending},
     * giving the caller a chance to apply backpressure.
     *
     * @param buffer the buffer to enqueue
     * @return {@code true} if the buffer was enqueued, {@code false}
     *         if the queue is at capacity
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
     * Returns whether this queue currently has no elements available
     * to the consumer.
     *
     * @return {@code true} if the queue is empty from the consumer's
     *         point of view
     */
    public boolean isEmpty() {
        var chunk = consumerChunk;
        var produced = Math.min((int) CHUNK_INDEX.getAcquire(chunk), chunkCapacity);
        return consumerOffset >= produced && chunk.next == null;
    }

    /**
     * Claims a contiguous slice of the current chunk's backing array
     * for the consumer to drain.
     *
     * <p>The returned view shares the queue's backing storage; the
     * consumer must call {@link #release(int)} once it has finished
     * consuming, so the entries can be cleared and the
     * {@link #pendingCount} accounting updated.
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
     * Releases the first {@code consumed} elements of the most
     * recently claimed region.
     *
     * <p>Clears the entries, advances {@link #consumerOffset}, hops to
     * the next chunk when the current one has been fully drained and
     * decrements {@link #pendingCount} so producers blocked on
     * backpressure can make progress.
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
