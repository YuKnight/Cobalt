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
 * The optional {@code <add_request code admin? expiration/>}
 * payload carried by a {@link SmaxProfilePictureGetRequest}.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutProfilePictureAddRequestMixin")
public final class SmaxProfilePictureGetAddRequestMixin {
    /**
     * The mandatory {@code code} attribute.
     */
    private final String addRequestCode;

    /**
     * The optional {@code admin} attribute (a user JID).
     */
    private final Jid addRequestAdmin;

    /**
     * The mandatory {@code expiration} attribute.
     */
    private final long addRequestExpiration;

    /**
     * Constructs a new add-request payload.
     *
     * @param addRequestCode       the code; never {@code null}
     * @param addRequestAdmin      the optional admin JID; may be
     *                             {@code null}
     * @param addRequestExpiration the expiration timestamp
     * @throws NullPointerException if {@code addRequestCode} is
     *                              {@code null}
     */
    public SmaxProfilePictureGetAddRequestMixin(String addRequestCode, Jid addRequestAdmin, long addRequestExpiration) {
        this.addRequestCode = Objects.requireNonNull(addRequestCode, "addRequestCode cannot be null");
        this.addRequestAdmin = addRequestAdmin;
        this.addRequestExpiration = addRequestExpiration;
    }

    /**
     * Returns the code.
     *
     * @return the code; never {@code null}
     */
    public String addRequestCode() {
        return addRequestCode;
    }

    /**
     * Returns the optional admin JID.
     *
     * @return an {@link Optional} carrying the JID
     */
    public Optional<Jid> addRequestAdmin() {
        return Optional.ofNullable(addRequestAdmin);
    }

    /**
     * Returns the expiration timestamp.
     *
     * @return the timestamp
     */
    public long addRequestExpiration() {
        return addRequestExpiration;
    }

    /**
     * Builds the {@code <add_request>} child node.
     *
     * @return the {@link Node}
     */
    @WhatsAppWebExport(moduleName = "WASmaxOutProfilePictureAddRequestMixin",
            exports = "mergeAddRequestMixin",
            adaptation = WhatsAppAdaptation.DIRECT)
    public Node toNode() {
        var builder = new NodeBuilder()
                .description("add_request")
                .attribute("code", addRequestCode)
                .attribute("expiration", addRequestExpiration);
        if (addRequestAdmin != null) {
            builder.attribute("admin", addRequestAdmin);
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
        var that = (SmaxProfilePictureGetAddRequestMixin) obj;
        return this.addRequestExpiration == that.addRequestExpiration
                && Objects.equals(this.addRequestCode, that.addRequestCode)
                && Objects.equals(this.addRequestAdmin, that.addRequestAdmin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(addRequestCode, addRequestAdmin, addRequestExpiration);
    }

    @Override
    public String toString() {
        return "SmaxProfilePictureGetAddRequestMixin[addRequestCode=" + addRequestCode
                + ", addRequestAdmin=" + addRequestAdmin
                + ", addRequestExpiration=" + addRequestExpiration + ']';
    }
}
