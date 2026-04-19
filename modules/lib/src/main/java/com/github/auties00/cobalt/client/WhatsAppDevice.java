package com.github.auties00.cobalt.client;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.device.DevicePlatformType;
import com.github.auties00.cobalt.model.device.pairing.ClientAppVersion;
import com.github.auties00.cobalt.model.device.pairing.ClientPlatformType;
import com.github.auties00.cobalt.util.DataUtils;
import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

import java.util.List;
import java.util.Objects;

/**
 * Describes the hardware and platform identity that a Cobalt client
 * advertises as its underlying device.
 *
 * <p>When Cobalt connects to the WhatsApp servers it must claim a device
 * identity: model name, manufacturer, operating system version, and
 * client platform (Android, iOS, Web, Desktop). That identity is embedded
 * into the handshake payload so the server can categorise the device for
 * telemetry, feature gating and User-Agent construction. Cobalt does not
 * auto-detect the host machine; it instead advertises a synthetic device
 * picked by the caller or by the provided factories.
 *
 * <p>Pre-built profiles are available via {@link #web()},
 * {@link #desktop()}, {@link #ios(boolean)}, and
 * {@link #android(boolean)}; the mobile factories randomise the
 * model/version tuple from a curated list to reduce
 * fingerprintability.
 *
 * @see WhatsAppClientType
 * @see WhatsAppClientBuilder.Options#device(WhatsAppDevice)
 * @see DevicePlatformType
 */
@ProtobufMessage
@WhatsAppWebModule(moduleName = "WAWebProtobufsCompanionReg.pb")
public final class WhatsAppDevice {
    /**
     * A curated list of realistic iOS device configurations used by the
     * {@link #ios(boolean)} factory method. Each entry represents a specific
     * iPhone model running a particular iOS version with its corresponding
     * build number and internal model identifier. A random entry is selected
     * during factory invocation to reduce fingerprinting surface.
     */
    private static final List<WhatsAppDevice> IOS_DEVICES = List.of(
            new WhatsAppDevice(
                    "iPhone 7",
                    "Apple",
                    null,
                    ClientAppVersion.of("14.8.1"),
                    "18H107",
                    "iPhone9,3",
                    WhatsAppClientType.MOBILE
            ),
            new WhatsAppDevice(
                    "iPhone 7",
                    "Apple",
                    null,
                    ClientAppVersion.of("15.8.2"),
                    "19H384",
                    "iPhone9,3",
                    WhatsAppClientType.MOBILE
            ),
            new WhatsAppDevice(
                    "iPhone 7 Plus",
                    "Apple",
                    null,
                    ClientAppVersion.of("14.8.1"),
                    "18H107",
                    "iPhone9,4",
                    WhatsAppClientType.MOBILE
            ),
            new WhatsAppDevice(
                    "iPhone 7 Plus",
                    "Apple",
                    null,
                    ClientAppVersion.of("15.8.2"),
                    "19H384",
                    "iPhone9,4",
                    WhatsAppClientType.MOBILE
            ),
            new WhatsAppDevice(
                    "iPhone 8",
                    "Apple",
                    null,
                    ClientAppVersion.of("13.7"),
                    "17H35",
                    "iPhone10,4",
                    WhatsAppClientType.MOBILE
            ),
            new WhatsAppDevice(
                    "iPhone 8",
                    "Apple",
                    null,
                    ClientAppVersion.of("14.8.1"),
                    "18H107",
                    "iPhone10,4",
                    WhatsAppClientType.MOBILE
            ),
            new WhatsAppDevice(
                    "iPhone 8",
                    "Apple",
                    null,
                    ClientAppVersion.of("15.8.2"),
                    "19H384",
                    "iPhone10,4",
                    WhatsAppClientType.MOBILE
            ),
            new WhatsAppDevice(
                    "iPhone 8",
                    "Apple",
                    null,
                    ClientAppVersion.of("16.7.7"),
                    "20H330",
                    "iPhone10,4",
                    WhatsAppClientType.MOBILE
            ),
            new WhatsAppDevice(
                    "iPhone 8 Plus",
                    "Apple",
                    null,
                    ClientAppVersion.of("14.8.1"),
                    "18H107",
                    "iPhone10,5",
                    WhatsAppClientType.MOBILE
            ),
            new WhatsAppDevice(
                    "iPhone 8 Plus",
                    "Apple",
                    null,
                    ClientAppVersion.of("15.8.2"),
                    "19H384",
                    "iPhone10,5",
                    WhatsAppClientType.MOBILE
            ),
            new WhatsAppDevice(
                    "iPhone 8 Plus",
                    "Apple",
                    null,
                    ClientAppVersion.of("16.7.7"),
                    "20H330",
                    "iPhone10,5",
                    WhatsAppClientType.MOBILE
            ),
            new WhatsAppDevice(
                    "iPhone X",
                    "Apple",
                    null,
                    ClientAppVersion.of("14.8.1"),
                    "18H107",
                    "iPhone10,6",
                    WhatsAppClientType.MOBILE
            ),
            new WhatsAppDevice(
                    "iPhone X",
                    "Apple",
                    null,
                    ClientAppVersion.of("15.8.2"),
                    "19H384",
                    "iPhone10,6",
                    WhatsAppClientType.MOBILE
            ),
            new WhatsAppDevice(
                    "iPhone X",
                    "Apple",
                    null,
                    ClientAppVersion.of("16.7.7"),
                    "20H330",
                    "iPhone10,6",
                    WhatsAppClientType.MOBILE
            ),
            new WhatsAppDevice(
                    "iPhone XR",
                    "Apple",
                    null,
                    ClientAppVersion.of("14.8.1"),
                    "18H107",
                    "iPhone11,8",
                    WhatsAppClientType.MOBILE
            ),
            new WhatsAppDevice(
                    "iPhone XR",
                    "Apple",
                    null,
                    ClientAppVersion.of("15.8.2"),
                    "19H384",
                    "iPhone11,8",
                    WhatsAppClientType.MOBILE
            ),
            new WhatsAppDevice(
                    "iPhone XR",
                    "Apple",
                    null,
                    ClientAppVersion.of("16.7.7"),
                    "20H330",
                    "iPhone11,8",
                    WhatsAppClientType.MOBILE
            ),
            new WhatsAppDevice(
                    "iPhone XR",
                    "Apple",
                    null,
                    ClientAppVersion.of("17.4.1"),
                    "21E236",
                    "iPhone11,8",
                    WhatsAppClientType.MOBILE
            ),
            new WhatsAppDevice(
                    "iPhone XS",
                    "Apple",
                    null,
                    ClientAppVersion.of("14.8.1"),
                    "18H107",
                    "iPhone11,2",
                    WhatsAppClientType.MOBILE
            ),
            new WhatsAppDevice(
                    "iPhone XS",
                    "Apple",
                    null,
                    ClientAppVersion.of("15.8.2"),
                    "19H384",
                    "iPhone11,2",
                    WhatsAppClientType.MOBILE
            ),
            new WhatsAppDevice(
                    "iPhone XS",
                    "Apple",
                    null,
                    ClientAppVersion.of("16.7.7"),
                    "20H330",
                    "iPhone11,2",
                    WhatsAppClientType.MOBILE
            ),
            new WhatsAppDevice(
                    "iPhone XS",
                    "Apple",
                    null,
                    ClientAppVersion.of("17.4.1"),
                    "21E236",
                    "iPhone11,2",
                    WhatsAppClientType.MOBILE
            ),
            new WhatsAppDevice(
                    "iPhone XS Max",
                    "Apple",
                    null,
                    ClientAppVersion.of("14.8.1"),
                    "18H107",
                    "iPhone11,6",
                    WhatsAppClientType.MOBILE
            ),
            new WhatsAppDevice(
                    "iPhone XS Max",
                    "Apple",
                    null,
                    ClientAppVersion.of("15.8.2"),
                    "19H384",
                    "iPhone11,6",
                    WhatsAppClientType.MOBILE
            ),
            new WhatsAppDevice(
                    "iPhone XS Max",
                    "Apple",
                    null,
                    ClientAppVersion.of("16.7.7"),
                    "20H330",
                    "iPhone11,6",
                    WhatsAppClientType.MOBILE
            ),
            new WhatsAppDevice(
                    "iPhone XS Max",
                    "Apple",
                    null,
                    ClientAppVersion.of("17.4.1"),
                    "21E236",
                    "iPhone11,6",
                    WhatsAppClientType.MOBILE
            )
    );

    /**
     * The user-facing model name of the device, such as {@code "iPhone 8"} or
     * {@code "Pixel_5"}.
     */
    @ProtobufProperty(index = 1, type = ProtobufType.STRING)
    String model;

    /**
     * The device manufacturer name, such as {@code "Apple"} or {@code "Google"}.
     */
    @ProtobufProperty(index = 2, type = ProtobufType.STRING)
    String manufacturer;

    /**
     * The platform type identifying the operating system and client variant, such as
     * {@link DevicePlatformType#IOS_PHONE} or {@link DevicePlatformType#ANDROID_PHONE}.
     *
     * @implNote WAWebProtobufsCompanionReg.pb: {@code DeviceProps$PlatformType}
     * constrains the set of wire values accepted here.
     */
    @WhatsAppWebExport(moduleName = "WAWebProtobufsCompanionReg.pb",
            exports = "DeviceProps$PlatformType", adaptation = WhatsAppAdaptation.DIRECT)
    @ProtobufProperty(index = 3, type = ProtobufType.ENUM)
    ClientPlatformType platform;

    /**
     * The operating system version running on the device, such as {@code 16.7.7} for
     * iOS or {@code 14} for Android.
     */
    @ProtobufProperty(index = 4, type = ProtobufType.MESSAGE)
    ClientAppVersion osDeviceAppVersion;

    /**
     * The OS build number string, such as {@code "20H330"} for iOS. May be {@code null}
     * for platforms where the build number is not applicable, in which case the
     * {@link #osBuildNumber()} accessor returns the OS version string instead.
     */
    @ProtobufProperty(index = 5, type = ProtobufType.STRING)
    String osBuildNumber;

    /**
     * The internal hardware model identifier, such as {@code "iPhone10,4"} for
     * iPhone 8 or {@code "Pixel_5"} for Google Pixel 5. May be {@code null} for
     * web and desktop clients.
     */
    @ProtobufProperty(index = 6, type = ProtobufType.STRING)
    String modelId;

    /**
     * The WhatsApp client type, distinguishing between mobile and web clients.
     */
    @ProtobufProperty(index = 7, type = ProtobufType.ENUM)
    WhatsAppClientType clientType;

    /**
     * Constructs a new {@code WhatsAppDevice} with the specified properties.
     *
     * @param model         the user-facing model name
     * @param manufacturer  the device manufacturer
     * @param platform      the platform type, or {@code null}
     * @param osDeviceAppVersion     the operating system version
     * @param osBuildNumber the OS build number, or {@code null}
     * @param modelId       the internal hardware model identifier, or {@code null}
     * @param clientType    the WhatsApp client type
     */
    WhatsAppDevice(
            String model,
            String manufacturer,
            ClientPlatformType platform,
            ClientAppVersion osDeviceAppVersion,
            String osBuildNumber,
            String modelId,
            WhatsAppClientType clientType
    ) {
        this.model = model;
        this.modelId = modelId;
        this.manufacturer = manufacturer;
        this.platform = platform;
        this.osDeviceAppVersion = osDeviceAppVersion;
        this.osBuildNumber = osBuildNumber;
        this.clientType = clientType;
    }

    /**
     * Creates a {@code WhatsAppDevice} configured as a browser-based
     * WhatsApp Web companion.
     *
     * <p>Mirrors what WA Web transmits in {@code ClientPayload.UserAgent}
     * from {@code WAWebClientPayload.C}: the wire platform is hardcoded to
     * {@link ClientPlatformType#WEB}, the app version block carries the
     * Chrome-derived {@code appVersion}, and the human-visible device
     * identity is Chrome on Windows so the server routes through the
     * WebSocket {@code /ws/chat} endpoint used by genuine browser
     * sessions.
     *
     * @implNote WAWebClientPayload.y: {@code platform:
     *           ClientPayload$UserAgent$Platform.WEB} for every browser
     *           surface; Chrome-on-Windows is the most common combination
     *           shipped in production logs, hence the chosen defaults.
     * @return a new web-configured device descriptor
     */
    public static WhatsAppDevice web() {
        return new WhatsAppDevice(
                "Chrome",
                "Google Inc.",
                ClientPlatformType.WEB,
                ClientAppVersion.of("10.0"),
                null,
                null,
                WhatsAppClientType.WEB
        );
    }

    /**
     * Creates a {@code WhatsAppDevice} configured as a WhatsApp Desktop
     * companion, auto-detecting the host platform.
     *
     * <p>Picks {@link ClientPlatformType#MACOS} when the JVM reports a
     * Darwin {@code os.name} and {@link ClientPlatformType#WINDOWS}
     * otherwise (Linux hosts also default to Windows because that is the
     * most common WA Desktop build in production and has no native
     * Linux counterpart). WA Desktop ships as an Electron bundle but
     * reuses the same {@code ClientPayload} shape as {@link #web()} over
     * a raw TCP+TLS transport (see {@code WhatsAppSocketClient.Desktop}).
     * Cobalt's socket layer selects that transport when the device
     * platform is {@code MACOS} or {@code WINDOWS}.
     *
     * @implNote WAWebClientPayload.b: {@code "Desktop" -> DESKTOP}
     *           DeviceProps platformType; {@code WAWebEnvironment.isWindows}
     *           branches inside {@code WAWebClientPayload.y} set the
     *           {@code UWP} variant and add the Windows build quaternary
     *           on the {@code appVersion}. The {@code UserAgent.platform}
     *           remains {@code WEB} on the wire for every desktop build;
     *           the Cobalt enum stored here only drives local transport
     *           selection ({@code WhatsAppSocketClient.newCipheredSocketClient}).
     * @return a new desktop-configured device descriptor matching the
     *         host platform, or a Windows descriptor when the host is
     *         neither Windows nor macOS
     */
    public static WhatsAppDevice desktop() {
        var osName = System.getProperty("os.name", "").toLowerCase();
        var isMac = osName.contains("mac") || osName.contains("darwin");
        if (isMac) {
            return new WhatsAppDevice(
                    "MacBook Pro",
                    "Apple",
                    ClientPlatformType.MACOS,
                    ClientAppVersion.of("14.5"),
                    null,
                    null,
                    WhatsAppClientType.WEB
            );
        } else {
            return new WhatsAppDevice(
                    "Desktop",
                    "Microsoft",
                    ClientPlatformType.WINDOWS,
                    ClientAppVersion.of("10.0"),
                    null,
                    null,
                    WhatsAppClientType.WEB
            );
        }
    }

    /**
     * Creates a {@code WhatsAppDevice} configured as a randomly selected iOS device.
     *
     * <p>A device configuration is chosen at random from an internal list of realistic
     * iPhone models and iOS versions. The platform is set to
     * {@link ClientPlatformType#IOS_BUSINESS} if the {@code business} parameter is
     * {@code true}, or {@link ClientPlatformType#IOS} otherwise.
     *
     * @param business {@code true} to configure the device for WhatsApp Business,
     *                 {@code false} for the consumer variant
     * @return a new iOS-configured device descriptor
     */
    public static WhatsAppDevice ios(boolean business) {
        var device = IOS_DEVICES.get(DataUtils.randomInt(IOS_DEVICES.size()));
        return new WhatsAppDevice(
                device.model,
                device.manufacturer,
                business ? ClientPlatformType.IOS_BUSINESS : ClientPlatformType.IOS,
                device.osDeviceAppVersion,
                device.osBuildNumber,
                device.modelId,
                WhatsAppClientType.MOBILE
        );
    }

    /**
     * Creates a {@code WhatsAppDevice} configured as a randomly generated Android device.
     *
     * <p>The device emulates a Google Pixel with a randomly selected model number
     * (Pixel 2 through Pixel 8) and Android version (11 through 15). The platform
     * is set to {@link ClientPlatformType#ANDROID_BUSINESS} if the {@code business}
     * parameter is {@code true}, or {@link ClientPlatformType#ANDROID} otherwise.
     *
     * @param business {@code true} to configure the device for WhatsApp Business,
     *                 {@code false} for the consumer variant
     * @return a new Android-configured device descriptor
     */
    public static WhatsAppDevice android(boolean business) {
        var model = "Pixel_" + DataUtils.randomInt(2, 9);
        return new WhatsAppDevice(
                model,
                "Google",
                business ? ClientPlatformType.ANDROID_BUSINESS : ClientPlatformType.ANDROID,
                ClientAppVersion.of(String.valueOf(DataUtils.randomInt(11, 16))),
                null,
                model,
                WhatsAppClientType.MOBILE
        );
    }

    /**
     * Returns the OS build number, falling back to the OS version string if the
     * build number is {@code null}.
     *
     * @return the OS build number string, never {@code null}
     */
    public String osBuildNumber() {
        return Objects.requireNonNullElse(osBuildNumber, osDeviceAppVersion.toString());
    }

    /**
     * Returns a User-Agent string suitable for HTTP requests issued by
     * this device.
     *
     * <p>Web and desktop platforms return a Chrome User-Agent (so
     * companion linking HTTP requests look browser-like), while mobile
     * platforms return the WhatsApp-specific User-Agent pattern
     * ({@code WhatsApp/<version> <platform>/<os> Device/<model>}) that
     * the official mobile clients emit.
     *
     * @param clientDeviceAppVersion the WhatsApp client version to embed
     *                               in the User-Agent
     * @return the formatted User-Agent string
     * @throws IllegalStateException if the underlying platform enum has an
     *                               unexpected value
     */
    public String toUserAgent(ClientAppVersion clientDeviceAppVersion) {
        if(platform == ClientPlatformType.WINDOWS || platform == ClientPlatformType.MACOS || platform == ClientPlatformType.WEB) {
            return "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36";
        }else {
            var platformName = switch (platform) {
                case ANDROID -> "Android";
                case ANDROID_BUSINESS -> "SMBA";
                case IOS -> "iOS";
                case IOS_BUSINESS -> "SMB iOS";
                default -> throw new IllegalStateException("Unexpected value: " + platform);
            };
            var deviceName = switch (platform) {
                case ANDROID, ANDROID_BUSINESS -> manufacturer + "-" + model;
                case IOS, IOS_BUSINESS -> model;
                case MACOS, WINDOWS -> throw new InternalError();
                default -> throw new IllegalStateException("Unexpected value: " + platform);
            };
            var deviceDeviceAppVersion = osDeviceAppVersion.toString();
            return "WhatsApp/%s %s/%s Device/%s".formatted(
                    clientDeviceAppVersion,
                    platformName,
                    deviceDeviceAppVersion,
                    deviceName
            );
        }
    }

    /**
     * Returns a copy of this device with its platform switched to the personal
     * (non-business) variant. If the platform is already a personal variant,
     * {@code this} is returned.
     *
     * @return this device if already personal, otherwise a new device with the
     *         personal platform variant
     */
    public WhatsAppDevice toPersonal() {
        return switch (platform) {
            case ANDROID_BUSINESS -> withPlatform(ClientPlatformType.ANDROID);
            case IOS_BUSINESS -> withPlatform(ClientPlatformType.IOS);
            default -> this;
        };
    }

    /**
     * Returns a copy of this device with its platform switched to the business
     * variant. If the platform is already a business variant, {@code this} is
     * returned.
     *
     * @return this device if already business, otherwise a new device with the
     *         business platform variant
     */
    public WhatsAppDevice toBusiness() {
        return switch (platform) {
            case ANDROID -> withPlatform(ClientPlatformType.ANDROID_BUSINESS);
            case ClientPlatformType.IOS -> withPlatform(ClientPlatformType.IOS_BUSINESS);
            default -> this;
        };
    }

    /**
     * Returns a copy of this device with the specified platform. If the given
     * platform is {@code null}, the current platform is retained.
     *
     * @param platform the new platform type, or {@code null} to keep the current one
     * @return a new device with the given platform
     */
    public WhatsAppDevice withPlatform(ClientPlatformType platform) {
        return new WhatsAppDevice(
                model,
                manufacturer,
                Objects.requireNonNullElse(platform, this.platform),
                osDeviceAppVersion,
                osBuildNumber,
                modelId,
                clientType
        );
    }

    /**
     * Returns the user-facing model name of this device.
     *
     * @return the model name, such as {@code "iPhone 8"} or {@code "Pixel_5"}
     */
    public String model() {
        return model;
    }

    /**
     * Returns the internal hardware model identifier of this device.
     *
     * @return the model identifier, such as {@code "iPhone10,4"}, or {@code null}
     *         for web and desktop clients
     */
    public String modelId() {
        return modelId;
    }

    /**
     * Returns the manufacturer name of this device.
     *
     * @return the manufacturer, such as {@code "Apple"} or {@code "Google"}
     */
    public String manufacturer() {
        return manufacturer;
    }

    /**
     * Returns the platform type of this device.
     *
     * @return the platform type identifying the operating system and client variant
     */
    public ClientPlatformType platform() {
        return platform;
    }

    /**
     * Returns the operating system version of this device.
     *
     * @return the OS version
     */
    public ClientAppVersion osDeviceAppVersion() {
        return osDeviceAppVersion;
    }

    /**
     * Returns the WhatsApp client type of this device.
     *
     * @return the client type, such as {@link WhatsAppClientType#MOBILE} or
     *         {@link WhatsAppClientType#WEB}
     */
    public WhatsAppClientType clientType() {
        return clientType;
    }

    /**
     * Sets the user-facing model name of this device.
     *
     * @param model the new model name
     * @return this instance
     */
    public void setModel(String model) {
        this.model = model;
    }

    /**
     * Sets the manufacturer name of this device.
     *
     * @param manufacturer the new manufacturer name
     * @return this instance
     */
    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    /**
     * Sets the platform type of this device.
     *
     * @param platform the new platform type
     * @return this instance
     */
    public void setPlatform(ClientPlatformType platform) {
        this.platform = platform;
    }

    /**
     * Sets the operating system version of this device.
     *
     * @param osDeviceAppVersion the new OS version
     * @return this instance
     */
    public void setOsDeviceAppVersion(ClientAppVersion osDeviceAppVersion) {
        this.osDeviceAppVersion = osDeviceAppVersion;
    }

    /**
     * Sets the OS build number of this device.
     *
     * @param osBuildNumber the new OS build number, or {@code null}
     * @return this instance
     */
    public void setOsBuildNumber(String osBuildNumber) {
        this.osBuildNumber = osBuildNumber;
    }

    /**
     * Sets the internal hardware model identifier of this device.
     *
     * @param modelId the new model identifier, or {@code null}
     * @return this instance
     */
    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    /**
     * Sets the WhatsApp client type of this device.
     *
     * @param clientType the new client type
     * @return this instance
     */
    public void setClientType(WhatsAppClientType clientType) {
        this.clientType = clientType;
    }

    /**
     * Compares this device to the specified object for equality.
     *
     * <p>Two {@code WhatsAppDevice} instances are considered equal if and only if all of
     * their properties (model, manufacturer, platform, OS version, OS build number,
     * model identifier, and client type) are equal.
     *
     * @param o the object to compare with
     * @return {@code true} if the objects are equal, {@code false} otherwise
     */
    @Override
    public boolean equals(Object o) {
        return o instanceof WhatsAppDevice that
                && Objects.equals(model, that.model)
                && Objects.equals(manufacturer, that.manufacturer)
                && platform == that.platform
                && Objects.equals(osDeviceAppVersion, that.osDeviceAppVersion)
                && Objects.equals(osBuildNumber, that.osBuildNumber)
                && Objects.equals(modelId, that.modelId)
                && clientType == that.clientType;
    }

    /**
     * Returns a hash code for this device based on all of its properties.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(model, manufacturer, platform, osDeviceAppVersion, osBuildNumber, modelId, clientType);
    }

    /**
     * Returns a string representation of this device for debugging purposes.
     *
     * @return a human-readable string containing all device properties
     */
    @Override
    public String toString() {
        return "JidCompanion[" +
                "model='" + model + '\'' +
                ", manufacturer='" + manufacturer + '\'' +
                ", platform=" + platform +
                ", osDeviceAppVersion=" + osDeviceAppVersion +
                ", osBuildNumber='" + osBuildNumber + '\'' +
                ", modelId='" + modelId + '\'' +
                ", clientType=" + clientType +
                ']';
    }
}
