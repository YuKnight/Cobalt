package com.github.auties00.cobalt.device.adv;

import com.github.auties00.cobalt.device.util.DeviceConstants;
import com.github.auties00.cobalt.exception.ADVValidationException;
import com.github.auties00.cobalt.model.auth.SignedDeviceIdentity;
import com.github.auties00.cobalt.model.auth.SignedDeviceIdentityBuilder;
import com.github.auties00.cobalt.model.auth.SignedDeviceIdentityHMACSpec;
import com.github.auties00.cobalt.model.auth.SignedDeviceIdentitySpec;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.util.SecureBytes;
import com.github.auties00.curve25519.Curve25519;
import com.github.auties00.libsignal.key.SignalIdentityKeyPair;

import com.github.auties00.cobalt.model.auth.KeyIndexListSpec;
import com.github.auties00.cobalt.model.auth.SignedKeyIndexListSpec;
import it.auties.protobuf.exception.ProtobufDeserializationException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Validator for ADV (Authenticated Device Verification) signatures.
 * ADV is used to validate companion device identities to prevent MITM attacks.
 * <p>
 * For companion devices (device != 0), the prekey response must contain a device-identity
 * node with a SignedDeviceIdentity protobuf. The signature chain is validated to ensure
 * the identity key belongs to a legitimate device linked to the account.
 */
public final class DeviceADVValidator {
    /**
     * Header bytes prepended to messages before account signature verification for E2EE devices.
     * Used for verifying that the account signature is valid.
     */
    private static final byte[] E2EE_ACCOUNT_SIGNATURE_HEADER = {6, 0};

    /**
     * Header bytes prepended to messages before device signature verification/creation for E2EE devices.
     * Used for both verifying remote device signatures and creating local device signatures.
     */
    private static final byte[] E2EE_DEVICE_SIGNATURE_HEADER = {6, 1};

    /**
     * Header bytes prepended to messages before account signature verification for hosted devices.
     * Used for verifying that the account signature is valid.
     */
    private static final byte[] HOSTED_ACCOUNT_SIGNATURE_HEADER = {6, 5};

    /**
     * Header bytes prepended to messages before device signature verification/creation for hosted devices.
     * Used for both verifying remote device signatures and creating local device signatures.
     */
    private static final byte[] HOSTED_DEVICE_SIGNATURE_HEADER = {6, 6};

    /**
     * Header bytes prepended to messages before key index list account signature verification.
     * Used for verifying signed key index lists in USync responses and device notifications.
     */
    private static final byte[] KEY_INDEX_LIST_SIGNATURE_HEADER = {6, 2};

    private DeviceADVValidator() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Extracts and validates a local device identity from an input node
     *
     * @param localJid              the JID of the local device being validated
     * @param localCompanionKeyPair the non-null companion key pair
     * @param localIdentityKeyPair  the non-null local identity key pair
     * @param deviceIdentityNode    the non-null device identity
     * @return the validated signed device identity
     * @throws ADVValidationException if the device identity is missing, empty, or HMAC validation fails
     */
    public static SignedDeviceIdentity extractAndValidateLocalSignedDeviceIdentity(Jid localJid, SignalIdentityKeyPair localCompanionKeyPair, SignalIdentityKeyPair localIdentityKeyPair, Node deviceIdentityNode) {
        Objects.requireNonNull(localJid, "localJid cannot be null");
        Objects.requireNonNull(localCompanionKeyPair, "localCompanionKeyPair cannot be null");
        Objects.requireNonNull(localIdentityKeyPair, "localIdentityKeyPair cannot be null");
        Objects.requireNonNull(deviceIdentityNode, "deviceIdentityNode cannot be null");

        try {
            var deviceIdentityHmacBytes = deviceIdentityNode.getChild("device-identity")
                    .orElseThrow(() -> new ADVValidationException(localJid, ADVValidationException.Type.MISSING_DEVICE_IDENTITY))
                    .toContentBytes()
                    .orElseThrow(() -> new ADVValidationException(localJid, ADVValidationException.Type.EMPTY_DEVICE_IDENTITY));
            var deviceIdentityHmac = SignedDeviceIdentityHMACSpec.decode(deviceIdentityHmacBytes);
            var mac = Mac.getInstance("HmacSHA256");
            var companionKey = localCompanionKeyPair.publicKey().toEncodedPoint();
            var companionSecretKey = new SecretKeySpec(companionKey, "HmacSHA256");
            mac.init(companionSecretKey);
            var advSign = mac.doFinal(deviceIdentityHmac.details());
            if (!Arrays.equals(deviceIdentityHmac.hmac(), advSign)) {
                throw new ADVValidationException(localJid, ADVValidationException.Type.HMAC_VALIDATION_FAILED);
            }

            var deviceIdentity = SignedDeviceIdentitySpec.decode(deviceIdentityHmac.details());
            Objects.requireNonNull(deviceIdentity, "deviceIdentity required");
            Objects.requireNonNull(deviceIdentity.details(), "details required");
            Objects.requireNonNull(deviceIdentity.accountSignatureKey(), "accountSignatureKey required");
            Objects.requireNonNull(deviceIdentity.accountSignature(), "accountSignature required");

            byte[] accountSignatureHeader;
            byte[] deviceSignatureHeader;
            switch (deviceIdentityHmac.encryptionType()) {
                case E2EE -> {
                    accountSignatureHeader = E2EE_ACCOUNT_SIGNATURE_HEADER;
                    deviceSignatureHeader = E2EE_DEVICE_SIGNATURE_HEADER;
                }

                case HOSTED -> {
                    accountSignatureHeader = HOSTED_ACCOUNT_SIGNATURE_HEADER;
                    deviceSignatureHeader = HOSTED_DEVICE_SIGNATURE_HEADER;
                }

                case null -> throw new IllegalStateException("Unknown encryption type");
            }

            var localIdentityKey = localIdentityKeyPair.publicKey().toEncodedPoint();

            var message = SecureBytes.concat(accountSignatureHeader, deviceIdentity.details(), localIdentityKey);
            if (!Curve25519.verifySignature(deviceIdentity.accountSignatureKey(), message, deviceIdentity.accountSignature())) {
                throw new ADVValidationException(localJid, ADVValidationException.Type.HMAC_VALIDATION_FAILED);
            }

            var deviceSignatureMessage = SecureBytes.concat(deviceSignatureHeader, deviceIdentity.details(), localIdentityKey, deviceIdentity.accountSignatureKey());
            var deviceSignature = Curve25519.sign(localIdentityKeyPair.privateKey().toEncodedPoint(), deviceSignatureMessage);

            return new SignedDeviceIdentityBuilder()
                    .details(deviceIdentity.details())
                    .accountSignatureKey(deviceIdentity.accountSignatureKey())
                    .accountSignature(deviceIdentity.accountSignature())
                    .deviceSignature(deviceSignature)
                    .build();
        } catch (GeneralSecurityException exception) {
            throw new ADVValidationException(localJid, ADVValidationException.Type.CRYPTO_ERROR, exception);
        }
    }

    /**
     * Extracts and validates a remote device identity from an input node
     *
     * @param localJid                the local device JID
     * @param remoteJid               the remote device JID
     * @param localIdentity           the signed local device identity
     * @param remoteIdentityNode      the signed remote device identity
     * @param remoteIdentityKey the device's claimed identity key (32 bytes)
     * @return the validated signed device identity
     * @throws ADVValidationException if the device identity is missing, empty, or HMAC validation fails
     */
    public static Optional<SignedDeviceIdentity> extractAndValidateRemoteSignedDeviceIdentity(Jid localJid, Jid remoteJid, SignedDeviceIdentity localIdentity, Node remoteIdentityNode, byte[] remoteIdentityKey) {
        Objects.requireNonNull(localJid, "localJid cannot be null");
        Objects.requireNonNull(remoteJid, "remoteJid cannot be null");
        Objects.requireNonNull(localIdentity, "localIdentity cannot be null");
        Objects.requireNonNull(remoteIdentityNode, "remoteIdentityNode cannot be null");
        Objects.requireNonNull(remoteIdentityKey, "remoteIdentityKey cannot be null");

        if (!requiresValidation(remoteJid)) {
            return Optional.empty();
        }

        var remoteIdentityBytes = remoteIdentityNode.getChild("device-identity")
                .orElseThrow(() -> new ADVValidationException(remoteJid, ADVValidationException.Type.MISSING_DEVICE_IDENTITY))
                .toContentBytes()
                .orElseThrow(() -> new ADVValidationException(remoteJid, ADVValidationException.Type.EMPTY_DEVICE_IDENTITY));
        var remoteIdentity = SignedDeviceIdentitySpec.decode(remoteIdentityBytes);

        byte[] accountSignatureHeader;
        byte[] deviceSignatureHeader;
        if (remoteJid.hasServer(JidServer.hosted()) || remoteJid.hasServer(JidServer.hostedLid())) {
            accountSignatureHeader = HOSTED_ACCOUNT_SIGNATURE_HEADER;
            deviceSignatureHeader = HOSTED_DEVICE_SIGNATURE_HEADER;
        } else {
            accountSignatureHeader = E2EE_ACCOUNT_SIGNATURE_HEADER;
            deviceSignatureHeader = E2EE_DEVICE_SIGNATURE_HEADER;
        }

        var remoteIdentityAccountSignatureKey = localJid.withoutData().equals(remoteJid.withoutData()) ? localIdentity.accountSignatureKey() : remoteIdentity.accountSignatureKey();
        if(remoteIdentityAccountSignatureKey == null) {
            return Optional.empty();
        }

        var remoteIdentityDetails = Objects.requireNonNull(remoteIdentity.details(), "details cannot be null");
        var remoteIdentityAccountSignature = Objects.requireNonNull(remoteIdentity.accountSignature(), "accountSignature cannot be null");
        var accountMessage = SecureBytes.concat(accountSignatureHeader, remoteIdentityDetails, remoteIdentityKey);
        if (!Curve25519.verifySignature(remoteIdentityAccountSignatureKey, accountMessage, remoteIdentityAccountSignature)) {
            throw new ADVValidationException(remoteJid, ADVValidationException.Type.ACCOUNT_SIGNATURE_FAILED);
        }

        var remoteIdentityDeviceSignature = Objects.requireNonNull(remoteIdentity.deviceSignature(), "deviceSignature cannot be null");
        var deviceMessage = SecureBytes.concat(deviceSignatureHeader, remoteIdentityDetails, remoteIdentityKey, remoteIdentityAccountSignatureKey);
        if (!Curve25519.verifySignature(remoteIdentityKey, deviceMessage, remoteIdentityDeviceSignature)) {
            throw new ADVValidationException(remoteJid, ADVValidationException.Type.DEVICE_SIGNATURE_FAILED);
        }

        return Optional.of(remoteIdentity);
    }

    /**
     * Result of validating a signed key index list.
     *
     * @param rawId        the raw identity ID
     * @param timestamp    the timestamp from the key index list
     * @param validIndexes the list of valid key indexes
     * @param currentIndex the current key index
     * @param accountType  the account type (E2EE or HOSTED)
     */
    public record ValidatedKeyIndexList(
            long rawId,
            long timestamp,
            List<Integer> validIndexes,
            int currentIndex,
            com.github.auties00.cobalt.model.auth.ADVEncryptionType accountType
    ) {}

    /**
     * Validates and decodes a signed key index list from raw bytes.
     * <p>
     * This method verifies the account signature on the key index list using the
     * embedded account signature key. The signature is verified using Curve25519
     * with the {@link #KEY_INDEX_LIST_SIGNATURE_HEADER} prefix.
     *
     * @param signedKeyIndexBytes the raw signed key index list bytes
     * @param storedAccountSignatureKey optional stored account signature key (used if not embedded in the protobuf)
     * @return the validated key index list data, or empty if validation fails
     */
    public static Optional<ValidatedKeyIndexList> validateAndDecodeSignedKeyIndexList(
            byte[] signedKeyIndexBytes,
            byte[] storedAccountSignatureKey
    ) {
        Objects.requireNonNull(signedKeyIndexBytes, "signedKeyIndexBytes cannot be null");

        try {
            // Decode the outer signed key index list protobuf
            var signedKeyIndexList = SignedKeyIndexListSpec.decode(signedKeyIndexBytes);
            if (signedKeyIndexList == null || signedKeyIndexList.details() == null) {
                return Optional.empty();
            }

            // Get the account signature key (embedded or stored)
            var accountSignatureKey = signedKeyIndexList.accountSignatureKey();
            if (accountSignatureKey == null || accountSignatureKey.length == 0) {
                accountSignatureKey = storedAccountSignatureKey;
            }
            if (accountSignatureKey == null || accountSignatureKey.length == 0) {
                return Optional.empty();
            }

            // Verify the account signature
            var accountSignature = signedKeyIndexList.accountSignature();
            if (accountSignature == null || accountSignature.length == 0) {
                return Optional.empty();
            }

            var message = SecureBytes.concat(KEY_INDEX_LIST_SIGNATURE_HEADER, signedKeyIndexList.details());
            if (!Curve25519.verifySignature(accountSignatureKey, message, accountSignature)) {
                return Optional.empty();
            }

            // Decode the inner key index list protobuf
            var keyIndexList = KeyIndexListSpec.decode(signedKeyIndexList.details());

            // Validate required fields
            if (keyIndexList.rawId() == 0 && keyIndexList.timestamp() == 0) {
                return Optional.empty();
            }

            return Optional.of(new ValidatedKeyIndexList(
                    keyIndexList.rawId(),
                    keyIndexList.timestamp(),
                    keyIndexList.validIndexes() != null ? keyIndexList.validIndexes() : List.of(),
                    keyIndexList.currentIndex(),
                    keyIndexList.accountType()
            ));
        } catch (ProtobufDeserializationException e) {
            return Optional.empty();
        }
    }

    /**
     * Validates and decodes a signed key index list using only the embedded account signature key.
     *
     * @param signedKeyIndexBytes the raw signed key index list bytes
     * @return the validated key index list data, or empty if validation fails
     */
    public static Optional<ValidatedKeyIndexList> validateAndDecodeSignedKeyIndexList(byte[] signedKeyIndexBytes) {
        return validateAndDecodeSignedKeyIndexList(signedKeyIndexBytes, null);
    }

    /**
     * Checks if a device requires ADV validation.
     * Companion devices (device ID != 0) require ADV validation.
     *
     * @param jid the device JID
     * @return true if ADV validation is required
     */
    private static boolean requiresValidation(Jid jid) {
        Objects.requireNonNull(jid, "jid cannot be null");
        return jid.device() != DeviceConstants.PRIMARY_DEVICE_ID;
    }
}