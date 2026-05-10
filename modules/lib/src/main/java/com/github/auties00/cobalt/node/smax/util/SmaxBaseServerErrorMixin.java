package com.github.auties00.cobalt.node.smax.util;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.node.Node;

import java.util.Objects;
import java.util.Optional;

/**
 * Shared parser for the catch-all {@code ServerError} variant produced by
 * every SMAX RPC.
 *
 * <p>Every domain in WA Web ships a {@code WASmaxIn*BaseServerErrorMixin}
 * module. These are all near-identical: assert the {@code <iq>} tag,
 * delegate envelope validation to {@link SmaxIqErrorResponseMixin#validate},
 * then route the {@code <error/>} child through {@code WASmaxIn*ServerErrors}
 * to extract a transient-internal-failure projection. Cobalt deduplicates
 * the family into the single helper here. Per-domain enums layer the
 * {@code 5xx} → semantic-name mapping where callers need it, but the bulk
 * of {@code ServerError} variants only need the {@code (code, text)} pair.
 */
@WhatsAppWebModule(moduleName = "WASmaxInGroupsBaseServerErrorMixin")
@WhatsAppWebModule(moduleName = "WASmaxInGroupsServerErrors")
@WhatsAppWebModule(moduleName = "WASmaxInAbPropsIQErrorBadRequestMixin")
@WhatsAppWebModule(moduleName = "WASmaxInAbPropsIQErrorFeatureNotImplementedMixin")
@WhatsAppWebModule(moduleName = "WASmaxInAbPropsIQErrorInternalServerErrorMixin")
@WhatsAppWebModule(moduleName = "WASmaxInAbPropsNoRetryErrors")
@WhatsAppWebModule(moduleName = "WASmaxInAbPropsIQErrorBadRequestOrFeatureNotImplementedMixinGroup")
@WhatsAppWebModule(moduleName = "WASmaxInAccountIQErrorBadRequestMixin")
@WhatsAppWebModule(moduleName = "WASmaxInBizAccessTokenIQErrorBadRequestMixin")
@WhatsAppWebModule(moduleName = "WASmaxInBizAccessTokenIQErrorForbiddenMixin")
@WhatsAppWebModule(moduleName = "WASmaxInBizAccessTokenIQErrorInternalServerErrorMixin")
@WhatsAppWebModule(moduleName = "WASmaxInBizAccessTokenIQErrorServiceUnavailableMixin")
@WhatsAppWebModule(moduleName = "WASmaxInBizAccessTokenHackBaseIQErrorResponseMixin")
@WhatsAppWebModule(moduleName = "WASmaxInBizCtwaAdAccountHackBaseIQErrorResponseMixin")
@WhatsAppWebModule(moduleName = "WASmaxInBizLinkingHackBaseIQErrorResponseMixin")
@WhatsAppWebModule(moduleName = "WASmaxInBizMarketingMessageHackBaseIQErrorResponseMixin")
@WhatsAppWebModule(moduleName = "WASmaxInBugReportingHackBaseIQErrorResponseMixin")
@WhatsAppWebModule(moduleName = "WASmaxInBugReportingIQErrorBadRequestMixin")
@WhatsAppWebModule(moduleName = "WASmaxInBugReportingIQErrorInternalServerErrorMixin")
@WhatsAppWebModule(moduleName = "WASmaxInBizCtwaAdAccountIQErrorBadRequestMixin")
@WhatsAppWebModule(moduleName = "WASmaxInBizCtwaAdAccountIQErrorForbiddenMixin")
@WhatsAppWebModule(moduleName = "WASmaxInBizCtwaAdAccountIQErrorInternalServerErrorMixin")
@WhatsAppWebModule(moduleName = "WASmaxInBizCtwaAdAccountIQErrorServiceUnavailableMixin")
@WhatsAppWebModule(moduleName = "WASmaxInBizCtwaNativeAdIQErrorBadRequestMixin")
@WhatsAppWebModule(moduleName = "WASmaxInBizCtwaNativeAdIQErrorForbiddenMixin")
@WhatsAppWebModule(moduleName = "WASmaxInBizCtwaNativeAdIQErrorInternalServerErrorMixin")
@WhatsAppWebModule(moduleName = "WASmaxInBizCtwaNativeAdIQErrorServiceUnavailableMixin")
@WhatsAppWebModule(moduleName = "WASmaxInBizLinkingIQErrorBadRequestMixin")
@WhatsAppWebModule(moduleName = "WASmaxInBizLinkingIQErrorForbiddenMixin")
@WhatsAppWebModule(moduleName = "WASmaxInBizLinkingIQErrorInternalServerErrorMixin")
@WhatsAppWebModule(moduleName = "WASmaxInBizMarketingMessageIQErrorBadRequestMixin")
@WhatsAppWebModule(moduleName = "WASmaxInBizSettingsIQErrorBadRequestMixin")
@WhatsAppWebModule(moduleName = "WASmaxInBotIQErrorBadRequestMixin")
@WhatsAppWebModule(moduleName = "WASmaxInBotIQErrorForbiddenMixin")
@WhatsAppWebModule(moduleName = "WASmaxInBotIQErrorInternalServerErrorMixin")
@WhatsAppWebModule(moduleName = "WASmaxInBotIQErrorNotAllowedMixin")
@WhatsAppWebModule(moduleName = "WASmaxInBizSettingsIQErrorFeatureNotImplementedMixin")
@WhatsAppWebModule(moduleName = "WASmaxInBizSettingsIQErrorInternalServerErrorMixin")
@WhatsAppWebModule(moduleName = "WASmaxInBizSettingsIQErrorServiceUnavailableMixin")
@WhatsAppWebModule(moduleName = "WASmaxInBizMarketingMessageIQErrorForbiddenMixin")
@WhatsAppWebModule(moduleName = "WASmaxInBizMarketingMessageIQErrorInternalServerErrorMixin")
@WhatsAppWebModule(moduleName = "WASmaxInBizMarketingMessageIQErrorServiceUnavailableMixin")
@WhatsAppWebModule(moduleName = "WASmaxInBizMarketingMessageIQErrorBadRequestOrForbiddenOrInternalServerErrorOrServiceUnavailableMixinGroup")
@WhatsAppWebModule(moduleName = "WASmaxInBizMsgUserFeedbackIQErrorBadRequestMixin")
@WhatsAppWebModule(moduleName = "WASmaxInBizMsgUserFeedbackIQErrorFeatureNotImplementedMixin")
@WhatsAppWebModule(moduleName = "WASmaxInBizMsgUserFeedbackIQErrorForbiddenMixin")
@WhatsAppWebModule(moduleName = "WASmaxInBizMsgUserFeedbackIQErrorInternalServerErrorMixin")
@WhatsAppWebModule(moduleName = "WASmaxInBizMsgUserFeedbackIQErrorNotAcceptableMixin")
@WhatsAppWebModule(moduleName = "WASmaxInBizMsgUserFeedbackIQErrorRateOverlimitMixin")
@WhatsAppWebModule(moduleName = "WASmaxInBizMsgUserFeedbackUpdatePreferenceReqErrors")
@WhatsAppWebModule(moduleName = "WASmaxInBizMsgUserFeedbackUpdatePreferenceServerErrors")
@WhatsAppWebModule(moduleName = "WASmaxInBlocklistsIQErrorBadRequestMixin")
@WhatsAppWebModule(moduleName = "WASmaxInBlocklistsIQErrorFeatureNotImplementedMixin")
@WhatsAppWebModule(moduleName = "WASmaxInBlocklistsIQErrorForbiddenMixin")
@WhatsAppWebModule(moduleName = "WASmaxInBlocklistsIQErrorInternalServerErrorMixin")
@WhatsAppWebModule(moduleName = "WASmaxInBlocklistsIQErrorNotAcceptableMixin")
@WhatsAppWebModule(moduleName = "WASmaxInBlocklistsIQErrorNotAllowedMixin")
@WhatsAppWebModule(moduleName = "WASmaxInBlocklistsIQErrorRateOverlimitMixin")
@WhatsAppWebModule(moduleName = "WASmaxInBlocklistsGetBlocklistErrors")
@WhatsAppWebModule(moduleName = "WASmaxInBlocklistsServerErrors")
@WhatsAppWebModule(moduleName = "WASmaxInBlocklistsUpdateBlocklistErrors")
@WhatsAppWebModule(moduleName = "WASmaxInBlocklistsUpdateOptoutErrors")
@WhatsAppWebModule(moduleName = "WASmaxInGroupsIQErrorNotAcceptableMixin")
@WhatsAppWebModule(moduleName = "WASmaxInGroupsIQErrorAlreadyExistsMixin")
@WhatsAppWebModule(moduleName = "WASmaxInGroupsIQErrorBadRequestMixin")
@WhatsAppWebModule(moduleName = "WASmaxInGroupsIQErrorConflictMixin")
@WhatsAppWebModule(moduleName = "WASmaxInGroupsIQErrorFallbackClientMixin")
@WhatsAppWebModule(moduleName = "WASmaxInGroupsIQErrorGoneMixin")
@WhatsAppWebModule(moduleName = "WASmaxInGroupsIQErrorItemNotFoundMixin")
@WhatsAppWebModule(moduleName = "WASmaxInGroupsIQErrorNotAllowedMixin")
@WhatsAppWebModule(moduleName = "WASmaxInGroupsIQErrorNotAuthorizedMixin")
@WhatsAppWebModule(moduleName = "WASmaxInGroupsIQErrorParentLinkedGroupsParticipantsResourceLimitMixin")
@WhatsAppWebModule(moduleName = "WASmaxInStatsIQErrorNotAcceptableMixin")
public final class SmaxBaseServerErrorMixin {

    /**
     * Private constructor. The class is a static-only utility.
     */
    private SmaxBaseServerErrorMixin() {
        throw new AssertionError("SmaxBaseServerErrorMixin cannot be instantiated");
    }

    /**
     * Tries to parse a server-error envelope.
     *
     * <p>Returns {@link Optional#empty()} when the envelope check fails
     * ({@code description != "iq"} or {@code type != "error"} or echoed
     * identifiers don't match), the {@code <error/>} child is missing or
     * malformed, or the parsed code is below the {@code 500} threshold
     * that distinguishes server-side from client-side errors.
     *
     * @param reply   the inbound error stanza. Never {@code null}
     * @param request the outbound request. Used to validate echoed
     *                identifiers. Never {@code null}
     * @return an {@link Optional} carrying the parsed envelope, or empty
     *         when the stanza does not match the server-error schema
     * @throws NullPointerException if either argument is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WASmaxInGroupsBaseServerErrorMixin",
            exports = "parseBaseServerErrorMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInAbPropsIQErrorFeatureNotImplementedMixin",
            exports = "parseIQErrorFeatureNotImplementedMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInBizMsgUserFeedbackIQErrorFeatureNotImplementedMixin",
            exports = "parseIQErrorFeatureNotImplementedMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInBizSettingsIQErrorFeatureNotImplementedMixin",
            exports = "parseIQErrorFeatureNotImplementedMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInBlocklistsIQErrorFeatureNotImplementedMixin",
            exports = "parseIQErrorFeatureNotImplementedMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInAbPropsIQErrorInternalServerErrorMixin",
            exports = "parseIQErrorInternalServerErrorMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInBizAccessTokenIQErrorInternalServerErrorMixin",
            exports = "parseIQErrorInternalServerErrorMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInBizCtwaAdAccountIQErrorInternalServerErrorMixin",
            exports = "parseIQErrorInternalServerErrorMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInBizCtwaNativeAdIQErrorInternalServerErrorMixin",
            exports = "parseIQErrorInternalServerErrorMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInBizLinkingIQErrorInternalServerErrorMixin",
            exports = "parseIQErrorInternalServerErrorMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInBizMarketingMessageIQErrorInternalServerErrorMixin",
            exports = "parseIQErrorInternalServerErrorMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInBizMsgUserFeedbackIQErrorInternalServerErrorMixin",
            exports = "parseIQErrorInternalServerErrorMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInBizSettingsIQErrorInternalServerErrorMixin",
            exports = "parseIQErrorInternalServerErrorMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInBlocklistsIQErrorInternalServerErrorMixin",
            exports = "parseIQErrorInternalServerErrorMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInBotIQErrorInternalServerErrorMixin",
            exports = "parseIQErrorInternalServerErrorMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInBugReportingIQErrorInternalServerErrorMixin",
            exports = "parseIQErrorInternalServerErrorMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInBizAccessTokenIQErrorServiceUnavailableMixin",
            exports = "parseIQErrorServiceUnavailableMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInBizCtwaAdAccountIQErrorServiceUnavailableMixin",
            exports = "parseIQErrorServiceUnavailableMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInBizCtwaNativeAdIQErrorServiceUnavailableMixin",
            exports = "parseIQErrorServiceUnavailableMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInBizMarketingMessageIQErrorServiceUnavailableMixin",
            exports = "parseIQErrorServiceUnavailableMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInBizSettingsIQErrorServiceUnavailableMixin",
            exports = "parseIQErrorServiceUnavailableMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInBizMarketingMessageIQErrorBadRequestOrForbiddenOrInternalServerErrorOrServiceUnavailableMixinGroup",
            exports = "parseIQErrorBadRequestOrForbiddenOrInternalServerErrorOrServiceUnavailableMixinGroup",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInAbPropsNoRetryErrors",
            exports = "parseNoRetryErrors", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInAbPropsIQErrorBadRequestOrFeatureNotImplementedMixinGroup",
            exports = "parseIQErrorBadRequestOrFeatureNotImplementedMixinGroup",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInBizAccessTokenHackBaseIQErrorResponseMixin",
            exports = "parseHackBaseIQErrorResponseMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInBizCtwaAdAccountHackBaseIQErrorResponseMixin",
            exports = "parseHackBaseIQErrorResponseMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInBizLinkingHackBaseIQErrorResponseMixin",
            exports = "parseHackBaseIQErrorResponseMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInBizMarketingMessageHackBaseIQErrorResponseMixin",
            exports = "parseHackBaseIQErrorResponseMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInBugReportingHackBaseIQErrorResponseMixin",
            exports = "parseHackBaseIQErrorResponseMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInBizMsgUserFeedbackUpdatePreferenceServerErrors",
            exports = "parseUpdatePreferenceServerErrors", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInBlocklistsServerErrors",
            exports = "parseServerErrors", adaptation = WhatsAppAdaptation.ADAPTED)
    public static Optional<SmaxIqErrorResponseMixin.Envelope> parseServerError(Node reply, Node request) {
        Objects.requireNonNull(reply, "reply cannot be null");
        Objects.requireNonNull(request, "request cannot be null");
        if (!SmaxIqErrorResponseMixin.validate(reply, request)) {
            return Optional.empty();
        }
        var envelope = SmaxIqErrorResponseMixin.parseError(reply).orElse(null);
        if (envelope == null) {
            return Optional.empty();
        }
        if (envelope.code() < 500) {
            return Optional.empty();
        }
        return Optional.of(envelope);
    }

    /**
     * Tries to parse a client-error envelope (codes {@code < 500}).
     *
     * <p>Used by the {@code ClientError} variants of every SMAX RPC. The
     * complementary range to {@link #parseServerError(Node, Node)}, which
     * gates on {@code code >= 500}; together the two ranges form a disjoint
     * partition of the non-negative integer space. The lower bound is open
     * (no floor) because a handful of WA mixins assert
     * {@code code} values below {@code 400} that WA still groups under
     * {@code ClientErrors} disjunctions — see
     * {@code WASmaxInGroupsIQErrorAlreadyExistsMixin} ({@code code=304}),
     * which participates in
     * {@code WASmaxInGroupsAcceptGroupAddClientErrors.parseAcceptGroupAddClientErrors}
     * and
     * {@code WASmaxInGroupsJoinLinkedGroupClientErrors.parseJoinLinkedGroupClientErrors}.
     *
     * @param reply   the inbound error stanza. Never {@code null}
     * @param request the outbound request. Used to validate echoed
     *                identifiers. Never {@code null}
     * @return an {@link Optional} carrying the parsed envelope, or empty
     *         when the stanza does not match the client-error schema
     * @throws NullPointerException if either argument is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WASmaxInAbPropsIQErrorBadRequestMixin",
            exports = "parseIQErrorBadRequestMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInAbPropsNoRetryErrors",
            exports = "parseNoRetryErrors", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInAbPropsIQErrorBadRequestOrFeatureNotImplementedMixinGroup",
            exports = "parseIQErrorBadRequestOrFeatureNotImplementedMixinGroup",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInAccountIQErrorBadRequestMixin",
            exports = "parseIQErrorBadRequestMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInBizAccessTokenIQErrorBadRequestMixin",
            exports = "parseIQErrorBadRequestMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInBizCtwaAdAccountIQErrorBadRequestMixin",
            exports = "parseIQErrorBadRequestMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInBizCtwaNativeAdIQErrorBadRequestMixin",
            exports = "parseIQErrorBadRequestMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInBizLinkingIQErrorBadRequestMixin",
            exports = "parseIQErrorBadRequestMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInBizMarketingMessageIQErrorBadRequestMixin",
            exports = "parseIQErrorBadRequestMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInBizMsgUserFeedbackIQErrorBadRequestMixin",
            exports = "parseIQErrorBadRequestMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInBizSettingsIQErrorBadRequestMixin",
            exports = "parseIQErrorBadRequestMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInBlocklistsIQErrorBadRequestMixin",
            exports = "parseIQErrorBadRequestMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInBotIQErrorBadRequestMixin",
            exports = "parseIQErrorBadRequestMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInBugReportingIQErrorBadRequestMixin",
            exports = "parseIQErrorBadRequestMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInGroupsIQErrorBadRequestMixin",
            exports = "parseIQErrorBadRequestMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInGroupsIQErrorConflictMixin",
            exports = "parseIQErrorConflictMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInGroupsIQErrorFallbackClientMixin",
            exports = "parseIQErrorFallbackClientMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInGroupsIQErrorGoneMixin",
            exports = "parseIQErrorGoneMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInGroupsIQErrorItemNotFoundMixin",
            exports = "parseIQErrorItemNotFoundMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInGroupsIQErrorLockedMixin",
            exports = "parseIQErrorLockedMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInGroupsIQErrorNotAuthorizedMixin",
            exports = "parseIQErrorNotAuthorizedMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInGroupsIQErrorParentLinkedGroupsParticipantsResourceLimitMixin",
            exports = "parseIQErrorParentLinkedGroupsParticipantsResourceLimitMixin",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInBizAccessTokenIQErrorForbiddenMixin",
            exports = "parseIQErrorForbiddenMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInBizCtwaAdAccountIQErrorForbiddenMixin",
            exports = "parseIQErrorForbiddenMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInBizCtwaNativeAdIQErrorForbiddenMixin",
            exports = "parseIQErrorForbiddenMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInBizLinkingIQErrorForbiddenMixin",
            exports = "parseIQErrorForbiddenMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInBizMarketingMessageIQErrorForbiddenMixin",
            exports = "parseIQErrorForbiddenMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInBizMsgUserFeedbackIQErrorForbiddenMixin",
            exports = "parseIQErrorForbiddenMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInBlocklistsIQErrorForbiddenMixin",
            exports = "parseIQErrorForbiddenMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInBotIQErrorForbiddenMixin",
            exports = "parseIQErrorForbiddenMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInGroupsIQErrorForbiddenMixin",
            exports = "parseIQErrorForbiddenMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInBizMsgUserFeedbackIQErrorNotAcceptableMixin",
            exports = "parseIQErrorNotAcceptableMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInBizMsgUserFeedbackIQErrorNotAcceptableMixin",
            exports = "parseIQErrorNotAcceptableField", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInBizMsgUserFeedbackIQErrorRateOverlimitMixin",
            exports = "parseIQErrorRateOverlimitMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInBlocklistsIQErrorRateOverlimitMixin",
            exports = "parseIQErrorRateOverlimitMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInBizMsgUserFeedbackUpdatePreferenceReqErrors",
            exports = "parseUpdatePreferenceReqErrors", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInBlocklistsGetBlocklistErrors",
            exports = "parseGetBlocklistErrors", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInBlocklistsUpdateBlocklistErrors",
            exports = "parseUpdateBlocklistErrors", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInBlocklistsUpdateOptoutErrors",
            exports = "parseUpdateOptoutErrors", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInBlocklistsIQErrorNotAcceptableMixin",
            exports = "parseIQErrorNotAcceptableMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInBlocklistsIQErrorNotAcceptableMixin",
            exports = "parseIQErrorNotAcceptableField", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInBlocklistsIQErrorNotAllowedMixin",
            exports = "parseIQErrorNotAllowedMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInBotIQErrorNotAllowedMixin",
            exports = "parseIQErrorNotAllowedMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInGroupsIQErrorNotAllowedMixin",
            exports = "parseIQErrorNotAllowedMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInGroupsIQErrorNotAcceptableMixin",
            exports = "parseIQErrorNotAcceptableMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInGroupsIQErrorNotAcceptableMixin",
            exports = "parseIQErrorNotAcceptableField", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInGroupsIQErrorAlreadyExistsMixin",
            exports = "parseIQErrorAlreadyExistsMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInStatsIQErrorNotAcceptableMixin",
            exports = "parseIQErrorNotAcceptableMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInStatsIQErrorNotAcceptableMixin",
            exports = "parseIQErrorNotAcceptableField", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInBizMarketingMessageIQErrorBadRequestOrForbiddenOrInternalServerErrorOrServiceUnavailableMixinGroup",
            exports = "parseIQErrorBadRequestOrForbiddenOrInternalServerErrorOrServiceUnavailableMixinGroup",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInBizAccessTokenHackBaseIQErrorResponseMixin",
            exports = "parseHackBaseIQErrorResponseMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInBizCtwaAdAccountHackBaseIQErrorResponseMixin",
            exports = "parseHackBaseIQErrorResponseMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInBizLinkingHackBaseIQErrorResponseMixin",
            exports = "parseHackBaseIQErrorResponseMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInBizMarketingMessageHackBaseIQErrorResponseMixin",
            exports = "parseHackBaseIQErrorResponseMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInBugReportingHackBaseIQErrorResponseMixin",
            exports = "parseHackBaseIQErrorResponseMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    public static Optional<SmaxIqErrorResponseMixin.Envelope> parseClientError(Node reply, Node request) {
        Objects.requireNonNull(reply, "reply cannot be null");
        Objects.requireNonNull(request, "request cannot be null");
        if (!SmaxIqErrorResponseMixin.validate(reply, request)) {
            return Optional.empty();
        }
        var envelope = SmaxIqErrorResponseMixin.parseError(reply).orElse(null);
        if (envelope == null) {
            return Optional.empty();
        }
        if (envelope.code() >= 500) {
            return Optional.empty();
        }
        return Optional.of(envelope);
    }
}
