package com.github.auties00.cobalt.exception;

/**
 * Exception thrown when an AB prop exists but cannot be converted to the expected type.
 * <p>
 * This exception indicates a configuration mismatch where the code expects one type
 * (e.g., Integer) but the server provided a value that cannot be parsed as that type.
 * <p>
 * This is a programming error that should be fixed by either:
 * <ul>
 *   <li>Updating the code to use the correct type</li>
 *   <li>Verifying the config code is correct</li>
 *   <li>Handling the type mismatch gracefully in the application logic</li>
 * </ul>
 */
public final class ABPropTypeMismatchException extends RuntimeException {
    private final int configCode;
    private final Class<?> expectedType;
    private final String actualValue;

    /**
     * Constructs a new type mismatch exception.
     *
     * @param configCode   the config code of the prop
     * @param expectedType the type that was expected
     * @param actualValue  the actual string value that couldn't be converted
     */
    public ABPropTypeMismatchException(int configCode, Class<?> expectedType, String actualValue) {
        super(String.format(
                "AB prop type mismatch: code=%d, expected=%s, actualValue='%s'",
                configCode,
                expectedType.getSimpleName(),
                actualValue
        ));
        this.configCode = configCode;
        this.expectedType = expectedType;
        this.actualValue = actualValue;
    }

    /**
     * Returns the config code of the prop that caused the mismatch.
     *
     * @return the config code
     */
    public int getConfigCode() {
        return configCode;
    }

    /**
     * Returns the expected type that could not be satisfied.
     *
     * @return the expected type class
     */
    public Class<?> getExpectedType() {
        return expectedType;
    }

    /**
     * Returns the actual string value that couldn't be converted.
     *
     * @return the actual value
     */
    public String getActualValue() {
        return actualValue;
    }
}
