package com.github.auties00.cobalt.sync.crypto;

import com.github.auties00.cobalt.exception.WhatsAppWebAppStateSyncException;
import com.github.auties00.cobalt.model.sync.SyncActionData;
import com.github.auties00.cobalt.model.sync.SyncActionValue;
import com.github.auties00.cobalt.model.sync.data.SyncdMutation;
import it.auties.protobuf.stream.ProtobufInputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Arrays;

public sealed interface DecryptedMutation {
    String index();
    SyncdMutation.SyncdOperation operation();
    long timestamp();

    record Untrusted(
            String index,
            byte[] indexMac,
            byte[] valueMac,
            SyncActionValue value,
            SyncdMutation.SyncdOperation operation,
            long timestamp
    ) implements DecryptedMutation {
        private static final int IV_LENGTH = 16;
        private static final int MAC_LENGTH = 32;
        private static final byte[] VERSION = {0x00, 0x00, 0x00, 0x02};

        public static Untrusted of(
                byte[] encryptedValue,
                byte[] indexMac,
                MutationKeys keys,
                SyncdMutation.SyncdOperation operation
        ) throws GeneralSecurityException {
            if (encryptedValue.length < IV_LENGTH + MAC_LENGTH) {
                throw new IllegalArgumentException("Encrypted value too short");
            }

            // Extract value MAC
            var valueMac = Arrays.copyOfRange(encryptedValue, encryptedValue.length - 32, encryptedValue.length);

            // Verify value MAC
            var mac = Mac.getInstance("HmacSHA256");
            mac.init(keys.valueMacKey());
            mac.update(operation.content());
            mac.update(VERSION);
            mac.update(encryptedValue, 0, IV_LENGTH);
            mac.update(encryptedValue, IV_LENGTH, encryptedValue.length - IV_LENGTH - MAC_LENGTH);
            var expectedMac = mac.doFinal();
            if (!MessageDigest.isEqual(valueMac, expectedMac)) {
                throw new WhatsAppWebAppStateSyncException.ValueMacMismatch();
            }

            // Decrypt payload with AES-256-CBC and decode protobuf
            var cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            var ivSpec = new IvParameterSpec(encryptedValue, 0, IV_LENGTH);
            cipher.init(Cipher.DECRYPT_MODE, keys.valueEncryptionKey(), ivSpec);
            var ciphertextStream = new ByteArrayInputStream(encryptedValue, IV_LENGTH, encryptedValue.length - IV_LENGTH - MAC_LENGTH);
            var plaintextStream = new CipherInputStream(ciphertextStream, cipher);
            var actionData = SyncActionDataSpec.decode(ProtobufInputStream.fromStream(plaintextStream));

            // Verify index MAC
            mac.init(keys.indexKey());
            var expectedIndexMac = mac.doFinal(actionData.index());
            if (!MessageDigest.isEqual(indexMac, expectedIndexMac)) {
                throw new WhatsAppWebAppStateSyncException.IndexMacMismatch();
            }

            // Build mutation
            return new Untrusted(
                    new String(actionData.index(), StandardCharsets.UTF_8),
                    indexMac,
                    valueMac,
                    actionData.value(),
                    operation,
                    actionData.value().timestamp()
            );
        }
    }

    record Trusted(
            String index,
            SyncActionValue value,
            SyncdMutation.SyncdOperation operation,
            long timestamp
    ) implements DecryptedMutation {

    }
}
