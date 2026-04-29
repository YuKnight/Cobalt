package com.github.auties00.cobalt.node.smax.profilepicture;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqResultResponseMixin;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The optional {@code <smax$any><tctoken t?>{any}</tctoken></smax$any>}
 * payload carried by a {@link SmaxProfilePictureGetRequest}.
 *
 * @implNote {@code WASmaxOutProfilePictureTCTokenMixin.mergeTCTokenMixin}
 *           wraps the {@code <tctoken/>} element under a
 *           {@code <smax$any/>} placeholder which itself nests a
 *           {@code <smax$any/>} grandchild carrying the privacy-token
 *           contents bytes.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutProfilePictureTCTokenMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutProfilePicturePrivacyTokenContentsMixin")
public final class SmaxProfilePictureGetTcTokenMixin {
    /**
     * The optional {@code t} timestamp on the {@code <tctoken/>}
     * element.
     */
    private final Long tctokenT;

    /**
     * The privacy-token contents bytes.
     */
    private final byte[] anyElementValue;

    /**
     * Constructs a new tctoken payload.
     *
     * @param tctokenT        the optional timestamp; may be
     *                        {@code null}
     * @param anyElementValue the contents bytes; never {@code null}
     * @throws NullPointerException if {@code anyElementValue} is
     *                              {@code null}
     */
    public SmaxProfilePictureGetTcTokenMixin(Long tctokenT, byte[] anyElementValue) {
        this.tctokenT = tctokenT;
        this.anyElementValue = Objects.requireNonNull(anyElementValue, "anyElementValue cannot be null");
    }

    /**
     * Returns the optional timestamp.
     *
     * @return an {@link Optional} carrying the timestamp
     */
    public Optional<Long> tctokenT() {
        return Optional.ofNullable(tctokenT);
    }

    /**
     * Returns the contents bytes.
     *
     * @return the bytes; never {@code null}
     */
    public byte[] anyElementValue() {
        return anyElementValue;
    }

    /**
     * Builds the {@code <smax$any/>} wrapper node.
     *
     * @return the {@link Node}
     */
    @WhatsAppWebExport(moduleName = "WASmaxOutProfilePictureTCTokenMixin",
            exports = "mergeTCTokenMixin",
            adaptation = WhatsAppAdaptation.DIRECT)
    public Node toNode() {
        // <tctoken t?>{anyValue}</tctoken>
        var tctokenBuilder = new NodeBuilder()
                .description("tctoken")
                .content(anyElementValue);
        if (tctokenT != null) {
            tctokenBuilder.attribute("t", tctokenT);
        }
        // <smax$any>{tctoken}</smax$any>
        return new NodeBuilder()
                .description("smax$any")
                .content(tctokenBuilder.build())
                .build();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxProfilePictureGetTcTokenMixin) obj;
        return Objects.equals(this.tctokenT, that.tctokenT)
                && Arrays.equals(this.anyElementValue, that.anyElementValue);
    }

    @Override
    public int hashCode() {
        var result = Objects.hash(tctokenT);
        result = 31 * result + Arrays.hashCode(anyElementValue);
        return result;
    }

    @Override
    public String toString() {
        return "SmaxProfilePictureGetTcTokenMixin[tctokenT=" + tctokenT
                + ", anyElementValue=" + Arrays.toString(anyElementValue) + ']';
    }
}
