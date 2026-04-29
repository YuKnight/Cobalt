package com.github.auties00.cobalt.node.smax.pushconfig;

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
 * Sealed family of payload variants — either a {@link Config}
 * registration or a {@link Clear} de-registration.
 *
 * @implNote {@code WASmaxOutPushConfigSetSetConfigOrSetClearMixinGroup.mergeSetSetConfigOrSetClearMixinGroup}
 *           switches over the {@code (setSetConfig, setClear)}
 *           pair; Cobalt models the disjunction as a sealed
 *           interface.
 */
public sealed interface SmaxPushConfigSetSetVariant
        permits SmaxPushConfigSetSetVariant.Config, SmaxPushConfigSetSetVariant.Clear {

    /**
     * Builds the {@code <config>} or {@code <clear/>} child node.
     *
     * @return the {@link Node}
     */
    Node toNode();

    /**
     * The {@code <config>} variant — registers the push channel
     * for a specific client family.
     *
     * <p>Carries exactly one of the platform-specific config
     * mixins ({@link FbConfig}, {@link AppleConfig},
     * {@link AndroidConfig}, {@link WnsConfig},
     * {@link EnterpriseConfig}, {@link WebConfig}).
     */
    @WhatsAppWebModule(moduleName = "WASmaxOutPushConfigSetSetConfigMixin")
    @WhatsAppWebModule(moduleName = "WASmaxOutPushConfigConfigMixins")
    final class Config implements SmaxPushConfigSetSetVariant {
        /**
         * The platform-specific config payload.
         */
        private final SmaxPushConfigSetConfigVariant config;

        /**
         * Constructs a new config variant.
         *
         * @param config the platform-specific config; never
         *               {@code null}
         * @throws NullPointerException if {@code config} is
         *                              {@code null}
         */
        public Config(SmaxPushConfigSetConfigVariant config) {
            this.config = Objects.requireNonNull(config, "config cannot be null");
        }

        /**
         * Returns the platform-specific config payload.
         *
         * @return the payload; never {@code null}
         */
        public SmaxPushConfigSetConfigVariant config() {
            return config;
        }

        /**
         * Builds the {@code <config>} child node.
         *
         * @return the {@link Node}
         */
        @Override
        @WhatsAppWebExport(moduleName = "WASmaxOutPushConfigSetSetConfigMixin",
                exports = "mergeSetSetConfigMixin",
                adaptation = WhatsAppAdaptation.DIRECT)
        public Node toNode() {
            return config.toNode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (Config) obj;
            return Objects.equals(this.config, that.config);
        }

        @Override
        public int hashCode() {
            return Objects.hash(config);
        }

        @Override
        public String toString() {
            return "SmaxPushConfigSetSetVariant.Config[config=" + config + ']';
        }
    }

    /**
     * The {@code <clear>} variant — drops the push registration
     * entirely.
     */
    @WhatsAppWebModule(moduleName = "WASmaxOutPushConfigSetClearMixin")
    final class Clear implements SmaxPushConfigSetSetVariant {
        /**
         * The optional platform scope ({@code "fb"} / {@code "apple"}
         * / {@code "android"} / {@code "wns"} / {@code "ent"} /
         * {@code "web"}).
         */
        private final String clearPlatform;

        /**
         * Constructs a new clear variant.
         *
         * @param clearPlatform the optional platform scope; may be
         *                      {@code null}
         */
        public Clear(String clearPlatform) {
            this.clearPlatform = clearPlatform;
        }

        /**
         * Returns the optional platform scope.
         *
         * @return an {@link Optional} carrying the scope
         */
        public Optional<String> clearPlatform() {
            return Optional.ofNullable(clearPlatform);
        }

        /**
         * Builds the {@code <clear platform?/>} child node.
         *
         * @return the {@link Node}
         */
        @Override
        @WhatsAppWebExport(moduleName = "WASmaxOutPushConfigSetClearMixin",
                exports = "mergeSetClearMixin",
                adaptation = WhatsAppAdaptation.DIRECT)
        public Node toNode() {
            var builder = new NodeBuilder()
                    .description("clear");
            if (clearPlatform != null) {
                builder.attribute("platform", clearPlatform);
            }
            return builder.build();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (Clear) obj;
            return Objects.equals(this.clearPlatform, that.clearPlatform);
        }

        @Override
        public int hashCode() {
            return Objects.hash(clearPlatform);
        }

        @Override
        public String toString() {
            return "SmaxPushConfigSetSetVariant.Clear[clearPlatform=" + clearPlatform + ']';
        }
    }
}
