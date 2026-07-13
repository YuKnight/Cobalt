package com.github.auties00.cobalt.log;

import com.github.auties00.cobalt.model.jid.Jid;

/**
 * A logging-parameter wrapper that carries a sensitive value together with the {@link Kind} of secret it
 * holds, so the logging pipeline renders a redacted, non-reversible token in place of the raw value.
 *
 * <p>Cobalt's type-dispatched redaction (see {@link LogRedactor}) recognises sensitive domain types such as
 * {@link Jid} and raw {@code byte[]} key material on its own, because their runtime type is enough to know
 * they are sensitive. A value that flows through a generic carrier is not: a phone number held as a
 * {@code String} or {@code long}, an access token, a one-time code, or an email address is indistinguishable
 * from a harmless identifier at the log call site. Wrapping such a value through one of the {@code Log}
 * factory methods ({@link Log#phone(String)}, {@link Log#token(String)}, {@link Log#code(String)},
 * {@link Log#email(String)}, {@link Log#jid(String)}, {@link Log#secret(Object)}) tags it with a {@link Kind}
 * so the formatter can mask it while keeping the output debuggable.
 *
 * <p>The wrapper is safe to use anywhere, not only inside Cobalt's own formatter: its {@link #toString()}
 * already yields the redacted token, so string concatenation, exception messages, and third-party
 * {@link System.LoggerFinder} bridges all observe the masked form. When redaction is disabled (see
 * {@link LogRedactor#enabled()}), {@link #toString()} yields the raw value so a trusted developer can read it
 * during local debugging.
 */
public final class LogRedacted {
    /**
     * Classifies the sort of secret a {@link LogRedacted} holds, which selects how {@link LogRedactor} renders it.
     */
    enum Kind {
        /**
         * A phone number in {@code E.164} or bare-digit form, carried as a {@code String} or {@code long}.
         */
        PHONE,
        /**
         * An email address.
         */
        EMAIL,
        /**
         * A one-time verification, registration, or device-pairing code.
         */
        CODE,
        /**
         * An access token, session token, invite code, or other opaque credential string.
         */
        TOKEN,
        /**
         * A {@link Jid} held as a {@code String} rather than as a {@link Jid} instance, so that type
         * dispatch cannot recognise it.
         */
        JID,
        /**
         * Any other secret that has no more specific classification.
         */
        SECRET
    }

    /**
     * The raw sensitive value, or {@code null}.
     */
    private final Object value;

    /**
     * The classification that selects the rendering.
     */
    private final Kind kind;

    /**
     * Creates a wrapper around {@code value} classified as {@code kind}.
     *
     * @param value the raw sensitive value, or {@code null}
     * @param kind  the classification that selects how the value is rendered
     */
    LogRedacted(Object value, Kind kind) {
        this.value = value;
        this.kind = kind;
    }

    /**
     * Returns the raw wrapped value.
     *
     * @return the wrapped value, or {@code null}
     */
    Object value() {
        return value;
    }

    /**
     * Returns the classification of the wrapped value.
     *
     * @return the kind
     */
    Kind kind() {
        return kind;
    }

    /**
     * Returns the redacted token for the wrapped value, or the raw value when redaction is disabled.
     *
     * @return the rendered string produced by {@link LogRedactor#render(LogRedacted)}
     */
    @Override
    public String toString() {
        return LogRedactor.render(this);
    }
}
