package com.github.auties00.cobalt.store;

import com.github.auties00.cobalt.client.WhatsAppClientSixPartsKeys;
import com.github.auties00.cobalt.client.WhatsAppClientType;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

/**
 * Factory contract for constructing and loading {@link WhatsAppStore}
 * instances.
 *
 * <p>Each factory encapsulates a specific storage strategy (RAM only,
 * or metadata snapshot on disk plus an LMDB env for messages) and
 * exposes a uniform API to create a brand-new session store or to load
 * an existing one by UUID, by phone number, or by most recently used
 * selection.
 *
 * <p>Concrete factories are obtained via the static factory methods on
 * this interface. Callers should not depend on the concrete factory
 * types directly.
 */
public interface WhatsAppStoreFactory {
    /**
     * Returns a factory that keeps the entire store in RAM and never
     * touches disk. Restarting the program loses every chat,
     * newsletter, message and key. Suitable for tests, ephemeral bots
     * and scratch programs.
     *
     * @return a new RAM-only factory
     */
    static WhatsAppStoreFactory temporary() {
        return TemporaryStoreFactory.INSTANCE;
    }

    /**
     * Returns a factory that snapshots session metadata to a single
     * {@code store.proto} file and stores message bodies in an embedded
     * LMDB environment under the default Cobalt session directory in
     * the user's home directory.
     *
     * @return a persistent factory using the default storage directory
     */
    static WhatsAppStoreFactory persistent() {
        return new PersistentStoreFactory();
    }

    /**
     * Returns a factory that snapshots session metadata to a single
     * {@code store.proto} file and stores message bodies in an embedded
     * LMDB environment under the given directory.
     *
     * @param directory the root directory under which per-session
     *                  folders are created
     * @return a persistent factory using the given storage directory
     */
    static WhatsAppStoreFactory persistent(Path directory) {
        return new PersistentStoreFactory(directory);
    }

    /**
     * Returns a factory that snapshots session metadata to a single
     * {@code store.proto} file and stores message bodies in an embedded
     * LMDB environment under the given directory, configured with the
     * given initial map size.
     *
     * <p>The map size is the maximum LMDB env file size (in bytes); the
     * env is automatically doubled on overflow, so this value functions
     * as a starting point rather than a hard cap. On Windows the file
     * is preallocated as sparse, so very large defaults can look
     * alarming in Explorer.
     *
     * @param directory the root directory under which per-session
     *                  folders are created
     * @param mapSize   the initial LMDB map size in bytes; must be
     *                  positive
     * @return a persistent factory using the given storage directory
     *         and map size
     */
    static WhatsAppStoreFactory persistent(Path directory, long mapSize) {
        return new PersistentStoreFactory(directory, mapSize);
    }

    /**
     * Loads an existing session store identified by its UUID.
     *
     * @param clientType the client type (web or mobile) to look up
     * @param uuid       the session UUID previously assigned at creation
     *                   time
     * @return the loaded store, or {@link Optional#empty()} if no session
     *         exists with that UUID
     * @throws IOException if the store file cannot be read or decoded
     */
    Optional<WhatsAppStore> load(WhatsAppClientType clientType, UUID uuid) throws IOException;

    /**
     * Loads an existing session store identified by its phone number.
     *
     * @param clientType  the client type (web or mobile) to look up
     * @param phoneNumber the phone number associated with the session
     * @return the loaded store, or {@link Optional#empty()} if no session
     *         exists for that phone number
     * @throws IOException if the store file cannot be read or decoded
     */
    Optional<WhatsAppStore> load(WhatsAppClientType clientType, long phoneNumber) throws IOException;

    /**
     * Loads the most recently persisted session for the given client type.
     *
     * @param clientType the client type (web or mobile) to look up
     * @return the most recent store, or {@link Optional#empty()} if no
     *         session directory exists
     * @throws IOException if the store file cannot be read or decoded
     */
    Optional<WhatsAppStore> loadLatest(WhatsAppClientType clientType) throws IOException;

    /**
     * Creates a new, empty session store identified by the given UUID.
     *
     * @param clientType the client type (web or mobile) for the new
     *                   session
     * @param uuid       the UUID to assign to the session, or {@code null}
     *                   to generate a random one
     * @return the newly created store
     * @throws IOException if the store directory cannot be created
     */
    WhatsAppStore create(WhatsAppClientType clientType, UUID uuid) throws IOException;

    /**
     * Creates a new, empty session store identified by the given phone
     * number.
     *
     * @param clientType  the client type (web or mobile) for the new
     *                    session
     * @param phoneNumber the phone number to associate with the session
     * @return the newly created store
     * @throws IOException if the store directory cannot be created
     */
    WhatsAppStore create(WhatsAppClientType clientType, long phoneNumber) throws IOException;

    /**
     * Creates a new session store using the data in {@code sixPartsKeys}
     *
     * @param clientType  the client type (web or mobile) for the new
     *                    session
     * @param sixPartsKeys the six parts key data
     * @return the newly created store
     * @throws IOException if the store directory cannot be created
     */
    WhatsAppStore create(WhatsAppClientType clientType, WhatsAppClientSixPartsKeys sixPartsKeys) throws IOException;
}
