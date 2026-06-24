package com.github.auties00.cobalt.calls2.core.participant;

import com.github.auties00.cobalt.calls2.common.CallDeviceJid;
import com.github.auties00.cobalt.calls2.media.sframe.SFrameKeyProvider;
import com.github.auties00.cobalt.exception.WhatsAppCallException;
import com.github.auties00.cobalt.model.call.datachannel.E2eRekeyPayload;
import com.github.auties00.cobalt.model.call.datachannel.RekeyKeyEntry;
import com.github.auties00.cobalt.model.call.datachannel.RekeyKeyType;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;

/**
 * Holds the per-participant end-to-end crypto state of a call participant.
 *
 * <p>Every participant carries its own crypto block: the 32-byte raw end-to-end key the
 * caller minted and distributed over the Signal pipeline, the key-generation version and
 * bookkeeping the engine validates before accepting that key, and the keys the engine
 * derives from it (the SFrame chain key and the per-participant SRTP master). This class is
 * both the data holder for that block and the entry point that derives the per-participant
 * keys from the stored raw key: {@link #deriveKeys(CallDeviceJid)} runs the byte-verified
 * {@link CallE2eKeyDerivation} chain for the participant's device JID and writes the SFrame
 * chain key and the SRTP master back into the holder, mirroring the engine's inline
 * derivation. The lower-level setters ({@link #sframeChainKey(byte[])},
 * {@link #srtpMaster(byte[])}) remain so a caller that derives elsewhere can install results
 * directly.
 *
 * <p>The single normative behaviour the ingest enforces is the engine's contract for a raw
 * key: the raw key length must lie in {@code 1..32} and the key-generation version must
 * equal {@value #SUPPORTED_KEYGEN_VER}. {@link #storeRawKey(byte[], int, int, boolean)}
 * applies both checks and records the key, version, key count, transaction id, and has-bot
 * flag together, mirroring the engine's atomic ingest. Once a raw key is stored the derived
 * keys are cleared, because they no longer correspond to the new key until they are
 * re-derived.
 *
 * <p>The SFrame chain key is derived per participant from the raw key bound to that
 * participant's device JID through {@link CallE2eKeyDerivation#deriveSframeBaseKey}, whose
 * schedule is verified byte-for-byte against live WASM memory; the transaction id keys the
 * SFrame key provider so that a repeated rekey for the same transaction is idempotent. The
 * SRTP master is the per-participant {@code AES_CM_128_HMAC_SHA1} master the transport keys
 * its peer-to-peer SRTP context from.
 *
 * <p>This class is not thread-safe; the engine mutates a participant's crypto block only
 * while holding the membership lock that guards the owning {@link CallParticipant}, and
 * the derivation itself runs under the call's stream lock. All key accessors return
 * defensive copies so stored key material cannot be mutated through a returned reference.
 *
 * @implNote This implementation composes the {@code ParticipantCryptoBlock} embedded in
 * the wa-voip WASM module {@code ff-tScznZ8P} {@code call_participant} struct
 * ({@code call_participant_crypto.cc}, base offset {@code +0x61c78}): the raw E2E key
 * (32 bytes, {@code +0x61c78}), the has-bot flag ({@code +0x61c98}), the key count
 * ({@code +0x61c9c}), the keygen version ({@code +0x61ca0}), the transaction id
 * ({@code +0x61ca8}), and the SRTP-and-SFrame-tx-done flag ({@code +0x61d24}). The ingest
 * validation reproduces {@code call_update_participant_keys} (fn10898): it rejects a raw
 * key length outside {@code 1..0x20} and a keygen version other than {@code 2}
 * ("Unsupported keygen ver %d"), then derives the per-participant keys inline through
 * {@code generate_sframe_keys_for_participant} (fn10894) and
 * {@code generate_srtp_and_p2p_keys_for_participant} (fn10895); {@link #deriveKeys(CallDeviceJid)}
 * reproduces that inline derivation. The SFrame chain-key derivation is
 * {@code derive_sframe_key} (fn10896, live fn11063): HKDF-SHA256 with
 * {@code IKM = rawKey[16:32]}, {@code salt = rawKey[0:16]}, and
 * {@code info = "e2e sframe key" || deviceJID} (the device JID as raw ASCII, no separator
 * and no NUL terminator), output 32 bytes, verified byte-for-byte against live WASM memory
 * for secret {@code 86e0004078464597d59c751fde9a8b61908dcbd04197ffdc7636582be7f439aa},
 * participant {@code 83116928594056:2@lid}, and base key
 * {@code 409102bf2c1a3816c76a6d64819d0c901556e030d5f33da251c13cdfcf0b9353}. The
 * per-participant SRTP master is {@code generate_srtp_and_p2p_keys_for_participant} (fn10895),
 * stored at participant {@code +0x3b8} in the native struct; {@code set_transport_p2p_keys}
 * (fn10892) then copies the 16-byte key half of the self and peer participants' masters into
 * the two transport peer-to-peer slots, so the two SRTP directions are two participants' one
 * master each, not two keys in one block.
 */
public final class CallParticipantCrypto {
    /**
     * Holds the required length, in bytes, of the raw end-to-end key.
     *
     * <p>The engine mints exactly this many random bytes for the call key and rejects an
     * ingested key longer than this.
     */
    public static final int RAW_E2E_KEY_LENGTH = 32;

    /**
     * Holds the only key-generation version the engine accepts.
     *
     * <p>An ingested key carrying any other version is rejected.
     */
    public static final int SUPPORTED_KEYGEN_VER = 2;

    /**
     * Holds the length, in bytes, of the derived SFrame chain key.
     */
    public static final int SFRAME_CHAIN_KEY_LENGTH = 32;

    /**
     * Holds the length, in bytes, of the derived per-participant SRTP master (a 16-byte
     * {@code AES_CM_128_HMAC_SHA1} key followed by a 14-byte salt).
     */
    public static final int SRTP_MASTER_LENGTH = 30;

    /**
     * Holds the ASCII label that prefixes the SFrame chain-key HKDF info string.
     *
     * <p>The crypto layer builds the HKDF info as these fourteen ASCII bytes immediately
     * followed by the participant's device JID rendered as raw ASCII, with no separator and
     * no NUL terminator.
     */
    public static final String SFRAME_HKDF_INFO_LABEL = "e2e sframe key";

    /**
     * Holds the sentinel transaction id meaning no rekey transaction has been recorded.
     */
    public static final int NO_TRANSACTION_ID = -1;

    /**
     * Holds the raw end-to-end key, or {@code null} until a key has been ingested.
     */
    private byte[] rawKey;

    /**
     * Holds the key-generation version recorded with the current raw key.
     */
    private int keygenVersion;

    /**
     * Holds the key count recorded with the current raw key.
     */
    private int keyCount;

    /**
     * Holds the rekey transaction id recorded with the current raw key, or
     * {@value #NO_TRANSACTION_ID} when none has been recorded.
     */
    private int transactionId;

    /**
     * Holds whether the current raw key was ingested for a participant that has a bot.
     */
    private boolean hasBot;

    /**
     * Holds the derived SFrame chain key, or {@code null} until the crypto layer derives
     * it.
     */
    private byte[] sframeChainKey;

    /**
     * Holds the derived per-participant SRTP master (16-byte key plus 14-byte salt), or
     * {@code null} until it is derived.
     */
    private byte[] srtpMaster;

    /**
     * Holds whether the self SRTP-and-SFrame transmit update has completed for the current
     * key.
     */
    private boolean transmitKeysInstalled;

    /**
     * Constructs an empty crypto block with no raw key, no derived keys, and no recorded
     * transaction.
     */
    public CallParticipantCrypto() {
        this.transactionId = NO_TRANSACTION_ID;
    }

    /**
     * Stores a freshly ingested raw end-to-end key together with its bookkeeping.
     *
     * <p>This applies the engine's ingest contract: the raw key length must lie in
     * {@code 1..}{@value #RAW_E2E_KEY_LENGTH} and {@code keygenVersion} must equal
     * {@value #SUPPORTED_KEYGEN_VER}. On success it records a defensive copy of the key,
     * the version, the key count, the transaction id, and the has-bot flag, and clears any
     * previously derived keys, since they no longer correspond to the new raw key. On
     * failure it throws and leaves the previous state untouched.
     *
     * @implNote This implementation reproduces the validation of
     * {@code call_update_participant_keys} (fn10898): a length check of {@code 1..0x20} and
     * a keygen-version check that rejects any value other than {@code 2}. The native path
     * logs "Unsupported keygen ver %d" and aborts the ingest; here the rejection is a
     * thrown {@link WhatsAppCallException.Srtp}.
     * @param rawKey        the raw end-to-end key bytes; never {@code null}
     * @param keygenVersion the key-generation version; must equal
     *                      {@value #SUPPORTED_KEYGEN_VER}
     * @param keyCount      the key count the engine recorded for this key
     * @param hasBot        whether the participant has a bot
     * @throws NullPointerException       if {@code rawKey} is {@code null}
     * @throws WhatsAppCallException.Srtp if the key length is outside {@code 1..32} or the
     *                                    keygen version is unsupported
     */
    public void storeRawKey(byte[] rawKey, int keygenVersion, int keyCount, boolean hasBot) {
        storeRawKey(rawKey, keygenVersion, keyCount, hasBot, NO_TRANSACTION_ID);
    }

    /**
     * Stores a freshly ingested raw end-to-end key, its bookkeeping, and the rekey
     * transaction id that delivered it.
     *
     * <p>This behaves exactly like {@link #storeRawKey(byte[], int, int, boolean)} but also
     * records the transaction id that keys the SFrame key provider, so a later rekey for
     * the same transaction can be recognized as a duplicate.
     *
     * @param rawKey        the raw end-to-end key bytes; never {@code null}
     * @param keygenVersion the key-generation version; must equal
     *                      {@value #SUPPORTED_KEYGEN_VER}
     * @param keyCount      the key count the engine recorded for this key
     * @param hasBot        whether the participant has a bot
     * @param transactionId the rekey transaction id, or {@value #NO_TRANSACTION_ID} if none
     * @throws NullPointerException       if {@code rawKey} is {@code null}
     * @throws WhatsAppCallException.Srtp if the key length is outside {@code 1..32} or the
     *                                    keygen version is unsupported
     */
    public void storeRawKey(byte[] rawKey, int keygenVersion, int keyCount, boolean hasBot, int transactionId) {
        if (rawKey == null) {
            throw new WhatsAppCallException.Srtp("raw E2E key cannot be null");
        }
        if (rawKey.length < 1 || rawKey.length > RAW_E2E_KEY_LENGTH) {
            throw new WhatsAppCallException.Srtp(
                    "raw E2E key length " + rawKey.length + " is outside the range 1.." + RAW_E2E_KEY_LENGTH);
        }
        if (keygenVersion != SUPPORTED_KEYGEN_VER) {
            throw new WhatsAppCallException.Srtp("Unsupported keygen ver " + keygenVersion);
        }
        var rawKeyHex = new StringBuilder(rawKey.length * 2);
        for (var b : rawKey) {
            rawKeyHex.append(String.format("%02x", b & 0xFF));
        }
        System.getLogger(CallParticipantCrypto.class.getName()).log(System.Logger.Level.INFO,
                "calls2 E2E raw call key ({0} bytes, keygenVer={1}): {2}", rawKey.length, keygenVersion, rawKeyHex);
        this.rawKey = rawKey.clone();
        this.keygenVersion = keygenVersion;
        this.keyCount = keyCount;
        this.hasBot = hasBot;
        this.transactionId = transactionId;
        this.sframeChainKey = null;
        this.srtpMaster = null;
        this.transmitKeysInstalled = false;
    }

    /**
     * Stores the raw end-to-end key carried by an inbound rekey payload.
     *
     * <p>The live capture confirms a rekey payload carries a single 32-byte raw key (inside
     * a {@code Message.Call.callKey}), not three pre-derived per-domain masters; the three
     * per-domain keys ({@link RekeyKeyType#AUDIO}, {@link RekeyKeyType#VIDEO},
     * {@link RekeyKeyType#APPDATA}) are derived locally from this one key. This convenience
     * extracts that single key from a single-entry {@link E2eRekeyPayload} and ingests it
     * through {@link #storeRawKey(byte[], int, int, boolean, int)}.
     *
     * @implNote This implementation reflects the resolved {@code rev-datachannel-rekey} /
     * {@code int-signal-crypto} finding that the wire {@code enc_rekey} payload carries one
     * 32-byte raw E2E key and the per-domain {@link RekeyKeyType} split
     * ({@link RekeyKeyType#AUDIO}, {@link RekeyKeyType#VIDEO}, {@link RekeyKeyType#APPDATA})
     * is a local derivation product, not a transmitted structure. It therefore requires the
     * payload to carry exactly one {@link RekeyKeyEntry} and rejects a multi-entry payload
     * rather than silently dropping all but the first entry, so that a future on-the-wire
     * shape that actually carried per-domain masters surfaces as an error instead of a
     * partial install. A 1:1 call never ships {@code enc_rekey} (verified across a
     * ~30-minute connected call including a video upgrade); the single-key shape is the only
     * one observed, and the multi-entry case remains unconfirmed pending a group-call
     * {@code enc_rekey} capture (see the {@code rev-datachannel-rekey} open question).
     * @param payload       the decoded rekey payload; never {@code null} and carrying exactly
     *                      one key entry
     * @param keyCount      the key count to record for the rekeyed key
     * @param hasBot        whether the participant has a bot
     * @param transactionId the rekey transaction id, or {@value #NO_TRANSACTION_ID} if none
     * @throws NullPointerException       if {@code payload} is {@code null}
     * @throws WhatsAppCallException.Srtp if the payload does not carry exactly one key entry,
     *                                    or if the key fails the ingest contract
     */
    public void storeRekeyPayload(E2eRekeyPayload payload, int keyCount, boolean hasBot, int transactionId) {
        if (payload == null) {
            throw new WhatsAppCallException.Srtp("rekey payload cannot be null");
        }
        var keys = payload.keys();
        if (keys.size() != 1) {
            throw new WhatsAppCallException.Srtp(
                    "rekey payload must carry exactly one key entry, got " + keys.size());
        }
        storeRawKey(keys.getFirst().key(), SUPPORTED_KEYGEN_VER, keyCount, hasBot, transactionId);
    }

    /**
     * Returns whether a raw end-to-end key has been ingested.
     *
     * @return {@code true} if a raw key is present
     */
    public boolean hasRawKey() {
        return rawKey != null;
    }

    /**
     * Returns the raw end-to-end key.
     *
     * <p>The returned array, when present, is a defensive copy; mutating it does not affect
     * this holder.
     *
     * @return an {@code Optional} holding a copy of the raw key, or empty if no key has
     *         been ingested
     */
    public Optional<byte[]> rawKey() {
        return Optional.ofNullable(rawKey)
                .map(byte[]::clone);
    }

    /**
     * Returns the key-generation version recorded with the current raw key.
     *
     * @return the keygen version, or {@code 0} if no key has been ingested
     */
    public int keygenVersion() {
        return keygenVersion;
    }

    /**
     * Returns the key count recorded with the current raw key.
     *
     * @return the key count, or {@code 0} if no key has been ingested
     */
    public int keyCount() {
        return keyCount;
    }

    /**
     * Returns the rekey transaction id recorded with the current raw key.
     *
     * @return the transaction id, or {@value #NO_TRANSACTION_ID} if none has been recorded
     */
    public int transactionId() {
        return transactionId;
    }

    /**
     * Returns whether the current raw key was ingested for a participant that has a bot.
     *
     * @return {@code true} if the participant has a bot
     */
    public boolean hasBot() {
        return hasBot;
    }

    /**
     * Records the SFrame chain key the crypto layer derived for this participant.
     *
     * <p>The chain key is the 32-byte output of the SFrame HKDF over this participant's raw
     * key, bound to the participant's device JID. This holder stores a defensive copy; it
     * does not perform the derivation.
     *
     * @param sframeChainKey the derived SFrame chain key; never {@code null}
     * @throws NullPointerException       if {@code sframeChainKey} is {@code null}
     * @throws WhatsAppCallException.Srtp if the chain key is not
     *                                    {@value #SFRAME_CHAIN_KEY_LENGTH} bytes
     */
    public void sframeChainKey(byte[] sframeChainKey) {
        if (sframeChainKey == null) {
            throw new WhatsAppCallException.Srtp("SFrame chain key cannot be null");
        }
        if (sframeChainKey.length != SFRAME_CHAIN_KEY_LENGTH) {
            throw new WhatsAppCallException.Srtp(
                    "SFrame chain key length " + sframeChainKey.length + " must be " + SFRAME_CHAIN_KEY_LENGTH);
        }
        this.sframeChainKey = sframeChainKey.clone();
    }

    /**
     * Returns the SFrame chain key the crypto layer derived for this participant.
     *
     * <p>The returned array, when present, is a defensive copy.
     *
     * @return an {@code Optional} holding a copy of the SFrame chain key, or empty if it
     *         has not been derived
     */
    public Optional<byte[]> sframeChainKey() {
        return Optional.ofNullable(sframeChainKey)
                .map(byte[]::clone);
    }

    /**
     * Installs this participant's derived SFrame chain key into a key provider.
     *
     * <p>This is the seam the media plane drives to make a participant's relayed SFrame media decryptable:
     * the provider it hands an inbound demux for this participant receives this participant's chain key
     * (its SFrame base key), keyed by the recorded {@linkplain #transactionId() rekey transaction id}, or
     * {@code 0} when no rekey transaction has been recorded. Installing a chain key for a transaction id
     * already present in the provider is a no-op, so a repeated rekey delivery is idempotent. Returns
     * {@code false} (and installs nothing) when this block has no derived chain key yet, so the caller can
     * skip a participant whose keys are not ready.
     *
     * @implNote This implementation reproduces the {@code generate_sframe_keys_for_participant} (fn10894)
     * step that calls {@code sframe_keyprovider_set_chainkey} (fn10815) with the participant's derived
     * base key and the rekey transaction id; the per-key-id frame cipher is then resolved by the frame's
     * key id, not by the transaction id. The native engine keeps one provider per participant
     * ({@code participant+0xe0}); this installs into whichever provider the media plane owns for the
     * participant.
     * @param provider the SFrame key provider to install the chain key into; never {@code null}
     * @return {@code true} if a chain key was installed, {@code false} if none is derived or the
     *         transaction was already present
     * @throws NullPointerException if {@code provider} is {@code null}
     */
    public boolean installSframeChainKey(SFrameKeyProvider provider) {
        Objects.requireNonNull(provider, "provider cannot be null");
        if (sframeChainKey == null) {
            return false;
        }
        var keyTransaction = transactionId == NO_TRANSACTION_ID ? 0L : transactionId;
        return provider.setChainKey(sframeChainKey.clone(), keyTransaction);
    }

    /**
     * Records the per-participant SRTP master the crypto layer derived for this participant.
     *
     * <p>The master is the thirty-byte {@code AES_CM_128_HMAC_SHA1} master (16-byte key
     * followed by 14-byte salt) derived from the raw key bound to this participant's device
     * JID. This holder stores a defensive copy; it does not perform the derivation unless
     * {@link #deriveKeys(CallDeviceJid)} is used.
     *
     * @implNote This implementation mirrors the per-participant SRTP master the engine writes
     * at {@code call_participant+0x3b8} ({@code generate_srtp_and_p2p_keys_for_participant},
     * fn10895); {@code set_transport_p2p_keys} (fn10892) later copies the 16-byte key half of
     * the self and peer participants' masters into the transport's two peer-to-peer slots.
     * @param srtpMaster the derived SRTP master; never {@code null}
     * @throws NullPointerException       if {@code srtpMaster} is {@code null}
     * @throws WhatsAppCallException.Srtp if the master is not {@value #SRTP_MASTER_LENGTH}
     *                                    bytes
     */
    public void srtpMaster(byte[] srtpMaster) {
        if (srtpMaster == null) {
            throw new WhatsAppCallException.Srtp("SRTP master cannot be null");
        }
        if (srtpMaster.length != SRTP_MASTER_LENGTH) {
            throw new WhatsAppCallException.Srtp(
                    "SRTP master length " + srtpMaster.length + " must be " + SRTP_MASTER_LENGTH);
        }
        this.srtpMaster = srtpMaster.clone();
    }

    /**
     * Returns the per-participant SRTP master.
     *
     * <p>The returned array, when present, is a defensive copy of the thirty-byte master
     * (16-byte {@code AES_CM_128_HMAC_SHA1} key followed by 14-byte salt). Split it with
     * {@link CallE2eKeyDerivation#srtpMasterKey(byte[])} and
     * {@link CallE2eKeyDerivation#srtpMasterSalt(byte[])}.
     *
     * @return an {@code Optional} holding a copy of the SRTP master, or empty if it has not
     *         been derived
     */
    public Optional<byte[]> srtpMaster() {
        return Optional.ofNullable(srtpMaster)
                .map(byte[]::clone);
    }

    /**
     * Derives this participant's SFrame chain key and SRTP master from the stored raw key and
     * the participant's device JID, then records both in this block.
     *
     * <p>This is the holder-driven form of the engine's inline per-participant derivation: it
     * requires a raw key to have been ingested, runs the byte-verified
     * {@link CallE2eKeyDerivation} chain for {@code deviceJid} under the stored
     * {@linkplain #keygenVersion() keygen version}, and stores the resulting SFrame base
     * (chain) key and SRTP master through {@link #sframeChainKey(byte[])} and
     * {@link #srtpMaster(byte[])}. The SFrame chain key is the one the media plane installs
     * into this participant's {@code SFrameKeyProvider} so its relayed media decrypts.
     *
     * <p>The raw key must be the full {@value #RAW_E2E_KEY_LENGTH} bytes for the chain, since
     * {@link CallE2eKeyDerivation} requires a full-length key; a shorter ingested key
     * (accepted by {@link #storeRawKey(byte[], int, int, boolean)} for the {@code 1..32}
     * contract) cannot be expanded and is rejected here.
     *
     * @implNote This implementation reproduces the per-participant derivation
     * {@code call_update_participant_keys} (fn10898) runs inline for every connected
     * participant: {@code generate_sframe_keys_for_participant} (fn10894) then
     * {@code generate_srtp_and_p2p_keys_for_participant} (fn10895), both gated on keygen
     * version {@code 2}.
     * @param deviceJid the participant's device JID binding the derivation; never {@code null}
     * @throws NullPointerException       if {@code deviceJid} is {@code null}
     * @throws WhatsAppCallException.Srtp if no raw key has been ingested, the keygen version is
     *                                    unsupported, or the raw key is not the full
     *                                    {@value #RAW_E2E_KEY_LENGTH} bytes
     */
    public void deriveKeys(CallDeviceJid deviceJid) {
        Objects.requireNonNull(deviceJid, "deviceJid cannot be null");
        if (rawKey == null) {
            throw new WhatsAppCallException.Srtp("cannot derive keys before a raw key is ingested");
        }
        if (rawKey.length != RAW_E2E_KEY_LENGTH) {
            throw new WhatsAppCallException.Srtp(
                    "cannot derive keys from a " + rawKey.length + "-byte raw key, " + RAW_E2E_KEY_LENGTH
                            + " required");
        }
        var chain = CallE2eKeyDerivation.deriveKeyChain(rawKey, keygenVersion, deviceJid);
        sframeChainKey(chain.sframeBaseKey());
        srtpMaster(chain.srtpMaster());
    }

    /**
     * Returns whether the self SRTP-and-SFrame transmit update has completed for the
     * current key.
     *
     * @return {@code true} if the transmit keys have been installed into the media plane
     */
    public boolean transmitKeysInstalled() {
        return transmitKeysInstalled;
    }

    /**
     * Marks whether the self SRTP-and-SFrame transmit update has completed for the current
     * key.
     *
     * @implNote This implementation mirrors the {@code srtp_sframe_tx_done} flag the engine
     * sets at {@code call_participant+0x61d24} once the self transmit key push to the media
     * plane succeeds ({@code call_update_self_participant_srtp_and_sframe_tx}, fn10893).
     * @param transmitKeysInstalled {@code true} once the transmit keys are installed
     * @return this crypto block
     */
    public CallParticipantCrypto transmitKeysInstalled(boolean transmitKeysInstalled) {
        this.transmitKeysInstalled = transmitKeysInstalled;
        return this;
    }

    /**
     * Builds the SFrame chain-key HKDF info bytes for the given device JID.
     *
     * <p>The info is the {@value #SFRAME_HKDF_INFO_LABEL} label rendered as ASCII,
     * immediately followed by the device JID rendered as raw ASCII, with no separator and
     * no NUL terminator. This is the exact info string the crypto layer must pass to the
     * SFrame HKDF; it is exposed here so the holder and the derivation engine agree on the
     * derivation context.
     *
     * @implNote This implementation reproduces the info construction of
     * {@code derive_sframe_key} (fn10896): the fourteen-byte label
     * {@code "e2e sframe key"} concatenated with the participant context string (the device
     * JID, bounded to {@code 0x50} = 80 bytes by {@code strnlen}). The live capture
     * confirmed the label is followed by the JID with no separator and no NUL terminator;
     * the {@code +NUL} and salt-swapped variants both failed to reproduce the captured base
     * key.
     * @param deviceJid the participant's device JID whose ASCII form binds the derivation;
     *                  never {@code null}
     * @return the HKDF info bytes
     * @throws NullPointerException if {@code deviceJid} is {@code null}
     */
    public static byte[] sframeHkdfInfo(CallDeviceJid deviceJid) {
        if (deviceJid == null) {
            throw new WhatsAppCallException.Srtp("deviceJid cannot be null");
        }
        var label = SFRAME_HKDF_INFO_LABEL.getBytes(StandardCharsets.US_ASCII);
        var jid = deviceJid.jid().toString().getBytes(StandardCharsets.US_ASCII);
        var info = new byte[label.length + jid.length];
        System.arraycopy(label, 0, info, 0, label.length);
        System.arraycopy(jid, 0, info, label.length, jid.length);
        return info;
    }

    @Override
    public String toString() {
        return "CallParticipantCrypto[hasRawKey=" + (rawKey != null)
                + ", keygenVersion=" + keygenVersion
                + ", keyCount=" + keyCount
                + ", transactionId=" + transactionId
                + ", hasBot=" + hasBot
                + ", sframeChainKeyDerived=" + (sframeChainKey != null)
                + ", srtpMasterDerived=" + (srtpMaster != null)
                + ", transmitKeysInstalled=" + transmitKeysInstalled
                + ']';
    }
}
