package com.github.auties00.cobalt.sync.crypto;

import javax.crypto.KDF;
import javax.crypto.spec.HKDFParameterSpec;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.GeneralSecurityException;
import java.util.List;

/**
 * LT-Hash (Lattice Hash) implementation for anti-tampering verification.
 *
 * <p>LT-Hash is a cryptographic accumulator with the following properties:
 * <ul>
 *   <li><b>Commutative</b>: hash(a, b) = hash(b, a)</li>
 *   <li><b>Associative</b>: hash(hash(a, b), c) = hash(a, hash(b, c))</li>
 *   <li><b>Reversible</b>: hash_remove(hash(a, b), b) = hash(a)</li>
 *   <li><b>Deterministic</b>: Same input always produces same output</li>
 * </ul>
 *
 * <p>This implementation uses HKDF-expanded values with unsigned 16-bit wrapping
 * arithmetic, matching the WhatsApp Web {@code WACryptoLtHash} algorithm. Each
 * value MAC is expanded via HKDF-SHA256 to 128 bytes, then treated as 64
 * little-endian Uint16 values for pointwise addition or subtraction with
 * wrapping overflow (mod 65536).
 *
 * @implNote WACryptoLtHash.LtHash16
 */
public final class MutationLTHash {
    /**
     * Length of the hash state in bytes (64 little-endian Uint16 values).
     * Exported as {@code KEY_LENGTH_BYTES} in WA Web.
     *
     * @implNote WACryptoLtHash.KEY_LENGTH_BYTES — constant {@code u = 128}
     */
    public static final int HASH_LENGTH = 128; // WACryptoLtHash: u = 128 (KEY_LENGTH_BYTES)

    /**
     * HKDF info string used for expanding value MACs.
     * Despite the WA Web variable name "salt", this is used as the HKDF info/context
     * parameter, not as the HKDF salt (which is null/default zeros).
     *
     * @implNote WACryptoLtHash.LT_HASH_ANTI_TAMPERING — {@code new LtHash16("WhatsApp Patch Integrity")} stored as {@code this.salt}
     */
    private static final byte[] HKDF_INFO = "WhatsApp Patch Integrity".getBytes(); // WACryptoLtHash: m = new d("WhatsApp Patch Integrity")

    /**
     * The empty/zero hash state.
     * Used as the initial state for a collection with no mutations.
     *
     * @implNote WACryptoLtHash.EMPTY_LT_HASH — constant {@code c = new ArrayBuffer(u)}
     */
    public static final byte[] EMPTY_HASH = new byte[HASH_LENGTH]; // WACryptoLtHash: c = new ArrayBuffer(u) (EMPTY_LT_HASH)

    /**
     * Prevents instantiation of this utility class.
     *
     * @implNote WACryptoLtHash.LT_HASH_ANTI_TAMPERING
     */
    private MutationLTHash() {

    }

    /**
     * Expands a value MAC to 128 bytes using HKDF-SHA256.
     *
     * <p>Performs HKDF-Extract with null salt (defaults to 32 zero bytes per RFC 5869)
     * followed by HKDF-Expand with info="WhatsApp Patch Integrity" and length=128.
     *
     * @implNote WACryptoLtHash.LtHash16.$1/$2 — calls {@code WACryptoHkdf.extractAndExpand(valueMac, this.salt, 128)}
     * @param valueMac the value MAC to expand
     * @return the expanded 128-byte value
     */
    private static byte[] expand(byte[] valueMac) {
        try {
            var kdf = KDF.getInstance("HKDF-SHA256"); // WACryptoHkdf.extractAndExpand
            var params = HKDFParameterSpec.ofExtract() // WACryptoHkdf.extractSha256(null, valueMac)
                    .addIKM(valueMac)
                    .thenExpand(HKDF_INFO, HASH_LENGTH); // WACryptoHkdf.expand(prk, this.salt, 128)
            return kdf.deriveData(params);
        } catch (GeneralSecurityException e) {
            throw new InternalError("Failed to expand value MAC via HKDF", e);
        }
    }

    /**
     * Performs pointwise unsigned 16-bit arithmetic on two hash buffers.
     *
     * <p>Treats both input buffers as arrays of 64 little-endian Uint16 values
     * and applies the given operator pointwise with unsigned 16-bit wrapping.
     *
     * @implNote WACryptoLtHash.LtHash16.performPointwiseWithOverflow
     * @param hash the current hash state buffer
     * @param expanded the HKDF-expanded value buffer
     * @param addition {@code true} for addition, {@code false} for subtraction
     * @return new hash state after the pointwise operation
     */
    private static byte[] performPointwiseWithOverflow(byte[] hash, byte[] expanded, boolean addition) {
        var hashBuf = ByteBuffer.wrap(hash).order(ByteOrder.LITTLE_ENDIAN); // WACryptoLtHash.performPointwiseWithOverflow: new DataView(t)
        var expandedBuf = ByteBuffer.wrap(expanded).order(ByteOrder.LITTLE_ENDIAN); // WACryptoLtHash.performPointwiseWithOverflow: new DataView(n)
        var result = ByteBuffer.allocate(hashBuf.capacity()).order(ByteOrder.LITTLE_ENDIAN); // WACryptoLtHash.performPointwiseWithOverflow: new DataView(new ArrayBuffer(e.byteLength))
        for (int i = 0; i < hashBuf.capacity(); i += 2) { // WACryptoLtHash.performPointwiseWithOverflow: l += s (s=2)
            var a = Short.toUnsignedInt(hashBuf.getShort()); // WACryptoLtHash.performPointwiseWithOverflow: e.getUint16(l, true)
            var b = Short.toUnsignedInt(expandedBuf.getShort()); // WACryptoLtHash.performPointwiseWithOverflow: o.getUint16(l, true)
            result.putShort((short) ((addition ? a + b : a - b) & 0xFFFF)); // WACryptoLtHash.performPointwiseWithOverflow: i.setUint16(l, r(e, t), true)
        }
        return result.array();
    }

    /**
     * Adds a single element to the current hash state.
     *
     * <p>The element (value MAC) is first expanded via HKDF-SHA256 to 128 bytes,
     * then both the current hash and expanded value are treated as 64
     * little-endian Uint16 values and pointwise added with unsigned 16-bit
     * wrapping overflow.
     *
     * @implNote WACryptoLtHash.LtHash16.$1 — single-element add via
     *           {@code performPointwiseWithOverflow(hash, expanded, (a, b) => a + b)}
     * @param currentHash the current hash state (must be {@link #HASH_LENGTH} bytes)
     * @param valueMac the value MAC to add (will be HKDF-expanded)
     * @return new hash state after addition
     */
    private static byte[] addSingle(byte[] currentHash, byte[] valueMac) {
        var expanded = expand(valueMac); // WACryptoLtHash.LtHash16.$1: extractAndExpand(valueMac, this.salt, 128)
        return performPointwiseWithOverflow(currentHash, expanded, true); // WACryptoLtHash.LtHash16.$1: performPointwiseWithOverflow(e, n, (a, b) => a + b)
    }

    /**
     * Removes a single element from the current hash state.
     *
     * <p>This is the inverse operation of {@link #addSingle(byte[], byte[])}.
     * The element (value MAC) is first expanded via HKDF-SHA256 to 128 bytes,
     * then both the current hash and expanded value are treated as 64
     * little-endian Uint16 values and pointwise subtracted with unsigned
     * 16-bit wrapping underflow.
     *
     * @implNote WACryptoLtHash.LtHash16.$2 — single-element subtract via
     *           {@code performPointwiseWithOverflow(hash, expanded, (a, b) => a - b)}
     * @param currentHash the current hash state (must be {@link #HASH_LENGTH} bytes)
     * @param valueMac the value MAC to remove (will be HKDF-expanded)
     * @return new hash state after removal
     */
    private static byte[] subtractSingle(byte[] currentHash, byte[] valueMac) {
        var expanded = expand(valueMac); // WACryptoLtHash.LtHash16.$2: extractAndExpand(valueMac, this.salt, 128)
        return performPointwiseWithOverflow(currentHash, expanded, false); // WACryptoLtHash.LtHash16.$2: performPointwiseWithOverflow(e, n, (a, b) => a - b)
    }

    /**
     * Adds multiple elements to the current hash state by reducing over the list.
     *
     * <p>Each value MAC is individually expanded and pointwise-added to the hash,
     * sequentially reducing from the initial {@code currentHash}.
     *
     * @implNote WACryptoLtHash.LtHash16.add — reduces over valueMacs calling {@code $1} for each
     * @param currentHash the current hash state (must be {@link #HASH_LENGTH} bytes)
     * @param valueMacs the list of value MACs to add
     * @return new hash state after all additions
     */
    public static byte[] add(byte[] currentHash, List<byte[]> valueMacs) {
        var result = currentHash; // WACryptoLtHash.LtHash16.add: Promise.resolve(r)
        for (var valueMac : valueMacs) { // WACryptoLtHash.LtHash16.add: o.reduce(...)
            result = addSingle(result, valueMac); // WACryptoLtHash.LtHash16.add: t.$1(yield e, n)
        }
        return result;
    }

    /**
     * Removes multiple elements from the current hash state by reducing over the list.
     *
     * <p>Each value MAC is individually expanded and pointwise-subtracted from the hash,
     * sequentially reducing from the initial {@code currentHash}.
     *
     * @implNote WACryptoLtHash.LtHash16.subtract — reduces over valueMacs calling {@code $2} for each
     * @param currentHash the current hash state (must be {@link #HASH_LENGTH} bytes)
     * @param valueMacs the list of value MACs to subtract
     * @return new hash state after all removals
     */
    public static byte[] subtract(byte[] currentHash, List<byte[]> valueMacs) {
        var result = currentHash; // WACryptoLtHash.LtHash16.subtract: Promise.resolve(r)
        for (var valueMac : valueMacs) { // WACryptoLtHash.LtHash16.subtract: o.reduce(...)
            result = subtractSingle(result, valueMac); // WACryptoLtHash.LtHash16.subtract: t.$2(yield e, n)
        }
        return result;
    }

    /**
     * Result of a {@link #subtractThenAdd} operation, containing both the final hash
     * and the intermediate subtract result.
     *
     * @implNote WACryptoLtHash.LtHash16.subtractThenAdd — returns {@code {ltHash: o, subtractResult: r}}
     * @param ltHash the final hash state after subtract and add operations
     * @param subtractResult the intermediate hash state after subtract but before add operations
     */
    public record SubtractThenAddResult(
            byte[] ltHash,
            byte[] subtractResult
    ) {

    }

    /**
     * Batch operation: removes multiple elements then adds multiple elements.
     *
     * <p>First subtracts all {@code toRemove} elements from the hash, producing an
     * intermediate {@code subtractResult}. Then adds all {@code toAdd} elements to
     * produce the final {@code ltHash}. Both results are returned.
     *
     * @implNote WACryptoLtHash.LtHash16.subtractThenAdd — calls {@code subtract(hash, toRemove)}
     *           then {@code add(subtractResult, toAdd)}, returning {@code {ltHash, subtractResult}}
     * @param currentHash the current hash state (must be {@link #HASH_LENGTH} bytes)
     * @param toAdd list of value MACs to add (may be empty)
     * @param toRemove list of value MACs to remove (may be empty)
     * @return a {@link SubtractThenAddResult} containing both the final hash and intermediate subtract result
     */
    public static SubtractThenAddResult subtractThenAdd(
        byte[] currentHash,
        List<byte[]> toAdd,
        List<byte[]> toRemove
    ) {
        var subtractResult = subtract(currentHash, toRemove); // WACryptoLtHash.LtHash16.subtractThenAdd: var r = yield this.subtract(e, n)
        var ltHash = add(subtractResult, toAdd); // WACryptoLtHash.LtHash16.subtractThenAdd: var o = yield this.add(r, t)
        return new SubtractThenAddResult(ltHash, subtractResult); // WACryptoLtHash.LtHash16.subtractThenAdd: {ltHash: o, subtractResult: r}
    }

    /**
     * Creates a copy of the given hash state.
     *
     * @implNote ADAPTED: Java defensive copy — no WA Web equivalent needed because
     *           JS ArrayBuffer has different ownership semantics
     * @param hash the hash state to copy
     * @return a new array with the same contents, or {@code null} if input is {@code null}
     */
    public static byte[] copy(byte[] hash) {
        return hash == null // ADAPTED: Java defensive copy
                ? null
                : hash.clone();
    }
}
