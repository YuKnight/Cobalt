package com.github.auties00.cobalt.message.send.bot;

import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.message.common.MessageContainer;
import com.github.auties00.cobalt.model.message.standard.TextMessage;
import com.github.auties00.cobalt.props.ABProp;
import com.github.auties00.cobalt.props.ABPropsService;
import com.github.auties00.cobalt.store.WhatsAppStore;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Detects bot accounts and determines bot message types.
 *
 * @apiNote WAWebBotGating, WAWebBotTypes, WAWebSimpleSignalPNToFBIDMigration
 */
public final class BotDetector {
    private static final System.Logger LOGGER = System.getLogger("BotDetector");

    private static final String META_AI_BOT_FBID = "867051314767696";

    /**
     * Full FBID bot list from WAWebSimpleSignalPNToFBIDMigration.
     * Maps FBID → phone number for all known first-party bot accounts.
     */
    private static final Set<String> KNOWN_FBID_BOTS = Set.of(
            "867051314767696", "1061492271844689", "245886058483988", "3509905702656130",
            "1059680132034576", "715681030623646", "1644971366323052", "582497970646566",
            "645459357769306", "294997126699143", "1522631578502677", "719421926276396",
            "1788488635002167", "24232338603080193", "689289903143209", "871626054177096",
            "362351902849370", "1744617646041527", "893887762270570", "1155032702135830",
            "333931965993883", "853748013058752", "1559068611564819", "890487432705716",
            "240254602395494", "1578420349663261", "322908887140421", "3713961535514771",
            "997884654811738", "403157239387035", "535242369074963", "946293427247659",
            "3664707673802291", "1821827464894892", "1760312477828757", "439480398712216",
            "1876735582800984", "984025089825661", "1001336351558186", "3739346336347061",
            "3632749426974980", "427864203481615", "1434734570493055", "992873449225921",
            "813087747426445", "806369104931434", "1220982902403148", "1365893374104393",
            "686482033622048", "1454999838411253", "718584497008509", "743520384213443",
            "1147715789823789", "1173034540372201", "974785541030953", "1122200255531507",
            "899669714813162", "631880108970650", "435816149330026", "1368717161184556",
            "7849963461784891", "3609617065968984", "356273980574602", "1043447920539760",
            "1052764336525346", "2631118843732685", "510505411332176", "1945664239227513",
            "1518594378764656", "1378821579456138", "490214716896013", "1028577858870699",
            "308915665545959", "845884253678900", "995031308616442", "2787365464763437",
            "1532790990671645", "302617036180485", "723376723197227", "8393570407377966",
            "1931159970680725", "401073885688605", "2234478453565422", "814748673882312",
            "26133635056281592", "1439804456676119", "889851503172161", "1018283232836879",
            "1012781386779537", "823280953239532", "1597090934573334", "485965054020343",
            "1033381648363446", "491802010206446", "1017139033184870", "499638325922174",
            "468946335863664", "1570389776875816", "1004342694328995", "1012240323971229",
            "392171787222419", "952081212945019", "444507875070178", "1274819440594668",
            "1397041101147050", "425657699872640", "532292852562549", "705863241720292",
            "476449815183959", "488071553854222", "468693832665397", "517422564037340",
            "819805466613825", "1847708235641382", "716282970644228", "521655380527741",
            "476193631941905", "485600497445562", "440217235683910", "523342446758478",
            "514784864360240", "505790121814530", "420008964419580", "492141680204555",
            "388462787271952", "423473920752072", "489574180468229", "432360635854105",
            "477878201669248", "351656951234045", "430178036732582", "434537312944552",
            "1240614300631808", "473135945605128", "423669800729310", "3685666705015792",
            "504196509016638", "346844785189449", "504823088911074", "402669415797083",
            "490939640234431", "875124128063715", "468788962654605", "562386196354570",
            "372159285928791", "531017479591050", "1328873881401826", "1608363646390484",
            "1229628561554232", "348802211530364", "3708535859420184", "415517767742187",
            "479330341612638", "480785414723083", "387299107507991", "333389813188944",
            "391794130316996", "457893470576314", "435550496166469", "1620162702100689",
            "867491058616043", "816224117357759", "334065176362830", "489973170554709",
            "473060669049665", "1221505815643060", "889000703096359", "475235961979883",
            "3434445653519934", "524503026827421", "1179639046403856", "471563305859144",
            "533896609192881", "365443583168041", "836082305329393", "1056787705969916",
            "503312598958357", "3718606738453460", "826066052850902", "1033611345091888",
            "3868390816783240", "7462677740498860", "436288576108573", "1047559746718900",
            "1099299455255491", "1202037301040633", "1720619402074074", "1030422235101467",
            "827238979523502", "1516443722284921", "1174442747196709", "1653165225503842",
            "1037648777635013", "551617757299900", "1158813558718726", "2463236450542262",
            "1550393252501466", "2057065188042796", "506163028760735", "2065249100538481",
            "1041382867195858", "886500209499603", "1491615624892655", "486563697299617",
            "1175736513679463", "491811473512352"
    );

    private final ABPropsService abPropsService;
    private final WhatsAppStore store;

    public BotDetector(ABPropsService abPropsService, WhatsAppStore store) {
        this.abPropsService = Objects.requireNonNull(abPropsService, "abPropsService cannot be null");
        this.store = Objects.requireNonNull(store, "store cannot be null");
    }

    /**
     * Checks if a JID is a bot.
     */
    public boolean isBot(Jid jid) {
        if (jid == null) {
            return false;
        }
        return isFbidBot(jid) || isPnBot(jid) || isMetaAiBot(jid);
    }

    /**
     * Checks if a JID is the Meta AI bot.
     */
    public boolean isMetaAiBot(Jid jid) {
        if (jid == null) {
            return false;
        }
        var user = jid.user();
        return user != null && (user.equals("13135550002") || user.equals("meta.ai"));
    }

    /**
     * Checks if bots are enabled.
     */
    public boolean isBotsEnabled() {
        return abPropsService.getBool(ABProp.WEB_BOT_ENABLED_AB_PROP_CODE).orElse(true);
    }

    /**
     * Checks if a JID is an FBID bot.
     * <p>
     * An FBID bot has server="bot" and device=0.
     *
     * @apiNote WAWebWid.isFbidBot: server === "bot" && (device == null || device === 0)
     */
    public boolean isFbidBot(Jid jid) {
        if (jid == null) {
            return false;
        }
        return jid.hasBotServer() && !jid.hasDevice();
    }

    /**
     * Checks if a JID is a PN (phone number) based bot.
     * <p>
     * A PN bot has the user server (c.us/s.whatsapp.net), a numeric user of
     * sufficient length, and device=0.
     *
     * @apiNote WAWebWid.isPnBot: server === "c.us" && regex.test(user) && (device == null || device === 0)
     */
    public boolean isPnBot(Jid jid) {
        if (jid == null) {
            return false;
        }
        if (!jid.hasUserServer()) {
            return false;
        }
        if (jid.hasDevice()) {
            return false;
        }
        var user = jid.user();
        return user != null && user.matches("\\d+") && user.length() >= 7;
    }

    /**
     * Gets the persona type for an FBID bot.
     *
     * @apiNote WAWebSimpleSignalPNToFBIDMigration.getFbidBotPersonaType
     */
    public BotPersonaType getFbidBotPersonaType(Jid jid) {
        if (jid == null || !isFbidBot(jid)) {
            return null;
        }

        var migrationEnabled = abPropsService.getBool(ABProp.AI_FBID_MIGRATION_SENDING_AB_PROP_CODE)
                .orElse(true);
        if (!migrationEnabled) {
            return null;
        }

        var user = jid.user();
        if (user == null) {
            return null;
        }

        if (META_AI_BOT_FBID.equals(user)) {
            return BotPersonaType.DEFAULT;
        }

        if (KNOWN_FBID_BOTS.contains(user)) {
            return BotPersonaType.FIRST_PARTY_CHARACTER;
        }

        return BotPersonaType.UGC;
    }

    /**
     * Gets the persona type string for an FBID bot.
     */
    public String getFbidBotPersonaTypeValue(Jid jid) {
        var personaType = getFbidBotPersonaType(jid);
        return personaType != null ? personaType.value() : null;
    }

    /**
     * Determines the bot message type for a message.
     */
    public BotMessageType getBotMessageType(MessageContainer message, String subtype, Jid chatJid) {
        if ("bot_request_welcome".equals(subtype)) {
            return BotMessageType.REQUEST_WELCOME;
        }

        if (isBotFeedbackMessage(message, chatJid)) {
            return BotMessageType.FEEDBACK;
        }

        if (isBot(chatJid)) {
            return detectBotMsgBodyType(message);
        }

        return null;
    }

    /**
     * Checks if a message is a bot feedback message.
     */
    public boolean isBotFeedbackMessage(MessageContainer message, Jid chatJid) {
        if (!isBotsEnabled()) {
            return false;
        }

        if (isBot(chatJid)) {
            return true;
        }

        var unwrapped = message.unbox();
        var contextualMessage = unwrapped.contentWithContext().orElse(null);
        if (contextualMessage != null) {
            var contextInfo = contextualMessage.contextInfo().orElse(null);
            if (contextInfo != null) {
                var quotedParticipant = contextInfo.quotedMessageSenderJid().orElse(null);
                if (quotedParticipant != null && isBot(quotedParticipant)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Gets the business bot type from the stanza biz_bot attribute.
     * <p>
     * The biz_bot attribute uses "1" for 1P and "3" for 3P.
     *
     * @apiNote WAWebHandleMsgParser: biz_bot === "1" → BIZ_1P, biz_bot === "3" → BIZ_3P
     */
    public BusinessBotType getBizBotType(Integer bizBotType) {
        if (bizBotType == null) {
            return null;
        }
        return switch (bizBotType) {
            case 1 -> BusinessBotType.BIZ_1P;
            case 3 -> BusinessBotType.BIZ_3P;
            default -> null;
        };
    }

    /**
     * Checks if open group bot sending is enabled.
     */
    public boolean isOpenGroupBotSendEnabled() {
        return abPropsService.getBool(ABProp.WEB_AI_GROUP_OPEN_SUPPORT_AB_PROP_CODE).orElse(false)
                && abPropsService.getBool(ABProp.AI_GROUP_PARTICIPATION_SEND_ENABLED_AB_PROP_CODE).orElse(false);
    }

    /**
     * Gets the Meta AI bot FBID JID.
     */
    public Jid getMetaAiBotFbidJid() {
        return Jid.metaAiBotAccount();
    }

    /**
     * Resolves the invokerJid for bot metadata.
     * <p>
     * For feedback messages, returns the botTargetSenderJid directly.
     * For non-feedback messages, converts the botTargetSenderJid to LID format.
     *
     * @param botTargetSenderJid the target sender JID
     * @param isFeedback         whether this is a feedback message
     * @return the resolved invoker JID string, or null
     * @apiNote WAWebGenerateBotMetadata function c (invokerJid resolution)
     */
    public String resolveInvokerJid(Jid botTargetSenderJid, boolean isFeedback) {
        if (botTargetSenderJid == null) {
            return null;
        }

        if (isFeedback) {
            return botTargetSenderJid.toString();
        }

        var lidJidOpt = store.getLidByPhoneNumber(botTargetSenderJid);
        if (lidJidOpt.isPresent()) {
            return lidJidOpt.get().toString();
        }

        LOGGER.log(System.Logger.Level.DEBUG,
                "Could not resolve LID for invokerJid: {0}", botTargetSenderJid);
        return null;
    }

    /**
     * Determines the bot JID for group messages.
     */
    public Optional<Jid> determineGroupBotJid(
            Jid invokedBotJid,
            Jid protocolMessageKeyParticipant,
            Jid botRespOrInvocationRevokeJid,
            boolean isBotFeedback,
            boolean isRevokeForBot,
            boolean isOpenBotGroup
    ) {
        Jid botJid;

        if (isBotFeedback) {
            botJid = protocolMessageKeyParticipant;
        } else if (isRevokeForBot) {
            botJid = botRespOrInvocationRevokeJid;
        } else if (isOpenGroupBotSendEnabled() && isOpenBotGroup) {
            botJid = getMetaAiBotFbidJid();
        } else {
            botJid = invokedBotJid;
        }

        if (botJid == null || !isBot(botJid)) {
            return Optional.empty();
        }

        return Optional.of(botJid);
    }

    private BotMessageType detectBotMsgBodyType(MessageContainer message) {
        return switch (message.content()) {
            case TextMessage textMessage when textMessage.text().startsWith("/") -> BotMessageType.COMMAND;
            case TextMessage _ -> BotMessageType.PROMPT;
            default -> null;
        };
    }
}
