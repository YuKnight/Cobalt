package com.github.auties00.cobalt.node;

import com.github.auties00.cobalt.exception.WhatsAppMalformedJidException;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.model.jid.Jid;
import it.auties.protobuf.model.ProtobufString;

import java.util.*;

/**
 * Represents the value of a single attribute attached to a WhatsApp
 * protocol {@link Node}.
 *
 * <p>Every stanza exchanged between a WhatsApp client and the server is a
 * tree of {@link Node}s whose elements may carry key/value attributes.
 * Attribute keys are always strings, but values may be plain text, binary
 * blobs, or JIDs that identify users, groups, or devices. This sealed
 * interface enumerates those three concrete value shapes and exposes a
 * uniform set of conversion helpers so callers can read an attribute as
 * the type they expect without special-casing on the underlying
 * representation.
 *
 * <p>Three concrete variants are permitted:
 * <ul>
 *   <li>{@link TextAttribute} for plain UTF-8 strings.</li>
 *   <li>{@link JidAttribute} for WhatsApp identifier values.</li>
 *   <li>{@link BytesAttribute} for raw binary data such as hashes or
 *       encoded tokens.</li>
 * </ul>
 *
 * @implNote WAWap: the on-wire attribute shape and encoding. WAXmlNode:
 *           the JS class whose {@code attrs} hash holds these values.
 * @see Node
 * @see Jid
 */
@WhatsAppWebModule(moduleName = "WAWap")
@WhatsAppWebModule(moduleName = "WAXmlNode")
public sealed interface NodeAttribute {
    /**
     * Returns the string representation of this attribute value.
     *
     * @return a non-null string representation
     */
    String toString();

    /**
     * Returns the byte-array representation of this attribute value.
     *
     * @return a non-null byte array
     */
    byte[] toBytes();

    /**
     * Returns the attribute value parsed as a {@link Jid}, when possible.
     *
     * @return an {@link Optional} containing the JID, or empty if the
     *         value cannot be parsed as a valid JID
     */
    Optional<Jid> toJid();

    /**
     * Returns the attribute value parsed as a {@code long}, when possible.
     *
     * @return an {@link OptionalLong} containing the parsed value, or
     *         empty if parsing fails
     */
    OptionalLong toLong();

    /**
     * Returns the attribute value parsed as an {@code int}, when possible.
     *
     * @return an {@link OptionalInt} containing the parsed value, or
     *         empty if parsing fails
     */
    OptionalInt toInt();

    /**
     * Returns the attribute value parsed as a {@code double}, when possible.
     *
     * @return an {@link OptionalDouble} containing the parsed value, or
     *         empty if parsing fails
     */
    OptionalDouble toDouble();

    /**
     * An attribute value that is stored as a plain UTF-8 string.
     *
     * <p>The most common attribute variant: identifiers, type names, state
     * flags, and any other textual metadata are encoded as text values and
     * decoded on the wire via the WAWap dictionary tokens.
     *
     * @param value the text value of this attribute, must not be null
     */
    record TextAttribute(String value) implements NodeAttribute {
        /**
         * Constructs a new text attribute, rejecting null values.
         *
         * @param value the text value
         * @throws NullPointerException if {@code value} is {@code null}
         */
        public TextAttribute {
            Objects.requireNonNull(value, "value cannot be null");
        }

        /**
         * Returns the stored string value as-is.
         *
         * @return the non-null string value
         */
        @Override
        public String toString() {
            return value;
        }

        /**
         * Returns the UTF-8 byte representation of the stored string.
         *
         * @return a non-null byte array containing the UTF-8 encoded text
         */
        @Override
        public byte[] toBytes() {
            return value.getBytes();
        }

        /**
         * Parses the stored text as a {@link Jid}.
         *
         * @return an {@link Optional} containing the parsed JID, or empty
         *         if the text is not a valid JID
         */
        @Override
        public Optional<Jid> toJid() {
            try {
                var result = Jid.of(value);
                return Optional.of(result);
            }catch (WhatsAppMalformedJidException exception) {
                return Optional.empty();
            }
        }

        /**
         * Parses the stored text as a {@code long}.
         *
         * @return an {@link OptionalLong} with the parsed value, or empty
         *         if the text is not a valid long literal
         */
        @Override
        public OptionalLong toLong() {
            try {
                var result = Long.parseLong(value);
                return OptionalLong.of(result);
            }catch (NumberFormatException exception) {
                return OptionalLong.empty();
            }
        }

        /**
         * Parses the stored text as an {@code int}.
         *
         * @return an {@link OptionalInt} with the parsed value, or empty
         *         if the text is not a valid int literal
         */
        @Override
        public OptionalInt toInt() {
            try {
                var result = Integer.parseInt(value);
                return OptionalInt.of(result);
            }catch (NumberFormatException exception) {
                return OptionalInt.empty();
            }
        }

        /**
         * Parses the stored text as a {@code double}.
         *
         * @return an {@link OptionalDouble} with the parsed value, or empty
         *         if the text is not a valid double literal
         */
        @Override
        public OptionalDouble toDouble() {
            try {
                var result = Double.parseDouble(value);
                return OptionalDouble.of(result);
            }catch (NumberFormatException exception) {
                return OptionalDouble.empty();
            }
        }
    }

    /**
     * An attribute value that holds a fully parsed {@link Jid}.
     *
     * <p>Used whenever a node attribute references a WhatsApp user, group,
     * device, or other entity. The JID is encoded on the wire in one of
     * the WAWap JID shapes (pair, AD, interop, or FB) depending on its
     * server.
     *
     * @param value the JID value, must not be null
     * @see Jid
     */
    record JidAttribute(Jid value) implements NodeAttribute {
        /**
         * Constructs a new JID attribute, rejecting null values.
         *
         * @param value the JID value
         * @throws NullPointerException if {@code value} is {@code null}
         */
        public JidAttribute {
            Objects.requireNonNull(value, "value cannot be null");
        }

        /**
         * Returns the JID serialised as its canonical string form.
         *
         * @return a non-null string representation of the JID
         */
        @Override
        public String toString() {
            return value.toString();
        }

        /**
         * Returns the JID serialised as UTF-8 bytes.
         *
         * <p>The JID is first converted to its string form, then encoded
         * to bytes via the platform default charset.
         *
         * @return a non-null byte array containing the UTF-8 encoded JID
         */
        @Override
        public byte[] toBytes() {
            return value.toString().getBytes();
        }

        /**
         * Returns the underlying JID.
         *
         * @return an {@link Optional} always containing the stored JID
         */
        @Override
        public Optional<Jid> toJid() {
            return Optional.of(value);
        }

        /**
         * Returns an empty {@link OptionalLong} because a JID has no
         * meaningful integer parse.
         *
         * @return {@link OptionalLong#empty()}
         */
        @Override
        public OptionalLong toLong() {
            return OptionalLong.empty();
        }

        /**
         * Returns an empty {@link OptionalInt} because a JID has no
         * meaningful integer parse.
         *
         * @return {@link OptionalInt#empty()}
         */
        @Override
        public OptionalInt toInt() {
            return OptionalInt.empty();
        }

        /**
         * Returns an empty {@link OptionalDouble} because a JID has no
         * meaningful floating-point parse.
         *
         * @return {@link OptionalDouble#empty()}
         */
        @Override
        public OptionalDouble toDouble() {
            return OptionalDouble.empty();
        }
    }

    /**
     * An attribute value that holds a raw binary blob.
     *
     * <p>Used for values that must not be interpreted as text, such as
     * cryptographic hashes, protocol tokens, or already-encoded data.
     * Decoding to a string uses the platform default charset and may
     * produce nonsense output when the bytes are not valid text.
     *
     * @param value the binary value, must not be null
     */
    record BytesAttribute(byte[] value) implements NodeAttribute {
        /**
         * Constructs a new bytes attribute, rejecting null values.
         *
         * @param value the binary value
         * @throws NullPointerException if {@code value} is {@code null}
         */
        public BytesAttribute {
            Objects.requireNonNull(value, "value cannot be null");
        }

        /**
         * Returns the bytes decoded as a string using the platform default
         * charset.
         *
         * @return a non-null string decoded from the binary data
         */
        @Override
        public String toString() {
            return new String(value);
        }

        /**
         * Returns the underlying byte array without copying.
         *
         * @return the non-null byte array
         */
        @Override
        public byte[] toBytes() {
            return value;
        }

        /**
         * Interprets the binary blob as a JID via the protobuf lazy string
         * decoder.
         *
         * @return an {@link Optional} containing the parsed JID, or empty
         *         if the bytes do not decode to a valid JID
         */
        @Override
        public Optional<Jid> toJid() {
            try {
                var result = Jid.of(ProtobufString.lazy(value));
                return Optional.of(result);
            }catch (WhatsAppMalformedJidException exception) {
                return Optional.empty();
            }
        }

        /**
         * Parses the bytes (interpreted as text) as a {@code long}.
         *
         * @return an {@link OptionalLong} with the parsed value, or empty
         *         if the text is not a valid long literal
         */
        @Override
        public OptionalLong toLong() {
            try {
                var result = Long.parseLong(toString());
                return OptionalLong.of(result);
            }catch (NumberFormatException exception) {
                return OptionalLong.empty();
            }
        }

        /**
         * Parses the bytes (interpreted as text) as an {@code int}.
         *
         * @return an {@link OptionalInt} with the parsed value, or empty
         *         if the text is not a valid int literal
         */
        @Override
        public OptionalInt toInt() {
            try {
                var result = Integer.parseInt(toString());
                return OptionalInt.of(result);
            }catch (NumberFormatException exception) {
                return OptionalInt.empty();
            }
        }

        /**
         * Parses the bytes (interpreted as text) as a {@code double}.
         *
         * @return an {@link OptionalDouble} with the parsed value, or empty
         *         if the text is not a valid double literal
         */
        @Override
        public OptionalDouble toDouble() {
            try {
                var result = Double.parseDouble(toString());
                return OptionalDouble.of(result);
            }catch (NumberFormatException exception) {
                return OptionalDouble.empty();
            }
        }
    }
}