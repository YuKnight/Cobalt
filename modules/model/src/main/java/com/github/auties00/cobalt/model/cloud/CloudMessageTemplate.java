package com.github.auties00.cobalt.model.cloud;

import com.alibaba.fastjson2.JSONArray;

import java.util.Objects;
import java.util.Optional;

/**
 * A registered WhatsApp Cloud API message template.
 *
 * <p>Templates are created under a WhatsApp Business Account, approved by Meta, and then referenced by
 * name and language when sending. This model carries the management view: the server-assigned id, the
 * name, the BCP-47 language code, the category ({@code MARKETING}, {@code UTILITY},
 * {@code AUTHENTICATION}), the review status, and the components definition (header, body, footer, and
 * buttons) as a JSON array matching the Cloud API {@code components} shape.
 *
 * <p>Sending a message that uses an approved template is done through the universal message model
 * rather than this type: build a template message container and pass it to the client's send method.
 * This type governs the template library, not the act of sending.
 */
public final class CloudMessageTemplate {
    /**
     * The server-assigned template id, or {@code null} before creation.
     */
    private final String id;

    /**
     * The template name.
     */
    private final String name;

    /**
     * The BCP-47 language code, for example {@code en_US}.
     */
    private final String language;

    /**
     * The template category, for example {@code MARKETING}.
     */
    private final String category;

    /**
     * The review status ({@code APPROVED}, {@code PENDING}, {@code REJECTED}), or {@code null} before
     * review.
     */
    private final String status;

    /**
     * The components definition as a JSON array matching the Cloud API {@code components} shape, or
     * {@code null} when unset.
     */
    private final JSONArray components;

    /**
     * Constructs a new message template.
     *
     * @param id         the server-assigned id, or {@code null} before creation
     * @param name       the template name
     * @param language   the BCP-47 language code
     * @param category   the template category
     * @param status     the review status, or {@code null} before review
     * @param components the components definition, or {@code null} when unset
     * @throws NullPointerException if {@code name}, {@code language}, or {@code category} is
     *                              {@code null}
     */
    public CloudMessageTemplate(String id, String name, String language, String category, String status,
                                JSONArray components) {
        this.id = id;
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.language = Objects.requireNonNull(language, "language must not be null");
        this.category = Objects.requireNonNull(category, "category must not be null");
        this.status = status;
        this.components = components;
    }

    /**
     * Returns the server-assigned template id.
     *
     * @return an {@link Optional} carrying the id, or empty before creation
     */
    public Optional<String> id() {
        return Optional.ofNullable(id);
    }

    /**
     * Returns the template name.
     *
     * @return the name
     */
    public String name() {
        return name;
    }

    /**
     * Returns the BCP-47 language code.
     *
     * @return the language code
     */
    public String language() {
        return language;
    }

    /**
     * Returns the template category.
     *
     * @return the category
     */
    public String category() {
        return category;
    }

    /**
     * Returns the review status.
     *
     * @return an {@link Optional} carrying the status, or empty before review
     */
    public Optional<String> status() {
        return Optional.ofNullable(status);
    }

    /**
     * Returns the components definition.
     *
     * @return an {@link Optional} carrying the components JSON array, or empty when unset
     */
    public Optional<JSONArray> components() {
        return Optional.ofNullable(components);
    }
}
