package com.github.auties00.cobalt.calls2.platform;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.util.Objects;

/**
 * Owns the off-heap memory lifetime of a single call subsystem, replacing the native engine's per-call
 * memory pool with a named {@link Arena}.
 *
 * <p>The native voip engine allocates almost everything a call needs (PJSIP transport state, parser
 * scratch, ICE candidates) out of named PJLIB memory pools, and wraps pool creation in a small RAII
 * helper so a pool is released when its owner goes out of scope. Cobalt has no PJLIB allocator: the
 * native libraries it binds through the foreign function and memory API expect raw, correctly-scoped
 * {@link MemorySegment} arguments, and the natural lifetime container for those segments is an
 * {@link Arena}. This class is that container with a name attached. It hands its backing arena to native
 * bindings as a {@link java.lang.foreign.SegmentAllocator}, allocates off-heap segments whose validity is
 * bounded by the arena's lifetime, and frees every one of them at once when {@link #close()} runs, which
 * mirrors the all-at-once teardown of a pool.
 *
 * <p>An instance is created through {@link #create(String)} and is an {@link AutoCloseable} resource;
 * the recommended use is a try-with-resources block whose scope matches the lifetime of the call
 * subsystem that owns the memory:
 *
 * {@snippet :
 *   try (var pool = WaCallMemoryArena.create("ice-transport")) {
 *       MemorySegment buffer = pool.allocate(1500);
 *       // hand buffer to a native binding; valid until the block exits
 *   }
 *   // every segment allocated from pool is now released
 * }
 *
 * <p>The backing arena is a confined arena: it may be accessed and closed only by the thread that
 * created it. That matches how a call subsystem is driven by a single owning thread and lets the arena
 * detect cross-thread misuse, exactly as a PJLIB pool is not thread-safe and is expected to be touched
 * by one owner. A subsystem shared across threads must hold its own arena per thread or serialize access.
 *
 * @implNote This implementation replaces the {@code wa_call_create_mem_pool} RAII wrapper (fn846 over
 * {@code wa_call_create_mem_pool}, fn10970) from {@code platforms/wasm/utils/WaCallMemoryPool.h} of the
 * wa-voip engine (WASM module {@code ff-tScznZ8P}). The native wrapper creates a PJLIB {@code pj_pool_t}
 * for a named pool and, on a nonzero status, logs {@code "wa_call_create_mem_pool failed with status: <n>
 * for pool: <name>"} and nulls the handle so the caller observes a failed pool. There is no Java analogue
 * to a PJLIB pool, so the pool becomes a confined {@link Arena}: {@link Arena#ofConfined()} cannot
 * report a status code and never returns a null handle, so the only failure surface is an exhausted
 * address space at allocation time, which the foreign memory API raises directly; the name is retained
 * for diagnostics and to echo the per-pool identity the native wrapper logs.
 */
public final class WaCallMemoryArena implements AutoCloseable {
    /**
     * Holds the diagnostic name of this pool, echoing the named-pool identity the native wrapper logs.
     */
    private final String name;

    /**
     * Holds the confined arena that backs every allocation and bounds their lifetime.
     */
    private final Arena arena;

    /**
     * Constructs a pool over a name and its backing arena.
     *
     * @param name  the diagnostic pool name
     * @param arena the confined arena that owns the off-heap memory
     */
    private WaCallMemoryArena(String name, Arena arena) {
        this.name = name;
        this.arena = arena;
    }

    /**
     * Creates a memory pool with the given diagnostic name.
     *
     * <p>The returned pool owns a fresh confined {@link Arena}; close it, ideally through
     * try-with-resources, to release every segment it allocated.
     *
     * @param name the diagnostic pool name
     * @return a new open memory pool
     * @throws NullPointerException if {@code name} is {@code null}
     */
    public static WaCallMemoryArena create(String name) {
        Objects.requireNonNull(name, "name cannot be null");
        return new WaCallMemoryArena(name, Arena.ofConfined());
    }

    /**
     * Returns the diagnostic name of this pool.
     *
     * @return the pool name
     */
    public String name() {
        return name;
    }

    /**
     * Returns the backing arena, suitable to pass to a native binding as its segment allocator.
     *
     * <p>The arena is a {@link java.lang.foreign.SegmentAllocator}, so a binding that accepts an
     * allocator can allocate its own arguments from this pool and inherit the pool's lifetime. Do not
     * close the returned arena directly; close the owning pool through {@link #close()} instead.
     *
     * @return the confined arena backing this pool
     */
    public Arena arena() {
        return arena;
    }

    /**
     * Allocates an off-heap segment of the given size from this pool.
     *
     * <p>The returned segment is valid until this pool is closed. Its contents are zero-initialized, as
     * guaranteed by the foreign memory API.
     *
     * @param byteSize the size of the segment in bytes
     * @return a new zero-initialized native segment of {@code byteSize} bytes
     * @throws IllegalArgumentException if {@code byteSize} is negative
     * @throws IllegalStateException    if this pool has been closed or is accessed from another thread
     */
    public MemorySegment allocate(long byteSize) {
        return arena.allocate(byteSize);
    }

    /**
     * Allocates an off-heap segment laid out for the given memory layout from this pool.
     *
     * <p>The segment is sized and aligned for {@code layout} and is valid until this pool is closed.
     *
     * @param layout the layout that sizes and aligns the segment
     * @return a new zero-initialized native segment for {@code layout}
     * @throws NullPointerException  if {@code layout} is {@code null}
     * @throws IllegalStateException if this pool has been closed or is accessed from another thread
     */
    public MemorySegment allocate(MemoryLayout layout) {
        Objects.requireNonNull(layout, "layout cannot be null");
        return arena.allocate(layout);
    }

    /**
     * Releases every segment this pool allocated.
     *
     * <p>After this call the backing arena is closed and all segments it handed out are invalidated;
     * touching any of them afterward raises an {@link IllegalStateException}. Closing a pool more than
     * once is rejected by the backing arena. This mirrors the all-at-once teardown of the native pool the
     * RAII wrapper releases at end of scope.
     *
     * @throws IllegalStateException if this pool has already been closed or is closed from a thread other
     *                               than the one that created it
     */
    @Override
    public void close() {
        arena.close();
    }
}
