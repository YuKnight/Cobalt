package com.github.auties00.cobalt.client;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;

/**
 * An authentication strategy for proxy connections.
 *
 * <p>Each proxy protocol family defines its own authenticator subtypes:
 * {@linkplain Http HTTP} authenticators produce {@code Proxy-Authorization}
 * headers, {@link Socks.V4} carries a user ID for the SOCKS4 handshake, and
 * {@linkplain Socks.V5 SOCKS5} authenticators handle RFC 1929 sub-negotiation.
 *
 * @see WhatsAppClientProxy
 */
public sealed interface WhatsAppClientProxyAuthenticator {

    /**
     * Authentication for HTTP CONNECT proxies. Implementations produce
     * a {@code Proxy-Authorization} header value.
     */
    sealed interface Http extends WhatsAppClientProxyAuthenticator {

        /**
         * Computes the {@code Proxy-Authorization} header value.
         *
         * @param method     the HTTP method (typically {@code "CONNECT"})
         * @param uri        the request URI ({@code "host:port"})
         * @param challenges {@code Proxy-Authenticate} values from a 407,
         *                   or an empty list on the initial attempt
         * @return the header value
         * @throws IOException if the computation fails
         */
        String authenticate(String method, String uri, List<String> challenges) throws IOException;

        /**
         * HTTP Basic authentication (RFC 7617).
         *
         * @param username the username
         * @param password the password, may be {@code null}
         */
        record Basic(String username, String password) implements Http {

            public Basic {
                Objects.requireNonNull(username, "username");
            }

            @Override
            public String authenticate(String method, String uri, List<String> challenges) {
                Objects.requireNonNull(method, "method");
                Objects.requireNonNull(uri, "uri");
                Objects.requireNonNull(challenges, "challenges");

                var pair = username + ":" + Objects.requireNonNullElse(password, "");
                return "Basic " + Base64.getEncoder()
                        .encodeToString(pair.getBytes(StandardCharsets.UTF_8));
            }
        }

        /**
         * HTTP Bearer token authentication (RFC 6750).
         *
         * @param token the bearer token
         */
        record Bearer(String token) implements Http {

            public Bearer {
                Objects.requireNonNull(token, "token");
            }

            @Override
            public String authenticate(String method, String uri, List<String> challenges) {
                Objects.requireNonNull(method, "method");
                Objects.requireNonNull(uri, "uri");
                Objects.requireNonNull(challenges, "challenges");

                return "Bearer " + token;
            }
        }

        /**
         * HTTP Digest authentication (RFC 7616). Supports {@code MD5},
         * {@code MD5-sess}, {@code SHA-256}, and {@code SHA-256-sess}.
         *
         * @param username the username
         * @param password the password
         */
        record Digest(String username, String password) implements Http {

            private static final HexFormat HEX = HexFormat.of();

            public Digest {
                Objects.requireNonNull(username, "username");
                Objects.requireNonNull(password, "password");
            }

            @Override
            public String authenticate(String method, String uri, List<String> challenges) throws IOException {
                Objects.requireNonNull(method, "method");
                Objects.requireNonNull(uri, "uri");
                Objects.requireNonNull(challenges, "challenges");

                var challenge = findDigestChallenge(challenges);
                var params = parseChallenge(challenge);
                var realm = params.getOrDefault("realm", "");
                var nonce = params.getOrDefault("nonce", "");
                var qop = normalizeQop(params.get("qop"));
                var algorithm = params.getOrDefault("algorithm", "MD5");
                var opaque = params.get("opaque");

                var cnonce = generateCnonce();
                var nc = "00000001";

                var ha1 = computeHA1(algorithm, realm, nonce, cnonce);
                var ha2 = hashHex(algorithm, method + ":" + uri);

                String response;
                if (qop != null) {
                    response = hashHex(algorithm,
                            ha1 + ":" + nonce + ":" + nc + ":" + cnonce + ":" + qop + ":" + ha2);
                } else {
                    response = hashHex(algorithm, ha1 + ":" + nonce + ":" + ha2);
                }

                var header = new StringBuilder(256);
                header.append("Digest username=\"").append(username)
                        .append("\", realm=\"").append(realm)
                        .append("\", nonce=\"").append(nonce)
                        .append("\", uri=\"").append(uri)
                        .append("\", response=\"").append(response).append('"');
                if (qop != null) {
                    header.append(", qop=").append(qop)
                            .append(", nc=").append(nc)
                            .append(", cnonce=\"").append(cnonce).append('"');
                }
                if (opaque != null) {
                    header.append(", opaque=\"").append(opaque).append('"');
                }
                if (!"MD5".equalsIgnoreCase(algorithm)) {
                    header.append(", algorithm=").append(algorithm);
                }

                return header.toString();
            }

            private static String findDigestChallenge(List<String> challenges) throws IOException {
                for (var challenge : challenges) {
                    if (challenge.length() >= 6
                        && challenge.regionMatches(true, 0, "Digest", 0, 6)) {
                        return challenge;
                    }
                }
                throw new IOException(
                        "No Digest challenge found in Proxy-Authenticate headers");
            }

            private static String normalizeQop(String qop) {
                return qop == null ? null : qop.indexOf(',') >= 0 ? "auth" : qop;
            }

            private String computeHA1(String algorithm, String realm,
                                      String nonce, String cnonce) throws IOException {
                var base = hashHex(algorithm,
                        username + ":" + realm + ":" + password);
                if (algorithm.toLowerCase().endsWith("-sess")) {
                    return hashHex(algorithm,
                            base + ":" + nonce + ":" + cnonce);
                }
                return base;
            }

            private static Map<String, String> parseChallenge(String challenge) {
                var params = new HashMap<String, String>();
                var start = challenge.toLowerCase().indexOf("digest");
                if (start == -1) {
                    return params;
                }

                var data = challenge.substring(start + 6).trim();
                var i = 0;
                while (i < data.length()) {
                    while (i < data.length()
                           && (data.charAt(i) == ' ' || data.charAt(i) == ',')) {
                        i++;
                    }
                    if (i >= data.length()) {
                        break;
                    }

                    var keyStart = i;
                    while (i < data.length()
                           && data.charAt(i) != '='
                           && data.charAt(i) != ' '
                           && data.charAt(i) != ',') {
                        i++;
                    }
                    if (i >= data.length() || data.charAt(i) != '=') {
                        break;
                    }
                    var key = data.substring(keyStart, i).trim().toLowerCase();
                    i++; // skip '='

                    String value;
                    if (i < data.length() && data.charAt(i) == '"') {
                        i++; // skip opening quote
                        var sb = new StringBuilder();
                        while (i < data.length() && data.charAt(i) != '"') {
                            if (data.charAt(i) == '\\' && i + 1 < data.length()) {
                                sb.append(data.charAt(i + 1));
                                i += 2;
                            } else {
                                sb.append(data.charAt(i));
                                i++;
                            }
                        }
                        value = sb.toString();
                        if (i < data.length()) {
                            i++; // skip closing quote
                        }
                    } else {
                        var valueStart = i;
                        while (i < data.length()
                               && data.charAt(i) != ','
                               && data.charAt(i) != ' ') {
                            i++;
                        }
                        value = data.substring(valueStart, i);
                    }

                    params.put(key, value);
                }
                return params;
            }

            private static String hashHex(String algorithm, String input) throws IOException {
                try {
                    var md = MessageDigest.getInstance(
                            algorithm.replace("-sess", ""));
                    return HEX.formatHex(
                            md.digest(input.getBytes(StandardCharsets.UTF_8)));
                } catch (NoSuchAlgorithmException e) {
                    throw new IOException(
                            algorithm + " digest algorithm not available", e);
                }
            }

            private static String generateCnonce() {
                var bytes = new byte[16];
                new SecureRandom().nextBytes(bytes);
                return HEX.formatHex(bytes);
            }
        }
    }

    sealed interface Socks extends WhatsAppClientProxyAuthenticator {
        /**
         * SOCKS4 user ID authentication. The user ID is sent as a
         * null-terminated ISO 8859-1 string during the SOCKS4 handshake.
         *
         * @param userId the user ID
         */
        record V4(String userId) implements Socks {
            public V4 {
                Objects.requireNonNull(userId, "userId");
            }
        }

        /**
         * Authentication for SOCKS5 proxies (RFC 1928). Implementations
         * participate in the SOCKS5 method negotiation handshake.
         */
        sealed interface V5 extends Socks {
            /** Returns the SOCKS5 method number (e.g. {@code 0x02} for username/password). */
            int methodId();

            /**
             * SOCKS5 username/password authentication (RFC 1929, method {@code 0x02}).
             * Both values are encoded as ISO 8859-1 and must be at most 255 bytes.
             *
             * @param username the username
             * @param password the password, may be {@code null} (treated as empty)
             */
            record UserPassword(String username, String password) implements V5 {
                public UserPassword(String username, String password) {
                    Objects.requireNonNull(username, "username");
                    if (username.getBytes(StandardCharsets.ISO_8859_1).length > 255) {
                        throw new IllegalArgumentException(
                                "username must be at most 255 bytes (ISO_8859_1)");
                    }
                    this.username = username;

                    if(password == null) {
                        this.password = "";
                    } else {
                        if (password.getBytes(StandardCharsets.ISO_8859_1).length > 255) {
                            throw new IllegalArgumentException(
                                    "password must be at most 255 bytes (ISO_8859_1)");
                        }
                        this.password = password;
                    }
                }

                @Override
                public int methodId() {
                    return 0x02;
                }
            }
        }
    }
}
