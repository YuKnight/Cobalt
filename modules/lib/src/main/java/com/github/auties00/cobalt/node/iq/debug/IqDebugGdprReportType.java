package com.github.auties00.cobalt.node.iq.debug;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import java.util.Optional;

/**
 * Closed set of GDPR report types recognised by the relay.
 *
 * @implNote {@code WAWebGdprConstants.ReportType =
 *           Mirrored(["Account","Newsletters"])}.
 */
@WhatsAppWebModule(moduleName = "WAWebGdprConstants")
public enum IqDebugGdprReportType {
    /**
     * The {@code Account} report type — covers the user's full
     * WhatsApp account export. Wire-encoded by omitting the
     * {@code report_type} attribute.
     */
    @WhatsAppWebExport(moduleName = "WAWebGdprConstants",
            exports = "ReportType.Account",
            adaptation = WhatsAppAdaptation.DIRECT)
    ACCOUNT(null),

    /**
     * The {@code Newsletters} report type — covers only the user's
     * newsletter follows / posts data. Wire-encoded as
     * {@code report_type="newsletters"}.
     */
    @WhatsAppWebExport(moduleName = "WAWebGdprConstants",
            exports = "ReportType.Newsletters",
            adaptation = WhatsAppAdaptation.DIRECT)
    NEWSLETTERS("newsletters");

    /**
     * The wire string emitted in the {@code report_type} attribute,
     * or {@code null} when the attribute is to be omitted entirely.
     */
    private final String wire;

    /**
     * Constructs a report-type constant.
     *
     * @param wire the wire string, or {@code null} to omit the
     *             attribute
     */
    IqDebugGdprReportType(String wire) {
        this.wire = wire;
    }

    /**
     * Returns the optional wire string for this report type.
     *
     * @return an {@link Optional} carrying the wire string, or
     *         empty when the attribute is omitted
     */
    public Optional<String> wire() {
        return Optional.ofNullable(wire);
    }
}
