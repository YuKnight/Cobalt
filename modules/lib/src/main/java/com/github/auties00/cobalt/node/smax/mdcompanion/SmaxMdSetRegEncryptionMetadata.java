package com.github.auties00.cobalt.node.smax.mdcompanion;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * Projection of the optional {@code <encryption-metadata/>} child
 * carried by the {@code <pair-success/>} stanza.
 */
@WhatsAppWebModule(moduleName = "WASmaxInMdAESEncryptionMetadataMixin")
public final class SmaxMdSetRegEncryptionMetadata {
    /**
     * The {@code version} attribute (always {@code "1"}).
     */
    private final String version;

    /**
     * The {@code algorithm} attribute (always {@code "aes-256-gcm"}).
     */
    private final String algorithm;

    /**
     * The {@code <encrypted_key/>} content bytes.
     */
    private final byte[] encryptedKey;

    /**
     * The {@code <nonce/>} content bytes.
     */
    private final byte[] nonce;

    /**
     * The {@code <encrypted_data/>} content bytes.
     */
    private final byte[] encryptedData;

    /**
     * The {@code <auth_tag/>} content bytes.
     */
    private final byte[] authTag;

    /**
     * Constructs a new metadata projection.
     *
     * @param version       the version literal; never {@code null}
     * @param algorithm     the algorithm literal; never {@code null}
     * @param encryptedKey  the wrapped key bytes; never {@code null}
     * @param nonce         the GCM nonce bytes; never {@code null}
     * @param encryptedData the ciphertext bytes; never {@code null}
     * @param authTag       the GCM auth tag bytes; never {@code null}
     * @throws NullPointerException if any argument is {@code null}
     */
    public SmaxMdSetRegEncryptionMetadata(String version, String algorithm,
                              byte[] encryptedKey, byte[] nonce,
                              byte[] encryptedData, byte[] authTag) {
        this.version = Objects.requireNonNull(version, "version cannot be null");
        this.algorithm = Objects.requireNonNull(algorithm, "algorithm cannot be null");
        this.encryptedKey = Objects.requireNonNull(encryptedKey, "encryptedKey cannot be null");
        this.nonce = Objects.requireNonNull(nonce, "nonce cannot be null");
        this.encryptedData = Objects.requireNonNull(encryptedData, "encryptedData cannot be null");
        this.authTag = Objects.requireNonNull(authTag, "authTag cannot be null");
    }

    /**
     * Returns the version literal.
     *
     * @return the version; never {@code null}
     */
    public String version() {
        return version;
    }

    /**
     * Returns the algorithm literal.
     *
     * @return the algorithm; never {@code null}
     */
    public String algorithm() {
        return algorithm;
    }

    /**
     * Returns the wrapped-key bytes.
     *
     * @return the bytes; never {@code null}
     */
    public byte[] encryptedKey() {
        return encryptedKey;
    }

    /**
     * Returns the nonce bytes.
     *
     * @return the bytes; never {@code null}
     */
    public byte[] nonce() {
        return nonce;
    }

    /**
     * Returns the ciphertext bytes.
     *
     * @return the bytes; never {@code null}
     */
    public byte[] encryptedData() {
        return encryptedData;
    }

    /**
     * Returns the auth-tag bytes.
     *
     * @return the bytes; never {@code null}
     */
    public byte[] authTag() {
        return authTag;
    }

    /**
     * Tries to parse an {@link SmaxMdSetRegEncryptionMetadata} projection.
     *
     * @param node the {@code <encryption-metadata/>} child; never {@code null}
     * @return an {@link Optional} carrying the projection, or empty
     */
    @WhatsAppWebExport(moduleName = "WASmaxInMdAESEncryptionMetadataMixin",
            exports = "parseAESEncryptionMetadataMixin",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public static Optional<SmaxMdSetRegEncryptionMetadata> of(Node node) {
        if (!node.hasAttribute("version", "1")) {
            return Optional.empty();
        }
        if (!node.hasAttribute("algorithm", "aes-256-gcm")) {
            return Optional.empty();
        }
        var encryptedKey = node.getChild("encrypted_key")
                .flatMap(Node::toContentBytes)
                .orElse(null);
        if (encryptedKey == null) {
            return Optional.empty();
        }
        var nonce = node.getChild("nonce")
                .flatMap(Node::toContentBytes)
                .orElse(null);
        if (nonce == null) {
            return Optional.empty();
        }
        var encryptedData = node.getChild("encrypted_data")
                .flatMap(Node::toContentBytes)
                .orElse(null);
        if (encryptedData == null) {
            return Optional.empty();
        }
        var authTag = node.getChild("auth_tag")
                .flatMap(Node::toContentBytes)
                .orElse(null);
        if (authTag == null) {
            return Optional.empty();
        }
        return Optional.of(new SmaxMdSetRegEncryptionMetadata("1", "aes-256-gcm", encryptedKey, nonce, encryptedData, authTag));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxMdSetRegEncryptionMetadata) obj;
        return Objects.equals(this.version, that.version)
                && Objects.equals(this.algorithm, that.algorithm)
                && Arrays.equals(this.encryptedKey, that.encryptedKey)
                && Arrays.equals(this.nonce, that.nonce)
                && Arrays.equals(this.encryptedData, that.encryptedData)
                && Arrays.equals(this.authTag, that.authTag);
    }

    @Override
    public int hashCode() {
        var result = Objects.hash(version, algorithm);
        result = 31 * result + Arrays.hashCode(encryptedKey);
        result = 31 * result + Arrays.hashCode(nonce);
        result = 31 * result + Arrays.hashCode(encryptedData);
        result = 31 * result + Arrays.hashCode(authTag);
        return result;
    }

    @Override
    public String toString() {
        return "SmaxMdSetRegEncryptionMetadata[version=" + version
                + ", algorithm=" + algorithm
                + ", encryptedKey=" + Arrays.toString(encryptedKey)
                + ", nonce=" + Arrays.toString(nonce)
                + ", encryptedData=" + Arrays.toString(encryptedData)
                + ", authTag=" + Arrays.toString(authTag) + ']';
    }
}
