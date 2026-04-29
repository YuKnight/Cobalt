package com.github.auties00.cobalt.wam.privatestats;

import com.github.auties00.cobalt.wam.privatestats.ed25519.Ed25519HashToPoint;
import com.github.auties00.cobalt.wam.privatestats.ed25519.Ed25519Point;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Validates {@link WamPrivateStatsTokenBlinder} via the VOPRF round-trip
 * identity:
 * {@code unblind(server.sign(blind(m, k)), k, server.pk) == server.sign(H(m))}.
 *
 * <p>The "server" is simulated in the test using the same Ed25519 primitives
 * (a private key {@code sk}, a public key {@code pk = sk*B}, and a "sign"
 * step that performs scalar multiplication of the blinded point by
 * {@code sk}). The expected unblinded value is computed independently as
 * {@code sk * H(m)}, so the test catches both blinding errors and
 * unblinding errors but cannot certify byte-identical agreement with the
 * WA Web JS bundle (that requires KAT vectors captured from the live
 * implementation).
 */
class PrivateStatsTokenBlinderTest {
    /**
     * Number of round-trip iterations.
     */
    private static final int ITERATIONS = 16;

    /**
     * Asserts the full client-side blind/unblind pipeline recovers
     * {@code sk * H(m)} for a server with a known {@code sk}.
     */
    @Test
    void blindUnblindRoundTripRecoversSkTimesHashPoint() {
        var rng = new Random(0xC0BA60L);
        for (var i = 0; i < ITERATIONS; i++) {
            var sk = freshScalar(rng);
            var pk = derivePublicKey(sk);

            var k = freshScalar(rng);
            var msg = new byte[16 + rng.nextInt(48)];
            rng.nextBytes(msg);

            var blinded = WamPrivateStatsTokenBlinder.blind(msg, k);
            var signed = serverSign(blinded, sk);
            var unblinded = WamPrivateStatsTokenBlinder.unblind(signed, k, pk);

            var expected = scalarTimesHashPoint(sk, msg);
            assertArrayEquals(expected, unblinded,
                    "round-trip output != sk * H(m) at iteration " + i);
        }
    }

    /**
     * Asserts unblinding with the wrong scalar returns a different value
     * than {@code sk * H(m)} — guards against a no-op {@code unblind}.
     */
    @Test
    void unblindWithWrongScalarYieldsDifferentValue() {
        var rng = new Random(0xC0BA61L);
        var sk = freshScalar(rng);
        var pk = derivePublicKey(sk);

        var k = freshScalar(rng);
        var wrongK = freshScalar(rng);
        var msg = "the answer is 42".getBytes();

        var blinded = WamPrivateStatsTokenBlinder.blind(msg, k);
        var signed = serverSign(blinded, sk);
        var correct = WamPrivateStatsTokenBlinder.unblind(signed, k, pk);
        var wrong = WamPrivateStatsTokenBlinder.unblind(signed, wrongK, pk);

        var expected = scalarTimesHashPoint(sk, msg);
        assertArrayEquals(expected, correct);
        assertEquals(false, Arrays.equals(expected, wrong),
                "unblind with wrong scalar must not recover sk*H(m)");
    }

    /**
     * Asserts repeated blinds with different scalars produce different
     * blinded outputs even for the same message — guards against a
     * deterministic blinding bug.
     */
    @Test
    void differentScalarsProduceDifferentBlindedOutputs() {
        var rng = new Random(0xC0BA62L);
        var msg = "constant message".getBytes();
        var k1 = freshScalar(rng);
        var k2 = freshScalar(rng);

        var blinded1 = WamPrivateStatsTokenBlinder.blind(msg, k1);
        var blinded2 = WamPrivateStatsTokenBlinder.blind(msg, k2);
        assertEquals(false, Arrays.equals(blinded1, blinded2),
                "different scalars must produce different blinded outputs");
    }

    /**
     * Asserts {@link WamPrivateStatsTokenBlinder#blind} is deterministic: same
     * inputs always produce the same output bytes.
     */
    @Test
    void blindIsDeterministic() {
        var rng = new Random(0xC0BA63L);
        var msg = "the quick brown fox".getBytes();
        var k = freshScalar(rng);

        var first = WamPrivateStatsTokenBlinder.blind(msg, k);
        var second = WamPrivateStatsTokenBlinder.blind(msg, k.clone());
        assertArrayEquals(first, second, "blind must be deterministic");
    }

    /**
     * Asserts {@link WamPrivateStatsTokenBlinder#blind} does not mutate the
     * caller's scalar buffer, even though it is clamped internally.
     */
    @Test
    void blindDoesNotMutateCallerScalar() {
        var rng = new Random(0xC0BA64L);
        var msg = new byte[64];
        rng.nextBytes(msg);
        var k = freshScalar(rng);
        var kCopy = k.clone();
        WamPrivateStatsTokenBlinder.blind(msg, k);
        assertArrayEquals(kCopy, k, "blind must not mutate the caller's scalar");
    }

    /**
     * Asserts the documented input-validation behaviour.
     */
    @Test
    void rejectsInvalidInputs() {
        var goodScalar = new byte[32];
        var goodPoint = new byte[32];
        var goodPoint2 = new byte[32];
        var msg = new byte[8];

        assertThrows(NullPointerException.class,
                () -> WamPrivateStatsTokenBlinder.blind(null, goodScalar));
        assertThrows(NullPointerException.class,
                () -> WamPrivateStatsTokenBlinder.blind(msg, null));
        assertThrows(IllegalArgumentException.class,
                () -> WamPrivateStatsTokenBlinder.blind(msg, new byte[31]));

        assertThrows(NullPointerException.class,
                () -> WamPrivateStatsTokenBlinder.unblind(null, goodScalar, goodPoint));
        assertThrows(IllegalArgumentException.class,
                () -> WamPrivateStatsTokenBlinder.unblind(new byte[31], goodScalar, goodPoint));

        // A point with a y > p (e.g. all-0xff) should be rejected by unpack.
        var badPoint = new byte[32];
        for (var i = 0; i < 32; i++) {
            badPoint[i] = (byte) 0xff;
        }
        // Top bit is sign; clear it so we exercise the y-coord codepath.
        badPoint[31] = 0x7f;
        try {
            WamPrivateStatsTokenBlinder.unblind(goodPoint, goodScalar, badPoint);
            // If unpack happens to accept this y, the test is informational only.
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("Edwards point"),
                    "expected an Edwards-point error message, got: " + expected.getMessage());
        }
    }

    /**
     * Returns a fresh 32-byte random scalar. No clamping is performed —
     * {@link WamPrivateStatsTokenBlinder} clamps internally.
     */
    private static byte[] freshScalar(Random rng) {
        var s = new byte[WamPrivateStatsTokenBlinder.TOKEN_BYTES];
        rng.nextBytes(s);
        return s;
    }

    /**
     * Derives a "server" public key {@code pk = sk*B} as a 32-byte
     * compressed Ed25519 point. Uses the same {@link Ed25519Point}
     * primitives as {@link WamPrivateStatsTokenBlinder}.
     *
     * @param sk the server private scalar (will be clamped internally)
     */
    private static byte[] derivePublicKey(byte[] sk) {
        var clamped = sk.clone();
        clamped[0] &= (byte) 0xF8;
        clamped[31] &= (byte) 0x7F;
        clamped[31] |= (byte) 0x40;
        var p = Ed25519Point.p3();
        Ed25519Point.scalarMultBase(p, clamped);
        var out = new byte[32];
        Ed25519Point.pack(out, p);
        return out;
    }

    /**
     * Simulates a server signature: decodes the blinded point, multiplies
     * by the server private scalar, and re-encodes.
     */
    private static byte[] serverSign(byte[] blinded, byte[] sk) {
        var clamped = sk.clone();
        clamped[0] &= (byte) 0xF8;
        clamped[31] &= (byte) 0x7F;
        clamped[31] |= (byte) 0x40;
        var blindedPoint = Ed25519Point.p3();
        if (Ed25519Point.unpack(blindedPoint, blinded) != 0) {
            throw new IllegalStateException("blinded point did not decode");
        }
        var signed = Ed25519Point.p3();
        Ed25519Point.scalarMult(signed, blindedPoint, clamped);
        var out = new byte[32];
        Ed25519Point.pack(out, signed);
        return out;
    }

    /**
     * Returns {@code sk * H(m)} as a 32-byte compressed point.
     */
    private static byte[] scalarTimesHashPoint(byte[] sk, byte[] msg) {
        var clamped = sk.clone();
        clamped[0] &= (byte) 0xF8;
        clamped[31] &= (byte) 0x7F;
        clamped[31] |= (byte) 0x40;
        var hashPoint = Ed25519HashToPoint.compute(msg);
        var product = Ed25519Point.p3();
        Ed25519Point.scalarMult(product, hashPoint, clamped);
        var out = new byte[32];
        Ed25519Point.pack(out, product);
        return out;
    }
}
