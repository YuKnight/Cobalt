package com.github.auties00.cobalt.node.iq.biz;

import java.util.Objects;

/**
 * Typed service-area entry carried inside the {@code <service_areas/>}
 * child of an {@link IqEditBusinessProfileRequest}. Combines a free-text
 * description, a radius in meters, and a center latitude and longitude.
 */
public final class IqEditBusinessProfileServiceArea {
    /**
     * The free-text area description.
     */
    private final String areaDescription;

    /**
     * The radius in meters.
     */
    private final double radius;

    /**
     * The center latitude.
     */
    private final double latitude;

    /**
     * The center longitude.
     */
    private final double longitude;

    /**
     * Constructs an area.
     *
     * @param areaDescription the description; never {@code null}
     * @param radius          the radius in meters
     * @param latitude        the latitude
     * @param longitude       the longitude
     * @throws NullPointerException if {@code areaDescription} is
     *                              {@code null}
     */
    public IqEditBusinessProfileServiceArea(String areaDescription, double radius, double latitude, double longitude) {
        this.areaDescription = Objects.requireNonNull(areaDescription, "areaDescription cannot be null");
        this.radius = radius;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Returns the description.
     *
     * @return the description; never {@code null}
     */
    public String areaDescription() {
        return areaDescription;
    }

    /**
     * Returns the radius.
     *
     * @return the radius
     */
    public double radius() {
        return radius;
    }

    /**
     * Returns the latitude.
     *
     * @return the latitude
     */
    public double latitude() {
        return latitude;
    }

    /**
     * Returns the longitude.
     *
     * @return the longitude
     */
    public double longitude() {
        return longitude;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (IqEditBusinessProfileServiceArea) obj;
        return Double.compare(this.radius, that.radius) == 0
                && Double.compare(this.latitude, that.latitude) == 0
                && Double.compare(this.longitude, that.longitude) == 0
                && Objects.equals(this.areaDescription, that.areaDescription);
    }

    @Override
    public int hashCode() {
        return Objects.hash(areaDescription, radius, latitude, longitude);
    }

    @Override
    public String toString() {
        return "IqEditBusinessProfileServiceArea[areaDescription=" + areaDescription
                + ", radius=" + radius + ", latitude=" + latitude + ", longitude=" + longitude + ']';
    }
}
