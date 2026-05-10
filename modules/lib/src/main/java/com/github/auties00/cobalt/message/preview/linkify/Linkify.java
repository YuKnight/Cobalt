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
 */
@WhatsAppWebModule(moduleName = "WALinkify")
@WhatsAppWebModule(moduleName = "WAWebLinkify")
public final class Linkify {
    /**
     * Atomic character class shared by host labels, paths, queries, and
     * anchors: an ASCII word character, any non-whitespace non-ASCII
     * character outside a small set of formatting punctuation, or a
     * percent-encoded byte.
     */
    private static final String CHAR_CLASS = "\\w|[^\\s\\u0000-\\u007F\\u00AB\\u00BB\\u2018\\u2019\\u201C\\u201D]|%[0-9a-f][0-9a-f]";

    /**
     * Suffix matching either a letter-only TLD or a Punycode IDN label.
     */
    private static final String TLD_SUFFIX = "[a-z]{2,}|xn--(?:" + CHAR_CLASS + ")+";

    /**
     * Single host-label fragment: a letter/digit run that may contain
     * dashes but cannot start or end with one.
     */
    private static final String HOST_LABEL = "(?:" + CHAR_CLASS + ")|(?:" + CHAR_CLASS + ")(?:" + CHAR_CLASS + "|-)*(?:" + CHAR_CLASS + ")";

    /**
     * Full host pattern: one or more labels followed by a final TLD
     * label.
     */
    private static final String HOST = "(?!_)(?:(?:" + HOST_LABEL + ")\\.)+(" + TLD_SUFFIX + ")(?!\\." + HOST_LABEL + ")";

    /**
     * Optional port suffix.
     */
    private static final String PORT = ":\\d{1,5}";

    /**
     * Trailing-punctuation set that may appear at the end of a URL but
     * should be trimmed because it belongs to the surrounding sentence.
     */
    private static final String TRAILING_PUNCT = "@!.?,(\\[{<\\u00AB\\u2018\\u201C:";

    /**
     * Path-character class: a {@link #CHAR_CLASS} character or any
     * non-whitespace, non-percent character.
     */
    private static final String PATH_CHAR = "(?:" + CHAR_CLASS + "|[^\\s%])";

    /**
     * Path component starting with a slash and consuming
     * {@link #PATH_CHAR}s lazily.
     */
    private static final String PATH = "/" + PATH_CHAR + "*?";

    /**
     * Negative look-ahead used to terminate the URL match at sentence
     * boundaries.
     */
    private static final String STOP_LOOKAHEAD = "[" + TRAILING_PUNCT + "]*(?!" + PATH_CHAR + "|#)";

    /**
     * Query component.
     */
    private static final String QUERY = "\\?(?!" + STOP_LOOKAHEAD + ")" + PATH_CHAR + "*?";

    /**
     * Anchor (fragment) component.
     */
    private static final String ANCHOR = "#" + PATH_CHAR + "*?";

    /**
     * Email local-part character class.
     */
    private static final String EMAIL_LOCAL_CHAR = "0-9a-z!#$%&'*+/=?^_`{|}~\\-";

    /**
     * Email local-part fragment.
     */
    private static final String EMAIL_LOCAL = "\\b\\w[" + EMAIL_LOCAL_CHAR + "]*(?:\\.[" + EMAIL_LOCAL_CHAR + "]+)*";

    /**
     * Pre-context required immediately before the URL.
     */
    private static final String PRE_CONTEXT = "^|\\W\\.|[^/\\w.]|_";

    /**
     * The composite URL pattern with nine capture groups.
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
     */
    private static final Pattern PATTERN = Pattern.compile(COMPOSITE, Pattern.CASE_INSENSITIVE);

    /**
     * Fast-path TLD presence check used to short-circuit a full match
     * when no candidate TLD appears in the body.
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
        // WALinkify q: F.lastIndex = 0; return U(B(e), t). Only the first regex match is considered;
        // when U returns null the function yields null without attempting subsequent matches.
        var matcher = PATTERN.matcher(text);
        if (!matcher.find()) {
            return Optional.empty();
        }
        return Optional.ofNullable(build(matcher, text, requireExplicitScheme));
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
     * Returns the match describing {@code text} when the entire input is
     * a single, well-formed {@code mailto:} address.
     *
     * <p>Mirrors {@code WALinkify.validateEmail}: the candidate must
     * cover {@code text} in its entirety, resolve to the {@code mailto:}
     * scheme, expose a non-empty username, and carry neither a query
     * string nor a fragment. Any other match shape is rejected.
     *
     * @param text the candidate email address
     * @return the match when {@code text} is a complete mailto address,
     *         otherwise empty
     */
    @WhatsAppWebExport(moduleName = "WALinkify", exports = "validateEmail",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static Optional<Match> validateEmail(String text) {
        if (text == null) {
            return Optional.empty();
        }
        var match = findLink(text, false).orElse(null);
        if (match == null) {
            return Optional.empty();
        }
        if (!match.url().equals(text)) {
            return Optional.empty();
        }
        if (!"mailto:".equals(match.scheme())) {
            return Optional.empty();
        }
        if (match.username() == null || match.username().isEmpty()) {
            return Optional.empty();
        }
        if (match.params() != null && !match.params().isEmpty()) {
            return Optional.empty();
        }
        if (match.anchor() != null && !match.anchor().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(match);
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
     */
    private static Match build(Matcher matcher, String input, boolean requireExplicitScheme) {
        var preContext = matcher.group(GROUP_PRE_CONTEXT);
        if (preContext == null) {
            return null;
        }
        // WALinkify U: var n = e[k].length, a = e[0], i = e.index + n, l = e.index, s = e[k] === "_".
        var preLength = preContext.length();
        var fullMatch = matcher.group(0);
        var matchStart = matcher.start();
        // WALinkify U: if (s && l - 1 && /\S/.test(e.input[l-1])) return null.
        // JS quirk: `l - 1` is falsy only when l === 1. When l === 0, e.input[-1] is undefined and
        // /\S/.test("undefined") returns true, so the match is rejected. We mirror the same behaviour:
        // accept only when l === 1 (prior char unreachable) or the prior character is whitespace.
        if ("_".equals(preContext) && matchStart != 1) {
            if (matchStart == 0 || !Character.isWhitespace(input.charAt(matchStart - 1))) {
                return null;
            }
        }
        // WALinkify U: u = e[x]; if (u.startsWith("xn--") && !TLD.has(toUnicode(u))) return null.
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
            // ADAPTED: WALinkify embeds the TLD list directly into the regex so the alternation only matches
            // recognised TLDs. Cobalt uses [a-z]{2,} in the regex and re-validates here so the TLD list lives
            // in a single Java set (TopLevelDomains.TLD).
            return null;
        }
        // WALinkify U: e[$][1] === "0" || !(0 < d && d < 65536).
        var portGroup = matcher.group(GROUP_PORT);
        if (portGroup != null && portGroup.length() > 1) {
            var port = Integer.parseInt(portGroup.substring(1));
            if (portGroup.charAt(1) == '0' || port <= 0 || port >= 65536) {
                return null;
            }
        }
        // WALinkify U: m = [M,N,P].find(t => e[t]) || 0 — rightmost defined of [anchor, params, path].
        var components = new String[]{
                matcher.group(GROUP_SCHEME),
                matcher.group(GROUP_EMAIL_LOCAL),
                matcher.group(GROUP_HOST),
                matcher.group(GROUP_PORT),
                matcher.group(GROUP_PATH),
                matcher.group(GROUP_QUERY),
                matcher.group(GROUP_ANCHOR)
        };
        var lastComponentGroup = 0;
        if (components[6] != null) {
            lastComponentGroup = GROUP_ANCHOR;
        } else if (components[5] != null) {
            lastComponentGroup = GROUP_QUERY;
        } else if (components[4] != null) {
            lastComponentGroup = GROUP_PATH;
        }
        // a tracks the canonical match string. Initially the full match (including pre-context); after the
        // truncation block below it is either the full URL or a rebuilt prefix+truncated-tail.
        var a = fullMatch;
        if (lastComponentGroup != 0) {
            var lastComponent = components[lastComponentGroup - GROUP_SCHEME];
            // WALinkify U: _.slice(-1) === "_" && a[i-1] === "_" — drop a trailing "_" when the JS bounds-quirk
            // happens to read another "_" earlier in `a`. JS treats out-of-bounds as undefined and skips, so
            // the check only fires when matchStart + preLength - 1 indexes into `a`.
            var probe = matchStart + preLength - 1;
            if (lastComponent.endsWith("_") && probe >= 0 && probe < a.length() && a.charAt(probe) == '_') {
                a = a.substring(0, a.length() - 1);
                lastComponent = lastComponent.substring(0, lastComponent.length() - 1);
                components[lastComponentGroup - GROUP_SCHEME] = lastComponent;
            }
            // WALinkify U: walk lastComponent tracking pending closers and a current expected closer.
            var pending = new ArrayDeque<Integer>();
            var expectedCloser = 0;
            var lastValid = 0;
            for (var h = 0; h < lastComponent.length(); h++) {
                var codePoint = (int) lastComponent.charAt(h);
                if (codePoint == expectedCloser) {
                    expectedCloser = pending.isEmpty() ? 0 : pending.pop();
                    if (expectedCloser == 0) {
                        lastValid = h;
                    }
                } else if (OPENING_TO_CLOSING.containsKey(codePoint)) {
                    if (expectedCloser != 0) {
                        pending.push(expectedCloser);
                    }
                    expectedCloser = OPENING_TO_CLOSING.get(codePoint);
                } else if (!CLOSING_TO_OPENING.containsKey(codePoint) || expectedCloser == 0) {
                    lastValid = h;
                }
            }
            if (lastValid != lastComponent.length() - 1) {
                if (lastComponentGroup == GROUP_QUERY && expectedCloser != 0) {
                    // WALinkify U: m === N && g !== 0 → unbalanced query: drop everything but slice off pre-context.
                    a = a.substring(preLength);
                } else {
                    // WALinkify U: rebuild from groups [2..lastComponentGroup-1] (with TLD subgroup removed) + truncated tail.
                    var rebuilt = new StringBuilder();
                    for (var g = GROUP_SCHEME; g < lastComponentGroup; g++) {
                        // WALinkify U: C.splice(x - I, 1) removes the TLD subgroup since it duplicates the host.
                        if (g == GROUP_TLD) {
                            continue;
                        }
                        var part = components[g - GROUP_SCHEME];
                        if (part != null && !part.isEmpty()) {
                            rebuilt.append(part);
                        }
                    }
                    rebuilt.append(lastComponent, 0, lastValid + 1);
                    a = rebuilt.toString();
                    // WALinkify U: F.lastIndex = i + a.length — Java's Matcher resumes from urlEnd by default;
                    // we mirror the JS reset by repositioning the matcher region for the next find() call.
                    matcher.region(matchStart + preLength + a.length(), input.length());
                }
            } else {
                // WALinkify U: balanced last component → strip pre-context only.
                a = a.substring(preLength);
            }
        } else {
            // WALinkify U: m === 0 → no path/query/anchor present, strip pre-context.
            a = a.substring(preLength);
        }
        var url = a;
        var scheme = components[0];
        var hasExplicitHttp = scheme != null && (scheme.equalsIgnoreCase("http://") || scheme.equalsIgnoreCase("https://"));
        // WALinkify U: if (t && !S) return null — drop bare-host matches when only HTTP(S) URLs are wanted.
        if (requireExplicitScheme && !hasExplicitHttp) {
            return null;
        }
        // WALinkify U: synthesise scheme when absent: irc:// / ftp:// / mailto: / http://.
        var href = url;
        if (scheme == null) {
            var lower = url.toLowerCase();
            if (lower.startsWith("irc.")) {
                scheme = "irc://";
            } else if (lower.startsWith("ftp.")) {
                scheme = "ftp://";
            } else if (components[1] != null) {
                scheme = "mailto:";
            } else {
                scheme = "http://";
            }
            href = scheme + url;
        } else {
            scheme = scheme.toLowerCase();
        }
        // WALinkify U: index = i + a.length (offset of first character past the match in the input).
        var index = matchStart + preLength + url.length();
        return new Match(
                href,
                url,
                index,
                input,
                scheme,
                components[1],
                components[2],
                components[3],
                components[4],
                components[5],
                components[6],
                hasExplicitHttp
        );
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
