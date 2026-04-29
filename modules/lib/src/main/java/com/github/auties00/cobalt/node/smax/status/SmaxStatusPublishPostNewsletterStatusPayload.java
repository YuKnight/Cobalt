package com.github.auties00.cobalt.node.smax.status;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import java.util.Objects;
import java.util.Optional;

/**
 * Sealed disjunction over the publish-payload addressing modes —
 * either a "client + server id" reference (publishing a
 * status-newsletter-reaction or status-newsletter-reaction-revoke
 * tied to a previously-published status identified by its
 * server-id) or a "client id only" reference (publishing a
 * brand-new status carrying an inner client-id content payload).
 *
 * @implNote {@code WASmaxOutStatusPublishClientPostNewsletterStatusAndServerOrPostNewsletterStatusIDMixinGroup.mergeClientPostNewsletterStatusAndServerOrPostNewsletterStatusIDMixinGroup}.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutStatusPublishClientPostNewsletterStatusAndServerOrPostNewsletterStatusIDMixinGroup")
public sealed interface SmaxStatusPublishPostNewsletterStatusPayload permits SmaxStatusPublishPostNewsletterStatusPayload.WithServerId, SmaxStatusPublishPostNewsletterStatusPayload.WithClientIdOnly {

    /**
     * The "client id + server id" payload — used for publishing a
     * status-newsletter-reaction or status-newsletter-reaction-revoke
     * that references a previously-published status.
     *
     * @implNote {@code WASmaxOutStatusPublishPostNewsletterStatusClientAndServerIDMixin.mergePostNewsletterStatusClientAndServerIDMixin}
     *           composes
     *           {@code WASmaxOutStatusPublishStatusNewsletterReactionStatusNewsletterReactionOrStatusNewsletterReactionRevokeMixinGroup}
     *           over a {@code <status id=STANZA_ID(t)
     *           server_id=INT(n)>...</status>} envelope.
     */
    @WhatsAppWebModule(moduleName = "WASmaxOutStatusPublishPostNewsletterStatusClientAndServerIDMixin")
    @WhatsAppWebModule(moduleName = "WASmaxOutStatusPublishStatusNewsletterReactionStatusNewsletterReactionOrStatusNewsletterReactionRevokeMixinGroup")
    final class WithServerId implements SmaxStatusPublishPostNewsletterStatusPayload {
        /**
         * The locally-generated stanza id assigned to the publish.
         */
        private final String stanzaId;

        /**
         * The server-id of the status the publish targets.
         */
        private final long statusServerId;

        /**
         * The inner reaction / reaction-revoke content payload as a
         * fully-built {@link Node}.
         */
        private final Node innerContent;

        /**
         * Constructs a new "client + server id" payload.
         *
         * @param stanzaId       the publish stanza id; never
         *                       {@code null}
         * @param statusServerId the server-id of the targeted
         *                       status
         * @param innerContent   the variant-shaped child payload;
         *                       never {@code null}
         * @throws NullPointerException if {@code stanzaId} or
         *                              {@code innerContent} is
         *                              {@code null}
         */
        public WithServerId(String stanzaId, long statusServerId, Node innerContent) {
            this.stanzaId = Objects.requireNonNull(stanzaId, "stanzaId cannot be null");
            this.statusServerId = statusServerId;
            this.innerContent = Objects.requireNonNull(innerContent, "innerContent cannot be null");
        }

        /**
         * Returns the publish stanza id.
         *
         * @return the id; never {@code null}
         */
        public String stanzaId() {
            return stanzaId;
        }

        /**
         * Returns the targeted status server-id.
         *
         * @return the server id
         */
        public long statusServerId() {
            return statusServerId;
        }

        /**
         * Returns the inner content node.
         *
         * @return the node; never {@code null}
         */
        public Node innerContent() {
            return innerContent;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (WithServerId) obj;
            return this.statusServerId == that.statusServerId
                    && Objects.equals(this.stanzaId, that.stanzaId)
                    && Objects.equals(this.innerContent, that.innerContent);
        }

        @Override
        public int hashCode() {
            return Objects.hash(stanzaId, statusServerId, innerContent);
        }

        @Override
        public String toString() {
            return "SmaxStatusPublishPostNewsletterStatusPayload.WithServerId[stanzaId=" + stanzaId
                    + ", statusServerId=" + statusServerId
                    + ", innerContent=" + innerContent + ']';
        }
    }

    /**
     * The "client id only" payload — used for publishing a brand-new
     * status carrying an inner client-id content payload.
     *
     * @implNote {@code WASmaxOutStatusPublishPostNewsletterStatusClientIDMixin.mergePostNewsletterStatusClientIDMixin}
     *           composes
     *           {@code WASmaxOutStatusPublishNewsletterClientIdContent}
     *           over a {@code <status id=STANZA_ID(t)>} envelope.
     */
    @WhatsAppWebModule(moduleName = "WASmaxOutStatusPublishPostNewsletterStatusClientIDMixin")
    @WhatsAppWebModule(moduleName = "WASmaxOutStatusPublishNewsletterClientIdContent")
    final class WithClientIdOnly implements SmaxStatusPublishPostNewsletterStatusPayload {
        /**
         * The locally-generated stanza id assigned to the publish.
         */
        private final String stanzaId;

        /**
         * The inner client-id content payload as a fully-built
         * node.
         */
        private final Node clientIdContent;

        /**
         * Constructs a new "client id only" payload.
         *
         * @param stanzaId        the publish stanza id; never
         *                        {@code null}
         * @param clientIdContent the inner client-id content node;
         *                        never {@code null}
         * @throws NullPointerException if either argument is
         *                              {@code null}
         */
        public WithClientIdOnly(String stanzaId, Node clientIdContent) {
            this.stanzaId = Objects.requireNonNull(stanzaId, "stanzaId cannot be null");
            this.clientIdContent = Objects.requireNonNull(clientIdContent, "clientIdContent cannot be null");
        }

        /**
         * Returns the publish stanza id.
         *
         * @return the id; never {@code null}
         */
        public String stanzaId() {
            return stanzaId;
        }

        /**
         * Returns the inner client-id content node.
         *
         * @return the node; never {@code null}
         */
        public Node clientIdContent() {
            return clientIdContent;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (WithClientIdOnly) obj;
            return Objects.equals(this.stanzaId, that.stanzaId)
                    && Objects.equals(this.clientIdContent, that.clientIdContent);
        }

        @Override
        public int hashCode() {
            return Objects.hash(stanzaId, clientIdContent);
        }

        @Override
        public String toString() {
            return "SmaxStatusPublishPostNewsletterStatusPayload.WithClientIdOnly[stanzaId=" + stanzaId
                    + ", clientIdContent=" + clientIdContent + ']';
        }
    }
}
