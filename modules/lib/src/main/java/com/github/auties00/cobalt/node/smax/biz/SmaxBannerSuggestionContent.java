package com.github.auties00.cobalt.node.smax.biz;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.node.Node;
import java.util.Objects;
import java.util.Optional;

/**
 * The {@code <content/>} projection. Banner copy plus locale and
 * optional localised parallels.
 */
@WhatsAppWebModule(moduleName = "WASmaxInBizCtwaActionBannerSuggestionRequest")
public final class SmaxBannerSuggestionContent {
    /**
     * The mandatory {@code locale} attribute.
     */
    private final String locale;

    /**
     * The mandatory {@code <heading/>} element-content.
     */
    private final String heading;

    /**
     * The mandatory {@code <body/>} element-content.
     */
    private final String body;

    /**
     * The mandatory {@code <highlight/>} element-content.
     */
    private final String highlight;

    /**
     * The optional {@code <localised_heading/>} projection.
     */
    private final SmaxBannerSuggestionLocalisedString localisedHeading;

    /**
     * The optional {@code <localised_body/>} projection.
     */
    private final SmaxBannerSuggestionLocalisedString localisedBody;

    /**
     * The optional {@code <localised_highlight/>} projection.
     */
    private final SmaxBannerSuggestionLocalisedString localisedHighlight;

    /**
     * Constructs a new content projection.
     *
     * @param locale             the locale; never {@code null}
     * @param heading            the heading; never {@code null}
     * @param body               the body; never {@code null}
     * @param highlight          the highlight; never {@code null}
     * @param localisedHeading   the optional localised heading; may
     *                           be {@code null}
     * @param localisedBody      the optional localised body; may be
     *                           {@code null}
     * @param localisedHighlight the optional localised highlight;
     *                           may be {@code null}
     * @throws NullPointerException if any of the four mandatory
     *                              arguments is {@code null}
     */
    public SmaxBannerSuggestionContent(String locale, String heading, String body, String highlight,
                   SmaxBannerSuggestionLocalisedString localisedHeading, SmaxBannerSuggestionLocalisedString localisedBody,
                   SmaxBannerSuggestionLocalisedString localisedHighlight) {
        this.locale = Objects.requireNonNull(locale, "locale cannot be null");
        this.heading = Objects.requireNonNull(heading, "heading cannot be null");
        this.body = Objects.requireNonNull(body, "body cannot be null");
        this.highlight = Objects.requireNonNull(highlight, "highlight cannot be null");
        this.localisedHeading = localisedHeading;
        this.localisedBody = localisedBody;
        this.localisedHighlight = localisedHighlight;
    }

    /**
     * Returns the locale.
     *
     * @return the locale; never {@code null}
     */
    public String locale() {
        return locale;
    }

    /**
     * Returns the heading.
     *
     * @return the heading; never {@code null}
     */
    public String heading() {
        return heading;
    }

    /**
     * Returns the body.
     *
     * @return the body; never {@code null}
     */
    public String body() {
        return body;
    }

    /**
     * Returns the highlight.
     *
     * @return the highlight; never {@code null}
     */
    public String highlight() {
        return highlight;
    }

    /**
     * Returns the optional localised heading.
     *
     * @return an {@link Optional} carrying the projection, or empty
     */
    public Optional<SmaxBannerSuggestionLocalisedString> localisedHeading() {
        return Optional.ofNullable(localisedHeading);
    }

    /**
     * Returns the optional localised body.
     *
     * @return an {@link Optional} carrying the projection, or empty
     */
    public Optional<SmaxBannerSuggestionLocalisedString> localisedBody() {
        return Optional.ofNullable(localisedBody);
    }

    /**
     * Returns the optional localised highlight.
     *
     * @return an {@link Optional} carrying the projection, or empty
     */
    public Optional<SmaxBannerSuggestionLocalisedString> localisedHighlight() {
        return Optional.ofNullable(localisedHighlight);
    }

    /**
     * Tries to parse the projection from the given node.
     *
     * @param node the {@code <content/>} node
     * @return an {@link Optional} carrying the projection, or empty
     *         when the node does not match the documented schema
     */
    public static Optional<SmaxBannerSuggestionContent> of(Node node) {
        Objects.requireNonNull(node, "node cannot be null");
        if (!node.hasDescription("content")) {
            return Optional.empty();
        }
        var locale = node.getAttributeAsString("locale").orElse(null);
        if (locale == null) {
            return Optional.empty();
        }
        var headingNode = node.getChild("heading").orElse(null);
        if (headingNode == null) {
            return Optional.empty();
        }
        var heading = headingNode.toContentString().orElse(null);
        if (heading == null) {
            return Optional.empty();
        }
        var bodyNode = node.getChild("body").orElse(null);
        if (bodyNode == null) {
            return Optional.empty();
        }
        var body = bodyNode.toContentString().orElse(null);
        if (body == null) {
            return Optional.empty();
        }
        var highlightNode = node.getChild("highlight").orElse(null);
        if (highlightNode == null) {
            return Optional.empty();
        }
        var highlight = highlightNode.toContentString().orElse(null);
        if (highlight == null) {
            return Optional.empty();
        }
        SmaxBannerSuggestionLocalisedString localisedHeading = null;
        var localisedHeadingNode = node.getChild("localised_heading").orElse(null);
        if (localisedHeadingNode != null) {
            var parsed = SmaxBannerSuggestionLocalisedString.of(localisedHeadingNode, "localised_heading");
            if (parsed.isEmpty()) {
                return Optional.empty();
            }
            localisedHeading = parsed.get();
        }
        SmaxBannerSuggestionLocalisedString localisedBody = null;
        var localisedBodyNode = node.getChild("localised_body").orElse(null);
        if (localisedBodyNode != null) {
            var parsed = SmaxBannerSuggestionLocalisedString.of(localisedBodyNode, "localised_body");
            if (parsed.isEmpty()) {
                return Optional.empty();
            }
            localisedBody = parsed.get();
        }
        SmaxBannerSuggestionLocalisedString localisedHighlight = null;
        var localisedHighlightNode = node.getChild("localised_highlight").orElse(null);
        if (localisedHighlightNode != null) {
            var parsed = SmaxBannerSuggestionLocalisedString.of(localisedHighlightNode, "localised_highlight");
            if (parsed.isEmpty()) {
                return Optional.empty();
            }
            localisedHighlight = parsed.get();
        }
        return Optional.of(new SmaxBannerSuggestionContent(locale, heading, body, highlight,
                localisedHeading, localisedBody, localisedHighlight));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxBannerSuggestionContent) obj;
        return Objects.equals(this.locale, that.locale)
                && Objects.equals(this.heading, that.heading)
                && Objects.equals(this.body, that.body)
                && Objects.equals(this.highlight, that.highlight)
                && Objects.equals(this.localisedHeading, that.localisedHeading)
                && Objects.equals(this.localisedBody, that.localisedBody)
                && Objects.equals(this.localisedHighlight, that.localisedHighlight);
    }

    @Override
    public int hashCode() {
        return Objects.hash(locale, heading, body, highlight,
                localisedHeading, localisedBody, localisedHighlight);
    }

    @Override
    public String toString() {
        return "SmaxBannerSuggestionContent[locale=" + locale
                + ", heading=" + heading
                + ", body=" + body
                + ", highlight=" + highlight
                + ", localisedHeading=" + localisedHeading
                + ", localisedBody=" + localisedBody
                + ", localisedHighlight=" + localisedHighlight + ']';
    }
}
