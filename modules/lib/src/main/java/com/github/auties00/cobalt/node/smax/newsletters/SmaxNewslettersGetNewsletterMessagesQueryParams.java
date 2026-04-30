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
 * Sealed disjunction over the newsletter addressing modes. Either a
 * JID-keyed query or an invite-key query.
 *
 * @implNote {@code WASmaxOutNewslettersQueryNewsletterParams.mergeQueryNewsletterParams}.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutNewslettersQueryNewsletterParams")
public sealed interface SmaxNewslettersGetNewsletterMessagesQueryParams permits SmaxNewslettersGetNewsletterMessagesQueryParams.ByJid, SmaxNewslettersGetNewsletterMessagesQueryParams.ByInvite {

    /**
     * Addressing the newsletter by its JID, optionally with a
     * {@code view_role} string the relay uses for ACL projection.
     *
     * @implNote {@code WASmaxOutNewslettersQueryNewsletterJIDParamsMixin.mergeQueryNewsletterJIDParamsMixin}.
     */
    @WhatsAppWebModule(moduleName = "WASmaxOutNewslettersQueryNewsletterJIDParamsMixin")
    final class ByJid implements SmaxNewslettersGetNewsletterMessagesQueryParams {
        /**
         * The newsletter JID being queried.
         */
        private final Jid newsletterJid;

        /**
         * The optional view-role projection string.
         */
        private final String viewRole;

        /**
         * Constructs a JID-addressed query.
         *
         * @param newsletterJid the newsletter JID; never {@code null}
         * @param viewRole      the optional view-role; may be
         *                      {@code null}
         * @throws NullPointerException if {@code newsletterJid} is
         *                              {@code null}
         */
        public ByJid(Jid newsletterJid, String viewRole) {
            this.newsletterJid = Objects.requireNonNull(newsletterJid, "newsletterJid cannot be null");
            this.viewRole = viewRole;
        }

        /**
         * Returns the newsletter JID.
         *
         * @return the JID; never {@code null}
         */
        public Jid newsletterJid() {
            return newsletterJid;
        }

        /**
         * Returns the optional view-role.
         *
         * @return an {@link Optional} carrying the view-role, or empty
         *         when omitted
         */
        public Optional<String> viewRole() {
            return Optional.ofNullable(viewRole);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (ByJid) obj;
            return Objects.equals(this.newsletterJid, that.newsletterJid)
                    && Objects.equals(this.viewRole, that.viewRole);
        }

        @Override
        public int hashCode() {
            return Objects.hash(newsletterJid, viewRole);
        }

        @Override
        public String toString() {
            return "SmaxNewslettersGetNewsletterMessagesQueryParams.ByJid[newsletterJid=" + newsletterJid + ", viewRole=" + viewRole + ']';
        }
    }

    /**
     * Addressing the newsletter by its invite key (the public link
     * token), optionally with a {@code view_role} string.
     *
     * @implNote {@code WASmaxOutNewslettersQueryNewsletterInviteParamsMixin.mergeQueryNewsletterInviteParamsMixin}.
     */
    @WhatsAppWebModule(moduleName = "WASmaxOutNewslettersQueryNewsletterInviteParamsMixin")
    final class ByInvite implements SmaxNewslettersGetNewsletterMessagesQueryParams {
        /**
         * The newsletter invite key.
         */
        private final String inviteKey;

        /**
         * The optional view-role projection string.
         */
        private final String viewRole;

        /**
         * Constructs an invite-addressed query.
         *
         * @param inviteKey the invite key; never {@code null}
         * @param viewRole  the optional view-role; may be {@code null}
         * @throws NullPointerException if {@code inviteKey} is
         *                              {@code null}
         */
        public ByInvite(String inviteKey, String viewRole) {
            this.inviteKey = Objects.requireNonNull(inviteKey, "inviteKey cannot be null");
            this.viewRole = viewRole;
        }

        /**
         * Returns the invite key.
         *
         * @return the key; never {@code null}
         */
        public String inviteKey() {
            return inviteKey;
        }

        /**
         * Returns the optional view-role.
         *
         * @return an {@link Optional} carrying the view-role, or empty
         *         when omitted
         */
        public Optional<String> viewRole() {
            return Optional.ofNullable(viewRole);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (ByInvite) obj;
            return Objects.equals(this.inviteKey, that.inviteKey)
                    && Objects.equals(this.viewRole, that.viewRole);
        }

        @Override
        public int hashCode() {
            return Objects.hash(inviteKey, viewRole);
        }

        @Override
        public String toString() {
            return "SmaxNewslettersGetNewsletterMessagesQueryParams.ByInvite[inviteKey=" + inviteKey + ", viewRole=" + viewRole + ']';
        }
    }
}
