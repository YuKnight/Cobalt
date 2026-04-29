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
import java.time.Instant;
import java.util.Optional;

/**
 * The response variant of {@link CreateNewsletterAdminInviteMexResponse} that exposes the data
 * returned by the server after a successful mutation.
 *
 * @implNote WAWebMexCreateNewsletterAdminInviteJob: adapts the JSON root returned by the GraphQL
 * mutation into a Java value object.
 */
@WhatsAppWebModule(moduleName = "WAWebMexCreateNewsletterAdminInviteJob")
public final class CreateNewsletterAdminInviteMexResponse implements MexOperation.Response.Json {
    private final Long inviteExpirationTime;
    private final String id;

    private CreateNewsletterAdminInviteMexResponse(Long inviteExpirationTime, String id) {
        this.inviteExpirationTime = inviteExpirationTime;
        this.id = id;
    }

    /**
     * Parses a MEX response from the given IQ response node.
     *
     * @implNote WAWebMexCreateNewsletterAdminInviteJob.createNewsletterAdminInvite: WA Web relies on the
     * GraphQL client to unwrap the response. Cobalt performs the
     * unwrapping manually from the IQ {@code <result>} child.
     * @param node the IQ response node received from the relay
     * @return an {@link Optional} containing the parsed response, or
     *         empty if the node is missing a result payload
     */
    public static Optional<CreateNewsletterAdminInviteMexResponse> of(Node node) {
        return node.getChild("result")
                .flatMap(Node::toContentBytes)
                .flatMap(CreateNewsletterAdminInviteMexResponse::of);
    }

    /**
     * Returns the {@code invite_expiration_time} field.
     *
     * @return an {@link Optional} containing the value as an {@link Instant}, or empty if absent
     */
    public Optional<Instant> inviteExpirationTime() {
        return Optional.ofNullable(inviteExpirationTime).map(Instant::ofEpochSecond);
    }

    /**
     * Returns the {@code id} field.
     *
     * @return an {@link Optional} containing the value, or empty if absent
     */
    public Optional<String> id() {
        return Optional.ofNullable(id);
    }

    /**
     * Parses a {@link CreateNewsletterAdminInviteMexResponse} from the raw JSON bytes of the
     * {@code <result>} child.
     *
     * @implNote WAWebMexCreateNewsletterAdminInviteJob.createNewsletterAdminInvite: mirrors the implicit
     * unwrapping that WA Web performs on the GraphQL response,
     * extracting the {@code xwa2_newsletter_admin_invite_create} root.
     * @param json the UTF-8 encoded JSON payload
     * @return an {@link Optional} containing the parsed response, or
     *         empty if the envelope is missing expected fields
     */
    private static Optional<CreateNewsletterAdminInviteMexResponse> of(byte[] json) {
        // WAWebMexCreateNewsletterAdminInviteJob.createNewsletterAdminInvite
        // Parses the raw JSON payload, bailing out if fastjson2 returns null
        var jsonObject = JSON.parseObject(json);
        if (jsonObject == null) {
            return Optional.empty();
        }

        // WAWebMexCreateNewsletterAdminInviteJob.createNewsletterAdminInvite
        // Descends into the standard GraphQL "data" envelope
        var data = jsonObject.getJSONObject("data");
        if (data == null) {
            return Optional.empty();
        }

        // WAWebMexCreateNewsletterAdminInviteJob.createNewsletterAdminInvite
        // Extracts the operation-specific root keyed by xwa2_newsletter_admin_invite_create
        var root = data.getJSONObject("xwa2_newsletter_admin_invite_create");
        if (root == null) {
            return Optional.empty();
        }

        var inviteExpirationTime = root.getLong("invite_expiration_time");
        var id = root.getString("id");

        return Optional.of(new CreateNewsletterAdminInviteMexResponse(inviteExpirationTime, id));
    }
}
