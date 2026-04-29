package com.github.auties00.cobalt.registration.push.apns.activation;

import com.github.auties00.cobalt.registration.push.apns.plist.Plist;
import com.github.auties00.cobalt.registration.push.apns.plist.value.PlistDataValue;
import com.github.auties00.cobalt.registration.push.apns.plist.value.PlistDictionaryValue;

import java.io.IOException;

/**
 * Decoded {@code <Protocol>} plist returned from
 * {@code albert.apple.com/.../deviceActivation}. Cobalt only consumes
 * the inner {@code DeviceCertificate} (an X.509 cert signed by Apple's
 * iPhone Device CA, valid for ~3 years). The other plist keys
 * ({@code ack-received}, {@code show-settings}) are read for sanity
 * but never consulted afterwards.
 *
 * @param deviceCertificate DER bytes of the Apple-signed device cert
 */
public record ApnsActivationInfo(byte[] deviceCertificate) {
    /**
     * Parses the {@code <Protocol>} plist into the structured
     * record, surfacing the {@code DeviceCertificate} bytes.
     *
     * @param plist the UTF-8 plist bytes from the activation
     *              response
     * @return the decoded activation info
     * @throws IOException if the plist is malformed or missing the
     *                     expected nested keys
     */
    public static ApnsActivationInfo ofPlist(byte[] plist) throws IOException {
        try {
            var root = (PlistDictionaryValue) Plist.parse(plist);
            var deviceActivation = (PlistDictionaryValue) root.get("device-activation").orElseThrow();
            var activationRecord = (PlistDictionaryValue) deviceActivation.get("activation-record").orElseThrow();
            var deviceCertificate = (PlistDataValue) activationRecord.get("DeviceCertificate").orElseThrow();
            return new ApnsActivationInfo(deviceCertificate.toByteArray());
        } catch (Exception e) {
            throw new IOException("Cannot parse device activation info", e);
        }
    }
}
