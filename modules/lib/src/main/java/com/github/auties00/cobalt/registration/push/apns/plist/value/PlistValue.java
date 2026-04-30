package com.github.auties00.cobalt.registration.push.apns.plist.value;

/**
 * Sealed value model for an Apple property list.
 *
 * <p>Mirrors the eight concrete types defined by the
 * {@code PropertyList-1.0.dtd}: {@link PlistDictionaryValue},
 * {@link PlistArrayValue}, {@link PlistStringValue},
 * {@link PlistDataValue}, {@link PlistIntegerValue},
 * {@link PlistFloatingPointValue}, {@link PlistBooleanValue}, and
 * {@link PlistDateValue}. Every variant is an immutable
 * {@code record}. The hierarchy is closed so callers can
 * pattern-match exhaustively over its members.
 */
public sealed interface PlistValue permits
        PlistDictionaryValue,
        PlistArrayValue,
        PlistStringValue,
        PlistDataValue,
        PlistIntegerValue,
        PlistFloatingPointValue,
        PlistBooleanValue,
        PlistDateValue {
}
