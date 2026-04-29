package com.github.auties00.cobalt.message.preview.gate;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Implements WhatsApp Web's homograph-attack heuristic on internationalised
 * domain names.
 *
 * <p>The algorithm splits the host into dot-separated labels, finds the
 * single label that contains non-ASCII characters, and scans that label's
 * Unicode code points for confusable symbols (Latin look-alikes that map
 * to a constrained set of natural languages). A label is considered
 * suspicious when:
 *
 * <ol>
 *   <li>Every code point is drawn from the high-confusable Cyrillic
 *       basic-Latin look-alike set, AND none of (a) the recipient's
 *       country code, (b) the local sender's country code, (c) the
 *       recipient's stated languages match a Cyrillic-script tag; or</li>
 *   <li>At least one but no more than two confusable code points map
 *       to languages disjoint from the recipient's languages and to
 *       regions disjoint from both country codes.</li>
 * </ol>
 *
 * <p>The implementation is a faithful, allocation-free port of WhatsApp
 * Web's {@code WAIdn.findSuspiciousCharacters}; the code-point→language
 * table, the Cyrillic-script language tags, the Cyrillic country
 * codes, the high-confusables string, and the
 * {@code LANGUAGE_TO_REGIONS} map are all transcribed verbatim from
 * the JS bundle.
 *
 * @implNote WAIdn.findSuspiciousCharacters +
 *           WALanguagesAndRegions.LANGUAGE_TO_REGIONS.
 */
@WhatsAppWebModule(moduleName = "WAIdn")
@WhatsAppWebModule(moduleName = "WALanguagesAndRegions")
final class Idn {
    /**
     * Pattern matching labels composed entirely of ASCII letters,
     * digits, and dashes. Labels that match are guaranteed to be
     * non-suspicious because they cannot mix scripts.
     *
     * @implNote WAIdn {@code _} regex.
     */
    private static final Pattern ASCII_LABEL = Pattern.compile("^[a-z0-9-]+$");

    /**
     * Maximum number of confusable code points the heuristic tolerates
     * before concluding the label is intentionally polyglot rather than
     * homographic.
     *
     * @implNote WAIdn {@code f} constant.
     */
    private static final int MAX_CONFUSABLES_BEFORE_BAIL = 2;

    /**
     * Cyrillic-script language tags consulted when every code point in
     * the label belongs to the high-confusable Cyrillic set; if the
     * recipient speaks one of these, the label is presumed legitimate.
     *
     * @implNote WAIdn {@code s} set.
     */
    @WhatsAppWebExport(moduleName = "WAIdn", exports = "findSuspiciousCharacters",
            adaptation = WhatsAppAdaptation.DIRECT)
    private static final Set<String> CYRILLIC_SCRIPT_LANGUAGES = Set.of(
            "abq", "ab", "ady", "av", "az", "ba", "be", "bs", "bg", "bua",
            "ce", "ckt", "cu", "cv", "crh", "dar", "dng", "myv", "evn",
            "gag", "inh", "kbd", "xal", "krc", "kaa", "kk", "kjh", "kca",
            "ky", "kv", "koi", "kpy", "kum", "lbe", "lez", "mk", "mns",
            "chm", "mdf", "mn", "ttt", "gld", "yrk", "nog", "os", "ru",
            "rue", "sr", "sh", "cjs", "alt", "tab", "tg", "tt", "tkr",
            "tk", "tyv", "ude", "udm", "uk", "uz", "mrj", "sah"
    );

    /**
     * Country-code prefixes that ship with phones whose users
     * realistically write Cyrillic.
     *
     * @implNote WAIdn {@code u} set.
     */
    @WhatsAppWebExport(moduleName = "WAIdn", exports = "findSuspiciousCharacters",
            adaptation = WhatsAppAdaptation.DIRECT)
    private static final Set<String> CYRILLIC_COUNTRY_CODES = Set.of(
            "374", "994", "375", "387", "359", "995", "7", "383", "996",
            "389", "373", "976", "382", "381", "992", "993", "380", "998"
    );

    /**
     * Cyrillic basic-Latin look-alike code points. When every code
     * point of the host label belongs to this set, the label is
     * considered Cyrillic-only and the script-mismatch path runs.
     *
     * @implNote WAIdn {@code c} string.
     */
    @WhatsAppWebExport(moduleName = "WAIdn", exports = "findSuspiciousCharacters",
            adaptation = WhatsAppAdaptation.DIRECT)
    private static final String HIGH_CONFUSABLES =
            "авгекмнорс"
                    + "тухшщьѕіј"
                    + "ѡѵүһӏԁԛԝԧ"
                    + "ꚙ";

    /**
     * Code-point → list of language tags that legitimately use that
     * code point.
     *
     * <p>The keys are individual non-ASCII code points; the values are
     * the BCP-47 language tags whose orthographies contain the code
     * point. A code point present in this map is a confusable
     * candidate; a code point absent from the map is treated as plain
     * Latin (no suspicion).
     *
     * @implNote WAIdn {@code e} map.
     */
    @WhatsAppWebExport(moduleName = "WAIdn", exports = "findSuspiciousCharacters",
            adaptation = WhatsAppAdaptation.DIRECT)
    private static final Map<Integer, List<String>> CHAR_TO_LANGUAGES = Map.ofEntries(
            Map.entry(0x00ED, List.of("ast", "ksf", "bas", "ca", "cs", "dua", "nl", "ee", "ewo", "fo", "fr", "gl", "kl", "hu", "is", "ga", "dyo", "kea", "kkj", "nmg", "lkt", "lag", "ln", "lu", "mas", "nnh", "jgo", "pt", "qu", "seh", "sk", "es", "to", "vai", "vi", "wae", "cy", "fy", "yav", "yo")),
            Map.entry(0x00EE, List.of("af", "agq", "bss", "bfd", "bas", "bkv", "btt", "nl", "igb", "ewo", "fr", "fur", "gaj", "gby", "kl", "jab", "atg", "jbu", "kkj", "naq", "ksh", "ku", "nmb", "lmp", "ln", "mda", "mg", "mas", "mzm", "jgo", "nin", "ann", "ro", "sg", "scn", "srn", "yer", "tr", "wa", "cy", "yav", "yle")),
            Map.entry(0x00FC, List.of("agg", "ast", "az", "br", "ca", "co", "cs", "nl", "etr", "et", "fr", "gl", "de", "tof", "god", "hu", "tbd", "geb", "ksh", "kup", "nds", "lb", "arn", "oc", "sg", "sk", "es", "gsw", "dts", "tr", "tk", "vo", "wa", "wae", "fy", "yll")),
            Map.entry(0x010B, List.of("mt")),
            Map.entry(0x012B, List.of("agq", "bss", "bfd", "bas", "bkv", "btt", "ddn", "igb", "kjy", "ich", "gux", "haw", "jab", "iby", "idu", "jbu", "kub", "nmg", "lv", "mda", "mi", "mas", "mql", "nin", "prg", "gd", "to", "yba", "yav")),
            Map.entry(0x0131, List.of("az", "crh", "tr")),
            Map.entry(0x013C, List.of("lv")),
            Map.entry(0x0142, List.of("dsb", "nv", "pl", "hsb")),
            Map.entry(0x0161, List.of("bs", "hr", "cs", "dzg", "et", "fi", "smn", "geb", "khq", "ses", "kun", "lkt", "lv", "lt", "dsb", "se", "nso", "prg", "sr", "sh", "sms", "sk", "sl", "taq", "twq", "tuq", "hsb", "wqe", "dje")),
            Map.entry(0x0199, List.of("ckl", "ank", "ha", "ikx", "kai", "hia", "mbu", "anc", "nin", "pip", "tal", "tan", "wja", "wji")),
            Map.entry(0x01C0, List.of("naq")),
            Map.entry(0x01C1, List.of("naq")),
            Map.entry(0x022F, List.of("liv")),
            Map.entry(0x0251, List.of("fmp", "dud", "tmh")),
            Map.entry(0x0253, List.of("fub", "yay", "bkc", "bjt", "bcn", "bas", "bsq", "bmq", "fue", "bys", "bwr", "cky", "fuq", "ckl", "asg", "dbq", "dnj", "dgh", "dow", "dua", "enn", "ff", "gby", "gba", "gmm", "ank", "gde", "gkp", "jgk", "ha", "hbb", "ikx", "kkj", "hig", "kzr", "kai", "kpe", "nmg", "hia", "ffm", "mbo", "mbu", "mif", "mzm", "mua", "sur", "anc", "fuv", "nin", "dgi", "pbi", "pip", "fuf", "cla", "sav", "srr", "sld", "sok", "tal", "tan", "yer", "ttr", "tik", "kdl", "tsw", "vai", "vut", "wja", "wji", "fuh", "gnd")),
            Map.entry(0x1E0D, List.of("tzm", "kab", "okr", "shi", "tmh", "taq")),
            Map.entry(0x1E5B, List.of("tzm", "kab", "shi")),
            Map.entry(0x1E6D, List.of("tzm", "kab", "shi", "tmh", "taq")),
            Map.entry(0x1E89, List.of()),
            Map.entry(0x1EA1, List.of("izi", "yaz", "blt", "vi")),
            Map.entry(0x1EB9, List.of("yay", "bom", "bin", "mfn", "dzg", "igb", "enn", "gkn", "iby", "ikk", "ikw", "izi", "okr", "yaz", "blt", "tan", "tuq", "vi", "yo")),
            Map.entry(0x1ECB, List.of("avu", "mfn", "igb", "enn", "iby", "ig", "ige", "ikk", "ikw", "izi", "okr", "blt", "tan", "vi"))
    );

    /**
     * Language → list of country codes where the language is widely
     * spoken.
     *
     * @implNote WALanguagesAndRegions.LANGUAGE_TO_REGIONS.
     */
    @WhatsAppWebExport(moduleName = "WALanguagesAndRegions", exports = "LANGUAGE_TO_REGIONS",
            adaptation = WhatsAppAdaptation.DIRECT)
    private static final Map<String, List<String>> LANGUAGE_TO_REGIONS = buildLanguageToRegions();

    /**
     * Hidden constructor for the utility class.
     *
     * @throws UnsupportedOperationException always
     */
    private Idn() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Builds the language→regions table. Held in a method so the
     * cumulative entry count fits the reasonable upper bound on
     * {@link Map#ofEntries(Map.Entry[])} call sizes.
     *
     * @return the immutable language→regions map
     * @implNote WALanguagesAndRegions.LANGUAGE_TO_REGIONS.
     */
    @WhatsAppWebExport(moduleName = "WALanguagesAndRegions", exports = "LANGUAGE_TO_REGIONS",
            adaptation = WhatsAppAdaptation.DIRECT)
    private static Map<String, List<String>> buildLanguageToRegions() {
        return Map.<String, List<String>>ofEntries(
                Map.entry("fub", List.of("237", "235", "234")),
                Map.entry("af", List.of("237", "27")),
                Map.entry("agq", List.of("237")),
                Map.entry("yay", List.of("234")),
                Map.entry("bss", List.of("237")),
                Map.entry("agg", List.of("675")),
                Map.entry("ast", List.of("34")),
                Map.entry("avu", List.of("243", "211")),
                Map.entry("az", List.of("994")),
                Map.entry("bkc", List.of("237", "241")),
                Map.entry("ksf", List.of("237")),
                Map.entry("bfd", List.of("237")),
                Map.entry("bjt", List.of("221")),
                Map.entry("bcn", List.of("234")),
                Map.entry("bas", List.of("237")),
                Map.entry("bsq", List.of("231")),
                Map.entry("bkv", List.of("234")),
                Map.entry("bom", List.of("234")),
                Map.entry("btt", List.of("234")),
                Map.entry("bin", List.of("234")),
                Map.entry("bmq", List.of("226", "223")),
                Map.entry("bs", List.of("387", "385", "383", "382", "381")),
                Map.entry("fue", List.of("229", "228")),
                Map.entry("br", List.of("33")),
                Map.entry("bys", List.of("234")),
                Map.entry("bwr", List.of("234")),
                Map.entry("cky", List.of("234")),
                Map.entry("ca", List.of("376", "33", "39", "34")),
                Map.entry("tzm", List.of("212")),
                Map.entry("fuq", List.of("234")),
                Map.entry("ckl", List.of("234")),
                Map.entry("asg", List.of("234")),
                Map.entry("co", List.of("33", "39")),
                Map.entry("crh", List.of("7")),
                Map.entry("hr", List.of("387", "385", "383", "382", "381")),
                Map.entry("mfn", List.of("234")),
                Map.entry("cs", List.of("420", "421")),
                Map.entry("dbq", List.of("237", "234")),
                Map.entry("dnj", List.of("225", "231")),
                Map.entry("dzg", List.of("235", "227")),
                Map.entry("ddn", List.of("229")),
                Map.entry("dgh", List.of("234")),
                Map.entry("dow", List.of("237")),
                Map.entry("dua", List.of("229")),
                Map.entry("nl", List.of("297", "32", "599", "31", "1", "597")),
                Map.entry("igb", List.of("234")),
                Map.entry("etr", List.of("675")),
                Map.entry("enn", List.of("234")),
                Map.entry("kgy", List.of("675")),
                Map.entry("et", List.of("372")),
                Map.entry("ich", List.of("234")),
                Map.entry("ee", List.of("229", "233", "228")),
                Map.entry("ewo", List.of("237")),
                Map.entry("fo", List.of("298")),
                Map.entry("fmp", List.of("237")),
                Map.entry("fi", List.of("358", "46")),
                Map.entry("fr", List.of("32", "229", "226", "257", "237", "1", "236", "242", "243", "235", "269", "253", "240", "33", "241", "509", "225", "352", "261", "223", "377", "227", "250", "221", "248", "41", "228", "678")),
                Map.entry("fur", List.of("39")),
                Map.entry("ff", List.of("237", "224", "222", "221")),
                Map.entry("gaj", List.of("675")),
                Map.entry("gl", List.of("34")),
                Map.entry("gby", List.of("234")),
                Map.entry("gba", List.of("236")),
                Map.entry("gmm", List.of("237")),
                Map.entry("de", List.of("43", "32", "49", "423", "352", "41")),
                Map.entry("tof", List.of("675")),
                Map.entry("god", List.of("225")),
                Map.entry("ank", List.of("234")),
                Map.entry("gkn", List.of("234")),
                Map.entry("gux", List.of("229", "226", "227", "228")),
                Map.entry("gde", List.of("237", "234")),
                Map.entry("gkp", List.of("224")),
                Map.entry("kl", List.of("299")),
                Map.entry("jgk", List.of("234")),
                Map.entry("ha", List.of("229", "237", "235", "233", "225", "227", "234", "249", "228")),
                Map.entry("haw", List.of("1")),
                Map.entry("hbb", List.of("234")),
                Map.entry("hu", List.of("36")),
                Map.entry("dud", List.of("234")),
                Map.entry("jab", List.of("234")),
                Map.entry("iby", List.of("234")),
                Map.entry("is", List.of("354")),
                Map.entry("idu", List.of("234")),
                Map.entry("ig", List.of("240")),
                Map.entry("ige", List.of("234")),
                Map.entry("ikx", List.of("256")),
                Map.entry("ikk", List.of("234")),
                Map.entry("ikq", List.of("234")),
                Map.entry("smn", List.of("358")),
                Map.entry("ga", List.of("353", "44")),
                Map.entry("atg", List.of("234")),
                Map.entry("izi", List.of("234")),
                Map.entry("dyo", List.of("220", "221")),
                Map.entry("jbu", List.of("237", "234")),
                Map.entry("kab", List.of("213")),
                Map.entry("kea", List.of("238")),
                Map.entry("tbd", List.of("675")),
                Map.entry("hig", List.of("234")),
                Map.entry("kai", List.of("234")),
                Map.entry("kkj", List.of("237", "236", "242")),
                Map.entry("kzr", List.of("237", "235")),
                Map.entry("naq", List.of("264")),
                Map.entry("geb", List.of("675")),
                Map.entry("okr", List.of("234")),
                Map.entry("ksh", List.of("49")),
                Map.entry("ses", List.of("223")),
                Map.entry("khq", List.of("223")),
                Map.entry("kun", List.of("291", "251")),
                Map.entry("kup", List.of("675")),
                Map.entry("kub", List.of("237", "234")),
                Map.entry("kpe", List.of("224", "231")),
                Map.entry("nmg", List.of("237", "240")),
                Map.entry("ku", List.of("963", "90")),
                Map.entry("lkt", List.of("1")),
                Map.entry("hia", List.of("234")),
                Map.entry("lag", List.of("255")),
                Map.entry("lv", List.of("371")),
                Map.entry("lmp", List.of("237")),
                Map.entry("ln", List.of("242", "243")),
                Map.entry("liv", List.of()),
                Map.entry("lt", List.of("370")),
                Map.entry("yaz", List.of("234")),
                Map.entry("nds", List.of("55", "49", "31")),
                Map.entry("dsb", List.of("49")),
                Map.entry("lu", List.of("243")),
                Map.entry("lb", List.of("32", "33", "49", "352")),
                Map.entry("mda", List.of("234")),
                Map.entry("mb", List.of("261")),
                Map.entry("mt", List.of("356")),
                Map.entry("mi", List.of("64")),
                Map.entry("arn", List.of("56")),
                Map.entry("mas", List.of("254", "255")),
                Map.entry("ffm", List.of("223")),
                Map.entry("mql", List.of("229", "228")),
                Map.entry("mbo", List.of("237")),
                Map.entry("mbu", List.of("234")),
                Map.entry("mif", List.of("237")),
                Map.entry("mzm", List.of("234")),
                Map.entry("mua", List.of("237", "235")),
                Map.entry("sur", List.of("234")),
                Map.entry("nv", List.of("1")),
                Map.entry("anc", List.of("234")),
                Map.entry("nnh", List.of("237")),
                Map.entry("jgo", List.of("237")),
                Map.entry("fuv", List.of("237", "234")),
                Map.entry("nin", List.of("234")),
                Map.entry("dgi", List.of("226")),
                Map.entry("se", List.of("358", "46", "47")),
                Map.entry("nso", List.of("27")),
                Map.entry("ann", List.of("234")),
                Map.entry("oc", List.of("33", "39", "377", "34")),
                Map.entry("pbi", List.of("237")),
                Map.entry("pip", List.of("234")),
                Map.entry("pl", List.of("48")),
                Map.entry("pt", List.of("244", "55", "238", "240", "245", "853", "258", "351", "239", "670")),
                Map.entry("prg", List.of()),
                Map.entry("fuf", List.of("224", "223", "221", "232")),
                Map.entry("qu", List.of("54", "591", "57", "593", "51")),
                Map.entry("ro", List.of("373", "40")),
                Map.entry("cla", List.of("234")),
                Map.entry("sav", List.of("221")),
                Map.entry("sg", List.of("236", "235", "243")),
                Map.entry("gd", List.of("44")),
                Map.entry("seh", List.of("258")),
                Map.entry("sr", List.of("387", "385", "383", "382", "381")),
                Map.entry("sh", List.of("387", "385", "383", "382", "381")),
                Map.entry("srr", List.of("220", "221")),
                Map.entry("sch", List.of("39")),
                Map.entry("sld", List.of("226")),
                Map.entry("sms", List.of("358")),
                Map.entry("sk", List.of("420", "421")),
                Map.entry("sl", List.of("386")),
                Map.entry("sok", List.of("235")),
                Map.entry("es", List.of("54", "501", "591", "56", "57", "506", "53", "593", "503", "240", "502", "504", "52", "505", "507", "595", "51", "34", "1", "598", "58")),
                Map.entry("srn", List.of("597")),
                Map.entry("gsw", List.of("43", "33", "49", "423", "41")),
                Map.entry("shi", List.of("212")),
                Map.entry("tal", List.of("234")),
                Map.entry("tmh", List.of("223")),
                Map.entry("taq", List.of("226", "223")),
                Map.entry("tan", List.of("234")),
                Map.entry("twq", List.of("227")),
                Map.entry("blt", List.of("856", "84")),
                Map.entry("yer", List.of("234")),
                Map.entry("tuq", List.of("235", "218", "227", "234")),
                Map.entry("ttr", List.of("234")),
                Map.entry("tik", List.of("237")),
                Map.entry("to", List.of("676")),
                Map.entry("dts", List.of("223")),
                Map.entry("kdl", List.of("234")),
                Map.entry("tsw", List.of("234")),
                Map.entry("tr", List.of("90")),
                Map.entry("tk", List.of("993")),
                Map.entry("hsb", List.of("49")),
                Map.entry("vai", List.of("231", "232")),
                Map.entry("vi", List.of("855", "84")),
                Map.entry("vo", List.of()),
                Map.entry("vut", List.of("237", "234")),
                Map.entry("wja", List.of("234")),
                Map.entry("wa", List.of("32")),
                Map.entry("wae", List.of("43", "39", "423", "41")),
                Map.entry("wji", List.of("234")),
                Map.entry("cy", List.of("44")),
                Map.entry("fy", List.of("31")),
                Map.entry("fuh", List.of("229", "226", "227")),
                Map.entry("yba", List.of("234")),
                Map.entry("yav", List.of("237")),
                Map.entry("yle", List.of("675")),
                Map.entry("yll", List.of("675")),
                Map.entry("yo", List.of("229", "234")),
                Map.entry("dje", List.of("227")),
                Map.entry("gnd", List.of("237"))
        );
    }

    /**
     * Returns whether {@code host} is suspicious for the given
     * recipient and sender context.
     *
     * <p>This is the package-private entry point for the link-preview
     * pipeline. See the class javadoc for the algorithm rationale.
     *
     * @param host                the host portion of the URL,
     *                            lower-cased before the call
     * @param recipientCountryCode the country-code prefix of the
     *                            recipient phone JID, or
     *                            {@code "ZZ"} for LID/group/newsletter
     *                            JIDs (matches WA Web's behaviour
     *                            when the recipient is not a phone
     *                            user)
     * @param selfCountryCode     the country-code prefix of the local
     *                            user's phone JID
     * @param recipientLanguages  the recipient's stated languages
     *                            (BCP-47 tags); the empty list when
     *                            unknown
     * @return {@code true} when the label is flagged as suspicious
     * @implNote WAIdn.findSuspiciousCharacters.
     */
    @WhatsAppWebExport(moduleName = "WAIdn", exports = "findSuspiciousCharacters",
            adaptation = WhatsAppAdaptation.DIRECT)
    static boolean isSuspicious(String host, String recipientCountryCode, String selfCountryCode,
                                List<String> recipientLanguages) {
        if (host == null || host.isEmpty()) {
            return false;
        }
        var languages = recipientLanguages == null ? List.<String>of() : recipientLanguages;
        var labels = host.split("\\.");
        String suspectLabel = null;
        for (var label : labels) {
            if (ASCII_LABEL.matcher(label).matches()) {
                continue;
            }
            if (suspectLabel != null) {
                return false;
            }
            suspectLabel = label;
        }
        if (suspectLabel == null) {
            return false;
        }
        var allHighConfusables = true;
        var matched = new ArrayList<Integer>();
        for (var i = 0; i < suspectLabel.length(); ) {
            var codePoint = suspectLabel.codePointAt(i);
            i += Character.charCount(codePoint);
            var inHighConfusable = HIGH_CONFUSABLES.indexOf(codePoint) >= 0;
            allHighConfusables = allHighConfusables && inHighConfusable;
            if (!allHighConfusables && CHAR_TO_LANGUAGES.containsKey(codePoint)) {
                if (matched.size() >= MAX_CONFUSABLES_BEFORE_BAIL) {
                    return false;
                }
                matched.add(codePoint);
            }
        }
        if (allHighConfusables) {
            if (CYRILLIC_COUNTRY_CODES.contains(selfCountryCode)
                    || CYRILLIC_COUNTRY_CODES.contains(recipientCountryCode)) {
                return false;
            }
            for (var lang : languages) {
                if (CYRILLIC_SCRIPT_LANGUAGES.contains(lang)) {
                    return false;
                }
            }
            return true;
        }
        if (matched.isEmpty()) {
            return false;
        }
        for (var codePoint : matched) {
            if (isCharacterSuspicious(codePoint, recipientCountryCode, selfCountryCode, languages)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether a single confusable code point should flag the
     * label.
     *
     * @param codePoint           the confusable code point
     * @param recipientCountryCode the recipient's country code
     * @param selfCountryCode     the local user's country code
     * @param recipientLanguages  the recipient's stated languages
     * @return {@code true} when the code point's languages and
     *         regions are disjoint from the recipient/sender context
     */
    private static boolean isCharacterSuspicious(int codePoint, String recipientCountryCode,
                                                 String selfCountryCode, List<String> recipientLanguages) {
        var languages = CHAR_TO_LANGUAGES.get(codePoint);
        if (languages == null) {
            return false;
        }
        for (var lang : recipientLanguages) {
            if (languages.contains(lang)) {
                return false;
            }
        }
        var coveredRegions = new HashSet<String>();
        for (var lang : languages) {
            var regions = LANGUAGE_TO_REGIONS.get(lang);
            if (regions != null) {
                coveredRegions.addAll(regions);
            }
        }
        return !coveredRegions.contains(recipientCountryCode)
                && !coveredRegions.contains(selfCountryCode);
    }

    /**
     * Extracts the country code from a phone JID's user portion.
     *
     * @param phoneUser the user-prefix digits of a phone JID
     * @return the resolved country code (1–3 digits) or the input
     *         when no longer prefix matches
     * @implNote WAPhoneFindCC.phoneCC.
     */
    @WhatsAppWebExport(moduleName = "WAPhoneFindCC", exports = "phoneCC",
            adaptation = WhatsAppAdaptation.DIRECT)
    static String countryCodeOf(String phoneUser) {
        if (phoneUser == null || phoneUser.isEmpty()) {
            return "";
        }
        var matcher = COUNTRY_CODE_PREFIX.matcher(phoneUser);
        if (matcher.find()) {
            return matcher.group();
        }
        return phoneUser.length() >= 3 ? phoneUser.substring(0, 3) : phoneUser;
    }

    /**
     * Pattern matching the leading country-code prefix of a phone
     * JID's user portion.
     *
     * @implNote WAPhoneFindCC {@code e} regex.
     */
    private static final Pattern COUNTRY_CODE_PREFIX = Pattern.compile(
            "^(1|2[07]|3[0-469]|4[013-9]|5[1-8]|6[0-6]|7|8[1246]|9[0-58])");
}
