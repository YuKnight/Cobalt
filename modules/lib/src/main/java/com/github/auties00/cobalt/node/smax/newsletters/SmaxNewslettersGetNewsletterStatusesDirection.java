package com.github.auties00.cobalt.node.smax.newsletters;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqResultResponseMixin;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Sealed disjunction over the pagination cursor. Either
 * {@code before} or {@code after} a server-id, or none for the latest
 * slice.
 *
 * @implNote {@code WASmaxOutNewslettersStatusDirections.mergeStatusDirections}.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutNewslettersStatusDirections")
public sealed interface SmaxNewslettersGetNewsletterStatusesDirection permits SmaxNewslettersGetNewsletterStatusesDirection.Before, SmaxNewslettersGetNewsletterStatusesDirection.After {

    /**
     * The {@code before} cursor. Fetch statuses with server-ids
     * strictly less than the given pivot.
     *
     * @implNote {@code WASmaxOutNewslettersStatusBeforeMixinMixin.mergeStatusBeforeMixinMixin}.
     */
    @WhatsAppWebModule(moduleName = "WASmaxOutNewslettersStatusBeforeMixinMixin")
    final class Before implements SmaxNewslettersGetNewsletterStatusesDirection {
        /**
         * The server-id pivot.
         */
        private final long pivot;

        /**
         * Constructs a {@code before} cursor.
         *
         * @param pivot the server-id pivot
         */
        public Before(long pivot) {
            this.pivot = pivot;
        }

        /**
         * Returns the pivot.
         *
         * @return the pivot
         */
        public long pivot() {
            return pivot;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Before that && this.pivot == that.pivot;
        }

        @Override
        public int hashCode() {
            return Long.hashCode(pivot);
        }

        @Override
        public String toString() {
            return "SmaxNewslettersGetNewsletterStatusesDirection.Before[pivot=" + pivot + ']';
        }
    }

    /**
     * The {@code after} cursor. Fetch statuses with server-ids
     * strictly greater than the given pivot.
     *
     * @implNote {@code WASmaxOutNewslettersStatusAfterMixinMixin.mergeStatusAfterMixinMixin}.
     */
    @WhatsAppWebModule(moduleName = "WASmaxOutNewslettersStatusAfterMixinMixin")
    final class After implements SmaxNewslettersGetNewsletterStatusesDirection {
        /**
         * The server-id pivot.
         */
        private final long pivot;

        /**
         * Constructs an {@code after} cursor.
         *
         * @param pivot the server-id pivot
         */
        public After(long pivot) {
            this.pivot = pivot;
        }

        /**
         * Returns the pivot.
         *
         * @return the pivot
         */
        public long pivot() {
            return pivot;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof After that && this.pivot == that.pivot;
        }

        @Override
        public int hashCode() {
            return Long.hashCode(pivot);
        }

        @Override
        public String toString() {
            return "SmaxNewslettersGetNewsletterStatusesDirection.After[pivot=" + pivot + ']';
        }
    }
}
