package com.github.auties00.cobalt.node.usync.result;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Success result of {@code WAWebUsyncBotProfile.botProfileParser}.
 *
 * <p>Carries the bot's profile metadata. Several fields are nullable in the
 * wire response (creator name, creator profile URL, "is meta-created" flag,
 * "posing as professional" classification) and are exposed via
 * {@link Optional}-returning accessors.
 */
@WhatsAppWebModule(moduleName = "WAWebUsyncBotProfile")
public final class BotProfileResult implements UsyncProtocolResponse {
    /**
     * Holds the bot's display name. Never {@code null} and defaults to the
     * empty string when the relay omits the {@code <name>} child.
     */
    private final String name;

    /**
     * Holds the opaque attribute string used by the rendering tier, carried
     * through verbatim. Never {@code null} and defaults to the empty string.
     */
    private final String attributes;

    /**
     * Holds the bot's profile description. Never {@code null} and defaults
     * to the empty string.
     */
    private final String description;

    /**
     * Holds the bot's category label. Never {@code null} and defaults to the
     * empty string.
     */
    private final String category;

    /**
     * Tracks whether the bot is the default suggestion for new conversations.
     */
    private final boolean isDefault;

    /**
     * Holds the suggested prompts to help the user start chatting. Never
     * {@code null} and defaults to the empty list.
     */
    private final List<Prompt> prompts;

    /**
     * Holds the bot's persona id (free-form string). Never {@code null} and
     * defaults to the empty string.
     */
    private final String personaId;

    /**
     * Holds the slash-command list. Never {@code null} and defaults to the
     * empty list.
     */
    private final List<Command> commands;

    /**
     * Holds the free-form blurb above the command list. Never {@code null}
     * and defaults to the empty string.
     */
    private final String commandsDescription;

    /**
     * Tracks whether Meta authored the bot, or {@code null} when the
     * {@code <is_meta_created>} child is absent.
     */
    private final Boolean isMetaCreated;

    /**
     * Holds the human creator's display name, or {@code null} when absent.
     */
    private final String creatorName;

    /**
     * Holds the human creator's profile URL, or {@code null} when absent.
     */
    private final String creatorProfileUrl;

    /**
     * Holds the bot's "posing as professional" classification, or
     * {@code null} when absent.
     */
    private final PosingAsProfessional posingAsProfessional;

    /**
     * Creates a new bot-profile result. Tolerates {@code null} on every field
     * except the four required strings.
     *
     * @param name                 the display name; must not be {@code null}
     * @param attributes           the attribute string; must not be
     *                             {@code null}
     * @param description          the profile description; must not be
     *                             {@code null}
     * @param category             the category label; must not be {@code null}
     * @param isDefault            whether the bot is the default suggestion
     * @param prompts              the suggested prompts; defaults to the
     *                             empty list when {@code null}
     * @param personaId            the persona id; must not be {@code null}
     * @param commands             the command list; defaults to the empty
     *                             list when {@code null}
     * @param commandsDescription  the commands description; must not be
     *                             {@code null}
     * @param isMetaCreated        whether Meta authored the bot, or
     *                             {@code null}
     * @param creatorName          the creator's display name, or {@code null}
     * @param creatorProfileUrl    the creator's profile URL, or {@code null}
     * @param posingAsProfessional the posing-as-professional classification,
     *                             or {@code null}
     */
    public BotProfileResult(
            String name,
            String attributes,
            String description,
            String category,
            boolean isDefault,
            List<Prompt> prompts,
            String personaId,
            List<Command> commands,
            String commandsDescription,
            Boolean isMetaCreated,
            String creatorName,
            String creatorProfileUrl,
            PosingAsProfessional posingAsProfessional) {
        this.name = Objects.requireNonNull(name, "name cannot be null");
        this.attributes = Objects.requireNonNull(attributes, "attributes cannot be null");
        this.description = Objects.requireNonNull(description, "description cannot be null");
        this.category = Objects.requireNonNull(category, "category cannot be null");
        this.isDefault = isDefault;
        this.prompts = prompts == null ? List.of() : List.copyOf(prompts);
        this.personaId = Objects.requireNonNull(personaId, "personaId cannot be null");
        this.commands = commands == null ? List.of() : List.copyOf(commands);
        this.commandsDescription = Objects.requireNonNull(commandsDescription, "commandsDescription cannot be null");
        this.isMetaCreated = isMetaCreated;
        this.creatorName = creatorName;
        this.creatorProfileUrl = creatorProfileUrl;
        this.posingAsProfessional = posingAsProfessional;
    }

    /**
     * Returns the bot's display name.
     *
     * @return the display name
     */
    public String name() {
        return name;
    }

    /**
     * Returns the opaque attributes string.
     *
     * @return the attributes string
     */
    public String attributes() {
        return attributes;
    }

    /**
     * Returns the bot's description.
     *
     * @return the description
     */
    public String description() {
        return description;
    }

    /**
     * Returns the bot's category.
     *
     * @return the category
     */
    public String category() {
        return category;
    }

    /**
     * Returns whether the bot is the default suggestion.
     *
     * @return {@code true} when the bot is the default
     */
    public boolean isDefault() {
        return isDefault;
    }

    /**
     * Returns the suggested-prompts list.
     *
     * @return the prompts, never {@code null}
     */
    public List<Prompt> prompts() {
        return prompts;
    }

    /**
     * Returns the persona id.
     *
     * @return the persona id
     */
    public String personaId() {
        return personaId;
    }

    /**
     * Returns the slash-command list.
     *
     * @return the commands, never {@code null}
     */
    public List<Command> commands() {
        return commands;
    }

    /**
     * Returns the free-form blurb above the command list.
     *
     * @return the commands description
     */
    public String commandsDescription() {
        return commandsDescription;
    }

    /**
     * Returns whether Meta authored the bot, when present.
     *
     * @return the meta-created flag
     */
    public Optional<Boolean> isMetaCreated() {
        return Optional.ofNullable(isMetaCreated);
    }

    /**
     * Returns the human creator's display name, when present.
     *
     * @return the creator name
     */
    public Optional<String> creatorName() {
        return Optional.ofNullable(creatorName);
    }

    /**
     * Returns the human creator's profile URL, when present.
     *
     * @return the creator profile URL
     */
    public Optional<String> creatorProfileUrl() {
        return Optional.ofNullable(creatorProfileUrl);
    }

    /**
     * Returns the "posing as professional" classification, when present.
     *
     * @return the classification
     */
    public Optional<PosingAsProfessional> posingAsProfessional() {
        return Optional.ofNullable(posingAsProfessional);
    }

    /**
     * One suggested prompt that the UI displays as a starter for the user to
     * send to the bot.
     */
    @WhatsAppWebModule(moduleName = "WAWebUsyncBotProfile")
    public static final class Prompt {
        /**
         * Holds the leading emoji glyph. Never {@code null} and defaults to
         * the empty string.
         */
        private final String emoji;

        /**
         * Holds the prompt text. Never {@code null} and defaults to the
         * empty string.
         */
        private final String text;

        /**
         * Creates a new prompt.
         *
         * @param emoji the emoji; must not be {@code null}
         * @param text  the text; must not be {@code null}
         */
        public Prompt(String emoji, String text) {
            this.emoji = Objects.requireNonNull(emoji, "emoji cannot be null");
            this.text = Objects.requireNonNull(text, "text cannot be null");
        }

        /**
         * Returns the leading emoji glyph.
         *
         * @return the emoji
         */
        public String emoji() {
            return emoji;
        }

        /**
         * Returns the prompt text.
         *
         * @return the text
         */
        public String text() {
            return text;
        }
    }

    /**
     * One slash-command supported by the bot.
     */
    @WhatsAppWebModule(moduleName = "WAWebUsyncBotProfile")
    public static final class Command {
        /**
         * Holds the slash-command identifier. Never {@code null}.
         */
        private final String name;

        /**
         * Holds the description shown in the picker. Never {@code null}.
         */
        private final String description;

        /**
         * Creates a new command descriptor.
         *
         * @param name        the slash-command identifier
         * @param description the picker description
         */
        public Command(String name, String description) {
            this.name = Objects.requireNonNull(name, "name cannot be null");
            this.description = Objects.requireNonNull(description, "description cannot be null");
        }

        /**
         * Returns the slash-command identifier.
         *
         * @return the identifier
         */
        public String name() {
            return name;
        }

        /**
         * Returns the picker description.
         *
         * @return the description
         */
        public String description() {
            return description;
        }
    }

    /**
     * Tristate flag for the bot's "posing as professional" classification.
     *
     * @implNote Mirrors {@code WAWebBotTypes.BotPosingAsProfessionalType}.
     */
    @WhatsAppWebModule(moduleName = "WAWebBotTypes")
    public enum PosingAsProfessional {
        /**
         * The relay returned {@code type="unknown"}.
         */
        UNKNOWN,
        /**
         * The relay returned {@code type="yes"}.
         */
        YES,
        /**
         * The relay returned {@code type="no"}.
         */
        NO
    }
}
