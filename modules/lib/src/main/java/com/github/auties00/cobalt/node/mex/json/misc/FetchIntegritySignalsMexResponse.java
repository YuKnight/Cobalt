package com.github.auties00.cobalt.node.mex.json.misc;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.mex.MexOperation;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.util.Objects;
import java.util.Optional;

/**
 * The response variant of {@link FetchIntegritySignalsMexResponse} that exposes
 * the data returned by the server after a successful query.
 *
 * @implNote WAWebMexFetchIntegritySignals: adapts the JSON root returned
 * by the GraphQL query into a Java value object. WA Web's
 * {@code fetchIntegritySignals} unwraps the response by reading
 * {@code i.xwa2_fetch_wa_users[0].integrity_signals_info} and
 * exposing {@code is_new_account} / {@code is_suspicious_start_chat}
 * via a {@code {isNewAccount, isSuspicious}} record; Cobalt mirrors
 * the same projection but keeps the underlying nullability so callers
 * can distinguish "absent" from "explicitly false".
 */
@WhatsAppWebModule(moduleName = "WAWebMexFetchIntegritySignals")
public final class FetchIntegritySignalsMexResponse implements MexOperation.Response.Json {
    private final Boolean isNewAccount;
    private final Boolean isSuspicious;

    private FetchIntegritySignalsMexResponse(Boolean isNewAccount, Boolean isSuspicious) {
        this.isNewAccount = isNewAccount;
        this.isSuspicious = isSuspicious;
    }

    /**
     * Parses a MEX response from the given IQ response node.
     *
     * @implNote WAWebMexFetchIntegritySignals.fetchIntegritySignals: WA
     * Web relies on the GraphQL client to unwrap the response. Cobalt
     * performs the unwrapping manually from the IQ {@code <result>}
     * child.
     * @param node the IQ response node received from the relay
     * @return an {@link Optional} containing the parsed response, or
     *         empty if the node is missing a result payload
     */
    @WhatsAppWebExport(moduleName = "WAWebMexFetchIntegritySignals", exports = "fetchIntegritySignals",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public static Optional<FetchIntegritySignalsMexResponse> of(Node node) {
        return node.getChild("result")
                .flatMap(Node::toContentBytes)
                .flatMap(FetchIntegritySignalsMexResponse::of);
    }

    /**
     * Returns the {@code is_new_account} field.
     *
     * @implNote WAWebMexFetchIntegritySignals.fetchIntegritySignals:
     * mirrors the WA Web {@code isNewAccount} property exposed by the
     * unwrapped response object.
     * @return an {@link Optional} containing the value, or empty if absent
     */
    public Optional<Boolean> isNewAccount() {
        return Optional.ofNullable(isNewAccount);
    }

    /**
     * Returns the {@code is_suspicious_start_chat} field.
     *
     * @implNote WAWebMexFetchIntegritySignals.fetchIntegritySignals:
     * mirrors the WA Web {@code isSuspicious} property, which is
     * derived from the wire-level {@code is_suspicious_start_chat}
     * scalar exposed by the {@code XWA2IntegritySignals} fragment.
     * @return an {@link Optional} containing the value, or empty if absent
     */
    public Optional<Boolean> isSuspicious() {
        return Optional.ofNullable(isSuspicious);
    }

    /**
     * Parses a {@link FetchIntegritySignalsMexResponse} from the raw JSON bytes of the
     * {@code <result>} child.
     *
     * @implNote WAWebMexFetchIntegritySignals.fetchIntegritySignals:
     * mirrors the implicit unwrapping that WA Web performs on the
     * GraphQL response, extracting the
     * {@code xwa2_fetch_wa_users[0].integrity_signals_info} sub-object.
     * @param json the UTF-8 encoded JSON payload
     * @return an {@link Optional} containing the parsed response, or
     *         empty if the envelope is missing expected fields
     */
    private static Optional<FetchIntegritySignalsMexResponse> of(byte[] json) {
        // WAWebMexFetchIntegritySignals.fetchIntegritySignals
        // Parses the raw JSON payload, bailing out if fastjson2 returns null
        var jsonObject = JSON.parseObject(json);
        if (jsonObject == null) {
            return Optional.empty();
        }

        // WAWebMexFetchIntegritySignals.fetchIntegritySignals
        // Descends into the standard GraphQL "data" envelope
        var data = jsonObject.getJSONObject("data");
        if (data == null) {
            return Optional.empty();
        }

        // WAWebMexFetchIntegritySignals.fetchIntegritySignals
        // Extracts the operation-specific root keyed by xwa2_fetch_wa_users (a JSON array)
        var rootArr = data.getJSONArray("xwa2_fetch_wa_users");
        if (rootArr == null || rootArr.isEmpty()) {
            return Optional.empty();
        }

        // WAWebMexFetchIntegritySignals.fetchIntegritySignals
        // l = (t = i.xwa2_fetch_wa_users) == null ? void 0 : t[0]
        var first = rootArr.getJSONObject(0);
        if (first == null) {
            return Optional.empty();
        }

        // WAWebMexFetchIntegritySignals.fetchIntegritySignals
        // p = l.integrity_signals_info; if (p == null) return null
        var info = first.getJSONObject("integrity_signals_info");
        if (info == null) {
            return Optional.empty();
        }

        var isNewAccount = info.getBoolean("is_new_account");
        var isSuspicious = info.getBoolean("is_suspicious_start_chat");

        return Optional.of(new FetchIntegritySignalsMexResponse(isNewAccount, isSuspicious));
    }
}
