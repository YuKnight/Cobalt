package com.github.auties00.cobalt.calls2.net.transport;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * Selects the best relay for a call from the per-relay round-trip latencies the relay-latency probe round
 * measured.
 *
 * <p>A call may be offered several relays; before the media leg comes up the engine probes each relay's
 * latency and elects the one to bind. This helper is the pure-Java election rule: given a list of relay
 * candidates each carrying a measured round-trip latency, it returns the index of the relay with the
 * lowest latency, preferring a {@linkplain Candidate#favored() server-favored} relay when one is present
 * and breaking a latency tie by relay order. A candidate with no measured latency is not eligible to win
 * but does not block the election; when no candidate has a measured latency the election yields no result,
 * and the caller falls back to the inline-delivered relay.
 *
 * <p>This rule is stateless; a caller builds the candidate list from the parsed relay-latency reports and
 * the relay list and calls {@link #elect(List)} once per probe round.
 *
 * @implNote This implementation reproduces the comparison core of {@code find_best_relay} (fn5170) and
 *           {@code update_best_relays} (fn5172) in {@code wa_transport_relay_election.cc} of the wa-voip
 *           WASM module {@code ff-tScznZ8P}: the native {@code find_best_relay} walks the relay list,
 *           sums each relay's per-peer connection latency over the connected peers, and keeps the relay
 *           that is reachable by the most peers and then has the lowest summed latency, with a favored
 *           relay short-circuiting the comparison. Cobalt's transport does not hold the native per-peer
 *           latency tables (those are populated by the relay-latency probe round, whose ping datagram wire
 *           shape is uncaptured; see {@link LiveRelayTransport}), so this port operates over the single
 *           measured round-trip latency the signaling layer already parses per relay
 *           ({@code RelayLatencyEntry}) rather than the per-peer matrix the SFU case needs; that reduces to
 *           the native rule for the one-to-one and small-group cases the captures cover, where each relay
 *           has one measured latency and the lowest wins.
 */
public final class RelayElection {
    /**
     * Prevents instantiation of this stateless election holder.
     */
    private RelayElection() {
        throw new AssertionError("RelayElection is not instantiable");
    }

    /**
     * Elects the best relay from the candidate list, returning its index.
     *
     * <p>Returns the index of a {@linkplain Candidate#favored() server-favored} candidate that carries a
     * measured latency when one is present; otherwise the index of the candidate with the lowest measured
     * latency, ties broken toward the earlier index. Returns an empty result when no candidate carries a
     * measured latency, so the caller falls back to the inline-delivered relay.
     *
     * @param candidates the relay candidates with their measured latencies, in relay order; never
     *                   {@code null}
     * @return the index of the elected candidate, or an empty result when none has a measured latency
     * @throws NullPointerException if {@code candidates} is {@code null} or holds a {@code null} element
     */
    public static OptionalInt elect(List<Candidate> candidates) {
        Objects.requireNonNull(candidates, "candidates cannot be null");
        var bestIndex = -1;
        var bestLatency = Integer.MAX_VALUE;
        for (var index = 0; index < candidates.size(); index++) {
            var candidate = Objects.requireNonNull(candidates.get(index), "candidate cannot be null");
            var latency = candidate.latency();
            if (latency.isEmpty()) {
                continue;
            }
            if (candidate.favored()) {
                return OptionalInt.of(index);
            }
            if (latency.getAsInt() < bestLatency) {
                bestLatency = latency.getAsInt();
                bestIndex = index;
            }
        }
        return bestIndex < 0 ? OptionalInt.empty() : OptionalInt.of(bestIndex);
    }

    /**
     * One relay candidate in an election round, pairing a relay identifier with its measured latency.
     *
     * <p>The {@code relayId} names the relay so the caller can map the elected index back to the relay it
     * binds. The {@code latencyMillis} is the probe round's measured round-trip latency in milliseconds, or
     * {@code -1} when the relay was not reachable in the probe round; {@code favored} records whether the
     * server marked this relay preferred.
     *
     * @param relayId       the relay identifier
     * @param latencyMillis the measured round-trip latency in milliseconds, or {@code -1} when not
     *                      measured
     * @param favored       whether the server marked this relay as preferred
     */
    public record Candidate(int relayId, int latencyMillis, boolean favored) {
        /**
         * Returns the measured round-trip latency, if the relay was reached in the probe round.
         *
         * <p>Wraps the {@link #latencyMillis()} component, mapping the {@code -1} sentinel to an empty
         * result so the election rule can treat an unmeasured relay as ineligible without inspecting the
         * raw sentinel.
         *
         * @return an {@link OptionalInt} holding the latency in milliseconds, or empty when not measured
         */
        public OptionalInt latency() {
            return latencyMillis < 0 ? OptionalInt.empty() : OptionalInt.of(latencyMillis);
        }
    }

    /**
     * The result of an election: the elected relay index and the relay it names.
     *
     * @param index   the index of the elected candidate in the candidate list
     * @param relayId the identifier of the elected relay
     */
    public record Elected(int index, int relayId) {
        /**
         * Returns the result for the candidate at the given index, if the election produced one.
         *
         * @param candidates    the candidate list the election ran over; never {@code null}
         * @param electedIndex  the elected index, or an empty result when none was elected
         * @return an {@link Optional} holding the result, or empty when no index was elected
         * @throws NullPointerException if {@code candidates} or {@code electedIndex} is {@code null}
         */
        public static Optional<Elected> of(List<Candidate> candidates, OptionalInt electedIndex) {
            Objects.requireNonNull(candidates, "candidates cannot be null");
            Objects.requireNonNull(electedIndex, "electedIndex cannot be null");
            if (electedIndex.isEmpty()) {
                return Optional.empty();
            }
            var index = electedIndex.getAsInt();
            return Optional.of(new Elected(index, candidates.get(index).relayId()));
        }
    }
}
