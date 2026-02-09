package com.github.auties00.cobalt.model.chat;

import java.util.Objects;
import java.util.Optional;

/**
 * Tracks the Click-to-WhatsApp (CTWA) entry point for a chat.
 *
 * <p>When a user opens a chat through a CTWA ad link, the entry point
 * metadata is recorded so it can be included in the first message's
 * {@code <ctwa_attribution>} stanza node.
 *
 * @apiNote WAWebExternalEntryPointPrefs.getExternalEntryPoint: retrieves
 * the stored entry point for a chat.
 * WAWebSendMsgCtwaAttributionNode: uses the entry point to build the
 * attribution JSON payload.
 */
public final class CtwaEntryPoint {
    private final String deepLinkType;
    private final boolean authSuccess;
    private final String partnerName;

    public CtwaEntryPoint(String deepLinkType, boolean authSuccess, String partnerName) {
        this.deepLinkType = Objects.requireNonNull(deepLinkType, "deepLinkType");
        this.authSuccess = authSuccess;
        this.partnerName = partnerName;
    }

    /**
     * Returns the deep link type (e.g. {@code "wa_ads"}, {@code "fb_ads"}).
     *
     * @return the link type, never {@code null}
     *
     * @apiNote WAWebExternalEntryPointPrefs: {@code deepLinkType} field.
     */
    public String deepLinkType() {
        return deepLinkType;
    }

    /**
     * Returns whether the CTWA auth flow succeeded.
     *
     * @return {@code true} if auth was successful
     *
     * @apiNote WAWebSendMsgCtwaAttributionNode: when {@code false},
     * includes {@code "s": 0} in the JSON payload.
     */
    public boolean authSuccess() {
        return authSuccess;
    }

    /**
     * Returns the partner (advertiser) name, if available.
     *
     * @return the partner name, or empty
     *
     * @apiNote WAWebSendMsgCtwaAttributionNode: included as
     * {@code "p": partnerName} in the JSON payload.
     */
    public Optional<String> partnerName() {
        return Optional.ofNullable(partnerName);
    }

    @Override
    public String toString() {
        return "CtwaEntryPoint[" +
                "deepLinkType=" + deepLinkType +
                ", authSuccess=" + authSuccess +
                ", partnerName=" + partnerName +
                ']';
    }
}
