package com.github.auties00.cobalt.node.binary;

import com.github.auties00.cobalt.exception.WhatsAppStreamException;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeAttribute;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.SequencedCollection;
import java.util.SequencedMap;

import static com.github.auties00.cobalt.node.binary.NodeTags.*;
import static com.github.auties00.cobalt.node.binary.NodeTokens.*;

/**
 * Serialises {@link Node} trees into WhatsApp's compact binary stanza
 * format.
 *
 * <p>Every outgoing stanza passes through this encoder before being wrapped
 * in a Noise encrypted frame. Strings are replaced with dictionary tokens
 * when possible, short numeric strings are packed as nibble or hex
 * sequences, binary blobs are length prefixed with 8, 20, or 32 bit widths,
 * and JIDs are written in one of four shapes ({@link NodeTags#JID_PAIR},
 * {@link NodeTags#AD_JID}, {@link NodeTags#JID_INTEROP}, {@link NodeTags#JID_FB})
 * depending on their server and device.
 *
 * <p>The class is a stateless utility. Callers compute the output size with
 * {@link #sizeOf(Node)}, allocate a buffer, and invoke
 * {@link #encode(Node, byte[], int, int)} to populate it.
 *
 * @see Node
 * @see NodeDecoder
 * @see NodeTokens
 * @see NodeTags
 */
@WhatsAppWebModule(moduleName = "WAWap")
public final class NodeEncoder {
    /**
     * VarHandle that writes 16 bit big endian integers into a byte array.
     */
    private static final VarHandle SHORT_HANDLE = MethodHandles.byteArrayViewVarHandle(short[].class, ByteOrder.BIG_ENDIAN);

    /**
     * VarHandle that writes 32 bit big endian integers into a byte array.
     */
    private static final VarHandle INT_HANDLE = MethodHandles.byteArrayViewVarHandle(int[].class, ByteOrder.BIG_ENDIAN);

    /**
     * Lookup table that maps an ASCII character to its 4 bit nibble code or
     * to {@code -1} when the character is outside the {@code [0-9.-]} set.
     */
    private static final byte[] NIBBLE_ENCODE = new byte[128];

    /**
     * Lookup table that maps an ASCII character to its 4 bit hex code or to
     * {@code -1} when the character is outside the {@code [0-9A-F]} set.
     */
    private static final byte[] HEX_ENCODE = new byte[128];

    static {
        Arrays.fill(NIBBLE_ENCODE, (byte) -1);
        Arrays.fill(HEX_ENCODE, (byte) -1);
        for (var i = 0; i <= 9; i++) {
            NIBBLE_ENCODE['0' + i] = (byte) i;
            HEX_ENCODE['0' + i] = (byte) i;
        }
        NIBBLE_ENCODE['-'] = 10;
        NIBBLE_ENCODE['.'] = 11;
        for (var i = 0; i < 6; i++) {
            HEX_ENCODE['A' + i] = (byte) (10 + i);
        }
    }

    /**
     * Exclusive upper bound for values that fit in an unsigned byte.
     */
    private static final int UNSIGNED_BYTE_MAX_VALUE = 256;

    /**
     * Exclusive upper bound for values that fit in an unsigned short.
     */
    private static final int UNSIGNED_SHORT_MAX_VALUE = 65536;

    /**
     * Exclusive upper bound for values that fit in 20 bits.
     */
    private static final int INT_20_MAX_VALUE = 1048576;

    /**
     * Prevents instantiation of this stateless utility.
     *
     * @throws UnsupportedOperationException always
     */
    private NodeEncoder() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Computes the exact byte count required to encode the supplied node,
     * including the leading flags byte.
     *
     * <p>Callers use the returned value to size the output buffer before
     * invoking {@link #encode(Node, byte[], int, int)}.
     *
     * @param node the node to size
     * @return the byte count required to encode the node
     * @throws IllegalArgumentException if the node exceeds the format's
     *         length limits
     */
    @WhatsAppWebExport(moduleName = "WAWap", exports = "encodeStanza",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public static int sizeOf(Node node) {
        return 1 + nodeLength(node);
    }

    /**
     * Returns the length of a single node's encoding without the leading
     * flags byte.
     *
     * @param input the node to size
     * @return the byte count required to encode the node body
     */
    private static int nodeLength(Node input){
        return listLength(input.size())
               + stringLength(input.description())
               + attributesLength(input.attributes())
               + contentLength(input);
    }

    /**
     * Returns the byte count required to write a list size header.
     *
     * @param size the list size
     * @return {@code 2} for an 8 bit length, {@code 3} for a 16 bit length
     * @throws IllegalArgumentException if the size exceeds the 16 bit
     *         maximum
     */
    private static int listLength(int size) {
        if (size < UNSIGNED_BYTE_MAX_VALUE) {
            return 2;
        }else if (size < UNSIGNED_SHORT_MAX_VALUE) {
            return 3;
        }else {
            throw new IllegalArgumentException("Cannot calculate list length: overflow");
        }
    }

    /**
     * Returns the byte count required to encode a string under the most
     * efficient applicable strategy.
     *
     * @implNote The encoder picks the first strategy that applies in this
     *           order: empty literal, single byte token, dictionary token,
     *           nibble or hex packed run, length prefixed UTF-8.
     * @param input the string to size
     * @return the byte count required to encode the string
     */
    private static int stringLength(String input){
        if (input.isEmpty()) {
            return 2;
        }

        var singleByteTokenIndex = SINGLE_BYTE_TOKENS.indexOf(input);
        if (singleByteTokenIndex != -1) {
            return 1;
        }

        var dictionary0TokenIndex = DICTIONARY_0_TOKENS.indexOf(input);
        if (dictionary0TokenIndex != -1) {
            return 2;
        }

        var dictionary1TokenIndex = DICTIONARY_1_TOKENS.indexOf(input);
        if (dictionary1TokenIndex != -1) {
            return 2;
        }

        var dictionary2TokenIndex = DICTIONARY_2_TOKENS.indexOf(input);
        if (dictionary2TokenIndex != -1) {
            return 2;
        }

        var dictionary3TokenIndex = DICTIONARY_3_TOKENS.indexOf(input);
        if (dictionary3TokenIndex != -1) {
            return 2;
        }

        var utf8Length = calculateUtf8Length(input);
        if (utf8Length < 128) {
            var packedType = getPackedType(input);
            if (packedType != -1) {
                return 2 + (input.length() + 1) / 2;
            }
        }

        return calculateLength(utf8Length);
    }

    /**
     * Returns the byte count required to write a binary length prefix
     * header.
     *
     * @param input the length value to encode
     * @return {@code 2} for 8 bit, {@code 4} for 20 bit, {@code 5} for 32 bit
     */
    private static int binaryLength(long input) {
        if (input < UNSIGNED_BYTE_MAX_VALUE) {
            return 2;
        }else if (input < INT_20_MAX_VALUE) {
            return 4;
        }else {
            return 5;
        }
    }

    /**
     * Returns the byte count required to encode an attribute map.
     *
     * @param attributes the attribute map to size
     * @return the byte count required to encode every key value pair
     */
    private static int attributesLength(SequencedMap<String, ? extends NodeAttribute> attributes) {
        var result = 0;
        for (var entry : attributes.entrySet()) {
            result += stringLength(entry.getKey()) + attributeLength(entry.getValue());
        }
        return result;
    }

    /**
     * Returns the byte count required to encode a single attribute value.
     *
     * @param attribute the attribute to size
     * @return the byte count required to encode the attribute
     */
    private static int attributeLength(NodeAttribute attribute){
        return switch (attribute) {
            case NodeAttribute.BytesAttribute(var bytes) -> bytesLength(bytes);
            case NodeAttribute.TextAttribute(var literal) -> stringLength(literal);
            case NodeAttribute.JidAttribute(var jid) -> jidLength(jid);
        };
    }

    /**
     * Returns the byte count required to encode a list of child nodes,
     * including its leading list size header.
     *
     * @param values the child nodes to size
     * @return the byte count required to encode the list
     */
    private static int childrenLength(SequencedCollection<Node> values) {
        var length = listLength(values.size());
        for(var value : values) {
            length += nodeLength(value);
        }
        return length;
    }

    /**
     * Returns the byte count required to encode the content slot of a node.
     *
     * @param node the node whose content to size
     * @return the byte count required to encode the content
     */
    private static int contentLength(Node node){
        return switch (node) {
            case Node.BytesNode(var _, var _, var bytes) -> bytesLength(bytes);
            case Node.ContainerNode(var _, var _, var children) -> childrenLength(children);
            case Node.EmptyNode _ -> 0;
            case Node.JidNode(var _, var _, var jid) -> jidLength(jid);
            case Node.TextNode(var _, var _, var text) -> stringLength(text);
        };
    }

    /**
     * Returns the byte count required to encode a binary blob with its
     * length prefix.
     *
     * @param bytes the blob to size
     * @return the byte count required to encode the blob
     */
    private static int bytesLength(byte[] bytes){
        return calculateLength(bytes.length);
    }

    /**
     * Returns the byte count required to encode a JID under the shape
     * appropriate for its server and device.
     *
     * @param jid the JID to size
     * @return the byte count required to encode the JID
     */
    private static int jidLength(Jid jid){
        if (jid.hasMessengerServer()) {
            return 1 + stringLength(jid.user()) + 2 + stringLength(jid.server().address());
        } else if (jid.hasInteropServer()) {
            var user = jid.user();
            var dashIndex = user.indexOf('-');
            var actualUser = dashIndex >= 0 ? user.substring(dashIndex + 1) : user;
            return 1 + stringLength(actualUser) + 2 + 2;
        } else if (jid.hasDevice()) {
            return 3 + stringLength(jid.user());
        } else {
            return 1 + (jid.hasUser() ? stringLength(jid.user()) : 1) + stringLength(jid.server().address());
        }
    }

    /**
     * Returns the byte count required to encode a payload of the supplied
     * length together with its binary length prefix.
     *
     * @param length the payload length in bytes
     * @return the byte count required to encode the prefixed payload
     */
    private static int calculateLength(int length) {
        return binaryLength(length) + length;
    }

    /**
     * Returns the byte count of a string when encoded as UTF-8.
     *
     * @param input the string to measure, may be {@code null}
     * @return the UTF-8 byte length, or {@code 0} when {@code input} is
     *         {@code null}
     */
    private static int calculateUtf8Length(String input) {
        var length = 0;
        if(input == null) {
            return length;
        }

        var len = input.length();
        for (var i = 0; i < len; i++) {
            var ch = input.charAt(i);
            if (ch <= 0x7F) {
                length++;
            } else if (ch <= 0x7FF) {
                length += 2;
            } else if (Character.isHighSurrogate(ch)) {
                length += 4;
                i++;
            } else {
                length += 3;
            }
        }
        return length;
    }

    /**
     * Encodes the supplied node into the output array starting at
     * {@code offset}.
     *
     * <p>The caller passes a {@code length} equal to the value returned by
     * {@link #sizeOf(Node)}. A mismatch indicates an internal encoder bug
     * and is reported through {@link WhatsAppStreamException.MalformedNode}.
     *
     * @implNote The leading zero byte is the WAWap flags byte. Bit
     *           {@code 1} would mark a DEFLATE compressed payload but
     *           Cobalt always writes uncompressed bytes.
     * @param node   the node to encode
     * @param output the destination byte array
     * @param offset the position in the destination array to start writing
     *               at
     * @param length the byte count expected to be written, as returned by
     *               {@link #sizeOf(Node)}
     * @return the offset positioned past the last written byte
     * @throws WhatsAppStreamException.MalformedNode if the node encodes to
     *         a different size than {@code length}
     */
    @WhatsAppWebExport(moduleName = "WAWap", exports = "encodeStanza",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WAWap", exports = "makeStanza",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public static int encode(Node node, byte[] output, int offset, int length) {
        output[offset] = 0;
        var result = writeNode(node, output, offset + 1);
        if(result - offset != length) {
            throw new WhatsAppStreamException.MalformedNode();
        }
        return result;
    }

    /**
     * Writes a complete node (size header, description, attributes,
     * content) to the output array.
     *
     * @param input  the node to write
     * @param output the destination byte array
     * @param offset the current write offset
     * @return the offset positioned past the last written byte
     */
    private static int writeNode(Node input, byte[] output, int offset){
        offset = writeList(input.size(), output, offset);
        offset = writeString(input.description(), output, offset);
        offset = writeAttributes(input.attributes(), output, offset);
        offset = writeContent(input, output, offset);
        return offset;
    }

    /**
     * Writes a list size tag followed by the size value, picking the
     * narrowest representation that fits.
     *
     * @param size   the list size
     * @param output the destination byte array
     * @param offset the current write offset
     * @return the offset positioned past the last written byte
     * @throws IllegalArgumentException if the size exceeds the 16 bit
     *         maximum
     */
    private static int writeList(int size, byte[] output, int offset) {
        if (size < UNSIGNED_BYTE_MAX_VALUE) {
            return writeList8((byte) size, output, offset);
        }else if (size < UNSIGNED_SHORT_MAX_VALUE) {
            return writeList16(size, output, offset);
        }else {
            throw new IllegalArgumentException("Cannot write list: overflow");
        }
    }

    /**
     * Writes a {@link NodeTags#LIST_8} tag followed by an 8 bit size byte.
     *
     * @param size   the list size
     * @param output the destination byte array
     * @param offset the current write offset
     * @return the offset positioned past the last written byte
     */
    private static int writeList8(byte size, byte[] output, int offset) {
        output[offset++] = LIST_8;
        output[offset++] = size;
        return offset;
    }

    /**
     * Writes a {@link NodeTags#LIST_16} tag followed by a 16 bit big endian
     * size value.
     *
     * @param size   the list size
     * @param output the destination byte array
     * @param offset the current write offset
     * @return the offset positioned past the last written byte
     */
    private static int writeList16(int size, byte[] output, int offset) {
        output[offset++] = LIST_16;
        SHORT_HANDLE.set(output, offset, (short) size);
        return offset + 2;
    }

    /**
     * Writes a string under the most efficient applicable strategy.
     *
     * @implNote The encoder picks the first strategy that applies in this
     *           order: empty literal, single byte token, dictionary token,
     *           nibble or hex packed run, length prefixed UTF-8.
     * @param input  the string to write
     * @param output the destination byte array
     * @param offset the current write offset
     * @return the offset positioned past the last written byte
     */
    private static int writeString(String input, byte[] output, int offset){
        if (input.isEmpty()) {
            output[offset++] = BINARY_8;
            output[offset++] = LIST_EMPTY;
            return offset;
        }

        var singleByteTokenIndex = SINGLE_BYTE_TOKENS.indexOf(input);
        if (singleByteTokenIndex != -1) {
            output[offset++] = (byte) singleByteTokenIndex;
            return offset;
        }

        var dictionary0TokenIndex = DICTIONARY_0_TOKENS.indexOf(input);
        if (dictionary0TokenIndex != -1) {
            output[offset++] = DICTIONARY_0;
            output[offset++] = (byte) dictionary0TokenIndex;
            return offset;
        }

        var dictionary1TokenIndex = DICTIONARY_1_TOKENS.indexOf(input);
        if (dictionary1TokenIndex != -1) {
            output[offset++] = DICTIONARY_1;
            output[offset++] = (byte) dictionary1TokenIndex;
            return offset;
        }

        var dictionary2TokenIndex = DICTIONARY_2_TOKENS.indexOf(input);
        if (dictionary2TokenIndex != -1) {
            output[offset++] = DICTIONARY_2;
            output[offset++] = (byte) dictionary2TokenIndex;
            return offset;
        }

        var dictionary3TokenIndex = DICTIONARY_3_TOKENS.indexOf(input);
        if (dictionary3TokenIndex != -1) {
            output[offset++] = DICTIONARY_3;
            output[offset++] = (byte) dictionary3TokenIndex;
            return offset;
        }

        var utf8Length = calculateUtf8Length(input);
        if (utf8Length < 128) {
            var packedType = getPackedType(input);
            if (packedType != -1) {
                return writePacked(input, packedType, output, offset);
            }
        }

        offset = writeBinary(utf8Length, output, offset);
        var encoded = input.getBytes(StandardCharsets.UTF_8);
        if(encoded.length != utf8Length) {
            throw new InternalError("Utf8 length mismatch");
        }
        System.arraycopy(encoded, 0, output, offset, utf8Length);
        return offset + utf8Length;
    }

    /**
     * Writes a binary length prefix using the narrowest tag that fits.
     *
     * @param input  the length value
     * @param output the destination byte array
     * @param offset the current write offset
     * @return the offset positioned past the last written byte
     */
    private static int writeBinary(int input, byte[] output, int offset) {
        if (input < UNSIGNED_BYTE_MAX_VALUE) {
            return writeBinary8((byte) input, output, offset);
        }else if (input < INT_20_MAX_VALUE) {
            return writeBinary20(input, output, offset);
        }else {
            return writeBinary32(input, output, offset);
        }
    }

    /**
     * Writes a {@link NodeTags#BINARY_8} tag followed by an 8 bit length
     * byte.
     *
     * @param input  the length value
     * @param output the destination byte array
     * @param offset the current write offset
     * @return the offset positioned past the last written byte
     */
    private static int writeBinary8(byte input, byte[] output, int offset) {
        output[offset++] = BINARY_8;
        output[offset++] = input;
        return offset;
    }

    /**
     * Writes a {@link NodeTags#BINARY_20} tag followed by a 20 bit big
     * endian length value.
     *
     * @param input  the length value
     * @param output the destination byte array
     * @param offset the current write offset
     * @return the offset positioned past the last written byte
     */
    private static int writeBinary20(int input, byte[] output, int offset) {
        output[offset++] = BINARY_20;
        output[offset++] = (byte) (input >> 16);
        output[offset++] = (byte) (input >> 8);
        output[offset++] = (byte) input;
        return offset;
    }

    /**
     * Writes a {@link NodeTags#BINARY_32} tag followed by a 32 bit big
     * endian length value.
     *
     * @param input  the length value
     * @param output the destination byte array
     * @param offset the current write offset
     * @return the offset positioned past the last written byte
     */
    private static int writeBinary32(int input, byte[] output, int offset) {
        output[offset++] = BINARY_32;
        INT_HANDLE.set(output, offset, input);
        return offset + 4;
    }

    /**
     * Determines whether the supplied string is eligible for nibble or hex
     * packed encoding.
     *
     * @implNote Nibble packing covers {@code [0-9.-]} and hex packing covers
     *           {@code [0-9A-F]}. Nibble packing is preferred when both
     *           shapes apply.
     * @param input the string to inspect
     * @return {@link NodeTags#NIBBLE_8}, {@link NodeTags#HEX_8}, or
     *         {@code -1} when neither shape applies
     */
    private static byte getPackedType(String input) {
        var nibble = true;
        var hex = true;
        for (var i = 0; i < input.length(); i++) {
            var ch = input.charAt(i);
            if (ch >= 128 || NIBBLE_ENCODE[ch] < 0) {
                nibble = false;
            }
            if (ch >= 128 || HEX_ENCODE[ch] < 0) {
                hex = false;
            }
            if (!nibble && !hex) {
                return -1;
            }
        }
        if (nibble) {
            return NIBBLE_8;
        } else {
            return HEX_8;
        }
    }

    /**
     * Writes a string in nibble or hex packed form.
     *
     * @implNote Two characters share each output byte. Odd lengths set the
     *           high bit of the byte count header and pad the trailing
     *           nibble with {@code 0xF}.
     * @param input  the string to encode
     * @param tag    {@link NodeTags#NIBBLE_8} or {@link NodeTags#HEX_8}
     * @param output the destination byte array
     * @param offset the current write offset
     * @return the offset positioned past the last written byte
     */
    private static int writePacked(String input, byte tag, byte[] output, int offset) {
        var table = tag == NIBBLE_8 ? NIBBLE_ENCODE : HEX_ENCODE;
        var len = input.length();
        output[offset++] = tag;
        var byteCount = (len + 1) / 2;
        if ((len & 1) == 1) {
            byteCount |= 128;
        }
        output[offset++] = (byte) byteCount;
        var i = 0;
        for (; i + 1 < len; i += 2) {
            output[offset++] = (byte) ((table[input.charAt(i)] << 4) | table[input.charAt(i + 1)]);
        }
        if (i < len) {
            output[offset++] = (byte) ((table[input.charAt(i)] << 4) | 0x0F);
        }
        return offset;
    }

    /**
     * Writes every entry of the supplied attribute map as a key value pair.
     *
     * @param attributes the attributes to write
     * @param output     the destination byte array
     * @param offset     the current write offset
     * @return the offset positioned past the last written byte
     */
    private static int writeAttributes(SequencedMap<String, ? extends NodeAttribute> attributes, byte[] output, int offset) {
        for (var entry : attributes.entrySet()) {
            offset = writeString(entry.getKey(), output, offset);
            offset = writeAttribute(entry.getValue(), output, offset);
        }
        return offset;
    }

    /**
     * Writes a single attribute value under its variant specific shape.
     *
     * @param attribute the attribute to write
     * @param output    the destination byte array
     * @param offset    the current write offset
     * @return the offset positioned past the last written byte
     */
    private static int writeAttribute(NodeAttribute attribute, byte[] output, int offset) {
        return switch (attribute) {
            case NodeAttribute.BytesAttribute(var buffer) -> writeBytes(buffer, output, offset);
            case NodeAttribute.JidAttribute(var jid) -> writeJid(jid, output, offset);
            case NodeAttribute.TextAttribute(var string) -> writeString(string, output, offset);
        };
    }

    /**
     * Writes the content slot of a node based on its concrete variant.
     *
     * @param content the node whose content to write
     * @param output  the destination byte array
     * @param offset  the current write offset
     * @return the offset positioned past the last written byte
     */
    private static int writeContent(Node content, byte[] output, int offset) {
        return switch (content) {
            case Node.EmptyNode _ -> offset;
            case Node.BytesNode(var _, var _, var buffer) -> writeBytes(buffer, output, offset);
            case Node.ContainerNode(var _, var _, var children) -> writeChildren(children, output, offset);
            case Node.JidNode(var _, var _, var jid) -> writeJid(jid, output, offset);
            case Node.TextNode(var _, var _, var text) -> writeString(text, output, offset);
        };
    }

    /**
     * Writes a list size header followed by the encoding of every child
     * node.
     *
     * @param values the child nodes to write
     * @param output the destination byte array
     * @param offset the current write offset
     * @return the offset positioned past the last written byte
     */
    private static int writeChildren(SequencedCollection<Node> values, byte[] output, int offset) {
        offset = writeList(values.size(), output, offset);
        for(var value : values) {
            offset = writeNode(value, output, offset);
        }
        return offset;
    }

    /**
     * Writes a binary blob with its length prefix.
     *
     * @param buffer the blob to write
     * @param output the destination byte array
     * @param offset the current write offset
     * @return the offset positioned past the last written byte
     */
    private static int writeBytes(byte[] buffer, byte[] output, int offset){
        var length = buffer.length;
        offset = writeBinary(length, output, offset);
        System.arraycopy(buffer, 0, output, offset, length);
        return offset + length;
    }

    /**
     * Writes a JID under the shape appropriate for its server and device.
     *
     * @implNote Messenger and interop JIDs carry a 16 bit device id;
     *           multi device JIDs carry an 8 bit device id and a domain
     *           code; plain JIDs use a user and server pair.
     * @param jid    the JID to write
     * @param output the destination byte array
     * @param offset the current write offset
     * @return the offset positioned past the last written byte
     */
    private static int writeJid(Jid jid, byte[] output, int offset){
        if (jid.hasMessengerServer()) {
            output[offset++] = JID_FB;
            offset = writeString(jid.user(), output, offset);
            SHORT_HANDLE.set(output, offset, (short) jid.device());
            offset += 2;
            return writeString(jid.server().address(), output, offset);
        } else if (jid.hasInteropServer()) {
            output[offset++] = JID_INTEROP;
            var user = jid.user();
            var dashIndex = user.indexOf('-');
            var integrator = 0;
            var actualUser = user;
            if (dashIndex >= 0) {
                for (var i = 0; i < dashIndex; i++) {
                    integrator = integrator * 10 + (user.charAt(i) - '0');
                }
                actualUser = user.substring(dashIndex + 1);
            }
            offset = writeString(actualUser, output, offset);
            SHORT_HANDLE.set(output, offset, (short) jid.device());
            offset += 2;
            SHORT_HANDLE.set(output, offset, (short) integrator);
            offset += 2;
            return offset;
        } else if (jid.hasDevice()) {
            output[offset++] = AD_JID;
            output[offset++] = (byte) getDomainForServer(jid.server());
            output[offset++] = (byte) jid.device();
            return writeString(jid.user(), output, offset);
        } else {
            output[offset++] = JID_PAIR;
            if(jid.hasUser()) {
                offset = writeString(jid.user(), output, offset);
            }else {
                output[offset++] = LIST_EMPTY;
            }
            return writeString(jid.server().address(), output, offset);
        }
    }

    /**
     * Maps a {@link JidServer} to its multi device JID domain code.
     *
     * @param server the server to translate
     * @return one of the {@code DOMAIN_*} constants in {@link NodeTags}
     */
    private static int getDomainForServer(JidServer server) {
        return switch (server.type()) {
            case LID -> DOMAIN_LID;
            case HOSTED -> DOMAIN_HOSTED;
            case HOSTED_LID -> DOMAIN_HOSTED_LID;
            default -> DOMAIN_WHATSAPP;
        };
    }
}
