package com.github.auties00.cobalt.message.receive.stanza;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;

import java.util.Optional;

/**
 * Holds the payment-related metadata parsed from an incoming message stanza's
 * {@code <pay>} and {@code <transaction>} children.
 *
 * <p>The two children appear as siblings (both direct children of the message node),
 * not nested. Cobalt merges them into a single record so callers do not need to know
 * which node carried which attribute. When both are present the {@code <transaction>}
 * fields take precedence.
 */
@WhatsAppWebModule(moduleName = "WAWebHandleMsgParser")
public final class MessageReceivePaymentInfo {
    /**
     * Whether this payment represents a futureproofed (novi) transaction.
     */
    private final boolean futureproofed;

    /**
     * String form of the receiver's JID.
     */
    private final String receiverJid;

    /**
     * ISO currency code of the payment.
     */
    private final String currency;

    /**
     * Payment amount in 1/1000 units (cents scaled by 1000 to preserve sub-cent
     * precision).
     */
    private final Long amount1000;

    /**
     * Unix-second timestamp of the transaction.
     */
    private final Long transactionTimestamp;

    /**
     * Transaction status, present only when the payment is relevant to the current
     * user (for example not a group payment between other participants).
     */
    private final String txnStatus;

    /**
     * Constructs a new payment info record.
     *
     * @param futureproofed        whether this is a novi-style futureproofed transaction
     * @param receiverJid          the receiver JID string, or {@code null}
     * @param currency             the ISO currency code, or {@code null}
     * @param amount1000           the payment amount in 1/1000 units, or {@code null}
     * @param txnStatus            the transaction status, or {@code null}
     * @param transactionTimestamp the Unix-second transaction timestamp, or {@code null}
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
     * Returns whether this transaction is a novi-style futureproofed payment.
     *
     * @return {@code true} if futureproofed
     */
    public boolean futureproofed() {
        return futureproofed;
    }

    /**
     * Returns the string form of the receiver's JID, when present.
     *
     * @return an {@link Optional} wrapping the receiver JID string
     */
    public Optional<String> receiverJid() {
        return Optional.ofNullable(receiverJid);
    }

    /**
     * Returns the ISO currency code, when present.
     *
     * @return an {@link Optional} wrapping the currency code
     */
    public Optional<String> currency() {
        return Optional.ofNullable(currency);
    }

    /**
     * Returns the payment amount in 1/1000 units, when present.
     *
     * @return an {@link Optional} wrapping the amount
     */
    public Optional<Long> amount1000() {
        return Optional.ofNullable(amount1000);
    }

    /**
     * Returns the transaction status, when present.
     *
     * @return an {@link Optional} wrapping the status
     */
    public Optional<String> txnStatus() {
        return Optional.ofNullable(txnStatus);
    }

    /**
     * Returns the Unix-second transaction timestamp, when present.
     *
     * @return an {@link Optional} wrapping the timestamp
     */
    public Optional<Long> transactionTimestamp() {
        return Optional.ofNullable(transactionTimestamp);
    }
}
