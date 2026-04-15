package com.github.auties00.cobalt.model.sync.action.business;

import com.github.auties00.cobalt.model.sync.SyncAction;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ProtobufMessage(name = "SyncActionValue.BusinessBroadcastListAction")
public final class BusinessBroadcastListAction implements SyncAction<BusinessBroadcastListActionArgs> {
    /**
     * Canonical WhatsApp Web action name for this action type.
     */
    public static final String ACTION_NAME = "business_broadcast_list";

    /**
     * Canonical WhatsApp Web action version for this action type.
     */
    public static final int ACTION_VERSION = 1;

    /**
     * Canonical WhatsApp Web collection name for this action type.
     */
    public static final SyncPatchType COLLECTION_NAME = SyncPatchType.REGULAR;

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


    @ProtobufProperty(index = 1, type = ProtobufType.BOOL)
    Boolean deleted;

    @ProtobufProperty(index = 2, type = ProtobufType.MESSAGE)
    List<BroadcastListParticipantAction> participants;

    @ProtobufProperty(index = 3, type = ProtobufType.STRING)
    String listName;

    @ProtobufProperty(index = 4, type = ProtobufType.STRING)
    List<String> labelIds;

    /**
     * The compiled audience expression that selects which contacts receive this
     * broadcast list. WhatsApp Web encodes the user-authored audience query as a
     * boolean expression over labels and contact attributes; the resolved
     * expression is persisted alongside the participant snapshot for replay on
     * other devices.
     *
     * @implNote WAWebProtobufSyncAction.pb BusinessBroadcastListAction.audienceExpression
     */
    @ProtobufProperty(index = 5, type = ProtobufType.STRING)
    String audienceExpression;


    BusinessBroadcastListAction(Boolean deleted, List<BroadcastListParticipantAction> participants, String listName, List<String> labelIds, String audienceExpression) {
        this.deleted = deleted;
        this.participants = participants;
        this.listName = listName;
        this.labelIds = labelIds;
        this.audienceExpression = audienceExpression;
    }

    public boolean deleted() {
        return deleted != null && deleted;
    }

    public List<BroadcastListParticipantAction> participants() {
        return participants == null ? List.of() : Collections.unmodifiableList(participants);
    }

    public Optional<String> listName() {
        return Optional.ofNullable(listName);
    }

    public List<String> labelIds() {
        return labelIds == null ? List.of() : Collections.unmodifiableList(labelIds);
    }

    /**
     * Returns the compiled audience expression for this broadcast list.
     *
     * <p>The audience expression encodes the user-authored predicate (e.g. label
     * membership combined with contact attributes) that determines which
     * contacts receive the broadcast. WhatsApp Web persists the resolved
     * expression so the same selection can be reproduced on linked devices.
     *
     * @implNote WAWebProtobufSyncAction.pb BusinessBroadcastListAction.audienceExpression
     * @return the audience expression, or {@link Optional#empty()} if none was supplied
     */
    public Optional<String> audienceExpression() {
        return Optional.ofNullable(audienceExpression);
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public void setParticipants(List<BroadcastListParticipantAction> participants) {
        this.participants = participants;
    }

    public void setListName(String listName) {
        this.listName = listName;
    }

    public void setLabelIds(List<String> labelIds) {
        this.labelIds = labelIds;
    }

    /**
     * Sets the compiled audience expression for this broadcast list.
     *
     * @implNote WAWebProtobufSyncAction.pb BusinessBroadcastListAction.audienceExpression
     * @param audienceExpression the audience expression to store, or {@code null} to clear it
     */
    public void setAudienceExpression(String audienceExpression) {
        this.audienceExpression = audienceExpression;
    }


}
