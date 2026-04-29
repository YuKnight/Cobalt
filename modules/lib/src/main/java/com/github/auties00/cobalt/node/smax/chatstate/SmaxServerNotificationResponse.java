package com.github.auties00.cobalt.node.smax.chatstate;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import java.util.Objects;
import java.util.Optional;

/**
 * The inbound projection of the {@code <chatstate/>} stanza.
 */
@WhatsAppWebModule(moduleName = "WASmaxInChatstateServerNotificationRequest")
public final class SmaxServerNotificationResponse implements SmaxOperation.Response {
    /**
     * The state-source disjunction (user vs. group); never
     * {@code null}.
     */
    private final SmaxServerNotificationStateSource stateSource;

    /**
     * The state-type disjunction (composing vs. paused); never
     * {@code null}.
     */
    private final SmaxServerNotificationStateType stateType;

    /**
     * The optional {@code testConfig} attribute carried by the
     * dev-only {@code <test config="…"/>} child; may be
     * {@code null}.
     */
    private final String testConfig;

    /**
     * Constructs a new server-notification projection.
     *
     * @param stateSource the source disjunction; never
     *                    {@code null}
     * @param stateType   the state-type disjunction; never
     *                    {@code null}
     * @param testConfig  the optional dev-only test config; may
     *                    be {@code null}
     * @throws NullPointerException if {@code stateSource} or
     *                              {@code stateType} is
     *                              {@code null}
     */
    public SmaxServerNotificationResponse(SmaxServerNotificationStateSource stateSource, SmaxServerNotificationStateType stateType, String testConfig) {
        this.stateSource = Objects.requireNonNull(stateSource, "stateSource cannot be null");
        this.stateType = Objects.requireNonNull(stateType, "stateType cannot be null");
        this.testConfig = testConfig;
    }

    /**
     * Returns the state-source disjunction.
     *
     * @return the source; never {@code null}
     */
    public SmaxServerNotificationStateSource stateSource() {
        return stateSource;
    }

    /**
     * Returns the state-type disjunction.
     *
     * @return the type; never {@code null}
     */
    public SmaxServerNotificationStateType stateType() {
        return stateType;
    }

    /**
     * Returns the optional dev-only {@code testConfig}.
     *
     * @return an {@link Optional} carrying the value, or empty
     *         when the relay omitted the {@code <test/>} child
     */
    public Optional<String> testConfig() {
        return Optional.ofNullable(testConfig);
    }

    /**
     * Tries to parse a {@link SmaxServerNotificationResponse} projection from the given
     * stanza.
     *
     * @param node the inbound {@code <chatstate/>} stanza; never
     *             {@code null}
     * @return an {@link Optional} carrying the projection, or
     *         {@link Optional#empty()} when the stanza does not
     *         match the documented shape
     * @throws NullPointerException if {@code node} is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WASmaxChatstateServerNotificationRPC",
            exports = "receiveServerNotificationRPC",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInChatstateServerNotificationRequest",
            exports = "parseServerNotificationRequest",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public static Optional<SmaxServerNotificationResponse> of(Node node) {
        Objects.requireNonNull(node, "node cannot be null");
        if (!node.hasDescription("chatstate")) {
            return Optional.empty();
        }
        var stateSource = SmaxServerNotificationStateSource.of(node).orElse(null);
        if (stateSource == null) {
            return Optional.empty();
        }
        var stateType = SmaxServerNotificationStateType.of(node).orElse(null);
        if (stateType == null) {
            return Optional.empty();
        }
        var testConfig = node.getChild("test")
                .flatMap(testNode -> testNode.getAttributeAsString("config"))
                .orElse(null);
        return Optional.of(new SmaxServerNotificationResponse(stateSource, stateType, testConfig));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxServerNotificationResponse) obj;
        return Objects.equals(this.stateSource, that.stateSource)
                && Objects.equals(this.stateType, that.stateType)
                && Objects.equals(this.testConfig, that.testConfig);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stateSource, stateType, testConfig);
    }

    @Override
    public String toString() {
        return "SmaxServerNotificationResponse[stateSource=" + stateSource
                + ", stateType=" + stateType
                + ", testConfig=" + testConfig + ']';
    }
}
