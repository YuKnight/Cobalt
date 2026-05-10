package com.github.auties00.cobalt.message.preview.linkify;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;

import java.util.Set;

/**
 * Snapshot of the top-level domains WhatsApp Web considers eligible
 * targets for URL detection and link-preview generation.
 *
 * <p>The set is cloned verbatim from {@code WATopLevelDomains.TLD} so
 * that {@link Linkify} produces exactly the same matches as the JS
 * runtime; deviating from the WA list would either miss links the user
 * intends to share or treat noise tokens as links.
 */
@WhatsAppWebModule(moduleName = "WATopLevelDomains")
final class TopLevelDomains {
    /**
     * The lower-case set of recognised top-level domain labels.
     */
    @WhatsAppWebExport(moduleName = "WATopLevelDomains", exports = "TLD",
            adaptation = WhatsAppAdaptation.DIRECT)
    static final Set<String> TLD = Set.of(
            "aaa", "abb", "abbott", "abogado", "abudhabi", "ac", "academy", "accountant", "accountants", "ad",
            "adult", "ae", "aero", "af", "afl", "africa", "ag", "agency", "ai", "aig",
            "airforce", "al", "alsace", "am", "amazon", "amex", "amsterdam", "android", "ao", "apartments",
            "app", "apple", "ar", "arab", "archi", "army", "arpa", "art", "as", "asia",
            "associates", "at", "au", "auction", "audi", "audio", "auspost", "auto", "autos", "aw",
            "aws", "ax", "az", "ba", "baby", "band", "bank", "bar", "barcelona", "barclaycard",
            "barclays", "bargains", "basketball", "bauhaus", "bayern", "bb", "bbva", "bd", "be", "beauty",
            "beer", "berlin", "best", "bet", "bf", "bg", "bh", "bi", "bible", "bid",
            "bike", "bingo", "bio", "biz", "bj", "black", "blackfriday", "blog", "blue", "bm",
            "bmw", "bn", "bnpparibas", "bo", "boats", "bond", "boo", "boston", "bot", "boutique",
            "box", "br", "bradesco", "broker", "brother", "brussels", "bs", "bt", "build", "builders",
            "business", "buzz", "bw", "by", "bz", "bzh", "ca", "cab", "cafe", "cam",
            "camera", "camp", "canon", "capetown", "capital", "car", "cards", "care", "career", "careers",
            "cars", "casa", "cash", "casino", "cat", "catering", "cba", "cc", "cd", "center",
            "ceo", "cern", "cf", "cfd", "cg", "ch", "charity", "chase", "chat", "cheap",
            "christmas", "chrome", "church", "ci", "citic", "city", "ck", "cl", "claims", "cleaning",
            "click", "clinic", "clothing", "cloud", "club", "clubmed", "cm", "cn", "co", "coach",
            "codes", "coffee", "college", "cologne", "com", "community", "company", "computer", "condos", "construction",
            "consulting", "contact", "contractors", "cooking", "cool", "coop", "corsica", "country", "coupons", "courses",
            "cpa", "cr", "credit", "cricket", "crs", "cu", "cuisinella", "cv", "cw", "cx",
            "cy", "cymru", "cyou", "cz", "dad", "dance", "date", "dating", "day", "de",
            "dealer", "deals", "delivery", "deloitte", "democrat", "dental", "desi", "design", "dev", "dhl",
            "diamonds", "diet", "digital", "direct", "directory", "discount", "diy", "dj", "dk", "dm",
            "do", "doctor", "dog", "domains", "download", "durban", "dvag", "dz", "earth", "ec",
            "eco", "edeka", "edu", "education", "ee", "eg", "email", "energy", "engineer", "engineering",
            "enterprises", "epson", "equipment", "es", "esq", "estate", "et", "eu", "eus", "events",
            "exchange", "expert", "exposed", "express", "extraspace", "fail", "faith", "family", "fan", "fans",
            "farm", "fashion", "feedback", "fi", "film", "finance", "financial", "fish", "fishing", "fit",
            "fitness", "fj", "fk", "flights", "flir", "florist", "flowers", "fm", "fo", "foo",
            "food", "football", "forex", "forsale", "forum", "foundation", "fox", "fr", "frl", "fujitsu",
            "fun", "fund", "furniture", "futbol", "fyi", "ga", "gal", "gallery", "game", "games",
            "garden", "gay", "gd", "gdn", "ge", "gent", "gf", "gg", "gh", "gi",
            "gift", "gifts", "gives", "giving", "gl", "glass", "gle", "global", "globo", "gm",
            "gmbh", "gn", "godaddy", "gold", "golf", "goog", "google", "gop", "gov", "gp",
            "gq", "gr", "graphics", "gratis", "green", "group", "gs", "gt", "guide", "guru",
            "gw", "gy", "hair", "hamburg", "haus", "health", "healthcare", "help", "hermes", "hiphop",
            "hk", "hm", "hn", "hockey", "holdings", "holiday", "homes", "honda", "horse", "host",
            "hosting", "house", "how", "hr", "ht", "hu", "ice", "icu", "id", "ie",
            "ikano", "il", "im", "immo", "immobilien", "in", "inc", "industries", "info", "ing",
            "ink", "institute", "insurance", "insure", "int", "international", "investments", "io", "iq", "ir",
            "irish", "is", "ismaili", "ist", "istanbul", "it", "itau", "itv", "java", "jcb",
            "je", "jetzt", "jewelry", "jio", "jm", "jnj", "jo", "jobs", "joburg", "jp",
            "kaufen", "ke", "kg", "kh", "ki", "kids", "kim", "kitchen", "kiwi", "kn",
            "koeln", "komatsu", "kp", "kpmg", "kr", "krd", "kred", "kw", "ky", "kyoto",
            "kz", "la", "land", "landrover", "lat", "law", "lawyer", "lb", "lc", "leclerc",
            "legal", "lgbt", "li", "lidl", "life", "lighting", "lilly", "limited", "limo", "link",
            "live", "lk", "llc", "loan", "loans", "local", "lol", "london", "love", "ls",
            "lt", "ltd", "ltda", "lu", "lundbeck", "luxe", "luxury", "lv", "ly", "ma",
            "madrid", "makeup", "man", "management", "mango", "market", "marketing", "markets", "mba", "mc",
            "md", "me", "media", "meet", "melbourne", "meme", "memorial", "men", "menu", "mg",
            "mh", "miami", "microsoft", "mil", "mk", "ml", "mm", "mn", "mo", "mobi",
            "moda", "moe", "mom", "monash", "money", "monster", "mortgage", "moscow", "motorcycles", "mov",
            "movie", "mp", "mq", "mr", "ms", "mt", "mu", "museum", "music", "mv",
            "mw", "mx", "my", "mz", "na", "nab", "nagoya", "name", "navy", "nc",
            "ne", "net", "network", "neustar", "new", "news", "next", "nexus", "nf", "ng",
            "ngo", "ni", "nico", "nike", "ninja", "nl", "no", "now", "np", "nr",
            "nrw", "ntt", "nu", "nyc", "nz", "observer", "okinawa", "om", "one", "ong",
            "onion", "onl", "online", "ooo", "orange", "org", "organic", "ovh", "pa", "page",
            "panasonic", "paris", "partners", "parts", "party", "pe", "pet", "pf", "pg", "ph",
            "pharmacy", "phd", "photo", "photography", "photos", "pics", "pictet", "pictures", "pink", "pioneer",
            "pizza", "pk", "pl", "place", "plumbing", "plus", "pm", "pn", "poker", "politie",
            "porn", "post", "pr", "press", "pro", "productions", "prof", "promo", "properties", "property",
            "ps", "pt", "pub", "pw", "py", "qa", "qpon", "quebec", "quest", "racing",
            "radio", "re", "realestate", "realtor", "recipes", "red", "rehab", "reisen", "ren", "rent",
            "rentals", "repair", "report", "republican", "rest", "restaurant", "review", "reviews", "rio", "rip",
            "ro", "rocks", "rodeo", "rs", "rsvp", "ru", "rugby", "ruhr", "run", "rw",
            "ryukyu", "sa", "saarland", "sale", "salon", "sandvik", "sanofi", "sap", "sarl", "saxo",
            "sb", "sbi", "sbs", "sc", "scb", "schmidt", "school", "schule", "schwarz", "science",
            "scot", "sd", "se", "seat", "security", "select", "sener", "services", "sex", "sexy",
            "sg", "sh", "sharp", "shell", "shiksha", "shoes", "shop", "shopping", "show", "si",
            "singles", "site", "sk", "ski", "skin", "sky", "sl", "sm", "sn", "sncf",
            "so", "soccer", "social", "software", "solar", "solutions", "sony", "soy", "space", "sport",
            "sr", "srl", "ss", "st", "statebank", "statefarm", "stockholm", "storage", "store", "stream",
            "studio", "study", "style", "su", "sucks", "supplies", "supply", "support", "surf", "surgery",
            "suzuki", "sv", "swiss", "sx", "sy", "sydney", "systems", "sz", "taipei", "tatamotors",
            "tatar", "tattoo", "tax", "taxi", "tc", "td", "team", "tech", "technology", "tel",
            "tennis", "teva", "tf", "tg", "th", "theater", "tickets", "tienda", "tips", "tirol",
            "tj", "tk", "tl", "tm", "tn", "to", "today", "tokyo", "tools", "top",
            "toshiba", "total", "tours", "town", "toyota", "toys", "tr", "trade", "trading", "training",
            "travel", "tt", "tube", "tui", "tv", "tw", "tz", "ua", "ug", "uk",
            "university", "uno", "uol", "us", "uy", "uz", "va", "vacations", "vanguard", "vc",
            "ve", "vegas", "ventures", "vet", "vg", "vi", "video", "vin", "vip", "vision",
            "vivo", "vlaanderen", "vn", "vodka", "vote", "voto", "voyage", "vu", "wales", "wang",
            "watch", "webcam", "weber", "website", "wedding", "weir", "wf", "wien", "wiki", "williamhill",
            "win", "wine", "woodside", "work", "works", "world", "ws", "wtf", "xin", "xyz",
            "yachts", "yandex", "ye", "yoga", "yokohama", "youtube", "yt", "za", "zappos", "zara",
            "zip", "zm", "zone", "zw",
            "бел", "дети", "москва", "онлайн", "рус", "рф", "укр",
            "भारत", "ভাৰত", "ભારત", "ଭାରତ", "セール", "中国",
            "公司", "我爱你", "移动", "网址", "网站", "网络",
            "닷넷", "닷컴", "한국"
    );

    /**
     * Hidden constructor for the constants holder.
     *
     * @throws UnsupportedOperationException always
     */
    private TopLevelDomains() {
        throw new UnsupportedOperationException("This is a constants holder and cannot be instantiated");
    }
}
