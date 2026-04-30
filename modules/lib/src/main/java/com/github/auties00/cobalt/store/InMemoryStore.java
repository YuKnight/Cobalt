package com.github.auties00.cobalt.store;

import com.github.auties00.cobalt.client.WhatsAppClientType;
import com.github.auties00.cobalt.client.WhatsAppDevice;
import com.github.auties00.cobalt.client.WhatsAppWebClientHistory;
import com.github.auties00.cobalt.model.business.BusinessVerifiedName;
import com.github.auties00.cobalt.model.business.profile.BusinessCategory;
import com.github.auties00.cobalt.model.call.CallOffer;
import com.github.auties00.cobalt.model.chat.*;
import com.github.auties00.cobalt.model.chat.group.GroupParticipant;
import com.github.auties00.cobalt.model.contact.Contact;
import com.github.auties00.cobalt.model.contact.OutContact;
import com.github.auties00.cobalt.model.device.identity.ADVSignedDeviceIdentity;
import com.github.auties00.cobalt.model.device.pairing.ClientAppVersion;
import com.github.auties00.cobalt.model.device.pairing.ClientPayload;
import com.github.auties00.cobalt.model.device.sync.MissingDeviceSyncKey;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.jid.JidProvider;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.model.media.MediaVisibility;
import com.github.auties00.cobalt.model.message.MessageInfo;
import com.github.auties00.cobalt.model.message.MessageKey;
import com.github.auties00.cobalt.model.message.PrivacySystemMessage;
import com.github.auties00.cobalt.model.message.system.appstate.AppStateSyncKey;
import com.github.auties00.cobalt.model.newsletter.*;
import com.github.auties00.cobalt.model.preference.Label;
import com.github.auties00.cobalt.model.preference.QuickReply;
import com.github.auties00.cobalt.model.preference.Sticker;
import com.github.auties00.cobalt.model.privacy.PrivacySettingEntry;
import com.github.auties00.cobalt.model.privacy.PrivacySettingType;
import com.github.auties00.cobalt.model.setting.ChatLockSettings;
import com.github.auties00.cobalt.model.setting.WallpaperSettings;
import com.github.auties00.cobalt.model.sync.SyncHashValue;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.util.StorePathUtils;
import com.github.auties00.libsignal.SignalProtocolAddress;
import com.github.auties00.libsignal.groups.SignalSenderKeyName;
import com.github.auties00.libsignal.groups.state.SignalSenderKeyRecord;
import com.github.auties00.libsignal.key.SignalIdentityKeyPair;
import com.github.auties00.libsignal.key.SignalIdentityPublicKey;
import com.github.auties00.libsignal.key.SignalPreKeyPair;
import com.github.auties00.libsignal.key.SignalSignedKeyPair;
import com.github.auties00.libsignal.state.SignalSessionRecord;
import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;
import it.auties.protobuf.stream.ProtobufInputStream;
import it.auties.protobuf.stream.ProtobufOutputStream;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.WARNING;
import static java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor;

/**
 * In-memory implementation of {@link WhatsAppStore} that persists its state
 * to protobuf files on disk.
 *
 * <p>This class specialises {@link AbstractWhatsAppStore} by holding all
 * chats and newsletters in {@link ConcurrentHashMap}s that are also serialised
 * as protobuf {@code MAP} fields on the outer store message. The root store
 * is serialised to {@code store.proto}, and every chat or newsletter is
 * serialised to its own {@code chat_*.proto} or {@code newsletter_*.proto}
 * file so that per-entity writes do not require rewriting the whole store.
 *
 * <p>{@link #save()} tracks per-entity hash codes and only rewrites files
 * whose content has changed. After loading, a virtual-thread worker
 * deserialises chats and newsletters in the background, keeping the boot
 * path fast for sessions with a large history.
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
@ProtobufMessage
final class InMemoryStore extends AbstractWhatsAppStore {
    /**
     * File-name prefix used for per-chat protobuf files under the session
     * directory.
     */
    private static final String CHAT_PREFIX = "chat_";

    /**
     * File-name prefix used for per-newsletter protobuf files under the
     * session directory.
     */
    private static final String NEWSLETTER_PREFIX = "newsletter_";

    /**
     * The chats keyed by their JID.
     */
    @ProtobufProperty(index = 82, type = ProtobufType.MAP, mapKeyType =  ProtobufType.STRING, mapValueType = ProtobufType.MESSAGE)
    final ConcurrentHashMap<Jid, Chat> chats;

    /**
     * The newsletters keyed by their JID.
     */
    @ProtobufProperty(index = 83, type = ProtobufType.MAP, mapKeyType =  ProtobufType.STRING, mapValueType = ProtobufType.MESSAGE)
    final ConcurrentHashMap<Jid, Newsletter> newsletters;

    /**
     * The status updates keyed by their message identifier.
     */
    private final ConcurrentHashMap<String, ChatMessageInfo> status;

    /**
     * The hash code of the root store at the time of the last successful save.
     */
    private volatile Integer storeHashCode;

    /**
     * The hash codes of every chat and newsletter at the time of the last
     * successful save, keyed by the {@code (storeId, jid)} pair.
     */
    private final ConcurrentMap<StoreJidPair, Integer> jidsHashCodes;

    /**
     * The virtual thread used to deserialise per-chat and per-newsletter
     * files in the background after loading.
     */
    private volatile Thread attributionThread;

    /**
     * Constructs a new in-memory store from the protobuf-decoded fields and
     * the per-chat and per-newsletter maps.
     *
     * @param uuid                                the session UUID
     * @param phoneNumber                         the associated phone number, or {@code null}
     * @param clientType                          the client type, must not be {@code null}
     * @param initializationTimeStamp             the moment at which the store was first created
     * @param device                              the device descriptor, must not be {@code null}
     * @param releaseChannel                      the release channel, may be {@code null}
     * @param online                              whether the user appears online
     * @param locale                              the locale code, may be {@code null}
     * @param name                                the display name, may be {@code null}
     * @param verifiedName                        the verified business name, may be {@code null}
     * @param profilePicture                      the profile picture URI, may be {@code null}
     * @param about                               the about text, may be {@code null}
     * @param jid                                 the user JID, may be {@code null}
     * @param lid                                 the user LID, may be {@code null}
     * @param businessAddress                     the business address, may be {@code null}
     * @param businessLongitude                   the business longitude, may be {@code null}
     * @param businessLatitude                    the business latitude, may be {@code null}
     * @param businessDescription                 the business description, may be {@code null}
     * @param businessWebsite                     the business website URL, may be {@code null}
     * @param businessEmail                       the business email, may be {@code null}
     * @param businessCategory                    the business category, may be {@code null}
     * @param contacts                            the contact map, must not be {@code null}
     * @param calls                               the call map, must not be {@code null}
     * @param privacySettings                     the privacy settings map, must not be {@code null}
     * @param unarchiveChats                      whether chats unarchive on new messages
     * @param twentyFourHourFormat                whether the 24-hour time format is preferred
     * @param newChatsEphemeralTimer              the default ephemeral timer for new chats
     * @param webHistoryPolicy                    the history sync policy for web clients, may be {@code null}
     * @param automaticPresenceUpdates            whether automatic presence updates are enabled
     * @param automaticMessageReceipts            whether automatic message receipts are enabled
     * @param checkPatchMacs                      whether patch MAC verification is enabled
     * @param syncedChats                         whether chats have been synced
     * @param syncedContacts                      whether contacts have been synced
     * @param syncedNewsletters                   whether newsletters have been synced
     * @param syncedStatus                        whether status updates have been synced
     * @param syncedWebAppState                   whether web app state has been synced
     * @param syncedBusinessCertificate           whether the business certificate has been synced
     * @param registrationId                      the Signal registration id, may be {@code null} to be generated
     * @param noiseKeyPair                        the Noise key pair, may be {@code null} to be generated
     * @param identityKeyPair                     the Signal identity key pair, may be {@code null} to be generated
     * @param signedDeviceIdentity                the signed device identity, may be {@code null}
     * @param signedKeyPair                       the signed pre-key pair, may be {@code null} to be derived
     * @param preKeys                             the pre-key map, must not be {@code null}
     * @param fdid                                the FDID, may be {@code null} to be generated
     * @param deviceId                            the device id, may be {@code null} to be generated
     * @param advertisingId                       the advertising id, may be {@code null} to be generated
     * @param identityId                          the identity id, may be {@code null} to be generated
     * @param backupToken                         the backup token, may be {@code null} to be generated
     * @param senderKeys                          the sender key map, must not be {@code null}
     * @param appStateKeys                        the app state sync key map, must not be {@code null}
     * @param sessions                            the Signal session map, must not be {@code null}
     * @param hashStates                          the patch hash state map, must not be {@code null}
     * @param registered                          whether registration completed
     * @param showSecurityNotifications           whether security notifications are shown
     * @param recentStickers                      the recent sticker map
     * @param favouriteStickers                   the favourite sticker map
     * @param quickReplies                        the quick reply map
     * @param labels                              the label map
     * @param clientVersion                       the client version, may be {@code null}
     * @param companionVersion                    the companion version, may be {@code null}
     * @param lastAdvCheckTime                    the last ADV check time, may be {@code null}
     * @param remoteIdentities                    the remote identity key map, may be {@code null}
     * @param missingSyncKeys                     the missing sync key map, may be {@code null}
     * @param advSecretKey                        the ADV HMAC secret, may be {@code null}
     * @param verifiedBusinessNames               the verified business name map, may be {@code null}
     * @param directory                           the session directory, must not be {@code null}
     * @param primaryDeviceSupportsSyncdRecovery  whether the primary device supports syncd recovery
     * @param disableLinkPreviews                 whether link previews are disabled
     * @param relayAllCalls                       whether all calls are relayed through WhatsApp servers
     * @param externalWebBeta                     whether the user has opted into the external web beta
     * @param chatLockSettings                    the chat lock settings, may be {@code null}
     * @param favoriteChats                       the ordered list of favourite chat JIDs, may be {@code null}
     * @param primaryFeatures                     the primary device feature flags, may be {@code null}
     * @param mentionEveryoneMuteExpirations      the mention-everyone mute expirations, may be {@code null}
     * @param orphanMutationEntries               the orphan mutation entries map
     * @param outContacts                         the outgoing contact map, may be {@code null}
     * @param clockSkewSeconds                    the server-vs-local clock skew in seconds
     * @param groupAbPropsEmergencyPushTimestamp  the timestamp of the last group AB-props push
     * @param abPropsAbKey                        the AB-props {@code abKey}, may be {@code null}
     * @param abPropsHash                         the AB-props hash, may be {@code null}
     * @param abPropsRefresh                      the AB-props refresh interval in seconds
     * @param abPropsLastSyncTime                 the AB-props last sync timestamp
     * @param abPropsRefreshId                    the AB-props refresh id
     * @param abPropsWebRefreshId                 the web-only AB-props refresh id
     * @param groupAbPropsRefreshId               the group AB-props refresh id
     * @param baseKeys                            the X3DH base-key dedupe map, may be {@code null}
     * @param chats                               the chat map keyed by JID
     * @param newsletters                         the newsletter map keyed by JID
     */
    InMemoryStore(
            UUID uuid, Long phoneNumber, WhatsAppClientType clientType, Instant initializationTimeStamp, WhatsAppDevice device, ClientPayload.ClientReleaseChannel releaseChannel, boolean online, String locale, String name, String verifiedName, URI profilePicture, String about, Jid jid, Jid lid, String businessAddress, Double businessLongitude, Double businessLatitude, String businessDescription, String businessWebsite, String businessEmail, BusinessCategory businessCategory, ConcurrentHashMap<Jid, Contact> contacts, ConcurrentHashMap<String, CallOffer> calls, ConcurrentHashMap<PrivacySettingType, PrivacySettingEntry> privacySettings, boolean unarchiveChats, boolean twentyFourHourFormat, ChatEphemeralTimer newChatsEphemeralTimer, WhatsAppWebClientHistory webHistoryPolicy, boolean automaticPresenceUpdates, boolean automaticMessageReceipts, boolean checkPatchMacs, boolean syncedChats, boolean syncedContacts, boolean syncedNewsletters, boolean syncedStatus, boolean syncedWebAppState, boolean syncedBusinessCertificate, Integer registrationId, SignalIdentityKeyPair noiseKeyPair, SignalIdentityKeyPair identityKeyPair, ADVSignedDeviceIdentity signedDeviceIdentity, SignalSignedKeyPair signedKeyPair, LinkedHashMap<Integer, SignalPreKeyPair> preKeys, UUID fdid, byte[] deviceId, UUID advertisingId, byte[] identityId, byte[] backupToken, ConcurrentMap<SignalSenderKeyName, SignalSenderKeyRecord> senderKeys, LinkedHashMap<String, AppStateSyncKey> appStateKeys, ConcurrentMap<SignalProtocolAddress, SignalSessionRecord> sessions, ConcurrentMap<SyncPatchType, SyncHashValue> hashStates, boolean registered, boolean showSecurityNotifications, ConcurrentMap<String, Sticker> recentStickers, ConcurrentMap<String, Sticker> favouriteStickers, ConcurrentMap<String, QuickReply> quickReplies, ConcurrentMap<String, Label> labels, ClientAppVersion clientVersion, ClientAppVersion companionVersion, Instant lastAdvCheckTime, ConcurrentMap<SignalProtocolAddress, SignalIdentityPublicKey> remoteIdentities, ConcurrentMap<String, MissingDeviceSyncKey> missingSyncKeys, byte[] advSecretKey, ConcurrentMap<Jid, BusinessVerifiedName> verifiedBusinessNames, Path directory, boolean primaryDeviceSupportsSyncdRecovery, boolean disableLinkPreviews, boolean relayAllCalls, boolean externalWebBeta, ChatLockSettings chatLockSettings, List<Jid> favoriteChats, List<String> primaryFeatures, ConcurrentMap<Jid, ChatMute> mentionEveryoneMuteExpirations, ConcurrentMap<SyncPatchType, AbstractWhatsAppStore.OrphanMutationEntries> orphanMutationEntries, ConcurrentHashMap<Jid, OutContact> outContacts, long clockSkewSeconds, Instant groupAbPropsEmergencyPushTimestamp, String abPropsAbKey, String abPropsHash, long abPropsRefresh, Instant abPropsLastSyncTime, long abPropsRefreshId, long abPropsWebRefreshId, long groupAbPropsRefreshId, ConcurrentMap<String, byte[]> baseKeys,
            ConcurrentHashMap<Jid, Chat> chats,
            ConcurrentHashMap<Jid, Newsletter> newsletters
    ) {
        super(uuid, phoneNumber, clientType, initializationTimeStamp, device, releaseChannel, online, locale, name, verifiedName, profilePicture, about, jid, lid, businessAddress, businessLongitude, businessLatitude, businessDescription, businessWebsite, businessEmail, businessCategory, contacts, calls, privacySettings, unarchiveChats, twentyFourHourFormat, newChatsEphemeralTimer, webHistoryPolicy, automaticPresenceUpdates, automaticMessageReceipts, checkPatchMacs, syncedChats, syncedContacts, syncedNewsletters, syncedStatus, syncedWebAppState, syncedBusinessCertificate, registrationId, noiseKeyPair, identityKeyPair, signedDeviceIdentity, signedKeyPair, preKeys, fdid, deviceId, advertisingId, identityId, backupToken, senderKeys, appStateKeys, sessions, hashStates, registered, showSecurityNotifications, recentStickers, favouriteStickers, quickReplies, labels, clientVersion, companionVersion, lastAdvCheckTime, remoteIdentities, missingSyncKeys, advSecretKey, verifiedBusinessNames, directory, primaryDeviceSupportsSyncdRecovery, disableLinkPreviews, relayAllCalls, externalWebBeta, chatLockSettings, favoriteChats, primaryFeatures, mentionEveryoneMuteExpirations, orphanMutationEntries, outContacts, clockSkewSeconds, groupAbPropsEmergencyPushTimestamp, abPropsAbKey, abPropsHash, abPropsRefresh, abPropsLastSyncTime, abPropsRefreshId, abPropsWebRefreshId, groupAbPropsRefreshId, baseKeys);
        this.chats = chats;
        this.newsletters = newsletters;
        this.status = new ConcurrentHashMap<>();
        this.jidsHashCodes = new ConcurrentHashMap<>();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Compares the live store hash code against the value captured at the
     * last save. When the store has changed, dispatches one virtual-thread
     * task per chat, per newsletter, and one for the root store, then waits
     * for all of them to complete via the auto-closing executor.
     */
    @Override
    public WhatsAppStore save() {
        var newHashCode = hashCode();
        if (storeHashCode == null || storeHashCode != newHashCode) {
            synchronized (this) {
                if(storeHashCode == null || storeHashCode != newHashCode) {
                    storeHashCode = newHashCode;
                    try (var executor = newVirtualThreadPerTaskExecutor()) {
                        executor.submit(() -> {
                            try {
                                serializeStore();
                            }catch (Throwable throwable) {
                                logger.log(WARNING, "Error while serializing store", throwable);
                            }
                        });

                        chats.forEach((_, chat) -> executor.submit(() -> {
                            try {
                                serializeChat(chat);
                            } catch (Throwable error) {
                                logger.log(WARNING, "Error while serializing chat", error);
                            }
                        }));

                        newsletters.forEach((_, newsletter) -> executor.submit(() -> {
                            try {
                                serializeNewsletter(newsletter);
                            } catch (Throwable error) {
                                logger.log(WARNING, "Error while serializing newsletter", error);
                            }
                        }));
                    }

                }
            }
        }
        return this;
    }

    /**
     * Serialises the root store to {@code store.proto} via a temp file and
     * an atomic move.
     *
     * @throws IOException if the file cannot be created, written or moved
     */
    private void serializeStore() throws IOException {
        var path = StorePathUtils.getSessionFile(clientType, directory, uuid.toString(), "store.proto");
        Files.createDirectories(path.getParent());
        var tempFile = Files.createTempFile(path.getFileName().toString(), ".tmp");
        try (var stream = Files.newOutputStream(tempFile)) {
            InMemoryStoreSpec.encode(this, ProtobufOutputStream.toStream(stream));
        }
        Files.move(tempFile, path, StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public void delete() throws IOException {
        var folderPath = StorePathUtils.getSessionDirectory(clientType, directory, uuid.toString());
        StorePathUtils.deleteRecursively(folderPath);
    }

    @Override
    public void await() {
        var thread = attributionThread;
        if (thread == null) {
            return;
        }
        try {
            thread.join();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Cannot finish deserializing store", exception);
        }
    }

    /**
     * Spawns the virtual thread that lazily deserialises every per-chat and
     * per-newsletter file under the session directory. Idempotent: subsequent
     * calls do nothing once the worker has been started.
     */
    void startBackgroundDeserialization() {
        if(attributionThread == null) {
            synchronized (this) {
                if(attributionThread == null) {
                    attributionThread = Thread.startVirtualThread(this::deserializeChatsAndNewsletters);
                }
            }
        }
    }

    /**
     * Walks the session directory and dispatches one deserialisation task
     * per file matching the chat or newsletter naming convention.
     */
    private void deserializeChatsAndNewsletters() {
        try {
            var sessionDirectory = StorePathUtils.getSessionDirectory(clientType, directory, uuid.toString());
            try (var walker = Files.walk(sessionDirectory); var executor = newVirtualThreadPerTaskExecutor()) {
                walker.forEach(path -> executor.submit(() -> {
                    try {
                        deserializeChatOrNewsletter(path);
                    } catch (IOException throwable) {
                        logger.log(ERROR, throwable);
                    }
                }));
            }
        } catch (Throwable throwable) {
            logger.log(ERROR, throwable);
        }
    }

    /**
     * Dispatches a single file to the chat or newsletter deserialiser based
     * on its file-name prefix, ignoring files that do not match either.
     *
     * @param path the file to inspect
     * @throws IOException if the file cannot be read or decoded
     */
    private void deserializeChatOrNewsletter(Path path) throws IOException {
        var fileName = path.getFileName().toString();
        if (fileName.startsWith(CHAT_PREFIX)) {
            deserializeChat(path);
        } else if (fileName.startsWith(NEWSLETTER_PREFIX)) {
            deserializeNewsletter(path);
        }
    }

    /**
     * Decodes one chat protobuf file and inserts it into {@link #chats},
     * recording its hash for the dirty-checking save path.
     *
     * @param chatFile the chat protobuf file to decode
     * @throws IOException if the file cannot be read or decoded
     */
    private void deserializeChat(Path chatFile) throws IOException {
        try (var stream = Files.newInputStream(chatFile)) {
            var chat = InMemoryStoreChatSpec.decode(ProtobufInputStream.fromStream(stream));
            var storeJidPair = new StoreJidPair(uuid, chat.jid());
            jidsHashCodes.put(storeJidPair, chat.hashCode());
            chats.put(chat.jid(), chat);
        }
    }

    /**
     * Decodes one newsletter protobuf file and inserts it into
     * {@link #newsletters}, recording its hash for the dirty-checking save
     * path.
     *
     * @param newsletterFile the newsletter protobuf file to decode
     * @throws IOException if the file cannot be read or decoded
     */
    private void deserializeNewsletter(Path newsletterFile) throws IOException {
        try (var stream = Files.newInputStream(newsletterFile)) {
            var newsletter = InMemoryStoreNewsletterSpec.decode(ProtobufInputStream.fromStream(stream));
            var storeJidPair = new StoreJidPair(uuid, newsletter.jid());
            jidsHashCodes.put(storeJidPair, newsletter.hashCode());
            newsletters.put(newsletter.jid(), newsletter);
        }
    }

    /**
     * Serialises the given chat to its dedicated protobuf file when its hash
     * has changed since the last save. Writes go through a temp file followed
     * by an atomic move.
     *
     * @param chat the chat to serialise
     * @throws IOException if the file cannot be created, written or moved
     */
    private void serializeChat(Chat chat) throws IOException {
        var outputFile = getMessagesContainerPathIfUpdated(chat.jid(), chat.hashCode(), CHAT_PREFIX);
        if (outputFile == null) {
            return;
        }

        var tempFile = Files.createTempFile(outputFile.getFileName().toString(), ".tmp");
        try (var stream = Files.newOutputStream(tempFile)) {
            InMemoryStoreChatSpec.encode(chat, ProtobufOutputStream.toStream(stream));
        }
        Files.move(tempFile, outputFile, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Serialises the given newsletter to its dedicated protobuf file when its
     * hash has changed since the last save. Writes go through a temp file
     * followed by an atomic move.
     *
     * @param newsletter the newsletter to serialise
     * @throws IOException if the file cannot be created, written or moved
     */
    private void serializeNewsletter(Newsletter newsletter) throws IOException {
        var outputFile = getMessagesContainerPathIfUpdated(newsletter.jid(), newsletter.hashCode(), NEWSLETTER_PREFIX);
        if(outputFile == null) {
            return;
        }

        var tempFile = Files.createTempFile(outputFile.getFileName().toString(), ".tmp");
        try (var stream = Files.newOutputStream(tempFile)) {
            InMemoryStoreNewsletterSpec.encode(newsletter, ProtobufOutputStream.toStream(stream));
        }
        Files.move(tempFile, outputFile, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Returns the destination path for the given chat or newsletter when its
     * content has changed since the last save, or {@code null} when the
     * captured hash matches.
     *
     * @param jid        the chat or newsletter JID
     * @param hashCode   the current hash code of the entity
     * @param filePrefix the file-name prefix
     * @return the destination path, or {@code null} when the entity is up to date
     * @throws IOException if the path cannot be resolved
     */
    private Path getMessagesContainerPathIfUpdated(Jid jid, int hashCode, String filePrefix) throws IOException {
        var identifier = new StoreJidPair(uuid, jid);
        var oldHashCode = jidsHashCodes.getOrDefault(identifier, -1);
        if (oldHashCode == hashCode) {
            return null;
        }
        jidsHashCodes.put(identifier, hashCode);
        var fileName = filePrefix + jid.user() + ".proto";
        return StorePathUtils.getSessionFile(clientType, directory, uuid.toString(), fileName);
    }

    /**
     * Logs an error encountered during serialisation at the {@code ERROR}
     * level.
     *
     * @param error the error to log
     */
    private void handleSerializeError(Throwable error) {

        logger.log(ERROR, error);
    }

    /**
     * Composite key identifying a chat or newsletter entry in
     * {@link #jidsHashCodes}.
     *
     * @param storeId the owning session UUID
     * @param jid     the chat or newsletter JID
     */
    private record StoreJidPair(UUID storeId, Jid jid) {

    }

    @Override
    public Optional<com.github.auties00.cobalt.model.chat.Chat> findChatByJid(JidProvider jid) {
        return switch (jid) {
            case null -> Optional.empty();
            case com.github.auties00.cobalt.model.chat.Chat chat -> Optional.of(chat);
            case Contact _, com.github.auties00.cobalt.model.newsletter.Newsletter _, Jid _, JidServer _-> {
                var targetJid = jid.toJid();
                if(targetJid.hasUserServer()) {
                    var jidChat = chats.get(targetJid);
                    if(jidChat != null) {
                        yield Optional.of(jidChat);
                    } else {
                        yield findLidByPhone(targetJid)
                                .map(chats::get);
                    }
                } else if(targetJid.hasLidServer()) {
                    var lidChat = chats.get(targetJid);
                    if(lidChat != null) {
                        yield Optional.of(lidChat);
                    } else {
                        // Multi-hop: oldLid → PN → chat, or oldLid → PN → currentLid → chat
                        var phone = findPhoneByLid(targetJid);
                        if(phone.isEmpty()) {
                            yield Optional.empty();
                        }
                        var phoneChat = chats.get(phone.get());
                        if(phoneChat != null) {
                            yield Optional.of(phoneChat);
                        }
                        yield findLidByPhone(phone.get())
                                .map(chats::get);
                    }
                } else {
                    var chat = chats.get(targetJid);
                    yield Optional.ofNullable(chat);
                }
            }
        };
    }

    @Override
    public Optional<? extends MessageInfo> findMessageById(JidProvider provider, String id) {
        return provider == null || id == null ? Optional.empty() : switch (provider) {
            case com.github.auties00.cobalt.model.chat.Chat chat -> findMessageById(chat, id);
            case com.github.auties00.cobalt.model.newsletter.Newsletter newsletter -> findMessageById(newsletter, id);
            case Contact contact -> findChatByJid(contact.jid())
                    .flatMap(chat -> findMessageById(chat, id));
            case Jid contactJid -> {
                if (contactJid.server().type() == JidServer.Type.NEWSLETTER) {
                    yield findNewsletterByJid(contactJid)
                            .flatMap(newsletter -> findMessageById(newsletter, id));
                } else if (Jid.statusBroadcastAccount().equals(contactJid)) {
                    yield Optional.ofNullable(status.get(id));
                } else {
                    yield findChatByJid(contactJid)
                            .flatMap(chat -> findMessageById(chat, id));
                }
            }
            case JidServer jidServer -> findChatByJid(jidServer.toJid())
                    .flatMap(chat -> findMessageById(chat, id));
        };
    }

    @Override
    public Optional<NewsletterMessageInfo> findMessageById(com.github.auties00.cobalt.model.newsletter.Newsletter newsletter, String id) {
        return newsletter == null || id == null ? Optional.empty() : newsletter.messages()
                .parallelStream()
                .filter(entry -> Objects.equals(id, entry.key().id().orElse(null)) || Objects.equals(id, String.valueOf(entry.serverId())))
                .findFirst();
    }

    @Override
    public Optional<ChatMessageInfo> findMessageById(com.github.auties00.cobalt.model.chat.Chat chat, String id) {
        return chat == null || id == null ? Optional.empty() : chat.messages()
                .parallelStream()
                .filter(message -> Objects.equals(message.key().id().orElse(null), id))
                .findAny();
    }

    @Override
    public Collection<com.github.auties00.cobalt.model.chat.Chat> chats() {
        return Collections.unmodifiableCollection(chats.values());
    }

    @Override
    public com.github.auties00.cobalt.model.chat.Chat addNewChat(Jid chatJid) {
        Objects.requireNonNull(chatJid, "chatJid cannot be null");
        var chat = new InMemoryStoreChatBuilder()
                .jid(chatJid)
                .build();
        chats.put(chatJid, chat);
        return chat;
    }

    @Override
    public Optional<com.github.auties00.cobalt.model.chat.Chat> removeChat(JidProvider chatJid) {
        if(chatJid == null) {
            return Optional.empty();
        } else {
            var targetJid = chatJid.toJid();
            if(targetJid.hasUserServer()) {
                var jidChat = chats.remove(targetJid);
                if(jidChat != null) {
                    return Optional.of(jidChat);
                } else {
                    return findLidByPhone(targetJid)
                            .map(chats::remove);
                }
            } else if(targetJid.hasLidServer()) {
                var lidChat = chats.remove(targetJid);
                if(lidChat != null) {
                    return Optional.of(lidChat);
                } else {
                    return findPhoneByLid(targetJid)
                            .map(chats::remove);
                }
            } else {
                var chat = chats.remove(targetJid);
                return Optional.ofNullable(chat);
            }
        }
    }

    @Override
    public ChatMessageInfo addStatus(ChatMessageInfo messageInfo) {
        Objects.requireNonNull(messageInfo, "messageInfo cannot be null");
        messageInfo.key().id().ifPresent(id -> status.put(id, messageInfo));
        return messageInfo;
    }

    @Override
    public Optional<ChatMessageInfo> removeStatus(String id) {
        return Optional.ofNullable(status.remove(id));
    }

    @Override
    public Optional<com.github.auties00.cobalt.model.newsletter.Newsletter> findNewsletterByJid(JidProvider jid) {
        return jid == null
                ? Optional.empty()
                : Optional.ofNullable(newsletters.get(jid.toJid()));
    }

    @Override
    public Collection<com.github.auties00.cobalt.model.newsletter.Newsletter> newsletters() {
        return Collections.unmodifiableCollection(newsletters.values());
    }

    @Override
    public com.github.auties00.cobalt.model.newsletter.Newsletter addNewNewsletter(Jid newsletterJid) {
        Objects.requireNonNull(newsletterJid, "newsletterJid cannot be null");
        var newsletter = new InMemoryStoreNewsletterBuilder()
                .jid(newsletterJid)
                .build();
        newsletters.put(newsletter.jid(), newsletter);
        return newsletter;
    }

    @Override
    public Optional<com.github.auties00.cobalt.model.newsletter.Newsletter> removeNewsletter(JidProvider newsletterJid) {
        return newsletterJid == null
                ? Optional.empty()
                : Optional.ofNullable(newsletters.remove(newsletterJid.toJid()));
    }

    @Override
    public Optional<? extends MessageInfo> findMessageByKey(MessageKey key) {
        var id = key.id();
        if(id.isEmpty()) {
            return Optional.empty();
        }

        var parentJid = key.parentJid();
        if(parentJid.isEmpty()) {
            return Optional.empty();
        }

        return findChatByJid(parentJid.get())
                .flatMap(chat -> chat.getMessageById(id.get()));
    }

    @Override
    public Collection<ChatMessageInfo> status() {
        return Collections.unmodifiableCollection(status.values());
    }

    @Override
    public boolean equals(Object o) {
        return o == this || o instanceof InMemoryStore that
                            && super.equals(o)
                            && Objects.equals(this.chats, that.chats)
                            && Objects.equals(this.newsletters, that.newsletters)
                            && Objects.equals(this.status, that.status);
    }

    @Override
    public int hashCode() {
        var hashCode = super.hashCode();
        for(var entry : chats.entrySet()) {
            var value = entry.getValue();
            if(value != null) {
                hashCode = hashCode * 31 + value.hashCode();
            }
        }
        for(var entry : newsletters.entrySet()) {
            var value = entry.getValue();
            if(value != null) {
                hashCode = hashCode * 31 + value.hashCode();
            }
        }
        for(var entry : status.entrySet()) {
            var value = entry.getValue();
            if(value != null) {
                hashCode = hashCode * 31 + value.hashCode();
            }
        }
        return hashCode;
    }

    /**
     * Chat variant used by {@link InMemoryStore} that drops the in-memory
     * messages list. Per-chat messages are read on demand from the dedicated
     * protobuf file rather than being kept resident.
     */
    @ProtobufMessage
    static final class Chat extends com.github.auties00.cobalt.model.chat.Chat {
        /**
         * Constructs a new in-memory chat with no in-memory message backing.
         * Forwards every chat-level field to the
         * {@link com.github.auties00.cobalt.model.chat.Chat} super constructor.
         */
        Chat(Jid jid, Jid newJid, Jid oldJid, Instant lastMsgTimestamp, Integer unreadCount, Boolean readOnly, Boolean endOfHistoryTransfer, ChatEphemeralTimer ephemeralExpiration, Instant ephemeralSettingTimestamp, EndOfHistoryTransferType endOfHistoryTransferType, Instant conversationTimestamp, String name, String pHash, Boolean notSpam, Boolean archived, ChatDisappearingMode disappearingMode, Integer unreadMentionCount, Boolean markedAsUnread, List<GroupParticipant> participant, byte[] tcToken, Instant tcTokenTimestamp, byte[] contactPrimaryIdentityKey, Instant pinned, ChatMute mute, WallpaperSettings wallpaper, MediaVisibility mediaVisibility, Instant tcTokenSenderTimestamp, Boolean suspended, Boolean terminated, Instant createdAt, String createdBy, String description, Boolean support, Boolean isParentGroup, String parentGroupId, Boolean isDefaultSubgroup, String displayName, Jid phoneNumberJid, Boolean shareOwnPhoneNumber, Boolean phoneNumberhDuplicateLidThread, Jid lid, String username, String lidOriginType, Integer commentsCount, Boolean locked, PrivacySystemMessage systemMessageToInsert, Boolean capiCreatedGroup, Jid accountLid, Boolean limitSharing, Instant limitSharingSettingTimestamp, ChatLimitSharing.TriggerType limitSharingTrigger, Boolean limitSharingInitiatedByMe, Boolean maibaAiThreadEnabled) {
            super(jid, newJid, oldJid, lastMsgTimestamp, unreadCount, readOnly, endOfHistoryTransfer, ephemeralExpiration, ephemeralSettingTimestamp, endOfHistoryTransferType, conversationTimestamp, name, pHash, notSpam, archived, disappearingMode, unreadMentionCount, markedAsUnread, participant, tcToken, tcTokenTimestamp, contactPrimaryIdentityKey, pinned, mute, wallpaper, mediaVisibility, tcTokenSenderTimestamp, suspended, terminated, createdAt, createdBy, description, support, isParentGroup, parentGroupId, isDefaultSubgroup, displayName, phoneNumberJid, shareOwnPhoneNumber, phoneNumberhDuplicateLidThread, lid, username, lidOriginType, commentsCount, locked, systemMessageToInsert, capiCreatedGroup, accountLid, limitSharing, limitSharingSettingTimestamp, limitSharingTrigger, limitSharingInitiatedByMe, maibaAiThreadEnabled);
        }

        @Override
        public SequencedCollection<ChatMessageInfo> messages() {
            return List.of();
        }

        @Override
        public void addMessage(ChatMessageInfo info) {
            Objects.requireNonNull(info);
        }

        @Override
        public boolean removeMessage(String id) {
            return false;
        }

        @Override
        public void removeMessages() {

        }

        @Override
        public Optional<ChatMessageInfo> getMessageById(String id) {
            return Optional.empty();
        }

        @Override
        public Optional<ChatMessageInfo> newestMessage() {
            return Optional.empty();
        }

        @Override
        public Optional<ChatMessageInfo> oldestMessage() {
            return Optional.empty();
        }
    }

    /**
     * Newsletter variant used by {@link InMemoryStore} that drops the
     * in-memory messages list. Per-newsletter messages are read on demand
     * from the dedicated protobuf file rather than being kept resident.
     */
    @ProtobufMessage
    static final class Newsletter extends com.github.auties00.cobalt.model.newsletter.Newsletter {
        /**
         * Constructs a new in-memory newsletter with no in-memory message
         * backing. Forwards every newsletter-level field to the
         * {@link com.github.auties00.cobalt.model.newsletter.Newsletter} super
         * constructor.
         */
        Newsletter(Jid jid, NewsletterState state, NewsletterMetadata metadata, NewsletterViewerMetadata viewerMetadata, int unreadMessagesCount, Instant timestamp) {
            super(jid, state, metadata, viewerMetadata, unreadMessagesCount, timestamp);
        }

        @Override
        public void addMessage(NewsletterMessageInfo info) {
            Objects.requireNonNull(info, "info cannot be null");
        }

        @Override
        public boolean removeMessage(String messageId) {
            return false;
        }

        @Override
        public void removeMessages() {

        }

        @Override
        public SequencedCollection<NewsletterMessageInfo> messages() {
            return List.of();
        }

        @Override
        public Optional<NewsletterMessageInfo> getMessageById(String messageId) {
            return Optional.empty();
        }

        @Override
        public Optional<NewsletterMessageInfo> oldestMessage() {
            return Optional.empty();
        }

        @Override
        public Optional<NewsletterMessageInfo> newestMessage() {
            return Optional.empty();
        }
    }

}
