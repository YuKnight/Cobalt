package com.github.auties00.cobalt.node.mex.json.community;

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
 * The parsed response for this MEX mutation.
 */
public final class TransferCommunityOwnershipMexResponse implements MexOperation.Response.Json {
    private final String groupId;
    private final LidMigrationState lidMigrationState;

    private TransferCommunityOwnershipMexResponse(String groupId, LidMigrationState lidMigrationState) {
        this.groupId = groupId;
        this.lidMigrationState = lidMigrationState;
    }

    /**
     * Parses the MEX response carried by an inbound IQ stanza.
     *
     * @implNote WAWebMexTransferCommunityOwnershipJob.mexTransferCommunityOwnershipJob:
     * reads {@code data.xwa2_group_update_users_role.group_id} and the
     * {@code lid_migration_state} sub-object.
     * @param node the inbound IQ stanza carrying the {@code <result>} child
     * @return the parsed response, or {@code Optional.empty()} if the
     *         expected JSON shape is absent
     */
    @WhatsAppWebExport(moduleName = "WAWebMexTransferCommunityOwnershipJob", exports = "mexTransferCommunityOwnershipJob",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public static Optional<TransferCommunityOwnershipMexResponse> of(Node node) {
        return node.getChild("result")
                .flatMap(Node::toContentBytes)
                .flatMap(TransferCommunityOwnershipMexResponse::of);
    }

    /**
     * Returns the {@code group_id} field.
     *
     * @return an {@link Optional} containing the value, or empty if absent
     */
    public Optional<String> groupId() {
        return Optional.ofNullable(groupId);
    }

    /**
     * Returns the {@code lid_migration_state} field.
     *
     * @return an {@link Optional} containing the value, or empty if absent
     */
    public Optional<LidMigrationState> lidMigrationState() {
        return Optional.ofNullable(lidMigrationState);
    }

    /**
     * A parsed {@code LidMigrationState} object.
     */
    public static final class LidMigrationState {
        private final String addressingMode;

        private LidMigrationState(String addressingMode) {
            this.addressingMode = addressingMode;
        }

        /**
         * Returns the {@code addressing_mode} field.
         *
         * @return an {@link Optional} containing the value, or empty if absent
         */
        public Optional<String> addressingMode() {
            return Optional.ofNullable(addressingMode);
        }

        /**
         * Parses a {@code LidMigrationState} from the given JSON object.
         *
         * @param obj the JSON object to parse
         * @return an {@link Optional} containing the parsed result, or empty if {@code obj} is {@code null}
         */
        static Optional<LidMigrationState> of(JSONObject obj) {
            if (obj == null) {
                return Optional.empty();
            }

            var addressingMode = obj.getString("addressing_mode");
            return Optional.of(new LidMigrationState(addressingMode));
        }

        /**
         * Parses a list of {@code LidMigrationState} from the given JSON array.
         *
         * @param arr the JSON array to parse
         * @return the list of parsed results, empty if {@code arr} is {@code null}
         */
        static List<LidMigrationState> ofArray(JSONArray arr) {
            if (arr == null) {
                return List.of();
            }

            var result = new ArrayList<LidMigrationState>(arr.size());
            for (var i = 0; i < arr.size(); i++) {
                of(arr.getJSONObject(i)).ifPresent(result::add);
            }
            return result;
        }
    }

    private static Optional<TransferCommunityOwnershipMexResponse> of(byte[] json) {
        var jsonObject = JSON.parseObject(json);
        if (jsonObject == null) {
            return Optional.empty();
        }

        var data = jsonObject.getJSONObject("data");
        if (data == null) {
            return Optional.empty();
        }

        var root = data.getJSONObject("xwa2_group_update_users_role");
        if (root == null) {
            return Optional.empty();
        }

        var groupId = root.getString("group_id");
        var lidMigrationState = LidMigrationState.of(root.getJSONObject("lid_migration_state")).orElse(null);

        return Optional.of(new TransferCommunityOwnershipMexResponse(groupId, lidMigrationState));
    }
}
