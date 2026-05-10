package com.github.auties00.cobalt.wam.privatestats;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.wam.privatestats.ed25519.Ed25519HashToPoint;
import com.github.auties00.cobalt.wam.privatestats.ed25519.Ed25519Point;

import java.util.Objects;

/**
 * Client-side blinding and unblinding for the WhatsApp private-stats
 * token protocol.
 *
 * <p>The protocol is a single-use blinded-token VOPRF on the Ed25519
 * curve. The client picks a random 32-byte scalar {@code k}, hashes
 * the message {@code m} to a curve point {@code H(m)}, and sends
 * {@code blinded = H(m) + k*B} to the server, where {@code B} is the
 * Ed25519 base point. The server replies with the signature
 * {@code signed = sk * blinded = sk*H(m) + k*pk}, where {@code sk} is
 * the server private key and {@code pk = sk*B} is its public key. The
 * client recovers {@code sk*H(m)} as {@code signed - k*pk}, the
 * unblinded VOPRF output.
 *
 * <p>Mirrors {@code privateStatsToken.blindToken} and
 * {@code privateStatsToken.unblindToken} from the
 * {@code WAWamPrivateStatsToken} module, which is exposed to
 * {@code WAWebIssuePrivateStatsToken} under the alias
 * {@code privateStatsToken}.
 *
 * @apiNote The scalar {@code k} must be uniformly random and used at
 * most once per message. Reusing {@code k} across messages leaks the
 * unblinded outputs of prior messages.
 */
@WhatsAppWebModule(moduleName = "WAWamPrivateStatsToken")
@WhatsAppWebModule(moduleName = "WAWebIssuePrivateStatsToken")
public final class WamPrivateStatsTokenBlinder {
    /**
     * Length of a token, scalar, or compressed Edwards point in bytes.
     */
    public static final int TOKEN_BYTES = 32;

    /**
     * Prevents instantiation of this utility class.
     *
     * @throws AssertionError always
     */
    private WamPrivateStatsTokenBlinder() {
        throw new AssertionError("PrivateStatsTokenBlinder is a utility class and must not be instantiated");
    }

    /**
     * Computes {@code blind(message, scalar) = H(message) + scalar*B}
     * and returns the 32-byte compressed Edwards encoding.
     *
     * <p>The scalar is clamped per the X25519/Ed25519 convention
     * (clear bits 0, 1, 2, and 255; set bit 254) before the scalar
     * multiplication.
     *
     * <p>Mirrors {@code privateStatsToken.blindToken}.
     *
     * @apiNote A clone of the scalar is clamped, so the caller's
     *          {@code scalar} array is not mutated.
     * @param message the message to blind, of any length
     * @param scalar  the client blinding scalar, exactly
     *                {@value #TOKEN_BYTES} bytes
     * @return a fresh 32-byte compressed encoding of the blinded point
     * @throws NullPointerException     if either argument is
     *                                  {@code null}
     * @throws IllegalArgumentException if {@code scalar} is not exactly
     *                                  {@value #TOKEN_BYTES} bytes
     */
    @WhatsAppWebExport(
            moduleName = "WAWamPrivateStatsToken",
            exports = "blindToken",
            adaptation = WhatsAppAdaptation.DIRECT
    )
    public static byte[] blind(byte[] message, byte[] scalar) {
        Objects.requireNonNull(message, "message must not be null");
        Objects.requireNonNull(scalar, "scalar must not be null");
        if (scalar.length != TOKEN_BYTES) {
            throw new IllegalArgumentException(
                    "scalar must be " + TOKEN_BYTES + " bytes, was " + scalar.length);
        }
        var clampedScalar = scalar.clone();
        clamp(clampedScalar);

        var scalarPoint = Ed25519Point.p3();
        Ed25519Point.scalarMultBase(scalarPoint, clampedScalar);

        var hashPoint = Ed25519HashToPoint.compute(message);
        Ed25519Point.add(hashPoint, scalarPoint);

        var out = new byte[TOKEN_BYTES];
        Ed25519Point.pack(out, hashPoint);
        return out;
    }

    /**
     * Recovers the unblinded VOPRF output from a server-signed
     * blinded token by computing
     * {@code unblind(signed, scalar, pk) = signed - scalar*pk}.
     *
     * <p>Mirrors {@code privateStatsToken.unblindToken}.
     * @param signed       the server-signed blinded token, exactly
     *                     {@value #TOKEN_BYTES} bytes
     * @param scalar       the same client scalar passed to
     *                     {@link #blind}, exactly
     *                     {@value #TOKEN_BYTES} bytes
     * @param serverPubKey the server public key as a compressed
     *                     Ed25519 point, exactly
     *                     {@value #TOKEN_BYTES} bytes
     * @return a fresh 32-byte compressed encoding of the unblinded
     *         point
     * @throws NullPointerException     if any argument is
     *                                  {@code null}
     * @throws IllegalArgumentException if any argument is not exactly
     *                                  {@value #TOKEN_BYTES} bytes, or
     *                                  if {@code signed} or
     *                                  {@code serverPubKey} is not a
     *                                  valid Edwards point
     */
    @WhatsAppWebExport(
            moduleName = "WAWamPrivateStatsToken",
            exports = "unblindToken",
            adaptation = WhatsAppAdaptation.ADAPTED
    )
    public static byte[] unblind(byte[] signed, byte[] scalar, byte[] serverPubKey) {
        Objects.requireNonNull(signed, "signed must not be null");
        Objects.requireNonNull(scalar, "scalar must not be null");
        Objects.requireNonNull(serverPubKey, "serverPubKey must not be null");
        if (signed.length != TOKEN_BYTES) {
            throw new IllegalArgumentException(
                    "signed must be " + TOKEN_BYTES + " bytes, was " + signed.length);
        }
        if (scalar.length != TOKEN_BYTES) {
            throw new IllegalArgumentException(
                    "scalar must be " + TOKEN_BYTES + " bytes, was " + scalar.length);
        }
        if (serverPubKey.length != TOKEN_BYTES) {
            throw new IllegalArgumentException(
                    "serverPubKey must be " + TOKEN_BYTES + " bytes, was " + serverPubKey.length);
        }

        var blindedPoint = Ed25519Point.p3();
        if (Ed25519Point.unpack(blindedPoint, signed) != 0) {
            throw new IllegalArgumentException("signed is not a valid Edwards point");
        }
        var negatedPubKey = Ed25519Point.p3();
        if (Ed25519Point.unpackNeg(negatedPubKey, serverPubKey) != 0) {
            throw new IllegalArgumentException("serverPubKey is not a valid Edwards point");
        }

        var clampedScalar = scalar.clone();
        clamp(clampedScalar);

        var negProduct = Ed25519Point.p3();
        Ed25519Point.scalarMult(negProduct, negatedPubKey, clampedScalar);
        Ed25519Point.add(blindedPoint, negProduct);

        var out = new byte[TOKEN_BYTES];
        Ed25519Point.pack(out, blindedPoint);
        return out;
    }

    /**
     * Applies the standard X25519/Ed25519 scalar clamp in place. This
     * clears bits 0, 1, and 2 of byte 0, clears bit 7 of byte 31, and
     * sets bit 6 of byte 31.
     *
     * <p>The clamp bounds the scalar to the safe range
     * {@code [2^254, 2^255)} with low-order bits zeroed, matching the
     * private {@code u} helper inside the
     * {@code WAWamPrivateStatsToken} module (also reachable through the
     * {@code privateStatsToken} alias).
     *
     * @param scalar the scalar bytes, exactly {@value #TOKEN_BYTES}
     *               bytes
     */
    private static void clamp(byte[] scalar) {
        scalar[0] &= (byte) 0xF8;
        scalar[31] &= (byte) 0x7F;
        scalar[31] |= (byte) 0x40;
    }
}
