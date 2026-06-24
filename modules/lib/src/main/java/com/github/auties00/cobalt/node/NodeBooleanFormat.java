package com.github.auties00.cobalt.node;

import java.util.Objects;

/**
 * Strategy that translates a stanza boolean to and from its on-the-wire string
 * form.
 *
 * <p>The WhatsApp binary-XMPP protocol has no dedicated boolean wire shape:
 * booleans travel as plain attribute or content strings, and the literal that
 * denotes truth differs by stanza family. Metadata stanzas (presence, devices,
 * business profile) carry {@code "true"} and {@code "false"}; the calling/voip
 * and payment stanzas carry {@code "1"} and {@code "0"}. The WhatsApp Web
 * client never centralises this: each parse site inlines its own comparison
 * (for example {@code attrString("enabled") === "true"} or
 * {@code attrString("invite-used") === "1"}). This interface lifts that inlined
 * decision into a value so the {@code getAttributeAsBool}/{@code toContentBool}
 * readers on {@link Node} and the boolean writers on {@link NodeBuilder} can be
 * pointed at the convention a given stanza actually uses.
 *
 * <p>Three predefined formats cover the conventions observed on the wire:
 * <ul>
 *   <li>{@link #LENIENT} decodes permissively and is the default used by every
 *       no-format reader and writer overload.
 *   <li>{@link #BOOLEAN} is the strict {@code "true"}/{@code "false"} form.
 *   <li>{@link #NUMERIC} is the strict {@code "1"}/{@code "0"} form.
 * </ul>
 *
 * <p>The interface is not sealed; a caller may supply a custom format for a
 * stanza family whose convention none of the predefined constants match.
 *
 * @implNote
 * This implementation keeps {@link #LENIENT}'s decode a strict superset of
 * {@link Boolean#parseBoolean(String)} (it additionally accepts {@code "1"}),
 * so routing the previously {@code parseBoolean}-backed readers through
 * {@link #LENIENT} can only widen, never narrow, the set of strings that read
 * as {@code true}.
 *
 * @see Node
 * @see NodeBuilder
 */
public interface NodeBooleanFormat {
    /**
     * Permissive format that recognises every truthy convention found on the
     * wire.
     *
     * <p>Decodes {@code "true"} (in any letter case) and {@code "1"} to
     * {@code true} and every other string, including {@code "false"} and
     * {@code "0"}, to {@code false}. Encodes through the
     * {@code "true"}/{@code "false"} pair. This is the default applied by the
     * {@link Node} readers and {@link NodeBuilder} writers that take no explicit
     * format, so a reader cannot mis-decode a {@code "1"} simply because its
     * stanza family was not classified.
     */
    NodeBooleanFormat LENIENT = new LenientNodeBooleanFormat();

    /**
     * Strict {@code "true"}/{@code "false"} format.
     *
     * <p>Decodes exactly the literal {@code "true"} (case-sensitive) to
     * {@code true} and every other string to {@code false}; encodes through the
     * {@code "true"}/{@code "false"} pair. Matches the convention WhatsApp Web
     * applies to metadata stanzas, where the inlined check is
     * {@code value === "true"}.
     */
    NodeBooleanFormat BOOLEAN = new LiteralNodeBooleanFormat("true", "false");

    /**
     * Strict {@code "1"}/{@code "0"} format.
     *
     * <p>Decodes exactly the literal {@code "1"} to {@code true} and every
     * other string to {@code false}; encodes through the {@code "1"}/{@code "0"}
     * pair. Matches the convention WhatsApp Web applies to the calling/voip and
     * payment stanzas, where the inlined check is {@code value === "1"}.
     */
    NodeBooleanFormat NUMERIC = new LiteralNodeBooleanFormat("1", "0");

    /**
     * Decodes the supplied wire string into a boolean under this format.
     *
     * <p>Called by the {@link Node} boolean readers with the textual view of an
     * attribute or content slot that is already known to be present, so
     * {@code value} is never {@code null} in normal use.
     *
     * @param value the wire string to interpret
     * @return {@code true} when {@code value} denotes truth under this format,
     *         {@code false} otherwise
     * @throws NullPointerException if {@code value} is {@code null} and the
     *                              format dereferences it
     */
    boolean decode(String value);

    /**
     * Encodes the supplied boolean into its wire string under this format.
     *
     * <p>Called by the {@link NodeBuilder} boolean writers to choose the literal
     * placed on the wire.
     *
     * @param value the boolean to serialise
     * @return the non null wire string for {@code value}
     */
    String encode(boolean value);
}

/**
 * Permissive {@link NodeBooleanFormat} backing {@link NodeBooleanFormat#LENIENT}.
 *
 * <p>Treats {@code "true"} (any letter case) and {@code "1"} as truthy and
 * encodes through the {@code "true"}/{@code "false"} pair, matching the literal
 * shape the builder emitted before formats were configurable.
 */
final class LenientNodeBooleanFormat implements NodeBooleanFormat {
    /**
     * Builds the singleton lenient format.
     *
     * <p>Package-private because the only intended instance is the
     * {@link NodeBooleanFormat#LENIENT} constant.
     */
    LenientNodeBooleanFormat() {
    }

    @Override
    public boolean decode(String value) {
        return "true".equalsIgnoreCase(value) || "1".equals(value);
    }

    @Override
    public String encode(boolean value) {
        return value ? "true" : "false";
    }
}

/**
 * Strict {@link NodeBooleanFormat} that maps each boolean to one fixed literal,
 * backing {@link NodeBooleanFormat#BOOLEAN} and {@link NodeBooleanFormat#NUMERIC}.
 *
 * <p>Decodes by case-sensitive equality against the true literal so only the
 * exact wire token a stanza family uses reads as {@code true}; encodes by
 * selecting the true or false literal.
 */
final class LiteralNodeBooleanFormat implements NodeBooleanFormat {
    /**
     * Holds the literal that denotes {@code true} on the wire.
     */
    private final String trueLiteral;

    /**
     * Holds the literal that denotes {@code false} on the wire.
     */
    private final String falseLiteral;

    /**
     * Builds a literal format over the supplied true and false tokens.
     *
     * @param trueLiteral  the wire token denoting {@code true}
     * @param falseLiteral the wire token denoting {@code false}
     * @throws NullPointerException if either literal is {@code null}
     */
    LiteralNodeBooleanFormat(String trueLiteral, String falseLiteral) {
        this.trueLiteral = Objects.requireNonNull(trueLiteral, "trueLiteral cannot be null");
        this.falseLiteral = Objects.requireNonNull(falseLiteral, "falseLiteral cannot be null");
    }

    @Override
    public boolean decode(String value) {
        return trueLiteral.equals(value);
    }

    @Override
    public String encode(boolean value) {
        return value ? trueLiteral : falseLiteral;
    }
}
