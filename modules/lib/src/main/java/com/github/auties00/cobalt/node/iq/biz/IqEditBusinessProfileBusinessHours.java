package com.github.auties00.cobalt.node.iq.biz;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Typed business-hours payload carried as the {@code <business_hours/>}
 * child of an {@link IqEditBusinessProfileRequest}. Wraps the optional
 * IANA timezone, the optional schedule note, and the per-day-of-week
 * configuration rows.
 */
public final class IqEditBusinessProfileBusinessHours {
    /**
     * The IANA timezone identifier, when supplied.
     */
    private final String timezone;

    /**
     * The optional note text rendered above the schedule grid.
     */
    private final String note;

    /**
     * The schedule rows.
     */
    private final List<IqEditBusinessProfileBusinessHoursConfig> config;

    /**
     * Constructs a payload.
     *
     * @param timezone the timezone; may be {@code null}
     * @param note     the note; may be {@code null}
     * @param config   the schedule rows; never {@code null}
     * @throws NullPointerException if {@code config} is {@code null}
     */
    public IqEditBusinessProfileBusinessHours(String timezone, String note, List<IqEditBusinessProfileBusinessHoursConfig> config) {
        this.timezone = timezone;
        this.note = note;
        Objects.requireNonNull(config, "config cannot be null");
        this.config = List.copyOf(config);
    }

    /**
     * Returns the timezone.
     *
     * @return an {@link Optional} carrying the timezone
     */
    public Optional<String> timezone() {
        return Optional.ofNullable(timezone);
    }

    /**
     * Returns the note text.
     *
     * @return an {@link Optional} carrying the note
     */
    public Optional<String> note() {
        return Optional.ofNullable(note);
    }

    /**
     * Returns the schedule rows.
     *
     * @return an unmodifiable list; never {@code null}
     */
    public List<IqEditBusinessProfileBusinessHoursConfig> config() {
        return config;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (IqEditBusinessProfileBusinessHours) obj;
        return Objects.equals(this.timezone, that.timezone)
                && Objects.equals(this.note, that.note)
                && Objects.equals(this.config, that.config);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timezone, note, config);
    }

    @Override
    public String toString() {
        return "IqEditBusinessProfileBusinessHours[timezone=" + timezone
                + ", note=" + note + ", config=" + config + ']';
    }
}
