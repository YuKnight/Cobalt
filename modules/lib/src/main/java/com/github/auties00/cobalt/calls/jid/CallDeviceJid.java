package com.github.auties00.cobalt.calls.jid;

import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.jid.JidServer;

import java.util.Objects;

/**
 * Represents a specific device of a callable user.
 *
 * <p>This is the call layer device address: a {@link CallJid} that always denotes a
 * particular device of an account rather than the bare account. It pairs the wrapped
 * {@link Jid} (which already carries the {@linkplain Jid#device() device identifier})
 * with the {@link CallJidDomainType} discriminator and exposes the device oriented
 * derivations the call layer needs: the owning user JID, the numeric device id, the
 * primary device test, and the hosted to lid compatible form.
 *
 * <p>A device JID is only valid for the device callable domains
 * ({@link CallJidDomainType#CALL}, {@link CallJidDomainType#LID},
 * {@link CallJidDomainType#BOT}, {@link CallJidDomainType#HOSTED_LID}); construction
 * rejects any other domain. Business hosted devices live on the
 * {@link CallJidDomainType#HOSTED_LID hosted.lid} domain and conventionally carry device
 * id {@value #HOSTED_DEVICE_ID}; their underlying account is treated as a
 * {@link CallJidDomainType#LID lid} user, which is reflected by both {@link #userJid()}
 * and {@link #compatibleJid()}. The primary device of any account is device
 * {@value #PRIMARY_DEVICE_ID}.
 *
 * <p>Instances are immutable. Example device JID strings:
 * {@snippet :
 * // a companion LID device (agent 1, device 2)
 * CallDeviceJid lidDevice = CallDeviceJid.of(Jid.of("123456789012345_1:2@lid"));
 * lidDevice.deviceId();       // 2
 * lidDevice.isPrimary();      // false
 * lidDevice.userJid();        // 123456789012345@lid
 *
 * // a business hosted device (hosted.lid, device 99) maps to its lid account
 * CallDeviceJid hosted = CallDeviceJid.of(Jid.of("123456789012345:99@hosted.lid"));
 * hosted.userJid();           // 123456789012345@lid
 * hosted.compatibleJid();     // 123456789012345:99@lid
 * }
 *
 * @implNote This implementation delegates every addressing derivation (owning user JID,
 *           device id, compatible form) to the wrapped {@link Jid} rather than reparsing
 *           the address, since a Cobalt {@link Jid} already carries its agent, device, and
 *           server components.
 * @param jid        the wrapped device bearing JID; never {@code null}
 * @param domainType the voip domain type classification of {@code jid}; never
 *                   {@code null} and always device callable
 * @see CallJid
 * @see CallJidDomainType
 * @see CallParticipantJid
 */
public record CallDeviceJid(Jid jid, CallJidDomainType domainType) {
    /**
     * The conventional device identifier carried by business hosted device JIDs.
     *
     * <p>Both the {@link CallJidDomainType#HOSTED_LID hosted.lid} domain and the non LID
     * hosted domain always address device id {@code 99}.
     */
    public static final int HOSTED_DEVICE_ID = 99;

    /**
     * The device identifier of the primary (default) device of an account.
     *
     * <p>A device JID whose device id equals {@code 0} addresses the primary device.
     */
    public static final int PRIMARY_DEVICE_ID = 0;

    /**
     * Validates the record components during construction.
     *
     * <p>Rejects a {@code null} component and any {@code domainType} that is not
     * device callable, so an instance always names a device on one of the
     * {@link CallJidDomainType#CALL call}, {@link CallJidDomainType#LID lid},
     * {@link CallJidDomainType#BOT bot}, or {@link CallJidDomainType#HOSTED_LID hosted.lid}
     * domains.
     *
     * @throws NullPointerException     if {@code jid} or {@code domainType} is
     *                                  {@code null}
     * @throws IllegalArgumentException if {@code domainType} is not device callable
     */
    public CallDeviceJid {
        Objects.requireNonNull(jid, "jid cannot be null");
        Objects.requireNonNull(domainType, "domainType cannot be null");
        if (!isDeviceCallable(domainType)) {
            throw new IllegalArgumentException("domainType " + domainType + " is not a device callable domain");
        }
    }

    /**
     * Returns a {@code CallDeviceJid} wrapping the given {@link Jid}, deriving its
     * domain type from the JID's server domain.
     *
     * <p>The address is first adapted into a {@link CallJid} carrying the
     * {@link CallJidUseCase#DEVICE device} use case, which resolves the domain type from
     * the {@linkplain Jid#server() server}'s {@link JidServer#type() type}; the resolved
     * domain type must be device callable.
     *
     * @implNote This implementation routes through {@link CallJid#of(Jid, CallJidUseCase)}
     *           with {@link CallJidUseCase#DEVICE} rather than resolving the domain type
     *           alone, so the device use case is classified for every address regardless
     *           of whether the wrapped JID carries an explicit device component.
     * @param jid the device bearing JID to wrap; never {@code null}
     * @return a {@code CallDeviceJid} for the given JID
     * @throws NullPointerException     if {@code jid} is {@code null}
     * @throws IllegalArgumentException if the JID's server domain does not map to a
     *                                  device callable {@link CallJidDomainType}
     */
    public static CallDeviceJid of(Jid jid) {
        Objects.requireNonNull(jid, "jid cannot be null");
        var callJid = CallJid.of(jid, CallJidUseCase.DEVICE);
        return new CallDeviceJid(callJid.jid(), callJid.domainType());
    }

    /**
     * Returns the primary device JID for the given user JID.
     *
     * <p>The primary device is device {@value #PRIMARY_DEVICE_ID} of the account; this
     * strips any agent and device data from {@code userJid} via {@link Jid#withoutData()}
     * and wraps the result. The user JID's server domain must be device callable.
     *
     * @param userJid the owning user JID; never {@code null}
     * @return the primary {@code CallDeviceJid} of the account
     * @throws NullPointerException     if {@code userJid} is {@code null}
     * @throws IllegalArgumentException if the user JID's server domain does not map to a
     *                                  device callable {@link CallJidDomainType}
     */
    public static CallDeviceJid primaryOf(Jid userJid) {
        Objects.requireNonNull(userJid, "userJid cannot be null");
        return of(userJid.withoutData());
    }

    /**
     * Returns the numeric device identifier of this device JID.
     *
     * <p>This is the {@linkplain Jid#device() device component} of the wrapped JID:
     * {@value #PRIMARY_DEVICE_ID} for the primary device and {@value #HOSTED_DEVICE_ID}
     * for business hosted devices.
     *
     * @return the device identifier, an unsigned short (0 to 65535)
     */
    public int deviceId() {
        return jid.device();
    }

    /**
     * Returns whether this JID addresses the primary (default) device of its account.
     *
     * @return {@code true} if the device identifier equals {@value #PRIMARY_DEVICE_ID}
     */
    public boolean isPrimary() {
        return jid.device() == PRIMARY_DEVICE_ID;
    }

    /**
     * Returns the owning user JID of this device.
     *
     * <p>This strips device and agent data and remaps hosted domains to their underlying
     * user domains via {@link Jid#toUserJid()}, so a
     * {@link CallJidDomainType#HOSTED_LID hosted.lid} device resolves to a
     * {@link CallJidDomainType#LID lid} user JID.
     *
     * @return the user JID this device belongs to; never {@code null}
     */
    public Jid userJid() {
        return jid.toUserJid();
    }

    /**
     * Returns the lid compatible device JID form of this device.
     *
     * <p>For a {@link CallJidDomainType#HOSTED_LID hosted.lid} device this derives a
     * {@link CallJidDomainType#LID lid} compatible JID that preserves the device id while
     * moving the address onto the {@code lid} domain. For every other domain the device
     * JID is already in its canonical form and is returned unchanged.
     *
     * @return the lid compatible device JID; never {@code null}
     */
    public Jid compatibleJid() {
        if (domainType == CallJidDomainType.HOSTED_LID) {
            return jid.withServer(JidServer.lid());
        }
        return jid;
    }

    /**
     * Returns whether the given domain type may name a device in the call layer.
     *
     * <p>A device is addressable on the {@link CallJidDomainType#CALL call},
     * {@link CallJidDomainType#LID lid}, {@link CallJidDomainType#BOT bot}, and
     * {@link CallJidDomainType#HOSTED_LID hosted.lid} domains; the phone number and
     * variant {@code s.whatsapp.net} slots, the group, and the newsletter domains are not
     * device callable.
     *
     * @implNote This implementation admits a broader set than
     *           {@link CallJidDomainType#isDeviceLid()}, which omits
     *           {@link CallJidDomainType#CALL}.
     * @param domainType the domain type to test; never {@code null}
     * @return {@code true} if a device JID may carry this domain type
     */
    private static boolean isDeviceCallable(CallJidDomainType domainType) {
        return domainType == CallJidDomainType.CALL
                || domainType == CallJidDomainType.LID
                || domainType == CallJidDomainType.BOT
                || domainType == CallJidDomainType.HOSTED_LID;
    }
}
