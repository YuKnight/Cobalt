package com.github.auties00.cobalt.util;

import com.github.auties00.cobalt.client.WhatsAppClientType;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Optional;

/**
 * Filesystem helpers that resolve the on-disk layout of Cobalt's persistent
 * stores.
 *
 * <p>Each {@link WhatsAppClientType} has its own home directory under a
 * caller-supplied base path, and each session is stored inside a UUID named
 * subdirectory. These helpers handle directory creation, lookup of the
 * most recently modified session and recursive deletion.
 *
 * @implNote Pure Cobalt utility with no WhatsApp Web counterpart; WhatsApp
 *     Web persists its equivalent data to IndexedDB via
 *     {@code WAWebSchema*} modules rather than the filesystem.
 */
public final class StorePathUtils {
    /**
     * Prevents instantiation of this utility class.
     *
     * @throws UnsupportedOperationException always
     */
    private StorePathUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Resolves the path of a specific file inside a session directory,
     * creating parent directories if necessary.
     *
     * @param clientType    the client type
     * @param baseDirectory the base storage directory
     * @param uuid          the session identifier
     * @param fileName      the file name within the session
     * @return the resolved path
     * @throws IOException if the parent directories cannot be created
     */
    public static Path getSessionFile(WhatsAppClientType clientType, Path baseDirectory, String uuid, String fileName) throws IOException {
        return getSessionDirectory(clientType, baseDirectory, uuid)
                .resolve(fileName);
    }

    /**
     * Returns the session directory with the most recent
     * {@code lastModifiedTime} under the home directory for the given
     * client type, or an empty optional if none exist.
     *
     * @param clientType    the client type
     * @param baseDirectory the base storage directory
     * @return the most recently modified session directory, if any
     * @throws IOException if the home directory cannot be walked
     */
    @SuppressWarnings({"ConstantValue"}) // I prefer the readability like this
    public static Optional<Path> getLatestSessionDirectory(WhatsAppClientType clientType, Path baseDirectory) throws IOException {
        var sessionsDirectory = getHomeDirectory(clientType, baseDirectory);
        try(var walker = Files.walk(sessionsDirectory, 0).skip(1)) {
            return walker.reduce((first, second) -> {
                var firstTimestamp = getLastModifiedTime(first);
                var secondTimestamp = getLastModifiedTime(second);
                if(firstTimestamp.isEmpty() && secondTimestamp.isEmpty()) {
                    return first;
                } else if(firstTimestamp.isPresent() && secondTimestamp.isEmpty()) {
                    return first;
                } else if(firstTimestamp.isEmpty() && secondTimestamp.isPresent()) {
                    return second;
                } else {
                    return firstTimestamp.get().compareTo(secondTimestamp.get()) >= 0
                            ? first
                            : second;
                }
            });
        }
    }

    /**
     * Returns the last modified time of the given path, swallowing
     * {@link IOException} as an empty optional.
     *
     * @param first the path to probe
     * @return the last modified time, if available
     */
    private static Optional<FileTime> getLastModifiedTime(Path first) {
        try {
            var result = Files.getLastModifiedTime(first);
            return Optional.of(result);
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    /**
     * Resolves the directory for a specific session under the home
     * directory for the given client type, creating it if necessary.
     *
     * @param clientType    the client type
     * @param baseDirectory the base storage directory
     * @param path          the session identifier
     * @return the resolved session directory, guaranteed to exist
     * @throws IOException if the directory cannot be created
     */
    public static Path getSessionDirectory(WhatsAppClientType clientType, Path baseDirectory, String path) throws IOException {
        var result = getHomeDirectory(clientType, baseDirectory)
                .resolve(path);
        Files.createDirectories(result);
        return result;
    }

    /**
     * Resolves the home directory for the given client type under the
     * provided base directory, creating it if necessary.
     *
     * @param type          the client type
     * @param baseDirectory the base storage directory
     * @return the resolved home directory, guaranteed to exist
     * @throws IOException if the directory cannot be created
     */
    public static Path getHomeDirectory(WhatsAppClientType type, Path baseDirectory) throws IOException {
        var id = switch (type) {
            case WEB -> "web";
            case MOBILE -> "mobile";
        };
        var result = baseDirectory.resolve(id);
        Files.createDirectories(result);
        return result;
    }

    /**
     * Recursively deletes the given path and all of its children.
     *
     * @param path the path to delete
     * @throws IOException if any filesystem operation fails
     */
    public static void deleteRecursively(Path path) throws IOException {
        if (Files.notExists(path)) {
            return;
        }
        Files.walkFileTree(path, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
