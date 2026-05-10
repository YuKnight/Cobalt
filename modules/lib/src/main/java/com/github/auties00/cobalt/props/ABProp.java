package com.github.auties00.cobalt.props;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;

import java.util.Objects;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

/**
 * Represents an A/B testing property (AB prop) definition with its configuration code and default values.
 *
 * <p>AB props are feature flags and configuration values that WhatsApp uses to control client behavior,
 * enable/disable features, and conduct A/B testing experiments. Each prop definition consists of:
 * <ul>
 * <li>A numeric {@code code} that uniquely identifies the property
 * <li>A {@code defaultValue} string used when the server has not sent a value for this prop
 * <li>A {@code debugDefaultValue} string used in place of {@code defaultValue} when the user
 *     has joined the WhatsApp Web Beta programme
 * </ul>
 *
 * <p>Both default values are always strings, matching the format in which values are received from
 * the server. Static conversion methods are provided to parse the string into typed values
 * (boolean, int, long, double).
 *
 * <p>This record is immutable and thread-safe.
 *
 * @param code              the unique numeric identifier for this configuration property
 * @param defaultValue      the production default value to use when the server has not provided
 *                          a value for this property, must not be {@code null}
 * @param debugDefaultValue the debug/beta default value used when the user has joined the
 *                          WhatsApp Web Beta programme, must not be {@code null}
 */
@WhatsAppWebModule(moduleName = "WAWebABPropsConfigs")
public record ABProp(int code, String defaultValue, String debugDefaultValue) {

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp STATUS_VIDEO_MAX_DURATION = new ABProp(175, "30", "30");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp QPL_ENABLED = new ABProp(212, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp QPL_UPLOAD_DELAY = new ABProp(215, "1440", "1");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp UPLOAD_STATUS_THUMB_MMS_ENABLED = new ABProp(246, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp UPLOAD_DOCUMENT_THUMB_MMS_ENABLED = new ABProp(247, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DOWNLOAD_STATUS_THUMB_MMS_ENABLED = new ABProp(249, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DOWNLOAD_DOCUMENT_THUMB_MMS_ENABLED = new ABProp(250, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SEND_STATUS_THUMB_IN_MESSAGE_DISABLED = new ABProp(252, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SEND_DOCUMENT_THUMB_IN_MESSAGE_DISABLED = new ABProp(253, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SEND_LINK_THUMB_IN_MESSAGE_DISABLED = new ABProp(254, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GROUPS_DOGFOODING_UI = new ABProp(308, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MD_ICDC_ENABLED = new ABProp(309, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MD_ICDC_HASH_LENGTH = new ABProp(310, "10", "10");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp STOP_ABPROPS_TRAFFIC_IN_SERVERPROPS_RESPONSE = new ABProp(315, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PLAYED_SELF_ENABLED = new ABProp(361, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp IN_APP_SUPPORT_V2_NUMBERS = new ABProp(390, "", "");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp EPHEMERAL_24H_DURATION = new ABProp(407, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp EPHEMERAL_ALLOW_GROUP_MEMBERS = new ABProp(432, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_COLLECTIONS_ENABLED = new ABProp(451, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_BIZ_ACTIVITY_REPORT_REQUEST = new ABProp(455, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PLM_PRODUCTS_MAX_BATCH_FETCH_SIZE = new ABProp(464, "18", "18");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp QPL_SAMPLING_AS_STRING = new ABProp(466, "json:{\"sampling\":[]}", "json:{\"sampling\":[]}");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BUSINESS_PROFILE_REFRESH_M1_ENABLED = new ABProp(470, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp JOINABLE_CLIENT_POLL_INTERVAL_MIN = new ABProp(522, "0", "5");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MESSAGE_LEVEL_REPORTING = new ABProp(535, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DISAPPEARING_MODE = new ABProp(536, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_GRANULAR_REJECT_REASONS = new ABProp(550, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GROUP_CATCH_UP = new ABProp(559, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CONSUMER_COLLECTIONS_ENABLED = new ABProp(582, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_ABPROP_GROUP_DESCRIPTION_LENGTH = new ABProp(592, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_ABPROP_EPHEMERAL_MESSAGES_ALLOWED_VALUES = new ABProp(593, "604800", "604800");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_ABPROP_DROP_FULL_HISTORY_SYNC = new ABProp(600, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ELEVATING_PROFILE_NAMES_ENABLED = new ABProp(604, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PAYMENTS_EXPRESSIVE_BACKGROUNDS_ENABLED = new ABProp(605, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CLIENT_GROUP_PARTICIPANTS_LIMIT = new ABProp(618, "257", "257");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp NATIVE_SHOP_PREVIEW_ENABLED = new ABProp(636, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PTT_CONVERSATION_WAVEFORM = new ABProp(637, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_GROUP_PROFILE_EDITOR = new ABProp(689, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CSAT_MESSAGE_RATING = new ABProp(690, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_COLLECTIONS_APPEAL_FLOW_ENABLED = new ABProp(724, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DROP_LAST_NAME = new ABProp(726, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp NUM_DAYS_KEY_INDEX_LIST_EXPIRATION = new ABProp(730, "35", "35");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp NUM_DAYS_BEFORE_DEVICE_EXPIRY_CHECK = new ABProp(731, "7", "7");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SYNC_ARCHIVE_V2_SETTING = new ABProp(736, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_ABPROP_COLLECTIONS_NUX_BANNER = new ABProp(741, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PTT_WAVEFORM_SEND = new ABProp(746, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ADV_V2_M4_M5 = new ABProp(753, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp NFM_RENDERING_ENABLED = new ABProp(760, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_ABPROP_BUSINESS_PROFILE_REFRESH_LINKED_ACCOUNT_ENABLED = new ABProp(764, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PTT_DRAFT_ENABLED = new ABProp(777, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp TOS_3_CLIENT_GATING_ENABLED = new ABProp(791, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp TRUSTED_CONTACTS = new ABProp(794, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PRIVACY_ALLOW_CONTACTS_EXCEPT = new ABProp(808, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp FACEBOOK_LINK_PREVIEW_USE_THUMBNAIL = new ABProp(810, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PAYMENT_STICKERS_RENDER_ENABLED = new ABProp(812, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp IN_APP_SUPPORT_V2_JUMP_TO_GROUP = new ABProp(819, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp REACTIONS_RECEIVE = new ABProp(827, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp REACTIONS_SEND = new ABProp(828, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp TAM_ATTACHMENT_CACHE_COMPACTION_ENABLED = new ABProp(838, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BUSINESS_THREADS_LOGGING_ENABLED = new ABProp(853, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MD_MIGRATION_EXPERIENCE = new ABProp(861, "2", "2");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp STICKER_MD_FAVORITE_STICKERS_ENABLED = new ABProp(864, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp TCTOKEN_DURATION = new ABProp(865, "604800", "604800");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_ABPROP_DIRECT_CONNECTION_MD = new ABProp(869, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PTT_PAUSABLE_ENABLED = new ABProp(871, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_STATUS_PSA = new ABProp(873, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_QUICK_REPLIES_V2_ENABLED = new ABProp(875, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp TOS_CLIENT_STATE_FETCH_ENABLED = new ABProp(877, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_ABPROP_BLOCK_CATALOG_CREATION_ECOMMERCE_COMPLIANCE_INDIA = new ABProp(894, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ADV_V2_M6 = new ABProp(903, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PRIVATE_STATS_BIZ_VIEW_LOGGING_ENABLED = new ABProp(904, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MEDIA_UPLOAD_PREKEYS_FETCH_ENABLED = new ABProp(907, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp TOS_CLIENT_STATE_FETCH_ITERATION = new ABProp(908, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp TCTOKEN_NUM_BUCKETS = new ABProp(909, "4", "4");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp TRUSTED_CONTACTS_TI = new ABProp(922, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_STICKER_STORE = new ABProp(930, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PTT_REMEMBER_PLAY_POSITION = new ABProp(952, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BANNED_SHOPS_UX_ENABLED = new ABProp(957, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp IN_APP_SUPPORT_V2_JUMP_TO_GROUP_WAIT_TIME_IN_MS = new ABProp(974, "5000", "5000");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_TOS_FILTERING_ENABLED = new ABProp(976, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PARENT_GROUP_VIEW_ENABLED = new ABProp(982, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_ORCHESTRATOR_ENABLED_VERSION = new ABProp(984, "bucket", "bucket");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp REACTION_CLEANUP_DAYS = new ABProp(987, "31", "31");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp TRUSTED_CONTACTS_SENDER = new ABProp(995, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp TCTOKEN_DURATION_SENDER = new ABProp(996, "604800", "604800");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp TCTOKEN_NUM_BUCKETS_SENDER = new ABProp(997, "4", "4");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_ECOMMERCE_COMPLIANCE_INDIA_M4 = new ABProp(1003, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp NO_DELETE_MESSAGE_TIME_LIMIT = new ABProp(1011, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMART_FILTERS_ENABLED = new ABProp(1015, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ADMIN_HFM_TOGGLE = new ABProp(1021, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BTM_THREADS_LOGGING_ENABLED = new ABProp(1022, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp IN_APP_SUPPORT_V2_NUMBER_PREFIXES = new ABProp(1031, "15517868", "15517868");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp NATIVE_COMMERCE_THREADS_LOGGING_ENABLED = new ABProp(1034, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SYSTEM_MSG_NUMBERS_FB_BRANDED = new ABProp(1035, "16325551023,16505434800,16503130062,16507885324,16508620604,16504228206,447710173736,16315551023,16505361212,16508129150,16315555102,16315558723,16505212669,16507885280,19032707825,0", "16325551023,16505434800,16503130062,16507885324,16508620604,16504228206,447710173736,16315551023,16505361212,16508129150,16315555102,16315558723,16505212669,16507885280,19032707825,0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SYSTEM_MSG_NUMBERS_FB_INC = new ABProp(1036, "", "");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp FORWARDED_PTT_UI_ENABLED = new ABProp(1040, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_SHOP_STOREFRONT_MESSAGE = new ABProp(1053, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PRIMARY_FEATURE_SYNC = new ABProp(1063, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DEV_PROP_STRING = new ABProp(1064, "", "");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DEV_PROP_BOOLEAN = new ABProp(1065, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DEV_PROP_INT = new ABProp(1066, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DEV_PROP_FLOAT = new ABProp(1067, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PRIVACY_NARRATIVE_V1 = new ABProp(1071, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_MULTI_DEVICE_AWARENESS = new ABProp(1074, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CSAT_MESSAGE_TRIGGER = new ABProp(1082, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GRAPHQL_PRIVACY_IMP_M1 = new ABProp(1096, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MEDIA_REUPLOAD_LIMIT_MB = new ABProp(1098, "100", "100");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_SEND_INVISIBLE_MSG_TO_NEW_GROUPS = new ABProp(1099, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_SEND_INVISIBLE_MSG_MIN_GROUP_SIZE = new ABProp(1100, "128", "128");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp LTHASH_CHECK_HOURS = new ABProp(1104, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp COUNTRY_CLIENT_GATING_ENABLED = new ABProp(1105, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ORDER_DETAILS_FROM_CART_ENABLED = new ABProp(1107, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp INTERACTIVE_MESSAGE_NATIVE_FLOW_KILLSWITCH = new ABProp(1133, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MESSAGE_COUNT_LOGGING_MD_ENABLED = new ABProp(1135, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp THREADS_LOGGING_OBSERVE_LIST_ENABLED = new ABProp(1168, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_INIT_CHAT_BATCH_SIZE = new ABProp(1171, "100", "100");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_INIT_CHAT_MAX_UNREAD_MESSAGE_COUNT = new ABProp(1172, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ORDER_DETAILS_CUSTOM_ITEM_ENABLED = new ABProp(1176, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ADMIN_REVOKE_RECEIVER = new ABProp(1177, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp REACTION_HISTORY_SYNC = new ABProp(1179, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SENDER_KEY_EXPIRED_LOGGING_ENABLED = new ABProp(1185, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ORDER_MANAGEMENT_ENABLED = new ABProp(1188, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp LOG_CLOCK_SKEW = new ABProp(1190, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_ECOMMERCE_COMPLIANCE_INDIA_M4_5 = new ABProp(1192, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_HIDE_UNSUPPORTED_CURRENCY_PRICE = new ABProp(1203, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GROWTH_LOCK_V0_ENABLED = new ABProp(1204, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ORDER_DETAILS_FROM_CATALOG_ENABLED = new ABProp(1212, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AUDIO_LEVEL_SPEAKING_THRESHOLD = new ABProp(1213, "30", "50");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp HYPERLINKED_PHONE_NUMBERS_ENABLED = new ABProp(1215, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MD_APP_STATE_REPORT_MD_SYNC_MUTATION_STATS = new ABProp(1221, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp QPL_INITIAL_UPLOAD_DELAY = new ABProp(1223, "5", "1");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_CATKIT_QUERY_VERSION = new ABProp(1229, "1", "1");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PARENT_GROUP_LINK_LIMIT = new ABProp(1238, "100", "100");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_THREADS_LOGGING_ENABLED = new ABProp(1251, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_CLICK_TO_CHAT_LOGGING_ENABLED = new ABProp(1252, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_BROADCAST_LOGGING_ENABLED = new ABProp(1253, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_STATUS_LOGGING_ENABLED = new ABProp(1254, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_BIZ_PROFILE_LOGGING_ENABLED = new ABProp(1255, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_REGISTRATION_FLOW_LOGGING_ENABLED = new ABProp(1256, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BTM_QPL_ENABLED = new ABProp(1272, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMART_FILTERS_ENABLED_CONSUMER = new ABProp(1287, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ADMIN_REVOKE_SENDER = new ABProp(1292, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GROUP_SIZE_LIMIT = new ABProp(1304, "257", "257");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ADD_DM_TO_CHAT_OVERFLOW_MENU = new ABProp(1309, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp COMMERCE_SANCTIONED = new ABProp(1319, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp COMMERCE_BLOKS_APPS_MAPPING = new ABProp(1320, "{\"address_message\":{\"app_id\":\"com.bloks.www.whatsapp.commerce.address_message\",\"expiration_secs\":300,\"version\":\"1.5\",\"supported_businesses\":[\"+918591749310\",\"+917977079770\",\"+12165552716\",\"+918591749310\",\"+917977079770\",\"+919324433533\",\"+917669800185\",\"+919355081749\",\"+917217010106\",\"+912248913727\",\"+912068135414\",\"+918368818019\",\"+917827971992\",\"+917827971988\",\"+911244632002\",\"+919999006542\",\"+917982465931\",\"+911244632030\",\"+918920528558\",\"+911244632026\",\"+918920530301\",\"+15550083895\",\"+12995550004\",\"+6589523673\",\"+6597685939\",\"+6580536071\",\"+6531631404\",\"+6590834813\",\"+6588867112\",\"+16615555837\",\"+12765985268\",\"+18055908026\"]},\"galaxy_message\":{\"flow_message_version\":{\"1\":{\"min_android_app_supported_version\":\"2.22.21\",\"min_ios_app_supported_version\":\"2.22.16\"}},\"app_id\":\"com.bloks.www.whatsapp.commerce.galaxy_message\",\"expiration_secs\":86400,\"version\":\"1.0\",\"flows\":{}}}", "{\"address_message\":{\"app_id\":\"com.bloks.www.whatsapp.commerce.address_message\",\"expiration_secs\":300,\"version\":\"1.5\",\"supported_businesses\":[\"+918591749310\",\"+917977079770\",\"+12165552716\",\"+918591749310\",\"+917977079770\",\"+919324433533\",\"+917669800185\",\"+919355081749\",\"+917217010106\",\"+912248913727\",\"+912068135414\",\"+918368818019\",\"+917827971992\",\"+917827971988\",\"+911244632002\",\"+919999006542\",\"+917982465931\",\"+911244632030\",\"+918920528558\",\"+911244632026\",\"+918920530301\",\"+15550083895\",\"+12995550004\",\"+6589523673\",\"+6597685939\",\"+6580536071\",\"+6531631404\",\"+6590834813\",\"+6588867112\",\"+16615555837\",\"+12765985268\",\"+18055908026\"]},\"galaxy_message\":{\"flow_message_version\":{\"1\":{\"min_android_app_supported_version\":\"2.22.21\",\"min_ios_app_supported_version\":\"2.22.16\"}},\"app_id\":\"com.bloks.www.whatsapp.commerce.galaxy_message\",\"expiration_secs\":86400,\"version\":\"1.0\",\"flows\":{}}}");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MORE_REACTIONS_OPTION = new ABProp(1322, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GRAPHQL_PRIVACY_IMP_M2 = new ABProp(1327, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SENDER_REVOKE_WINDOW_SENDER = new ABProp(1333, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SENDER_REVOKE_WINDOW_RECEIVER = new ABProp(1334, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SENDER_REVOKE_UI = new ABProp(1335, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp NUX_SYNC = new ABProp(1343, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_ABPROP_BUSINESS_PROFILE_REFRESH_LINKED_ACCOUNTS_KILLSWITCH = new ABProp(1351, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp KEEP_IN_CHAT_RECEIVER = new ABProp(1352, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp KEEP_IN_CHAT_SENDER = new ABProp(1353, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp REACTIONS_ANIMATIONS = new ABProp(1361, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp COMPANION_MIN_VERSIONS = new ABProp(1367, "json:[]", "json:[]");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CALL_LINK_VERSION = new ABProp(1372, "0", "1");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_PREKEYS_FETCH_FIRST_BATCH_SIZE = new ABProp(1373, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp IN_APP_SURVEY_ENABLED = new ABProp(1377, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MD_APP_STATE_GATE_D34336913 = new ABProp(1379, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp POLL_CREATION_ENABLED = new ABProp(1394, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp POLL_RECEIVING_ENABLED = new ABProp(1395, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DDM_REVERSED_OPTIONS = new ABProp(1397, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SYNCD_PERIODIC_SYNC_DAYS = new ABProp(1400, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp POLL_NAME_LENGTH = new ABProp(1406, "255", "255");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp POLL_OPTION_LENGTH = new ABProp(1407, "100", "100");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp POLL_OPTION_COUNT = new ABProp(1408, "12", "12");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp POLL_OFFLINE_ACCURACY = new ABProp(1409, "30", "30");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp POLL_CLEANUP_DAYS = new ABProp(1410, "31", "31");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GROUP_SUSPEND_V1_ENABLED = new ABProp(1415, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_PRODUCT_PRICE_LABEL = new ABProp(1417, "control", "control");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PNH_HISTORICAL_MAPPING_RETENTION_SECONDS = new ABProp(1429, "7776000", "7776000");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp HEARTBEAT_INTERVAL_S = new ABProp(1430, "10", "5");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp INTERACTIVE_RESPONSE_MESSAGE_KILLSWITCH = new ABProp(1435, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp INTERACTIVE_RESPONSE_MESSAGE_NATIVE_FLOW_KILLSWITCH = new ABProp(1436, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp TRUSTED_CONTACTS_RECIPROCITY = new ABProp(1437, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_MULTI_DEVICE_AGENTS_ENABLED = new ABProp(1438, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp STATUS_QUICK_REPLY_ENABLED = new ABProp(1455, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BIZ_API_VOIP_ENABLED = new ABProp(1464, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_ORANGE_ENABLED = new ABProp(1469, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp QUANTITY_CONTROLS_ENABLED = new ABProp(1480, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_MELON_DISPLAY_ENABLED = new ABProp(1483, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_MELON_MANAGEMENT_ENABLED = new ABProp(1484, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp REACTIONS_ANIMATIONS_SIMPLE = new ABProp(1485, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_CTWA_ADS_ACTION_BANNER_ENABLED = new ABProp(1495, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_SYNCD_MAX_MUTATIONS_TO_PROCESS_DURING_RESUME = new ABProp(1513, "1000", "1000");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CATALOG_CATEGORIES_ENABLED = new ABProp(1514, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MD_OFFLINE_V2_M2_ENABLED = new ABProp(1517, "10", "10");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DISAPPEARING_MESSAGES_CHAT_PICKER = new ABProp(1518, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp STATUS_INLINE_LINK_PREVIEW_ENABLED = new ABProp(1522, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CALL_ONLY_PRIMARY_DEVICE_LIMIT_EXCEEDED = new ABProp(1525, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SILENT_GROUP_EXIT = new ABProp(1527, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SILENT_GROUP_EXIT_PAST_PARTICIPANTS = new ABProp(1528, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PROFILE_PHOTO_RINGS_FOR_STATUS_ENABLED = new ABProp(1533, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp POLL_VOTE_PROCESSING_ENABLED = new ABProp(1541, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp LOBBY_TIMEOUT_MIN = new ABProp(1565, "0", "1");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp TRUSTED_CONTACTS_CHAT_STATE_OPTIMIZATION = new ABProp(1566, "old", "old");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp IS_META_EMPLOYEE = new ABProp(1570, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_BILLING_ENABLED = new ABProp(1583, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_PREMIUM_MD_LIMIT_PERF_TRACKER_ENABLED = new ABProp(1591, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ORDER_DETAILS_PAYMENT_INSTRUCTIONS_ENABLED = new ABProp(1595, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SILENT_GROUP_EXIT_DIALOG = new ABProp(1597, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SILENT_GROUP_EXIT_SYNC = new ABProp(1598, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp INCENTIVE_PROGRAM_LOGGING_ENABLED = new ABProp(1599, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ORDER_DETAILS_QUICK_PAY = new ABProp(1600, "{\"allowed_product_type\":\"none\"}", "{\"allowed_product_type\":\"none\"}");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp REACTIONS_CHAT_PREVIEW = new ABProp(1605, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp IN_APP_SURVEY_PHONE_NUMBERS = new ABProp(1607, "16508638904,52226802372654", "16508638904,52226802372654");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHATLIST_FILTERS_V1 = new ABProp(1608, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MD_SYNCD_24_HOUR_TIME_FORMAT_SYNC_ENABLED = new ABProp(1612, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SILENT_GROUP_EXIT_DB = new ABProp(1613, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_BILLING_PREMIUM_ACCESS_CONFIG = new ABProp(1619, "", "");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MESSAGE_QUICK_REPLY = new ABProp(1623, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_PUSH_NOTIFICATIONS = new ABProp(1643, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp QM_LEAN_MSG = new ABProp(1645, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp COMMUNITY_SUSPEND_V0_ENABLED = new ABProp(1653, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PARENT_GROUP_ADMINS_LIMIT = new ABProp(1655, "20", "20");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_QUANTITY_CONTROLS_ENABLED = new ABProp(1659, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SEND_CART_CTA_LONG_BUTTON_ENABLED = new ABProp(1660, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_MELON_LOGGING_ENABLED = new ABProp(1669, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DM_UPDATED_SYSTEM_MESSAGE = new ABProp(1670, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_MULTI_DEVICE_AGENTS_LOGGING_ENABLED = new ABProp(1671, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_BILLING_LOGGING_ENABLED = new ABProp(1672, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp KIC_ORPHAN_CLEANUP_DAYS = new ABProp(1673, "31", "31");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PRODUCT_SEARCH_M1_ENABLED = new ABProp(1678, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_ABPROP_SCREEN_LOCK_ENABLED = new ABProp(1680, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_CTWA_LOG_USER_JOURNEY_ENABLED = new ABProp(1681, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ORDER_DETAILS_TOTAL_MINIMUM_VALUE = new ABProp(1683, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ORDER_DETAILS_TOTAL_MAXIMUM_VALUE = new ABProp(1684, "500000000", "500000000");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp TRUSTED_CONTACTS_OP = new ABProp(1687, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_CATALOG_COLLECTIONS_REORDERING_ENABLED = new ABProp(1688, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp COMMERCE_METADATA_SUPPORTED_BUSINESS = new ABProp(1693, "18785550326,918591749310,917977079770,12245555037,5515997781156,5511989238421,555191894444,905333860133,908502213040,5511916282555,551147664020,622150851766,551121038525", "18785550326,447766028329,918591749310,917977079770,12245555037,5515997781156,5511989238421,555191894444,905333860133,908502213040,5511916282555,551147664020,622150851766");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp KEEP_IN_CHAT_UNDO_DURATION_LIMIT = new ABProp(1698, "2592000", "2592000");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_DCP_ENABLED = new ABProp(1701, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp IS_MESSAGE_SECRET_ENABLED = new ABProp(1707, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp VIEW_ONCE_SP_RECEIVER = new ABProp(1710, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp VIEW_ONCE_SP_SENDER = new ABProp(1711, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ORDER_DETAILS_TOTAL_ORDER_MINIMUM_VALUE = new ABProp(1719, "1", "1");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_COMMAND_PALETTE = new ABProp(1726, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GROUP_JOIN_REQUEST_M1 = new ABProp(1727, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GROUP_JOIN_REQUEST_M2 = new ABProp(1728, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_GROUP_PROFILE_EDITOR = new ABProp(1745, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DOCUMENTS_WITH_CAPTIONS_RECEIVE = new ABProp(1749, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DOCUMENTS_WITH_CAPTIONS_SEND = new ABProp(1750, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_ABPROP_CORE_WAM_RUNTIME = new ABProp(1753, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp EXTERNAL_PAYMENTS_SUPPORTED_BUSINESS = new ABProp(1763, "+917000770007", "+918369150604,+917000770007");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ORDER_DETAILS_PAYMENT_OPTIONS = new ABProp(1767, "{\"payment_options\":[{\"type\":\"JioPay\",\"url_regex_list\":[\"^https://www.jio.com/.*$\",\"^https://t.jio/.*$\",\"^http://tiny.jio.com/.*$\"],\"title\":{\"name\":\"jiopay_title\",\"default_text\":\"Pay on Jio.com\"},\"subtitle\":{\"name\":\"jiopay_subtitle\",\"default_text\":\"Go to Jio.com website\"},\"button\":{\"name\":\"jiopay_button\",\"default_text\":\"Proceed to Jio.com\"}}]}", "{\"payment_options\":[{\"type\":\"JioPay\",\"url_regex_list\":[\"^https://www.jio.com/.*$\",\"^https://t.jio/.*$\",\"^http://tiny.jio.com/.*$\"],\"title\":{\"name\":\"jiopay_title\",\"default_text\":\"Pay on Jio.com\"},\"subtitle\":{\"name\":\"jiopay_subtitle\",\"default_text\":\"Go to Jio.com website\"},\"button\":{\"name\":\"jiopay_button\",\"default_text\":\"Proceed to Jio.com\"}}]}");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_PHASE_OUT_NOT_A_BUSINESS_V2 = new ABProp(1771, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_OFFLINE_RESUME_QPL_ENABLED = new ABProp(1773, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp IS_META_EMPLOYEE_OR_INTERNAL_TESTER = new ABProp(1777, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SYNCD_LTHASH_CONSISTENCY_CHECK_ON_SNAPSHOT_MAC_MISMATCH = new ABProp(1783, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp REACTIONS_KEYBOARD_HIDES_THREE_FLAGS = new ABProp(1792, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_CATALOG_COLLECTION_ITEMS_REORDERING_ENABLED = new ABProp(1794, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_MD_AGENT_CHAT_ASSIGNMENT_ENABLED = new ABProp(1798, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_SYNCD_FATAL_FIELDS_FROM_L1104589PRV2 = new ABProp(1808, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_UNIFIED_FLOW = new ABProp(1809, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PNH_CTWA = new ABProp(1823, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GROUP_CHAT_PROFILE_PICTURES_ENABLED = new ABProp(1825, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp REACTIONS_PANEL_PREKEYS_FETCH_ENABLED = new ABProp(1828, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp RECENT_STICKER_ROLLOUT_PHASE = new ABProp(1829, "4", "4");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp REPORT_CALL_REPLAYER_ID = new ABProp(1834, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DISABLE_AUTO_DOWNLOAD = new ABProp(1838, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_DATA_MAX_LENGTH = new ABProp(1841, "768", "768");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_CLIENT_CHAT_PSA = new ABProp(1844, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DIRECT_CONNECTION_BUSINESS_NUMBERS = new ABProp(1846, "16005554444,918591749310,917977079770", "16005554444,918591749310,917977079770");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_CUSTOM_URL_DISPLAY_V2_ENABLED = new ABProp(1849, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_MULTI_SKIN_TONED_EMOJI_PICKER = new ABProp(1850, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp TEXT_STATUS_URL_LOGGING_ENABLED = new ABProp(1851, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp STATUS_REACTION_EMOJIS = new ABProp(1852, "[128525, 128514, 128558, 128546, 128591, 128079, 127881, 128175]", "[128525, 128514, 128558, 128546, 128591, 128079, 127881, 128175]");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp FORWARD_MEDIA_WITH_CAPTIONS = new ABProp(1853, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp STATUS_REPLY_RECEIVED_LOGGING_ENABLED = new ABProp(1859, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GROUP_SIZE_BYPASSING_SAMPLING = new ABProp(1861, "100000", "100000");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp COMMUNITY_ADMIN_PROMOTION_ONE_TIME_PROMPT = new ABProp(1864, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp REVOKES_LOGGING_UNSAMPLED = new ABProp(1865, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_CTWA_ACTION_BANNER_LOGGING_ENABLED = new ABProp(1866, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SHARE_PHONE_NUMBER_ON_CART_SEND_TO_DIRECT_CONNECTION_BIZ_ENABLED = new ABProp(1867, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp VOICE_STATUS_RECEIPT_ENABLED = new ABProp(1875, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GROUP_JOIN_REQUEST_M2_SETTING = new ABProp(1887, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp USYNC_LID = new ABProp(1892, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_MULTI_DEVICE_AGENTS_LOGGING_V2_ENABLED = new ABProp(1897, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_PTT_STREAMER_UPLOAD = new ABProp(1902, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_TEMP_COVER_PHOTO_PRIVACY_MESSAGING = new ABProp(1913, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ADMIN_INCLUDE_MESSAGE_SECRET_IN_CAG = new ABProp(1921, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_SEND_INVISIBLE_MSG_MAX_GROUP_SIZE = new ABProp(1945, "1024", "1024");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp POLL_RESULT_DETAILS_VIEW_ENABLED = new ABProp(1948, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SHOW_SHOPS_SUNSET_BANNER = new ABProp(1949, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PORTRAIT_THUMB_ENABLED_CHAT = new ABProp(1961, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp NOTE_TO_SELF = new ABProp(1967, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp STATUS_QUICK_REPLY_RECEIVER_CHANGES_ENABLED = new ABProp(1974, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_MULTI_DEVICE_MESSAGE_ATTRIBUTION_ENABLED = new ABProp(1981, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PARENT_GROUP_LINK_LIMIT_COMMUNITY_CREATION = new ABProp(1990, "10", "20");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MD_SYNCD_PRIMARY_VERSION_SYNC_ENABLED = new ABProp(1993, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp KEEP_IN_CHAT_UI_CONTENT = new ABProp(2005, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SYNCD_DO_NOT_FATAL_ON_SNAPSHOT_MAC_MISMATCH_IN_PATCHES = new ABProp(2007, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GRAPHQL_LOCALE_REMAPPING = new ABProp(2014, "{}", "{}");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_MESSAGE_LIST_A11Y_REDESIGN = new ABProp(2016, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_ENABLE_PROFILE_PIC_THUMB_DB_CACHING = new ABProp(2018, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp STATUS_CAPTION_LINK_DETECTION_ENABLED = new ABProp(2032, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CAG_REACTIONS_RECEIVE = new ABProp(2035, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CAG_REACTIONS_SEND = new ABProp(2036, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp STATUS_FROM_ME_UNSEEN_ENABLED = new ABProp(2039, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_ENABLE_BIZ_CATALOG_VIEW_PS_LOGGING = new ABProp(2056, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GROUP_SUSPEND_APPEAL_INCLUDE_ENTITY_ID_ENABLED = new ABProp(2057, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_ABPROP_MEDIA_LINKS_DOCS_SEARCH = new ABProp(2063, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp STATUS_VIEW_ERROR_TYPE_LOGGING_ENABLED = new ABProp(2086, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MMS_VCACHE_AGGREGATION_ENABLED = new ABProp(2134, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MMS_HOT_CONTENT_TIMESPAN_IN_SECONDS = new ABProp(2136, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SEND_CART_CTA_LONG_BUTTON_ALTERNATIVE_TEXT_TYPE = new ABProp(2153, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp FAVORITE_STICKER_RMR_SYNC_ENABLED = new ABProp(2155, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_LINK_PREVIEW_SYNC_ENABLED = new ABProp(2156, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_MD_AGENT_CHAT_ASSIGNMENT_SYSTEM_MESSAGES_ENABLED = new ABProp(2157, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_CTWA_BILLING_ENABLED = new ABProp(2158, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp VIDEO_STREAM_BUFFERING_UI_ENABLED = new ABProp(2167, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MESSAGE_EDIT_RECEIVE = new ABProp(2189, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MESSAGE_EDIT_SEND = new ABProp(2190, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PREKEY_FETCH_IQ_FOR_MISSING_DEVICES_ENABLED = new ABProp(2193, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp POLL_CREATION_ONE_ON_ONE_CHATS_ENABLED = new ABProp(2194, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PARENT_GROUP_VIEW_ENABLED_FOR_SMB_ON_WEB = new ABProp(2205, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PARENT_GROUP_CREATE_ENABLED_FOR_SMB_ON_WEB = new ABProp(2206, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_MD_AGENT_CHAT_ASSIGNMENT_NUX_IMPRESSIONS = new ABProp(2207, "0", "3");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MEX_PHASE3_ENABLED = new ABProp(2249, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MEX_PHASE3_STATUS_FLAGS = new ABProp(2250, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_MAX_CONTACTS_TO_SHOW_COMMON_GROUPS = new ABProp(2264, "10", "10");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_MAX_FOUND_COMMON_GROUPS_DISPLAYED = new ABProp(2268, "15", "15");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_MESSAGE_CUSTOM_ARIA_LABEL = new ABProp(2280, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GIF_AUTOPLAY_ENABLED = new ABProp(2281, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BLOCK_FROM_CHAT_LIST = new ABProp(2290, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp EXTENSIONS_MESSAGE_SUPPORT_VERSION = new ABProp(2306, "{\"1\":{\"min_android_app_supported_version\":\"2.22.21\"},\"2\":{\"min_android_app_supported_version\":\"2.22.23.11\",\"min_ios_app_supported_version\":\"2.23.18.15\"},\"3\":{\"min_android_app_supported_version\":\"2.23.17.10\",\"min_ios_app_supported_version\":\"2.23.18.15\"}}", "{\"1\":{\"min_android_app_supported_version\":\"2.22.21\"},\"2\":{\"min_android_app_supported_version\":\"2.22.23\",\"min_ios_app_supported_version\":\"2.23.18.15\"},\"3\":{\"min_android_app_supported_version\":\"2.23.17\",\"min_ios_app_supported_version\":\"2.23.18.15\"}}");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DOCUMENT_PREVIEW_CAPTION_CHANGES_ENABLED = new ABProp(2307, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp COEX_BIZ_STATES_SYS_MSG_ENABLED = new ABProp(2320, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp V_ID_DEPRECATION_ENABLED = new ABProp(2334, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PNH_CAG_SHOW_MASKED_MEMBERS = new ABProp(2346, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PARENT_GROUP_CREATE_PRIVACY = new ABProp(2356, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GROUP_JOIN_REQUEST_M0_ANYONE_CAN_JOIN = new ABProp(2367, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GROUP_JOIN_REQUEST_M3 = new ABProp(2369, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BLOCK_FROM_NOTIFICATION = new ABProp(2374, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GROUP_JOIN_REQUEST_M2_PUSHNAME = new ABProp(2376, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp FOUR_REACTIONS_IN_BUBBLE_ENABLED = new ABProp(2378, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PARENT_GROUP_MIN_PARTICIPANTS_FOR_GROUP_ENTRY_POINT = new ABProp(2382, "20", "1");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp POLL_A11Y_ENABLED = new ABProp(2390, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp URL_SEND_RECEIVE_LOGGING_ENABLED = new ABProp(2430, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp INLINE_VIDEO_PLAYBACK_ADDITIONAL_LOGGING_ENABLED = new ABProp(2431, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PNH_CAG_FUTURE_PROOF_BANNER = new ABProp(2433, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PARENT_GROUP_TAP_TO_REQUEST_ENABLED = new ABProp(2436, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PARENT_GROUP_TAP_TO_ADD_ENABLED = new ABProp(2446, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PARENT_GROUP_NO_DISCLAIMER = new ABProp(2447, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GROUP_JOIN_REQUEST_M2_BANNER_ON_CONVERSATION = new ABProp(2449, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PNH_SPLIT_THREADS_DETECTION = new ABProp(2479, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PNH_GROUP_LID = new ABProp(2507, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_NON_BLOCKING_OFFLINE_RESUME_MAX_MESSAGE_COUNT = new ABProp(2508, "1000", "1000");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CAG_MEMBER_KEY_ROTATION_OPTIMIZATION = new ABProp(2521, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BLOCK_ENTRY_POINT_LOGGING_ENABLED = new ABProp(2522, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ELEVATED_PUSH_NAMES_V2_ENABLED = new ABProp(2540, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp NEW_END_CALL_SURVEY_POP_UP_USER_INTERVAL_S = new ABProp(2553, "-1", "-1");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp OUT_OF_SYNC_DISAPPEARING_MESSAGES_LOGGING = new ABProp(2561, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp LINK_PREVIEW_WAIT_TIME = new ABProp(2566, "7", "7");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp NON_MESSAGE_DATA_REQUEST_LOGGING_ENABLED = new ABProp(2573, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PROJECT_PDF_ENABLED = new ABProp(2575, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_BIZ_PROFILE_CUSTOM_URL = new ABProp(2582, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_BIZ_PROFILE_CUSTOM_URL_NOTIFICATIONS = new ABProp(2583, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_INIT_BWE_FOR_GROUP_CALL = new ABProp(2601, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MEDIA_PICKER_SELECT_LIMIT = new ABProp(2614, "30", "30");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_SCREEN_LOCK_MAX_RETRIES = new ABProp(2622, "10", "10");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp NOTE_TO_SELF_ENTRY_POINT = new ABProp(2630, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PLACEHOLDER_MESSAGE_KEY_HASH_LOGGING = new ABProp(2639, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp VID_STREAM_PAUSE_RESUME_JB_RESET_THRESHOLD_MS = new ABProp(2642, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp POLLS_FAST_FOLLOW_ENABLED = new ABProp(2661, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp POLLS_SEARCH_SUPPORT_ENABLED = new ABProp(2662, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp POLLS_REPLY_SUPPORT_ENABLED = new ABProp(2663, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MEDIA_PICKER_SELECT_LIMIT_NEW = new ABProp(2693, "30", "30");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_MD_AGENT_CHAT_ASSIGNMENT_SYSTEM_MESSAGES_LOGGING_V2_ENABLED = new ABProp(2709, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp EPHEMERAL_SYNC_RESPONSE = new ABProp(2714, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_DISPLAY_NAME_FOR_ENTERPRISE_BIZ_VLEVEL_LOW_KILLSWITCH = new ABProp(2715, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_DISPLAY_NAME_FOR_BIZ_VLEVEL_LOW_KILLSWITCH = new ABProp(2716, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp POLL_CHATLIST_PREVIEW_ENABLED = new ABProp(2720, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_STATUS_REPORTING = new ABProp(2728, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MD_LINK_DEVICE_WITH_PHONE_NUMBER_ENABLED = new ABProp(2734, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp POLL_RECEIVING_CAG_ENABLED = new ABProp(2737, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp POLL_CREATION_CAG_ENABLED = new ABProp(2738, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GROUP_INVITE_NEW_BOTTOM_SHEET_ENABLED = new ABProp(2749, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PNH_IDENTITY_VERIFICATION_V3 = new ABProp(2751, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PROACTIVE_DISTRIBUTE_SENDER_KEYS_ENABLED = new ABProp(2757, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ELEVATED_PUSH_NAMES_V2_M1_FOLLOW_UP_ENABLED = new ABProp(2763, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp COMMUNITY_ANNOUNCEMENT_GROUP_SIZE_LIMIT = new ABProp(2774, "5000", "5000");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp FULLSCREEN_ANIMATION_FOR_KEYWORD = new ABProp(2776, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SYNCD_ADDITIONAL_MUTATIONS_COUNT = new ABProp(2777, "1", "1");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_MD_AGENT_CHAT_ASSIGNMENT_SYSTEM_MESSAGES_CHATS_REORDER_ENABLED = new ABProp(2778, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_MD_AGENT_CHAT_ASSIGNMENT_CHATS_REORDER_ON_CHAT_ASSIGNMENT_ENABLED = new ABProp(2787, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_MD_AGENT_CHAT_ASSIGNMENT_CHATS_REORDER_ON_CHAT_UNASSIGNMENT_ENABLED = new ABProp(2788, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_MESSAGE_PLUGIN_FRONTEND_REGISTRATION_ENABLED = new ABProp(2793, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp USE_APPDATA_STANZA_ON_RECEIVER = new ABProp(2795, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp USE_APPDATA_STANZA_ON_SENDER = new ABProp(2796, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SETTINGS_SEARCH = new ABProp(2800, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_SOOX_MESSAGE_RECEIVING = new ABProp(2802, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_LAZY_PULL = new ABProp(2814, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_SOOX_MESSAGE_SENDING = new ABProp(2832, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SUPPORTS_KEEP_IN_CHAT_IN_CAG = new ABProp(2844, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp UNIFY_END_CALL_EVENTS = new ABProp(2856, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MINIMUM_PERCENTAGE_TO_PROACTIVE_DISTRIBUTE_SENDER_KEYS = new ABProp(2860, "200", "50");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp INAPP_BANNER_CLIENT_ENABLED = new ABProp(2871, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp EXTENSIONS_TEMPLATE_KILLSWITCH = new ABProp(2885, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PTT_TRANSCRIPTION_ENABLED = new ABProp(2890, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BIZ_EXTENSIONS_METADATA_CACHE_TTL_MINUTES = new ABProp(2891, "1440", "1440");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BIZ_EXTENSIONS_METADATA_BAN_TTL_MINUTES = new ABProp(2892, "525600", "525600");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp UTM_TRACKING_ENABLED = new ABProp(2895, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp UTM_TRACKING_EXPIRATION_HOURS = new ABProp(2896, "24", "24");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_CTWA_WEB_THREAD_AD_ATTRIBUTION_ENABLED = new ABProp(2898, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ELEVATED_PUSH_NAMES_V2_M2_ENABLED = new ABProp(2904, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_MD_AGENT_CHAT_ASSIGNMENT_NOTIFICATIONS_ENABLED = new ABProp(2908, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MPM_NFM_FORWARDING_ENABLED = new ABProp(2909, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CALL_ADMIN_VERSION = new ABProp(2912, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GROUP_JOIN_REQUEST_M2_LOGGING = new ABProp(2913, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ATTACHMENT_TRAY_LOGGING_ENABLED = new ABProp(2914, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MAXIMUM_GROUP_SIZE_FOR_RCAT = new ABProp(2915, "100", "100");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_SMB_DATA_SHARING_CONSENT = new ABProp(2934, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_CONSUMER_DATA_SHARING_CONSENT = new ABProp(2935, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MARK_AS_ACTION = new ABProp(2936, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PNH_SPLIT_THREAD_CASE1_DETECTION = new ABProp(2939, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp IS_INTERNAL_TESTER = new ABProp(2945, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PNH_CAG_BLOCK_LID_IN_LIMBO = new ABProp(2962, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MESSAGE_EDIT_WINDOW_DURATION_SECONDS = new ABProp(2983, "1200", "1200");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp UGR_ENABLED = new ABProp(3010, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp UGC_ENABLED = new ABProp(3011, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ORDER_DETAILS_PAYMENT_PROTECTION_LINK = new ABProp(3014, "https://faq.whatsapp.com/725152392426717", "https://faq.whatsapp.com/725152392426717");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PAIRLESS_LOGGING_ATTRIBUTION_WINDOW = new ABProp(3017, "7", "7");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp COMMUNITY_GROUPS_NAVIGATION = new ABProp(3023, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_NATIVE_FETCH_MEDIA_DOWNLOAD = new ABProp(3031, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp REPORT_STRING_COMPREHENSION = new ABProp(3032, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_IMAGE_MAX_EDGE = new ABProp(3042, "1600", "1600");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_MARKETING_MESSAGES_ENABLED = new ABProp(3046, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp POLLS_SINGLE_OPTION_CONTROL_ENABLED = new ABProp(3050, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PAYMENTS_LINK_TO_LITE_CONSUMER_ENABLED = new ABProp(3051, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ALLOW_SUBGROUP_ADMIN_TO_UNLINK = new ABProp(3054, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_CTWA_WEB_ENTRYPOINT_HOME_HEADER_ENABLED = new ABProp(3058, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PNH_PN_FOR_LID_CHAT_SYNC = new ABProp(3062, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ORIGINAL_QUALITY_IMAGE_MIN_EDGE = new ABProp(3068, "2560", "2560");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SEND_CAG_MEMBER_REVOKES_AS_GDM = new ABProp(3069, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SHARE_OWN_PN_SYNC = new ABProp(3070, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DISABLE_STATUS_TO_NON_SUB = new ABProp(3077, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp COMMUNITY_SUBGROUP_ICON_VARIANT = new ABProp(3078, "0", "2");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PARENT_GROUP_REMOVE_ORPHANED_MEMBERS = new ABProp(3079, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp EXTERNAL_BETA_CAN_JOIN = new ABProp(3081, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp REWORD_SUBJECT_TO_GROUP_NAME_ENABLED = new ABProp(3088, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SECURITY_FIXES_BITMAP = new ABProp(3094, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_CTWA_WEB_ENTRYPOINT_HOME_HEADER_DROPDOWN_ENABLED = new ABProp(3095, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GROUP_MENTIONS_IN_CAG = new ABProp(3097, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PREKEY_FETCH_IQ_PNH_LID_ENABLED = new ABProp(3103, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_MARKETING_MESSAGES_PRODUCT_IDS = new ABProp(3113, "", "");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MEDIA_LARGE_FILE_AWARENESS_POPUP_FILE_SIZE_IN_MB = new ABProp(3115, "2048", "2048");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_RECEIVING_HD_PHOTO_QUALITY = new ABProp(3116, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp COMMUNITY_ANNOUNCEMENT_IMPROVEMENT_M1 = new ABProp(3121, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_RAMBUTAN_ENABLED = new ABProp(3124, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_RAMBUTAN_PRODUCT_IDS = new ABProp(3125, "", "");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ALT_DEVICE_LINKING_ENABLED = new ABProp(3128, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_STORE_QUOTA_MANAGER_ENABLED = new ABProp(3133, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_BROWSER_QUOTA_THRESHOLD = new ABProp(3134, "100", "100");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_BROWSER_MIN_STORAGE_QUOTA = new ABProp(3135, "5", "5");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_ORIGINAL_PHOTO_QUALITY_UPLOAD_ENABLED = new ABProp(3136, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PINNED_MESSAGES_M0 = new ABProp(3138, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PINNED_MESSAGES_M1_RECEIVER = new ABProp(3139, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PINNED_MESSAGES_M1_SENDER = new ABProp(3140, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PINNED_MESSAGES_M2 = new ABProp(3141, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PARENT_GROUP_SUBGROUP_FILTER = new ABProp(3147, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp NEWSLETTER_REPORTING_ENABLED = new ABProp(3148, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp NEWSLETTER_SUSPEND_ENABLED = new ABProp(3149, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_DEPRECATE_MMS4_HASH_BASED_DOWNLOAD = new ABProp(3152, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PARENT_GROUP_ENHANCED_DESCRIPTION_ENABLED = new ABProp(3154, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MUTE_DIALOG_DESCRIPTION = new ABProp(3155, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MUTE_ALWAYS_SHOW_NOTIFICATION_ACTION = new ABProp(3156, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp POLLS_NOTIFICATION_ENABLED = new ABProp(3158, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_BIZ_TOOL_LOGGING_IMPROVEMENT = new ABProp(3169, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CALLING_SCREEN_SHARE_VERSION = new ABProp(3171, "2", "2");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp APPEND_MESSAGE_WHEN_FORWARDING_MEDIA = new ABProp(3177, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GROUP_SUSPEND_V2_ENABLED = new ABProp(3180, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp EXPIRING_GROUPS_ENABLED = new ABProp(3181, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_CHAT_PSA_AUTO_PLAY_VIDEOS = new ABProp(3182, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DEFAULT_VIDEO_LIMIT_MB = new ABProp(3185, "16", "64");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp EXTENSIONS_GRAPHQL_CTA_DISABLE = new ABProp(3192, "2498088", "2498088");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp RECENT_EMOJIS_SYNC = new ABProp(3198, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_IMAGE_MAX_HD_EDGE = new ABProp(3204, "2560", "2560");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DEVICE_SWITCHING_ENABLED = new ABProp(3205, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ALLOW_NL_LINKPREVIEW = new ABProp(3209, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp EXTENSIONS_USER_REPORT_STORE_MAX_DATA_EXCHANGES_PER_SESSION = new ABProp(3211, "10", "10");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp EXTENSIONS_USER_REPORT_STORE_MAX_DATA_MAX_SESSIONS_PER_MESSAGE = new ABProp(3212, "3", "3");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ATTACH_MENU_REDESIGN_ENABLED = new ABProp(3223, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_E2E_BACKFILL_EXPIRE_TIME = new ABProp(3234, "5", "60");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_SILENT_OFFER = new ABProp(3235, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp COMMUNITY_ANNOUNCEMENT_IMPROVEMENT_M2 = new ABProp(3239, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ORDER_MESSAGES_EPHEMERAL_EXCEPTION_ENABLED = new ABProp(3240, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_CATALOG_MESSAGES_DOWNLOAD_THUMBNAIL_ON_RECEIVER_ENABLED = new ABProp(3247, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GROUP_CHAT_PROFILE_PICTURES_V2_ENABLED = new ABProp(3261, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PARENT_GROUP_HOME_HEADER_ACTIONS_ENABLED = new ABProp(3267, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MESSAGE_EDIT_CLIENT_ENTRY_POINT_LIMIT_SECONDS = new ABProp(3272, "900", "900");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AUTODOWNLOAD_UPDATE_IN_GROUP_CHAT = new ABProp(3273, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SEND_EXTENDED_NACK_ENABLED = new ABProp(3280, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_WALDO_SERVICE_OFFERINGS_SELECTION_ENABLED = new ABProp(3285, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_CTWA_WEB_FETCH_LINKED_ACCOUNTS_ENABLED = new ABProp(3294, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SYNCD_REPORT_KEY_STATS = new ABProp(3301, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DM_ADDITIONAL_DURATIONS = new ABProp(3305, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_DAYS_SINCE_RECEIVE_LOGGING = new ABProp(3322, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_SMB_DATA_SHARING_OPT_IN_COOL_OFF_PERIOD = new ABProp(3331, "259200", "259200");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp HISTORY_SYNC_ON_DEMAND = new ABProp(3337, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp HQP_LOG_ENABLED = new ABProp(3349, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WDS_RADIUS_AND_CASING = new ABProp(3350, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PTV_SENDING_ENABLED = new ABProp(3354, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PTV_RECEIVING_ENABLED = new ABProp(3355, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PTV_MAX_DURATION_SECONDS = new ABProp(3356, "60", "60");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CALLING_LID_VERSION = new ABProp(3358, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PERSISTED_PROFILE_NAME = new ABProp(3366, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_CTWA_WEB_ENTRYPOINT_MANAGE_ADS_HOME_HEADER_DROPDOWN_ENABLED = new ABProp(3376, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp COMMUNITY_ANNOUNCEMENT_IMPROVEMENT_M3 = new ABProp(3380, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GROUP_JOIN_REQUEST_CAN_VIEW_OPTIONAL_MESSAGE = new ABProp(3383, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GROUP_JOIN_REQUEST_CAN_SEND_OPTIONAL_MESSAGE = new ABProp(3384, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp NEWSLETTER_ENABLED = new ABProp(3385, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_EXPRESSION_PANELS = new ABProp(3420, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp POLLS_SINGLE_OPTION_SENDER_CONTROL_ENABLED = new ABProp(3433, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp POLLS_SINGLE_OPTION_RECIEVER_CONTROL_ENABLED = new ABProp(3434, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp POLLS_SINGLE_OPTION_RECEIVER_CONTROL_ENABLED = new ABProp(3437, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp TEMPLATE_BUTTON_IMPROVEMENTS_ON = new ABProp(3444, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GROUP_JOIN_REQUEST_M3_SORT_BY_TIME = new ABProp(3451, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GROUP_JOIN_REQUEST_M3_BANNER = new ABProp(3452, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PNH_IDENTITY_VERIFICATION_V3_PN_GENERATION = new ABProp(3458, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_PROJECT_WALDO_SET_PRICE_TIER_BIZ_PROFILE_ENABLED = new ABProp(3467, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PNH_1ON1_LID_EXPECTED = new ABProp(3469, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DF_ENABLED = new ABProp(3472, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PDF_AUTO_START_INTERVAL_SECONDS = new ABProp(3479, "86400", "30");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PNH_COMPANION_HISTORY_SYNC_LID_CHAT = new ABProp(3481, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PTV_AUTOPLAY_ENABLED = new ABProp(3482, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PTV_AUTOPLAY_LOOP_LIMIT = new ABProp(3483, "3", "3");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp NOYB_OPT_OUT_FLAG = new ABProp(3488, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ALLOW_LID_CONTACTS_STORAGE = new ABProp(3519, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp YOUTUBE_INLINE_PLAYBACK_KILLSWITCH = new ABProp(3522, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp QP_CAMPAIGN_CLIENT_ENABLED = new ABProp(3536, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp INBOX_MANAGEMENT_FILTERS_M2 = new ABProp(3554, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GROUP_JOIN_REQUEST_M3_INVITED_TAB = new ABProp(3571, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ANIMATED_EMOJIS_ENABLED = new ABProp(3575, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PLACEHOLDER_MESSAGE_RESEND = new ABProp(3579, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp RABBIT_ENABLED = new ABProp(3603, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MEX_NEWSLETTER_KILLSWITCH = new ABProp(3604, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MEX_NEWSLETTER_FLAGS = new ABProp(3605, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp NEWSLETTER_CREATION_ENABLED = new ABProp(3607, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PARENT_GROUP_INFO_UPDATES_ENABLED = new ABProp(3616, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp IS_COUPON_BUTTON_ENABLED = new ABProp(3630, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp COUPON_COPY_BUTTON_URL = new ABProp(3631, "https://www.whatsapp.com/coupon?code=", "https://www.whatsapp.com/coupon?code=");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PLACEHOLDER_MESSAGE_RESEND_MAXIMUM_DAYS_LIMIT = new ABProp(3639, "14", "14");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp HISTORY_SYNC_ON_DEMAND_TIME_BOUNDARY_DAYS = new ABProp(3642, "365", "365");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp LID_GROUPS_NEW_GROUP_CREATION = new ABProp(3645, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DEFAULT_GIF_LIMIT_MB = new ABProp(3656, "16", "64");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DEFAULT_AUDIO_LIMIT_MB = new ABProp(3657, "16", "64");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DEFAULT_STATUS_MEDIA_LIMIT_MB = new ABProp(3659, "16", "64");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DEFAULT_MEDIA_LIMIT_MB = new ABProp(3660, "16", "64");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SERVICE_IMPROVEMENT_OPT_OUT_FLAG = new ABProp(3664, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp HIGH_QUALITY_LINK_PREVIEW_ENABLED = new ABProp(3665, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GIF_MIN_PLAY_LOOPS = new ABProp(3682, "1", "1");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GIF_MAX_PLAY_LOOPS = new ABProp(3683, "3", "3");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GIF_MAX_PLAY_DURATION = new ABProp(3684, "5", "5");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CAPTION_EDIT_RECEIVE = new ABProp(3686, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CAPTION_EDIT_SEND = new ABProp(3687, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp LID_GROUPS_HANDLE_SERVER_ADDRESSING_MODE = new ABProp(3688, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHAT_UPSELL_FOR_1ON1_INVITES = new ABProp(3689, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ORDERS_EXPANSION_RECEIVER_COUNTRIES_ALLOWED = new ABProp(3690, "", "");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MD_LINK_DEVICE_WITH_PHONE_NUMBER_FORCE_ENABLED = new ABProp(3693, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MAX_NUM_PARTICIPANTS_FOR_SS = new ABProp(3694, "8", "8");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp REPORT_TO_ADMIN_KILL_SWITCH = new ABProp(3695, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp REPORT_TO_ADMIN_ENABLED = new ABProp(3696, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp QUICK_PROMOTION_BANNER_CLIENT_ENABLED = new ABProp(3712, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_MESSAGE_PROCESSING_CACHE_SIZE = new ABProp(3728, "400", "400");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PINNED_MESSAGES_M2_PIN_MAX = new ABProp(3732, "1", "1");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp COMMUNITY_SUBGROUP_SWITCHER_ENTRYPOINT_ENABLED = new ABProp(3738, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PAYMENTS_MERCHANT_GLOBAL_ORDERS_VALUE_PROPS_BANNER_ENABLED = new ABProp(3744, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp RETRY_RECEIPT_ERROR_CODE_ENABLED = new ABProp(3750, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ALLOW_LID_CONTACTS_NEW_1ON1_CHAT = new ABProp(3751, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ALLOW_LID_CONTACTS_ADD_TO_GROUP = new ABProp(3752, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ALLOW_LID_CONTACTS_CALLING = new ABProp(3762, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ALLOW_LID_CONTACTS_PRIVACY_SETTINGS = new ABProp(3763, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ORDERS_EXPANSION_PAYING_ENABLED = new ABProp(3771, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp NEWSLETTER_MEDIA_AUTODOWNLOAD_MODE = new ABProp(3778, "3", "3");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MEDIA_ENGAGEMENT_LOGGING_ENABLED = new ABProp(3787, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ALLOW_SHARE_LID_CONTACTS_VCARD = new ABProp(3789, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ALLOW_PARSE_LID_CONTACTS_VCARD = new ABProp(3790, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_ADDITIONAL_LABEL_EVENT_LOGGING_ENABLED = new ABProp(3793, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PARENT_GROUP_DIRECTORY_ENABLED = new ABProp(3795, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp LID_GROUPS_OUTGOING_EXPLICIT_ADDRESS_MODE = new ABProp(3803, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp LID_GROUPS_AGGREGATE_PARTICIPANT_CHANGE_SYSTEM_MESSAGE = new ABProp(3804, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MSGD_DROP_DEVICE_NOTIFICATIONS = new ABProp(3806, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp NEWSLETTER_TOS_NOTICE_ID = new ABProp(3810, "20601216", "20601216");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp HISTORY_SYNC_ON_DEMAND_MESSAGE_COUNT = new ABProp(3811, "50", "50");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PINNED_MESSAGES_M1_SENDER_DEBUG_EXPIRY_DURATION_SECS = new ABProp(3813, "86400", "86400");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CLIENT_MESSAGE_ID_MEDIA_DOWNLOAD_LOG_ENABLED = new ABProp(3820, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp UNIFIED_OTP_COPY_CODE_URL = new ABProp(3827, "https://www.whatsapp.com/otp/copy/", "https://www.whatsapp.com/otp/copy/");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp UNIFIED_OTP_RETRIEVER_URL = new ABProp(3828, "https://www.whatsapp.com/otp/code", "https://www.whatsapp.com/otp/code");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PARENT_GROUP_ALLOW_MEMBER_ADDED_GROUPS_M1 = new ABProp(3829, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp NEWSLETTER_CREATION_TOS_ID = new ABProp(3834, "20601217", "20601217");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp NEWSLETTER_CREATION_NUX_ID = new ABProp(3835, "20601218", "20601218");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SHOW_BOTTOM_SHEET_GALLERY = new ABProp(3844, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp TS_NAVIGATION_COMMUNITY_ENABLED = new ABProp(3858, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp TS_BIT_ARRAY_ENABLED = new ABProp(3859, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp TS_SESSION_DURATION_MS = new ABProp(3860, "600000", "600000");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp COMMUNITY_BREAKOUT_GROUPS_ENABLED = new ABProp(3864, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp APPEND_MESSAGE_WHEN_FORWARDING_MEDIA_WITHOUT_CAPTION = new ABProp(3875, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp LID_GROUPS_CREATE_LID_INDIVIDUAL_CHATS = new ABProp(3876, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_ENABLED = new ABProp(3877, "0", "2");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_CREATION_ENABLED = new ABProp(3878, "0", "2");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_DIRECTORY_ENABLED = new ABProp(3879, "0", "2");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SHOW_CHANNELS_NOT_AVAILABLE_DIALOG = new ABProp(3880, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp HISTORY_SYNC_ON_DEMAND_TIMEOUT_MS = new ABProp(3882, "10000", "10000");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_CLIENT_PULL_TIMEOUT_MS = new ABProp(3890, "10000", "10000");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GROUP_JOIN_REQUEST_M3_GROUPS_IN_COMMON = new ABProp(3895, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNEL_SUPPORTED_MESSAGE_TYPES = new ABProp(3919, "1, 2, 3, 5, 9, 10, 12, 15", "1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp HD_VIDEO_LABEL_ENABLED = new ABProp(3934, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PER_SEND_HD_VIDEO_SETTING_ENABLED = new ABProp(3935, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp HD_VIDEO_MIN_STREAMING_BANDWIDTH = new ABProp(3936, "150", "150");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp VNAME_LOGGING_AND_DEBUGGING = new ABProp(3961, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp FIRST_MESSAGE_EXPERIENCE = new ABProp(3962, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp COMMUNITY_SHORTER_GROUP_CREATION_ENABLED = new ABProp(3966, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_STATUS_REPORT_AND_BLOCK = new ABProp(3988, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PRIVACY_TIPS_GROUPS_BUILD = new ABProp(3995, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PRIVACY_TIPS_CALLERS_BUILD = new ABProp(3996, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PRIVACY_TIPS_STATUS_BUILD = new ABProp(3997, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PRIVACY_TIPS_PROFILE_BUILD = new ABProp(3998, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp UNIFIED_E2EE_COPY_BUILD = new ABProp(3999, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp UNIFIED_E2EE_UI_BUILD = new ABProp(4000, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_PREMIUM_MESSAGES_SPAM_REPORT_ENABLED = new ABProp(4005, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp VERIFIED_BUSINESS_NUMBERS_FOR_BUSINESS_NAME_UPDATE = new ABProp(4006, "", "917531875318,919004990049");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BONSAI_ENABLED = new ABProp(4010, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_CTWA_ACTION_BANNER_LOGGING_ENABLED_WEB = new ABProp(4022, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_CHAT_PSA_FORWARDS = new ABProp(4033, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp KIC_MSG_SEND_EXPIRY_SEC = new ABProp(4042, "86400", "86400");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GROUP_MENTIONS_IN_SUBGROUPS = new ABProp(4087, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CAG_MESSAGE_EDIT_RECEIVE = new ABProp(4089, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CAG_MESSAGE_EDIT_SEND = new ABProp(4090, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BROADCAST_MESSAGE_EDIT_RECEIVE = new ABProp(4091, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BROADCAST_MESSAGE_EDIT_SEND = new ABProp(4092, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp EXPANDED_TEXT_FORMATTING_ENABLED = new ABProp(4093, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CALL_LOG_PREFER_PUSH_NAMES = new ABProp(4094, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CALL_DETAILS_PREFER_PUSH_NAMES = new ABProp(4095, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PDF_EXTERNAL_DEEPLINK_ENABLED = new ABProp(4100, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp UGC_PARTICIPANT_LIMIT = new ABProp(4118, "5", "5");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DM_RELIABILITY_REFACTOR = new ABProp(4131, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp HISTORY_SYNC_ON_DEMAND_WITH_ANDROID_BETA = new ABProp(4135, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PAYMENTS_BR_INSTALLMENT_BUYER_LEARN_MORE_LINK = new ABProp(4144, "https://faq.whatsapp.com/1134168457974360", "https://faq.whatsapp.com/1134168457974360");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp HISTORY_SYNC_LOOP_INTERVAL_MS = new ABProp(4149, "20000", "20000");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SUPPORT_TICKET_DATA_COLLECTION_IMPROVEMENTS = new ABProp(4150, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DEFAULT_VIDEO_LIMIT_MB_NEWSLETTER = new ABProp(4155, "16", "16");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp COMMUNITY_SUBGROUP_IDENTITY_V2 = new ABProp(4160, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp LID_GROUPS_MESSAGE_SEND_VALIDATION = new ABProp(4162, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BONSAI_RECEIVER_ENABLED = new ABProp(4165, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp HD_VIDEO_DEFINITION_MIN_EDGE = new ABProp(4171, "720", "720");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp HD_VIDEO_DEFINITION_MAX_EDGE = new ABProp(4172, "864", "864");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp HD_VIDEO_DEFINITION_MIN_EDGE_WITH_MAX_EDGE = new ABProp(4175, "480", "480");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PARENT_GROUP_ALLOW_MEMBER_ADDED_GROUPS_M2 = new ABProp(4184, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CALL_END_RATING_VERSION = new ABProp(4185, "1", "1");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GROUP_CALL_MAX_PARTICIPANTS = new ABProp(4190, "32", "32");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp QP_PUSH_NOTIFICATIONS_ENABLED = new ABProp(4200, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PRIVACY_TIP_EXPIRATION_MIN = new ABProp(4214, "10080", "10080");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_RESTRICTED_UPDATES_ENABLED = new ABProp(4219, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MEMBER_NAME_TAG_ENABLED = new ABProp(4233, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PAYMENTS_BR_CONTENT_OPTIMIZATION_VARIANT = new ABProp(4248, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PAYMENTS_BR_INSTALLMENT_SELLER_LEARN_MORE_LINK = new ABProp(4254, "https://faq.whatsapp.com/253337763937767", "https://faq.whatsapp.com/253337763937767");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CART_ORDER_CREATION_SHORTCUT_ENABLED = new ABProp(4257, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_RECOMMENDED_CACHE_TTL_MS = new ABProp(4271, "604800000", "86400000");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BOT_RESPONSE_FUTUREPROOF_MESSAGE_ENABLED = new ABProp(4274, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DIRECTORY_SORT_KILL_SWITCH = new ABProp(4282, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DIRECTORY_SEARCH_KILL_SWITCH = new ABProp(4283, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp P2M_EXTERNAL_PAYMENTS_LINK_ENABLED = new ABProp(4295, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SOOX_MEDIA_SEND_KEEP_OLD_BUTTON = new ABProp(4297, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNEL_REACTIONS_ENABLED = new ABProp(4306, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNEL_FOLLOWER_LIST_ENABLED = new ABProp(4307, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp RECOMMENDED_CHANNELS_CACHE_MAX_TTL = new ABProp(4308, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp RECOMMENDED_CHANNELS_BACKGROUND_REFRESH = new ABProp(4309, "14400000", "1800000");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PRIVACY_TIPS_KILLSWITCH = new ABProp(4314, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp EXTENSIONS_PROPAGATE_ERRORS_VIA_DATACHANNEL_ENABLED = new ABProp(4317, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MESSAGE_EDIT_BUBBLE_ANIMATION = new ABProp(4325, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNEL_PULL_MESSAGE_UPDATES_THRESHOLD_SECONDS = new ABProp(4326, "120", "120");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_OTP_COPY_CODE_DISABLED = new ABProp(4330, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNEL_FORWARD_TO_CHAT_ENABLED = new ABProp(4338, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp COMMUNITY_SUBGROUP_JOIN_FROM_SYSTEM_MESSAGE_ENABLED = new ABProp(4345, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_ML_BWE_MODEL_DOWNLOAD = new ABProp(4349, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_RECOMMENDED_ENABLED = new ABProp(4356, "0", "2");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_WAITLIST_ENABLED = new ABProp(4357, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp HISTORY_SYNC_ON_DEMAND_FAILURE_LIMIT = new ABProp(4364, "10", "10");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp HISTORY_SYNC_ON_DEMAND_COOLDOWN_SEC = new ABProp(4365, "7200", "7200");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp HISTORY_SYNC_ON_DEMAND_REQUEST_SEND_KILLSWITCH = new ABProp(4366, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp NEWSLETTER_MEDIA_AUTODOWNLOAD_JITTER_MULTIPLIER = new ABProp(4369, "5000", "1000");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp NEWSLETTER_MEDIA_AUTODOWNLOAD_QUEUE_MAX_CONCURRENCY = new ABProp(4370, "5", "5");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp OIBWE_SLOW_POLLING = new ABProp(4382, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp OIBWE_PROBING_MODE = new ABProp(4383, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp FLATTENED_REACTIONS_COLLECTION = new ABProp(4390, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp EXTENSIONS_JID_DENYLIST_FOR_PROPAGATING_ERRORS_VIA_DATACHANNEL = new ABProp(4407, "", "");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BONSAI_PTT_ENABLED = new ABProp(4416, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BONSAI_UPDATE_INTERVAL = new ABProp(4417, "86400", "86400");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BUSINESS_TOOL_ENHANCED_LOGGING = new ABProp(4427, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PINNED_MESSAGES_SENDER_SHORT_EXPIRY_DURATIONS_ENABLED = new ABProp(4432, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ANYONE_CAN_ADD_TO_GROUPS_BY_DEFAULT = new ABProp(4441, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp NEWSLETTER_MEDIA_PRIORITY_QUEUE_INCOMING_MAX_SIZE = new ABProp(4479, "32", "32");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PNH_CAG_DISABLE_REACTIONS_GROUP_SIZE = new ABProp(4495, "10000", "10000");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_UPDATES_TAB_LOGGING_ENABLED = new ABProp(4506, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WAVOIP_ML_BWE_MODEL_DOWNLOAD_VERSIONS = new ABProp(4514, "11,3011,8011,999994,999992,999878,899878,799878,999879,999876,999875,300004,300005,999623,999624,799879,797801,79780199,797803,799851,797808,900111,900112,900113,900114,999830,999829,999638,200000,200002,699000,699001,699002,699003,699020,699021,699022,699023,699028,699029,699030,699031,699032,699040,699041,699042,900128,999639,800002,800003,800004,800005,7780307,800007,800008,800009,900155,900156,7780211,7780210,465100,5790213,900160,900161,900162,850001,900163,900165,57903172,900166,900172,900174,465202,465801,90014899,900177,900178,7780500,7780501,7780708,5790715,7780715,7780730,7780801,7780811,91080003,91000001", "11,3011,8011,999994,999992,999878,899878,799878,999879,999876,999875,300004,300005,999623,999624,799879,797801,79780199,797803,799851,797808,900111,900112,900113,900114,999830,999829,999638,200000,200002,699000,699001,699002,699003,699020,699021,699022,699023,699028,699029,699030,699031,699032,699040,699041,699042,900128,999639,800002,800003,800004,800005,7780307,800007,800008,800009,900155,900156,7780211,7780210,465100,5790213,900160,900161,900162,850001,900163,900165,57903172,900166,900172,900174,465202,465801,90014899,900177,900178,7780500,7780501,7780708,5790715,7780715,7780730,7780801,7780811,91080003,91000001");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PARENT_GROUP_ALLOW_MEMBER_ADDED_GROUPS_DEFAULT_ON_CREATION = new ABProp(4530, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BONSAI_AVATAR_ENABLED = new ABProp(4532, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PNH_SYNC_IDENTITY_KEYS_AND_DEVICES = new ABProp(4533, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MAX_PIXELS_SIZE_ALLOWED_FOR_IMAGE = new ABProp(4538, "921600", "921600");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp IN_APP_COMMS_MANAGE_ADS_WEB_BANNER_CAMPAIGN_ENABLED = new ABProp(4542, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PTV_NUX_ENABLED = new ABProp(4548, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PTV_BUTTON_PERSISTENCE_ENABLED = new ABProp(4549, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ADD_MEMBER_SYSTEM_MESSAGE = new ABProp(4579, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_PREMIUM_MESSAGES_INTERACTIVITY_RENDERING_ENABLED = new ABProp(4596, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp FUN_STICKERS_LOCALE_LANGS = new ABProp(4631, "en", "en");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_WAITLIST_LOGGING_ENABLED = new ABProp(4632, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNEL_REACTIONS_SENDING_ENABLED = new ABProp(4633, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_DYI_ENABLED = new ABProp(4635, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp FUN_STICKERS_PHASE2_ENABLED = new ABProp(4643, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNEL_FORWARD_TO_CHAT_V2_ENABLED = new ABProp(4644, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNEL_VIEWS_DURATION_MILLISECONDS = new ABProp(4648, "250", "250");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_LARGE_NUMBER_FORMAT_ENABLED = new ABProp(4653, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PARENT_GROUP_MEMBER_CAN_ADD_ENABLED = new ABProp(4654, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_PREMIUM_MESSAGES_CLICK_LOGGING_ENABLED = new ABProp(4657, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_CLEAR_FORMATTED_PREVIEW = new ABProp(4659, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WAVOIP_ML_BWE_MODEL_DOWNLOAD_VERSIONS_VOIP_LAB = new ABProp(4661, "7,8,9,10,11,12,13,16,17,5011,5012,999998,999996,999994,999993,999992,300001", "7,8,9,10,11,12,13,16,17,5011,5012,999998,999996,999994,999993,999992,300001");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CAROUSEL_MESSAGE_CLIENT_ENABLED = new ABProp(4668, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PDF_CLIENT_DRIVEN_ROLLOUT_ENABLED = new ABProp(4679, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PDF_MAX_DOWNLOAD_JITTER_TIME_SECONDS = new ABProp(4680, "180", "180");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_INTERNAL_IN_APP_BUG_REPORTING_ENABLE = new ABProp(4681, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNEL_FORWARD_TO_CHAT_V2_MESSAGE_NAVIGATION_ENABLED = new ABProp(4682, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNEL_FORWARD_TO_CHAT_V2_NEW_UI_ENABLED = new ABProp(4683, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_VIEW_COUNTS_DISPLAY_TO_FOLLOWERS_ENABLED = new ABProp(4684, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp IS_LTO_OFFER_ENABLED = new ABProp(4693, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp INTERNAL_BUG_REPORTING_V1_ENABLED = new ABProp(4697, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CALL_FAVORITES_VERSION = new ABProp(4708, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MAX_GROUP_SIZE_FOR_LONG_RINGTONE = new ABProp(4710, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNEL_VIEW_COUNTS_ENABLED = new ABProp(4721, "0", "3");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNEL_PLAYABLE_MESSAGE_VIEWS_DURATION_MILLISECONDS = new ABProp(4722, "3000", "3000");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_STICKER_SUGGESTIONS_ENABLE = new ABProp(4726, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PARENT_GROUP_ANNOUNCEMENT_COMMENTS_ENABLED = new ABProp(4727, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PARENT_GROUP_ANNOUNCEMENT_COMMENTS_PARTICIPANT_LIMIT = new ABProp(4728, "1024", "1024");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BONSAI_TI_TIMEOUT_DURATION_MS = new ABProp(4736, "10000", "10000");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp USERNAME_CREATION = new ABProp(4745, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp USERNAME_CONTACT_DISPLAY = new ABProp(4746, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp USERNAME_CHANGE = new ABProp(4747, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp USERNAME_1ON1_CHAT = new ABProp(4748, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp USERNAME_GROUP_PARTICIPANTS = new ABProp(4749, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_SEND_VIEW_RECEIPT_ENABLED = new ABProp(4760, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_ENHANCED_LABEL_LOGGING = new ABProp(4761, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_SEARCH_BY_DATE_ENABLED = new ABProp(4770, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PDF_MD_SUPPORT_ENABLED = new ABProp(4779, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PAYMENTS_BR_PIX_PHASE_1_SELLER_ENABLED = new ABProp(4781, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_DIRECTORY_LOGGING_ENABLED = new ABProp(4783, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_CREATION_LOGGING_ENABLED = new ABProp(4784, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_VIDEO_LIMIT_MB = new ABProp(4787, "16", "16");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_VALUE_HOLDOUT_H2_23_ENABLED = new ABProp(4796, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp IN_APP_SUPPORT_CAPI_NUMBER_PREFIXES = new ABProp(4799, "155178684", "155178684");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_UWP_SHARE_ANY_WINDOW = new ABProp(4801, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_CACHED_MEDIA_MANAGER = new ABProp(4812, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp LOW_CACHE_HIT_RATE_MEDIA_TYPES = new ABProp(4836, "ptt,audio,document,ppic", "ptt,audio,document,ppic");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WAE_METADATA_INTEGRITY_TIMEOUT_MINUTES = new ABProp(4849, "5", "5");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp COMMUNITY_EXAMPLES = new ABProp(4852, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNEL_FORWARD_TO_CHAT_LINK_ENABLED = new ABProp(4860, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_DYI_MAX_FILE_SIZE_IN_BYTES_WARNING_THRESHOLD = new ABProp(4866, "1000000000", "1000000000");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp UNIFIED_E2EE_COPY_LAUNCH = new ABProp(4869, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp UNIFIED_E2EE_UI_LAUNCH = new ABProp(4870, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WABAI_MESSAGE_RENDERING_ENABLED = new ABProp(4873, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNEL_REACTIONS_SETTINGS_ENABLED = new ABProp(4887, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ROW_BUYER_ORDER_REVAMP_M0_ENABLED = new ABProp(4893, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp POST_STATUS_IN_COMPANION = new ABProp(4905, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp EVOLVE_ABOUT_M1_ENABLED = new ABProp(4921, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SOOX_LONG_PRESS_DURATION_MS = new ABProp(4922, "500", "500");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp TS_EXTERNAL_ENABLED = new ABProp(4928, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp TS_SURFACE_KILLSWITCH = new ABProp(4929, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMBA_PREMIUM_MESSAGES_INTERACTIVITY_CATALOG_CTA_CONSUMER_ENABLED = new ABProp(4942, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_PREMIUM_MESSAGES_INTERACTIVITY_CATALOG_CTA_CONSUMER_ENABLED = new ABProp(4957, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BONSAI_WORD_STREAMING_ENABLED = new ABProp(4974, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PAYMENTS_BR_INFO_ARCHITECTURE_ORDERS_HUB_ENABLED = new ABProp(4976, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_SPAM_REPORT_IQ_WITH_PRIVACY_TOKEN = new ABProp(4991, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_PRIVACY_TOKEN_WITH_TIMESTAMP = new ABProp(4992, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp COMMUNITY_MEMBERS_BOTTOMSHEET_POST_CREATION_ENABLED = new ABProp(5000, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp VNAME_CERT_DEPRECATION = new ABProp(5001, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp UG_CHAT_BANNER_ENABLED = new ABProp(5002, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_LABELS_CTWA_DATA_SHARING = new ABProp(5009, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_FILTER_OUT_SUBSCRIBED_IN_DIRECTORY_NULL_STATE = new ABProp(5015, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp COMMUNITY_GENERAL_CHAT_UI_ENABLED = new ABProp(5021, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp TS_NAVIGATION_CHANNELS_ENABLED = new ABProp(5040, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNEL_LINK_IN_NAV_BAR_ENABLED = new ABProp(5041, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_PREMIUM_MESSAGES_URL_CTA_ALERT_DIALOG_ENABLED = new ABProp(5044, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp COMMUNITY_HISTORY_SETTING_RECEIVE = new ABProp(5046, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_LAZY_LOADING_OF_CALL_VIEW_ELEMENTS = new ABProp(5053, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PNH_CAG_DISABLE_POLLS_GROUP_SIZE = new ABProp(5056, "10000", "10000");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp LTO_OFFER_MEDIA_ASPECT_RATIO = new ABProp(5073, "0.800000011920929", "0.800000011920929");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PARENT_GROUP_ALLOW_MEMBER_SUGGEST_EXISTING_M3_SENDER = new ABProp(5077, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PARENT_GROUP_ALLOW_MEMBER_SUGGEST_EXISTING_M3_RECEIVER = new ABProp(5078, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_PRELOAD_CHAT_MESSAGES = new ABProp(5079, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_ADD_PARTICIPANT_WHILE_CALLING_SENDER = new ABProp(5088, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_RECOMMENDED_V2_UI_ENABLED = new ABProp(5096, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_NONCRITICAL_HISTORY_SYNC_MESSAGE_PROCESSING_BREAK_ITERATION = new ABProp(5106, "100", "100");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PARENT_GROUP_JOIN_REQUEST_SYSTEM_ENABLED = new ABProp(5109, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_TC_TOKEN_DB_READ_ENABLED = new ABProp(5110, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp UNIFIED_E2EE_BOTTOMSHEET = new ABProp(5111, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp UNIFIED_E2EE_SECURITY_PAGE = new ABProp(5112, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp UNIFIED_E2EE_BACKUP_PAGE = new ABProp(5113, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BUYER_INITIATED_ORDER_REQUEST_VARIANT_ENABLED = new ABProp(5114, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp IM_NFM_DYNAMIC_MESSAGE_KILLSWITCH = new ABProp(5124, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_DIRECTORY_V2_ENABLED = new ABProp(5126, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_DIRECTORY_V2_FILTER_TYPES = new ABProp(5127, "", "1, 2, 3, 4, 5, 6");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PARENT_GROUP_ANNOUNCEMENT_COMMENTS_RECEIVER_ENABLED = new ABProp(5141, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp COMMUNITY_GENERAL_CHAT_MAX_AUTO_ADD_USERS = new ABProp(5144, "1024", "1024");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_CLEAR_TRACKING = new ABProp(5151, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_ADMIN_CONTEXT_CARD_ENABLED = new ABProp(5158, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_GEOSUSPEND_ENABLED = new ABProp(5161, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp COMMUNITY_NAVIGATE_TO_UNREAD_SUBGROUP_ENABLED = new ABProp(5169, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp INBOX_FILTERS_ENABLED = new ABProp(5171, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp INBOX_FILTERS_FAVORITES_ENABLED = new ABProp(5172, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_MESSAGE_EDIT_ENABLED = new ABProp(5174, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp FORCE_TRANSCODE_VIDEOS = new ABProp(5178, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp FORCE_TRANSCODE_PHOTOS = new ABProp(5179, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNEL_REACTIONS_SENDER_LIST_ENABLED = new ABProp(5185, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_MESSAGE_LINK_ENABLED = new ABProp(5188, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SELLER_ORDERS_MANAGEMENT_REVAMP = new ABProp(5190, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp COMMUNITY_HISTORY_SETTING_SEND = new ABProp(5191, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp COMMUNITY_HISTORY_RECEIVE = new ABProp(5192, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp COMMUNITY_HISTORY_SEND = new ABProp(5193, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_DIRECTORY_FETCH_LIMIT = new ABProp(5203, "50", "50");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_DIRECTORY_SEARCH_DEBOUNCE_MS = new ABProp(5204, "250", "250");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GROUP_JOIN_REQUEST_M3_PUSH_NOTIFICATION = new ABProp(5212, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WABAI_MESSAGE_FEEDBACK_ENABLED = new ABProp(5215, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_GEOSUSPEND_ADMIN_ALERTS_ENABLED = new ABProp(5216, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_FOLLOWERS_LIST_CACHE_REFRESH_MILLISECONDS = new ABProp(5217, "60000", "60000");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WABAI_MARKETING_MESSAGE_CONTENT_GEN_ENABLED = new ABProp(5224, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WAVOIP_ML_BWE_PLC_MODEL_DOWNLOAD_VERSIONS = new ABProp(5228, "", "");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WAVOIP_ML_BWE_PLC_MODEL_DOWNLOAD_VERSIONS_VOIP_LAB = new ABProp(5229, "", "");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WAVOIP_ML_BWE_UNDERSHOOT_MODEL_DOWNLOAD_VERSIONS = new ABProp(5231, "", "");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WAVOIP_ML_BWE_UNDERSHOOT_MODEL_DOWNLOAD_VERSIONS_VOIP_LAB = new ABProp(5232, "", "");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp REPORT_BLOCK_CLASSIFICATION_LOGGING_ENABLED = new ABProp(5245, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BONSAI_INLINE_FEEDBACK_ENABLED = new ABProp(5246, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp EXTENSIONS_CENTRAL_CONFIG_KILLSWITCH = new ABProp(5247, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PAYMENTS_MERCHANT_GLOBAL_ORDERS_VALUE_PROPS_BANNER_LOGGING_ENABLED = new ABProp(5255, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNEL_CORE_EVENT_LOGGING_ENABLED = new ABProp(5262, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp FIRST_MESSAGE_EXPERIENCE_V2 = new ABProp(5263, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_OFFLINE_DYNAMIC_BATCH_SIZE_ENABLED = new ABProp(5271, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNEL_REACTIONS_SETTINGS_NONE_OPTION_ENABLED = new ABProp(5274, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BLUE_ENABLED = new ABProp(5276, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BONSAI_CAROUSEL_ENABLED = new ABProp(5283, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_HIDE_NEWS_URL_PREVIEW = new ABProp(5287, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp USERNAME_USYNC = new ABProp(5290, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PTV_BUTTON_TOOLTIP_ANIMATION_ENABLED = new ABProp(5292, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BLUE_EDUCATION_ENABLED = new ABProp(5295, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_OFFLINE_DYNAMIC_BATCH_CONFIG = new ABProp(5297, "{\"version\": \"progressive\", \"multiplier\": 0.25}", "{\"version\": \"progressive\", \"multiplier\": 0.25}");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_DIRECTORY_V2_CACHE_TTL_MS = new ABProp(5303, "7200000", "3600000");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_DIRECTORY_V2_CACHE_REFRESH_INTERVAL_MS = new ABProp(5304, "1800000", "600000");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WIN_CALL_LOG_SEND_OUTGOING_SYNCD_MUTATIONS = new ABProp(5308, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DM_INITIATOR_TRIGGER = new ABProp(5309, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PTV_BUTTON_ANIMATION_ENABLED = new ABProp(5317, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PREMIUM_BLUE_ENABLED = new ABProp(5318, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_MESSAGE_LABELS_CTWA_DATA_SHARING = new ABProp(5324, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_CONTENT_GEN_ENABLED = new ABProp(5330, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp EXTENSIONS_GEOBLOCKING_ENABLED = new ABProp(5333, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_EVOLVE_ABOUT_SEND_ENABLED = new ABProp(5347, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_TURN_ON_CALL_NOTIFICATION_REMINDERS = new ABProp(5360, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BONSAI_ENTRY_POINT_ENABLED = new ABProp(5362, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_COEX_SYSTEM_MESSAGE = new ABProp(5383, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PTT_BUTTON_TOGGLE_COOLDOWN = new ABProp(5384, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PARENT_GROUP_MEMBER_CAN_ADD_DEFAULT_EVERYONE_ENABLED = new ABProp(5385, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PTV_BUTTON_RESET_MINIMIZE_THRESHOLD = new ABProp(5386, "-1", "-1");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp LARGE_NUMBER_FORMAT_USES_GENERIC_PLURAL = new ABProp(5402, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SIGNALING_LATENCY_SETTINGS = new ABProp(5408, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PTV_RECORDING_COUNTDOWN_INTERVAL = new ABProp(5412, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BONSAI_WAITLIST_UPDATE_INTERVAL = new ABProp(5413, "21600", "21600");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PAYMENTS_INFO_ARCHITECTURE_ORDERS_HUB_ENABLED = new ABProp(5414, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CALL_LOG_SYNC_ENABLED_M1 = new ABProp(5417, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PTV_SETTING = new ABProp(5418, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PTV_SETTING_SENDS_THRESHOLD = new ABProp(5419, "-1", "-1");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp NEWSLETTER_TOS_NOTICE_VERSION = new ABProp(5448, "1", "1");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp COMMUNITY_GENERAL_CHAT_CREATE_ENABLED = new ABProp(5453, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp NEWSLETTER_CREATION_TOS_VERSION_V2 = new ABProp(5456, "1", "1");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp NEWSLETTER_CREATION_NUX_VERSION_V2 = new ABProp(5457, "1", "1");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BONSAI_WAITLIST_ENABLED = new ABProp(5459, "true", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_LABEL_IMPROVEMENTS_M2 = new ABProp(5463, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_RECOMMENDED_V2_RECENTLY_FOLLOWED_CHANNELS_BELOW_ENABLED = new ABProp(5464, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_DIRECTORY_V2_LOGGING_ENABLED = new ABProp(5471, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PINNED_MESSSAGES_M1_RECEIVER_FIRST_TIME_SERVER_TS_STORAGE = new ABProp(5474, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PTV_SETTING_DURATION_THRESHOLD_SECONDS = new ABProp(5483, "604800", "604800");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_DIRECTORY_PAGINATION_ENABLED = new ABProp(5487, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_DYI_LOGGING_ENABLED = new ABProp(5488, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp UPDATES_TAB_OPEN_CHANNELS_FIELDS_LOGGING_ENABLED = new ABProp(5489, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_SHARE_LINK_LOGGING_ENABLED = new ABProp(5491, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_FORWARD_LOGGING_V2_ENABLED = new ABProp(5492, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_MAX_MESSAGES_BATCH_PULL = new ABProp(5494, "100", "100");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_RESUME_OPTIMIZED_READ_RECEIPT_SEND_INTERVAL = new ABProp(5502, "500", "500");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PTV_BUTTON_REDESIGN_VERSION = new ABProp(5507, "102", "102");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_HQ_LINK_PREVIEW = new ABProp(5511, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AUTODOWNLOAD_UPDATE_IN_ONE_ONE_CHAT = new ABProp(5517, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ROW_BUYER_ORDER_REVAMP_M0_NUX_BANNER_ENABLED = new ABProp(5518, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_PRE_ACKS_M3_ENABLED = new ABProp(5521, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_POLL_CREATION_ENABLED = new ABProp(5533, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_POLL_SINGLE_OPTION_CONTROL_ENABLE = new ABProp(5534, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_CAROUSEL_MESSAGE_CLIENT_LOGGING = new ABProp(5542, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SUPPORT_TICKET_DEVICE_LOG_RETENTION_PERIOD_DAYS = new ABProp(5553, "3", "3");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_MANAGE_ADS_TAB_WEB = new ABProp(5554, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp INVALID_HOSTED_COMPANION_NACK_ENABLED = new ABProp(5555, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp EVENTS_CREATE = new ABProp(5562, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp EVENTS_VIEW = new ABProp(5563, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DESKTOP_UPSELL_MAC_CTA_CHATLIST_DROPDOWN = new ABProp(5565, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DESKTOP_UPSELL_MAC_CTA_SEARCH_RESULTS_TOASTBAR = new ABProp(5567, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DESKTOP_UPSELL_MAC_CTA_CALL_BTN = new ABProp(5568, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DESKTOP_UPSELL_MAC_CTA_INTRO_PANEL = new ABProp(5569, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SELLER_ORDER_PAYMENT_REQUEST_ENABLED = new ABProp(5574, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BUYER_ORDER_PAYMENT_REQUEST_ENABLED = new ABProp(5575, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DM_RELIABILITY_LOGGING = new ABProp(5580, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_MEDIA_VIEW_REPLY = new ABProp(5582, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BOT_3P_ENABLED = new ABProp(5587, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp NEWSLETTER_TOS_NOTICE_ID_SMB_WEB = new ABProp(5597, "20601216", "20601216");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp NEWSLETTER_CREATION_TOS_ID_SMB_WEB = new ABProp(5598, "20601217", "20601217");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp COMPANION_BIZ_LABEL_SYNC_ENABLED = new ABProp(5610, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_SMB_DATA_SHARING_SETTINGS_KILLSWITCH = new ABProp(5615, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNEL_INFO_ADMIN_METADATA_FETCHING_ENABLED = new ABProp(5621, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp LID_OUTGOING_MSG_ATTACH_META_TAG = new ABProp(5623, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_MEDIA_CACHE_SETTING_ENABLED = new ABProp(5625, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SAGA_ENABLED = new ABProp(5626, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BONSAI_ENGLISH_ONLY = new ABProp(5637, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_SEND_ALBUM_ENABLED = new ABProp(5643, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_MESSAGE_LOADING_INDICATORS_ENABLED = new ABProp(5646, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp COMMUNITY_GENERAL_CHAT_NOTIFICATION_FOLLOWUP_ENABLED = new ABProp(5665, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_QUICK_REPLY_LABELS = new ABProp(5671, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GIMMICK_PHASE_TWO_RECEIVER_ENABLED = new ABProp(5692, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GIMMICK_PHASE_TWO_SENDER_ENABLED = new ABProp(5693, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp COMPANION_BIZ_QUICK_REPLY_SYNC_ENABLED = new ABProp(5694, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp RT_RECEIVE_REPORTING_TAG = new ABProp(5718, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_BUSINESS_ACTION_BAR_ENABLED = new ABProp(5719, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_EXPRESSION_PANELS_SHOW_LESS_STICKERS = new ABProp(5726, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WABAI_CONSENT_COOLDOWN = new ABProp(5746, "-1", "-1");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WABAI_CONSENT_REQUIRED = new ABProp(5747, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp POLL_ANIMATIONS_ENABLED = new ABProp(5753, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp INBOX_FILTERS_RESET_TIMEOUT = new ABProp(5765, "1800", "1800");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp UPDATES_SEARCH_ENABLED = new ABProp(5769, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ORDER_STATUSES_REVAMP_M1_ENABLED = new ABProp(5770, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_ENABLE_MSG_HISTORY_METRICS = new ABProp(5777, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BOT_COMMANDS_1P_ENABLED = new ABProp(5811, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PARENT_GROUP_ANNOUNCEMENT_COMMENTS_HISTORY_SYNC_RECEIVER_ENABLED = new ABProp(5813, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ORDER_BIZHOME_BADGING_ENABLED = new ABProp(5814, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_FETCH_INCREASED_VIDEO_DATA_FOR_THUMBNAILS_WEB_ENABLED = new ABProp(5827, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp EVOLVE_ABOUT_M1_RECEIVER_ENABLED = new ABProp(5839, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BLUE_STRINGS_ENABLED = new ABProp(5846, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp UPDATES_REMOTE_SEARCH_ENABLED = new ABProp(5851, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp COLLAPSING_RECOMMENDED_CHANNELS_ENABLED = new ABProp(5852, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_DIRECTORY_PAGE_SIZE = new ABProp(5853, "50", "50");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PAYMENTS_BR_PIX_PHASE_2_SELLER_ENABLED = new ABProp(5861, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PAYMENTS_BR_SUPPORTED_PAYMENT_METHOD_SELLER_ENABLED = new ABProp(5862, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MM_DATA_SHARING_DISCLOSURE_ENABLED = new ABProp(5869, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_PROACTIVE_MESSAGE_GAP_HANDLING_ENABLED = new ABProp(5871, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_PTT_SENDER_ENABLED = new ABProp(5875, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_PTT_RECEIVER_ENABLED = new ABProp(5876, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_PTT_TRANSCRIPTION_BLOCKED = new ABProp(5884, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BIZ_API_VIRTUAL_PHONE_NUMBER_STRING_RANGES = new ABProp(5894, "{}", "{\"ranges\":[{\"start\": 12115556300, \"end\": 12115556300}, {\"start\": 18895551110, \"end\": 18895551110}, {\"start\": 18895554429, \"end\": 18895554429}]}");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp UPDATES_TAB_SEARCH_LOGGING_ENABLED = new ABProp(5909, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BLUE_CLIENT_P0_LOGGING_ENABLED = new ABProp(5918, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_FETCH_METADATA_FROM_SERVER_BY_INVITE_CODE_WEB = new ABProp(5935, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BR_BUYER_AWARENESS_EXPERIENCES = new ABProp(5944, "{}", "{\"updated_order_bubble_subtext\": 0, \"updated_order_cta\": 0}");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_GEOSUSPEND_APPEALS_ENABLED = new ABProp(5959, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_SUSPEND_APPEALS_ENABLED = new ABProp(5973, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SUPPORTED_ENCODER_DECODER_LOGGING_ENABLED = new ABProp(5977, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BOT_3P_STATUS = new ABProp(5985, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DATA_SHARING_TRANSPARENCY_INDICATOR_DURATION = new ABProp(5990, "604800", "604800");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MEDIA_SENT_AS_DOC_THUMBNAIL_MAX_EDGE = new ABProp(6003, "280", "280");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp EXPANDED_TEXT_FORMATTING_PREVIEW_ENABLED = new ABProp(6004, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CALLEE_ACCEPT_TIMEOUT_MS = new ABProp(6007, "30000", "30000");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MEDIA_AUTODOWNLOAD_AUDIO_MAX_SIZE = new ABProp(6040, "524288", "524288");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MEDIA_AUTODOWNLOAD_GIF_AS_IMAGE_MAX_SIZE = new ABProp(6041, "524288", "524288");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp INACTIVE_DAYS_TO_DISABLE_STATUS_AUTODOWNLOAD = new ABProp(6042, "14", "14");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp UNIFIED_POLL_VOTE_ADDON_INFRA_ENABLED = new ABProp(6046, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BONSAI_PLUGIN_STORAGE_ENABLED = new ABProp(6048, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp INBOX_FILTERS_HAPTIC_FEEDBACK_ENABLED = new ABProp(6052, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp FMX_CTWA_KILL_SWITCH = new ABProp(6061, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_BIZ_ACTION_BAR_HOLDOUT = new ABProp(6073, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_MULTI_ADMIN_ENABLED = new ABProp(6096, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_STICKER_SENDING_ENABLED = new ABProp(6110, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_STICKER_RECEIVING_ENABLED = new ABProp(6111, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BLUE_EDUCATION_V2_ENABLED = new ABProp(6127, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp NEWSLETTER_ALL_TOS_DISABLED = new ABProp(6133, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_PULL_LAST_MSG_ON_CHANNEL_OPEN = new ABProp(6138, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DSA_CHANNELS_REPORT_UNLAWFUL_CONTENT_ENABLED = new ABProp(6145, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DSA_CHANNELS_ENFORCEMENT_SUSPENSION_ENABLED = new ABProp(6146, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DSA_CHANNELS_ENFORCEMENT_GEO_SUSPENSION_ENABLED = new ABProp(6147, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DSA_CHANNELS_ENFORCEMENT_MESSAGES_ENABLED = new ABProp(6148, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ORDER_PAYMENT_AMOUNT_DETECTION_ENABLED = new ABProp(6151, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp TEXT_STATUS_TTL_SECONDS_ALLOWLIST = new ABProp(6153, "1800,3600,7200,14400,28800,86400", "1800,3600,7200,14400,28800,86400");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_LABEL_IMPROVEMENTS_REORDERING = new ABProp(6162, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp INCLUDE_PRIVACY_TOKEN_FLAG_IN_DELIVERY_RECEIPT = new ABProp(6163, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ORDER_SHOW_PAYMENT_REQUEST_ON_PAYMENT_AMOUNT_DETECTION_ENABLED = new ABProp(6167, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp EVOLVE_ABOUT_M1_RECEIVER_FOR_NEW_SURFACES_ENABLED = new ABProp(6172, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_BUSINESS_ACTION_BAR_EXPOSED = new ABProp(6184, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp RBI_SHOW_CONSISTENT_BLOCK_DIALOG_ALL_ENTRY_POINTS = new ABProp(6185, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp RBI_REVAMPED_BLOCK_REPORT_DIALOG_DESIGN = new ABProp(6186, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp RBI_CHANGE_DELETE_BEHAVIOR_BLOCK_REPORT_FLOWS = new ABProp(6187, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_POLL_RECEIVE_ENABLED = new ABProp(6191, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp EVENT_NAME_LENGTH_LIMIT = new ABProp(6207, "100", "100");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp EVENT_DESCRIPTION_LENGTH_LIMIT = new ABProp(6208, "2048", "2048");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_ENTRY_POINT_CONFIG_FETCH_THRESHHOLD = new ABProp(6214, "43200000", "2000");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp KILL_SWITCH_CTWA_ML_ENTRY_POINT_CONFIG = new ABProp(6215, "true", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_CTWA_ML_ENTRY_POINT_CONFIG = new ABProp(6216, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WAVOIP_ML_BWE_PLC_MODEL_DOWNLOAD_VERSIONS_LAUNCHED = new ABProp(6230, "", "");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WAVOIP_ML_BWE_UNDERSHOOT_MODEL_DOWNLOAD_VERSIONS_LAUNCHED = new ABProp(6231, "", "");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_MANAGE_ADS_TAB_WEB_AD_ACTIONS_MENU = new ABProp(6237, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_MANAGE_ADS_TAB_WEB_AD_METRICS = new ABProp(6238, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SYSTEM_MSG_TEXT_STYLING = new ABProp(6246, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SYSTEM_MSG_TRUNCATION = new ABProp(6247, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MEDIA_SENT_AS_DOC_THUMB_BLUR_RADIUS = new ABProp(6249, "2", "2");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_FUNNEL_LOGGING = new ABProp(6250, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BONSAI_CHAT_LIST_ENTRY_POINT_ENABLED = new ABProp(6251, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_UWP_SCREEN_SHARE_TEACHING_TIP = new ABProp(6264, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp EVENT_WA_CALL_MAX_DAYS_IN_ADVANCE = new ABProp(6265, "365", "365");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MAX_PARTICIPANTS_IN_GROUP_QUERIES = new ABProp(6267, "50000", "50000");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_PTT_LOGGING_ENABLED = new ABProp(6274, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp HD_MEDIA_TOOLTIP_ENABLED = new ABProp(6286, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BLUE_CLIENT_P1_LOGGING_ENABLED = new ABProp(6288, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DSA_CHANNELS_REPORTS_OUTCOME_LIST_ENABLED = new ABProp(6297, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_VIOLATING_MESSAGE_APPEALS_ENABLED = new ABProp(6321, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_REPORT_OUTCOME_APPEALS_ENABLED = new ABProp(6322, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_FORWARD_TO_CHANNEL_ENABLED = new ABProp(6323, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_MATERIAL_REFRESH = new ABProp(6332, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PNH_CTWA_SHOW_PRIVACY_SYSTEM_MSG = new ABProp(6336, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BLUE_PROFILE_LOCKED_UI_ENABLED = new ABProp(6337, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BOT_3P_DIY_DELETE_ENABLED = new ABProp(6348, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BATCH_GROUP_INFO_NEW_PIPELINE_ENABLED = new ABProp(6350, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PNH_CTWA_CONSUMER_UI = new ABProp(6355, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_BLOCK_UNSUPPORTED_STICKERS_ENABLED = new ABProp(6373, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp HD_MEDIA_TOOLTIP_KILLSWITCH_ENABLED = new ABProp(6378, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_POLL_VOTER_LIST_ENABLED = new ABProp(6382, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_NON_BASIC_STICKERS_ENABLED = new ABProp(6383, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_MULTI_ADMIN_NON_FOLLOWERS_ENABLED = new ABProp(6389, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_SKIP_EXPIRED_STATUS_ERROR = new ABProp(6391, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PARENT_GROUP_ANNOUNCEMENT_COMMENTS_RECEIVER_VALIDATION_KILLSWITCH = new ABProp(6416, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_STATUS_UPDATES_PRODUCTION_ENABLED = new ABProp(6442, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_STATUS_UPDATES_PRODUCTION_MESSAGE_TYPES = new ABProp(6443, "1, 2, 9", "1, 2, 9");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_STATUS_UPDATES_CONSUMPTION_ENABLED = new ABProp(6444, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_STATUS_INVITE_LINKS_PRODUCTION_ENABLED = new ABProp(6445, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BONSAI_CAROUSEL_REELS_PROFILE_PHOTO_ENABLED = new ABProp(6458, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BONSAI_CAROUSEL_HQ_THUMBNAIL_ENABLED = new ABProp(6459, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_MULTI_ADMIN_MAX_ADMIN_COUNT = new ABProp(6461, "16", "16");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp IS_CAPI_GROUPS_ALPHA_ENABLED = new ABProp(6473, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_POLL_MESSAGE_SHORT_NUMBER_FORMAT_ENABLED = new ABProp(6489, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp NEWSLETTER_ADMIN_INVITE_TOS_ID = new ABProp(6498, "20610101", "20610101");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp COEX_INFRA_LOGGING_ENABLED = new ABProp(6500, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_AUDIO_FILES_SENDER_ENABLED = new ABProp(6505, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_AUDIO_FILES_RECEIVER_ENABLED = new ABProp(6506, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DESKTOP_UPSELL_MAC_CTA_MISSED_CALL = new ABProp(6532, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_MULTI_ADMIN_RECEIVER_ENABLED = new ABProp(6535, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp NEWSLETTER_ADMIN_INVITE_TOS_ID_SMB_WEB = new ABProp(6536, "20610104", "20610104");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp TS_SMB_CATALOG = new ABProp(6547, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MAIBA_1P_STATUS = new ABProp(6566, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp RT_SYNC_REPORTING_TAG = new ABProp(6578, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CALLING_AUDIO_SHARE_VERSION = new ABProp(6598, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MEMBER_SUGGESTED_GROUP_FETCH_SYNC_VERSION = new ABProp(6600, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MEMBER_SUGGESTED_GROUP_PUSH = new ABProp(6601, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_SYNCD_DEBUG_DATA_IN_PATCH = new ABProp(6614, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp META_VERIFIED_NEWSLETTER_ENABLED = new ABProp(6618, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_JPEG_QUALITY = new ABProp(6619, "92", "92");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp STATUS_STOP_DOWNLOAD_AT_IMAGE_QUALITY_SCAN_INDEX = new ABProp(6623, "2", "2");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMBA_PREMIUM_MESSAGES_INSIGHTS_V2_TRACKABLE_LINK_DOMAIN = new ABProp(6626, "w.meta.me", "w.meta.me");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_PWA_BACKGROUND_SYNC = new ABProp(6656, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DRAFT_ORDERS_ENABLED = new ABProp(6659, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp POLL_UPDATE_DROP_UNKNOWNS = new ABProp(6662, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_DESIGN_REFRESH = new ABProp(6665, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ORDER_DETAILS_PAYMENT_INSTRUCTIONS_SYNC_ENABLED = new ABProp(6670, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp OTP_MASK_LINKED_DEVICES = new ABProp(6673, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BLUE_PHASE1A_FAST_FOLLOW_ENABLED = new ABProp(6687, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMBA_PREMIUM_MESSAGES_LEAVING_WA_CONTENT = new ABProp(6693, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_PWA_BACKGROUND_SYNC_MIN_INTERVAL_HOURS = new ABProp(6706, "24", "24");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CALL_OFFER_REDIAL_STATS_VERSION = new ABProp(6709, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_STATUS_IMAGE_HD_QUALITY_ENABLED = new ABProp(6713, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_STATUS_VIDEO_HD_QUALITY_ENABLED = new ABProp(6714, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp TS_BUSINESS_INTERACTIONS_ENABLED = new ABProp(6715, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp RT_CLEAN_REPORTING_TAG = new ABProp(6723, "31", "31");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MEX_GROUP_MIGRATION_ENABLED = new ABProp(6724, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MEX_GROUP_FLAGS = new ABProp(6725, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp STATUS_VIDEO_MAX_DURATION_SECOND = new ABProp(6728, "60", "60");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp VCARD_MAX_SIZE_KB = new ABProp(6736, "5000", "5000");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp IS_ROI_DATA_CONTROLLER_METRICS_ENABLED = new ABProp(6748, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GIMMICK_PHASE_TWO_DATA = new ABProp(6784, "{}", "{}");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GIMMICK_PHASE_TWO_DATA_SUFFIX = new ABProp(6785, "", "");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_NEW_MEDIA_AS_DOCUMENT_CHAT_MESSAGE_LAYOUT = new ABProp(6786, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GIMMICK_PHASE_TWO_LOGGING_ENABLED = new ABProp(6787, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp LID_STATUS_SEND_ENABLED = new ABProp(6791, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_STATUS_CONSUMPTION_DISABLE_TOOLTIP_ENABLED = new ABProp(6801, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_BUSINESS_TOOLS_DRAWER_ENABLED = new ABProp(6803, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_RECENT_SYNC_HANDLING_LOOP_RESTART_V2_ENABLED = new ABProp(6804, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_SMB_LABEL_REORDERING_M2_TWO_WAY = new ABProp(6805, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp IS_PMX_FUNNEL_METRICS_LOGGING_ENABLED = new ABProp(6816, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_LID_GROUPS_ALLOW_REFERENCE_BY_ALTERNATIVE_JID = new ABProp(6836, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp IS_PMX_HASHED_MSG_KEY_LOGGING_ENABLED = new ABProp(6837, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DATA_PRIVACY_PHASE_2_ENABLED = new ABProp(6843, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp STATUS_PTT_MAX_DURATION_SECOND = new ABProp(6845, "60", "60");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_INTERN_DOGFOODING_UPSELL_ENABLED = new ABProp(6858, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_INTERN_DOGFOODING_UPSELL_SNOOZE_DURATION = new ABProp(6859, "86400", "86400");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_INTERN_DOGFOODING_UPSELL_CONTENT = new ABProp(6860, "", "");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp UNIFIED_REACTION_ADDON_INFRA_ENABLED = new ABProp(6877, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ADV_ACCEPT_HOSTED_DEVICES = new ABProp(6939, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_AUDIO_FILES_SENDER_WAVEFORM_ENABLED = new ABProp(6943, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp INBOX_FILTERS_READ_UNREAD_LOGGING_ENABLED = new ABProp(6967, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MEDIA_SHARING_TEAM_HOLDOUT_H1_2024 = new ABProp(6972, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp LID_STATUS_CREATE_INDIVIDUAL_LID_CHATS = new ABProp(6978, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_AUDIO_FILES_DISPLAY_WAVEFORM_ENABLED = new ABProp(6996, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PAYMENTS_BR_PIX_PHASE_1_SELLER_SYNC_ENABLED = new ABProp(7024, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SAGA_COPY = new ABProp(7044, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_DB_TABLE_USAGE_ENABLED = new ABProp(7073, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BONSAI_PLUGINS_V2_ENABLED = new ABProp(7075, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SUPPORT_MESSAGE_FEEDBACK_ENABLED = new ABProp(7080, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BR_PAY_WITH_PIX_CTA_BUYER_ENABLED = new ABProp(7101, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BR_PAY_WITH_CARD_CTA_BUYER_ENABLED = new ABProp(7102, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BR_PAY_WITH_CARD_CTA_SELLER_ENABLED = new ABProp(7103, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp INBOX_FILTERS_SMB_ENABLED = new ABProp(7108, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp HD_MEDIA_TOOLTIP_LOGGING_ENABLED = new ABProp(7122, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_MULTI_ADMIN_TRANSFER_OWNERSHIP_ENABLED = new ABProp(7124, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_BMX_PAIRLESS_LOGGING_REFACTORING = new ABProp(7129, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DATA_PRIVACY_PHASE_2_NON_E2EE_ENABLED = new ABProp(7131, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_INACTIVE_RECEIPTS_V2 = new ABProp(7132, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_3PD_CONVERSION_SIGNAL_LOGGING = new ABProp(7138, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DM_INITIATOR_TRIGGER_GROUPS = new ABProp(7141, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_EXTRA_LID_GROUP_LOGGING_ATTRIBUTES = new ABProp(7145, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_PRODUCT_CAROUSEL_MESSAGE = new ABProp(7177, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp COMMUNITY_PERMISSIONS_M1 = new ABProp(7180, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GIMMICK_PHASE_TWO_CACHE_SIZE = new ABProp(7185, "20", "20");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_INBOX_COMPACT_STYLE_ENABLED = new ABProp(7197, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_ADMIN_REPLY_ENABLED = new ABProp(7211, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_MANAGE_ADS_TAB_WEB_RECOVERY_FLOW = new ABProp(7215, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp INBOX_FILTERS_SMB_ALWAYS_VISIBLE = new ABProp(7221, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_QUICK_FORWARDING_BUTTON_MODE = new ABProp(7234, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_ADMIN_REPLY_RECEIVER_ENABLED = new ABProp(7237, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ORDERS_TWO_FOLD_CONFIRMATION_ENABLED = new ABProp(7238, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_NUDGE_TO_UNMUTE_MODE = new ABProp(7239, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_MULTI_ADMIN_TRANSFER_OWNERSHIP_RECEIVER_ENABLED = new ABProp(7244, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_MULTI_M1_IMPROVEMENTS_ENABLED = new ABProp(7245, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_RECENT_SYNC_WORKER_COMPATIBLE_HANDLING = new ABProp(7247, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp EVENTS_CREATE_REGULAR_GROUPS_ENABLED = new ABProp(7258, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_FORWARD_LOGGING_V2_ADDITIONAL_FIELDS_ENABLED = new ABProp(7264, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp FAVORITES_LIMIT = new ABProp(7267, "100", "100");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_DISABLE_SW_ON_SAFARI_PWA = new ABProp(7281, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp VISIBLE_MESSAGE_DROP_PLACEHOLDER_ENABLED_INTERNAL_ONLY = new ABProp(7287, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp INBOX_FILTERS_SMB_MESSAGES_FOLDER = new ABProp(7293, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp INBOX_FILTERS_SMB_LABEL_DROPDOWN = new ABProp(7294, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_FORCE_VOIP_LOGGING = new ABProp(7300, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp NATIVE_CONTACT_COMPANION_CHANGE_ENABLED = new ABProp(7301, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp EVENTS_START_NOTIFICATION = new ABProp(7306, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_COUPONS_ENABLED = new ABProp(7313, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_CATALOG_GRAPHQL_CREATE_COLLECTION = new ABProp(7319, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_CATALOG_GRAPHQL_CREATE_PRODUCT_CATALOG = new ABProp(7320, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_CATALOG_GRAPHQL_GET_PRODUCT_CATALOG = new ABProp(7321, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_CATALOG_GRAPHQL_ADD_PRODUCT = new ABProp(7322, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_CATALOG_GRAPHQL_EDIT_PRODUCT = new ABProp(7323, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_CATALOG_GRAPHQL_GET_PRODUCT = new ABProp(7324, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_CATALOG_GRAPHQL_REPORT_PRODUCT = new ABProp(7325, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_CATALOG_GRAPHQL_APPEAL_PRODUCT = new ABProp(7326, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_CATALOG_GRAPHQL_UPDATE_COLLECTION = new ABProp(7327, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_CATALOG_GRAPHQL_UPDATE_COLLECTION_LIST = new ABProp(7328, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_CATALOG_GRAPHQL_DELETE_COLLECTION = new ABProp(7329, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_CATALOG_GRAPHQL_APPEAL_COLLECTION = new ABProp(7330, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_CATALOG_GRAPHQL_GET_COLLECTION = new ABProp(7331, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_CATALOG_GRAPHQL_GET_SINGLE_COLLECTION = new ABProp(7332, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_RECENT_SYNC_CHUNK_DOWNLOAD_OPTIMIZATION = new ABProp(7356, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp EVENTS_EDIT_SEND = new ABProp(7357, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp EVENTS_EDIT_RECEIVE = new ABProp(7358, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_PINNING_ENABLED = new ABProp(7387, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_QUICK_FORWARDING_MEDIA_ONLY = new ABProp(7389, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DM_INITIATOR_TRIGGER_DAILY_LOGS = new ABProp(7402, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_INTERACTIVE_SINGLE_PRODUCT_MESSAGE = new ABProp(7408, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp EVENTS_M3_ALLOW_GUESTS_SEND = new ABProp(7420, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp EVENTS_M3_ALLOW_GUESTS_RECEIVE = new ABProp(7421, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_AUTODOWNLOAD_STICKERS = new ABProp(7422, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp LID_GROUP_SEND_LID_MENTION = new ABProp(7439, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CUSTOM_RACING_EMOJI = new ABProp(7463, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PINNED_MESSAGES_M2_IMAGE_THUMBNAIL = new ABProp(7467, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp USERNAME_SECURITY_CODE_GENERATION = new ABProp(7468, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp USERNAME_SECURITY_CODE_VERIFICATION = new ABProp(7469, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SIMILAR_CHANNELS_IN_THREAD_ON_FOLLOW_ENABLED = new ABProp(7472, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SIMILAR_CHANNELS_IN_CHANNEL_DETAILS_ENABLED = new ABProp(7473, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GATE_NEW_INVITE_CONTACT_PICKER = new ABProp(7478, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MAX_PINNED_CHATS_COUNT = new ABProp(7489, "3", "3");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GROUP_MEMBER_RECOMMENDATIONS_TS_LOGGING = new ABProp(7492, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_RECENT_SYNC_NEXT_CHUNK_FETCH_OPTIMIZATION = new ABProp(7494, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_STICKER_SHARING_USER_JOURNEY_LOGGING = new ABProp(7503, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_IMAGE_VIDEO_SHARING_USER_JOURNEY_LOGGING = new ABProp(7504, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp EVENTS_M3_ATTENDEE_CHAT = new ABProp(7509, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp EVENTS_M3_COVER_IMAGE_SEND = new ABProp(7510, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp EVENTS_M3_COVER_IMAGE_RECEIVE = new ABProp(7511, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp USERNAME_CHAT_SEARCH_DEBOUNCE_INTERVAL_MS = new ABProp(7512, "1500", "1500");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp UPDATES_TAB_STATUS_LARGE_TILES_ENABLED = new ABProp(7516, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_NUDGE_TO_UNMUTE_SHOULD_SHOW_TOOLTIP = new ABProp(7529, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_NUDGE_TO_UNMUTE_MAX_NUDGES_COUNT = new ABProp(7530, "2", "2");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ESR_USE_NEW_XMPP_TYPE = new ABProp(7534, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp UWP_VOIP_INCOMING_CALL_NOTIFICATION_VERSION = new ABProp(7541, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_PAYMENT_NOTIFICATIONS_ACK_KICK_FIX_ENABLED = new ABProp(7546, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp QP_EDIT_PROFILE_BANNER_SURFACE_ENABLED = new ABProp(7549, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_PROFILE_PICTURE_DELETION_APPEALS_ENABLED = new ABProp(7552, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DSA_CHANNELS_ENFORCEMENT_PROFILE_PICTURE_DELETION_ENABLED = new ABProp(7553, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SIMILAR_CHANNELS_MAX_LIMIT = new ABProp(7559, "10", "10");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SIMILAR_CHANNELS_MIN_LIMIT = new ABProp(7560, "4", "4");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_ORDER_AMOUNT_AND_CURRENCY_LOGGING_ENABLED = new ABProp(7563, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp COEX_SYSTEM_MESSAGE_LOGGING_ENABLED = new ABProp(7564, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ADDON_INFRA_ENABLE_PERF_LOGGING = new ABProp(7567, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp STATE_FILTER_AA_TEXAS = new ABProp(7572, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_VIDEO_AUTOPLAY = new ABProp(7588, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DSA_INFORMATION_FOR_EU_ONLY_ENABLED = new ABProp(7592, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MEX_GROUP_GET_SUBGROUPS = new ABProp(7598, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp COMMUNITY_PERMISSIONS_M2 = new ABProp(7608, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MEX_GROUP_SET_ALLOW_NON_ADMIN_SUBGROUP_CREATION = new ABProp(7609, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_VALUE_HOLDOUT_H1_24_ENABLED = new ABProp(7617, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SERVER_GROUPS_RECEIVE_MEX_PROPERTY_UPDATE_NOTIF = new ABProp(7632, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CONTACT_PERMISSION_DEEPLINK_ENABLED = new ABProp(7633, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PROFILE_PICTURE_DEEPLINK_ENABLED = new ABProp(7634, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp INBOX_FILTERS_CUSTOM_SMB_ENABLED = new ABProp(7637, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BONSAI_COMPOSER_NEW_AI_CHAT_ENTRYPOINT_ENABLED = new ABProp(7639, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_GRAPHQL_TO_FETCH_QP_ENABLED = new ABProp(7645, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_GRAPHQL_TO_FETCH_QP_FREQUENCY_MINS = new ABProp(7646, "1320", "5");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_GRAPHQL_TO_FETCH_QP_SURFACE_IDS = new ABProp(7647, "", "");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MEX_GROUP_GET_GROUP_INFO = new ABProp(7653, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_QUICK_FORWARDING_DOWNLOAD_MISSING_MEDIA = new ABProp(7662, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp US_AA = new ABProp(7671, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_QPL_LOGGING = new ABProp(7677, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_UPDATES_TAB_FUZZY_SEARCH_ENABLED = new ABProp(7681, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_REACTIONS_BOTTOMSHEET_TAP_TO_REACT_ENABLED = new ABProp(7682, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_DIRECTORY_CATEGORIES_ENABLED = new ABProp(7685, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_DIRECTORY_QUICK_ACTION_BUTTON_ENABLED = new ABProp(7686, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ADVANCED_SECURITY_MENU = new ABProp(7691, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CALLING_32P_VERSION = new ABProp(7709, "0", "3");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_NOTES_V1_ENABLED = new ABProp(7710, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_OTHER_ITEMS_ON_NAVBAR_ENABLED = new ABProp(7732, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_DIRECTORY_CATEGORY_TYPES = new ABProp(7734, "3,7,6,4,1,5,2", "3,7,6,4,1,5,2");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WIN_NETWORK_STATE_WATCHDOG_INTERVAL = new ABProp(7737, "30", "30");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_MEDIA_STORAGE_MANAGEMENT_ENABLED = new ABProp(7761, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp INBOX_FILTERS_SUPPRESS_CONTACT_FILTER = new ABProp(7769, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DROP_INNER_MESSAGE_CONTEXT_INFOS_WHEN_SENDING = new ABProp(7772, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp KEY_HASH_IN_MSG_SEND_AND_MEDIA_UPLOAD_LOGGING = new ABProp(7773, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_OPTIMIZED_RESUME_HANDSHAKE_ENABLED = new ABProp(7805, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GROUP_MEMBER_RECOMMENDATIONS_M1 = new ABProp(7809, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SINGLE_E2EE_SESSION_MIGRATION_STATE_OUTGOING = new ABProp(7820, "2", "2");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SINGLE_E2EE_SESSION_MIGRATION_STATE_INCOMING = new ABProp(7821, "2", "2");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp EXPANDED_FORMATTING_V2_EXPANSION = new ABProp(7822, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WAMO_SUB_CONSUMER_ENABLED = new ABProp(7828, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WAMO_SUB_ADMIN_ENABLED = new ABProp(7829, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SEND_THUMB_MMS_STATUS_PJPEG_IMAGE = new ABProp(7835, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BONSAI_SUPPORTED_LANGUAGES = new ABProp(7848, "en", "en");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DEVICE_CAPABILITIES_SYNC_ENABLED = new ABProp(7853, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_COMMS_SOCKET_RECONNECT_ENABLED = new ABProp(7854, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PAYMENTS_BR_PIX_QUICK_REPLY_ENABLED = new ABProp(7857, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MEDIA_VIEWER_REACTION_ENABLED = new ABProp(7859, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MEDIA_VIEWER_REPLIES_ENABLED = new ABProp(7860, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_MESSAGE_DROP_BULK_DB_OPERATION_FALLBACK_ENABLED = new ABProp(7865, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp STATUS_MENTIONS_RECEIVER = new ABProp(7869, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_STICKER_VERIFICATION_FOR_GIMMICK = new ABProp(7886, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp COMMUNITY_PERMISSIONS_M1_5 = new ABProp(7889, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MAX_MESSAGE_PER_CHUNK_COUNT_TUNING_ENABLED = new ABProp(7897, "5000", "5000");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_MUTEX_PING_TIMEOUT_SECONDS = new ABProp(7898, "10", "10");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MEX_GROUP_QUERY_INVITE_LINK = new ABProp(7908, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp NOTIFICATION_PERMISSION_DEEPLINK_ENABLED = new ABProp(7911, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CREATE_NEW_GROUP_DEEPLINK_ENABLED = new ABProp(7912, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AUDIO_SHARING_ENABLE_NOTICE = new ABProp(7917, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_POLL_VOTERS_SUMMARY_CACHE_TTL_MS = new ABProp(7919, "120000", "120000");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_POLL_VOTERS_DETAILS_CACHE_TTL_MS = new ABProp(7920, "300000", "300000");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_WASM_WORKER_ENABLED_WWW = new ABProp(7924, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp EVENTS_EDIT_AND_CANCEL_SYSTEM_MESSAGES = new ABProp(7941, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PRIVACY_DISABLE_LINK_PREVIEWS = new ABProp(7955, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp STATUS_DEEPLINK_ENABLED = new ABProp(7965, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp META_VERIFIED_BADGE_EDUCATION_VAI_CONTENT = new ABProp(7976, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DIRECTORY_CATEGORIES_NEWSLETTERS_PER_CATEGORY_LIMIT = new ABProp(7986, "10", "10");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp LIFT_ME_UP_M1 = new ABProp(8000, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BONSAI_REMOVE_META_AI_SHORTCUT_SETTING_SWITCH = new ABProp(8002, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BONSAI_META_AI_SHORTCUT_TOS_ENABLED = new ABProp(8004, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_OFFLINE_RESUME_MAX_BATCH_SIZE = new ABProp(8006, "200", "400");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_LONG_TERM_HOLDOUT_CONTENT_ENABLED = new ABProp(8015, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_SOCKET_PARALLEL_CONNECTION_ENABLED = new ABProp(8019, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_SEARCH_EXPERIENCE_ENABLED = new ABProp(8025, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_SEARCH_NULL_STATE_ENABLED = new ABProp(8026, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SUSPICIOUS_LINK_RTLO_UNLINKIFIED = new ABProp(8028, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp REMOVE_OPTIONAL_DEBUG_INFO = new ABProp(8036, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_SINGLE_SIGNAL_SESSION_AND_IDENTITY_READ_USING_LID = new ABProp(8046, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_FORWARD_FROM_CHAT_TO_CHANNEL_ENABLED = new ABProp(8054, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_VERIFIED_BADGE_IN_COMPACT_INBOX_ENABLED = new ABProp(8059, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PARENT_GROUP_IMPROVE_SUBGROUP_ACTIVATION_VARIANT = new ABProp(8062, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_SEARCH_MAX_NUM_SUGGESTIONS = new ABProp(8076, "5", "5");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp XPLAT_ATTACHMENT_FORMAT_CHECK_V2 = new ABProp(8082, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_SEARCH_NULL_STATE_UPDATE_INTERVAL = new ABProp(8100, "86400", "86400");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_RECOMMENDED_V3_UI_ENABLED = new ABProp(8108, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_STICKY_HD_PHOTO_SETTING_ENABLED = new ABProp(8115, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp STATUS_THUMB_IGNORE_THUMB_IN_MESSAGE = new ABProp(8122, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp STATUS_THUMB_PREFER_PROGRESSIVE_OVER_MMS = new ABProp(8123, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_JSHALT_PING_AND_RECONNECT_VERSION = new ABProp(8131, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_BUSINESS_FUNNEL_LOGGING = new ABProp(8140, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp APP_EXIT_REASON_VERSION = new ABProp(8147, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_DIRECTORY_CATEGORIES_CACHE_REFRESH_INTERVAL_MS = new ABProp(8151, "86400000", "600000");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BR_ENABLE_PAYMENT_LOGOS_ON_BUBBLE = new ABProp(8160, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_AD_ACCOUNT_TOKEN_STORAGE_KILL_SWITCH_WEB = new ABProp(8166, "true", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_RECOMMENDED_V3_UI_LIMIT = new ABProp(8167, "5", "5");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SEARCH_THE_WEB_DIALOG_REDESIGN = new ABProp(8171, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_LARGER_LINK_PREVIEWS = new ABProp(8172, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CALLING_UX_LOGGING_BITMAP = new ABProp(8175, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BR_PIX_BIZ_PROFILE_ENTRYPOINT_ENABLED = new ABProp(8178, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MEX_GROUP_GET_GROUP_INFO_MODE = new ABProp(8179, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_LID_CALL_LINK = new ABProp(8180, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BONSAI_USER_JURISDICTION_ENUM = new ABProp(8181, "row", "us");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_BUSINESS_TOOLS_TOP_CARD_ENABLED = new ABProp(8191, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_STICKER_SHARING_USER_JOURNEY_QPL_LOGGING = new ABProp(8193, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp STATUS_THUMB_MMS_COMPRESSION = new ABProp(8216, "0.47999998927116394", "0.47999998927116394");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp STATUS_THUMB_MMS_MAX_EDGE_SIZE = new ABProp(8217, "96", "96");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp STATUS_THUMB_MMS_ASPECT_FILL = new ABProp(8218, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNEL_TO_CHANNEL_FORWARDING_LOGGING_ENABLED = new ABProp(8227, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_ONE_REQUEST_PTT_UPLOAD_PROTO = new ABProp(8264, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_RECENT_SYNC_CHUNK_DATA_HANDLING_WORKER = new ABProp(8270, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CATALOG_VIDEO_UPLOAD = new ABProp(8272, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CALL_INFO_MANAGER_VERSION = new ABProp(8303, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp EVENT_SHOW_ADD_TO_CALENDAR_OPTION = new ABProp(8309, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_META_VERIFIED_CONTEXT_CARD = new ABProp(8313, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp REPORT_BLOCK_IMPROVEMENTS_FOR_GROUPS_ENABLED = new ABProp(8327, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp NULL_STATE_META_AI_MODEL_VERSION = new ABProp(8331, "Llama 3", "Llama 3");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_PENDING_MESSAGE_CACHE_ENABLED = new ABProp(8353, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp UNIFIED_PIN_ADDON_TABLE_ENABLED = new ABProp(8356, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp EVENTS_SEND_ONE_ON_ONE_CHAT = new ABProp(8357, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_MV_CUSTOM_URL_SCALING_ENABLED = new ABProp(8359, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp IMPROVE_SUBGROUP_ACTIVATION_LINK_TO_EXISTING_COMMUNITY = new ABProp(8377, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp EVENTS_ADVANCED_LOCATION_INPUT = new ABProp(8380, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_INVITE_CLOSE_LOOP = new ABProp(8400, "-1", "-1");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_OFFLINE_MESSAGE_PROCESSOR_TIMEOUT_SECONDS = new ABProp(8406, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_SEARCH_NULL_STATE_ROW_COUNT = new ABProp(8407, "3", "3");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MEX_USYNC_LID_QUERY = new ABProp(8420, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MEX_USYNC_USERNAME_QUERY = new ABProp(8421, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp EVENTS_SEARCH_INDEXING_SUPPORT = new ABProp(8451, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WINDOWS_GRACEFUL_DEGRADATION_VERSION = new ABProp(8454, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEBC_PAGE_LOAD_EARLY_COMMIT_ENABLED = new ABProp(8458, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SEARCH_THE_WEB_URL_OFFER = new ABProp(8473, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_CONSUMER_FUNNEL_LOGGING = new ABProp(8492, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp COMMUNITY_TRANSFER_OWNERSHIP_RECEIVER = new ABProp(8499, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MESSAGE_PARTIAL_SELECTION = new ABProp(8502, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BIZ_AI_SMB_AGENTS_AUTOMATIC_REPLY_ENABLED = new ABProp(8505, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp EVENTS_M3_END_TIME_SEND = new ABProp(8508, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp EVENTS_M3_END_TIME_RECEIVE = new ABProp(8509, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp COMMUNITY_HIDDEN_GROUPS_ENABLED = new ABProp(8510, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ORDERS_FROM_CART_WITHOUT_PRICE_ENABLED = new ABProp(8511, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp UPDATES_TAB_STATUS_TILES_MODE_WITH_NEWSLETTERS = new ABProp(8521, "0", "1");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp UPDATES_TAB_STATUS_TILES_MODE_NO_NEWSLETTERS = new ABProp(8522, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp STATUS_THUMB_PROGRESSIVE_SCAN_NUMBER = new ABProp(8523, "1", "1");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_CALL_CONTROL_M5 = new ABProp(8524, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ALBUM_V2_RECEIVING_ENABLED = new ABProp(8528, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ALBUM_V2_SENDER_ENABLED = new ABProp(8529, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp COMMUNITY_GROUP_VISIBILITY_ENABLED = new ABProp(8530, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_SS_32P_LOGGING = new ABProp(8538, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp IMPROVE_SUBGROUP_ACTIVATION_NEW_SUBGROUP_POLL_INTERVAL = new ABProp(8541, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp IMPROVE_SUBGROUP_ACTIVATION_SUBGROUP_POLL_INTERVAL = new ABProp(8542, "43200", "43200");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_EXPRESSION_PANELS_SHOW_LESS_STICKERS_V_2 = new ABProp(8545, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_COMMUNITIES_GENERAL_CHAT_V_2 = new ABProp(8580, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp UNIFIED_SESSION_VERSION = new ABProp(8581, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp UNIFIED_SESSION_LOG_CALL_EVENT = new ABProp(8582, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DSA_ARTICLE21_CHANNEL_SUSPEND_ENABLED = new ABProp(8591, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DSA_ARTICLE_21_CHANNEL_GEOSUSPEND_ENABLED = new ABProp(8592, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DSA_APPEALS_QUERY_OPTIMIZATION_ENABLED = new ABProp(8593, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DSA_ARTICLE_21_CHANNEL_PROFILE_PICTURE_DELETION_ENABLED = new ABProp(8594, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DSA_ARTICLE21_CHANNEL_VIOLATING_MESSAGES_ENABLED = new ABProp(8595, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp LIFT_ME_UP_M2 = new ABProp(8596, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CATALOG_VIDEO_MAX_SIZE_MB = new ABProp(8606, "50", "50");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CALLING_DIALER_ENABLE = new ABProp(8607, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_STATUS_LIKES_RECEIVE_ENABLED = new ABProp(8611, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp TEXT_USER_JOURNEY_LOGGING_WAM_ENABLED = new ABProp(8627, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PTT_USER_JOURNEY_LOGGING_WAM_ENABLED = new ABProp(8630, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_UPDATES_TAB_LONG_PRESS_ACTIONS_ENABLED = new ABProp(8651, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_UPDATES_TAB_GROUP_ACTIONS_ENABED = new ABProp(8652, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_UPDATES_TAB_SWIPE_ACTIONS_ENABLED = new ABProp(8653, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_AD_ACCOUNT_NONCE_RETRIES_MAX_WEB = new ABProp(8663, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_AD_ACCOUNT_NONCE_PUSH_WAIT_TIMEOUT_WEB = new ABProp(8664, "20", "20");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp COEX_STATUS_REPLY_PRIVACY_DISCLAIMER_ENABLED = new ABProp(8674, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_FLUSH_MESSAGE_CACHE_BEFORE_SENDING_PREACKS_ENABLED = new ABProp(8683, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_METABOT_SEND_IMAGE_LIMIT = new ABProp(8685, "1", "1");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_INBOX_ACTIONS_ENABLED = new ABProp(8686, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp INVITE_FROM_CALL_LIST_SEARCH = new ABProp(8711, "-1", "-1");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp STATUS_LIKES_LOGGING_ENABLED = new ABProp(8728, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CAMERA_HEALTH_CHECK_DELAY = new ABProp(8739, "5000", "5000");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CAMERA_HEALTH_CHECK_PERIOD = new ABProp(8740, "2000", "2000");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp STATUS_POSTER_SIDE_GATING_ENABLED = new ABProp(8742, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp LOTTIE_STICKER_RENDERING_KILLSWITCH = new ABProp(8743, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_META_VERIFIED_TIER_LOGGING_ENABLED = new ABProp(8758, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp USERNAME_MEX_ACCOUNT_SYNC_ENABLED = new ABProp(8763, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_BACKGROUND_SYNC_V2 = new ABProp(8782, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MESSAGE_ASSOCIATION_INFRA_ENABLED = new ABProp(8783, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp LID_MIGRATION_NOTIFICATIONS_ENABLED = new ABProp(8785, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DSA_NEW_USER_JOURNEY_ENABLED = new ABProp(8786, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp EVENT_COVER_IMAGE_RECEIVER_ENABLED = new ABProp(8792, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp EVENT_COVER_IMAGE_SENDER_ENABLED = new ABProp(8793, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp META_VARIANTS_VIEW_ENABLED = new ABProp(8798, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_CATALOG_GRAPHQL_GET_PRODUCT_LIST = new ABProp(8799, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GRAPHQL_GET_PRODUCT_LIST = new ABProp(8800, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_PARSE_ALWAYS_SHOW_AD_ATTRIBUTION = new ABProp(8804, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp APPDATA_META_SENDING_ENABLED = new ABProp(8814, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GROWTH_DRY_RUN_CONNECTICUT_HOLDOUT_PLACEHOLDER_ABPROP = new ABProp(8816, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp FIELDSTATS_EVENT_VALIDATION_SKIP_CONFIG = new ABProp(8834, "\"\"", "\"\"");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp FIELDSTATS_VALIDATION_ENABLED = new ABProp(8835, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp INBOX_FILTERS_CONTEXT_MENU_ENABLED = new ABProp(8840, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp UPDATES_TAB_STATUS_TILES_QPL_LOGGING_ENABLED = new ABProp(8842, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp RT_VALIDATE_REPORTING_TOKEN = new ABProp(8858, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp RT_STORE_REPORTING_DATA = new ABProp(8859, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp RT_SENDER_REPORTING_TOKEN_VERSION = new ABProp(8860, "2", "2");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_WAME_MESSAGE_SUPPORT = new ABProp(8884, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_MINIMIZE_INDIVIDUAL_MUTATION_WRITE = new ABProp(8910, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp INBOX_FILTERS_FAVORITES_ENABLED_COMPANIONS = new ABProp(8928, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_EXPRESSION_PANEL_2 = new ABProp(8950, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PAYMENTS_BR_FORCE_COPY_PIX_CTA_ENABLED = new ABProp(8953, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_PRODUCER_INSIGHTS_ENABLED = new ABProp(8960, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_PAYMENT_LINKS_URL_REGEX_LIST = new ABProp(8969, "{}", "{}");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_PAYMENT_LINKS_PSPS = new ABProp(8970, "{}", "{}");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PAYMENTS_BR_COPY_PIX_CODE_API_MERCHANT_ENABLED = new ABProp(9017, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp COMMUNITY_MEDIA = new ABProp(9027, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_ORDERS_GRAPHQL_GET_ORDER_INFO = new ABProp(9030, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_ORDERS_GRAPHQL_PLACE_ORDER = new ABProp(9031, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_ORDERS_GRAPHQL_REFRESH_CART = new ABProp(9032, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_JS_HALT_ACTIVE_PING_TIMEOUT = new ABProp(9042, "200", "200");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_MUTEX_BLOCK_STRATEGY = new ABProp(9047, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp EVENTS_M3_PIN_CUSTOMIZATION_RECEIVE = new ABProp(9062, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp EVENTS_M3_PIN_CUSTOMIZATION_SEND = new ABProp(9063, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_ADDITIONAL_USER_JOURNEY_LOGGING = new ABProp(9064, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_PROMISE_QUEUE_WAIT_MUTATION_BUG_FIX = new ABProp(9065, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_CHAT_LIST_STICKER_EMOJIS = new ABProp(9069, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp RT_SEND_REPORTING_DATA = new ABProp(9070, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PAYMENTS_BR_MERCHANT_PSP_ACCOUNT_STATUS_SYNC = new ABProp(9076, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp LAZY_SYSTEM_MESSAGE_INSERTION_ENABLED = new ABProp(9077, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp REMOVE_SINGLE_EMOJI_BUBBLE_BACKGROUND_ENABLED = new ABProp(9083, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp OFFLINE_RESUME_ORPHAN_ADDONS_WITH_FAILED_PARENTS = new ABProp(9107, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp EVENTS_M3_DYNAMIC_PIN_EXPIRY_RECEIVE = new ABProp(9108, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp UPDATES_TAB_STATUS_TILES_MIN_VISIBLE_COUNT = new ABProp(9124, "3.5", "3.5");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_MEDIA_EDITOR_IMAGE_FILTER_ENABLED = new ABProp(9146, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp FLOWS_TERMINATION_MESSAGE_V2_SENDING_ENABLED = new ABProp(9157, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_METABOT_IMAGE_INPUT_LANGUAGES = new ABProp(9163, " ", "en");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BUMP_RECEIVER_EXISTING_THREAD_WITH_INVITER = new ABProp(9173, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SEND_INVALID_PROTOBUF_NACK_FAILURE_REASON = new ABProp(9174, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BUMP_SENDER_EXISTING_THREAD_FROM_INVITE_NOTIF = new ABProp(9177, "-1", "-1");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_COMMUNITY_PERMISSIONS_ENABLED = new ABProp(9179, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp EVENTS_M3_REMINDERS_SEND = new ABProp(9180, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_GRAPHQL_TOKEN_RECOVERY_DURING_ACCOUNT_RECOVERY_ENABLED = new ABProp(9197, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_SEND_MESSAGE_CONTEXT_IN_OUTER_LAYER = new ABProp(9202, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp HASH_IDENTITY_KEYS_FOR_QR_CODE_DEVICE_VERIFICATION = new ABProp(9211, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_PAYMENT_LINKS_LOGGING_ENABLED = new ABProp(9213, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_STORAGE_VERSION_SQUASH_STRATEGY = new ABProp(9218, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_CTWA_CHAT_HEADER_LABEL_ENTRY_POINT_ENABLED = new ABProp(9223, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_MEDIA_EDITOR_NEW_TOOLBAR = new ABProp(9251, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp EVENTS_M3_MAYBE_RESPONE_SEND = new ABProp(9277, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp EVENTS_M3_MAYBE_RESPONSE_RECEIVE = new ABProp(9278, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_UNWRAP_MESSAGE_FOR_STANZA_ATTRIBUTES = new ABProp(9290, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp VERIFIED_BADGE_IN_CHATS_LIST_ENABLED = new ABProp(9292, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp EMOJI_SEARCH_TOKENIZE = new ABProp(9296, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_NEW_CUSTOMER_LABEL_SIGNALS = new ABProp(9302, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_WAM_DATA_LOSS_MITIGATION_BUFFERING_DURATION = new ABProp(9307, "5", "5");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_WAM_DATA_LOSS_MITIGATION_ROTATE_INTERVAL = new ABProp(9308, "120", "120");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DIRECTORY_CATEGORIES_DISPLAY_NEWSLETTERS_PER_CATEGORY_LIMIT = new ABProp(9312, "4", "4");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_LAST_LINE_LINKS_PADDING_ENABLED = new ABProp(9342, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp OPTIMIZED_DELIVERY_SIGNAL_COLLECTION_ENABLED = new ABProp(9348, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_REMINDERS_ENABLED = new ABProp(9379, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_STICKER_DOWNLOAD_M1 = new ABProp(9406, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNEL_FORWARD_BOTTOM_BUTTON_ENABLED = new ABProp(9422, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_UI_REDESIGN_M1_NAVBAR_VARIANT = new ABProp(9426, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MEX_GROUP_GET_GROUP_INFO_BY_INVITE_CODE = new ABProp(9428, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp LID_ONE_ON_ONE_MIGRATION_ENABLED = new ABProp(9435, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_PRODUCER_INSIGHTS_MIN_FOLLOWERS = new ABProp(9447, "100", "100");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DIRECTORY_CATEGORIES_ENDLESS_SCROLLING_ENABLED = new ABProp(9448, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp STATUS_RANKING_POSTER_SIDE_GATING_ENABLED = new ABProp(9453, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp COLLAPSING_RECOMMENDED_CHANNELS_ENABLED_BY_DEFAULT = new ABProp(9477, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_PDFN_TOS_SHORTCUT_NOTICE_ID = new ABProp(9482, " ", " ");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_PDFN_TOS_INVOKE_NOTICE_ID = new ABProp(9483, " ", " ");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MEDIA_VIEWER_DEFAULT_REACTION_ENABLED = new ABProp(9486, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_WAM_MAX_BUFFER_UPLOAD_SIZE_BYTES = new ABProp(9501, "64000", "64000");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp VOIP_CALL_COORDINATOR_VERSION = new ABProp(9502, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp FLOWS_RESPONSE_DOWNLOAD_BUTTON_ENABLED = new ABProp(9509, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_RESET_LID_CALL_LINK = new ABProp(9516, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp STATUS_FUTURE_PROOFING = new ABProp(9522, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MEX_USYNC_ABOUT_STATUS = new ABProp(9524, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BONSAI_FP_UGC_SENDER = new ABProp(9541, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SEARCH_THE_WEB_IMAGE_SEARCH = new ABProp(9547, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SEARCH_THE_WEB_TEXT_SEARCH = new ABProp(9548, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ALWAYS_INCLUDE_GROUP_ID_IN_PROFILE_PHOTO_REQUEST = new ABProp(9562, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp RT_CLEAN_REPORTING_TOKEN = new ABProp(9567, "31", "31");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_OPTIMIZE_ACTIVE_MSG_RANGE_OFFLINE_RESUME_ENABLED = new ABProp(9575, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_LEAD_LABELS_SEND_SIGNALS = new ABProp(9593, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_AD_CREATION_ENTRY_POINT_CATALOG_WEB = new ABProp(9596, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp LISTS_LABELS_MIGRATION_ENABLED = new ABProp(9597, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_EXPERIMENT_GRAPHQL_CONFIG = new ABProp(9601, " ", " ");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp UNIFIED_SESSION_LOG_TS_EVENT = new ABProp(9611, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WIN_ENABLE_SS_BUTTON_AUDIO = new ABProp(9633, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_ADMIN_INSIGHTS_GIZMOS_ENABLED = new ABProp(9641, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp LID_MIGRATION_FOR_BIZ_APP_ENABLED = new ABProp(9652, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_LOGGING_USER_TO_DEVICE_SIZE_BUCKET = new ABProp(9656, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp EVENTS_M3_RSVP_NOTIFICATION_GROUPING = new ABProp(9665, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PROFILE_SCRAPING_PRIVACY_TOKEN_IN_PHOTO_IQ = new ABProp(9666, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PROFILE_SCRAPING_PRIVACY_TOKEN_IN_USYNC = new ABProp(9667, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PROFILE_SCRAPING_PRIVACY_TOKEN_IN_ABOUT_IQ = new ABProp(9668, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SINGLE_EMOJI_LOGGING_ENABLED = new ABProp(9669, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_CATALOG_VIDEO_VIEW_ENABLED = new ABProp(9671, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_CATALOG_VIDEO_VIEW_FALLBACK_ENABLED = new ABProp(9672, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_BUSY_REASON_FS = new ABProp(9674, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_AD_CREATION_ENTRY_POINT_CATALOG_PRODUCT_WEB = new ABProp(9677, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_PTT_MAIN_GATE_SUPPORTED_LANGUAGES = new ABProp(9694, " ", "en");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_CTWA_WEB_HIDE_AD_CONTEXT_IF_SOFT_DISMISSED_IN_PRIMARY = new ABProp(9729, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MEX_USYNC_DDM_QUERY = new ABProp(9731, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MEX_USYNC_BIZ_QUERY = new ABProp(9732, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MEX_USYNC_DEVICES_QUERY = new ABProp(9733, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MEX_USYNC_PICTURE_QUERY = new ABProp(9736, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp USE_PER_CHAT_WALLPAPER = new ABProp(9756, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ANIMATED_EMOJI_FINAL_SET_ENABLED = new ABProp(9757, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ANIMATED_EMOJI_SET_1_ENABLED = new ABProp(9758, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_HD_MEDIA_GLOBAL_SETTING_ENABLED = new ABProp(9778, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_PRODUCER_INSIGHTS_HIDE_DELTAS = new ABProp(9792, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MEDIA_SHARING_TEAM_HOLDOUT_H2_2024 = new ABProp(9797, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MESSAGING_EXPERIENCE_TEAM_HOLDOUT_H2_2024 = new ABProp(9798, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_MARKETING_MESSAGE_FMX_ENABLED = new ABProp(9804, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp STATUS_MENTIONS_IN_CHAT_RECEIVER = new ABProp(9816, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp STATUS_MENTIONS_IN_CHAT_SENDER = new ABProp(9817, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp RT_REPORT_TOKEN_FROM_INCLUSION_LIST = new ABProp(9818, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_TOKEN_RECOVERY_NOT_ME_ENABLED = new ABProp(9828, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_TOKEN_RECOVERY_FULL_SCREEN_ENABLED = new ABProp(9829, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WHATSAPP_VPV_LOGGING_ENABLED = new ABProp(9833, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_VPV_LOGGING_ENABLED = new ABProp(9834, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_NOTES_PRIVACY_STRING = new ABProp(9843, "2", "2");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BR_PAY_WITH_PAYMENT_LINK_CTA_BUYER_ENABLED = new ABProp(9847, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp RBI_PRE_TICK_REPORT = new ABProp(9871, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BRIGADING_PRIVACY_SETTING_ENABLED = new ABProp(9876, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp STATUS_RANKING_TOP_CONTACTS_COUNT = new ABProp(9921, "30", "30");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SAGA_V1_REENGAGEMENT_ENABLED = new ABProp(9924, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp EVENTS_CREATE_CAG_ENABLED = new ABProp(9932, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_REACTION_NOTIFICATION_VIA_ADD_ON_API = new ABProp(9933, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SAGA_V1_ENABLED = new ABProp(9942, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SAGA_V1_NUX_ENABLED = new ABProp(9944, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp LID_ONE_TO_ONE_MIGRATION_OUTGOING_STANZAS = new ABProp(9987, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MM_MESSAGE_LEVEL_FEEDBACK_ENABLED = new ABProp(10011, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WALLPAPER_UPLOAD = new ABProp(10015, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WABBA_ENABLED = new ABProp(10024, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp IGNORE_JOINABLE_TERMINATE_IN_ACCEPT_SENT_STATE = new ABProp(10045, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_CALLING_DEEP_LINK_ERROR = new ABProp(10051, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ALWAYS_INCLUDE_GROUP_ID_IN_USYNC_REQUEST = new ABProp(10080, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_MAX_SOCKET_LOOP_WAIT_TIME_SEC = new ABProp(10087, "900", "900");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MM_LOG_LINKS_CLICKED_IN_MESSAGE_BODY = new ABProp(10097, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_RING_FOR_GC_ON_OFFER_EXPIRE = new ABProp(10103, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MEX_GROUPS_GET_PARTICIPATING_GROUPS = new ABProp(10118, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_MESSAGE_LABEL_DEPRECATION_PHASE = new ABProp(10165, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp EVENTS_M3_SEARCH_SUPPORT_ENABLED = new ABProp(10173, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_DIRECTORY_CATEGORIES_LOGGING_ENABLED = new ABProp(10188, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp INBOX_FILTERS_FAVORITES_DEEPLINK_ENABLED = new ABProp(10195, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CATEGORY_DEEPLINK_ENABLED = new ABProp(10215, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp COLLECTION_DEEPLINK_ENABLED = new ABProp(10216, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CATEGORY_AND_COLLECTION_DEEPLINKS_CATALOG_FILTER = new ABProp(10218, "917977079770", "16005554444");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_MESSAGE_LABEL_DEPRECATION_CHAT_LIST_BANNER_COOL_DOWN_SECONDS = new ABProp(10219, "432000", "432000");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_MESSAGE_LABEL_DEPRECATION_MAX_VOLUME = new ABProp(10220, "100", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_MESSAGE_LABEL_DEPRECATION_TARGET_DATE = new ABProp(10221, "2024-09-19", "2024-09-19");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp COLLECTION_AND_CATEGORY_DEEPLINK_LOGGING_ENABLED = new ABProp(10224, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_WEFR_CLIENT_EXPO_PULSE = new ABProp(10230, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_OFFLINE_STAGE_MANAGER_SINGLETON_ENABLED = new ABProp(10235, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_UWP_SWAP_VIDEO_STREAM = new ABProp(10241, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp METAAI_DOSA = new ABProp(10248, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp HIDE_CHANNEL_TOMBSTONE_MESSAGES_ENABLED = new ABProp(10259, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_NOTES_CONTENT_MAX_LIMIT = new ABProp(10272, "5000", "5000");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp IGNORE_ONE_TO_ONE_TERMINATE_IN_GROUP_CALL = new ABProp(10273, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp OPTIMIZED_DELIVERY_SIGNAL_COLLECTION_CONFIG = new ABProp(10302, "{}", "{}");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp OPTIMIZED_DELIVERY_TOKENS_STORAGE_CONFIG = new ABProp(10303, "{}", "{}");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp LISTS_FEATURE_ENABLED = new ABProp(10313, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_FETCH_AND_LOG_CAPABILITIES = new ABProp(10325, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_CAPABILITIES_ENABLED = new ABProp(10328, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_MARKETING_MESSAGE_BODY_LINKS_ENABLED = new ABProp(10336, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_HOME_GRAPHQL_ENABLED = new ABProp(10344, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_MM_BIZ_AI_DISCLOSURE_UPDATE_ENABLED = new ABProp(10379, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_MM_BIZ_AI_TOS_FILTERING_ENABLED = new ABProp(10388, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_PAYMENT_LINKS_SELLER_LOGGING_ENABLED = new ABProp(10389, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNEL_POLL_FORWARDING_ENABLED = new ABProp(10412, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNEL_POLL_STATUS_CARD_ENABLED = new ABProp(10413, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp POLL_RESULT_SNAPSHOT_MESSAGE_RECEIVER_ENABLED = new ABProp(10414, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_UWP_DEVICE_SWITCH_BANNER = new ABProp(10416, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp RECEIVE_STICKER_PACK = new ABProp(10419, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WINDOWS_SS_CAPTURE_DRIVER_TYPE = new ABProp(10434, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp REACTION_USER_JOURNEY_LOGGING_ENABLED = new ABProp(10438, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_GET_SUBSCRIPTIONS_GRAPHQL_MIGRATION_ENABLED = new ABProp(10441, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_AGENT_CHAT_LIST_INDICATOR_ENABLED = new ABProp(10455, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_AGENT_THREAD_CONTROL_NOTIFICATION_ENABLED = new ABProp(10456, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WHATSAPP_AUTHENTICATED_GRAPHQL_QPL_LOGGING_ENABLED = new ABProp(10462, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_META_VERIFIED_DEEPLINK_HANDLING_ENABLED = new ABProp(10469, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp COEX_LAZY_INSERTION_LOGGING_ENABLED = new ABProp(10487, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNEL_PTT_STATUS_CARD_ENABLED = new ABProp(10513, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SHARE_CONTENT_USER_JOURNEY_LOGGING_WAM_ENABLED = new ABProp(10516, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PRIVACY_TOKEN_SENDING_ON_ALL_1_ON_1_MESSAGES = new ABProp(10518, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_CUSTOM_URL_GET_USER_GRAPHQL_MIGRATION_ENABLED = new ABProp(10519, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_COMMUNITY_SUSPEND_AND_APPEALS = new ABProp(10539, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CALL_OFFER_FAILED_SOFT_LANDING_SCREEN_VERSION = new ABProp(10559, "0", "1");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_CALL_RESULT_FIX_FOR_404_ACCEPT_NACK = new ABProp(10565, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BR_ORDER_MESSAGE_COPY_PIX_KEY_CTA_ENABLED = new ABProp(10566, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CAMERA_ERROR_BANNERS_VERSION = new ABProp(10584, "0", "1");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SAGA_V1_CAROUSEL = new ABProp(10609, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_STORE_LABEL_USAGE_ENABLED = new ABProp(10618, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_STORE_LABEL_USAGE_MAX_LABEL_COUNT = new ABProp(10619, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ANIMATED_EMOJIS_OPTIMIZED_ASSET_ENABLED = new ABProp(10639, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp REMOVE_BOT_INVOKE_FUTUREPROOF_WRAPPING = new ABProp(10646, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNEL_TOMBSTONE_MESSAGES_VISIBILITY_DISABLED = new ABProp(10657, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CALL_FAVORITES_ENABLED_COMPANIONS = new ABProp(10666, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MM_MESSAGE_LEVEL_FEEDBACK_ANALYTICS_ENABLED = new ABProp(10667, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MM_MESSAGE_LEVEL_FEEDBACK_NOT_INTERESTED_MENU_ENABLED = new ABProp(10668, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MM_MESSAGE_LEVEL_FEEDBACK_TEXT_OVERRIDE = new ABProp(10669, "", "");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp COMMUNITY_HIDDEN_GROUPS_RECEIVER_ENABLED = new ABProp(10689, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CATALOG_VIDEO_VIEW_ENABLED = new ABProp(10691, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_STICKER_PACK_MESSAGES_SENDING = new ABProp(10692, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp QUICK_PROMOTION_SETTINGS_BANNER_CLIENT_ENABLED = new ABProp(10707, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ALBUM_V2_FORWARD_AS_ALBUM_ENABLED = new ABProp(10725, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ALBUM_V2_REACTIONS_ENABLED = new ABProp(10726, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ALBUM_V2_REPLYING_ENABLED = new ABProp(10727, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MM_MESSAGE_LEVEL_FEEDBACK_SERVER_UNDO_ENABLED = new ABProp(10749, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_SEND_OPTIMISTIC_UPDATE = new ABProp(10755, "'[]'", "");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp INBOX_LIST_BASED_NOTIFICATION_ENABLED = new ABProp(10772, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_NON_BLOCKING_FIRE_UI_UPDATE = new ABProp(10795, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MEX_GROUPS_GET_LINKED_SUBGROUP = new ABProp(10797, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MEX_GROUPS_BATCH_GET_GROUP_INFO = new ABProp(10805, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_MESSAGE_DISCLAIMER_ENABLED = new ABProp(10817, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_HISTORY_SYNC_ALLOW_DUPLICATE_IN_BULK_ERROR = new ABProp(10842, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ALBUM_V2_ITEM_WITH_CAPTION_IN_ALBUM_ENABLED = new ABProp(10847, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ALBUM_V2_MIN_ITEMS_TO_SEND_AS_ALBUM_ENABLED = new ABProp(10848, "4", "4");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_VIEW_MODE_USAGE_ENABLED = new ABProp(10856, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_VOIP_STACK_ENC_REJECT = new ABProp(10858, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BRITISH_ENGLISH_LOCALIZATION_ENABLED_WEB = new ABProp(10865, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SHARE_PAYMENT_STATUS_WITH_API_MERCHANT = new ABProp(10873, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_RICH_RESPONSE_LISTS_ENABLED = new ABProp(10886, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CREATE_GROUP_AS_LID = new ABProp(10956, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WINRT_RENDERER = new ABProp(10966, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WABBA_RECEIVER_ENABLED = new ABProp(10970, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MUSIC_OHAI_PROXY_URL = new ABProp(10975, "https://meta-ohttp-relay-prod.fastly-edge.com/", "https://meta-ohttp-relay-prod.fastly-edge.com/");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp COMMUNITY_SPAM_EXITING_DELETE_CHATS_ENABLED = new ABProp(10981, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_RICH_RESPONSE_HEADING_ENABLED = new ABProp(10986, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_USER_JOURNEY_LOGGING_MIGRATION = new ABProp(10999, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_LONG_TERM_HOLDOUT_CLIENT_SIDE_CHECK = new ABProp(11000, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_ADV_LOGOUT_ON_SELF_DEVICE_LIST_EXPIRED = new ABProp(11011, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WAMO_SUB_LOGGING_ENABLED_V2 = new ABProp(11017, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WAMO_SUB_ADMIN_ENABLED_V2 = new ABProp(11020, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WAMO_SUB_CONSUMER_ENABLED_V2 = new ABProp(11021, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp META_CATALOG_LINKING_M2_ENABLED = new ABProp(11029, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp META_CATALOG_LINKING_M3_ENABLED = new ABProp(11030, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_SIGNAL_DECOUPLING_ENABLED = new ABProp(11035, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_APPLY_LATEST_DB_SCHEMA_OPTIMIZATION_ENABLED = new ABProp(11040, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp LID_MIGRATION_FOR_VNAME_ENABLED = new ABProp(11049, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp LID_ONE_ON_ONE_MIGRATION_LOG_OUT_ON_MISMATCH = new ABProp(11050, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WAMO_SUB_MESSAGES_SUPPORTED = new ABProp(11062, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SINGLE_E2EE_SESSION_ENABLED_FOR_GENERAL_OUTGOING_LID_STANZAS = new ABProp(11063, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_IMAGE_SEARCH_QUERY_LOGGING = new ABProp(11068, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_BIZ_AI_CHAT_ASSIGNMENT_HIDING_ENABLED = new ABProp(11084, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp QUICK_REPLIES_PROMOTION_CONFIG = new ABProp(11096, "{}", "{}");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_FORWARD_NAVIGATION_STAY_IN_CHANNEL = new ABProp(11119, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DEFAULT_ENDPOINT_THREAD_POLL_TIMEOUT = new ABProp(11129, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_HOME_BOT_PROFILE_SYNC_INTERVAL_SEC = new ABProp(11168, "86400", "86400");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CART_LID_MIGRATION_ENABLED = new ABProp(11180, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_USER_JOURNEY_LOGGING_NOTES_KILL_SWITCH = new ABProp(11187, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_CHANNEL_IMAGE_SERVER_THUMBNAIL = new ABProp(11191, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_CHANNEL_VIDEO_SERVER_THUMBNAIL = new ABProp(11192, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp STATUS_MAX_PARTICIPANTS_FOR_RCAT = new ABProp(11195, "100", "100");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_CUSTOM_LABEL_SIGNALS_ENABLED = new ABProp(11205, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SHARE_PAYMENT_STATUS_WITH_API_MERCHANT_LIST = new ABProp(11217, "", "");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp LID_MIGRATION_DAILY_LOGGING_ENABLED = new ABProp(11231, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MM_OPT_OUT_ENABLED = new ABProp(11241, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ALBUM_V2_KEEP_ASSOCIATION_FOR_REVOKED_ITEM_ENABLED = new ABProp(11259, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PRIVACY_TOKEN_SENDING_ON_GROUP_CREATE = new ABProp(11261, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PRIVACY_TOKEN_SENDING_ON_GROUP_PARTICIPANT_ADD = new ABProp(11262, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp VIEW_ONCE_META_TAG_SENDING_ENABLED = new ABProp(11282, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_QPL_LOGGING_H2_2024_ENABLED = new ABProp(11284, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PRODUCT_CATALOG_REPORT_PRODUCT_GRAPHQL_ENABLED = new ABProp(11328, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CATALOG_LID_MIGRATION_ENABLED = new ABProp(11342, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CATALOG_PRODUCT_SALE_PRICE_ENABLED = new ABProp(11343, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp LID_OUTGOING_MSG_ATTACH_PEER_RECIPIENT_INFO = new ABProp(11364, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MEX_ADD_PARTICIPANTS_TO_GROUP = new ABProp(11392, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp FGX_UPDATE_H2_2024 = new ABProp(11410, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp UPDATES_TAB_MAX_UNCOLLAPSED_STATUS_ROWS_LARGE_SCREENS = new ABProp(11411, "4", "4");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp UPDATES_TAB_MAX_UNCOLLAPSED_STATUS_ROWS_SMALL_SCREENS = new ABProp(11412, "4", "4");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp LID_ONE_ON_ONE_MIGRATION_PEER_SYNC_LIMIT = new ABProp(11416, "30000", "30000");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_META_VERIFIED_HIDE_BIZ_SEARCH_ATTRIBUTION_ENABLED = new ABProp(11420, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ALBUM_V2_LATE_ARRIVAL_MARK_ALBUM_MESSAGE_UNREAD_ENABLED = new ABProp(11423, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_NEW_ONGOING_CALL_CELL_UI = new ABProp(11426, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_TENOR_STICKER_SEARCH = new ABProp(11433, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_TENOR_V2_GIFS = new ABProp(11434, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp FORCE_GIF_PROVIDER = new ABProp(11435, "", "");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp LID_ONE_ON_ONE_PRE_MIGRATION_MAT_ENABLED = new ABProp(11440, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_FTS_INDEXER_MEM_LEAK_FIX_ENABLED = new ABProp(11449, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ML_MODEL_DOWNLOAD_SKIP_HASH_CHECK = new ABProp(11454, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SFU_SECONDARY_REMOTE_BWE_IMPL = new ABProp(11472, "0", "8");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ALBUM_V2_SENDER_IN_GROUPS_ENABLED = new ABProp(11473, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DATA_PRIVACY_PHASE_2_NON_GROUP_CHAT_E2EE_ENABLED = new ABProp(11474, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DEFAULT_PROFILE_PICS_M1 = new ABProp(11482, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp COMPANION_HISTORY_SYNC_ASSOCIATION_INFO_RECEIVER_ENABLED = new ABProp(11486, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PAYMENTS_BR_PIX_PROMINENCE_ENABLED = new ABProp(11501, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_HISTORY_SYNC_SHOULD_USE_ENC_HANDLE = new ABProp(11513, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PAYMENTS_BR_PIX_PROMINENCE_CONFIG = new ABProp(11517, "{}", "{}");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp IGNORE_JOINABLE_TERMINATE_ON_EXPIRED_OFFER = new ABProp(11519, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PAYMENTS_BR_ORDER_DETAILS_WITH_ATTACHMENTS_FEATURE_ENABLED = new ABProp(11527, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp NYE_REACTION_ENABLED = new ABProp(11531, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ALBUM_V2_SENDER_DELAY_DISMISS_MEDIA_PICKER_ENABLED = new ABProp(11554, "0", "0.20000000298023224");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ALBUM_V2_SENDER_WAIT_FOR_INITIAL_ITEMS_ENABLED = new ABProp(11555, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CATALOG_VIDEO_MAX_VIDEOS_PER_PRODUCT = new ABProp(11566, "9", "9");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_GETTERS_NOOP_CACHE = new ABProp(11584, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_MEMORY_STAT_V2_COMMIT_INTERVAL = new ABProp(11585, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_EMOJI_REGEXP_SIZE_FIX_ENABLED = new ABProp(11597, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_CATALOG_GRAPHQL_VERIFY_POSTCODE = new ABProp(11624, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WABBA_CROSSPOST_ENABLED = new ABProp(11626, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GROUP_SAFETY_CHECK_ENABLED = new ABProp(11627, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp NATIVE_CONTACT_COMPANION_NUX_LEARN_MORE_ARTICLE_ID = new ABProp(11644, "1191526044909364", "1191526044909364");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MESSAGE_ASSOCIATION_INFRA_HISTORY_SYNC_ENABLED = new ABProp(11648, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_CATALOG_GRAPHQL_UPDATE_PRODUCT_VISIBILITY = new ABProp(11651, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp EXTERNAL_CTX_AUTHORISE_WA_CHAT = new ABProp(11655, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_FBID_MIGRATION_RECEIVE_ENABLED = new ABProp(11660, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PAYMENTS_BR_P2M_BOLETO_ENABLED = new ABProp(11671, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_CATALOG_GRAPHQL_GET_PUBLIC_KEY = new ABProp(11690, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_CLIENT_IMAGE_MMS_THUMBNAIL_DOWNLOAD_ENABLED = new ABProp(11693, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_CLIENT_VIDEO_MMS_THUMBNAIL_DOWNLOAD_ENABLED = new ABProp(11694, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ADD_TO_CALL_IN_CHAT_THREAD = new ABProp(11700, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PAYMENT_SUPPORT_NUMBER_PREFIXES = new ABProp(11708, "1551786880", "1551786880");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp FORWARD_FLOW_LINKS_IMPROVEMENT_ENABLED = new ABProp(11731, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp NYE_REACTION_IN_TRAY_ENABLED = new ABProp(11732, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SAGA_PROTOBUF_AI_STARDUST_WEB = new ABProp(11756, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp USERNAME_ENGAGEMENT_NETWORK_IMPACT_LOGGING = new ABProp(11794, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SORT_ONLY_FILTERED_CONTACTS = new ABProp(11809, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_SMB_MESSAGE_LABEL_DEPRECATION_PHASE_FOR_SMBA = new ABProp(11817, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SYNCD_MUTATION_AND_BUNDLE_LOGGING = new ABProp(11821, "{\"allowlist\": []}", "{\"allowlist\": []}");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SAGA_PROTOBUF_SHOW_SYSMSG_WEB = new ABProp(11832, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp KMP_SYNCD_CRYPTO_MODULE_ENABLED = new ABProp(11851, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_PAYMENT_LINKS_LINK_CLICK_LOGGING_ENABLED = new ABProp(11856, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_META_VERIFIED_EDUCATION_BOTTOMSHEET_FOR_BADGE = new ABProp(11858, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_CURVE25519_DONNA_SMALL_BUFFER_SIZE_ENABLED = new ABProp(11861, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp NOTIFICATION_HIGHLIGHT_GROUP_SIZE_THRESHOLD = new ABProp(11891, "130", "130");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_PAYMENT_LINKS_LINK_CLICK_PSP_LOGGING_ENABLED = new ABProp(11908, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AGE_COLLECTION_ALERT_ALL_AGES_ENABLED = new ABProp(11912, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MEX_GROUPS_SET_GROUP_DESCRIPTION = new ABProp(11931, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MEX_SET_MEMBERSHIP_APPROVAL_MODE = new ABProp(11932, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ALBUM_V2_ITEM_WITH_CAPTION_IN_ALBUM_RECEIVER_ENABLED = new ABProp(11943, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_STICKER_PACK_MESSAGE_SENDING_CHANNELS = new ABProp(11952, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_FBID_MIGRATION_SENDING = new ABProp(11965, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_PAYMENT_LINKS_LINK_CLICK_FUNNEL_ID_LOGGING_ENABLED = new ABProp(11966, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp FUTUREPROOF_ASSOCIATED_CHILD_ENABLED = new ABProp(11976, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp USE_SIGNED_SHIMMED_URL_LINK = new ABProp(11977, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNEL_PHOTO_POLL_RECEIVER_ENABLED = new ABProp(11980, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNEL_PHOTO_POLL_SENDER_ENABLED = new ABProp(11989, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp LID_MIGRATION_FOR_BIZ_PROFILE_ENABLED = new ABProp(12000, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_GROUP_CREATE_OR_ADD_RATE_LIMITING_ERROR_UX = new ABProp(12020, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp LID_MIGRATION_FOR_NOTES_ENABLED = new ABProp(12025, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CART_ITEM_DB_LID_MIGRATION_ENABLED = new ABProp(12041, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CLICK_TO_FLOWS_CTA_LOGGING_ENABLED = new ABProp(12045, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp REVOKE_EDIT_ATTRIBUTE_VALIDATION_ENABLED = new ABProp(12055, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp COMMUNITY_EXIT_IMPROVEMENTS_M1 = new ABProp(12097, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_CATALOG_GRAPHQL_COMMERCE_SETTINGS = new ABProp(12099, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_INTERNAL_CONVERSION_METADATA = new ABProp(12137, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MM_OPT_OUT_FMX_STOP_FOR_HIGH_TRUST = new ABProp(12172, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BR_PIX_HOLDOUT_ENABLED = new ABProp(12182, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_UNIFIED_CHAT_SOCKET_OPENER_ENABLED = new ABProp(12204, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CARE_EMOJI = new ABProp(12209, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp FGX_UPDATE_H2_2022_LOGGING_ENABLED = new ABProp(12224, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNEL_ENFORCEMENTS_SHOW_UPDATE_FOOTER = new ABProp(12243, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp OHAI_REQUEST_KB_SIZE = new ABProp(12248, "20", "20");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp NYE_REACTION_SIMULTANEOUS_ANIMATIONS_COUNT = new ABProp(12250, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp STATUS_MENTIONS_GROUP_MENTION_RECEIVER = new ABProp(12254, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WABBA_QPL_ENABLED = new ABProp(12257, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp POLL_RESULT_SNAPSHOT_POLLTYPE_ENVELOPE_ENABLED = new ABProp(12258, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_NEW_CHAT_FLOW_REFRESH = new ABProp(12275, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_NEW_CHAT_FLOW_REFRESH_VARIANT = new ABProp(12276, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_VIEW_COUNTS_VPV_LOGGING_ENABLED = new ABProp(12295, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp TEMPLATE_MESSAGE_BUTTON_LIMIT = new ABProp(12301, "-1", "-1");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_RICH_RESPONSE_CODE_ENABLED = new ABProp(12303, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MM_USER_CONTROLS_IN_BLOCK_ENABLED = new ABProp(12316, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_LOGGING_ENTRYPOINT_ENABLED = new ABProp(12318, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MM_GET_OPTOUTLIST_TTL_IN_SECONDS = new ABProp(12322, "604800", "86400");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp USERNAME_CALL_SEARCH_ENABLED = new ABProp(12327, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_FORWARD_FLOW_ENABLED = new ABProp(12365, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MEX_USYNC_PAY_QUERY = new ABProp(12371, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WAM_DISABLE_ABKEY_ATTRIBUTE = new ABProp(12390, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WAM_DISABLE_EXPOKEY_ATTRIBUTE = new ABProp(12391, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp USERNAME_INCLUDE_LID_USERNAME_SEARCH = new ABProp(12402, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WABBA_ARTWORK_PIXEL_SIDE_SIZE = new ABProp(12421, "150", "150");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_METADATA_UPDATE_FOR_LOTTIE = new ABProp(12474, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_SIGNAL_FUTURE_MESSAGES_MAX = new ABProp(12509, "20000", "20000");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp FLOWS_WA_WEB = new ABProp(12520, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp FLOWS_LID_MIGRATION_ENABLED = new ABProp(12521, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SHOPPING_LID_MIGRATION_ENABLED = new ABProp(12522, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp NFR_LOG_DISCOVERY_ENTRY_POINT_SENDER = new ABProp(12526, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ALBUM_V2_MIN_ITEMS_TO_SEND_ALBUM_WITH_CAPTION = new ABProp(12538, "2", "2");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_RICH_RESPONSE_MAIN_GATE_ENABLED = new ABProp(12539, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_CATALOG_GRAPHQL_DELETE_PRODUCT = new ABProp(12543, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp OTP_LID_MIGRATION_ENABLED = new ABProp(12553, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ALBUM_V2_SENDER_LAST_INDEX_TO_THROTTLE_SEND = new ABProp(12595, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp RT_SENDER_DUAL_ENCRYPTED_MSG_ENABLED = new ABProp(12623, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp EXTERNAL_CTX_AUTHORISE_WA_CHAT_PROVISIONAL = new ABProp(12636, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_SYNCD_QUERY_INDEX_MAC_USING_MULTIPLE_QUERIES = new ABProp(12642, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SHOULD_REPORT_INVALID_INDEX_MAC = new ABProp(12657, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_RICH_RESPONSE_THREAD_SURFING_ENABLED = new ABProp(12663, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_MMS_HISTORY_SYNC_BLOB_MEDIA_TYPE_FIX = new ABProp(12665, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp COMMUNITY_EXIT_IMPROVEMENTS_M2 = new ABProp(12671, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_VOICE_MULTIMODAL_COMPOSER_ENABLED = new ABProp(12692, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_GIPHY_GIFS_API = new ABProp(12694, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_GIPHY_STICKERS_API = new ABProp(12695, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_TENOR_GIFS_API = new ABProp(12696, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_TENOR_STICKERS_API = new ABProp(12697, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WAMO_SUB_PROCESS_MESSAGE_KILL_SWITCH = new ABProp(12722, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_SOCKET_OPENER_MD_DEPRECATED = new ABProp(12725, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp EXTERNAL_CTX_URL_PARAM_NAMES = new ABProp(12726, "partnertoken", "partnertoken");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MM_OPT_OUT_LIST_SERVER_SYNC_ENABLED = new ABProp(12758, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp EXTERNAL_CTX_AUTHORISE_EXISTING_CHATS = new ABProp(12761, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_FBID_MIGRATION_INVOKE_RECEIVE_ENABLED = new ABProp(12795, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MEDIA_VIEWER_ACCELERATED_PLAYBACK_ENABLED = new ABProp(12813, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BIZ_AI_AGENT_CONSUMER_MERGE_TOS_ENABLED = new ABProp(12820, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp EXTERNAL_CTX_LOG_LINK_METADATA = new ABProp(12822, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_DEXIE_HOOKS_SUPPORT_ENABLED = new ABProp(12831, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CATALOG_PRODUCT_CACHE_TTL = new ABProp(12835, "15", "15");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MM_MESSAGE_LEVEL_FEEDBACK_UNDO_TOAST_ENABLED = new ABProp(12855, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CONTACT_AND_CHAT_FUZZY_SEARCH_DISTANCE_THRESHOLD = new ABProp(12863, "0.30000001192092896", "0.30000001192092896");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CONTACT_AND_CHAT_FUZZY_SEARCH_ENABLED = new ABProp(12864, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CONTACT_AND_CHAT_FUZZY_SEARCH_TIMEOUT_THRESHOLD = new ABProp(12865, "5", "5");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_CACHE_STORAGE_CONFIG_DISABLE_IGNORE_SEARCH = new ABProp(12878, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp NOTIFICATION_SILENT_COMMAND_ENABLED = new ABProp(12908, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp REUSE_CACHED_CERTS_FOR_DATA_CHANNEL = new ABProp(12913, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENTRYPOINT_CONVERSION_LID_MIGRATION_ENABLED = new ABProp(12918, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp OVERRIDE_ADV_ACCOUNT_SIGNATURE_KEY_ENABLED = new ABProp(12933, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ANIMATED_EMOJIS_AUTOPLAY_SETTING_ENABLED = new ABProp(12957, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp IS_EMPLOYEE_ACCOUNT = new ABProp(12978, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_DISCOVERY_VPV_LOGGING_MIN_DURATION_MS = new ABProp(12979, "250", "250");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp LID_GROUP_CREATION_ADDRESSING_MODE_OVERRIDE = new ABProp(12985, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNEL_OSA_REPORTING_ENABLED = new ABProp(12987, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_CLIENT_STICKER_PACK_MMS_THUMBNAIL_DOWNLOAD_ENABLED = new ABProp(12988, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PRIVATE_OSA_REPORTING_ENABLED = new ABProp(12990, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_UI_REFRESH_M1 = new ABProp(12993, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DISCLOSURE_FOR_THE_MARKETING_MESSAGE_BODY_LINKS_ENABLED = new ABProp(12994, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SHIMMED_LINKS_IN_THE_MARKETING_MESSAGE_BODY_ENABLED = new ABProp(12995, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MM_MESSAGE_LEVEL_FEEDBACK_ARTICLE = new ABProp(13019, "1027276182478056", "1027276182478056");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MM_USER_CONTROLS_IN_FMX_BLOCK_ENABLED = new ABProp(13020, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp FT_VALIDATION_FAILURE_DROP_PLACEHOLDER = new ABProp(13063, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MM_BRAND_MAPPING_TTL_IN_SECONDS = new ABProp(13068, "604800", "86400");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MM_MESSAGE_LEVEL_FEEDBACK_WITH_STOP_ARTICLE = new ABProp(13085, "849628780369041", "849628780369041");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WABBA_MENTIONS_RESHARE_HANDLING_ENABLED = new ABProp(13113, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WABBA_SAVE_TO_CAMERA_ROLL_ENABLED = new ABProp(13114, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WABBA_STICKER_RECEIVER_ENABLED = new ABProp(13115, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp LABEL_JID_DB_LID_MIGRATION_ENABLED = new ABProp(13132, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MEX_GROUPS_SET_UNSET_ANNOUNCEMENT = new ABProp(13134, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp LID_ONE_ON_ONE_MIGRATION_COMPATIBLE = new ABProp(13161, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp EARLY_AUDIO_DRIVER_CAPTURE_AT_NATIVE = new ABProp(13166, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp EARLY_AUDIO_DRIVER_PRE_BUFFERING = new ABProp(13168, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp STATUS_UNIFIED_TIMESTAMP_ENABLED = new ABProp(13179, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_FIX_PTT_IDLE_CPU_CONSUMPTION_ENABLED = new ABProp(13189, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SYNCD_LID_SHOULD_DO_LAZY_CLEANUP = new ABProp(13197, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNEL_ALBUM_V2_RECEIVING_ENABLED = new ABProp(13219, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNEL_ALBUM_V2_SENDER_ENABLED = new ABProp(13220, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_AUDIO_DEVICE_ASYNC_START = new ABProp(13231, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_ENABLE_BIZ_DATA_SHARING_AFTER_NUX_DISMISS = new ABProp(13240, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_VOICE_ENTRY_POINT_LOGGING_ENABLED = new ABProp(13247, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_MESSAGE_STARRING_ENABLED = new ABProp(13249, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WABBA_PREUPLOAD_ARWORK = new ABProp(13253, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ANYONE_CAN_LINK_TO_GROUPS = new ABProp(13268, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp STATUS_SAVE_TO_CAMERA_ROLL_ENABLED = new ABProp(13280, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_VOICE_MULTIMODAL_COMPOSER_TEXT_MODE_TEXT_LLM_ENABLED = new ABProp(13285, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_RICH_RESPONSE_INLINE_IMAGE_ENABLED = new ABProp(13298, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MEDIA_SHARING_TEAM_HOLDOUT_H1_2025 = new ABProp(13306, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_SHARE_FLOW_ENABLED = new ABProp(13320, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CUSTOM_RACING_EMOJI_FEB2025 = new ABProp(13322, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp EMOJI_SEARCH_CLDR = new ABProp(13323, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_CALLING_USERNAME = new ABProp(13359, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_PTV_ENABLED = new ABProp(13366, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DESKTOP_CALL_CONTROLS_REDESIGN_ENABLED = new ABProp(13378, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PER_CUSTOMER_DATA_SHARING_CONTROLS_ELIGIBLE = new ABProp(13383, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_DOWNLOAD_3PD_SIGNALS = new ABProp(13385, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_AI_AGENTS_WEB_CHAT_ASSIGNMENT_INTEROP_ENABLED = new ABProp(13387, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_PRODUCT_COUNTRY_OF_ORIGIN_M1 = new ABProp(13415, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp USE_CACHED_APP_SETTINGS_FROM_GLOBAL_CTX = new ABProp(13428, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp VIDEO_UPGRADE_WITHOUT_CONFIRMATION_ONE_ON_ONE_CALLS = new ABProp(13450, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp RASTERIZE_TEXT_STATUS_ENABLED = new ABProp(13459, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp RASTERIZE_TEXT_STATUS_PIXEL_WIDTH = new ABProp(13460, "1080", "1080");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BIZ_AI_AUTO_SAVE_ENABLED = new ABProp(13464, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BIZ_AI_COACHING_ENABLED = new ABProp(13465, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MEDIA_SHARING_TEAM_QUALITY_HOLDOUT_H1_2025 = new ABProp(13466, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_BOT_PROACTIVE_MESSAGE_ENABLED = new ABProp(13478, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_3PD_DATA_SHARING_ON_THREAD_ENTRY = new ABProp(13485, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_GROUP_CALL_AV_SWTICH = new ABProp(13487, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ANIMATED_RACE_MERCEDES_CAR_EMOJI_ENABLED = new ABProp(13490, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_UNIFIED_CALL_BUTTONS_IN_CHAT = new ABProp(13497, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MM_USER_CONTROLS_EXPOSURE = new ABProp(13510, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MEMBER_NAME_TAG_RECEIVER_ENABLED = new ABProp(13523, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MEMBER_NAME_TAG_SENDER_ENABLED = new ABProp(13524, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_RICH_RESPONSE_SUBHEADING_ENABLED = new ABProp(13530, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WAMO_SUB_CREATOR_ONBOARDING_MANAGEMENT_ENABLED = new ABProp(13531, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp HAND_RAISE_RECEIVER_ENABLED = new ABProp(13540, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp HAND_RAISE_SENDER_ENABLED = new ABProp(13541, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp REACTIONS_RECEIVER_ENABLED = new ABProp(13542, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp REACTIONS_SENDER_ENABLED = new ABProp(13543, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_PTV_RECEIVING_ENABLED = new ABProp(13559, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp EXTERNAL_CTX_FOA_LOGGING = new ABProp(13565, "1", "1");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_FBID_MIGRATION_INVOKE_SEND_ENABLED = new ABProp(13571, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_RICH_RESPONSE_GRID_IMAGE_ENABLED = new ABProp(13578, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_SHOW_ADS_DATA_SHARING_AFTER_MESSAGE = new ABProp(13579, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp RASTERIZE_TEXT_STATUS_IMAGE_QUALITY = new ABProp(13595, "75", "75");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp TEXT_STATUS_REFRESH_VARIANT = new ABProp(13596, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_FMX_AGM_ENABLED = new ABProp(13597, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp RT_MMS_RETRY_RECEIVE = new ABProp(13622, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp RT_MMS_RETRY_SEND = new ABProp(13623, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_CALL_LINKS_PUSH_NOTIFICATION = new ABProp(13679, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp STICKY_CHAT_PROFILE_PICTURE_ENABLED = new ABProp(13692, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_IMAGINE_COMMAND_ENABLED = new ABProp(13696, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_TURN_OFF_VIDEO_IN_INCOMING_ONE_ON_ONE_CALLS_V2 = new ABProp(13698, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_IMAGINE_SYSTEM_MESSAGE_ENABLED = new ABProp(13699, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_RICH_RESPONSE_STRUCTURED_RESPONSE_CODE_ENABLED = new ABProp(13703, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_USE_WINDOWS_SIDELOAD = new ABProp(13708, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_HYBRID_NEW_CHAT_FLYOUT_ENABLED = new ABProp(13735, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WINDOWS_HYBRID_ABPROPS_TEST = new ABProp(13772, "default", "debug");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_PTV_FORWARDING_ENABLED = new ABProp(13776, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_PTV_FORWARDING_TO_STATUS_ENABLED = new ABProp(13777, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_EARLY_AUDIO_DRIVER_START = new ABProp(13807, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PREMIUM_BROADCAST_SMB_CAPPING_ENABLED = new ABProp(13808, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp OHAI_QPL_ENABLED = new ABProp(13833, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp IS_GROUP_CHAT_OPEN_LOGGING_ENABLED = new ABProp(13864, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DEFENSE_MODE_AVAILABLE = new ABProp(13874, "0", "1");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_FORWARD_FLOW_SURFACE_META_AI_AS_CONTACT_ENABLED = new ABProp(13879, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WAMO_IS_INTERNAL_TESTER = new ABProp(13886, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp LID_ONE_ON_ONE_MIGRATION_PEER_SYNC_TIMEOUT_IN_SECONDS = new ABProp(13936, "300", "300");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp NEWSLETTERS_VIDEO_PLAYBACK_WABBA_LOGGING_ENABLED = new ABProp(13954, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GROUP_STATUS_RECEIVER_ENABLED = new ABProp(13956, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GROUP_STATUS_SENDER_ENABLED = new ABProp(13957, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_SUSPENDED_CHATS_UI_IMPROVEMENTS_AND_CLEANUP = new ABProp(13961, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_PDFN_TOS_INLINE_NOTICES = new ABProp(13970, " ", " ");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp USERNAME_CONTACT_SIDELIST = new ABProp(13983, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_GROUP_EXIT_EXPERIENCE = new ABProp(13996, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp UPDATES_QUICK_PROMOTION_BANNER_ENABLED = new ABProp(13997, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MM_USER_CONTROLS_EXCEPTION_NUMBER_PREFIXES = new ABProp(13999, "", "");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_ONLY_RECOMMENDED_UNIT_ROWS = new ABProp(14001, "4", "4");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNEL_MESSAGE_STARRING_LOGGING_ENABLED = new ABProp(14002, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp UPDATES_TAB_CHANNELS_SHOW_MUTED_INDICATOR_ENABLED = new ABProp(14007, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_PDFN_NUX_AI_WORLD_NOTICE_ID = new ABProp(14034, " ", " ");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_PDFN_TOS_AI_WORLD_NOTICE_ID = new ABProp(14035, " ", " ");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CUSTOM_UNCOVERED_RACING_EMOJI_FEB2025 = new ABProp(14037, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SNAPL_NEWSLETTER_LOGGING_MEDIA_ID_PLACEHOLDER_STRING = new ABProp(14064, "-1", "-1");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp USERNAME_GROUP_LEARNING_ENABLED = new ABProp(14078, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_MSG_LIST_VIRTUALIZATION = new ABProp(14084, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp UPDATES_QP_BANNER_LOGGING_ENABLED = new ABProp(14126, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_RICH_RESPONSE_SMB_WEB_STRUCTURED_RESPONSE_ENABLED = new ABProp(14140, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_RICH_RESPONSE_WEB_STRUCTURED_RESPONSE_ENABLED = new ABProp(14141, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp NEWSLETTERS_AUTOPLAY_VIDEO_SNAPL_LOGGING_ENABLED = new ABProp(14162, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp RASTERIZE_TEXT_STATUS_SEND_IMAGE_SOURCE_TYPE_ENABLED = new ABProp(14166, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp RASTERIZE_TEXT_STATUS_USE_DYNAMIC_PRESENTATION_TIME_ENABLED = new ABProp(14167, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp VIEW_REPLIES_INFRA_ENABLED = new ABProp(14199, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp EARLY_BOT_CONNECT_EVENT_BITMAP = new ABProp(14200, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_RICH_RESPONSE_STRUCTURED_RESPONSE_INLINE_REELS_ENABLED = new ABProp(14215, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_REWRITE_ENABLED = new ABProp(14219, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_REWRITE_SUPPORTED_LANGUAGES = new ABProp(14220, " ", "en");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_UK_OSA_ENABLED = new ABProp(14249, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PRIVATE_MESSAGING_UK_OSA_ENABLED = new ABProp(14250, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_FAVICONS_UPDATE_M1 = new ABProp(14260, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp IS_PART_OF_GSC_EXPERIMENT = new ABProp(14279, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp USERNAME_NUMERIC_CODE_V4 = new ABProp(14286, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEFR_AA_TEST = new ABProp(14293, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_CATALOG_RECOVERY_FLOW_ENABLED = new ABProp(14294, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ADM = new ABProp(14296, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_CTWA_GET_CONTEX_BIZ_GRAPHQL_ENABLED = new ABProp(14297, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_WAFFLE = new ABProp(14300, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp LID_TRUSTED_TOKEN_ISSUE_TO_LID = new ABProp(14303, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SUPPORT_LIDS = new ABProp(14317, "4200746488034,30563255730192,70334669676777,19349129719984,66065505775654,133814269518032,243799792062487,7323238039569,269290422947912,261718412386336,4351103873168,12391299473616,92410801582180,277730033709185,36090878648473,79882365190287,94274800595104,117794058317863,115784047153172,179250745360524,7301780005088,166653589463190,94249030815912,198964645236955,198427807899653,23656948363422,255735573270728,106670109786240,130932396826763,18855208456329", "4200746488034,30563255730192,70334669676777,19349129719984,66065505775654,133814269518032,243799792062487,7323238039569,269290422947912,261718412386336,4351103873168,12391299473616,92410801582180,277730033709185,36090878648473,79882365190287,94274800595104,117794058317863,115784047153172,179250745360524,7301780005088,166653589463190,94249030815912,198964645236955,198427807899653,23656948363422,255735573270728,106670109786240,130932396826763,18855208456329");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WAMO_STATUS_PREVIEW_ENABLED = new ABProp(14332, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PAYMENT_SUPPORT_LIDS = new ABProp(14333, "116664750354676,128385682505839,46635358933114,26521959944357,200206125658243,179985503506636,187797998674170,228746200088715,117914552262794,10158134550607", "116664750354676,128385682505839,46635358933114,26521959944357,200206125658243,179985503506636,187797998674170,228746200088715,117914552262794,10158134550607");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MV4WA_ACTIVE_SUBSCRIBER_WEB_ENTRYPOINT_ENABLED = new ABProp(14337, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GIF_PROVIDER = new ABProp(14343, "1", "1");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PAYMENT_BR_HOLDOUT = new ABProp(14358, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp RASTERIZE_TEXT_STATUS_M1_5_ENABLED = new ABProp(14383, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp LID_MIGRATION_WABME_SHOPPING_NETWORK_TRANSLATION_ENABLED = new ABProp(14386, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp UPDATES_PRIVACY_NOTICE_ROLLOUT_DATE = new ABProp(14387, "1742310000", "1742310000");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PAYMENT_LINKS_CTA_BUTTON_ENABLED = new ABProp(14394, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp RENDER_UPDATED_DISCLOSURE = new ABProp(14407, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp STICKER_ANNOTATIONS_RECEIVING_ENABLED = new ABProp(14416, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp STICKER_ANNOTATIONS_SENDING_ENABLED = new ABProp(14417, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MV4WA_NON_SUBSCRIBER_WEB_ENTRYPOINT_ENABLED = new ABProp(14435, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp LISTS_BASED_NOTIFICATION_ENABLED = new ABProp(14456, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SYNCD_SENTINEL_TIMEOUT_SECONDS = new ABProp(14485, "3", "3");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SYNCD_KEY_MAX_USE_DAYS = new ABProp(14488, "30", "30");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SAGA_V1_CAROUSEL_NO_EXPOSURE = new ABProp(14491, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SYNCD_WAIT_FOR_KEY_TIMEOUT_DAYS = new ABProp(14492, "7", "7");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SYNCD_INLINE_MUTATIONS_MAX_COUNT = new ABProp(14494, "100", "100");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SYNCD_PATCH_PROTOBUF_MAX_SIZE = new ABProp(14495, "10", "10");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MD_SYNCD_LOGGING_SPEC_ENABLED = new ABProp(14499, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp VIDEO_UPGRADE_WITHOUT_CONFIRMATION_ONE_ON_ONE_CALLS_SENDER_V2 = new ABProp(14508, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MV4WA_NON_SUBSCRIBER_WEB_QP_ENABLED = new ABProp(14546, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_CTWA_GET_ACCOUNT_NONCE_GRAPHQL_ENABLED = new ABProp(14558, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp LIMIT_SHARING_ENABLED = new ABProp(14563, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp USERNAME_CONTACT_USYNC_LID_BASED = new ABProp(14565, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_CTWA_GET_LINKED_ACCOUNTS_GRAPHQL_ENABLED = new ABProp(14568, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_SPAM_REPORTING_PRE_LOGGING = new ABProp(14573, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BIZ_MESSAGES_WEBP_IMAGES_ENABLE = new ABProp(14585, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp OPTIMIZED_DELIVERY_MULTIPLE_COLLECTION_WINDOWS_ENABLED = new ABProp(14588, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_GROUP_EXIT_EXPERIENCE_LOGGING = new ABProp(14589, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_UGC_VOICE_FS_LOGGING = new ABProp(14641, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PAYMENT_LINKS_CTA_BUTTON_PSP_VARIANT_LIST = new ABProp(14643, "{}", "{}");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_CTWA_UPLOAD_AD_MEDIA_GRAPHQL_ENABLED = new ABProp(14648, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DESKTOP_CALL_CONTROLS_CHAT_BUTTON_ENABLED = new ABProp(14660, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_CHAT_INFO_ACTION_BUTTONS_REFRESH = new ABProp(14664, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp RASTERIZE_TEXT_STATUS_LINK_ACTION_RECEIVER_ENABLED = new ABProp(14666, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp HYBRID_EDUCATIONAL_DIALOGS_ENABLED = new ABProp(14674, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp HYBRID_EDUCATIONAL_DIALOG_START_AT = new ABProp(14675, " ", " ");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp EDUCATIONAL_DIALOGS_BUTTON_ENABLED = new ABProp(14676, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SEARCH_USER_JOURNEY_LOGGING_WAM_ENABLED = new ABProp(14682, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_REWRITE_TONE_MODIFIERS = new ABProp(14743, "rephrase,professional,funny,supportive", "rephrase,professional,funny,supportive");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GROUP_DESCRIPTION_LENGTH = new ABProp(14778, "2048", "2048");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GROUP_MAX_SUBJECT = new ABProp(14801, "100", "100");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_FMX_ACTION_WAM_LOGGING = new ABProp(14846, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_HOME_CREATION_BONSAI_SUPPORTED_LANGUAGES = new ABProp(14857, "en,es", "en,es");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CONTACT_LID_BASED_LIST_RETRIEVING = new ABProp(14859, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_BIZ_PROFILE_OPTIONS = new ABProp(14881, "116", "116");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_INVOKE_ENABLED = new ABProp(14886, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_CUSTOM_LABEL_ALGORITHM = new ABProp(14887, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp LID_MIGRATION_WABME_FLOWS_NETWORK_TRANSLATION_ENABLED = new ABProp(14896, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_REWRITE_ENTRY_POINT_MIN_WORDS = new ABProp(14923, "4", "4");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_REWRITE_NUM_SUGGESTIONS = new ABProp(14924, "3", "3");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_PAYMENT_LINKS_CTA_VARIANT = new ABProp(14957, "2", "2");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_PAYMENT_LINKS_CTA_BUTTON_KILL_SWITCH = new ABProp(14967, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp EMOJI_SYRIA_FLAG_UPDATE = new ABProp(14973, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WAMO_PRIVACY_TOS_LINKED_HIGHLIGHTED_NOTICE_ID = new ABProp(14985, "20610204", "20610204");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WAMO_PRIVACY_TOS_UNLINKED_HIGHLIGHTED_NOTICE_ID = new ABProp(14987, "20610203", "20610203");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_PAYMENT_LINKS_CTA_PSP_LIST = new ABProp(14998, "{}", "{}");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp RT_EDIT_RECEIVE = new ABProp(15016, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_CTWA_UNLINK_ACCOUNT_GRAPHQL_ENABLED = new ABProp(15039, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp USERNAME_CONTACT_LID_BASED_RETRIEVING = new ABProp(15066, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PAYMENTS_BR_PIX_DI_DISABLE_CACHING_BANK_CPF_TOS = new ABProp(15086, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp LIMIT_SHARING_ENABLED_FOR_1ON1_CHAT = new ABProp(15127, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp LIMIT_SHARING_ENABLED_FOR_GROUP_CHAT = new ABProp(15128, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp LIMIT_SHARING_PROTOCOL_MESSAGE_RECEIVER_ENABLED = new ABProp(15129, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp RT_WEB_DELAY_PROCESSING = new ABProp(15181, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_AUTO_ADD_CALL_LINK_CREATOR = new ABProp(15184, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_COMMUNITY_EXIT_EXPERIENCE = new ABProp(15219, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_QUESTION_RECEIVER_MESSAGE_TYPES_M1_ENABLED = new ABProp(15246, "", " 22");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_COMMUNITY_EXIT_EXPERIENCE_LOGGING = new ABProp(15250, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WAMO_PRIVACY_TOS_SHOW_CHANNELS_NUX_ENABLED = new ABProp(15254, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp NEWSLETTER_NUX_NOTICE_ID = new ABProp(15255, "20610210", "20610210");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp NEWSLETTER_ADMIN_INVITE_NUX_ID = new ABProp(15256, "20610220", "20610220");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp RT_RECEIVER_DUAL_ENCRYPTED_MSG_ENABLED = new ABProp(15258, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_RICH_RESPONSE_SMB_WEB_STRUCTURED_RESPONSE_RECEIVER_ENABLED = new ABProp(15266, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_RICH_RESPONSE_WEB_STRUCTURED_RESPONSE_RECEIVER_ENABLED = new ABProp(15269, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_IMPORTANT_LABEL_SENDS_SIGNALS = new ABProp(15271, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_PDFN_TOS_NON_BLOCKING_NOTICES = new ABProp(15280, "", "");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_PDFN_TOS_MASTER_NOTICE_ID = new ABProp(15295, " ", " ");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GENAI_EARLY_AUDIO_PRE_BUF_SIZE = new ABProp(15306, "100", "100");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_SMB_DETECTED_OUTCOME_LABELS_ENABLED = new ABProp(15307, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_SMB_DETECTED_OUTCOME_LABELS_MERGER_ENABLED = new ABProp(15308, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GROUP_HISTORY_RECEIVE = new ABProp(15311, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GROUP_HISTORY_SEND = new ABProp(15313, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_PDFN_TOS_AI_SUMMARY_NOTICE_ID = new ABProp(15316, "", "");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MULTI_PPL_TYPING_INDICATOR_FOR_CHATLIST_GROUPS_VARIANT = new ABProp(15370, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_QUESTION_SENDER_MESSAGE_TYPES_M1_ENABLED = new ABProp(15418, " ", " ");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SEARCH_THE_WEB_DESIGN_EXPERIMENT_V1 = new ABProp(15423, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp TEST_NEW_FILTER_CREATE_ADS_BEFORE = new ABProp(15452, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_WEB_CALLING = new ABProp(15461, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_SMB_DETECTED_OUTCOME_LABELS_SOAK_ENABLED = new ABProp(15472, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ASTERIA_NAME_STRING = new ABProp(15485, "Asteria", "Meta AI Plus");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PRIVACY_TOKEN_ONLY_CHECK_LID = new ABProp(15491, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp USERNAME_ADOPTION_AND_ENGAGEMENT_MONITORING_ENABLED = new ABProp(15493, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_HYBRID_CALL_LINKS_JOIN = new ABProp(15501, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_HYBRID_CALL_LINKS_CREATION = new ABProp(15502, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_UPCOMING_SCHEDULE_CALL_EVENTS_IN_CALLS_TAB = new ABProp(15514, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CALL_REPORTING_EXPLORATION_LOGGING = new ABProp(15521, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BIZ_AI_IN_THREAD_UNMUTE_V2 = new ABProp(15523, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_3PD_DATA_SHARING_COOLDOWN_FOR_OPTED_OUT = new ABProp(15530, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_CATALOG_VIEWING_VARIANTS_ENABLED = new ABProp(15534, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_REWRITE_REQUEST_TIMEOUT_SEC = new ABProp(15536, "10", "10");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SUPPORT_USE_DEDICATED_SYSTEM_EVENT = new ABProp(15537, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp VCARD_AS_DOCUMENT_SIZE_KB = new ABProp(15549, "64", "64");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_GROWTH_EMPTY_STATE_UPSELL_VARIANT_M1 = new ABProp(15557, "1", "1");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_FILE_ICONOGRAPHY_REFRESHED = new ABProp(15563, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp NEW_HASH_FOR_PS_GROUP_GSC_LOGGING = new ABProp(15568, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_RICH_RESPONSE_REASONING_ENABLED = new ABProp(15589, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_SMB_DETECTED_OUTCOME_LABELS_BANNERS_ENABLED = new ABProp(15591, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_SUPPRESS_MESSAGE_VIA_AD_ON_CONSUMER_ENABLED = new ABProp(15595, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp USERNAME_ENABLE_CONTACT_CREATION = new ABProp(15628, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BIZ_AI_CONSUMER_TOS_UPDATE_ENABLED = new ABProp(15643, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PHONE_NUMBER_SHARING_FLOW = new ABProp(15653, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNEL_MEDIA_FULL_SCREEN_REACTIONS_ENABLED = new ABProp(15666, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_3PD_DATA_SHARING_COOLDOWN_MAX_TIMES_SHOWN_FOR_OPTED_OUT = new ABProp(15686, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp QUOTED_MESSAGE_USER_JOURNEY_LOGGING_ENABLED = new ABProp(15694, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WAMO_AGM_ENABLED = new ABProp(15714, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GROUP_HISTORY_NOTICE_RECEIVE = new ABProp(15722, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DUAL_UPLOAD_SD_VIDEO_LIMIT_MB = new ABProp(15751, "16", "64");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_OPEN_QPL_IMPROVEMENTS_ENABLED = new ABProp(15754, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CREATE_GROUP_AND_ADD_MEMBER_OVERFLOW = new ABProp(15772, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp QBM_LOGGING_DECISION_ID_ENABLED = new ABProp(15783, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_FETCH_PRIVACY_LIST_MY_CONTACTS_EXCEPT = new ABProp(15788, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp LID_ONE_TO_ONE_MIGRATION_EVENT_RESPONSE_FORCE_PN_JID = new ABProp(15791, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp OGN_ENABLE_ONLINE_STATUS_IN_GROUP_INFO = new ABProp(15866, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp OGN_HEADER_SUBTITLE = new ABProp(15867, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp OPTIMIZED_DELIVERY_SIGNAL_COLLECTION_ON_COMPANIONS_ENABLED = new ABProp(15884, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_SMB_DATA_SHARING_CONSENT_STATE_LOGGING = new ABProp(15907, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp KMP_SYNCD_ENGINE_CRYPTO_ENABLED = new ABProp(15909, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp USERNAME_CONTACT_UI = new ABProp(15916, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp USERNAME_SEARCH = new ABProp(15956, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_CHAT_PERSISTENT_META_AI_BANNER_ENABLED = new ABProp(15967, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_CHAT_PERSISTENT_META_AI_BANNER_LOGGING_ENABLED = new ABProp(15968, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_PRE_WARM_AUDIO_COMPONENT = new ABProp(15994, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CONTACT_AND_CHAT_FUZZY_SEARCH_SIMILARITY_OPTIMIZATION_ENABLED = new ABProp(16010, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PAYMENT_LINKS_LOGGING_ENABLED_WITHOUT_PREVIEW = new ABProp(16050, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp FORWARDED_MESSAGE_USER_JOURNEY_LOGGING_ENABLED = new ABProp(16055, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MESSAGE_EDIT_TO_MESSAGE_SECRET_SENDER_ENABLED = new ABProp(16057, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_ALL_LANGUAGES_ENABLED = new ABProp(16091, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp LID_GROUP_MIGRATION_NON_MEMBER_IQ = new ABProp(16104, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BUSINESS_TELEMTRY_LOGGING_DECISION_SERVICE_ENABLED = new ABProp(16110, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_DEBUG_COLOR_CODE_RETRY_MESSAGES = new ABProp(16138, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp USERNAME_GROUP_MUTATION_ENABLED = new ABProp(16148, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PAYMENTS_BR_PIX_ON_WEB = new ABProp(16156, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PRIVACY_SETTINGS_PROFILE_LID_MIGRATION_ENABLE = new ABProp(16161, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_SUPPRESS_MESSAGE_VIA_AD_ON_CONSUMER_DB_LEVEL_ENABLED = new ABProp(16185, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GROUP_CREATE_ADD_USING_LID_JIDS = new ABProp(16192, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_IS_MULTI_ADMIN_LID_MIGRATION_ENABLED = new ABProp(16193, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PRIVACY_SETTINGS_ABOUT_LID_MIGRATION_ENABLE = new ABProp(16195, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ATTACH_TRANSPORT_RTX = new ABProp(16201, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GROUP_AB_PROPS = new ABProp(16206, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_SEARCH_BAR_2025_REDESIGN_ENABLED = new ABProp(16208, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_MAC_BETA_UPSELL = new ABProp(16223, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SCHEDULE_CALL_SHOW_JOIN_BUTTON_TIME_INTERVAL_MINS = new ABProp(16253, "5", "5");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SCHEDULE_CALL_SHOW_UPCOMING_BANNER_TIME_INTERVAL_MINS = new ABProp(16254, "1440", "1440");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp VIDEO_PLAY_WAM_LOGGING_REVAMP_ENABLED = new ABProp(16270, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PRIVACY_SETTINGS_GROUP_ADD_LID_MIGRATION_ENABLE = new ABProp(16274, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PRIVACY_SETTINGS_PRESENCE_LID_MIGRATION_ENABLE = new ABProp(16275, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_PEER_SNAPSHOT_RECOVERY = new ABProp(16329, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GROUP_HISTORY_BUMP_MESSAGE_ID = new ABProp(16346, "200", "200");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp LIMIT_SHARING_UPDATE_ENABLED_WEB = new ABProp(16376, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WAVOIP_ML_BWE_IN_TEST_MODELS = new ABProp(16392, "", "");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SHOW_INTEGRITY_SCREENSHARING_FRICTION_UI = new ABProp(16411, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BIZ_AI_PRIORITY_LIST_ENABLED = new ABProp(16420, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WDS_RADIO_BASE = new ABProp(16448, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp UPDATED_SUSPICIOUS_CHARACTER_LINK_UI = new ABProp(16486, "0", "1");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_OFFLINE_RESUME_WAIT_FOR_PING_RESPONSE_ENABLED = new ABProp(16488, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_VIDEO_PLAY_LOGGING_ENABLED = new ABProp(16491, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_REWRITE_IN_EXPRESSION_TRAY_ENABLED = new ABProp(16510, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp FILES_MEDIA_HUB_WEB_VARIANT = new ABProp(16511, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_INACTIVE_GROUP_LID_MIGRATION = new ABProp(16520, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_CALL_LINK_CALL_LOG_AGGREGATION = new ABProp(16523, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp RICH_ORDER_STATUS_WA_WEB = new ABProp(16534, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MEMBER_NAME_TAG_DB_ENABLED = new ABProp(16551, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_RICH_RESPONSE_STRUCTURED_RESPONSE_TRUNCATED_TABLE_ENABLED = new ABProp(16564, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PNH_THREAD_PROMOTION_TO_GENERAL_LID = new ABProp(16632, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PDFN_TS_MS_LOGGING_ENABLED = new ABProp(16637, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PDFN_ENFORCE_MIN_IMPRESSION_DUR_MSECS = new ABProp(16653, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_RICH_RESPONSE_FORWARD_SENDING_ENABLED = new ABProp(16681, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_RICH_RESPONSE_FORWARD_RECEIVING_ENABLED = new ABProp(16682, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SUPPORT_LID_VCARD = new ABProp(16692, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MM_SIGNAL_SHARING_VERIFICATION_SYSTEM_LID_ENABLED = new ABProp(16727, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp USERNAME_CONTACT_HANDLE_USERNAME_DELETION = new ABProp(16730, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PHASH_V3_ENABLED = new ABProp(16731, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_INVITE_ADMINS_IN_CONTEXT_CARD_ENABLED = new ABProp(16746, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WDS_WEB_BUTTON = new ABProp(16785, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_INVITE_CONTACTS_TO_FOLLOW_PRODUCER_ENABLED = new ABProp(16789, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_INVITE_CONTACTS_TO_FOLLOW_CONSUMER_ENABLED = new ABProp(16790, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp REACTIONS_ALIGNMENT_FOR_TRANSPARENT_MESSAGES_ENABLED = new ABProp(16792, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_PTV_FORWARDED_ATTRIBUTION_UI_ENABLED = new ABProp(16793, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_AD_ACCOUNT_FIXER = new ABProp(16795, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_CONSOLE_LOG_LEVEL = new ABProp(16806, "3", "1");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp REACTIONS_SENDER_ENABLED_GROUP_CALL = new ABProp(16807, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_PDF_THUMBNAIL_SIZE_IN_BYTES = new ABProp(16834, "1300", "1300");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_IS_TEXT_QUESTION_FORWADING_ENABLED = new ABProp(16854, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_STICKER_FORWARDED_ATTRIBUTION_UI_ENABLED = new ABProp(16856, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_STICKER_PACK_FORWARDED_ATTRIBUTION_UI_ENABLED = new ABProp(16858, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PAYMENT_LINKS_TRUST_SIGNALS_METATAG_ENABLED = new ABProp(16866, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp USERNAME_CONTACT_1ON1_CHAT_PICKER = new ABProp(16877, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_REWRITE_STACK_UNDO_ENABLED = new ABProp(16943, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MM_OPT_OUT_LID_MIGRATION_ENABLED = new ABProp(16952, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_OFFLINE_RESUME_WAIT_FOR_PING_TIMEOUT_SECONDS = new ABProp(16956, "10", "10");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ADDING_CALL_LOGS_TO_SOME_REPORTS = new ABProp(16960, "0", "1");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp VIEW_REPLIES_WITH_THREADID_ENABLED = new ABProp(16998, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_SMB_DETECTED_OUTCOME_LABELS_LOGGING = new ABProp(17002, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ADJUSTABLE_CHATLIST_PANEL = new ABProp(17024, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SUSPICIOUS_CHAR_LINK_DIALOG_REDESIGN_LOGGING_ENABLED = new ABProp(17035, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp VOICE_CHAT_COMPANION_EXPERIENCE_VERSION = new ABProp(17052, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ANDROID_NFM_PAYLOAD_VALIDATION_KILLSWITCH_V2 = new ABProp(17070, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp STATUS_ALLOW_FORWARDING_TO_STATUS_ON_WEB = new ABProp(17071, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_EMOJI_FORWARDED_ATTRIBUTION_UI_ENABLED = new ABProp(17081, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_SMB_DATA_SHARING_CONSENT_STATE_ERROR_LOGGING = new ABProp(17082, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BIZ_AI_WEB_AI_HUB_TAP_CTA_SHOW_ALERT = new ABProp(17093, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp RASTERIZE_TEXT_STATUS_PIXEL_WIDTH_WITHOUT_MUSIC = new ABProp(17100, "1080", "1080");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_SIMPLIFIED_PROFILE_PAGE_ENABLED = new ABProp(17104, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DEEPLINK_STATUS_PRIVACY_SCREEN_ENABLED = new ABProp(17149, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_MEDIA_MAX_AUTODOWNLOAD = new ABProp(17153, "32", "32");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MMS_VCARD_AUTODOWNLOAD_SIZE_KB = new ABProp(17156, "64", "64");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PAYMENT_LINKS_TRUST_SIGNALS_METATAG_PSP_LIST = new ABProp(17162, "{\"psp\":[\"mercadopago\"]} ", "{\"psp\":[\"mercadopago\"]} ");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MEDIA_SHARING_TEAM_HOLDOUT_H2_2025 = new ABProp(17189, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp HISTORY_SYNC_ON_DEMAND_EXTENDED = new ABProp(17197, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp HISTORY_SYNC_ON_DEMAND_COMPANION = new ABProp(17198, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp TEST_STRING_LIST = new ABProp(17210, "Test,123", "Test,123");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WIN_HYBRID_TYPING_INDICATOR_ENABLED = new ABProp(17272, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_UGC_NOT_AN_EXPERT_ENABLED = new ABProp(17285, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_SMB_DATA_SHARING_CONSENT_STATE_REGULAR_SYNC = new ABProp(17301, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_LOGOUT_DB_HIST_SYNC_FAILED = new ABProp(17319, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_UNEXPECTED_LOCALE_RELOAD_FIX_ENABLED = new ABProp(17328, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_RESUME_TIMER_FIX_ENABLED = new ABProp(17329, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_DAU_OVERREPORTING_FIX = new ABProp(17350, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PAYMENT_LINKS_TRUST_SIGNALS_OTHER_METATAGS_ENABLED = new ABProp(17355, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_RICH_RESPONSE_SIDE_BY_SIDE_SURVEY_ENABLED = new ABProp(17408, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_QUESTION_FOLLOWER_ENABLED = new ABProp(17425, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_QUESTION_ADMIN_ENABLED = new ABProp(17426, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_BUSINESS_BROADCAST_IMPORT_CONTACT = new ABProp(17433, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp USERNAMES_ENFORCE_NO_PN_LEARNING = new ABProp(17448, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_LINKED_CATALOG_CONSUMER_CART_ENABLED = new ABProp(17466, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_HYBRID_SIMPLE_CHAT_CONVERSATION_CONTEXT_MENU_ENABLED = new ABProp(17479, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BOT_PROFILE_SYNC_MIGRATION_ENABLED = new ABProp(17485, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BIZ_AI_COEX_COMPANION_BIZ_PROFILE_FETCH_ENABLED = new ABProp(17493, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_RATING_AND_REVIEW_ENABLED = new ABProp(17540, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_SUPPRESS_MESSAGE_VIA_AD_SPAM_WEB = new ABProp(17580, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_TRIM_TRUNK_PREFIX = new ABProp(17599, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_QUESTIONS_INTEGRITY_M1_ENABLED = new ABProp(17600, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_CTWA_CHAT_HEADER_LABEL_ENTRY_POINT_NON_LABEL_USER_TIMESTAMP = new ABProp(17609, "1752537600", "1752537600");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp USERNAME_CONTACT_SYNCD_SUPPORT_ENABLE = new ABProp(17614, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_SEARCH_NULL_STATE_CONVO_STARTER_SUGGESTIONS_UPDATE_INTERVAL = new ABProp(17623, "86400", "86400");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MM_DATA_SHARING_DISCLOSURE_ON_CHAT_OPEN_ENABLED = new ABProp(17630, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp HYBRID_FONT_SIZE_DROPDOWN = new ABProp(17637, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_HYBRID_CONTEXT_MENU_REACTIONS_ENABLED = new ABProp(17650, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_UGC_PARODY_NEW_BEHAVIOR_ENABLED = new ABProp(17692, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_CONVO_STARTER_EXPERIMENT_GRAPHQL_CONFIG = new ABProp(17695, "", "");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CALLS_TAB_USERNAME_GLOBAL_SEARCH_ENABLED = new ABProp(17698, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_SEARCH_25_FEATURES_ENABLED = new ABProp(17703, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_OPEN_QPL_USER_RID_LOGGING_ENABLED = new ABProp(17712, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp HYBRID_NUX_BETA_50_ENABLED = new ABProp(17717, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_CALLS_TAB_EMPTY_STATE_BUTTONS = new ABProp(17724, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_CONVERSATION_STARTERS_GRAPHQL_CONFIG = new ABProp(17730, "", "");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_CALLING_PHONE_NUMBER_PRIVACY = new ABProp(17731, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMBA_BUSINESS_BROADCAST_GENAI_TEXT = new ABProp(17743, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHAT_PHOTO_POLL_SENDER_ENABLED = new ABProp(17788, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WDS_WEB_CHECKBOX = new ABProp(17790, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MESSAGE_EDIT_TO_MESSAGE_SECRET_RECEIVER_ENABLED = new ABProp(17811, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_INDIVIDUAL_NEW_CHAT_MSG_CAPPING_LIMIT = new ABProp(17845, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WDS_WEB_TYPE_MIGRATION = new ABProp(17903, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMBA_BUSINESS_BROADCAST_RECIPIENT_LIMIT = new ABProp(17937, "-1", "-1");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHAT_PHOTO_POLL_RECEIVER_ENABLED = new ABProp(17941, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMOOTHIE_PERFORMANCE_MSG_SEND = new ABProp(17942, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMOOTHIE_PERFORMANCE_CHATLIST_SEARCH = new ABProp(17946, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CALLING_RUST_MIGRATION_BITMAP = new ABProp(17954, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp META_AI_IN_APP_SURVEY_ENABLED = new ABProp(17956, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_METABOT_DOCUMENT_UPLOAD_ENABLED = new ABProp(17957, "", "");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_RICH_RESPONSE_FORWARD_VERIFICATION_ENABLED = new ABProp(17968, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp OTP_HISTORY_SYNC_WITH_PLACE_HOLDER = new ABProp(17980, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_SIGNAL_AUTO_ACKNOWLEDGE_STALE_SESSIONS = new ABProp(17987, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_SIGNAL_PREVENT_OLD_SESSION_LOOKUP_AND_PROMOTE = new ABProp(17988, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_HYBRID_HISTORY_SYNC_WINDOW = new ABProp(17994, "365", "365");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMOOTHIE_PERFORMANCE_MSG_SEND_FOLLOWUP = new ABProp(17996, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_COMPANION_BOOTSTRAP_BIZ_PROFILE_USYNC = new ABProp(18009, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMOOTHIE_PERFORMANCE_COMMAND_PALETTE = new ABProp(18021, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ADVANCED_CHAT_PRIVACY_CONTENT_UPDATE_JULY_25 = new ABProp(18025, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp UTILITY_ORDER_STATUS_USE_CURRENT_MESSAGE_PAYLOAD_FOR_ORDER_DETAIL = new ABProp(18030, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp LID_STATUS_PRE_MAT_ENABLED = new ABProp(18040, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp COEX_CALLING_ENABLED = new ABProp(18047, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_GRID_LAYOUT_TILE_UNIFICATION = new ABProp(18066, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp HYBRID_INCREMENTAL_ZOOMING_SIMPLE_ENABLED = new ABProp(18080, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_AVATARS_ON_WEB_COMPANION = new ABProp(18081, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PUSHNAME_BLOCKLIST_STARTING_WITH_AT = new ABProp(18097, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp INTERNAL_GROUP_INDICATOR = new ABProp(18109, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp KALEIDOSCOPE_THUMBNAIL_VALIDATION = new ABProp(18114, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_SMB_IGNORE_WAMO_CTWA_PAYLOADS = new ABProp(18117, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MM_SIGNAL_SHARING_COLLECTION_WINDOW_LOGGING_ENABLED = new ABProp(18126, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp USERNAME_CONTACT_UI_LOGGING = new ABProp(18146, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_AV_DOWNGRADE_1ON1 = new ABProp(18165, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMOOTHIE_PERFORMANCE_RESIZE = new ABProp(18177, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DAU_FIX_DELAY_PRESENCE_ON_FOCUS = new ABProp(18189, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp USERNAME_CONTACT_UI_VCARD = new ABProp(18204, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp UTILITY_ORDER_STATUS_THUMBNAIL_AUTODOWNLOAD_ENABLED = new ABProp(18206, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_RETRY_QUOTA_EXCEEDED_IDB_WRITES = new ABProp(18213, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PUSH_NAME_IN_GLOBAL_SEARCH_NON_CONTACTS_ENABLED = new ABProp(18216, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp LISTS_SMB_ENABLED = new ABProp(18229, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp KMP_SYNCD_ENGINE_OUTGOING_PROCESSOR_ENABLED = new ABProp(18234, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp LISTS_SMB_LABELS_CONVENTION_ENABLED = new ABProp(18250, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp USERNAME_GLOBAL_SEARCH_ENABLED = new ABProp(18251, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEBVIEW2_DISABLE_GPU_ACCELERATION = new ABProp(18262, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_QUOTA_EXCEEDED_APP_RELOAD_FLOW_ENABLED = new ABProp(18269, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_FORWARD_ATTRIBUTION_ENABLED = new ABProp(18286, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp STATUS_POG_ID_ROTATION_WINDOW_DAYS = new ABProp(18297, "-1", "-1");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ADDING_CALL_LOGS_IN_SOME_REPORTS_DB_QUERY_KILLSWITCH = new ABProp(18306, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_HYBRID_3_MONTH_HISTORY_SYNC_WINDOW = new ABProp(18307, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_REWRITE_NON_STREAMING_ENABLED = new ABProp(18316, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GROUP_HISTORY_SENDER_SPEED_BUMP_DIALOG_DISPLAY_TIMES = new ABProp(18317, "3", "3");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp UTILITY_ORDER_STATUS_ENABLE_MERGING_LOGIC_FOR_ORDER_ITEMS = new ABProp(18348, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp IOS_GLOBAL_SEARCH_PREFIX_BASED = new ABProp(18382, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp HISTORY_SYNC_ON_DEMAND_TIME_BOUNDARY_DAYS_DESKTOPS = new ABProp(18391, "1095", "1095");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_QUESTION_REPLY_RECEIVER_MESSAGE_TYPES_M1_ENABLED = new ABProp(18393, "", "25");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_QUESTION_REPLY_SENDER_MESSAGE_TYPES_M1_ENABLED = new ABProp(18394, "", "22");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp RT_MESSAGE_DROP_WHEN_INVALID = new ABProp(18397, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GROUP_HISTORY_MESSAGE_COUNT_LIMIT = new ABProp(18405, "100", "100");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GROUP_HISTORY_MESSAGES_TIME_LIMIT_SECS = new ABProp(18406, "1209600", "1209600");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_CONVERSATION_MESSAGES_ISLAND = new ABProp(18412, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_3PD_DATA_SHARING_COOLDOWN_MAX_TIMES_SHOWN_FOR_DISMISSED = new ABProp(18423, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp OHAI_QPL_RID_LOGGING_ENABLED = new ABProp(18446, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp STATUS_DUAL_UPLOAD_QPL_RID_LOGGING_ENABLED = new ABProp(18447, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp STATUS_RANKING_QPL_RID_LOGGING_ENABLED = new ABProp(18448, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp STATUS_LOCATION_QPL_RID_LOGGING_ENABLED = new ABProp(18449, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp STATUS_EMS_QPL_RID_LOGGING_ENABLED = new ABProp(18450, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp STATUS_MUSIC_QPL_RID_LOGGING_ENABLED = new ABProp(18451, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_MEM_MEMOIZE_CONCURRENT_LRU_MEDIA_STORE_ACCESS_ENABLED = new ABProp(18457, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_CODA_FEATURE_ENABLED = new ABProp(18464, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_NEW_USER_ACTION_STANZA_FOR_RAISE_HAND_SENDER = new ABProp(18489, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_CALL_GRID_HIDE_SELF_VIEW = new ABProp(18492, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MAX_MUTATIONS_FOR_SYNC_RECOVERY = new ABProp(18535, "2000", "2000");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_SCAMS_GROUP_ENGAGMENT_LOGGING = new ABProp(18542, "0", "1");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_ADMIN_NOTIFICATIONS_ENABLED = new ABProp(18560, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_CHAT_THREADS_ENABLED = new ABProp(18587, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WDS_WEB_TOOLTIP = new ABProp(18601, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_CREATION_ENTRYPOINT_IN_DIRECTORY_ENABLED = new ABProp(18613, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_DATA_SHARING_DISCLOSURE_NEW_CONTENT_FOR_INFO_SHARING = new ABProp(18621, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_DATA_SHARING_DISCLOSURE_NEW_ICON = new ABProp(18623, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PIX_HOLDOUT_ENABLED = new ABProp(18659, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_DISABLE_LOGS_LOW_END_DEVICE = new ABProp(18660, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DIALER_PAD_FOR_NEW_CHATS = new ABProp(18688, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_QUERY_COMMENTS_TABLE_ONLY_FOR_CAG_ENABLED = new ABProp(18696, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp RNR_MIN_DAYS_USER_ACTIVE = new ABProp(18702, "2", "2");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp RNR_DAYS_COOLDOWN = new ABProp(18703, "100000", "100000");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp OPTIMIZED_DELIVERY_BLOCK_AND_REPORT_ENTRY_POINTS_ALLOWLIST_WEB = new ABProp(18736, "4,10,12,13,14,15,17,18,24,31,32,33,34,35,36,39,40,45", "4,10,12,13,14,15,17,18,24,31,32,33,34,35,36,39,40,45");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_RATING_AND_REVIEW_CONTEXTUAL_PROMPT_ENABLED = new ABProp(18737, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_SEARCH_EXPERIENCE_WEB_ENABLED = new ABProp(18740, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_RICH_RESPONSE_UR_MEDIA_GRID_ENABLED = new ABProp(18746, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_LOW_END_DEVICE_LEVEL = new ABProp(18747, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ANDI_TEST_ABPROP = new ABProp(18752, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_CHAT_META_AI_BANNER_M2_ENABLED = new ABProp(18784, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SNAPSHOT_RECOVERY_MAX_MUTATIONS_COUNT_ALLOWED = new ABProp(18786, "2000", "2000");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PSI_IN_SEARCH_PROTOTYPE_ENABLED = new ABProp(18795, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_FALCO_CLEAR_LOCAL_STORAGE_QUEUE_ENABLED = new ABProp(18835, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_PTT_FORWARDING_FIX = new ABProp(18840, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_MIGRATE_AWAY_FROM_INLINE_TOS_ENABLED = new ABProp(18843, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PAYMENTS_COPY_PASTE_MEXICO_ENABLED = new ABProp(18844, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_NATIVE_ADS_CREATION_WEB_ENABLED = new ABProp(18857, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CUSTOM_NOTIFICATION_TONES = new ABProp(18884, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_CREATION_ENTRYPOINT_IN_UPDATES_TAB_ENABLED = new ABProp(18925, "0", "1");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp NEW_CHAT_MSG_CAPPING_FIRST_WARNING_THRESHOLD_PERCENTAGE = new ABProp(18967, "50", "50");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp USERNAME_CHECK_DEBOUNCE_IN_MS = new ABProp(18975, "600", "600");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_QUESTION_FETCH_RESPONSES_PAGE_SIZE = new ABProp(18984, "30", "30");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_QUESTION_FORWARD_MESSAGE_TYPES_CHAT_M1_ENABLED = new ABProp(18988, "", "22");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMOOTHIE_PERFORMANCE_RESIZE_FOLLOWUP = new ABProp(18992, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMOOTHIE_PERFORMANCE_CSS_DOM = new ABProp(18995, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp COEX_EDIT_MSG_ENABLED = new ABProp(19039, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_INVITE_CONTACTS_TO_FOLLOW_UI_ENABLED = new ABProp(19050, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_REPLY_FORWARD_MESSAGE_TYPES_CHAT_M1_ENABLED = new ABProp(19053, "", "25");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp UTILITY_ORDER_STATUS_LOGGING_ENABLED = new ABProp(19059, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_AC_SHARED_MEMORIES_ENABLED = new ABProp(19081, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DISAPPEARING_MESSAGE_TIMER_MORE_OPTIONS = new ABProp(19083, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_HISTORY_SYNC_DYNAMIC_THROTTLING = new ABProp(19110, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PUBLIC_BUG_REPORTING_SIDEBAR = new ABProp(19124, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PUBLIC_BUG_REPORTING_SETTINGS = new ABProp(19127, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_NOTIFICATIONS_BANNER_VARIANT = new ABProp(19168, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_QUESTION_FORWARD_MESSAGE_TYPES_STATUS_M1_ENABLED = new ABProp(19169, "", "22");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_GRID_LAYOUT_HSCROLL_RESIZING = new ABProp(19179, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp FREQUENTLY_FORWARDED_GROUP_SETTING_ENABLED = new ABProp(19185, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_REPLY_FORWARD_SHOW_QUOTED_SNIPPET_IN_CHATS = new ABProp(19210, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_CHANNEL_CREATE_DELETE_LOGGING = new ABProp(19222, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DM_RECEIVER_ALLOWED_VALUES = new ABProp(19232, "{\"timers\": [0, 86400, 604800, 7776000]}", "{\"timers\": [0, 86400, 604800, 7776000]}");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PAYMENTS_BR_PIX_WEB_ATTACHMENT_TRAY = new ABProp(19276, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp COEX_EDIT_MESSAGE_ENABLED_CONSUMER = new ABProp(19284, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp COEX_REVOKE_MESSAGE_ENABLED = new ABProp(19285, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHATLIST_SHOW_DRAFT_FOR_EMPTY_CHAT = new ABProp(19287, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ANIMATED_EMOJIS_SET_2_ENABLED = new ABProp(19288, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_ANR_THROTTLE_HISTORY_SYNC_DB_WRITES = new ABProp(19298, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp NEWSLETTER_RCAT_FIELD_GENERATING_ENABLED = new ABProp(19303, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_NOTIFICATIONS_BANNER_NEW_LOGIC_ENABLED = new ABProp(19399, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PAYMENT_LINK_TRACE_ID_LOGGING_ENABLED = new ABProp(19440, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp HYBRID_FLYTRAP_FEEDBACK_ENABLED = new ABProp(19495, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MM_OPT_OUT_ALWAYS_USE_LID_IN_IQ = new ABProp(19502, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DESKTOP_UPSELL_INTRO_PANEL_ILLUSTRATION_VARIANT = new ABProp(19518, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_QPL_IMPROVEMENTS_SUPPORTED_TYPES = new ABProp(19589, "", "1,2");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_RICH_RESPONSE_FORWARDING_VERIFICATION_ENABLED_V1 = new ABProp(19590, "\"none\"", "\"none\"");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp VOICE_AI_CONVERSATION_STARTER_LATENCY_TRACKING = new ABProp(19624, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp COEX_REVOKE_MESSAGE_ENABLED_CONSUMER = new ABProp(19633, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_MENTION_EVERYONE_RECEIVER = new ABProp(19652, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_MENTION_EVERYONE_SENDER = new ABProp(19653, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_WEB_FORWARD_FLOW_ENABLED = new ABProp(19676, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp LID_STATUS_NON_SOAKED_CLIENT_SUPPORT_ENABLED = new ABProp(19696, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_HYBRID_GETTERS_CACHE_ENABLED = new ABProp(19700, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_USE_FROM_ME_SENT_BY_ME = new ABProp(19724, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp LINK_PREVIEW_M3_TRUNCATE_WHEN_TRAILING = new ABProp(19730, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PTT_LOCK_MINIMUM_DURATION_MS = new ABProp(19735, "1000", "1000");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_PER_CUSTOMER_DATA_SHARING_CONTROLS_DO_NOT_SHOW_MSG_UNTIL_CHOSEN = new ABProp(19763, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_QUIZ_SENDING_ENABLED = new ABProp(19777, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_QUIZ_RECEIVING_ENABLED = new ABProp(19778, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MESSAGE_CAPPING_UPSELL_VERSION = new ABProp(19781, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GROUP_HISTORY_MESSAGE_COUNT_RECEIVER_UPPER_LIMIT = new ABProp(19811, "100", "100");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_WAITING_ROOM_UI = new ABProp(19819, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_METABOT_DOCUMENT_UPLOAD_SIZE_LIMIT_MB = new ABProp(19823, "40", "40");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_ENABLE_IMPROVED_BULK_MERGE = new ABProp(19854, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp VIEW_REPLIES_ENTRY_POINT = new ABProp(19860, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CALL_GRID_LAYOUT_LOGGING_VERSION = new ABProp(19870, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp NEWSLETTER_FORWARD_COUNTER_UI_ENABLED = new ABProp(19888, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp NEWSLETTER_FORWARD_COUNTER_INFRA_ENABLED = new ABProp(19889, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_FMX_LOGGING = new ABProp(19893, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_RATE_APP_PROMPT = new ABProp(19894, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_HYBRID_VIDEO_TRANSCODING = new ABProp(19895, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_CHANNEL_VIDEO_SERVER_TRANSCODE_UPLOAD = new ABProp(19920, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_CORE_BIZ_PROFILE_UX_REFRESHED = new ABProp(19929, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_FALCO_BANZAI_ENABLED = new ABProp(19934, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_INDIVIDUAL_NEW_CHAT_MSG_CAPPING_OTE_REASON = new ABProp(19942, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_WEB_CUSTOM_LABEL_SIGNALS_ENABLED = new ABProp(19985, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_METABOT_DOCUMENT_UPLOAD_PAGE_COUNT_LIMIT = new ABProp(19987, "100000", "100000");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_QUESTION_RESPONSE_RATE_LIMIT_MAX_COUNT_IN_CLIENT_UI = new ABProp(19989, "5", "5");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_FIX_DUPLICATED_LIDS_HISTORY_SYNC = new ABProp(19994, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WAMO_AD_FETCH_AND_CACHING = new ABProp(20034, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_UGC_HIDE_ENABLED = new ABProp(20041, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_IP_TOKEN_ENABLED = new ABProp(20043, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_QUESTIONS_REMOVE_QUESTION_METADATA_WHEN_FORWARDING_ENABLED = new ABProp(20059, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_APP_LOCK_UPSELL = new ABProp(20064, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_HYBRID_VIDEO_TRANSCODING_FOR_VALID_MP4 = new ABProp(20070, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BIZ_AI_AGENT_THREAD_STATUS_HISTORY_SYNC_ENABLED = new ABProp(20099, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_BOT_IDENTITY_KEY_VERIFICATION_VERSION = new ABProp(20107, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_WEB_3PD_DATA_SHARING_COOLDOWN_FOR_OPTED_OUT = new ABProp(20131, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_QUESTIONS_INCLUDE_QUESTION_METADATA_WHEN_FORWARDING_ENABLED = new ABProp(20148, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp NOISE_PQ_MODE = new ABProp(20161, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_STICKER_PACK_RENDERING = new ABProp(20182, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNEL_STICKER_PACK_FORWARDING = new ABProp(20212, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_MSG_LIST_SMART_MARGIN_VIRTUALIZATION = new ABProp(20214, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_SMB_DETECTED_OUTCOME_LISTS_ENABLED = new ABProp(20220, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WIN_HYBRID_FORCE_PERSISTENT_STORAGE_PERMISSION = new ABProp(20260, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_MUSIC_RECEIVER_ENABLED = new ABProp(20266, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PAYMENTS_COPY_PASTE_MEXICO_CLABE_BANK_LIST = new ABProp(20318, "{   \"133\": \"ACTINVER\",   \"062\": \"AFIRME\",   \"721\": \"albo\",   \"706\": \"ARCUS FI\",   \"659\": \"ASP INTEGRA OPC\",   \"127\": \"AZTECA\",   \"166\": \"BaBien\",   \"030\": \"BAJIO\",   \"002\": \"BANAMEX\",   \"154\": \"BANCO COVALTO\",   \"006\": \"BANCOMEXT\",   \"137\": \"BANCOPPEL\",   \"160\": \"BANCO S3\",   \"152\": \"BANCREA\",   \"019\": \"BANJERCITO\",   \"147\": \"BANKAOOL\",   \"106\": \"BANK OF AMERICA\",   \"159\": \"BANK OF CHINA\",   \"009\": \"BANOBRAS\",   \"072\": \"BANORTE\",   \"058\": \"BANREGIO\",   \"060\": \"BANSI\",   \"001\": \"BANXICO\",   \"129\": \"BARCLAYS\",   \"145\": \"BBASE\",   \"012\": \"BBVA MEXICO\",   \"112\": \"BMONEX\",   \"677\": \"CAJA POP MEXICA\",   \"683\": \"CAJA TELEFONIST\",   \"715\": \"CASHI CUENTA\",   \"631\": \"CI BOLSA\",   \"124\": \"CITI MEXICO\",   \"901\": \"CLS\",   \"903\": \"CoDi Valida\",   \"130\": \"COMPARTAMOS\",   \"140\": \"CONSUBANCO\",   \"652\": \"CREDICAPITAL\",   \"688\": \"CREDICLUB\",   \"680\": \"CRISTOBAL COLON\",   \"723\": \"Cuenca\",   \"729\": \"Dep y Pag Dig\",   \"151\": \"DONDE\",   \"616\": \"FINAMEX\",   \"634\": \"FINCOMUN\",   \"734\": \"FINCO PAY\",   \"699\": \"FONDEADORA\",   \"685\": \"FONDO (FIRA)\",   \"601\": \"GBM\",   \"167\": \"HEY BANCO\",   \"168\": \"HIPOTECARIA FED\",   \"021\": \"HSBC\",   \"155\": \"ICBC\",   \"036\": \"INBURSA\",   \"902\": \"INDEVAL\",   \"150\": \"INMOBILIARIO\",   \"059\": \"INVEX\",   \"110\": \"JP MORGAN\",   \"128\": \"KAPITAL\",   \"661\": \"KLAR\",   \"653\": \"KUSPIT\",   \"670\": \"LIBERTAD\",   \"602\": \"MASARI\",   \"722\": \"Mercado Pago W\",   \"720\": \"MexPago\",   \"042\": \"MIFEL\",   \"158\": \"MIZUHO BANK\",   \"600\": \"MONEXCB\",   \"108\": \"MUFG\",   \"132\": \"MULTIVA BANCO\",   \"135\": \"NAFIN\",   \"638\": \"NU MEXICO\",   \"710\": \"NVIO\",   \"148\": \"PAGATODO\",   \"732\": \"Peibo\",   \"620\": \"PROFUTURO\",   \"156\": \"SABADELL\",   \"014\": \"SANTANDER\",   \"044\": \"SCOTIABANK\",   \"157\": \"SHINHAN\",   \"728\": \"SPIN BY OXXO\",   \"646\": \"STP\",   \"703\": \"TESORED\",   \"684\": \"TRANSFER\",   \"138\": \"UALA\",   \"656\": \"UNAGRA\",   \"617\": \"VALMEX\",   \"605\": \"VALUE\",   \"113\": \"VE POR MAS\",   \"141\": \"VOLKSWAGEN\" }", "{   \"133\": \"ACTINVER\",   \"062\": \"AFIRME\",   \"721\": \"albo\",   \"706\": \"ARCUS FI\",   \"659\": \"ASP INTEGRA OPC\",   \"127\": \"AZTECA\",   \"166\": \"BaBien\",   \"030\": \"BAJIO\",   \"002\": \"BANAMEX\",   \"154\": \"BANCO COVALTO\",   \"006\": \"BANCOMEXT\",   \"137\": \"BANCOPPEL\",   \"160\": \"BANCO S3\",   \"152\": \"BANCREA\",   \"019\": \"BANJERCITO\",   \"147\": \"BANKAOOL\",   \"106\": \"BANK OF AMERICA\",   \"159\": \"BANK OF CHINA\",   \"009\": \"BANOBRAS\",   \"072\": \"BANORTE\",   \"058\": \"BANREGIO\",   \"060\": \"BANSI\",   \"001\": \"BANXICO\",   \"129\": \"BARCLAYS\",   \"145\": \"BBASE\",   \"012\": \"BBVA MEXICO\",   \"112\": \"BMONEX\",   \"677\": \"CAJA POP MEXICA\",   \"683\": \"CAJA TELEFONIST\",   \"715\": \"CASHI CUENTA\",   \"631\": \"CI BOLSA\",   \"124\": \"CITI MEXICO\",   \"901\": \"CLS\",   \"903\": \"CoDi Valida\",   \"130\": \"COMPARTAMOS\",   \"140\": \"CONSUBANCO\",   \"652\": \"CREDICAPITAL\",   \"688\": \"CREDICLUB\",   \"680\": \"CRISTOBAL COLON\",   \"723\": \"Cuenca\",   \"729\": \"Dep y Pag Dig\",   \"151\": \"DONDE\",   \"616\": \"FINAMEX\",   \"634\": \"FINCOMUN\",   \"734\": \"FINCO PAY\",   \"699\": \"FONDEADORA\",   \"685\": \"FONDO (FIRA)\",   \"601\": \"GBM\",   \"167\": \"HEY BANCO\",   \"168\": \"HIPOTECARIA FED\",   \"021\": \"HSBC\",   \"155\": \"ICBC\",   \"036\": \"INBURSA\",   \"902\": \"INDEVAL\",   \"150\": \"INMOBILIARIO\",   \"059\": \"INVEX\",   \"110\": \"JP MORGAN\",   \"128\": \"KAPITAL\",   \"661\": \"KLAR\",   \"653\": \"KUSPIT\",   \"670\": \"LIBERTAD\",   \"602\": \"MASARI\",   \"722\": \"Mercado Pago W\",   \"720\": \"MexPago\",   \"042\": \"MIFEL\",   \"158\": \"MIZUHO BANK\",   \"600\": \"MONEXCB\",   \"108\": \"MUFG\",   \"132\": \"MULTIVA BANCO\",   \"135\": \"NAFIN\",   \"638\": \"NU MEXICO\",   \"710\": \"NVIO\",   \"148\": \"PAGATODO\",   \"732\": \"Peibo\",   \"620\": \"PROFUTURO\",   \"156\": \"SABADELL\",   \"014\": \"SANTANDER\",   \"044\": \"SCOTIABANK\",   \"157\": \"SHINHAN\",   \"728\": \"SPIN BY OXXO\",   \"646\": \"STP\",   \"703\": \"TESORED\",   \"684\": \"TRANSFER\",   \"138\": \"UALA\",   \"656\": \"UNAGRA\",   \"617\": \"VALMEX\",   \"605\": \"VALUE\",   \"113\": \"VE POR MAS\",   \"141\": \"VOLKSWAGEN\" }");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ANYONE_CAN_LINK_M2_CLIENT_CHANGES_ENABLED = new ABProp(20331, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp COEX_HIDDEN_CAPI_HSM_MESSAGES_ON_SMB_COMPANION_TYPE = new ABProp(20335, "MARKETING", "MARKETING");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp COEX_HIDE_CAPI_MARKETING_MESSAGES_ON_SMB_COMPANION_ENABLED = new ABProp(20337, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ADMIN_ONLY_MENTION_EVERYONE_GROUP_SIZE = new ABProp(20354, "33", "33");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENGAGEMENT_ORIGIN_STATUS_LOGGING_ENABLED = new ABProp(20365, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_USE_KALEIDOSCOPE_MEDIA_CHECK_ENABLED = new ABProp(20375, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MM_USER_CONTROLS_ENTRY_POINTS_UPDATE_M1_MENU = new ABProp(20381, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WAMO_PRIVACY_TOS_DEEMED_ACCEPTANCE_ROW_UNLINKED_NOTICE_ID = new ABProp(20384, "20610230", "20610230");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WAMO_PRIVACY_TOS_DEEMED_ACCEPTANCE_ROW_LINKED_NOTICE_ID = new ABProp(20385, "20610231", "20610231");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WAMO_PRIVACY_TOS_DEEMED_ACCEPTANCE_KILLSWITCH = new ABProp(20386, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MM_USER_CONTROLS_ENTRY_POINTS_UPDATE_M1_ICON = new ABProp(20388, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WAMO_STATUS_AD_PREVIEW_LINK_V2 = new ABProp(20399, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_CHAT_META_AI_GLASSES_BANNER_ENABLED = new ABProp(20405, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_NATIVE_ADS_CREATION_WEB_HAWK_TOOL_ENABLED = new ABProp(20442, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PIX_PAYMENT_REQUEST_ENABLED = new ABProp(20449, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PIX_ONBOARDING_EDUCATION_ENABLED = new ABProp(20450, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp USERNAME_MAX_LENGTH = new ABProp(20459, "35", "35");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMBA_BUSINESS_BROADCAST_GENAI_CUSTOM_USER_PROMPT_ENABLED = new ABProp(20464, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_PIX_AWARENESS_NUX_WWW_10_06 = new ABProp(20492, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp USERNAME_MIN_LENGTH = new ABProp(20494, "3", "3");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_VIDEO_METRICS_FIX = new ABProp(20520, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_WEB_META_AI_IMAGE_INPUT_ENABLED = new ABProp(20522, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SCAMS_GROUP_ENGAGEMENT_MAX_GROUPS_LIMIT = new ABProp(20543, "20", "20");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PENDING_GROUP_REQUESTS_PERSISTENT_BANNER = new ABProp(20545, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNEL_ENFORCEMENT_LOGGING_ENABLED = new ABProp(20549, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_PINNING_NUDGE_ENABLED = new ABProp(20551, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEBTP_USE_THUMBNAIL_RENDERER = new ABProp(20555, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_VOIP_VIDEO_RENDERER = new ABProp(20573, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_WEB_META_AI_PDF_DOCUMENT_INPUT_ENABLED = new ABProp(20581, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_INDIVIDUAL_NEW_CHAT_MSG_LATEST_RAMPUP_DATE = new ABProp(20601, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_SEARCH_META_AI_SEND_BUTTON_ENABLED = new ABProp(20603, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEBTP_USE_PDF_RENDERER = new ABProp(20607, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_CLEAR_SELECTED_CHATS_ENABLED = new ABProp(20626, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_INDIVIDUAL_NEW_CHAT_MSG_CAPPING_FETCH_TTL_SECONDS = new ABProp(20649, "3600", "3600");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_CHAT_THREADS_INFRA_ENABLED = new ABProp(20652, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GROUP_HISTORY_SUPPORT_HISTORY_SYNC_RECEIVER_PRE_CHAT = new ABProp(20658, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_INDIVIDUAL_NEW_CHAT_MSG_CAPPING_MV_GET_SUBSCRIPTION_V2 = new ABProp(20667, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp KMP_SYNCD_ENGINE_INCOMING_PROCESSOR_ENABLED = new ABProp(20682, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_NATIVE_ADS_CREATION_WEB_TARGETING_MODAL_HAWK_TOOL_ENABLED = new ABProp(20731, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_WWAI_ON_COMPANION = new ABProp(20768, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_3PD_ADD_MSG_DEPTH = new ABProp(20795, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PROFILE_SCRAPING_PRIVACY_TOKEN_IN_ABOUT_USYNC = new ABProp(20798, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp FAVORITE_STICKER_SYNC_AFTER_PAIRING_ENABLED_WEB = new ABProp(20815, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp VIEW_REPLIES_IS_COMPOSER_ENABLED = new ABProp(20817, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BIZ_AI_TOS_VARIANT = new ABProp(20833, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_INVITE_CONTACTS_TO_FOLLOW_RECEIVER_LOGGING_ENABLED = new ABProp(20836, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_INVITE_CONTACTS_TO_FOLLOW_SENDER_LOGGING_ENABLED = new ABProp(20837, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_3P_CONTACTS_SHARE_HYBRID = new ABProp(20849, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_VOIP_VIDEO_RENDERER_THREADING_MODE = new ABProp(20859, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_INDIVIDUAL_NEW_CHAT_MSG_CAPPING_ENABLED = new ABProp(20865, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_IMAGINE_MEDIA_VIEWER_ENABLED = new ABProp(20882, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp HIDE_AUTO_QUOTES_ON_WEB = new ABProp(20892, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_WINDOWS_JUMPLIST_HYBRID = new ABProp(20899, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_REWRITE_LOAD_MORE_ENABLED = new ABProp(20918, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_WEB_GROUP_CALLING = new ABProp(20924, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMBA_BUSINESS_BROADCAST_GENAI_SHARE_MESSAGE_HISTORY = new ABProp(20926, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMBA_BUSINESS_BROADCAST_GENAI_TEXT_MODEL = new ABProp(20929, "LLAMA", "LLAMA");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMBA_BUSINESS_BROADCAST_GENAI_TEXT_MAX_TRIES = new ABProp(20946, "30", "30");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WDS_WEB_CHIP = new ABProp(20970, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp USERNAME_CONTACT_PRIVACY_SETTING_ALLOW_UNCONTACT_SET_ENABLE = new ABProp(20993, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_PIX_AWARENESS_NPUX_WWW_10_06 = new ABProp(21006, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_PIX_AWARENESS_PUX_WWW_10_06 = new ABProp(21007, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp HISTORY_SYNC_ON_DEMAND_COMPLETE = new ABProp(21014, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp HISTORY_SYNC_ON_DEMAND_COMPLETE_COMPANION = new ABProp(21024, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_WINDOWS_HYBRID_JUMPLIST_CONTACTS = new ABProp(21057, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_THREADS_INFRA_ENABLED = new ABProp(21062, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DSA_21_CHANNEL_REPORTING_ENABLED = new ABProp(21073, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_RENDER_MEDIA_EDITOR_PREVIEW_AS_IMG = new ABProp(21076, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_DYNAMIC_MODEL_BRANDING_ENABLED = new ABProp(21086, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PRIVACY_SETTINGS_USERNAME_SENDING_ENABLE = new ABProp(21089, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_REWRITE_LANGUAGES_AND_TONES_CONFIG = new ABProp(21139, "{}", "{\"en\": \"rephrase,professional,funny,supportive,proofread\"}");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHATLIST_PREVENT_AUTOREAD = new ABProp(21156, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_MSG_BUBBLE_CONTEXT_MENU_ENABLED = new ABProp(21166, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_SHARING_FILES_FROM_WEB_WINDOWS_HYBRID = new ABProp(21184, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp NAV_CHAIN_ENABLED = new ABProp(21185, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WCF_SKIP_SENDING_1_100 = new ABProp(21194, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp HS_THUMBNAIL_SYNC_DAYS_LIMIT = new ABProp(21217, "0", "90");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PAA_SUPPORT_FOR_DISABLED_EPEHEMERALITY = new ABProp(21235, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_NAVIGATION_BAR_UPDATES_TAB = new ABProp(21250, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GROUP_HISTORY_SETTINGS = new ABProp(21261, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MM_DATA_SHARING_DISCLOSURE_ENABLED_COMPANION_HISTORY_SYNC = new ABProp(21288, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GROUP_HISTORY_MESSAGES_TIME_LIMIT_RECEIVER_ENFORCEMENT_SECS = new ABProp(21313, "1209600", "1209600");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_CAPPING_LOCAL_DATA_LOGIC_UPDATE = new ABProp(21348, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_VOIP_VIDEO_CAPTURE_IMPL = new ABProp(21350, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WDS_WEB_ROBOTO = new ABProp(21379, "0", "3");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp KALEIDOSCOPE_FORENSIC_UPLOAD_MASK = new ABProp(21401, "0", "3");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_INDIVIDUAL_NEW_CHAT_MSG_FCI_STALENESS_TTL_IN_SECONDS = new ABProp(21410, "120", "120");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMBA_BB_GENAI_COMPOSER_MIN_WORDS = new ABProp(21447, "4", "4");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GROUP_HISTORY_SETTINGS_TOGGLE_UI = new ABProp(21481, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_STATUS_CROSSPOSTING_ENABLED = new ABProp(21501, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_BUSINESS_BROADCAST_SEND_WEB = new ABProp(21508, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_CONTINUOUS_SESSION_TRANSPARENCY_NOTICE_ENABLED = new ABProp(21510, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_TOOLTIP_FOR_MEDIA_HUB = new ABProp(21535, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_IMAGINE_MEDIA_VIEWER_VERSION = new ABProp(21555, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GROUP_HISTORY_RECEIVER_FLOATING_BANNER = new ABProp(21568, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_TEMPLATE_SENTIMENT_SURVEY_USE_FIELDSTATS = new ABProp(21582, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp STATUS_RESPONSIVENESS_LOGGING_ENABLED = new ABProp(21588, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_QUESTIONS_CACHE_SIZE = new ABProp(21590, "30", "30");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp UNIFIED_CALLING_ENTRY_POINT_DESKTOP_TYPE = new ABProp(21591, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_WAITING_ROOM_ADMIN_UI = new ABProp(21676, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_VOIP_AUDIO_CAPTURE_IMPL = new ABProp(21688, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_VOIP_AUDIO_PLAYBACK_IMPL = new ABProp(21689, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp RT_SWAPPED_FALLBACK_VALIDATION = new ABProp(21718, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WAVOIP_ML_MEDIA_AUTOMOS_MODEL_DOWNLOAD_VERSIONS = new ABProp(21731, "", "");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WAVOIP_ML_BWE_CONG_MODEL_DOWNLOAD_VERSIONS = new ABProp(21732, "", "");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WAVOIP_ML_BWE_RL_MODEL_DOWNLOAD_VERSIONS = new ABProp(21733, "", "");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WAVOIP_ML_BWE_TR_MODEL_DOWNLOAD_VERSIONS = new ABProp(21734, "", "");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WAVOIP_ML_MEDIA_VSR_MODEL_DOWNLOAD_VERSIONS = new ABProp(21735, "", "");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WAVOIP_ML_MEDIA_VMOS_MODEL_DOWNLOAD_VERSIONS = new ABProp(21736, "", "");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WAVOIP_ML_MEDIA_NS_MODEL_DOWNLOAD_VERSIONS = new ABProp(21737, "", "");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WAVOIP_ML_BWE_HD_TARGET_MODEL_DOWNLOAD_VERSIONS = new ABProp(21738, "", "");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BR_PAYMENTS_PIX_GROUPS_ENABLED = new ABProp(21741, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_WAE_QPL_ENABLED = new ABProp(21742, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WAMO_PRIVACY_TOS_DEEMED_ACCEPTANCE_ROW_UNLINKED_NOTICE_ID_V2 = new ABProp(21770, "20610232", "20610232");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WAMO_PRIVACY_TOS_DEEMED_ACCEPTANCE_ROW_LINKED_NOTICE_ID_V2 = new ABProp(21771, "20610233", "20610233");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MM_OPTIMIZED_DELIVERY_REPLACING_SHIMMED_LINKS_ENABLED = new ABProp(21782, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEBVIEW2_ENABLE_OFFLINE_SUPPORT = new ABProp(21793, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp FUNCTIONAL_CHATLIST_ENABLED = new ABProp(21799, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp FUNCTIONAL_CHATLIST_PANEL_ENABLED = new ABProp(21800, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WAVOIP_ML_TEMP_MODEL_DOWNLOAD_VERSIONS = new ABProp(21815, "", "");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_SUPPRESS_MESSAGE_WITH_EXTERNAL_AD_REPLY_CONSUMER_DB_LEVEL_ENABLED = new ABProp(21819, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WAVOIP_ML_BWE_GC_UNDERSHOOT_MODEL_DOWNLOAD_VERSIONS = new ABProp(21821, "", "");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WAVOIP_ML_BWE_GC_HD_TARGET_MODEL_DOWNLOAD_VERSIONS = new ABProp(21822, "", "");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp COEX_EDIT_MESSAGE_ENABLED_FOR_MAIBA_BUSINESS = new ABProp(21886, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp COEX_EDIT_MESSAGE_ENABLED_FOR_MAIBA_CONSUMER = new ABProp(21888, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp USERNAME_LID_MIGRATION_CALLING = new ABProp(21890, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp COEX_REVOKE_MESSAGE_ENABLED_FOR_MAIBA_BUSINESS = new ABProp(21891, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp COEX_REVOKE_MESSAGE_ENABLED_FOR_MAIBA_CONSUMER = new ABProp(21892, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_ENABLE_GRANULAR_NOTIFICATIONS = new ABProp(21909, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_DISABLE_PREFETCH_LOADABLES = new ABProp(21917, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DEFENSE_MODE_QUARANTINE_MESSAGE_EXPIRATION_WINDOW = new ABProp(21918, "1210000", "1210000");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DEFENSE_MODE_QUARANTINE_BULK_UNBLOCK_LIMIT = new ABProp(21921, "50", "50");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp USERNAME_SUGGESTIONS_ENABLED = new ABProp(21984, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_AGM_FLOW_CTA = new ABProp(22006, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_REPLY_MESSAGE_CONTEXT_MAX_COUNT = new ABProp(22024, "20", "20");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_REPLY_MESSAGE_CONTEXT_TRIGGER_MIN_COUNT = new ABProp(22025, "10", "10");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_CHAT_THREAD_CAPABILITY_ENABLED = new ABProp(22038, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_SMB_AE_ONBOARDING_COMPATABILITY_CODE = new ABProp(22067, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_CHAT_THREADS_HISTORICAL_MESSAGES_MIGRATION_ENABLED = new ABProp(22070, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_LISTS_M2_ENABLED = new ABProp(22086, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_MUSIC_FORWARDING_DISABLED = new ABProp(22089, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_LISTS_M1_ENABLED = new ABProp(22090, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_WAM_MAXIMUM_WAIT_TIME_FOR_ONLINE_SECONDS = new ABProp(22091, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_TIERING_IMPROVEMENTS = new ABProp(22139, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_CACHE_OPEN_FAILED_RELOAD_FLOW_ENABLED = new ABProp(22155, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_GROUPS_OPEN_ENABLED = new ABProp(22165, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_GROUP_PARTICIPATION_ENABLED = new ABProp(22171, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_GROUP_PARTICIPATION_ADD_ENABLED = new ABProp(22183, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_GROUP_PARTICIPATION_SEND_ENABLED = new ABProp(22184, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_CALLING_PERF_OPTIMIZATIONS_BITMASK = new ABProp(22186, "1", "1");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp UTILITY_ORDER_STATUS_BUSINESS_VIEW_ENABLED = new ABProp(22190, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_INVITE_LINK_PREVIEW_ENABLED = new ABProp(22195, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_INVITE_LINK_PREVIEW_IMPROVEMENT_ENABLED = new ABProp(22196, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp NEWSLETTER_FORWARD_COUNTER_BUMP_OWN_CHANNEL_UPDATES_FOWARDS = new ABProp(22203, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp NEWSLETTER_FORWARD_COUNTER_BUMP_FORWARDS_TO_SELF = new ABProp(22204, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp NEWSLETTER_FORWARD_COUNTER_BUMP_SECOND_ORDER_FORWARDS = new ABProp(22205, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp NEWSLETTER_FORWARD_COUNTER_MAX_SEND_AFTER_RANDOM_TIME = new ABProp(22206, "3600", "60");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GROUP_HISTORY_SETTINGS_QUERY = new ABProp(22230, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_GROUP_PARTICIPATION_ADD_TEE_ENABLED = new ABProp(22236, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WAMO_PRIVACY_TOS_LOGGING_KILLSWITCH = new ABProp(22237, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BR_PAYMENTS_HOME_DURATION_RULE_FOR_PUX_BANNER = new ABProp(22249, "604800", "604800");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp REPORTING_SEND_IS_KNOWN_CHAT = new ABProp(22256, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_INVITE_CONTACTS_TO_FOLLOW_RECEIVER_INVALID_MESSAGE_DROP_ENDABLED = new ABProp(22280, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_PDFN_NUX_AI_GROUP_NOTICE_ID = new ABProp(22298, " ", " ");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_METABOT_DOCUMENT_OCR_IMAGE_CONVERSION_ENABLED = new ABProp(22301, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_FUTUREPROOF_GALAXY_FLOW_MESSAGE_FOR_BUSINESS_NUMBERS = new ABProp(22311, "", "");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp NEWSLETTER_SECOND_ORDER_FORWARDING_LOGGING_ENABLED = new ABProp(22312, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_ADMIN_PROFILES_SENDER_ENABLED = new ABProp(22316, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_ADMIN_PROFILES_RECEIVER_ENABLED = new ABProp(22318, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GROUP_HISTORY_REPORTING = new ABProp(22329, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp IN_APP_BUG_REPORTING_DESCRIPTION_GOOD_QUALITY_CHARS = new ABProp(22361, "50", "50");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp IN_APP_BUG_REPORTING_SHOW_QUALITY_HINTS_V1 = new ABProp(22363, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp STATUS_POST_INVENTORY_LOGGING_ENABLED = new ABProp(22366, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMBA_BUSINESS_BROADCAST_GENAI_MASTER_ABPROP = new ABProp(22384, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp USERNAME_GET_MEX_QUERY = new ABProp(22411, "true", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp UTILITY_PAYMENT_REMINDER_M1_ENABLED = new ABProp(22434, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_REACTIONS_2 = new ABProp(22469, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp EMPTY_STATE_REFRESH_CHAT = new ABProp(22470, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_CONTEXTUAL_WRITING_HELP_ENABLED = new ABProp(22488, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_GROUP_PARTICIPATION_TEE_CONTEXT_WINDOW_SIZE = new ABProp(22500, "200", "200");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_IAB_FOR_ORDER_STATUS = new ABProp(22509, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DSA_26_RECEIVER_ENABLED = new ABProp(22515, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DSA_26_SENDER_ENABLED = new ABProp(22516, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MEDIA_HUB_HISTORY_MAX_DAYS = new ABProp(22518, "14", "14");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp VID_PORT_FRM_BUF_MUTEX_FIXES = new ABProp(22525, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CALL_SPEAKER_BORDER_HIDE_DELAY_MS = new ABProp(22548, "500", "500");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PIX_PAYMENT_REQUEST_RECEIVER_ENABLED = new ABProp(22552, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_CORE_BIZ_PROFILE_UX_REFRESHED_V2 = new ABProp(22561, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_SHOW_STATUS_RING_FOR_NO_UNREAD = new ABProp(22567, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp UPDATES_TAB_CHANNELS_CREATION_ENTRYPOINT_BACKTEST_ENABLED = new ABProp(22575, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_PHONE_NUMBER_GLOBAL_SEARCH = new ABProp(22603, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WIN_HYBRID_VOIP_ANR_OPTIMIZATIONS = new ABProp(22616, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_CREATE_GROUP_IN_FILTER = new ABProp(22617, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_MUSIC_VIPER_ENABLED = new ABProp(22635, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MEMBER_NAME_TAG_WEB_SENDER_ENABLED = new ABProp(22654, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MEMBER_NAME_TAG_WEB_RECEIVER_ENABLED = new ABProp(22655, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_RICH_RESPONSE_POST_CITATIONS_ENABLED = new ABProp(22672, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SETTINGS_SYNC_ENABLED = new ABProp(22692, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_CATALOG_GRAPHQL_PLACE_ORDER = new ABProp(22705, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_CATALOG_GRAPHQL_FETCH_ORDER = new ABProp(22706, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_RICH_RESPONSE_ZEITGEIST_CAROUSEL_ENABLED = new ABProp(22750, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_CONTEXTUAL_WRITING_HELP_NUM_SUGGESTIONS = new ABProp(22759, "4", "4");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_PDFN_NUX_AI_GROUP_TEE_NOTICE_ID = new ABProp(22773, " ", " ");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MM_OPTIMIZED_DELIVERY_APP_CTA_ENABLED = new ABProp(22776, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_MEDIA_IMAGE_UPLOAD_CACHE = new ABProp(22784, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp UTILITY_PAYMENT_REMINDER_M1_LOGGING_ENABLED = new ABProp(22785, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_IMAGINE_LOADING_INDICATOR_ENABLED = new ABProp(22795, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_CONTEXTUAL_WRITING_HELP_LANGUAGES_AND_TONES_CONFIG = new ABProp(22797, "{}", "{\"en\": \"auto,professional,funny,supportive\"}");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_CALLING_SCREEN_SHARING = new ABProp(22798, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_SHARE_CONTENT_UJ = new ABProp(22813, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MESSAGE_KEYS_ASYNC_CHUNK_SIZE = new ABProp(22815, "50", "50");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SYNCED_MESSAGE_KEYS_PROCESSING_TYPE = new ABProp(22825, "control", "control");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_INVITE_CONTACTS_TO_FOLLOW_BROADCAST_CLEANUP_AFTER_SEND_ENABLED = new ABProp(22868, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WDS_WEB_MENU_M1 = new ABProp(22920, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WDS_WEB_SEARCH_BAR = new ABProp(22921, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_FAVICON_BADGING_ENABLED = new ABProp(22924, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_ANR_FILE_SIZE_THRESHOLD_TO_USE_WORKER_MB = new ABProp(22930, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_ANR_MEDIA_CHUNK_ENC_DELAY_ENABLED = new ABProp(22931, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp EMPTY_UNREAD_FILTER_CTA_VARIANT = new ABProp(22962, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_VOIP_WASM_VARIANT = new ABProp(22997, "prod-nonlab", "prod-nonlab");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WAWEB_CHATINFO_REFRESH = new ABProp(23018, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_GRAPHQL_MERCHANT_INFO_SET_COMPLIANCE = new ABProp(23026, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_GRAPHQL_MERCHANT_INFO_GET_COMPLIANCE = new ABProp(23027, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BR_SMB_PAYMENTSHOME_ENABLED = new ABProp(23042, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_VOIP_LOAD_WASM_VARIANT = new ABProp(23045, "prod-nonlab", "prod-nonlab");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEBVIEW2_DISABLE_GPU_ACCELERATION_MEMORY_THRESHOLD_MB = new ABProp(23073, "-1", "-1");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMBA_BUSINESS_BROADCAST_GENAI_TEXT_LANGUAGES = new ABProp(23102, "en,es", "en,es");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_SMB_DATA_SHARING_COOLDOWN_EXP_BACKOFF_ENABLED_FOR_DISMISSED = new ABProp(23127, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_SMB_DATA_SHARING_COOLDOWN_EXP_BACKOFF_CAP = new ABProp(23128, "3", "3");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WDS_WEB_MENU_M2 = new ABProp(23153, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp INTEGRITY_PRODUCT_LEVERS_LOGGING_ENABLED = new ABProp(23163, "0", "1");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_ADMIN_PROFILES_UPDATE_ENABLED = new ABProp(23168, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_CHAT_THREADS_WEB_ENABLED = new ABProp(23169, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_ADMIN_PROFILES_FORWARDING_TO_CHATS_ENABLED = new ABProp(23170, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_ADMIN_PROFILES_FORWARDING_TO_STATUS_ENABLED = new ABProp(23171, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_ADMIN_PROFILES_LIST_ENABLED = new ABProp(23174, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_SESSION_TRANSPARENCY_META_AI_ENABLED = new ABProp(23188, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_ANR_ASYNC_MEDIA_DECRYPTION_ENABLED = new ABProp(23200, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_RICH_RESPONSE_UR_IMAGINE_ENABLED = new ABProp(23213, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AURA_ENABLED = new ABProp(23270, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AURA_APP_ICON_ENABLED = new ABProp(23271, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AURA_APP_ICON_BENEFIT_ACTIVE = new ABProp(23272, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AURA_APP_THEMES_BENEFIT_ACTIVE = new ABProp(23273, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AURA_APP_THEMES_ENABLED = new ABProp(23274, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AURA_PINNED_CHATS_ENABLED = new ABProp(23277, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AURA_PINNED_CHATS_BENEFIT_ACTIVE = new ABProp(23278, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp IMP_SEND_SIGNAL_POST_CONNECT_WEBC_ENABLED = new ABProp(23322, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp IMP_SEND_SIGNAL_POST_CONNECT_DELAY = new ABProp(23323, "500", "500");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SAGA_MESSAGE_FEEDBACK_USING_CANONICAL_ENT = new ABProp(23328, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_UNIFIED_RESPONSE_SENDER_WEB_ENABLED = new ABProp(23347, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_UNIFIED_RESPONSE_RECEIVER_WEB_ENABLED = new ABProp(23348, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_CHAT_THREADS_INFRA_SENDER_ENABLED = new ABProp(23357, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WDS_WEB_MENU_REACTION_TRAY_V2 = new ABProp(23419, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MEX_GET_PRIVACY_SETTINGS_MODE = new ABProp(23463, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp COEX_CALLING_PERMISSIONS_3P_ENABLED = new ABProp(23464, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_FIX_DUPLICATED_LIDS_HISTORY_SYNC_REFRESH = new ABProp(23474, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp NEWSLETTER_CHANNEL_UPDATE_PINNING_ENABLED = new ABProp(23476, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WDS_WEB_TOAST = new ABProp(23486, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEBTP_USE_PDF_EDITOR = new ABProp(23498, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WAMO_PRIVACY_TOS_T1_DA_ROW_UNLINKED_NOTICE_ID = new ABProp(23521, "20610250", "20610250");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WAMO_PRIVACY_TOS_T1_DA_ROW_LINKED_NOTICE_ID = new ABProp(23522, "20610251", "20610251");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WAMO_PRIVACY_TOS_T1_DA_KILLSWITCH = new ABProp(23526, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_AI_GROUP_ENABLE = new ABProp(23529, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_AI_GROUP_OPEN_SUPPORT = new ABProp(23530, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_USE_CONTACT_LID_FOR_PN_CHATS = new ABProp(23557, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BOOKING_CONFIRMATION_ENABLED_WA_WEB = new ABProp(23559, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_HYBRID_APPLY_LATEST_DB_SCHEMA_OPTIMIZATION_ENABLED = new ABProp(23595, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNEL_VIEWS_VPV_DEFINITION_ENABLED = new ABProp(23616, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp IAB_SIGNAL_COLLECTIONS_ENABLE = new ABProp(23619, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MEX_UPDATE_PRIVACY_SETTINGS_ENABLED = new ABProp(23639, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_NATIVE_ADS_CREATION_WEB_ENABLED_NO_EXPOSURE = new ABProp(23655, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SHARE_ALL_CONTACTS_TO_WINDOWS = new ABProp(23675, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AIGC_VERSION = new ABProp(23692, "1", "1");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_CHAT_THREADS_WEB_MSGS_LOAD_LIMIT = new ABProp(23694, "50", "50");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_CHATPSA_FORWARDING = new ABProp(23695, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_GROUP_MULTI_MODAL_ENABLED = new ABProp(23723, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_WEB_ASK_META_AI_ENABLED = new ABProp(23725, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_BROADCAST_HOLDOUT_PLACEHOLDER = new ABProp(23740, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNEL_ENFORCEMENT_POLICY_EDUCATION_ENABLED = new ABProp(23745, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp META_AI_CONTACT_SEARCH = new ABProp(23768, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp META_AI_FORWARD = new ABProp(23770, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WAMO_PRIVACY_TOS_T1_DA_BANNER_ROW_LINKED_NOTICE_ID = new ABProp(23779, "20610253", "20610253");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WAMO_PRIVACY_TOS_T1_DA_BANNER_ROW_UNLINKED_NOTICE_ID = new ABProp(23780, "20610252", "20610252");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_VOIP_DYNAMIC_THREAD_PREALLOCATE_COUNT = new ABProp(23789, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp USERNAME_CHANNELS_PN_PRIVACY_ENABLED = new ABProp(23795, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp TOP_LEVEL_MESSAGE_SECRET_CHECK = new ABProp(23796, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GROUP_STATUS_CHAT_REORDER_ENABLED = new ABProp(23799, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_ALBUM_RECEIVER_ENABLED = new ABProp(23809, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_DEVICE_ID_TEST_ENABLED = new ABProp(23810, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp USERNAME_ENABLED_ON_COMPANION = new ABProp(23817, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_RICH_RESPONSE_INLINE_LINKS_ENABLED = new ABProp(23819, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DISABLE_LIBAOM_REGISTRATION = new ABProp(23836, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SCHEDULED_MESSAGES_SENDER_ENABLED = new ABProp(23845, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMBI_PREMIUM_BROADCAST_MAX_RECIPIENT_LIMIT = new ABProp(23857, "256", "500");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_ALBUM_SENDER_ENABLED = new ABProp(23859, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MEX_GET_PRIVACY_CONTACT_LIST_ENABLED = new ABProp(23874, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BIZ_AI_CONSUMER_TOS_UPDATE_WEB = new ABProp(23880, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_MODE_SELECTOR_ENABLED = new ABProp(23885, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WAMO_MIN_NEXT_FETCH_TIME_SECONDS = new ABProp(23892, "3600", "3600");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WAMO_MAX_NEXT_FETCH_TIME_SECONDS = new ABProp(23893, "3600", "3600");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp COEX_CALLING_ENABLED_BUSINESS = new ABProp(23933, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PIX_ONBOARDING_NEW_CONTENT_ENABLED = new ABProp(23953, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_GROUP_PARTICIPATION_TEE_REQUEST_DEBOUNCING_INTERVAL = new ABProp(23965, "2", "2");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_GROUP_PARTICIPATION_TEE_GS_AUTH_ENABLED = new ABProp(23970, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BUG_REPORTING_ASYNC_ATTACHMENTS_ENABLED = new ABProp(23978, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_IN_MEMORY_MEDIA_BLOB_CACHE_SIZE_LIMIT = new ABProp(23993, "250000000", "250000000");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNEL_STATUS_CREATION = new ABProp(23994, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNEL_STATUS_CONSUMPTION = new ABProp(23995, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_QUESTIONS_SEARCH_ENABLED = new ABProp(24004, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PIX_ONBOARDING_EDUCATION_V2_ENABLED = new ABProp(24008, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_RICH_RESPONSE_UR_IMAGINE_FORWARDING_ENABLED = new ABProp(24017, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GROUP_FROM_GROUP = new ABProp(24024, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GROUP_HISTORY_ASSOCIATED_MESSAGE_SENDER_ENABLED = new ABProp(24033, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GROUP_HISTORY_ASSOCIATED_MESSAGE_RECEIVER_ENABLED = new ABProp(24034, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_INFRA_TEE_TEST_REQUEST_ENABLED = new ABProp(24042, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AURA_RINGTONES_ENABLED = new ABProp(24047, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AURA_RINGTONES_BENEFIT_ACTIVE = new ABProp(24050, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_DOWNLOAD_USAGE_EVENT = new ABProp(24074, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp THREADS_LOGGING_V2_ENABLED = new ABProp(24100, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_UNIFIED_RESPONSE_IMAGINE_RECEIVER_WEB_ENABLED = new ABProp(24109, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp LISTS_CHAT_LIST_ROW_PILL_ENABLED = new ABProp(24133, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_WEB_CALLS_TAB = new ABProp(24135, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BIZ_AI_AGENT_CONSUMER_CONSENT_IMPROVEMENT = new ABProp(24141, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BIZ_AI_AGENT_CONSUMER_CONSENT_MSG_RESEND = new ABProp(24142, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_HISTORY_SYNC_WORKER_ENABLED = new ABProp(24147, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BUG_REPORTING_USING_GRAPHQL = new ABProp(24161, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WAVOIP_ML_TRANSPORT_DOWNLOAD_VERSIONS = new ABProp(24173, "", "");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WAVOIP_ML_NADL_MODEL_DOWNLOAD_VERSIONS = new ABProp(24174, "", "");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_THREADS_NAV_HEADER_REDESIGN = new ABProp(24178, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CLEAR_COLLECTION_ASYNC_CHUNK_SIZE = new ABProp(24188, "100", "100");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CLEAR_COLLECTION_PROCESSING_TYPE = new ABProp(24189, "control", "control");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CPX_DUMMY_ABPROP_HOLDOUT_H1_2026 = new ABProp(24208, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WDS_WEB_CALENDAR = new ABProp(24213, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp FLOWS_WA_WEB_AGM_CTA = new ABProp(24215, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp FLOWS_WA_WEB_RESPONSES_DOWNLOAD = new ABProp(24216, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BIZ_AI_TYPING_INDICATOR_BANNER_IMPROVEMENT = new ABProp(24232, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AURA_PAINTED_DOOR_STICKERS_ENABLED = new ABProp(24241, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_MENTION_EVERYONE_SYNCD_SENDER = new ABProp(24244, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp HIDE_SILENT_SYSTEM_MESSAGE_ENABLED = new ABProp(24268, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SILENT_GROUP_USERNAME_ACTIVITIES_ENABLED = new ABProp(24269, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_DISPLAY_LID_CONTACTS = new ABProp(24280, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AURA_RINGTONES_PER_CHAT_ENABLED = new ABProp(24289, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_CONSUMER_NOVA_ENABLED = new ABProp(24295, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_LABEL_SYNC_CRITICAL_EVENT_LOGGING = new ABProp(24311, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_FORCE_LID_CHATS_IN_HISTORY = new ABProp(24343, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_ADMIN_PROFILES_SETTINGS_ENABLED = new ABProp(24347, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_GROUP_SEND_MENTIONED_PUSHNAME_ENABLED = new ABProp(24361, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_LOG_CAPACITY_OVERRIDE = new ABProp(24363, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_CONSUMER_ENTRY_POINT_ENABLED = new ABProp(24380, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_ATTACH_MENU_ADD_DRAWING_ENABLED = new ABProp(24384, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BR_SMB_PIX_PAYMENT_REQUEST_VARIANT = new ABProp(24388, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BIZ_AI_AGENT_CONSUMER_NUX_COOLDOWN = new ABProp(24400, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp POLL_END_TIME_ENABLED = new ABProp(24405, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_IAB_SIGNALS_FOR_MM = new ABProp(24419, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BUG_REPORTING_RID_IN_FLYTRAP = new ABProp(24421, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BUG_REPORTING_PRE_UPLOADED_ATTACHMENTS_ON_BUG_CREATION_ENABLED = new ABProp(24422, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_HORIZONTAL_LINK_PREVIEWS = new ABProp(24425, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_ENABLE_FOLLOW_UP_REPLY_ICON = new ABProp(24429, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_ANYONE_CAN_LINK_M2 = new ABProp(24432, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_PDFN_NUX_AI_GROUP_DISCOVER_NOTICE_ID = new ABProp(24437, " ", " ");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_WEB_MANUS_GROWTH_QP_BANNER_V1 = new ABProp(24469, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GROUP_USERNAME_UPDATES_AS_MEMBER_UPDATES_ENABLED = new ABProp(24477, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_CHAT_THREADS_COMPANION_VARIANT = new ABProp(24478, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_UNIFIED_RESPONSE_QPL_LOGGING = new ABProp(24484, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp IS_AI_MODE_SELECTOR_VISIBLE = new ABProp(24489, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_CONSUMER_NOVA_ENTRY_POINT_SETTINGS_ENABLED = new ABProp(24495, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_WEB_CALLING_NUX = new ABProp(24504, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp POLL_ADD_OPTION_ENABLED = new ABProp(24517, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp POLL_HIDE_VOTERS_ENABLED = new ABProp(24518, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_REDUCE_FORCED_LAYOUT_CHAT_OPEN = new ABProp(24526, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_RICH_RESPONSE_UR_IMAGINE_VIDEO_ENABLED = new ABProp(24534, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_WORKER_GROUP_PARTICIPANTS_SYNC_ENABLED = new ABProp(24535, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp UTILITY_PAYMENT_REMINDER_M2_ENABLED = new ABProp(24537, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_CHANNELS_MIGRATE_SUBSCRIBERS_TO_FOLLOWERS_ENABLED = new ABProp(24540, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_CHANNELS_COMET_VIDEO_PLAYER_ENABLED_V2 = new ABProp(24541, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_GLOBAL_SEARCH_PREFIX_BASED = new ABProp(24559, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_MULTI_PPL_TYPING_INDICATOR_FOR_CHATLIST_GROUPS_VARIANT = new ABProp(24560, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GROUP_MEMBER_UPDATES_HIDE_IN_THREAD_ENABLED = new ABProp(24584, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GROUP_MEMBER_UPDATES_USERNAMES_UI_ENABLED = new ABProp(24585, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GROUP_MEMBER_UPDATES_USERNAMES_DB_ENABLED = new ABProp(24586, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CALLING_AV_SYNC_WEBRTC = new ABProp(24599, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SCHEDULED_MESSAGES_RECEIVER_ENABLED = new ABProp(24610, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GROUP_MEMBER_UPDATES_USERNAMES_ENABLED = new ABProp(24617, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_GROUP_CALL_VERSION = new ABProp(24652, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_GROUP_CALL_ADD_IN_CALL_AHGC_ENABLED = new ABProp(24654, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_GROUP_CALL_MAX_VERSION_BY_PLATFORM = new ABProp(24655, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_GROUP_CALL_MAX_VERSION_BY_COUNTRY = new ABProp(24656, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MEDIA_KEY_DOMAIN_SEPARATION_ENABLED = new ABProp(24661, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PAYMENT_LINKS_TRUST_SIGNALS_OTHER_METATAG_KILL_SWITCH_ENABLED = new ABProp(24662, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_WEB_NATIVE_ADS_MVP_QE1_ENABLED = new ABProp(24668, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_WEB_NATIVE_ADS_MVP_QE2_ENABLED = new ABProp(24669, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_CANONICAL_ENT_WEB_REG_RECOVERY_ENABLED = new ABProp(24682, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_RICH_RESPONSE_COMPACT_CITATIONS_ENABLED_CODE = new ABProp(24721, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp LISTS_SMB_WEB_ENABLED = new ABProp(24732, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp RT_GHS_SENDER_ENABLED = new ABProp(24741, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp RT_GHS_RECEIVER_ENABLED = new ABProp(24742, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BIZ_AI_CONSUMER_TOS_NOTICE_IQ_WEB = new ABProp(24754, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_USER_JOURNEY_THREAD_VISIT_COUNT = new ABProp(24757, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_WEB_NATIVE_ADS_MVP_QE1_ENABLED_NO_EXPOSURE = new ABProp(24761, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp REMINDERS_CHECK_NOTIFICATION_PERMISSION = new ABProp(24769, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_CONTACT_SEARCH_TOKENIZED_ENABLED = new ABProp(24773, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_WINDOWS_XDR_CHAT_HANDOFF = new ABProp(24783, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_STATUS_COMET_VIDEO_PLAYER_ENABLED = new ABProp(24791, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AURA_STICKERS_ENABLED = new ABProp(24800, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AURA_STICKERS_BENEFIT_ACTIVE = new ABProp(24801, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_WEB_CALLING_BETA_UPSELL = new ABProp(24812, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_REQUEST_MISSING_KEYS_FOR_REMOVES = new ABProp(24838, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_MENTION_EVERYONE_RECEIVER_WEB = new ABProp(24843, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_MENTION_EVERYONE_SENDER_WEB = new ABProp(24844, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BUG_REPORTING_ABPROPS_UPLOADED_ON_SUBMISSOIN = new ABProp(24850, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENHANCED_MENTION_SUGGESTIONS_NON_GROUP_MEMBERS_ENABLED = new ABProp(24852, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CCI_COMPLIANCE_MM = new ABProp(24853, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_WEB_VOIP_DEDICATED_WORKER = new ABProp(24868, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_BULK_ADD_CONTACTS_ENABLED = new ABProp(24875, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WINDOWS_CONTACTS_SYNC_INTERVAL = new ABProp(24882, "60", "5");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WINDOWS_CONTACTS_INITIAL_SYNC_DELAY = new ABProp(24883, "10", "1");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp POLL_END_TIME_RECEIVING_ENABLED = new ABProp(24884, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp POLL_HIDE_VOTERS_RECEIVING_ENABLED = new ABProp(24885, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp POLL_CREATOR_EDIT_RECEIVING_VERSION = new ABProp(24886, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp POLL_CREATOR_EDIT_ENABLED = new ABProp(24887, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_3PD_MESSAGE_ANALYTICS = new ABProp(24895, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_VIDEO_COMET_VIDEO_PLAYER_ENABLED = new ABProp(24905, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_NCT_TOKEN_SALT_CREATION_ENABLED = new ABProp(24915, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_WORKER_ADV_PROCESSING_ENABLED = new ABProp(24924, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_CANONICAL_ENT_WEB_REG_ENABLED = new ABProp(24925, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_EXPIRED_MEDIA_THUMBNAIL_ENABLED = new ABProp(24926, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_NCT_TOKEN_SEND_ENABLED = new ABProp(24941, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_ME_TAB = new ABProp(24944, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_SELF_PROFILE_PHOTO_FIX_ENABLED = new ABProp(24945, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_CONSUMER_NOVA_SETTINGS_GREEN_DOT_ENABLED = new ABProp(24955, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DEFENSE_MODE_QUARANTINE = new ABProp(24959, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_CHAT_THREADS_SEARCH_ENABLED = new ABProp(24963, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CCI_COMPLIANCE_CTWA = new ABProp(24983, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_3PD_OPT_OUT_COUNTER_OPTIMIZATION_ENABLED = new ABProp(24984, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_WAITING_ROOM_LOGGING = new ABProp(24991, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_IAB_PAYMENT_REMINDERS = new ABProp(25004, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_REDUCE_CASCADING_UPDATES_CHAT_OPEN = new ABProp(25006, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_ANYONE_CAN_LINK_M2_FLOOD_LIMIT = new ABProp(25009, "10", "10");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_STATUS_FIRST_UPLOAD_FIX_ENABLED = new ABProp(25015, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MM_DISCLOSURE_LEARN_MORE_ARTICLE_ID = new ABProp(25021, "263784176043634", "263784176043634");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp STATUS_INFRA_1_1_SESSION_SPLIT = new ABProp(25034, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_COMET_VIDEO_PLAYER_SNAPL = new ABProp(25065, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp IM_BLOKS_WIDGET_ENABLE = new ABProp(25071, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_T_ENABLED = new ABProp(25078, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_ENABLE_STATUS_HQ_THUMBNAIL = new ABProp(25079, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_FILE_UPLOAD_SUPPORTED_FILE_TYPES = new ABProp(25090, "", "");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_FILE_UPLOAD_COUNT_LIMIT = new ABProp(25093, "0", "1");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_CORE_BIZ_PROFILE_EDIT_ADDRESS = new ABProp(25118, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_BOT_INTEGRATION_ENABLED = new ABProp(25119, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PUSH_NAME_IN_GLOBAL_SEARCH_NON_CONTACTS_ENABLED_FOR_BACKTEST = new ABProp(25123, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WDS_WEB_EXPRESSIONS_PANEL = new ABProp(25144, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_LOGGING_QBM_INCOMING_MESSAGE = new ABProp(25149, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_STATUS_VIEWER_SIDE_POSTER_IDENTIFIERS_ENABLED = new ABProp(25151, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_WEB_VOIP_PLATFORM_AV_SYNC = new ABProp(25177, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_SMB_LABEL_CHAT_HEADER_ENABLED_WEB = new ABProp(25180, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_NCT_TOKEN_HISTORY_SYNC_ENABLED = new ABProp(25189, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_WEB_VOIP_SCTP_WORKER = new ABProp(25202, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_BUSINESS_BROADCAST_MULTI_AUDIENCE_SEND_WEB = new ABProp(25206, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AURA_STICKERS_OVERLAY_ANIMATION_ENABLED = new ABProp(25210, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_NCT_TOKEN_SYNCD_ENABLED = new ABProp(25253, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_ARCHIVE_ENABLED = new ABProp(25254, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ACP_REMOVAL = new ABProp(25255, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_BOT_INTEGRATION_BOT_PROFILE = new ABProp(25268, "", "");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_BOT_INTEGRATION_HISTORY_SYNC_ENABLED = new ABProp(25269, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_DYNAMIC_MODE_SELECTOR_ENABLED = new ABProp(25287, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_GROUP_INFO_NOTIFICATION_ROW = new ABProp(25292, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MAY_HAVE_MESSAGES_ENABLED = new ABProp(25303, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_WAM_FALCO_MODE = new ABProp(25306, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_WAM_FALCO_SHADOW_EVENT_IDS = new ABProp(25309, "", "");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_SEARCH_EMPTY_STATE_M1 = new ABProp(25310, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_UR_IMAGINE_VIDEO_ENABLED = new ABProp(25329, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_IMAGINE_UR_ENABLED = new ABProp(25331, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_UR_BLOKS_ENABLED = new ABProp(25332, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp EXTENDED_PRODUCT_LEVERS_FUNNEL_LOGGING = new ABProp(25350, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WDS_WEB_SUBMENUS = new ABProp(25351, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp USERNAME_EXPOSED_LOGGING_ENABLED = new ABProp(25353, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CCI_COMPLIANCE_CTWA_LEARN_MORE_HYPERLINK = new ABProp(25366, "https://faq.whatsapp.com/785493319976156/", "https://faq.whatsapp.com/785493319976156/");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_CONSUMER_NOVA_ELIGIBILITY_SUBSCRIPTION_STATUS_CHECK_ENABLED = new ABProp(25388, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_WEB_VOIP_DYNAMIC_FPS_THROTTLE = new ABProp(25394, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GOOGLE_MAPS_API_KEY_AUTH = new ABProp(25407, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_HIGHLIGHT_ME_MENTION = new ABProp(25408, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_ANR_BATCH_AND_QUEUE_BULK_CONTACTS_DB_WRITES_ENABLED = new ABProp(25413, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_GROUP_EXPERIMENTATION_ENABLE = new ABProp(25414, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SUPPORT_MESSAGE_REPORT_DESCRIPTION_ENABLED = new ABProp(25419, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MM_DATA_SHARING_DISCLOSURE_ENABLED_ADDITIONAL_TRANSPARENCY_LARGE_SCREENS = new ABProp(25421, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_DEFAULT_PROFILE_PICS = new ABProp(25455, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BIZ_VPV_IMPRESSION_LOGGING_ENABLED = new ABProp(25465, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_BOT_INTEGRATION_HISTORY_SYNC_PRE_CHATD_ENABLED = new ABProp(25469, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_CHAT_THREADS_PIN_ENABLED = new ABProp(25517, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_CHAT_THREADS_PIN_MAX_COUNT = new ABProp(25520, "3", "3");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_FILE_UPLOAD_SIZE_LIMIT_MB = new ABProp(25524, "40", "40");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_NOTIFY_FOR = new ABProp(25544, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CLEANUP_REDUNDANT_QUOTED_MEDIA_OVER_WIRE_ENABLED = new ABProp(25572, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_3PD_OPT_OUT_CONFIRMATION_ENABLED = new ABProp(25577, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BR_PAYMENTS_PAYMENT_REQUEST_CTA = new ABProp(25599, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_CHAT_SEARCH_ENTRYPOINT = new ABProp(25609, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_WEB_VOIP_P2P = new ABProp(25621, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_T_ONLY_ENABLED = new ABProp(25636, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp STICKER_STORE_TESTING_ENABLED = new ABProp(25639, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_MEDIA_COMPUTE_IN_WORKER_ENABLED = new ABProp(25641, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AFTER_READ_SENDING_ENABLED = new ABProp(25648, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AFTER_READ_RECEIVER_ENABLED = new ABProp(25649, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_T_FOLDERED_ENABLED = new ABProp(25650, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_BASE_VIDEO_COMET_VIDEO_PLAYER_ENABLED = new ABProp(25660, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_GROUP_DISCARD_DIALOG_CONTACT_THRESHOLD = new ABProp(25682, "-1", "2");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PARSE_DECISION_ID_AND_SOURCE_FOR_NON_HSM_ENABLED = new ABProp(25737, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp POLL_ADD_OPTION_RECEIVING_ENABLED = new ABProp(25758, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp USERNAME_KEY_UPSELL_MAX_NUMBERS = new ABProp(25789, "1", "1");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp USERNAME_KEY_UPSELL_MAX_CHARACTERS = new ABProp(25790, "8", "8");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp USERNAME_KEY_CONDITIONAL_UPSELL = new ABProp(25791, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_DYNAMIC_MODE_SELECTOR_TTL_SECONDS = new ABProp(25797, "86400", "86400");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp HSM_TAG_IN_HISTORY_SYNC_DESERIALIZATION_ENABLED = new ABProp(25804, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_LISTS_FULL_WIDTH_FILTERS = new ABProp(25805, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_GROUPS_IN_COMMON_MULTI_CONTACT = new ABProp(25808, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_DATE_MARKER_CALENDAR_ENABLED = new ABProp(25811, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CONTACTS_FROM_COMMON_GROUPS_SECTION_ENABLED = new ABProp(25817, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNEL_US_NCII_REPORTING_ENABLED = new ABProp(25818, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_COPY_LINK_URL_ENABLED = new ABProp(25820, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_CALENDAR_MESSAGE_DENSITY_ENABLED = new ABProp(25823, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_HIGHLIGHT_ME_MENTION_GROUPSIZE_THRESHOLD = new ABProp(25836, "130", "130");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_BIZ_BROADCASTS_DOCUMENT_ATTACHMENT = new ABProp(25840, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_BIZ_PROFILE_GRAPHQL_MIGRATION = new ABProp(25846, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_RICH_RESPONSE_JSON_PATCH_STREAMING_ENABLED = new ABProp(25871, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_PDFN_TOS_AI_PRIVACY_NOTICE_ID = new ABProp(25896, "", "");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_WEB_VOIP_VIDEO_RESOLUTION_CAP = new ABProp(25899, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GROUP_HISTORY_BUNDLE_TIME_LIMIT_RECEIVER_ENFORCEMENT_SECS = new ABProp(25910, "1209600", "1209600");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AURA_LOGGING_ENABLED = new ABProp(25914, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_ANR_NOOP_GC_ENABLED = new ABProp(25915, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp RESURRECTED_UPSELL = new ABProp(25916, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_JOIN_GROUP_CONTEXT = new ABProp(25925, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_SUBSCRIPTION_ENABLED = new ABProp(25927, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_UNIFIED_RESPONSE_RECEIVER_WEB_ENABLED_V2 = new ABProp(25929, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_UNIFIED_RESPONSE_RECEIVER_WEB_TIMESTAMP_V2 = new ABProp(25930, "1772082000", "1772082000");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_PRELOAD_CONVERSATION_CHAT_OPEN = new ABProp(25937, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENHANCED_MENTION_LIMIT = new ABProp(25951, "5", "5");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_REACTION_INACTIVE_RECEIPT = new ABProp(25954, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_VOIP_LONG_TASK_LOGS_SAMPLING_RATE = new ABProp(25982, "0.10000000149011612", "0.10000000149011612");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ACP_REMOVAL_EPOCH_TIME = new ABProp(25993, "1782518400", "1782518400");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SUPPORT_CONTACT_FORM_USING_GRAPHQL = new ABProp(26001, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_WEB_VOIP_PROXY_AND_SCTP_WORKERS = new ABProp(26012, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp TRANSCODE_AND_REPAIR_VIDEOS = new ABProp(26027, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BR_PIX_KEY_BUBBLE_CONTENT_UPDATE = new ABProp(26033, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GROUP_HISTORY_OUT_OF_WINDOW_PIN_SENDER = new ABProp(26037, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GROUP_HISTORY_OUT_OF_WINDOW_PINS_RECEIVER = new ABProp(26039, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp USERNAME_CHANGE_NOTIFICATION_PROCESS_DEBOUNCE_MS = new ABProp(26059, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp TAPPABLE_LINKS_IN_POLL_OPTION_ENABLED = new ABProp(26062, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_WEBCODEC_VIDEO_ENCODE = new ABProp(26079, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AURA_SUBSCRIPTION_SIMULATION_ENABLED = new ABProp(26086, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_REACTIONS_MOTION_V2_ENABLED = new ABProp(26102, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp IMPROVE_GROUP_REPORTING = new ABProp(26114, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_FOLLOWER_INVITE_CREATION_MODAL_ENABLED = new ABProp(26120, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_WORKER_PREKEY_PROCESSING_ENABLED = new ABProp(26133, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WAWEB_CROSSPOSTING_ATTRIBUTIONS = new ABProp(26138, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_FORWARD_COUNTER_ON_STATUS_CARD_ENABLED = new ABProp(26148, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_WEB_CUSTOMER_MANAGEMENT_ENABLED = new ABProp(26165, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_PDFN_NUX_AI_GROUP_TEE_DISCOVER_NOTICE_ID = new ABProp(26171, "20260212", "20260212");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GC_DEVICE_SWITCHING_KILLSWITCH = new ABProp(26182, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_HATCH_INTEGRATION_ENABLED = new ABProp(26189, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_HATCH_INTEGRATION_BOT_PROFILE = new ABProp(26190, "", "");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp IS_INDIVIDUAL_SUSPICIOUS_FMX_ENABLED = new ABProp(26191, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_THREAD_LOADING_INFRA_ENABLED = new ABProp(26192, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_WAM_FALCO_LOGGING_ENABLED = new ABProp(26200, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_EXPORT_CHAT = new ABProp(26201, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SUGGESTED_AUDIENCES_WA_WEB = new ABProp(26207, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_PNLESS_STANZAS = new ABProp(26211, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp NEWSLETTER_STATUS_RECEIVER_ENABLED = new ABProp(26217, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DM_RECEIVER_AFTER_READ_ALLOW_VALUES = new ABProp(26218, "{\"timers\": [0, 900]}", "{\"timers\": [0, 900]}");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp USERNAME_KEY_UPSELL_MODE = new ABProp(26220, "0", "1");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AFTER_READ_FALLBACK_DURATION = new ABProp(26225, "86400", "86400");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_ASTERIA_ENABLED = new ABProp(26234, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_CALLING_ENABLE_ON_WINDOWS = new ABProp(26259, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GROUP_SUSPENSION_APPEALS_REDESIGN_ENABLED = new ABProp(26276, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEBTP_EDIT_PDF_IN_WHATSAPP_ENABLED = new ABProp(26279, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MM_1PD_POST_DC_NEW_SCHEMA_ENABLED = new ABProp(26280, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MM_1PD_POST_DC_DEPTH_LIMIT = new ABProp(26281, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MM_1PD_POST_DC_OLD_SCHEMA_DISABLED = new ABProp(26282, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_CROSSPOST_SETTINGS_SYNC = new ABProp(26296, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_BLOCK_IB_AR_FOR_WABAI = new ABProp(26302, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BUG_REPORTING_ATTACH_VIEW_DUMP_PRE_BUG_CREATION = new ABProp(26307, "true", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BUG_REPORTING_ATTACH_PATHFINDER_PRE_BUG_CREATION = new ABProp(26311, "true", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_QP_CONVERSION_TRACKING_INFRA = new ABProp(26331, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CALLING_RUST_MIGRATION_INCOMING_STANZA_ENABLED = new ABProp(26338, "", "");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AURA_STATUS_SEARCH_ENABLED = new ABProp(26346, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SCHEDULED_MESSAGES_WINDOW_DURATION_MAX_SECONDS = new ABProp(26347, "1209600", "1209600");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SCHEDULED_MESSAGES_WINDOW_DURATION_MIN_SECONDS = new ABProp(26348, "600", "600");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_ATTACH_ICON_VARIANT = new ABProp(26386, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp INAPP_SIGNUP_CONFIRMATION_MESSAGE_ENABLED = new ABProp(26390, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_PHOTO_POLLS_GENAI_ENABLED = new ABProp(26392, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_ASTERIA_ELIGIBILITY_SUBSCRIPTION_STATUS_CHECK_ENABLED = new ABProp(26399, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CALLING_E2E_KEYGEN_VIA_SELF_LID = new ABProp(26411, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BUSINESS_BROADCAST_CAMPAIGN_SYNCD_ENABLED = new ABProp(26426, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_OFFER_V2_UPGRADE = new ABProp(26435, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_CORE_BIZ_PROFILE_PREVIEW = new ABProp(26441, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_HATCH_INTEGRATION_HISTORY_SYNC_PRE_CHATD_ENABLED = new ABProp(26445, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GROUP_HISTORY_SEND_AFTER_JOIN = new ABProp(26451, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp STICKERS_EMOJI_TAGGING_ENABLED = new ABProp(26465, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_AGM_SIGNUP_ENABLED = new ABProp(26467, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_STATUS_LIKES_SEND_V2_ENABLED = new ABProp(26470, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_FEATURE_PARITY_SMALL_WINS = new ABProp(26481, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AUTH_AGENTS_CONSUMER_EXP_ENABLED = new ABProp(26492, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_HATCH_INTEGRATION_HISTORY_SYNC_ENABLED = new ABProp(26517, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_LEAD_TAXONOMY = new ABProp(26531, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AURA_STATUS_SEARCH_MAX_VIEWERS = new ABProp(26545, "1000", "1000");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AURA_STATUS_SEARCH_TIMEOUT_THRESHOLD = new ABProp(26546, "5", "5");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp IS_EXPAND_FMX_ACCOUNT_AGE_UI_ENABLED = new ABProp(26548, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp IS_EXPAND_FMX_ACCOUNT_AGE_BOLDED_NON_AUTO_EXPOSE = new ABProp(26549, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp IS_EXPAND_FMX_MEX_ENABLED = new ABProp(26550, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp IS_EXPAND_FMX_ENABLED_NON_AUTO_EXPOSE = new ABProp(26551, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_PRE_CHAT_DEVICE_ID_TEST = new ABProp(26553, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_DOWNLOAD_MIMETYPE_CHECK_BLOCK_ENABLED = new ABProp(26555, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AURA_STICKERS_PREVIEW_MAX_ANIMATION_COUNT = new ABProp(26602, "5", "5");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_SHOW_HD_PHOTO = new ABProp(26610, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_BIZAI_2WAY_INTEGRATION_ENABLED = new ABProp(26613, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_BIZAI_2WAY_INTEGRATION_HISTORY_SYNC_PRE_CHATD_ENABLED = new ABProp(26614, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_CHAT_THEMES = new ABProp(26629, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WAWEB_STATUS_CLOSE_FRIENDS_VIEWER_SIDE_ENABLED = new ABProp(26659, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp NEWSLETTER_STATUS_CREATION_ENABLED = new ABProp(26669, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BIZ_AI_RESPONDING_LIST_ENABLED = new ABProp(26670, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp INTERACTIVE_BLOKS_WIDGET_WEB_ENABLED = new ABProp(26685, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_SMB_MULTISELECT_ENABLED = new ABProp(26719, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_CONTACT_AND_CHAT_FUZZY_SEARCH_ENABLED = new ABProp(26728, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_CONTACT_AND_CHAT_FUZZY_SEARCH_SIMILARITY_OPTIMIZATION_ENABLED = new ABProp(26729, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_CONTACT_AND_CHAT_FUZZY_SEARCH_DISTANCE_THRESHOLD = new ABProp(26731, "0.30000001192092896", "0.30000001192092896");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_CONTACT_AND_CHAT_FUZZY_SEARCH_TIMEOUT_THRESHOLD = new ABProp(26733, "5", "5");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp VOIP_ENABLE_WEBRTC_STATS_POLLING = new ABProp(26744, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_LEARNING_CLEAR_CHAT_DISABLE_EMPTY_CHATS = new ABProp(26745, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PARSE_ENCRYPTED_DSM_MSG_FIX = new ABProp(26772, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WDS_WEB_COMPOSER_TOOLBAR_V2 = new ABProp(26773, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_CHAT_THREADS_INFRA_WEB_ENABLED = new ABProp(26776, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MM_SIGNAL_SHARING_VERIFICATION_NEW_SIGNAL_TYPE_ORIGIN = new ABProp(26784, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp LARGE_SCREENS_NEW_CHAT_BUTTON_VARIANTS = new ABProp(26788, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_CHAT_THREADS_WEB_KILLSWITCH_ENABLED = new ABProp(26806, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_DISCUSS_PRIVATELY = new ABProp(26815, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_WEB_VOIP_VIRTUAL_VIDEO_CAPTURE_DRIVER = new ABProp(26817, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PRIVACY_SCREEN_ENABLED = new ABProp(26820, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp FEATURE_KEY_STORE_INFRA_ENABLED = new ABProp(26829, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_WEB_VOIP_VIRTUAL_AUDIO_CAPTURE_DRIVER = new ABProp(26838, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PAYMENTS_BR_P2P_PIX_COPY_KEY_BUYER_LOGGING = new ABProp(26847, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_MENU_SHARE_GROUP = new ABProp(26850, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CALLING_RUST_MIGRATION_INCOMING_STANZA_BITMAP = new ABProp(26876, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp REMOVE_PN_DEPENDENCIES = new ABProp(26888, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_ADD_CONTACT = new ABProp(26892, "", "");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_BB_WEB_AUDIENCE_EXPRESSION_SYNC_READ = new ABProp(26894, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WAVOIP_ENABLE_ML_NAMESPACE_V2 = new ABProp(26947, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp INTEGRITY_CHECKPOINTS_ENABLED = new ABProp(26961, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp KS_USE_COMPONENT_MODEL = new ABProp(26966, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_WDS_CALLING_DROPDOWN = new ABProp(26974, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_ASTERIA_ROLLOUT_ENABLED = new ABProp(26996, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PIX_PAYMENT_REQUEST_UPDATE_STATUS_ENABLED = new ABProp(27006, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PAYMENTS_BR_P2M_ORDER_DETAILS_BUYER_LOGGING = new ABProp(27008, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PAYMENTS_BR_P2M_PIX_COPY_KEY_BUYER_LOGGING = new ABProp(27026, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PAYMENTS_BR_PAYMENT_LINKS_BUYER_LOGGING = new ABProp(27027, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PAYMENTS_BR_P2M_PIX_COPY_CODE_BUYER_LOGGING = new ABProp(27028, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PAYMENTS_BR_P2M_PIX_IN_GROUPS_BUYER_LOGGING = new ABProp(27029, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp STATUS_LIKES_FIFA_LOTTIE_FULL_SCREEN_ANIMATION_ENABLED = new ABProp(27054, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_CONSUMER_NOVA_SUBSCRIPTION_NOTIFICATIONS_ENABLED = new ABProp(27068, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_ENABLE_SYNCD_KEY_PERSISTENCE_ONLY_AFTER_SERVER_ACK = new ABProp(27069, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_PAYMENT_REQUEST_STATUS_UPDATE = new ABProp(27077, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BUSINESS_BROADCAST_INSIGHTS_SYNC_PAST_X_DAYS = new ABProp(27082, "30", "30");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_MAIBA_WASS_MIGRATION_RECEIVING = new ABProp(27083, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_MAIBA_WASS_MIGRATION_SENDING = new ABProp(27084, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PAYMENTS_BR_P2M_PAY_NOW_BUYER_LOGGING = new ABProp(27092, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PAYMENTS_BR_P2M_VIEW_ORDER_BUYER_LOGGING = new ABProp(27093, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PAYMENTS_BR_P2M_PIX_MORE_WAYS_TO_PAY_BUYER_LOGGING = new ABProp(27094, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PAYMENTS_BR_P2M_COMPLETED_PAYMENT_INTENT_BUYER_LOGGING = new ABProp(27095, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PAYMENTS_BR_P2M_COPY_BOLETO_CODE_BUYER_LOGGING = new ABProp(27096, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WAVOIP_ML_BWE_QUICKHD_MODEL_DOWNLOAD_VERSIONS = new ABProp(27109, "", "");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PAYMENTS_BR_P2P_PIX_COPY_CODE_BUYER_LOGGING = new ABProp(27114, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_QP_EMERGENCY_FORCE_FETCH_NONCE = new ABProp(27115, "", "");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_ASTERIA_META_AI_SETTINGS_TAB_ENTRYPOINT_ENABLED = new ABProp(27118, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_CHANGE_LIST_WDS_SUBMENU = new ABProp(27123, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MD_SYNCD_MUTATION_LOGGING = new ABProp(27124, "{\"allowlist\": []}", "{\"allowlist\": []}");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MD_SYNCD_MUTATION_SUMMARY_LOGGING = new ABProp(27125, "{\"allowlist\": []}", "{\"allowlist\": []}");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MD_SYNCD_BUNDLE_LOGGING = new ABProp(27126, "{\"allowlist\": []}", "{\"allowlist\": []}");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AURA_PINNED_CHATS_TARGETED_NUX_FORCE = new ABProp(27135, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEBTP_THUMBNAIL_RENDERER_TIMEOUT_MS = new ABProp(27148, "3000", "3000");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_FORWARD_TO_SMALL_GROUPS = new ABProp(27157, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_PAYMENTS_SMB_LABELS_CONVENTION_ENABLED = new ABProp(27172, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_PAYMENTS_SMB_ENABLED = new ABProp(27173, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp DISABLE_RAISE_HAND_1ON1 = new ABProp(27177, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_CHAT_THREADS_FUZZY_SEARCH_ENABLED = new ABProp(27199, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AURA_SETTINGS_ROW_ENABLED = new ABProp(27210, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BUSINESS_BROADCAST_INSIGHTS_CAMPAIGN_TTL_DAYS = new ABProp(27218, "30", "30");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ACS_USE_GRAPHQL_ISSUANCE = new ABProp(27219, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WMI_WORKER_SCHEDULER_WEB = new ABProp(27237, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WAWEB_ENABLE_LEGACY_IMAGE_ZOOM = new ABProp(27239, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_STATUS_CONSUMPTION_ENTRYPOINTS = new ABProp(27240, "0", "3");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_ANR_ASYNC_MSG_SEND_HANDLER = new ABProp(27249, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WDS_WEB_RICH_TEXT_FIELD = new ABProp(27264, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_WEB_VOIP_ANR_OPTIMIZATIONS = new ABProp(27268, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_TEST_ABPROP_DELETE_ME = new ABProp(27274, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp OPUS_TIME = new ABProp(27277, "1784516400", "1784516400");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp OPUS_ENABLED = new ABProp(27278, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BR_PAYMENTS_PAYMENT_DETECTION_ENHANCEMENT = new ABProp(27309, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_CHAT_THREADS_HISTORY_ICON_VARIANT = new ABProp(27316, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_STARRED_MSGS_SEARCH = new ABProp(27353, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_RICH_RESPONSE_UNKNOWN_SENDER_PREVIEW_ENABLED = new ABProp(27355, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_HATCH_INTEGRATION_TAB_ENABLED = new ABProp(27356, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp USERNAME_1ON1_SYS_MSG_CREATION_UPSELL_ENABLED = new ABProp(27359, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_COMPOSER_HEIGHT_INCREASE_ENABLED = new ABProp(27441, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_CONTEXT_CARD_INVITE_FOLLOWERS_ENABLED = new ABProp(27449, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_MSG_INFRA_REMOVE_DEVICES_ON_406_ERROR_ENABLED = new ABProp(27463, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_HATCH_VIDEO_UPLOAD_ENABLED = new ABProp(27470, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CALL_INFO_OPTIMIZATIONS_VERSION = new ABProp(27483, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_BUSINESS_BROADCAST_SEND_WEB_SMBA = new ABProp(27486, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_HQ_IMAGE_THUMBNAIL_IN_CHAT_SCANS = new ABProp(27512, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_AE_MODEL_META_DATA_ENABLED = new ABProp(27515, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_AE_MODEL_META_DATA_SIGNAL_ENABLED = new ABProp(27516, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_CHAT_THREADS_IMPLICIT_ROUTING_STRATEGY = new ABProp(27519, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEBTP_PRELOAD_THUMBNAIL_RENDERER_NO_EXPOSURE = new ABProp(27534, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEBTP_THUMBNAIL_RENDERER_MODE = new ABProp(27535, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_CORE_REC_CARD = new ABProp(27568, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_WEBRTC_VIDEO_JB = new ABProp(27591, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_IMPORTANT_MSG_NOTIFICATION = new ABProp(27614, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_EDIT_BEFORE_FORWARDING_TO_STATUS = new ABProp(27616, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp STATUS_E2EE_SEND_OVER_STATUS_STANZA = new ABProp(27620, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp STATUS_E2EE_RECV_OVER_STATUS_STANZA = new ABProp(27622, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_PATHFINDER_LOGGING = new ABProp(27628, "0", "3");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_RICH_RESPONSE_UNKNOWN_SENDER_VERIFICATION_MASKING_ENABLED = new ABProp(27635, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_3PD_AGGREGATED_CONVERSION_ANALYTICS_ENABLED = new ABProp(27641, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_COPY_PASTE_P2P = new ABProp(27642, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_ORDER_DETAILS_FOR_PAYMENT_KEY = new ABProp(27643, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_HATCH_COMMANDS_ENABLED = new ABProp(27660, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp EXPAND_FMX_MEX_SHOULD_USE_FMX_USE_CASE = new ABProp(27662, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp INTEGRITY_CHECKPOINTS_DEFAULT_ENABLED = new ABProp(27663, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_DRAWER_DESCRIPTOR_ENABLED = new ABProp(27677, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_VOIP_SCTP_WORKER_SAFARI_EXP = new ABProp(27695, "1", "1");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_SCROLLABLE_REACTION_TRAY_ENABLED = new ABProp(27709, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_FREQUENT_REACTIONS_STORE_ENABLED = new ABProp(27710, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_FREQUENT_REACTIONS_WEIGHT_REDUCER = new ABProp(27711, "90", "90");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_FREQUENT_REACTIONS_REACTS_AGO_THRESHOLD = new ABProp(27712, "10", "10");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_ENABLE_MENTION_MESSAGE = new ABProp(27714, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_FOCUS_MANAGEMENT_FOR_STATUS_AUDIENCE = new ABProp(27719, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ANIMATED_SOCCER_BALL_TEST_ENABLED = new ABProp(27750, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ANIMATED_SOCCER_BALL_PROD_ENABLED = new ABProp(27751, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_MEDIA_WORKER_SPLIT_ENABLED = new ABProp(27753, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WAVOIP_ML_MEDIA_UVQ_MODEL_DOWNLOAD_VERSIONS = new ABProp(27756, "", "");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_LOADER_BUTTON_UIX_IMPROVEMENT = new ABProp(27768, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_ANR_ASYNC_CONTACTS_RESTORE_FROM_DB_ENABLED = new ABProp(27775, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_MEDIA_UPLOAD_RETRY_RETRIES_COUNT = new ABProp(27782, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp REMOVE_DEVICE_PN_DEPENDENCIES = new ABProp(27791, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp OPUS_T = new ABProp(27803, "2147483647", "2147483647");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp USE_CUSTOM_SOCCER_BALL_FOR_REACTION_ENABLED = new ABProp(27807, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_CHAT_META_AI_HOME_WEB_ENABLED = new ABProp(27817, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp LIGHTWEIGHT_GROUP_CREATION = new ABProp(27819, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SOCCER_REACTION_IN_TRAY_ENABLED = new ABProp(27833, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SOCCER_BALL_REACTION_FULL_ANIMATION_ENABLED = new ABProp(27834, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp COEXV2_SEND_ENABLED = new ABProp(27839, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_BIZ_QUALITY_TELEMETRY_MESSAGE_CLICKS_ENABLED = new ABProp(27854, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_BIZ_QUALITY_TELEMETRY_ENABLED = new ABProp(27855, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WDS_WEB_BADGE = new ABProp(27856, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_SEARCH_EMOJI_PICKER = new ABProp(27857, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp INAPP_SIGNUP_AGM_CTA_EXPERIMENT = new ABProp(27860, "1", "1");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp TIMEOUT_MEX_CALL_EXPAND_FMX_TRUST_SIGNALS = new ABProp(27862, "600", "600");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_HATCH_DOCUMENT_UPLOAD_SIZE_LIMIT_MB = new ABProp(27873, "20", "20");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_HATCH_FORWARDING_HTML_ENABLED = new ABProp(27876, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SENDER_SECRET_ENCRYPTED_MESSAGE_REMOVE_MESSAGE_SECRET = new ABProp(27913, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_JUMP_TO_CART = new ABProp(27939, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEBTP_PDF_RENDERER_MODE_NO_EXPOSURE = new ABProp(27941, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GIPHY_PMA_SHUTOFF_ENABLED = new ABProp(27942, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AURA_PREMIUM_STICKERS_KILLSWITCH = new ABProp(27946, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_CHATLIST_RENDER_CHAT_OPEN = new ABProp(27947, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WDS_WEB_PROFILE_PHOTO = new ABProp(27954, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_SHOW_TO_HIDE_ENABLED = new ABProp(27958, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp P2P_PILLS_ENABLED = new ABProp(27959, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_VOIP_CAPTURE_VIDEO_ROTATION_TYPE = new ABProp(27973, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BACKFILL_SUPPORTS_COEX_COMPANION = new ABProp(27975, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp HOSTED_MESSAGE_FLAG_ENABLED = new ABProp(27979, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WAVOIP_ML_BWE_HD_TARGET_MODEL_DOWNLOAD_VERSIONS_V2 = new ABProp(27990, "", "");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WAVOIP_ML_BWE_CONG_MODEL_DOWNLOAD_VERSIONS_V2 = new ABProp(27991, "", "");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WAVOIP_ML_BWE_TR_MODEL_DOWNLOAD_VERSIONS_V2 = new ABProp(27996, "", "");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WAVOIP_ML_BWE_PLC_MODEL_DOWNLOAD_VERSIONS_V2 = new ABProp(27998, "", "");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMBW_BUSINESS_BROADCAST_SMART_COLUMN_DETECTION_ENABLED = new ABProp(27999, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WAVOIP_ML_NADL_MODEL_DOWNLOAD_VERSIONS_V2 = new ABProp(28015, "", "");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WAVOIP_ML_BWE_GC_UNDERSHOOT_MODEL_DOWNLOAD_VERSIONS_V2 = new ABProp(28019, "", "");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WAVOIP_ML_BWE_GC_HD_TARGET_MODEL_DOWNLOAD_VERSIONS_V2 = new ABProp(28021, "", "");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_CHAT_META_AI_HOME_DEFAULT_LANDING_ENABLED = new ABProp(28033, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_VOIP_VIDEO_LOW_CAP_WIDTH = new ABProp(28041, "480", "480");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_VOIP_VIDEO_LOW_CAP_HEIGHT = new ABProp(28042, "270", "270");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_VOIP_VIDEO_MID_CAP_WIDTH = new ABProp(28043, "640", "640");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_VOIP_VIDEO_MID_CAP_HEIGHT = new ABProp(28044, "360", "360");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_CALLING_AUTO_POPOUT_VIDEO = new ABProp(28046, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_FALCO_CONSOLE_LOGGER = new ABProp(28054, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BLOCKLIST_SYSTEM_MSG_ON_FULL_REFETCH = new ABProp(28070, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GROUP_MEMBER_UPDATES_USERNAME_DESCRIPTION_ENABLED = new ABProp(28087, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENHANCED_MENTION_SUGGESTIONS_MIN_MENTION_CHAR_COUNT = new ABProp(28089, "-1", "-1");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp M2_AUDIENCE_DYNAMIC_RULES = new ABProp(28099, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp COEXV2_RECV_ENABLED = new ABProp(28110, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ALWAYS_BACKFILL_TO_COEX_COMPANION = new ABProp(28124, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CONSUMER_GRAPHQL_ENABLE_DOUBLE_LOG_FOR_SURVEY = new ABProp(28129, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_BUSINESS_BROADCAST_SEND_WEB_NO_EXP = new ABProp(28138, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_BUSINESS_BROADCAST_SEND_WEB_SMBA_NO_EXP = new ABProp(28139, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp INAPP_SIGNUP_M1_LOGGING_ENABLED = new ABProp(28142, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SYNCD_USE_INDEX_FOR_LTHASH_LOOKUP = new ABProp(28144, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp APPOINTMENT_BOOKING_BLOKS_ENABLED = new ABProp(28146, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_VISIBILITY_LOGGING_FULLSCREEN_MEDIA_ENABLED = new ABProp(28148, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_CHAT_THEME_DRAWER_TITLE = new ABProp(28157, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp FETCH_QP_VIA_GRAPHQL_WEB_ENABLED = new ABProp(28158, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CONSUMER_GRAPHQL_WEB_TO_FETCH_QP_SURFACE_IDS = new ABProp(28159, "{}", "{}");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp OUT_CONTACT_INVITES_ENABLED = new ABProp(28170, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_VOIP_LOW_RESOURCE_DEVICE = new ABProp(28203, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_PULSE_ON_UNREAD_BADGE_ENABLED = new ABProp(28224, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_WEB_LOG_DOWNLOAD = new ABProp(28226, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_NATIVE_WEB_ENABLE_AD_RECREATE = new ABProp(28258, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_ASSET_REPLACEMENT_ENABLED = new ABProp(28265, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_GENAI_STRAW_HAT = new ABProp(28268, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BUSINESS_BROADCASTS_SYNCD_WAM_LOGGING = new ABProp(28277, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_GROUP_TEE_HISTORY_SHARE_ENABLED = new ABProp(28278, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_ENABLE_CAMERA_CAPTURE_REFRESH = new ABProp(28316, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CROSS_DEVICE_MESSAGE_EDITING = new ABProp(28340, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AURA_KILL_SWITCH = new ABProp(28345, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp COEX_IICON_BACKFILL = new ABProp(28349, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GROUP_SUSPENSION_APPEALS_REDESIGN_VARIANT_ENABLE = new ABProp(28376, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WAVOIP_LARGE_VSR_MODEL_DOWNLOAD_VERSIONS = new ABProp(28388, "", "");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CANONICAL_ENT_COMPANION_SERVER_CACHED_NONCE_ENABLED = new ABProp(28399, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CALLING_RUST_MIGRATION_INCOMING_ACK_STANZA_BITMAP = new ABProp(28434, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_MENTION_SEARCH = new ABProp(28455, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_BIZ_AI_LISTS_PILLS = new ABProp(28470, "None", "None");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_BIZ_BROADCASTS_CATALOG_ATTACHMENT = new ABProp(28471, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MM_TAP_TARGET_BLOKS_CLIENT_HYDRATION_ENABLED = new ABProp(28473, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNEL_STATUS_DEEPLINK_ENABLED = new ABProp(28500, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_PUSH_NAME_IN_GLOBAL_SEARCH_NON_CONTACTS_ENABLED = new ABProp(28506, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp RELAX_INTEGRITY_CONSTRAINTS_FOR_BB_WA_TENURED_ACCOUNTS = new ABProp(28516, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_WEB_CATEGORY_SEARCH_VIA_GRAPH_ENABLED = new ABProp(28519, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CONSUMER_WEB_QP_GRAPHQL_TO_FETCH_QP_FREQUENCY_MINS = new ABProp(28529, "1320", "1320");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BIZ_AI_TOOLS_SETTINGS = new ABProp(28552, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WDS_WEB_DIALOG = new ABProp(28557, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MM_OPTIMIZED_DELIVERY_ARCHIVE_SIGNAL_SHARING_ENABLED = new ABProp(28558, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WDS_WEB_ACTION_TILE_REFRESH = new ABProp(28564, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MM_DISCLOSURE_HANDLE_TOS_FAILURES_ENABLED = new ABProp(28572, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_BIZ_SIMPLE_SIGNAL_ENABLED = new ABProp(28573, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_BIZ_QUALITY_TELEMETRY_MESSAGE_READS_ENABLED = new ABProp(28574, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_GIZMO_INTEGRATION_ENABLED = new ABProp(28584, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AI_SUBSCRIPTION_IMAGINE_INTENT_ENABLED = new ABProp(28585, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_BIZ_QUALITY_TELEMETRY_MESSAGE_LEVEL_ACTIONS_ENABLED = new ABProp(28590, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_META_ONE_ENABLED = new ABProp(28611, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_META_ONE_ROLLOUT_ENABLED = new ABProp(28612, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_META_ONE_ELIGIBILITY_SUBSCRIPTION_STATUS_CHECK_ENABLED = new ABProp(28613, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_QUICK_REACTIONS = new ABProp(28621, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PNH_HISTORY_SYNC_FORCE_GENERAL = new ABProp(28664, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp USERNAME_API_RATE_LIMIT_ENABLED = new ABProp(28678, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_BIZ_SIMPLE_SIGNAL_GROUP_ENABLED = new ABProp(28679, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_SETUP_ERROR_RESULT_CHECK = new ABProp(28689, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_RESHARE_POSTER_SIDE_ENABLED = new ABProp(28732, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GROUP_HISTORY_AFTER_JOIN_PREREQUISITES = new ABProp(28787, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AUTH_AGENT_SOFT_OFFBOARDING_ENABLED = new ABProp(28802, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp INAPP_SIGNUP_QPL_LOGGING_ENABLED = new ABProp(28806, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_STATUS_RESHARER_FLOW_ENABLED = new ABProp(28812, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_STATUS_RESHARE_ATTRIBUTION_ENABLED = new ABProp(28813, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp INAPP_SIGNUP_CONFIRMATION_SYSTEM_MESSAGE_DELAY = new ABProp(28821, "3000", "3000");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_CALLING_FULL_SCREEN_TOGGLE_ENABLED = new ABProp(28830, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_ANR_THROTTLE_SIGNAL_SNAPSHOT_ENABLED = new ABProp(28890, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp IM_NFM_MULTI_STEP_FORM_KILLSWITCH = new ABProp(28891, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_BOT_TOS_CHECK_REFINIEMENT = new ABProp(28897, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_VOIP_ADAPTIVE_GRID_PAGE_SIZE = new ABProp(28909, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_BOT_PROFILE_GQL_MIGRATION_ENABLED = new ABProp(28941, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_CONTACT_SORT_LETTERS_FIRST = new ABProp(28962, "-1", "-1");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_NATIVE_WEB_DRAFT_AD_ENABLED = new ABProp(28989, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MM_OPTIMIZED_DELIVERY_TOKEN_FALLBACK_DISABLED = new ABProp(29002, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMBW_BUSINESS_BROADCAST_DUPLICATE_ENABLED = new ABProp(29021, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp USERNAME_KEY_REDESIGN_ENABLED = new ABProp(29026, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CALL_INFO_USE_TYPED_JID = new ABProp(29027, "0", "0");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SMB_BUSINESS_BROADCAST_PRO_ENABLED = new ABProp(29033, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MM_OPTIMIZED_DELIVERY_UNIQUE_TOKEN_PER_MESSAGE_ID_ENABLED = new ABProp(29037, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_BLOCKED_PARTICIPANT_CHAT_WARNING = new ABProp(29038, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_BLOCKED_PARTICIPANT_CALL_WARNING = new ABProp(29039, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_ANR_PRUNE_CMC = new ABProp(29060, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_FREQUENTLY_CONTACTED_ENABLED = new ABProp(29063, "-1", "-1");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_NATIVE_WEB_SCENARIO_ROUTING_ENABLED = new ABProp(29074, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_ANR_BATCH_PROFILE_PICTURE_BRIDGE_OPERATIONS = new ABProp(29122, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_IN_APP_POLICY_DETAIL_ENABLED = new ABProp(29132, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ANIMATED_EMOJI_USE_LAZY_PARSING = new ABProp(29140, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GROUP_CALLING_WAVE_RECEIVING_ENABLED = new ABProp(29161, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp NO_LARGE_EMOJI_REGEX = new ABProp(29172, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WMI_ASYNC_AWAIT_PREP = new ABProp(29197, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SHORTCAKE_COMPANION_PROLOGUE__PASSKEYS__HANDOFF_ENABLED = new ABProp(29204, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp SHORTCAKE_COMPANION_PROLOGUE__PASSKEYS__ENABLED = new ABProp(29206, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CHANNELS_QUESTIONS_RESPONSES_DRAWER_LOADING_SHIMMER_ENABLED = new ABProp(29209, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp INFO_DRAWER_REFRESH = new ABProp(29210, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp VID_PORT_ENABLE_CAPTURE_FPS_MEDIAN_FILTER = new ABProp(29214, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ACS_USE_GRAPHQL_FOR_MIGRATION_TEST = new ABProp(29217, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ACS_USE_GRAPHQL_FOR_FORWARD_COUNTER = new ABProp(29218, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_CALL_TRANSFER_NOTIFICATION = new ABProp(29242, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp GROUP_CALLING_WAVE_SENDING_ENABLED = new ABProp(29247, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_LARGE_GROUP_PRESENCE_ENABLED = new ABProp(29279, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_SMALL_GROUP_PRESENCE_ENABLED = new ABProp(29280, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_META_ONE_LAUNCH_FREE_TRIAL_ENABLED = new ABProp(29290, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_MATCH_PRIMARY_ICONS = new ABProp(29293, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_ANR_GROUP_METADATA_YIELD = new ABProp(29294, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BIZ_AI_WEB_ONBOARDING_HANDOFF = new ABProp(29298, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AURA_MEDIA_OFFLOAD_BENEFIT_ACTIVE = new ABProp(29308, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_SYNC_FOR_DRAFT_MESSAGES = new ABProp(29314, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_3PD_DATA_SHARING_TITLE_CHANGE = new ABProp(29332, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_3PD_DATA_SHARING_ADDITIONAL_LOGGING = new ABProp(29333, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PAYMENTS_UPR_BUBBLE_COUNTRIES = new ABProp(29342, "", "");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp MARK_AS_VERIFIED_ENABLED = new ABProp(29343, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BIZ_AI_TOOLS_SYNC = new ABProp(29383, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp AURA_MEDIA_OFFLOAD_ENABLED = new ABProp(29391, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_READ_SELF_WATERMARK_RECEIVE_STORE_TS = new ABProp(29396, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_ANR_SPINNER_GPU_ANIMATION = new ABProp(29405, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_CHAT_THEMES_LOGGING = new ABProp(29457, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp BUG_REPORTING_NOT_SHIPPED_YET_ENABLED = new ABProp(29458, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_ANR_ASYNC_SQLITE_BRIDGE_OPERATIONS = new ABProp(29460, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_CANONICAL_REG_RELOAD_ENABLED = new ABProp(29472, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_EXTERNAL_AD_REPLY_URL_ALLOWLIST_DOMAINS = new ABProp(29484, ".whatsapp.net,.whatsapp.com,.fbcdn.net,.facebook.com,.instagram.com,.cdninstagram.com", ".whatsapp.net,.whatsapp.com,.fbcdn.net,.facebook.com,.instagram.com,.cdninstagram.com");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_REMOVE_MESSAGE_SECRET_FROM_QUOTED_ENABLED = new ABProp(29491, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_MOVE_MESSAGE_SECRET_TOP_LEVEL_ENABLED = new ABProp(29492, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_WEBCODEC_REQUIRE_KEYFRAME = new ABProp(29510, "true", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp CTWA_FAVORITES_LIST_SENDS_SIGNALS = new ABProp(29529, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_EXPANSION_COUNTRIES_BONSAI_ENABLED = new ABProp(29543, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_READ_SELF_WATERMARK_SEND_STORE_TS = new ABProp(29546, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WIN_PDF_RENDERING_ENABLED = new ABProp(29548, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_ANR_ASYNC_NATIVE_APP_STATE_BRIDGE_ENABLED = new ABProp(29551, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp P2P_PILLS_ALLOWLIST = new ABProp(29554, "[{ \"business_id\": \"34666845417\", \"pills\": [\"CHAT\", \"PROFILE\", \"BOOK_APPOINTMENT\", \"CATALOG\", \"BESTSELLERS\", \"OFFERS\", \"ABOUT_US\"] }]", "[{ \"business_id\": \"34666845417\", \"pills\": [\"CHAT\", \"PROFILE\", \"BOOK_APPOINTMENT\", \"CATALOG\", \"BESTSELLERS\", \"OFFERS\", \"ABOUT_US\"] }]");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_CALLING_OFFLINE_RESUME_ORDERING = new ABProp(29564, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_WHATS_NEW_CAROUSEL = new ABProp(29618, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_WHATS_NEW_BANNER = new ABProp(29619, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_WHATS_NEW_BANNER_SHORT_COOLDOWN = new ABProp(29620, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_WHATS_NEW_AUTO_MODAL = new ABProp(29621, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_WHATS_NEW_AUTO_MODAL_SHORT_COOLDOWN = new ABProp(29622, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PREMIUM_MSG_BB_CAMPAIGN_SYNC_ENABLED = new ABProp(29650, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp P2P_PILLS_ALLOWLIST_ENTRIES = new ABProp(29708, "{ \"entries\": [{ \"business_id\": \"34666845417\", \"pills\": [\"CHAT\", \"PROFILE\", \"ABOUT_US\"] }]}", "{ \"entries\": [{ \"business_id\": \"34666845417\", \"pills\": [\"CHAT\", \"PROFILE\", \"ABOUT_US\"] }]}");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WEB_WHATS_NEW_BANNER_SHORT_COOLDOWN_V2 = new ABProp(29709, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp P2P_PILLS_ENABLED_FOR_INELIGIBLE_CONTACTS = new ABProp(29715, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp WA_WEB_BOT_ORPHAN_LOGIC_ENABLED = new ABProp(29753, "false", "true");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp ENABLE_WEB_VOIP_WEBTRANSPORT = new ABProp(29764, "false", "false");

    /**
     * This constant was generated automatically by {@code tooling/web-ab-props-extractor}.
     */
    public static final ABProp PAYMENTS_BR_P2M_BUYER_LOGGING_PHASE_2 = new ABProp(29803, "false", "false");

    /**
     * Constructs a new {@code ABProp} definition.
     *
     * @throws NullPointerException if {@code defaultValue} or {@code debugDefaultValue}
     *         is {@code null}
     */
    public ABProp {
        Objects.requireNonNull(defaultValue, "defaultValue cannot be null");
        Objects.requireNonNull(debugDefaultValue, "debugDefaultValue cannot be null");
    }

    /**
     * Converts a string value to a boolean.
     *
     * <p>The following values are considered {@code true}: {@code "1"}, {@code "True"},
     * and {@code "true"}. All other values are considered {@code false}.
     *
     * @param value the string value to convert
     * @return the boolean representation of the given value
     */
    public static boolean toBoolean(String value) {
        return "1".equals(value)
                || "True".equals(value)
                || "true".equals(value);
    }

    /**
     * Attempts to convert a string value to an integer.
     *
     * @param value the string value to parse
     * @return an {@link OptionalInt} containing the integer value if parsing succeeds,
     *         or empty if it fails
     */
    public static OptionalInt toInt(String value) {
        try {
            return OptionalInt.of(Integer.parseInt(value));
        } catch (NumberFormatException exception) {
            return OptionalInt.empty();
        }
    }

    /**
     * Attempts to convert a string value to a long.
     *
     * @param value the string value to parse
     * @return an {@link OptionalLong} containing the long value if parsing succeeds,
     *         or empty if it fails
     */
    public static OptionalLong toLong(String value) {
        try {
            return OptionalLong.of(Long.parseLong(value));
        } catch (NumberFormatException exception) {
            return OptionalLong.empty();
        }
    }

    /**
     * Attempts to convert a string value to a double (floating-point).
     *
     * @param value the string value to parse
     * @return an {@link OptionalDouble} containing the double value if parsing succeeds,
     *         or empty if it fails
     */
    public static OptionalDouble toDouble(String value) {
        try {
            return OptionalDouble.of(Double.parseDouble(value));
        } catch (NumberFormatException exception) {
            return OptionalDouble.empty();
        }
    }

    @Override
    public String toString() {
        return "ABProp[code=%d, defaultValue='%s', debugDefaultValue='%s']"
                .formatted(code, defaultValue, debugDefaultValue);
    }
}
