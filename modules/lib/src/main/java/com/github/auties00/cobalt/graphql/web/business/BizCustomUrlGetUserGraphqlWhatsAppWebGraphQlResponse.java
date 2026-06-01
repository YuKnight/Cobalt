package com.github.auties00.cobalt.graphql.web.business;

import com.alibaba.fastjson2.JSONObject;
import com.github.auties00.cobalt.graphql.web.WhatsAppWebGraphQlOperation;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.business.profile.BusinessCustomUrlIdentity;
import com.github.auties00.cobalt.model.business.profile.BusinessCustomUrlIdentityBuilder;
import com.github.auties00.cobalt.model.jid.Jid;

import java.util.Optional;

/**
 * Parses the WhatsApp Web GraphQL response of the custom-url resolution query built by
 * {@link BizCustomUrlGetUserGraphqlWhatsAppWebGraphQlRequest} into a {@link BusinessCustomUrlIdentity}.
 *
 * <p>Reads the linked {@code xwa_custom_url_get_user} field and projects its {@code success} flag, the
 * resolved owner {@code lid}, and the {@code error_code} plus {@code error_text} failure pair onto the
 * Cobalt domain model. This query resolves the slug to the owner's privacy-preserving alternate id.
 *
 * @see BizCustomUrlGetUserGraphqlWhatsAppWebGraphQlRequest
 */
@WhatsAppWebModule(moduleName = "WAWebBizCustomUrlGetUserGraphqlQuery")
public final class BizCustomUrlGetUserGraphqlWhatsAppWebGraphQlResponse implements WhatsAppWebGraphQlOperation.Response {
    /**
     * Holds the parsed resolution outcome.
     */
    private final BusinessCustomUrlIdentity identity;

    /**
     * Constructs a response wrapping the parsed resolution outcome.
     *
     * <p>Reserved for the static parser.
     *
     * @param identity the parsed resolution outcome, or {@code null} when the relay omitted the field
     */
    private BizCustomUrlGetUserGraphqlWhatsAppWebGraphQlResponse(BusinessCustomUrlIdentity identity) {
        this.identity = identity;
    }

    /**
     * Parses the WhatsApp Web GraphQL response from the unwrapped GraphQL {@code data} object.
     *
     * <p>Reads the linked root {@code xwa_custom_url_get_user} and projects its resolution flag, the
     * resolved owner {@code lid} as the linked identifier, and the failure pair onto a
     * {@link BusinessCustomUrlIdentity}; the returned {@link Optional} is empty when {@code data} or the
     * resolution field is missing.
     *
     * @param data the unwrapped GraphQL {@code data} object returned by
     *             {@link com.github.auties00.cobalt.graphql.web.WhatsAppWebGraphQlClient#send(WhatsAppWebGraphQlOperation.Request)}
     * @return the parsed response, or empty when {@code data} or the resolution field is missing
     */
    @WhatsAppWebExport(moduleName = "WAWebBizCustomUrlGetUserGraphqlQuery", exports = "default",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public static Optional<BizCustomUrlGetUserGraphqlWhatsAppWebGraphQlResponse> of(JSONObject data) {
        if (data == null) {
            return Optional.empty();
        }
        var root = data.getJSONObject("xwa_custom_url_get_user");
        if (root == null) {
            return Optional.empty();
        }
        var success = root.getBoolean("success");
        var lidString = root.getString("lid");
        var lid = lidString == null ? null : Jid.of(lidString);
        var identity = new BusinessCustomUrlIdentityBuilder()
                .resolved(success != null && success)
                .linkedIdentifier(lid)
                .errorCode(root.getString("error_code"))
                .errorText(root.getString("error_text"))
                .build();
        return Optional.of(new BizCustomUrlGetUserGraphqlWhatsAppWebGraphQlResponse(identity));
    }

    /**
     * Returns the parsed resolution outcome.
     *
     * <p>The returned {@link BusinessCustomUrlIdentity} reports whether the slug resolved and, on
     * success, carries the owner's privacy-preserving alternate identifier.
     *
     * @return the parsed resolution outcome, never {@code null}
     */
    public BusinessCustomUrlIdentity identity() {
        return identity;
    }
}
