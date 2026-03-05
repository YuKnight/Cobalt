package com.github.auties00.cobalt.socket.layer.tunnel.http;

import com.github.auties00.cobalt.socket.layer.SocketClientLayer;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * A buffered byte reader for HTTP-style response parsing on top of a
 * {@link SocketClientLayer}.
 *
 * <p>This utility centralizes status-line parsing, header scanning, and
 * timeout/EOF handling used by CONNECT and WebSocket upgrade handshakes.
 *
 * <p>The internal read buffer is backed by a raw {@code byte[]} with
 * manual position/limit tracking, so the per-byte fast path in
 * {@link #nextByte(long)} reduces to a bounds check and an array load
 * with no virtual dispatch.
 *
 * <p>In addition to the byte-at-a-time API ({@link #nextByte(long)},
 * {@link #nextHeaderByte(long)}), bulk buffer primitives are exposed
 * so that call sites can implement zero-resize, exact-sized piece
 * collection for header value reads:
 * {@link #distanceToLineFeed()}, {@link #peekBuffered(int)},
 * {@link #getBuffered(byte[], int, int)}, {@link #skipBuffered(int)},
 * {@link #bufferedRemaining()}, {@link #refillBuffer(long)}, and
 * {@link #accountHeaderBytes(int)}.
 */
public final class HttpResponseReader {
    private static final int HTTP_VERSION_MAJOR = 1;
    private static final int HTTP_VERSION_MINOR_MIN = 0;
    private static final int HTTP_VERSION_MINOR_MAX = 1;
    private static final byte CARRIAGE_RETURN = '\r';
    private static final byte LINE_FEED = '\n';
    private static final byte SPACE = ' ';

    /**
     * The capacity of the internal read buffer.
     */
    private static final int BUFFER_SIZE = 512;

    /**
     * The transport layer used for binary reads.
     */
    private final SocketClientLayer innerLayer;

    /**
     * The error message used when a deadline is exceeded.
     */
    private final String timeoutMessage;

    /**
     * The error message used when header bytes exceed the configured limit.
     */
    private final String headersTooLargeMessage;

    /**
     * The error message used when EOF is reached while parsing.
     */
    private final String eofMessage;

    /**
     * The maximum number of header bytes allowed before an error is raised.
     */
    private final int maxHeaderSize;

    /**
     * The maximum number of spaces tolerated in the status line before the
     * status code.
     */
    private final int maxStatusLineSpaces;

    /**
     * The raw read buffer.  Bytes from the transport layer are read into
     * this array via {@link #bufWrapper} and then consumed directly with
     * {@link #bufPos}/{@link #bufLimit}.
     */
    private byte[] buf;

    /**
     * A reusable {@link ByteBuffer} wrapper around {@link #buf}, used only
     * for the {@link SocketClientLayer#readBinary(ByteBuffer, boolean)} call
     * during buffer refills.
     */
    private ByteBuffer bufWrapper;

    /**
     * The index of the next byte to return from {@link #buf}.
     */
    private int bufPos;

    /**
     * One past the index of the last valid byte in {@link #buf}.
     */
    private int bufLimit;

    /**
     * Running count of header bytes consumed since the last call to
     * {@link #startHeaderSection()}.
     */
    private int headerBytesRead;

    /**
     * Creates a reusable response reader.
     *
     * @param innerLayer             the source layer used for reads
     * @param timeoutMessage         message used when deadlines are exceeded
     * @param headersTooLargeMessage message used when header bytes exceed the limit
     * @param eofMessage             message used when EOF is reached while parsing
     * @param maxHeaderSize          maximum header bytes allowed
     * @param maxStatusLineSpaces    maximum spaces tolerated in status line before code
     */
    public HttpResponseReader(
            SocketClientLayer innerLayer,
            String timeoutMessage,
            String headersTooLargeMessage,
            String eofMessage,
            int maxHeaderSize,
            int maxStatusLineSpaces
    ) {
        this.innerLayer = innerLayer;
        this.timeoutMessage = timeoutMessage;
        this.headersTooLargeMessage = headersTooLargeMessage;
        this.eofMessage = eofMessage;
        this.maxHeaderSize = maxHeaderSize;
        this.maxStatusLineSpaces = maxStatusLineSpaces;
    }

    /**
     * Clears buffered state before starting a new HTTP response parse.
     */
    public void reset() {
        this.buf = null;
        this.bufWrapper = null;
        this.bufPos = 0;
        this.bufLimit = 0;
        this.headerBytesRead = 0;
    }

    /**
     * Returns unread buffered bytes from the last refill, if any.
     *
     * @return leftover bytes in read mode, or {@code null} if none
     */
    public ByteBuffer remainingBytes() {
        if (buf == null || bufPos >= bufLimit) {
            return null;
        }
        return ByteBuffer.wrap(buf, bufPos, bufLimit - bufPos).slice();
    }

    /**
     * Starts header-byte accounting for an upcoming header section.
     */
    public void startHeaderSection() {
        this.headerBytesRead = 0;
    }

    /**
     * Reads and parses an HTTP status line from the current stream position.
     *
     * @param deadline absolute deadline in epoch milliseconds
     * @return the parsed status code
     * @throws IOException on timeout, malformed response, or EOF
     */
    public int readStatusLine(long deadline) throws IOException {
        byte b;
        do {
            b = nextByte(deadline);
        } while (b == SPACE || b == CARRIAGE_RETURN || b == LINE_FEED);

        if (b != 'H'
                || nextByte(deadline) != 'T'
                || nextByte(deadline) != 'T'
                || nextByte(deadline) != 'P'
                || nextByte(deadline) != '/') {
            throw new IOException("Invalid HTTP response: expected HTTP/1.0 or HTTP/1.1");
        }

        var major = nextByte(deadline) - '0';
        if (major != HTTP_VERSION_MAJOR) {
            throw new IOException("Invalid HTTP response: expected HTTP/1.0 or HTTP/1.1");
        }
        if (nextByte(deadline) != '.') {
            throw new IOException("Invalid HTTP response: expected HTTP/1.0 or HTTP/1.1");
        }

        var minor = nextByte(deadline) - '0';
        if (minor != HTTP_VERSION_MINOR_MIN && minor != HTTP_VERSION_MINOR_MAX) {
            throw new IOException("Invalid HTTP response: unsupported version HTTP/1." + minor);
        }

        b = nextByte(deadline);
        if (b != SPACE) {
            throw new IOException("Invalid HTTP response: expected space after HTTP version");
        }
        for (var spacesSkipped = 1; ; spacesSkipped++) {
            b = nextByte(deadline);
            if (b != SPACE) {
                break;
            }
            if (spacesSkipped >= maxStatusLineSpaces) {
                throw new IOException("Invalid HTTP response: too many spaces in status line");
            }
        }

        var d1 = b;
        var d2 = nextByte(deadline);
        var d3 = nextByte(deadline);
        if (d1 < '0' || d1 > '9' || d2 < '0' || d2 > '9' || d3 < '0' || d3 > '9') {
            throw new IOException("Invalid HTTP response: status code contains non-digit characters");
        }

        return (d1 - '0') * 100 + (d2 - '0') * 10 + (d3 - '0');
    }

    /**
     * Skips response headers until the terminating empty line.
     *
     * @param deadline absolute deadline in epoch milliseconds
     * @throws IOException on timeout, EOF, or size overflow
     */
    public void skipHeaders(long deadline) throws IOException {
        startHeaderSection();
        var firstLine = true;
        var lineIsEmpty = false;

        while (innerLayer.isConnected()) {
            var b = nextHeaderByte(deadline);

            if (b == CARRIAGE_RETURN) {
                continue;
            }

            if (b == LINE_FEED) {
                if (firstLine) {
                    firstLine = false;
                    lineIsEmpty = true;
                    continue;
                }
                if (lineIsEmpty) {
                    return;
                }
                lineIsEmpty = true;
                continue;
            }

            firstLine = false;
            lineIsEmpty = false;
        }

        throw new IOException(eofMessage);
    }

    /**
     * Reads one header byte and enforces the configured header limit.
     *
     * @param deadline absolute deadline in epoch milliseconds
     * @return the next header byte
     * @throws IOException on timeout, EOF, or size overflow
     */
    public byte nextHeaderByte(long deadline) throws IOException {
        if (++headerBytesRead > maxHeaderSize) {
            throw new IOException(headersTooLargeMessage);
        }
        return nextByte(deadline);
    }

    /**
     * Skips bytes until and including the next LF.
     *
     * @param deadline absolute deadline in epoch milliseconds
     * @throws IOException on timeout, EOF, or size overflow
     */
    public void skipToEndOfLine(long deadline) throws IOException {
        byte b;
        do {
            b = nextHeaderByte(deadline);
        } while (b != LINE_FEED);
    }

    /**
     * Scans the current buffer for a LF byte without consuming any data.
     *
     * @return distance in bytes from the current position to the LF
     *         (exclusive), or {@code -1} if no LF is buffered
     */
    public int distanceToLineFeed() {
        for (var i = bufPos; i < bufLimit; i++) {
            if (buf[i] == LINE_FEED) {
                return i - bufPos;
            }
        }
        return -1;
    }

    /**
     * Returns the byte at the given offset from the current buffer
     * position without consuming it.
     *
     * @param offset the zero-based offset from the current position
     * @return the byte at that offset
     */
    public byte peekBuffered(int offset) {
        return buf[bufPos + offset];
    }

    /**
     * Copies exactly {@code length} bytes from the current buffer position
     * into {@code dst} and advances the buffer position.
     *
     * @param dst    the destination array
     * @param dstPos the starting offset in {@code dst}
     * @param length the number of bytes to copy
     */
    public void getBuffered(byte[] dst, int dstPos, int length) {
        System.arraycopy(buf, bufPos, dst, dstPos, length);
        bufPos += length;
    }

    /**
     * Advances the buffer position by {@code count} bytes without copying.
     *
     * @param count the number of bytes to skip
     */
    public void skipBuffered(int count) {
        bufPos += count;
    }

    /**
     * Returns the number of unread bytes remaining in the buffer.
     *
     * @return buffered byte count
     */
    public int bufferedRemaining() {
        return bufLimit - bufPos;
    }

    /**
     * Adds {@code count} to the header-byte counter and throws if the
     * configured limit is exceeded.
     *
     * <p>Call sites that perform bulk reads via
     * {@link #getBuffered(byte[], int, int)} must use this method to
     * maintain the header size limit that
     * {@link #nextHeaderByte(long)} enforces per byte.
     *
     * @param count the number of header bytes consumed
     * @throws IOException if the header size limit is exceeded
     */
    public void accountHeaderBytes(int count) throws IOException {
        headerBytesRead += count;
        if (headerBytesRead > maxHeaderSize) {
            throw new IOException(headersTooLargeMessage);
        }
    }

    /**
     * Fills the read buffer from the transport layer.
     *
     * <p>After a successful refill, the buffer position is {@code 0} and
     * the limit is the number of bytes read.  Allocates the buffer on
     * first use.
     *
     * @param deadline absolute deadline in epoch milliseconds
     * @throws IOException on timeout, EOF, or zero-length read
     */
    public void refillBuffer(long deadline) throws IOException {
        if (buf == null) {
            buf = new byte[BUFFER_SIZE];
            bufWrapper = ByteBuffer.wrap(buf);
        }
        checkDeadline(deadline);
        bufWrapper.clear();
        var length = innerLayer.readBinary(bufWrapper, false);
        if (length > 0) {
            bufPos = 0;
            bufLimit = length;
            return;
        }
        if (length < 0) {
            throw new IOException(eofMessage);
        }
        throw new IOException("Unexpected zero-length read from transport layer");
    }

    /**
     * Reads the next byte from the response stream using buffered refills.
     *
     * <p>The fast path is a single array bounds check and load, with no
     * virtual dispatch.  Buffer refills are handled by the out-of-line
     * {@link #nextByteSlow(long)} method so that the JIT can inline this
     * method into callers without code bloat.
     *
     * @param deadline absolute deadline in epoch milliseconds
     * @return the next byte
     * @throws IOException on timeout or EOF
     */
    public byte nextByte(long deadline) throws IOException {
        if (bufPos < bufLimit) {
            return buf[bufPos++];
        }
        return nextByteSlow(deadline);
    }

    /**
     * Slow path for {@link #nextByte(long)}: refills the buffer from the
     * transport layer and returns the first byte.
     *
     * @param deadline absolute deadline in epoch milliseconds
     * @return the first byte of the newly filled buffer
     * @throws IOException on timeout, EOF, or zero-length read
     */
    private byte nextByteSlow(long deadline) throws IOException {
        refillBuffer(deadline);
        return buf[bufPos++];
    }

    /**
     * Throws if the deadline has expired.
     *
     * @param deadline absolute deadline in epoch milliseconds
     * @throws IOException on timeout
     */
    public void checkDeadline(long deadline) throws IOException {
        if (System.currentTimeMillis() > deadline) {
            throw new IOException(timeoutMessage);
        }
    }
}
