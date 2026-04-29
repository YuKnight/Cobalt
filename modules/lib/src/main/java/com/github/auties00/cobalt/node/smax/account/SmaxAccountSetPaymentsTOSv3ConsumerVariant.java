package com.github.auties00.cobalt.node.smax.account;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Sealed disjunction over the v3-payments-ToS payload variants —
 * either a BR-consumer (FBPAY) shape or a UPI-consumer shape. Both
 * variants carry one or more {@code <additional_notice notice=...>}
 * children whose enum values describe the surface-specific notices
 * that must accompany the ToS acceptance.
 *
 * @implNote {@code WASmaxOutAccountSetPaymentsTOSv3BRConsumerOrSetPaymentsTOSv3UPIConsumerPaymentsTOSv3MixinGroup.mergeSetPaymentsTOSv3BRConsumerOrSetPaymentsTOSv3UPIConsumerPaymentsTOSv3MixinGroup}.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutAccountSetPaymentsTOSv3BRConsumerOrSetPaymentsTOSv3UPIConsumerPaymentsTOSv3MixinGroup")
public sealed interface SmaxAccountSetPaymentsTOSv3ConsumerVariant permits SmaxAccountSetPaymentsTOSv3ConsumerVariant.BrConsumer, SmaxAccountSetPaymentsTOSv3ConsumerVariant.UpiConsumer {

    /**
     * The BR-consumer (FBPAY) variant.
     *
     * @implNote {@code WASmaxOutAccountSetPaymentsTOSv3BRConsumerPaymentsTOSv3Mixin.mergeSetPaymentsTOSv3BRConsumerPaymentsTOSv3Mixin}.
     */
    @WhatsAppWebModule(moduleName = "WASmaxOutAccountSetPaymentsTOSv3BRConsumerPaymentsTOSv3Mixin")
    final class BrConsumer implements SmaxAccountSetPaymentsTOSv3ConsumerVariant {
        /**
         * The 1..10 BR-consumer notice enum literals — one of
         * {@code "BRP2PCONSENT"}, {@code "BRPAYPRIVACYPOLICY"},
         * {@code "BRPAYTOS"}, {@code "BRPAYWATOS"}.
         */
        private final List<String> additionalNotices;

        /**
         * Constructs a new BR-consumer variant.
         *
         * @param additionalNotices the notice list (1..10 entries);
         *                          never {@code null}, never empty
         * @throws NullPointerException     if
         *                                  {@code additionalNotices}
         *                                  is {@code null}
         * @throws IllegalArgumentException if the list is empty or
         *                                  has more than 10 entries
         */
        public BrConsumer(List<String> additionalNotices) {
            Objects.requireNonNull(additionalNotices, "additionalNotices cannot be null");
            if (additionalNotices.isEmpty() || additionalNotices.size() > 10) {
                throw new IllegalArgumentException("additionalNotices must contain 1..10 entries");
            }
            this.additionalNotices = List.copyOf(additionalNotices);
        }

        /**
         * Returns the notice list.
         *
         * @return an unmodifiable list of notice enum literals;
         *         never {@code null}
         */
        public List<String> additionalNotices() {
            return additionalNotices;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (BrConsumer) obj;
            return Objects.equals(this.additionalNotices, that.additionalNotices);
        }

        @Override
        public int hashCode() {
            return Objects.hash(additionalNotices);
        }

        @Override
        public String toString() {
            return "SmaxAccountSetPaymentsTOSv3ConsumerVariant.BrConsumer[additionalNotices=" + additionalNotices + ']';
        }
    }

    /**
     * The UPI-consumer (Indian payments) variant.
     *
     * @implNote {@code WASmaxOutAccountSetPaymentsTOSv3UPIConsumerPaymentsTOSv3Mixin.mergeSetPaymentsTOSv3UPIConsumerPaymentsTOSv3Mixin}.
     */
    @WhatsAppWebModule(moduleName = "WASmaxOutAccountSetPaymentsTOSv3UPIConsumerPaymentsTOSv3Mixin")
    final class UpiConsumer implements SmaxAccountSetPaymentsTOSv3ConsumerVariant {
        /**
         * The 1..10 UPI-consumer notice enum literals — one of
         * {@code "PAYTOSV3"}, {@code "UPIPAYPRIVACYPOLICY"}.
         */
        private final List<String> additionalNotices;

        /**
         * Constructs a new UPI-consumer variant.
         *
         * @param additionalNotices the notice list (1..10 entries);
         *                          never {@code null}, never empty
         * @throws NullPointerException     if
         *                                  {@code additionalNotices}
         *                                  is {@code null}
         * @throws IllegalArgumentException if the list is empty or
         *                                  has more than 10 entries
         */
        public UpiConsumer(List<String> additionalNotices) {
            Objects.requireNonNull(additionalNotices, "additionalNotices cannot be null");
            if (additionalNotices.isEmpty() || additionalNotices.size() > 10) {
                throw new IllegalArgumentException("additionalNotices must contain 1..10 entries");
            }
            this.additionalNotices = List.copyOf(additionalNotices);
        }

        /**
         * Returns the notice list.
         *
         * @return an unmodifiable list of notice enum literals;
         *         never {@code null}
         */
        public List<String> additionalNotices() {
            return additionalNotices;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (UpiConsumer) obj;
            return Objects.equals(this.additionalNotices, that.additionalNotices);
        }

        @Override
        public int hashCode() {
            return Objects.hash(additionalNotices);
        }

        @Override
        public String toString() {
            return "SmaxAccountSetPaymentsTOSv3ConsumerVariant.UpiConsumer[additionalNotices=" + additionalNotices + ']';
        }
    }
}
