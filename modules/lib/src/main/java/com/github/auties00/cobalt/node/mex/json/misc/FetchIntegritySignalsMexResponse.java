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
 * Parsed response of the {@link FetchIntegritySignalsMexRequest} query,
 * exposing the {@code is_new_account} and {@code is_suspicious_start_chat}
 * scalars from the {@code XWA2IntegritySignals} fragment.
 */
@WhatsAppWebModule(moduleName = "WAWebMexFetchIntegritySignals")
public final class FetchIntegritySignalsMexResponse implements MexOperation.Response.Json {
    /**
     * The {@code is_new_account} scalar from the integrity signals info
     * sub-object.
     */
    private final Boolean isNewAccount;
    /**
     * The {@code is_suspicious_start_chat} scalar from the integrity signals
     * info sub-object.
     */
    private final Boolean isSuspicious;

    /**
     * Constructs a response wrapping the two boolean scalars parsed from the
     * {@code integrity_signals_info} sub-object.
     *
     * @param isNewAccount  the {@code is_new_account} scalar, or {@code null} if absent
     * @param isSuspicious  the {@code is_suspicious_start_chat} scalar, or {@code null} if absent
     */
    private FetchIntegritySignalsMexResponse(Boolean isNewAccount, Boolean isSuspicious) {
        this.isNewAccount = isNewAccount;
        this.isSuspicious = isSuspicious;
    }

    /**
     * Parses a MEX response from the given IQ response node.
     *
     * @param node the IQ response node received from the relay
     * @return an {@link Optional} containing the parsed response, or empty
     *         if the node is missing a result payload
     */
    @WhatsAppWebExport(moduleName = "WAWebMexFetchIntegritySignals", exports = "fetchIntegritySignals",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public static Optional<FetchIntegritySignalsMexResponse> of(Node node) {
        return node.getChild("result")
                .flatMap(Node::toContentBytes)
                .flatMap(FetchIntegritySignalsMexResponse::of);
    }

    /**
     * Returns the {@code is_new_account} scalar.
     *
     * @return an {@link Optional} containing the value, or empty if absent
     */
    public Optional<Boolean> isNewAccount() {
        return Optional.ofNullable(isNewAccount);
    }

    /**
     * Returns the {@code is_suspicious_start_chat} scalar.
     *
     * @return an {@link Optional} containing the value, or empty if absent
     */
    public Optional<Boolean> isSuspicious() {
        return Optional.ofNullable(isSuspicious);
    }

    /**
     * Parses a {@link FetchIntegritySignalsMexResponse} from the raw JSON
     * bytes of the {@code <result>} child.
     *
     * @param json the UTF-8 encoded JSON payload
     * @return an {@link Optional} containing the parsed response, or empty
     *         if the envelope is missing expected fields
     */
    private static Optional<FetchIntegritySignalsMexResponse> of(byte[] json) {
        var jsonObject = JSON.parseObject(json);
        if (jsonObject == null) {
            return Optional.empty();
        }

        var data = jsonObject.getJSONObject("data");
        if (data == null) {
            return Optional.empty();
        }

        var rootArr = data.getJSONArray("xwa2_fetch_wa_users");
        if (rootArr == null || rootArr.isEmpty()) {
            return Optional.empty();
        }

        // l = (t = i.xwa2_fetch_wa_users) == null ? void 0 : t[0]
        var first = rootArr.getJSONObject(0);
        if (first == null) {
            return Optional.empty();
        }

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
