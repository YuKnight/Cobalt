package com.github.auties00.cobalt.node.smax.biz;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * The {@code <localised_*>} projection — value plus localisation
 * metadata. Used by all three of {@code <localised_heading/>},
 * {@code <localised_body/>} and {@code <localised_highlight/>}.
 */
@WhatsAppWebModule(moduleName = "WASmaxInBizCtwaActionBannerSuggestionRequest")
public final class SmaxBannerSuggestionLocalisedString {
    /**
     * The {@code value} attribute — the localised string.
     */
    private final String value;

    /**
     * The mandatory localisation-metadata projection.
     */
    private final SmaxBannerSuggestionLocalisationMetadata localisationMetadata;

    /**
     * Constructs a new localised-string projection.
     *
     * @param value                the localised string; never
     *                             {@code null}
     * @param localisationMetadata the metadata projection; never
     *                             {@code null}
     * @throws NullPointerException if either argument is
     *                              {@code null}
     */
    public SmaxBannerSuggestionLocalisedString(String value, SmaxBannerSuggestionLocalisationMetadata localisationMetadata) {
        this.value = Objects.requireNonNull(value, "value cannot be null");
        this.localisationMetadata = Objects.requireNonNull(localisationMetadata,
                "localisationMetadata cannot be null");
    }

    /**
     * Returns the localised string.
     *
     * @return the value; never {@code null}
     */
    public String value() {
        return value;
    }

    /**
     * Returns the localisation metadata.
     *
     * @return the metadata; never {@code null}
     */
    public SmaxBannerSuggestionLocalisationMetadata localisationMetadata() {
        return localisationMetadata;
    }

    /**
     * Tries to parse the projection from the given node.
     *
     * @param node        the {@code <localised_*>} node
     * @param expectedTag the expected tag name (one of
     *                    {@code "localised_heading"},
     *                    {@code "localised_body"},
     *                    {@code "localised_highlight"})
     * @return an {@link Optional} carrying the projection, or empty
     *         when the node does not match the documented schema
     */
    public static Optional<SmaxBannerSuggestionLocalisedString> of(Node node, String expectedTag) {
        Objects.requireNonNull(node, "node cannot be null");
        Objects.requireNonNull(expectedTag, "expectedTag cannot be null");
        if (!node.hasDescription(expectedTag)) {
            return Optional.empty();
        }
        var value = node.getAttributeAsString("value").orElse(null);
        if (value == null) {
            return Optional.empty();
        }
        var metadataNode = node.getChild("localisation_metadata").orElse(null);
        if (metadataNode == null) {
            return Optional.empty();
        }
        var metadata = SmaxBannerSuggestionLocalisationMetadata.of(metadataNode).orElse(null);
        if (metadata == null) {
            return Optional.empty();
        }
        return Optional.of(new SmaxBannerSuggestionLocalisedString(value, metadata));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxBannerSuggestionLocalisedString) obj;
        return Objects.equals(this.value, that.value)
                && Objects.equals(this.localisationMetadata, that.localisationMetadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, localisationMetadata);
    }

    @Override
    public String toString() {
        return "SmaxBannerSuggestionLocalisedString[value=" + value
                + ", localisationMetadata=" + localisationMetadata + ']';
    }
}
