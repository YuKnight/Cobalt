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
 * Sealed disjunction over the pagination cursor. Either {@code before}
 * or {@code after} a server-id, or none for the latest slice.
 *
 * @implNote {@code WASmaxOutNewslettersMessageDirections.mergeMessageDirections}.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutNewslettersMessageDirections")
public sealed interface SmaxNewslettersGetNewsletterMessagesDirection permits SmaxNewslettersGetNewsletterMessagesDirection.Before, SmaxNewslettersGetNewsletterMessagesDirection.After {

    /**
     * The {@code before} cursor. Fetch messages with server-ids
     * strictly less than the given pivot.
     *
     * @implNote {@code WASmaxOutNewslettersBeforeMixinMixin.mergeBeforeMixinMixin}.
     */
    @WhatsAppWebModule(moduleName = "WASmaxOutNewslettersBeforeMixinMixin")
    final class Before implements SmaxNewslettersGetNewsletterMessagesDirection {
        /**
         * The server-id pivot.
         */
        private final long pivot;

        /**
         * Constructs a {@code before} cursor at the given pivot.
         *
         * @param pivot the server-id pivot
         */
        public Before(long pivot) {
            this.pivot = pivot;
        }

        /**
         * Returns the server-id pivot.
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
            return "SmaxNewslettersGetNewsletterMessagesDirection.Before[pivot=" + pivot + ']';
        }
    }

    /**
     * The {@code after} cursor. Fetch messages with server-ids
     * strictly greater than the given pivot.
     *
     * @implNote {@code WASmaxOutNewslettersAfterMixinMixin.mergeAfterMixinMixin}.
     */
    @WhatsAppWebModule(moduleName = "WASmaxOutNewslettersAfterMixinMixin")
    final class After implements SmaxNewslettersGetNewsletterMessagesDirection {
        /**
         * The server-id pivot.
         */
        private final long pivot;

        /**
         * Constructs an {@code after} cursor at the given pivot.
         *
         * @param pivot the server-id pivot
         */
        public After(long pivot) {
            this.pivot = pivot;
        }

        /**
         * Returns the server-id pivot.
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
            return "SmaxNewslettersGetNewsletterMessagesDirection.After[pivot=" + pivot + ']';
        }
    }
}
