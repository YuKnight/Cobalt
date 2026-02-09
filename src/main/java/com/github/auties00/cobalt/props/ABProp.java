package com.github.auties00.cobalt.props;

import java.util.Objects;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

/**
 * Represents an A/B testing property (AB prop) configuration value received from WhatsApp servers.
 * <p>
 * AB props are feature flags and configuration values that WhatsApp uses to control client behavior,
 * enable/disable features, and conduct A/B testing experiments. Each prop consists of:
 * <ul>
 *   <li>A numeric {@code code} that uniquely identifies the property</li>
 *   <li>A string {@code value} containing the actual value (may represent bool, int, float, or string)</li>
 *   <li>An optional {@code exposureKey} used for experiment tracking</li>
 * </ul>
 * <p>
 * This record is immutable and thread-safe.
 *
 * @param code   the unique numeric identifier for this configuration property
 * @param value  the string representation of the property value
 * @param exposureKey  optional key used for tracking experiment exposure, may be null
 */
public record ABProp(int code, String value, Long exposureKey) {
    /**
     * Controls whether LID migration is enabled.
     */
    public static final int LID_STATUS_SEND_ENABLED_AB_PROP_CODE = 6791;

    /**
     * Controls whether web client supports AI group open features.
     * Must be true for Meta AI bot features to work.
     */
    public static final int WEB_AI_GROUP_OPEN_SUPPORT_AB_PROP_CODE = 23530;

    /**
     * Controls whether AI group participation is enabled.
     * Must be true (along with WEB_AI_GROUP_OPEN_SUPPORT) for Meta AI bot to be included in phash.
     */
    public static final int AI_GROUP_PARTICIPATION_ENABLED_AB_PROP_CODE = 22171;

    /**
     * Number of days after which key index lists expire.
     * Device lists older than this threshold are considered fully expired.
     * Default: 35 days.
     */
    public static final int NUM_DAYS_KEY_INDEX_LIST_EXPIRATION_AB_PROP_CODE = 730;

    /**
     * Number of days before device expiry to trigger pre-expiration check.
     * Device lists within this threshold of expiration should be proactively refreshed.
     * Default: 7 days.
     */
    public static final int NUM_DAYS_BEFORE_DEVICE_EXPIRY_CHECK_AB_PROP_CODE = 731;

    /**
     * Controls whether to trigger logout when the user's own device list expires.
     * Per WhatsApp Web: when true, ADV expiration of own device list triggers logout.
     * Default: false.
     */
    public static final int WEB_ADV_LOGOUT_ON_SELF_DEVICE_LIST_EXPIRED_AB_PROP_CODE = 11011;

    /**
     * AB prop code for enabling hosted device validation.
     * Default: false.
     */
    public static final int ADV_ACCEPT_HOSTED_DEVICES_AB_PROP_CODE = 6939;

    /**
     * Controls whether to override the ADV account signature key for hosted devices.
     * When enabled, the accountSignatureKey from the signed key index list is saved
     * as the identity key for users with hosted devices.
     * Requires ADV_ACCEPT_HOSTED_DEVICES to also be enabled.
     */
    public static final int OVERRIDE_ADV_ACCOUNT_SIGNATURE_KEY_ENABLED_AB_PROP_CODE = 0; // TODO: Find actual code

    /**
     * Controls whether username display is enabled for contacts.
     * Per WhatsApp Web WAWebUsernameGatingUtils.usernameDisplayedEnabled():
     * checks this AB prop value.
     *
     * @apiNote WAWebABPropsConfigs: username_contact_display:[4746,"bool",!1,!0]
     */
    public static final int USERNAME_CONTACT_DISPLAY_AB_PROP_CODE = 4746;

    /**
     * Controls whether username protocol is enabled in USync queries.
     * When enabled, USync queries include the username protocol to fetch
     * username information for contacts.
     * <p>
     * Per WhatsApp Web WAWebUsernameGatingUtils.usernameUsyncEnabled():
     * This gates the addition of the username protocol element to USync queries.
     */
    public static final int USERNAME_USYNC_AB_PROP_CODE = 0; // TODO: Find actual code

    /**
     * Number of days to wait for a missing sync key before triggering a fatal error.
     * Per WhatsApp Web WAWebSyncdGatingUtils.getSyncdWaitForKeyTimeoutDays()
     * Default: 7 days.
     */
    public static final int SYNCD_WAIT_FOR_KEY_TIMEOUT_DAYS_AB_PROP_CODE = 14492;

    /**
     * Controls whether AI group participation sending is enabled.
     * Per WhatsApp Web WAWebBotGroupGatingUtils.isOpenGroupBotSendEnabled()
     * Requires WEB_AI_GROUP_OPEN_SUPPORT to also be enabled.
     */
    public static final int AI_GROUP_PARTICIPATION_SEND_ENABLED_AB_PROP_CODE = 22172;

    /**
     * Controls whether bot functionality is enabled on web client.
     * Per WhatsApp Web WAWebBotGating.isBotEnabled()
     */
    public static final int WEB_BOT_ENABLED_AB_PROP_CODE = 0; // TODO: Find actual code

    /**
     * Controls whether CTWA context logging is enabled.
     * Per WhatsApp Web WAWebCtwaAttributionUtils
     */
    public static final int CTWA_CONTEXT_LOGGING_ENABLED_AB_PROP_CODE = 11655;

    /**
     * Controls whether business hosted devices feature is enabled.
     * Per WhatsApp Web WAWebBizCoexGatingUtils.bizHostedDevicesEnabled()
     */
    public static final int BIZ_HOSTED_DEVICES_ENABLED_AB_PROP_CODE = 0; // TODO: Find actual code

    /**
     * Controls whether AI FBID migration for sending is enabled.
     * Per WhatsApp Web WAWebSimpleSignalPNToFBIDMigration.getFbidBotPersonaType()
     * When enabled, FBID bots include persona_type attribute in message stanzas.
     */
    public static final int AI_FBID_MIGRATION_SENDING_AB_PROP_CODE = 0; // TODO: Find actual code

    /**
     * Controls whether SAGA V1 is enabled for CAPI support accounts.
     * Per WhatsApp Web WAWebABPropsSaga.getIsSagaV1Enabled()
     */
    public static final int SAGA_V1_ENABLED_AB_PROP_CODE = 0; // TODO: Find actual code

    /**
     * Controls whether SAGA V1 reengagement is enabled.
     * Per WhatsApp Web WAWebABPropsSaga.getIsSagaV1ReengagementEnabled()
     */
    public static final int SAGA_V1_REENGAGEMENT_ENABLED_AB_PROP_CODE = 0; // TODO: Find actual code

    /**
     * Controls whether SAGA V1 carousel is enabled.
     * Per WhatsApp Web WAWebABPropsSaga.getIsSagaV1CarouselEnabled()
     */
    public static final int SAGA_V1_CAROUSEL_ENABLED_AB_PROP_CODE = 0; // TODO: Find actual code

    /**
     * Controls whether trust contact tokens are sent on all 1:1 messages.
     * Per WhatsApp Web WAWebSendMsgCreateFanoutStanza: checks this AB prop
     * to determine whether to include the {@code <tctoken>} node.
     *
     * @apiNote WAWebABPropsConfigs: privacy_token_sending_on_all_1_on_1_messages:[10518,"bool",!1,!1]
     */
    public static final int PRIVACY_TOKEN_SENDING_ON_ALL_1_ON_1_MESSAGES_AB_PROP_CODE = 10518;

    /**
     * Controls the ICDC identity key hash truncation length in bytes.
     * The actual hash length is {@code max(this value, 8)}.
     * Default: 10.
     *
     * @apiNote WAWebABPropsConfigs: md_icdc_hash_length:[310,"int",10,10]
     */
    public static final int MD_ICDC_HASH_LENGTH_AB_PROP_CODE = 310;

    /**
     * Maximum group size for generating content bindings (RCAT).
     * Per WhatsApp Web WAWebMsgRcatUtils.genContentBindingForMsg:
     * Content bindings are only generated for groups smaller than this threshold.
     * Default: 100.
     */
    public static final int MAXIMUM_GROUP_SIZE_FOR_RCAT_AB_PROP_CODE = 2915;

    /**
     * Controls the sender reporting token version.
     * Reporting tokens are enabled when this value is {@code > 0}.
     * Per WhatsApp Web WAWebMessagingGatingUtils.isReportingTokenSendingEnabled():
     * checks {@code getSenderReportingTokenVersion() > 0}.
     *
     * @apiNote WAWebABPropsConfigs: rt_sender_reporting_token_version:[8860,"int",2,2]
     */
    public static final int RT_SENDER_REPORTING_TOKEN_VERSION_AB_PROP_CODE = 8860;

    /**
     * Controls whether sending messages to the open Meta AI bot in
     * groups is enabled.
     *
     * @apiNote WAWebBotGroupGatingUtils.isOpenGroupBotSendEnabled:
     * checks {@code web_ai_group_open_support === true} AND
     * {@code ai_group_participation_send_enabled === true}.
     * WAWebABPropsConfigs: ai_group_participation_send_enabled:[22184,"bool",false,false]
     */
    public static final int OPEN_GROUP_BOT_SEND_ENABLED_AB_PROP_CODE = 22184;

    public ABProp {
        Objects.requireNonNull(value, "value cannot be null");
    }

    /**
     * Converts this property's value to a boolean.
     *
     * @return the boolean representation of this property's value
     */
    public boolean asBoolean() {
        return "1".equals(value)
            || "True".equals(value)
            || "true".equals(value);
    }

    /**
     * Attempts to convert this property's value to an integer.
     *
     * @return an {@link OptionalInt} containing the integer value if parsing succeeds, or empty if it fails
     */
    public OptionalInt asInt() {
        try {
            return OptionalInt.of(Integer.parseInt(value));
        } catch (NumberFormatException exception) {
            return OptionalInt.empty();
        }
    }

    /**
     * Attempts to convert this property's value to a long.
     *
     * @return an {@link OptionalLong} containing the long value if parsing succeeds, or empty if it fails
     */
    public OptionalLong asLong() {
        try {
            return OptionalLong.of(Long.parseLong(value));
        } catch (NumberFormatException exception) {
            return OptionalLong.empty();
        }
    }

    /**
     * Attempts to convert this property's value to a double (floating-point).
     *
     * @return an {@link OptionalDouble} containing the double value if parsing succeeds, or empty if it fails
     */
    public OptionalDouble asDouble() {
        try {
            return OptionalDouble.of(Double.parseDouble(value));
        } catch (NumberFormatException exception) {
            return OptionalDouble.empty();
        }
    }

    /**
     * Returns this property's value as a string.
     *
     * @return the string representation of this property's value
     */
    public String asString() {
        return value;
    }

    @Override
    public String toString() {
        return "ABProp[code=%d, value='%s', exposureKey=%s]"
                .formatted(code, value, exposureKey);
    }
}
