package com.github.auties00.cobalt.graphql.web.auth;

import com.alibaba.fastjson2.JSONObject;
import com.github.auties00.cobalt.graphql.web.WhatsAppWebGraphQlOperation;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.model.business.auth.BusinessSignupMetadata;
import com.github.auties00.cobalt.model.business.auth.BusinessSignupMetadataBuilder;

import java.util.Optional;

/**
 * Parses the WhatsApp Web GraphQL response of the WhatsApp Business signup-metadata query built by
 * {@link SignupMetadataWhatsAppWebGraphQlRequest} into a {@link BusinessSignupMetadata}.
 *
 * <p>Reads the linked root {@code wa_signup_metadata} and projects its scalars (server-issued signup
 * identifier, consent message, and privacy-policy URL) onto the Cobalt domain model. WhatsApp Web
 * treats the reply as invalid when either the id or the message is missing.
 *
 * @see SignupMetadataWhatsAppWebGraphQlRequest
 */
@WhatsAppWebModule(moduleName = "WAWebSignupMetadataQuery")
public final class SignupMetadataWhatsAppWebGraphQlResponse implements WhatsAppWebGraphQlOperation.Response {
    /**
     * Holds the parsed signup metadata.
     */
    private final BusinessSignupMetadata metadata;

    /**
     * Constructs a response wrapping the parsed signup metadata.
     *
     * <p>Reserved for the static parser.
     *
     * @param metadata the parsed signup metadata, or {@code null} when the relay omitted the field
     */
    private SignupMetadataWhatsAppWebGraphQlResponse(BusinessSignupMetadata metadata) {
        this.metadata = metadata;
    }

    /**
     * Parses the WhatsApp Web GraphQL response from the unwrapped GraphQL {@code data} object.
     *
     * <p>Reads the linked root {@code wa_signup_metadata} and projects it onto a
     * {@link BusinessSignupMetadata}; the returned {@link Optional} is empty when {@code data} or
     * the metadata object is missing.
     *
     * @param data the unwrapped GraphQL {@code data} object returned by
     *             {@link com.github.auties00.cobalt.graphql.web.WhatsAppWebGraphQlClient#send(WhatsAppWebGraphQlOperation.Request)}
     * @return the parsed response, or empty when {@code data} or the metadata object is missing
     */
    public static Optional<SignupMetadataWhatsAppWebGraphQlResponse> of(JSONObject data) {
        if (data == null) {
            return Optional.empty();
        }

        var node = data.getJSONObject("wa_signup_metadata");
        if (node == null) {
            return Optional.empty();
        }

        var metadata = new BusinessSignupMetadataBuilder()
                .id(node.getString("id"))
                .signupMessage(node.getString("signup_message"))
                .privacyPolicyUrl(node.getString("privacy_policy_url"))
                .build();
        return Optional.of(new SignupMetadataWhatsAppWebGraphQlResponse(metadata));
    }

    /**
     * Returns the parsed signup metadata.
     *
     * @return the parsed {@link BusinessSignupMetadata}, never {@code null}
     */
    public BusinessSignupMetadata metadata() {
        return metadata;
    }
}
