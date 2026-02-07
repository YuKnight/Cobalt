package com.github.auties00.cobalt.message.send.token;

import java.util.Objects;

/**
 * The result of generating a reporting token: the version and
 * the token bytes.
 *
 * @apiNote WAWebReportingTokenUtils.genReportingToken: returns
 * {@code {version, reportingToken}}.
 * @see ReportingToken
 */
public final class ReportingTokenResult {
    private final int version;
    private final byte[] token;

    /**
     * Creates a new reporting token result.
     *
     * @param version the reporting token version
     * @param token   the token bytes
     * @throws NullPointerException if {@code token} is {@code null}
     */
    public ReportingTokenResult(int version, byte[] token) {
        this.version = version;
        this.token = Objects.requireNonNull(token, "token");
    }

    /**
     * Returns the reporting token version.
     *
     * @return the version
     *
     * @apiNote WAWebReportingTokenUtils.REPORTING_TOKEN_VERSION:
     * {@code {DEFAULT: 1, HISTORY_SYNC: -1}}.
     */
    public int version() {
        return version;
    }

    /**
     * Returns the token bytes.
     *
     * @return the 16-byte reporting token
     */
    public byte[] token() {
        return token;
    }

    @Override
    public String toString() {
        return "ReportingTokenResult[version=" + version +
                ", tokenLength=" + token.length + ']';
    }
}
