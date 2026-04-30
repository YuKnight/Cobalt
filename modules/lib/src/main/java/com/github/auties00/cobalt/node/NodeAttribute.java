package com.github.auties00.cobalt.node;

import com.github.auties00.cobalt.exception.WhatsAppMalformedJidException;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.model.jid.Jid;
import it.auties.protobuf.model.ProtobufString;

import java.util.*;

/**
 * Represents a single attribute value attached to a {@link Node}.
 *
 * <p>Stanzas exchanged between a WhatsApp client and the server are trees
 * of nodes whose elements may carry key value attributes. Keys are always
 * strings, but values fall into one of three concrete shapes: a UTF-8
 * string, a binary blob, or a fully parsed {@link Jid}. This sealed
 * interface enumerates those shapes and exposes a uniform set of
 * conversion helpers so callers can read an attribute as the type they
 * expect without special casing on the underlying representation.
 *
 * <p>Attributes are typically produced through {@link NodeBuilder} and
 * consumed through the various {@code getAttributeAs...} helpers on
 * {@link Node}.
 *
 * @see Node
 * @see NodeBuilder
 * @see Jid
 */
@WhatsAppWebModule(moduleName = "WAWap")
@WhatsAppWebModule(moduleName = "WAXmlNode")
public sealed interface NodeAttribute {
    /**
     * Returns the string representation of this attribute value.
     *
     * @return a non null string view of the value
     */
    String toString();

    /**
     * Returns the byte array representation of this attribute value.
     *
     * @return a non null byte view of the value
     */
    byte[] toBytes();

    /**
     * Returns the value parsed as a {@link Jid}, when possible.
     *
     * @return an {@link Optional} that holds the parsed JID, or empty when
     *         the value cannot be parsed
     */
    Optional<Jid> toJid();

    /**
     * Returns the value parsed as a {@code long}, when possible.
     *
     * @return an {@link OptionalLong} that holds the parsed value, or empty
     *         when parsing fails
     */
    OptionalLong toLong();

    /**
     * Returns the value parsed as an {@code int}, when possible.
     *
     * @return an {@link OptionalInt} that holds the parsed value, or empty
     *         when parsing fails
     */
    OptionalInt toInt();

    /**
     * Returns the value parsed as a {@code double}, when possible.
     *
     * @return an {@link OptionalDouble} that holds the parsed value, or
     *         empty when parsing fails
     */
    OptionalDouble toDouble();

    /**
     * Attribute variant whose value is a UTF-8 string.
     *
     * <p>Most attribute values fall in this variant: identifiers, type
     * names, state flags, and any other textual metadata.
     *
     * @param value the textual value
     */
    record TextAttribute(String value) implements NodeAttribute {
        /**
         * Builds a text attribute, rejecting a {@code null} value.
         *
         * @throws NullPointerException if {@code value} is {@code null}
         */
        public TextAttribute {
            Objects.requireNonNull(value, "value cannot be null");
        }

        /**
         * Returns the stored string verbatim.
         *
         * @return the non null string value
         */
        @Override
        public String toString() {
            return value;
        }

        /**
         * Returns the UTF-8 encoding of the stored string.
         *
         * @return a non null byte array holding the UTF-8 encoding
         */
        @Override
        public byte[] toBytes() {
            return value.getBytes();
        }

        /**
         * Parses the stored string as a {@link Jid}.
         *
         * @return an {@link Optional} that holds the parsed JID, or empty
         *         when the string is not a valid JID
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
         * Parses the stored string as a {@code long}.
         *
         * @return an {@link OptionalLong} that holds the parsed value, or
         *         empty when the string is not a valid long literal
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
         * Parses the stored string as an {@code int}.
         *
         * @return an {@link OptionalInt} that holds the parsed value, or
         *         empty when the string is not a valid int literal
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
         * Parses the stored string as a {@code double}.
         *
         * @return an {@link OptionalDouble} that holds the parsed value, or
         *         empty when the string is not a valid double literal
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
     * Attribute variant whose value is a fully parsed {@link Jid}.
     *
     * <p>Used whenever an attribute references a WhatsApp user, group,
     * device, or other addressable entity.
     *
     * @param value the JID value
     * @see Jid
     */
    record JidAttribute(Jid value) implements NodeAttribute {
        /**
         * Builds a JID attribute, rejecting a {@code null} value.
         *
         * @throws NullPointerException if {@code value} is {@code null}
         */
        public JidAttribute {
            Objects.requireNonNull(value, "value cannot be null");
        }

        /**
         * Returns the canonical string form of the stored JID.
         *
         * @return a non null string view of the JID
         */
        @Override
        public String toString() {
            return value.toString();
        }

        /**
         * Returns the UTF-8 encoding of the canonical JID string.
         *
         * @return a non null byte array holding the UTF-8 encoding
         */
        @Override
        public byte[] toBytes() {
            return value.toString().getBytes();
        }

        /**
         * Returns the stored JID.
         *
         * @return an {@link Optional} that always holds the stored JID
         */
        @Override
        public Optional<Jid> toJid() {
            return Optional.of(value);
        }

        /**
         * Returns an empty result because a JID has no integer parse.
         *
         * @return {@link OptionalLong#empty()}
         */
        @Override
        public OptionalLong toLong() {
            return OptionalLong.empty();
        }

        /**
         * Returns an empty result because a JID has no integer parse.
         *
         * @return {@link OptionalInt#empty()}
         */
        @Override
        public OptionalInt toInt() {
            return OptionalInt.empty();
        }

        /**
         * Returns an empty result because a JID has no floating point
         * parse.
         *
         * @return {@link OptionalDouble#empty()}
         */
        @Override
        public OptionalDouble toDouble() {
            return OptionalDouble.empty();
        }
    }

    /**
     * Attribute variant whose value is an opaque binary blob.
     *
     * <p>Used for values that must not be interpreted as text, such as
     * cryptographic hashes, protocol tokens, or already encoded payloads.
     * Decoding the blob to a string uses the platform default charset and
     * may produce unintended output when the bytes are not valid text.
     *
     * @param value the binary value
     */
    record BytesAttribute(byte[] value) implements NodeAttribute {
        /**
         * Builds a bytes attribute, rejecting a {@code null} value.
         *
         * @throws NullPointerException if {@code value} is {@code null}
         */
        public BytesAttribute {
            Objects.requireNonNull(value, "value cannot be null");
        }

        /**
         * Returns the bytes decoded as a string under the platform default
         * charset.
         *
         * @return a non null string decoded from the binary data
         */
        @Override
        public String toString() {
            return new String(value);
        }

        /**
         * Returns the underlying byte array without copying.
         *
         * @return the non null byte array
         */
        @Override
        public byte[] toBytes() {
            return value;
        }

        /**
         * Parses the binary blob as a {@link Jid} via the protobuf lazy
         * string decoder.
         *
         * @return an {@link Optional} that holds the parsed JID, or empty
         *         when the bytes do not decode to a valid JID
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
         * Parses the bytes as text and then as a {@code long}.
         *
         * @return an {@link OptionalLong} that holds the parsed value, or
         *         empty when the text is not a valid long literal
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
         * Parses the bytes as text and then as an {@code int}.
         *
         * @return an {@link OptionalInt} that holds the parsed value, or
         *         empty when the text is not a valid int literal
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
         * Parses the bytes as text and then as a {@code double}.
         *
         * @return an {@link OptionalDouble} that holds the parsed value, or
         *         empty when the text is not a valid double literal
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
