package com.github.auties00.cobalt.call.internal;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Temporary diagnostic sink that records one provenance-correct media-crypto dataset per call.
 *
 * <p>For each call it writes a single JSONL file under {@code .temp/call-re/cap-<callId>.jsonl}
 * containing the relay-issued hop-by-hop key, the SRTP master and WARP message-integrity key derived
 * from it via the confirmed two-stage HKDF-SHA256 schedule, the raw {@code <relay>} attributes (to
 * confirm whether {@code warp_mi_tag_len} is present on the wire), and the first several raw inbound
 * SRTP packets. Bundling all of these for one {@code callId} guarantees the key and the packets
 * correspond to the same call, which is the precondition the offline SRTP/WARP analysis needs.
 *
 * @implNote This implementation is throwaway reverse-engineering scaffolding, not production code; it
 * derives keys with the labels proven from the voip wasm ({@code "hbh srtp salt"}/{@code "hbh srtp key"}
 * for SRTP and {@code "warp auth salt"}/{@code "warp auth key"} for the WARP MI tag) and swallows all
 * I/O and crypto errors so it can never disturb the media path.
 */
public final class CallMediaCapture {
    /**
     * Directory the per-call capture files are written to.
     */
    private static final Path DIR = Path.of("C:\\Users\\Alessandro Autiero\\Desktop\\Cobalt1\\.temp\\call-re");

    /**
     * Maximum number of inbound packets recorded per call.
     */
    private static final int MAX_PACKETS = 64;

    /**
     * Per-call inbound-packet counters, keyed by call id.
     */
    private static final ConcurrentHashMap<String, AtomicInteger> COUNTERS = new ConcurrentHashMap<>();

    /**
     * Prevents instantiation of this static-only diagnostic sink.
     */
    private CallMediaCapture() {
    }

    /**
     * Records the hop-by-hop key for a call and the keys derived from it.
     *
     * @param callId the call id the key belongs to
     * @param hbhKey the 30-byte relay hop-by-hop key
     */
    public static void keys(String callId, byte[] hbhKey) {
        if (callId == null || hbhKey == null || hbhKey.length != 30) {
            return;
        }
        try {
            var a = new byte[14];
            var b = new byte[16];
            System.arraycopy(hbhKey, 0, a, 0, 14);
            System.arraycopy(hbhKey, 14, b, 0, 16);
            var zero32 = new byte[32];
            var srtpChain = hkdf(a, zero32, "hbh srtp salt", 32);
            var srtpMaster = hkdf(b, srtpChain, "hbh srtp key", 30);
            var warpChain = hkdf(a, zero32, "warp auth salt", 32);
            var warpKey = hkdf(b, warpChain, "warp auth key", 32);
            append(callId, "{\"kind\":\"keys\",\"callId\":\"" + callId + "\""
                    + ",\"hbhKey\":\"" + hex(hbhKey) + "\""
                    + ",\"srtpChainSalt\":\"" + hex(srtpChain) + "\""
                    + ",\"srtpMaster\":\"" + hex(srtpMaster) + "\""
                    + ",\"warpAuthKey\":\"" + hex(warpKey) + "\"}");
        } catch (Throwable _) {
        }
    }

    /**
     * Records the raw attribute string of the {@code <relay>} node for a call.
     *
     * @param callId the call id the relay block belongs to
     * @param attrs  a human-readable rendering of the relay node attributes and child descriptions
     */
    public static void relay(String callId, String attrs) {
        if (callId == null || attrs == null) {
            return;
        }
        append(callId, "{\"kind\":\"relay\",\"callId\":\"" + callId + "\",\"attrs\":\""
                + attrs.replace("\\", "\\\\").replace("\"", "\\\"") + "\"}");
    }

    /**
     * Records one raw inbound SRTP packet for a call, up to the per-call cap.
     *
     * @param callId the call id the packet arrived on
     * @param packet the raw protected packet bytes as seen by the SRTP handler
     */
    public static void packet(String callId, byte[] packet) {
        if (callId == null || packet == null) {
            return;
        }
        var n = COUNTERS.computeIfAbsent(callId, _ -> new AtomicInteger()).incrementAndGet();
        if (n > MAX_PACKETS) {
            return;
        }
        append(callId, "{\"kind\":\"pkt\",\"callId\":\"" + callId + "\",\"n\":" + n
                + ",\"len\":" + packet.length + ",\"hex\":\"" + hex(packet) + "\"}");
    }

    /**
     * Appends a single JSON line to the capture file for a call, creating it if needed.
     *
     * @param callId the call id selecting the target file
     * @param line   the JSON line to append
     */
    private static synchronized void append(String callId, String line) {
        try {
            Files.createDirectories(DIR);
            var file = DIR.resolve("cap-" + callId.replaceAll("[^A-Za-z0-9_-]", "_") + ".jsonl");
            try (Writer w = Files.newBufferedWriter(file, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                w.write(line);
                w.write('\n');
            }
        } catch (IOException _) {
        }
    }

    /**
     * Performs an HKDF-SHA256 extract-and-expand over the given inputs.
     *
     * @param ikm  the input keying material
     * @param salt the salt (the extract HMAC key)
     * @param info the context/label string, encoded as ASCII
     * @param len  the number of output bytes
     * @return the derived key material
     * @throws Exception if the platform lacks HmacSHA256
     */
    private static byte[] hkdf(byte[] ikm, byte[] salt, String info, int len) throws Exception {
        var mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(salt, "HmacSHA256"));
        var prk = mac.doFinal(ikm);
        var infoBytes = info.getBytes(StandardCharsets.US_ASCII);
        var out = new byte[len];
        var t = new byte[0];
        var pos = 0;
        var counter = 1;
        while (pos < len) {
            mac.init(new SecretKeySpec(prk, "HmacSHA256"));
            mac.update(t);
            mac.update(infoBytes);
            mac.update((byte) counter);
            t = mac.doFinal();
            var take = Math.min(t.length, len - pos);
            System.arraycopy(t, 0, out, pos, take);
            pos += take;
            counter++;
        }
        return out;
    }

    /**
     * Encodes a byte array as a lowercase hex string.
     *
     * @param bytes the bytes to encode
     * @return the hex string
     */
    private static String hex(byte[] bytes) {
        var sb = new StringBuilder(bytes.length * 2);
        for (var x : bytes) {
            sb.append(Character.forDigit((x >> 4) & 0xF, 16)).append(Character.forDigit(x & 0xF, 16));
        }
        return sb.toString();
    }
}
