package com.github.auties00.cobalt.message.preview.model;

import com.github.auties00.cobalt.model.message.payment.PaymentLinkMetadata;
import com.github.auties00.cobalt.model.message.payment.PaymentLinkMetadata.PaymentLinkHeader.PaymentLinkHeaderType;
import com.github.auties00.cobalt.model.message.payment.PaymentLinkMetadata.PaymentLinkProvider;

/**
 * Payment-link descriptors threaded through the pipeline for the
 * payment-link preview type. Materialised onto the wire as a
 * {@link PaymentLinkMetadata} with a
 * {@link PaymentLinkHeaderType#LINK_PREVIEW} header and a
 * {@link PaymentLinkProvider} carrying the resolved PSP.
 *
 * @param psp the resolved payment service provider label
 */
public record PaymentLinkDetails(String psp) {
}
