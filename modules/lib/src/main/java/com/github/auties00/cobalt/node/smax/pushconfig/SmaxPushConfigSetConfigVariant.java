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
 * Sealed family of platform-specific {@code <config>} payloads.
 *
 * @implNote {@code WASmaxOutPushConfigConfigMixins.mergeConfigMixins}
 *           switches over the
 *           {@code (fBClient, androidClient, appleClient, wNSClient,
 *           enterpriseClient, webClient)} sextet.
 */
public sealed interface SmaxPushConfigSetConfigVariant
        permits SmaxPushConfigSetConfigVariant.FbConfig, SmaxPushConfigSetConfigVariant.AndroidConfig,
        SmaxPushConfigSetConfigVariant.AppleConfig, SmaxPushConfigSetConfigVariant.WnsConfig,
        SmaxPushConfigSetConfigVariant.EnterpriseConfig, SmaxPushConfigSetConfigVariant.WebConfig {

    /**
     * Builds the {@code <config platform=…>} child node.
     *
     * @return the {@link Node}
     */
    Node toNode();

    /**
     * The Facebook-client {@code <config platform="fb">} variant.
     */
    @WhatsAppWebModule(moduleName = "WASmaxOutPushConfigFBClientMixin")
    final class FbConfig implements SmaxPushConfigSetConfigVariant {
        /**
         * The {@code appid} attribute.
         */
        private final String configAppid;

        /**
         * The {@code deviceid} attribute.
         */
        private final String configDeviceid;

        /**
         * The optional {@code fbid} attribute.
         */
        private final String configFbid;

        /**
         * Constructs a new FB-client config.
         *
         * @param configAppid    the appid; never {@code null}
         * @param configDeviceid the device id; never {@code null}
         * @param configFbid     the optional FB id; may be
         *                       {@code null}
         * @throws NullPointerException if any required argument is
         *                              {@code null}
         */
        public FbConfig(String configAppid, String configDeviceid, String configFbid) {
            this.configAppid = Objects.requireNonNull(configAppid, "configAppid cannot be null");
            this.configDeviceid = Objects.requireNonNull(configDeviceid, "configDeviceid cannot be null");
            this.configFbid = configFbid;
        }

        /**
         * Returns the appid.
         *
         * @return the appid; never {@code null}
         */
        public String configAppid() {
            return configAppid;
        }

        /**
         * Returns the device id.
         *
         * @return the device id; never {@code null}
         */
        public String configDeviceid() {
            return configDeviceid;
        }

        /**
         * Returns the optional FB id.
         *
         * @return an {@link Optional} carrying the FB id
         */
        public Optional<String> configFbid() {
            return Optional.ofNullable(configFbid);
        }

        /**
         * Builds the {@code <config platform="fb">} node.
         *
         * @return the {@link Node}
         */
        @Override
        @WhatsAppWebExport(moduleName = "WASmaxOutPushConfigFBClientMixin",
                exports = "mergeFBClientMixin",
                adaptation = WhatsAppAdaptation.DIRECT)
        public Node toNode() {
            var builder = new NodeBuilder()
                    .description("config")
                    .attribute("platform", "fb")
                    .attribute("appid", configAppid)
                    .attribute("deviceid", configDeviceid);
            if (configFbid != null) {
                builder.attribute("fbid", configFbid);
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
            var that = (FbConfig) obj;
            return Objects.equals(this.configAppid, that.configAppid)
                    && Objects.equals(this.configDeviceid, that.configDeviceid)
                    && Objects.equals(this.configFbid, that.configFbid);
        }

        @Override
        public int hashCode() {
            return Objects.hash(configAppid, configDeviceid, configFbid);
        }

        @Override
        public String toString() {
            return "SmaxPushConfigSetConfigVariant.FbConfig[configAppid=" + configAppid
                    + ", configDeviceid=" + configDeviceid
                    + ", configFbid=" + configFbid + ']';
        }
    }

    /**
     * The Android-client {@code <config>} variant — wraps a list of
     * per-group mute items.
     *
     * @implNote {@code WASmaxOutPushConfigAndroidClientMixin.mergeAndroidClientMixin}
     *           emits a bare {@code <config>} carrying
     *           {@code REPEATED_CHILD(item, 0, ∞)} of
     *           {@code <item jid=GROUP_JID mute=INT/>} and then
     *           merges in
     *           {@code WASmaxOutPushConfigAndroidClientConfigMixin}
     *           for the trailing platform-specific attributes.
     */
    @WhatsAppWebModule(moduleName = "WASmaxOutPushConfigAndroidClientMixin")
    @WhatsAppWebModule(moduleName = "WASmaxOutPushConfigAndroidClientConfigMixin")
    final class AndroidConfig implements SmaxPushConfigSetConfigVariant {
        /**
         * The list of {@code <item jid mute/>} mute entries.
         */
        private final List<AndroidMuteItem> itemArgs;

        /**
         * Constructs a new Android-client config.
         *
         * @param itemArgs the mute items; never {@code null}
         * @throws NullPointerException if {@code itemArgs} is
         *                              {@code null}
         */
        public AndroidConfig(List<AndroidMuteItem> itemArgs) {
            Objects.requireNonNull(itemArgs, "itemArgs cannot be null");
            this.itemArgs = List.copyOf(itemArgs);
        }

        /**
         * Returns the mute items.
         *
         * @return an unmodifiable list; never {@code null}
         */
        public List<AndroidMuteItem> itemArgs() {
            return itemArgs;
        }

        /**
         * Builds the {@code <config>} child node.
         *
         * @return the {@link Node}
         */
        @Override
        @WhatsAppWebExport(moduleName = "WASmaxOutPushConfigAndroidClientMixin",
                exports = "mergeAndroidClientMixin",
                adaptation = WhatsAppAdaptation.DIRECT)
        public Node toNode() {
            var children = new ArrayList<Node>(itemArgs.size());
            for (var item : itemArgs) {
                children.add(item.toNode());
            }
            return new NodeBuilder()
                    .description("config")
                    .content(children)
                    .build();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (AndroidConfig) obj;
            return Objects.equals(this.itemArgs, that.itemArgs);
        }

        @Override
        public int hashCode() {
            return Objects.hash(itemArgs);
        }

        @Override
        public String toString() {
            return "SmaxPushConfigSetConfigVariant.AndroidConfig[itemArgs=" + itemArgs + ']';
        }

        /**
         * A single {@code <item jid=GROUP_JID mute=INT/>} mute
         * entry carried by an {@link AndroidConfig}.
         */
        public static final class AndroidMuteItem {
            /**
             * The group JID being muted.
             */
            private final Jid itemJid;

            /**
             * The numeric mute marker.
             */
            private final long itemMute;

            /**
             * Constructs a new mute item.
             *
             * @param itemJid  the group JID; never {@code null}
             * @param itemMute the mute marker
             * @throws NullPointerException if {@code itemJid} is
             *                              {@code null}
             */
            public AndroidMuteItem(Jid itemJid, long itemMute) {
                this.itemJid = Objects.requireNonNull(itemJid, "itemJid cannot be null");
                this.itemMute = itemMute;
            }

            /**
             * Returns the group JID.
             *
             * @return the JID; never {@code null}
             */
            public Jid itemJid() {
                return itemJid;
            }

            /**
             * Returns the mute marker.
             *
             * @return the marker
             */
            public long itemMute() {
                return itemMute;
            }

            /**
             * Builds the {@code <item jid mute/>} child node.
             *
             * @return the {@link Node}
             */
            @WhatsAppWebExport(moduleName = "WASmaxOutPushConfigAndroidClientMixin",
                    exports = "makeAndroidClientItem",
                    adaptation = WhatsAppAdaptation.DIRECT)
            public Node toNode() {
                return new NodeBuilder()
                        .description("item")
                        .attribute("jid", itemJid)
                        .attribute("mute", itemMute)
                        .build();
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) {
                    return true;
                }
                if (obj == null || obj.getClass() != this.getClass()) {
                    return false;
                }
                var that = (AndroidMuteItem) obj;
                return this.itemMute == that.itemMute
                        && Objects.equals(this.itemJid, that.itemJid);
            }

            @Override
            public int hashCode() {
                return Objects.hash(itemJid, itemMute);
            }

            @Override
            public String toString() {
                return "SmaxPushConfigSetConfigVariant.AndroidConfig.AndroidMuteItem[itemJid=" + itemJid
                        + ", itemMute=" + itemMute + ']';
            }
        }
    }

    /**
     * The Apple-client {@code <config platform>} variant — a thick
     * record of APNs / iOS-specific attributes.
     *
     * @implNote {@code WASmaxOutPushConfigAppleClientMixin.mergeAppleClientMixin}
     *           carries 24 separate optional attributes plus a
     *           per-{@code item} list. Cobalt models the full
     *           shape verbatim.
     */
    @WhatsAppWebModule(moduleName = "WASmaxOutPushConfigAppleClientMixin")
    final class AppleConfig implements SmaxPushConfigSetConfigVariant {
        /**
         * The mandatory {@code platform} attribute (typically one
         * of {@code "iphone"}, {@code "ipad"}).
         */
        private final String configPlatform;

        /**
         * Whether the {@code version="2"} marker is set.
         */
        private final boolean hasConfigVersion2;

        /**
         * The optional {@code id} attribute.
         */
        private final String configId;

        /**
         * The optional {@code voip} attribute.
         */
        private final String configVoip;

        /**
         * The mandatory {@code preview} attribute.
         */
        private final String configPreview;

        /**
         * The mandatory {@code default} attribute.
         */
        private final String configDefault;

        /**
         * The mandatory {@code groups} attribute.
         */
        private final String configGroups;

        /**
         * The mandatory {@code call} attribute.
         */
        private final String configCall;

        /**
         * The optional {@code status_sound} attribute.
         */
        private final String configStatusSound;

        /**
         * The mandatory {@code lg} attribute.
         */
        private final String configLg;

        /**
         * The mandatory {@code lc} attribute.
         */
        private final String configLc;

        /**
         * The optional {@code background_location} attribute.
         */
        private final String configBackgroundLocation;

        /**
         * The optional {@code nse_ver} attribute.
         */
        private final String configNseVer;

        /**
         * The optional {@code nse_call} attribute.
         */
        private final String configNseCall;

        /**
         * The optional {@code nse_read} attribute.
         */
        private final String configNseRead;

        /**
         * The optional {@code nse_retry} attribute.
         */
        private final String configNseRetry;

        /**
         * The optional {@code reg_push} attribute.
         */
        private final String configRegPush;

        /**
         * The optional {@code pkey} attribute.
         */
        private final String configPkey;

        /**
         * The mandatory {@code voip_payload_type} attribute.
         */
        private final String configVoipPayloadType;

        /**
         * The optional {@code settings} attribute.
         */
        private final Long configSettings;

        /**
         * The optional {@code app_mute} attribute.
         */
        private final Long configAppMute;

        /**
         * The optional {@code apple_watch_id} attribute.
         */
        private final String configAppleWatchId;

        /**
         * The optional {@code apple_watch_pkey} attribute.
         */
        private final String configAppleWatchPkey;

        /**
         * The list of {@code <item>} entries.
         */
        private final List<AppleItem> itemArgs;

        /**
         * Constructs a new Apple-client config.
         *
         * @param configPlatform           the platform; never
         *                                 {@code null}
         * @param hasConfigVersion2        whether {@code version="2"}
         * @param configId                 the optional id
         * @param configVoip               the optional voip token
         * @param configPreview            the preview marker; never
         *                                 {@code null}
         * @param configDefault            the default marker; never
         *                                 {@code null}
         * @param configGroups             the groups marker; never
         *                                 {@code null}
         * @param configCall               the call marker; never
         *                                 {@code null}
         * @param configStatusSound        the optional status-sound
         *                                 marker
         * @param configLg                 the language; never
         *                                 {@code null}
         * @param configLc                 the locale; never
         *                                 {@code null}
         * @param configBackgroundLocation the optional bg-location
         *                                 marker
         * @param configNseVer             the optional NSE version
         * @param configNseCall            the optional NSE call
         *                                 marker
         * @param configNseRead            the optional NSE read
         *                                 marker
         * @param configNseRetry           the optional NSE retry
         *                                 marker
         * @param configRegPush            the optional reg-push
         *                                 marker
         * @param configPkey               the optional pkey
         * @param configVoipPayloadType    the voip payload type;
         *                                 never {@code null}
         * @param configSettings           the optional settings
         *                                 mask
         * @param configAppMute            the optional app-mute
         *                                 mask
         * @param configAppleWatchId       the optional Apple Watch
         *                                 id
         * @param configAppleWatchPkey     the optional Apple Watch
         *                                 pkey
         * @param itemArgs                 the per-item entries;
         *                                 never {@code null}
         * @throws NullPointerException if any required argument is
         *                              {@code null}
         */
        public AppleConfig(String configPlatform, boolean hasConfigVersion2,
                           String configId, String configVoip,
                           String configPreview, String configDefault, String configGroups,
                           String configCall, String configStatusSound,
                           String configLg, String configLc, String configBackgroundLocation,
                           String configNseVer, String configNseCall, String configNseRead,
                           String configNseRetry, String configRegPush, String configPkey,
                           String configVoipPayloadType, Long configSettings, Long configAppMute,
                           String configAppleWatchId, String configAppleWatchPkey,
                           List<AppleItem> itemArgs) {
            this.configPlatform = Objects.requireNonNull(configPlatform, "configPlatform cannot be null");
            this.hasConfigVersion2 = hasConfigVersion2;
            this.configId = configId;
            this.configVoip = configVoip;
            this.configPreview = Objects.requireNonNull(configPreview, "configPreview cannot be null");
            this.configDefault = Objects.requireNonNull(configDefault, "configDefault cannot be null");
            this.configGroups = Objects.requireNonNull(configGroups, "configGroups cannot be null");
            this.configCall = Objects.requireNonNull(configCall, "configCall cannot be null");
            this.configStatusSound = configStatusSound;
            this.configLg = Objects.requireNonNull(configLg, "configLg cannot be null");
            this.configLc = Objects.requireNonNull(configLc, "configLc cannot be null");
            this.configBackgroundLocation = configBackgroundLocation;
            this.configNseVer = configNseVer;
            this.configNseCall = configNseCall;
            this.configNseRead = configNseRead;
            this.configNseRetry = configNseRetry;
            this.configRegPush = configRegPush;
            this.configPkey = configPkey;
            this.configVoipPayloadType = Objects.requireNonNull(configVoipPayloadType,
                    "configVoipPayloadType cannot be null");
            this.configSettings = configSettings;
            this.configAppMute = configAppMute;
            this.configAppleWatchId = configAppleWatchId;
            this.configAppleWatchPkey = configAppleWatchPkey;
            Objects.requireNonNull(itemArgs, "itemArgs cannot be null");
            this.itemArgs = List.copyOf(itemArgs);
        }

        /**
         * Returns the platform marker.
         *
         * @return the marker; never {@code null}
         */
        public String configPlatform() {
            return configPlatform;
        }

        /**
         * Returns whether {@code version="2"} is set.
         *
         * @return {@code true} when set
         */
        public boolean hasConfigVersion2() {
            return hasConfigVersion2;
        }

        /**
         * Returns the optional id.
         *
         * @return an {@link Optional} carrying the id
         */
        public Optional<String> configId() {
            return Optional.ofNullable(configId);
        }

        /**
         * Returns the optional voip token.
         *
         * @return an {@link Optional} carrying the token
         */
        public Optional<String> configVoip() {
            return Optional.ofNullable(configVoip);
        }

        /**
         * Returns the preview marker.
         *
         * @return the marker; never {@code null}
         */
        public String configPreview() {
            return configPreview;
        }

        /**
         * Returns the default marker.
         *
         * @return the marker; never {@code null}
         */
        public String configDefault() {
            return configDefault;
        }

        /**
         * Returns the groups marker.
         *
         * @return the marker; never {@code null}
         */
        public String configGroups() {
            return configGroups;
        }

        /**
         * Returns the call marker.
         *
         * @return the marker; never {@code null}
         */
        public String configCall() {
            return configCall;
        }

        /**
         * Returns the optional status-sound marker.
         *
         * @return an {@link Optional} carrying the marker
         */
        public Optional<String> configStatusSound() {
            return Optional.ofNullable(configStatusSound);
        }

        /**
         * Returns the language tag.
         *
         * @return the tag; never {@code null}
         */
        public String configLg() {
            return configLg;
        }

        /**
         * Returns the locale tag.
         *
         * @return the tag; never {@code null}
         */
        public String configLc() {
            return configLc;
        }

        /**
         * Returns the optional background-location marker.
         *
         * @return an {@link Optional} carrying the marker
         */
        public Optional<String> configBackgroundLocation() {
            return Optional.ofNullable(configBackgroundLocation);
        }

        /**
         * Returns the optional NSE version.
         *
         * @return an {@link Optional} carrying the version
         */
        public Optional<String> configNseVer() {
            return Optional.ofNullable(configNseVer);
        }

        /**
         * Returns the optional NSE call marker.
         *
         * @return an {@link Optional} carrying the marker
         */
        public Optional<String> configNseCall() {
            return Optional.ofNullable(configNseCall);
        }

        /**
         * Returns the optional NSE read marker.
         *
         * @return an {@link Optional} carrying the marker
         */
        public Optional<String> configNseRead() {
            return Optional.ofNullable(configNseRead);
        }

        /**
         * Returns the optional NSE retry marker.
         *
         * @return an {@link Optional} carrying the marker
         */
        public Optional<String> configNseRetry() {
            return Optional.ofNullable(configNseRetry);
        }

        /**
         * Returns the optional reg-push marker.
         *
         * @return an {@link Optional} carrying the marker
         */
        public Optional<String> configRegPush() {
            return Optional.ofNullable(configRegPush);
        }

        /**
         * Returns the optional pkey.
         *
         * @return an {@link Optional} carrying the pkey
         */
        public Optional<String> configPkey() {
            return Optional.ofNullable(configPkey);
        }

        /**
         * Returns the voip payload type.
         *
         * @return the type; never {@code null}
         */
        public String configVoipPayloadType() {
            return configVoipPayloadType;
        }

        /**
         * Returns the optional settings mask.
         *
         * @return an {@link Optional} carrying the mask
         */
        public Optional<Long> configSettings() {
            return Optional.ofNullable(configSettings);
        }

        /**
         * Returns the optional app-mute mask.
         *
         * @return an {@link Optional} carrying the mask
         */
        public Optional<Long> configAppMute() {
            return Optional.ofNullable(configAppMute);
        }

        /**
         * Returns the optional Apple Watch id.
         *
         * @return an {@link Optional} carrying the id
         */
        public Optional<String> configAppleWatchId() {
            return Optional.ofNullable(configAppleWatchId);
        }

        /**
         * Returns the optional Apple Watch pkey.
         *
         * @return an {@link Optional} carrying the pkey
         */
        public Optional<String> configAppleWatchPkey() {
            return Optional.ofNullable(configAppleWatchPkey);
        }

        /**
         * Returns the per-item entries.
         *
         * @return an unmodifiable list; never {@code null}
         */
        public List<AppleItem> itemArgs() {
            return itemArgs;
        }

        /**
         * Builds the {@code <config>} node.
         *
         * @return the {@link Node}
         */
        @Override
        @WhatsAppWebExport(moduleName = "WASmaxOutPushConfigAppleClientMixin",
                exports = "mergeAppleClientMixin",
                adaptation = WhatsAppAdaptation.DIRECT)
        public Node toNode() {
            var children = new ArrayList<Node>(itemArgs.size());
            for (var item : itemArgs) {
                children.add(item.toNode());
            }
            var builder = new NodeBuilder()
                    .description("config")
                    .attribute("platform", configPlatform)
                    .attribute("version", "2", hasConfigVersion2)
                    .attribute("preview", configPreview)
                    .attribute("default", configDefault)
                    .attribute("groups", configGroups)
                    .attribute("call", configCall)
                    .attribute("lg", configLg)
                    .attribute("lc", configLc)
                    .attribute("voip_payload_type", configVoipPayloadType);
            if (configId != null) {
                builder.attribute("id", configId);
            }
            if (configVoip != null) {
                builder.attribute("voip", configVoip);
            }
            if (configStatusSound != null) {
                builder.attribute("status_sound", configStatusSound);
            }
            if (configBackgroundLocation != null) {
                builder.attribute("background_location", configBackgroundLocation);
            }
            if (configNseVer != null) {
                builder.attribute("nse_ver", configNseVer);
            }
            if (configNseCall != null) {
                builder.attribute("nse_call", configNseCall);
            }
            if (configNseRead != null) {
                builder.attribute("nse_read", configNseRead);
            }
            if (configNseRetry != null) {
                builder.attribute("nse_retry", configNseRetry);
            }
            if (configRegPush != null) {
                builder.attribute("reg_push", configRegPush);
            }
            if (configPkey != null) {
                builder.attribute("pkey", configPkey);
            }
            if (configSettings != null) {
                builder.attribute("settings", configSettings);
            }
            if (configAppMute != null) {
                builder.attribute("app_mute", configAppMute);
            }
            if (configAppleWatchId != null) {
                builder.attribute("apple_watch_id", configAppleWatchId);
            }
            if (configAppleWatchPkey != null) {
                builder.attribute("apple_watch_pkey", configAppleWatchPkey);
            }
            builder.content(children);
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
            var that = (AppleConfig) obj;
            return this.hasConfigVersion2 == that.hasConfigVersion2
                    && Objects.equals(this.configPlatform, that.configPlatform)
                    && Objects.equals(this.configId, that.configId)
                    && Objects.equals(this.configVoip, that.configVoip)
                    && Objects.equals(this.configPreview, that.configPreview)
                    && Objects.equals(this.configDefault, that.configDefault)
                    && Objects.equals(this.configGroups, that.configGroups)
                    && Objects.equals(this.configCall, that.configCall)
                    && Objects.equals(this.configStatusSound, that.configStatusSound)
                    && Objects.equals(this.configLg, that.configLg)
                    && Objects.equals(this.configLc, that.configLc)
                    && Objects.equals(this.configBackgroundLocation, that.configBackgroundLocation)
                    && Objects.equals(this.configNseVer, that.configNseVer)
                    && Objects.equals(this.configNseCall, that.configNseCall)
                    && Objects.equals(this.configNseRead, that.configNseRead)
                    && Objects.equals(this.configNseRetry, that.configNseRetry)
                    && Objects.equals(this.configRegPush, that.configRegPush)
                    && Objects.equals(this.configPkey, that.configPkey)
                    && Objects.equals(this.configVoipPayloadType, that.configVoipPayloadType)
                    && Objects.equals(this.configSettings, that.configSettings)
                    && Objects.equals(this.configAppMute, that.configAppMute)
                    && Objects.equals(this.configAppleWatchId, that.configAppleWatchId)
                    && Objects.equals(this.configAppleWatchPkey, that.configAppleWatchPkey)
                    && Objects.equals(this.itemArgs, that.itemArgs);
        }

        @Override
        public int hashCode() {
            var result = Objects.hash(configPlatform, hasConfigVersion2, configId, configVoip,
                    configPreview, configDefault, configGroups, configCall, configStatusSound,
                    configLg, configLc, configBackgroundLocation, configNseVer, configNseCall,
                    configNseRead, configNseRetry, configRegPush, configPkey, configVoipPayloadType,
                    configSettings, configAppMute, configAppleWatchId, configAppleWatchPkey);
            result = 31 * result + Objects.hashCode(itemArgs);
            return result;
        }

        @Override
        public String toString() {
            return "SmaxPushConfigSetConfigVariant.AppleConfig[configPlatform=" + configPlatform
                    + ", hasConfigVersion2=" + hasConfigVersion2
                    + ", configId=" + configId
                    + ", configVoip=" + configVoip
                    + ", configPreview=" + configPreview
                    + ", configDefault=" + configDefault
                    + ", configGroups=" + configGroups
                    + ", configCall=" + configCall
                    + ", configStatusSound=" + configStatusSound
                    + ", configLg=" + configLg
                    + ", configLc=" + configLc
                    + ", configBackgroundLocation=" + configBackgroundLocation
                    + ", configNseVer=" + configNseVer
                    + ", configNseCall=" + configNseCall
                    + ", configNseRead=" + configNseRead
                    + ", configNseRetry=" + configNseRetry
                    + ", configRegPush=" + configRegPush
                    + ", configPkey=" + configPkey
                    + ", configVoipPayloadType=" + configVoipPayloadType
                    + ", configSettings=" + configSettings
                    + ", configAppMute=" + configAppMute
                    + ", configAppleWatchId=" + configAppleWatchId
                    + ", configAppleWatchPkey=" + configAppleWatchPkey
                    + ", itemArgs=" + itemArgs + ']';
        }

        /**
         * A single {@code <item jid mute? notify? call?/>} entry
         * carried by an {@link AppleConfig}.
         */
        public static final class AppleItem {
            /**
             * The target JID.
             */
            private final Jid itemJid;

            /**
             * The optional mute marker.
             */
            private final Long itemMute;

            /**
             * The optional notify marker.
             */
            private final String itemNotify;

            /**
             * The optional call marker.
             */
            private final String itemCall;

            /**
             * Constructs a new entry.
             *
             * @param itemJid    the target JID; never {@code null}
             * @param itemMute   the optional mute marker; may be
             *                   {@code null}
             * @param itemNotify the optional notify marker; may be
             *                   {@code null}
             * @param itemCall   the optional call marker; may be
             *                   {@code null}
             * @throws NullPointerException if {@code itemJid} is
             *                              {@code null}
             */
            public AppleItem(Jid itemJid, Long itemMute, String itemNotify, String itemCall) {
                this.itemJid = Objects.requireNonNull(itemJid, "itemJid cannot be null");
                this.itemMute = itemMute;
                this.itemNotify = itemNotify;
                this.itemCall = itemCall;
            }

            /**
             * Returns the target JID.
             *
             * @return the JID; never {@code null}
             */
            public Jid itemJid() {
                return itemJid;
            }

            /**
             * Returns the optional mute marker.
             *
             * @return an {@link Optional} carrying the marker
             */
            public Optional<Long> itemMute() {
                return Optional.ofNullable(itemMute);
            }

            /**
             * Returns the optional notify marker.
             *
             * @return an {@link Optional} carrying the marker
             */
            public Optional<String> itemNotify() {
                return Optional.ofNullable(itemNotify);
            }

            /**
             * Returns the optional call marker.
             *
             * @return an {@link Optional} carrying the marker
             */
            public Optional<String> itemCall() {
                return Optional.ofNullable(itemCall);
            }

            /**
             * Builds the {@code <item>} child node.
             *
             * @return the {@link Node}
             */
            @WhatsAppWebExport(moduleName = "WASmaxOutPushConfigAppleClientMixin",
                    exports = "makeAppleClientItem",
                    adaptation = WhatsAppAdaptation.DIRECT)
            public Node toNode() {
                var builder = new NodeBuilder()
                        .description("item")
                        .attribute("jid", itemJid);
                if (itemMute != null) {
                    builder.attribute("mute", itemMute);
                }
                if (itemNotify != null) {
                    builder.attribute("notify", itemNotify);
                }
                if (itemCall != null) {
                    builder.attribute("call", itemCall);
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
                var that = (AppleItem) obj;
                return Objects.equals(this.itemJid, that.itemJid)
                        && Objects.equals(this.itemMute, that.itemMute)
                        && Objects.equals(this.itemNotify, that.itemNotify)
                        && Objects.equals(this.itemCall, that.itemCall);
            }

            @Override
            public int hashCode() {
                return Objects.hash(itemJid, itemMute, itemNotify, itemCall);
            }

            @Override
            public String toString() {
                return "SmaxPushConfigSetConfigVariant.AppleConfig.AppleItem[itemJid=" + itemJid
                        + ", itemMute=" + itemMute
                        + ", itemNotify=" + itemNotify
                        + ", itemCall=" + itemCall + ']';
            }
        }
    }

    /**
     * The Windows-Notification-Service-client {@code <config
     * platform="wns">} variant.
     */
    @WhatsAppWebModule(moduleName = "WASmaxOutPushConfigWNSClientMixin")
    final class WnsConfig implements SmaxPushConfigSetConfigVariant {
        /**
         * The optional {@code version} attribute.
         */
        private final String configVersion;

        /**
         * The mandatory {@code id} attribute.
         */
        private final String configId;

        /**
         * Constructs a new WNS config.
         *
         * @param configVersion the optional version; may be
         *                      {@code null}
         * @param configId      the WNS id; never {@code null}
         * @throws NullPointerException if {@code configId} is
         *                              {@code null}
         */
        public WnsConfig(String configVersion, String configId) {
            this.configVersion = configVersion;
            this.configId = Objects.requireNonNull(configId, "configId cannot be null");
        }

        /**
         * Returns the optional version.
         *
         * @return an {@link Optional} carrying the version
         */
        public Optional<String> configVersion() {
            return Optional.ofNullable(configVersion);
        }

        /**
         * Returns the WNS id.
         *
         * @return the id; never {@code null}
         */
        public String configId() {
            return configId;
        }

        /**
         * Builds the {@code <config platform="wns">} node.
         *
         * @return the {@link Node}
         */
        @Override
        @WhatsAppWebExport(moduleName = "WASmaxOutPushConfigWNSClientMixin",
                exports = "mergeWNSClientMixin",
                adaptation = WhatsAppAdaptation.DIRECT)
        public Node toNode() {
            var builder = new NodeBuilder()
                    .description("config")
                    .attribute("platform", "wns")
                    .attribute("id", configId);
            if (configVersion != null) {
                builder.attribute("version", configVersion);
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
            var that = (WnsConfig) obj;
            return Objects.equals(this.configVersion, that.configVersion)
                    && Objects.equals(this.configId, that.configId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(configVersion, configId);
        }

        @Override
        public String toString() {
            return "SmaxPushConfigSetConfigVariant.WnsConfig[configVersion=" + configVersion
                    + ", configId=" + configId + ']';
        }
    }

    /**
     * The Enterprise-client {@code <config platform="ent">}
     * variant.
     */
    @WhatsAppWebModule(moduleName = "WASmaxOutPushConfigEnterpriseClientMixin")
    final class EnterpriseConfig implements SmaxPushConfigSetConfigVariant {
        /**
         * The mandatory {@code id} attribute.
         */
        private final String configId;

        /**
         * Constructs a new enterprise-client config.
         *
         * @param configId the enterprise id; never {@code null}
         * @throws NullPointerException if {@code configId} is
         *                              {@code null}
         */
        public EnterpriseConfig(String configId) {
            this.configId = Objects.requireNonNull(configId, "configId cannot be null");
        }

        /**
         * Returns the enterprise id.
         *
         * @return the id; never {@code null}
         */
        public String configId() {
            return configId;
        }

        /**
         * Builds the {@code <config platform="ent">} node.
         *
         * @return the {@link Node}
         */
        @Override
        @WhatsAppWebExport(moduleName = "WASmaxOutPushConfigEnterpriseClientMixin",
                exports = "mergeEnterpriseClientMixin",
                adaptation = WhatsAppAdaptation.DIRECT)
        public Node toNode() {
            return new NodeBuilder()
                    .description("config")
                    .attribute("platform", "ent")
                    .attribute("id", configId)
                    .build();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (EnterpriseConfig) obj;
            return Objects.equals(this.configId, that.configId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(configId);
        }

        @Override
        public String toString() {
            return "SmaxPushConfigSetConfigVariant.EnterpriseConfig[configId=" + configId + ']';
        }
    }

    /**
     * The Web-client {@code <config platform="web">} variant —
     * carries the W3C Push API endpoint, auth secret, and P-256
     * application server key.
     */
    @WhatsAppWebModule(moduleName = "WASmaxOutPushConfigWebClientMixin")
    final class WebConfig implements SmaxPushConfigSetConfigVariant {
        /**
         * The W3C Push API endpoint URL.
         */
        private final String configEndpoint;

        /**
         * The Push API auth secret.
         */
        private final String configAuth;

        /**
         * The P-256 application-server public key.
         */
        private final String configP256dh;

        /**
         * The optional language tag.
         */
        private final String configLg;

        /**
         * The optional locale tag.
         */
        private final String configLc;

        /**
         * Constructs a new web-client config.
         *
         * @param configEndpoint the endpoint URL; never
         *                       {@code null}
         * @param configAuth     the auth secret; never {@code null}
         * @param configP256dh   the P-256 key; never {@code null}
         * @param configLg       the optional language; may be
         *                       {@code null}
         * @param configLc       the optional locale; may be
         *                       {@code null}
         * @throws NullPointerException if any required argument is
         *                              {@code null}
         */
        public WebConfig(String configEndpoint, String configAuth, String configP256dh,
                         String configLg, String configLc) {
            this.configEndpoint = Objects.requireNonNull(configEndpoint, "configEndpoint cannot be null");
            this.configAuth = Objects.requireNonNull(configAuth, "configAuth cannot be null");
            this.configP256dh = Objects.requireNonNull(configP256dh, "configP256dh cannot be null");
            this.configLg = configLg;
            this.configLc = configLc;
        }

        /**
         * Returns the endpoint URL.
         *
         * @return the URL; never {@code null}
         */
        public String configEndpoint() {
            return configEndpoint;
        }

        /**
         * Returns the auth secret.
         *
         * @return the secret; never {@code null}
         */
        public String configAuth() {
            return configAuth;
        }

        /**
         * Returns the P-256 key.
         *
         * @return the key; never {@code null}
         */
        public String configP256dh() {
            return configP256dh;
        }

        /**
         * Returns the optional language tag.
         *
         * @return an {@link Optional} carrying the tag
         */
        public Optional<String> configLg() {
            return Optional.ofNullable(configLg);
        }

        /**
         * Returns the optional locale tag.
         *
         * @return an {@link Optional} carrying the tag
         */
        public Optional<String> configLc() {
            return Optional.ofNullable(configLc);
        }

        /**
         * Builds the {@code <config platform="web">} node.
         *
         * @return the {@link Node}
         */
        @Override
        @WhatsAppWebExport(moduleName = "WASmaxOutPushConfigWebClientMixin",
                exports = "mergeWebClientMixin",
                adaptation = WhatsAppAdaptation.DIRECT)
        public Node toNode() {
            var builder = new NodeBuilder()
                    .description("config")
                    .attribute("platform", "web")
                    .attribute("endpoint", configEndpoint)
                    .attribute("auth", configAuth)
                    .attribute("p256dh", configP256dh);
            if (configLg != null) {
                builder.attribute("lg", configLg);
            }
            if (configLc != null) {
                builder.attribute("lc", configLc);
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
            var that = (WebConfig) obj;
            return Objects.equals(this.configEndpoint, that.configEndpoint)
                    && Objects.equals(this.configAuth, that.configAuth)
                    && Objects.equals(this.configP256dh, that.configP256dh)
                    && Objects.equals(this.configLg, that.configLg)
                    && Objects.equals(this.configLc, that.configLc);
        }

        @Override
        public int hashCode() {
            return Objects.hash(configEndpoint, configAuth, configP256dh, configLg, configLc);
        }

        @Override
        public String toString() {
            return "SmaxPushConfigSetConfigVariant.WebConfig[configEndpoint=" + configEndpoint
                    + ", configAuth=" + configAuth
                    + ", configP256dh=" + configP256dh
                    + ", configLg=" + configLg
                    + ", configLc=" + configLc + ']';
        }
    }
}
