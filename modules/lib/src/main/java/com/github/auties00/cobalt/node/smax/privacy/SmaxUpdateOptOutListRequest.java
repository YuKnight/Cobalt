package com.github.auties00.cobalt.node.smax.privacy;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.jid.JidServer;
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
 * The outbound stanza variant.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutBlocklistsUpdateOptOutListRequest")
public final class SmaxUpdateOptOutListRequest implements SmaxOperation.Request {
    /**
     * The target business JID being opted in or out of marketing
     * messages.
     */
    private final Jid itemJid;

    /**
     * The category string scoping the opt-out (e.g. one of the
     * marketing-messages user-controls categories).
     */
    private final String itemCategory;

    /**
     * The action string ({@code "in"}/{@code "out"}/{@code "signup"}
     * etc., per the WA Web user-controls flows).
     */
    private final String itemAction;

    /**
     * The optional client-side digest of the cached opt-out list.
     */
    private final String itemDhash;

    /**
     * The optional reason marker. Surfaced through the marketing
     * messages user-controls reasons enum.
     */
    private final String itemReason;

    /**
     * The optional entry-point marker. The user-controls entry-point
     * enum.
     */
    private final String itemEntryPoint;

    /**
     * The optional signup id. Set when the action originates from
     * a marketing-message signup.
     */
    private final String itemSignupId;

    /**
     * The optional opt-out duration in seconds.
     */
    private final Integer itemDuration;

    /**
     * Constructs a new request.
     *
     * @param itemJid        the target business JID; never
     *                       {@code null}
     * @param itemCategory   the category string; never {@code null}
     * @param itemAction     the action string; never {@code null}
     * @param itemDhash      the optional cached digest; may be
     *                       {@code null}
     * @param itemReason     the optional reason; may be {@code null}
     * @param itemEntryPoint the optional entry-point; may be
     *                       {@code null}
     * @param itemSignupId   the optional signup id; may be
     *                       {@code null}
     * @param itemDuration   the optional duration; may be {@code null}
     * @throws NullPointerException if {@code itemJid}, {@code itemCategory},
     *                              or {@code itemAction} is {@code null}
     */
    public SmaxUpdateOptOutListRequest(Jid itemJid, String itemCategory, String itemAction,
                   String itemDhash, String itemReason, String itemEntryPoint,
                   String itemSignupId, Integer itemDuration) {
        this.itemJid = Objects.requireNonNull(itemJid, "itemJid cannot be null");
        this.itemCategory = Objects.requireNonNull(itemCategory, "itemCategory cannot be null");
        this.itemAction = Objects.requireNonNull(itemAction, "itemAction cannot be null");
        this.itemDhash = itemDhash;
        this.itemReason = itemReason;
        this.itemEntryPoint = itemEntryPoint;
        this.itemSignupId = itemSignupId;
        this.itemDuration = itemDuration;
    }

    /**
     * Returns the target business JID.
     *
     * @return the JID; never {@code null}
     */
    public Jid itemJid() {
        return itemJid;
    }

    /**
     * Returns the category string.
     *
     * @return the category; never {@code null}
     */
    public String itemCategory() {
        return itemCategory;
    }

    /**
     * Returns the action string.
     *
     * @return the action; never {@code null}
     */
    public String itemAction() {
        return itemAction;
    }

    /**
     * Returns the optional cached digest.
     *
     * @return an {@link Optional} carrying the digest, or empty when
     *         omitted
     */
    public Optional<String> itemDhash() {
        return Optional.ofNullable(itemDhash);
    }

    /**
     * Returns the optional reason marker.
     *
     * @return an {@link Optional} carrying the reason, or empty when
     *         omitted
     */
    public Optional<String> itemReason() {
        return Optional.ofNullable(itemReason);
    }

    /**
     * Returns the optional entry-point marker.
     *
     * @return an {@link Optional} carrying the entry-point, or empty
     *         when omitted
     */
    public Optional<String> itemEntryPoint() {
        return Optional.ofNullable(itemEntryPoint);
    }

    /**
     * Returns the optional signup id.
     *
     * @return an {@link Optional} carrying the signup id, or empty
     *         when omitted
     */
    public Optional<String> itemSignupId() {
        return Optional.ofNullable(itemSignupId);
    }

    /**
     * Returns the optional opt-out duration.
     *
     * @return an {@link Optional} carrying the duration in seconds,
     *         or empty when omitted
     */
    public Optional<Integer> itemDuration() {
        return Optional.ofNullable(itemDuration);
    }

    /**
     * Builds the outbound IQ stanza.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutBlocklistsUpdateOptOutListRequest",
            exports = "makeUpdateOptOutListRequest", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        //   {jid, category, action, dhash?, reason?, entry_point?, signup_id?, duration?})
        var itemBuilder = new NodeBuilder()
                .description("item")
                .attribute("jid", itemJid)
                .attribute("category", itemCategory)
                .attribute("action", itemAction);
        if (itemDhash != null) {
            itemBuilder.attribute("dhash", itemDhash);
        }
        if (itemReason != null) {
            itemBuilder.attribute("reason", itemReason);
        }
        if (itemEntryPoint != null) {
            itemBuilder.attribute("entry_point", itemEntryPoint);
        }
        if (itemSignupId != null) {
            itemBuilder.attribute("signup_id", itemSignupId);
        }
        if (itemDuration != null) {
            itemBuilder.attribute("duration", itemDuration.intValue());
        }
        //   {to: S_WHATSAPP_NET, xmlns: "optoutlist", type: "set", id: generateId()})
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "optoutlist")
                .attribute("to", JidServer.user())
                .attribute("type", "set")
                .content(itemBuilder.build());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxUpdateOptOutListRequest) obj;
        return Objects.equals(this.itemJid, that.itemJid)
                && Objects.equals(this.itemCategory, that.itemCategory)
                && Objects.equals(this.itemAction, that.itemAction)
                && Objects.equals(this.itemDhash, that.itemDhash)
                && Objects.equals(this.itemReason, that.itemReason)
                && Objects.equals(this.itemEntryPoint, that.itemEntryPoint)
                && Objects.equals(this.itemSignupId, that.itemSignupId)
                && Objects.equals(this.itemDuration, that.itemDuration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemJid, itemCategory, itemAction, itemDhash, itemReason,
                itemEntryPoint, itemSignupId, itemDuration);
    }

    @Override
    public String toString() {
        return "SmaxUpdateOptOutListRequest[itemJid=" + itemJid
                + ", itemCategory=" + itemCategory
                + ", itemAction=" + itemAction
                + ", itemDhash=" + itemDhash
                + ", itemReason=" + itemReason
                + ", itemEntryPoint=" + itemEntryPoint
                + ", itemSignupId=" + itemSignupId
                + ", itemDuration=" + itemDuration + ']';
    }
}
