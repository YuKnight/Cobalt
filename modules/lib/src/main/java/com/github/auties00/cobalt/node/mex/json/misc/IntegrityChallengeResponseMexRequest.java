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
     * The numeric GraphQL query identifier assigned by the WhatsApp relay
     * to the {@code IntegrityChallengeResponse} compiled mutation.
     *
     * @implNote WAWebMexIntegrityChallengeResponseMutation.graphql:
     * corresponds to the compiled document id registered for the
     * {@code mexSubmitPasskeyChallengeResponse} mutation.
     */
    public static final String QUERY_ID = "26230331493320650";

    /**
     * The GraphQL operation name reported by WA Web's
     * {@code MexPerfTracker} when dispatching this query, mirroring the
     * {@code params.name} value of the compiled mexSubmitPasskeyChallengeResponse
     * operation.
     *
     * <p>The constant is exposed through {@link #name()} so
     * call sites can reach the same telemetry tag WA Web emits without
     * duplicating the literal at every dispatch site.
     *
     * @implNote WAWebMexIntegrityChallengeResponse: WA Web invokes the operation through
     * {@code WAWebMexClient.fetchQuery} which forwards to
     * {@code WAWebMexNativeClient}; the native client passes the
     * {@code params.name} of the compiled GraphQL artifact to
     * {@code MexPerfTracker.setOperationName}. Cobalt mirrors that
     * scalar verbatim as {@code "mexSubmitPasskeyChallengeResponse"}.
     */
    public static final String OPERATION_NAME = "mexSubmitPasskeyChallengeResponse";
    /**
     * The hard-coded {@code challenge_type} discriminator emitted on
     * every request.
     *
     * @implNote WAWebMexIntegrityChallengeResponse.mexSubmitPasskeyChallengeResponse:
     * the JS source declares {@code var s = "PASSKEY"} as the only
     * supported challenge type and reuses the constant for every
     * call.
     */
    String CHALLENGE_TYPE = "PASSKEY";

    private final byte[] signedChallenge;
    private final boolean prfAvailable;

    /**
     * Constructs a request that submits the given passkey-signed
     * challenge response to the relay.
     *
     * @implNote WAWebMexIntegrityChallengeResponse.mexSubmitPasskeyChallengeResponse:
     * WA Web's {@code function*(e)} accepts an opaque object {@code e}
     * carrying the WebAuthn assertion fields and constructs
     * {@code {input: {challenge_type: "PASSKEY", passkey_response: {signed_challenge: btoa(JSON.stringify(e)), prf_available: e.prf_output != null}}}}
     * as the GraphQL variables payload. Cobalt accepts the already
     * JSON-serialised assertion as a raw byte array and base64-encodes
     * it inline during {@link #toNode()}, mirroring the JS
     * {@code btoa(JSON.stringify(e))} call site.
     * @param signedChallenge the raw bytes of the JSON-serialised
     *                        WebAuthn assertion; must not be
     *                        {@code null}
     * @param prfAvailable    whether the assertion carries a
     *                        {@code prf_output} field, mirroring the
     *                        {@code e.prf_output != null} JS check
     */
    public IntegrityChallengeResponseMexRequest(byte[] signedChallenge, boolean prfAvailable) {
        this.signedChallenge = Objects.requireNonNull(signedChallenge, "signedChallenge cannot be null");
        this.prfAvailable = prfAvailable;
    }

    /**
     * Returns the compiled GraphQL query identifier projected from
     * {@link #QUERY_ID}.
     *
     * @implNote WAWebMexIntegrityChallengeResponse: WA Web reads the {@code params.id}
     *           field of the compiled artifact and forwards it to
     *           {@code MexPerfTracker.setQueryId}; Cobalt projects
     *           the same scalar through this accessor.
     * @return the constant {@link #QUERY_ID}; never
     *         {@code null}
     */
    @Override
    public String id() {
        return QUERY_ID;
    }

    /**
     * Returns the GraphQL operation name projected from
     * {@link #OPERATION_NAME}.
     *
     * @implNote WAWebMexIntegrityChallengeResponse: WA Web's
     *           {@code WAWebMexNativeClient.fetchQuery} reads
     *           {@code params.name} from the compiled GraphQL
     *           artifact and forwards it to
     *           {@code MexPerfTracker.setOperationName}; Cobalt
     *           projects the same scalar through this accessor.
     * @return the constant {@link #OPERATION_NAME};
     *         never {@code null}
     */
    @Override
    public String name() {
        return OPERATION_NAME;
    }

    /**
     * Builds the IQ stanza that dispatches this operation to the
     * WhatsApp relay.
     *
     * @implNote WAWebMexIntegrityChallengeResponse.mexSubmitPasskeyChallengeResponse:
     * WA Web constructs the {@code variables} object inline as
     * {@code {input: {challenge_type: "PASSKEY", passkey_response: {signed_challenge: btoa(JSON.stringify(e)), prf_available: e.prf_output != null}}}}
     * and delegates to {@code WAWebMexClient.fetchQuery}. Cobalt
     * writes the JSON directly via {@code fastjson2.JSONWriter},
     * encodes the raw assertion bytes via
     * {@link Base64#getEncoder()} to mirror the JS {@code btoa} call,
     * and wraps the envelope through
     * {@link Json#createMexNode(String, String)}.
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         serialised GraphQL variables
     */
    @WhatsAppWebExport(moduleName = "WAWebMexIntegrityChallengeResponse", exports = "mexSubmitPasskeyChallengeResponse",
            adaptation = WhatsAppAdaptation.DIRECT)
    @Override
    public NodeBuilder toNode() {
        // WAWebMexIntegrityChallengeResponse.mexSubmitPasskeyChallengeResponse
        // Opens a UTF-8 JSON writer that will serialise the GraphQL variables envelope
        try (var writer = JSONWriter.ofUTF8()) {
            // WAWebMexIntegrityChallengeResponse.mexSubmitPasskeyChallengeResponse
            // Begins the outer envelope and the nested "variables" object consumed by WAWebMexClient.fetchQuery
            writer.startObject();
            writer.writeName("variables");
            writer.writeColon();
            writer.startObject();

            // WAWebMexIntegrityChallengeResponse.mexSubmitPasskeyChallengeResponse
            // input: {challenge_type: "PASSKEY", passkey_response: {...}}
            writer.writeName("input");
            writer.writeColon();
            writer.startObject();

            writer.writeName("challenge_type");
            writer.writeColon();
            writer.writeString(CHALLENGE_TYPE);

            // WAWebMexIntegrityChallengeResponse.mexSubmitPasskeyChallengeResponse
            // passkey_response: {signed_challenge: btoa(JSON.stringify(e)), prf_available: e.prf_output != null}
            writer.writeName("passkey_response");
            writer.writeColon();
            writer.startObject();
            writer.writeName("signed_challenge");
            writer.writeColon();
            // WAWebMexIntegrityChallengeResponse.mexSubmitPasskeyChallengeResponse
            // btoa(JSON.stringify(e)) is implemented as a standard base64 encode of the already-JSON-serialised assertion bytes
            writer.writeString(Base64.getEncoder().encodeToString(signedChallenge));
            writer.writeName("prf_available");
            writer.writeColon();
            writer.writeBool(prfAvailable);
            writer.endObject();

            writer.endObject();
            writer.endObject();
            writer.endObject();

            // DIRECT: WAWebMexIntegrityChallengeResponse.mexSubmitPasskeyChallengeResponse
            // Flushes the JSON buffer into a StringWriter and wraps it in the shared MEX IQ envelope
            try (var output = new StringWriter()) {
                writer.flushTo(output);
                return Json.createMexNode(QUERY_ID, output.toString());
            }
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }
}
