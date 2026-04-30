package com.github.auties00.cobalt.stream.message;

/**
 * Enumerates all fine-grained payment message status values used internally by the
 * payment notification and transaction handling pipeline.
 *
 * <p>Each constant corresponds to a specific combination of transaction type and
 * server-reported status string, as defined in the WhatsApp Web payment status
 * utilities. The mapping from raw server status strings to these constants is
 * performed by the {@code paymentMessageStatus} helper methods in the notification
 * and message stream handlers.
 */
public enum PaymentMessageStatus {
    /**
     * No status has been set or the status could not be determined from
     * the server-reported values.
     */
    STATUS_UNSET,

    /**
     * A payment request has been initialized.
     */
    REQUEST_PAY_INIT,

    /**
     * A payment request has been successfully collected.
     */
    REQUEST_PAY_SUCCESS,

    /**
     * A payment request has failed.
     */
    REQUEST_PAY_FAILED,

    /**
     * A payment request has failed due to risk assessment.
     */
    REQUEST_PAY_FAILED_RISK,

    /**
     * A payment request has been rejected by the payer.
     */
    REQUEST_PAY_REJECTED,

    /**
     * A payment request has expired before being fulfilled.
     */
    REQUEST_PAY_EXPIRED,

    /**
     * A payment request has been fulfilled by the payer.
     */
    REQUEST_PAY_FULFILLED,

    /**
     * A payment request has been cancelled by the requester.
     */
    REQUEST_PAY_CANCELLED,

    /**
     * A payment request is in the process of being cancelled.
     */
    REQUEST_PAY_CANCELLING,

    /**
     * A scheduled payment request has been successfully collected.
     */
    REQUEST_PAY_SCHEDULED_PAYMENT_SUCCESS,

    /**
     * A received payment has been initialized.
     */
    RECV_PAY_INIT,

    /**
     * A received payment is pending setup by the receiver.
     */
    RECV_PAY_PENDING_SETUP,

    /**
     * A received payment is pending due to a direct account settlement issue.
     */
    RECV_PAY_PENDING,

    /**
     * A received payment failed but a retry is being attempted.
     */
    RECV_PAY_RETRY_ON_FAILURE,

    /**
     * A received payment has failed.
     */
    RECV_PAY_FAILURE,

    /**
     * A received payment has been successfully processed.
     */
    RECV_PAY_SUCCESS,

    /**
     * A received payment has expired.
     */
    RECV_PAY_EXPIRED,

    /**
     * A received payment has failed due to risk assessment.
     */
    RECV_PAY_FAILURE_RISK,

    /**
     * A received payment withdrawal is being processed.
     */
    RECV_PAY_WITHDRAWAL_PROCESSING,

    /**
     * A received payment withdrawal has failed.
     */
    RECV_PAY_WITHDRAWAL_FAILURE,

    /**
     * A received payment withdrawal has permanently failed.
     */
    RECV_PAY_WITHDRAWAL_PERMANENT_FAILED,

    /**
     * A received payment has been cancelled by the sender.
     */
    RECV_PAY_SENDER_CANCELED,

    /**
     * A sent payment has been initialized.
     */
    SEND_PAY_INIT,

    /**
     * A sent payment is pending receiver setup.
     */
    SEND_PAY_PENDING_RECEIVER,

    /**
     * A sent payment is pending due to a direct account settlement issue.
     */
    SEND_PAY_PENDING,

    /**
     * A sent payment refund is pending due to a direct account settlement issue.
     */
    SEND_PAY_REFUND_PENDING,

    /**
     * A sent payment has been successfully processed.
     */
    SEND_PAY_SUCCESS,

    /**
     * A sent payment has failed.
     */
    SEND_PAY_FAILURE,

    /**
     * A sent payment has failed due to risk assessment.
     */
    SEND_PAY_FAILURE_RISK,

    /**
     * A sent payment has been refunded.
     */
    SEND_PAY_REFUNDED,

    /**
     * A refund for a sent payment has failed.
     */
    SEND_PAY_REFUND_FAILED,

    /**
     * A sent payment has failed during receiver-side processing.
     */
    SEND_PAY_FAILURE_RECEIVER,

    /**
     * A refund for a sent payment has failed during processing.
     */
    SEND_PAY_REFUND_FAILED_PROCESSING,

    /**
     * A sent payment is pending refund after a final direct account settlement failure.
     */
    SEND_PAY_PENDING_REFUND,

    /**
     * A sent payment authorization cancellation has failed during processing.
     */
    SEND_PAY_AUTH_CANCEL_FAILED_PROCESSING,

    /**
     * A sent payment authorization cancellation has failed.
     */
    SEND_PAY_AUTH_CANCEL_FAILED,

    /**
     * A sent payment authorization has been cancelled.
     */
    SEND_PAY_AUTH_CANCELED,

    /**
     * A sent payment has expired.
     */
    SEND_PAY_EXPIRED,

    /**
     * A sent payment authorization has succeeded.
     */
    SEND_PAY_AUTH_SUCCESS,

    /**
     * A sent payment authorization success is being cancelled.
     */
    SEND_PAY_AUTH_SUCCESS_CANCELING,

    /**
     * A sent payment is under review by the payment provider.
     */
    SEND_PAY_IN_REVIEW,

    /**
     * A sent payment is pending processing.
     */
    SEND_PAY_PENDING_PROCESSING,

    /**
     * A sent payment has been cancelled by the user.
     */
    SEND_PAY_USER_CANCELED,

    /**
     * A withdrawal has been initialized.
     */
    WITHDRAWAL_INIT,

    /**
     * A withdrawal is pending processing.
     */
    WITHDRAWAL_PENDING,

    /**
     * A withdrawal is under review.
     */
    WITHDRAWAL_IN_REVIEW,

    /**
     * A withdrawal has been successfully processed.
     */
    WITHDRAWAL_SUCCESS,

    /**
     * A withdrawal has failed.
     */
    WITHDRAWAL_FAILED,

    /**
     * A withdrawal has been cancelled by the user.
     */
    WITHDRAWAL_USER_CANCELED,

    /**
     * A withdrawal has expired.
     */
    WITHDRAWAL_EXPIRED,

    /**
     * A withdrawal is currently active.
     */
    WITHDRAWAL_ACTIVE
}
