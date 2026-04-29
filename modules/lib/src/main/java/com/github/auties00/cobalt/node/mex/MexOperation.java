package com.github.auties00.cobalt.node.mex;

import com.alibaba.fastjson2.JSONObject;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.NodeBuilder;

import java.util.Optional;

/**
 * Root sealed type for every MEX (Media Exchange) operation modelled in
 * Cobalt.
 *
 * <p>MEX is WhatsApp's GraphQL-over-XMPP transport for structured queries
 * and mutations. Each concrete MEX operation in WA Web ships as a
 * {@code WAWebMex...Job} module that calls
 * {@code WAWebMexClient.fetchQuery(queryDef, variables)} with a compiled
 * query identifier plus the GraphQL variables payload, dispatched as an
 * {@code <iq xmlns="w:mex">} stanza.
 *
 * <p>Cobalt collapses every MEX trio (request shape, response shape,
 * dispatcher) into a per-operation sealed interface that permits a
 * {@code Request} and a {@code Response} variant. Each {@code Request}
 * additionally implements either {@link Request.Json} or {@link Request.Argo}
 * to declare its payload encoding; each {@code Response} mirrors the same
 * choice through {@link Response.Json} / {@link Response.Argo}.
 *
 * <p>The sealed hierarchy permits exactly two participants — {@link Request}
 * and {@link Response} — so any MEX operation handle is statically
 * classified as one or the other. {@link Request} declares the contract
 * required to dispatch the operation: the GraphQL query id, the operation
 * name reported to telemetry, and a {@code toNode()} factory producing the
 * outbound stanza. {@link Response} carries no methods today; it exists
 * purely as the closed counterpart of {@code Request} so the entire MEX
 * surface can be reasoned about as a single sealed type.
 *
 * @implNote {@code WAWebMexClient}, {@code WAWebMexNativeClient}: every WA
 *           Web job that depends on {@code WAWebMexClient} ultimately calls
 *           {@code fetchQuery(queryDef, variables)}. The {@code params.id}
 *           and {@code params.name} fields fed into
 *           {@code MexPerfTracker} are the same scalars surfaced by
 *           {@link Request#id()} and {@link Request#name()}; the variables
 *           payload is the body that {@link Request.Json#createMexNode} or
 *           {@link Request.Argo#createMexNode} wraps in the canonical
 *           {@code <iq xmlns="w:mex"><query query_id="..."/></iq>}
 *           envelope.
 */
@WhatsAppWebModule(moduleName = "WAWebMexClient")
public sealed interface MexOperation permits MexOperation.Request, MexOperation.Response {
    /**
     * The outbound side of a MEX operation — every concrete operation's
     * {@code Request} variant implements either {@link Json} or
     * {@link Argo} depending on the payload encoding.
     *
     * <p>Carries the GraphQL identifiers and the stanza factory required by
     * {@code WhatsAppClient.sendNode(MexOperation.Out)} so that call
     * sites can dispatch a request by passing the typed value directly.
     */
    sealed interface Request extends MexOperation {
        /**
         * Returns the compiled GraphQL query identifier the WhatsApp relay
         * uses to look up this operation's persisted document.
         *
         * <p>Mirrors the {@code params.id} value WA Web passes to
         * {@code WAWebMexNativeClient.fetchQuery}. Each concrete operation
         * interface supplies the constant via a {@code QUERY_ID} field
         * that the implementing {@code Request} class projects through
         * this accessor.
         *
         * @return the GraphQL query identifier; never {@code null}
         */
        String id();

        /**
         * Returns the GraphQL operation name reported by WA Web's
         * {@code MexPerfTracker} for this query / mutation.
         *
         * <p>Mirrors the {@code params.name} value WA Web passes to
         * {@code WAWebMexNativeClient.fetchQuery} (for example
         * {@code "mexUpdateNewsletter"}). Each concrete operation
         * interface supplies the constant via an {@code OPERATION_NAME}
         * field that the implementing {@code Request} class projects
         * through this accessor.
         *
         * @return the GraphQL operation name; never {@code null}
         */
        String name();

        /**
         * Builds the outbound MEX IQ stanza for this request.
         *
         * <p>Each concrete {@code Request} implementation serialises its
         * GraphQL variables (JSON for {@link Json},
         * Argo-encoded bytes for {@link Argo}) and wraps them in
         * the canonical {@code <iq xmlns="w:mex"><query query_id="..."/></iq>}
         * envelope through the corresponding
         * {@code createMexNode(String, ...)} helper.
         *
         * @return the outbound stanza builder; never {@code null}
         */
        NodeBuilder toNode();

        /**
         * Marker for MEX requests whose GraphQL variables are encoded as JSON
         * strings.
         *
         * <p>JSON is the encoding used by the vast majority of WA Web MEX
         * jobs: newsletter management, username administration, group
         * metadata queries, community subgroup operations, etc. The
         * {@link #createMexNode(String, String)} helper produces the
         * canonical IQ envelope that wraps the JSON variables payload.
         *
         * @implNote {@code WAWebMexClient.fetchQuery},
         *           {@code WAWebMexNativeClient.fetchQuery}: every WA Web
         *           job ending in {@code Job} or {@code JobMutation} that
         *           depends on {@code WAWebMexClient} ultimately calls
         *           {@code fetchQuery(queryDef, variables)} which serialises
         *           the variables as JSON before dispatching through
         *           {@code WAWebMexNativeClient}. Cobalt collapses the same
         *           flow into the static helper below.
         */
        @WhatsAppWebModule(moduleName = "WAWebMexClient")
        @WhatsAppWebModule(moduleName = "WAWebMexNativeClient")
        @WhatsAppWebModule(moduleName = "WAWebMexRelayEnvironment")
        non-sealed interface Json extends Request {
            /**
             * Builds the MEX IQ stanza that wraps a JSON-encoded GraphQL
             * query.
             *
             * <p>The returned {@link NodeBuilder} is not yet built so callers
             * can attach additional attributes before the stanza is
             * dispatched.
             *
             * @implNote {@code WAWebMexRelayEnvironment.sendMexIq}: the
             *           canonical WA Web transport call constructs
             *           {@code wap("iq", {id: generateId(),
             *           to: S_WHATSAPP_NET, type: "get", xmlns: "w:mex"},
             *           WapNode("query", {query_id: CUSTOM_STRING(t)},
             *           Binary.build(JSON.stringify(e)).readByteArrayView()))}.
             *           Cobalt emits the same stanza shape; the caller-side
             *           {@code id} attribute is injected by
             *           {@code WhatsAppClient.sendNode} when missing,
             *           mirroring WA Web's {@code generateId()} call. WA
             *           Web passes the JSON payload as a {@code Uint8Array}
             *           via {@code Binary.build(...).readByteArrayView()};
             *           Cobalt accepts a {@code String} here because the
             *           WAWap encoder produces identical wire bytes for
             *           either representation.
             * @param queryId     the numeric query identifier assigned to
             *                    the compiled GraphQL operation by the WA
             *                    relay
             * @param jsonPayload the JSON string containing the serialised
             *                    {@code {"variables": ...}} envelope
             * @return a {@link NodeBuilder} prepared for the IQ stanza;
             *         callers may still mutate attributes before building
             */
            @WhatsAppWebExport(moduleName = "WAWebMexClient", exports = "fetchQuery",
                    adaptation = WhatsAppAdaptation.ADAPTED)
            @WhatsAppWebExport(moduleName = "WAWebMexNativeClient", exports = "fetchQuery",
                    adaptation = WhatsAppAdaptation.ADAPTED)
            static NodeBuilder createMexNode(String queryId, String jsonPayload) {
                // WAWebMexRelayEnvironment.sendMexIq: WapNode("query", {query_id: CUSTOM_STRING(t)},
                // Binary.build(JSON.stringify(e)).readByteArrayView())
                var queryNode = new NodeBuilder()
                        .description("query")
                        .attribute("query_id", queryId)
                        .content(jsonPayload)
                        .build();

                // WAWebMexRelayEnvironment.sendMexIq: wap("iq", {id, to: S_WHATSAPP_NET,
                // type: "get", xmlns: "w:mex"}, queryNode) - id is added by sendNode when missing
                return new NodeBuilder()
                        .description("iq")
                        .attribute("xmlns", "w:mex")
                        .attribute("to", JidServer.user())
                        .attribute("type", "get")
                        .content(queryNode);
            }
        }

        /**
         * Marker for MEX requests whose GraphQL variables are encoded with
         * the Argo binary format rather than JSON.
         *
         * <p>Argo is a compact binary wire format used by a small number of
         * performance-sensitive MEX endpoints. The transport envelope is
         * identical to the JSON variant: an IQ stanza with the
         * {@code w:mex} namespace wrapping a {@code <query>} node tagged
         * with {@code query_id}. Only the body of the query and the server
         * reply differ, carrying raw Argo-encoded bytes.
         *
         * @implNote {@code WAWebMexClient}, {@code WAWebMexNativeClient}: in
         *           WA Web the transport choice is driven by the GraphQL
         *           query metadata; in Cobalt it is expressed as a separate
         *           sub-interface. The current WA Web snapshot dispatches
         *           every MEX operation through the JSON path — the Argo
         *           branch is preserved here as a forward-looking
         *           {@code ADAPTED} extension that emits an identical outer
         *           IQ envelope so it can be enabled without further
         *           changes.
         */
        @WhatsAppWebModule(moduleName = "WAWebMexClient")
        @WhatsAppWebModule(moduleName = "WAWebMexNativeClient")
        @WhatsAppWebModule(moduleName = "WAWebMexRelayEnvironment")
        non-sealed interface Argo extends Request {
            /**
             * Builds the MEX IQ stanza that wraps an Argo-encoded GraphQL
             * query.
             *
             * <p>The returned {@link NodeBuilder} is not yet built so callers
             * can attach additional attributes before the stanza is
             * dispatched.
             *
             * @implNote {@code WAWebMexRelayEnvironment.sendMexIq}: the
             *           canonical WA Web transport call constructs
             *           {@code wap("iq", {id, to: S_WHATSAPP_NET, type: "get",
             *           xmlns: "w:mex"}, WapNode("query", {query_id:
             *           CUSTOM_STRING(t)}, bytes))}. Cobalt emits the same
             *           stanza shape with the Argo payload occupying the
             *           same byte-array body slot the JSON variant uses for
             *           its UTF-8 envelope; the outer IQ attributes are
             *           byte-for-byte identical.
             * @param queryId     the numeric query identifier assigned to
             *                    the compiled GraphQL operation by the WA
             *                    relay
             * @param argoPayload the Argo-encoded GraphQL variables
             * @return a {@link NodeBuilder} prepared for the IQ stanza;
             *         callers may still mutate attributes before building
             */
            @WhatsAppWebExport(moduleName = "WAWebMexClient", exports = "fetchQuery",
                    adaptation = WhatsAppAdaptation.ADAPTED)
            @WhatsAppWebExport(moduleName = "WAWebMexNativeClient", exports = "fetchQuery",
                    adaptation = WhatsAppAdaptation.ADAPTED)
            static NodeBuilder createMexNode(String queryId, byte[] argoPayload) {
                // WAWebMexRelayEnvironment.sendMexIq: WapNode("query", {query_id: CUSTOM_STRING(t)},
                // <argoPayload bytes>) - identical to JSON variant but with raw Argo bytes
                var queryNode = new NodeBuilder()
                        .description("query")
                        .attribute("query_id", queryId)
                        .content(argoPayload)
                        .build();

                // WAWebMexRelayEnvironment.sendMexIq: wap("iq", {id, to: S_WHATSAPP_NET,
                // type: "get", xmlns: "w:mex"}, queryNode) - id is added by sendNode when missing
                return new NodeBuilder()
                        .description("iq")
                        .attribute("xmlns", "w:mex")
                        .attribute("to", JidServer.user())
                        .attribute("type", "get")
                        .content(queryNode);
            }
        }
    }

    /**
     * The inbound side of a MEX operation — every concrete operation's
     * {@code Response} variant implements either {@link Json} or
     * {@link Argo} depending on the payload encoding.
     *
     * <p>Carries no abstract methods today; the type exists purely as
     * the closed counterpart of {@link Request} so the entire MEX
     * surface can be reasoned about as a single sealed hierarchy.
     */
    sealed interface Response extends MexOperation {
        /**
         * Marker for MEX responses whose payloads decode from JSON.
         *
         * @implNote {@code WAWebMexGetTypename.getTypename}: helper for
         *           reading the synthetic {@code __typename} discriminator
         *           injected by the relay into GraphQL response objects.
         */
        @WhatsAppWebModule(moduleName = "WAWebMexGetTypename")
        non-sealed interface Json extends Response {
            /**
             * Returns the GraphQL {@code __typename} discriminator carried
             * by a MEX response object, if any.
             *
             * <p>MEX responses are GraphQL payloads whose concrete shape is
             * identified by the synthetic {@code __typename} field injected
             * by the relay. For example, the {@code xwa2_group_query_by_id}
             * envelope carries one of {@code "XWA2Group"},
             * {@code "XWA2CommunityGroup"},
             * {@code "XWA2CommunityDefaultSubGroup"} or
             * {@code "XWA2CommunitySubGroup"} to distinguish standalone
             * groups from community parents and subgroups. Callers use the
             * returned tag to branch on the underlying entity type before
             * projecting the rest of the payload into a domain model.
             *
             * <p>The helper is null-safe: when the response object is
             * missing or does not carry a {@code __typename} field, it
             * returns {@link Optional#empty()} to mirror the
             * {@code obj?.__typename} optional-chaining semantics used in
             * the original JavaScript helper.
             *
             * @implNote {@code WAWebMexGetTypename.getTypename}:
             *           {@code function e(e){return e==null?void
             *           0:e.__typename}}. Cobalt exposes the same null-safe
             *           property read as an {@link Optional} accessor.
             * @param obj the MEX response JSON object to inspect; may be
             *            {@code null}
             * @return an {@link Optional} wrapping the {@code __typename}
             *         string, or {@link Optional#empty()} if {@code obj} is
             *         {@code null} or does not expose that field
             */
            @WhatsAppWebExport(moduleName = "WAWebMexGetTypename", exports = "getTypename",
                    adaptation = WhatsAppAdaptation.ADAPTED)
            static Optional<String> getTypename(JSONObject obj) {
                // WAWebMexGetTypename.getTypename: return obj==null ? void 0 : obj.__typename
                if (obj == null) {
                    return Optional.empty();
                }
                return Optional.ofNullable(obj.getString("__typename"));
            }
        }

        /**
         * Marker for MEX responses whose payloads decode from Argo binary.
         *
         * <p>No helpers today — the Argo decode path is not yet wired in
         * the current WA Web snapshot.
         */
        non-sealed interface Argo extends Response {
        }
    }

}
