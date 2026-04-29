package com.github.auties00.cobalt.node.iq.biz;

import java.util.Objects;
import java.util.Optional;

/**
 * One business-hours configuration row.
 */
public final class IqEditBusinessProfileBusinessHoursConfig {
    /**
     * The day of week.
     */
    private final String dayOfWeek;

    /**
     * The mode (e.g. {@code "open_specific_hours"}).
     */
    private final String mode;

    /**
     * The optional opening time (minutes since midnight).
     */
    private final Integer openTime;

    /**
     * The optional closing time (minutes since midnight).
     */
    private final Integer closeTime;

    /**
     * Constructs a row.
     *
     * @param dayOfWeek the day; never {@code null}
     * @param mode      the mode; never {@code null}
     * @param openTime  the open time; may be {@code null}
     * @param closeTime the close time; may be {@code null}
     * @throws NullPointerException if {@code dayOfWeek} or
     *                              {@code mode} is {@code null}
     */
    public IqEditBusinessProfileBusinessHoursConfig(String dayOfWeek, String mode, Integer openTime, Integer closeTime) {
        this.dayOfWeek = Objects.requireNonNull(dayOfWeek, "dayOfWeek cannot be null");
        this.mode = Objects.requireNonNull(mode, "mode cannot be null");
        this.openTime = openTime;
        this.closeTime = closeTime;
    }

    /**
     * Returns the day.
     *
     * @return the day; never {@code null}
     */
    public String dayOfWeek() {
        return dayOfWeek;
    }

    /**
     * Returns the mode.
     *
     * @return the mode; never {@code null}
     */
    public String mode() {
        return mode;
    }

    /**
     * Returns the open time.
     *
     * @return an {@link Optional} carrying the time
     */
    public Optional<Integer> openTime() {
        return Optional.ofNullable(openTime);
    }

    /**
     * Returns the close time.
     *
     * @return an {@link Optional} carrying the time
     */
    public Optional<Integer> closeTime() {
        return Optional.ofNullable(closeTime);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (IqEditBusinessProfileBusinessHoursConfig) obj;
        return Objects.equals(this.dayOfWeek, that.dayOfWeek)
                && Objects.equals(this.mode, that.mode)
                && Objects.equals(this.openTime, that.openTime)
                && Objects.equals(this.closeTime, that.closeTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dayOfWeek, mode, openTime, closeTime);
    }

    @Override
    public String toString() {
        return "IqEditBusinessProfileBusinessHoursConfig[dayOfWeek=" + dayOfWeek
                + ", mode=" + mode + ']';
    }
}
