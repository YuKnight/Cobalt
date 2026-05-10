package com.github.auties00.cobalt.node.binary;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeAttribute;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.Inflater;
import java.util.zip.DataFormatException;

import static com.github.auties00.cobalt.node.binary.NodeTags.*;
import static com.github.auties00.cobalt.node.binary.NodeTokens.*;

/**
 * Parses {@link Node} trees from WhatsApp's binary stanza format.
 *
 * <p>Inbound WebSocket frames from the WhatsApp server are either raw or
 * DEFLATE compressed binary blobs encoded with the WAWap protocol. This
 * class turns those blobs back into node trees by reading the leading size
 * header, description, attribute list, and typed content (sized list, JID
 * variant, hex or nibble packed run, dictionary token, single byte token,
 * or binary blob).
 *
 * <p>Callers create a decoder via {@link #of(ByteBuffer)}, which inspects
 * the leading flags byte and selects the direct or inflating
 * implementation, and then invoke {@link #decode()} to obtain the root
 * node. The decoder implements {@link AutoCloseable} so the underlying
 * {@link Inflater} can be released deterministically.
 *
 * @see Node
 * @see NodeAttribute
 * @see NodeEncoder
 * @see NodeTokens
 * @see NodeTags
 */
@WhatsAppWebModule(moduleName = "WAWap")
public sealed abstract class NodeDecoder implements AutoCloseable {
    /**
     * VarHandle that reads 16 bit big endian integers from a byte array.
     */
    private static final VarHandle SHORT_HANDLE = MethodHandles.byteArrayViewVarHandle(short[].class, ByteOrder.BIG_ENDIAN);

    /**
     * VarHandle that reads 32 bit big endian integers from a byte array.
     */
    private static final VarHandle INT_HANDLE = MethodHandles.byteArrayViewVarHandle(int[].class, ByteOrder.BIG_ENDIAN);

    /**
     * Alphabet used to decode nibble packed strings.
     */
    private static final char[] NIBBLE_ALPHABET = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-', '.', '�', '�', '�', '�'};

    /**
     * Alphabet used to decode hex packed strings.
     */
    private static final char[] HEX_ALPHABET = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    /**
     * Capacity of the temporary buffer used by the inflating decoder.
     */
    private static final int DECOMPRESSION_BUFFER_SIZE = 8192;

    /**
     * Source buffer that holds the raw or compressed bytes still to be
     * read.
     */
    final ByteBuffer source;

    /**
     * Builds a decoder backed by the supplied source buffer.
     *
     * @param source the buffer containing the encoded stanza
     */
    private NodeDecoder(ByteBuffer source) {
        this.source = source;
    }

    /**
     * Builds a decoder appropriate for the leading flags byte of the
     * supplied stanza buffer.
     * @param source the buffer containing the encoded stanza
     * @return an inflating decoder when the compression flag is set, a
     *         direct decoder otherwise
     */
    @WhatsAppWebExport(moduleName = "WAWap", exports = "decodeStanza",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public static NodeDecoder of(ByteBuffer source) {
        var flags = source.get() & 0xFF;
        if ((flags & 2) != 0) {
            return new Compressed(source);
        } else {
            return new Uncompressed(source);
        }
    }

    /**
     * Decodes the root {@link Node} of this stanza.
     *
     * @return the decoded node
     * @throws IOException if the input is truncated or fails to decompress
     */
    @WhatsAppWebExport(moduleName = "WAWap", exports = "decodeStanza",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public final Node decode() throws IOException {
        return readNode();
    }

    /**
     * Returns whether this decoder still has bytes available to be read.
     *
     * @return {@code true} when more bytes remain
     */
    public abstract boolean hasData();

    /**
     * Reads the next byte as an unsigned value.
     *
     * @return the next byte in the {@code 0..255} range
     * @throws IOException if no more bytes are available
     */
    abstract int read() throws IOException;

    /**
     * Reads the next two bytes as an unsigned 16 bit big endian integer.
     *
     * @return the next 16 bit value in the {@code 0..65535} range
     * @throws IOException if fewer than two bytes are available
     */
    abstract int readShort() throws IOException;

    /**
     * Reads the next four bytes as a signed 32 bit big endian integer.
     *
     * @return the next 32 bit signed value
     * @throws IOException if fewer than four bytes are available
     */
    abstract int readInt() throws IOException;

    /**
     * Reads the next {@code length} bytes into a fresh array.
     *
     * @param length the number of bytes to read
     * @return a newly allocated array holding the bytes that were read
     * @throws IOException if fewer than {@code length} bytes are available
     */
    abstract byte[] readBytes(int length) throws IOException;

    /**
     * Reads a complete node from the source.
     * @return the decoded node
     * @throws IOException if the stream is truncated or holds a malformed
     *         tag
     */
    private Node readNode() throws IOException {
        var size = readNodeSize();
        if(size == 0) {
            throw new IOException("Unexpected empty node");
        }

        var description = readString();
        var attrs = readAttributes(size - 1);

        if((size & 1) == 1) {
            return new Node.EmptyNode(description, attrs);
        }

        var tag = (byte) read();
        return switch (tag) {
            case LIST_EMPTY -> new Node.EmptyNode(description, attrs);
            case JID_INTEROP -> new Node.JidNode(description, attrs, readInteropJid());
            case JID_FB -> new Node.JidNode(description, attrs, readFbJid());
            case AD_JID -> new Node.JidNode(description, attrs, readAdJid());
            case LIST_8 -> new Node.ContainerNode(description, attrs, readList8());
            case LIST_16 -> new Node.ContainerNode(description, attrs, readList16());
            case JID_PAIR -> new Node.JidNode(description, attrs, readJidPair());
            case HEX_8 -> new Node.TextNode(description, attrs, readPacked(HEX_ALPHABET));
            case BINARY_8 -> new Node.BytesNode(description, attrs, readBinary8());
            case BINARY_20 -> new Node.BytesNode(description, attrs, readBinary20());
            case BINARY_32 -> new Node.BytesNode(description, attrs, readBinary32());
            case NIBBLE_8 -> new Node.TextNode(description, attrs, readPacked(NIBBLE_ALPHABET));
            case DICTIONARY_0 -> new Node.TextNode(description, attrs, readDictionaryToken(DICTIONARY_0_TOKENS));
            case DICTIONARY_1 -> new Node.TextNode(description, attrs, readDictionaryToken(DICTIONARY_1_TOKENS));
            case DICTIONARY_2 -> new Node.TextNode(description, attrs, readDictionaryToken(DICTIONARY_2_TOKENS));
            case DICTIONARY_3 -> new Node.TextNode(description, attrs, readDictionaryToken(DICTIONARY_3_TOKENS));
            default -> {
                var index = tag & 0xFF;
                if (index >= 240) {
                    throw new IOException("Unexpected tag in node content: " + index);
                }
                yield new Node.TextNode(description, attrs, readSingleByteToken(tag));
            }
        };
    }

    /**
     * Reads a list size header.
     *
     * @return the list size
     * @throws IOException if reading fails
     * @throws IllegalStateException if the leading byte is not a known
     *         list size tag
     */
    private int readNodeSize() throws IOException {
        var token = (byte) read();
        return switch (token) {
            case LIST_8 -> read() & 0xFF;
            case LIST_16 -> readShort();
            default -> throw new IllegalStateException("Unexpected value: " + token);
        };
    }

    /**
     * Reads a string under whichever encoding tag appears next.
     *
     * @return the decoded string, or {@code null} when the leading tag is
     *         {@link NodeTags#LIST_EMPTY}
     * @throws IOException if the leading tag is not a string shape
     */
    private String readString() throws IOException {
        var tag = (byte) read();
        return switch (tag) {
            case LIST_EMPTY -> null;
            case HEX_8 -> readPacked(HEX_ALPHABET);
            case NIBBLE_8 -> readPacked(NIBBLE_ALPHABET);
            case BINARY_8 -> new String(readBinary8(), StandardCharsets.UTF_8);
            case BINARY_20 -> new String(readBinary20(), StandardCharsets.UTF_8);
            case BINARY_32 -> new String(readBinary32(), StandardCharsets.UTF_8);
            case DICTIONARY_0 -> readDictionaryToken(DICTIONARY_0_TOKENS);
            case DICTIONARY_1 -> readDictionaryToken(DICTIONARY_1_TOKENS);
            case DICTIONARY_2 -> readDictionaryToken(DICTIONARY_2_TOKENS);
            case DICTIONARY_3 -> readDictionaryToken(DICTIONARY_3_TOKENS);
            default -> {
                var index = tag & 0xFF;
                if (index >= 240) {
                    throw new IOException("Unexpected tag in string position: " + index);
                }
                yield readSingleByteToken(tag);
            }
        };
    }

    /**
     * Reads a binary blob with an 8 bit length prefix.
     *
     * @return the bytes that were read
     * @throws IOException if the stream is truncated
     */
    private byte[] readBinary8() throws IOException {
        var size = read() & 0xFF;
        return readBytes(size);
    }

    /**
     * Reads a binary blob with a 20 bit big endian length prefix.
     *
     * @return the bytes that were read
     * @throws IOException if the stream is truncated
     */
    private byte[] readBinary20() throws IOException {
        var size = ((read() & 0x0F) << 16)
                   | (read() << 8)
                   | read();
        return readBytes(size);
    }

    /**
     * Reads a binary blob with a 32 bit big endian length prefix.
     *
     * @return the bytes that were read
     * @throws IOException if the stream is truncated
     */
    private byte[] readBinary32() throws IOException {
        return readBytes(readInt());
    }

    /**
     * Reads an 8 bit token index and resolves it through the supplied
     * dictionary.
     *
     * @param dictionary the dictionary to look up
     * @return the resolved string
     * @throws IOException if the stream is truncated
     */
    private String readDictionaryToken(NodeTokens dictionary) throws IOException {
        var index = read() & 0xFF;
        return dictionary.get(index);
    }

    /**
     * Resolves a single byte token through {@link NodeTokens#SINGLE_BYTE_TOKENS}.
     *
     * @param tag the token byte
     * @return the resolved string
     */
    private String readSingleByteToken(byte tag) {
        var index = tag & 0xFF;
        return SINGLE_BYTE_TOKENS.get(index);
    }

    /**
     * Reads {@code size / 2} attribute key value pairs preserving their
     * declaration order.
     *
     * @param size the number of size units that the attribute block
     *             consumes (always even)
     * @return the parsed attribute map
     * @throws IOException if the stream is truncated
     */
    private SequencedMap<String, NodeAttribute> readAttributes(int size) throws IOException {
        var attributes = new LinkedHashMap<String, NodeAttribute>(size / 2);
        while (size >= 2) {
            var key = readString();
            var value = readAttribute();
            attributes.put(key, value);
            size -= 2;
        }
        return attributes;
    }

    /**
     * Reads a single attribute value under whichever encoding tag appears
     * next.
     *
     * @return the parsed attribute, or {@code null} for {@link NodeTags#LIST_EMPTY}
     *         and list shapes
     * @throws IOException if the stream is truncated or holds a malformed
     *         tag
     */
    private NodeAttribute readAttribute() throws IOException {
        var tag = (byte) read();
        return switch (tag) {
            case LIST_EMPTY -> null;
            case JID_INTEROP -> new NodeAttribute.JidAttribute(readInteropJid());
            case JID_FB -> new NodeAttribute.JidAttribute(readFbJid());
            case AD_JID -> new NodeAttribute.JidAttribute(readAdJid());
            case LIST_8 -> {
                readList8();
                yield null;
            }
            case LIST_16 -> {
                readList16();
                yield null;
            }
            case JID_PAIR -> new NodeAttribute.JidAttribute(readJidPair());
            case HEX_8 -> new NodeAttribute.TextAttribute(readPacked(HEX_ALPHABET));
            case BINARY_8 -> new NodeAttribute.BytesAttribute(readBinary8());
            case BINARY_20 -> new NodeAttribute.BytesAttribute(readBinary20());
            case BINARY_32 -> new NodeAttribute.BytesAttribute(readBinary32());
            case NIBBLE_8 -> new NodeAttribute.TextAttribute(readPacked(NIBBLE_ALPHABET));
            case DICTIONARY_0 -> new NodeAttribute.TextAttribute(readDictionaryToken(DICTIONARY_0_TOKENS));
            case DICTIONARY_1 -> new NodeAttribute.TextAttribute(readDictionaryToken(DICTIONARY_1_TOKENS));
            case DICTIONARY_2 -> new NodeAttribute.TextAttribute(readDictionaryToken(DICTIONARY_2_TOKENS));
            case DICTIONARY_3 -> new NodeAttribute.TextAttribute(readDictionaryToken(DICTIONARY_3_TOKENS));
            default -> {
                var index = tag & 0xFF;
                if (index >= 240) {
                    throw new IOException("Unexpected tag in attribute position: " + index);
                }
                yield new NodeAttribute.TextAttribute(readSingleByteToken(tag));
            }
        };
    }

    /**
     * Reads a list of child nodes with an 8 bit length prefix.
     *
     * @return the parsed children in declaration order
     * @throws IOException if the stream is truncated
     */
    private SequencedCollection<Node> readList8() throws IOException {
        var length = read() & 0xFF;
        return readList(length);
    }

    /**
     * Reads a list of child nodes with a 16 bit big endian length prefix.
     *
     * @return the parsed children in declaration order
     * @throws IOException if the stream is truncated
     */
    private SequencedCollection<Node> readList16() throws IOException {
        return readList(readShort());
    }

    /**
     * Reads {@code size} consecutive child nodes.
     *
     * @param size the number of nodes to read
     * @return the parsed children in declaration order
     * @throws IOException if the stream is truncated
     */
    private SequencedCollection<Node> readList(int size) throws IOException {
        var results = new ArrayList<Node>(size);
        for (var index = 0; index < size; index++) {
            var node = readNode();
            results.add(node);
        }
        return results;
    }

    /**
     * Reads a packed string under the supplied alphabet.
     * @param alphabet the 16 entry alphabet to translate nibbles through
     * @return the decoded string
     * @throws IOException if the stream is truncated
     */
    private String readPacked(char[] alphabet) throws IOException {
        var token = read() & 0xFF;
        var start = token >>> 7;
        var end = token & 127;
        var string = new char[2 * end - start];
        for(var index = 0; index < string.length - 1; index += 2) {
            token = read() & 0xFF;
            string[index] = alphabet[token >>> 4];
            string[index + 1] = alphabet[15 & token];
        }
        if (start != 0) {
            token = read() & 0xFF;
            string[string.length - 1] = alphabet[token >>> 4];
        }
        return String.valueOf(string);
    }

    /**
     * Reads a {@link NodeTags#JID_PAIR} body.
     *
     * @return the parsed JID
     * @throws IOException if the stream is truncated
     * @throws NullPointerException if the server component is missing
     */
    private Jid readJidPair() throws IOException {
        var user = readString();
        var server = JidServer.of(Objects.requireNonNull(readString(), "Malformed value pair: no server"));
        return user == null ? Jid.of(server) : Jid.of(user, server);
    }

    /**
     * Reads an {@link NodeTags#AD_JID} body.
     * @return the parsed JID
     * @throws IOException if the stream is truncated or carries an unknown
     *         domain code
     */
    private Jid readAdJid() throws IOException {
        var domainType = read() & 0xFF;
        var device = read() & 0xFF;
        var user = readString();
        var server = switch (domainType) {
            case DOMAIN_WHATSAPP -> JidServer.user();
            case DOMAIN_LID -> JidServer.lid();
            case DOMAIN_HOSTED_LID -> JidServer.hostedLid();
            default -> {
                if ((domainType & 1) == 0 && (domainType & DOMAIN_HOSTED) != 0) {
                    yield JidServer.hosted();
                }
                throw new IOException("Invalid AD_JID domain type: " + domainType);
            }
        };
        return Jid.of(user, server, device, 0);
    }

    /**
     * Reads a {@link NodeTags#JID_FB} body.
     * @return the parsed JID
     * @throws IOException if the stream is truncated
     */
    private Jid readFbJid() throws IOException {
        var user = readString();
        var device = readShort();
        var _ = readString();
        return Jid.of(user, JidServer.messenger(), device, 0);
    }

    /**
     * Reads a {@link NodeTags#JID_INTEROP} body.
     * @return the parsed JID
     * @throws IOException if the stream is truncated
     */
    private Jid readInteropJid() throws IOException {
        var user = readString();
        var device = readShort();
        var integrator = readShort();
        var _ = readString();
        return Jid.of(integrator + "-" + user, JidServer.interop(), device, 0);
    }

    /**
     * {@inheritDoc}
     *
     * @throws IOException if the decoder cannot be closed
     */
    @Override
    public abstract void close() throws IOException;

    /**
     * Decoder implementation that reads directly from the source buffer
     * without decompressing.
     */
    private static final class Uncompressed extends NodeDecoder {
        /**
         * Builds an uncompressed decoder backed by the supplied buffer.
         *
         * @param source the buffer to read from
         */
        Uncompressed(ByteBuffer source) {
            super(source);
        }

        @Override
        public boolean hasData() {
            return source.hasRemaining();
        }

        @Override
        int read() throws IOException {
            if (!source.hasRemaining()) {
                throw new IOException("Unexpected end of data");
            }
            return source.get() & 0xFF;
        }

        @Override
        int readShort() throws IOException {
            if (source.remaining() < 2) {
                throw new IOException("Unexpected end of data");
            }
            return source.getShort() & 0xFFFF;
        }

        @Override
        int readInt() throws IOException {
            if (source.remaining() < 4) {
                throw new IOException("Unexpected end of data");
            }
            return source.getInt();
        }

        @Override
        byte[] readBytes(int length) throws IOException {
            if (source.remaining() < length) {
                throw new IOException("Insufficient data available");
            }
            var result = new byte[length];
            source.get(result);
            return result;
        }

        @Override
        public void close() {
        }
    }

    /**
     * Decoder implementation that inflates DEFLATE compressed source bytes
     * through a buffered staging area before serving reads.
     */
    private static final class Compressed extends NodeDecoder {
        /**
         * Inflater that decompresses the source bytes.
         */
        private final Inflater inflater;

        /**
         * Staging buffer that holds inflated bytes pending consumption.
         */
        private final byte[] decompressionBuffer;

        /**
         * Working buffer that feeds compressed bytes into the inflater.
         */
        private final byte[] inflaterInputBuffer;

        /**
         * Read offset into {@link #decompressionBuffer}.
         */
        private int bufferPosition;

        /**
         * Count of valid bytes in {@link #decompressionBuffer}.
         */
        private int bufferLimit;

        /**
         * Builds a compressed decoder backed by the supplied buffer.
         *
         * @param source the buffer of DEFLATE compressed bytes
         */
        Compressed(ByteBuffer source) {
            super(source);
            this.inflater = new Inflater();
            this.decompressionBuffer = new byte[DECOMPRESSION_BUFFER_SIZE];
            this.inflaterInputBuffer = new byte[DECOMPRESSION_BUFFER_SIZE];
        }

        @Override
        public boolean hasData() {
            return bufferPosition < bufferLimit
                   || !inflater.finished()
                   || source.hasRemaining();
        }

        @Override
        int read() throws IOException {
            if (bufferPosition >= bufferLimit) {
                fillDecompressionBuffer();
            }

            if (bufferPosition >= bufferLimit) {
                throw new IOException("Unexpected end of decompressed data");
            }

            return decompressionBuffer[bufferPosition++] & 0xFF;
        }

        @Override
        int readShort() throws IOException {
            ensureAvailable(2);
            var value = (short) SHORT_HANDLE.get(decompressionBuffer, bufferPosition);
            bufferPosition += 2;
            return value & 0xFFFF;
        }

        @Override
        int readInt() throws IOException {
            ensureAvailable(4);
            var value = (int) INT_HANDLE.get(decompressionBuffer, bufferPosition);
            bufferPosition += 4;
            return value;
        }

        /**
         * Ensures that at least {@code needed} contiguous inflated bytes
         * are available starting at {@link #bufferPosition}.
         * @param needed the minimum number of contiguous bytes required
         * @throws IOException if the source ends before enough bytes are
         *         inflated
         */
        private void ensureAvailable(int needed) throws IOException {
            var available = bufferLimit - bufferPosition;
            if (available >= needed) {
                return;
            }
            if (available > 0) {
                System.arraycopy(decompressionBuffer, bufferPosition, decompressionBuffer, 0, available);
            }
            bufferPosition = 0;
            bufferLimit = available;
            try {
                while (bufferLimit < needed) {
                    if (inflater.needsInput() && source.hasRemaining()) {
                        var toRead = Math.min(source.remaining(), inflaterInputBuffer.length);
                        source.get(inflaterInputBuffer, 0, toRead);
                        inflater.setInput(inflaterInputBuffer, 0, toRead);
                    }
                    var inflated = inflater.inflate(decompressionBuffer, bufferLimit, decompressionBuffer.length - bufferLimit);
                    if (inflated == 0) {
                        throw new IOException("Unexpected end of decompressed data");
                    }
                    bufferLimit += inflated;
                }
            } catch (DataFormatException e) {
                throw new IOException("Decompression error", e);
            }
        }

        @Override
        byte[] readBytes(int length) throws IOException {
            var result = new byte[length];
            var offset = 0;
            while (offset < length) {
                if (bufferPosition >= bufferLimit) {
                    fillDecompressionBuffer();
                }

                if (bufferPosition >= bufferLimit) {
                    throw new IOException("Unexpected end of decompressed data");
                }

                var available = bufferLimit - bufferPosition;
                var toRead = Math.min(available, length - offset);
                System.arraycopy(decompressionBuffer, bufferPosition, result, offset, toRead);
                bufferPosition += toRead;
                offset += toRead;
            }
            return result;
        }

        /**
         * Refills the staging buffer with another inflated block.
         *
         * @throws IOException if the source bytes are malformed
         */
        private void fillDecompressionBuffer() throws IOException {
            try {
                if (inflater.needsInput() && source.hasRemaining()) {
                    var available = Math.min(source.remaining(), inflaterInputBuffer.length);
                    source.get(inflaterInputBuffer, 0, available);
                    inflater.setInput(inflaterInputBuffer, 0, available);
                }

                bufferPosition = 0;
                bufferLimit = inflater.inflate(decompressionBuffer);
            } catch (DataFormatException e) {
                throw new IOException("Decompression error", e);
            }
        }

        @Override
        public void close() {
            inflater.close();
        }
    }
}
