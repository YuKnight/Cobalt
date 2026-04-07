package com.github.auties00.cobalt.model.sync.action.media;

import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.sync.SyncActionEmptyArgs;
import com.github.auties00.cobalt.model.sync.SyncAction;
import com.github.auties00.cobalt.model.sync.SyncPatchType;

import java.util.Collections;
import java.util.List;
import it.auties.protobuf.annotation.*;
import it.auties.protobuf.model.*;
import java.util.Optional;

@ProtobufMessage(name = "SyncActionValue.StatusPrivacyAction")
public final class StatusPrivacyAction implements SyncAction<SyncActionEmptyArgs> {
    /**
     * Canonical WhatsApp Web action name for this action type.
     */
    public static final String ACTION_NAME = "status_privacy";

    /**
     * Canonical WhatsApp Web action version for this action type.
     */
    public static final int ACTION_VERSION = 7;

    /**
     * Canonical WhatsApp Web collection name for this action type.
     */
    public static final SyncPatchType COLLECTION_NAME = SyncPatchType.REGULAR_HIGH;

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


    @ProtobufProperty(index = 1, type = ProtobufType.ENUM)
    StatusDistributionMode mode;

    @ProtobufProperty(index = 2, type = ProtobufType.STRING)
    List<Jid> userJid;

    /**
     * Whether the user has opted in to cross-posting their status updates to
     * Facebook. Mirrors the {@code shareToFB} flag persisted by WhatsApp Web's
     * status privacy sync action.
     *
     * @implNote WAWebProtobufSyncAction.pb StatusPrivacyAction.shareToFB
     */
    @ProtobufProperty(index = 3, type = ProtobufType.BOOL)
    Boolean shareToFB;

    /**
     * Whether the user has opted in to cross-posting their status updates to
     * Instagram. Mirrors the {@code shareToIG} flag persisted by WhatsApp Web's
     * status privacy sync action.
     *
     * @implNote WAWebProtobufSyncAction.pb StatusPrivacyAction.shareToIG
     */
    @ProtobufProperty(index = 4, type = ProtobufType.BOOL)
    Boolean shareToIG;

    /**
     * The set of named custom audiences this user has defined for status
     * sharing. Each custom list captures an explicit selection of contacts the
     * user can target without falling back to the global allow/deny lists.
     *
     * @implNote WAWebProtobufSyncAction.pb StatusPrivacyAction.customLists
     */
    @ProtobufProperty(index = 5, type = ProtobufType.MESSAGE)
    List<CustomList> customLists;


    StatusPrivacyAction(StatusDistributionMode mode, List<Jid> userJid, Boolean shareToFB, Boolean shareToIG, List<CustomList> customLists) {
        this.mode = mode;
        this.userJid = userJid;
        this.shareToFB = shareToFB;
        this.shareToIG = shareToIG;
        this.customLists = customLists;
    }

    public Optional<StatusDistributionMode> mode() {
        return Optional.ofNullable(mode);
    }

    public List<Jid> userJid() {
        return userJid == null ? List.of() : Collections.unmodifiableList(userJid);
    }

    /**
     * Returns whether status updates should be cross-posted to Facebook.
     *
     * @implNote WAWebProtobufSyncAction.pb StatusPrivacyAction.shareToFB
     * @return the {@code shareToFB} flag, or {@link Optional#empty()} if unset
     */
    public Optional<Boolean> shareToFB() {
        return Optional.ofNullable(shareToFB);
    }

    /**
     * Returns whether status updates should be cross-posted to Instagram.
     *
     * @implNote WAWebProtobufSyncAction.pb StatusPrivacyAction.shareToIG
     * @return the {@code shareToIG} flag, or {@link Optional#empty()} if unset
     */
    public Optional<Boolean> shareToIG() {
        return Optional.ofNullable(shareToIG);
    }

    /**
     * Returns the user-defined custom audiences for status sharing.
     *
     * @implNote WAWebProtobufSyncAction.pb StatusPrivacyAction.customLists
     * @return an unmodifiable view of the custom lists, never {@code null}
     */
    public List<CustomList> customLists() {
        return customLists == null ? List.of() : Collections.unmodifiableList(customLists);
    }

    public void setMode(StatusDistributionMode mode) {
        this.mode = mode;
    }

    public void setUserJid(List<Jid> userJid) {
        this.userJid = userJid;
    }

    /**
     * Sets whether status updates should be cross-posted to Facebook.
     *
     * @implNote WAWebProtobufSyncAction.pb StatusPrivacyAction.shareToFB
     * @param shareToFB the new {@code shareToFB} value, or {@code null} to clear it
     */
    public void setShareToFB(Boolean shareToFB) {
        this.shareToFB = shareToFB;
    }

    /**
     * Sets whether status updates should be cross-posted to Instagram.
     *
     * @implNote WAWebProtobufSyncAction.pb StatusPrivacyAction.shareToIG
     * @param shareToIG the new {@code shareToIG} value, or {@code null} to clear it
     */
    public void setShareToIG(Boolean shareToIG) {
        this.shareToIG = shareToIG;
    }

    /**
     * Sets the custom audience lists for status sharing.
     *
     * @implNote WAWebProtobufSyncAction.pb StatusPrivacyAction.customLists
     * @param customLists the custom lists to store, or {@code null} to clear them
     */
    public void setCustomLists(List<CustomList> customLists) {
        this.customLists = customLists;
    }

    /**
     * The mode that selects which group of recipients can view a status update.
     *
     * @implNote WAWebProtobufSyncAction.pb StatusPrivacyAction.StatusDistributionMode
     */
    @ProtobufEnum(name = "SyncActionValue.StatusPrivacyAction.StatusDistributionMode")
    public enum StatusDistributionMode {
        /**
         * Restricts visibility to the contacts contained in the {@code userJid}
         * allow list.
         *
         * @implNote WAWebProtobufSyncAction.pb StatusPrivacyAction.StatusDistributionMode.ALLOW_LIST
         */
        ALLOW_LIST(0),
        /**
         * Hides the status from the contacts contained in the {@code userJid}
         * deny list while showing it to all other contacts.
         *
         * @implNote WAWebProtobufSyncAction.pb StatusPrivacyAction.StatusDistributionMode.DENY_LIST
         */
        DENY_LIST(1),
        /**
         * Shares the status with every contact in the address book.
         *
         * @implNote WAWebProtobufSyncAction.pb StatusPrivacyAction.StatusDistributionMode.CONTACTS
         */
        CONTACTS(2),
        /**
         * Shares the status with the user's curated close-friends list.
         *
         * @implNote WAWebProtobufSyncAction.pb StatusPrivacyAction.StatusDistributionMode.CLOSE_FRIENDS
         */
        CLOSE_FRIENDS(3),
        /**
         * Shares the status with the recipients enumerated by a named
         * {@link CustomList} entry.
         *
         * @implNote WAWebProtobufSyncAction.pb StatusPrivacyAction.StatusDistributionMode.CUSTOM_LIST
         */
        CUSTOM_LIST(4);

        StatusDistributionMode(@ProtobufEnumIndex int index) {
            this.index = index;
        }

        final int index;

        public int index() {
            return this.index;
        }
    }

    /**
     * A user-defined named audience for status sharing.
     *
     * <p>Each custom list captures a curated selection of contacts the user can
     * target without falling back to the global allow/deny lists. The fields
     * mirror the {@code SyncActionValue.StatusPrivacyAction.CustomList} message
     * from {@code WAWebProtobufSyncAction.pb}.
     *
     * @implNote WAWebProtobufSyncAction.pb StatusPrivacyAction.CustomList
     */
    @ProtobufMessage(name = "SyncActionValue.StatusPrivacyAction.CustomList")
    public static final class CustomList {
        /**
         * The opaque identifier of this custom list as assigned by WhatsApp Web.
         *
         * @implNote WAWebProtobufSyncAction.pb StatusPrivacyAction.CustomList.listId
         */
        @ProtobufProperty(index = 1, type = ProtobufType.STRING)
        String listId;

        /**
         * The display name of this custom list.
         *
         * @implNote WAWebProtobufSyncAction.pb StatusPrivacyAction.CustomList.name
         */
        @ProtobufProperty(index = 2, type = ProtobufType.STRING)
        String name;

        /**
         * The optional emoji glyph associated with this custom list.
         *
         * @implNote WAWebProtobufSyncAction.pb StatusPrivacyAction.CustomList.emoji
         */
        @ProtobufProperty(index = 3, type = ProtobufType.STRING)
        String emoji;

        /**
         * Whether this custom list is the currently selected audience.
         *
         * @implNote WAWebProtobufSyncAction.pb StatusPrivacyAction.CustomList.isSelected
         */
        @ProtobufProperty(index = 4, type = ProtobufType.BOOL)
        Boolean isSelected;

        /**
         * Constructs a new custom list with the supplied identifier, display
         * name, optional emoji and selection flag.
         *
         * @implNote WAWebProtobufSyncAction.pb StatusPrivacyAction.CustomList
         * @param listId     the opaque identifier of the list
         * @param name       the display name of the list
         * @param emoji      the optional emoji glyph for the list
         * @param isSelected whether the list is currently selected
         */
        CustomList(String listId, String name, String emoji, Boolean isSelected) {
            this.listId = listId;
            this.name = name;
            this.emoji = emoji;
            this.isSelected = isSelected;
        }

        /**
         * Returns the opaque identifier of this custom list.
         *
         * @implNote WAWebProtobufSyncAction.pb StatusPrivacyAction.CustomList.listId
         * @return the list identifier, or {@link Optional#empty()} if unset
         */
        public Optional<String> listId() {
            return Optional.ofNullable(listId);
        }

        /**
         * Returns the display name of this custom list.
         *
         * @implNote WAWebProtobufSyncAction.pb StatusPrivacyAction.CustomList.name
         * @return the display name, or {@link Optional#empty()} if unset
         */
        public Optional<String> name() {
            return Optional.ofNullable(name);
        }

        /**
         * Returns the optional emoji glyph for this custom list.
         *
         * @implNote WAWebProtobufSyncAction.pb StatusPrivacyAction.CustomList.emoji
         * @return the emoji glyph, or {@link Optional#empty()} if unset
         */
        public Optional<String> emoji() {
            return Optional.ofNullable(emoji);
        }

        /**
         * Returns whether this custom list is currently selected.
         *
         * @implNote WAWebProtobufSyncAction.pb StatusPrivacyAction.CustomList.isSelected
         * @return the selection flag, or {@link Optional#empty()} if unset
         */
        public Optional<Boolean> isSelected() {
            return Optional.ofNullable(isSelected);
        }

        /**
         * Sets the opaque identifier of this custom list.
         *
         * @implNote WAWebProtobufSyncAction.pb StatusPrivacyAction.CustomList.listId
         * @param listId the new identifier, or {@code null} to clear it
         */
        public void setListId(String listId) {
            this.listId = listId;
        }

        /**
         * Sets the display name of this custom list.
         *
         * @implNote WAWebProtobufSyncAction.pb StatusPrivacyAction.CustomList.name
         * @param name the new display name, or {@code null} to clear it
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * Sets the optional emoji glyph for this custom list.
         *
         * @implNote WAWebProtobufSyncAction.pb StatusPrivacyAction.CustomList.emoji
         * @param emoji the new emoji glyph, or {@code null} to clear it
         */
        public void setEmoji(String emoji) {
            this.emoji = emoji;
        }

        /**
         * Sets whether this custom list is currently selected.
         *
         * @implNote WAWebProtobufSyncAction.pb StatusPrivacyAction.CustomList.isSelected
         * @param isSelected the new selection flag, or {@code null} to clear it
         */
        public void setIsSelected(Boolean isSelected) {
            this.isSelected = isSelected;
        }
    }
}
