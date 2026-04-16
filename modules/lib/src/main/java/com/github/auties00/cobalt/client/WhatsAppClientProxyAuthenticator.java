package com.github.auties00.cobalt.client;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

/**
 * An authentication strategy for outbound proxy connections used by
 * {@link WhatsAppClient}.
 *
 * <p>WhatsApp clients commonly sit behind an enterprise proxy, so the
 * socket stack needs to negotiate credentials with the proxy before the
 * TLS/Noise tunnel to the WhatsApp server is established. This sealed
 * interface groups the authentication strategies Cobalt supports: HTTP
 * {@code CONNECT} proxies produce a {@code Proxy-Authorization} header,
 * SOCKS4 proxies carry a plain user ID, and SOCKS5 proxies participate in
 * the RFC 1929 sub-negotiation handshake.
 *
 * <p>Concrete implementations are provided as records so they integrate
 * naturally with pattern matching in the proxy tunnel clients.
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
         * HTTP Basic authentication, as defined by RFC 7617.
         *
         * <p>The username and password are concatenated with a colon,
         * UTF-8 encoded and Base64 encoded to produce the
         * {@code Proxy-Authorization: Basic} value.
         *
         * @param username the username; must not be {@code null}
         * @param password the password; {@code null} is treated as an empty
         *                 string
         */
        record Basic(String username, String password) implements Http {

            /**
             * Canonical record constructor that validates the
             * {@code username} is non-null.
             *
             * @throws NullPointerException if {@code username} is {@code null}
             */
            public Basic {
                Objects.requireNonNull(username, "username");
            }

            /**
             * Returns the {@code Basic} {@code Proxy-Authorization} value
             * derived from this record's credentials.
             *
             * @param method     the HTTP method; validated for nullness but
             *                   not otherwise used by Basic authentication
             * @param uri        the request URI; validated for nullness
             * @param challenges the server challenges; validated for
             *                   nullness
             * @return the {@code Basic <credentials>} header value
             */
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
         * HTTP Bearer token authentication, as defined by RFC 6750.
         *
         * <p>The token is prefixed with the literal {@code "Bearer "} and
         * emitted as the {@code Proxy-Authorization} header value without
         * any additional encoding.
         *
         * @param token the opaque bearer token; must not be {@code null}
         */
        record Bearer(String token) implements Http {

            /**
             * Canonical record constructor that validates the
             * {@code token} is non-null.
             *
             * @throws NullPointerException if {@code token} is {@code null}
             */
            public Bearer {
                Objects.requireNonNull(token, "token");
            }

            /**
             * Returns the {@code Bearer} {@code Proxy-Authorization} value
             * derived from this record's token.
             *
             * @param method     the HTTP method; validated for nullness
             * @param uri        the request URI; validated for nullness
             * @param challenges the server challenges; validated for
             *                   nullness
             * @return the {@code Bearer <token>} header value
             */
            @Override
            public String authenticate(String method, String uri, List<String> challenges) {
                Objects.requireNonNull(method, "method");
                Objects.requireNonNull(uri, "uri");
                Objects.requireNonNull(challenges, "challenges");

                return "Bearer " + token;
            }
        }
    }

    /**
     * Authentication for the SOCKS family of proxies.
     *
     * <p>The two supported variants are SOCKS4 (and SOCKS4a), which carry a
     * bare user ID, and SOCKS5, which negotiate an authentication method
     * code during the initial handshake.
     */
    sealed interface Socks extends WhatsAppClientProxyAuthenticator {

        /**
         * SOCKS4 user ID authentication.
         *
         * <p>The user ID is serialised as a null-terminated ISO 8859-1
         * string and embedded into the SOCKS4 connect request. SOCKS4
         * offers no actual authentication: the identifier is purely
         * informational for the proxy logs.
         *
         * @param userId the user identifier to send during the SOCKS4
         *               handshake; must not be {@code null}
         */
        record V4(String userId) implements Socks {
            /**
             * Canonical record constructor that validates the
             * {@code userId} is non-null.
             *
             * @throws NullPointerException if {@code userId} is {@code null}
             */
            public V4 {
                Objects.requireNonNull(userId, "userId");
            }
        }

        /**
         * Authentication for SOCKS5 proxies, as defined by RFC 1928.
         *
         * <p>Implementations advertise a SOCKS5 method number that is
         * offered to the proxy during the initial method negotiation and
         * then handle any method-specific sub-negotiation.
         */
        sealed interface V5 extends Socks {

            /**
             * Returns the SOCKS5 method identifier that this authenticator
             * announces during the method negotiation.
             *
             * <p>Common values are {@code 0x00} (no authentication) and
             * {@code 0x02} (username/password, RFC 1929).
             *
             * @return the SOCKS5 method identifier
             */
            int methodId();

            /**
             * SOCKS5 username/password authentication (method {@code 0x02},
             * as defined by RFC 1929).
             *
             * <p>Both fields are ISO 8859-1 encoded and are limited to 255
             * bytes each by the wire format. A {@code null} password is
             * tolerated and treated as an empty string to match common
             * proxy deployments that only enforce the username field.
             *
             * @param username the username; must not be {@code null} and
             *                 must be at most 255 ISO 8859-1 bytes
             * @param password the password; may be {@code null} (treated as
             *                 an empty string) and, when non-null, must be
             *                 at most 255 ISO 8859-1 bytes
             */
            record UserPassword(String username, String password) implements V5 {

                /**
                 * The SOCKS5 method identifier for the username/password
                 * sub-negotiation defined by RFC 1929.
                 */
                private static final int METHOD_ID = 0x02;
                /**
                 * The maximum number of ISO 8859-1 bytes allowed in either
                 * the username or the password field, as dictated by the
                 * RFC 1929 wire format.
                 */
                private static final int MAX_LENGTH = 255;

                /**
                 * Constructs a new {@code UserPassword} authenticator after
                 * validating the byte-length constraints imposed by RFC
                 * 1929.
                 *
                 * @param username the username; must not be {@code null}
                 *                 and must fit in 255 ISO 8859-1 bytes
                 * @param password the password; may be {@code null}
                 *                 (treated as empty) and, when non-null,
                 *                 must fit in 255 ISO 8859-1 bytes
                 * @throws NullPointerException if {@code username} is
                 *                              {@code null}
                 * @throws IllegalArgumentException if either field exceeds
                 *                                  255 ISO 8859-1 bytes
                 */
                public UserPassword(String username, String password) {
                    Objects.requireNonNull(username, "username");
                    if (username.getBytes(StandardCharsets.ISO_8859_1).length > MAX_LENGTH) {
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

                /**
                 * Returns the SOCKS5 method identifier for username/password
                 * authentication.
                 *
                 * @return {@code 0x02}, as defined by RFC 1929
                 */
                @Override
                public int methodId() {
                    return METHOD_ID;
                }
            }
        }
    }
}
