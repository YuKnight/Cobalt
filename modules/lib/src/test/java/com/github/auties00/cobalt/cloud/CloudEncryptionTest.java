package com.github.auties00.cobalt.cloud;

import com.github.auties00.cobalt.client.cloud.CloudWhatsAppClient;
import com.github.auties00.cobalt.model.cloud.CloudApiVersion;
import com.github.auties00.cobalt.model.cloud.waba.CloudBusinessEncryption;
import com.github.auties00.cobalt.model.cloud.waba.CloudBusinessEncryptionSignatureStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Exercises the business encryption read and write through a {@link RecordingHttpClient}.
 */
@DisplayName("Cloud business encryption")
class CloudEncryptionTest {
    private static final String PHONE_ID = "1234567890";
    private static final String PEM = "-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8A"
            + "MIIBCgKCAQEArandom...IDAQAB\n-----END PUBLIC KEY-----";

    private static RecordingHttpClient http() {
        return new RecordingHttpClient();
    }

    private static CloudWhatsAppClient client(RecordingHttpClient http) {
        return CloudWhatsAppClient.builder()
                .loadConnection("token", PHONE_ID)
                .apiVersion(CloudApiVersion.V23_0)
                .httpClient(http)
                .build();
    }

    @Nested
    @DisplayName("get")
    class Get {
        @Test
        @DisplayName("maps the first data entry to the model with a valid status")
        void valid() {
            var http = http();
            http.respondWith("{\"data\":[{\"business_public_key\":\"" + PEM.replace("\n", "\\n")
                    + "\",\"business_public_key_signature_status\":\"VALID\"}]}");
            var encryption = client(http).queryBusinessEncryption().orElseThrow();
            assertEquals(PEM, encryption.businessPublicKey());
            assertEquals(CloudBusinessEncryptionSignatureStatus.VALID,
                    encryption.businessPublicKeySignatureStatus().orElseThrow());
            assertEquals("GET", http.lastMethod());
            assertTrue(http.lastUri().toString().endsWith(PHONE_ID + "/whatsapp_business_encryption"));
        }

        @Test
        @DisplayName("carries the mismatch status verbatim")
        void mismatch() {
            var http = http();
            http.respondWith("{\"data\":[{\"business_public_key\":\"" + PEM.replace("\n", "\\n")
                    + "\",\"business_public_key_signature_status\":\"MISMATCH\"}]}");
            var encryption = client(http).queryBusinessEncryption().orElseThrow();
            assertEquals(CloudBusinessEncryptionSignatureStatus.MISMATCH,
                    encryption.businessPublicKeySignatureStatus().orElseThrow());
        }

        @Test
        @DisplayName("returns empty for an empty data array")
        void empty() {
            var http = http();
            http.respondWith("{\"data\":[]}");
            assertTrue(client(http).queryBusinessEncryption().isEmpty());
        }
    }

    @Nested
    @DisplayName("edit")
    class Edit {
        @Test
        @DisplayName("posts the public key as a form field")
        void post() {
            var http = http();
            http.respondWith("{\"success\":true}");
            client(http).editBusinessEncryption(new CloudBusinessEncryption(PEM, null));
            assertEquals("POST", http.lastMethod());
            assertTrue(http.lastUri().toString().endsWith(PHONE_ID + "/whatsapp_business_encryption"));
            assertTrue(http.lastBody().contains("business_public_key="));
        }

        @Test
        @DisplayName("rejects a null encryption configuration")
        void nullKey() {
            assertThrows(NullPointerException.class, () -> client(http()).editBusinessEncryption(null));
        }
    }
}
