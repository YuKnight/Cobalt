package com.github.auties00.cobalt.node.usync.result;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Success result of {@code WAWebUsyncDevice.deviceParser}.
 *
 * <p>Carries the peer's full device list and, when present, their signed
 * key-index-list metadata.
 */
@WhatsAppWebModule(moduleName = "WAWebUsyncDevice")
public final class DeviceResult implements UsyncProtocolResponse {
    /**
     * Holds the list of devices linked to the peer. Never {@code null}.
     */
    private final List<Device> devices;

    /**
     * Holds the peer's signed key-index-list metadata, or {@code null} if
     * the relay did not return a {@code <key-index-list>} child.
     */
    private final KeyIndex keyIndex;

    /**
     * Creates a new device result.
     *
     * @param devices  the device list; defaults to an empty list when
     *                 {@code null}
     * @param keyIndex the signed key-index, or {@code null}
     */
    public DeviceResult(List<Device> devices, KeyIndex keyIndex) {
        this.devices = devices == null ? List.of() : List.copyOf(devices);
        this.keyIndex = keyIndex;
    }

    /**
     * Returns the device list.
     *
     * @return the list, never {@code null}
     */
    public List<Device> devices() {
        return devices;
    }

    /**
     * Returns the signed key-index, when present.
     *
     * @return the key-index
     */
    public Optional<KeyIndex> keyIndex() {
        return Optional.ofNullable(keyIndex);
    }

    /**
     * One device in the peer's device list.
     */
    @WhatsAppWebModule(moduleName = "WAWebUsyncDevice")
    public static final class Device {
        /**
         * Holds the device id (small integer).
         */
        private final int id;

        /**
         * Holds the device's signed key index, or {@code null} if the relay
         * omitted the attribute.
         */
        private final Integer keyIndex;

        /**
         * Tracks whether the device is a hosted device. {@code false} when
         * the attribute is absent.
         */
        private final boolean hosted;

        /**
         * Creates a new device entry.
         *
         * @param id       the device id
         * @param keyIndex the signed key index, or {@code null}
         * @param hosted   the {@code is_hosted} flag
         */
        public Device(int id, Integer keyIndex, boolean hosted) {
            this.id = id;
            this.keyIndex = keyIndex;
            this.hosted = hosted;
        }

        /**
         * Returns the device id.
         *
         * @return the id
         */
        public int id() {
            return id;
        }

        /**
         * Returns the signed key index, when present.
         *
         * @return the key index
         */
        public Optional<Integer> keyIndex() {
            return Optional.ofNullable(keyIndex);
        }

        /**
         * Returns whether the device is marked as hosted.
         *
         * @return {@code true} if {@code is_hosted="true"}
         */
        public boolean hosted() {
            return hosted;
        }
    }

    /**
     * The peer's signed key-index-list metadata.
     */
    @WhatsAppWebModule(moduleName = "WAWebUsyncDevice")
    public static final class KeyIndex {
        /**
         * Holds the timestamp the index was signed at. Never {@code null}.
         */
        private final Instant timestamp;

        /**
         * Holds the raw signed protobuf bytes, or {@code null} when the
         * {@code <key-index-list>} element had no inline content.
         */
        private final byte[] signedBytes;

        /**
         * Holds the {@code expected_ts} attribute, or {@code null} when
         * absent.
         */
        private final Instant expectedTimestamp;

        /**
         * Creates a new key-index metadata.
         *
         * @param timestamp         the signed timestamp; must not be
         *                          {@code null}
         * @param signedBytes       the raw signed protobuf bytes, or
         *                          {@code null}
         * @param expectedTimestamp the expected timestamp, or {@code null}
         */
        public KeyIndex(Instant timestamp, byte[] signedBytes, Instant expectedTimestamp) {
            this.timestamp = Objects.requireNonNull(timestamp, "timestamp cannot be null");
            this.signedBytes = signedBytes;
            this.expectedTimestamp = expectedTimestamp;
        }

        /**
         * Returns the signed timestamp.
         *
         * @return the timestamp, never {@code null}
         */
        public Instant timestamp() {
            return timestamp;
        }

        /**
         * Returns the raw signed bytes, when present.
         *
         * @return the signed bytes
         */
        public Optional<byte[]> signedBytes() {
            return Optional.ofNullable(signedBytes);
        }

        /**
         * Returns the expected timestamp, when present.
         *
         * @return the expected timestamp
         */
        public Optional<Instant> expectedTimestamp() {
            return Optional.ofNullable(expectedTimestamp);
        }
    }
}
