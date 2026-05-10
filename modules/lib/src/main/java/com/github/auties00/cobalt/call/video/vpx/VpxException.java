package com.github.auties00.cobalt.call.video.vpx;

import com.github.auties00.cobalt.call.video.vpx.bindings.LibVpx;

import java.lang.foreign.MemorySegment;

/**
 * Thrown when a libvpx operation fails — wraps a non-zero
 * {@code vpx_codec_err_t} return code or a Java-side invariant
 * violation (closed codec, wrong frame dimensions, etc.). Surfaces
 * libvpx's human-readable error string from
 * {@code vpx_codec_err_to_string} when a numeric code is the cause.
 */
public class VpxException extends RuntimeException {
    /**
     * Constructs a new exception with the given message.
     *
     * @param message the detail message
     */
    public VpxException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the given message and cause.
     *
     * @param message the detail message
     * @param cause   the underlying cause
     */
    public VpxException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Builds an exception whose message includes libvpx's textual
     * description of the {@code vpx_codec_err_t} code.
     *
     * @param prefix human-readable context ("encode failed")
     * @param errCode the {@code vpx_codec_err_t} return value
     * @return a new exception ready to throw
     */
    static VpxException fromErr(String prefix, int errCode) {
        var msg = errString(errCode);
        return new VpxException(prefix + ": " + msg + " (code " + errCode + ")");
    }

    /**
     * Reads libvpx's static error string for the given error code.
     *
     * @param errCode the {@code vpx_codec_err_t} value
     * @return the error string, or "unknown" if the lookup fails
     */
    private static String errString(int errCode) {
        try {
            var ptr = LibVpx.vpx_codec_err_to_string(errCode);
            if (ptr.equals(MemorySegment.NULL)) {
                return "unknown";
            }
            return ptr.reinterpret(Long.MAX_VALUE).getString(0);
        } catch (Throwable t) {
            return "unknown";
        }
    }
}
