package com.github.auties00.cobalt.message.preview.linkify;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;

import java.net.IDN;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Detects HTTP/HTTPS/mailto/IRC/FTP links inside a text body, mirroring
 * the URL-detection algorithm WhatsApp Web uses to decide which message
 * substrings should trigger a link preview.
 *
 * <p>The implementation transcribes {@code WALinkify}'s composite
 * regular expression character-for-character, validates the host
 * against the embedded TLD list, decodes Punycode IDN labels, balances
 * trailing punctuation against opening brackets and quotes, and
 * synthesises an explicit scheme when the user typed a bare hostname.
 * The output preserves the original matched substring, the canonical
 * URL, the offset into the source body, the resolved scheme, and the
 * parsed host/port/path/query/fragment so {@link LinkPreviewService}
 * and the rich-preview branches can dispatch off the components without
 * re-parsing.
 *
 * @implNote WAWebLinkify wraps WALinkify with the WAWebUserPrefsMeUser
 *           and WASuspiciousLinks plumbing for the suspicious-character
 *           score. Cobalt does not surface that score (the consumers in
 *           this package never read it), so the wrapper collapses into
 *           this class.
 */
@WhatsAppWebModule(moduleName = "WALinkify")
@WhatsAppWebModule(moduleName = "WAWebLinkify")
public final class Linkify {
    /**
     * Atomic character class shared by host labels, paths, queries, and
     * anchors: an ASCII word character, any non-whitespace non-ASCII
     * character outside a small set of formatting punctuation, or a
     * percent-encoded byte.
     *
     * @implNote WALinkify {@code d} fragment.
     */
    private static final String CHAR_CLASS = "\\w|[^\\s\\u0000-\\u007F\\u00AB\\u00BB\\u2018\\u2019\\u201C\\u201D]|%[0-9a-f][0-9a-f]";

    /**
     * Suffix matching either a letter-only TLD or a Punycode IDN label.
     *
     * @implNote WALinkify {@code m} fragment.
     */
    private static final String TLD_SUFFIX = "[a-z]{2,}|xn--(?:" + CHAR_CLASS + ")+";

    /**
     * Single host-label fragment: a letter/digit run that may contain
     * dashes but cannot start or end with one.
     *
     * @implNote WALinkify {@code p} fragment.
     */
    private static final String HOST_LABEL = "(?:" + CHAR_CLASS + ")|(?:" + CHAR_CLASS + ")(?:" + CHAR_CLASS + "|-)*(?:" + CHAR_CLASS + ")";

    /**
     * Full host pattern: one or more labels followed by a final TLD
     * label.
     *
     * @implNote WALinkify {@code _} fragment.
     */
    private static final String HOST = "(?!_)(?:(?:" + HOST_LABEL + ")\\.)+(" + TLD_SUFFIX + ")(?!\\." + HOST_LABEL + ")";

    /**
     * Optional port suffix.
     *
     * @implNote WALinkify {@code f} fragment.
     */
    private static final String PORT = ":\\d{1,5}";

    /**
     * Trailing-punctuation set that may appear at the end of a URL but
     * should be trimmed because it belongs to the surrounding sentence.
     *
     * @implNote WALinkify {@code g} fragment.
     */
    private static final String TRAILING_PUNCT = "@!.?,(\\[{<\\u00AB\\u2018\\u201C:";

    /**
     * Path-character class: a {@link #CHAR_CLASS} character or any
     * non-whitespace, non-percent character.
     *
     * @implNote WALinkify {@code h} fragment.
     */
    private static final String PATH_CHAR = "(?:" + CHAR_CLASS + "|[^\\s%])";

    /**
     * Path component starting with a slash and consuming
     * {@link #PATH_CHAR}s lazily.
     *
     * @implNote WALinkify {@code y} fragment.
     */
    private static final String PATH = "/" + PATH_CHAR + "*?";

    /**
     * Negative look-ahead used to terminate the URL match at sentence
     * boundaries.
     *
     * @implNote WALinkify {@code C} fragment.
     */
    private static final String STOP_LOOKAHEAD = "[" + TRAILING_PUNCT + "]*(?!" + PATH_CHAR + "|#)";

    /**
     * Query component.
     *
     * @implNote WALinkify {@code b} fragment.
     */
    private static final String QUERY = "\\?(?!" + STOP_LOOKAHEAD + ")" + PATH_CHAR + "*?";

    /**
     * Anchor (fragment) component.
     *
     * @implNote WALinkify {@code v} fragment.
     */
    private static final String ANCHOR = "#" + PATH_CHAR + "*?";

    /**
     * Email local-part character class.
     *
     * @implNote WALinkify {@code S} fragment.
     */
    private static final String EMAIL_LOCAL_CHAR = "0-9a-z!#$%&'*+/=?^_`{|}~\\-";

    /**
     * Email local-part fragment.
     *
     * @implNote WALinkify {@code R} fragment.
     */
    private static final String EMAIL_LOCAL = "\\b\\w[" + EMAIL_LOCAL_CHAR + "]*(?:\\.[" + EMAIL_LOCAL_CHAR + "]+)*";

    /**
     * Pre-context required immediately before the URL.
     *
     * @implNote WALinkify {@code L} fragment.
     */
    private static final String PRE_CONTEXT = "^|\\W\\.|[^/\\w.]|_";

    /**
     * The composite URL pattern with nine capture groups.
     *
     * @implNote WALinkify {@code E} fragment.
     */
    private static final String COMPOSITE = "(" + PRE_CONTEXT + ")"
            + "((?:http|https)://|mailto:)?"
            + "(" + EMAIL_LOCAL + "@)?"
            + "(" + HOST + ")"
            + "(?:(?!" + HOST_LABEL + ")|(?=_))"
            + "(?:(?=[^:/?#])|(" + PORT + ")?"
            + "(" + PATH + ")?"
            + "(" + QUERY + ")?"
            + "(" + ANCHOR + ")?"
            + "(?=" + STOP_LOOKAHEAD + "))";

    /**
     * Capture-group index for the pre-context.
     */
    private static final int GROUP_PRE_CONTEXT = 1;

    /**
     * Capture-group index for the explicit scheme.
     */
    private static final int GROUP_SCHEME = 2;

    /**
     * Capture-group index for the email local part.
     */
    private static final int GROUP_EMAIL_LOCAL = 3;

    /**
     * Capture-group index for the entire host fragment.
     */
    private static final int GROUP_HOST = 4;

    /**
     * Capture-group index for the TLD label inside the host.
     */
    private static final int GROUP_TLD = 5;

    /**
     * Capture-group index for the port suffix.
     */
    private static final int GROUP_PORT = 6;

    /**
     * Capture-group index for the path.
     */
    private static final int GROUP_PATH = 7;

    /**
     * Capture-group index for the query.
     */
    private static final int GROUP_QUERY = 8;

    /**
     * Capture-group index for the anchor.
     */
    private static final int GROUP_ANCHOR = 9;

    /**
     * Maps closing punctuation to its matching opening character so
     * trailing brackets/quotes are trimmed only when they were not
     * opened earlier in the URL.
     *
     * @implNote WALinkify {@code w} map.
     */
    private static final Map<Integer, Integer> CLOSING_TO_OPENING = Map.ofEntries(
            Map.entry((int) '"', (int) '"'),
            Map.entry((int) ')', (int) '('),
            Map.entry((int) '>', (int) '<'),
            Map.entry((int) ']', (int) '['),
            Map.entry((int) '}', (int) '{'),
            Map.entry(0x00BB, 0x00AB),
            Map.entry(0x2019, 0x2018),
            Map.entry(0x201D, 0x201C)
    );

    /**
     * Maps opening punctuation to its matching closing character.
     *
     * @implNote WALinkify {@code A} map.
     */
    private static final Map<Integer, Integer> OPENING_TO_CLOSING = Map.ofEntries(
            Map.entry((int) '"', (int) '"'),
            Map.entry((int) '(', (int) ')'),
            Map.entry((int) '<', (int) '>'),
            Map.entry((int) '[', (int) ']'),
            Map.entry((int) '{', (int) '}'),
            Map.entry(0x00AB, 0x00BB),
            Map.entry(0x2018, 0x2019),
            Map.entry(0x201C, 0x201D)
    );

    /**
     * Compiled composite pattern, case-insensitive.
     *
     * @implNote WALinkify {@code F} pattern.
     */
    private static final Pattern PATTERN = Pattern.compile(COMPOSITE, Pattern.CASE_INSENSITIVE);

    /**
     * Fast-path TLD presence check used to short-circuit a full match
     * when no candidate TLD appears in the body.
     *
     * @implNote WALinkify {@code O} pattern.
     */
    private static final Pattern TLD_GUARD = Pattern.compile("\\.(?:" + TLD_SUFFIX + ")", Pattern.CASE_INSENSITIVE);

    /**
     * Hidden constructor for the utility class.
     *
     * @throws UnsupportedOperationException always
     */
    private Linkify() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Returns every URL detected in {@code text}, in order of
     * appearance.
     *
     * @param text                  the text to scan
     * @param requireExplicitScheme whether to keep only matches that
     *                              carry an explicit
     *                              {@code http(s)://} scheme
     * @return the detected URLs
     */
    @WhatsAppWebExport(moduleName = "WALinkify", exports = "findLinks",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static List<Match> findLinks(String text, boolean requireExplicitScheme) {
        if (text == null || !TLD_GUARD.matcher(text).find()) {
            return List.of();
        }
        var out = new ArrayList<Match>();
        var matcher = PATTERN.matcher(text);
        while (matcher.find()) {
            var match = build(matcher, text, requireExplicitScheme);
            if (match != null) {
                out.add(match);
            }
        }
        return out;
    }

    /**
     * Returns the first URL detected in {@code text}, if any.
     *
     * @param text                  the text to scan
     * @param requireExplicitScheme whether to require an explicit
     *                              http/https/mailto scheme
     * @return the first match, or empty when no URL is detected
     */
    @WhatsAppWebExport(moduleName = "WALinkify", exports = "findLink",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static Optional<Match> findLink(String text, boolean requireExplicitScheme) {
        if (text == null || !TLD_GUARD.matcher(text).find()) {
            return Optional.empty();
        }
        var matcher = PATTERN.matcher(text);
        while (matcher.find()) {
            var match = build(matcher, text, requireExplicitScheme);
            if (match != null) {
                return Optional.of(match);
            }
        }
        return Optional.empty();
    }

    /**
     * Returns whether {@code text} contains at least one HTTP(S) link.
     *
     * @param text the text to scan
     * @return {@code true} when an HTTP(S) link is detected
     */
    @WhatsAppWebExport(moduleName = "WAWebLinkify", exports = "hasHttpLink",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static boolean hasHttpLink(String text) {
        return findLink(text, true).isPresent();
    }

    /**
     * Builds a single {@link Match} from the current matcher state.
     *
     * <p>Validates the TLD against {@link TopLevelDomains#TLD},
     * normalises Punycode IDN hosts, balances trailing punctuation, and
     * synthesises an implicit scheme when none was typed. Returns
     * {@code null} when the candidate fails any of these checks.
     *
     * @param matcher               the regex matcher positioned on a
     *                              successful match
     * @param input                 the original input text
     * @param requireExplicitScheme whether to drop matches without an
     *                              explicit scheme
     * @return the materialised {@link Match}, or {@code null} when the
     *         candidate is invalid
     * @implNote WALinkify {@code U} helper.
     */
    private static Match build(Matcher matcher, String input, boolean requireExplicitScheme) {
        var preContext = matcher.group(GROUP_PRE_CONTEXT);
        if (preContext == null) {
            return null;
        }
        var preLength = preContext.length();
        var matchStart = matcher.start();
        if ("_".equals(preContext) && matchStart - 1 >= 0 && !Character.isWhitespace(input.charAt(matchStart - 1))) {
            return null;
        }
        var tld = matcher.group(GROUP_TLD);
        if (tld != null && tld.startsWith("xn--")) {
            try {
                var unicode = IDN.toUnicode(tld);
                if (!TopLevelDomains.TLD.contains(unicode.toLowerCase())) {
                    return null;
                }
            } catch (IllegalArgumentException malformed) {
                return null;
            }
        } else if (tld != null && !TopLevelDomains.TLD.contains(tld.toLowerCase())) {
            return null;
        }
        var portGroup = matcher.group(GROUP_PORT);
        if (portGroup != null && portGroup.length() > 1) {
            var port = Integer.parseInt(portGroup.substring(1));
            if (portGroup.charAt(1) == '0' || port <= 0 || port >= 65536) {
                return null;
            }
        }
        var rawUrl = input.substring(matchStart + preLength, matcher.end());
        // WA picks the rightmost defined component, trying anchor first then query then
        // path, and trims its closing brackets and quotes. When a closer has no opener
        // earlier in the component it is dropped together with everything after it.
        var trimmed = trimBalanced(rawUrl);
        var scheme = matcher.group(GROUP_SCHEME);
        var hasExplicitHttp = scheme != null && (scheme.equalsIgnoreCase("http://") || scheme.equalsIgnoreCase("https://"));
        if (requireExplicitScheme && !hasExplicitHttp) {
            return null;
        }
        var href = trimmed;
        if (scheme == null) {
            if (trimmed.toLowerCase().startsWith("irc.")) {
                scheme = "irc://";
            } else if (trimmed.toLowerCase().startsWith("ftp.")) {
                scheme = "ftp://";
            } else if (matcher.group(GROUP_EMAIL_LOCAL) != null) {
                scheme = "mailto:";
            } else {
                scheme = "http://";
            }
            href = scheme + trimmed;
        } else {
            scheme = scheme.toLowerCase();
        }
        var index = matchStart + preLength + trimmed.length();
        return new Match(
                href,
                trimmed,
                index,
                input,
                scheme,
                matcher.group(GROUP_EMAIL_LOCAL),
                matcher.group(GROUP_HOST),
                matcher.group(GROUP_PORT),
                matcher.group(GROUP_PATH),
                matcher.group(GROUP_QUERY),
                matcher.group(GROUP_ANCHOR),
                hasExplicitHttp
        );
    }

    /**
     * Trims trailing punctuation from {@code value} so that closing
     * brackets, quotes, and end-of-sentence markers are only kept when
     * their opening counterpart appears earlier in the same value.
     *
     * <p>Mirrors WhatsApp Web's {@code WALinkify} inner loop: walk every
     * code point and track a "currently expected closer" plus a stack
     * of pending closers from earlier opens. The last valid index
     * advances when (a) the expected closer matches and the stack
     * empties, (b) the character is not a known closer, or (c) we
     * encounter a stray closer at top level. The substring up to the
     * last valid index is the canonical URL.
     *
     * @param value the URL substring to trim
     * @return the trimmed substring
     * @implNote WALinkify inner loop on the chosen group.
     */
    private static String trimBalanced(String value) {
        if (value.isEmpty()) {
            return value;
        }
        var pending = new ArrayDeque<Integer>();
        var expectedCloser = 0;
        var lastValid = -1;
        for (var i = 0; i < value.length(); i++) {
            var codePoint = (int) value.charAt(i);
            if (codePoint == expectedCloser) {
                expectedCloser = pending.isEmpty() ? 0 : pending.pop();
                if (expectedCloser == 0) {
                    lastValid = i;
                }
            } else if (OPENING_TO_CLOSING.containsKey(codePoint)) {
                if (expectedCloser != 0) {
                    pending.push(expectedCloser);
                }
                expectedCloser = OPENING_TO_CLOSING.get(codePoint);
            } else if (!CLOSING_TO_OPENING.containsKey(codePoint) || expectedCloser == 0) {
                lastValid = i;
            }
        }
        return lastValid < 0 ? value : value.substring(0, lastValid + 1);
    }

    /**
     * One detected URL match.
     *
     * @param href     the canonical URL with an explicit scheme
     * @param url      the literal substring as it appeared in the body
     * @param index    the offset of the first character past the match
     *                 in the source body
     * @param input    the original input text
     * @param scheme   the resolved scheme (always lower-case;
     *                 synthesised when the user did not type one)
     * @param username the email local part with the trailing
     *                 {@code @}, or {@code null} for non-mailto URLs
     * @param domain   the host portion of the URL
     * @param port     the port suffix including the leading colon, or
     *                 {@code null} when no port was provided
     * @param path     the path component, or {@code null}
     * @param params   the query component, or {@code null}
     * @param anchor   the fragment component, or {@code null}
     * @param isHttp   whether the user typed an explicit
     *                 {@code http(s)://} scheme
     */
    public record Match(
            String href,
            String url,
            int index,
            String input,
            String scheme,
            String username,
            String domain,
            String port,
            String path,
            String params,
            String anchor,
            boolean isHttp
    ) {
    }
}
