package com.github.auties00.cobalt.model.setting.privacy;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Outcome of a delta-opt-out-list query, modelled as a sealed family
 * over the two semantic shapes the relay can return.
 *
 * <p>The query carries a client-side digest of the cached opt-out
 * list. When the digest still matches the relay's current state the
 * relay answers with {@link Unchanged}. Otherwise it answers with
 * {@link Updated} carrying the fresh list of {@link OptOutEntry
 * entries} and the new digest.
 *
 * <p>Pattern match on the variant to react to the two cases. The
 * sealed hierarchy guarantees the {@code switch} is exhaustive.
 */
public sealed interface OptOutListResult permits OptOutListResult.Unchanged, OptOutListResult.Updated {
    /**
     * Indicates the cached opt-out list is still in sync with the
     * relay. The caller does not need to touch any local state.
     */
    final class Unchanged implements OptOutListResult {
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
            return "OptOutListResult.Unchanged[]";
        }
    }

    /**
     * Indicates the relay returned a fresh opt-out list to replace
     * the caller's cache.
     *
     * <p>{@link #entries()} is the up-to-date list of opt-out
     * memberships. {@link #dhash()} is the optional new server-side
     * digest the caller should pass back next time.
     */
    final class Updated implements OptOutListResult {
        /**
         * The fresh list of opt-out entries. Never {@code null}; may
         * be empty.
         */
        private final List<OptOutEntry> entries;

        /**
         * The optional new server-side digest.
         */
        private final String dhash;

        /**
         * Constructs a new {@code Updated} result.
         *
         * @param entries the fresh list of opt-out entries; never
         *                {@code null}
         * @param dhash   the optional new digest; may be {@code null}
         * @throws NullPointerException if {@code entries} is
         *                              {@code null}
         */
        public Updated(List<OptOutEntry> entries, String dhash) {
            this.entries = List.copyOf(Objects.requireNonNull(entries, "entries cannot be null"));
            this.dhash = dhash;
        }

        /**
         * Returns the fresh list of opt-out entries.
         *
         * @return an unmodifiable list of entries; never {@code null}
         */
        public List<OptOutEntry> entries() {
            return entries;
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
                    && Objects.equals(entries, that.entries)
                    && Objects.equals(dhash, that.dhash);
        }

        @Override
        public int hashCode() {
            return Objects.hash(entries, dhash);
        }

        @Override
        public String toString() {
            return "OptOutListResult.Updated[entries=" + entries
                    + ", dhash=" + dhash + ']';
        }
    }
}
