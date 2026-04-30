package com.github.auties00.cobalt.node.mex.json.newsletter;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.node.mex.MexOperation;
import com.github.auties00.cobalt.node.Node;
import java.time.Instant;
import java.util.Optional;

/**
 * Response variant for {@link CreateNewsletterAdminInviteMexRequest} exposing
 * the invite identifier and its expiration timestamp.
 *
 * @implNote The body wraps the
 *           {@code data.xwa2_newsletter_admin_invite_create} root of the JSON
 *           response.
 */
@WhatsAppWebModule(moduleName = "WAWebMexCreateNewsletterAdminInviteJob")
public final class CreateNewsletterAdminInviteMexResponse implements MexOperation.Response.Json {
    /**
     * The unix-second timestamp at which the created invite expires.
     */
    private final Long inviteExpirationTime;

    /**
     * The identifier of the newsletter the invite targets.
     */
    private final String id;

    /**
     * Creates a response carrying the parsed scalar fields.
     *
     * @param inviteExpirationTime the unix-second invite expiration
     *                             timestamp
     * @param id                   the newsletter identifier
     */
    private CreateNewsletterAdminInviteMexResponse(Long inviteExpirationTime, String id) {
        this.inviteExpirationTime = inviteExpirationTime;
        this.id = id;
    }

    /**
     * Parses a MEX response from the given IQ response node.
     *
     * @param node the IQ response node received from the relay
     * @return an {@link Optional} containing the parsed response, or empty if
     *         the node is missing a result payload
     */
    public static Optional<CreateNewsletterAdminInviteMexResponse> of(Node node) {
        return node.getChild("result")
                .flatMap(Node::toContentBytes)
                .flatMap(CreateNewsletterAdminInviteMexResponse::of);
    }

    /**
     * Returns the invite expiration timestamp.
     *
     * @return an {@link Optional} containing the value as an {@link Instant},
     *         or empty if absent
     */
    public Optional<Instant> inviteExpirationTime() {
        return Optional.ofNullable(inviteExpirationTime).map(Instant::ofEpochSecond);
    }

    /**
     * Returns the newsletter identifier.
     *
     * @return an {@link Optional} containing the value, or empty if absent
     */
    public Optional<String> id() {
        return Optional.ofNullable(id);
    }

    /**
     * Parses a response from the raw JSON bytes of the {@code <result>}
     * child.
     *
     * @param json the UTF-8 encoded JSON payload
     * @return an {@link Optional} containing the parsed response, or empty if
     *         the envelope is missing expected fields
     */
    private static Optional<CreateNewsletterAdminInviteMexResponse> of(byte[] json) {
        var jsonObject = JSON.parseObject(json);
        if (jsonObject == null) {
            return Optional.empty();
        }

        var data = jsonObject.getJSONObject("data");
        if (data == null) {
            return Optional.empty();
        }

        var root = data.getJSONObject("xwa2_newsletter_admin_invite_create");
        if (root == null) {
            return Optional.empty();
        }

        var inviteExpirationTime = root.getLong("invite_expiration_time");
        var id = root.getString("id");

        return Optional.of(new CreateNewsletterAdminInviteMexResponse(inviteExpirationTime, id));
    }
}
