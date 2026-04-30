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
 * <p>Each factory encapsulates a specific storage strategy (fully in-memory
 * with protobuf file persistence, or a future persistent backend) and
 * exposes a uniform API to create a brand-new session store or to load an
 * existing one by UUID, by phone number, or by most recently used
 * selection.
 *
 * <p>Concrete factories are obtained via the static factory methods on this
 * interface. Callers should not depend on the concrete factory types
 * directly.
 */
public interface WhatsAppStoreFactory {
    /**
     * Returns a factory that keeps the entire store in memory and persists
     * it to protobuf files in the default Cobalt session directory under
     * the user's home directory.
     *
     * @return a new in-memory factory using the default storage directory
     */
    static WhatsAppStoreFactory inMemory() {
        return new InMemoryStoreFactory();
    }

    /**
     * Returns a factory that keeps the entire store in memory and persists
     * it to protobuf files under the given directory.
     *
     * @param directory the root directory under which per-session folders
     *                  are created
     * @return a new in-memory factory using the given storage directory
     */
    static WhatsAppStoreFactory inMemory(Path directory) {
        return new InMemoryStoreFactory(directory);
    }

    /**
     * Returns a factory that lazily decodes the store from persistent
     * storage.
     *
     * @return a persistent factory
     * @throws UnsupportedOperationException always: persistent stores are
     *         not yet implemented
     */
    static WhatsAppStoreFactory persistent() {
        // FIXME
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a factory that lazily decodes the store from persistent
     * storage under the given directory.
     *
     * @param directory the root directory under which per-session folders
     *                  are created
     * @return a persistent factory using the given storage directory
     * @throws UnsupportedOperationException always: persistent stores are
     *         not yet implemented
     */
    static WhatsAppStoreFactory persistent(Path directory) {
        // FIXME
        throw new UnsupportedOperationException();
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
     * Loads an existing session store identified by the phone number
     * extracted from a six-parts pairing key bundle.
     *
     * @param clientType the client type (web or mobile) to look up
     * @param keys       the six-parts keys whose phone number is used for
     *                   lookup
     * @return the loaded store, or {@link Optional#empty()} if no session
     *         exists for that phone number
     * @throws IOException if the store file cannot be read or decoded
     */
    default Optional<WhatsAppStore> load(WhatsAppClientType clientType, WhatsAppClientSixPartsKeys keys) throws IOException {
        return load(clientType, keys.phoneNumber());
    }

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
}
