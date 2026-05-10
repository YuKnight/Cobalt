package com.github.auties00.cobalt.node.smax.privacy;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import java.util.Objects;
import java.util.Optional;

/**
 * The outbound stanza variant. Wraps a single {@code <item>} child
 * carrying the marketing-message opt-out parameters in the canonical
 * {@code <iq xmlns="optoutlist" type="set">} envelope addressed at
 * {@code s.whatsapp.net}. Three of the eight item attributes
 * ({@code jid}, {@code category}, {@code action}) are mandatory; the
 * remaining five ({@code dhash}, {@code reason}, {@code entry_point},
 * {@code signup_id}, {@code duration}) are optional and are emitted
 * only when supplied. The {@code id} attribute is generated downstream
 * by {@code WhatsAppClient.sendNode} via the same path used by every
 * other SMAX request — Cobalt does not call {@code WAWap.generateId}
 * inline because dispatch ownership is centralised on the client.
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
        // WASmaxOutBlocklistsUpdateBlockListRequest.makeUpdateOptOutListRequest:
        // smax("item", { jid: USER_JID(r), category: CUSTOM_STRING(a), action: CUSTOM_STRING(i),
        //                dhash: OPTIONAL(CUSTOM_STRING, l), reason: OPTIONAL(CUSTOM_STRING, s),
        //                entry_point: OPTIONAL(CUSTOM_STRING, u), signup_id: OPTIONAL(CUSTOM_STRING, c),
        //                duration: OPTIONAL(INT, d) })
        // USER_JID is a thin wrapper over WAWap.JID, and Cobalt's
        // NodeBuilder.attribute(String, JidProvider) emits the wap-encoded JID directly.
        var itemBuilder = new NodeBuilder()
                .description("item")
                .attribute("jid", itemJid)
                .attribute("category", itemCategory)
                .attribute("action", itemAction);
        if (itemDhash != null) {
            // dhash: OPTIONAL(CUSTOM_STRING, l)
            itemBuilder.attribute("dhash", itemDhash);
        }
        if (itemReason != null) {
            // reason: OPTIONAL(CUSTOM_STRING, s)
            itemBuilder.attribute("reason", itemReason);
        }
        if (itemEntryPoint != null) {
            // entry_point: OPTIONAL(CUSTOM_STRING, u)
            itemBuilder.attribute("entry_point", itemEntryPoint);
        }
        if (itemSignupId != null) {
            // signup_id: OPTIONAL(CUSTOM_STRING, c)
            itemBuilder.attribute("signup_id", itemSignupId);
        }
        if (itemDuration != null) {
            // duration: OPTIONAL(INT, d) — WAWap.INT serialises the integer as a numeric attribute;
            // NodeBuilder.attribute(String, int) forwards through the same Number coercion path.
            itemBuilder.attribute("duration", itemDuration.intValue());
        }
        // WASmaxOutBlocklistsUpdateBlockListRequest.makeUpdateOptOutListRequest:
        // smax("iq", { to: S_WHATSAPP_NET, xmlns: "optoutlist", type: "set", id: generateId() }, <item .../>)
        // The id attribute is generated downstream by WhatsAppClient.sendNode — Cobalt centralises
        // generateId() on the client so every SMAX request omits it from toNode().
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
