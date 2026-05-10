package com.github.auties00.cobalt.node.iq.disappearing;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.iq.IqOperation;
import java.time.Duration;
import java.util.Objects;

/**
 * The outbound stanza variant — wraps a single
 * {@code <disappearing_mode duration=SECONDS/>} child in the
 * canonical {@code <iq xmlns="disappearing_mode" type="set"/>}
 * envelope.
 */
@WhatsAppWebModule(moduleName = "WAWebSetDisappearingModeJob")
public final class IqSetDisappearingModeRequest implements IqOperation.Request {
    /**
     * The new default disappearing-mode duration. {@link Duration#ZERO}
     * disables the feature.
     */
    private final Duration duration;

    /**
     * Constructs a new request.
     *
     * @param duration the new default duration; never {@code null}
     * @throws NullPointerException     if {@code duration} is
     *                                  {@code null}
     * @throws IllegalArgumentException if {@code duration} is
     *                                  negative
     */
    public IqSetDisappearingModeRequest(Duration duration) {
        Objects.requireNonNull(duration, "duration cannot be null");
        if (duration.isNegative()) {
            throw new IllegalArgumentException("duration cannot be negative");
        }
        this.duration = duration;
    }

    /**
     * Returns the new default duration.
     *
     * @return the duration; never {@code null}
     */
    public Duration duration() {
        return duration;
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and
     *         the {@code <disappearing_mode>} payload
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebSetDisappearingModeJob",
            exports = "setDisappearingMode",
            adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        // WAWebSetDisappearingModeJob: wap("disappearing_mode",{duration:CUSTOM_STRING(String(t))})
        var dmNode = new NodeBuilder()
                .description("disappearing_mode")
                .attribute("duration", String.valueOf(duration.toSeconds()))
                .build();
        // WAWebSetDisappearingModeJob: wap("iq",{xmlns:"disappearing_mode",to:S_WHATSAPP_NET,type:"set",id}, ...)
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "disappearing_mode")
                .attribute("to", JidServer.user())
                .attribute("type", "set")
                .content(dmNode);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (IqSetDisappearingModeRequest) obj;
        return Objects.equals(this.duration, that.duration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(duration);
    }

    @Override
    public String toString() {
        return "IqSetDisappearingModeRequest[duration=" + duration + ']';
    }
}
