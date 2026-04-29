package com.github.auties00.cobalt.wam.privatestats;

import com.github.auties00.cobalt.util.DataUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.Objects;

/**
 * Uploads WAM private-stats buffers to the
 * {@code https://dit.whatsapp.net/deidentified_telemetry} endpoint.
 *
 * <p>Mirrors the {@code privateStatsUpload} module bundled with WA Web. The
 * request is a {@code multipart/form-data} POST carrying:
 *
 * <ul>
 *   <li>{@code access_token} — a hard-coded Facebook-style app credential
 *       ({@value #ACCESS_TOKEN}) that gates access to the endpoint.</li>
 *   <li>{@code credential} — {@code base64UrlSafe(token) + "+" +
 *       base64UrlSafe(HMAC-SHA256(sharedSecret, buffer))}, where
 *       {@code (token, sharedSecret)} comes from a fresh
 *       {@link WamPrivateStatsTokenIssuer#issue} round-trip.</li>
 *   <li>{@code message} — the encoded WAM buffer as an octet-stream attachment
 *       named {@value #BUFFER_FILE_NAME}.</li>
 *   <li>{@code meta_data} — JSON {@code {"t": unixSeconds, "p": 0}}.</li>
 * </ul>
 *
 * <p>The {@code access_token}, file name, and metadata layout were recovered
 * from the live JS bundle on 2026-04-27.
 *
 * @apiNote Java's {@link HttpClient} does not have native
 * {@code multipart/form-data} support; the body is assembled by hand with
 * a randomly generated boundary string.
 */
public final class WamPrivateStatsUploader {
    /**
     * The {@code dit.whatsapp.net} endpoint that accepts the upload.
     */
    private static final URI ENDPOINT = URI.create("https://dit.whatsapp.net/deidentified_telemetry");

    /**
     * The hard-coded {@code access_token} the WA Web JS bundle sends. It is
     * a Facebook {@code app_id|app_secret} pair, not a per-user secret.
     */
    private static final String ACCESS_TOKEN = "245118376424571|3e7d275052f1522bf3200afcf53841a7";

    /**
     * The filename advertised for the buffer attachment in the multipart
     * body, matching the WA Web bundle's {@code "WAMEventBuffer.dat"}
     * literal.
     */
    private static final String BUFFER_FILE_NAME = "WAMEventBuffer.dat";

    /**
     * The metadata field {@code p} ("priority" or similar opaque tag), set
     * to {@code 0} on every upload by the WA Web bundle.
     */
    private static final int META_PRIORITY = 0;

    /**
     * Base64 URL-safe encoder without padding, matching
     * {@code WABase64.encodeB64UrlSafe(bytes, true)}.
     */
    private static final Base64.Encoder URL_BASE64 = Base64.getUrlEncoder().withoutPadding();

    /**
     * Alphabet used for the random suffix of the multipart boundary,
     * mirroring WebKit's choice (mixed-case alphanumeric, no special
     * characters).
     */
    private static final char[] BOUNDARY_ALPHABET =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();

    /**
     * Length in characters of the random suffix appended to the boundary
     * prefix, matching WebKit's 16-character convention.
     */
    private static final int BOUNDARY_SUFFIX_LENGTH = 16;

    /**
     * Boundary prefix matching the WebKit/Chromium convention. Chosen so
     * that a Cobalt-issued request is byte-indistinguishable from a Chrome
     * {@code FormData} POST, which is the kind of request the
     * {@code dit.whatsapp.net} endpoint normally accepts.
     */
    private static final String BOUNDARY_PREFIX = "----WebKitFormBoundary";

    /**
     * Shared issuer used to acquire fresh {@link WamPrivateStatsToken}s on
     * every upload.
     */
    private final WamPrivateStatsTokenIssuer issuer;

    /**
     * HTTP client used for the {@code POST}. Reused across uploads for
     * connection pooling.
     */
    private final HttpClient httpClient;

    /**
     * Constructs a new uploader bound to a token issuer. The HTTP client
     * is created internally with default settings.
     *
     * @param issuer the issuer to use for acquiring tokens; must not be
     *               {@code null}
     * @throws NullPointerException if {@code issuer} is {@code null}
     */
    public WamPrivateStatsUploader(WamPrivateStatsTokenIssuer issuer) {
        this(issuer, HttpClient.newHttpClient());
    }

    /**
     * Constructs a new uploader bound to a token issuer and a specific
     * HTTP client (e.g. for testing or shared connection pooling).
     *
     * @param issuer     the issuer to use for acquiring tokens; must not be
     *                   {@code null}
     * @param httpClient the HTTP client to use; must not be {@code null}
     * @throws NullPointerException if either argument is {@code null}
     */
    public WamPrivateStatsUploader(WamPrivateStatsTokenIssuer issuer, HttpClient httpClient) {
        this.issuer = Objects.requireNonNull(issuer, "issuer must not be null");
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient must not be null");
    }

    /**
     * Uploads a single WAM buffer.
     *
     * <p>Acquires a fresh token from the configured
     * {@link WamPrivateStatsTokenIssuer}, authenticates the buffer via
     * {@code HMAC-SHA256(sharedSecret, buffer)}, and {@code POST}s the
     * multipart body to the endpoint. Returns a {@link WamPrivateStatsUploadResult} describing
     * the outcome; this method does not retry, leaving retry policy to
     * the caller.
     *
     * @param buffer the encoded WAM buffer
     * @return the upload outcome
     * @throws NullPointerException if {@code buffer} is {@code null}
     */
    public WamPrivateStatsUploadResult upload(byte[] buffer) {
        Objects.requireNonNull(buffer, "buffer must not be null");

        WamPrivateStatsToken token;
        try {
            token = issuer.issue();
        } catch (RuntimeException e) {
            return new WamPrivateStatsUploadResult(WamPrivateStatsUploadResult.Type.ERROR_CREDENTIAL, -1);
        }

        var credential = buildCredential(token, buffer);
        var boundary = generateBoundary();
        var body = buildMultipartBody(boundary, credential, buffer);

        var request = HttpRequest.newBuilder(ENDPOINT)
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .build();

        try {
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            var status = response.statusCode();
            return switch (status) {
                case 200 -> new WamPrivateStatsUploadResult(WamPrivateStatsUploadResult.Type.SUCCESS, status);
                case 400 -> new WamPrivateStatsUploadResult(WamPrivateStatsUploadResult.Type.ERROR_PARSING, status);
                case 401 -> new WamPrivateStatsUploadResult(WamPrivateStatsUploadResult.Type.ERROR_ACCESS_TOKEN, status);
                case 429, 500 -> new WamPrivateStatsUploadResult(WamPrivateStatsUploadResult.Type.ERROR_SERVER_OTHER, status);
                default -> new WamPrivateStatsUploadResult(WamPrivateStatsUploadResult.Type.ERROR_OTHER, status);
            };
        } catch (Throwable _) {
            return new WamPrivateStatsUploadResult(WamPrivateStatsUploadResult.Type.ERROR_OTHER, -1);
        }
    }

    /**
     * Generates a per-request multipart boundary in the WebKit/Chromium
     * format ({@code ----WebKitFormBoundary} + 16 random alphanumeric
     * characters).
     *
     * <p>This makes the request byte-indistinguishable from a Chrome
     * {@code FormData} POST. The boundary just needs to be unique within a
     * request and absent from any part — the format itself is arbitrary,
     * but matching the browser's pattern is the lowest-risk choice for an
     * endpoint that primarily serves browser traffic.
     *
     * @return a fresh boundary string
     */
    private String generateBoundary() {
        var suffix = new char[BOUNDARY_SUFFIX_LENGTH];
        for (var i = 0; i < BOUNDARY_SUFFIX_LENGTH; i++) {
            suffix[i] = BOUNDARY_ALPHABET[DataUtils.randomInt(BOUNDARY_ALPHABET.length)];
        }
        return BOUNDARY_PREFIX + new String(suffix);
    }

    /**
     * Builds the {@code credential} multipart field value:
     * {@code base64UrlSafe(token) + "+" + base64UrlSafe(hmac)}.
     *
     * <p>HMAC-SHA256 is computed inline so the
     * {@link NoSuchAlgorithmException} / {@link InvalidKeyException} catch
     * blocks live with the only call site that can produce them.
     *
     * @param token  the issued token
     * @param buffer the buffer being uploaded
     * @return the credential string
     */
    private static String buildCredential(WamPrivateStatsToken token, byte[] buffer) {
        try {
            var mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(token.sharedSecret(), "HmacSHA256"));
            var hmac = mac.doFinal(buffer);
            return URL_BASE64.encodeToString(token.token())
                    + "+"
                    + URL_BASE64.encodeToString(hmac);
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError("HmacSHA256 must be available on every JVM", e);
        } catch (InvalidKeyException e) {
            throw new IllegalStateException("HMAC key rejected", e);
        }
    }

    /**
     * Assembles the {@code multipart/form-data} body by computing its
     * total length up front and writing directly into a single
     * heap-allocated {@code byte[]}, avoiding the copy that
     * {@link java.io.ByteArrayOutputStream#toByteArray} performs.
     *
     * <p>Layout:
     *
     * <pre>
     * --boundary
     * Content-Disposition: form-data; name="access_token"
     *
     * 245118376424571|3e7d275052f1522bf3200afcf53841a7
     * --boundary
     * Content-Disposition: form-data; name="credential"
     *
     * {credential}
     * --boundary
     * Content-Disposition: form-data; name="message"; filename="WAMEventBuffer.dat"
     * Content-Type: application/octet-stream
     *
     * {buffer bytes}
     * --boundary
     * Content-Disposition: form-data; name="meta_data"
     *
     * {"t":unixSeconds,"p":0}
     * --boundary--
     * </pre>
     *
     * @param boundary   the boundary string (must not appear in any part)
     * @param credential the credential string
     * @param buffer     the WAM buffer
     * @return the body bytes
     */
    private static byte[] buildMultipartBody(String boundary, String credential, byte[] buffer) {
        var partSeparator = ("--" + boundary + "\r\n").getBytes(StandardCharsets.US_ASCII);
        var closingBoundary = ("--" + boundary + "--\r\n").getBytes(StandardCharsets.US_ASCII);
        var crlf = "\r\n".getBytes(StandardCharsets.US_ASCII);

        var accessTokenHeader = "Content-Disposition: form-data; name=\"access_token\"\r\n\r\n"
                .getBytes(StandardCharsets.US_ASCII);
        var accessTokenValue = ACCESS_TOKEN.getBytes(StandardCharsets.US_ASCII);

        var credentialHeader = "Content-Disposition: form-data; name=\"credential\"\r\n\r\n"
                .getBytes(StandardCharsets.US_ASCII);
        var credentialValue = credential.getBytes(StandardCharsets.US_ASCII);

        var messageHeader = ("Content-Disposition: form-data; name=\"message\"; filename=\""
                + BUFFER_FILE_NAME + "\"\r\nContent-Type: application/octet-stream\r\n\r\n")
                .getBytes(StandardCharsets.US_ASCII);

        var metaDataHeader = "Content-Disposition: form-data; name=\"meta_data\"\r\n\r\n"
                .getBytes(StandardCharsets.US_ASCII);
        var metaDataValue = ("{\"t\":" + Instant.now().getEpochSecond() + ",\"p\":" + META_PRIORITY + "}")
                .getBytes(StandardCharsets.US_ASCII);

        var totalLength = 4 * partSeparator.length
                + accessTokenHeader.length + accessTokenValue.length + crlf.length
                + credentialHeader.length + credentialValue.length + crlf.length
                + messageHeader.length + buffer.length + crlf.length
                + metaDataHeader.length + metaDataValue.length + crlf.length
                + closingBoundary.length;

        var body = new byte[totalLength];
        var offset = 0;

        System.arraycopy(partSeparator, 0, body, offset, partSeparator.length);
        offset = offset + partSeparator.length;
        System.arraycopy(accessTokenHeader, 0, body, offset, accessTokenHeader.length);
        offset = offset + accessTokenHeader.length;
        System.arraycopy(accessTokenValue, 0, body, offset, accessTokenValue.length);
        offset = offset + accessTokenValue.length;
        System.arraycopy(crlf, 0, body, offset, crlf.length);
        offset = offset + crlf.length;

        System.arraycopy(partSeparator, 0, body, offset, partSeparator.length);
        offset = offset + partSeparator.length;
        System.arraycopy(credentialHeader, 0, body, offset, credentialHeader.length);
        offset = offset + credentialHeader.length;
        System.arraycopy(credentialValue, 0, body, offset, credentialValue.length);
        offset = offset + credentialValue.length;
        System.arraycopy(crlf, 0, body, offset, crlf.length);
        offset = offset + crlf.length;

        System.arraycopy(partSeparator, 0, body, offset, partSeparator.length);
        offset = offset + partSeparator.length;
        System.arraycopy(messageHeader, 0, body, offset, messageHeader.length);
        offset = offset + messageHeader.length;
        System.arraycopy(buffer, 0, body, offset, buffer.length);
        offset = offset + buffer.length;
        System.arraycopy(crlf, 0, body, offset, crlf.length);
        offset = offset + crlf.length;

        System.arraycopy(partSeparator, 0, body, offset, partSeparator.length);
        offset = offset + partSeparator.length;
        System.arraycopy(metaDataHeader, 0, body, offset, metaDataHeader.length);
        offset = offset + metaDataHeader.length;
        System.arraycopy(metaDataValue, 0, body, offset, metaDataValue.length);
        offset = offset + metaDataValue.length;
        System.arraycopy(crlf, 0, body, offset, crlf.length);
        offset = offset + crlf.length;

        System.arraycopy(closingBoundary, 0, body, offset, closingBoundary.length);
        return body;
    }
}
