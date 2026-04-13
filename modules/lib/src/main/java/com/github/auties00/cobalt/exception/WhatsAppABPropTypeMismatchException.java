package com.github.auties00.cobalt.exception;

import java.util.Objects;

/**
 * Exception thrown when an A/B test property (AB prop) value cannot be converted to the expected type.
 * <p>
 * WhatsApp uses A/B testing configuration properties to control feature rollouts and behavior.
 * These properties are sent from the server as string values with associated type hints. This
 * exception is thrown when the client attempts to read a property as a specific type, but the
 * value cannot be parsed as that type.
 *
 * <h2>AB Props Architecture</h2>
 * AB props are used by WhatsApp to:
 * <ul>
 *   <li>Control feature flags and rollouts</li>
 *   <li>Configure rate limits and thresholds</li>
 *   <li>Enable/disable experimental features</li>
 *   <li>Customize behavior per user segment</li>
 * </ul>
 *
 * <h2>Property Types</h2>
 * AB props can contain values of various types:
 * <ul>
 *   <li>{@link Boolean} - Feature flags (true/false)</li>
 *   <li>{@link Integer} - Numeric configuration values</li>
 *   <li>{@link Long} - Large numeric values or timestamps</li>
 *   <li>{@link Double} - Decimal values (e.g., probabilities)</li>
 *   <li>{@link String} - Text configuration values</li>
 * </ul>
 *
 * <h2>Possible Causes</h2>
 * <ul>
 *   <li><b>Server configuration error:</b> Server sent wrong type for config code</li>
 *   <li><b>Protocol update:</b> Property type changed in a new protocol version</li>
 *   <li><b>Client bug:</b> Code expects wrong type for a property</li>
 *   <li><b>Data corruption:</b> Value was corrupted during transmission</li>
 * </ul>
 *
 * <h2>Recovery</h2>
 * This is a non-fatal error. When this occurs:
 * <ol>
 *   <li>Log the mismatch for debugging</li>
 *   <li>Use a default value if available</li>
 *   <li>Consider updating the code to handle the actual type</li>
 * </ol>
 */
public final class WhatsAppABPropTypeMismatchException extends WhatsAppException {

    /**
     * The numeric configuration code that identifies the AB prop.
     */
    private final int configCode;

    /**
     * The type that the caller expected to receive.
     */
    private final Class<?> expectedType;

    /**
     * The actual string value that could not be converted to the expected type.
     */
    private final String actualValue;

    /**
     * Constructs a new AB prop type mismatch exception.
     *
     * @param configCode   the numeric configuration code identifying the AB prop
     * @param expectedType the type that was expected but could not be obtained;
     *                     must not be null
     * @param actualValue  the actual string value that couldn't be converted;
     *                     must not be null
     * @throws NullPointerException if expectedType or actualValue is null
     */
    public WhatsAppABPropTypeMismatchException(int configCode, Class<?> expectedType, String actualValue) {
        super(String.format(
                "AB prop type mismatch: code=%d, expected=%s, actualValue='%s'",
                configCode,
                Objects.requireNonNull(expectedType, "expectedType cannot be null").getSimpleName(),
                Objects.requireNonNull(actualValue, "actualValue cannot be null")
        ));
        this.configCode = configCode;
        this.expectedType = expectedType;
        this.actualValue = actualValue;
    }

    /**
     * Returns the numeric configuration code that identifies the AB prop.
     * <p>
     * Configuration codes are unique identifiers assigned to each AB prop by WhatsApp.
     * This code can be used to look up the expected type and meaning of the property.
     *
     * @return the config code
     */
    public int configCode() {
        return configCode;
    }

    /**
     * Returns the type that was expected but could not be obtained.
     * <p>
     * This is the type parameter that was passed to the property accessor method.
     *
     * @return the expected type class; never null
     */
    public Class<?> expectedType() {
        return expectedType;
    }

    /**
     * Returns the actual string value that couldn't be converted to the expected type.
     * <p>
     * This value can be examined to determine the actual type or format of the property,
     * which may help in diagnosing the cause of the mismatch.
     *
     * @return the actual string value; never null
     */
    public String actualValue() {
        return actualValue;
    }

    /**
     * Returns whether this exception represents a fatal error.
     * <p>
     * AB prop type mismatches are non-fatal. The client can continue operating,
     * typically by using a default value for the affected property.
     *
     * @return {@code false}
     */
    @Override
    public boolean isFatal() {
        return false;
    }
}
