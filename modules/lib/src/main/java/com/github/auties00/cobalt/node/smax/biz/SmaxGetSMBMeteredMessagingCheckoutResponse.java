package com.github.auties00.cobalt.node.smax.biz;

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
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * Sealed family of inbound reply variants produced by the relay in
 * response to a {@link SmaxGetSMBMeteredMessagingCheckoutRequest}.
 *
 * @implNote {@code WASmaxSmbMeteredMessagingAccountGetSMBMeteredMessagingCheckoutRPC.sendGetSMBMeteredMessagingCheckoutRPC}
 *           tries {@code Success} → {@code Error} in order. Cobalt
 *           collapses the {@code Error} arm into the
 *           {@code ClientError}/{@code ServerError} pair via the
 *           shared {@link SmaxBaseServerErrorMixin} helpers.
 */
public sealed interface SmaxGetSMBMeteredMessagingCheckoutResponse extends SmaxOperation.Response
        permits SmaxGetSMBMeteredMessagingCheckoutResponse.Success, SmaxGetSMBMeteredMessagingCheckoutResponse.ClientError, SmaxGetSMBMeteredMessagingCheckoutResponse.ServerError {

    /**
     * Tries each {@link SmaxGetSMBMeteredMessagingCheckoutResponse} variant in priority order and
     * returns the first that parses cleanly.
     *
     * @param node    the inbound IQ stanza received from the relay;
     *                never {@code null}
     * @param request the original outbound stanza. Used to validate
     *                echoed identifiers; never {@code null}
     * @return an {@link Optional} carrying the parsed variant, or
     *         {@link Optional#empty()} when no documented variant
     *         matched the stanza shape
     * @throws NullPointerException if either argument is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WASmaxSmbMeteredMessagingAccountGetSMBMeteredMessagingCheckoutRPC",
            exports = "sendGetSMBMeteredMessagingCheckoutRPC",
            adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends SmaxGetSMBMeteredMessagingCheckoutResponse> of(Node node, Node request) {
        Objects.requireNonNull(node, "node cannot be null");
        Objects.requireNonNull(request, "request cannot be null");
        var success = Success.of(node, request);
        if (success.isPresent()) {
            return success;
        }
        var clientError = ClientError.of(node, request);
        if (clientError.isPresent()) {
            return clientError;
        }
        return ServerError.of(node, request);
    }

    /**
     * The {@code Success} reply variant. Carries the projected
     * cost, optional discounts, integrity-eligibility marker,
     * account-balance triple and optional quota state.
     *
     * @implNote {@code WASmaxInSmbMeteredMessagingAccountGetSMBMeteredMessagingCheckoutResponseSuccess.parseGetSMBMeteredMessagingCheckoutResponseSuccess}
     *           validates the {@code <iq from id type="result">}
     *           envelope, projects the mandatory {@code <cost/>},
     *           {@code <integrity/>} and {@code <account_balance/>}
     *           children, optional {@code <discounts/>} grandchildren
     *           and the optional {@code <quota/>} child.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInSmbMeteredMessagingAccountGetSMBMeteredMessagingCheckoutResponseSuccess")
    final class Success implements SmaxGetSMBMeteredMessagingCheckoutResponse {
        /**
         * The mandatory cost projection.
         */
        private final Cost cost;

        /**
         * The mandatory integrity-eligibility marker.
         */
        private final SmaxGetSMBMeteredMessagingCheckoutIntegrityEligibility integrityIsEligible;

        /**
         * The mandatory account-balance projection.
         */
        private final AccountBalance accountBalance;

        /**
         * The optional quota projection.
         */
        private final Quota quota;

        /**
         * Constructs a new successful reply.
         *
         * @param cost                the cost projection; never
         *                            {@code null}
         * @param integrityIsEligible the eligibility marker; never
         *                            {@code null}
         * @param accountBalance      the balance projection; never
         *                            {@code null}
         * @param quota               the optional quota projection;
         *                            may be {@code null}
         * @throws NullPointerException if any of {@code cost},
         *                              {@code integrityIsEligible}
         *                              or {@code accountBalance} is
         *                              {@code null}
         */
        public Success(Cost cost, SmaxGetSMBMeteredMessagingCheckoutIntegrityEligibility integrityIsEligible,
                       AccountBalance accountBalance, Quota quota) {
            this.cost = Objects.requireNonNull(cost, "cost cannot be null");
            this.integrityIsEligible = Objects.requireNonNull(integrityIsEligible,
                    "integrityIsEligible cannot be null");
            this.accountBalance = Objects.requireNonNull(accountBalance, "accountBalance cannot be null");
            this.quota = quota;
        }

        /**
         * Returns the cost projection.
         *
         * @return the cost; never {@code null}
         */
        public Cost cost() {
            return cost;
        }

        /**
         * Returns the integrity-eligibility marker.
         *
         * @return the marker; never {@code null}
         */
        public SmaxGetSMBMeteredMessagingCheckoutIntegrityEligibility integrityIsEligible() {
            return integrityIsEligible;
        }

        /**
         * Returns the account-balance projection.
         *
         * @return the balance; never {@code null}
         */
        public AccountBalance accountBalance() {
            return accountBalance;
        }

        /**
         * Returns the optional quota projection.
         *
         * @return an {@link Optional} carrying the projection, or
         *         empty when the relay omitted the
         *         {@code <quota/>} child
         */
        public Optional<Quota> quota() {
            return Optional.ofNullable(quota);
        }

        /**
         * Tries to parse a {@link Success} variant from the given
         * inbound stanza.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant,
         *         or empty when the stanza does not match the
         *         success schema
         */
        @WhatsAppWebExport(moduleName = "WASmaxInSmbMeteredMessagingAccountGetSMBMeteredMessagingCheckoutResponseSuccess",
                exports = "parseGetSMBMeteredMessagingCheckoutResponseSuccess",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Success> of(Node node, Node request) {
            Objects.requireNonNull(node, "node cannot be null");
            Objects.requireNonNull(request, "request cannot be null");
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            var costNode = node.getChild("cost").orElse(null);
            if (costNode == null) {
                return Optional.empty();
            }
            var cost = Cost.of(costNode).orElse(null);
            if (cost == null) {
                return Optional.empty();
            }
            var integrityNode = node.getChild("integrity").orElse(null);
            if (integrityNode == null) {
                return Optional.empty();
            }
            var integrityStr = integrityNode.getAttributeAsString("is_eligible").orElse(null);
            var integrity = SmaxGetSMBMeteredMessagingCheckoutIntegrityEligibility.of(integrityStr).orElse(null);
            if (integrity == null) {
                return Optional.empty();
            }
            var balanceNode = node.getChild("account_balance").orElse(null);
            if (balanceNode == null) {
                return Optional.empty();
            }
            var balance = AccountBalance.of(balanceNode).orElse(null);
            if (balance == null) {
                return Optional.empty();
            }
            Quota quota = null;
            var quotaNode = node.getChild("quota").orElse(null);
            if (quotaNode != null) {
                var parsed = Quota.of(quotaNode);
                if (parsed.isEmpty()) {
                    return Optional.empty();
                }
                quota = parsed.get();
            }
            return Optional.of(new Success(cost, integrity, balance, quota));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (Success) obj;
            return Objects.equals(this.cost, that.cost)
                    && this.integrityIsEligible == that.integrityIsEligible
                    && Objects.equals(this.accountBalance, that.accountBalance)
                    && Objects.equals(this.quota, that.quota);
        }

        @Override
        public int hashCode() {
            return Objects.hash(cost, integrityIsEligible, accountBalance, quota);
        }

        @Override
        public String toString() {
            return "SmaxGetSMBMeteredMessagingCheckoutResponse.Success[cost=" + cost
                    + ", integrityIsEligible=" + integrityIsEligible
                    + ", accountBalance=" + accountBalance
                    + ", quota=" + quota + ']';
        }

        /**
         * The {@code <cost/>} child projection. Full cost
         * breakdown.
         */
        @WhatsAppWebModule(moduleName = "WASmaxInSmbMeteredMessagingAccountGetSMBMeteredMessagingCheckoutResponseSuccess")
        public static final class Cost {
            /**
             * The pre-tax cost (currency-minor units).
             */
            private final int beforeTax;

            /**
             * The tax amount (currency-minor units).
             */
            private final int tax;

            /**
             * The currency offset (decimal places).
             */
            private final int offset;

            /**
             * The currency identifier.
             */
            private final String currency;

            /**
             * The optional pre-discount base (currency-minor units).
             */
            private final Integer base;

            /**
             * The optional pre-discount base formatted.
             */
            private final String baseFormatted;

            /**
             * The optional discount-percent.
             */
            private final Integer discountPercent;

            /**
             * The optional pre-discount cost (currency-minor units).
             */
            private final Integer beforeDiscount;

            /**
             * The optional pre-discount cost formatted.
             */
            private final String beforeDiscountFormatted;

            /**
             * The optional list of applied discounts (0..10
             * entries).
             */
            private final List<Discount> discounts;

            /**
             * Constructs a new cost projection.
             *
             * @param beforeTax               the pre-tax cost
             * @param tax                     the tax amount
             * @param offset                  the currency offset
             * @param currency                the currency
             *                                identifier; never
             *                                {@code null}
             * @param base                    the optional base; may
             *                                be {@code null}
             * @param baseFormatted           the optional formatted
             *                                base; may be
             *                                {@code null}
             * @param discountPercent         the optional discount
             *                                percent; may be
             *                                {@code null}
             * @param beforeDiscount          the optional
             *                                pre-discount cost; may
             *                                be {@code null}
             * @param beforeDiscountFormatted the optional formatted
             *                                pre-discount cost; may
             *                                be {@code null}
             * @param discounts               the optional list of
             *                                applied discounts; may
             *                                be {@code null}
             *                                (treated as empty)
             * @throws NullPointerException if {@code currency} is
             *                              {@code null}
             */
            public Cost(int beforeTax, int tax, int offset, String currency,
                        Integer base, String baseFormatted,
                        Integer discountPercent, Integer beforeDiscount,
                        String beforeDiscountFormatted,
                        List<Discount> discounts) {
                this.beforeTax = beforeTax;
                this.tax = tax;
                this.offset = offset;
                this.currency = Objects.requireNonNull(currency, "currency cannot be null");
                this.base = base;
                this.baseFormatted = baseFormatted;
                this.discountPercent = discountPercent;
                this.beforeDiscount = beforeDiscount;
                this.beforeDiscountFormatted = beforeDiscountFormatted;
                this.discounts = discounts == null ? List.of() : List.copyOf(discounts);
            }

            /**
             * Returns the pre-tax cost.
             *
             * @return the cost in currency-minor units
             */
            public int beforeTax() {
                return beforeTax;
            }

            /**
             * Returns the tax amount.
             *
             * @return the tax in currency-minor units
             */
            public int tax() {
                return tax;
            }

            /**
             * Returns the currency offset.
             *
             * @return the offset in decimal places
             */
            public int offset() {
                return offset;
            }

            /**
             * Returns the currency identifier.
             *
             * @return the currency code; never {@code null}
             */
            public String currency() {
                return currency;
            }

            /**
             * Returns the optional base cost.
             *
             * @return an {@link OptionalInt} carrying the value, or
             *         empty
             */
            public OptionalInt base() {
                return base == null ? OptionalInt.empty() : OptionalInt.of(base);
            }

            /**
             * Returns the optional formatted base cost.
             *
             * @return an {@link Optional} carrying the value, or
             *         empty
             */
            public Optional<String> baseFormatted() {
                return Optional.ofNullable(baseFormatted);
            }

            /**
             * Returns the optional discount percent.
             *
             * @return an {@link OptionalInt} carrying the value, or
             *         empty
             */
            public OptionalInt discountPercent() {
                return discountPercent == null ? OptionalInt.empty() : OptionalInt.of(discountPercent);
            }

            /**
             * Returns the optional pre-discount cost.
             *
             * @return an {@link OptionalInt} carrying the value, or
             *         empty
             */
            public OptionalInt beforeDiscount() {
                return beforeDiscount == null ? OptionalInt.empty() : OptionalInt.of(beforeDiscount);
            }

            /**
             * Returns the optional formatted pre-discount cost.
             *
             * @return an {@link Optional} carrying the value, or
             *         empty
             */
            public Optional<String> beforeDiscountFormatted() {
                return Optional.ofNullable(beforeDiscountFormatted);
            }

            /**
             * Returns the optional list of applied discounts.
             *
             * @return an unmodifiable list of 0..10 entries; never
             *         {@code null}
             */
            public List<Discount> discounts() {
                return discounts;
            }

            /**
             * Tries to parse the projection from the given node.
             *
             * @param node the {@code <cost/>} node
             * @return an {@link Optional} carrying the projection,
             *         or empty when the node does not match the
             *         documented schema
             */
            public static Optional<Cost> of(Node node) {
                Objects.requireNonNull(node, "node cannot be null");
                if (!node.hasDescription("cost")) {
                    return Optional.empty();
                }
                var beforeTax = node.getAttributeAsInt("before_tax");
                if (beforeTax.isEmpty()) {
                    return Optional.empty();
                }
                var tax = node.getAttributeAsInt("tax");
                if (tax.isEmpty()) {
                    return Optional.empty();
                }
                var offset = node.getAttributeAsInt("offset");
                if (offset.isEmpty()) {
                    return Optional.empty();
                }
                var currency = node.getAttributeAsString("currency").orElse(null);
                if (currency == null) {
                    return Optional.empty();
                }
                Integer base = null;
                var baseOpt = node.getAttributeAsInt("base");
                if (baseOpt.isPresent()) {
                    base = baseOpt.getAsInt();
                }
                var baseFormatted = node.getAttributeAsString("base_formatted").orElse(null);
                Integer discountPercent = null;
                var discountPercentOpt = node.getAttributeAsInt("discount_percent");
                if (discountPercentOpt.isPresent()) {
                    discountPercent = discountPercentOpt.getAsInt();
                }
                Integer beforeDiscount = null;
                var beforeDiscountOpt = node.getAttributeAsInt("before_discount");
                if (beforeDiscountOpt.isPresent()) {
                    beforeDiscount = beforeDiscountOpt.getAsInt();
                }
                var beforeDiscountFormatted = node.getAttributeAsString("before_discount_formatted").orElse(null);
                var discounts = new ArrayList<Discount>();
                var discountsNode = node.getChild("discounts").orElse(null);
                if (discountsNode != null) {
                    var iter = discountsNode.streamChildren("discount").iterator();
                    while (iter.hasNext()) {
                        var parsed = Discount.of(iter.next());
                        if (parsed.isEmpty()) {
                            return Optional.empty();
                        }
                        discounts.add(parsed.get());
                    }
                    if (discounts.size() > 10) {
                        return Optional.empty();
                    }
                }
                return Optional.of(new Cost(beforeTax.getAsInt(), tax.getAsInt(), offset.getAsInt(),
                        currency, base, baseFormatted, discountPercent, beforeDiscount,
                        beforeDiscountFormatted, discounts));
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) {
                    return true;
                }
                if (obj == null || obj.getClass() != this.getClass()) {
                    return false;
                }
                var that = (Cost) obj;
                return this.beforeTax == that.beforeTax
                        && this.tax == that.tax
                        && this.offset == that.offset
                        && Objects.equals(this.currency, that.currency)
                        && Objects.equals(this.base, that.base)
                        && Objects.equals(this.baseFormatted, that.baseFormatted)
                        && Objects.equals(this.discountPercent, that.discountPercent)
                        && Objects.equals(this.beforeDiscount, that.beforeDiscount)
                        && Objects.equals(this.beforeDiscountFormatted, that.beforeDiscountFormatted)
                        && Objects.equals(this.discounts, that.discounts);
            }

            @Override
            public int hashCode() {
                return Objects.hash(beforeTax, tax, offset, currency, base, baseFormatted,
                        discountPercent, beforeDiscount, beforeDiscountFormatted, discounts);
            }

            @Override
            public String toString() {
                return "SmaxGetSMBMeteredMessagingCheckoutResponse.Success.Cost[beforeTax=" + beforeTax
                        + ", tax=" + tax
                        + ", offset=" + offset
                        + ", currency=" + currency
                        + ", base=" + base
                        + ", baseFormatted=" + baseFormatted
                        + ", discountPercent=" + discountPercent
                        + ", beforeDiscount=" + beforeDiscount
                        + ", beforeDiscountFormatted=" + beforeDiscountFormatted
                        + ", discounts=" + discounts + ']';
            }

            /**
             * Single applied discount entry.
             */
            @WhatsAppWebModule(moduleName = "WASmaxInSmbMeteredMessagingAccountGetSMBMeteredMessagingCheckoutResponseSuccess")
            public static final class Discount {
                /**
                 * The discount type.
                 */
                private final SmaxGetSMBMeteredMessagingCheckoutDiscountType type;

                /**
                 * The optional percentage value (only meaningful for
                 * {@link SmaxGetSMBMeteredMessagingCheckoutDiscountType#PERCENTAGE}).
                 */
                private final Integer percentage;

                /**
                 * The discount amount (currency-minor units).
                 */
                private final int amount;

                /**
                 * The formatted discount amount.
                 */
                private final String amountFormatted;

                /**
                 * Constructs a new discount entry.
                 *
                 * @param type            the discount type; never
                 *                        {@code null}
                 * @param percentage      the optional percentage
                 *                        value; may be {@code null}
                 * @param amount          the discount amount
                 * @param amountFormatted the formatted amount;
                 *                        never {@code null}
                 * @throws NullPointerException if {@code type} or
                 *                              {@code amountFormatted}
                 *                              is {@code null}
                 */
                public Discount(SmaxGetSMBMeteredMessagingCheckoutDiscountType type, Integer percentage,
                                int amount, String amountFormatted) {
                    this.type = Objects.requireNonNull(type, "type cannot be null");
                    this.percentage = percentage;
                    this.amount = amount;
                    this.amountFormatted = Objects.requireNonNull(amountFormatted,
                            "amountFormatted cannot be null");
                }

                /**
                 * Returns the discount type.
                 *
                 * @return the type; never {@code null}
                 */
                public SmaxGetSMBMeteredMessagingCheckoutDiscountType type() {
                    return type;
                }

                /**
                 * Returns the optional percentage.
                 *
                 * @return an {@link OptionalInt} carrying the
                 *         value, or empty
                 */
                public OptionalInt percentage() {
                    return percentage == null ? OptionalInt.empty() : OptionalInt.of(percentage);
                }

                /**
                 * Returns the discount amount.
                 *
                 * @return the amount in currency-minor units
                 */
                public int amount() {
                    return amount;
                }

                /**
                 * Returns the formatted amount.
                 *
                 * @return the formatted string; never {@code null}
                 */
                public String amountFormatted() {
                    return amountFormatted;
                }

                /**
                 * Tries to parse the entry from the given node.
                 *
                 * @param node the {@code <discount/>} node
                 * @return an {@link Optional} carrying the parsed
                 *         entry, or empty when the node does not
                 *         match the documented schema
                 */
                public static Optional<Discount> of(Node node) {
                    Objects.requireNonNull(node, "node cannot be null");
                    if (!node.hasDescription("discount")) {
                        return Optional.empty();
                    }
                    var typeStr = node.getAttributeAsString("type").orElse(null);
                    var type = SmaxGetSMBMeteredMessagingCheckoutDiscountType.of(typeStr).orElse(null);
                    if (type == null) {
                        return Optional.empty();
                    }
                    Integer percentage = null;
                    var percentageOpt = node.getAttributeAsInt("percentage");
                    if (percentageOpt.isPresent()) {
                        percentage = percentageOpt.getAsInt();
                    }
                    var amountOpt = node.getAttributeAsInt("amount");
                    if (amountOpt.isEmpty()) {
                        return Optional.empty();
                    }
                    var amountFormatted = node.getAttributeAsString("amount_formatted").orElse(null);
                    if (amountFormatted == null) {
                        return Optional.empty();
                    }
                    return Optional.of(new Discount(type, percentage, amountOpt.getAsInt(), amountFormatted));
                }

                @Override
                public boolean equals(Object obj) {
                    if (obj == this) {
                        return true;
                    }
                    if (obj == null || obj.getClass() != this.getClass()) {
                        return false;
                    }
                    var that = (Discount) obj;
                    return this.amount == that.amount
                            && this.type == that.type
                            && Objects.equals(this.percentage, that.percentage)
                            && Objects.equals(this.amountFormatted, that.amountFormatted);
                }

                @Override
                public int hashCode() {
                    return Objects.hash(type, percentage, amount, amountFormatted);
                }

                @Override
                public String toString() {
                    return "SmaxGetSMBMeteredMessagingCheckoutResponse.Success.Cost.Discount[type=" + type
                            + ", percentage=" + percentage
                            + ", amount=" + amount
                            + ", amountFormatted=" + amountFormatted + ']';
                }
            }
        }

        /**
         * The {@code <account_balance/>} child projection. The
         * billing / available / offset triple for the calling
         * business's metered-messaging wallet.
         */
        @WhatsAppWebModule(moduleName = "WASmaxInSmbMeteredMessagingAccountGetSMBMeteredMessagingCheckoutResponseSuccess")
        public static final class AccountBalance {
            /**
             * The total billed-to-date balance.
             */
            private final int billing;

            /**
             * The currently-available balance.
             */
            private final int available;

            /**
             * The currency offset (decimal places).
             */
            private final int offset;

            /**
             * Constructs a new balance projection.
             *
             * @param billing   the billed balance
             * @param available the available balance
             * @param offset    the currency offset
             */
            public AccountBalance(int billing, int available, int offset) {
                this.billing = billing;
                this.available = available;
                this.offset = offset;
            }

            /**
             * Returns the billed-to-date balance.
             *
             * @return the balance in currency-minor units
             */
            public int billing() {
                return billing;
            }

            /**
             * Returns the available balance.
             *
             * @return the balance in currency-minor units
             */
            public int available() {
                return available;
            }

            /**
             * Returns the currency offset.
             *
             * @return the offset in decimal places
             */
            public int offset() {
                return offset;
            }

            /**
             * Tries to parse the projection from the given node.
             *
             * @param node the {@code <account_balance/>} node
             * @return an {@link Optional} carrying the projection,
             *         or empty when the node does not match the
             *         documented schema
             */
            public static Optional<AccountBalance> of(Node node) {
                Objects.requireNonNull(node, "node cannot be null");
                if (!node.hasDescription("account_balance")) {
                    return Optional.empty();
                }
                var billing = node.getAttributeAsInt("billing");
                if (billing.isEmpty()) {
                    return Optional.empty();
                }
                var available = node.getAttributeAsInt("available");
                if (available.isEmpty()) {
                    return Optional.empty();
                }
                var offset = node.getAttributeAsInt("offset");
                if (offset.isEmpty()) {
                    return Optional.empty();
                }
                return Optional.of(new AccountBalance(billing.getAsInt(), available.getAsInt(), offset.getAsInt()));
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) {
                    return true;
                }
                if (obj == null || obj.getClass() != this.getClass()) {
                    return false;
                }
                var that = (AccountBalance) obj;
                return this.billing == that.billing
                        && this.available == that.available
                        && this.offset == that.offset;
            }

            @Override
            public int hashCode() {
                return Objects.hash(billing, available, offset);
            }

            @Override
            public String toString() {
                return "SmaxGetSMBMeteredMessagingCheckoutResponse.Success.AccountBalance[billing=" + billing
                        + ", available=" + available
                        + ", offset=" + offset + ']';
            }
        }

        /**
         * The {@code <quota/>} child projection. The calling
         * business's monthly free-message quota state.
         */
        @WhatsAppWebModule(moduleName = "WASmaxInSmbMeteredMessagingAccountGetSMBMeteredMessagingCheckoutResponseSuccess")
        public static final class Quota {
            /**
             * The remaining message quota.
             */
            private final int remaining;

            /**
             * The total monthly quota.
             */
            private final int totalMonthly;

            /**
             * The optional one-shot single-credits balance.
             */
            private final Integer singleCredits;

            /**
             * The optional total-available-credits projection.
             */
            private final Integer totalAvailableCredits;

            /**
             * Constructs a new quota projection.
             *
             * @param remaining             the remaining quota
             * @param totalMonthly          the total monthly quota
             * @param singleCredits         the optional
             *                              single-credits balance;
             *                              may be {@code null}
             * @param totalAvailableCredits the optional total
             *                              available credits; may
             *                              be {@code null}
             */
            public Quota(int remaining, int totalMonthly,
                         Integer singleCredits, Integer totalAvailableCredits) {
                this.remaining = remaining;
                this.totalMonthly = totalMonthly;
                this.singleCredits = singleCredits;
                this.totalAvailableCredits = totalAvailableCredits;
            }

            /**
             * Returns the remaining quota.
             *
             * @return the remaining count
             */
            public int remaining() {
                return remaining;
            }

            /**
             * Returns the total monthly quota.
             *
             * @return the total count
             */
            public int totalMonthly() {
                return totalMonthly;
            }

            /**
             * Returns the optional single-credits balance.
             *
             * @return an {@link OptionalInt} carrying the value,
             *         or empty
             */
            public OptionalInt singleCredits() {
                return singleCredits == null ? OptionalInt.empty() : OptionalInt.of(singleCredits);
            }

            /**
             * Returns the optional total-available-credits.
             *
             * @return an {@link OptionalInt} carrying the value,
             *         or empty
             */
            public OptionalInt totalAvailableCredits() {
                return totalAvailableCredits == null
                        ? OptionalInt.empty()
                        : OptionalInt.of(totalAvailableCredits);
            }

            /**
             * Tries to parse the projection from the given node.
             *
             * @param node the {@code <quota/>} node
             * @return an {@link Optional} carrying the projection,
             *         or empty when the node does not match the
             *         documented schema
             */
            public static Optional<Quota> of(Node node) {
                Objects.requireNonNull(node, "node cannot be null");
                if (!node.hasDescription("quota")) {
                    return Optional.empty();
                }
                var remaining = node.getAttributeAsInt("remaining");
                if (remaining.isEmpty()) {
                    return Optional.empty();
                }
                var totalMonthly = node.getAttributeAsInt("total_monthly");
                if (totalMonthly.isEmpty()) {
                    return Optional.empty();
                }
                Integer singleCredits = null;
                var singleCreditsOpt = node.getAttributeAsInt("single_credits");
                if (singleCreditsOpt.isPresent()) {
                    singleCredits = singleCreditsOpt.getAsInt();
                }
                Integer totalAvailableCredits = null;
                var totalAvailableCreditsOpt = node.getAttributeAsInt("total_available_credits");
                if (totalAvailableCreditsOpt.isPresent()) {
                    totalAvailableCredits = totalAvailableCreditsOpt.getAsInt();
                }
                return Optional.of(new Quota(remaining.getAsInt(), totalMonthly.getAsInt(),
                        singleCredits, totalAvailableCredits));
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) {
                    return true;
                }
                if (obj == null || obj.getClass() != this.getClass()) {
                    return false;
                }
                var that = (Quota) obj;
                return this.remaining == that.remaining
                        && this.totalMonthly == that.totalMonthly
                        && Objects.equals(this.singleCredits, that.singleCredits)
                        && Objects.equals(this.totalAvailableCredits, that.totalAvailableCredits);
            }

            @Override
            public int hashCode() {
                return Objects.hash(remaining, totalMonthly, singleCredits, totalAvailableCredits);
            }

            @Override
            public String toString() {
                return "SmaxGetSMBMeteredMessagingCheckoutResponse.Success.Quota[remaining=" + remaining
                        + ", totalMonthly=" + totalMonthly
                        + ", singleCredits=" + singleCredits
                        + ", totalAvailableCredits=" + totalAvailableCredits + ']';
            }
        }
    }

    /**
     * The {@code ClientError} reply variant. The relay rejected the
     * request with a documented {@code 4xx} error code drawn from
     * the SMB metered-messaging error catalogue.
     *
     * @implNote {@code WASmaxInSmbMeteredMessagingAccountGetSMBMeteredMessagingCheckoutResponseError.parseGetSMBMeteredMessagingCheckoutResponseError}
     *           routes the {@code <error/>} child through
     *           {@code WASmaxInSmbMeteredMessagingAccountGetSmbMeteredMessagingCheckoutIqErrors};
     *           Cobalt collapses to the raw {@code (code, text)}
     *           pair via the shared {@link SmaxBaseServerErrorMixin}.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInSmbMeteredMessagingAccountGetSMBMeteredMessagingCheckoutResponseError")
    @WhatsAppWebModule(moduleName = "WASmaxInSmbMeteredMessagingAccountGetSmbMeteredMessagingCheckoutIqErrors")
    final class ClientError implements SmaxGetSMBMeteredMessagingCheckoutResponse {
        /**
         * The numeric server-side error code.
         */
        private final int errorCode;

        /**
         * The human-readable error text, when the relay supplied
         * one.
         */
        private final String errorText;

        /**
         * Constructs a new client-error reply.
         *
         * @param errorCode the numeric error code
         * @param errorText the optional human-readable text; may be
         *                  {@code null}
         */
        public ClientError(int errorCode, String errorText) {
            this.errorCode = errorCode;
            this.errorText = errorText;
        }

        /**
         * Returns the numeric error code.
         *
         * @return the error code
         */
        public int errorCode() {
            return errorCode;
        }

        /**
         * Returns the optional human-readable error text.
         *
         * @return an {@link Optional} carrying the error text, or
         *         empty when the relay omitted it
         */
        public Optional<String> errorText() {
            return Optional.ofNullable(errorText);
        }

        /**
         * Tries to parse a {@link ClientError} variant from the
         * given inbound stanza.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant,
         *         or empty when the stanza does not match the
         *         client-error schema
         */
        @WhatsAppWebExport(moduleName = "WASmaxInSmbMeteredMessagingAccountGetSMBMeteredMessagingCheckoutResponseError",
                exports = "parseGetSMBMeteredMessagingCheckoutResponseError",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<ClientError> of(Node node, Node request) {
            var envelope = SmaxBaseServerErrorMixin.parseClientError(node, request).orElse(null);
            if (envelope == null) {
                return Optional.empty();
            }
            return Optional.of(new ClientError(envelope.code(), envelope.text()));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (ClientError) obj;
            return this.errorCode == that.errorCode && Objects.equals(this.errorText, that.errorText);
        }

        @Override
        public int hashCode() {
            return Objects.hash(errorCode, errorText);
        }

        @Override
        public String toString() {
            return "SmaxGetSMBMeteredMessagingCheckoutResponse.ClientError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }

    /**
     * The {@code ServerError} reply variant. The relay encountered
     * a transient internal failure ({@code 5xx}).
     *
     * @implNote Sourced from the {@code 5xx} arms of
     *           {@code WASmaxInSmbMeteredMessagingAccountGetSmbMeteredMessagingCheckoutIqErrors};
     *           Cobalt routes through the shared
     *           {@link SmaxBaseServerErrorMixin}.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInSmbMeteredMessagingAccountGetSMBMeteredMessagingCheckoutResponseError")
    final class ServerError implements SmaxGetSMBMeteredMessagingCheckoutResponse {
        /**
         * The numeric server-side error code.
         */
        private final int errorCode;

        /**
         * The human-readable error text, when the relay supplied
         * one.
         */
        private final String errorText;

        /**
         * Constructs a new server-error reply.
         *
         * @param errorCode the numeric error code
         * @param errorText the optional human-readable text; may be
         *                  {@code null}
         */
        public ServerError(int errorCode, String errorText) {
            this.errorCode = errorCode;
            this.errorText = errorText;
        }

        /**
         * Returns the numeric error code.
         *
         * @return the error code
         */
        public int errorCode() {
            return errorCode;
        }

        /**
         * Returns the optional human-readable error text.
         *
         * @return an {@link Optional} carrying the error text, or
         *         empty when the relay omitted it
         */
        public Optional<String> errorText() {
            return Optional.ofNullable(errorText);
        }

        /**
         * Tries to parse a {@link ServerError} variant from the
         * given inbound stanza.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant,
         *         or empty when the stanza does not match the
         *         server-error schema
         */
        public static Optional<ServerError> of(Node node, Node request) {
            var envelope = SmaxBaseServerErrorMixin.parseServerError(node, request).orElse(null);
            if (envelope == null) {
                return Optional.empty();
            }
            return Optional.of(new ServerError(envelope.code(), envelope.text()));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (ServerError) obj;
            return this.errorCode == that.errorCode && Objects.equals(this.errorText, that.errorText);
        }

        @Override
        public int hashCode() {
            return Objects.hash(errorCode, errorText);
        }

        @Override
        public String toString() {
            return "SmaxGetSMBMeteredMessagingCheckoutResponse.ServerError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }
}
