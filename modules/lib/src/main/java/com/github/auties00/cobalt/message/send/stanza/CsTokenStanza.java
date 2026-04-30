package com.github.auties00.cobalt.message.send.stanza;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.props.ABProp;
import com.github.auties00.cobalt.props.ABPropsService;
import com.github.auties00.cobalt.store.WhatsAppStore;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Objects;

/**
 * Builds the {@code <cstoken>} stanza child node carrying an HMAC-SHA-256 non-contact
 * token (NCT) for the recipient.
 *
 * <p>The token is included only when the {@code wa_nct_token_send_enabled} AB prop is
 * enabled, the recipient is a regular user (not a bot or group), an NCT salt is available
 * in the store, and the chat has an {@code accountLid} for the recipient.
 *
 * <p>The HMAC is computed as {@code HMAC-SHA-256(salt, accountLid.toString())} and cached
 * per recipient LID with a maximum of {@value #MAX_CACHE_SIZE} entries. The salt itself
 * is cached to avoid redundant decoding and the HMAC cache is cleared whenever the salt
 * changes.
 *
 * @see TcTokenStanza
 * @see ChatFanoutStanza
 */
@WhatsAppWebModule(moduleName = "WAWebSendMsgCreateFanoutStanza")
public final class CsTokenStanza {
    /**
     * Logger for HMAC computation failures and missing data.
     */
    private static final System.Logger LOGGER = System.getLogger(CsTokenStanza.class.getName());

    /**
     * Maximum number of cached HMAC results per salt.
     */
    private static final int MAX_CACHE_SIZE = 5;

    /**
     * The HMAC algorithm used for token computation.
     */
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    /**
     * Store used to retrieve the NCT salt and the chat's account LID.
     */
    private final WhatsAppStore store;

    /**
     * AB props service used to gate token emission via
     * {@code wa_nct_token_send_enabled}.
     */
    private final ABPropsService abPropsService;

    /**
     * Cached reference to the last salt bytes used, to avoid re-decoding.
     */
    private byte[] cachedSalt;

    /**
     * Cached HMAC results keyed by the recipient LID string.
     */
    private final LinkedHashMap<String, byte[]> hmacCache;

    /**
     * Creates a new CS token stanza builder.
     *
     * @param store          the WhatsApp store for salt and chat lookup
     * @param abPropsService the AB props service for feature gating
     * @throws NullPointerException if any argument is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WAWebSendMsgCreateFanoutStanza", exports = "genCsTokenBody",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public CsTokenStanza(WhatsAppStore store, ABPropsService abPropsService) {
        this.store = Objects.requireNonNull(store, "store");
        this.abPropsService = Objects.requireNonNull(abPropsService, "abPropsService");
        this.hmacCache = new LinkedHashMap<>();
    }

    /**
     * Builds the {@code <cstoken>} node for the given chat recipient.
     *
     * <p>Returns {@code null} if the AB prop is disabled, the recipient is not a regular
     * user, no NCT salt is available, or the chat has no {@code accountLid}.
     *
     * @param chatJid the recipient chat JID
     * @return the cstoken node, or {@code null} if not applicable
     */
    @WhatsAppWebExport(moduleName = "WAWebSendMsgCreateFanoutStanza", exports = "genCsTokenBody",
            adaptation = WhatsAppAdaptation.DIRECT)
    public Node build(Jid chatJid) {
        if (!abPropsService.getBool(ABProp.WA_NCT_TOKEN_SEND_ENABLED)) {
            return null;
        }

        if (!isRegularUser(chatJid)) {
            return null;
        }

        var salt = store.nctSalt().orElse(null);
        if (salt == null) {
            LOGGER.log(System.Logger.Level.WARNING, "[nct-cstoken] no salt available in store");
            return null;
        }

        var chat = store.findChatByJid(chatJid).orElse(null);
        var recipientLid = chat != null ? chat.accountLid().orElse(null) : null;
        if (recipientLid == null) {
            LOGGER.log(System.Logger.Level.WARNING, "[nct-cstoken] recipientLid is null");
            return null;
        }

        try {
            // The HMAC cache is keyed implicitly to the current salt; clearing it on
            // salt change keeps cache hits valid without per-entry salt tags.
            if (cachedSalt == null || !Arrays.equals(cachedSalt, salt)) {
                cachedSalt = salt;
                hmacCache.clear();
            }

            var lidString = recipientLid.toString();

            var cached = hmacCache.get(lidString);
            if (cached != null) {
                return new NodeBuilder()
                        .description("cstoken")
                        .content(cached)
                        .build();
            }

            var mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(salt, HMAC_ALGORITHM));
            var hmacResult = mac.doFinal(lidString.getBytes(StandardCharsets.UTF_8));

            if (hmacCache.size() >= MAX_CACHE_SIZE) {
                var firstKey = hmacCache.keySet().iterator().next();
                if (firstKey != null) {
                    hmacCache.remove(firstKey);
                }
            }
            hmacCache.put(lidString, hmacResult);

            return new NodeBuilder()
                    .description("cstoken")
                    .content(hmacResult)
                    .build();
        } catch (GeneralSecurityException e) {
            LOGGER.log(System.Logger.Level.WARNING,
                    "[nct-cstoken] generation failed - {0}", e.getMessage());
            return null;
        }
    }

    /**
     * Returns whether the given JID represents a regular user, excluding bots, groups
     * and broadcasts.
     *
     * @param jid the JID to check
     * @return {@code true} if the JID is a regular user
     * @implNote ADAPTED: Cobalt does not have a PSA check; the PSA account is rare and
     * irrelevant for NCT tokens, so {@code isUser() && !isBot()} is used as a
     * sufficient approximation.
     */
    @WhatsAppWebExport(moduleName = "WAWebWid", exports = "isRegularUser",
            adaptation = WhatsAppAdaptation.ADAPTED)
    private static boolean isRegularUser(Jid jid) {
        return (jid.hasUserServer() || jid.hasLidServer()) && !jid.isBot();
    }
}
