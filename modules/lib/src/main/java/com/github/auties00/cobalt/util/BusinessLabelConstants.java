package com.github.auties00.cobalt.util;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.wam.type.CtwaLabelType;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * Holds the compile-time constants and pure lookup functions that WhatsApp
 * Business clients use to render and classify the built-in chat labels.
 *
 * <p>The class exposes the two colour palettes shipped for the label picker
 * (Android and iPhone), the integer identifiers and display names of the
 * eight predefined labels, the canonical wire subtype strings, the maximum
 * number of characters a user may type into a label name, and the three
 * pure mapping helpers that convert between those representations.
 *
 * <p>The palettes are exposed as unmodifiable {@link List}s to preserve the
 * {@code Object.freeze} immutability that WhatsApp Web applies to the
 * underlying arrays. Helpers return {@link OptionalInt} or {@link Optional}
 * instead of the {@code undefined}-or-value pattern used by the JavaScript
 * source.
 */
@WhatsAppWebModule(moduleName = "WAWebLabelConstants")
public final class BusinessLabelConstants {
    /**
     * The ordered palette of twenty hex colour swatches the Android WhatsApp
     * client uses to paint chat labels.
     *
     * <p>The colour at index {@code i} is the colour associated with palette
     * slot {@code i}. A {@link com.github.auties00.cobalt.model.preference.Label}
     * stores an index into this palette rather than a concrete colour so
     * that the same numeric value always produces the same visual colour.
     */
    @WhatsAppWebExport(moduleName = "WAWebLabelConstants",
            exports = "ANDROID_LABEL_COLOR_PALETTE",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final List<String> ANDROID_LABEL_COLOR_PALETTE = List.of(
            "#FF9485", "#64C4FF", "#FFD429", "#DFAEF0", "#99B6C1",
            "#55CCB3", "#FF9DFF", "#D3A91D", "#6D7CCE", "#D7E752",
            "#00D0E2", "#FFC5C7", "#93CEAC", "#F74848", "#00A0F2",
            "#83E422", "#FFAF04", "#B5EBFF", "#9BA6FF", "#9368CF"
    );

    /**
     * The ordered palette of twenty hex colour swatches the iPhone WhatsApp
     * client uses to paint chat labels.
     *
     * <p>The colour at index {@code i} is the colour associated with palette
     * slot {@code i}. A {@link com.github.auties00.cobalt.model.preference.Label}
     * stores an index into this palette rather than a concrete colour so
     * that the same numeric value always produces the same visual colour.
     */
    @WhatsAppWebExport(moduleName = "WAWebLabelConstants",
            exports = "IPHONE_LABEL_COLOR_PALETTE",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final List<String> IPHONE_LABEL_COLOR_PALETTE = List.of(
            "#A62C71", "#90A841", "#C1A03F", "#792138", "#AE8774",
            "#F0B330", "#B6B327", "#C69FCC", "#8B6990", "#FF8A8C",
            "#54C265", "#FF7B6B", "#26C4DC", "#57C9FF", "#74676A",
            "#7E90A3", "#5696FF", "#6E257E", "#7ACBA5", "#243640"
    );

    /**
     * The predefined-id integer assigned to the "New customer" label.
     */
    @WhatsAppWebExport(moduleName = "WAWebLabelConstants",
            exports = "PREDEFINED_LABEL_IDS",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final int PREDEFINED_LABEL_ID_NEW_CUSTOMER = 1;

    /**
     * The predefined-id integer assigned to the "New order" label.
     */
    @WhatsAppWebExport(moduleName = "WAWebLabelConstants",
            exports = "PREDEFINED_LABEL_IDS",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final int PREDEFINED_LABEL_ID_NEW_ORDER = 2;

    /**
     * The predefined-id integer assigned to the "Pending payment" label.
     */
    @WhatsAppWebExport(moduleName = "WAWebLabelConstants",
            exports = "PREDEFINED_LABEL_IDS",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final int PREDEFINED_LABEL_ID_PENDING_PAYMENT = 3;

    /**
     * The predefined-id integer assigned to the "Paid" label.
     */
    @WhatsAppWebExport(moduleName = "WAWebLabelConstants",
            exports = "PREDEFINED_LABEL_IDS",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final int PREDEFINED_LABEL_ID_PAID = 4;

    /**
     * The predefined-id integer assigned to the "Order complete" label.
     */
    @WhatsAppWebExport(moduleName = "WAWebLabelConstants",
            exports = "PREDEFINED_LABEL_IDS",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final int PREDEFINED_LABEL_ID_ORDER_COMPLETE = 5;

    /**
     * The predefined-id integer assigned to the "Important" label.
     */
    @WhatsAppWebExport(moduleName = "WAWebLabelConstants",
            exports = "PREDEFINED_LABEL_IDS",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final int PREDEFINED_LABEL_ID_IMPORTANT = 6;

    /**
     * The predefined-id integer assigned to the "Follow up" label.
     */
    @WhatsAppWebExport(moduleName = "WAWebLabelConstants",
            exports = "PREDEFINED_LABEL_IDS",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final int PREDEFINED_LABEL_ID_FOLLOW_UP = 7;

    /**
     * The predefined-id integer assigned to the "Lead" label.
     */
    @WhatsAppWebExport(moduleName = "WAWebLabelConstants",
            exports = "PREDEFINED_LABEL_IDS",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final int PREDEFINED_LABEL_ID_LEAD = 8;

    /**
     * The predefined-id integer reserved for the "Delivery-Order: new order"
     * derived label, which {@link #mapPredefinedIdToLabelName(int)} folds
     * back to {@link #PREDEFINED_LABEL_NAME_NEW_ORDER}.
     */
    @WhatsAppWebExport(moduleName = "WAWebLabelConstants",
            exports = "PREDEFINED_LABEL_IDS",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final int PREDEFINED_LABEL_ID_DO_NEW_ORDER = 9;

    /**
     * The predefined-id integer reserved for the "Delivery-Order: lead"
     * derived label, which {@link #mapPredefinedIdToLabelName(int)} folds
     * back to {@link #PREDEFINED_LABEL_NAME_LEAD}.
     */
    @WhatsAppWebExport(moduleName = "WAWebLabelConstants",
            exports = "PREDEFINED_LABEL_IDS",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final int PREDEFINED_LABEL_ID_DO_LEAD = 10;

    /**
     * The user-visible display name of the "New customer" predefined label.
     */
    @WhatsAppWebExport(moduleName = "WAWebLabelConstants",
            exports = "PREDEFINED_LABEL_NAMES",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final String PREDEFINED_LABEL_NAME_NEW_CUSTOMER = "New customer";

    /**
     * The user-visible display name of the "New order" predefined label.
     */
    @WhatsAppWebExport(moduleName = "WAWebLabelConstants",
            exports = "PREDEFINED_LABEL_NAMES",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final String PREDEFINED_LABEL_NAME_NEW_ORDER = "New order";

    /**
     * The user-visible display name of the "Pending payment" predefined label.
     */
    @WhatsAppWebExport(moduleName = "WAWebLabelConstants",
            exports = "PREDEFINED_LABEL_NAMES",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final String PREDEFINED_LABEL_NAME_PENDING_PAYMENT = "Pending payment";

    /**
     * The user-visible display name of the "Paid" predefined label.
     */
    @WhatsAppWebExport(moduleName = "WAWebLabelConstants",
            exports = "PREDEFINED_LABEL_NAMES",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final String PREDEFINED_LABEL_NAME_PAID = "Paid";

    /**
     * The user-visible display name of the "Order complete" predefined label.
     */
    @WhatsAppWebExport(moduleName = "WAWebLabelConstants",
            exports = "PREDEFINED_LABEL_NAMES",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final String PREDEFINED_LABEL_NAME_ORDER_COMPLETE = "Order complete";

    /**
     * The user-visible display name of the "Important" predefined label.
     */
    @WhatsAppWebExport(moduleName = "WAWebLabelConstants",
            exports = "PREDEFINED_LABEL_NAMES",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final String PREDEFINED_LABEL_NAME_IMPORTANT = "Important";

    /**
     * The user-visible display name of the "Follow up" predefined label.
     */
    @WhatsAppWebExport(moduleName = "WAWebLabelConstants",
            exports = "PREDEFINED_LABEL_NAMES",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final String PREDEFINED_LABEL_NAME_FOLLOW_UP = "Follow up";

    /**
     * The user-visible display name of the "Lead" predefined label.
     */
    @WhatsAppWebExport(moduleName = "WAWebLabelConstants",
            exports = "PREDEFINED_LABEL_NAMES",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final String PREDEFINED_LABEL_NAME_LEAD = "Lead";

    /**
     * The canonical subtype string WhatsApp transmits on the wire for the
     * "New customer" custom-label subtype.
     */
    static final String CUSTOM_LABEL_SUBTYPE_NEW_CUSTOMER = "new_customer";

    /**
     * The canonical subtype string WhatsApp transmits on the wire for the
     * "New order" custom-label subtype.
     */
    static final String CUSTOM_LABEL_SUBTYPE_NEW_ORDER = "new_order";

    /**
     * The canonical subtype string WhatsApp transmits on the wire for the
     * "Pending payment" custom-label subtype.
     */
    static final String CUSTOM_LABEL_SUBTYPE_PENDING_PAYMENT = "pending_payment";

    /**
     * The canonical subtype string WhatsApp transmits on the wire for the
     * "Paid" custom-label subtype.
     */
    static final String CUSTOM_LABEL_SUBTYPE_PAID = "paid";

    /**
     * The canonical subtype string WhatsApp transmits on the wire for the
     * "Order complete" custom-label subtype.
     */
    static final String CUSTOM_LABEL_SUBTYPE_ORDER_COMPLETE = "order_complete";

    /**
     * The canonical subtype string WhatsApp transmits on the wire for the
     * "Important" custom-label subtype.
     */
    static final String CUSTOM_LABEL_SUBTYPE_IMPORTANT = "important";

    /**
     * The canonical subtype string WhatsApp transmits on the wire for the
     * "Follow up" custom-label subtype.
     */
    static final String CUSTOM_LABEL_SUBTYPE_FOLLOW_UP = "follow_up";

    /**
     * The canonical subtype string WhatsApp transmits on the wire for the
     * "Lead" custom-label subtype.
     */
    static final String CUSTOM_LABEL_SUBTYPE_LEAD = "lead";

    /**
     * The maximum number of characters a user may type into a label name.
     *
     * <p>WhatsApp enforces this limit client-side so that labels always fit
     * on the chat list card. Application code should validate user-entered
     * names against this ceiling before constructing a
     * {@link com.github.auties00.cobalt.model.preference.Label}.
     */
    @WhatsAppWebExport(moduleName = "WAWebLabelConstants",
            exports = "LABEL_NAME_MAX_LENGTH",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final int LABEL_NAME_MAX_LENGTH = 100;

    /**
     * Prevents instantiation of this utility class.
     */
    private BusinessLabelConstants() {
        throw new AssertionError("BusinessLabelConstants is not instantiable");
    }

    /**
     * Returns the predefined-label identifier associated with the given
     * user-visible label name when that name matches one of the eight
     * predefined display strings WhatsApp ships.
     *
     * <p>The lookup is case-sensitive. All other inputs, including
     * {@code null}, return an empty {@link OptionalInt}.
     *
     * @param labelName the user-visible label name to resolve
     * @return the predefined identifier of the matching built-in label, or
     *         an empty {@link OptionalInt} when {@code labelName} is not a
     *         predefined display name
     */
    @WhatsAppWebExport(moduleName = "WAWebLabelConstants",
            exports = "mapLabelNameToPredefinedId",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static OptionalInt mapLabelNameToPredefinedId(String labelName) {
        if (labelName == null) {
            return OptionalInt.empty();
        }
        return switch (labelName) {
            case PREDEFINED_LABEL_NAME_NEW_CUSTOMER -> OptionalInt.of(PREDEFINED_LABEL_ID_NEW_CUSTOMER);
            case PREDEFINED_LABEL_NAME_NEW_ORDER -> OptionalInt.of(PREDEFINED_LABEL_ID_NEW_ORDER);
            case PREDEFINED_LABEL_NAME_PENDING_PAYMENT -> OptionalInt.of(PREDEFINED_LABEL_ID_PENDING_PAYMENT);
            case PREDEFINED_LABEL_NAME_PAID -> OptionalInt.of(PREDEFINED_LABEL_ID_PAID);
            case PREDEFINED_LABEL_NAME_ORDER_COMPLETE -> OptionalInt.of(PREDEFINED_LABEL_ID_ORDER_COMPLETE);
            case PREDEFINED_LABEL_NAME_IMPORTANT -> OptionalInt.of(PREDEFINED_LABEL_ID_IMPORTANT);
            case PREDEFINED_LABEL_NAME_FOLLOW_UP -> OptionalInt.of(PREDEFINED_LABEL_ID_FOLLOW_UP);
            case PREDEFINED_LABEL_NAME_LEAD -> OptionalInt.of(PREDEFINED_LABEL_ID_LEAD);
            default -> OptionalInt.empty();
        };
    }

    /**
     * Returns the {@link CtwaLabelType} that corresponds to the given custom
     * label subtype string used on the Click-to-WhatsApp ads telemetry
     * channel.
     *
     * <p>Unknown subtype strings, including {@code null}, fold back to
     * {@link CtwaLabelType#LEAD} so the CTWA sink always receives a valid
     * value.
     *
     * @param subtype the custom-label subtype string, typically sourced from
     *                a server-side sync, may be {@code null}
     * @return the matching {@link CtwaLabelType}, or {@link CtwaLabelType#LEAD}
     *         when {@code subtype} is {@code null} or unrecognised
     */
    @WhatsAppWebExport(moduleName = "WAWebLabelConstants",
            exports = "mapCustomLabelSubtypeToCTWALabelType",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static CtwaLabelType mapCustomLabelSubtypeToCTWALabelType(String subtype) {
        if (subtype == null) {
            return CtwaLabelType.LEAD;
        }
        return switch (subtype) {
            case CUSTOM_LABEL_SUBTYPE_NEW_CUSTOMER -> CtwaLabelType.NEW_CUSTOMER;
            case CUSTOM_LABEL_SUBTYPE_NEW_ORDER -> CtwaLabelType.NEW_ORDER;
            case CUSTOM_LABEL_SUBTYPE_PENDING_PAYMENT -> CtwaLabelType.PENDING_PAYMENT;
            case CUSTOM_LABEL_SUBTYPE_PAID -> CtwaLabelType.PAID;
            case CUSTOM_LABEL_SUBTYPE_ORDER_COMPLETE -> CtwaLabelType.ORDER_COMPLETE;
            case CUSTOM_LABEL_SUBTYPE_IMPORTANT -> CtwaLabelType.IMPORTANT;
            case CUSTOM_LABEL_SUBTYPE_FOLLOW_UP -> CtwaLabelType.FOLLOW_UP;
            default -> CtwaLabelType.LEAD;
        };
    }

    /**
     * Returns the user-visible display name for the given predefined label
     * identifier.
     *
     * <p>Accepts the eight primary predefined ids
     * ({@link #PREDEFINED_LABEL_ID_NEW_CUSTOMER} through
     * {@link #PREDEFINED_LABEL_ID_LEAD}) and the two derived Delivery-Order
     * identifiers, which fold back onto the corresponding primary names.
     *
     * @param predefinedId the predefined label identifier to resolve
     * @return the display name associated with {@code predefinedId}, or an
     *         empty {@link Optional} when {@code predefinedId} is not a
     *         known predefined label id
     */
    @WhatsAppWebExport(moduleName = "WAWebLabelConstants",
            exports = "mapPredefinedIdToLabelName",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static Optional<String> mapPredefinedIdToLabelName(int predefinedId) {
        return switch (predefinedId) {
            case PREDEFINED_LABEL_ID_NEW_CUSTOMER -> Optional.of(PREDEFINED_LABEL_NAME_NEW_CUSTOMER);
            case PREDEFINED_LABEL_ID_NEW_ORDER, PREDEFINED_LABEL_ID_DO_NEW_ORDER
                    -> Optional.of(PREDEFINED_LABEL_NAME_NEW_ORDER);
            case PREDEFINED_LABEL_ID_PENDING_PAYMENT -> Optional.of(PREDEFINED_LABEL_NAME_PENDING_PAYMENT);
            case PREDEFINED_LABEL_ID_PAID -> Optional.of(PREDEFINED_LABEL_NAME_PAID);
            case PREDEFINED_LABEL_ID_ORDER_COMPLETE -> Optional.of(PREDEFINED_LABEL_NAME_ORDER_COMPLETE);
            case PREDEFINED_LABEL_ID_IMPORTANT -> Optional.of(PREDEFINED_LABEL_NAME_IMPORTANT);
            case PREDEFINED_LABEL_ID_FOLLOW_UP -> Optional.of(PREDEFINED_LABEL_NAME_FOLLOW_UP);
            case PREDEFINED_LABEL_ID_LEAD, PREDEFINED_LABEL_ID_DO_LEAD
                    -> Optional.of(PREDEFINED_LABEL_NAME_LEAD);
            default -> Optional.empty();
        };
    }
}
