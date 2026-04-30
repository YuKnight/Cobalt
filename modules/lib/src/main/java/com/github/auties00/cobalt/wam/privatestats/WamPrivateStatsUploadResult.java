package com.github.auties00.cobalt.wam.privatestats;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;

import java.util.Objects;

/**
 * Outcome of one private-stats buffer upload attempt.
 *
 * @param result           the categorised result code, mirroring
 *                         {@code WAWebWamEnumPsBufferUploadResult}
 * @param httpResponseCode the HTTP status code returned by the
 *                         endpoint, or {@code -1} when the request
 *                         could not be sent
 */
@WhatsAppWebModule(moduleName = "WAWebUploadPrivateStatsBackend")
@WhatsAppWebModule(moduleName = "WAWebWamEnumPsBufferUploadResult")
public record WamPrivateStatsUploadResult(Type result, int httpResponseCode) {
    /**
     * Validates the {@code result} component for null.
     *
     * @throws NullPointerException if {@code result} is {@code null}
     */
    public WamPrivateStatsUploadResult {
        Objects.requireNonNull(result, "result must not be null");
    }

    /**
     * Categorised upload outcomes mirroring the {@code result}
     * strings passed to the WhatsApp Web
     * {@code PsBufferUploadWamEvent}.
     */
    public enum Type {
        /**
         * Server returned {@code 200}.
         */
        SUCCESS,
        /**
         * Server returned {@code 500} or {@code 429}.
         */
        ERROR_SERVER_OTHER,
        /**
         * Server returned {@code 400} with a parse error indicator.
         */
        ERROR_PARSING,
        /**
         * Server returned {@code 400} with a decode error indicator.
         */
        ERROR_DECODING,
        /**
         * Token issuance failed and a credential could not be produced.
         */
        ERROR_CREDENTIAL,
        /**
         * Server returned {@code 401}; the {@code access_token} is no
         * longer accepted.
         */
        ERROR_ACCESS_TOKEN,
        /**
         * Network error or unexpected HTTP status.
         */
        ERROR_OTHER
    }
}
