package com.github.auties00.cobalt.node.mex.json.misc;

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
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;

/**
 * Submits a passkey-signed integrity challenge response, returning the
 * relay's verdict on whether the challenge was accepted.
 *
 * <p>The mutation is the second leg of the passkey integrity-challenge
 * handshake: after the relay issues a challenge, the client signs it with
 * the user's WebAuthn credential and submits the result here. The
 * compiled GraphQL artifact projects two scalar fields,
 * {@code success} and {@code error_message}, on the
 * {@code XWA2IntegrityChallengeResponsePayload} root.
 *
 * @implNote WAWebMexIntegrityChallengeResponse: adapts the
 * {@code mexSubmitPasskeyChallengeResponse} GraphQL mutation, which in WA
 * Web is invoked via {@code WAWebMexClient.fetchQuery} and whose response
 * is unwrapped by the same module to expose the
 * {@code xwa2_submit_integrity_challenge_response} root verbatim. Cobalt
 * models the request and response as sibling variants of a sealed
 * interface rather than a free-standing async function.
 */
@WhatsAppWebModule(moduleName = "WAWebMexIntegrityChallengeResponse")
public final class IntegrityChallengeResponseMexRequest implements MexOperation.Request.Json {
    /**
     * The numeric GraphQL query identifier assigned by the WhatsApp relay to
     * the compiled {@code mexSubmitPasskeyChallengeResponse} mutation.
     */
    public static final String QUERY_ID = "26230331493320650";

    /**
     * The GraphQL operation name reported by WA Web's {@code MexPerfTracker}
     * when dispatching this query, mirroring the {@code params.name} value of
     * the compiled {@code mexSubmitPasskeyChallengeResponse} operation.
     */
    public static final String OPERATION_NAME = "mexSubmitPasskeyChallengeResponse";
    /**
     * The hard-coded {@code challenge_type} discriminator emitted on every
     * request. The JS source declares {@code var s = "PASSKEY"} as the only
     * supported challenge type and reuses the constant for every call.
     */
    String CHALLENGE_TYPE = "PASSKEY";

    /**
     * The raw bytes of the JSON-serialised WebAuthn assertion, base64-encoded
     * inline during dispatch to mirror the JS {@code btoa(JSON.stringify(e))}
     * call site.
     */
    private final byte[] signedChallenge;
    /**
     * Whether the assertion carries a {@code prf_output} field, mirroring the
     * {@code e.prf_output != null} JS check.
     */
    private final boolean prfAvailable;

    /**
     * Constructs a request that submits the given passkey-signed challenge
     * response to the relay.
     *
     * @implNote Cobalt accepts the already JSON-serialised assertion as a raw
     * byte array and base64-encodes it inline during {@link #toNode()},
     * mirroring the JS {@code btoa(JSON.stringify(e))} call site.
     * @param signedChallenge the raw bytes of the JSON-serialised WebAuthn
     *                        assertion, must not be {@code null}
     * @param prfAvailable    whether the assertion carries a {@code prf_output}
     *                        field
     * @throws NullPointerException if {@code signedChallenge} is {@code null}
     */
    public IntegrityChallengeResponseMexRequest(byte[] signedChallenge, boolean prfAvailable) {
        this.signedChallenge = Objects.requireNonNull(signedChallenge, "signedChallenge cannot be null");
        this.prfAvailable = prfAvailable;
    }

    /**
     * Returns the compiled GraphQL query identifier.
     *
     * @return the constant {@link #QUERY_ID}; never {@code null}
     */
    @Override
    public String id() {
        return QUERY_ID;
    }

    /**
     * Returns the GraphQL operation name.
     *
     * @return the constant {@link #OPERATION_NAME}; never {@code null}
     */
    @Override
    public String name() {
        return OPERATION_NAME;
    }

    /**
     * Builds the IQ stanza that dispatches this operation to the WhatsApp relay.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         serialised GraphQL variables
     */
    @WhatsAppWebExport(moduleName = "WAWebMexIntegrityChallengeResponse", exports = "mexSubmitPasskeyChallengeResponse",
            adaptation = WhatsAppAdaptation.DIRECT)
    @Override
    public NodeBuilder toNode() {
        try (var writer = JSONWriter.ofUTF8()) {
            writer.startObject();
            writer.writeName("variables");
            writer.writeColon();
            writer.startObject();

            writer.writeName("input");
            writer.writeColon();
            writer.startObject();

            writer.writeName("challenge_type");
            writer.writeColon();
            writer.writeString(CHALLENGE_TYPE);

            writer.writeName("passkey_response");
            writer.writeColon();
            writer.startObject();
            writer.writeName("signed_challenge");
            writer.writeColon();
            // btoa(JSON.stringify(e)) is implemented as a standard base64 encode
            // of the already-JSON-serialised assertion bytes
            writer.writeString(Base64.getEncoder().encodeToString(signedChallenge));
            writer.writeName("prf_available");
            writer.writeColon();
            writer.writeBool(prfAvailable);
            writer.endObject();

            writer.endObject();
            writer.endObject();
            writer.endObject();

            try (var output = new StringWriter()) {
                writer.flushTo(output);
                return Json.createMexNode(QUERY_ID, output.toString());
            }
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }
}
