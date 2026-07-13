package com.github.auties00.cobalt.log;

import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.stanza.Stanza;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

/**
 * Turns a log parameter into a redacted, non-reversible, still-correlatable token.
 *
 * <p>Cobalt's logging must not leak the phone numbers, JIDs, message content, and cryptographic material that
 * flow through the client, yet a redacted log is only useful if it stays debuggable. This class reconciles the
 * two with a keyed fingerprint: a value is rendered as a short token derived from
 * {@code SHA-256(salt || value)}, so the <em>same</em> value always renders to the <em>same</em> token and two
 * <em>distinct</em> values render to <em>distinct</em> tokens, letting a reader correlate occurrences across
 * lines without ever seeing the real value. The salt is a per-process random value by default, which matters
 * for low-entropy secrets such as phone numbers: an unsalted hash of a phone number is trivially reversed by
 * enumerating the number space, whereas a per-process salt blocks that while preserving within-run
 * correlation. Set {@link #SALT_PROPERTY} to pin the salt when correlation across two runs (two log files) is
 * needed instead.
 *
 * <p>Redaction is <strong>type-dispatched</strong>, never reflective: {@link #redact(Object)} masks the small
 * set of types whose own {@code toString()} would leak ({@link Jid}, raw {@code byte[]}, {@link Stanza}) plus
 * the {@link LogRedacted} wrappers produced by the {@code Log} factories, and passes everything else through
 * unchanged. This is safe because Cobalt's domain types are protobuf messages with no {@code toString()}, so
 * an un-dispatched object renders as an identity hash ({@code ClassName@hash}) and leaks nothing on its own;
 * sensitive data that hides inside a generic {@code String}/{@code long} must be wrapped at the call site (see
 * {@link LogRedacted}).
 *
 * <p>Redaction is on by default and disabled by setting {@link #REDACT_PROPERTY} (or the
 * {@link #REDACT_ENV_VARIABLE} environment variable) to {@code false}, which turns every rendering into a
 * pass-through of the raw value for trusted local debugging.
 */
public final class LogRedactor {
    /**
     * The system property that disables redaction when set to {@code false}, checked before
     * {@link #REDACT_ENV_VARIABLE}.
     */
    public static final String REDACT_PROPERTY = "cobalt.log.redact";

    /**
     * The environment variable that disables redaction when set to {@code false} and {@link #REDACT_PROPERTY}
     * is unset.
     */
    public static final String REDACT_ENV_VARIABLE = "COBALT_LOG_REDACT";

    /**
     * The system property that pins the fingerprint salt to a fixed value, enabling correlation of tokens
     * across separate runs; when unset, a fresh per-process random salt is used.
     */
    public static final String SALT_PROPERTY = "cobalt.log.redact.salt";

    /**
     * The number of leading digest bytes rendered as the hexadecimal fingerprint, giving a six-character
     * token that is short enough to read yet wide enough (2^24 values) to make accidental collisions rare in
     * a single run.
     */
    private static final int FINGERPRINT_BYTES = 3;

    /**
     * The keyed-hash salt mixed into every fingerprint; a fixed value from {@link #SALT_PROPERTY} when set,
     * otherwise a per-process random value.
     */
    private static final byte[] SALT = computeSalt();

    /**
     * Whether redaction is currently active; initialised from {@link #REDACT_PROPERTY} and adjustable at
     * runtime through {@link #setEnabled(boolean)}.
     */
    private static volatile boolean enabled = computeEnabled();

    /**
     * Prevents instantiation of this static-only holder.
     *
     * @throws AssertionError always, since the class is never meant to be instantiated
     */
    private LogRedactor() {
        throw new AssertionError("No instances");
    }

    /**
     * Returns whether redaction is currently active.
     *
     * @return {@code true} when parameters are masked, {@code false} when they pass through raw
     */
    public static boolean enabled() {
        return enabled;
    }

    /**
     * Sets whether redaction is active, overriding the {@link #REDACT_PROPERTY} default.
     *
     * @param value {@code true} to mask parameters, {@code false} to pass them through raw
     */
    static void setEnabled(boolean value) {
        enabled = value;
    }

    /**
     * Returns a redacted surrogate for {@code value} suitable for message formatting, or {@code value} itself
     * when it needs no masking or when redaction is disabled.
     *
     * <p>Dispatch is by runtime type: {@link LogRedacted} wrappers, {@link Jid}s, raw {@code byte[]} arrays, and
     * {@link Stanza}s are masked; every other value passes through unchanged, which is safe because Cobalt's
     * other loggable types render as an identity hash rather than exposing their fields.
     *
     * @param value the log parameter to redact, or {@code null}
     * @return the masked surrogate, or the original value when no masking applies
     */
    static Object redact(Object value) {
        if (!enabled || value == null) {
            return value;
        }
        return switch (value) {
            case LogRedacted redacted -> redacted.toString();
            case Jid jid -> redactJid(jid);
            case byte[] bytes -> redactBytes(bytes);
            case Stanza stanza -> redactStanza(stanza);
            default -> value;
        };
    }

    /**
     * Renders a {@link LogRedacted} wrapper as its masked token, or as the raw value when redaction is disabled.
     *
     * @param redacted the wrapper to render; must not be {@code null}
     * @return the masked token, or the raw value string when redaction is disabled or the value is
     * {@code null}
     */
    static String render(LogRedacted redacted) {
        var value = redacted.value();
        if (value == null) {
            return "null";
        }
        if (!enabled) {
            return String.valueOf(value);
        }
        return switch (redacted.kind()) {
            case PHONE -> "phone(#" + fingerprint(phoneBytes(value)) + ")";
            case EMAIL -> "email(#" + fingerprint(stringBytes(value)) + ")";
            case CODE -> "code(#" + fingerprint(stringBytes(value)) + ")";
            case TOKEN -> "token(len=" + String.valueOf(value).length() + ",#" + fingerprint(stringBytes(value)) + ")";
            case JID -> redactJidString(String.valueOf(value));
            case SECRET -> "secret(#" + fingerprint(secretBytes(value)) + ")";
        };
    }

    /**
     * Redacts a {@link Jid} by keeping the routing structure and hashing only the user component.
     *
     * <p>The server, agent, and device components are protocol routing information rather than user data, so
     * they are preserved; only the {@linkplain Jid#user() user} part (a phone number, a LID, or a group or
     * newsletter identifier) is replaced by a fingerprint. Server-only JIDs (those with no user) carry no user
     * data and are returned verbatim.
     *
     * @param jid the JID to redact; must not be {@code null}
     * @return the redacted string, mirroring {@link Jid#toString()} with the user part replaced by a
     * fingerprint, for example {@code #a3f91c:7@lid}
     */
    static String redactJid(Jid jid) {
        var user = jid.user();
        if (user == null) {
            return jid.toString();
        }
        var result = new StringBuilder();
        result.append('#').append(fingerprint(user.getBytes(StandardCharsets.UTF_8)));
        if (jid.hasAgent()) {
            result.append('_').append(jid.agent());
        }
        if (jid.hasDevice()) {
            result.append(':').append(jid.device());
        }
        result.append('@').append(jid.server());
        return result.toString();
    }

    /**
     * Redacts a JID carried as a {@code String}, parsing it and delegating to {@link #redactJid(Jid)}.
     *
     * @param jid the JID string to redact
     * @return the redacted JID string, or a generic {@code jid(#...)} token when the string does not parse
     */
    private static String redactJidString(String jid) {
        try {
            var parsed = Jid.of(jid);
            return parsed == null ? "null" : redactJid(parsed);
        } catch (RuntimeException ignored) {
            return "jid(#" + fingerprint(jid.getBytes(StandardCharsets.UTF_8)) + ")";
        }
    }

    /**
     * Redacts a raw byte array, preserving its length but never its content.
     *
     * @param bytes the array to redact; must not be {@code null}
     * @return a token of the form {@code bytes(len=N,#fingerprint)}
     */
    static String redactBytes(byte[] bytes) {
        return "bytes(len=" + bytes.length + ",#" + fingerprint(bytes) + ")";
    }

    /**
     * Redacts a {@link Stanza} to a structural summary that reveals its shape but none of its values.
     *
     * <p>The summary keeps the description, the attribute keys, the child count, and whether a content slot is
     * present, all of which are protocol structure. Attribute values, textual and binary content, and the
     * recursive contents of child nodes are omitted entirely, since any of them can carry a JID, a token, or
     * message payload.
     *
     * @param stanza the stanza to redact; must not be {@code null}
     * @return a summary of the form {@code stanza(description attrs=[k1, k2] children=N)}
     */
    static String redactStanza(Stanza stanza) {
        var result = new StringBuilder("stanza(");
        result.append(stanza.description());
        var attributes = stanza.attributes();
        if (!attributes.isEmpty()) {
            result.append(" attrs=").append(attributes.keySet());
        }
        var children = stanza.children().size();
        if (children > 0) {
            result.append(" children=").append(children);
        } else if (stanza.hasContent()) {
            result.append(" content=<redacted>");
        }
        return result.append(')').toString();
    }

    /**
     * Computes the fingerprint of {@code data} as the first {@link #FINGERPRINT_BYTES} bytes of
     * {@code SHA-256(salt || data)}, rendered as lowercase hexadecimal.
     *
     * @param data the bytes to fingerprint; must not be {@code null}
     * @return the six-character hexadecimal fingerprint
     * @throws AssertionError if the platform lacks the {@code SHA-256} digest, which every conformant JRE
     *                        provides
     */
    static String fingerprint(byte[] data) {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            digest.update(SALT);
            digest.update(data);
            return HexFormat.of().formatHex(digest.digest(), 0, FINGERPRINT_BYTES);
        } catch (NoSuchAlgorithmException exception) {
            throw new AssertionError("SHA-256 is required but unavailable", exception);
        }
    }

    /**
     * Returns the UTF-8 bytes of a phone number, dropping a single leading {@code +} so the {@code E.164} and
     * bare-digit forms of the same number fingerprint identically.
     *
     * @param value the phone number value; must not be {@code null}
     * @return the normalised UTF-8 bytes
     */
    private static byte[] phoneBytes(Object value) {
        var text = String.valueOf(value);
        if (!text.isEmpty() && text.charAt(0) == '+') {
            text = text.substring(1);
        }
        return text.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Returns the UTF-8 bytes of a value's string form.
     *
     * @param value the value; must not be {@code null}
     * @return the UTF-8 bytes of {@link String#valueOf(Object)}
     */
    private static byte[] stringBytes(Object value) {
        return String.valueOf(value).getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Returns the bytes to fingerprint for a generic secret, using the array itself when the value is a
     * {@code byte[]} and its string form otherwise.
     *
     * @param value the secret value; must not be {@code null}
     * @return the bytes to fingerprint
     */
    private static byte[] secretBytes(Object value) {
        return value instanceof byte[] bytes ? bytes : stringBytes(value);
    }

    /**
     * Reads the initial redaction state from {@link #REDACT_PROPERTY}, falling back to
     * {@link #REDACT_ENV_VARIABLE}.
     *
     * @return {@code false} only when one of the two sources is set to {@code false} (case-insensitively),
     * {@code true} otherwise
     */
    private static boolean computeEnabled() {
        var raw = System.getProperty(REDACT_PROPERTY);
        if (raw == null || raw.isBlank()) {
            raw = System.getenv(REDACT_ENV_VARIABLE);
        }
        return raw == null || !raw.trim().equalsIgnoreCase("false");
    }

    /**
     * Computes the fingerprint salt from {@link #SALT_PROPERTY} when set, otherwise generates a fresh
     * 16-byte per-process random salt.
     *
     * @return the salt bytes
     */
    private static byte[] computeSalt() {
        var configured = System.getProperty(SALT_PROPERTY);
        if (configured != null && !configured.isBlank()) {
            return configured.getBytes(StandardCharsets.UTF_8);
        }
        var salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return salt;
    }
}
