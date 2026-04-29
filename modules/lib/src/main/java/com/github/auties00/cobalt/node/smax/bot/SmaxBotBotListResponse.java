package com.github.auties00.cobalt.node.smax.bot;

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
 * Sealed family of inbound reply variants.
 *
 * @implNote {@code WASmaxBotBotListRPC.sendBotListRPC} tries
 *           {@code SuccessV2} → {@code SuccessV3} → {@code Error}
 *           in order.
 */
public sealed interface SmaxBotBotListResponse extends SmaxOperation.Response
        permits SmaxBotBotListResponse.SuccessV2, SmaxBotBotListResponse.SuccessV3, SmaxBotBotListResponse.Error {

    /**
     * Tries each {@link SmaxBotBotListResponse} variant in priority order and
     * returns the first that parses cleanly.
     *
     * @param node    the inbound IQ stanza received from the relay;
     *                never {@code null}
     * @param request the original outbound stanza — used to validate
     *                echoed identifiers; never {@code null}
     * @return an {@link Optional} carrying the parsed variant, or
     *         {@link Optional#empty()} when no documented variant
     *         matched the stanza shape
     * @throws NullPointerException if either argument is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WASmaxBotBotListRPC",
            exports = "sendBotListRPC", adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends SmaxBotBotListResponse> of(Node node, Node request) {
        Objects.requireNonNull(node, "node cannot be null");
        Objects.requireNonNull(request, "request cannot be null");
        // WASmaxBotBotListRPC: tries V2 → V3 → Error
        var v2 = SuccessV2.of(node, request);
        if (v2.isPresent()) {
            return v2;
        }
        var v3 = SuccessV3.of(node, request);
        if (v3.isPresent()) {
            return v3;
        }
        return Error.of(node, request);
    }

    /**
     * The {@code SuccessV2} reply variant — the legacy v2 directory
     * shape. Distinguished by the literal {@code v="2"} on the
     * top-level {@code <bot>} child.
     *
     * @implNote {@code WASmaxInBotBotListResponseSuccessV2.parseBotListResponseSuccessV2}
     *           validates the IQ-result envelope, asserts
     *           {@code <bot v="2">}, requires a {@code <default>}
     *           bot-of-the-day entry, then projects every
     *           {@code <section>} child via
     *           {@code mapChildrenWithTag(section, 1, ∞)} into
     *           typed sections each carrying typed
     *           {@link BotEntry} children with optional
     *           {@link ThemeBundle} overrides.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInBotBotListResponseSuccessV2")
    @WhatsAppWebModule(moduleName = "WASmaxInBotIQResultResponseMixin")
    final class SuccessV2 implements SmaxBotBotListResponse {
        /**
         * The protocol revision — always the literal {@code "2"}.
         */
        private final String botV;

        /**
         * The directory's "bot of the day" default entry's JID.
         */
        private final Jid botDefaultJid;

        /**
         * The directory's "bot of the day" default entry's persona
         * id.
         */
        private final String botDefaultPersonaId;

        /**
         * The directory sections.
         */
        private final List<Section> botSection;

        /**
         * Constructs a new V2 reply.
         *
         * @param botV                the protocol revision (always
         *                            {@code "2"}); never {@code null}
         * @param botDefaultJid       the default-entry JID; never
         *                            {@code null}
         * @param botDefaultPersonaId the default-entry persona id;
         *                            never {@code null}
         * @param botSection          the directory sections; never
         *                            {@code null}
         * @throws NullPointerException if any argument is
         *                              {@code null}
         */
        public SuccessV2(String botV, Jid botDefaultJid, String botDefaultPersonaId,
                         List<Section> botSection) {
            this.botV = Objects.requireNonNull(botV, "botV cannot be null");
            this.botDefaultJid = Objects.requireNonNull(botDefaultJid, "botDefaultJid cannot be null");
            this.botDefaultPersonaId = Objects.requireNonNull(botDefaultPersonaId, "botDefaultPersonaId cannot be null");
            this.botSection = List.copyOf(Objects.requireNonNullElse(botSection, List.of()));
        }

        /**
         * Returns the protocol revision.
         *
         * @return the revision; always {@code "2"}; never
         *         {@code null}
         */
        public String botV() {
            return botV;
        }

        /**
         * Returns the default-entry JID.
         *
         * @return the JID; never {@code null}
         */
        public Jid botDefaultJid() {
            return botDefaultJid;
        }

        /**
         * Returns the default-entry persona id.
         *
         * @return the persona id; never {@code null}
         */
        public String botDefaultPersonaId() {
            return botDefaultPersonaId;
        }

        /**
         * Returns the directory sections.
         *
         * @return an unmodifiable list; never {@code null}
         */
        public List<Section> botSection() {
            return botSection;
        }

        /**
         * Tries to parse a {@link SuccessV2} variant from the given
         * inbound stanza.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant
         */
        @WhatsAppWebExport(moduleName = "WASmaxInBotBotListResponseSuccessV2",
                exports = "parseBotListResponseSuccessV2",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<SuccessV2> of(Node node, Node request) {
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            var botChild = node.getChild("bot").orElse(null);
            if (botChild == null) {
                return Optional.empty();
            }
            // literal(attrString, "v", "2")
            if (!botChild.hasAttribute("v", "2")) {
                return Optional.empty();
            }
            var defaultChild = botChild.getChild("default").orElse(null);
            if (defaultChild == null) {
                return Optional.empty();
            }
            var defaultJid = defaultChild.getAttributeAsJid("jid").orElse(null);
            if (defaultJid == null) {
                return Optional.empty();
            }
            var defaultPersonaId = defaultChild.getAttributeAsString("persona_id").orElse(null);
            if (defaultPersonaId == null) {
                return Optional.empty();
            }
            var sections = new ArrayList<Section>();
            for (var sectionNode : botChild.getChildren("section")) {
                var section = Section.of(sectionNode).orElse(null);
                if (section == null) {
                    return Optional.empty();
                }
                sections.add(section);
            }
            // WA Web requires at least 1 section: mapChildrenWithTag(section, 1, ∞)
            if (sections.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(new SuccessV2("2", defaultJid, defaultPersonaId, sections));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (SuccessV2) obj;
            return Objects.equals(this.botV, that.botV)
                    && Objects.equals(this.botDefaultJid, that.botDefaultJid)
                    && Objects.equals(this.botDefaultPersonaId, that.botDefaultPersonaId)
                    && Objects.equals(this.botSection, that.botSection);
        }

        @Override
        public int hashCode() {
            return Objects.hash(botV, botDefaultJid, botDefaultPersonaId, botSection);
        }

        @Override
        public String toString() {
            return "SmaxBotBotListResponse.SuccessV2[botV=" + botV
                    + ", botDefaultJid=" + botDefaultJid
                    + ", botDefaultPersonaId=" + botDefaultPersonaId
                    + ", botSection=" + botSection + ']';
        }

        /**
         * A single V2 section — carries a typed list of
         * {@link BotEntry} entries.
         */
        public static final class Section {
            /**
             * The section name (free-form, displayed verbatim).
             */
            private final String name;

            /**
             * The section type discriminator.
             */
            private final SmaxBotBotListSectionType type;

            /**
             * The bot entries.
             */
            private final List<BotEntry> bot;

            /**
             * Constructs a new section.
             *
             * @param name the section name; never {@code null}
             * @param type the section type; never {@code null}
             * @param bot  the bot entries; never {@code null}
             * @throws NullPointerException if any argument is
             *                              {@code null}
             */
            public Section(String name, SmaxBotBotListSectionType type, List<BotEntry> bot) {
                this.name = Objects.requireNonNull(name, "name cannot be null");
                this.type = Objects.requireNonNull(type, "type cannot be null");
                this.bot = List.copyOf(Objects.requireNonNullElse(bot, List.of()));
            }

            /**
             * Returns the section name.
             *
             * @return the name; never {@code null}
             */
            public String name() {
                return name;
            }

            /**
             * Returns the section type.
             *
             * @return the type; never {@code null}
             */
            public SmaxBotBotListSectionType type() {
                return type;
            }

            /**
             * Returns the bot entries.
             *
             * @return an unmodifiable list; never {@code null}
             */
            public List<BotEntry> bot() {
                return bot;
            }

            /**
             * Tries to parse a section from the given
             * {@code <section>} child.
             *
             * @param node the {@code <section>} child
             * @return an {@link Optional} carrying the parsed
             *         section
             */
            @WhatsAppWebExport(moduleName = "WASmaxInBotBotListResponseSuccessV2",
                    exports = "parseBotListResponseSuccessV2BotSection",
                    adaptation = WhatsAppAdaptation.ADAPTED)
            public static Optional<Section> of(Node node) {
                Objects.requireNonNull(node, "node cannot be null");
                if (!node.hasDescription("section")) {
                    return Optional.empty();
                }
                var name = node.getAttributeAsString("name").orElse(null);
                if (name == null) {
                    return Optional.empty();
                }
                var typeAttr = node.getAttributeAsString("type").orElse(null);
                if (typeAttr == null) {
                    return Optional.empty();
                }
                var type = SmaxBotBotListSectionType.ofWire(typeAttr).orElse(null);
                if (type == null) {
                    return Optional.empty();
                }
                var bots = new ArrayList<BotEntry>();
                for (var botNode : node.getChildren("bot")) {
                    var bot = BotEntry.of(botNode).orElse(null);
                    if (bot == null) {
                        return Optional.empty();
                    }
                    bots.add(bot);
                }
                return Optional.of(new Section(name, type, bots));
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) {
                    return true;
                }
                if (obj == null || obj.getClass() != this.getClass()) {
                    return false;
                }
                var that = (Section) obj;
                return Objects.equals(this.name, that.name)
                        && this.type == that.type
                        && Objects.equals(this.bot, that.bot);
            }

            @Override
            public int hashCode() {
                return Objects.hash(name, type, bot);
            }

            @Override
            public String toString() {
                return "SmaxBotBotListResponse.SuccessV2.Section[name=" + name
                        + ", type=" + type
                        + ", bot=" + bot + ']';
            }
        }

        /**
         * A single V2 bot entry — carries the bot JID, persona id,
         * optional usage count, and optional theme overrides.
         */
        public static final class BotEntry {
            /**
             * The bot JID.
             */
            private final Jid jid;

            /**
             * The bot persona id.
             */
            private final String personaId;

            /**
             * The optional usage count.
             */
            private final Integer count;

            /**
             * The optional theme overrides (0..2 entries — the WA
             * Web parser asserts {@code mapChildrenWithTag(theme, 0, 2)}).
             */
            private final List<ThemeBundle> theme;

            /**
             * Constructs a new bot entry.
             *
             * @param jid       the bot JID; never {@code null}
             * @param personaId the persona id; never {@code null}
             * @param count     the optional usage count; may be
             *                  {@code null}
             * @param theme     the theme overrides; never
             *                  {@code null}
             * @throws NullPointerException if {@code jid},
             *                              {@code personaId}, or
             *                              {@code theme} are
             *                              {@code null}
             */
            public BotEntry(Jid jid, String personaId, Integer count, List<ThemeBundle> theme) {
                this.jid = Objects.requireNonNull(jid, "jid cannot be null");
                this.personaId = Objects.requireNonNull(personaId, "personaId cannot be null");
                this.count = count;
                this.theme = List.copyOf(Objects.requireNonNullElse(theme, List.of()));
            }

            /**
             * Returns the bot JID.
             *
             * @return the JID; never {@code null}
             */
            public Jid jid() {
                return jid;
            }

            /**
             * Returns the persona id.
             *
             * @return the persona id; never {@code null}
             */
            public String personaId() {
                return personaId;
            }

            /**
             * Returns the optional usage count.
             *
             * @return an {@link Optional} carrying the count
             */
            public Optional<Integer> count() {
                return Optional.ofNullable(count);
            }

            /**
             * Returns the theme overrides.
             *
             * @return an unmodifiable list (0..2 entries); never
             *         {@code null}
             */
            public List<ThemeBundle> theme() {
                return theme;
            }

            /**
             * Tries to parse a bot entry from the given
             * {@code <bot>} child.
             *
             * @param node the {@code <bot>} child
             * @return an {@link Optional} carrying the parsed entry
             */
            @WhatsAppWebExport(moduleName = "WASmaxInBotBotListResponseSuccessV2",
                    exports = "parseBotListResponseSuccessV2BotSectionBot",
                    adaptation = WhatsAppAdaptation.ADAPTED)
            public static Optional<BotEntry> of(Node node) {
                Objects.requireNonNull(node, "node cannot be null");
                if (!node.hasDescription("bot")) {
                    return Optional.empty();
                }
                var jid = node.getAttributeAsJid("jid").orElse(null);
                if (jid == null) {
                    return Optional.empty();
                }
                var personaId = node.getAttributeAsString("persona_id").orElse(null);
                if (personaId == null) {
                    return Optional.empty();
                }
                var countAttr = node.getAttributeAsInt("count").orElse(-1);
                Integer count = null;
                if (countAttr >= 0) {
                    count = countAttr;
                } else if (node.hasAttribute("count")) {
                    return Optional.empty();
                }
                var themes = new ArrayList<ThemeBundle>();
                for (var themeNode : node.getChildren("theme")) {
                    var theme = ThemeBundle.of(themeNode).orElse(null);
                    if (theme == null) {
                        return Optional.empty();
                    }
                    themes.add(theme);
                }
                if (themes.size() > 2) {
                    return Optional.empty();
                }
                return Optional.of(new BotEntry(jid, personaId, count, themes));
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) {
                    return true;
                }
                if (obj == null || obj.getClass() != this.getClass()) {
                    return false;
                }
                var that = (BotEntry) obj;
                return Objects.equals(this.jid, that.jid)
                        && Objects.equals(this.personaId, that.personaId)
                        && Objects.equals(this.count, that.count)
                        && Objects.equals(this.theme, that.theme);
            }

            @Override
            public int hashCode() {
                return Objects.hash(jid, personaId, count, theme);
            }

            @Override
            public String toString() {
                return "SmaxBotBotListResponse.SuccessV2.BotEntry[jid=" + jid
                        + ", personaId=" + personaId
                        + ", count=" + count
                        + ", theme=" + theme + ']';
            }
        }

        /**
         * A single theme bundle — carries the dark/light mode
         * literal plus the three colour element values
         * (background, primary text, secondary text).
         */
        public static final class ThemeBundle {
            /**
             * The theme mode discriminator.
             */
            private final SmaxBotBotListThemeMode mode;

            /**
             * The {@code <background>} child's element value.
             */
            private final String backgroundElementValue;

            /**
             * The {@code <primary_text>} child's element value.
             */
            private final String primaryTextElementValue;

            /**
             * The {@code <secondary_text>} child's element value.
             */
            private final String secondaryTextElementValue;

            /**
             * Constructs a new theme bundle.
             *
             * @param mode                      the theme mode; never
             *                                  {@code null}
             * @param backgroundElementValue    the background
             *                                  element value; never
             *                                  {@code null}
             * @param primaryTextElementValue   the primary-text
             *                                  element value; never
             *                                  {@code null}
             * @param secondaryTextElementValue the secondary-text
             *                                  element value; never
             *                                  {@code null}
             * @throws NullPointerException if any argument is
             *                              {@code null}
             */
            public ThemeBundle(SmaxBotBotListThemeMode mode, String backgroundElementValue,
                               String primaryTextElementValue, String secondaryTextElementValue) {
                this.mode = Objects.requireNonNull(mode, "mode cannot be null");
                this.backgroundElementValue = Objects.requireNonNull(backgroundElementValue,
                        "backgroundElementValue cannot be null");
                this.primaryTextElementValue = Objects.requireNonNull(primaryTextElementValue,
                        "primaryTextElementValue cannot be null");
                this.secondaryTextElementValue = Objects.requireNonNull(secondaryTextElementValue,
                        "secondaryTextElementValue cannot be null");
            }

            /**
             * Returns the theme mode.
             *
             * @return the mode; never {@code null}
             */
            public SmaxBotBotListThemeMode mode() {
                return mode;
            }

            /**
             * Returns the background element value.
             *
             * @return the value; never {@code null}
             */
            public String backgroundElementValue() {
                return backgroundElementValue;
            }

            /**
             * Returns the primary-text element value.
             *
             * @return the value; never {@code null}
             */
            public String primaryTextElementValue() {
                return primaryTextElementValue;
            }

            /**
             * Returns the secondary-text element value.
             *
             * @return the value; never {@code null}
             */
            public String secondaryTextElementValue() {
                return secondaryTextElementValue;
            }

            /**
             * Tries to parse a theme bundle from the given
             * {@code <theme>} child.
             *
             * @param node the {@code <theme>} child
             * @return an {@link Optional} carrying the parsed
             *         bundle
             */
            @WhatsAppWebExport(moduleName = "WASmaxInBotBotListResponseSuccessV2",
                    exports = "parseBotListResponseSuccessV2BotSectionBotTheme",
                    adaptation = WhatsAppAdaptation.ADAPTED)
            public static Optional<ThemeBundle> of(Node node) {
                Objects.requireNonNull(node, "node cannot be null");
                if (!node.hasDescription("theme")) {
                    return Optional.empty();
                }
                var modeAttr = node.getAttributeAsString("mode").orElse(null);
                if (modeAttr == null) {
                    return Optional.empty();
                }
                var mode = SmaxBotBotListThemeMode.ofWire(modeAttr).orElse(null);
                if (mode == null) {
                    return Optional.empty();
                }
                var backgroundChild = node.getChild("background").orElse(null);
                if (backgroundChild == null) {
                    return Optional.empty();
                }
                var background = backgroundChild.toContentString().orElse(null);
                if (background == null) {
                    return Optional.empty();
                }
                var primaryChild = node.getChild("primary_text").orElse(null);
                if (primaryChild == null) {
                    return Optional.empty();
                }
                var primary = primaryChild.toContentString().orElse(null);
                if (primary == null) {
                    return Optional.empty();
                }
                var secondaryChild = node.getChild("secondary_text").orElse(null);
                if (secondaryChild == null) {
                    return Optional.empty();
                }
                var secondary = secondaryChild.toContentString().orElse(null);
                if (secondary == null) {
                    return Optional.empty();
                }
                return Optional.of(new ThemeBundle(mode, background, primary, secondary));
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) {
                    return true;
                }
                if (obj == null || obj.getClass() != this.getClass()) {
                    return false;
                }
                var that = (ThemeBundle) obj;
                return this.mode == that.mode
                        && Objects.equals(this.backgroundElementValue, that.backgroundElementValue)
                        && Objects.equals(this.primaryTextElementValue, that.primaryTextElementValue)
                        && Objects.equals(this.secondaryTextElementValue, that.secondaryTextElementValue);
            }

            @Override
            public int hashCode() {
                return Objects.hash(mode, backgroundElementValue,
                        primaryTextElementValue, secondaryTextElementValue);
            }

            @Override
            public String toString() {
                return "SmaxBotBotListResponse.SuccessV2.ThemeBundle[mode=" + mode
                        + ", backgroundElementValue=" + backgroundElementValue
                        + ", primaryTextElementValue=" + primaryTextElementValue
                        + ", secondaryTextElementValue=" + secondaryTextElementValue + ']';
            }
        }
    }

    /**
     * The {@code SuccessV3} reply variant — the current v3 directory
     * shape. Distinguished by the literal {@code v="3"} on the
     * top-level {@code <bot>} child.
     *
     * @implNote {@code WASmaxInBotBotListResponseSuccessV3.parseBotListResponseSuccessV3}
     *           validates the IQ-result envelope, asserts
     *           {@code <bot v="3" bhash="…">}, optionally accepts
     *           a {@code <default>} bot entry, then projects every
     *           {@code <section>} child via
     *           {@code mapChildrenWithTag(section, 0, ∞)}. Each
     *           section carries a {@code display_type} routing
     *           literal and typed {@link BotEntry} children with
     *           an optional {@code card_title} attribute (no theme
     *           overrides at the v3 wire layer).
     */
    @WhatsAppWebModule(moduleName = "WASmaxInBotBotListResponseSuccessV3")
    @WhatsAppWebModule(moduleName = "WASmaxInBotIQResultResponseMixin")
    final class SuccessV3 implements SmaxBotBotListResponse {
        /**
         * The protocol revision — always the literal {@code "3"}.
         */
        private final String botV;

        /**
         * The relay-side directory digest.
         */
        private final String botBhash;

        /**
         * The optional "bot of the day" default entry.
         */
        private final DefaultEntry botDefault;

        /**
         * The directory sections.
         */
        private final List<Section> botSection;

        /**
         * Constructs a new V3 reply.
         *
         * @param botV       the protocol revision (always
         *                   {@code "3"}); never {@code null}
         * @param botBhash   the directory digest; never
         *                   {@code null}
         * @param botDefault the optional default entry; may be
         *                   {@code null}
         * @param botSection the directory sections; never
         *                   {@code null}
         * @throws NullPointerException if {@code botV},
         *                              {@code botBhash}, or
         *                              {@code botSection} are
         *                              {@code null}
         */
        public SuccessV3(String botV, String botBhash, DefaultEntry botDefault,
                         List<Section> botSection) {
            this.botV = Objects.requireNonNull(botV, "botV cannot be null");
            this.botBhash = Objects.requireNonNull(botBhash, "botBhash cannot be null");
            this.botDefault = botDefault;
            this.botSection = List.copyOf(Objects.requireNonNullElse(botSection, List.of()));
        }

        /**
         * Returns the protocol revision.
         *
         * @return the revision; always {@code "3"}; never
         *         {@code null}
         */
        public String botV() {
            return botV;
        }

        /**
         * Returns the directory digest.
         *
         * @return the digest; never {@code null}
         */
        public String botBhash() {
            return botBhash;
        }

        /**
         * Returns the optional default entry.
         *
         * @return an {@link Optional} carrying the default entry
         */
        public Optional<DefaultEntry> botDefault() {
            return Optional.ofNullable(botDefault);
        }

        /**
         * Returns the directory sections.
         *
         * @return an unmodifiable list; never {@code null}
         */
        public List<Section> botSection() {
            return botSection;
        }

        /**
         * Tries to parse a {@link SuccessV3} variant from the given
         * inbound stanza.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant
         */
        @WhatsAppWebExport(moduleName = "WASmaxInBotBotListResponseSuccessV3",
                exports = "parseBotListResponseSuccessV3",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<SuccessV3> of(Node node, Node request) {
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            var botChild = node.getChild("bot").orElse(null);
            if (botChild == null) {
                return Optional.empty();
            }
            // literal(attrString, "v", "3")
            if (!botChild.hasAttribute("v", "3")) {
                return Optional.empty();
            }
            var bhash = botChild.getAttributeAsString("bhash").orElse(null);
            if (bhash == null) {
                return Optional.empty();
            }
            DefaultEntry defaultEntry = null;
            var defaultChild = botChild.getChild("default").orElse(null);
            if (defaultChild != null) {
                defaultEntry = DefaultEntry.of(defaultChild).orElse(null);
                if (defaultEntry == null) {
                    return Optional.empty();
                }
            }
            var sections = new ArrayList<Section>();
            for (var sectionNode : botChild.getChildren("section")) {
                var section = Section.of(sectionNode).orElse(null);
                if (section == null) {
                    return Optional.empty();
                }
                sections.add(section);
            }
            return Optional.of(new SuccessV3("3", bhash, defaultEntry, sections));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (SuccessV3) obj;
            return Objects.equals(this.botV, that.botV)
                    && Objects.equals(this.botBhash, that.botBhash)
                    && Objects.equals(this.botDefault, that.botDefault)
                    && Objects.equals(this.botSection, that.botSection);
        }

        @Override
        public int hashCode() {
            return Objects.hash(botV, botBhash, botDefault, botSection);
        }

        @Override
        public String toString() {
            return "SmaxBotBotListResponse.SuccessV3[botV=" + botV
                    + ", botBhash=" + botBhash
                    + ", botDefault=" + botDefault
                    + ", botSection=" + botSection + ']';
        }

        /**
         * The V3 "bot of the day" default entry — carries the bot
         * JID and persona id only.
         */
        public static final class DefaultEntry {
            /**
             * The default entry's JID.
             */
            private final Jid jid;

            /**
             * The default entry's persona id.
             */
            private final String personaId;

            /**
             * Constructs a new default entry.
             *
             * @param jid       the JID; never {@code null}
             * @param personaId the persona id; never {@code null}
             * @throws NullPointerException if either argument is
             *                              {@code null}
             */
            public DefaultEntry(Jid jid, String personaId) {
                this.jid = Objects.requireNonNull(jid, "jid cannot be null");
                this.personaId = Objects.requireNonNull(personaId, "personaId cannot be null");
            }

            /**
             * Returns the JID.
             *
             * @return the JID; never {@code null}
             */
            public Jid jid() {
                return jid;
            }

            /**
             * Returns the persona id.
             *
             * @return the persona id; never {@code null}
             */
            public String personaId() {
                return personaId;
            }

            /**
             * Tries to parse a default entry from the given
             * {@code <default>} child.
             *
             * @param node the {@code <default>} child
             * @return an {@link Optional} carrying the parsed entry
             */
            @WhatsAppWebExport(moduleName = "WASmaxInBotBotListResponseSuccessV3",
                    exports = "parseBotListResponseSuccessV3BotDefault",
                    adaptation = WhatsAppAdaptation.ADAPTED)
            public static Optional<DefaultEntry> of(Node node) {
                Objects.requireNonNull(node, "node cannot be null");
                if (!node.hasDescription("default")) {
                    return Optional.empty();
                }
                var jid = node.getAttributeAsJid("jid").orElse(null);
                if (jid == null) {
                    return Optional.empty();
                }
                var personaId = node.getAttributeAsString("persona_id").orElse(null);
                if (personaId == null) {
                    return Optional.empty();
                }
                return Optional.of(new DefaultEntry(jid, personaId));
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) {
                    return true;
                }
                if (obj == null || obj.getClass() != this.getClass()) {
                    return false;
                }
                var that = (DefaultEntry) obj;
                return Objects.equals(this.jid, that.jid)
                        && Objects.equals(this.personaId, that.personaId);
            }

            @Override
            public int hashCode() {
                return Objects.hash(jid, personaId);
            }

            @Override
            public String toString() {
                return "SmaxBotBotListResponse.SuccessV3.DefaultEntry[jid=" + jid
                        + ", personaId=" + personaId + ']';
            }
        }

        /**
         * A single V3 section — adds {@link SmaxBotBotListSectionDisplayType}
         * routing on top of the V2 (name, type, bot[]) shape.
         */
        public static final class Section {
            /**
             * The section name.
             */
            private final String name;

            /**
             * The section type discriminator.
             */
            private final SmaxBotBotListSectionType type;

            /**
             * The section display-type discriminator.
             */
            private final SmaxBotBotListSectionDisplayType displayType;

            /**
             * The bot entries.
             */
            private final List<BotEntry> bot;

            /**
             * Constructs a new section.
             *
             * @param name        the section name; never
             *                    {@code null}
             * @param type        the section type; never
             *                    {@code null}
             * @param displayType the display-type discriminator;
             *                    never {@code null}
             * @param bot         the bot entries; never
             *                    {@code null}
             * @throws NullPointerException if any argument is
             *                              {@code null}
             */
            public Section(String name, SmaxBotBotListSectionType type, SmaxBotBotListSectionDisplayType displayType,
                           List<BotEntry> bot) {
                this.name = Objects.requireNonNull(name, "name cannot be null");
                this.type = Objects.requireNonNull(type, "type cannot be null");
                this.displayType = Objects.requireNonNull(displayType, "displayType cannot be null");
                this.bot = List.copyOf(Objects.requireNonNullElse(bot, List.of()));
            }

            /**
             * Returns the section name.
             *
             * @return the name; never {@code null}
             */
            public String name() {
                return name;
            }

            /**
             * Returns the section type.
             *
             * @return the type; never {@code null}
             */
            public SmaxBotBotListSectionType type() {
                return type;
            }

            /**
             * Returns the display-type discriminator.
             *
             * @return the discriminator; never {@code null}
             */
            public SmaxBotBotListSectionDisplayType displayType() {
                return displayType;
            }

            /**
             * Returns the bot entries.
             *
             * @return an unmodifiable list; never {@code null}
             */
            public List<BotEntry> bot() {
                return bot;
            }

            /**
             * Tries to parse a section from the given
             * {@code <section>} child.
             *
             * @param node the {@code <section>} child
             * @return an {@link Optional} carrying the parsed
             *         section
             */
            @WhatsAppWebExport(moduleName = "WASmaxInBotBotListResponseSuccessV3",
                    exports = "parseBotListResponseSuccessV3BotSection",
                    adaptation = WhatsAppAdaptation.ADAPTED)
            public static Optional<Section> of(Node node) {
                Objects.requireNonNull(node, "node cannot be null");
                if (!node.hasDescription("section")) {
                    return Optional.empty();
                }
                var name = node.getAttributeAsString("name").orElse(null);
                if (name == null) {
                    return Optional.empty();
                }
                var typeAttr = node.getAttributeAsString("type").orElse(null);
                if (typeAttr == null) {
                    return Optional.empty();
                }
                var type = SmaxBotBotListSectionType.ofWire(typeAttr).orElse(null);
                if (type == null) {
                    return Optional.empty();
                }
                var displayTypeAttr = node.getAttributeAsString("display_type").orElse(null);
                if (displayTypeAttr == null) {
                    return Optional.empty();
                }
                var displayType = SmaxBotBotListSectionDisplayType.ofWire(displayTypeAttr).orElse(null);
                if (displayType == null) {
                    return Optional.empty();
                }
                var bots = new ArrayList<BotEntry>();
                for (var botNode : node.getChildren("bot")) {
                    var bot = BotEntry.of(botNode).orElse(null);
                    if (bot == null) {
                        return Optional.empty();
                    }
                    bots.add(bot);
                }
                return Optional.of(new Section(name, type, displayType, bots));
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) {
                    return true;
                }
                if (obj == null || obj.getClass() != this.getClass()) {
                    return false;
                }
                var that = (Section) obj;
                return Objects.equals(this.name, that.name)
                        && this.type == that.type
                        && this.displayType == that.displayType
                        && Objects.equals(this.bot, that.bot);
            }

            @Override
            public int hashCode() {
                return Objects.hash(name, type, displayType, bot);
            }

            @Override
            public String toString() {
                return "SmaxBotBotListResponse.SuccessV3.Section[name=" + name
                        + ", type=" + type
                        + ", displayType=" + displayType
                        + ", bot=" + bot + ']';
            }
        }

        /**
         * A single V3 bot entry — carries the bot JID, persona id,
         * optional card title, and optional usage count (no theme
         * overrides at the v3 wire layer).
         */
        public static final class BotEntry {
            /**
             * The bot JID.
             */
            private final Jid jid;

            /**
             * The bot persona id.
             */
            private final String personaId;

            /**
             * The optional card title.
             */
            private final String cardTitle;

            /**
             * The optional usage count.
             */
            private final Integer count;

            /**
             * Constructs a new bot entry.
             *
             * @param jid       the bot JID; never {@code null}
             * @param personaId the persona id; never {@code null}
             * @param cardTitle the optional card title; may be
             *                  {@code null}
             * @param count     the optional usage count; may be
             *                  {@code null}
             * @throws NullPointerException if {@code jid} or
             *                              {@code personaId} are
             *                              {@code null}
             */
            public BotEntry(Jid jid, String personaId, String cardTitle, Integer count) {
                this.jid = Objects.requireNonNull(jid, "jid cannot be null");
                this.personaId = Objects.requireNonNull(personaId, "personaId cannot be null");
                this.cardTitle = cardTitle;
                this.count = count;
            }

            /**
             * Returns the bot JID.
             *
             * @return the JID; never {@code null}
             */
            public Jid jid() {
                return jid;
            }

            /**
             * Returns the persona id.
             *
             * @return the persona id; never {@code null}
             */
            public String personaId() {
                return personaId;
            }

            /**
             * Returns the optional card title.
             *
             * @return an {@link Optional} carrying the title
             */
            public Optional<String> cardTitle() {
                return Optional.ofNullable(cardTitle);
            }

            /**
             * Returns the optional usage count.
             *
             * @return an {@link Optional} carrying the count
             */
            public Optional<Integer> count() {
                return Optional.ofNullable(count);
            }

            /**
             * Tries to parse a bot entry from the given
             * {@code <bot>} child.
             *
             * @param node the {@code <bot>} child
             * @return an {@link Optional} carrying the parsed entry
             */
            @WhatsAppWebExport(moduleName = "WASmaxInBotBotListResponseSuccessV3",
                    exports = "parseBotListResponseSuccessV3BotSectionBot",
                    adaptation = WhatsAppAdaptation.ADAPTED)
            public static Optional<BotEntry> of(Node node) {
                Objects.requireNonNull(node, "node cannot be null");
                if (!node.hasDescription("bot")) {
                    return Optional.empty();
                }
                var jid = node.getAttributeAsJid("jid").orElse(null);
                if (jid == null) {
                    return Optional.empty();
                }
                var personaId = node.getAttributeAsString("persona_id").orElse(null);
                if (personaId == null) {
                    return Optional.empty();
                }
                var cardTitle = node.getAttributeAsString("card_title").orElse(null);
                var countAttr = node.getAttributeAsInt("count").orElse(-1);
                Integer count = null;
                if (countAttr >= 0) {
                    count = countAttr;
                } else if (node.hasAttribute("count")) {
                    return Optional.empty();
                }
                return Optional.of(new BotEntry(jid, personaId, cardTitle, count));
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) {
                    return true;
                }
                if (obj == null || obj.getClass() != this.getClass()) {
                    return false;
                }
                var that = (BotEntry) obj;
                return Objects.equals(this.jid, that.jid)
                        && Objects.equals(this.personaId, that.personaId)
                        && Objects.equals(this.cardTitle, that.cardTitle)
                        && Objects.equals(this.count, that.count);
            }

            @Override
            public int hashCode() {
                return Objects.hash(jid, personaId, cardTitle, count);
            }

            @Override
            public String toString() {
                return "SmaxBotBotListResponse.SuccessV3.BotEntry[jid=" + jid
                        + ", personaId=" + personaId
                        + ", cardTitle=" + cardTitle
                        + ", count=" + count + ']';
            }
        }
    }

    /**
     * The {@code Error} reply variant — the relay rejected the
     * request.
     *
     * <p>The bot domain projects four documented variants:
     * {@code internal-server-error/500},
     * {@code forbidden/403},
     * {@code bad-request/400},
     * {@code not-allowed/405}. Cobalt collapses them into the
     * single {@code (errorCode, errorText)} pair.
     *
     * @implNote {@code WASmaxInBotBotListResponseError.parseBotListResponseError}
     *           composes the IQ-error envelope check with
     *           {@code WASmaxInBotBotListErrors.parseBotListErrors}
     *           which is the disjunction of the four projected
     *           error mixins.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInBotBotListResponseError")
    @WhatsAppWebModule(moduleName = "WASmaxInBotBotListErrors")
    final class Error implements SmaxBotBotListResponse {
        /**
         * The numeric error code.
         */
        private final int errorCode;

        /**
         * The human-readable error text.
         */
        private final String errorText;

        /**
         * Constructs a new error reply.
         *
         * @param errorCode the numeric error code
         * @param errorText the optional human-readable text; may be
         *                  {@code null}
         */
        public Error(int errorCode, String errorText) {
            this.errorCode = errorCode;
            this.errorText = errorText;
        }

        /**
         * Returns the numeric error code.
         *
         * @return the error code
         */
        public int errorCode() {
            return errorCode;
        }

        /**
         * Returns the optional human-readable error text.
         *
         * @return an {@link Optional} carrying the error text
         */
        public Optional<String> errorText() {
            return Optional.ofNullable(errorText);
        }

        /**
         * Tries to parse an {@link Error} variant from the given
         * inbound stanza.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant
         */
        @WhatsAppWebExport(moduleName = "WASmaxInBotBotListResponseError",
                exports = "parseBotListResponseError",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Error> of(Node node, Node request) {
            // The bot Error disjunction mixes 400/403/405 (client-range) and 500
            // (server-range), so try both helpers and accept whichever envelope matches.
            var serverEnvelope = SmaxBaseServerErrorMixin.parseServerError(node, request).orElse(null);
            if (serverEnvelope != null) {
                return Optional.of(new Error(serverEnvelope.code(), serverEnvelope.text()));
            }
            var clientEnvelope = SmaxBaseServerErrorMixin.parseClientError(node, request).orElse(null);
            if (clientEnvelope != null) {
                return Optional.of(new Error(clientEnvelope.code(), clientEnvelope.text()));
            }
            return Optional.empty();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (Error) obj;
            return this.errorCode == that.errorCode && Objects.equals(this.errorText, that.errorText);
        }

        @Override
        public int hashCode() {
            return Objects.hash(errorCode, errorText);
        }

        @Override
        public String toString() {
            return "SmaxBotBotListResponse.Error[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }
}
