package com.github.auties00.cobalt.node.usync;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * One user entry in a {@link UsyncQuery}.
 *
 * <p>A USync request carries a list of user entries. Each entry tells the
 * relay which peer the per-protocol elements apply to. WhatsApp Web accepts
 * four ways to address a peer (by canonical {@link Jid id}, by phone number,
 * by username, or by a phone-JID hint). To make the "at least one addressing
 * identifier" invariant impossible to violate at compile time, instances are
 * created through one of the four {@code by*} static factories instead of a
 * public no-arg constructor.
 *
 * <p>Once created, callers chain {@code with*} setters to attach optional
 * fields such as LID, device-list hashes, persona id, contact type, and
 * trusted-contact token. Per-protocol payload data is read back by each
 * {@code UsyncProtocol.buildUserElement} when the query stanza is assembled.
 */
@WhatsAppWebModule(moduleName = "WAWebUsyncUser")
public final class UsyncUser {
    /**
     * Holds the canonical JID for this user entry.
     */
    private Jid id;

    /**
     * Holds the LID JID for this user entry.
     */
    private Jid lid;

    /**
     * Holds the phone-number JID for this user entry.
     */
    private Jid phoneJid;

    /**
     * Holds the phone number, in E.164 form without the leading {@code +}.
     */
    private String phoneNumber;

    /**
     * Holds the cached device-list hash, base64-encoded.
     */
    private String deviceHash;

    /**
     * Holds the timestamp the local cache last refreshed at.
     */
    private Instant timestamp;

    /**
     * Holds the timestamp the relay should compare against.
     */
    private Instant expectedTimestamp;

    /**
     * Holds the persona id used by the bot-profile protocol.
     */
    private String personaId;

    /**
     * Holds the username used by the contact protocol's username addressing.
     */
    private String username;

    /**
     * Holds the username PIN that accompanies a username addressing.
     */
    private String pin;

    /**
     * Holds the contact-protocol type discriminator, for instance {@code "in"}.
     */
    private String contactType;

    /**
     * Holds the trusted-contact token used by the status protocol.
     */
    private byte[] trustedContactToken;

    /**
     * Hidden constructor that is invoked through the {@code by*} factories.
     */
    private UsyncUser() {
    }

    /**
     * Creates a user entry addressed by canonical JID.
     *
     * @param id the canonical JID; must not be {@code null}
     * @return a fresh entry
     */
    @WhatsAppWebExport(moduleName = "WAWebUsyncUser",
            exports = "USyncUser.withId", adaptation = WhatsAppAdaptation.ADAPTED)
    public static UsyncUser byId(Jid id) {
        Objects.requireNonNull(id, "id cannot be null");
        var user = new UsyncUser();
        user.id = id;
        return user;
    }

    /**
     * Creates a user entry addressed by phone number.
     *
     * @param phoneNumber the phone number, in E.164 form without the leading
     *                    {@code +}; must not be {@code null}
     * @return a fresh entry
     */
    @WhatsAppWebExport(moduleName = "WAWebUsyncUser",
            exports = "USyncUser.withPhone", adaptation = WhatsAppAdaptation.ADAPTED)
    public static UsyncUser byPhoneNumber(String phoneNumber) {
        Objects.requireNonNull(phoneNumber, "phoneNumber cannot be null");
        var user = new UsyncUser();
        user.phoneNumber = phoneNumber;
        return user;
    }

    /**
     * Creates a user entry addressed by username.
     *
     * @param username the username (without leading {@code @}); must not be
     *                 {@code null}
     * @return a fresh entry
     */
    @WhatsAppWebExport(moduleName = "WAWebUsyncUser",
            exports = "USyncUser.withUsername", adaptation = WhatsAppAdaptation.ADAPTED)
    public static UsyncUser byUsername(String username) {
        Objects.requireNonNull(username, "username cannot be null");
        var user = new UsyncUser();
        user.username = username;
        return user;
    }

    /**
     * Creates a user entry addressed by phone-JID hint. The relay maps the
     * hint to a canonical JID before processing the per-protocol elements.
     *
     * @param phoneJid the phone-number JID; must not be {@code null}
     * @return a fresh entry
     */
    @WhatsAppWebExport(moduleName = "WAWebUsyncUser",
            exports = "USyncUser.withPnJid", adaptation = WhatsAppAdaptation.ADAPTED)
    public static UsyncUser byPhoneJid(Jid phoneJid) {
        Objects.requireNonNull(phoneJid, "phoneJid cannot be null");
        var user = new UsyncUser();
        user.phoneJid = phoneJid;
        return user;
    }

    /**
     * Attaches a canonical JID alongside the primary addressing slot.
     *
     * @param id the canonical JID
     * @return this builder
     */
    @WhatsAppWebExport(moduleName = "WAWebUsyncUser",
            exports = "USyncUser.withId", adaptation = WhatsAppAdaptation.DIRECT)
    public UsyncUser withId(Jid id) {
        this.id = id;
        return this;
    }

    /**
     * Attaches the LID identifier for this user.
     *
     * @param lid the LID JID
     * @return this builder
     */
    @WhatsAppWebExport(moduleName = "WAWebUsyncUser",
            exports = "USyncUser.withLid", adaptation = WhatsAppAdaptation.DIRECT)
    public UsyncUser withLid(Jid lid) {
        this.lid = lid;
        return this;
    }

    /**
     * Attaches a phone-JID hint.
     *
     * @param phoneJid the phone-number JID
     * @return this builder
     */
    @WhatsAppWebExport(moduleName = "WAWebUsyncUser",
            exports = "USyncUser.withPnJid", adaptation = WhatsAppAdaptation.DIRECT)
    public UsyncUser withPhoneJid(Jid phoneJid) {
        this.phoneJid = phoneJid;
        return this;
    }

    /**
     * Sets the cached device-list hash. Used by the device protocol to obtain
     * a {@code <devices type="omitted">} response when the local cache is
     * still in sync with the server.
     *
     * @param deviceHash the hash, base64-encoded
     * @return this builder
     */
    @WhatsAppWebExport(moduleName = "WAWebUsyncUser",
            exports = "USyncUser.withDeviceHash", adaptation = WhatsAppAdaptation.ADAPTED)
    public UsyncUser withDeviceHash(String deviceHash) {
        this.deviceHash = (deviceHash == null || deviceHash.isBlank()) ? null : deviceHash;
        return this;
    }

    /**
     * Sets the timestamp the local cache last refreshed at.
     *
     * @param timestamp the cache timestamp
     * @return this builder
     */
    @WhatsAppWebExport(moduleName = "WAWebUsyncUser",
            exports = "USyncUser.withTs", adaptation = WhatsAppAdaptation.ADAPTED)
    public UsyncUser withTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    /**
     * Sets the expected timestamp the relay should compare against.
     *
     * @param expectedTimestamp the expected timestamp
     * @return this builder
     */
    @WhatsAppWebExport(moduleName = "WAWebUsyncUser",
            exports = "USyncUser.withExpectedTs", adaptation = WhatsAppAdaptation.ADAPTED)
    public UsyncUser withExpectedTimestamp(Instant expectedTimestamp) {
        this.expectedTimestamp = expectedTimestamp;
        return this;
    }

    /**
     * Sets the persona id used by the bot-profile protocol.
     *
     * @param personaId the persona identifier
     * @return this builder
     */
    @WhatsAppWebExport(moduleName = "WAWebUsyncUser",
            exports = "USyncUser.withPersonaId", adaptation = WhatsAppAdaptation.DIRECT)
    public UsyncUser withPersonaId(String personaId) {
        this.personaId = personaId;
        return this;
    }

    /**
     * Sets the username PIN accompanying a username addressing scheme.
     *
     * @param pin the username PIN
     * @return this builder
     */
    @WhatsAppWebExport(moduleName = "WAWebUsyncUser",
            exports = "USyncUser.withUsernameKey", adaptation = WhatsAppAdaptation.DIRECT)
    public UsyncUser withPin(String pin) {
        this.pin = pin;
        return this;
    }

    /**
     * Sets the contact-protocol type discriminator.
     *
     * @param contactType the type literal, for instance {@code "in"} or
     *                    {@code "out"}
     * @return this builder
     */
    @WhatsAppWebExport(moduleName = "WAWebUsyncUser",
            exports = "USyncUser.withType", adaptation = WhatsAppAdaptation.DIRECT)
    public UsyncUser withContactType(String contactType) {
        this.contactType = contactType;
        return this;
    }

    /**
     * Sets the trusted-contact token attached to status queries.
     *
     * @param trustedContactToken the raw token bytes
     * @return this builder
     */
    @WhatsAppWebExport(moduleName = "WAWebUsyncUser",
            exports = "USyncUser.withTcToken", adaptation = WhatsAppAdaptation.DIRECT)
    public UsyncUser withTrustedContactToken(byte[] trustedContactToken) {
        this.trustedContactToken = trustedContactToken;
        return this;
    }

    /**
     * Returns the canonical JID, when present.
     *
     * @return the canonical JID
     */
    @WhatsAppWebExport(moduleName = "WAWebUsyncUser",
            exports = "USyncUser.getId", adaptation = WhatsAppAdaptation.DIRECT)
    public Optional<Jid> id() {
        return Optional.ofNullable(id);
    }

    /**
     * Returns the LID, when present.
     *
     * @return the LID
     */
    @WhatsAppWebExport(moduleName = "WAWebUsyncUser",
            exports = "USyncUser.getLid", adaptation = WhatsAppAdaptation.DIRECT)
    public Optional<Jid> lid() {
        return Optional.ofNullable(lid);
    }

    /**
     * Returns the phone-number JID, when present.
     *
     * @return the phone-number JID
     */
    @WhatsAppWebExport(moduleName = "WAWebUsyncUser",
            exports = "USyncUser.getPnJid", adaptation = WhatsAppAdaptation.DIRECT)
    public Optional<Jid> phoneJid() {
        return Optional.ofNullable(phoneJid);
    }

    /**
     * Returns the phone number, when present.
     *
     * @return the phone number
     */
    @WhatsAppWebExport(moduleName = "WAWebUsyncUser",
            exports = "USyncUser.getPhone", adaptation = WhatsAppAdaptation.DIRECT)
    public Optional<String> phoneNumber() {
        return Optional.ofNullable(phoneNumber);
    }

    /**
     * Returns the cached device-list hash, when present.
     *
     * @return the device-list hash
     */
    @WhatsAppWebExport(moduleName = "WAWebUsyncUser",
            exports = "USyncUser.getDeviceHash", adaptation = WhatsAppAdaptation.DIRECT)
    public Optional<String> deviceHash() {
        return Optional.ofNullable(deviceHash);
    }

    /**
     * Returns the cache timestamp, when present.
     *
     * @return the cache timestamp
     */
    @WhatsAppWebExport(moduleName = "WAWebUsyncUser",
            exports = "USyncUser.getTs", adaptation = WhatsAppAdaptation.DIRECT)
    public Optional<Instant> timestamp() {
        return Optional.ofNullable(timestamp);
    }

    /**
     * Returns the expected timestamp, when present.
     *
     * @return the expected timestamp
     */
    @WhatsAppWebExport(moduleName = "WAWebUsyncUser",
            exports = "USyncUser.getExpectedTs", adaptation = WhatsAppAdaptation.DIRECT)
    public Optional<Instant> expectedTimestamp() {
        return Optional.ofNullable(expectedTimestamp);
    }

    /**
     * Returns the persona id, when present.
     *
     * @return the persona id
     */
    @WhatsAppWebExport(moduleName = "WAWebUsyncUser",
            exports = "USyncUser.getPersonaId", adaptation = WhatsAppAdaptation.DIRECT)
    public Optional<String> personaId() {
        return Optional.ofNullable(personaId);
    }

    /**
     * Returns the username, when present.
     *
     * @return the username
     */
    @WhatsAppWebExport(moduleName = "WAWebUsyncUser",
            exports = "USyncUser.getUsername", adaptation = WhatsAppAdaptation.DIRECT)
    public Optional<String> username() {
        return Optional.ofNullable(username);
    }

    /**
     * Returns the username PIN, when present.
     *
     * @return the username PIN
     */
    @WhatsAppWebExport(moduleName = "WAWebUsyncUser",
            exports = "USyncUser.getPin", adaptation = WhatsAppAdaptation.DIRECT)
    public Optional<String> pin() {
        return Optional.ofNullable(pin);
    }

    /**
     * Returns the contact-protocol type, when present.
     *
     * @return the contact-protocol type
     */
    @WhatsAppWebExport(moduleName = "WAWebUsyncUser",
            exports = "USyncUser.getType", adaptation = WhatsAppAdaptation.DIRECT)
    public Optional<String> contactType() {
        return Optional.ofNullable(contactType);
    }

    /**
     * Returns the trusted-contact token, when present.
     *
     * @return the trusted-contact token
     */
    @WhatsAppWebExport(moduleName = "WAWebUsyncUser",
            exports = "USyncUser.getTcToken", adaptation = WhatsAppAdaptation.DIRECT)
    public Optional<byte[]> trustedContactToken() {
        return Optional.ofNullable(trustedContactToken);
    }
}
