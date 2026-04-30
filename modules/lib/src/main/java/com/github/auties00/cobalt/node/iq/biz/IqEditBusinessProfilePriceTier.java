package com.github.auties00.cobalt.node.iq.biz;

import java.util.Objects;

/**
 * Typed {@code (id, symbol, description)} price-tier triple carried by
 * the price-tier list responses fetched alongside business profile
 * editing.
 */
public final class IqEditBusinessProfilePriceTier {
    /**
     * The price-tier id.
     */
    private final String id;

    /**
     * The currency symbol.
     */
    private final String symbol;

    /**
     * The free-text description.
     */
    private final String description;

    /**
     * Constructs a tier.
     *
     * @param id          the id; never {@code null}
     * @param symbol      the symbol; never {@code null}
     * @param description the description; never {@code null}
     * @throws NullPointerException if any argument is {@code null}
     */
    public IqEditBusinessProfilePriceTier(String id, String symbol, String description) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.symbol = Objects.requireNonNull(symbol, "symbol cannot be null");
        this.description = Objects.requireNonNull(description, "description cannot be null");
    }

    /**
     * Returns the id.
     *
     * @return the id; never {@code null}
     */
    public String id() {
        return id;
    }

    /**
     * Returns the currency symbol.
     *
     * @return the symbol; never {@code null}
     */
    public String symbol() {
        return symbol;
    }

    /**
     * Returns the description.
     *
     * @return the description; never {@code null}
     */
    public String description() {
        return description;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (IqEditBusinessProfilePriceTier) obj;
        return Objects.equals(this.id, that.id)
                && Objects.equals(this.symbol, that.symbol)
                && Objects.equals(this.description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, symbol, description);
    }

    @Override
    public String toString() {
        return "IqEditBusinessProfilePriceTier[id=" + id + ", symbol=" + symbol
                + ", description=" + description + ']';
    }
}
