package com.github.auties00.cobalt.node.smax.waffle;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * Typed projection of the {@code <encryption_metadata version="1"
 * algorithm="rsa2048">} subtree shared by every {@code WASmaxOut/InWaffle*}
 * RPC that exchanges encrypted payloads.
 *
 * <p>The mixin carries four opaque byte blobs. The RSA-2048 wrapped
 * symmetric key, the AES-GCM nonce, the AES-GCM ciphertext, and the
 * AES-GCM authentication tag. Cobalt collapses the WA Web {@code
 * WASmaxOutWaffleRSAEncryptionMetadataMixin}/{@code
 * WASmaxInWaffleRSAEncryptionMetadataMixin} pair into the single value
 * class here; every Waffle RPC that needs the mixin embeds an instance.
 *
 * @implNote {@code WASmaxOutWaffleRSAEncryptionMetadataMixin.mergeRSAEncryptionMetadataMixin}
 *           composes {@code <encryption_metadata version="1"
 *           algorithm="rsa2048">} carrying four content-bytes children;
 *           {@code WASmaxInWaffleRSAEncryptionMetadataMixin.parseRSAEncryptionMetadataMixin}
 *           projects the same shape on the inbound side. Cobalt keeps the
 *           four byte arrays as package-private fields to mirror the WA
 *           Web call sites' field-access patterns.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutWaffleRSAEncryptionMetadataMixin")
@WhatsAppWebModule(moduleName = "WASmaxInWaffleRSAEncryptionMetadataMixin")
public final class SmaxWaffleRsaEncryptionMetadata {
    /**
     * The RSA-wrapped symmetric key.
     */
    private final byte[] encryptedKey;

    /**
     * The AES-GCM nonce.
     */
    private final byte[] nonce;

    /**
     * The AES-GCM ciphertext.
     */
    private final byte[] encryptedData;

    /**
     * The AES-GCM authentication tag.
     */
    private final byte[] authTag;

    /**
     * Constructs a new mixin instance.
     *
     * @param encryptedKey  the RSA-wrapped symmetric key; never
     *                      {@code null}
     * @param nonce         the AES-GCM nonce; never {@code null}
     * @param encryptedData the AES-GCM ciphertext; never {@code null}
     * @param authTag       the AES-GCM authentication tag; never
     *                      {@code null}
     * @throws NullPointerException if any argument is {@code null}
     */
    public SmaxWaffleRsaEncryptionMetadata(byte[] encryptedKey, byte[] nonce,
                                           byte[] encryptedData, byte[] authTag) {
        this.encryptedKey = Objects.requireNonNull(encryptedKey, "encryptedKey cannot be null");
        this.nonce = Objects.requireNonNull(nonce, "nonce cannot be null");
        this.encryptedData = Objects.requireNonNull(encryptedData, "encryptedData cannot be null");
        this.authTag = Objects.requireNonNull(authTag, "authTag cannot be null");
    }

    /**
     * Returns the RSA-wrapped symmetric key.
     *
     * @return the wrapped key bytes; never {@code null}
     */
    public byte[] encryptedKey() {
        return encryptedKey;
    }

    /**
     * Returns the AES-GCM nonce.
     *
     * @return the nonce bytes; never {@code null}
     */
    public byte[] nonce() {
        return nonce;
    }

    /**
     * Returns the AES-GCM ciphertext.
     *
     * @return the ciphertext bytes; never {@code null}
     */
    public byte[] encryptedData() {
        return encryptedData;
    }

    /**
     * Returns the AES-GCM authentication tag.
     *
     * @return the tag bytes; never {@code null}
     */
    public byte[] authTag() {
        return authTag;
    }

    /**
     * Builds the {@code <encryption_metadata/>} subtree carrying the four
     * encrypted blobs.
     *
     * @return the {@code <encryption_metadata/>} {@link Node}; never
     *         {@code null}
     */
    @WhatsAppWebExport(moduleName = "WASmaxOutWaffleRSAEncryptionMetadataMixin",
            exports = "mergeRSAEncryptionMetadataMixin", adaptation = WhatsAppAdaptation.DIRECT)
    public Node toNode() {
        var encryptedKeyNode = new NodeBuilder()
                .description("encrypted_key")
                .content(encryptedKey)
                .build();
        var nonceNode = new NodeBuilder()
                .description("nonce")
                .content(nonce)
                .build();
        var encryptedDataNode = new NodeBuilder()
                .description("encrypted_data")
                .content(encryptedData)
                .build();
        var authTagNode = new NodeBuilder()
                .description("auth_tag")
                .content(authTag)
                .build();
        return new NodeBuilder()
                .description("encryption_metadata")
                .attribute("version", "1")
                .attribute("algorithm", "rsa2048")
                .content(encryptedKeyNode, nonceNode, encryptedDataNode, authTagNode)
                .build();
    }

    /**
     * Tries to parse a {@link SmaxWaffleRsaEncryptionMetadata} from the
     * given {@code <encryption_metadata/>} subtree.
     *
     * @param node the {@code <encryption_metadata/>} stanza; never
     *             {@code null}
     * @return an {@link Optional} carrying the parsed mixin, or empty
     *         when the stanza doesn't match the expected shape
     * @throws NullPointerException if {@code node} is {@code null}
     *
     * @implNote {@code WASmaxInWaffleRSAEncryptionMetadataMixin.parseRSAEncryptionMetadataMixin}
     *           runs the same checks plus content-bytes range assertions
     *           ({@code [1, 2048]} for {@code encrypted_key},
     *           {@code [1, 128]} for {@code nonce} and {@code auth_tag},
     *           {@code [1, 8192]} for {@code encrypted_data}); Cobalt
     *           drops the size assertions because every consumer
     *           cryptographically validates the blobs anyway.
     */
    @WhatsAppWebExport(moduleName = "WASmaxInWaffleRSAEncryptionMetadataMixin",
            exports = "parseRSAEncryptionMetadataMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    public static Optional<SmaxWaffleRsaEncryptionMetadata> of(Node node) {
        Objects.requireNonNull(node, "node cannot be null");
        if (!node.hasAttribute("version", "1")) {
            return Optional.empty();
        }
        if (!node.hasAttribute("algorithm", "rsa2048")) {
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
        return Optional.of(new SmaxWaffleRsaEncryptionMetadata(encryptedKey, nonce, encryptedData, authTag));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxWaffleRsaEncryptionMetadata) obj;
        return Arrays.equals(this.encryptedKey, that.encryptedKey)
                && Arrays.equals(this.nonce, that.nonce)
                && Arrays.equals(this.encryptedData, that.encryptedData)
                && Arrays.equals(this.authTag, that.authTag);
    }

    @Override
    public int hashCode() {
        var result = Arrays.hashCode(encryptedKey);
        result = 31 * result + Arrays.hashCode(nonce);
        result = 31 * result + Arrays.hashCode(encryptedData);
        result = 31 * result + Arrays.hashCode(authTag);
        return result;
    }

    @Override
    public String toString() {
        return "SmaxWaffleRsaEncryptionMetadata[encryptedKey="
                + (encryptedKey != null ? encryptedKey.length + " bytes" : "null")
                + ", nonce="
                + (nonce != null ? nonce.length + " bytes" : "null")
                + ", encryptedData="
                + (encryptedData != null ? encryptedData.length + " bytes" : "null")
                + ", authTag="
                + (authTag != null ? authTag.length + " bytes" : "null") + ']';
    }
}
