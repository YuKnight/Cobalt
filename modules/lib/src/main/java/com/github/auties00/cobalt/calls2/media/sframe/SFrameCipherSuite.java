package com.github.auties00.cobalt.calls2.media.sframe;


/**
 * Enumerates the five SFrame cipher suites the call media path can negotiate, each binding a
 * BoringSSL EVP cipher identity to a truncated authentication-tag length.
 *
 * <p>SFrame seals every media payload with AES-128 in counter mode for confidentiality and a
 * truncated HMAC-SHA256 for authentication. The suite index selects which EVP cipher variant the
 * native engine instantiates and how many leading bytes of the HMAC are kept as the tag; the AES
 * key is always 16 bytes (AES-128), the HMAC key always 32 bytes, and the AES counter block always
 * 16 bytes, so only the EVP variant and the tag length differ across suites. Suite {@link #SUITE_0}
 * carries a zero-length tag, meaning no authentication.
 *
 * <p>The 1:1 and group call media path negotiated by WhatsApp Web uses {@link #SUITE_3}: AES-128-CTR
 * with a 4-byte truncated HMAC-SHA256 tag (IETF SFrame cipher suite 3 in RFC 9605 numbering). The
 * per-stream suite is negotiated in signaling through the {@code sframe_cipher_suite} stream setting
 * (an index in {@code 0..4}).
 *
 * @implNote This implementation reproduces the two suite-indexed tables read by the wa-voip WASM
 * module {@code ff-tScznZ8P}: the EVP cipher id table {@code DAT_ram_000fa5e8 = [2,3,4,0,1]}
 * (fn6880 {@code SFrameAesCipher::cipherCreate}) and the truncated tag-length table
 * {@code DAT_ram_000fa5fc = [0,4,8,8,8]} (fn6879 {@code sframe_cipher_taglen}). Cobalt resolves the
 * cipher purely through JCA ({@code Cipher("AES/CTR/NoPadding")} and {@code Mac("HmacSHA256")}), so
 * the raw EVP cipher enum value is retained only for parity and diagnostics; the operative
 * difference Cobalt acts on is the {@linkplain #tagLength() tag length}. The AES/HMAC/IV sizes are the
 * constants fn6878 {@code SFrameAesCipher::initCipher} validates (AES key 16, HMAC key 0x20, IV
 * 0x10, key-params marker 0x1c).
 */
public enum SFrameCipherSuite {
    /**
     * Suite {@code 0}: EVP cipher id {@code 2}, no authentication tag ({@code tagLength == 0}).
     *
     * <p>With a zero-length tag this suite applies no HMAC authentication; it is the
     * unauthenticated variant and is not used by the standard WhatsApp media path.
     */
    SUITE_0(0, 2, 0),

    /**
     * Suite {@code 1}: EVP cipher id {@code 3}, 4-byte truncated HMAC-SHA256 tag.
     */
    SUITE_1(1, 3, 4),

    /**
     * Suite {@code 2}: EVP cipher id {@code 4}, 8-byte truncated HMAC-SHA256 tag.
     */
    SUITE_2(2, 4, 8),

    /**
     * Suite {@code 3}: EVP cipher id {@code 0}, 4-byte truncated HMAC-SHA256 tag; the suite the
     * WhatsApp call media path negotiates.
     *
     * <p>This is AES-128-CTR with a 4-byte tag, IETF SFrame cipher suite 3 in RFC 9605 numbering,
     * and is the {@linkplain #defaultSuite() default} for Cobalt's media transform.
     */
    SUITE_3(3, 0, 4),

    /**
     * Suite {@code 4}: EVP cipher id {@code 1}, 8-byte truncated HMAC-SHA256 tag.
     */
    SUITE_4(4, 1, 8);

    /**
     * Holds the AES encryption-key length, in bytes, shared by every suite (AES-128).
     *
     * <p>The native cipher copies exactly this many bytes from the resolved key as the AES key
     * regardless of suite ({@code SFrameAesCipher} field at offset 0x14).
     */
    public static final int AES_KEY_LENGTH = 16;

    /**
     * Holds the HMAC-SHA256 authentication-key length, in bytes, shared by every suite.
     *
     * <p>The native {@code initCipher} validates the auth key length equals {@code 0x20} and copies
     * that many bytes as the HMAC key ({@code SFrameAesCipher} field at offset 0x28).
     */
    public static final int HMAC_KEY_LENGTH = 32;

    /**
     * Holds the AES counter-block (IV) length, in bytes, shared by every suite.
     *
     * <p>The native {@code cipherCreate} requires the IV length equals {@code kAESIVLen == 0x10}.
     */
    public static final int IV_LENGTH = 16;

    /**
     * Holds the suite index used on the wire and as the array index into the EVP and tag-length
     * tables.
     */
    private final int index;

    /**
     * Holds the BoringSSL EVP cipher enum value the native engine selects for this suite.
     */
    private final int evpCipherId;

    /**
     * Holds the truncated authentication-tag length, in bytes, for this suite.
     */
    private final int tagLength;

    /**
     * Constructs a suite constant bound to its wire index, native EVP cipher id, and tag length.
     *
     * @param index       the suite index in {@code 0..4}, used on the wire and as a table index
     * @param evpCipherId the BoringSSL EVP cipher enum value (retained for parity only)
     * @param tagLength   the truncated HMAC-SHA256 tag length in bytes
     */
    SFrameCipherSuite(int index, int evpCipherId, int tagLength) {
        this.index = index;
        this.evpCipherId = evpCipherId;
        this.tagLength = tagLength;
    }

    /**
     * Returns the suite index used on the wire and as the index into the suite tables.
     *
     * @return the suite index, {@code 0} through {@code 4}
     */
    public int index() {
        return index;
    }

    /**
     * Returns the BoringSSL EVP cipher enum value the native engine selects for this suite.
     *
     * <p>Cobalt resolves the cipher through JCA and does not consume this value operationally; it is
     * exposed for parity with the recovered {@code DAT_ram_000fa5e8 = [2,3,4,0,1]} table and for
     * diagnostics.
     *
     * @return the EVP cipher id, one of {@code 0,1,2,3,4}
     */
    public int evpCipherId() {
        return evpCipherId;
    }

    /**
     * Returns the truncated authentication-tag length, in bytes, this suite appends after the
     * ciphertext.
     *
     * <p>The values are {@code SUITE_0 -> 0}, {@code SUITE_1 -> 4}, {@code SUITE_2 -> 8},
     * {@code SUITE_3 -> 4}, {@code SUITE_4 -> 8}; a value of {@code 0} means the suite performs no
     * authentication.
     *
     * @return the tag length in bytes
     */
    public int tagLength() {
        return tagLength;
    }

    /**
     * Returns whether this suite appends an authentication tag.
     *
     * @return {@code true} unless the {@linkplain #tagLength() tag length} is zero
     */
    public boolean isAuthenticated() {
        return tagLength > 0;
    }

    /**
     * Returns the suite for the given wire index.
     *
     * @param index the suite index in {@code 0..4}
     * @return the matching suite
     * @throws IllegalArgumentException if {@code index} is outside {@code 0..4}
     */
    public static SFrameCipherSuite ofIndex(int index) {
        for (var suite : values()) {
            if (suite.index == index) {
                return suite;
            }
        }
        throw new IllegalArgumentException("Unknown SFrame cipher suite index: " + index);
    }

    /**
     * Returns the suite the WhatsApp call media path negotiates by default.
     *
     * <p>This is {@link #SUITE_3} (AES-128-CTR + 4-byte HMAC-SHA256 tag), the suite both the 1:1 and
     * group media paths use.
     *
     * @return {@link #SUITE_3}
     */
    public static SFrameCipherSuite defaultSuite() {
        return SUITE_3;
    }
}
