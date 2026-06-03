package com.github.auties00.cobalt.model.cloud;

import java.util.Objects;

/**
 * A version of Meta's Graph API that the WhatsApp Cloud API client can target.
 *
 * <p>Every Cloud API request is addressed to a versioned graph base of the form
 * {@code https://graph.facebook.com/<version>/}. Meta releases a new version roughly twice a year and
 * supports each one for about two years from its release; a request to a retired version is served by
 * the oldest still-supported version. The known versions are exposed as constants on this type, and
 * arbitrary versions can be constructed directly.
 *
 * <p>{@link #DEFAULT} names the version the client targets when none is selected; it tracks the latest
 * stable version. {@link #V23_0} is the version Meta's published OpenAPI specification is generated
 * from and the version Cobalt's request and webhook shapes were validated against; because Graph API
 * versions are backward compatible, those shapes remain valid against newer versions.
 *
 * @param version the URL segment this version maps to, for example {@code v23.0}
 */
public record CloudApiVersion(String version) {
    /**
     * Graph API {@code v19.0}, released January 2024.
     */
    public static final CloudApiVersion V19_0 = new CloudApiVersion("v19.0");

    /**
     * Graph API {@code v20.0}, released May 2024.
     */
    public static final CloudApiVersion V20_0 = new CloudApiVersion("v20.0");

    /**
     * Graph API {@code v21.0}, released October 2024.
     */
    public static final CloudApiVersion V21_0 = new CloudApiVersion("v21.0");

    /**
     * Graph API {@code v22.0}, released January 2025.
     */
    public static final CloudApiVersion V22_0 = new CloudApiVersion("v22.0");

    /**
     * Graph API {@code v23.0}, released May 2025; the version the published OpenAPI specification is
     * generated from.
     */
    public static final CloudApiVersion V23_0 = new CloudApiVersion("v23.0");

    /**
     * Graph API {@code v24.0}, released October 2025.
     */
    public static final CloudApiVersion V24_0 = new CloudApiVersion("v24.0");

    /**
     * Graph API {@code v25.0}, released February 2026.
     */
    public static final CloudApiVersion V25_0 = new CloudApiVersion("v25.0");

    /**
     * The latest version Cobalt is aware of.
     */
    public static final CloudApiVersion LATEST = V25_0;

    /**
     * The version the client targets when none is selected; tracks {@link #LATEST}.
     */
    public static final CloudApiVersion DEFAULT = LATEST;

    /**
     * Validates the canonical constructor's component.
     *
     * @throws NullPointerException if {@code version} is {@code null}
     */
    public CloudApiVersion {
        Objects.requireNonNull(version, "version must not be null");
    }
}
