package com.github.auties00.cobalt.node;

import com.github.auties00.cobalt.exception.WhatsAppMalformedJidException;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.binary.NodeDecoder;
import com.github.auties00.cobalt.node.binary.NodeEncoder;
import it.auties.protobuf.model.ProtobufString;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

/**
 * Represents a node in WhatsApp's XMPP-like stanza tree.
 *
 * <p>Every message exchanged with the WhatsApp server is a tree of nodes
 * serialised through WhatsApp's compact binary XML protocol. A node has a
 * tag (the {@code description}), an ordered map of attributes, and an
 * optional content (plain text, a JID, a binary blob, or a list of child
 * nodes). This interface is the Cobalt counterpart of WhatsApp Web's
 * {@code WapNode} / {@code XmlNode} classes and offers read-side access
 * to every constituent part along with conversion helpers that callers use
 * to read attributes and content in the type they expect.
 *
 * <p>The concrete variants permitted by the sealed hierarchy are:
 * <ul>
 *   <li>{@link EmptyNode} for nodes without content.</li>
 *   <li>{@link TextNode} for nodes whose content is a UTF-8 string.</li>
 *   <li>{@link JidNode} for nodes whose content is a single JID.</li>
 *   <li>{@link BytesNode} for nodes whose content is a binary blob.</li>
 *   <li>{@link ContainerNode} for nodes that contain child nodes.</li>
 * </ul>
 *
 * @implNote WAWap.WapNode: the canonical JS node class used by the
 *           encode/decode pipeline. WAXmlNode.XmlNode: the richer
 *           debug-oriented XML representation that WA Web uses for
 *           logging and structured pattern matching.
 * @see NodeBuilder
 * @see NodeEncoder
 * @see NodeDecoder
 */
@WhatsAppWebModule(moduleName = "WAWap")
@WhatsAppWebModule(moduleName = "WAXmlNode")
public sealed interface Node {
    /**
     * Returns the description (tag name) of this node.
     * The description typically identifies the type or purpose of the node in the protocol.
     *
     * @return the node's description as a string
     */
    String description();

    /**
     * Checks if this node's description matches the specified description.
     *
     * @param description the description to compare against
     * @return {@code true} if the descriptions match, {@code false} otherwise
     */
    default boolean hasDescription(String description) {
        return Objects.equals(description(), description);
    }

    /**
     * Returns the attributes of this node as an unmodifiable sequenced map.
     * Attributes provide metadata about the node, similar to XML attributes.
     *
     * @return a sequenced map of attribute names to attribute values
     */
    SequencedMap<String, NodeAttribute> attributes();

    /**
     * Retrieves an optional attribute by key.
     *
     * @param key the attribute key to look up
     * @return an {@link Optional} containing the attribute if present, or empty if not found
     */
    default Optional<NodeAttribute> getAttribute(String key) {
        Objects.requireNonNull(key, "key cannot be null");
        return Optional.ofNullable(attributes().get(key));
    }

    /**
     * Returns the value of the given attribute as a string.
     *
     * @param key the attribute key
     * @return an {@link Optional} holding the string value, or empty if
     *         the attribute is absent
     */
    default Optional<String> getAttributeAsString(String key) {
        return getAttribute(key)
                .map(NodeAttribute::toString);
    }

    /**
     * Returns the value of the given attribute parsed as a boolean.
     *
     * @param key the attribute key
     * @return an {@link Optional} holding the parsed boolean, or empty if
     *         the attribute is absent
     */
    default Optional<Boolean> getAttributeAsBool(String key) {
        return getAttribute(key)
                .map(NodeAttribute::toString)
                .map(Boolean::parseBoolean);
    }

    /**
     * Returns the value of the given attribute as a string, falling back
     * to the supplied default if the attribute is absent.
     *
     * @param key          the attribute key
     * @param defaultValue the value to return when the attribute is absent
     * @return the attribute value or the default
     */
    default String getAttributeAsString(String key, String defaultValue) {
        return getAttributeAsString(key)
                .orElse(defaultValue);
    }

    /**
     * Returns the value of the given attribute parsed as a boolean,
     * falling back to the supplied default if the attribute is absent.
     *
     * @param key          the attribute key
     * @param defaultValue the value to return when the attribute is absent
     * @return the parsed boolean or the default
     */
    default boolean getAttributeAsBool(String key, boolean defaultValue) {
        return getAttributeAsBool(key)
                .orElse(defaultValue);
    }

    /**
     * Returns the value of the given attribute as a byte array.
     *
     * @param key the attribute key
     * @return an {@link Optional} holding the byte array, or empty if
     *         the attribute is absent
     */
    default Optional<byte[]> getAttributeAsBytes(String key) {
        return getAttribute(key)
                .map(NodeAttribute::toBytes);
    }

    /**
     * Returns the value of the given attribute as a byte array, falling
     * back to the supplied default if the attribute is absent.
     *
     * @param key          the attribute key
     * @param defaultValue the value to return when the attribute is absent
     * @return the byte array or the default
     */
    default byte[] getAttributeAsBytes(String key, byte[] defaultValue) {
        return getAttribute(key)
                .map(NodeAttribute::toBytes)
                .orElse(defaultValue);
    }

    /**
     * Returns the value of the given attribute parsed as a {@link Jid}.
     *
     * @param key the attribute key
     * @return an {@link Optional} holding the JID, or empty if the
     *         attribute is absent or not a valid JID
     */
    default Optional<Jid> getAttributeAsJid(String key) {
        return getAttribute(key)
                .flatMap(NodeAttribute::toJid);
    }

    /**
     * Returns the value of the given attribute parsed as a {@link Jid},
     * falling back to the supplied default if the attribute is absent or
     * not a valid JID.
     *
     * @param key          the attribute key
     * @param defaultValue the value to return when the attribute is absent
     *                     or not parseable
     * @return the JID or the default
     */
    default Jid getAttributeAsJid(String key, Jid defaultValue) {
        return getAttribute(key)
                .flatMap(NodeAttribute::toJid)
                .orElse(defaultValue);
    }

    /**
     * Returns the value of the given attribute parsed as a {@code long}.
     *
     * @param key the attribute key
     * @return an {@link OptionalLong} holding the parsed value, or empty
     *         if the attribute is absent or not a valid long
     */
    default OptionalLong getAttributeAsLong(String key) {
        var result = getAttribute(key);
        return result.isEmpty() ? OptionalLong.empty() : result.get().toLong();
    }

    /**
     * Returns the value of the given attribute parsed as a {@code long},
     * falling back to the supplied default if the attribute is absent or
     * not parseable.
     *
     * @param key          the attribute key
     * @param defaultValue the value to return when the attribute is absent
     *                     or not parseable
     * @return the parsed long or the default
     */
    default long getAttributeAsLong(String key, long defaultValue) {
        var result = getAttribute(key);
        return result.isEmpty() ? defaultValue : result.get().toLong().orElse(defaultValue);
    }

    /**
     * Returns the value of the given attribute parsed as a boxed
     * {@link Long}, falling back to the supplied default (which may be
     * {@code null}) if the attribute is absent or not parseable.
     *
     * @param key          the attribute key
     * @param defaultValue the value to return when the attribute is absent
     *                     or not parseable, may be {@code null}
     * @return the parsed {@link Long}, or the default
     */
    default Long getAttributeAsLong(String key, Long defaultValue) {
        var result = getAttribute(key);
        if (result.isEmpty()) {
            return defaultValue;
        }

        var converted = result.get().toLong();
        if(converted.isEmpty()) {
            return defaultValue;
        }

        return converted.getAsLong();
    }

    /**
     * Returns the value of the given attribute parsed as an {@code int}.
     *
     * @param key the attribute key
     * @return an {@link OptionalInt} holding the parsed value, or empty
     *         if the attribute is absent or not a valid int
     */
    default OptionalInt getAttributeAsInt(String key) {
        var result = getAttribute(key);
        return result.isEmpty() ? OptionalInt.empty() : result.get().toInt();
    }

    /**
     * Returns the value of the given attribute parsed as an {@code int},
     * falling back to the supplied default if the attribute is absent or
     * not parseable.
     *
     * @param key          the attribute key
     * @param defaultValue the value to return when the attribute is absent
     *                     or not parseable
     * @return the parsed int or the default
     */
    default int getAttributeAsInt(String key, int defaultValue) {
        var result = getAttribute(key);
        return result.isEmpty() ? defaultValue : result.get().toInt().orElse(defaultValue);
    }

    /**
     * Returns the value of the given attribute parsed as a boxed
     * {@link Integer}, falling back to the supplied default (which may
     * be {@code null}) if the attribute is absent or not parseable.
     *
     * @param key          the attribute key
     * @param defaultValue the value to return when the attribute is absent
     *                     or not parseable, may be {@code null}
     * @return the parsed {@link Integer}, or the default
     */
    default Integer getAttributeAsInt(String key, Integer defaultValue) {
        var result = getAttribute(key);
        if (result.isEmpty()) {
            return defaultValue;
        }

        var converted = result.get().toInt();
        if(converted.isEmpty()) {
            return defaultValue;
        }

        return converted.getAsInt();
    }

    /**
     * Returns the value of the given attribute parsed as a {@code double}.
     *
     * @param key the attribute key
     * @return an {@link OptionalDouble} holding the parsed value, or empty
     *         if the attribute is absent or not a valid double
     */
    default OptionalDouble getAttributeAsDouble(String key) {
        var result = getAttribute(key);
        return result.isEmpty() ? OptionalDouble.empty() : result.get().toDouble();
    }

    /**
     * Returns the value of the given attribute parsed as a {@code double},
     * falling back to the supplied default if the attribute is absent or
     * not parseable.
     *
     * @param key          the attribute key
     * @param defaultValue the value to return when the attribute is absent
     *                     or not parseable
     * @return the parsed double or the default
     */
    default double getAttributeAsDouble(String key, double defaultValue) {
        var result = getAttribute(key);
        return result.isEmpty() ? defaultValue : result.get().toDouble().orElse(defaultValue);
    }

    /**
     * Returns the value of the given attribute parsed as a boxed
     * {@link Double}, falling back to the supplied default (which may
     * be {@code null}) if the attribute is absent or not parseable.
     *
     * @param key          the attribute key
     * @param defaultValue the value to return when the attribute is absent
     *                     or not parseable, may be {@code null}
     * @return the parsed {@link Double}, or the default
     */
    default Double getAttributeAsDouble(String key, Double defaultValue) {
        var result = getAttribute(key);
        if (result.isEmpty()) {
            return defaultValue;
        }

        var converted = result.get().toDouble();
        if(converted.isEmpty()) {
            return defaultValue;
        }

        return converted.getAsDouble();
    }

    /**
     * Retrieves an optional attribute by key.
     *
     * @param key the attribute key to look up
     * @return an {@link Optional} containing the attribute if present, or empty if not found
     */
    default Stream<NodeAttribute> streamAttribute(String key) {
        Objects.requireNonNull(key, "key cannot be null");
        return Stream.ofNullable(attributes().get(key));
    }

    /**
     * Returns a single-element stream with the attribute's string value,
     * or an empty stream if the attribute is absent.
     *
     * @param key the attribute key
     * @return a {@link Stream} yielding the string value or nothing
     */
    default Stream<String> streamAttributeAsString(String key) {
        return streamAttribute(key)
                .map(NodeAttribute::toString);
    }

    /**
     * Returns a single-element stream with the attribute's parsed boolean
     * value, or an empty stream if the attribute is absent.
     *
     * @param key the attribute key
     * @return a {@link Stream} yielding the boolean value or nothing
     */
    default Stream<Boolean> streamAttributeAsBool(String key) {
        return streamAttribute(key)
                .map(NodeAttribute::toString)
                .map(Boolean::parseBoolean);
    }

    /**
     * Returns a single-element stream with the attribute's byte-array
     * value, or an empty stream if the attribute is absent.
     *
     * @param key the attribute key
     * @return a {@link Stream} yielding the byte array or nothing
     */
    default Stream<byte[]> streamAttributeAsBytes(String key) {
        return streamAttribute(key)
                .map(NodeAttribute::toBytes);
    }

    /**
     * Returns a single-element stream with the attribute parsed as a
     * {@link Jid}, or an empty stream if the attribute is absent or
     * cannot be parsed.
     *
     * @param key the attribute key, must not be {@code null}
     * @return a {@link Stream} yielding the parsed JID or nothing
     */
    default Stream<Jid> streamAttributeAsJid(String key) {
        Objects.requireNonNull(key, "key cannot be null");
        var attributeValue = attributes().get(key);
        return attributeValue != null
                ? attributeValue.toJid().stream()
                : Stream.empty();
    }

    /**
     * Returns a single-element stream with the attribute parsed as a
     * {@code long}, or an empty stream if the attribute is absent or
     * cannot be parsed.
     *
     * @param key the attribute key, must not be {@code null}
     * @return a {@link LongStream} yielding the parsed long or nothing
     */
    default LongStream streamAttributeAsLong(String key) {
        Objects.requireNonNull(key, "key cannot be null");
        var attributeValue = attributes().get(key);
        return attributeValue != null
                ? attributeValue.toLong().stream()
                : LongStream.empty();
    }

    /**
     * Returns a single-element stream with the attribute parsed as an
     * {@code int}, or an empty stream if the attribute is absent or
     * cannot be parsed.
     *
     * @param key the attribute key, must not be {@code null}
     * @return an {@link IntStream} yielding the parsed int or nothing
     */
    default IntStream streamAttributeAsInt(String key) {
        Objects.requireNonNull(key, "key cannot be null");
        var attributeValue = attributes().get(key);
        return attributeValue != null
                ? attributeValue.toInt().stream()
                : IntStream.empty();
    }

    /**
     * Returns a single-element stream with the attribute parsed as a
     * {@code double}, or an empty stream if the attribute is absent or
     * cannot be parsed.
     *
     * @param key the attribute key, must not be {@code null}
     * @return a {@link DoubleStream} yielding the parsed double or nothing
     */
    default DoubleStream streamAttributeAsDouble(String key) {
        Objects.requireNonNull(key, "key cannot be null");
        var attributeValue = attributes().get(key);
        return attributeValue != null
                ? attributeValue.toDouble().stream()
                : DoubleStream.empty();
    }

    /**
     * Retrieves a required attribute by key.
     *
     * @param key the attribute key to look up
     * @return the attribute value
     * @throws NoSuchElementException if the attribute is not present
     */
    default NodeAttribute getRequiredAttribute(String key) {
        var result = attributes().get(key);
        if(result == null) {
            throw new NoSuchElementException("No attribute with key " + key + " found in node " + description() + " with attributes " + attributes());
        }

        return result;
    }

    /**
     * Returns the required attribute as a string.
     *
     * @param key the attribute key
     * @return the string value
     * @throws NoSuchElementException if the attribute is not present
     */
    default String getRequiredAttributeAsString(String key) {
        return getRequiredAttribute(key)
                .toString();
    }

    /**
     * Returns the required attribute parsed as a boolean.
     *
     * @param key the attribute key
     * @return the parsed boolean value
     * @throws NoSuchElementException if the attribute is not present
     */
    default boolean getRequiredAttributeAsBool(String key) {
        var result = getRequiredAttribute(key)
                .toString();
        return Boolean.parseBoolean(result);
    }

    /**
     * Returns the required attribute as a byte array.
     *
     * @param key the attribute key
     * @return the byte-array value
     * @throws NoSuchElementException if the attribute is not present
     */
    default byte[] getRequiredAttributeAsBytes(String key) {
        return getRequiredAttribute(key)
                .toBytes();
    }

    /**
     * Returns the required attribute parsed as a {@link Jid}.
     *
     * @param key the attribute key
     * @return the parsed JID
     * @throws NoSuchElementException if the attribute is not present
     * @throws IllegalArgumentException if the attribute is not a valid JID
     */
    default Jid getRequiredAttributeAsJid(String key) {
        var requiredAttribute = getRequiredAttribute(key);
        return requiredAttribute
                .toJid()
                .orElseThrow(() -> new IllegalArgumentException("Cannot convert required attribute " + key + " to JID. Attribute value: " + requiredAttribute));
    }

    /**
     * Returns the required attribute parsed as a {@code long}.
     *
     * @param key the attribute key
     * @return the parsed long
     * @throws NoSuchElementException if the attribute is not present
     * @throws IllegalArgumentException if the attribute is not a valid long
     */
    default long getRequiredAttributeAsLong(String key) {
        var requiredAttribute = getRequiredAttribute(key);
        return requiredAttribute
                .toLong()
                .orElseThrow(() -> new IllegalArgumentException("Cannot convert required attribute " + key + " to long. Attribute value: " + requiredAttribute));
    }

    /**
     * Returns the required attribute parsed as an {@code int}.
     *
     * @param key the attribute key
     * @return the parsed int
     * @throws NoSuchElementException if the attribute is not present
     * @throws IllegalArgumentException if the attribute is not a valid int
     */
    default int getRequiredAttributeAsInt(String key) {
        var requiredAttribute = getRequiredAttribute(key);
        return requiredAttribute
                .toInt()
                .orElseThrow(() -> new IllegalArgumentException("Cannot convert required attribute " + key + " to int. Attribute value: " + requiredAttribute));
    }

    /**
     * Returns the required attribute parsed as a {@code double}.
     *
     * @param key the attribute key
     * @return the parsed double
     * @throws NoSuchElementException if the attribute is not present
     * @throws IllegalArgumentException if the attribute is not a valid
     *         double
     */
    default double getRequiredAttributeAsDouble(String key) {
        var requiredAttribute = getRequiredAttribute(key);
        return requiredAttribute
                .toDouble()
                .orElseThrow(() -> new IllegalArgumentException("Cannot convert required attribute " + key + " to double. Attribute value: " + requiredAttribute));
    }

    /**
     * Returns whether this node has an attribute with the given key.
     *
     * @param key the attribute key, must not be {@code null}
     * @return {@code true} if the attribute is present
     */
    default boolean hasAttribute(String key) {
        Objects.requireNonNull(key, "key cannot be null");
        return attributes().containsKey(key);
    }

    /**
     * Returns whether this node has an attribute with the given key and
     * exact string value.
     *
     * @param key   the attribute key, must not be {@code null}
     * @param value the expected string value
     * @return {@code true} if the attribute is present and equal to
     *         {@code value}
     */
    default boolean hasAttribute(String key, String value) {
        Objects.requireNonNull(key, "key cannot be null");
        var attribute = attributes().get(key);
        return attribute != null
               && attribute.toString().equals(value);
    }

    /**
     * Returns whether this node has an attribute with the given key and
     * byte-array value.
     *
     * @param key   the attribute key, must not be {@code null}
     * @param value the expected byte-array value
     * @return {@code true} if the attribute is present and its bytes
     *         equal {@code value}
     */
    default boolean hasAttribute(String key, byte[] value) {
        Objects.requireNonNull(key, "key cannot be null");
        var attribute = attributes().get(key);
        return attribute != null
               && Arrays.equals(attribute.toBytes(), value);
    }

    /**
     * Returns whether this node has an attribute with the given key that
     * equals the supplied {@link Jid}.
     *
     * @param key   the attribute key, must not be {@code null}
     * @param value the expected JID value
     * @return {@code true} if the attribute is present and parses to
     *         {@code value}
     */
    default boolean hasAttribute(String key, Jid value) {
        Objects.requireNonNull(key, "key cannot be null");
        var attribute = attributes().get(key);
        if(attribute == null) {
            return false;
        }

        var attributeValue = attribute.toJid();
        return attributeValue.isPresent()
               && Objects.equals(attributeValue.get(), value);
    }

    /**
     * Returns whether this node has an attribute with the given key that
     * parses to the supplied {@code long}.
     *
     * @param key   the attribute key, must not be {@code null}
     * @param value the expected long value
     * @return {@code true} if the attribute is present and parses to
     *         {@code value}
     */
    default boolean hasAttribute(String key, long value) {
        Objects.requireNonNull(key, "key cannot be null");
        var attribute = attributes().get(key);
        if(attribute == null) {
            return false;
        }

        var attributeValue = attribute.toLong();
        return attributeValue.isPresent()
               && attributeValue.getAsLong() == value;
    }

    /**
     * Returns whether this node has an attribute with the given key that
     * parses to the supplied {@code int}.
     *
     * @param key   the attribute key, must not be {@code null}
     * @param value the expected int value
     * @return {@code true} if the attribute is present and parses to
     *         {@code value}
     */
    default boolean hasAttribute(String key, int value) {
        Objects.requireNonNull(key, "key cannot be null");
        var attribute = attributes().get(key);
        if(attribute == null) {
            return false;
        }

        var attributeValue = attribute.toInt();
        return attributeValue.isPresent()
               && attributeValue.getAsInt() == value;
    }

    /**
     * Returns whether this node has an attribute with the given key that
     * parses to the supplied {@code double}.
     *
     * @param key   the attribute key, must not be {@code null}
     * @param value the expected double value
     * @return {@code true} if the attribute is present and parses to
     *         {@code value}
     */
    default boolean hasAttribute(String key, double value) {
        Objects.requireNonNull(key, "key cannot be null");
        var attribute = attributes().get(key);
        if(attribute == null) {
            return false;
        }

        var attributeValue = attribute.toDouble();
        return attributeValue.isPresent()
               && attributeValue.getAsDouble() == value;
    }

    /**
     * Returns whether this node has an attribute with the given key that
     * parses to the supplied {@code boolean}.
     *
     * @param key   the attribute key, must not be {@code null}
     * @param value the expected boolean value
     * @return {@code true} if the attribute is present and parses to
     *         {@code value}
     */
    default boolean hasAttribute(String key, boolean value) {
        Objects.requireNonNull(key, "key cannot be null");
        var attribute = attributes().get(key);
        if(attribute == null) {
            return false;
        }

        var attributeValue = attribute.toString();
        return Boolean.parseBoolean(attributeValue) == value;
    }

    /**
     * Checks whether this node has children.
     * Empty nodes return {@code false}, while all other node types return {@code true}.
     *
     * @return {@code true} if the node has children, {@code false} otherwise
     */
    boolean hasContent();

    /**
     * Returns whether this node's content equals the given text.
     *
     * @param content the expected text content
     * @return {@code true} if the node has text content equal to
     *         {@code content}
     */
    boolean hasContent(String content);

    /**
     * Returns whether this node's content equals the given JID.
     *
     * @param content the expected JID content
     * @return {@code true} if the node has JID content equal to
     *         {@code content}
     */
    boolean hasContent(Jid content);

    /**
     * Returns whether this node's content equals the given byte array.
     *
     * @param content the expected byte-array content
     * @return {@code true} if the node has binary content equal to
     *         {@code content}
     */
    boolean hasContent(byte[] content);
    /**
     * Calculates the size of the node based on its attributes and whether it contains children.
     * The size is computed as:
     * <ul>
     *   <li>one unit for the description</li>
     *   <li>two units for each attribute (key and value)</li>
     *   <li>one unit if the node contains children/li>
     * </ul>
     *
     * @return the calculated size of the node
     */
    default int size() {
        return 1 // Description
               + (attributes().size() * 2) // Attributes
               + (hasContent() ? 1 : 0); // Content
    }

    /**
     * Converts the content of this node to a buffer, if possible.
     *
     * @return an {@code Optional} containing the content a buffer if possible, otherwise an empty {@code Optional}
     */
    Optional<byte[]> toContentBytes();

    /**
     * Returns a single-element stream with the node's content as a byte
     * array, or an empty stream if the conversion is not possible.
     *
     * @return a {@link Stream} yielding the byte-array content or nothing
     */
    default Stream<byte[]> streamContentBytes() {
        return toContentBytes()
                .stream();
    }

    /**
     * Converts the content of this node to an InputStream, if possible.
     *
     * @return an {@code Optional} containing the content an InputStream if possible, otherwise an empty {@code Optional}
     */
    Optional<InputStream> toContentStream();

    /**
     * Returns a single-element stream with the node's content as an
     * {@link InputStream}, or an empty stream if the conversion is not
     * possible.
     *
     * @return a {@link Stream} yielding the {@link InputStream} or nothing
     */
    default Stream<InputStream> streamContentStream() {
        return toContentStream()
                .stream();
    }

    /**
     * Converts the content of this node to a string, if possible.
     *
     * @return an {@code Optional} containing the content as a string if possible, otherwise an empty {@code Optional}
     */
    Optional<String> toContentString();

    /**
     * Returns a single-element stream with the node's content as a
     * string, or an empty stream if the conversion is not possible.
     *
     * @return a {@link Stream} yielding the string or nothing
     */
    default Stream<String> streamContentString() {
        return toContentString()
                .stream();
    }

    /**
     * Returns the content of this node parsed as a boolean.
     *
     * @return an {@link Optional} holding the parsed boolean, or empty if
     *         the content cannot be converted to a string
     */
    default Optional<Boolean> toContentBool() {
        return toContentString()
                .map(Boolean::parseBoolean);
    }

    /**
     * Returns a single-element stream with the node's content as a
     * parsed boolean, or an empty stream if the conversion is not
     * possible.
     *
     * @return a {@link Stream} yielding the parsed boolean or nothing
     */
    default Stream<Boolean> streamContentBool() {
        return toContentBool()
                .stream();
    }

    /**
     * Returns the content of this node parsed as an {@link Integer}.
     *
     * @return an {@link Optional} holding the parsed int, or empty if
     *         the content is not a valid int
     */
    default Optional<Integer> toContentInt() {
        return toContentString().map(str -> {
            try {
                return Integer.parseInt(str);
            }catch (NumberFormatException _) {
                return null;
            }
        });
    }

    /**
     * Returns a single-element stream with the node's content as an
     * {@link Integer}, or an empty stream if parsing fails.
     *
     * @return a {@link Stream} yielding the parsed int or nothing
     */
    default Stream<Integer> streamContentInt() {
        return toContentInt()
                .stream();
    }

    /**
     * Returns the content of this node parsed as a {@link Long}.
     *
     * @return an {@link Optional} holding the parsed long, or empty if
     *         the content is not a valid long
     */
    default Optional<Long> toContentLong() {
        return toContentString().map(str -> {
            try {
                return Long.parseLong(str);
            }catch (NumberFormatException _) {
                return null;
            }
        });
    }

    /**
     * Returns a single-element stream with the node's content as a
     * {@link Long}, or an empty stream if parsing fails.
     *
     * @return a {@link Stream} yielding the parsed long or nothing
     */
    default Stream<Long> streamContentLong() {
        return toContentLong()
                .stream();
    }

    /**
     * Converts the content of this node to a jid, if possible.
     *
     * @return an {@code Optional} containing the content as a jid if possible, otherwise an empty {@code Optional}
     */
    Optional<Jid> toContentJid();

    /**
     * Returns a single-element stream with the node's content as a
     * {@link Jid}, or an empty stream if parsing fails.
     *
     * @return a {@link Stream} yielding the JID or nothing
     */
    default Stream<Jid> streamContentJid() {
        return toContentJid()
                .stream();
    }

    /**
     * Retrieves the child nodes contained in this container node, if any.
     *
     * @return a sequenced collection of child nodes contained in this container node
     */
    SequencedCollection<Node> children();

    /**
     * Returns the children of this node as a {@link Stream}.
     *
     * @return a non-null stream over the child nodes
     */
    default Stream<Node> streamChildren() {
        return children()
                .stream();
    }

    /**
     * Retrieves the first child Node in this container, if present.
     *
     * @return an {@code Optional} containing the first child Node if it exists,
     *         otherwise an empty {@code Optional}.
     */
    default Optional<Node> getChild() {
        var children = children();
        return children.isEmpty()
                ? Optional.empty()
                : Optional.ofNullable(children.getFirst());
    }

    /**
     * Retrieves the first child Node in this container, if present.
     *
     * @return a {@code Stream} containing the first child Node if it exists,
     *         otherwise an empty {@code Stream}.
     */
    default Stream<Node> streamChild() {
        var children = children();
        return children.isEmpty()
                ? Stream.empty()
                : Stream.of(children.getFirst());
    }

    /**
     * Finds a child node by its description within the current container node.
     * If no child node with the specified description exists, an empty {@code Optional} is returned.
     *
     * @param description the description of the child node to find; cannot be null
     * @return an {@code Optional} containing the child node if one is found
     *         with the specified description, otherwise an empty {@code Optional}
     * @throws NullPointerException if the given description is null
     */
    default Optional<Node> getChild(String description) {
        Objects.requireNonNull(description, "description cannot be null");
        return streamChildren(description)
                .findFirst();
    }

    /**
     * Finds the first child node whose description matches one of the provided descriptions.
     * The descriptions are checked in the same order they are provided.
     *
     * @param descriptions the descriptions of the child nodes to find; cannot be null
     * @return an {@link Optional} containing the first matching child node if one is found,
     *         otherwise an empty {@code Optional}
     * @throws NullPointerException if the descriptions array or one of its values is null
     */
    default Optional<Node> getChild(String... descriptions) {
        Objects.requireNonNull(descriptions, "descriptions cannot be null");
        for (var description : descriptions) {
            Objects.requireNonNull(description, "description cannot be null");
            var child = getChild(description);
            if (child.isPresent()) {
                return child;
            }
        }

        return Optional.empty();
    }

    /**
     * Finds a child node by its description within the current container node.
     * If no child node with the specified description exists, {@code defaultValue} is returned
     *
     * @param description the description of the child node to find; cannot be null
     * @param defaultValue the default value to return if no node with the provided description exists
     * @return the child node with the provided description, if found, otherwise {@code defaultValue}
     * @throws NullPointerException if the given description is null
     */
    default Node getChild(String description, Node defaultValue) {
        return getChild(description)
                .orElse(defaultValue);
    }

    /**
     * Finds a child node by its description within the current container node.
     * If no child node with the specified description exists, an {@code IllegalArgumentException} is thrown.
     *
     * @param description the description of the child node to find; cannot be null
     * @return the first child node with the specified description
     * @throws NullPointerException if the given description is null
     * @throws IllegalArgumentException if no child node with the specified description exists
     */
    default Node getRequiredChild(String description) {
        Objects.requireNonNull(description, "description cannot be null");
        return streamChildren(description)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No child node found with description: " + description));
    }

    /**
     * Finds the first child node whose description matches one of the provided descriptions.
     * The descriptions are checked in the same order they are provided.
     *
     * @param descriptions the descriptions of the child nodes to find; cannot be null
     * @return the first matching child node
     * @throws NullPointerException if the descriptions array or one of its values is null
     * @throws IllegalArgumentException if no child node with any of the specified descriptions exists
     */
    default Node getRequiredChild(String... descriptions) {
        return getChild(descriptions)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No child node found with descriptions: " + Arrays.toString(descriptions)
                ));
    }

    /**
     * Finds a child node by its description within the current container node.
     * If no child node with the specified description exists, an empty {@code Stream} is returned.
     *
     * @param description the description of the child node to find; cannot be null
     * @return a {@code Stream} containing the child node if one is found
     *         with the specified description, otherwise an empty {@code Stream}
     * @throws NullPointerException if the given description is null
     */
    default Stream<Node> streamChild(String description) {
        Objects.requireNonNull(description, "description cannot be null");
        return streamChildren(description)
                .findFirst()
                .stream();
    }

    /**
     * Finds the first child node whose description matches one of the provided descriptions.
     * The descriptions are checked in the same order they are provided.
     *
     * @param descriptions the descriptions of the child nodes to find; cannot be null
     * @return a {@code Stream} containing the first matching child node if one is found,
     *         otherwise an empty {@code Stream}
     * @throws NullPointerException if the descriptions array or one of its values is null
     */
    default Stream<Node> streamChild(String... descriptions) {
        return getChild(descriptions)
                .stream();
    }

    /**
     * Finds all children nodes by their descriptions within the current container node.
     * If no child node with the specified description exists, an empty {@code SequencedCollection} is returned.
     *
     * @param description the description of the children nodes to find; cannot be null
     * @return a {@code SequencedCollection} containing the children nodes
     * @throws NullPointerException if the given description is null
     */
    default SequencedCollection<Node> getChildren(String description) {
        Objects.requireNonNull(description, "description cannot be null");
        return children()
                .stream()
                .filter(node -> node.hasDescription(description))
                .toList();
    }

    /**
     * Finds all children nodes whose descriptions match any of the provided descriptions.
     * The returned collection preserves the original child order.
     *
     * @param descriptions the descriptions of the child nodes to find; cannot be null
     * @return a {@code SequencedCollection} containing the matching children nodes
     * @throws NullPointerException if the descriptions array or one of its values is null
     */
    default SequencedCollection<Node> getChildren(String... descriptions) {
        return streamChildren(descriptions)
                .toList();
    }

    /**
     * Finds all children nodes by their descriptions within the current container node.
     * If no child node with the specified description exists, an empty {@code Stream} is returned.
     *
     * @param description the description of the children nodes to find; cannot be null
     * @return an {@code Stream} containing the children nodes
     * @throws NullPointerException if the given description is null
     */
    default Stream<Node> streamChildren(String description) {
        Objects.requireNonNull(description, "description cannot be null");
        return children()
                .stream()
                .filter(node -> node.hasDescription(description));
    }

    /**
     * Finds all children nodes whose descriptions match any of the provided descriptions.
     * The returned stream preserves the original child order.
     *
     * @param descriptions the descriptions of the child nodes to find; cannot be null
     * @return a {@code Stream} containing the matching children nodes
     * @throws NullPointerException if the descriptions array or one of its values is null
     */
    default Stream<Node> streamChildren(String... descriptions) {
        var descriptionSet = new LinkedHashSet<String>();
        Objects.requireNonNull(descriptions, "descriptions cannot be null");
        for (var description : descriptions) {
            descriptionSet.add(Objects.requireNonNull(description, "description cannot be null"));
        }

        return children()
                .stream()
                .filter(node -> descriptionSet.contains(node.description()));
    }

    /**
     * Checks whether this node has a child node with the specified description.
     *
     * @param description the description of the child node to check for; cannot be null
     * @return {@code true} if a child node with the specified description exists, {@code false} otherwise
     */
    default boolean hasChild(String description) {
        Objects.requireNonNull(description, "description cannot be null");
        return children()
                .stream()
                .anyMatch(node -> node.hasDescription(description));
    }

    /**
     * Checks whether this node has a child whose description matches any of the provided descriptions.
     *
     * @param descriptions the descriptions of the child nodes to check for; cannot be null
     * @return {@code true} if any matching child node exists, {@code false} otherwise
     * @throws NullPointerException if the descriptions array or one of its values is null
     */
    default boolean hasChild(String... descriptions) {
        return getChild(descriptions).isPresent();
    }

    /**
     * A node variant that has no content, only a tag and attributes.
     *
     * <p>Used for stanzas that convey their meaning entirely through the
     * tag name and attributes, such as simple presence or ack stanzas.
     *
     * @param description the node's tag name
     * @param attributes  the node's attribute map
     */
    record EmptyNode(String description, SequencedMap<String, NodeAttribute> attributes) implements Node {
        /**
         * Constructs an empty node, rejecting null arguments.
         *
         * @throws NullPointerException if any argument is {@code null}
         */
        public EmptyNode {
            Objects.requireNonNull(description, "description cannot be null");
            Objects.requireNonNull(attributes, "attributes cannot be null");
        }

        @Override
        public SequencedMap<String, NodeAttribute> attributes() {
            return Collections.unmodifiableSequencedMap(attributes);
        }

        @Override
        public boolean hasContent() {
            return false;
        }
        @Override
        public boolean hasContent(String content) {
            return false;
        }

        @Override
        public boolean hasContent(Jid content) {
            return false;
        }

        @Override
        public Optional<String> toContentString() {
            return Optional.empty();
        }

        @Override
        public boolean hasContent(byte[] content) {
            return false;
        }
        @Override
        public Optional<Jid> toContentJid() {
            return Optional.empty();
        }

        @Override
        public SequencedCollection<Node> children() {
            return List.of();
        }

        @Override
        public Optional<byte[]> toContentBytes() {
            return Optional.empty();
        }

        @Override
        public Optional<InputStream> toContentStream() {
            return Optional.empty();
        }

        @Override
        public boolean equals(Object o) {
            return switch (o) {
                case EmptyNode(var thatDescription, var thatAttributes) -> Objects.equals(description, thatDescription) && Objects.equals(attributes, thatAttributes);
                case TextNode(var thatDescription, var thatAttributes, var thatContent) -> Objects.equals(description, thatDescription) && Objects.equals(attributes, thatAttributes) && hasContent(thatContent);
                case BytesNode(var thatDescription, var thatAttributes, var thatContent) -> Objects.equals(description, thatDescription) && Objects.equals(attributes, thatAttributes) && hasContent(thatContent);
                case null, default -> false;
            };
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(description, attributes);
        }

        @Override
        public String toString() {
            var result = new StringBuilder();
            result.append("Node[description=");
            result.append(description);

            if(!attributes.isEmpty()) {
                result.append(", attributes=");
                result.append(attributes);
            }

            result.append("]");

            return result.toString();
        }
    }

    /**
     * A node variant whose content is a UTF-8 string.
     *
     * <p>Used for stanzas whose payload is textual data such as a status
     * message, a JID serialised as text, or an informational blurb.
     *
     * @param description the node's tag name
     * @param attributes  the node's attribute map
     * @param content     the text content
     */
    record TextNode(String description, SequencedMap<String, NodeAttribute> attributes, String content) implements Node {
        /**
         * Constructs a text node, rejecting null arguments.
         *
         * @throws NullPointerException if any argument is {@code null}
         */
        public TextNode {
            Objects.requireNonNull(description, "description cannot be null");
            Objects.requireNonNull(attributes, "attributes cannot be null");
            Objects.requireNonNull(content, "content cannot be null");
        }

        @Override
        public SequencedMap<String, NodeAttribute> attributes() {
            return Collections.unmodifiableSequencedMap(attributes);
        }

        @Override
        public boolean hasContent() {
            return true;
        }

        @Override
        public boolean hasContent(String content) {
            return Objects.equals(this.content, content);
        }

        @Override
        public boolean hasContent(Jid content) {
            return content != null && Objects.equals(this.content, content.toString());
        }

        @Override
        public boolean hasContent(byte[] content) {
            return content != null && Objects.equals(this.content, new String(content));
        }
        @Override
        public Optional<byte[]> toContentBytes() {
            return Optional.of(content.getBytes());
        }

        @Override
        public Optional<InputStream> toContentStream() {
            return Optional.of(new ByteArrayInputStream(content.getBytes()));
        }

        @Override
        public Optional<Jid> toContentJid() {
            try {
                var result = Jid.of(content);
                return Optional.of(result);
            }catch (WhatsAppMalformedJidException exception) {
                return Optional.empty();
            }
        }

        @Override
        public Optional<String> toContentString() {
            return Optional.of(content);
        }

        @Override
        public SequencedCollection<Node> children() {
            return List.of();
        }

        @Override
        public boolean equals(Object o) {
            return switch (o) {
                case EmptyNode(var thatDescription, var thatAttributes) -> Objects.equals(description, thatDescription) 
                        && Objects.equals(attributes, thatAttributes) 
                        && content.isEmpty();
                case TextNode(var thatDescription, var thatAttributes, var thatContent) -> Objects.equals(description, thatDescription) 
                        && Objects.equals(attributes, thatAttributes) 
                        && hasContent(thatContent);
                case JidNode(var thatDescription, var thatAttributes, var thatContent) -> Objects.equals(description, thatDescription) 
                        && Objects.equals(attributes, thatAttributes) 
                        && hasContent(thatContent);
                case BytesNode(var thatDescription, var thatAttributes, var thatContent) -> Objects.equals(description, thatDescription) 
                        && Objects.equals(attributes, thatAttributes) 
                        && hasContent(thatContent);
                case null, default -> false;
            };
        }

        @Override
        public int hashCode() {
            return Objects.hash(description, attributes, content);
        }
        @Override
        public String toString() {
            var result = new StringBuilder();
            result.append("Node[description=");
            result.append(description());

            if(!attributes.isEmpty()) {
                result.append(", attributes=");
                result.append(attributes);
            }

            if(content != null) {
                result.append(", content=");
                result.append(content);
            }

            result.append("]");

            return result.toString();
        }
    }

    /**
     * A node variant whose content is a single {@link Jid}.
     *
     * <p>Used for stanzas whose payload identifies a user, group, or
     * device. The JID is serialised on the wire using one of the WAWap
     * JID shapes rather than as plain text.
     *
     * @param description the node's tag name
     * @param attributes  the node's attribute map
     * @param content     the JID content
     */
    record JidNode(String description, SequencedMap<String, NodeAttribute> attributes, Jid content) implements Node {
        /**
         * Constructs a JID node, rejecting null arguments.
         *
         * @throws NullPointerException if any argument is {@code null}
         */
        public JidNode {
            Objects.requireNonNull(description, "description cannot be null");
            Objects.requireNonNull(attributes, "attributes cannot be null");
            Objects.requireNonNull(content, "content cannot be null");
        }

        @Override
        public SequencedMap<String, NodeAttribute> attributes() {
            return Collections.unmodifiableSequencedMap(attributes);
        }

        @Override
        public boolean hasContent() {
            return true;
        }

        @Override
        public boolean hasContent(String content) {
            return Objects.equals(this.content.toString(), content);
        }

        @Override
        public boolean hasContent(Jid content) {
            return Objects.equals(this.content, content);
        }

        @Override
        public boolean hasContent(byte[] content) {
            return content != null && Objects.equals(this.content.toString(), new String(content));
        }
        @Override
        public Optional<String> toContentString() {
            return Optional.of(content.toString());
        }

        @Override
        public Optional<Jid> toContentJid() {
            return Optional.of(content);
        }

        @Override
        public Optional<byte[]> toContentBytes() {
            return Optional.of(content.toString().getBytes());
        }

        @Override
        public Optional<InputStream> toContentStream() {
            return Optional.of(new ByteArrayInputStream(content.toString().getBytes()));
        }

        @Override
        public SequencedCollection<Node> children() {
            return List.of();
        }

        @Override
        public boolean equals(Object o) {
            return switch (o) {
                case TextNode(var thatDescription, var thatAttributes, var thatContent) -> Objects.equals(description, thatDescription) 
                        && Objects.equals(attributes, thatAttributes) 
                        && hasContent(thatContent);
                case JidNode(var thatDescription, var thatAttributes, var thatContent) -> Objects.equals(description, thatDescription) 
                        && Objects.equals(attributes, thatAttributes) 
                        && hasContent(thatContent);
                case BytesNode(var thatDescription, var thatAttributes, var thatContent) -> Objects.equals(description, thatDescription) 
                        && Objects.equals(attributes, thatAttributes) 
                        && hasContent(thatContent);
                case null, default -> false;
            };
        }

        @Override
        public int hashCode() {
            return Objects.hash(description, attributes, content);
        }

        @Override
        public String toString() {
            var result = new StringBuilder();
            result.append("Node[description=");
            result.append(description());

            if(!attributes.isEmpty()) {
                result.append(", attributes=");
                result.append(attributes);
            }

            if(content != null) {
                result.append(", content=");
                result.append(content);
            }

            result.append("]");

            return result.toString();
        }
    }

    /**
     * A node variant whose content is a binary blob.
     *
     * <p>Used for stanzas that carry raw bytes such as encrypted payloads
     * (Signal ciphertext), media thumbnails, or any other opaque data.
     *
     * @param description the node's tag name
     * @param attributes  the node's attribute map
     * @param content     the binary content
     */
    record BytesNode(String description, SequencedMap<String, NodeAttribute> attributes, byte[] content) implements Node {
        /**
         * Constructs a bytes node, rejecting null arguments.
         *
         * @throws NullPointerException if any argument is {@code null}
         */
        public BytesNode {
            Objects.requireNonNull(description, "description cannot be null");
            Objects.requireNonNull(attributes, "attributes cannot be null");
            Objects.requireNonNull(content, "content cannot be null");
        }

        @Override
        public SequencedMap<String, NodeAttribute> attributes() {
            return Collections.unmodifiableSequencedMap(attributes);
        }

        @Override
        public Optional<byte[]> toContentBytes() {
            return Optional.of(content);
        }

        @Override
        public Optional<InputStream> toContentStream() {
            return Optional.of(new ByteArrayInputStream(content));
        }

        @Override
        public Optional<Jid> toContentJid() {
            try {
                var result = Jid.of(ProtobufString.lazy(content));
                return Optional.of(result);
            } catch (WhatsAppMalformedJidException exception) {
                return Optional.empty();
            }
        }

        @Override
        public Optional<String> toContentString() {
            var decoded = new String(content);
            return Optional.of(decoded);
        }

        @Override
        public boolean hasContent() {
            return true;
        }

        @Override
        public boolean hasContent(String content) {
            return Objects.equals(new String(this.content), content);
        }

        @Override
        public boolean hasContent(Jid content) {
            return content != null && Objects.equals(new String(this.content), content.toString());
        }

        @Override
        public boolean hasContent(byte[] content) {
            return Arrays.equals(this.content, content);
        }
        @Override
        public SequencedCollection<Node> children() {
            return List.of();
        }

        @Override
        public boolean equals(Object o) {
            return switch (o) {
                case TextNode(var thatDescription, var thatAttributes, var thatContent) -> Objects.equals(description, thatDescription) 
                        && Objects.equals(attributes, thatAttributes) 
                        && hasContent(thatContent);
                case JidNode(var thatDescription, var thatAttributes, var thatContent) -> Objects.equals(description, thatDescription) 
                        && Objects.equals(attributes, thatAttributes) 
                        && hasContent(thatContent);
                case BytesNode(var thatDescription, var thatAttributes, var thatContent) -> Objects.equals(description, thatDescription) 
                        && Objects.equals(attributes, thatAttributes) 
                        && hasContent(thatContent);
                case null, default -> false;
            };
        }

        @Override
        public int hashCode() {
            return Objects.hash(description, attributes, Arrays.hashCode(content));
        }
        @Override
        public String toString() {
            var result = new StringBuilder();
            result.append("Node[description=");
            result.append(description());

            if(!attributes.isEmpty()) {
                result.append(", attributes=");
                result.append(attributes);
            }

            if(content != null) {
                if(hasDescription("result") || hasDescription("query") || hasDescription("body")) {
                    result.append(", content=");
                    result.append(new String(content));
                }else {
                    result.append(", content=");
                    result.append(Arrays.toString(content));
                }
            }

            result.append("]");

            return result.toString();
        }
    }

    /**
     * A node variant whose content is a sequence of child nodes.
     *
     * <p>This is the recursive case of the stanza tree: a container node
     * groups a set of children under a common tag, matching the XML
     * element-with-children shape used across the WhatsApp protocol.
     *
     * @param description the node's tag name
     * @param attributes  the node's attribute map
     * @param children    the child nodes in the order they appear
     */
    record ContainerNode(String description, SequencedMap<String, NodeAttribute> attributes, SequencedCollection<Node> children) implements Node {
        /**
         * Constructs a container node, rejecting null arguments.
         *
         * @throws NullPointerException if any argument is {@code null}
         */
        public ContainerNode {
            Objects.requireNonNull(description, "description cannot be null");
            Objects.requireNonNull(attributes, "attributes cannot be null");
            Objects.requireNonNull(children, "children cannot be null");
        }

        @Override
        public SequencedMap<String, NodeAttribute> attributes() {
            return Collections.unmodifiableSequencedMap(attributes);
        }

        @Override
        public SequencedCollection<Node> children() {
            return Collections.unmodifiableSequencedCollection(children);
        }

        @Override
        public boolean hasContent() {
            return true;
        }

        @Override
        public boolean hasContent(Jid content) {
            return false;
        }

        @Override
        public boolean hasContent(byte[] content) {
            return false;
        }

        @Override
        public boolean hasContent(String content) {
            return false;
        }
        @Override
        public Optional<byte[]> toContentBytes() {
            return Optional.empty();
        }

        @Override
        public Optional<String> toContentString() {
            return Optional.empty();
        }

        @Override
        public Optional<Jid> toContentJid() {
            return Optional.empty();
        }

        @Override
        public Optional<InputStream> toContentStream() {
            return Optional.empty();
        }

        @Override
        public boolean equals(Object o) {
            return switch (o) {
                case EmptyNode(var thatDescription, var thatAttributes) -> Objects.equals(description, thatDescription)
                                                                           && Objects.equals(attributes, thatAttributes);
                case TextNode(var thatDescription, var thatAttributes, var thatContent) -> Objects.equals(description, thatDescription)
                        && Objects.equals(attributes, thatAttributes)
                        && hasContent(thatContent);
                case JidNode(var thatDescription, var thatAttributes, var thatContent) -> Objects.equals(description, thatDescription)
                        && Objects.equals(attributes, thatAttributes)
                        && hasContent(thatContent);
                case BytesNode(var thatDescription, var thatAttributes, var thatContent) -> Objects.equals(description, thatDescription)
                        && Objects.equals(attributes, thatAttributes)
                        && hasContent(thatContent);
                case ContainerNode(var thatDescription, var thatAttributes, var thatContent) -> Objects.equals(description, thatDescription)
                        && Objects.equals(attributes, thatAttributes)
                        && Objects.equals(children, thatContent);
                case null, default -> false;
            };
        }

        @Override
        public int hashCode() {
            return Objects.hash(description, attributes, children);
        }

        @Override
        public String toString() {
            var result = new StringBuilder();
            result.append("Node[description=");
            result.append(description());

            if(!attributes.isEmpty()) {
                result.append(", attributes=");
                result.append(attributes);
            }

            if(!children.isEmpty()) {
                result.append(", children=");
                result.append(children);
            }

            result.append("]");

            return result.toString();
        }
    }
}
