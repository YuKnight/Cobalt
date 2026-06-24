package com.github.auties00.cobalt.calls2.common;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

/**
 * Enumerates the video decoder formats a device advertises in its video-decode
 * capability descriptor.
 *
 * <p>Each device advertises which video codecs it can decode through a comma-separated
 * token string (the {@code vid_dec} attribute) carried alongside its capability bitset
 * and embedded in offer, accept, and group-info messages. The engine parses that token
 * string into a codec bitmask, and during negotiation it intersects the local mask with
 * the peer's advertised mask and selects the surviving codec of highest priority. The
 * recognized formats are {@link #H264}, {@link #H265}, {@link #VP8}, {@link #VP9}, and
 * {@link #AV1}.
 *
 * <p>Each constant carries the {@link #token() wire token} used in the descriptor string
 * and a {@link #priority() priority} that orders codecs during negotiation; a higher
 * priority is preferred when more than one codec is common to both sides. When a
 * descriptor string fails to parse, the engine falls back to {@link #H264} only; this
 * enum exposes that behaviour through {@link #parse(String)}, which returns a set
 * containing only {@link #H264} for any unrecognized or malformed input.
 *
 * @implNote This implementation ports the {@code vid_dec} codec vocabulary and the
 * intersect-and-take-max-priority negotiation ({@code combine_vid_codec_capability_and_prio},
 * fn11246) of the wa-voip WASM module {@code ff-tScznZ8P}. The bitmask bit assignments are
 * recovered from the codec-type cascade {@code pjmedia_vid_codec_name_str_to_codec_type}
 * (fn5297, {@code vid_codec_util.cc}) and its inverse (fn5296, {@code codec_utils.cc}), both
 * driven by {@code vid_codec_capability_str_to_bitmask} (fn11245, {@code codec_utils.cc:110})
 * which ORs one single-bit codec-type value per comma-separated token; the two directions
 * agree exactly on {@code H264=0x01}, {@code VP8=0x02}, {@code VP9=0x04}, {@code H265=0x08},
 * {@code AV1=0x10}. The case-insensitive ({@code strcasecmp}, fn12543) token vocabulary and
 * its aliases are read at the recovered DAT string addresses ({@code H264} at {@code 0xc8a63},
 * {@code VP8} at {@code 0xc7502}, {@code VP9} at {@code 0xc6fbe}, {@code H265} at {@code 0xc84e5},
 * {@code AV1} at {@code 0xc9e1f}); the numeric ids {@code 1/2/4/8/16} and the aliases
 * {@code H.264}/{@code H.265}/{@code AVC}/{@code HEVC} map to the same bits. The H264-only
 * fallback is taken from the parse-failure log "set_vid_dec_capability_str dec_mask to H264
 * since converting vid_dec_cap_str ... to bitmask failed". The descriptor table at data
 * segment offset {@code 0x135e60} is the pjmedia pixel-format (fourcc) descriptor array
 * ordered {@code H264,H265,VP8,VP9} with no {@code AV1} entry; it is NOT the capability
 * bitmask and is deliberately not used for bit assignment, which is why the middle codecs
 * here are ordered {@code VP8,VP9,H265} and not {@code H265,VP8,VP9}.
 */
public enum VideoDecoderCapability {
    /**
     * The H.264 (AVC) decoder, bit {@code 0} (mask {@code 0x01}), the negotiation fallback
     * codec.
     *
     * <p>This is the codec the engine assumes when a capability descriptor fails to
     * parse, so it is treated as universally available.
     */
    H264("H264", 0, 4, "H.264", "AVC", "1"),

    /**
     * The VP8 decoder, bit {@code 1} (mask {@code 0x02}).
     */
    VP8("VP8", 1, 2, "2"),

    /**
     * The VP9 decoder, bit {@code 2} (mask {@code 0x04}).
     */
    VP9("VP9", 2, 1, "4"),

    /**
     * The H.265 (HEVC) decoder, bit {@code 3} (mask {@code 0x08}).
     */
    H265("H265", 3, 3, "H.265", "HEVC", "8"),

    /**
     * The AV1 decoder, bit {@code 4} (mask {@code 0x10}).
     */
    AV1("AV1", 4, 0, "16");

    /**
     * The codec selected by the engine when a capability descriptor fails to parse.
     */
    private static final VideoDecoderCapability FALLBACK = H264;

    /**
     * The wire token used to name this codec in a video-decode capability descriptor.
     */
    private final String token;

    /**
     * The bit index this codec occupies in the codec bitmask.
     */
    private final int bit;

    /**
     * The negotiation priority of this codec; higher is preferred.
     */
    private final int priority;

    /**
     * The additional inbound-only tokens that resolve to this codec, alongside {@link #token}.
     *
     * <p>These are the alternate names and numeric ids the engine's case-insensitive codec-type cascade
     * accepts for the same codec; they are recognized by {@link #ofToken(String)} on the inbound path but
     * are never emitted, since {@link #token()} is the single canonical wire token.
     */
    private final String[] aliases;

    /**
     * Constructs a codec constant bound to its wire token, bitmask bit, negotiation priority, and the
     * additional inbound-only token aliases the engine accepts for it.
     *
     * @param token    the canonical wire token emitted in the capability descriptor
     * @param bit      the bitmask bit index
     * @param priority the negotiation priority, higher being preferred
     * @param aliases  the additional inbound-only tokens that resolve to this codec
     */
    VideoDecoderCapability(String token, int bit, int priority, String... aliases) {
        this.token = token;
        this.bit = bit;
        this.priority = priority;
        this.aliases = aliases;
    }

    /**
     * Returns the wire token used to name this codec in a video-decode capability
     * descriptor.
     *
     * @return the wire token, such as {@code "H264"} or {@code "AV1"}
     */
    public String token() {
        return token;
    }

    /**
     * Returns the bit index this codec occupies in the codec bitmask.
     *
     * <p>The bits are {@code H264=0}, {@code VP8=1}, {@code VP9=2}, {@code H265=3}, and
     * {@code AV1=4}, recovered from the engine's codec-type cascade
     * ({@code pjmedia_vid_codec_name_str_to_codec_type}) that backs
     * {@code vid_codec_capability_str_to_bitmask}.
     *
     * @return the bitmask bit index, {@code 0} through {@code 4}
     */
    public int bit() {
        return bit;
    }

    /**
     * Returns the bitmask value (a single set bit) this codec contributes to a codec
     * bitmask.
     *
     * <p>The values are {@code H264=0x01}, {@code VP8=0x02}, {@code VP9=0x04},
     * {@code H265=0x08}, and {@code AV1=0x10}.
     *
     * @return {@code 1 << bit()}
     */
    public int mask() {
        return 1 << bit;
    }

    /**
     * Returns the negotiation priority of this codec.
     *
     * <p>When more than one codec is common to both sides, the engine selects the codec
     * with the highest priority.
     *
     * @return the negotiation priority, higher being preferred
     */
    public int priority() {
        return priority;
    }

    /**
     * Returns the codec whose {@linkplain #token() wire token} or recovered alias equals the given token,
     * ignoring case.
     *
     * <p>Matching honours the full token vocabulary recovered from the engine's case-insensitive codec-type
     * cascade: the canonical token ({@code H264}, {@code VP8}, {@code VP9}, {@code H265}, {@code AV1}), the
     * alternate names ({@code H.264}, {@code AVC}, {@code H.265}, {@code HEVC}), and the numeric ids
     * ({@code 1}, {@code 2}, {@code 4}, {@code 8}, {@code 16}) all resolve to the same codec. The input is
     * trimmed before comparison.
     *
     * @param token the wire token or alias to resolve
     * @return the matching codec, or {@link Optional#empty()} if no codec matches
     */
    public static Optional<VideoDecoderCapability> ofToken(String token) {
        if (token == null) {
            return Optional.empty();
        }
        var trimmed = token.trim();
        for (var codec : values()) {
            if (codec.token.equalsIgnoreCase(trimmed)) {
                return Optional.of(codec);
            }
            for (var alias : codec.aliases) {
                if (alias.equalsIgnoreCase(trimmed)) {
                    return Optional.of(codec);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Parses a comma-separated video-decode capability descriptor into a set of codecs.
     *
     * <p>Tokens are split on commas, trimmed, and resolved through {@link #ofToken(String)}, so each
     * token matches case-insensitively against a codec's canonical wire token or any of its recovered
     * aliases; unrecognized tokens are ignored. If the input is {@code null}, blank, or yields no
     * recognized codec, the result is a set containing only {@link #H264}, matching the engine's
     * parse-failure fallback.
     *
     * @param descriptor the comma-separated capability descriptor, may be {@code null}
     * @return the parsed set of codecs, never empty
     */
    public static Set<VideoDecoderCapability> parse(String descriptor) {
        if (descriptor == null || descriptor.isBlank()) {
            return EnumSet.of(FALLBACK);
        }
        var result = EnumSet.noneOf(VideoDecoderCapability.class);
        for (var part : descriptor.split(",")) {
            ofToken(part).ifPresent(result::add);
        }
        return result.isEmpty() ? EnumSet.of(FALLBACK) : result;
    }

    /**
     * Serializes a set of codecs into a comma-separated capability descriptor.
     *
     * <p>Codecs are emitted in descending {@link #priority()} order so the
     * highest-priority codec appears first. An empty set serializes to the
     * {@link #H264} token, matching the fallback the engine assumes when no codec is
     * advertised.
     *
     * @param codecs the codecs to serialize
     * @return the comma-separated capability descriptor, never empty
     */
    public static String toDescriptor(Set<VideoDecoderCapability> codecs) {
        var ordered = new ArrayList<>(codecs.isEmpty() ? EnumSet.of(FALLBACK) : codecs);
        ordered.sort(Comparator.comparingInt(VideoDecoderCapability::priority).reversed());
        var builder = new StringBuilder();
        for (var codec : ordered) {
            if (!builder.isEmpty()) {
                builder.append(',');
            }
            builder.append(codec.token);
        }
        return builder.toString();
    }

    /**
     * Returns the codecs common to two advertised capability sets.
     *
     * <p>This is the intersection step of codec negotiation: only codecs both sides can
     * decode survive.
     *
     * @param self the local codec set
     * @param peer the peer's advertised codec set
     * @return the intersection of the two sets, possibly empty
     */
    public static Set<VideoDecoderCapability> intersect(Set<VideoDecoderCapability> self, Set<VideoDecoderCapability> peer) {
        var result = EnumSet.copyOf(self.isEmpty() ? EnumSet.noneOf(VideoDecoderCapability.class) : self);
        result.retainAll(peer);
        return result;
    }

    /**
     * Returns the negotiated codec for two advertised capability sets.
     *
     * <p>This combines {@link #intersect(Set, Set)} with the max-priority selection: it
     * returns the highest-{@link #priority() priority} codec common to both sides, or
     * {@link Optional#empty()} when the two sets share no codec.
     *
     * @param self the local codec set
     * @param peer the peer's advertised codec set
     * @return the negotiated codec, or {@link Optional#empty()} if there is no common
     *         codec
     */
    public static Optional<VideoDecoderCapability> negotiate(Set<VideoDecoderCapability> self, Set<VideoDecoderCapability> peer) {
        return intersect(self, peer)
                .stream()
                .max(Comparator.comparingInt(VideoDecoderCapability::priority));
    }
}
