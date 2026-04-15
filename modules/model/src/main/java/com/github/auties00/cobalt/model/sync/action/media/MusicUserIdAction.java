package com.github.auties00.cobalt.model.sync.action.media;

import com.github.auties00.cobalt.model.sync.SyncActionEmptyArgs;
import com.github.auties00.cobalt.model.sync.SyncAction;

import java.util.Collections;
import java.util.Map;
import it.auties.protobuf.annotation.*;
import it.auties.protobuf.model.*;
import java.util.Optional;

/**
 * A sync action that propagates the user's music streaming service identifier
 * across linked devices.
 *
 * <p>The action carries the resolved music user identifier together with a map
 * of provider-specific identifiers, mirroring the {@code SyncActionValue.MusicUserIdAction}
 * message used by WhatsApp Web to share the music linking state established on
 * one device with the user's other linked devices.
 *
 * @implNote WAWebProtobufSyncAction.pb MusicUserIdAction
 */
@ProtobufMessage(name = "SyncActionValue.MusicUserIdAction")
public final class MusicUserIdAction implements SyncAction<SyncActionEmptyArgs> {
    /**
     * Canonical WhatsApp Web action name for this action type.
     */
    public static final String ACTION_NAME = "music_user_id";

    /**
     * Canonical WhatsApp Web action version for this action type.
     */
    public static final int ACTION_VERSION = 1;

    /**
     * {@inheritDoc}
     */
    @Override
    public String actionName() {
        return ACTION_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int actionVersion() {
        return ACTION_VERSION;
    }


    /**
     * The user's primary music user identifier.
     *
     * @implNote WAWebProtobufSyncAction.pb MusicUserIdAction.musicUserId
     */
    @ProtobufProperty(index = 1, type = ProtobufType.STRING)
    String musicUserId;

    /**
     * Map of music provider identifiers (e.g. service name to user identifier)
     * that supplement the primary {@code musicUserId} field.
     *
     * @implNote WAWebProtobufSyncAction.pb MusicUserIdAction.musicUserIdMap
     */
    @ProtobufProperty(index = 2, type = ProtobufType.MAP, mapKeyType = ProtobufType.STRING, mapValueType = ProtobufType.STRING)
    Map<String, String> musicUserIdMap;


    /**
     * Constructs a new {@code MusicUserIdAction} carrying the supplied primary
     * music identifier and provider identifier map.
     *
     * @implNote WAWebProtobufSyncAction.pb MusicUserIdAction
     * @param musicUserId    the primary music user identifier, or {@code null} if unset
     * @param musicUserIdMap the provider identifier map, or {@code null} if unset
     */
    MusicUserIdAction(String musicUserId, Map<String, String> musicUserIdMap) {
        this.musicUserId = musicUserId;
        this.musicUserIdMap = musicUserIdMap;
    }

    /**
     * Returns the primary music user identifier carried by this action.
     *
     * @implNote WAWebProtobufSyncAction.pb MusicUserIdAction.musicUserId
     * @return the music user identifier, or {@link Optional#empty()} if unset
     */
    public Optional<String> musicUserId() {
        return Optional.ofNullable(musicUserId);
    }

    /**
     * Returns the provider identifier map carried by this action.
     *
     * @implNote WAWebProtobufSyncAction.pb MusicUserIdAction.musicUserIdMap
     * @return an unmodifiable view of the provider identifier map, never {@code null}
     */
    public Map<String, String> musicUserIdMap() {
        return musicUserIdMap == null ? Map.of() : Collections.unmodifiableMap(musicUserIdMap);
    }

    /**
     * Sets the primary music user identifier carried by this action.
     *
     * @implNote WAWebProtobufSyncAction.pb MusicUserIdAction.musicUserId
     * @param musicUserId the new music user identifier, or {@code null} to clear it
     */
    public void setMusicUserId(String musicUserId) {
        this.musicUserId = musicUserId;
    }

    /**
     * Sets the provider identifier map carried by this action.
     *
     * @implNote WAWebProtobufSyncAction.pb MusicUserIdAction.musicUserIdMap
     * @param musicUserIdMap the new provider identifier map, or {@code null} to clear it
     */
    public void setMusicUserIdMap(Map<String, String> musicUserIdMap) {
        this.musicUserIdMap = musicUserIdMap;
    }
}
