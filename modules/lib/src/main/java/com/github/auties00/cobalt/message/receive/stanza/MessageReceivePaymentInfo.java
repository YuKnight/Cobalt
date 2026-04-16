package com.github.auties00.cobalt.message.receive.stanza;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;

import java.util.Optional;

/**
 * Holds the payment-related metadata parsed from an incoming message
 * stanza's {@code <pay>} and {@code <transaction>} children.
 *
 * <p>Payment messages in WhatsApp carry information about a payment
 * flow (send, request, invite), the receiver, and the transaction
 * details (currency, amount, timestamp, and status). The {@code <pay>}
 * and {@code <transaction>} nodes appear as siblings (both direct
 * children of the message node), not nested; Cobalt merges them into
 * a single payment info record so the caller does not need to know
 * which node carried which attribute.
 *
 * @implNote WAWebHandleMsgParser function R(): parses pay node type,
 * receiver JID string, transaction currency/amount/timestamp/status,
 * and novi/futureproof detection.
 */
@WhatsAppWebModule(moduleName = "WAWebHandleMsgParser")
public final class MessageReceivePaymentInfo {
    /**
     * Whether this payment represents a futureproofed (novi)
     * transaction.
     *
     * @implNote WAWebHandleMsgParser function R(): set when
     * {@code isNoviTransaction(pay)} or
     * {@code isNoviTransaction(transaction)} is true.
     */
    private final boolean futureproofed;

    /**
     * The string form of the receiver's JID.
     *
     * @implNote WAWebHandleMsgParser function R(): from
     * {@code l.receiver.toString()} for transaction, or
     * {@code n.attrString("receiver")} and
     * {@code e.attrString("recipient")} for pay send.
     */
    private final String receiverJid;

    /**
     * The ISO currency code of the payment.
     *
     * @implNote WAWebHandleMsgParser function R(): from {@code l.currency}
     * or {@code getAmount1000AndCurrency(n).currency}.
     */
    private final String currency;

    /**
     * The payment amount in 1/1000 units (for example cents scaled by
     * 1000 to preserve sub-cent precision).
     *
     * @implNote WAWebHandleMsgParser function R(): from {@code l.amount1000}
     * or {@code getAmount1000AndCurrency(n).amount1000}.
     */
    private final Long amount1000;

    /**
     * The Unix-second timestamp of the transaction.
     *
     * @implNote WAWebHandleMsgParser function R(): from {@code l.ts} or
     * {@code e.attrInt("t")} for pay send.
     */
    private final Long transactionTimestamp;

    /**
     * The transaction status, present only when the payment is relevant
     * to the current user (for example not a group payment between
     * other participants).
     *
     * @implNote WAWebHandleMsgParser function R(): from
     * {@code getPaymentTxnWebStatus(l.status)} or
     * {@code PaymentInfo$TxnStatus.INIT} for pay send.
     */
    private final String txnStatus;

    /**
     * Constructs a new payment info record with all parsed fields.
     *
     * @param futureproofed        whether this is a novi-style futureproofed transaction
     * @param receiverJid          the receiver JID string, or {@code null}
     * @param currency             the ISO currency code, or {@code null}
     * @param amount1000           the payment amount in 1/1000 units, or {@code null}
     * @param txnStatus            the transaction status, or {@code null}
     * @param transactionTimestamp the Unix-second transaction timestamp, or {@code null}
     *
     * @implNote WAWebHandleMsgParser function R(): builds the payment
     * info object with the merged pay/transaction fields.
     */
    @WhatsAppWebExport(moduleName = "WAWebHandleMsgParser", exports = "incomingMsgParser",
            adaptation = WhatsAppAdaptation.DIRECT)
    public MessageReceivePaymentInfo(
            boolean futureproofed,
            String receiverJid,
            String currency,
            Long amount1000,
            String txnStatus,
            Long transactionTimestamp
    ) {
        this.futureproofed = futureproofed;
        this.receiverJid = receiverJid;
        this.currency = currency;
        this.amount1000 = amount1000;
        this.txnStatus = txnStatus;
        this.transactionTimestamp = transactionTimestamp;
    }

    /**
     * Returns whether this transaction is a novi-style futureproofed
     * payment.
     *
     * @return {@code true} if futureproofed
     * @implNote WAWebHandleMsgParser function R(): {@code futureproofed}.
     */
    public boolean futureproofed() {
        return futureproofed;
    }

    /**
     * Returns the string form of the receiver's JID, when present.
     *
     * @return an {@link Optional} wrapping the receiver JID string
     * @implNote WAWebHandleMsgParser function R(): {@code receiverJid}.
     */
    public Optional<String> receiverJid() {
        return Optional.ofNullable(receiverJid);
    }

    /**
     * Returns the ISO currency code of the payment, when present.
     *
     * @return an {@link Optional} wrapping the currency code
     * @implNote WAWebHandleMsgParser function R(): {@code currency}.
     */
    public Optional<String> currency() {
        return Optional.ofNullable(currency);
    }

    /**
     * Returns the payment amount in 1/1000 units, when present.
     *
     * @return an {@link Optional} wrapping the amount
     * @implNote WAWebHandleMsgParser function R(): {@code amount1000}.
     */
    public Optional<Long> amount1000() {
        return Optional.ofNullable(amount1000);
    }

    /**
     * Returns the transaction status, when present.
     *
     * @return an {@link Optional} wrapping the status
     * @implNote WAWebHandleMsgParser function R(): {@code txnStatus}.
     */
    public Optional<String> txnStatus() {
        return Optional.ofNullable(txnStatus);
    }

    /**
     * Returns the Unix-second transaction timestamp, when present.
     *
     * @return an {@link Optional} wrapping the timestamp
     * @implNote WAWebHandleMsgParser function R(): {@code transactionTimestamp}.
     */
    public Optional<Long> transactionTimestamp() {
        return Optional.ofNullable(transactionTimestamp);
    }
}
