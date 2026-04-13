package com.github.auties00.cobalt.client;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;

/**
 * A proxy configuration for network connections.
 *
 * <p>Two protocol families are supported: {@linkplain Http HTTP CONNECT}
 * and {@linkplain Socks SOCKS}. Instances are obtained from static factory
 * methods such as {@link #ofHttp(String, int)} and
 * {@link #ofSocks5(String, int)}, or parsed from a URI via {@link #of(URI)}.
 *
 * @see WhatsAppClientProxyAuthenticator
 */
public sealed interface WhatsAppClientProxy {

    /**
     * Returns the proxy server hostname.
     */
    String host();

    /**
     * Returns the proxy server port (1-65535).
     */
    int port();

    /**
     * Returns the authenticator for this proxy, if any.
     *
     * @return an optional authenticator
     */
    Optional<? extends WhatsAppClientProxyAuthenticator> authenticator();

    /**
     * Creates a plain HTTP CONNECT proxy with no authentication.
     *
     * @param host the proxy hostname
     * @param port the proxy port
     * @return the proxy configuration
     */
    static Http.Plain ofHttp(String host, int port) {
        return new Http.Plain(host, port, null);
    }

    /**
     * Creates a plain HTTP CONNECT proxy with the given authenticator.
     *
     * @param host          the proxy hostname
     * @param port          the proxy port
     * @param authenticator the authentication strategy
     * @return the proxy configuration
     */
    static Http.Plain ofHttp(String host, int port, WhatsAppClientProxyAuthenticator.Http authenticator) {
        return new Http.Plain(host, port, authenticator);
    }

    /**
     * Creates a TLS-encrypted HTTP CONNECT proxy with no authentication.
     *
     * @param host the proxy hostname
     * @param port the proxy port
     * @return the proxy configuration
     */
    static Http.Secure ofHttps(String host, int port) {
        return new Http.Secure(host, port, null);
    }

    /**
     * Creates a TLS-encrypted HTTP CONNECT proxy with the given authenticator.
     *
     * @param host          the proxy hostname
     * @param port          the proxy port
     * @param authenticator the authentication strategy
     * @return the proxy configuration
     */
    static Http.Secure ofHttps(String host, int port, WhatsAppClientProxyAuthenticator.Http authenticator) {
        return new Http.Secure(host, port, authenticator);
    }

    /**
     * Creates a SOCKS5 proxy with local DNS resolution and no authentication.
     *
     * @param host the proxy hostname
     * @param port the proxy port
     * @return the proxy configuration
     */
    static Socks.V5.Local ofSocks5(String host, int port) {
        return new Socks.V5.Local(host, port, null);
    }

    /**
     * Creates a SOCKS5 proxy with local DNS resolution.
     *
     * @param host          the proxy hostname
     * @param port          the proxy port
     * @param authenticator the SOCKS5 authenticator
     * @return the proxy configuration
     */
    static Socks.V5.Local ofSocks5(String host, int port, WhatsAppClientProxyAuthenticator.Socks.V5 authenticator) {
        return new Socks.V5.Local(host, port, authenticator);
    }

    /**
     * Creates a SOCKS5 proxy with remote DNS resolution and no authentication.
     *
     * @param host the proxy hostname
     * @param port the proxy port
     * @return the proxy configuration
     */
    static Socks.V5.Remote ofSocks5h(String host, int port) {
        return new Socks.V5.Remote(host, port, null);
    }

    /**
     * Creates a SOCKS5 proxy with remote DNS resolution.
     *
     * @param host          the proxy hostname
     * @param port          the proxy port
     * @param authenticator the SOCKS5 authenticator
     * @return the proxy configuration
     */
    static Socks.V5.Remote ofSocks5h(String host, int port, WhatsAppClientProxyAuthenticator.Socks.V5 authenticator) {
        return new Socks.V5.Remote(host, port, authenticator);
    }

    /**
     * Creates a SOCKS4 proxy with no user ID.
     *
     * @param host the proxy hostname
     * @param port the proxy port
     * @return the proxy configuration
     */
    static Socks.V4.Local ofSocks4(String host, int port) {
        return new Socks.V4.Local(host, port, null);
    }

    /**
     * Creates a SOCKS4 proxy with the given authenticator.
     *
     * @param host          the proxy hostname
     * @param port          the proxy port
     * @param authenticator the SOCKS4 user ID authenticator
     * @return the proxy configuration
     */
    static Socks.V4.Local ofSocks4(String host, int port, WhatsAppClientProxyAuthenticator.Socks.V4 authenticator) {
        return new Socks.V4.Local(host, port, authenticator);
    }

    /**
     * Creates a SOCKS4a proxy with remote DNS resolution and no user ID.
     *
     * @param host the proxy hostname
     * @param port the proxy port
     * @return the proxy configuration
     */
    static Socks.V4.Remote ofSocks4a(String host, int port) {
        return new Socks.V4.Remote(host, port, null);
    }

    /**
     * Creates a SOCKS4a proxy with remote DNS resolution.
     *
     * @param host          the proxy hostname
     * @param port          the proxy port
     * @param authenticator the SOCKS4 user ID authenticator
     * @return the proxy configuration
     */
    static Socks.V4.Remote ofSocks4a(String host, int port, WhatsAppClientProxyAuthenticator.Socks.V4 authenticator) {
        return new Socks.V4.Remote(host, port, authenticator);
    }

    /**
     * Parses a proxy configuration from a URI.
     *
     * <p>Supported schemes (with default ports): {@code http} (80),
     * {@code https} (443), {@code socks4} (1080), {@code socks4a} (1080),
     * {@code socks5} (1080), {@code socks5h} (1080). Credentials are
     * extracted from the URI user-info component when present.
     *
     * @param uri the proxy URI
     * @return the proxy configuration
     * @throws IllegalArgumentException if the scheme is unsupported
     */
    static WhatsAppClientProxy of(URI uri) {
        Objects.requireNonNull(uri, "uri");
        var scheme = Objects.requireNonNull(uri.getScheme(), "Proxy URI scheme cannot be null").toLowerCase();
        var host = Objects.requireNonNull(uri.getHost(), "Proxy URI host cannot be null");
        var port = uri.getPort();
        var userInfo = uri.getUserInfo();

        String username = null;
        String password = null;
        if (userInfo != null) {
            var colon = userInfo.indexOf(':');
            username = colon == -1 ? userInfo : userInfo.substring(0, colon);
            password = colon == -1 ? "" : userInfo.substring(colon + 1);
        }

        return switch (scheme) {
            case "http" -> {
                var p = port == -1 ? 80 : port;
                yield username != null
                        ? ofHttp(host, p, new WhatsAppClientProxyAuthenticator.Http.Basic(username, password))
                        : ofHttp(host, p);
            }
            case "https" -> {
                var p = port == -1 ? 443 : port;
                yield username != null
                        ? ofHttps(host, p, new WhatsAppClientProxyAuthenticator.Http.Basic(username, password))
                        : ofHttps(host, p);
            }
            case "socks4" -> {
                var p = port == -1 ? 1080 : port;
                yield username != null
                        ? ofSocks4(host, p, new WhatsAppClientProxyAuthenticator.Socks.V4(username))
                        : ofSocks4(host, p);
            }
            case "socks4a" -> {
                var p = port == -1 ? 1080 : port;
                yield username != null
                        ? ofSocks4a(host, p, new WhatsAppClientProxyAuthenticator.Socks.V4(username))
                        : ofSocks4a(host, p);
            }
            case "socks5" -> {
                var p = port == -1 ? 1080 : port;
                yield username != null
                        ? ofSocks5(host, p, new WhatsAppClientProxyAuthenticator.Socks.V5.UserPassword(username, password))
                        : ofSocks5(host, p);
            }
            case "socks5h" -> {
                var p = port == -1 ? 1080 : port;
                yield username != null
                        ? ofSocks5h(host, p, new WhatsAppClientProxyAuthenticator.Socks.V5.UserPassword(username, password))
                        : ofSocks5h(host, p);
            }
            default -> throw new IllegalArgumentException(
                    "Unsupported proxy scheme: " + scheme);
        };
    }

    /**
     * An HTTP CONNECT proxy. The connection to the proxy itself may be
     * plaintext ({@link Plain}) or TLS-encrypted ({@link Secure}).
     */
    sealed interface Http extends WhatsAppClientProxy {

        @Override
        Optional<WhatsAppClientProxyAuthenticator.Http> authenticator();

        /** A plain (unencrypted) HTTP CONNECT proxy. */
        final class Plain implements Http {

            private final String host;
            private final int port;
            private final WhatsAppClientProxyAuthenticator.Http authenticator;

            private Plain(String host, int port, WhatsAppClientProxyAuthenticator.Http authenticator) {
                Objects.requireNonNull(host, "host");
                if (port < 1 || port > 65535) {
                    throw new IllegalArgumentException(
                            "port must be between 1 and 65535: " + port);
                }
                this.host = host;
                this.port = port;
                this.authenticator = authenticator;
            }

            @Override
            public String host() {
                return host;
            }

            @Override
            public int port() {
                return port;
            }

            @Override
            public Optional<WhatsAppClientProxyAuthenticator.Http> authenticator() {
                return Optional.ofNullable(authenticator);
            }

            @Override
            public boolean equals(Object obj) {
                return obj == this
                       || (obj instanceof Plain other
                           && host.equals(other.host)
                           && port == other.port
                           && authenticator.equals(other.authenticator));
            }

            @Override
            public int hashCode() {
                return Objects.hash(host, port, authenticator);
            }

            @Override
            public String toString() {
                return "Http.Plain[host=" + host
                       + ", port=" + port
                       + ", authenticator=" + authenticator + "]";
            }
        }

        /**
         * A TLS-encrypted HTTP CONNECT proxy. The hostname is used for
         * both SNI and certificate verification.
         */
        final class Secure implements Http {

            private final String host;
            private final int port;
            private final WhatsAppClientProxyAuthenticator.Http authenticator;

            private Secure(String host, int port, WhatsAppClientProxyAuthenticator.Http authenticator) {
                Objects.requireNonNull(host, "host");
                if (port < 1 || port > 65535) {
                    throw new IllegalArgumentException(
                            "port must be between 1 and 65535: " + port);
                }
                this.host = host;
                this.port = port;
                this.authenticator = authenticator;
            }

            @Override
            public String host() {
                return host;
            }

            @Override
            public int port() {
                return port;
            }

            @Override
            public Optional<WhatsAppClientProxyAuthenticator.Http> authenticator() {
                return Optional.ofNullable(authenticator);
            }

            @Override
            public boolean equals(Object obj) {
                return obj == this || (obj instanceof Secure other
                                       && host.equals(other.host)
                                       && port == other.port);
            }

            @Override
            public int hashCode() {
                return Objects.hash(host, port, authenticator);
            }

            @Override
            public String toString() {
                return "Http.Secure[host=" + host
                       + ", port=" + port
                       + ", authenticator=" + authenticator + "]";
            }
        }
    }

    /**
     * A SOCKS proxy. Covers both SOCKS4/4a and SOCKS5 (RFC 1928).
     *
     * <p>SOCKS4 variants ({@link V4.Local}, {@link V4.Remote}) support an optional
     * user ID. SOCKS5 variants ({@link V5.Local}, {@link V5.Remote}) support RFC 1929
     * username/password authentication.
     */
    sealed interface Socks extends WhatsAppClientProxy {
        @Override
        Optional<? extends WhatsAppClientProxyAuthenticator.Socks> authenticator();

        /**
         * A SOCKS4 proxy. The connection resolves hostnames
         * locally ({@link Local}) or remotely ({@link Remote}, SOCKS4a).
         */
        sealed interface V4 extends Socks {
            @Override
            Optional<WhatsAppClientProxyAuthenticator.Socks.V4> authenticator();

            /** A SOCKS4 proxy. Resolves hostnames locally; IPv4 only. */
            final class Local implements V4 {

                private final String host;
                private final int port;
                private final WhatsAppClientProxyAuthenticator.Socks.V4 authenticator;

                private Local(String host, int port, WhatsAppClientProxyAuthenticator.Socks.V4 authenticator) {
                    Objects.requireNonNull(host, "host");
                    if (port < 1 || port > 65535) {
                        throw new IllegalArgumentException(
                                "port must be between 1 and 65535: " + port);
                    }
                    this.host = host;
                    this.port = port;
                    this.authenticator = authenticator;
                }

                @Override
                public String host() {
                    return host;
                }

                @Override
                public int port() {
                    return port;
                }

                @Override
                public Optional<WhatsAppClientProxyAuthenticator.Socks.V4> authenticator() {
                    return Optional.ofNullable(authenticator);
                }

                @Override
                public boolean equals(Object obj) {
                    return obj == this || (obj instanceof Local other
                                           && host.equals(other.host)
                                           && port == other.port);
                }

                @Override
                public int hashCode() {
                    return Objects.hash(host, port);
                }

                @Override
                public String toString() {
                    return "Socks.V4.Local[host=" + host
                           + ", port=" + port
                           + ", authenticator=" + authenticator + "]";
                }
            }

            /**
             * A SOCKS4a proxy. The proxy resolves hostnames on the client's
             * behalf by encoding a deliberate invalid IP ({@code 0.0.0.x})
             * and appending the domain name.
             */
            final class Remote implements V4 {

                private final String host;
                private final int port;
                private final WhatsAppClientProxyAuthenticator.Socks.V4 authenticator;

                private Remote(String host, int port, WhatsAppClientProxyAuthenticator.Socks.V4 authenticator) {
                    Objects.requireNonNull(host, "host");
                    if (port < 1 || port > 65535) {
                        throw new IllegalArgumentException(
                                "port must be between 1 and 65535: " + port);
                    }
                    this.host = host;
                    this.port = port;
                    this.authenticator = authenticator;
                }

                @Override
                public String host() {
                    return host;
                }

                @Override
                public int port() {
                    return port;
                }

                @Override
                public Optional<WhatsAppClientProxyAuthenticator.Socks.V4> authenticator() {
                    return Optional.ofNullable(authenticator);
                }

                @Override
                public boolean equals(Object obj) {
                    return obj == this || (obj instanceof Remote other
                                           && host.equals(other.host)
                                           && port == other.port);
                }

                @Override
                public int hashCode() {
                    return Objects.hash(host, port);
                }

                @Override
                public String toString() {
                    return "Socks.V4.Remote[host=" + host
                           + ", port=" + port
                           + ", authenticator=" + authenticator + "]";
                }
            }
        }

        /**
         * A SOCKS5 proxy (RFC 1928). The connection resolves hostnames
         * locally ({@link Local}) or remotely ({@link Remote}, socks5h).
         */
        sealed interface V5 extends Socks {
            @Override
            Optional<WhatsAppClientProxyAuthenticator.Socks.V5> authenticator();

            /** A SOCKS5 proxy with local DNS resolution (RFC 1928). */
            final class Local implements V5 {

                private final String host;
                private final int port;
                private final WhatsAppClientProxyAuthenticator.Socks.V5 authenticator;

                private Local(String host, int port, WhatsAppClientProxyAuthenticator.Socks.V5 authenticator) {
                    Objects.requireNonNull(host, "host");
                    if (port < 1 || port > 65535) {
                        throw new IllegalArgumentException(
                                "port must be between 1 and 65535: " + port);
                    }
                    this.host = host;
                    this.port = port;
                    this.authenticator = authenticator;
                }

                @Override
                public String host() {
                    return host;
                }

                @Override
                public int port() {
                    return port;
                }

                @Override
                public Optional<WhatsAppClientProxyAuthenticator.Socks.V5> authenticator() {
                    return Optional.ofNullable(authenticator);
                }

                @Override
                public boolean equals(Object obj) {
                    return obj == this || (obj instanceof Local other
                                           && host.equals(other.host)
                                           && port == other.port);
                }

                @Override
                public int hashCode() {
                    return Objects.hash(host, port, authenticator);
                }

                @Override
                public String toString() {
                    return "Socks.V5.Local[host=" + host
                           + ", port=" + port
                           + ", authenticator=" + authenticator + "]";
                }
            }

            /**
             * A SOCKS5 proxy with remote DNS resolution ({@code socks5h}).
             * The proxy resolves hostnames on the client's behalf, preventing
             * DNS leaks.
             */
            final class Remote implements V5 {

                private final String host;
                private final int port;
                private final WhatsAppClientProxyAuthenticator.Socks.V5 authenticator;

                private Remote(String host, int port, WhatsAppClientProxyAuthenticator.Socks.V5 authenticator) {
                    Objects.requireNonNull(host, "host");
                    if (port < 1 || port > 65535) {
                        throw new IllegalArgumentException(
                                "port must be between 1 and 65535: " + port);
                    }
                    this.host = host;
                    this.port = port;
                    this.authenticator = authenticator;
                }

                @Override
                public String host() {
                    return host;
                }

                @Override
                public int port() {
                    return port;
                }

                @Override
                public Optional<WhatsAppClientProxyAuthenticator.Socks.V5> authenticator() {
                    return Optional.ofNullable(authenticator);
                }

                @Override
                public boolean equals(Object obj) {
                    return obj == this || (obj instanceof Remote other
                                           && host.equals(other.host)
                                           && port == other.port);
                }

                @Override
                public int hashCode() {
                    return Objects.hash(host, port, authenticator);
                }

                @Override
                public String toString() {
                    return "Socks.V5.Remote[host=" + host
                           + ", port=" + port
                           + ", authenticator=" + authenticator + "]";
                }
            }
        }
    }
}
