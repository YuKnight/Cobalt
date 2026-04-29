package com.github.auties00.cobalt.registration.push.apns.courier;

import com.github.auties00.cobalt.registration.push.apns.plist.Plist;
import com.github.auties00.cobalt.registration.push.apns.plist.value.PlistDataValue;
import com.github.auties00.cobalt.registration.push.apns.plist.value.PlistDictionaryValue;
import com.github.auties00.cobalt.registration.push.apns.plist.value.PlistIntegerValue;
import com.github.auties00.cobalt.registration.push.apns.plist.value.PlistStringValue;

import java.io.IOException;

/**
 * Result of {@code GET http://init-p01st.push.apple.com/bag}: the
 * courier hostname plus the count of replicas Apple is currently
 * advertising. Clients pick a random index in {@code [1, hostCount)}
 * and connect to {@code <index>-<hostname>:443}.
 *
 * @param hostCount how many courier replicas are live (Apple
 *                  typically reports a value between 30 and 50)
 * @param hostname  the suffix of the courier DNS name (typically
 *                  {@code "courier.push.apple.com"})
 */
public record ApnsBag(int hostCount, String hostname) {
    /**
     * Parses the bag response, which is itself a plist whose
     * {@code "bag"} key holds a nested plist with the courier
     * metadata.
     *
     * @param plist the raw plist bytes from the bag endpoint
     * @return the decoded bag
     * @throws IOException if the plist is malformed or missing the
     *                     expected nested keys
     */
    public static ApnsBag ofPlist(byte[] plist) throws IOException {
        try {
            var outer = (PlistDictionaryValue) Plist.parse(plist);
            var bagData = (PlistDataValue) outer.get("bag").orElseThrow();
            var bag = (PlistDictionaryValue) Plist.parse(bagData.toByteArray());
            var hostCount = (int) ((PlistIntegerValue) bag.get("APNSCourierHostcount").orElseThrow()).value();
            var hostname = ((PlistStringValue) bag.get("APNSCourierHostname").orElseThrow()).value();
            return new ApnsBag(hostCount, hostname);
        } catch (Exception e) {
            throw new IOException("Cannot parse APNS bag", e);
        }
    }
}
