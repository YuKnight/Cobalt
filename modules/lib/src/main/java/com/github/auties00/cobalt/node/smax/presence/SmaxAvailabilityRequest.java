package com.github.auties00.cobalt.node.smax.presence;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import java.util.Objects;
import java.util.Optional;

/**
 * The outbound stanza variant — wraps the optional {@code type}
 * (e.g. {@code "available"} or {@code "unavailable"}) and {@code name}
 * (the local push-name) attributes into the bare
 * {@code <presence/>} envelope.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutPresenceAvailabilityRequest")
public final class SmaxAvailabilityRequest implements SmaxOperation.Request {
    /**
     * The optional presence type (e.g. {@code "available"} /
     * {@code "unavailable"}). When {@code null} the relay treats the
     * stanza as a pure name update.
     */
    private final String presenceType;

    /**
     * The optional push-name to advertise. When {@code null} the
     * relay reuses the previously-broadcast value.
     */
    private final String presenceName;

    /**
     * Constructs a new availability broadcast.
     *
     * @param presenceType the optional presence type; may be
     *                     {@code null}
     * @param presenceName the optional push-name; may be {@code null}
     */
    public SmaxAvailabilityRequest(String presenceType, String presenceName) {
        this.presenceType = presenceType;
        this.presenceName = presenceName;
    }

    /**
     * Returns the optional presence type.
     *
     * @return an {@link Optional} carrying the type, or empty when
     *         omitted
     */
    public Optional<String> presenceType() {
        return Optional.ofNullable(presenceType);
    }

    /**
     * Returns the optional push-name.
     *
     * @return an {@link Optional} carrying the name, or empty when
     *         omitted
     */
    public Optional<String> presenceName() {
        return Optional.ofNullable(presenceName);
    }

    /**
     * Builds the outbound presence stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the
     *         {@code <presence type? name?/>} envelope
     *
     * @implNote {@code WASmaxOutPresenceAvailabilityRequest.makeAvailabilityRequest}
     *           emits {@code smax("presence", {type:OPTIONAL,
     *           name:OPTIONAL})}; both attributes are dropped when
     *           the caller passes {@code null}.
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutPresenceAvailabilityRequest",
            exports = "makeAvailabilityRequest", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        return new NodeBuilder()
                .description("presence")
                .attribute("type", presenceType)
                .attribute("name", presenceName);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxAvailabilityRequest) obj;
        return Objects.equals(this.presenceType, that.presenceType)
                && Objects.equals(this.presenceName, that.presenceName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(presenceType, presenceName);
    }

    @Override
    public String toString() {
        return "SmaxAvailabilityRequest[presenceType=" + presenceType
                + ", presenceName=" + presenceName + ']';
    }
}
