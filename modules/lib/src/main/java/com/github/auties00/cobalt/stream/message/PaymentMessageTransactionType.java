package com.github.auties00.cobalt.stream.message;

/**
 * Enumerates the transaction types used by the payment notification and transaction
 * handling pipeline to classify a payment's direction and mechanism.
 *
 * <p>Each constant corresponds to a specific server-reported transaction type string
 * combined with the {@code fromMe} flag, as defined in the WhatsApp Web payment
 * status utilities. The mapping from raw server type strings to these constants is
 * performed by the {@code paymentMessageTransactionType} helper methods.
 */
public enum PaymentMessageTransactionType {
    /**
     * No transaction type has been set or the type is unknown.
     */
    TYPE_UNSET,

    /**
     * A peer-to-peer payment sent by the current user.
     */
    TYPE_P2P_SENT,

    /**
     * A peer-to-peer payment received by the current user.
     */
    TYPE_P2P_RCVD,

    /**
     * A peer-to-peer payment within a group conversation.
     */
    TYPE_P2P_GRP,

    /**
     * A peer-to-peer payment with no direction information available.
     */
    TYPE_P2P_NO_INFO,

    /**
     * A future-dated peer-to-peer payment.
     */
    TYPE_FUTURE,

    /**
     * A peer-to-peer payment request sent by the current user.
     */
    TYPE_P2P_REQ_SENT,

    /**
     * A peer-to-peer payment request received by the current user.
     */
    TYPE_P2P_REQ_RCVD,

    /**
     * A scheduled payment for a received peer-to-peer request.
     */
    TYPE_P2P_REQ_SCHEDULED_PAYMENT_RCVD,

    /**
     * A peer-to-peer payment request within a group conversation.
     */
    TYPE_P2P_REQ_GRP,

    /**
     * A person-to-merchant payment sent by the current user.
     */
    TYPE_P2M_SENT,

    /**
     * A person-to-merchant payment received by the current user (merchant).
     */
    TYPE_P2M_RCVD,

    /**
     * A merchant payout transaction.
     */
    TYPE_P2M_PAYOUT,

    /**
     * A transaction whose details are missing or incomplete.
     */
    TYPE_MISSING_DETAILS,

    /**
     * A deposit transaction.
     */
    TYPE_DEPOSIT,

    /**
     * A refund transaction.
     */
    TYPE_REFUND,

    /**
     * A withdrawal transaction.
     */
    TYPE_WITHDRAWAL
}
