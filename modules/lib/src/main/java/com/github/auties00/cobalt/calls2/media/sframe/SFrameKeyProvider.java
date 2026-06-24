package com.github.auties00.cobalt.calls2.media.sframe;

import com.github.auties00.cobalt.calls2.platform.VoipCryptoNative;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Holds the SFrame chain key for one stream and resolves the per-key-id {@link SFrameCipher} from it,
 * caching ciphers by key id and evicting stale ones by counter distance.
 *
 * <p>A call participant device installs a 32-byte chain key (the
 * {@linkplain SFrameKeyDerivation#deriveBaseKey(byte[], String) derived base key}) into its provider,
 * keyed by the signaling transaction id that delivered it. Installing a chain key for a transaction
 * id already present is a no-op, so a duplicated rekey delivery is idempotent. When a frame names a
 * key id, the provider returns the cached cipher for that key id, or derives a fresh one from the
 * current chain key on the first reference. A rekey installs a new chain key under a new key id; old
 * ciphers stay cached and serve late-arriving frames until their counter falls too far behind the
 * newest accepted counter, at which point they are evicted.
 *
 * <p>The chain key is 32 bytes but the cipher needs a 16-byte AES key, a 32-byte HMAC key, and a
 * 12-byte salt (the salt drives the cipher's counter mask). The native libsframe key store resolves the
 * key id through its vtable slot {@code +0x1c} into that {@code 0x1c}-byte key-params buffer plus the
 * HMAC key; the exact derivation is a native callback not present in this WASM and is not yet recovered.
 * Cobalt therefore expands the chain key into the AES and HMAC keys with a single HKDF-SHA256-Expand (a
 * self-consistent placeholder for the unrecovered derivation) and installs an all-zero salt, so the
 * cipher's counter mask is all-zero until the real per-key-id derivation and its salt are recovered (see
 * the {@code TODO} on this class). An instance is single-writer, driven by the encode or decode path of
 * one stream; it holds no internal lock.
 *
 * @implNote This implementation reproduces {@code sframe_keyprovider_create} (fn10812, ratchet/window
 * depth 30000, mode 3, key length {@code 0x20}), {@code sframe_keyprovider_set_chainkey} (fn10815,
 * {@code keyLen 0x20}, key-id/counter width 8, return code {@code 0xd} when the transaction's key
 * already exists), and the key-id cipher cache {@code sframe_keyprovider_get_or_derive_cipher}
 * (fn6948, evicting entries whose counter distance to the newest exceeds {@code 0x6fc23ac01}) of the
 * wa-voip WASM module {@code ff-tScznZ8P}. The cipher suite is read from the keyprovider (fn6949,
 * suite index in {@code 1..4}); Cobalt defaults to {@link SFrameCipherSuite#defaultSuite()} (suite 3,
 * AES-128-CTR + 4-byte tag), the suite the live call media path negotiates. {@code set_chainkey}
 * forwards through the key-store vtable {@code +0x10} (fn10753 -> fn10755 in
 * {@code tree/_unattributed/unattributed.c}), which validates the key, stores it, and returns
 * {@code 0xd} when the key already exists; {@code get_or_derive_cipher} (fn6948) resolves the key id
 * through the key-store vtable {@code +0x1c} into a key-params buffer, which fn6878 then reads as a
 * {@code 0x1c}-byte AES-key-plus-12-byte-salt block and a {@code 0x20}-byte HMAC key. The
 * chain-key-to-cipher expansion modelled here is a placeholder delegated to
 * {@link VoipCryptoNative#hkdfExpand(byte[], byte[], int)} (JCA {@code KDF("HKDF-SHA256")}); it is NOT
 * the recovered native derivation. The salt the native key params carry is now plumbed structurally
 * into {@link SFrameCipher} (the cipher XORs {@link SFrameCipher#counterMaskFromSalt(byte[])} into each
 * counter block), but its VALUE is unrecovered, so this provider installs the all-zero salt
 * ({@link SFrameCipher#zeroCounterMask()}) until the derivation is reversed.
 * <p>
 * TODO: recover the exact per-key-id key derivation the native key store performs at vtable slot
 * {@code 0x1c} (the mode-3 libsframe ratchet, fn10765 in {@code tree/_unattributed/unattributed.c}).
 * Three points are unrecovered: (1) whether it expands the chain key per key id (as modelled here) or
 * ratchets it forward before expanding; (2) the HKDF-Expand info label (NO {@code e2e sframe}* string
 * other than {@code "e2e sframe key"}, {@code "e2e sframe key generation"} and
 * {@code "e2e sframe key update"} is pinned in the WASM, and none of those is referenced by any
 * decompiled derivation function, so this expansion uses no recovered label); (3) the 12-byte
 * counter-mask salt VALUE that the native key params carry, which feeds {@link SFrameCipher}'s counter
 * mask. Missing evidence: the host HKDF/AES crypto-callback bodies (a native dependency, NOT present in
 * this WASM) AND one live ENCRYPTED frame. The live group capture verified the chain-key install
 * (32-byte base key via {@code set_chainkey}) and a 2026-06-15 follow-up
 * (re/calls2-spec/captures/sframe-frame-live.json) further proved {@code derive_sframe_key} pauses in a
 * voip worker on a real membership rekey, but {@code wa_sframe_encrypt} (and its only in-WASM video
 * caller {@code wa_video_sframe_encode_cb}) still NEVER fired even with a 3-party SFU group VIDEO call
 * provably encoding video at 14 fps: SFU group media is relay hop-by-hop SRTP with the SFrame per-frame
 * transform not engaged, and a 1:1 call uses no SFrame, so the per-key-id cipher material is never
 * observable on the wire and the chain-key-to-cipher expansion here stays a placeholder.
 */
public final class SFrameKeyProvider {
    /**
     * Holds the ratchet/window depth the native provider is created with.
     */
    public static final int PROVIDER_DEPTH = 30000;

    /**
     * Holds the ratchet mode the native provider is created with.
     */
    public static final int PROVIDER_MODE = 3;

    /**
     * Holds the chain-key length, in bytes, the provider stores ({@code 0x20}).
     */
    public static final int CHAIN_KEY_LENGTH = 32;

    /**
     * Holds the key-id and counter byte width the provider is configured with.
     */
    public static final int KEY_ID_COUNTER_WIDTH = 8;

    /**
     * Holds the counter distance beyond which a cached cipher is evicted ({@code 0x6fc23ac01},
     * about thirty billion).
     */
    public static final long EVICTION_DISTANCE = 0x6fc23ac01L;

    /**
     * Holds the total length, in bytes, of the cipher material expanded from the chain key: the AES
     * key followed by the HMAC key.
     */
    private static final int CIPHER_MATERIAL_LENGTH =
            SFrameCipherSuite.AES_KEY_LENGTH + SFrameCipherSuite.HMAC_KEY_LENGTH;

    /**
     * Holds the ASCII info for the chain-key-to-cipher HKDF expansion.
     *
     * <p>This labels the single expansion that turns the 32-byte chain key into the
     * {@value #CIPHER_MATERIAL_LENGTH}-byte cipher material; it is internal to the provider and not
     * carried on the wire. This label is a PLACEHOLDER, not a recovered value: the only pinned
     * {@code e2e sframe}* labels in the WASM string table are {@code "e2e sframe key"},
     * {@code "e2e sframe key generation"} and {@code "e2e sframe key update"}, and none is referenced by
     * any decompiled per-key-id derivation. It is kept only so the expansion has a deterministic,
     * self-consistent label until the native key-store derivation ({@code fn10765}, key-store vtable slot
     * {@code +0x1c}) is recovered; see the {@code TODO} on this class.
     */
    private static final byte[] CIPHER_EXPAND_INFO =
            "e2e sframe key-material".getBytes(StandardCharsets.US_ASCII);

    /**
     * Holds the cipher suite the provider applies to every key id it resolves.
     */
    private final SFrameCipherSuite suite;

    /**
     * Holds the current chain key, or {@code null} until a chain key is installed.
     */
    private byte[] chainKey;

    /**
     * Holds the transaction ids whose chain keys have been installed, so a duplicate install is a
     * no-op.
     */
    private final Map<Long, byte[]> chainKeysByTransaction = new LinkedHashMap<>();

    /**
     * Holds the per-key-id cipher cache, ordered by insertion for deterministic eviction scans.
     */
    private final Map<Long, SFrameCipher> ciphersByKeyId = new LinkedHashMap<>();

    /**
     * Holds the newest counter observed across all key ids, the reference point for eviction by
     * counter distance.
     */
    private long newestCounter;

    /**
     * Constructs a key provider applying the default cipher suite.
     */
    public SFrameKeyProvider() {
        this(SFrameCipherSuite.defaultSuite());
    }

    /**
     * Constructs a key provider applying the given cipher suite to every key id it resolves.
     *
     * @param suite the cipher suite to apply
     * @throws NullPointerException if {@code suite} is {@code null}
     */
    public SFrameKeyProvider(SFrameCipherSuite suite) {
        this.suite = Objects.requireNonNull(suite, "suite cannot be null");
    }

    /**
     * Installs a 32-byte chain key for a signaling transaction id, making it the current chain key.
     *
     * <p>Installing a chain key for a transaction id already present is a no-op and returns
     * {@code false}, mirroring the native return code {@code 0xd} for a duplicate transaction; this
     * makes a duplicated rekey delivery idempotent. A fresh transaction id stores the chain key,
     * makes it current, and clears the cached ciphers so the next frame derives against the new key.
     *
     * @param chainKey      the 32-byte chain key, the participant's derived SFrame base key
     * @param transactionId the signaling transaction id that delivered the chain key
     * @return {@code true} if the chain key was installed, {@code false} if the transaction id was
     *         already present
     * @throws NullPointerException     if {@code chainKey} is {@code null}
     * @throws IllegalArgumentException if {@code chainKey} is not {@value #CHAIN_KEY_LENGTH} bytes
     */
    public boolean setChainKey(byte[] chainKey, long transactionId) {
        Objects.requireNonNull(chainKey, "chainKey cannot be null");
        if (chainKey.length != CHAIN_KEY_LENGTH) {
            throw new IllegalArgumentException(
                    "chainKey must be " + CHAIN_KEY_LENGTH + " bytes, got " + chainKey.length);
        }
        if (chainKeysByTransaction.containsKey(transactionId)) {
            return false;
        }
        var stored = chainKey.clone();
        chainKeysByTransaction.put(transactionId, stored);
        this.chainKey = stored;
        ciphersByKeyId.clear();
        return true;
    }

    /**
     * Returns whether a chain key has been installed and is available to resolve ciphers.
     *
     * @return {@code true} if a chain key is current
     */
    public boolean hasChainKey() {
        return chainKey != null;
    }

    /**
     * Returns the cipher for a key id, deriving and caching it from the current chain key on the
     * first reference.
     *
     * <p>A cached cipher is returned directly; otherwise the chain key is expanded into the cipher's
     * AES and HMAC keys and the resulting cipher is cached under the key id. The {@code counter} is
     * the frame counter associated with the request and advances the newest-counter reference used to
     * evict ciphers whose key ids have fallen {@value #EVICTION_DISTANCE} counters or more behind.
     *
     * @param keyId   the key id naming which SFrame key the frame uses
     * @param counter the frame counter associated with the request
     * @return the cipher for {@code keyId}, or {@code null} if no chain key has been installed
     */
    public SFrameCipher cipherForKeyId(long keyId, long counter) {
        if (Long.compareUnsigned(counter, newestCounter) > 0) {
            newestCounter = counter;
        }
        evictStaleCiphers();
        var cached = ciphersByKeyId.get(keyId);
        if (cached != null) {
            return cached;
        }
        if (chainKey == null) {
            return null;
        }
        var cipher = deriveCipher(chainKey);
        ciphersByKeyId.put(keyId, cipher);
        return cipher;
    }

    /**
     * Derives an {@link SFrameCipher} from a chain key by expanding it into the AES and HMAC keys and
     * installing the per-key-id counter-mask salt.
     *
     * <p>A single HKDF-SHA256-Expand of the chain key yields {@value #CIPHER_MATERIAL_LENGTH} bytes,
     * sliced into the {@value SFrameCipherSuite#AES_KEY_LENGTH}-byte AES key followed by the
     * {@value SFrameCipherSuite#HMAC_KEY_LENGTH}-byte HMAC key. The cipher's 16-byte counter mask is the
     * {@link SFrameCipher#counterMaskFromSalt(byte[])} of the per-key-id salt; because that salt value is
     * not yet recovered (see the class {@code TODO}), this installs the all-zero salt
     * ({@link SFrameCipher#zeroCounterMask()}), which leaves the counter block unmasked.
     *
     * @param key the 32-byte chain key
     * @return the cipher keyed for the stream
     */
    private SFrameCipher deriveCipher(byte[] key) {
        var material = VoipCryptoNative.hkdfExpand(key, CIPHER_EXPAND_INFO, CIPHER_MATERIAL_LENGTH);
        var aesKey = new byte[SFrameCipherSuite.AES_KEY_LENGTH];
        var authKey = new byte[SFrameCipherSuite.HMAC_KEY_LENGTH];
        System.arraycopy(material, 0, aesKey, 0, SFrameCipherSuite.AES_KEY_LENGTH);
        System.arraycopy(material, SFrameCipherSuite.AES_KEY_LENGTH, authKey, 0,
                SFrameCipherSuite.HMAC_KEY_LENGTH);
        return new SFrameCipher(suite, aesKey, authKey, SFrameCipher.zeroCounterMask());
    }

    /**
     * Evicts cached ciphers whose key ids have fallen at least {@value #EVICTION_DISTANCE} counters
     * behind the newest observed counter.
     *
     * <p>The native cache evicts by counter distance rather than by key-id count; here the newest
     * counter is the reference and any key id older than {@code newestCounter - EVICTION_DISTANCE} is
     * dropped, since no in-window frame can still name it.
     */
    private void evictStaleCiphers() {
        if (Long.compareUnsigned(newestCounter, EVICTION_DISTANCE) < 0) {
            return;
        }
        var oldest = newestCounter - EVICTION_DISTANCE;
        ciphersByKeyId.keySet().removeIf(keyId -> Long.compareUnsigned(keyId, oldest) < 0);
    }
}
