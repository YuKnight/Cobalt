package com.github.auties00.cobalt.props;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.model.jid.Jid;

import java.util.List;
import java.util.Objects;

/**
 * Carries the projected payload of a successful
 * {@code getGroupAbPropsProtocol} call.
 *
 * <p>Mirrors the JS object literal returned by
 * {@code WAResultOrError.makeResult({groupJid, hash, refresh, refreshId, props})}
 * inside {@code WAGetGroupAbPropsProtocol.s}, with each
 * {@code Optional} field defaulting to empty when the relay omitted
 * the corresponding attribute.
 *
 * @param groupJid  the group JID echoed back from the request
 * @param hash      the relay-returned content hash, or {@code null}
 *                  when omitted
 * @param refresh   the relay-returned refresh-cooldown hint in
 *                  seconds, or {@code null} when omitted
 * @param refreshId the relay-returned refresh id, or {@code null}
 *                  when omitted
 * @param props     the projected experiment-config list, never
 *                  {@code null}
 */
@WhatsAppWebModule(moduleName = "WAGetGroupAbPropsProtocol")
public record GroupAbPropsResult(Jid groupJid, String hash, Integer refresh, Integer refreshId, List<Entry> props) {
    /**
     * Constructs a new result, defensively copying the prop list and
     * rejecting a {@code null} {@code groupJid}.
     *
     * @param groupJid  the group JID. Never {@code null}
     * @param hash      the relay-returned hash. May be {@code null}
     * @param refresh   the relay-returned refresh-cooldown. May be
     *                  {@code null}
     * @param refreshId the relay-returned refresh id. May be
     *                  {@code null}
     * @param props     the projected entry list. Never {@code null}
     * @throws NullPointerException if {@code groupJid} or
     *                              {@code props} is {@code null}
     */
    public GroupAbPropsResult {
        Objects.requireNonNull(groupJid, "groupJid cannot be null");
        props = List.copyOf(props);
    }

    /**
     * Carries one {@code ExperimentConfig <prop>} child from a
     * {@code getGroupAbPropsProtocol} response.
     *
     * <p>Mirrors the JS object literal pushed by
     * {@code WAGetGroupAbPropsProtocol.c} when an entry's mixin name
     * is {@code "ExperimentConfig"}.
     *
     * @param configCode    the numeric experiment code
     * @param configValue   the experiment value as a raw string. Never
     *                      {@code null}
     * @param configExpoKey the optional exposure key as a string, or
     *                      {@code null} when the relay omitted it
     */
    @WhatsAppWebModule(moduleName = "WAGetGroupAbPropsProtocol")
    public record Entry(int configCode, String configValue, String configExpoKey) {
        /**
         * Constructs a new entry, rejecting a {@code null}
         * {@code configValue}.
         *
         * @param configCode    the numeric experiment code
         * @param configValue   the experiment value. Never {@code null}
         * @param configExpoKey the optional exposure key, or
         *                      {@code null}
         * @throws NullPointerException if {@code configValue} is
         *                              {@code null}
         */
        public Entry {
            Objects.requireNonNull(configValue, "configValue cannot be null");
        }
    }
}
