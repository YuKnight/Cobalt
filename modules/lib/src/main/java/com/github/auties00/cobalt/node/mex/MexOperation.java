package com.github.auties00.cobalt.node.mex;

import com.alibaba.fastjson2.JSONObject;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.NodeBuilder;

import java.util.Optional;

/**
 * Root sealed type for every MEX (Media Exchange) operation modelled in Cobalt.
 *
 * <p>MEX is WhatsApp's GraphQL-over-XMPP transport for structured queries and
 * mutations. Each concrete MEX operation in WA Web ships as a
 * {@code WAWebMex...Job} module that calls
 * {@code WAWebMexClient.fetchQuery(queryDef, variables)} with a compiled query
 * identifier plus the GraphQL variables payload, dispatched as an
 * {@code <iq xmlns="w:mex">} stanza.
 *
 * <p>Cobalt collapses every MEX trio (request shape, response shape,
 * dispatcher) into a per-operation sealed interface that permits a
 * {@code Request} and a {@code Response} variant. Each {@code Request}
 * additionally implements either {@link Request.Json} or {@link Request.Argo}
 * to declare its payload encoding, and each {@code Response} mirrors the same
 * choice through {@link Response.Json} or {@link Response.Argo}.
 *
 * @implNote The {@code params.id} and {@code params.name} scalars fed into
 *           {@code MexPerfTracker} by {@code WAWebMexClient.fetchQuery} are
 *           surfaced by {@link Request#id()} and {@link Request#name()}, and
 *           the variables payload is the body that
 *           {@link Request.Json#createMexNode} or
 *           {@link Request.Argo#createMexNode} wraps in the canonical
 *           {@code <iq xmlns="w:mex"><query query_id="..."/></iq>} envelope.
 */
@WhatsAppWebModule(moduleName = "WAWebMexClient")
public sealed interface MexOperation permits MexOperation.Request, MexOperation.Response {
    /**
     * The outbound side of a MEX operation. Every concrete operation's
     * {@code Request} variant implements either {@link Json} or {@link Argo}
     * depending on the payload encoding.
     */
    sealed interface Request extends MexOperation {
        /**
         * Returns the compiled GraphQL query identifier the WhatsApp relay
         * uses to look up this operation's persisted document.
         *
         * @return the GraphQL query identifier, never {@code null}
         */
        String id();

        /**
         * Returns the GraphQL operation name reported by WA Web's
         * {@code MexPerfTracker} for this query or mutation.
         *
         * @return the GraphQL operation name, never {@code null}
         */
        String name();

        /**
         * Builds the outbound MEX IQ stanza for this request.
         *
         * <p>Each concrete {@code Request} implementation serialises its
         * GraphQL variables (JSON for {@link Json}, Argo-encoded bytes for
         * {@link Argo}) and wraps them in the canonical
         * {@code <iq xmlns="w:mex"><query query_id="..."/></iq>} envelope
         * through the corresponding {@code createMexNode} helper.
         *
         * @return the outbound stanza builder, never {@code null}
         */
        NodeBuilder toNode();

        /**
         * Marker for MEX requests whose GraphQL variables are encoded as JSON.
         *
         * <p>JSON is the encoding used by the vast majority of WA Web MEX jobs
         * including newsletter management, username administration, group
         * metadata queries and community subgroup operations.
         */
        @WhatsAppWebModule(moduleName = "WAWebMexClient")
        @WhatsAppWebModule(moduleName = "WAWebMexNativeClient")
        @WhatsAppWebModule(moduleName = "WAWebMexRelayEnvironment")
        non-sealed interface Json extends Request {
            /**
             * Builds the MEX IQ stanza that wraps a JSON-encoded GraphQL
             * query.
             *
             * @implNote WA Web passes the JSON payload as a {@code Uint8Array}
             *           via {@code Binary.build(...).readByteArrayView()}.
             *           Cobalt accepts a {@code String} here because the WAWap
             *           encoder produces identical wire bytes for either
             *           representation. The IQ {@code id} attribute is
             *           injected by {@code WhatsAppClient.sendNode} when
             *           missing, mirroring WA Web's {@code generateId()} call.
             * @param queryId     the numeric query identifier assigned to the
             *                    compiled GraphQL operation by the WA relay
             * @param jsonPayload the JSON string containing the serialised
             *                    {@code variables} envelope
             * @return a {@link NodeBuilder} prepared for the IQ stanza,
             *         callers may still mutate attributes before building
             */
            @WhatsAppWebExport(moduleName = "WAWebMexClient", exports = "fetchQuery",
                    adaptation = WhatsAppAdaptation.ADAPTED)
            @WhatsAppWebExport(moduleName = "WAWebMexNativeClient", exports = "fetchQuery",
                    adaptation = WhatsAppAdaptation.ADAPTED)
            static NodeBuilder createMexNode(String queryId, String jsonPayload) {
                var queryNode = new NodeBuilder()
                        .description("query")
                        .attribute("query_id", queryId)
                        .content(jsonPayload)
                        .build();

                return new NodeBuilder()
                        .description("iq")
                        .attribute("xmlns", "w:mex")
                        .attribute("to", JidServer.user())
                        .attribute("type", "get")
                        .content(queryNode);
            }
        }

        /**
         * Marker for MEX requests whose GraphQL variables are encoded with the
         * Argo binary format rather than JSON.
         *
         * <p>The transport envelope is identical to the JSON variant: an IQ
         * stanza with the {@code w:mex} namespace wrapping a {@code <query>}
         * node tagged with {@code query_id}. Only the body of the query and
         * the server reply differ, carrying raw Argo-encoded bytes.
         *
         * @implNote The current WA Web snapshot dispatches every MEX operation
         *           through the JSON path. The Argo branch is preserved here
         *           as a forward-looking extension that emits an identical
         *           outer IQ envelope so it can be enabled without further
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
             * @implNote The Argo payload occupies the same byte-array body
             *           slot the JSON variant uses for its UTF-8 envelope, and
             *           the outer IQ attributes are byte-for-byte identical.
             * @param queryId     the numeric query identifier assigned to the
             *                    compiled GraphQL operation by the WA relay
             * @param argoPayload the Argo-encoded GraphQL variables
             * @return a {@link NodeBuilder} prepared for the IQ stanza,
             *         callers may still mutate attributes before building
             */
            @WhatsAppWebExport(moduleName = "WAWebMexClient", exports = "fetchQuery",
                    adaptation = WhatsAppAdaptation.ADAPTED)
            @WhatsAppWebExport(moduleName = "WAWebMexNativeClient", exports = "fetchQuery",
                    adaptation = WhatsAppAdaptation.ADAPTED)
            static NodeBuilder createMexNode(String queryId, byte[] argoPayload) {
                var queryNode = new NodeBuilder()
                        .description("query")
                        .attribute("query_id", queryId)
                        .content(argoPayload)
                        .build();

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
     * The inbound side of a MEX operation. Every concrete operation's
     * {@code Response} variant implements either {@link Json} or {@link Argo}
     * depending on the payload encoding.
     *
     * <p>Carries no abstract methods today. The type exists purely as the
     * closed counterpart of {@link Request} so the entire MEX surface can be
     * reasoned about as a single sealed hierarchy.
     */
    sealed interface Response extends MexOperation {
        /**
         * Marker for MEX responses whose payloads decode from JSON.
         */
        @WhatsAppWebModule(moduleName = "WAWebMexGetTypename")
        non-sealed interface Json extends Response {
            /**
             * Returns the GraphQL {@code __typename} discriminator carried by
             * a MEX response object, if any.
             *
             * <p>MEX responses are GraphQL payloads whose concrete shape is
             * identified by the synthetic {@code __typename} field injected by
             * the relay. For example the {@code xwa2_group_query_by_id}
             * envelope carries one of {@code "XWA2Group"},
             * {@code "XWA2CommunityGroup"},
             * {@code "XWA2CommunityDefaultSubGroup"} or
             * {@code "XWA2CommunitySubGroup"} so callers can branch on the
             * underlying entity type before projecting the rest of the payload
             * into a domain model.
             *
             * <p>The helper is null-safe and mirrors the
             * {@code obj?.__typename} optional-chaining semantics used in the
             * original JavaScript helper.
             *
             * @param obj the MEX response JSON object to inspect, may be
             *            {@code null}
             * @return an {@link Optional} wrapping the {@code __typename}
             *         string, or {@link Optional#empty()} if {@code obj} is
             *         {@code null} or does not expose that field
             */
            @WhatsAppWebExport(moduleName = "WAWebMexGetTypename", exports = "getTypename",
                    adaptation = WhatsAppAdaptation.ADAPTED)
            static Optional<String> getTypename(JSONObject obj) {
                if (obj == null) {
                    return Optional.empty();
                }
                return Optional.ofNullable(obj.getString("__typename"));
            }
        }

        /**
         * Marker for MEX responses whose payloads decode from Argo binary.
         *
         * <p>No helpers today. The Argo decode path is not yet wired in the
         * current WA Web snapshot.
         */
        non-sealed interface Argo extends Response {
        }
    }

}
