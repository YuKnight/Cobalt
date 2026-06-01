package com.github.auties00.cobalt.model.business.ads;

import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

import java.util.Objects;
import java.util.Optional;

/**
 * Input model for the estimated-daily-reach query of the Click-to-WhatsApp
 * ad creation flow.
 *
 * <p>A Click-to-WhatsApp ad is a paid promotion that opens a chat with the
 * business when tapped. While reviewing the ad the merchant sees a curve
 * projecting how many people the ad is expected to reach per day for a
 * chosen budget, audience, and placement. This input carries the
 * parameters the server uses to compute that projection.
 *
 * <p>The {@link #targetingSpecAudienceJson() audience targeting},
 * {@link #optimizationGoalInputJson() optimisation goal},
 * {@link #audienceOptionAudienceJson() chosen audience option}, and
 * {@link #configuredPlacementSpecJson() placement specification} are each
 * JSON-encoded objects whose field sets are defined by the server-side
 * input types and are therefore carried verbatim as strings. The
 * {@link #adAccountId() ad account}, {@link #currency() currency}, and
 * {@link #postId() promoted post id} scope the estimate. The
 * {@link #flowId() flow id} is an opaque correlator the editor mints to
 * group all telemetry, drafts, and queries belonging to one ad-creation
 * funnel run; the {@link #flow() flow name} is the textual step inside
 * that funnel where the estimate was requested.
 */
@ProtobufMessage(name = "BusinessAdEstimatedReachQuery")
public final class BusinessAdEstimatedReachQuery {
    /**
     * Advertising-account identifier the estimate is computed for. Unset
     * omits the variable.
     */
    @ProtobufProperty(index = 1, type = ProtobufType.STRING)
    final String adAccountId;

    /**
     * JSON-encoded audience targeting specification. The field set is
     * defined by the server and is carried verbatim. Unset omits the
     * variable.
     */
    @ProtobufProperty(index = 2, type = ProtobufType.STRING)
    final String targetingSpecAudienceJson;

    /**
     * JSON-encoded optimisation goal. The field set is defined by the
     * server and is carried verbatim. Unset omits the variable.
     */
    @ProtobufProperty(index = 3, type = ProtobufType.STRING)
    final String optimizationGoalInputJson;

    /**
     * JSON-encoded chosen audience option. The field set is defined by
     * the server and is carried verbatim. Unset omits the variable.
     */
    @ProtobufProperty(index = 4, type = ProtobufType.STRING)
    final String audienceOptionAudienceJson;

    /**
     * JSON-encoded configured placement specification. The field set is
     * defined by the server and is carried verbatim. Unset omits the
     * variable.
     */
    @ProtobufProperty(index = 5, type = ProtobufType.STRING)
    final String configuredPlacementSpecJson;

    /**
     * Billing currency the estimate is expressed in. Unset omits the
     * variable.
     */
    @ProtobufProperty(index = 6, type = ProtobufType.STRING)
    final String currency;

    /**
     * Identifier of the promoted post the estimate is computed for. Unset
     * omits the variable.
     */
    @ProtobufProperty(index = 7, type = ProtobufType.STRING)
    final String postId;

    /**
     * Opaque correlator grouping all telemetry, drafts, and queries that
     * belong to one ad-creation funnel run. Unset omits the variable.
     */
    @ProtobufProperty(index = 8, type = ProtobufType.STRING)
    final String flowId;

    /**
     * Textual name of the step inside the ad-creation funnel where the
     * estimate was requested. Unset omits the variable.
     */
    @ProtobufProperty(index = 9, type = ProtobufType.STRING)
    final String flow;

    /**
     * Constructs a new {@code BusinessAdEstimatedReachQuery}. Every
     * argument may be {@code null} to omit the corresponding variable from
     * the request.
     *
     * @param adAccountId                 the advertising-account identifier,
     *                                    or {@code null}
     * @param targetingSpecAudienceJson   the JSON-encoded audience targeting,
     *                                    or {@code null}
     * @param optimizationGoalInputJson   the JSON-encoded optimisation goal,
     *                                    or {@code null}
     * @param audienceOptionAudienceJson  the JSON-encoded chosen audience
     *                                    option, or {@code null}
     * @param configuredPlacementSpecJson the JSON-encoded configured
     *                                    placement, or {@code null}
     * @param currency                    the billing currency, or
     *                                    {@code null}
     * @param postId                      the promoted post identifier, or
     *                                    {@code null}
     * @param flowId                      the funnel-run correlator, or
     *                                    {@code null}
     * @param flow                        the funnel-step name, or
     *                                    {@code null}
     */
    public BusinessAdEstimatedReachQuery(String adAccountId, String targetingSpecAudienceJson,
                                         String optimizationGoalInputJson,
                                         String audienceOptionAudienceJson,
                                         String configuredPlacementSpecJson, String currency,
                                         String postId, String flowId, String flow) {
        this.adAccountId = adAccountId;
        this.targetingSpecAudienceJson = targetingSpecAudienceJson;
        this.optimizationGoalInputJson = optimizationGoalInputJson;
        this.audienceOptionAudienceJson = audienceOptionAudienceJson;
        this.configuredPlacementSpecJson = configuredPlacementSpecJson;
        this.currency = currency;
        this.postId = postId;
        this.flowId = flowId;
        this.flow = flow;
    }

    /**
     * Returns the advertising-account identifier.
     *
     * @return an {@link Optional} carrying the identifier, or empty when
     *         unset
     */
    public Optional<String> adAccountId() {
        return Optional.ofNullable(adAccountId);
    }

    /**
     * Returns the JSON-encoded audience targeting specification.
     *
     * @return an {@link Optional} carrying the JSON, or empty when unset
     */
    public Optional<String> targetingSpecAudienceJson() {
        return Optional.ofNullable(targetingSpecAudienceJson);
    }

    /**
     * Returns the JSON-encoded optimisation goal.
     *
     * @return an {@link Optional} carrying the JSON, or empty when unset
     */
    public Optional<String> optimizationGoalInputJson() {
        return Optional.ofNullable(optimizationGoalInputJson);
    }

    /**
     * Returns the JSON-encoded chosen audience option.
     *
     * @return an {@link Optional} carrying the JSON, or empty when unset
     */
    public Optional<String> audienceOptionAudienceJson() {
        return Optional.ofNullable(audienceOptionAudienceJson);
    }

    /**
     * Returns the JSON-encoded configured placement specification.
     *
     * @return an {@link Optional} carrying the JSON, or empty when unset
     */
    public Optional<String> configuredPlacementSpecJson() {
        return Optional.ofNullable(configuredPlacementSpecJson);
    }

    /**
     * Returns the billing currency.
     *
     * @return an {@link Optional} carrying the currency, or empty when
     *         unset
     */
    public Optional<String> currency() {
        return Optional.ofNullable(currency);
    }

    /**
     * Returns the identifier of the promoted post.
     *
     * @return an {@link Optional} carrying the identifier, or empty when
     *         unset
     */
    public Optional<String> postId() {
        return Optional.ofNullable(postId);
    }

    /**
     * Returns the funnel-run correlator.
     *
     * @return an {@link Optional} carrying the correlator, or empty when
     *         unset
     */
    public Optional<String> flowId() {
        return Optional.ofNullable(flowId);
    }

    /**
     * Returns the funnel-step name.
     *
     * @return an {@link Optional} carrying the name, or empty when unset
     */
    public Optional<String> flow() {
        return Optional.ofNullable(flow);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (BusinessAdEstimatedReachQuery) obj;
        return Objects.equals(adAccountId, that.adAccountId)
                && Objects.equals(targetingSpecAudienceJson, that.targetingSpecAudienceJson)
                && Objects.equals(optimizationGoalInputJson, that.optimizationGoalInputJson)
                && Objects.equals(audienceOptionAudienceJson, that.audienceOptionAudienceJson)
                && Objects.equals(configuredPlacementSpecJson, that.configuredPlacementSpecJson)
                && Objects.equals(currency, that.currency)
                && Objects.equals(postId, that.postId)
                && Objects.equals(flowId, that.flowId)
                && Objects.equals(flow, that.flow);
    }

    @Override
    public int hashCode() {
        return Objects.hash(adAccountId, targetingSpecAudienceJson, optimizationGoalInputJson,
                audienceOptionAudienceJson, configuredPlacementSpecJson, currency, postId, flowId,
                flow);
    }

    @Override
    public String toString() {
        return "BusinessAdEstimatedReachQuery[" +
                "adAccountId=" + adAccountId + ", " +
                "targetingSpecAudienceJson=" + targetingSpecAudienceJson + ", " +
                "optimizationGoalInputJson=" + optimizationGoalInputJson + ", " +
                "audienceOptionAudienceJson=" + audienceOptionAudienceJson + ", " +
                "configuredPlacementSpecJson=" + configuredPlacementSpecJson + ", " +
                "currency=" + currency + ", " +
                "postId=" + postId + ", " +
                "flowId=" + flowId + ", " +
                "flow=" + flow + ']';
    }
}
