package com.github.auties00.cobalt.node.smax.bugreporting;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * Typed projection of a single {@code <media iv cipherKey type?
 * fileName?>{bytes}</media>} attachment carried by a
 * {@link SmaxBugReportingReportBugRequest}.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutBugReportingReportBugRequest")
public final class SmaxBugReportingReportBugMediaUpload {
    /**
     * The 16-byte IV used to encrypt the upload (carried as a
     * {@code CUSTOM_STRING} attribute, but always opaque bytes
     * encoded as base64 / hex).
     */
    private final String mediaIv;

    /**
     * The 32-byte cipher key.
     */
    private final String mediaCipherKey;

    /**
     * The optional MIME type (e.g. {@code "image/jpeg"}).
     */
    private final String mediaType;

    /**
     * The optional original file name.
     */
    private final String mediaFileName;

    /**
     * The encrypted blob carried as the {@code <media>} child's
     * content bytes.
     */
    private final byte[] mediaElementValue;

    /**
     * Constructs a new media upload entry.
     *
     * @param mediaIv             the IV. Never {@code null}
     * @param mediaCipherKey      the cipher key. Never {@code null}
     * @param mediaType           the optional MIME type. May be
     *                            {@code null}
     * @param mediaFileName       the optional file name. May be
     *                            {@code null}
     * @param mediaElementValue   the encrypted blob. Never
     *                            {@code null}
     * @throws NullPointerException if any required argument is
     *                              {@code null}
     */
    public SmaxBugReportingReportBugMediaUpload(String mediaIv, String mediaCipherKey, String mediaType,
                       String mediaFileName, byte[] mediaElementValue) {
        this.mediaIv = Objects.requireNonNull(mediaIv, "mediaIv cannot be null");
        this.mediaCipherKey = Objects.requireNonNull(mediaCipherKey, "mediaCipherKey cannot be null");
        this.mediaType = mediaType;
        this.mediaFileName = mediaFileName;
        this.mediaElementValue = Objects.requireNonNull(mediaElementValue, "mediaElementValue cannot be null");
    }

    /**
     * Returns the IV.
     *
     * @return the IV. Never {@code null}
     */
    public String mediaIv() {
        return mediaIv;
    }

    /**
     * Returns the cipher key.
     *
     * @return the key. Never {@code null}
     */
    public String mediaCipherKey() {
        return mediaCipherKey;
    }

    /**
     * Returns the optional MIME type.
     *
     * @return an {@link Optional} carrying the MIME type
     */
    public Optional<String> mediaType() {
        return Optional.ofNullable(mediaType);
    }

    /**
     * Returns the optional file name.
     *
     * @return an {@link Optional} carrying the file name
     */
    public Optional<String> mediaFileName() {
        return Optional.ofNullable(mediaFileName);
    }

    /**
     * Returns the encrypted blob.
     *
     * @return the bytes. Never {@code null}
     */
    public byte[] mediaElementValue() {
        return mediaElementValue;
    }

    /**
     * Builds the {@code <media iv cipherKey type? fileName?>{bytes}</media>}
     * child node.
     *
     * @return the {@link Node}
     */
    @WhatsAppWebExport(moduleName = "WASmaxOutBugReportingReportBugRequest",
            exports = "makeReportBugRequestMedia",
            adaptation = WhatsAppAdaptation.DIRECT)
    public Node toNode() {
        var builder = new NodeBuilder()
                .description("media")
                .attribute("iv", mediaIv)
                .attribute("cipherKey", mediaCipherKey)
                .content(mediaElementValue);
        if (mediaType != null) {
            builder.attribute("type", mediaType);
        }
        if (mediaFileName != null) {
            builder.attribute("fileName", mediaFileName);
        }
        return builder.build();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxBugReportingReportBugMediaUpload) obj;
        return Objects.equals(this.mediaIv, that.mediaIv)
                && Objects.equals(this.mediaCipherKey, that.mediaCipherKey)
                && Objects.equals(this.mediaType, that.mediaType)
                && Objects.equals(this.mediaFileName, that.mediaFileName)
                && Arrays.equals(this.mediaElementValue, that.mediaElementValue);
    }

    @Override
    public int hashCode() {
        var result = Objects.hash(mediaIv, mediaCipherKey, mediaType, mediaFileName);
        result = 31 * result + Arrays.hashCode(mediaElementValue);
        return result;
    }

    @Override
    public String toString() {
        return "SmaxBugReportingReportBugMediaUpload[mediaIv=" + mediaIv
                + ", mediaCipherKey=" + mediaCipherKey
                + ", mediaType=" + mediaType
                + ", mediaFileName=" + mediaFileName
                + ", mediaElementValue=" + Arrays.toString(mediaElementValue) + ']';
    }
}
