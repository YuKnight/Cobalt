package com.github.auties00.cobalt.calls2.core;

import com.github.auties00.cobalt.model.call.CallEndReason;

import java.util.Optional;
import java.util.OptionalInt;

/**
 * Enumerates the result codes the wa-voip engine records for a call outcome.
 *
 * <p>The engine stores a call result separately from the call state: the state machine tracks where a
 * call is in its lifecycle, while the result records why it ended (or that it succeeded). This is the
 * result field the engine sets through its {@code set_call_result} path, distinct from the call-state
 * machine and from the separate call-log-state field. Each constant binds the numeric result code where
 * it was recovered ({@link #wireCode()}) and projects to the user-facing {@link CallEndReason} via
 * {@link #toEndReason()}.
 *
 * <p>The projection is defined by mapping each result to the wire {@code reason} literal it corresponds
 * to and delegating to {@link CallEndReason#fromWireValue(String)}, so the mapping rides the model's own
 * stable, unknown-collapsing wire contract rather than depending on the spelling of any model constant.
 * A result that does not denote a termination reason, such as {@link #ACCEPTED}, projects to
 * {@link CallEndReason#UNKNOWN}.
 *
 * @implNote This implementation ports the call-result codes the engine writes at {@code call_context +
 * 0x478} ({@code set_call_result}, fn10923, {@code call_state.cc:109}) in module {@code ff-tScznZ8P}. Every
 * code is confirmed against the byte-read {@code result_to_str} flat pointer table (fn10674/fn13682, data
 * offset {@code 0x125a18}, base=1), which matches the WAM {@code CALL_RESULT_TYPE} enum exactly:
 * {@code 1 = "Connected"}, {@code 6 = "SetupError"}, {@code 7 = "ServerNack"},
 * {@code 8 = "CallOfferAckNotReceived"}, {@code 0x13 = "PeerSetupError"}, {@code 0x16 = "AcceptedElsewhere"},
 * {@code 0x17 = "RejectedElsewhere"}, {@code 0x19 = "CallIsFull"}, {@code 0x1c = "CallDoesNotExistForRejoin"}.
 * The terminate-axis codes come from {@code handle_terminate} (fn11492), gated by the terminate reason:
 * {@code relay_bind_failed -> 6}, {@code setup_failed -> 0x13}, {@code accepted_elsewhere -> 0x16},
 * {@code rejected_elsewhere -> 0x17}, and the accept-after-terminate race {@code -> 0x1c}; a normal hangup
 * leaves the result at {@link #ACCEPTED} (the engine's {@code Connected}), the hangup being recorded on the
 * separate terminate-reason field, not the call result. The offer codes come from the offer path: an
 * unacknowledged offer {@code -> 8} (fn11513) and an offer NACK {@code -> 7} (fn11501); the accept-ack NACK
 * codes from {@code handle_accept_ack} (fn11502): {@code 404 -> 0x1c}, {@code 434 -> 0x19}. The reject-family
 * codes come from {@code reject_reason_to_call_result} (fn10924): {@code RejectedByUser -> 2},
 * {@code RejectedByServer -> 3}, {@code RejectedTOS -> 0xf}, {@code RejectedE2E -> 0x10}, and
 * {@code RejectedUnavailable -> 0x11}, so {@link #wireCode()} is bound for every constant.
 */
public enum Calls2CallResult {
    /**
     * Represents a call that connected successfully; not a termination reason.
     */
    ACCEPTED(1),

    /**
     * Represents a call that failed to set up, internal result string {@code SetupError}.
     *
     * <p>The engine records this on a local setup failure; {@code handle_terminate} sets it for the
     * {@code relay_bind_failed} terminate reason. {@link #toEndReason()} maps it to
     * {@link CallEndReason#SETUP_FAILED}.
     *
     * @implNote This implementation binds result code {@code 6} from the byte-read {@code result_to_str}
     * table entry 6 (data offset {@code 0x125a18}, string {@code "SetupError"}); {@code handle_terminate}
     * (fn11492) sets {@code set_call_result(6)} gated by the {@code relay_bind_failed} reason. A normal
     * hangup is NOT this code; it leaves the result at {@link #ACCEPTED} ({@code Connected}), the hangup
     * being recorded on the separate terminate-reason field.
     */
    SETUP_ERROR(6),

    /**
     * Represents an outbound offer the server rejected with a NACK, internal result string
     * {@code ServerNack}.
     *
     * <p>The engine records this when an offer-ack carries an error (the offer NACK). {@link #toEndReason()}
     * maps it to {@link CallEndReason#SETUP_FAILED}.
     *
     * @implNote This implementation binds result code {@code 7} from the byte-read {@code result_to_str}
     * table entry 7 (data offset {@code 0x125a18}, string {@code "ServerNack"}) and from the offer-ack
     * handler {@code handle_offer_ack} (fn11501), which sets {@code set_call_result(7)} on the NACK leg.
     * Unlike the accept ack, the offer-ack error carries no 404/434 sub-map; an offer NACK collapses to this
     * single code.
     */
    SERVER_NACK(7),

    /**
     * Represents an outbound call whose offer was never acknowledged by the server; the initial result
     * for a freshly placed call, cleared once the offer is acknowledged.
     */
    CALL_OFFER_ACK_NOT_RECEIVED(8),

    /**
     * Represents a call that failed to set up on the peer side, internal result string
     * {@code PeerSetupError}.
     *
     * <p>The engine records this on a peer-side setup failure; {@code handle_terminate} sets it for the
     * {@code setup_failed} terminate reason. {@link #toEndReason()} maps it to
     * {@link CallEndReason#SETUP_FAILED}.
     *
     * @implNote This implementation binds result code {@code 0x13} from the byte-read {@code result_to_str}
     * table entry 19 (data offset {@code 0x125a18}, string {@code "PeerSetupError"});
     * {@code handle_terminate} (fn11492) sets {@code set_call_result(0x13)} gated by the {@code setup_failed}
     * reason.
     */
    PEER_SETUP_ERROR(0x13),

    /**
     * Represents a call accepted on another device of the same account.
     */
    ACCEPTED_ELSEWHERE(0x16),

    /**
     * Represents a call rejected on another device of the same account.
     */
    REJECTED_ELSEWHERE(0x17),

    /**
     * Represents an accept the server rejected because the call no longer exists, internal result string
     * {@code CallDoesNotExistForRejoin}.
     *
     * <p>The engine records this result on an accept NACK carrying server error {@code 404}, and on the
     * terminate path when the local user accepted a call that had already been terminated so there is no
     * call left to rejoin. There is no distinct terminate-reason wire literal, so {@link #toEndReason()}
     * collapses to {@link CallEndReason#SETUP_FAILED}, the accept-side setup failure.
     *
     * @implNote This implementation binds result code {@code 0x1c} from the byte-read {@code result_to_str}
     * table entry 28 (data offset {@code 0x125a18}, string {@code "CallDoesNotExistForRejoin"}) and from the
     * accept-ack NACK map in {@code handle_accept_ack} (fn11502, {@code messages/handlers/basic.cc}), where
     * accept-ack error {@code 404} ({@code 0x194}) yields {@code set_call_result(0x1c)};
     * {@code handle_terminate} (fn11492) sets the same code when the user accepts after a terminate
     * ({@code parse_received_call_terminate_reason: setting call result to CALL_DOES_NOT_EXIST_FOR_REJOIN as
     * the user accepted the call after terminate}).
     */
    CALL_DOES_NOT_EXIST_FOR_REJOIN(0x1c),

    /**
     * Represents an accept the server rejected because the call is full, internal result string
     * {@code CallIsFull}.
     *
     * <p>The engine records this result on an accept NACK carrying server error {@code 434}. There is no
     * distinct terminate-reason wire literal, so {@link #toEndReason()} collapses to
     * {@link CallEndReason#SETUP_FAILED}, the accept-side setup failure.
     *
     * @implNote This implementation binds result code {@code 0x19} from the byte-read {@code result_to_str}
     * table entry 25 (data offset {@code 0x125a18}, string {@code "CallIsFull"}) and from the accept-ack NACK
     * map in {@code handle_accept_ack} (fn11502, {@code messages/handlers/basic.cc}), where accept-ack error
     * {@code 434} ({@code 0x1b2}) yields {@code set_call_result(0x19)}.
     */
    CALL_IS_FULL(0x19),

    /**
     * Represents a call rejected by the user, internal result string {@code RejectedByUser}.
     *
     * <p>The engine sets this result when an inbound {@code <reject>} stanza carries an empty reject
     * reason. There is no distinct terminate-reason wire literal, so {@link #toEndReason()} collapses to
     * the generic {@code rejected} reason.
     *
     * @implNote This implementation binds result code {@code 2} from {@code result_to_str} table entry
     * index 1 (data offset {@code 0x125a18}) and from {@code reject_reason_to_call_result} (fn10924,
     * {@code audio_stream.cc:16432-16435}), where an empty reject reason yields {@code set_call_result(2)}.
     */
    REJECTED_BY_USER(2),

    /**
     * Represents a call rejected by the server, internal result string {@code RejectedByServer}.
     *
     * <p>The engine sets this result for the inbound {@code <reject>} reason {@code uncallable}. There is
     * no distinct terminate-reason wire literal, so {@link #toEndReason()} collapses to the generic
     * {@code rejected} reason.
     *
     * @implNote This implementation binds result code {@code 3} from {@code result_to_str} table entry
     * index 2 (data offset {@code 0x125a18}) and from {@code reject_reason_to_call_result} (fn10924,
     * {@code audio_stream.cc:16427-16430}), where reject reason {@code uncallable} yields
     * {@code set_call_result(3)}.
     */
    REJECTED_BY_SERVER(3),

    /**
     * Represents a call rejected because the peer was unavailable, internal result string
     * {@code RejectedUnavailable}.
     *
     * <p>The engine sets this result for the inbound {@code <reject>} reason {@code unavailable}. This
     * value stays distinct only on the engine's result-string and call-log axis (result code {@code 0x11},
     * paralleling the model {@code CallLog.CallResult.UNAVAILABLE}); on the terminate axis there is no
     * matching {@link CallEndReason} member, so {@link #toEndReason()} collapses to the generic
     * {@code rejected} reason.
     *
     * @implNote This implementation binds result code {@code 0x11} from {@code result_to_str} table entry
     * index 16 (data offset {@code 0x125a18}) and from {@code reject_reason_to_call_result} (fn10924,
     * {@code audio_stream.cc:16453-16456}), where reject reason {@code unavailable} yields
     * {@code set_call_result(0x11)}. {@code unavailable} is a {@code <reject>} (message type 4) reason
     * sub-type, not a terminate (message type 5) reason literal; the terminate-reason mapper
     * {@code call_terminate_reason_from_string} (fn10925, {@code message_buffer.cc:408}) has no
     * {@code unavailable} case, so this code cannot project to a dedicated terminate reason and is
     * deliberately collapsed to {@code rejected} rather than given its own {@link CallEndReason}.
     */
    REJECTED_UNAVAILABLE(0x11),

    /**
     * Represents a call rejected because the terms of service were not accepted, internal result string
     * {@code RejectedTOS}.
     *
     * <p>The engine sets this result for the inbound {@code <reject>} reason {@code tos}. There is no
     * distinct terminate-reason wire literal, so {@link #toEndReason()} collapses to the generic
     * {@code rejected} reason.
     *
     * @implNote This implementation binds result code {@code 0xf} from {@code result_to_str} table entry
     * index 14 (data offset {@code 0x125a18}) and from {@code reject_reason_to_call_result} (fn10924,
     * {@code audio_stream.cc:16442-16445}), where reject reason {@code tos} yields
     * {@code set_call_result(0xf)}.
     */
    REJECTED_TOS(0xf),

    /**
     * Represents a call rejected because of an end-to-end encryption failure, internal result string
     * {@code RejectedE2E}.
     *
     * <p>The engine sets this result for the inbound {@code <reject>} reasons {@code enc} and
     * {@code no-raw-e2e}. There is no distinct terminate-reason wire literal, so {@link #toEndReason()}
     * collapses to the generic {@code rejected} reason.
     *
     * @implNote This implementation binds result code {@code 0x10} from {@code result_to_str} table entry
     * index 15 (data offset {@code 0x125a18}) and from {@code reject_reason_to_call_result} (fn10924,
     * {@code audio_stream.cc:16437-16440} for {@code enc} and {@code 16463-16466} for {@code no-raw-e2e}),
     * both yielding {@code set_call_result(0x10)}.
     */
    REJECTED_E2E(0x10);

    /**
     * Holds the numeric result code the engine stores in the call context.
     */
    private final int wireCode;

    /**
     * Constructs a result bound to its recovered numeric code.
     *
     * @param wireCode the numeric result code the engine stores in the call context
     */
    Calls2CallResult(int wireCode) {
        this.wireCode = wireCode;
    }

    /**
     * Returns the numeric result code the engine stores in the call context.
     *
     * <p>Every constant now carries a code recovered from the engine's {@code result_to_str} table and
     * its reject-reason mapper, so the result is always present; the {@link OptionalInt} return type is
     * retained for API stability.
     *
     * @return the numeric result code, always present
     */
    public OptionalInt wireCode() {
        return OptionalInt.of(wireCode);
    }

    /**
     * Projects this result onto the user-facing {@link CallEndReason}.
     *
     * <p>Each result is mapped to the wire {@code reason} literal it corresponds to, then resolved
     * through {@link CallEndReason#fromWireValue(String)}. {@link #ACCEPTED} carries no termination
     * reason and resolves to {@link CallEndReason#UNKNOWN}. The setup-failure results
     * ({@link #CALL_OFFER_ACK_NOT_RECEIVED}, {@link #SERVER_NACK}, {@link #SETUP_ERROR},
     * {@link #PEER_SETUP_ERROR}, {@link #CALL_DOES_NOT_EXIST_FOR_REJOIN}, {@link #CALL_IS_FULL}) all map to
     * the {@code setup_failed} reason. Every reject-family variant collapses to the generic
     * {@code rejected} reason: their
     * distinctions ({@code unavailable}, {@code tos}, {@code enc}, {@code uncallable}) are
     * {@code <reject>}-stanza reason sub-types that the engine's terminate-reason mapper does not carry,
     * so none of them has its own {@link CallEndReason} member.
     *
     * @return the corresponding public end reason, never {@code null}
     */
    public CallEndReason toEndReason() {
        var wireReason = switch (this) {
            case ACCEPTED -> null;
            case CALL_OFFER_ACK_NOT_RECEIVED, SERVER_NACK, SETUP_ERROR, PEER_SETUP_ERROR,
                 CALL_DOES_NOT_EXIST_FOR_REJOIN, CALL_IS_FULL -> "setup_failed";
            case ACCEPTED_ELSEWHERE -> "accepted_elsewhere";
            case REJECTED_ELSEWHERE -> "rejected_elsewhere";
            case REJECTED_BY_USER, REJECTED_BY_SERVER, REJECTED_UNAVAILABLE, REJECTED_TOS, REJECTED_E2E ->
                    "rejected";
        };
        return CallEndReason.fromWireValue(wireReason);
    }

    /**
     * Maps an accept-ack NACK error code to the call result the engine records for it.
     *
     * <p>The server returns an {@code <ack class="call" type="accept">} carrying an {@code error} code when
     * it rejects an accept. The two known codes map onto a result: {@code 404} to
     * {@link #CALL_DOES_NOT_EXIST_FOR_REJOIN} (the call no longer exists) and {@code 434} to
     * {@link #CALL_IS_FULL}. Any other error code records no specific result; the call still ends as an
     * accept-side setup failure, so the caller falls back to the generic outcome.
     *
     * @implNote This implementation ports the accept-ack NACK map in {@code handle_accept_ack} (fn11502,
     * {@code messages/handlers/basic.cc}): {@code error 404 (0x194) -> set_call_result(0x1c)} and
     * {@code error 434 (0x1b2) -> set_call_result(0x19)}; any other non-zero error sets no result code.
     *
     * @param error the {@code error} code carried on the accept NACK
     * @return the matching result, or {@link Optional#empty()} when {@code error} is not a mapped code
     */
    public static Optional<Calls2CallResult> fromAcceptAckError(int error) {
        return switch (error) {
            case 404 -> Optional.of(CALL_DOES_NOT_EXIST_FOR_REJOIN);
            case 434 -> Optional.of(CALL_IS_FULL);
            default -> Optional.empty();
        };
    }
}
