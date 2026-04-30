package com.github.auties00.cobalt.message.send.token;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;

import java.util.Objects;

/**
 * The result of generating a reporting token: the version paired with the token bytes.
 *
 * @see ReportingToken
 */
@WhatsAppWebModule(moduleName = "WAWebReportingTokenUtils")
public final class ReportingTokenResult {
    /**
     * The reporting token version that was used to derive the HMAC key.
     */
    private final int version;

    /**
     * The 16-byte truncated HMAC tag.
     */
    private final byte[] token;

    /**
     * Creates a new reporting token result.
     *
     * @param version the reporting token version
     * @param token   the token bytes
     * @throws NullPointerException if {@code token} is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WAWebReportingTokenUtils", exports = "genReportingToken",
            adaptation = WhatsAppAdaptation.DIRECT)
    public ReportingTokenResult(int version, byte[] token) {
        this.version = version;
        this.token = Objects.requireNonNull(token, "token");
    }

    /**
     * Returns the reporting token version.
     *
     * @return the version
     */
    @WhatsAppWebExport(moduleName = "WAWebReportingTokenUtils", exports = "genReportingToken",
            adaptation = WhatsAppAdaptation.DIRECT)
    public int version() {
        return version;
    }

    /**
     * Returns the token bytes.
     *
     * @return the 16-byte reporting token
     */
    @WhatsAppWebExport(moduleName = "WAWebReportingTokenUtils", exports = "genReportingToken",
            adaptation = WhatsAppAdaptation.DIRECT)
    public byte[] token() {
        return token;
    }

    /**
     * Returns a string representation of this reporting token result.
     *
     * @return a string containing the version and token length
     */
    @Override
    public String toString() {
        return "ReportingTokenResult[version=" + version +
                ", tokenLength=" + token.length + ']';
    }
}
