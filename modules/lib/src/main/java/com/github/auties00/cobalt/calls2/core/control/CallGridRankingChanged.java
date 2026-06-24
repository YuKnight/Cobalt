package com.github.auties00.cobalt.calls2.core.control;

import com.github.auties00.cobalt.calls2.core.CallEventType;
import com.github.auties00.cobalt.model.jid.Jid;

import java.util.List;
import java.util.Objects;

/**
 * A {@link ControlCallEvent} reporting a new participant ordering for the call grid.
 *
 * <p>The engine ranks group-call participants for display by a comparator that orders hand-raised
 * participants first, then active speakers, then by rank, then by stable index. When that ordering
 * changes, the engine emits this with the new {@link #ranking() ranking}: the participant device JIDs in
 * display order, the speaker first.
 *
 * @implNote This implementation models the payload of the wa-voip event {@code 0x6f}
 * ({@link CallEventType#CALL_GRID_RANKING_CHANGED}) of module {@code ff-tScznZ8P}, emitted when the
 * grid-ranking comparator (hand-raised desc, active-speaker desc, rank desc, index asc) produces a
 * different order. The full native payload byte layout is not recovered; Cobalt carries the ordered
 * participant list the listener surface needs.
 * @param ranking the participant device JIDs in display order, the highest-ranked first; never
 *                {@code null}, defensively copied and unmodifiable
 */
public record CallGridRankingChanged(List<Jid> ranking) implements ControlCallEvent {
    /**
     * Validates the record components and defensively copies the ranking list.
     *
     * @throws NullPointerException if {@code ranking} is {@code null} or contains a {@code null} element
     */
    public CallGridRankingChanged {
        Objects.requireNonNull(ranking, "ranking cannot be null");
        ranking = List.copyOf(ranking);
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link CallEventType#CALL_GRID_RANKING_CHANGED}
     */
    @Override
    public CallEventType type() {
        return CallEventType.CALL_GRID_RANKING_CHANGED;
    }
}
