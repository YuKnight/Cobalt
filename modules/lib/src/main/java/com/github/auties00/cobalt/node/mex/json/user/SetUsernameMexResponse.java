package com.github.auties00.cobalt.node.mex.json.user;

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
import java.util.Objects;
import java.util.Optional;

/**
 * The parsed response for this MEX mutation.
 */
public final class SetUsernameMexResponse implements MexOperation.Response.Json {
    private final String result;

    private SetUsernameMexResponse(String result) {
        this.result = result;
    }

    /**
     * Parses the MEX response carried by an inbound IQ stanza.
     *
     * @implNote WAWebMexSetUsernameJob.mexSetUsernameQueryJob: reads the
     * {@code result} status token from the mutation payload.
     * @param node the inbound IQ stanza carrying the {@code <result>} child
     * @return the parsed response, or {@code Optional.empty()} if the
     *         expected JSON shape is absent
     */
    @WhatsAppWebExport(moduleName = "WAWebMexSetUsernameJob", exports = "mexSetUsernameQueryJob",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public static Optional<SetUsernameMexResponse> of(Node node) {
        return node.getChild("result")
                .flatMap(Node::toContentBytes)
                .flatMap(SetUsernameMexResponse::of);
    }

    /**
     * Returns the {@code result} field.
     *
     * @return an {@link Optional} containing the value, or empty if absent
     */
    public Optional<String> result() {
        return Optional.ofNullable(result);
    }

    /**
     * Returns whether the username mutation succeeded.
     *
     * <p>Mirrors the WA Web {@code mexSetUsernameQueryJob} return value,
     * which evaluates {@code result?.xwa2_username_set?.result === "SUCCESS"}
     * after awaiting the relay response.
     *
     * @implNote WAWebMexSetUsernameJob.mexSetUsernameQueryJob: returns
     * {@code ((a=l.xwa2_username_set)==null?void 0:a.result)==="SUCCESS"}.
     * @return {@code true} if the {@code result} field equals
     *         {@code "SUCCESS"}, {@code false} otherwise (including when
     *         the field is absent)
     */
    @WhatsAppWebExport(moduleName = "WAWebMexSetUsernameJob", exports = "mexSetUsernameQueryJob",
            adaptation = WhatsAppAdaptation.DIRECT)
    public boolean isSuccess() {
        // WAWebMexSetUsernameJob.mexSetUsernameQueryJob: (a==null?void 0:a.result)==="SUCCESS"
        return Objects.equals(result, "SUCCESS");
    }

    private static Optional<SetUsernameMexResponse> of(byte[] json) {
        var jsonObject = JSON.parseObject(json);
        if (jsonObject == null) {
            return Optional.empty();
        }

        var data = jsonObject.getJSONObject("data");
        if (data == null) {
            return Optional.empty();
        }

        var root = data.getJSONObject("xwa2_username_set");
        if (root == null) {
            return Optional.empty();
        }

        var result = root.getString("result");

        return Optional.of(new SetUsernameMexResponse(result));
    }
}
