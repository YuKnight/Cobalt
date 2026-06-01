package com.github.auties00.cobalt.graphql.web.misc;

import com.alibaba.fastjson2.JSONObject;
import com.github.auties00.cobalt.graphql.web.WhatsAppWebGraphQlOperation;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.model.business.postcode.BusinessPostcodeVerification;
import com.github.auties00.cobalt.model.business.postcode.BusinessPostcodeVerificationBuilder;
import com.github.auties00.cobalt.model.business.postcode.BusinessPostcodeVerificationResult;

import java.util.Optional;

/**
 * Parses the WhatsApp Web GraphQL response of the verify-postcode query built by
 * {@link GraphQlVerifyPostcodeJobWhatsAppWebGraphQlRequest} into a {@link BusinessPostcodeVerification}.
 *
 * <p>Reads the linked root {@code xwa_product_catalog_get_verify_postcode} and flattens its nested
 * {@code postcode_verification_result -> result_code / encrypted_location_name} chain onto the
 * Cobalt domain model: the postcode-verification verdict and, on success, the encrypted
 * servicing-location name.
 *
 * @see GraphQlVerifyPostcodeJobWhatsAppWebGraphQlRequest
 */
@WhatsAppWebModule(moduleName = "WAWebGraphQLVerifyPostcodeJobQuery")
public final class GraphQlVerifyPostcodeJobWhatsAppWebGraphQlResponse implements WhatsAppWebGraphQlOperation.Response {
    /**
     * Holds the parsed postcode verification.
     */
    private final BusinessPostcodeVerification verification;

    /**
     * Constructs a response wrapping the parsed postcode verification.
     *
     * <p>Reserved for the static parser.
     *
     * @param verification the parsed postcode verification, or {@code null} when the relay omitted
     *                     the field
     */
    private GraphQlVerifyPostcodeJobWhatsAppWebGraphQlResponse(BusinessPostcodeVerification verification) {
        this.verification = verification;
    }

    /**
     * Parses the WhatsApp Web GraphQL response from the unwrapped GraphQL {@code data} object.
     *
     * <p>Reads the linked root {@code xwa_product_catalog_get_verify_postcode} and projects it onto
     * a {@link BusinessPostcodeVerification}; the returned {@link Optional} is empty when
     * {@code data} or the verification object is missing.
     *
     * @param data the unwrapped GraphQL {@code data} object returned by
     *             {@link com.github.auties00.cobalt.graphql.web.WhatsAppWebGraphQlClient#send(WhatsAppWebGraphQlOperation.Request)}
     * @return the parsed response, or empty when {@code data} or the verification object is missing
     */
    public static Optional<GraphQlVerifyPostcodeJobWhatsAppWebGraphQlResponse> of(JSONObject data) {
        if (data == null) {
            return Optional.empty();
        }

        var root = data.getJSONObject("xwa_product_catalog_get_verify_postcode");
        if (root == null) {
            return Optional.empty();
        }

        var inner = root.getJSONObject("postcode_verification_result");
        if (inner == null) {
            return Optional.empty();
        }

        var result = BusinessPostcodeVerificationResult.ofRelayResultCode(inner.getString("result_code"))
                .orElse(BusinessPostcodeVerificationResult.INVALID_POSTCODE);
        var verification = new BusinessPostcodeVerificationBuilder()
                .result(result)
                .encryptedLocationName(inner.getString("encrypted_location_name"))
                .build();
        return Optional.of(new GraphQlVerifyPostcodeJobWhatsAppWebGraphQlResponse(verification));
    }

    /**
     * Returns the parsed postcode verification.
     *
     * @return the parsed {@link BusinessPostcodeVerification}, never {@code null}
     */
    public BusinessPostcodeVerification verification() {
        return verification;
    }
}
