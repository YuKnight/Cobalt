package com.github.auties00.cobalt.node.mex.json.misc;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.mex.MexOperation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The parsed response for the {@link FetchOHAIKeyConfigMexRequest} MEX
 * query, exposing the OHAI key configuration list returned by the relay.
 *
 * <p>WA Web reduces the raw {@code ohai_configs} array down to the entry
 * with the earliest {@code expiration_date} and projects only the fields
 * needed by {@code WAWebOHAIClient.fetchOHAI}. Cobalt is a library and
 * preserves the raw configuration list so callers can pick the active
 * entry themselves; the field projection inside each entry is identical
 * to the WA Web shape.
 *
 * @implNote WAWebFetchOHAIKeyConfigJob: adapts the JSON root returned by
 * the GraphQL query into a Java value object. WA Web reads
 * {@code data.xwa2_ohai_configurations.ohai_configs} and reduces the
 * array to the entry with the earliest {@code expiration_date}; Cobalt
 * exposes the full list via {@link #ohaiConfigs()}.
 */
@WhatsAppWebModule(moduleName = "WAWebFetchOHAIKeyConfigJob")
public final class FetchOHAIKeyConfigMexResponse implements MexOperation.Response.Json {
    private final List<OhaiConfig> ohaiConfigs;

    private FetchOHAIKeyConfigMexResponse(List<OhaiConfig> ohaiConfigs) {
        this.ohaiConfigs = ohaiConfigs;
    }

    /**
     * Parses a MEX response from the given IQ response node.
     *
     * @implNote WAWebFetchOHAIKeyConfigJob.mexFetchOHAIKeyConfig: WA Web
     * relies on the GraphQL client to unwrap the response. Cobalt
     * performs the unwrapping manually from the IQ {@code <result>}
     * child.
     * @param node the IQ response node received from the relay
     * @return an {@link Optional} containing the parsed response, or empty
     *         if the node is missing a result payload
     */
    @WhatsAppWebExport(moduleName = "WAWebFetchOHAIKeyConfigJob", exports = "mexFetchOHAIKeyConfig",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public static Optional<FetchOHAIKeyConfigMexResponse> of(Node node) {
        return node.getChild("result")
                .flatMap(Node::toContentBytes)
                .flatMap(FetchOHAIKeyConfigMexResponse::of);
    }

    /**
     * Returns the {@code ohai_configs} field carrying the unfiltered list
     * of OHAI key configurations advertised by the relay.
     *
     * @return the list of configurations, empty if the relay returned no
     *         entries
     */
    public List<OhaiConfig> ohaiConfigs() {
        return ohaiConfigs;
    }

    /**
     * Parses a {@link FetchOHAIKeyConfigMexResponse} from the raw JSON
     * bytes of the {@code <result>} child.
     *
     * @implNote WAWebFetchOHAIKeyConfigJob.mexFetchOHAIKeyConfig: mirrors
     * the implicit unwrapping that WA Web performs on the GraphQL
     * response, descending into
     * {@code data.xwa2_ohai_configurations.ohai_configs}.
     * @param json the UTF-8 encoded JSON payload
     * @return an {@link Optional} containing the parsed response, or empty
     *         if the envelope is missing expected fields
     */
    private static Optional<FetchOHAIKeyConfigMexResponse> of(byte[] json) {
        var jsonObject = JSON.parseObject(json);
        if (jsonObject == null) {
            return Optional.empty();
        }

        var data = jsonObject.getJSONObject("data");
        if (data == null) {
            return Optional.empty();
        }

        var root = data.getJSONObject("xwa2_ohai_configurations");
        if (root == null) {
            return Optional.empty();
        }

        var configs = OhaiConfig.ofArray(root.getJSONArray("ohai_configs"));
        return Optional.of(new FetchOHAIKeyConfigMexResponse(configs));
    }

    /**
     * A single OHAI key configuration entry.
     *
     * <p>Mirrors the {@code XWA2OHAIConfig} fragment in
     * {@code WAWebFetchOHAIKeyConfigJobQuery.graphql} which selects
     * {@code aead_id}, {@code expiration_date}, {@code kdf_id},
     * {@code kem_id}, {@code key_id}, {@code last_updated_time} and
     * {@code public_key} as required (THROW-on-null) scalar fields.
     */
    public static final class OhaiConfig {
        private final String aeadId;
        private final String expirationDate;
        private final String kdfId;
        private final String kemId;
        private final String keyId;
        private final String lastUpdatedTime;
        private final String publicKey;

        private OhaiConfig(String aeadId, String expirationDate, String kdfId, String kemId,
                           String keyId, String lastUpdatedTime, String publicKey) {
            this.aeadId = aeadId;
            this.expirationDate = expirationDate;
            this.kdfId = kdfId;
            this.kemId = kemId;
            this.keyId = keyId;
            this.lastUpdatedTime = lastUpdatedTime;
            this.publicKey = publicKey;
        }

        /**
         * Returns the {@code aead_id} field, identifying the AEAD cipher
         * suite used by this OHAI key.
         *
         * @return an {@link Optional} containing the value, or empty if absent
         */
        public Optional<String> aeadId() {
            return Optional.ofNullable(aeadId);
        }

        /**
         * Returns the {@code expiration_date} field, the Unix-time epoch
         * second after which this key is considered expired.
         *
         * @return an {@link Optional} containing the value, or empty if absent
         */
        public Optional<String> expirationDate() {
            return Optional.ofNullable(expirationDate);
        }

        /**
         * Returns the {@code kdf_id} field, identifying the HPKE KDF.
         *
         * @return an {@link Optional} containing the value, or empty if absent
         */
        public Optional<String> kdfId() {
            return Optional.ofNullable(kdfId);
        }

        /**
         * Returns the {@code kem_id} field, identifying the HPKE KEM.
         *
         * @return an {@link Optional} containing the value, or empty if absent
         */
        public Optional<String> kemId() {
            return Optional.ofNullable(kemId);
        }

        /**
         * Returns the {@code key_id} field, the opaque server-assigned
         * identifier of this OHAI key entry.
         *
         * @return an {@link Optional} containing the value, or empty if absent
         */
        public Optional<String> keyId() {
            return Optional.ofNullable(keyId);
        }

        /**
         * Returns the {@code last_updated_time} field, the Unix-time epoch
         * second at which the relay last issued this entry.
         *
         * @return an {@link Optional} containing the value, or empty if absent
         */
        public Optional<String> lastUpdatedTime() {
            return Optional.ofNullable(lastUpdatedTime);
        }

        /**
         * Returns the {@code public_key} field carrying the OHAI public
         * key bytes encoded as a hexadecimal string.
         *
         * @return an {@link Optional} containing the value, or empty if absent
         */
        public Optional<String> publicKey() {
            return Optional.ofNullable(publicKey);
        }

        /**
         * Parses an {@code OhaiConfig} from the given JSON object.
         *
         * @param obj the JSON object to parse
         * @return an {@link Optional} containing the parsed result, or
         *         empty if {@code obj} is {@code null}
         */
        static Optional<OhaiConfig> of(JSONObject obj) {
            if (obj == null) {
                return Optional.empty();
            }

            var aeadId = obj.getString("aead_id");
            var expirationDate = obj.getString("expiration_date");
            var kdfId = obj.getString("kdf_id");
            var kemId = obj.getString("kem_id");
            var keyId = obj.getString("key_id");
            var lastUpdatedTime = obj.getString("last_updated_time");
            var publicKey = obj.getString("public_key");
            return Optional.of(new OhaiConfig(aeadId, expirationDate, kdfId, kemId, keyId, lastUpdatedTime, publicKey));
        }

        /**
         * Parses a list of {@code OhaiConfig} entries from the given JSON
         * array.
         *
         * @param arr the JSON array to parse
         * @return the list of parsed results, empty if {@code arr} is {@code null}
         */
        static List<OhaiConfig> ofArray(JSONArray arr) {
            if (arr == null) {
                return List.of();
            }

            var result = new ArrayList<OhaiConfig>(arr.size());
            for (var i = 0; i < arr.size(); i++) {
                of(arr.getJSONObject(i)).ifPresent(result::add);
            }
            return result;
        }
    }
}
