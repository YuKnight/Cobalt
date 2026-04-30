package com.github.auties00.cobalt.registration.push.apns.plist;

import com.github.auties00.cobalt.registration.push.apns.plist.binary.PlistBinaryParser;
import com.github.auties00.cobalt.registration.push.apns.plist.binary.PlistBinaryWriter;
import com.github.auties00.cobalt.registration.push.apns.plist.value.PlistValue;
import com.github.auties00.cobalt.registration.push.apns.plist.xml.PlistXmlParser;
import com.github.auties00.cobalt.registration.push.apns.plist.xml.PlistXmlWriter;

import java.io.IOException;

/**
 * Static facade over the format-specific plist parsers and writers.
 *
 * <p>{@link #parse(byte[])} auto-detects between the binary and XML
 * formats by looking at the magic bytes. The {@code writeXml} and
 * {@code writeBinary} methods delegate to the corresponding
 * format-specific writer. Callers should route through this class
 * rather than the implementation classes directly.
 *
 * <p>The class is non-instantiable.
 */
public final class Plist {
    /**
     * Hidden constructor. The class is a stateless namespace.
     */
    private Plist() {
    }

    /**
     * Parses a plist, auto-detecting between the binary and XML
     * formats by looking at the magic bytes.
     *
     * @param data the source bytes (non-{@code null})
     * @return the root value
     * @throws IOException if the source is malformed for the
     *                     detected format
     */
    public static PlistValue parse(byte[] data) throws IOException {
        if (PlistBinaryParser.isBinary(data)) {
            return PlistBinaryParser.parse(data);
        }
        return PlistXmlParser.parse(data);
    }

    /**
     * Serializes {@code root} as an XML plist with the canonical
     * Apple preamble.
     *
     * @param root the root value
     * @return the UTF-8 encoded XML bytes
     */
    public static byte[] writeXml(PlistValue root) {
        return PlistXmlWriter.write(root);
    }

    /**
     * Serializes {@code root} as a {@code bplist00} binary plist.
     *
     * @param root the root value
     * @return the binary plist bytes
     */
    public static byte[] writeBinary(PlistValue root) {
        return PlistBinaryWriter.write(root);
    }
}
