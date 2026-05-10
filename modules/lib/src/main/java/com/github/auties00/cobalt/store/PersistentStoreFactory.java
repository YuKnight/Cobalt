package com.github.auties00.cobalt.store;

import com.github.auties00.cobalt.client.WhatsAppClientSixPartsKeys;
import com.github.auties00.cobalt.client.WhatsAppClientType;
import com.github.auties00.cobalt.client.WhatsAppDevice;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.util.StorePathUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Factory implementation that persists session metadata to a single
 * {@code store.proto} file and offloads message bodies to an embedded
 * {@link PersistentMessageStore LMDB} environment under each session directory.
 *
 * <p>Each session lives under
 * {@code <baseDirectory>/<clientType>/<sessionId>/} and contains:
 * <ul>
 *   <li>{@code store.proto} — metadata snapshot for chats, newsletters,
 *       contacts, sync state, Signal-protocol state and AB-props.
 *   <li>{@code messages.lmdb/} — the LMDB env file pair holding chat
 *       messages, newsletter messages and the global status feed.
 * </ul>
 *
 * <p>On {@link #load} the factory deserialises the snapshot synchronously
 * and then walks the LMDB env once to recover orphan chats or
 * newsletters whose messages were committed but whose metadata did not
 * survive the post-commit window. Recovered entries are surfaced via the
 * normal {@link WhatsAppStore#chats()}
 * and {@link WhatsAppStore#newsletters()}
 * collections so callers see the same shape regardless of whether the
 * last shutdown was clean or crashy.
 */
final class PersistentStoreFactory implements WhatsAppStoreFactory {
    /**
     * Default root directory for Cobalt persistent sessions:
     * {@code $HOME/.cobalt/proto}.
     */
    private static final Path DEFAULT_DIRECTORY = Path.of(System.getProperty("user.home"), ".cobalt", "proto");

    /**
     * Default LMDB map size — 256&nbsp;MiB. On Windows this is the
     * preallocated sparse file size, so anything much larger looks
     * alarming in Explorer; the {@link PersistentMessageStore} doubles this on
     * {@code MDB_MAP_FULL} so the cap rises automatically when needed.
     */
    private static final long DEFAULT_MAP_SIZE = 256L * 1024 * 1024;

    /**
     * Root directory under which per-session folders are created.
     */
    private final Path directory;

    /**
     * Initial LMDB map size for newly opened envs.
     */
    private final long mapSize;

    /**
     * Constructs a new factory using the default storage directory and
     * the default map size.
     */
    PersistentStoreFactory() {
        this(DEFAULT_DIRECTORY, DEFAULT_MAP_SIZE);
    }

    /**
     * Constructs a new factory using the given root directory and the
     * default map size.
     *
     * @param directory the root directory under which per-session
     *                  folders are created; must not be {@code null}
     */
    PersistentStoreFactory(Path directory) {
        this(directory, DEFAULT_MAP_SIZE);
    }

    /**
     * Constructs a new factory using the given root directory and
     * initial map size.
     *
     * @param directory the root directory under which per-session
     *                  folders are created; must not be {@code null}
     * @param mapSize   the initial LMDB map size in bytes; must be
     *                  positive
     */
    PersistentStoreFactory(Path directory, long mapSize) {
        if (mapSize <= 0) {
            throw new IllegalArgumentException("mapSize must be positive");
        }
        this.directory = Objects.requireNonNull(directory, "directory cannot be null");
        this.mapSize = mapSize;
    }

    @Override
    public Optional<WhatsAppStore> load(WhatsAppClientType clientType, UUID uuid) throws IOException {
        Objects.requireNonNull(clientType, "clientType cannot be null");
        Objects.requireNonNull(uuid, "uuid cannot be null");
        return loadSession(clientType, uuid.toString());
    }

    @Override
    public Optional<WhatsAppStore> load(WhatsAppClientType clientType, long phoneNumber) throws IOException {
        Objects.requireNonNull(clientType, "clientType cannot be null");
        return loadSession(clientType, String.valueOf(phoneNumber));
    }

    @Override
    public Optional<WhatsAppStore> loadLatest(WhatsAppClientType clientType) throws IOException {
        Objects.requireNonNull(clientType, "clientType cannot be null");
        var latest = StorePathUtils.getLatestSessionDirectory(clientType, directory);
        if (latest.isEmpty()) {
            return Optional.empty();
        }
        return loadSession(clientType, latest.get().getFileName().toString());
    }

    /**
     * Loads the session identified by {@code sessionId} for the given
     * client type, opens its LMDB env, attaches it to the deserialised
     * store and runs the orphan-recovery pass.
     *
     * @param clientType the client type
     * @param sessionId  the session uuid string or phone-number string
     * @return the loaded store, or {@link Optional#empty()} if no
     *         {@code store.proto} exists for that session
     * @throws IOException if the metadata file cannot be read or
     *                     decoded
     */
    private Optional<WhatsAppStore> loadSession(WhatsAppClientType clientType, String sessionId) throws IOException {
        var storeFile = PersistentStore.storeFilePath(clientType, directory, sessionId);
        if (Files.notExists(storeFile)) {
            return Optional.empty();
        }
        var store = PersistentStore.deserialize(storeFile);
        var envPath = PersistentStore.messagesEnvPath(clientType, directory, sessionId);
        var messageStore = PersistentMessageStore.open(envPath, mapSize);
        store.attachMessageStore(messageStore);
        recoverOrphans(store, messageStore);
        return Optional.of(store);
    }

    /**
     * Inserts metadata stubs for every chat or newsletter that holds
     * messages in {@code messageStore} but has no corresponding entry in
     * the deserialised metadata snapshot. Bridges the post-commit
     * window where an LMDB write succeeded but the next metadata save
     * never happened (e.g. process killed between the two).
     *
     * @param store         the freshly attached store
     * @param messageStore  the just-opened LMDB facade
     */
    private static void recoverOrphans(PersistentStore store, PersistentMessageStore messageStore) {
        for (var chatJid : messageStore.distinctChatJids()) {
            if (!store.chats.containsKey(chatJid)) {
                store.addNewChat(chatJid);
            }
        }
        for (var newsletterJid : messageStore.distinctNewsletterJids()) {
            if (!store.newsletters.containsKey(newsletterJid)) {
                store.addNewNewsletter(newsletterJid);
            }
        }
    }

    @Override
    public WhatsAppStore create(WhatsAppClientType clientType, UUID uuid) throws IOException {
        Objects.requireNonNull(clientType, "clientType cannot be null");
        var resolvedUuid = Objects.requireNonNullElseGet(uuid, UUID::randomUUID);
        var sessionId = resolvedUuid.toString();
        var sessionDirectory = StorePathUtils.getSessionDirectory(clientType, directory, sessionId);
        var store = new PersistentStoreBuilder()
                .uuid(resolvedUuid)
                .clientType(clientType)
                .device(defaultDevice(clientType))
                .directory(sessionDirectory)
                .build();
        attachFreshLmdb(store, clientType, sessionId);
        return store;
    }

    @Override
    public WhatsAppStore create(WhatsAppClientType clientType, long phoneNumber) throws IOException {
        Objects.requireNonNull(clientType, "clientType cannot be null");
        var sessionId = String.valueOf(phoneNumber);
        var sessionDirectory = StorePathUtils.getSessionDirectory(clientType, directory, sessionId);
        var store = new PersistentStoreBuilder()
                .uuid(UUID.randomUUID())
                .phoneNumber(phoneNumber)
                .clientType(clientType)
                .device(defaultDevice(clientType))
                .directory(sessionDirectory)
                .build();
        attachFreshLmdb(store, clientType, sessionId);
        return store;
    }

    @Override
    public WhatsAppStore create(WhatsAppClientType clientType, WhatsAppClientSixPartsKeys sixPartsKeys) throws IOException {
        Objects.requireNonNull(clientType, "clientType cannot be null");
        Objects.requireNonNull(sixPartsKeys, "sixPartsKeys cannot be null");
        var phoneNumber = sixPartsKeys.phoneNumber();
        var sessionId = String.valueOf(phoneNumber);
        var sessionDirectory = StorePathUtils.getSessionDirectory(clientType, directory, sessionId);
        var store = new PersistentStoreBuilder()
                .directory(sessionDirectory)
                .uuid(UUID.randomUUID())
                .phoneNumber(phoneNumber)
                .noiseKeyPair(sixPartsKeys.noiseKeyPair())
                .identityKeyPair(sixPartsKeys.identityKeyPair())
                .identityId(sixPartsKeys.identityId())
                .clientType(clientType)
                .device(WhatsAppDevice.web())
                .registered(true)
                .jid(Jid.of(phoneNumber))
                .build();
        attachFreshLmdb(store, clientType, sessionId);
        return store;
    }

    /**
     * Opens a fresh LMDB env for {@code sessionId} and wires it into
     * {@code store}. Used by every {@code create(...)} overload after
     * the metadata builder produces an empty store.
     *
     * @param store      the freshly built store
     * @param clientType the client type
     * @param sessionId  the session uuid string or phone-number string
     * @throws IOException if the env directory cannot be created
     */
    private void attachFreshLmdb(PersistentStore store, WhatsAppClientType clientType, String sessionId) throws IOException {
        var envPath = PersistentStore.messagesEnvPath(clientType, directory, sessionId);
        store.attachMessageStore(PersistentMessageStore.open(envPath, mapSize));
    }

    /**
     * Returns the synthetic device descriptor used for newly created
     * sessions of the given client type.
     *
     * @param clientType the client type
     * @return a fresh {@link WhatsAppDevice} suitable for the type
     */
    private static WhatsAppDevice defaultDevice(WhatsAppClientType clientType) {
        return switch (clientType) {
            case WEB -> WhatsAppDevice.desktop();
            case MOBILE -> WhatsAppDevice.ios(false);
        };
    }
}
