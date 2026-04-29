package com.github.auties00.cobalt.node.mex.json.newsletter;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.node.mex.MexOperation;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.util.Optional;

/**
 * The response variant of {@link AcceptNewsletterAdminInviteMexResponse} that
 * exposes the identifier echoed back by the server once the invite is
 * accepted.
 *
 * @implNote WAWebMexAcceptNewsletterAdminInviteJob: adapts the return
 * value of the GraphQL mutation, which is the object keyed under
 * {@code xwa2_newsletter_admin_invite_accept} in the JSON response body.
 */
@WhatsAppWebModule(moduleName = "WAWebMexAcceptNewsletterAdminInviteJob")
public final class AcceptNewsletterAdminInviteMexResponse implements MexOperation.Response.Json {
    /**
     * The identifier of the newsletter whose admin invite was accepted, as
     * echoed by the server.
     */
    private final String id;

    /**
     * Creates a response carrying the newsletter identifier returned by
     * the server.
     *
     * @param id the newsletter identifier echoed by the server
     */
    private AcceptNewsletterAdminInviteMexResponse(String id) {
        this.id = id;
    }

    /**
     * Parses a MEX response from the given IQ response node.
     *
     * <p>The IQ result contains a {@code <result>} child whose binary
     * content carries the JSON reply. The reply is expected to contain a
     * {@code data.xwa2_newsletter_admin_invite_accept} object with a
     * single {@code id} field.
     *
     * @implNote WAWebMexAcceptNewsletterAdminInviteJob.acceptNewsletterAdminInvite:
     * WA Web relies on the GraphQL client to unwrap the response. Cobalt
     * performs the unwrapping manually because the stanza is returned by
     * the IQ framework rather than a JS promise.
     * @param node the IQ response node received from the relay
     * @return an {@link Optional} containing the parsed response, or empty
     *         if the node does not contain a well-formed result payload
     */
    public static Optional<AcceptNewsletterAdminInviteMexResponse> of(Node node) {
        return node.getChild("result")
                .flatMap(Node::toContentBytes)
                .flatMap(AcceptNewsletterAdminInviteMexResponse::of);
    }

    /**
     * Returns the identifier of the newsletter whose invite was accepted.
     *
     * @return an {@link Optional} containing the identifier, or empty if
     *         the server did not echo it back
     */
    public Optional<String> id() {
        return Optional.ofNullable(id);
    }

    /**
     * Parses a {@link AcceptNewsletterAdminInviteMexResponse} from the raw JSON bytes of the
     * {@code <result>} child.
     *
     * @implNote WAWebMexAcceptNewsletterAdminInviteJob.acceptNewsletterAdminInvite:
     * mirrors the implicit unwrapping that WA Web performs on the GraphQL
     * response, extracting the {@code xwa2_newsletter_admin_invite_accept}
     * root before returning.
     * @param json the UTF-8 encoded JSON payload
     * @return an {@link Optional} containing the parsed response, or empty
     *         if the envelope lacks a {@code data} or result root
     */
    private static Optional<AcceptNewsletterAdminInviteMexResponse> of(byte[] json) {
        // WAWebMexAcceptNewsletterAdminInviteJob.acceptNewsletterAdminInvite
        // Parses the raw JSON payload into a fastjson2 JSONObject and returns empty if the parser yields null
        var jsonObject = JSON.parseObject(json);
        if (jsonObject == null) {
            return Optional.empty();
        }

        // WAWebMexAcceptNewsletterAdminInviteJob.acceptNewsletterAdminInvite
        // Descends into the standard GraphQL "data" envelope, returning empty when the server reported only errors
        var data = jsonObject.getJSONObject("data");
        if (data == null) {
            return Optional.empty();
        }

        // WAWebMexAcceptNewsletterAdminInviteJob.acceptNewsletterAdminInvite
        // Extracts the mutation-specific root object, which the server keys by xwa2_newsletter_admin_invite_accept
        var root = data.getJSONObject("xwa2_newsletter_admin_invite_accept");
        if (root == null) {
            return Optional.empty();
        }

        var id = root.getString("id");

        return Optional.of(new AcceptNewsletterAdminInviteMexResponse(id));
    }
}
