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
 * Sealed disjunction over the optional contacts/replied filter — at
 * most one is applied per request.
 *
 * @implNote {@code WASmaxOutNewslettersContactsOrRepliedFilterMixinMixinGroup.mergeContactsOrRepliedFilterMixinMixinGroup}.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutNewslettersContactsOrRepliedFilterMixinMixinGroup")
public sealed interface SmaxNewslettersGetNewsletterResponsesFilter permits SmaxNewslettersGetNewsletterResponsesFilter.Contacts, SmaxNewslettersGetNewsletterResponsesFilter.Replied {

    /**
     * Filters the response slice to entries authored by the user's
     * address-book contacts.
     *
     * @implNote {@code WASmaxOutNewslettersContactsFilterMixinMixin.mergeContactsFilterMixinMixin}.
     */
    @WhatsAppWebModule(moduleName = "WASmaxOutNewslettersContactsFilterMixinMixin")
    final class Contacts implements SmaxNewslettersGetNewsletterResponsesFilter {
        /**
         * Constructs a new contacts filter.
         */
        public Contacts() {
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            return obj != null && obj.getClass() == this.getClass();
        }

        @Override
        public int hashCode() {
            return Contacts.class.hashCode();
        }

        @Override
        public String toString() {
            return "SmaxNewslettersGetNewsletterResponsesFilter.Contacts[]";
        }
    }

    /**
     * Filters the response slice to entries the question owner has
     * already explicitly replied to.
     *
     * @implNote {@code WASmaxOutNewslettersRepliedFilterMixinMixin.mergeRepliedFilterMixinMixin}.
     */
    @WhatsAppWebModule(moduleName = "WASmaxOutNewslettersRepliedFilterMixinMixin")
    final class Replied implements SmaxNewslettersGetNewsletterResponsesFilter {
        /**
         * Constructs a new replied filter.
         */
        public Replied() {
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            return obj != null && obj.getClass() == this.getClass();
        }

        @Override
        public int hashCode() {
            return Replied.class.hashCode();
        }

        @Override
        public String toString() {
            return "SmaxNewslettersGetNewsletterResponsesFilter.Replied[]";
        }
    }
}
