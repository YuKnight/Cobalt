package com.github.auties00.cobalt.model.contact;

import com.github.auties00.cobalt.model.jid.Jid;
import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

import java.util.Objects;
import java.util.Optional;

/**
 * A model representing an outgoing contact entry used by the WhatsApp Web
 * "invite by contact" feature.
 *
 * <p>An outgoing contact is a locally stored reference to a phone-number
 * identified person used by the macOS WhatsApp Web client to invite new users
 * to WhatsApp. Each record stores only the contact's {@link Jid}, full name,
 * and first name. Outgoing contacts are managed independently from regular
 * {@link Contact} records: they live in their own dedicated store and are
 * never merged into the address-book contact collection, mirroring WhatsApp
 * Web's separation between the {@code contact} and {@code out-contact}
 * IndexedDB tables.
 *
 * <p>This class is a local model only. Modifying its fields does not send any
 * request to the WhatsApp servers; it simply reflects the locally cached state
 * synchronised through the {@code regular_low} app state collection by the
 * {@link com.github.auties00.cobalt.sync.handler.OutContactHandler outgoing
 * contact sync handler}.
 *
 * @implNote WAWebSchemaOutContact.addTable — defines the {@code out-contact}
 *           IndexedDB table with columns {@code (id, fullName, firstName)}
 *           accessed exclusively by {@code WAWebDBOutContactDatabaseApi}
 * @see Contact
 */
@ProtobufMessage
public final class OutContact {
    /**
     * The non-{@code null} phone-number-based JID that uniquely identifies this
     * outgoing contact. In the WhatsApp Web {@code out-contact} database, this
     * corresponds to the primary key ({@code id}) column. The JID encodes the
     * contact's phone number and server information.
     *
     * @implNote WAWebSchemaOutContact.addTable — {@code id} primary key column
     */
    @ProtobufProperty(index = 1, type = ProtobufType.STRING)
    final Jid jid;

    /**
     * The full name associated with this outgoing contact, as supplied by the
     * sync action that created the entry. In the WhatsApp Web
     * {@code out-contact} database, this corresponds to the {@code fullName}
     * column. This value is {@code null} if no full name was provided by the
     * sync action.
     *
     * @implNote WAWebSchemaOutContact.addTable — {@code fullName} column
     */
    @ProtobufProperty(index = 2, type = ProtobufType.STRING)
    String fullName;

    /**
     * The first name associated with this outgoing contact, either explicitly
     * supplied by the sync action or derived from the first whitespace-delimited
     * token of the {@linkplain #fullName() full name}. In the WhatsApp Web
     * {@code out-contact} database, this corresponds to the {@code firstName}
     * column. This value is {@code null} if neither value was available.
     *
     * @implNote WAWebSchemaOutContact.addTable — {@code firstName} column
     */
    @ProtobufProperty(index = 3, type = ProtobufType.STRING)
    String firstName;

    /**
     * Constructs a new outgoing contact with the given field values.
     *
     * @param jid       the non-{@code null} JID uniquely identifying this
     *                  outgoing contact
     * @param fullName  the full name to associate with this outgoing contact,
     *                  or {@code null}
     * @param firstName the first name to associate with this outgoing contact,
     *                  or {@code null}
     * @implNote WAWebSchemaOutContact.addTable — record constructor matching
     *           the {@code (id, fullName, firstName)} table columns
     */
    OutContact(Jid jid, String fullName, String firstName) {
        this.jid = Objects.requireNonNull(jid, "jid cannot be null");
        this.fullName = fullName;
        this.firstName = firstName;
    }

    /**
     * Returns the non-{@code null} JID that uniquely identifies this outgoing
     * contact.
     *
     * @return the outgoing contact's phone-number-based JID
     * @implNote WAWebSchemaOutContact.addTable — accessor for the {@code id}
     *           primary key column
     */
    public Jid jid() {
        return this.jid;
    }

    /**
     * Returns the full name associated with this outgoing contact.
     *
     * @return an {@code Optional} containing the full name, or empty if not
     *         available
     * @implNote WAWebSchemaOutContact.addTable — accessor for the
     *           {@code fullName} column
     */
    public Optional<String> fullName() {
        return Optional.ofNullable(this.fullName);
    }

    /**
     * Sets the full name for this outgoing contact.
     *
     * @param fullName the full name to set, or {@code null} to clear
     * @return this outgoing contact instance for method chaining
     * @implNote WAWebSchemaOutContact.addTable — mutator for the
     *           {@code fullName} column
     */
    public OutContact setFullName(String fullName) {
        this.fullName = fullName;
        return this;
    }

    /**
     * Returns the first name associated with this outgoing contact.
     *
     * @return an {@code Optional} containing the first name, or empty if not
     *         available
     * @implNote WAWebSchemaOutContact.addTable — accessor for the
     *           {@code firstName} column
     */
    public Optional<String> firstName() {
        return Optional.ofNullable(this.firstName);
    }

    /**
     * Sets the first name for this outgoing contact.
     *
     * @param firstName the first name to set, or {@code null} to clear
     * @return this outgoing contact instance for method chaining
     * @implNote WAWebSchemaOutContact.addTable — mutator for the
     *           {@code firstName} column
     */
    public OutContact setFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    /**
     * Returns a hash code based on this outgoing contact's {@linkplain #jid()
     * JID}.
     *
     * @return the hash code of the JID
     * @implNote WAWebSchemaOutContact.addTable — equality is keyed on the
     *           {@code id} primary key
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(this.jid);
    }

    /**
     * Returns whether this outgoing contact is equal to the given object. Two
     * outgoing contacts are considered equal if they have the same
     * {@linkplain #jid() JID}.
     *
     * @param other the object to compare with
     * @return {@code true} if the other object is an {@code OutContact} with
     *         the same JID
     * @implNote WAWebSchemaOutContact.addTable — equality is keyed on the
     *           {@code id} primary key
     */
    @Override
    public boolean equals(Object other) {
        return other instanceof OutContact that && Objects.equals(this.jid, that.jid);
    }
}
