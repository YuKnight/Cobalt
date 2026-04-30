package com.github.auties00.cobalt.node.iq.biz;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.iq.IqOperation;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The outbound {@code <iq xmlns="w:biz" type="set">} stanza that mutates
 * the current merchant's business profile. Each non-{@code null} field
 * becomes one child of the {@code <business_profile/>} delta payload, so
 * callers can patch any subset of address, geo coordinates, description,
 * email, websites, categories, business hours, price tier or service
 * areas in a single round-trip.
 */
@WhatsAppWebModule(moduleName = "WAWebBusinessProfileJob")
public final class IqEditBusinessProfileRequest implements IqOperation.Request {
    /**
     * The optional address line.
     */
    private final String address;

    /**
     * The optional latitude.
     */
    private final Double latitude;

    /**
     * The optional longitude.
     */
    private final Double longitude;

    /**
     * The optional self-description.
     */
    private final String description;

    /**
     * The optional contact email.
     */
    private final String email;

    /**
     * The optional list of website slots — empty list clears all
     * slots; non-null overrides whatever the relay had cached.
     */
    private final List<IqEditBusinessProfileWebsite> websites;

    /**
     * The optional list of category ids.
     */
    private final List<String> categories;

    /**
     * The optional business-hours payload.
     */
    private final IqEditBusinessProfileBusinessHours businessHours;

    /**
     * The optional price-tier id — non-null triggers the
     * {@code <price_tier/>} child.
     */
    private final String priceTierId;

    /**
     * The optional list of service-area entries.
     */
    private final List<IqEditBusinessProfileServiceArea> serviceAreas;

    /**
     * Constructs a request directly. Use {@link #builder()} for a
     * fluent alternative.
     *
     * @param address       see {@link #address()}
     * @param latitude      see {@link #latitude()}
     * @param longitude     see {@link #longitude()}
     * @param description   see {@link #description()}
     * @param email         see {@link #email()}
     * @param websites      see {@link #websites()}
     * @param categories    see {@link #categories()}
     * @param businessHours see {@link #businessHours()}
     * @param priceTierId   see {@link #priceTierId()}
     * @param serviceAreas  see {@link #serviceAreas()}
     */
    public IqEditBusinessProfileRequest(String address,
                   Double latitude,
                   Double longitude,
                   String description,
                   String email,
                   List<IqEditBusinessProfileWebsite> websites,
                   List<String> categories,
                   IqEditBusinessProfileBusinessHours businessHours,
                   String priceTierId,
                   List<IqEditBusinessProfileServiceArea> serviceAreas) {
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = description;
        this.email = email;
        this.websites = websites == null ? null : List.copyOf(websites);
        this.categories = categories == null ? null : List.copyOf(categories);
        this.businessHours = businessHours;
        this.priceTierId = priceTierId;
        this.serviceAreas = serviceAreas == null ? null : List.copyOf(serviceAreas);
    }

    /**
     * Returns the address.
     *
     * @return an {@link Optional} carrying the address
     */
    public Optional<String> address() {
        return Optional.ofNullable(address);
    }

    /**
     * Returns the latitude.
     *
     * @return an {@link Optional} carrying the latitude
     */
    public Optional<Double> latitude() {
        return Optional.ofNullable(latitude);
    }

    /**
     * Returns the longitude.
     *
     * @return an {@link Optional} carrying the longitude
     */
    public Optional<Double> longitude() {
        return Optional.ofNullable(longitude);
    }

    /**
     * Returns the description.
     *
     * @return an {@link Optional} carrying the description
     */
    public Optional<String> description() {
        return Optional.ofNullable(description);
    }

    /**
     * Returns the email.
     *
     * @return an {@link Optional} carrying the email
     */
    public Optional<String> email() {
        return Optional.ofNullable(email);
    }

    /**
     * Returns the website slots.
     *
     * @return an {@link Optional} carrying the list
     */
    public Optional<List<IqEditBusinessProfileWebsite>> websites() {
        return Optional.ofNullable(websites);
    }

    /**
     * Returns the category id list.
     *
     * @return an {@link Optional} carrying the list
     */
    public Optional<List<String>> categories() {
        return Optional.ofNullable(categories);
    }

    /**
     * Returns the business-hours payload.
     *
     * @return an {@link Optional} carrying the payload
     */
    public Optional<IqEditBusinessProfileBusinessHours> businessHours() {
        return Optional.ofNullable(businessHours);
    }

    /**
     * Returns the price-tier id.
     *
     * @return an {@link Optional} carrying the id
     */
    public Optional<String> priceTierId() {
        return Optional.ofNullable(priceTierId);
    }

    /**
     * Returns the service-area list.
     *
     * @return an {@link Optional} carrying the list
     */
    public Optional<List<IqEditBusinessProfileServiceArea>> serviceAreas() {
        return Optional.ofNullable(serviceAreas);
    }

    /**
     * Returns a fresh builder.
     *
     * @return a new {@link Builder}; never {@code null}
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebBusinessProfileJob",
            exports = "editBusinessProfile", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var children = new ArrayList<Node>();
        if (address != null) {
            children.add(new NodeBuilder().description("address").content(address).build());
        }
        if (latitude != null) {
            children.add(new NodeBuilder().description("latitude")
                    .content(String.valueOf(latitude.doubleValue())).build());
        }
        if (longitude != null) {
            children.add(new NodeBuilder().description("longitude")
                    .content(String.valueOf(longitude.doubleValue())).build());
        }
        if (description != null) {
            children.add(new NodeBuilder().description("description").content(description).build());
        }
        if (email != null) {
            children.add(new NodeBuilder().description("email").content(email).build());
        }
        if (websites != null) {
            if (websites.isEmpty()) {
                children.add(new NodeBuilder().description("website").build());
            } else {
                if (websites.size() >= 1) {
                    children.add(new NodeBuilder().description("website")
                            .content(websites.get(0).url()).build());
                }
                if (websites.size() >= 2) {
                    children.add(new NodeBuilder().description("website")
                            .content(websites.get(1).url()).build());
                }
            }
        }
        if (categories != null) {
            var categoryNodes = new ArrayList<Node>();
            for (var id : categories) {
                categoryNodes.add(new NodeBuilder()
                        .description("category")
                        .attribute("id", id)
                        .build());
            }
            children.add(new NodeBuilder()
                    .description("categories")
                    .content(categoryNodes)
                    .build());
        }
        if (businessHours != null) {
            children.add(buildBusinessHoursNode(businessHours));
        }
        if (priceTierId != null) {
            children.add(new NodeBuilder()
                    .description("price_tier")
                    .attribute("id", priceTierId)
                    .attribute("symbol", "")
                    .content("")
                    .build());
        }
        if (serviceAreas != null) {
            var serviceAreaNodes = new ArrayList<Node>();
            for (var sa : serviceAreas) {
                var areaDescription = new NodeBuilder()
                        .description("area_description")
                        .content(sa.areaDescription())
                        .build();
                var areaRadius = new NodeBuilder()
                        .description("area_radius_meters")
                        .content(String.valueOf(sa.radius()))
                        .build();
                var lat = new NodeBuilder()
                        .description("latitude")
                        .content(String.valueOf(sa.latitude()))
                        .build();
                var lon = new NodeBuilder()
                        .description("longitude")
                        .content(String.valueOf(sa.longitude()))
                        .build();
                var areaCenter = new NodeBuilder()
                        .description("area_center")
                        .content(List.of(lat, lon))
                        .build();
                serviceAreaNodes.add(new NodeBuilder()
                        .description("service_area")
                        .content(List.of(areaDescription, areaRadius, areaCenter))
                        .build());
            }
            children.add(new NodeBuilder()
                    .description("service_areas")
                    .content(serviceAreaNodes)
                    .build());
        }
        var businessProfileNode = new NodeBuilder()
                .description("business_profile")
                .attribute("v", "3")
                .attribute("mutation_type", "delta")
                .content(children)
                .build();
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "w:biz")
                .attribute("to", JidServer.user())
                .attribute("type", "set")
                .content(businessProfileNode);
    }

    /**
     * Builds the {@code <business_hours/>} child from the supplied
     * payload.
     *
     * @param hours the payload; never {@code null}
     * @return the built node; never {@code null}
     */
    private static Node buildBusinessHoursNode(IqEditBusinessProfileBusinessHours hours) {
        var hoursChildren = new ArrayList<Node>();
        if (hours.note().isPresent() && !hours.note().get().isEmpty()) {
            hoursChildren.add(new NodeBuilder()
                    .description("business_hours_note")
                    .content(hours.note().get())
                    .build());
        }
        for (var c : hours.config()) {
            var configBuilder = new NodeBuilder()
                    .description("business_hours_config")
                    .attribute("day_of_week", c.dayOfWeek())
                    .attribute("mode", c.mode());
            if (c.openTime().isPresent()) {
                configBuilder.attribute("open_time", String.valueOf(c.openTime().get()));
            }
            if (c.closeTime().isPresent()) {
                configBuilder.attribute("close_time", String.valueOf(c.closeTime().get()));
            }
            hoursChildren.add(configBuilder.build());
        }
        var businessHoursBuilder = new NodeBuilder()
                .description("business_hours");
        if (hours.timezone().isPresent()) {
            businessHoursBuilder.attribute("timezone", hours.timezone().get());
        }
        return businessHoursBuilder
                .content(hoursChildren)
                .build();
    }

    /**
     * Fluent builder for {@link IqEditBusinessProfileRequest}.
     */
    public static final class Builder {
        /**
         * The optional address.
         */
        private String address;

        /**
         * The optional latitude.
         */
        private Double latitude;

        /**
         * The optional longitude.
         */
        private Double longitude;

        /**
         * The optional description.
         */
        private String description;

        /**
         * The optional email.
         */
        private String email;

        /**
         * The optional website slots.
         */
        private List<IqEditBusinessProfileWebsite> websites;

        /**
         * The optional category id list.
         */
        private List<String> categories;

        /**
         * The optional business-hours payload.
         */
        private IqEditBusinessProfileBusinessHours businessHours;

        /**
         * The optional price-tier id.
         */
        private String priceTierId;

        /**
         * The optional service-area list.
         */
        private List<IqEditBusinessProfileServiceArea> serviceAreas;

        /**
         * Package-private — use {@link IqEditBusinessProfileRequest#builder()}.
         */
        Builder() {
        }

        /**
         * Sets the address.
         *
         * @param address the address; may be {@code null}
         * @return this builder; never {@code null}
         */
        public Builder address(String address) {
            this.address = address;
            return this;
        }

        /**
         * Sets the latitude.
         *
         * @param latitude the latitude; may be {@code null}
         * @return this builder; never {@code null}
         */
        public Builder latitude(Double latitude) {
            this.latitude = latitude;
            return this;
        }

        /**
         * Sets the longitude.
         *
         * @param longitude the longitude; may be {@code null}
         * @return this builder; never {@code null}
         */
        public Builder longitude(Double longitude) {
            this.longitude = longitude;
            return this;
        }

        /**
         * Sets the description.
         *
         * @param description the description; may be {@code null}
         * @return this builder; never {@code null}
         */
        public Builder description(String description) {
            this.description = description;
            return this;
        }

        /**
         * Sets the email.
         *
         * @param email the email; may be {@code null}
         * @return this builder; never {@code null}
         */
        public Builder email(String email) {
            this.email = email;
            return this;
        }

        /**
         * Sets the website slot list.
         *
         * @param websites the websites; may be {@code null}
         * @return this builder; never {@code null}
         */
        public Builder websites(List<IqEditBusinessProfileWebsite> websites) {
            this.websites = websites;
            return this;
        }

        /**
         * Sets the category id list.
         *
         * @param categories the categories; may be {@code null}
         * @return this builder; never {@code null}
         */
        public Builder categories(List<String> categories) {
            this.categories = categories;
            return this;
        }

        /**
         * Sets the business-hours payload.
         *
         * @param businessHours the payload; may be {@code null}
         * @return this builder; never {@code null}
         */
        public Builder businessHours(IqEditBusinessProfileBusinessHours businessHours) {
            this.businessHours = businessHours;
            return this;
        }

        /**
         * Sets the price-tier id.
         *
         * @param priceTierId the id; may be {@code null}
         * @return this builder; never {@code null}
         */
        public Builder priceTierId(String priceTierId) {
            this.priceTierId = priceTierId;
            return this;
        }

        /**
         * Sets the service-area list.
         *
         * @param serviceAreas the areas; may be {@code null}
         * @return this builder; never {@code null}
         */
        public Builder serviceAreas(List<IqEditBusinessProfileServiceArea> serviceAreas) {
            this.serviceAreas = serviceAreas;
            return this;
        }

        /**
         * Builds a new {@link IqEditBusinessProfileRequest}.
         *
         * @return the built request; never {@code null}
         */
        public IqEditBusinessProfileRequest build() {
            return new IqEditBusinessProfileRequest(address, latitude, longitude, description, email,
                    websites, categories, businessHours, priceTierId, serviceAreas);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (IqEditBusinessProfileRequest) obj;
        return Objects.equals(this.address, that.address)
                && Objects.equals(this.latitude, that.latitude)
                && Objects.equals(this.longitude, that.longitude)
                && Objects.equals(this.description, that.description)
                && Objects.equals(this.email, that.email)
                && Objects.equals(this.websites, that.websites)
                && Objects.equals(this.categories, that.categories)
                && Objects.equals(this.businessHours, that.businessHours)
                && Objects.equals(this.priceTierId, that.priceTierId)
                && Objects.equals(this.serviceAreas, that.serviceAreas);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, latitude, longitude, description, email,
                websites, categories, businessHours, priceTierId, serviceAreas);
    }

    @Override
    public String toString() {
        return "IqEditBusinessProfileRequest[address=" + address
                + ", description=" + description + ", email=" + email
                + ", websites=" + websites + ']';
    }
}
