package com.github.auties00.cobalt.node.smax.bugreporting;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqResultResponseMixin;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The outbound {@code <iq xmlns="fb:thrift_iq" smax_id="105"
 * type="set">} stanza variant.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutBugReportingReportBugRequest")
@WhatsAppWebModule(moduleName = "WASmaxOutBugReportingHackBaseIQSetRequestMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutBugReportingBaseIQSetRequestMixin")
public final class SmaxBugReportingReportBugRequest implements SmaxOperation.Request {
    /**
     * The optional sender JID. When set, routed verbatim into the
     * IQ's {@code from} attribute.
     */
    private final Jid iqFrom;

    /**
     * The free-form description text shown to the user. Never
     * {@code null}.
     */
    private final String descriptionElementValue;

    /**
     * The free-form debug-information JSON blob. Never {@code null}.
     */
    private final String debugInformationJsonElementValue;

    /**
     * The optional device-log handle attached to the report.
     */
    private final String deviceLogHandleElementValue;

    /**
     * The optional list of media uploads. Between {@code 0} and
     * {@code 10} entries.
     */
    private final List<SmaxBugReportingReportBugMediaUpload> mediaUploads;

    /**
     * The optional title label.
     */
    private final String titleElementValue;

    /**
     * The optional category label.
     */
    private final String categoryElementValue;

    /**
     * The optional client/server join-key marker.
     */
    private final String clientServerJoinKeyElementValue;

    /**
     * The optional reproducibility marker.
     */
    private final String reproducibilityElementValue;

    /**
     * Constructs a new bug-report request.
     *
     * @param iqFrom                          the optional sender
     *                                        JID. May be
     *                                        {@code null}
     * @param descriptionElementValue         the free-form
     *                                        description. Never
     *                                        {@code null}
     * @param debugInformationJsonElementValue the debug JSON. Never
     *                                        {@code null}
     * @param deviceLogHandleElementValue     the optional log
     *                                        handle. May be
     *                                        {@code null}
     * @param mediaUploads                    the optional media
     *                                        uploads. Never
     *                                        {@code null}. At most
     *                                        {@code 10} entries
     * @param titleElementValue               the optional title;
     *                                        may be {@code null}
     * @param categoryElementValue            the optional category;
     *                                        may be {@code null}
     * @param clientServerJoinKeyElementValue the optional join-key;
     *                                        may be {@code null}
     * @param reproducibilityElementValue     the optional
     *                                        reproducibility
     *                                        marker. May be
     *                                        {@code null}
     * @throws NullPointerException     if any required argument is
     *                                  {@code null}
     * @throws IllegalArgumentException if {@code mediaUploads}
     *                                  carries more than {@code 10}
     *                                  entries
     */
    public SmaxBugReportingReportBugRequest(Jid iqFrom,
                   String descriptionElementValue,
                   String debugInformationJsonElementValue,
                   String deviceLogHandleElementValue,
                   List<SmaxBugReportingReportBugMediaUpload> mediaUploads,
                   String titleElementValue,
                   String categoryElementValue,
                   String clientServerJoinKeyElementValue,
                   String reproducibilityElementValue) {
        this.iqFrom = iqFrom;
        this.descriptionElementValue = Objects.requireNonNull(descriptionElementValue,
                "descriptionElementValue cannot be null");
        this.debugInformationJsonElementValue = Objects.requireNonNull(debugInformationJsonElementValue,
                "debugInformationJsonElementValue cannot be null");
        this.deviceLogHandleElementValue = deviceLogHandleElementValue;
        Objects.requireNonNull(mediaUploads, "mediaUploads cannot be null");
        if (mediaUploads.size() > 10) {
            throw new IllegalArgumentException(
                    "mediaUploads must carry at most 10 entries");
        }
        this.mediaUploads = List.copyOf(mediaUploads);
        this.titleElementValue = titleElementValue;
        this.categoryElementValue = categoryElementValue;
        this.clientServerJoinKeyElementValue = clientServerJoinKeyElementValue;
        this.reproducibilityElementValue = reproducibilityElementValue;
    }

    /**
     * Returns the optional sender JID.
     *
     * @return an {@link Optional} carrying the JID
     */
    public Optional<Jid> iqFrom() {
        return Optional.ofNullable(iqFrom);
    }

    /**
     * Returns the free-form description text.
     *
     * @return the description. Never {@code null}
     */
    public String descriptionElementValue() {
        return descriptionElementValue;
    }

    /**
     * Returns the debug-information JSON blob.
     *
     * @return the blob. Never {@code null}
     */
    public String debugInformationJsonElementValue() {
        return debugInformationJsonElementValue;
    }

    /**
     * Returns the optional device-log handle.
     *
     * @return an {@link Optional} carrying the handle
     */
    public Optional<String> deviceLogHandleElementValue() {
        return Optional.ofNullable(deviceLogHandleElementValue);
    }

    /**
     * Returns the list of media uploads.
     *
     * @return an unmodifiable list. Never {@code null}
     */
    public List<SmaxBugReportingReportBugMediaUpload> mediaUploads() {
        return mediaUploads;
    }

    /**
     * Returns the optional title label.
     *
     * @return an {@link Optional} carrying the title
     */
    public Optional<String> titleElementValue() {
        return Optional.ofNullable(titleElementValue);
    }

    /**
     * Returns the optional category label.
     *
     * @return an {@link Optional} carrying the category
     */
    public Optional<String> categoryElementValue() {
        return Optional.ofNullable(categoryElementValue);
    }

    /**
     * Returns the optional client/server join-key marker.
     *
     * @return an {@link Optional} carrying the key
     */
    public Optional<String> clientServerJoinKeyElementValue() {
        return Optional.ofNullable(clientServerJoinKeyElementValue);
    }

    /**
     * Returns the optional reproducibility marker.
     *
     * @return an {@link Optional} carrying the marker
     */
    public Optional<String> reproducibilityElementValue() {
        return Optional.ofNullable(reproducibilityElementValue);
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and
     *         payload
     *
     * @implNote {@code WASmaxOutBugReportingReportBugRequest.makeReportBugRequest}
     *           composes the
     *           {@code WASmaxOutBugReportingHackBaseIQSetRequestMixin}
     *           ({@code from? to=S_WHATSAPP_NET}) and
     *           {@code WASmaxOutBugReportingBaseIQSetRequestMixin}
     *           ({@code id=generateId() type="set"}) over a payload
     *           comprising mandatory {@code <description/>} and
     *           {@code <debug_information_json/>} children, an
     *           OPTIONAL_CHILD {@code <device_log_handle/>}, a
     *           REPEATED_CHILD ({@code 0..10}) of
     *           {@code <media iv cipherKey type? fileName?/>}
     *           entries, and four trailing OPTIONAL_CHILD entries
     *           ({@code <title/>}, {@code <category/>},
     *           {@code <client_server_join_key/>},
     *           {@code <reproducibility/>}). The envelope carries
     *           the literal {@code xmlns="fb:thrift_iq"} and
     *           {@code smax_id=105} attributes.
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutBugReportingReportBugRequest",
            exports = "makeReportBugRequest", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var children = new ArrayList<Node>();
        // <description>{descriptionElementValue}</description>
        var descriptionNode = new NodeBuilder()
                .description("description")
                .content(descriptionElementValue)
                .build();
        children.add(descriptionNode);
        // <debug_information_json>{...}</debug_information_json>
        var debugNode = new NodeBuilder()
                .description("debug_information_json")
                .content(debugInformationJsonElementValue)
                .build();
        children.add(debugNode);
        if (deviceLogHandleElementValue != null) {
            // OPTIONAL_CHILD: <device_log_handle>{...}</device_log_handle>
            var deviceLogNode = new NodeBuilder()
                    .description("device_log_handle")
                    .content(deviceLogHandleElementValue)
                    .build();
            children.add(deviceLogNode);
        }
        // REPEATED_CHILD(media, 0, 10)
        for (var media : mediaUploads) {
            children.add(media.toNode());
        }
        if (titleElementValue != null) {
            // OPTIONAL_CHILD: <title>{...}</title>
            var titleNode = new NodeBuilder()
                    .description("title")
                    .content(titleElementValue)
                    .build();
            children.add(titleNode);
        }
        if (categoryElementValue != null) {
            // OPTIONAL_CHILD: <category>{...}</category>
            var categoryNode = new NodeBuilder()
                    .description("category")
                    .content(categoryElementValue)
                    .build();
            children.add(categoryNode);
        }
        if (clientServerJoinKeyElementValue != null) {
            // OPTIONAL_CHILD: <client_server_join_key>{...}</client_server_join_key>
            var joinKeyNode = new NodeBuilder()
                    .description("client_server_join_key")
                    .content(clientServerJoinKeyElementValue)
                    .build();
            children.add(joinKeyNode);
        }
        if (reproducibilityElementValue != null) {
            // OPTIONAL_CHILD: <reproducibility>{...}</reproducibility>
            var reproducibilityNode = new NodeBuilder()
                    .description("reproducibility")
                    .content(reproducibilityElementValue)
                    .build();
            children.add(reproducibilityNode);
        }
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "fb:thrift_iq")
                .attribute("smax_id", 105)
                .attribute("from", iqFrom)
                .attribute("to", Jid.userServer())
                .attribute("type", "set")
                .content(children);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxBugReportingReportBugRequest) obj;
        return Objects.equals(this.iqFrom, that.iqFrom)
                && Objects.equals(this.descriptionElementValue, that.descriptionElementValue)
                && Objects.equals(this.debugInformationJsonElementValue, that.debugInformationJsonElementValue)
                && Objects.equals(this.deviceLogHandleElementValue, that.deviceLogHandleElementValue)
                && Objects.equals(this.mediaUploads, that.mediaUploads)
                && Objects.equals(this.titleElementValue, that.titleElementValue)
                && Objects.equals(this.categoryElementValue, that.categoryElementValue)
                && Objects.equals(this.clientServerJoinKeyElementValue, that.clientServerJoinKeyElementValue)
                && Objects.equals(this.reproducibilityElementValue, that.reproducibilityElementValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(iqFrom, descriptionElementValue, debugInformationJsonElementValue,
                deviceLogHandleElementValue, mediaUploads, titleElementValue, categoryElementValue,
                clientServerJoinKeyElementValue, reproducibilityElementValue);
    }

    @Override
    public String toString() {
        return "SmaxBugReportingReportBugRequest[iqFrom=" + iqFrom
                + ", descriptionElementValue=" + descriptionElementValue
                + ", debugInformationJsonElementValue=" + debugInformationJsonElementValue
                + ", deviceLogHandleElementValue=" + deviceLogHandleElementValue
                + ", mediaUploads=" + mediaUploads
                + ", titleElementValue=" + titleElementValue
                + ", categoryElementValue=" + categoryElementValue
                + ", clientServerJoinKeyElementValue=" + clientServerJoinKeyElementValue
                + ", reproducibilityElementValue=" + reproducibilityElementValue + ']';
    }
}
