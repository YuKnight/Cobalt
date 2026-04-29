package com.github.auties00.cobalt.registration.push.apns.plist.value;

/**
 * Plist floating-point value (the plist {@code <real>} type).
 * {@code float} sources are widened to {@code double} losslessly.
 *
 * @param value the floating-point value
 */
public record PlistFloatingPointValue(double value) implements PlistValue {
}
