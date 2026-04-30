package com.github.auties00.cobalt.wam.privatestats.ed25519;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Hashes an arbitrary byte string to a point on the Ed25519
 * prime-order subgroup, using the
 * {@code WACryptoEd25519.hashToPoint} composition: a SHA-512 of the
 * message, an Elligator-2 map onto the Curve25519 Montgomery form, a
 * birational lift to the twisted-Edwards form, and a cofactor
 * multiplication by {@code 8} to land in the prime-order subgroup.
 *
 * <p>This is <em>not</em> a standard hash-to-curve construction. It
 * predates RFC 9380 and is WhatsApp's own composition, used solely
 * by {@code privateStatsToken.blindToken}. The validation strategy
 * for this class therefore relies on KAT vectors captured from the
 * live WhatsApp Web JavaScript bundle rather than an independent
 * specification.
 *
 * @implNote Mirrors {@code WACryptoEd25519.hashToPoint} together
 *     with its private helpers {@code E}, {@code k}, {@code D},
 *     {@code x}, {@code $}, {@code W}, {@code V}, {@code A},
 *     {@code F}, {@code O}, {@code M}, {@code P}, {@code N}, and
 *     {@code B}.
 */
@WhatsAppWebModule(moduleName = "WACryptoEd25519")
public final class Ed25519HashToPoint {
    /**
     * Canonical 32-byte little-endian encoding of the field element
     * {@code sqrt(-1) mod p}, used as the alternate square-root branch in
     * {@link #sqrt}.
     *
     * <p>Mirrors the {@code T} byte array in {@code WACryptoEd25519}.
     */
    private static final byte[] SQRT_M1_BYTES = {
            (byte) 176, (byte) 160, (byte) 14, (byte) 74, (byte) 39, (byte) 27, (byte) 238, (byte) 196,
            (byte) 120, (byte) 228, (byte) 47, (byte) 173, (byte) 6, (byte) 24, (byte) 67, (byte) 47,
            (byte) 167, (byte) 215, (byte) 251, (byte) 61, (byte) 153, (byte) 0, (byte) 77, (byte) 43,
            (byte) 11, (byte) 223, (byte) 193, (byte) 79, (byte) 128, (byte) 36, (byte) 131, (byte) 43
    };

    /**
     * Canonical 32-byte little-endian encoding of the field element
     * {@code sqrt(-486664) mod p}, used in {@link #liftMontToP3} as the
     * scaling factor of the Curve25519 → Ed25519 birational map.
     *
     * <p>Mirrors the {@code w} byte array in {@code WACryptoEd25519}.
     */
    private static final byte[] SQRT_NEG_A_PLUS_2_BYTES = {
            (byte) 6, (byte) 126, (byte) 69, (byte) 255, (byte) 170, (byte) 4, (byte) 110, (byte) 204,
            (byte) 130, (byte) 26, (byte) 125, (byte) 75, (byte) 209, (byte) 211, (byte) 161, (byte) 197,
            (byte) 126, (byte) 79, (byte) 252, (byte) 3, (byte) 220, (byte) 8, (byte) 123, (byte) 210,
            (byte) 187, (byte) 6, (byte) 160, (byte) 96, (byte) 244, (byte) 237, (byte) 38, (byte) 15
    };

    /**
     * The Curve25519 Montgomery curve parameter {@code A = 486662}, encoded
     * as 16 radix-{@code 2^16} limbs.
     */
    private static final long[] A_MONT = {
            0x6D06L, 0x7L, 0L, 0L,
            0L, 0L, 0L, 0L,
            0L, 0L, 0L, 0L,
            0L, 0L, 0L, 0L
    };

    /**
     * The constant {@code 2}, used in {@link #squareTimesTwo}.
     */
    private static final long[] TWO = {
            2L, 0L, 0L, 0L,
            0L, 0L, 0L, 0L,
            0L, 0L, 0L, 0L,
            0L, 0L, 0L, 0L
    };

    /**
     * Prevents instantiation of this utility class.
     *
     * @throws AssertionError always
     */
    private Ed25519HashToPoint() {
        throw new AssertionError("Ed25519HashToPoint is a utility class and must not be instantiated");
    }

    /**
     * Hashes a byte string to a point on the Ed25519 prime-order subgroup.
     *
     * <p>The output point is in extended-Edwards form, in the prime-order
     * subgroup (i.e. the cofactor-{@code 8} component has been mapped out).
     *
     * <p>Mirrors {@code WACryptoEd25519.hashToPoint} (the {@code H} helper).
     *
     * @param message the message to hash; any length
     * @return a freshly allocated extended-Edwards point
     */
    public static long[][] compute(byte[] message) {
        var digest = sha512(message);
        var sign = (digest[31] & 0x80) >>> 7;
        digest[31] &= 0x7f;

        var u = Ed25519Field.gf();
        Ed25519Field.unpack25519(u, digest);

        var mont = Ed25519Field.gf();
        elligator(mont, u);

        var lifted = Ed25519Point.p3();
        liftMontToP3(lifted, mont, sign);

        var result = Ed25519Point.p3();
        cofactorMul8(result, lifted);
        return result;
    }

    /**
     * Computes the SHA-512 digest of the input message.
     *
     * <p>Replaces {@code WACryptoPrimitives.lowlevel.crypto_hash} with the
     * JDK's intrinsified implementation. The two are byte-identical for any
     * input.
     *
     * @param message the message
     * @return a fresh 64-byte digest
     */
    private static byte[] sha512(byte[] message) {
        try {
            var md = MessageDigest.getInstance("SHA-512");
            return md.digest(message);
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError("SHA-512 must be available on every JVM", e);
        }
    }

    /**
     * Computes {@code o = 2 * t^2}.
     *
     * <p>Mirrors the {@code E} helper.
     *
     * @param o the destination
     * @param t the operand
     */
    private static void squareTimesTwo(long[] o, long[] t) {
        var sq = Ed25519Field.gf();
        Ed25519Field.square(sq, t);
        Ed25519Field.mul(o, TWO, sq);
    }

    /**
     * Evaluates the Curve25519 Montgomery polynomial at {@code t}:
     * {@code o = t^3 + A*t^2 + t} where {@code A = 486662}.
     *
     * <p>This is the right-hand side of the Curve25519 equation
     * {@code v^2 = u^3 + A*u^2 + u}, used in {@link #elligator} to test
     * whether a candidate {@code u} lies on the curve, and in
     * {@link #liftMontToP3} to recover the {@code v} coordinate.
     *
     * <p>Mirrors the {@code k} helper.
     *
     * @param o the destination
     * @param t the {@code u} coordinate at which to evaluate
     */
    private static void curve25519Polynomial(long[] o, long[] t) {
        var sq = Ed25519Field.gf();
        Ed25519Field.square(sq, t);
        var atimes = Ed25519Field.gf();
        Ed25519Field.mul(atimes, A_MONT, t);
        var sum = Ed25519Field.gf();
        Ed25519Field.add(sum, sq, atimes);
        var sumPlusOne = Ed25519Field.gf();
        Ed25519Field.add(sumPlusOne, sum, Ed25519Field.gfFromSmall(1));
        Ed25519Field.mul(o, t, sumPlusOne);
    }

    /**
     * Computes the canonical square root: {@code o = sqrt(t)} chosen so
     * that {@code o^2 = t}.
     *
     * <p>For {@code p ≡ 5 (mod 8)} the root is computed as
     * {@code c = t^((p+3)/8)}; if {@code c^2 = t} the result is {@code c},
     * otherwise it is {@code c * sqrt(-1)}. The branch on {@code c^2 == t}
     * is constant-time via {@link Ed25519Point#neq25519} and {@link
     * Ed25519Field#sel25519}.
     *
     * <p>Mirrors the {@code D} helper.
     *
     * @param o the destination
     * @param t the operand (assumed to be a quadratic residue)
     */
    private static void sqrt(long[] o, long[] t) {
        var sqrtMinusOne = Ed25519Field.gf();
        Ed25519Field.unpack25519(sqrtMinusOne, SQRT_M1_BYTES);
        var u = Ed25519Field.gf();
        Ed25519Field.pow2523(u, t);
        var c = Ed25519Field.gf();
        Ed25519Field.mul(c, t, u);
        var d = Ed25519Field.gf();
        Ed25519Field.square(d, c);
        var alt = Ed25519Field.gf();
        Ed25519Field.mul(alt, c, sqrtMinusOne);
        // If c^2 != t, swap c with alt so c becomes the alternate root.
        Ed25519Field.sel25519(c, alt, Ed25519Point.neq25519(d, t));
        Ed25519Field.set25519(o, c);
    }

    /**
     * Computes the Edwards y-coordinate from a Montgomery u-coordinate:
     * {@code o = (t - 1) / (t + 1)}.
     *
     * <p>Mirrors the {@code x} helper.
     *
     * @param o the destination Edwards y
     * @param t the Montgomery u
     */
    private static void montUToEdwardsY(long[] o, long[] t) {
        var minus = Ed25519Field.gf();
        Ed25519Field.sub(minus, t, Ed25519Field.gfFromSmall(1));
        var plus = Ed25519Field.gf();
        Ed25519Field.add(plus, t, Ed25519Field.gfFromSmall(1));
        var inv = Ed25519Field.gf();
        Ed25519Field.inv25519(inv, plus);
        Ed25519Field.mul(o, minus, inv);
    }

    /**
     * Negates a field element: {@code o = -t mod p}.
     *
     * <p>Mirrors the {@code $} helper.
     *
     * @param o the destination
     * @param t the operand
     */
    private static void negate(long[] o, long[] t) {
        Ed25519Field.sub(o, Ed25519Field.gf(), t);
    }

    /**
     * Returns {@code 1} if {@code e} is a non-zero quadratic non-residue
     * modulo {@code p}, {@code 0} otherwise.
     *
     * <p>Computes the Legendre symbol bit by raising {@code e} to
     * {@code (p-1)/2} and reading bit 7 of the canonical encoding's last
     * byte (which is set for {@code -1} and clear for {@code 1}).
     *
     * <p>Mirrors the {@code W} helper.
     *
     * @param e the operand
     * @return {@code 0} if QR (or zero), {@code 1} if NQR
     */
    private static long isNonResidue(long[] e) {
        var l = Ed25519Field.gf();
        Ed25519Field.pow2523(l, e);
        var s = Ed25519Field.gf();
        Ed25519Field.square(s, l);
        var u = Ed25519Field.gf();
        Ed25519Field.square(u, s);
        var c = Ed25519Field.gf();
        Ed25519Field.mul(c, u, e);
        var d = Ed25519Field.gf();
        Ed25519Field.mul(d, c, e);
        var packed = new byte[Ed25519Field.BYTES];
        Ed25519Field.pack25519(packed, d);
        return packed[31] & 1L;
    }

    /**
     * Conditionally copies {@code src} into {@code dst} when
     * {@code bit == 1}; leaves {@code dst} unchanged when {@code bit == 0}.
     *
     * <p>This branch is on a <em>public</em> indicator (the Legendre symbol
     * bit, the SHA-512 sign bit, or a parity bit derived from public inputs);
     * a real {@code if} is acceptable. This matches the behaviour of the
     * {@code I} helper in {@code WACryptoEd25519}, which uses
     * {@code n === 1 && e.set(t)}.
     *
     * @param dst the possibly-overwritten destination
     * @param src the source
     * @param bit must be exactly {@code 0} or {@code 1}
     */
    private static void setIfBitOne(long[] dst, long[] src, long bit) {
        if (bit == 1) {
            Ed25519Field.set25519(dst, src);
        }
    }

    /**
     * Applies the Elligator-2 map: takes a 256-bit field element {@code t}
     * (with bit 255 cleared) and produces a Curve25519 Montgomery
     * u-coordinate {@code o} on the curve.
     *
     * <p>Computes {@code u = -A / (1 + 2 t^2)}; if {@code u^3 + A u^2 + u}
     * is a quadratic residue this is the output, otherwise the output is
     * {@code -u - A}, the second pre-image of the same quadratic-residue
     * branch.
     *
     * <p>Mirrors the {@code V} helper.
     *
     * @param o the destination Montgomery u
     * @param t the input field element
     */
    private static void elligator(long[] o, long[] t) {
        var twoTSq = Ed25519Field.gf();
        squareTimesTwo(twoTSq, t);
        var denom = Ed25519Field.gf();
        Ed25519Field.add(denom, twoTSq, Ed25519Field.gfFromSmall(1));
        var denomInv = Ed25519Field.gf();
        Ed25519Field.inv25519(denomInv, denom);
        var aOverDenom = Ed25519Field.gf();
        Ed25519Field.mul(aOverDenom, denomInv, A_MONT);
        var candidate = Ed25519Field.gf();
        negate(candidate, aOverDenom);

        var rhs = Ed25519Field.gf();
        curve25519Polynomial(rhs, candidate);
        var nonResidue = isNonResidue(rhs);

        var aIfNqr = Ed25519Field.gf();
        setIfBitOne(aIfNqr, A_MONT, nonResidue);
        var combined = Ed25519Field.gf();
        Ed25519Field.add(combined, candidate, aIfNqr);
        var negated = Ed25519Field.gf();
        negate(negated, combined);
        setIfBitOne(combined, negated, nonResidue);
        Ed25519Field.set25519(o, combined);
    }

    /**
     * Lifts a Curve25519 Montgomery u-coordinate to an Ed25519
     * extended-Edwards point with the requested sign.
     *
     * <p>Computes {@code y = (u-1)/(u+1)} (the standard birational map),
     * recovers the corresponding {@code v} via the Curve25519 polynomial
     * and a square root, then composes the Ed25519 x-coordinate as
     * {@code u * sqrt(-486664) / v} and conditionally negates it so that
     * its parity matches the requested sign bit.
     *
     * <p>Mirrors the {@code A} helper.
     *
     * @param p    the destination point
     * @param u    the Montgomery u
     * @param sign the sign bit (0 or 1) for the recovered Edwards x
     */
    private static void liftMontToP3(long[][] p, long[] u, long sign) {
        var sqrtNegAPlusTwo = Ed25519Field.gf();
        Ed25519Field.unpack25519(sqrtNegAPlusTwo, SQRT_NEG_A_PLUS_2_BYTES);

        var edwardsY = Ed25519Field.gf();
        montUToEdwardsY(edwardsY, u);

        var rhs = Ed25519Field.gf();
        curve25519Polynomial(rhs, u);
        var v = Ed25519Field.gf();
        sqrt(v, rhs);

        var uTimesK = Ed25519Field.gf();
        Ed25519Field.mul(uTimesK, u, sqrtNegAPlusTwo);
        var vInv = Ed25519Field.gf();
        Ed25519Field.inv25519(vInv, v);
        var edwardsX = Ed25519Field.gf();
        Ed25519Field.mul(edwardsX, uTimesK, vInv);

        var negated = Ed25519Field.gf();
        negate(negated, edwardsX);
        var parityMismatch = Ed25519Point.par25519(edwardsX) ^ sign;
        setIfBitOne(edwardsX, negated, parityMismatch);

        Ed25519Field.set25519(p[0], edwardsX);
        Ed25519Field.set25519(p[1], edwardsY);
        Ed25519Field.set25519(p[2], Ed25519Field.gfFromSmall(1));
        Ed25519Field.mul(p[3], p[0], p[1]);
    }

    /**
     * Multiplies a point by the Ed25519 cofactor {@code 8} via three
     * doublings. Used to land the output of {@link #liftMontToP3} in the
     * prime-order subgroup.
     *
     * <p>Each doubling is performed with the Hisil-Wong-Carter-Dawson
     * formulas; the intermediate steps interconvert between projective
     * (3-element) and extended (4-element) coordinates to avoid recomputing
     * the {@code T} coordinate when only the next double is needed.
     *
     * <p>Mirrors the {@code B} helper.
     *
     * @param r the destination point
     * @param p the input point
     */
    private static void cofactorMul8(long[][] r, long[][] p) {
        var p3Tmp = Ed25519Point.p3();
        var p2Tmp = new long[][]{Ed25519Field.gf(), Ed25519Field.gf(), Ed25519Field.gf()};
        doubleP3ToP3(p3Tmp, p);
        projectFromP3(p2Tmp, p3Tmp);
        applyDoublingFormula(p3Tmp, p2Tmp);
        projectFromP3(p2Tmp, p3Tmp);
        applyDoublingFormula(p3Tmp, p2Tmp);
        finalizeFromP3(r, p3Tmp);
    }

    /**
     * Computes {@code dst = 2 * src} where both are extended-Edwards points
     * (4-element form). Internally drops to the projective (3-element) form
     * to invoke the doubling transform.
     *
     * <p>Mirrors the {@code O} helper.
     *
     * @param dst the destination 4-element point
     * @param src the input 4-element point
     */
    private static void doubleP3ToP3(long[][] dst, long[][] src) {
        var threeTuple = new long[][]{Ed25519Field.gf(), Ed25519Field.gf(), Ed25519Field.gf()};
        Ed25519Field.set25519(threeTuple[0], src[0]);
        Ed25519Field.set25519(threeTuple[1], src[1]);
        Ed25519Field.set25519(threeTuple[2], src[2]);
        applyDoublingFormula(dst, threeTuple);
    }

    /**
     * Projects an extended-Edwards 4-element point into a 3-element tuple
     * {@code (X*T, Y*Z, Z*T)}, reusable as the input to
     * {@link #applyDoublingFormula}.
     *
     * <p>Mirrors the {@code P} helper.
     *
     * @param dst the 3-element destination
     * @param src the 4-element input
     */
    private static void projectFromP3(long[][] dst, long[][] src) {
        Ed25519Field.mul(dst[0], src[0], src[3]);
        Ed25519Field.mul(dst[1], src[1], src[2]);
        Ed25519Field.mul(dst[2], src[2], src[3]);
    }

    /**
     * Finalises the cofactor-mul ladder by emitting the full 4-element
     * extended-Edwards form, recovering the {@code T} coordinate as
     * {@code X*Y}.
     *
     * <p>Mirrors the {@code N} helper.
     *
     * @param dst the 4-element destination
     * @param src the 4-element input from the last doubling
     */
    private static void finalizeFromP3(long[][] dst, long[][] src) {
        Ed25519Field.mul(dst[0], src[0], src[3]);
        Ed25519Field.mul(dst[1], src[1], src[2]);
        Ed25519Field.mul(dst[2], src[2], src[3]);
        Ed25519Field.mul(dst[3], src[0], src[1]);
    }

    /**
     * Applies the Hisil-Wong-Carter-Dawson doubling formula on a 3-element
     * input tuple, producing a 4-element extended-Edwards output.
     *
     * <p>Mirrors the local {@code M} helper inside {@code WACryptoEd25519}
     * (not to be confused with {@code lowlevel.M}, the field
     * multiplication).
     *
     * @param dst the 4-element destination
     * @param src the 3-element input
     */
    private static void applyDoublingFormula(long[][] dst, long[][] src) {
        Ed25519Field.square(dst[0], src[0]);
        Ed25519Field.square(dst[2], src[1]);
        squareTimesTwo(dst[3], src[2]);
        Ed25519Field.add(dst[1], src[0], src[1]);
        var tmp = Ed25519Field.gf();
        Ed25519Field.square(tmp, dst[1]);
        Ed25519Field.add(dst[1], dst[2], dst[0]);
        Ed25519Field.sub(dst[2], dst[2], dst[0]);
        Ed25519Field.sub(dst[0], tmp, dst[1]);
        Ed25519Field.sub(dst[3], dst[3], dst[2]);
    }
}
