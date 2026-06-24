package com.github.auties00.cobalt.calls2.net.transport;

/**
 * Provides the shared big-endian byte-buffer primitives the WARP and STUN codecs in this package use
 * to read and write multi-byte integer fields.
 *
 * <p>WhatsApp's call control protocols (WARP media control, STUN binding) place their multi-byte
 * integer fields in network (big-endian) byte order. This holder centralizes the unsigned
 * {@code u16}/{@code u24}/{@code u32} read and write helpers so each codec does not repeat the shift
 * arithmetic. The methods are package-private; they are an internal detail of the transport codecs and
 * carry no external contract.
 *
 * @implNote This implementation is a plain big-endian helper with no WA counterpart of its own; the
 *           byte order it encodes is the network order the wa-voip serializers
 *           ({@code sfu/wa_warp_msg.cc}, {@code transport/wa_stun_msg.cc}) use for every length and
 *           integer attribute.
 */
final class WarpCodecSupport {
    /**
     * Prevents instantiation of this stateless helper holder.
     */
    private WarpCodecSupport() {
        throw new AssertionError("WarpCodecSupport is not instantiable");
    }

    /**
     * Writes an unsigned sixteen-bit value big-endian into a buffer.
     *
     * @param out    the destination buffer
     * @param offset the index of the high byte
     * @param value  the value to write, masked to sixteen bits
     * @return the index immediately after the two written bytes
     */
    static int putU16(byte[] out, int offset, int value) {
        out[offset] = (byte) (value >>> 8);
        out[offset + 1] = (byte) value;
        return offset + 2;
    }

    /**
     * Writes an unsigned thirty-two-bit value big-endian into a buffer.
     *
     * @param out    the destination buffer
     * @param offset the index of the most-significant byte
     * @param value  the value to write
     * @return the index immediately after the four written bytes
     */
    static int putU32(byte[] out, int offset, int value) {
        out[offset] = (byte) (value >>> 24);
        out[offset + 1] = (byte) (value >>> 16);
        out[offset + 2] = (byte) (value >>> 8);
        out[offset + 3] = (byte) value;
        return offset + 4;
    }

    /**
     * Reads an unsigned sixteen-bit big-endian value from a buffer.
     *
     * @param in     the source buffer
     * @param offset the index of the high byte
     * @return the unsigned value in {@code 0..65535}
     */
    static int readU16(byte[] in, int offset) {
        return ((in[offset] & 0xff) << 8) | (in[offset + 1] & 0xff);
    }

    /**
     * Reads an unsigned thirty-two-bit big-endian value from a buffer into a {@code long}.
     *
     * @param in     the source buffer
     * @param offset the index of the most-significant byte
     * @return the unsigned value in {@code 0..4294967295}
     */
    static long readU32(byte[] in, int offset) {
        return ((long) (in[offset] & 0xff) << 24)
                | ((in[offset + 1] & 0xff) << 16)
                | ((in[offset + 2] & 0xff) << 8)
                | (in[offset + 3] & 0xff);
    }
}
