package com.github.auties00.cobalt.calls2.common;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;

import java.util.Optional;

/**
 * Parses an uncompressed {@code <voip_settings>} JSON document into a {@link VoipParams}
 * set.
 *
 * <p>The {@code <voip_settings uncompressed="1">} element carries plaintext JSON, not a
 * protobuf or a compressed blob, so this deserializer reads it with the project's JSON
 * library rather than the native pjlib JSON parser the engine uses. The document is a tree
 * of nested objects keyed by the section names ({@code aec}, {@code bwe}, {@code encode},
 * and the rest) plus flat scalar fields and a {@code voip_settings_version} object. This
 * deserializer resolves each modelled {@link VoipParamKey} by following its dotted path
 * through that tree, storing the typed scalar value into the result set; it stores every
 * remaining leaf under its flattened dotted path so no parsed value is dropped.
 *
 * <p>A key's dotted path encodes both its struct namespace and its location in the JSON
 * tree: the namespace prefix ({@code p->}, {@code mvp->}, {@code vp->}, or {@code tp->}) is
 * stripped, and the remainder is split on {@code .} into the sequence of object keys to
 * descend. A modelled key that does not resolve to a present leaf of the matching type is
 * left unset rather than defaulted.
 *
 * <p>The version object's {@code release_type} and {@code version_number} fields are
 * preserved as flattened unmodelled values. They are not promoted to {@link VoipParamKey}
 * constants unless the native descriptor table carries matching dotted paths.
 *
 * @implNote This implementation reproduces the JSON-to-param-tree step
 * ({@code voip_param_deserializer.cc} install plus {@code fill_in_voip_params_private}) of
 * the wa-voip WASM module {@code ff-tScznZ8P} with the project JSON library
 * ({@code com.alibaba.fastjson2}) standing in for the native {@code pjlib-util json.c}
 * parser. The captured document carried the top-level sections {@code aec, bwe, encode,
 * options, rc, rc_dyn, sfu, uaqc, vid_rc} and a {@code voip_settings_version} object with
 * {@code version_number} {@code 151561} (re/calls2-spec/SPEC.md sec 9.3;
 * re/calls2-spec/captures/CAPTURE-FINDINGS.md Q4;
 * re/calls2-spec/parts/rev-common.json algorithms entry {@code JSON -> param tree}).
 */
public final class VoipParamJsonDeserializer {
    /**
     * Constructs a voip-param JSON deserializer.
     *
     * <p>The deserializer is stateless; one instance can parse any number of documents.
     */
    public VoipParamJsonDeserializer() {

    }

    /**
     * Parses the given JSON text into a voip-param set.
     *
     * <p>The text must be the uncompressed JSON body of a {@code <voip_settings>} element,
     * a single JSON object at its root. Every modelled key that resolves to a present leaf
     * of the matching type is stored typed; every other leaf is retained under its
     * flattened dotted path.
     *
     * @param json the uncompressed JSON body of a {@code <voip_settings>} element
     * @return the parsed voip-param set
     * @throws NullPointerException     if {@code json} is {@code null}
     * @throws IllegalArgumentException if {@code json} is not a single JSON object
     */
    public VoipParams parse(String json) {
        if (json == null) {
            throw new NullPointerException("json must not be null");
        }
        JSONObject root;
        try {
            root = JSON.parseObject(json);
        } catch (JSONException exception) {
            throw new IllegalArgumentException("voip_settings body is not valid JSON", exception);
        }
        if (root == null) {
            throw new IllegalArgumentException("voip_settings body is not a JSON object");
        }
        var params = new VoipParams();
        for (var key : VoipParamKey.values()) {
            resolveModelled(key, root, params);
        }
        flattenUnmodelled(root, "", params);
        return params;
    }

    /**
     * Resolves one modelled key against the document and stores its typed value if present.
     *
     * <p>The key's namespace prefix is stripped and the remaining dotted path is followed
     * through the tree. A section-root key (one whose value is a nested object) is not
     * stored as a scalar; only scalar and scalar-array leaves of the matching type are
     * captured.
     *
     * @param key    the modelled key to resolve
     * @param root   the root JSON object of the document
     * @param params the set to store the resolved value into
     */
    private void resolveModelled(VoipParamKey key, JSONObject root, VoipParams params) {
        var path = stripNamespace(key.dottedPath());
        var leaf = resolveLeaf(root, path);
        if (leaf.isEmpty()) {
            return;
        }
        store(key, leaf.get(), params);
    }

    /**
     * Strips the {@code p->}, {@code mvp->}, {@code vp->}, or {@code tp->} namespace prefix
     * from a dotted path.
     *
     * @param dottedPath the fully-qualified dotted path
     * @return the dotted path with its namespace prefix removed
     */
    private String stripNamespace(String dottedPath) {
        var arrow = dottedPath.indexOf("->");
        return arrow < 0 ? dottedPath : dottedPath.substring(arrow + 2);
    }

    /**
     * Follows a dot-separated path through the document and returns the leaf value.
     *
     * <p>Descends one object key at a time; if any intermediate key is missing or is not an
     * object, the path does not resolve.
     *
     * @param root the root JSON object of the document
     * @param path the dot-separated path to follow
     * @return the leaf value, or {@link Optional#empty()} if the path does not resolve
     */
    private Optional<Object> resolveLeaf(JSONObject root, String path) {
        var segments = path.split("\\.");
        JSONObject current = root;
        for (var index = 0; index < segments.length - 1; index++) {
            var next = current.get(segments[index]);
            if (!(next instanceof JSONObject child)) {
                return Optional.empty();
            }
            current = child;
        }
        return Optional.ofNullable(current.get(segments[segments.length - 1]));
    }

    /**
     * Coerces and stores one resolved leaf value under its modelled key.
     *
     * <p>The leaf is coerced to match the key's {@linkplain VoipParamKey#type() value type}:
     * an integer key takes a {@link Number} as a long, a float key takes a {@link Number} as
     * a double, a string key takes a {@link String}, and an array key takes a
     * {@link JSONArray} whose elements are all numbers. A leaf that does not match is skipped.
     *
     * @param key    the modelled key being stored
     * @param leaf   the resolved leaf value
     * @param params the set to store into
     */
    private void store(VoipParamKey key, Object leaf, VoipParams params) {
        switch (key.type()) {
            case INTEGER -> {
                if (leaf instanceof Number number) {
                    params.putInteger(key, number.longValue());
                }
            }
            case FLOAT -> {
                if (leaf instanceof Number number) {
                    params.putDouble(key, number.doubleValue());
                }
            }
            case STRING -> {
                if (leaf instanceof String string) {
                    params.putString(key, string);
                }
            }
            case ARRAY, ARRAY_COUNT -> {
                if (leaf instanceof JSONArray array) {
                    storeArray(key, array, params);
                }
            }
        }
    }

    /**
     * Stores a JSON array leaf as an integer or floating-point array under its key.
     *
     * <p>The array is read as a {@code long[]} when every element is an integral number and
     * as a {@code double[]} otherwise; an array containing a non-numeric element is skipped.
     *
     * @param key    the modelled array key being stored
     * @param array  the resolved JSON array
     * @param params the set to store into
     */
    private void storeArray(VoipParamKey key, JSONArray array, VoipParams params) {
        var allIntegral = true;
        for (var element : array) {
            if (!(element instanceof Number)) {
                return;
            }
            if (!(element instanceof Integer || element instanceof Long || element instanceof Short || element instanceof Byte)) {
                allIntegral = false;
            }
        }
        if (allIntegral) {
            var out = new long[array.size()];
            for (var index = 0; index < out.length; index++) {
                out[index] = ((Number) array.get(index)).longValue();
            }
            params.putIntegerArray(key, out);
        } else {
            var out = new double[array.size()];
            for (var index = 0; index < out.length; index++) {
                out[index] = ((Number) array.get(index)).doubleValue();
            }
            params.putDoubleArray(key, out);
        }
    }

    /**
     * Walks the document and stores every scalar leaf under its flattened dotted path.
     *
     * <p>Nested objects are descended with their key appended to the running prefix joined
     * by {@code .}; the {@code voip_settings_version} object is descended like any other so
     * its fields are retained. Array and object leaves other than descended objects are
     * stored verbatim so the parsed document is preserved in full.
     *
     * @implNote This implementation deliberately retains the {@code rc_dyn} and
     * {@code vid_rc_dyn} dynamic rate-control rule arrays as opaque unmodelled
     * {@link JSONArray} values rather than compiling them into executable
     * {@link VoipParamCondition} plus {@link DynVoipParamUpdater.DynRuleEntry} rules. The
     * captured document carries them as a list of rule objects, each mixing {@code cond_*}
     * condition keys with operand strings (a range as {@code "lo,hi"} with {@code *} or empty
     * meaning unbounded, a mask or scalar as a bare integer) and non-{@code cond_} parameter
     * overrides (re/calls2-spec/captures/voip-settings-full.json {@code rc_dyn}/{@code vid_rc_dyn}),
     * so the operand shapes are partly readable here. Compilation is not done in this
     * deserializer because the full {@code cond_*} family classification is unrecoverable from
     * the registry descriptor (see the TODO on {@link VoipParamCondition.VoipParamConditionKind})
     * and the spec assigns the rule-table wire format and the per-round matcher to the
     * BWE/rate-control reader, not to this glue layer (re/calls2-spec/SPEC.md sec 9.3). The
     * verbatim retention loses no parsed value; that reader compiles the rules from the
     * preserved arrays when it owns the matcher.
     *
     * @param node   the current JSON object being walked
     * @param prefix the running dotted prefix for keys at this depth
     * @param params the set to store unmodelled leaves into
     */
    private void flattenUnmodelled(JSONObject node, String prefix, VoipParams params) {
        for (var entry : node.entrySet()) {
            var path = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            var value = entry.getValue();
            if (value instanceof JSONObject child) {
                flattenUnmodelled(child, path, params);
            } else {
                params.putUnmodelled(path, value);
            }
        }
    }
}
