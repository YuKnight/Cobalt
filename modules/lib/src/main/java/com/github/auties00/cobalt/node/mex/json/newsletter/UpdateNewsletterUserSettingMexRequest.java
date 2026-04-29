package com.github.auties00.cobalt.node.mex.json.newsletter;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Updates the authenticated user's personal settings for a newsletter.
 *
 * <p>User-scoped newsletter settings include notification mutes and other per-viewer preferences. This mutation applies the supplied setting change and returns the updated setting object.
 *
 * @implNote WAWebMexUpdateNewsletterUserSetting: adapts the {@code mexUpdateNewsletterUserSetting} GraphQL mutation,
 * which in WA Web is invoked via {@code WAWebMexClient.fetchQuery} and
 * whose response is unwrapped by the same module. Cobalt models the request
 * and response as sibling variants of a sealed interface rather than a
 * free-standing async function.
 */
@WhatsAppWebModule(moduleName = "WAWebMexUpdateNewsletterUserSetting")
public final class UpdateNewsletterUserSettingMexRequest implements MexOperation.Request.Json {
    /**
     * The numeric GraphQL query identifier assigned by the WhatsApp relay
     * to the {@code UpdateNewsletterUserSetting} compiled mutation.
     *
     * @implNote WAWebMexUpdateNewsletterUserSettingMutation.graphql: corresponds to the compiled
     * document id registered for the {@code mexUpdateNewsletterUserSetting} mutation.
     */
    public static final String QUERY_ID = "31938993655691868";

    /**
     * The GraphQL operation name reported by WA Web's
     * {@code MexPerfTracker} when dispatching this query, mirroring the
     * {@code params.name} value of the compiled mexUpdateNewsletterUserSetting
     * operation.
     *
     * <p>The constant is exposed through {@link #name()} so
     * call sites can reach the same telemetry tag WA Web emits without
     * duplicating the literal at every dispatch site.
     *
     * @implNote WAWebMexUpdateNewsletterUserSetting: WA Web invokes the operation through
     * {@code WAWebMexClient.fetchQuery} which forwards to
     * {@code WAWebMexNativeClient}; the native client passes the
     * {@code params.name} of the compiled GraphQL artifact to
     * {@code MexPerfTracker.setOperationName}. Cobalt mirrors that
     * scalar verbatim as {@code "mexUpdateNewsletterUserSetting"}.
     */
    public static final String OPERATION_NAME = "mexUpdateNewsletterUserSetting";
    private final String newsletterId;
    private final String type;
    private final String value;

    public UpdateNewsletterUserSettingMexRequest(String newsletterId, String type, String value) {
        this.newsletterId = newsletterId;
        this.type = type;
        this.value = value;
    }

    /**
     * Returns the compiled GraphQL query identifier projected from
     * {@link #QUERY_ID}.
     *
     * @implNote WAWebMexUpdateNewsletterUserSetting: WA Web reads the {@code params.id}
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
     * @implNote WAWebMexUpdateNewsletterUserSetting: WA Web's
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
     * @implNote WAWebMexUpdateNewsletterUserSetting.mexUpdateNewsletterUserSetting: WA Web constructs the
     * {@code variables} object inline as {@code {input: {newsletter_id, type, value}}}
     * and delegates to {@code WAWebMexClient.fetchQuery}. Cobalt writes the JSON directly
     * via {@code fastjson2.JSONWriter} and wraps it through
     * {@link Json#createMexNode(String, String)}.
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         serialised GraphQL variables
     */
    @WhatsAppWebExport(moduleName = "WAWebMexUpdateNewsletterUserSetting", exports = "mexUpdateNewsletterUserSetting",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @Override
    public NodeBuilder toNode() {
        // WAWebMexUpdateNewsletterUserSetting.mexUpdateNewsletterUserSetting
        // Opens a UTF-8 JSON writer that will serialise the GraphQL variables envelope
        try (var writer = JSONWriter.ofUTF8()) {
            // WAWebMexUpdateNewsletterUserSetting.mexUpdateNewsletterUserSetting
            // Begins the outer envelope and the nested "variables" object consumed by WAWebMexClient.fetchQuery
            writer.startObject();
            writer.writeName("variables");
            writer.writeColon();
            writer.startObject();
            // WAWebMexUpdateNewsletterUserSetting.mexUpdateNewsletterUserSetting
            // Emits the nested "input" object: {newsletter_id, type, value}
            writer.writeName("input");
            writer.writeColon();
            writer.startObject();
            if (newsletterId != null) {
                writer.writeName("newsletter_id");
                writer.writeColon();
                writer.writeString(newsletterId);
            }
            if (type != null) {
                writer.writeName("type");
                writer.writeColon();
                writer.writeString(type);
            }
            if (value != null) {
                writer.writeName("value");
                writer.writeColon();
                writer.writeString(value);
            }
            writer.endObject();
            writer.endObject();
            writer.endObject();

            // ADAPTED: WAWebMexUpdateNewsletterUserSetting.mexUpdateNewsletterUserSetting
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
