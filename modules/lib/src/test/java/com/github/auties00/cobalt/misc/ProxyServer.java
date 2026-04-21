package com.github.auties00.cobalt.misc;

import com.github.auties00.cobalt.client.WhatsAppClientProxy;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A minimal local proxy server for integration testing.
 *
 * <p>Implementations support HTTP CONNECT, HTTPS CONNECT, SOCKS4, and
 * SOCKS5.  Each proxy runs on a random ephemeral port and can be
 * converted to the corresponding {@link WhatsAppClientProxy} via
 * {@link #toProxy()}.
 *
 * <p>Call {@link #close()} to shut down the server and release the port.
 */
public sealed abstract class ProxyServer implements Closeable {
    /**
     * The executor for handling client connections.
     */
    protected final ExecutorService executor;

    /**
     * The bound server socket.
     */
    protected final ServerSocket serverSocket;

    /**
     * Creates and starts a proxy server.
     *
     * @param serverSocket the bound server socket
     */
    protected ProxyServer(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
        this.executor = Executors.newVirtualThreadPerTaskExecutor();
        executor.submit(this::acceptLoop);
    }

    /**
     * Returns the local port this proxy is listening on.
     *
     * @return the port number
     */
    public int port() {
        return serverSocket.getLocalPort();
    }

    /**
     * Returns the {@link WhatsAppClientProxy} configuration pointing
     * at this local server.
     *
     * @return the proxy configuration
     */
    public abstract WhatsAppClientProxy toProxy();

    @Override
    public void close() throws IOException {
        executor.shutdownNow();
        serverSocket.close();
    }

    /**
     * Accepts connections in a loop until the server socket is closed.
     */
    private void acceptLoop() {
        try {
            while (!serverSocket.isClosed()) {
                var client = serverSocket.accept();
                executor.submit(() -> {
                    try {
                        handleClient(client);
                    } catch (IOException _) {
                    } finally {
                        try {
                            client.close();
                        } catch (IOException _) {
                        }
                    }
                });
            }
        } catch (IOException _) {
            // Server socket closed
        }
    }

    /**
     * Handles a single client connection.
     *
     * @param client the client socket
     * @throws IOException if an I/O error occurs
     */
    protected abstract void handleClient(Socket client) throws IOException;

    /**
     * Relays bytes between two sockets until one side closes.
     *
     * @param a the first socket
     * @param b the second socket
     */
    protected static void relay(Socket a, Socket b) {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            executor.submit(() -> pipe(a, b));
            pipe(b, a);
        }
    }

    /**
     * Pipes bytes from source to destination until EOF.
     */
    private static void pipe(Socket source, Socket destination) {
        try {
            var buf = new byte[8192];
            var in = source.getInputStream();
            var out = destination.getOutputStream();
            int n;
            while ((n = in.read(buf)) != -1) {
                out.write(buf, 0, n);
                out.flush();
            }
        } catch (IOException _) {
        } finally {
            try {
                destination.shutdownOutput();
            } catch (IOException _) {
            }
        }
    }

    /**
     * Creates a plain HTTP CONNECT proxy.
     *
     * @return a new proxy server
     * @throws IOException if the server socket cannot be bound
     */
    public static ProxyServer http() throws IOException {
        return new HttpProxy(new ServerSocket(0));
    }

    /**
     * Creates an HTTPS CONNECT proxy with a self-signed certificate.
     *
     * @return a new proxy server
     * @throws Exception if the server socket or TLS context cannot be created
     */
    public static ProxyServer https() throws Exception {
        var kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        var keyPair = kpg.generateKeyPair();

        var now = Instant.now();
        var subject = new X500Name("CN=localhost");
        var certBuilder = new JcaX509v3CertificateBuilder(
                subject, BigInteger.ONE,
                Date.from(now), Date.from(now.plus(Duration.ofDays(1))),
                subject, keyPair.getPublic()
        );
        var signer = new JcaContentSignerBuilder("SHA256withRSA").build(keyPair.getPrivate());
        var cert = new JcaX509CertificateConverter().getCertificate(certBuilder.build(signer));

        var ks = KeyStore.getInstance("PKCS12");
        ks.load(null, null);
        ks.setKeyEntry("test", keyPair.getPrivate(), new char[0], new Certificate[]{cert});

        var kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, new char[0]);

        var sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), null, null);

        var serverSocket = sslContext.getServerSocketFactory().createServerSocket(0);
        return new HttpsProxy(serverSocket);
    }

    /**
     * Creates a SOCKS4 proxy.
     *
     * @return a new proxy server
     * @throws IOException if the server socket cannot be bound
     */
    public static ProxyServer socks4() throws IOException {
        return new Socks4Proxy(new ServerSocket(0));
    }

    /**
     * Creates a SOCKS5 proxy.
     *
     * @return a new proxy server
     * @throws IOException if the server socket cannot be bound
     */
    public static ProxyServer socks5() throws IOException {
        return new Socks5Proxy(new ServerSocket(0));
    }

    private static final class HttpProxy extends ProxyServer {
        private HttpProxy(ServerSocket serverSocket) {
            super(serverSocket);
        }

        @Override
        public WhatsAppClientProxy toProxy() {
            return WhatsAppClientProxy.ofHttp("127.0.0.1", port());
        }

        @Override
        protected void handleClient(Socket client) throws IOException {
            var in = client.getInputStream();
            var line = readLine(in);
            if (line == null || !line.toUpperCase().startsWith("CONNECT ")) {
                sendResponse(client, 400, "Bad Request");
                return;
            }

            var target = parseConnectTarget(line);
            if (target == null) {
                sendResponse(client, 400, "Bad Request");
                return;
            }

            // Consume remaining headers
            while (true) {
                var header = readLine(in);
                if (header == null || header.isEmpty()) {
                    break;
                }
            }

            try {
                var remote = new Socket(target.getHostString(), target.getPort());
                sendResponse(client, 200, "Connection Established");
                relay(client, remote);
                remote.close();
            } catch (IOException e) {
                sendResponse(client, 502, "Bad Gateway");
            }
        }

        private static void sendResponse(Socket client, int code, String reason) throws IOException {
            var response = "HTTP/1.1 " + code + " " + reason + "\r\n\r\n";
            client.getOutputStream().write(response.getBytes(StandardCharsets.US_ASCII));
            client.getOutputStream().flush();
        }
    }

    private static final class HttpsProxy extends ProxyServer {
        private HttpsProxy(ServerSocket serverSocket) {
            super(serverSocket);
        }

        @Override
        public WhatsAppClientProxy toProxy() {
            return WhatsAppClientProxy.ofHttps("127.0.0.1", port());
        }

        @Override
        protected void handleClient(Socket client) throws IOException {
            // Client already speaks TLS (SSLServerSocket handles it)
            var in = client.getInputStream();
            var line = readLine(in);
            if (line == null || !line.toUpperCase().startsWith("CONNECT ")) {
                var response = "HTTP/1.1 400 Bad Request\r\n\r\n";
                client.getOutputStream().write(response.getBytes(StandardCharsets.US_ASCII));
                client.getOutputStream().flush();
                return;
            }

            var target = parseConnectTarget(line);
            if (target == null) {
                var response = "HTTP/1.1 400 Bad Request\r\n\r\n";
                client.getOutputStream().write(response.getBytes(StandardCharsets.US_ASCII));
                client.getOutputStream().flush();
                return;
            }

            // Consume remaining headers
            while (true) {
                var header = readLine(in);
                if (header == null || header.isEmpty()) {
                    break;
                }
            }

            try {
                var remote = new Socket(target.getHostString(), target.getPort());
                var response = "HTTP/1.1 200 Connection Established\r\n\r\n";
                client.getOutputStream().write(response.getBytes(StandardCharsets.US_ASCII));
                client.getOutputStream().flush();
                relay(client, remote);
                remote.close();
            } catch (IOException e) {
                var response = "HTTP/1.1 502 Bad Gateway\r\n\r\n";
                client.getOutputStream().write(response.getBytes(StandardCharsets.US_ASCII));
                client.getOutputStream().flush();
            }
        }
    }

    private static final class Socks4Proxy extends ProxyServer {
        private Socks4Proxy(ServerSocket serverSocket) {
            super(serverSocket);
        }

        @Override
        public WhatsAppClientProxy toProxy() {
            return WhatsAppClientProxy.ofSocks4("127.0.0.1", port());
        }

        @Override
        protected void handleClient(Socket client) throws IOException {
            var in = client.getInputStream();
            var version = in.read();
            if (version != 4) {
                return;
            }

            var command = in.read();
            if (command != 1) {
                sendSocks4Reply(client.getOutputStream(), 0x5B);
                return;
            }

            var portHi = in.read();
            var portLo = in.read();
            var port = (portHi << 8) | portLo;

            var ip = new byte[4];
            if (in.read(ip) != 4) {
                return;
            }

            // Read user ID (null-terminated)
            skipNullTerminated(in);

            // Check for SOCKS4a (IP = 0.0.0.x where x != 0)
            String host;
            if (ip[0] == 0 && ip[1] == 0 && ip[2] == 0 && ip[3] != 0) {
                host = readNullTerminated(in);
            } else {
                host = InetAddress.getByAddress(ip).getHostAddress();
            }

            try {
                var remote = new Socket(host, port);
                sendSocks4Reply(client.getOutputStream(), 0x5A);
                relay(client, remote);
                remote.close();
            } catch (IOException e) {
                sendSocks4Reply(client.getOutputStream(), 0x5B);
            }
        }

        private static void sendSocks4Reply(OutputStream out, int status) throws IOException {
            var reply = new byte[8];
            reply[0] = 0x00;
            reply[1] = (byte) status;
            out.write(reply);
            out.flush();
        }
    }

    // ---- SOCKS5 ----
    private static final class Socks5Proxy extends ProxyServer {
        private Socks5Proxy(ServerSocket serverSocket) {
            super(serverSocket);
        }

        @Override
        public WhatsAppClientProxy toProxy() {
            return WhatsAppClientProxy.ofSocks5("127.0.0.1", port());
        }

        @Override
        protected void handleClient(Socket client) throws IOException {
            var in = client.getInputStream();
            var out = client.getOutputStream();

            // Greeting
            var version = in.read();
            if (version != 5) {
                return;
            }

            var nMethods = in.read();
            var methods = in.readNBytes(nMethods);

            // Accept NO_AUTH
            out.write(new byte[]{0x05, 0x00});
            out.flush();

            // Connect request
            if (in.read() != 5) {
                return;
            }
            var command = in.read();
            in.read(); // reserved

            var addrType = in.read();
            String host;
            switch (addrType) {
                case 0x01 -> { // IPv4
                    var addr = in.readNBytes(4);
                    host = InetAddress.getByAddress(addr).getHostAddress();
                }
                case 0x03 -> { // Domain
                    var len = in.read();
                    var domain = in.readNBytes(len);
                    host = new String(domain, StandardCharsets.US_ASCII);
                }
                case 0x04 -> { // IPv6
                    var addr = in.readNBytes(16);
                    host = InetAddress.getByAddress(addr).getHostAddress();
                }
                default -> {
                    sendSocks5Reply(out, 0x08, addrType);
                    return;
                }
            }

            var portHi = in.read();
            var portLo = in.read();
            var port = (portHi << 8) | portLo;

            if (command != 1) {
                sendSocks5Reply(out, 0x07, addrType);
                return;
            }

            try {
                var remote = new Socket(host, port);
                var bound = remote.getLocalAddress().getAddress();
                var boundPort = remote.getLocalPort();
                var reply = ByteBuffer.allocate(4 + bound.length + 2);
                reply.put((byte) 0x05);
                reply.put((byte) 0x00); // success
                reply.put((byte) 0x00); // reserved
                reply.put((byte) (bound.length == 4 ? 0x01 : 0x04));
                reply.put(bound);
                reply.putShort((short) boundPort);
                out.write(reply.array());
                out.flush();
                relay(client, remote);
                remote.close();
            } catch (IOException e) {
                sendSocks5Reply(out, 0x05, addrType);
            }
        }

        private static void sendSocks5Reply(OutputStream out, int replyCode, int addrType) throws IOException {
            var reply = new byte[]{0x05, (byte) replyCode, 0x00, (byte) addrType, 0, 0, 0, 0, 0, 0};
            out.write(reply);
            out.flush();
        }
    }

    /**
     * Reads an ASCII line terminated by CRLF or LF.
     */
    private static String readLine(InputStream in) throws IOException {
        var sb = new StringBuilder();
        int b;
        while ((b = in.read()) != -1) {
            if (b == '\n') {
                return sb.toString().stripTrailing();
            }
            sb.append((char) b);
        }
        return sb.isEmpty() ? null : sb.toString().stripTrailing();
    }

    /**
     * Parses "CONNECT host:port HTTP/1.x" into host and port.
     */
    private static InetSocketAddress parseConnectTarget(String line) {
        var parts = line.split("\\s+");
        if (parts.length < 2) {
            return null;
        }
        var authority = parts[1];
        var colon = authority.lastIndexOf(':');
        if (colon <= 0) {
            return null;
        }
        try {
            var host = authority.substring(0, colon);
            var port = Integer.parseInt(authority.substring(colon + 1));
            return new InetSocketAddress(host, port);
        } catch (NumberFormatException _) {
            return null;
        }
    }

    /**
     * Reads bytes until a null terminator, discarding them.
     */
    private static void skipNullTerminated(InputStream in) throws IOException {
        int b;
        while ((b = in.read()) != -1 && b != 0) {
            // skip
        }
    }

    /**
     * Reads a null-terminated ASCII string.
     */
    private static String readNullTerminated(InputStream in) throws IOException {
        var sb = new StringBuilder();
        int b;
        while ((b = in.read()) != -1 && b != 0) {
            sb.append((char) b);
        }
        return sb.toString();
    }

}
