package com.github.auties00.cobalt.model.setting.privacy;

import com.github.auties00.cobalt.model.jid.Jid;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Outcome of a delta-block-list query, modelled as a sealed family
 * over the two semantic shapes the relay can return.
 *
 * <p>The query carries a client-side digest of the cached block list.
 * When the digest still matches the relay's current state the relay
 * answers with {@link Unchanged} so the caller can skip the rebuild.
 * Otherwise the relay answers with {@link Updated} carrying the fresh
 * list of blocked JIDs and the new digest the caller should remember
 * for the next round-trip.
 *
 * <p>Pattern match on the variant to react to the two cases. The
 * sealed hierarchy guarantees the {@code switch} is exhaustive.
 */
public sealed interface BlockListResult permits BlockListResult.Unchanged, BlockListResult.Updated {
    /**
     * Indicates the cached block list is still in sync with the
     * relay. The caller does not need to touch any local state.
     */
    final class Unchanged implements BlockListResult {
        /**
         * Constructs a new {@code Unchanged} marker.
         */
        public Unchanged() {
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Unchanged;
        }

        @Override
        public int hashCode() {
            return Unchanged.class.hashCode();
        }

        @Override
        public String toString() {
            return "BlockListResult.Unchanged[]";
        }
    }

    /**
     * Indicates the relay returned a fresh block list to replace the
     * caller's cache.
     *
     * <p>{@link #blockedJids()} is the up-to-date list of blocked
     * users. {@link #dhash()} is the optional new server-side digest
     * the caller should pass back next time to allow the relay to
     * answer with an {@link Unchanged} result instead of resending
     * the whole list.
     */
    final class Updated implements BlockListResult {
        /**
         * The fresh list of blocked JIDs. Never {@code null}; may be
         * empty when the user has unblocked every contact.
         */
        private final List<Jid> blockedJids;

        /**
         * The optional new server-side digest. Callers should remember
         * this value and pass it back on the next fetch to enable the
         * delta-fetch fast path.
         */
        private final String dhash;

        /**
         * Constructs a new {@code Updated} result.
         *
         * @param blockedJids the fresh list of blocked JIDs; never
         *                    {@code null}
         * @param dhash       the optional new digest; may be
         *                    {@code null}
         * @throws NullPointerException if {@code blockedJids} is
         *                              {@code null}
         */
        public Updated(List<Jid> blockedJids, String dhash) {
            this.blockedJids = List.copyOf(Objects.requireNonNull(blockedJids, "blockedJids cannot be null"));
            this.dhash = dhash;
        }

        /**
         * Returns the fresh list of blocked JIDs.
         *
         * @return an unmodifiable list of JIDs; never {@code null}
         */
        public List<Jid> blockedJids() {
            return blockedJids;
        }

        /**
         * Returns the optional new digest.
         *
         * @return an {@link Optional} carrying the digest, or empty
         *         when the relay omitted it
         */
        public Optional<String> dhash() {
            return Optional.ofNullable(dhash);
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Updated that
                    && Objects.equals(blockedJids, that.blockedJids)
                    && Objects.equals(dhash, that.dhash);
        }

        @Override
        public int hashCode() {
            return Objects.hash(blockedJids, dhash);
        }

        @Override
        public String toString() {
            return "BlockListResult.Updated[blockedJids=" + blockedJids
                    + ", dhash=" + dhash + ']';
        }
    }
}
