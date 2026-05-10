package com.github.auties00.cobalt.model.setting.privacy;

import com.github.auties00.cobalt.model.jid.Jid;

import java.util.List;
import java.util.Objects;

/**
 * Outcome of a per-category contact-blacklist query, modelled as a
 * sealed family over the two semantic shapes the relay can return.
 *
 * <p>When the relay omits the inner blacklist payload the caller's
 * cache is in sync — surfaced as {@link Unchanged}. When the relay
 * sends a fresh blacklist the caller should replace its cache —
 * surfaced as {@link Updated} carrying the fresh list of blocked
 * JIDs, the new server-side digest, and the addressing mode the
 * relay used to encode the entries.
 */
public sealed interface ContactBlacklistResult permits ContactBlacklistResult.Unchanged, ContactBlacklistResult.Updated {
    /**
     * Indicates the cached contact blacklist is still in sync with
     * the relay. The caller does not need to touch any local state.
     */
    final class Unchanged implements ContactBlacklistResult {
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
            return "ContactBlacklistResult.Unchanged[]";
        }
    }

    /**
     * Indicates the relay returned a fresh per-category contact
     * blacklist to replace the caller's cache.
     *
     * <p>{@link #blockedJids()} is the up-to-date list of blocked
     * users for the queried category. {@link #dhash()} is the new
     * server-side digest. {@link #addressingMode()} echoes the
     * addressing mode used to encode the entries.
     */
    final class Updated implements ContactBlacklistResult {
        /**
         * The fresh list of blocked JIDs for the queried category.
         */
        private final List<Jid> blockedJids;

        /**
         * The new server-side digest.
         */
        private final String dhash;

        /**
         * The addressing mode the relay used to encode the entries.
         */
        private final ContactBlacklistAddressingMode addressingMode;

        /**
         * Constructs a new {@code Updated} result.
         *
         * @param blockedJids     the fresh list of blocked JIDs; never
         *                        {@code null}
         * @param dhash           the new digest; never {@code null}
         * @param addressingMode  the addressing mode; never
         *                        {@code null}
         * @throws NullPointerException if any argument is {@code null}
         */
        public Updated(List<Jid> blockedJids, String dhash, ContactBlacklistAddressingMode addressingMode) {
            this.blockedJids = List.copyOf(Objects.requireNonNull(blockedJids, "blockedJids cannot be null"));
            this.dhash = Objects.requireNonNull(dhash, "dhash cannot be null");
            this.addressingMode = Objects.requireNonNull(addressingMode, "addressingMode cannot be null");
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
         * Returns the new digest.
         *
         * @return the digest; never {@code null}
         */
        public String dhash() {
            return dhash;
        }

        /**
         * Returns the addressing mode the relay used to encode the
         * entries.
         *
         * @return the addressing mode; never {@code null}
         */
        public ContactBlacklistAddressingMode addressingMode() {
            return addressingMode;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Updated that
                    && Objects.equals(blockedJids, that.blockedJids)
                    && Objects.equals(dhash, that.dhash)
                    && addressingMode == that.addressingMode;
        }

        @Override
        public int hashCode() {
            return Objects.hash(blockedJids, dhash, addressingMode);
        }

        @Override
        public String toString() {
            return "ContactBlacklistResult.Updated[blockedJids=" + blockedJids
                    + ", dhash=" + dhash
                    + ", addressingMode=" + addressingMode + ']';
        }
    }
}
