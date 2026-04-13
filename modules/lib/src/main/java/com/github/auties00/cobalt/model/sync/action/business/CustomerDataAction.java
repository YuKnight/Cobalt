package com.github.auties00.cobalt.model.sync.action.business;

import com.github.auties00.cobalt.model.mixin.InstantSecondsMixin;
import com.github.auties00.cobalt.model.sync.SyncAction;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

import java.time.Instant;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * A sync action representing customer data (CRM) associated with a chat.
 *
 * <p>Per WhatsApp Web {@code WAWebCustomerDataSync}, this action stores
 * business customer relationship data such as contact type, email,
 * phone numbers, birthday, address, acquisition source, lead stage,
 * last order timestamp, and creation/modification timestamps.
 *
 * <p>It is synced via the {@code REGULAR_LOW} collection.
 *
 * @implNote WAWebCustomerDataSync, WAWebProtobufSyncAction.pb — SyncActionValue$CustomerDataAction
 */
@ProtobufMessage(name = "SyncActionValue.CustomerDataAction")
public final class CustomerDataAction implements SyncAction<CustomerDataActionArgs> {
    /**
     * Canonical WhatsApp Web action name for this action type.
     *
     * @implNote WASyncdConst.Actions.CustomerData
     */
    public static final String ACTION_NAME = "customer_data";

    /**
     * Canonical WhatsApp Web action version for this action type.
     *
     * @implNote WAWebCustomerDataSync.getVersion
     */
    public static final int ACTION_VERSION = 1;

    /**
     * Canonical WhatsApp Web collection name for this action type.
     *
     * @implNote WAWebCustomerDataSync.collectionName — CollectionName.RegularLow
     */
    public static final SyncPatchType COLLECTION_NAME = SyncPatchType.REGULAR_LOW;

    @ProtobufProperty(index = 1, type = ProtobufType.STRING)
    final String chatJid;

    @ProtobufProperty(index = 2, type = ProtobufType.INT32)
    final Integer contactType;

    @ProtobufProperty(index = 3, type = ProtobufType.STRING)
    final String email;

    @ProtobufProperty(index = 4, type = ProtobufType.STRING)
    final String altPhoneNumbers;

    @ProtobufProperty(index = 5, type = ProtobufType.INT64, mixins = InstantSecondsMixin.class)
    final Instant birthday;

    @ProtobufProperty(index = 6, type = ProtobufType.STRING)
    final String address;

    @ProtobufProperty(index = 7, type = ProtobufType.INT32)
    final Integer acquisitionSource;

    @ProtobufProperty(index = 8, type = ProtobufType.INT32)
    final Integer leadStage;

    @ProtobufProperty(index = 9, type = ProtobufType.INT64, mixins = InstantSecondsMixin.class)
    final Instant lastOrder;

    @ProtobufProperty(index = 10, type = ProtobufType.INT64, mixins = InstantSecondsMixin.class)
    final Instant createdAt;

    @ProtobufProperty(index = 11, type = ProtobufType.INT64, mixins = InstantSecondsMixin.class)
    final Instant modifiedAt;

    CustomerDataAction(String chatJid, Integer contactType, String email, String altPhoneNumbers,
                       Instant birthday, String address, Integer acquisitionSource, Integer leadStage,
                       Instant lastOrder, Instant createdAt, Instant modifiedAt) {
        this.chatJid = chatJid;
        this.contactType = contactType;
        this.email = email;
        this.altPhoneNumbers = altPhoneNumbers;
        this.birthday = birthday;
        this.address = address;
        this.acquisitionSource = acquisitionSource;
        this.leadStage = leadStage;
        this.lastOrder = lastOrder;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
    }

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
     * Returns the chat JID this customer data is associated with.
     *
     * @return the chat JID, or empty if not set
     */
    public Optional<String> chatJid() {
        return Optional.ofNullable(chatJid);
    }

    /**
     * Returns the contact type.
     *
     * @return the contact type, or empty if not set
     */
    public OptionalInt contactType() {
        return contactType == null ? OptionalInt.empty() : OptionalInt.of(contactType);
    }

    /**
     * Returns the customer email.
     *
     * @return the email, or empty if not set
     */
    public Optional<String> email() {
        return Optional.ofNullable(email);
    }

    /**
     * Returns the customer's alternative phone numbers.
     *
     * @return the alternative phone numbers, or empty if not set
     */
    public Optional<String> altPhoneNumbers() {
        return Optional.ofNullable(altPhoneNumbers);
    }

    /**
     * Returns the customer's birthday.
     *
     * @return the birthday, or empty if not set
     */
    public Optional<Instant> birthday() {
        return Optional.ofNullable(birthday);
    }

    /**
     * Returns the customer's address.
     *
     * @return the address, or empty if not set
     */
    public Optional<String> address() {
        return Optional.ofNullable(address);
    }

    /**
     * Returns the acquisition source.
     *
     * @return the acquisition source, or empty if not set
     */
    public OptionalInt acquisitionSource() {
        return acquisitionSource == null ? OptionalInt.empty() : OptionalInt.of(acquisitionSource);
    }

    /**
     * Returns the lead stage.
     *
     * @return the lead stage, or empty if not set
     */
    public OptionalInt leadStage() {
        return leadStage == null ? OptionalInt.empty() : OptionalInt.of(leadStage);
    }

    /**
     * Returns the last order timestamp.
     *
     * @return the last order time, or empty if not set
     */
    public Optional<Instant> lastOrder() {
        return Optional.ofNullable(lastOrder);
    }

    /**
     * Returns the creation timestamp.
     *
     * @return the creation time, or empty if not set
     */
    public Optional<Instant> createdAt() {
        return Optional.ofNullable(createdAt);
    }

    /**
     * Returns the last modification timestamp.
     *
     * @return the modification time, or empty if not set
     */
    public Optional<Instant> modifiedAt() {
        return Optional.ofNullable(modifiedAt);
    }
}
