package com.github.auties00.cobalt.node.smax.biz;

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
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * The outbound stanza variant. Wraps the {@code <participants/>}
 * payload and the four optional toggles inside the canonical
 * {@code <iq xmlns="w:biz" type="get">} envelope.
 *
 * <p>Use {@link #builder()} for fluent construction; the primary
 * constructor accepts every argument explicitly.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutSmbMeteredMessagingAccountGetSMBMeteredMessagingCheckoutRequest")
@WhatsAppWebModule(moduleName = "WASmaxOutSmbMeteredMessagingAccountHackBaseIQGetRequestMixin")
public final class SmaxGetSMBMeteredMessagingCheckoutRequest implements SmaxOperation.Request {
    /**
     * The recipient JIDs (1..2000 entries).
     */
    private final List<Jid> participants;

    /**
     * Whether to attach the {@code <use_ad_account/>} marker.
     */
    private final boolean useAdAccount;

    /**
     * Whether to attach the {@code <skip_dedupe/>} marker.
     */
    private final boolean skipDedupe;

    /**
     * The optional {@code <offer id/>} value.
     */
    private final String offerId;

    /**
     * The optional list of pending-campaign entries (0..200).
     */
    private final List<SmaxGetSMBMeteredMessagingCheckoutPendingCampaign> pendingCampaigns;

    /**
     * Constructs a request directly. Use {@link #builder()} for
     * fluent construction.
     *
     * @param participants     the list of recipient JIDs; never
     *                         {@code null}; must contain 1..2000
     *                         entries
     * @param useAdAccount     whether to attach the
     *                         {@code <use_ad_account/>} marker
     * @param skipDedupe       whether to attach the
     *                         {@code <skip_dedupe/>} marker
     * @param offerId          the optional {@code <offer id/>}
     *                         value; may be {@code null}
     * @param pendingCampaigns the optional list of pending campaigns;
     *                         may be {@code null} (treated as empty)
     * @throws NullPointerException     if {@code participants} is
     *                                  {@code null}
     * @throws IllegalArgumentException if {@code participants} is
     *                                  outside {@code 1..2000} or
     *                                  {@code pendingCampaigns} has
     *                                  more than 200 entries
     */
    public SmaxGetSMBMeteredMessagingCheckoutRequest(List<Jid> participants,
                   boolean useAdAccount,
                   boolean skipDedupe,
                   String offerId,
                   List<SmaxGetSMBMeteredMessagingCheckoutPendingCampaign> pendingCampaigns) {
        Objects.requireNonNull(participants, "participants cannot be null");
        if (participants.isEmpty()) {
            throw new IllegalArgumentException("participants must contain at least one entry");
        }
        if (participants.size() > 2000) {
            throw new IllegalArgumentException("participants must contain at most 2000 entries");
        }
        var pcCopy = pendingCampaigns == null ? List.<SmaxGetSMBMeteredMessagingCheckoutPendingCampaign>of() : List.copyOf(pendingCampaigns);
        if (pcCopy.size() > 200) {
            throw new IllegalArgumentException("pendingCampaigns must contain at most 200 entries");
        }
        this.participants = List.copyOf(participants);
        this.useAdAccount = useAdAccount;
        this.skipDedupe = skipDedupe;
        this.offerId = offerId;
        this.pendingCampaigns = pcCopy;
    }

    /**
     * Returns a fresh builder.
     *
     * @return a new {@link Builder}; never {@code null}
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns the recipient JIDs.
     *
     * @return an unmodifiable list of 1..2000 JIDs
     */
    public List<Jid> participants() {
        return participants;
    }

    /**
     * Returns whether the {@code <use_ad_account/>} marker is
     * attached.
     *
     * @return {@code true} when the marker is emitted
     */
    public boolean useAdAccount() {
        return useAdAccount;
    }

    /**
     * Returns whether the {@code <skip_dedupe/>} marker is
     * attached.
     *
     * @return {@code true} when the marker is emitted
     */
    public boolean skipDedupe() {
        return skipDedupe;
    }

    /**
     * Returns the optional offer ID.
     *
     * @return an {@link Optional} carrying the offer ID, or empty
     */
    public Optional<String> offerId() {
        return Optional.ofNullable(offerId);
    }

    /**
     * Returns the list of pending campaigns.
     *
     * @return an unmodifiable list of 0..200 entries; never
     *         {@code null}
     */
    public List<SmaxGetSMBMeteredMessagingCheckoutPendingCampaign> pendingCampaigns() {
        return pendingCampaigns;
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and
     *         the checkout payload
     *
     * @implNote {@code WASmaxOutSmbMeteredMessagingAccountGetSMBMeteredMessagingCheckoutRequest.makeGetSMBMeteredMessagingCheckoutRequest}
     *           composes
     *           {@code WASmaxOutSmbMeteredMessagingAccountHackBaseIQGetRequestMixin}
     *           ({@code id=generateId()}, {@code type="get"}) over a
     *           bare {@code <iq xmlns="w:biz" smax_id=120>} root
     *           that carries a {@code <participants/>} block plus up
     *           to four optional children.
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutSmbMeteredMessagingAccountGetSMBMeteredMessagingCheckoutRequest",
            exports = "makeGetSMBMeteredMessagingCheckoutRequest",
            adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var children = new ArrayList<Node>();
        var participantChildren = new ArrayList<Node>();
        for (var jid : participants) {
            var toNode = new NodeBuilder()
                    .description("to")
                    .attribute("jid", jid)
                    .build();
            participantChildren.add(toNode);
        }
        var participantsNode = new NodeBuilder()
                .description("participants")
                .content(participantChildren)
                .build();
        children.add(participantsNode);
        if (useAdAccount) {
            children.add(new NodeBuilder().description("use_ad_account").build());
        }
        if (skipDedupe) {
            children.add(new NodeBuilder().description("skip_dedupe").build());
        }
        if (offerId != null) {
            var offerNode = new NodeBuilder()
                    .description("offer")
                    .attribute("id", offerId)
                    .build();
            children.add(offerNode);
        }
        if (!pendingCampaigns.isEmpty()) {
            var campaignChildren = new ArrayList<Node>();
            for (var entry : pendingCampaigns) {
                campaignChildren.add(entry.toNode());
            }
            var pendingNode = new NodeBuilder()
                    .description("pending_campaigns")
                    .content(campaignChildren)
                    .build();
            children.add(pendingNode);
        }
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "w:biz")
                .attribute("type", "get")
                .content(children);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxGetSMBMeteredMessagingCheckoutRequest) obj;
        return this.useAdAccount == that.useAdAccount
                && this.skipDedupe == that.skipDedupe
                && Objects.equals(this.participants, that.participants)
                && Objects.equals(this.offerId, that.offerId)
                && Objects.equals(this.pendingCampaigns, that.pendingCampaigns);
    }

    @Override
    public int hashCode() {
        return Objects.hash(participants, useAdAccount, skipDedupe, offerId, pendingCampaigns);
    }

    @Override
    public String toString() {
        return "SmaxGetSMBMeteredMessagingCheckoutRequest[participants=" + participants
                + ", useAdAccount=" + useAdAccount
                + ", skipDedupe=" + skipDedupe
                + ", offerId=" + offerId
                + ", pendingCampaigns=" + pendingCampaigns + ']';
    }

    /**
     * Fluent builder for {@link SmaxGetSMBMeteredMessagingCheckoutRequest}.
     *
     * <p>Mandatory input is at least one participant via
     * {@link #addParticipant(Jid)}; every other setter is optional.
     * Call {@link #build()} when the desired toggles have been
     * chosen.
     */
    public static final class Builder {
        /**
         * The accumulating participant list.
         */
        private final List<Jid> participants = new ArrayList<>();

        /**
         * The accumulating pending-campaigns list.
         */
        private final List<SmaxGetSMBMeteredMessagingCheckoutPendingCampaign> pendingCampaigns = new ArrayList<>();

        /**
         * The use-ad-account toggle.
         */
        private boolean useAdAccount;

        /**
         * The skip-dedupe toggle.
         */
        private boolean skipDedupe;

        /**
         * The optional offer id.
         */
        private String offerId;

        /**
         * Constructs a fresh builder. Prefer
         * {@link SmaxGetSMBMeteredMessagingCheckoutRequest#builder()} as the canonical entry point.
         */
        public Builder() {
        }

        /**
         * Appends a single participant JID.
         *
         * @param jid the recipient JID; never {@code null}
         * @return this builder
         * @throws NullPointerException if {@code jid} is
         *                              {@code null}
         */
        public Builder addParticipant(Jid jid) {
            Objects.requireNonNull(jid, "jid cannot be null");
            this.participants.add(jid);
            return this;
        }

        /**
         * Appends every JID in the supplied list.
         *
         * @param entries the JIDs to append; never {@code null}
         * @return this builder
         * @throws NullPointerException if {@code entries} or any
         *                              entry is {@code null}
         */
        public Builder addParticipants(List<Jid> entries) {
            Objects.requireNonNull(entries, "entries cannot be null");
            for (var entry : entries) {
                addParticipant(entry);
            }
            return this;
        }

        /**
         * Sets the use-ad-account toggle.
         *
         * @param flag the desired flag value
         * @return this builder
         */
        public Builder useAdAccount(boolean flag) {
            this.useAdAccount = flag;
            return this;
        }

        /**
         * Sets the skip-dedupe toggle.
         *
         * @param flag the desired flag value
         * @return this builder
         */
        public Builder skipDedupe(boolean flag) {
            this.skipDedupe = flag;
            return this;
        }

        /**
         * Sets the optional offer ID.
         *
         * @param offerId the offer ID; may be {@code null}
         * @return this builder
         */
        public Builder offerId(String offerId) {
            this.offerId = offerId;
            return this;
        }

        /**
         * Appends a pending-campaign entry.
         *
         * @param entry the entry; never {@code null}
         * @return this builder
         * @throws NullPointerException if {@code entry} is
         *                              {@code null}
         */
        public Builder addPendingCampaign(SmaxGetSMBMeteredMessagingCheckoutPendingCampaign entry) {
            Objects.requireNonNull(entry, "entry cannot be null");
            this.pendingCampaigns.add(entry);
            return this;
        }

        /**
         * Materialises a {@link SmaxGetSMBMeteredMessagingCheckoutRequest}.
         *
         * @return the constructed request; never {@code null}
         * @throws IllegalArgumentException when no participants
         *                                  were added or when
         *                                  participant / pending
         *                                  campaign caps are
         *                                  exceeded
         */
        public SmaxGetSMBMeteredMessagingCheckoutRequest build() {
            return new SmaxGetSMBMeteredMessagingCheckoutRequest(participants, useAdAccount, skipDedupe, offerId, pendingCampaigns);
        }
    }
}
