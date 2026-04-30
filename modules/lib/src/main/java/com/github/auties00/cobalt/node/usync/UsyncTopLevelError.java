package com.github.auties00.cobalt.node.usync;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;

import java.util.Objects;

/**
 * Top-level error returned when the IQ failed entirely (network error, server
 * NACK, or any other failure where the response's {@code type} attribute is
 * not {@code "result"}).
 *
 * @implNote Matches the catch branch of the {@code deprecatedSendIq} call in
 *     {@code WAWebUsync.execute} where the response shape is
 *     {@code {error: {all: ...}}}.
 */
@WhatsAppWebModule(moduleName = "WAWebUsync")
public final class UsyncTopLevelError {
    /**
     * Holds the {@code code} attribute on the {@code <error>} child of the IQ.
     */
    private final int errorCode;

    /**
     * Holds the {@code text} attribute on the {@code <error>} child. Never
     * {@code null} because it defaults to the empty string when absent.
     */
    private final String errorText;

    /**
     * Holds the {@code type} attribute on the {@code <error>} child. Never
     * {@code null} because it defaults to the empty string when absent.
     */
    private final String errorType;

    /**
     * Creates a new top-level error.
     *
     * @param errorCode the error code
     * @param errorText the error text, coerced to the empty string when
     *                  {@code null}
     * @param errorType the error type, coerced to the empty string when
     *                  {@code null}
     */
    public UsyncTopLevelError(int errorCode, String errorText, String errorType) {
        this.errorCode = errorCode;
        this.errorText = Objects.requireNonNullElse(errorText, "");
        this.errorType = Objects.requireNonNullElse(errorType, "");
    }

    /**
     * Returns the error code.
     *
     * @return the {@code code} attribute value
     */
    public int errorCode() {
        return errorCode;
    }

    /**
     * Returns the error text.
     *
     * @return the {@code text} attribute value, never {@code null}
     */
    public String errorText() {
        return errorText;
    }

    /**
     * Returns the error type.
     *
     * @return the {@code type} attribute value, never {@code null}
     */
    public String errorType() {
        return errorType;
    }
}
