package com.github.auties00.cobalt.wam.privatestats.ed25519;

import java.security.MessageDigest;

/**
 * Edwards-curve point arithmetic on Ed25519, implemented over
 * {@link Ed25519Field}.
 *
 * <p>Points are held in extended twisted-Edwards coordinates as a 4-element
 * array of {@link Ed25519Field#LIMBS}-limb field elements
 * {@code {X, Y, Z, T}} satisfying {@code x = X/Z}, {@code y = Y/Z},
 * {@code x*y = T/Z}. This matches the {@code p3Element} layout of
 * {@code WACryptoEd25519} and the {@code lowlevel} routines of
 * {@code tweetnacl-js}.
 *
 * <p>The {@link #add}, {@link #scalarMult}, and {@link #scalarMultBase}
 * routines mirror {@code lowlevel.add}, {@code lowlevel.scalarmult}, and
 * {@code lowlevel.scalarbase} byte for byte. {@link #pack}, {@link #par25519},
 * {@link #unpackNeg} and {@link #neq25519} reproduce the corresponding
 * compressed-encoding helpers.
 *
 * <p>All operations are constant time with respect to point coordinates and
 * scalar bits. Branches on point or scalar data are implemented as masked
 * {@link Ed25519Field#sel25519} swaps.
 */
public final class Ed25519Point {
    /**
     * Edwards-curve constant {@code d = -121665 / 121666 (mod p)}, encoded
     * as 16 little-endian radix-{@code 2^16} limbs.
     */
    private static final long[] D = {
            0x78a3, 0x1359, 0x4dca, 0x75eb,
            0xd8ab, 0x4141, 0x0a4d, 0x0070,
            0xe898, 0x7779, 0x4079, 0x8cc7,
            0xfe73, 0x2b6f, 0x6cee, 0x5203
    };

    /**
     * The constant {@code 2 * d}, used in the extended-Edwards addition
     * formula.
     */
    private static final long[] D2 = {
            0xf159, 0x26b2, 0x9b94, 0xebd6,
            0xb156, 0x8283, 0x149a, 0x00e0,
            0xd130, 0xeef3, 0x80f2, 0x198e,
            0xfce7, 0x56df, 0xd9dc, 0x2406
    };

    /**
     * Affine x-coordinate of the Ed25519 base point {@code B}.
     */
    private static final long[] BASE_X = {
            0xd51a, 0x8f25, 0x2d60, 0xc956,
            0xa7b2, 0x9525, 0xc760, 0x692c,
            0xdc5c, 0xfdd6, 0xe231, 0xc0a4,
            0x53fe, 0xcd6e, 0x36d3, 0x2169
    };

    /**
     * Affine y-coordinate of the Ed25519 base point {@code B}, equal to
     * {@code 4/5 (mod p)}.
     */
    private static final long[] BASE_Y = {
            0x6658, 0x6666, 0x6666, 0x6666,
            0x6666, 0x6666, 0x6666, 0x6666,
            0x6666, 0x6666, 0x6666, 0x6666,
            0x6666, 0x6666, 0x6666, 0x6666
    };

    /**
     * Square root of {@code -1} in the field, used in
     * {@link #unpackNeg} for the alternate square-root branch.
     */
    private static final long[] I = {
            0xa0b0, 0x4a0e, 0x1b27, 0xc4ee,
            0xe478, 0xad2f, 0x1806, 0x2f43,
            0xd7a7, 0x3dfb, 0x0099, 0x2b4d,
            0xdf0b, 0x4fc1, 0x2480, 0x2b83
    };

    /**
     * Prevents instantiation of this utility class.
     *
     * @throws AssertionError always
     */
    private Ed25519Point() {
        throw new AssertionError("Ed25519Point is a utility class and must not be instantiated");
    }

    /**
     * Allocates a fresh extended-Edwards point: a 4-element array of
     * zero-initialised field elements.
     *
     * <p>Mirrors {@code WACryptoEd25519.p3Element}.
     *
     * @return a new {@code long[4][LIMBS]} point ready to be initialised
     */
    public static long[][] p3() {
        return new long[][]{
                Ed25519Field.gf(),
                Ed25519Field.gf(),
                Ed25519Field.gf(),
                Ed25519Field.gf()
        };
    }

    /**
     * Adds two extended-Edwards points in place: {@code p = p + q}.
     *
     * <p>Implements the unified Edwards addition formula from
     * Hisil-Wong-Carter-Dawson, mirroring {@code lowlevel.add}.
     *
     * @param p the destination point (mutated)
     * @param q the right operand (read only)
     */
    public static void add(long[][] p, long[][] q) {
        var a = Ed25519Field.gf();
        var b = Ed25519Field.gf();
        var c = Ed25519Field.gf();
        var d = Ed25519Field.gf();
        var e = Ed25519Field.gf();
        var f = Ed25519Field.gf();
        var g = Ed25519Field.gf();
        var h = Ed25519Field.gf();
        var t = Ed25519Field.gf();

        Ed25519Field.sub(a, p[1], p[0]);
        Ed25519Field.sub(t, q[1], q[0]);
        Ed25519Field.mul(a, a, t);
        Ed25519Field.add(b, p[0], p[1]);
        Ed25519Field.add(t, q[0], q[1]);
        Ed25519Field.mul(b, b, t);
        Ed25519Field.mul(c, p[3], q[3]);
        Ed25519Field.mul(c, c, D2);
        Ed25519Field.mul(d, p[2], q[2]);
        Ed25519Field.add(d, d, d);
        Ed25519Field.sub(e, b, a);
        Ed25519Field.sub(f, d, c);
        Ed25519Field.add(g, d, c);
        Ed25519Field.add(h, b, a);

        Ed25519Field.mul(p[0], e, f);
        Ed25519Field.mul(p[1], h, g);
        Ed25519Field.mul(p[2], g, f);
        Ed25519Field.mul(p[3], e, h);
    }

    /**
     * Constant-time conditional swap of two points: if {@code b == 1} swaps
     * {@code p} and {@code q} coordinate by coordinate, otherwise leaves
     * both unchanged.
     *
     * @param p the first point
     * @param q the second point
     * @param b the swap bit; must be exactly {@code 0} or {@code 1}
     */
    public static void cswap(long[][] p, long[][] q, long b) {
        for (var i = 0; i < 4; i++) {
            Ed25519Field.sel25519(p[i], q[i], b);
        }
    }

    /**
     * Returns the parity (least-significant bit of the canonical 32-byte
     * encoding) of a field element.
     *
     * <p>Mirrors {@code lowlevel.par25519}.
     *
     * @param a the field element
     * @return {@code 0} or {@code 1}
     */
    public static int par25519(long[] a) {
        var d = new byte[Ed25519Field.BYTES];
        Ed25519Field.pack25519(d, a);
        return d[0] & 1;
    }

    /**
     * Constant-time inequality test on two field elements: returns the
     * non-zero result of {@link MessageDigest#isEqual} negated to a
     * tweetnacl-style boolean.
     *
     * <p>Mirrors {@code lowlevel.neq25519} which delegates to
     * {@code crypto_verify_32}; the comparison itself is constant time
     * because {@code MessageDigest.isEqual} is documented constant time
     * since JDK 7.
     *
     * @param a the left field element
     * @param b the right field element
     * @return {@code 0} if equal, {@code 1} otherwise
     */
    public static int neq25519(long[] a, long[] b) {
        var c = new byte[Ed25519Field.BYTES];
        var d = new byte[Ed25519Field.BYTES];
        Ed25519Field.pack25519(c, a);
        Ed25519Field.pack25519(d, b);
        return MessageDigest.isEqual(c, d) ? 0 : 1;
    }

    /**
     * Encodes a point in compressed Edwards form: 32 bytes holding the
     * y-coordinate, with the sign of x packed into the high bit of the last
     * byte.
     *
     * <p>Mirrors the {@code j} helper in {@code WACryptoEd25519} and
     * {@code lowlevel.pack}.
     *
     * @param r the 32-byte destination buffer
     * @param p the point to encode
     */
    public static void pack(byte[] r, long[][] p) {
        var tx = Ed25519Field.gf();
        var ty = Ed25519Field.gf();
        var zi = Ed25519Field.gf();
        Ed25519Field.inv25519(zi, p[2]);
        Ed25519Field.mul(tx, p[0], zi);
        Ed25519Field.mul(ty, p[1], zi);
        Ed25519Field.pack25519(r, ty);
        r[31] = (byte) (r[31] ^ (par25519(tx) << 7));
    }

    /**
     * Decodes a 32-byte compressed Edwards point and stores its negation
     * into {@code r}.
     *
     * <p>The "negation on decode" convention is inherited from NaCl/SUPERCOP:
     * Ed25519 verification uses {@code -A} where it would mathematically use
     * {@code A}, so the public unpack routine pre-negates. Callers that want
     * the actual decoded point must negate the X coordinate after.
     *
     * <p>Mirrors {@code lowlevel.unpackneg}.
     *
     * @param r the destination point
     * @param p the 32-byte compressed encoding
     * @return {@code 0} on success, {@code -1} if {@code p} is not a valid
     * point on the curve
     */
    public static int unpackNeg(long[][] r, byte[] p) {
        var t = Ed25519Field.gf();
        var chk = Ed25519Field.gf();
        var num = Ed25519Field.gf();
        var den = Ed25519Field.gf();
        var den2 = Ed25519Field.gf();
        var den4 = Ed25519Field.gf();
        var den6 = Ed25519Field.gf();

        Ed25519Field.set25519(r[2], Ed25519Field.gfFromSmall(1));
        Ed25519Field.unpack25519(r[1], p);
        Ed25519Field.square(num, r[1]);
        Ed25519Field.mul(den, num, D);
        Ed25519Field.sub(num, num, r[2]);
        Ed25519Field.add(den, r[2], den);

        Ed25519Field.square(den2, den);
        Ed25519Field.square(den4, den2);
        Ed25519Field.mul(den6, den4, den2);
        Ed25519Field.mul(t, den6, num);
        Ed25519Field.mul(t, t, den);

        Ed25519Field.pow2523(t, t);
        Ed25519Field.mul(t, t, num);
        Ed25519Field.mul(t, t, den);
        Ed25519Field.mul(t, t, den);
        Ed25519Field.mul(r[0], t, den);

        Ed25519Field.square(chk, r[0]);
        Ed25519Field.mul(chk, chk, den);
        if (neq25519(chk, num) != 0) {
            Ed25519Field.mul(r[0], r[0], I);
        }

        Ed25519Field.square(chk, r[0]);
        Ed25519Field.mul(chk, chk, den);
        if (neq25519(chk, num) != 0) {
            return -1;
        }

        if (par25519(r[0]) == ((p[31] & 0xff) >> 7)) {
            var zero = Ed25519Field.gf();
            Ed25519Field.sub(r[0], zero, r[0]);
        }

        Ed25519Field.mul(r[3], r[0], r[1]);
        return 0;
    }

    /**
     * Decodes a 32-byte compressed Edwards point. Equivalent to
     * {@link #unpackNeg} followed by an in-place negation of the
     * X and T coordinates, since Edwards negation maps {@code (X, Y, Z, T)}
     * to {@code (-X, Y, Z, -T)}.
     *
     * <p>Mirrors the {@code U} helper in {@code WACryptoEd25519}, which
     * exposes the positive-unpack on top of {@code unpackneg}.
     *
     * @param r the destination point
     * @param p the 32-byte compressed encoding
     * @return {@code 0} on success, {@code -1} if {@code p} is not a valid
     * point on the curve
     */
    public static int unpack(long[][] r, byte[] p) {
        var tmp = p3();
        var status = unpackNeg(tmp, p);
        if (status != 0) {
            return -1;
        }
        var zero = Ed25519Field.gf();
        Ed25519Field.sub(r[0], zero, tmp[0]);
        Ed25519Field.set25519(r[1], tmp[1]);
        Ed25519Field.set25519(r[2], tmp[2]);
        Ed25519Field.sub(r[3], zero, tmp[3]);
        return 0;
    }

    /**
     * Variable-base scalar multiplication: {@code p = s * q} where {@code s}
     * is a 32-byte little-endian scalar.
     *
     * <p>Implemented as a Montgomery ladder over the 256 bits of {@code s},
     * using {@link #cswap} for constant-time bit handling.
     *
     * <p>Mirrors {@code lowlevel.scalarmult}.
     *
     * @param p the destination point
     * @param q the base point (mutated as scratch space; pass a copy if the
     *          caller still needs it)
     * @param s the scalar, 32 bytes little endian
     */
    public static void scalarMult(long[][] p, long[][] q, byte[] s) {
        Ed25519Field.set25519(p[0], Ed25519Field.gf());
        Ed25519Field.set25519(p[1], Ed25519Field.gfFromSmall(1));
        Ed25519Field.set25519(p[2], Ed25519Field.gfFromSmall(1));
        Ed25519Field.set25519(p[3], Ed25519Field.gf());
        for (var i = 255; i >= 0; --i) {
            var b = ((s[i >>> 3] & 0xff) >> (i & 7)) & 1;
            cswap(p, q, b);
            add(q, p);
            add(p, p);
            cswap(p, q, b);
        }
    }

    /**
     * Fixed-base scalar multiplication: {@code p = s * B} where {@code B} is
     * the Ed25519 base point and {@code s} is a 32-byte little-endian scalar.
     *
     * <p>Mirrors {@code lowlevel.scalarbase}.
     *
     * @param p the destination point
     * @param s the scalar, 32 bytes little endian
     */
    public static void scalarMultBase(long[][] p, byte[] s) {
        var q = p3();
        Ed25519Field.set25519(q[0], BASE_X);
        Ed25519Field.set25519(q[1], BASE_Y);
        Ed25519Field.set25519(q[2], Ed25519Field.gfFromSmall(1));
        Ed25519Field.mul(q[3], BASE_X, BASE_Y);
        scalarMult(p, q, s);
    }
}
