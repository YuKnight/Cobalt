package com.github.auties00.cobalt.model.privacy;

import com.github.auties00.cobalt.model.jid.Jid;
import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a single privacy preference configured on the current user's WhatsApp account.
 *
 * <p>A privacy setting entry is composed of three parts: the {@link PrivacySettingType}
 * that identifies which setting is being configured (for example last seen, profile picture,
 * status), the {@link PrivacySettingValue} that identifies the audience allowed to see that
 * piece of information (for example everyone, contacts, nobody), and an optional list of
 * {@link Jid} values that refines the audience when the value is an exception or allowlist.
 *
 * <p>The {@code excluded} list has a different meaning depending on the value:
 * <ul>
 *   <li>When the value is {@link PrivacySettingValue#CONTACTS_EXCEPT} it contains the contacts
 *       that are blocked from seeing the information, while all other contacts can see it.</li>
 *   <li>When the value is {@link PrivacySettingValue#CONTACTS_ONLY} it contains the only
 *       contacts that are allowed to see the information.</li>
 *   <li>For every other value the list is empty because the audience is fully determined by
 *       the value alone.</li>
 * </ul>
 *
 * <p>Instances of this class are immutable. The {@code excluded} list returned by
 * {@link #excluded()} is an unmodifiable view.
 */
@ProtobufMessage
public final class PrivacySettingEntry {
    /**
     * The type of setting that this entry configures.
     *
     * <p>Identifies which aspect of the user's profile or activity is being controlled, such
     * as who can see the last seen timestamp or who can add the user to groups.
     */
    @ProtobufProperty(index = 1, type = ProtobufType.ENUM)
    final PrivacySettingType type;

    /**
     * The audience selected for the setting identified by {@link #type}.
     *
     * <p>Determines which users are allowed to see or interact with the piece of information
     * controlled by this entry. The set of values that a given type supports can be queried
     * via {@link PrivacySettingType#supportedValues()}.
     */
    @ProtobufProperty(index = 2, type = ProtobufType.ENUM)
    final PrivacySettingValue value;

    /**
     * The list of contacts that refine the audience selected by {@link #value}.
     *
     * <p>The exact meaning of this list depends on {@link #value}: when the value is
     * {@link PrivacySettingValue#CONTACTS_EXCEPT} it is a blocklist, when the value is
     * {@link PrivacySettingValue#CONTACTS_ONLY} it is an allowlist, and otherwise it is
     * empty.
     */
    @ProtobufProperty(index = 3, type = ProtobufType.STRING)
    final List<Jid> excluded;

    /**
     * Creates a new privacy setting entry with the given type, value and list of contacts.
     *
     * <p>If {@code excluded} is {@code null} it is replaced with an empty list so that
     * {@link #excluded()} never returns {@code null}.
     *
     * @param type     the type of privacy setting being configured, must not be {@code null}
     * @param value    the audience selected for the setting, must not be {@code null}
     * @param excluded the contacts that refine the audience, or {@code null} for an empty list
     * @throws NullPointerException if {@code type} or {@code value} is {@code null}
     */
    PrivacySettingEntry(PrivacySettingType type, PrivacySettingValue value, List<Jid> excluded) {
        this.type = Objects.requireNonNull(type, "type cannot be null");
        this.value = Objects.requireNonNull(value, "value cannot be null");
        this.excluded = Objects.requireNonNullElse(excluded, List.of());
    }

    /**
     * Returns the type of setting configured by this entry.
     *
     * @return the privacy setting type, never {@code null}
     */
    public PrivacySettingType type() {
        return type;
    }

    /**
     * Returns the audience selected for this entry.
     *
     * @return the privacy setting value, never {@code null}
     */
    public PrivacySettingValue value() {
        return value;
    }

    /**
     * Returns an unmodifiable view of the contacts that refine the audience of this entry.
     *
     * <p>The returned list is a blocklist when {@link #value()} is
     * {@link PrivacySettingValue#CONTACTS_EXCEPT}, an allowlist when it is
     * {@link PrivacySettingValue#CONTACTS_ONLY}, and empty otherwise.
     *
     * @return an unmodifiable list of excluded or allowed contacts, never {@code null}
     */
    public List<Jid> excluded() {
        return Collections.unmodifiableList(excluded);
    }

    /**
     * Returns whether this entry uses the {@code excluded} list as a blocklist.
     *
     * <p>This is {@code true} only when {@link #value()} is
     * {@link PrivacySettingValue#CONTACTS_EXCEPT}.
     *
     * @return {@code true} if the entry has an effective blocklist, {@code false} otherwise
     */
    private boolean hasExcluded() {
        return value == PrivacySettingValue.CONTACTS_EXCEPT;
    }

    /**
     * Compares this entry to another object for equality.
     *
     * <p>Two entries are considered equal when they share the same type, the same value and
     * the same list of excluded or allowed contacts in the same order.
     *
     * @param o the object to compare against
     * @return {@code true} if the given object is an equivalent privacy setting entry
     */
    @Override
    public boolean equals(Object o) {
        return o instanceof PrivacySettingEntry that
                && Objects.equals(type, that.type)
                && Objects.equals(value, that.value)
                && Objects.equals(excluded, that.excluded);
    }

    /**
     * Returns a hash code consistent with {@link #equals(Object)}.
     *
     * @return a hash code computed from the type, value and excluded list
     */
    @Override
    public int hashCode() {
        return Objects.hash(type, value, excluded);
    }

    /**
     * Returns a human readable representation of this entry that lists the type, value and
     * excluded contacts.
     *
     * <p>The format is intended for logging and debugging and is not part of the public
     * contract of this class.
     *
     * @return a string representation of this entry
     */
    @Override
    public String toString() {
        return "PrivacySettingEntry[" +
                "type=" + type +
                ", value=" + value +
                ", excluded=" + excluded +
                ']';
    }
}
