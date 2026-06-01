package com.github.auties00.cobalt.model.business.profile;

import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

import java.util.Objects;
import java.util.Optional;

/**
 * Input model for the postal-address typeahead of the WhatsApp Business
 * profile editor.
 *
 * <p>When a merchant edits a Business profile and types the registered
 * place of operation, the address picker streams place suggestions ranked
 * by relevance to the partial text. This input carries the parameters that
 * select which suggestions the server returns: the partial-address
 * {@link #query() query text}, an optional {@link #locale() locale} so the
 * response is translated and sorted for the right language, and an
 * optional {@link #country() country bias} so suggestions in the merchant's
 * country come first.
 *
 * <p>Field shape: the {@code query}, {@code locale} and {@code country}
 * fields are derived from WhatsApp Business address-autocomplete
 * conventions. They are best-guess and may not cover every server-accepted
 * field; additional optional fields will be added as they surface in live
 * request captures.
 */
@ProtobufMessage(name = "BusinessAddressAutocompleteQuery")
public final class BusinessAddressAutocompleteQuery {
    /**
     * Partial address text the merchant has typed so far. The server uses
     * this text to retrieve and rank place suggestions. Required by the
     * autocomplete backend; an unset value omits the variable and yields
     * an empty suggestion list.
     */
    @ProtobufProperty(index = 1, type = ProtobufType.STRING)
    final String query;

    /**
     * IETF BCP 47 language tag (for example {@code en-US}, {@code pt-BR})
     * the server should localise suggestions to. Unset omits the variable
     * so the server applies its default locale.
     */
    @ProtobufProperty(index = 2, type = ProtobufType.STRING)
    final String locale;

    /**
     * ISO 3166-1 alpha-2 country code (for example {@code IN}, {@code BR})
     * to bias the suggestions toward. Unset omits the variable so the
     * server does not apply a country bias.
     */
    @ProtobufProperty(index = 3, type = ProtobufType.STRING)
    final String country;

    /**
     * Constructs a new {@code BusinessAddressAutocompleteQuery}. Every
     * argument may be {@code null} to omit the corresponding variable from
     * the request.
     *
     * @param query   the partial address text, or {@code null}
     * @param locale  the BCP 47 language tag, or {@code null}
     * @param country the ISO 3166-1 alpha-2 country code, or {@code null}
     */
    public BusinessAddressAutocompleteQuery(String query, String locale, String country) {
        this.query = query;
        this.locale = locale;
        this.country = country;
    }

    /**
     * Returns the partial address text the merchant has typed.
     *
     * @return an {@link Optional} carrying the query text, or empty when
     *         unset
     */
    public Optional<String> query() {
        return Optional.ofNullable(query);
    }

    /**
     * Returns the BCP 47 language tag the response should be localised to.
     *
     * @return an {@link Optional} carrying the locale, or empty when unset
     */
    public Optional<String> locale() {
        return Optional.ofNullable(locale);
    }

    /**
     * Returns the ISO 3166-1 alpha-2 country code biasing the suggestions.
     *
     * @return an {@link Optional} carrying the country code, or empty when
     *         unset
     */
    public Optional<String> country() {
        return Optional.ofNullable(country);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (BusinessAddressAutocompleteQuery) obj;
        return Objects.equals(query, that.query)
                && Objects.equals(locale, that.locale)
                && Objects.equals(country, that.country);
    }

    @Override
    public int hashCode() {
        return Objects.hash(query, locale, country);
    }

    @Override
    public String toString() {
        return "BusinessAddressAutocompleteQuery[" +
                "query=" + query + ", " +
                "locale=" + locale + ", " +
                "country=" + country + ']';
    }
}
