package com.github.auties00.cobalt.wam;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.client.WhatsAppClientType;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.chat.ChatMessageInfo;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.message.MessageContainer;
import com.github.auties00.cobalt.model.message.commerce.OrderMessage;
import com.github.auties00.cobalt.model.message.commerce.ProductMessage;
import com.github.auties00.cobalt.model.message.contact.ContactMessage;
import com.github.auties00.cobalt.model.message.contact.ContactsArrayMessage;
import com.github.auties00.cobalt.model.message.event.EncEventResponseMessage;
import com.github.auties00.cobalt.model.message.event.EventMessage;
import com.github.auties00.cobalt.model.message.event.EventResponseMessage;
import com.github.auties00.cobalt.model.message.interactive.InteractiveMessage;
import com.github.auties00.cobalt.model.message.interactive.InteractiveMessageContent;
import com.github.auties00.cobalt.model.message.list.ListMessage;
import com.github.auties00.cobalt.model.message.list.ListResponseMessage;
import com.github.auties00.cobalt.model.message.location.LiveLocationMessage;
import com.github.auties00.cobalt.model.message.location.LocationMessage;
import com.github.auties00.cobalt.model.message.media.AlbumMessage;
import com.github.auties00.cobalt.model.message.media.AudioMessage;
import com.github.auties00.cobalt.model.message.media.DocumentMessage;
import com.github.auties00.cobalt.model.message.media.ImageMessage;
import com.github.auties00.cobalt.model.message.media.StickerMessage;
import com.github.auties00.cobalt.model.message.media.StickerPackMessage;
import com.github.auties00.cobalt.model.message.media.VideoMessage;
import com.github.auties00.cobalt.model.message.poll.PollCreationMessage;
import com.github.auties00.cobalt.model.message.poll.PollUpdateMessage;
import com.github.auties00.cobalt.model.message.security.EncReactionMessage;
import com.github.auties00.cobalt.model.message.system.PinInChatMessage;
import com.github.auties00.cobalt.model.message.text.ExtendedTextMessage;
import com.github.auties00.cobalt.model.message.text.ReactionMessage;
import com.github.auties00.cobalt.props.ABProp;
import com.github.auties00.cobalt.props.ABPropsService;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.util.DataUtils;
import com.github.auties00.cobalt.wam.binary.WamGlobalEncoder;
import com.github.auties00.cobalt.wam.event.PsIdUpdateEventBuilder;
import com.github.auties00.cobalt.wam.privatestats.WamPrivateStatsTokenIssuer;
import com.github.auties00.cobalt.wam.privatestats.WamPrivateStatsUploader;
import com.github.auties00.cobalt.wam.event.SendDocumentEventBuilder;
import com.github.auties00.cobalt.wam.event.WamClientErrorsEventBuilder;
import com.github.auties00.cobalt.wam.model.WamEventSpec;
import com.github.auties00.cobalt.wam.model.WamChannel;
import com.github.auties00.cobalt.wam.privatestats.WamPrivateStatsId;
import com.github.auties00.cobalt.wam.type.AgentEngagementEnumType;
import com.github.auties00.cobalt.wam.type.BotType;
import com.github.auties00.cobalt.wam.type.DocumentType;
import com.github.auties00.cobalt.wam.type.E2eDeviceType;
import com.github.auties00.cobalt.wam.type.InvisibleMessageCategoryType;
import com.github.auties00.cobalt.wam.type.MediaType;
import com.github.auties00.cobalt.wam.type.MessageType;
import com.github.auties00.cobalt.wam.type.PsIdAction;
import com.google.i18n.phonenumbers.PhoneNumberUtil;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

/**
 * A service that collects, batches, and uploads WhatsApp Metrics (WAM)
 * telemetry events over the three transport channels.
 *
 * <p>Events are committed via {@link #commit(WamEventSpec)} and held
 * in memory until the next periodic flush or an immediate flush for
 * {@link WamChannel#REALTIME} events. On flush, one or more byte
 * buffers are allocated for each channel with pending events, each
 * capped at {@link #MAX_BUFFER_SIZE} bytes.
 *
 * <p>Events committed before {@link #initialize()} is called are
 * queued in an init queue and replayed once initialization completes,
 * matching WhatsApp Web's {@code WAWebWamInitQueue} mechanism.
 *
 * <p>This service does not persist unsent buffers across sessions.
 * WhatsApp Web persists pending buffers to IndexedDB every 5 seconds
 * and restores them on page reload; this implementation treats all
 * in-flight data as ephemeral — buffers that have not been uploaded
 * when the service is closed are silently discarded.
 *
 * @apiNote This service emits {@code WamClientErrorsWamEvent} with
 * {@code wamClientBufferDropErrorCount = 1} when a buffer is dropped
 * because it exceeds {@link #MAX_UPLOAD_SIZE}, mirroring WA Web's
 * {@code _executePending} and {@code oe} (upload) self-metric paths.
 * This service does not emit {@code WamDroppedEventWamEvent} for
 * per-event validation failures: WA Web uses it to track events
 * rejected by {@code runPreCommitValidation} for operational
 * visibility; this implementation logs a warning instead.
 *
 * @implNote Adapts the WA Web WAM telemetry pipeline: event commit comes
 *     from {@code WAWebWamCommonLogEvent}, buffer rotation and flush from
 *     {@code WAWebWam}, AB-prop sampling overrides from
 *     {@code WAWebEventSampling}, beacon sequence numbers from
 *     {@code WAWebWamBeaconing} and private-stats identifier rotation from
 *     {@code WAWebWamPrivateStats}.
 * @see WamEventSpec
 * @see WamGlobalEncoder
 * @see WamChannel
 */
@WhatsAppWebModule(moduleName = "WAWebWam")
@WhatsAppWebModule(moduleName = "WAWebWamCommonLogEvent")
@WhatsAppWebModule(moduleName = "WAWebL10NCountryCodes")
@WhatsAppWebModule(moduleName = "WAWebBrowserApi")
@WhatsAppWebModule(moduleName = "WAWebWamMsgUtils")
@WhatsAppWebModule(moduleName = "WAWebProcessRawMediaLogging")
@WhatsAppWebExport(moduleName = "WAWebWam", exports = "Wam", adaptation = WhatsAppAdaptation.ADAPTED)
public final class WamService {
    private static final Logger LOGGER = Logger.getLogger(WamService.class.getName());

    private static final VarHandle SHORT_HANDLE =
            MethodHandles.byteArrayViewVarHandle(short[].class, ByteOrder.LITTLE_ENDIAN);

    /**
     * Size of the WAM buffer header in bytes:
     * {@code "WAM"(3) + version(1) + streamId(1) + seqNum(2 LE) + channel(1)}.
     */
    private static final int HEADER_SIZE = 8;

    private static final byte[] WAM_MAGIC = {'W', 'A', 'M'};
    private static final int PROTOCOL_VERSION = 5;
    private static final int STREAM_ID = 1;

    /**
     * Interval in seconds between serialization checks. Matches the
     * WhatsApp Web two-tier timing where events are serialized every 5
     * seconds and the rotation/upload cycle runs every 120 seconds.
     */
    private static final int SERIALIZE_INTERVAL_SECONDS = 5;

    /**
     * Interval in seconds between rotation/upload cycles.
     */
    private static final int FLUSH_INTERVAL_SECONDS = 120;

    /**
     * Maximum size of a single WAM buffer in bytes before it is rotated
     * and a new buffer is started. Matches the JS constant
     * {@code WAM_MAX_BUFFER_SIZE}.
     */
    private static final int MAX_BUFFER_SIZE = 50_000;

    /**
     * Maximum size of a WAM buffer that may be uploaded. Buffers
     * exceeding this size are dropped. Matches the JS constant
     * {@code WAM_MAX_BUFFER_SIZE_FOR_UPLOAD}.
     */
    private static final int MAX_UPLOAD_SIZE = 64_000;

    /**
     * Maximum number of retry attempts for a failed buffer upload.
     */
    private static final int MAX_RETRIES = 2;

    /**
     * Base delay in milliseconds for exponential backoff between
     * upload retries.
     */
    private static final long RETRY_BASE_DELAY_MS = 1_000;

    /**
     * Maximum delay in milliseconds for exponential backoff between
     * upload retries.
     */
    private static final long RETRY_MAX_DELAY_MS = 120_000;

    /**
     * Maximum value for the uint16 sequence number before wrapping
     * back to 1.
     */
    private static final int MAX_SEQUENCE_NUMBER = 0xFFFF;

    /**
     * Device classification value for DESKTOP, matching the JS enum
     * {@code DEVICE_CLASSIFICATION.DESKTOP}.
     *
     * @implNote Adapts {@code WAWebFalcoCanonicalDeviceClassification}
     * which returns the string {@code "desktop"}; this numeric constant
     * is the encoded form written into the Falco global
     * {@code deviceClassification} (14507).
     */
    @WhatsAppWebExport(moduleName = "WAWebFalcoCanonicalDeviceClassification", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    private static final int DEVICE_CLASSIFICATION_DESKTOP = 4;

    /**
     * App build type value for RELEASE, matching the JS enum
     * {@code APP_BUILD_TYPE.RELEASE}.
     *
     * @apiNote WAWebWamEnumAppBuildType.APP_BUILD_TYPE: ALPHA, BETA,
     * RELEASE values.
     */
    private static final int APP_BUILD_RELEASE = 4;

    /**
     * Web client environment value for PROD, matching the JS enum
     * {@code WEBC_ENV_CODE.PROD}.
     */
    private static final int WEBC_ENV_PROD = 0;

    /**
     * Web platform value for WEB, matching the JS enum
     * {@code WEBC_WEB_PLATFORM_TYPE.WEB}.
     */
    private static final int PLATFORM_WEBCLIENT = 1;

    /**
     * Timeout in milliseconds to wait for connectivity before
     * attempting a WAM buffer upload.
     */
    private static final long CONNECTIVITY_WAIT_TIMEOUT_MS = 30_000;

    private final WhatsAppClient client;
    private final ABPropsService abPropsService;
    private final ConcurrentMap<WamChannel, List<WamPendingEvent>> pending;
    private final Map<WamChannel, AtomicInteger> sequenceNumbers;
    private final WamBeaconing beaconing;
    private final WamPrivateStatsId privateStatsId;
    private final WamPrivateStatsUploader privateStatsUploader;
    private final WamSamplingOverride samplingOverride;
    private final Map<WamChannel, Map<Integer, Object>> prevSessionGlobals;
    private final ConcurrentLinkedQueue<Runnable> initQueue;

    private volatile boolean initialized;
    private ScheduledExecutorService scheduler;

    private volatile long platform;
    private volatile String appVersion;
    private volatile String deviceName;
    /**
     * Approximate device memory class in megabytes, reported as the
     * WAM global with index {@code 655} ({@code memClass}).
     *
     * @implNote In WA Web this is derived from
     *           {@code self.navigator.deviceMemory * 1000}, short-circuiting
     *           to {@code 1000} when gkx {@code 17565} (prod low-end device)
     *           is set. Cobalt runs headless on the JVM where
     *           {@code navigator.deviceMemory} has no analog, so the value
     *           is approximated from {@link Runtime#maxMemory()}, and the
     *           low-end-device override is not applicable.
     */
    @WhatsAppWebExport(
            moduleName = "WAWebBrowserApi",
            exports = "getMemClass",
            adaptation = WhatsAppAdaptation.ADAPTED
    )
    private volatile int memClass;
    /**
     * Number of logical CPUs reported as the WAM global with index
     * {@code 10317} ({@code numCpu}).
     *
     * @implNote In WA Web this is {@code self.navigator.hardwareConcurrency},
     *           short-circuiting to {@code 1} when gkx {@code 17565} (prod
     *           low-end device) is set. Cobalt runs headless on the JVM and
     *           uses {@link Runtime#availableProcessors()}; the
     *           low-end-device override is not applicable.
     */
    @WhatsAppWebExport(
            moduleName = "WAWebBrowserApi",
            exports = "getNumCpu",
            adaptation = WhatsAppAdaptation.ADAPTED
    )
    private volatile int numCpu;
    private volatile String browser;
    private volatile String browserVersion;
    private volatile String osVersion;
    private volatile String deviceVersion;
    private volatile String webcTabId;
    private volatile String abKey2;
    private volatile int webcRevision;
    private volatile String companionAppVersion;
    private volatile String psCountryCode;
    private volatile boolean serviceImprovementOptOut;
    private volatile String pushPhase;

    /**
     * Constructs a new {@code WamService} bound to the given client.
     *
     * <p>The service is not active until {@link #initialize()} is called
     * after the client has authenticated.
     *
     * @param client         the WhatsApp client instance, must not be
     *                       {@code null}
     * @param abPropsService the AB props service for reading the AB key
     *                       and feature flags, must not be {@code null}
     */
    public WamService(WhatsAppClient client, ABPropsService abPropsService) {
        this.client = Objects.requireNonNull(client, "client cannot be null");
        this.abPropsService = Objects.requireNonNull(abPropsService, "abPropsService cannot be null");
        this.pending = new ConcurrentHashMap<>();
        this.sequenceNumbers = new EnumMap<>(WamChannel.class);
        for (var channel : WamChannel.values()) {
            sequenceNumbers.put(channel, new AtomicInteger(1));
        }
        this.beaconing = new WamBeaconing();
        this.privateStatsId = new WamPrivateStatsId();
        this.privateStatsUploader = new WamPrivateStatsUploader(new WamPrivateStatsTokenIssuer(client));
        this.samplingOverride = new WamSamplingOverride();
        this.prevSessionGlobals = new EnumMap<>(WamChannel.class);
        this.initQueue = new ConcurrentLinkedQueue<>();
    }

    /**
     * Initializes the service by snapshotting session globals from the
     * client store, loading sampling overrides from AB props, and
     * starting the periodic flush threads.
     *
     * <p>This method should be called once after the client has
     * authenticated and the store's JID and version are available.
     *
     * @implNote Adapts {@code WAWebWam.initWamRuntime}: the JS routine
     *     resolves {@code WAWebWamGlobals.PrivateStatsAllIds}, calls
     *     {@code WAWebWamPrivateStats.initPrivateStats}, starts the
     *     {@code WAShiftTimer} that drains pending events, registers the
     *     runtime singleton with {@code WAWebWamRuntimeProvider}, then
     *     replays the queue of pre-init {@code commit} / {@code set}
     *     calls. Cobalt collapses the registration into constructor DI
     *     and the {@code commitOnSet} toggle around {@code Global.set} is
     *     not needed because Cobalt does not back globals with a reactive
     *     proxy. The pre-init queue is implemented by {@link #initQueue}.
     */
    @WhatsAppWebExport(moduleName = "WAWebWam", exports = "initWamRuntime", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WAWebWam", exports = "commitOnSet", adaptation = WhatsAppAdaptation.ADAPTED)
    public void initialize() {
        var store = client.store();
        var version = store.clientVersion();
        this.appVersion = version != null ? version.toString() : null;
        this.platform = store.clientType() == WhatsAppClientType.WEB ? 8L : 2L;
        this.deviceName = store.name();
        // ADAPTED: WAWebBrowserApi.getMemClass - navigator.deviceMemory*1000 is headless-unavailable
        this.memClass = (int) (Runtime.getRuntime().maxMemory() / (1024 * 1024));
        // ADAPTED: WAWebBrowserApi.getNumCpu - navigator.hardwareConcurrency is headless-unavailable
        this.numCpu = Runtime.getRuntime().availableProcessors();
        this.browser = "Chrome";
        this.browserVersion = appVersion;
        this.osVersion = System.getProperty("os.name", "") + " " + System.getProperty("os.version", "");
        this.deviceVersion = osVersion;
        this.webcTabId = UUID.randomUUID().toString();
        this.abKey2 = abPropsService.getBool(ABProp.WAM_DISABLE_ABKEY_ATTRIBUTE)
                ? null
                : abPropsService.abKey().orElse("");
        this.webcRevision = version != null ? version.tertiary().orElse(0) : 0;
        this.companionAppVersion = store.companionVersion()
                .map(Object::toString)
                .orElse(null);
        this.psCountryCode = derivePsCountryCode();
        this.serviceImprovementOptOut = abPropsService.getBool(ABProp.SERVICE_IMPROVEMENT_OPT_OUT_FLAG);
        this.pushPhase = getPushPhase();

        // Load WAM event sampling overrides from AB props
        var configs = abPropsService.samplingConfigs();
        if (!configs.isEmpty()) {
            samplingOverride.replaceAll(configs);
        }

        this.initialized = true;

        // Drain the init queue: replay events committed before initialization
        Runnable action;
        while ((action = initQueue.poll()) != null) {
            action.run();
        }

        this.scheduler = Executors.newSingleThreadScheduledExecutor(Thread.ofVirtual().factory());
        scheduler.scheduleWithFixedDelay(this::checkMidCycleUpload, SERIALIZE_INTERVAL_SECONDS, SERIALIZE_INTERVAL_SECONDS, TimeUnit.SECONDS);
        scheduler.scheduleWithFixedDelay(this::flush, FLUSH_INTERVAL_SECONDS, FLUSH_INTERVAL_SECONDS, TimeUnit.SECONDS);

        // WAWebWamPrivateStats.initPrivateStats: emit a PsIdUpdateEvent
        // with action CREATED for every rotation group that has no
        // prior value in storage. Cobalt does not persist PS IDs
        // across sessions, so every group is treated as freshly created
        // on each initialization.
        for (var info : privateStatsId.snapshotAll()) {
            commit(new PsIdUpdateEventBuilder()
                    .psIdAction(PsIdAction.CREATED)
                    .psIdKey(info.keyHashInt())
                    .psIdRotationFrequence(info.rotationDays())
                    .build());
        }
    }

    /**
     * Derives the PS country code from the user's phone number, matching
     * WhatsApp Web's {@code WAWebL10NCountryCodes.getCountryShortcodeByPhone}.
     *
     * @implNote WA Web walks a hand-maintained prefix trie
     *           (leading-digit -&gt; next-digit -&gt; ... -&gt; {@code c} leaf)
     *           hardcoded inside {@code WAWebL10NCountryCodes}, with two
     *           special-case rules applied on top: a leading {@code 1}
     *           that does not match a NANP sub-prefix falls back to
     *           {@code "US"}, and a leading {@code 7} that would match
     *           {@code "RU"} is remapped to {@code "KZ"} when the second
     *           digit is {@code 6} or {@code 7}. Cobalt defers the prefix
     *           analysis to Google's {@code libphonenumber}, whose
     *           region-code database encodes the same NANP fallback and
     *           the Kazakhstan {@code 7-6xx}/{@code 7-7xx} split as
     *           authoritative prefix data. The output format (ISO 3166-1
     *           alpha-2) is identical, and libphonenumber is kept up to
     *           date with ITU E.164 allocations rather than a handwritten
     *           trie snapshot.
     * @return the two-letter ISO 3166-1 alpha-2 country code in
     *         lowercase, or {@code null} if not derivable
     */
    @WhatsAppWebExport(moduleName = "WAWebL10NCountryCodes",
            exports = "getCountryShortcodeByPhone",
            adaptation = WhatsAppAdaptation.ADAPTED)
    private String derivePsCountryCode() {
        var phoneNumber = client.store().phoneNumber();
        if (phoneNumber.isEmpty()) {
            return null;
        }

        try {
            var parsed = PhoneNumberUtil.getInstance().parse("+" + phoneNumber.getAsLong(), null);
            var regionCode = PhoneNumberUtil.getInstance().getRegionCodeForNumber(parsed);
            return regionCode != null ? regionCode.toLowerCase(Locale.ROOT) : null;
        } catch (Exception _) {
            return null;
        }
    }

    /**
     * Commits an event for later transmission.
     *
     * <p>The event is first validated via {@link WamEventSpec#validate()}.
     * If validation fails, the event is silently discarded and a warning
     * is logged, matching WhatsApp Web's {@code runPreCommitValidation()}
     * behaviour.
     *
     * <p>The event's sampling weight is resolved by first checking for
     * a runtime override via {@link WamSamplingOverride}, then falling
     * back to the static {@link WamEventSpec#releaseWeight()}. If the
     * event is sampled out, it is silently discarded.
     *
     * <p>For {@link WamChannel#REALTIME} events, an immediate flush is
     * scheduled.
     *
     * @param event the event to commit, must not be {@code null}
     */
    public void commit(WamEventSpec event) {
        Objects.requireNonNull(event, "event cannot be null");
        if (!event.markCommitted()) {
            LOGGER.warning("WAM redundant commit: " + event.getClass().getSimpleName());
            return;
        }

        if (!event.validate()) {
            LOGGER.warning("WAM event failed validation: " + event.getClass().getSimpleName());
            return;
        }

        var weight = effectiveWeight(event);
        if (weight > 1) {
            if (DataUtils.randomInt(weight) != 0) {
                return;
            }
        }

        var pe = new WamPendingEvent(event, Instant.now().getEpochSecond());
        pending.compute(event.channel(), (_, list) -> {
            if (list == null) {
                list = new ArrayList<>();
            }
            list.add(pe);
            return list;
        });

        if (event.channel() == WamChannel.REALTIME) {
            Thread.ofVirtual().start(() -> flushChannel(WamChannel.REALTIME));
        }
    }

    /**
     * Commits an event and returns a future that completes when the
     * buffer containing the event is flushed to the server.
     *
     * <p>This matches WhatsApp Web's {@code commitAndWaitForFlush()}
     * which returns a Promise resolved on buffer flush.
     *
     * @param event the event to commit, must not be {@code null}
     * @return a future that completes when the event's buffer is flushed,
     *         or completes immediately if the event was sampled out or
     *         failed validation
     */
    public CompletableFuture<Void> commitAndWaitForFlush(WamEventSpec event) {
        Objects.requireNonNull(event, "event cannot be null");
        if (!event.markCommitted()) {
            LOGGER.warning("WAM redundant commit: " + event.getClass().getSimpleName());
            return CompletableFuture.completedFuture(null);
        }

        if (!event.validate()) {
            LOGGER.warning("WAM event failed validation: " + event.getClass().getSimpleName());
            return CompletableFuture.completedFuture(null);
        }

        var weight = effectiveWeight(event);
        if (weight > 1) {
            if (DataUtils.randomInt(weight) != 0) {
                return CompletableFuture.completedFuture(null);
            }
        }

        var future = new CompletableFuture<Void>();
        var pe = new WamPendingEvent(event, Instant.now().getEpochSecond(), future);
        pending.compute(event.channel(), (_, list) -> {
            if (list == null) {
                list = new ArrayList<>();
            }
            list.add(pe);
            return list;
        });

        if (event.channel() == WamChannel.REALTIME) {
            Thread.ofVirtual().start(() -> flushChannel(WamChannel.REALTIME));
        }

        return future;
    }

    /**
     * Registers a runtime sampling weight override for the given event
     * id. The override takes precedence over the static annotation
     * weight until removed.
     *
     * @param eventId the numeric WAM event identifier
     * @param weight  the overridden sampling weight (must be positive)
     */
    public void setSamplingOverride(int eventId, int weight) {
        samplingOverride.put(eventId, weight);
    }

    /**
     * Removes any runtime sampling weight override for the given event
     * id, reverting to the static annotation weight.
     *
     * @param eventId the numeric WAM event identifier
     */
    public void removeSamplingOverride(int eventId) {
        samplingOverride.remove(eventId);
    }

    /**
     * Replaces all current sampling overrides with the entries from the
     * given map.
     *
     * @param overrides a map from event id to sampling weight
     */
    public void replaceSamplingOverrides(Map<Integer, Integer> overrides) {
        samplingOverride.replaceAll(overrides);
    }

    /**
     * Flushes all pending events across all channels.
     *
     * <p>For each channel with pending events, one or more buffers are
     * built and sent — each capped at {@link #MAX_BUFFER_SIZE} bytes.
     * Buffers exceeding {@link #MAX_UPLOAD_SIZE} are dropped.
     *
     * <p>The pending list is atomically swapped to {@code null} so that
     * new events committed during the flush are not lost.
     *
     * @implNote Adapts {@code WAWebWam.sendAllLogs}: the JS routine
     *     reads pending buffers from {@code WAWebWamStorage} (IndexedDB)
     *     for a single buffer-key, uploads each to either
     *     {@code WAWebUploadStatsBackend} or
     *     {@code WAWebUploadPrivateStatsBackend}, drops oversize buffers
     *     emitting a {@code WamClientErrorsWamEvent} with
     *     {@code wamClientBufferDropErrorCount = 1}, and on partial
     *     success re-persists the failed payloads. Cobalt does not
     *     persist pending buffers to disk, so this method is the direct
     *     in-memory equivalent: {@code flushChannel} drains the pending
     *     list and {@code buildAndSend} performs the equivalent oversize
     *     drop and self-metric emission.
     */
    @WhatsAppWebExport(moduleName = "WAWebWam", exports = "sendAllLogs", adaptation = WhatsAppAdaptation.ADAPTED)
    public void flush() {
        if (!initialized) {
            return;
        }

        for (var channel : WamChannel.values()) {
            flushChannel(channel);
        }
    }

    /**
     * Checks whether any non-realtime channel has accumulated more than
     * {@link #MAX_BUFFER_SIZE} bytes of pending events and triggers an
     * early flush if so.
     *
     * <p>This implements the mid-cycle upload behaviour from WhatsApp
     * Web's two-tier timing system, where a 5-second serialization
     * timer checks for oversized buffers between the 120-second
     * rotation cycles.
     */
    private void checkMidCycleUpload() {
        if (!initialized) {
            return;
        }

        for (var channel : WamChannel.values()) {
            if (channel == WamChannel.REALTIME) {
                continue;
            }

            var oversized = new boolean[1];
            pending.compute(channel, (_, list) -> {
                if (list != null && !list.isEmpty()) {
                    var size = HEADER_SIZE;
                    for (var pe : list) {
                        size += pe.event().sizeOf();
                        if (size > MAX_BUFFER_SIZE) {
                            oversized[0] = true;
                            break;
                        }
                    }
                }
                return list;
            });

            if (oversized[0]) {
                flushChannel(channel);
            }
        }
    }

    /**
     * Drains and uploads all pending events for the given channel.
     *
     * <p>Events are consumed in order and packed into buffers up to
     * {@link #MAX_BUFFER_SIZE}. Each full buffer is encoded and sent
     * before the next batch begins.
     *
     * <p>For the {@link WamChannel#PRIVATE} channel, PS IDs are rotated
     * before flushing.
     *
     * @param channel the channel to flush
     */
    private void flushChannel(WamChannel channel) {
        var events = swapPending(channel);
        if (events.isEmpty()) {
            return;
        }

        if (channel == WamChannel.PRIVATE) {
            // WAWebWamPrivateStats internal rotate function: after
            // regenerating a PS ID value, logs a PsIdUpdateEvent with
            // action ROTATED for that entry.
            var rotated = privateStatsId.rotateAndReportChanges();
            for (var info : rotated) {
                commit(new PsIdUpdateEventBuilder()
                        .psIdAction(PsIdAction.ROTATED)
                        .psIdKey(info.keyHashInt())
                        .psIdRotationFrequence(info.rotationDays())
                        .build());
            }
            flushPrivateByPsIdGroup(events);
        } else {
            var bufferKey = channel == WamChannel.REGULAR ? "regular" : "realtime";
            flushEventList(channel, events, bufferKey);
        }
    }

    /**
     * Groups private events by their PS ID hash and flushes each group
     * as a separate buffer, matching the per-PS-ID-group buffer
     * separation in WhatsApp Web's {@code _executePendingForContext}.
     *
     * @param events the drained private events
     */
    private void flushPrivateByPsIdGroup(List<WamPendingEvent> events) {
        var groups = new LinkedHashMap<Integer, List<WamPendingEvent>>();
        for (var pe : events) {
            groups.computeIfAbsent(pe.event().privateStatsId(), _ -> new ArrayList<>()).add(pe);
        }
        for (var entry : groups.entrySet()) {
            var bufferKey = privateStatsId.getKeyNameForHash(entry.getKey());
            flushEventList(WamChannel.PRIVATE, entry.getValue(), bufferKey);
        }
    }

    /**
     * Flushes a list of events for the given channel, building one or
     * more buffers capped at {@link #MAX_BUFFER_SIZE}.
     *
     * <p>Buffer rotation uses a post-check: the event that pushes the
     * buffer over the limit stays in the current buffer, and a new
     * buffer is started for subsequent events. This matches WhatsApp
     * Web's behaviour where a buffer may momentarily exceed the limit
     * by one event.
     *
     * @param channel   the transport channel
     * @param events    the events to flush
     * @param bufferKey the beaconing buffer key
     */
    private void flushEventList(WamChannel channel, List<WamPendingEvent> events, String bufferKey) {
        var beacons = new OptionalInt[events.size()];
        var weights = new int[events.size()];
        for (var i = 0; i < events.size(); i++) {
            beacons[i] = beaconing.nextSequenceNumber(bufferKey);
            weights[i] = effectiveWeight(events.get(i).event());
        }

        var batchStart = 0;
        var globalsBytes = encodeGlobals(channel);
        var batchSize = HEADER_SIZE + globalsBytes.length;

        for (var i = 0; i < events.size(); i++) {
            var pe = events.get(i);
            var eventSize = computePerEventGlobalsSize(pe, channel, beacons[i]) + pe.event().sizeOf(weights[i]);
            batchSize += eventSize;

            if (batchSize > MAX_BUFFER_SIZE) {
                buildAndSend(channel, events, weights, beacons, globalsBytes, batchStart, i + 1, batchSize);
                batchStart = i + 1;
                if (batchStart < events.size()) {
                    globalsBytes = encodeGlobals(channel);
                    batchSize = HEADER_SIZE + globalsBytes.length;
                }
            }
        }

        if (batchStart < events.size()) {
            buildAndSend(channel, events, weights, beacons, globalsBytes, batchStart, events.size(), batchSize);
        }
    }

    /**
     * Builds a single WAM buffer from a slice of events and sends it.
     *
     * @param channel     the transport channel
     * @param events      the full event list
     * @param weights     pre-computed sampling weights parallel to the events
     *                    list, re-fetched at flush time
     * @param beacons     pre-computed beacon sequence numbers parallel to
     *                    the events list
     * @param globalsBytes the pre-encoded session globals bytes
     * @param from        the inclusive start index in the event list
     * @param to          the exclusive end index in the event list
     * @param size        the pre-computed total buffer size
     */
    private void buildAndSend(WamChannel channel, List<WamPendingEvent> events, int[] weights, OptionalInt[] beacons, byte[] globalsBytes, int from, int to, int size) {
        if (size > MAX_UPLOAD_SIZE) {
            LOGGER.warning("Dropping WAM buffer of " + size + " bytes (exceeds upload limit)");
            // WAWebWam.oe: when WAWebWamUtils.isWamBufferTooBigToUpload
            // is true, the buffer is dropped and a WamClientErrorsWamEvent
            // with wamClientBufferDropErrorCount = 1 is committed as a
            // pipeline self-metric.
            commit(new WamClientErrorsEventBuilder()
                    .wamClientBufferDropErrorCount(1)
                    .build());
            completeFutures(events, from, to);
            return;
        }

        var buffer = new byte[size];
        var offset = writeHeader(buffer, channel);
        System.arraycopy(globalsBytes, 0, buffer, offset, globalsBytes.length);
        offset += globalsBytes.length;

        for (var i = from; i < to; i++) {
            var pe = events.get(i);
            offset = writePerEventGlobals(pe, channel, beacons[i], buffer, offset);
            offset = pe.event().encode(buffer, offset, weights[i]);
        }

        assert offset == size : "Buffer size mismatch: wrote " + offset + " but allocated " + size;

        if (channel == WamChannel.PRIVATE) {
            sendPrivateWithRetry(buffer);
        } else {
            sendWithRetry(buffer);
        }

        completeFutures(events, from, to);
    }

    /**
     * Completes flush futures for the given range of events.
     *
     * @param events the event list
     * @param from   the inclusive start index
     * @param to     the exclusive end index
     */
    private static void completeFutures(List<WamPendingEvent> events, int from, int to) {
        for (var i = from; i < to; i++) {
            var future = events.get(i).flushFuture();
            if (future != null) {
                future.complete(null);
            }
        }
    }

    /**
     * Encodes the session globals for the given channel, writing only
     * globals that have changed since the last flush for this channel.
     *
     * <p>On first call for a channel, all globals are written. On
     * subsequent calls, only dirty (changed) globals and null
     * transitions are written. This matches WhatsApp Web's
     * dirty-tracking approach where each {@code WamContext} maintains
     * its own independent {@code prevGlobals}.
     *
     * @param channel the transport channel
     * @return the encoded globals as a byte array
     */
    private byte[] encodeGlobals(WamChannel channel) {
        var current = buildFullCurrentGlobals(channel);
        var prev = prevSessionGlobals.get(channel);

        var dirty = new ArrayList<Map.Entry<Integer, Object>>();
        var nullTransitions = new ArrayList<Integer>();

        for (var entry : current.entrySet()) {
            var prevValue = prev != null ? prev.get(entry.getKey()) : null;
            if (!Objects.equals(prevValue, entry.getValue())) {
                dirty.add(entry);
            }
        }

        if (prev != null) {
            for (var fieldId : prev.keySet()) {
                if (!current.containsKey(fieldId)) {
                    nullTransitions.add(fieldId);
                }
            }
        }

        var size = 0;
        for (var entry : dirty) {
            size += WamGlobalEncoder.dynamicGlobalSize(entry.getKey(), entry.getValue());
        }
        for (var fieldId : nullTransitions) {
            size += WamGlobalEncoder.nullGlobalSize(fieldId);
        }

        var bytes = new byte[size];
        var offset = 0;
        for (var entry : dirty) {
            offset = WamGlobalEncoder.writeDynamicGlobal(entry.getKey(), entry.getValue(), bytes, offset);
        }
        for (var fieldId : nullTransitions) {
            offset = WamGlobalEncoder.writeNullGlobal(fieldId, bytes, offset);
        }

        prevSessionGlobals.put(channel, current);
        return bytes;
    }

    /**
     * Builds a map of all current session global values keyed by field
     * ID for the given channel. Used for dirty-tracking comparisons.
     *
     * @param channel the transport channel
     * @return the current globals snapshot
     */
    private Map<Integer, Object> buildFullCurrentGlobals(WamChannel channel) {
        var globals = new LinkedHashMap<Integer, Object>();
        // 11 - platform (regular, private)
        globals.put(11, platform);
        // 13 - deviceName (regular, private)
        if (deviceName != null) globals.put(13, deviceName);
        // 15 - osVersion (regular, private)
        if (osVersion != null) globals.put(15, osVersion);
        // 17 - appVersion (regular, private)
        if (appVersion != null) globals.put(17, appVersion);
        // 21 - appIsBetaRelease (regular, private)
        globals.put(21, false);
        if (channel != WamChannel.PRIVATE) {
            // 23 - networkIsWifi (regular)
            globals.put(23, true);
            // 295 - browserVersion (regular)
            if (browserVersion != null) globals.put(295, browserVersion);
            // 633 - webcEnv (regular)
            globals.put(633, (long) WEBC_ENV_PROD);
        }
        // 655 - memClass (regular, private)
        globals.put(655, (long) memClass);
        if (channel != WamChannel.PRIVATE) {
            // 779 - browser (regular)
            if (browser != null) globals.put(779, browser);
        }
        // 899 - webcWebPlatform (regular, private)
        globals.put(899, (long) PLATFORM_WEBCLIENT);
        if (channel != WamChannel.PRIVATE) {
            // 1005 - webcPhoneAppVersion (regular)
            if (companionAppVersion != null) globals.put(1005, companionAppVersion);
        }
        // 1657 - appBuild (regular, private)
        globals.put(1657, (long) APP_BUILD_RELEASE);
        // 3543 - streamId (regular, private)
        globals.put(3543, (long) STREAM_ID);
        if (channel != WamChannel.PRIVATE) {
            // 3727 - webcTabId (regular)
            if (webcTabId != null) globals.put(3727, webcTabId);
            // 4473 - abKey2 (regular)
            if (abKey2 != null) globals.put(4473, abKey2);
            // 4505 - deviceVersion (regular)
            if (deviceVersion != null) globals.put(4505, deviceVersion);
            // 6605 - webcWebArch (regular) - WAWebWam.getPushPhase
            if (pushPhase != null) globals.put(6605, pushPhase);
        }
        // 6251 - ocVersion (regular, private)
        globals.put(6251, 1L);
        if (channel == WamChannel.PRIVATE) {
            // 6833 - psCountryCode (private)
            if (psCountryCode != null) globals.put(6833, psCountryCode);
        }
        if (channel != WamChannel.PRIVATE) {
            // 10317 - numCpu (regular)
            globals.put(10317, (long) numCpu);
        }
        // 13293 - serviceImprovementOptOut (regular, private)
        globals.put(13293, serviceImprovementOptOut);
        if (channel != WamChannel.PRIVATE) {
            // 14507 - deviceClassification (regular)
            globals.put(14507, (long) DEVICE_CLASSIFICATION_DESKTOP);
            // 18491 - webcRevision (regular)
            globals.put(18491, (long) webcRevision);
        }
        return globals;
    }

    /**
     * Returns the byte count of per-event globals (commit time,
     * beaconing sequence, private stats id).
     *
     * @param pe      the pending event
     * @param channel the transport channel
     * @param beacon  the pre-computed beacon sequence number
     * @return the per-event globals size in bytes
     */
    private int computePerEventGlobalsSize(WamPendingEvent pe, WamChannel channel, OptionalInt beacon) {
        var size = WamGlobalEncoder.commitTimeSize(pe.commitTimeSeconds());
        if (beacon.isPresent()) {
            size += WamGlobalEncoder.beaconSessionIdSize(beacon.getAsInt());
        }
        if (channel == WamChannel.PRIVATE) {
            var psId = privateStatsId.getValueForHash(pe.event().privateStatsId());
            size += WamGlobalEncoder.psIdSize(psId);
        }
        return size;
    }

    /**
     * Writes the per-event globals (commit time, beaconing sequence,
     * private stats id) into the output buffer.
     *
     * @param pe      the pending event
     * @param channel the transport channel
     * @param beacon  the pre-computed beacon sequence number
     * @param buffer  the output byte array
     * @param offset  the current offset
     * @return the new offset after writing
     */
    private int writePerEventGlobals(WamPendingEvent pe, WamChannel channel, OptionalInt beacon, byte[] buffer, int offset) {
        offset = WamGlobalEncoder.writeCommitTime(pe.commitTimeSeconds(), buffer, offset);
        if (beacon.isPresent()) {
            offset = WamGlobalEncoder.writeBeaconSessionId(beacon.getAsInt(), buffer, offset);
        }
        if (channel == WamChannel.PRIVATE) {
            var psId = privateStatsId.getValueForHash(pe.event().privateStatsId());
            offset = WamGlobalEncoder.writePsId(psId, buffer, offset);
        }
        return offset;
    }

    /**
     * Atomically swaps the pending list for the given channel with
     * {@code null}, returning the old contents.
     *
     * <p>Uses {@link ConcurrentHashMap#compute} to ensure that no
     * concurrent {@link #commit} call can append to the returned list
     * after the swap.
     *
     * @param channel the channel to drain
     * @return the drained events, never {@code null}
     */
    private List<WamPendingEvent> swapPending(WamChannel channel) {
        var result = new ArrayList<WamPendingEvent>();
        pending.compute(channel, (_, list) -> {
            if (list != null && !list.isEmpty()) {
                result.addAll(list);
            }
            return null;
        });
        return result;
    }

    /**
     * Returns the effective sampling weight for the given event, checking
     * the runtime override map first.
     *
     * @param event the event to query
     * @return the effective sampling weight
     */
    private int effectiveWeight(WamEventSpec event) {
        var override = samplingOverride.get(event.id());
        return override.isPresent() ? Math.abs(override.getAsInt()) : event.releaseWeight();
    }

    /**
     * Writes the 8-byte WAM buffer header.
     *
     * @param buffer  the output buffer
     * @param channel the transport channel
     * @return the offset after the header (always {@value HEADER_SIZE})
     */
    private int writeHeader(byte[] buffer, WamChannel channel) {
        var offset = 0;
        System.arraycopy(WAM_MAGIC, 0, buffer, offset, WAM_MAGIC.length);
        offset += WAM_MAGIC.length;
        buffer[offset++] = (byte) PROTOCOL_VERSION;
        buffer[offset++] = (byte) STREAM_ID;
        SHORT_HANDLE.set(buffer, offset, (short) nextSequenceNumber(channel));
        offset += 2;
        buffer[offset++] = (byte) channel.id();
        return offset;
    }

    /**
     * Returns the next sequence number for the given channel, wrapping
     * from {@link #MAX_SEQUENCE_NUMBER} back to 1.
     *
     * <p>Each channel maintains an independent sequence counter,
     * matching WhatsApp Web's {@code SequenceNumberGenerator} which
     * creates per-channel counters.
     *
     * @param channel the transport channel
     * @return the next sequence number in {@code [1, 65535]}
     */
    private int nextSequenceNumber(WamChannel channel) {
        return sequenceNumbers.get(channel).getAndUpdate(current -> {
            var next = current + 1;
            return next > MAX_SEQUENCE_NUMBER ? 1 : next;
        });
    }

    /**
     * Waits for the client to be connected before attempting a buffer
     * upload, with a timeout of {@link #CONNECTIVITY_WAIT_TIMEOUT_MS}.
     *
     * <p>This matches WhatsApp Web's {@code waitIfOffline()} with a
     * 30-second timeout, preserving retry budget by not attempting
     * uploads while disconnected.
     */
    private void waitIfDisconnected() {
        if (client.isConnected()) {
            return;
        }

        var deadline = System.currentTimeMillis() + CONNECTIVITY_WAIT_TIMEOUT_MS;
        try {
            while (!client.isConnected() && System.currentTimeMillis() < deadline) {
                Thread.sleep(1_000);
            }
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Sends a WAM buffer via an XMPP {@code <iq>} stanza, retrying
     * with exponential backoff on transient server errors.
     *
     * <p>Before the first attempt, waits for connectivity if the client
     * is currently disconnected, matching WhatsApp Web's
     * {@code waitIfOffline()} behaviour.
     *
     * <p>The server response is inspected: a {@code type="result"}
     * response indicates success; a {@code type="error"} response with
     * a {@code 5xx} status code is retried; all other errors are
     * permanent and the buffer is dropped.
     *
     * @param buffer the encoded WAM buffer
     */
    private void sendWithRetry(byte[] buffer) {
        waitIfDisconnected();

        for (var attempt = 0; attempt <= MAX_RETRIES; attempt++) {
            try {
                var response = sendViaIq(buffer);
                var type = response.getAttributeAsString("type", "");
                if ("result".equals(type)) {
                    return;
                }

                var errorCode = parseErrorCode(response);
                if (errorCode >= 500 && attempt < MAX_RETRIES) {
                    var delay = computeBackoffDelay(attempt);
                    LOGGER.fine("WAM upload got " + errorCode + ", retrying in " + delay + "ms (attempt " + (attempt + 1) + ")");
                    Thread.sleep(delay);
                    continue;
                }

                LOGGER.warning("WAM upload failed with error " + errorCode + ", dropping buffer");
                // WAWebWam._executePending catch block: when upload fails
                // permanently, a WamClientErrorsWamEvent with
                // wamClientBufferDropErrorCount = 1 is committed to track
                // the dropped buffer.
                commit(new WamClientErrorsEventBuilder()
                        .wamClientBufferDropErrorCount(1)
                        .build());
                return;
            } catch (InterruptedException _) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception e) {
                if (attempt < MAX_RETRIES) {
                    try {
                        var delay = computeBackoffDelay(attempt);
                        LOGGER.fine("WAM upload failed (" + e.getMessage() + "), retrying in " + delay + "ms");
                        Thread.sleep(delay);
                    } catch (InterruptedException _) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                } else {
                    LOGGER.warning("WAM upload failed after " + MAX_RETRIES + " retries: " + e.getMessage());
                    // WAWebWam._executePending catch block: exhausted
                    // retries count as a dropped buffer for pipeline
                    // self-metrics.
                    commit(new WamClientErrorsEventBuilder()
                            .wamClientBufferDropErrorCount(1)
                            .build());
                }
            }
        }
    }

    /**
     * Sends a private-channel WAM buffer through the
     * {@link WamPrivateStatsUploader} with retry on transient server errors.
     *
     * <p>Mirrors the behaviour of the WA Web {@code privateStatsUpload}
     * module. Each attempt:
     *
     * <ol>
     *   <li>Acquires a fresh single-use authentication token via
     *       {@link WamPrivateStatsTokenIssuer} (the {@code <sign_credential>}
     *       IQ round-trip with the Ed25519 blinded-token VOPRF).</li>
     *   <li>POSTs a multipart {@code message} body to
     *       {@code https://dit.whatsapp.net/deidentified_telemetry} with
     *       the buffer authenticated by
     *       {@code HMAC-SHA256(sharedSecret, buffer)}.</li>
     * </ol>
     *
     * <p>Retry policy follows WA Web's classification:
     * {@code 200} returns immediately, {@code 500}/network errors retry up
     * to {@link #MAX_RETRIES} times with exponential backoff,
     * {@code 400}/{@code 401}/{@code 429}/other are permanent and the
     * buffer is dropped (with a {@code WamClientErrorsEvent} accounting
     * for the dropped buffer).
     *
     * @param buffer the encoded WAM buffer
     */
    private void sendPrivateWithRetry(byte[] buffer) {
        waitIfDisconnected();

        for (var attempt = 0; attempt <= MAX_RETRIES; attempt++) {
            var result = privateStatsUploader.upload(buffer);
            switch (result.result()) {
                case SUCCESS -> {
                    return;
                }
                case ERROR_SERVER_OTHER, ERROR_OTHER, ERROR_CREDENTIAL -> {
                    if (attempt < MAX_RETRIES) {
                        try {
                            var delay = computeBackoffDelay(attempt);
                            LOGGER.fine("Private WAM upload got " + result.result()
                                    + " (HTTP " + result.httpResponseCode() + "), retrying in "
                                    + delay + "ms (attempt " + (attempt + 1) + ")");
                            Thread.sleep(delay);
                        } catch (InterruptedException _) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                        continue;
                    }
                    LOGGER.warning("Private WAM upload failed after " + MAX_RETRIES
                            + " retries: " + result.result()
                            + " (HTTP " + result.httpResponseCode() + ")");
                    commit(new WamClientErrorsEventBuilder()
                            .wamClientBufferDropErrorCount(1)
                            .build());
                    return;
                }
                default -> {
                    LOGGER.warning("Private WAM upload failed permanently: " + result.result()
                            + " (HTTP " + result.httpResponseCode() + ")");
                    commit(new WamClientErrorsEventBuilder()
                            .wamClientBufferDropErrorCount(1)
                            .build());
                    return;
                }
            }
        }
    }

    /**
     * Sends a WAM buffer via an XMPP {@code <iq>} stanza and waits
     * for the server response.
     *
     * @param buffer the encoded WAM buffer
     * @return the server response node
     */
    private Node sendViaIq(byte[] buffer) {
        var add = new NodeBuilder()
                .description("add")
                .attribute("t", String.valueOf(Instant.now().getEpochSecond()))
                .content(buffer)
                .build();
        var iq = new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "w:stats")
                .attribute("to", "s.whatsapp.net")
                .attribute("type", "set")
                .content(add);
        return client.sendNode(iq);
    }

    /**
     * Parses the HTTP-style error code from an IQ error response.
     *
     * <p>The error code is extracted from the {@code code} attribute of
     * a child {@code <error>} element. If no such element exists,
     * returns {@code 0}.
     *
     * @param response the server response node
     * @return the error code, or {@code 0} if not found
     */
    private static int parseErrorCode(Node response) {
        var children = response.getChildren("error");
        if (children.isEmpty()) {
            return 0;
        }
        var error = children.getFirst();
        return (int) error.getAttributeAsLong("code", 0L);
    }

    /**
     * Computes the backoff delay for the given retry attempt using
     * exponential backoff with 10% jitter. Matches the JS implementation
     * bug where Math.pow(2, attempt) does not multiply by base delay.
     *
     * @param attempt the zero-based retry attempt number
     * @return the delay in milliseconds
     */
    private static long computeBackoffDelay(int attempt) {
        var delay = attempt == 0 ? RETRY_BASE_DELAY_MS : (long) Math.pow(2, attempt);
        if (delay > RETRY_MAX_DELAY_MS) delay = RETRY_MAX_DELAY_MS;
        if (delay < RETRY_BASE_DELAY_MS) delay = RETRY_BASE_DELAY_MS;
        var jitter = (long) (delay * 0.1 * DataUtils.randomDouble());
        return delay + jitter;
    }

    /**
     * Returns the {@code webcWebArch} push-phase string for the current
     * build, or {@code null} if no phase is configured.
     *
     * <p>The JS implementation maps the build constant
     * {@code PUSH_PHASE} through a fixed alias table:
     * {@code "sandcastle" -> "dev"}, {@code "trunkstable" -> "C1"};
     * unmapped phases pass through. When the {@code 26256} gatekeeper is
     * set the value is forced to {@code "jest-e2e"}.
     *
     * @implNote Cobalt is a third-party client without an internal
     *     {@code PUSH_PHASE} build constant or jest-e2e harness, so this
     *     method always returns {@code null} and {@code webcWebArch}
     *     remains absent from emitted globals.
     * @return the push-phase string, or {@code null} when not applicable
     */
    @WhatsAppWebExport(moduleName = "WAWebWam", exports = "getPushPhase", adaptation = WhatsAppAdaptation.ADAPTED)
    private static String getPushPhase() {
        // WAWebWam.ie: alias map sandcastle->dev, trunkstable->C1; gkx 26256 -> jest-e2e.
        // Cobalt has no PUSH_PHASE build constant nor jest-e2e harness.
        return null;
    }

    /**
     * Stops the flush threads and performs a final flush of all pending
     * events.
     */
    public void close() {
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
        flush();
        initialized = false;
    }

    /**
     * Returns the WAM {@link MediaType} classification for the payload
     * carried by the given {@link ChatMessageInfo}.
     *
     * <p>The method delegates to {@link #getWamMediaType(MessageContainer)}
     * after resolving the wrapped message container. {@link MediaType#NONE}
     * is returned for unrecognised or unclassified message types.
     *
     * @param info the chat message info whose payload is being
     *             classified; may be {@code null}
     * @return the WAM media-type classification for the resolved
     * payload, or {@link MediaType#NONE} if no matching classification
     * exists
     *
     * @implNote WAWebWamMsgUtils.getWamMediaType accepts a {@code Msg}
     * model; Cobalt's closest equivalent is {@link ChatMessageInfo},
     * so the helper unwraps {@link ChatMessageInfo#message()} before
     * forwarding to the container-level overload.
     */
    @WhatsAppWebExport(moduleName = "WAWebWamMsgUtils", exports = "getWamMediaType",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public MediaType getWamMediaType(ChatMessageInfo info) {
        return info == null ? MediaType.NONE : getWamMediaType(info.message());
    }

    /**
     * Returns the WAM {@link MediaType} classification for the resolved
     * content of the given {@link MessageContainer}.
     *
     * <p>The method mirrors WA Web's {@code getWamMediaType} switch table:
     * every branch of the WA Web function corresponds to an
     * {@code instanceof} check here. The mapping covers the common media
     * payloads used in PSA-style flows (image, video, GIF, document,
     * audio, PTT, sticker) plus the placeholder entries for the fallback
     * categories. Unrecognised types fall back to {@link MediaType#NONE}.
     *
     * @param container the container whose resolved content should be
     *                  classified; {@code null} yields
     *                  {@link MediaType#NONE}
     * @return the WAM media-type classification for the resolved
     * payload
     *
     * @implNote Adapts the {@code switch (e.type)} cascade in
     * {@code WAWebWamMsgUtils.getWamMediaType}. Only the branches that
     * Cobalt's message model can currently distinguish are implemented;
     * payloads that have no dedicated Cobalt counterpart fall through to
     * {@link MediaType#NONE}.
     */
    @WhatsAppWebExport(moduleName = "WAWebWamMsgUtils", exports = "getWamMediaType",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public MediaType getWamMediaType(MessageContainer container) {
        if (container == null) {
            return MediaType.NONE;
        }
        var content = container.content();
        return switch (content) {
            case ImageMessage ignored -> MediaType.PHOTO; // WAWebWamMsgUtils: case "image" -> MEDIA_TYPE.PHOTO
            case VideoMessage video -> video.gifPlayback() // WAWebWamMsgUtils: case "video" -> e.isGif ? GIF : VIDEO
                    ? MediaType.GIF
                    : MediaType.VIDEO;
            case AudioMessage audio -> audio.ptt() // WAWebWamMsgUtils: case "audio" -> AUDIO; case "ptt" -> PTT
                    ? MediaType.PTT
                    : MediaType.AUDIO;
            case DocumentMessage ignored -> MediaType.DOCUMENT; // WAWebWamMsgUtils: case "document" -> DOCUMENT
            case StickerMessage ignored -> MediaType.STICKER; // WAWebWamMsgUtils: case "sticker" -> STICKER
            case StickerPackMessage ignored -> MediaType.STICKER_PACK; // WAWebWamMsgUtils: case "sticker-pack" -> STICKER_PACK
            case ReactionMessage ignored -> MediaType.REACTION; // WAWebWamMsgUtils: case "reaction"/"reaction_enc" -> REACTION
            case EncReactionMessage ignored -> MediaType.REACTION; // WAWebWamMsgUtils: case "reaction_enc" -> REACTION
            case PollCreationMessage ignored -> MediaType.POLL_CREATE; // WAWebWamMsgUtils: case "poll_creation" -> POLL_CREATE
            case PollUpdateMessage ignored -> MediaType.POLL_VOTE; // WAWebWamMsgUtils: case "poll_update"/"poll_vote" -> POLL_VOTE
            case ContactMessage ignored -> MediaType.CONTACT; // WAWebWamMsgUtils: case "vcard" -> CONTACT
            case ContactsArrayMessage ignored -> MediaType.CONTACT_ARRAY; // WAWebWamMsgUtils: case "multi_vcard" -> CONTACT_ARRAY
            case LocationMessage ignored ->
                    MediaType.LOCATION; // WAWebWamMsgUtils: case "location" (non-live) -> LOCATION
            case LiveLocationMessage ignored -> MediaType.LIVE_LOCATION; // WAWebWamMsgUtils: case "location" when isLive -> LIVE_LOCATION
            case ProductMessage ignored -> MediaType.PRODUCT_IMAGE; // WAWebWamMsgUtils: case "product" -> PRODUCT_IMAGE
            case ListMessage ignored -> MediaType.LIST; // WAWebWamMsgUtils: case "list" (SINGLE_SELECT) -> LIST
            case ListResponseMessage ignored -> MediaType.LIST_REPLY; // WAWebWamMsgUtils: case "list_response" -> LIST_REPLY
            case OrderMessage ignored -> MediaType.ORDER; // WAWebWamMsgUtils: case "order" -> ORDER
            case EventResponseMessage ignored -> MediaType.EVENT_RESPOND; // WAWebWamMsgUtils: case "event_response" -> EVENT_RESPOND
            case EncEventResponseMessage ignored -> MediaType.EVENT_RESPOND; // WAWebWamMsgUtils: case "event_response" -> EVENT_RESPOND
            case EventMessage ignored -> MediaType.EVENT_CREATE; // WAWebWamMsgUtils: case "event_creation" -> EVENT_CREATE
            case AlbumMessage ignored -> MediaType.MEDIA_ALBUM; // WAWebWamMsgUtils: case "album" -> MEDIA_ALBUM
            case PinInChatMessage ignored -> MediaType.PIN_IN_CHAT; // WAWebWamMsgUtils: case "pin_message" -> PIN_IN_CHAT
            case ExtendedTextMessage ignored -> MediaType.TEXT; // WAWebWamMsgUtils: case "chat" -> default TEXT branch in m(e.matchedText)
            case null, default -> MediaType.NONE; // WAWebWamMsgUtils: default -> MEDIA_TYPE.NONE
        };
    }

    /**
     * Returns the WAM {@link MessageType} classification derived from the
     * chat JID carried by the given {@link ChatMessageInfo}.
     *
     * <p>The method mirrors WA Web's {@code WAWebWamMsgUtils.getWamMessageType}
     * switch on {@code e.isStatus() / e.isGroupMsg() / isBroadcast(e.id.remote)
     * / isNewsletter(e.id.remote)} fallback to
     * {@link MessageType#INDIVIDUAL}. The {@code STATUS} branch is
     * disambiguated before {@code BROADCAST} because status messages live on
     * the broadcast server but must not be reported as generic broadcasts.
     *
     * @param info the chat message info whose destination is being
     *             classified; {@code null} yields {@link MessageType#INDIVIDUAL}
     * @return the WAM message-type classification, defaulting to
     * {@link MessageType#INDIVIDUAL} when the destination does not match any
     * recognised server category
     *
     * @implNote Adapts the flat {@code if} cascade in
     * {@code WAWebWamMsgUtils.getWamMessageType}. Cobalt's message model does
     * not expose {@code getBroadcastId()} / non-status broadcasts distinctly,
     * so the helper treats any non-status broadcast JID as
     * {@link MessageType#BROADCAST}.
     */
    @WhatsAppWebExport(moduleName = "WAWebWamMsgUtils", exports = "getWamMessageType",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public MessageType getWamMessageType(ChatMessageInfo info) {
        if (info == null) {
            return MessageType.INDIVIDUAL;
        }
        var parent = info.key().parentJid().orElse(null);
        if (parent == null) {
            return MessageType.INDIVIDUAL;
        }
        return getWamMessageType(parent);
    }

    /**
     * Returns the WAM {@link MessageType} classification derived from the
     * given chat JID.
     *
     * <p>The method is the JID-level fan-out of
     * {@link #getWamMessageType(ChatMessageInfo)}: it classifies purely based
     * on the server component of the supplied JID.
     *
     * @param chatJid the chat JID whose server is being classified;
     *                {@code null} yields {@link MessageType#INDIVIDUAL}
     * @return the WAM message-type classification, defaulting to
     * {@link MessageType#INDIVIDUAL} when the server is a user / LID / bot
     * domain or unrecognised
     *
     * @implNote Adapts the {@code WAWebWamMsgUtils.getWamMessageType} fallback
     * table; the {@code STATUS} check precedes the {@code BROADCAST} check to
     * match WA Web's disambiguation order.
     */
    @WhatsAppWebExport(moduleName = "WAWebWamMsgUtils", exports = "getWamMessageType",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public MessageType getWamMessageType(Jid chatJid) {
        if (chatJid == null) {
            return MessageType.INDIVIDUAL;
        }
        if (chatJid.isStatusBroadcastAccount()) { // WAWebMsgGetters.getIsStatus -> MESSAGE_TYPE.STATUS
            return MessageType.STATUS;
        }
        if (chatJid.hasGroupOrCommunityServer()) { // WAWebMsgGetters.getIsGroupMsg -> MESSAGE_TYPE.GROUP
            return MessageType.GROUP;
        }
        if (chatJid.hasBroadcastServer()) { // WAWebWid.isBroadcast -> MESSAGE_TYPE.BROADCAST
            return MessageType.BROADCAST;
        }
        if (chatJid.hasNewsletterServer()) { // WAWebWid.isNewsletter -> MESSAGE_TYPE.CHANNEL
            return MessageType.CHANNEL;
        }
        return MessageType.INDIVIDUAL; // WAWebWamMsgUtils: default -> MESSAGE_TYPE.INDIVIDUAL
    }

    /**
     * Returns the WAM {@link MessageType} classification derived from the
     * stanza-level {@link com.github.auties00.cobalt.message.receive.stanza.MessageType}
     * enum produced during parsing.
     *
     * <p>WA Web feeds {@code msgInfo.type} (a string of {@code chat /
     * group / peer_broadcast / other_broadcast / direct_peer_status /
     * other_status}) into {@code getMessageTypeFromMsgInfoType}, which
     * normalises broadcasts into {@link MessageType#BROADCAST} and status
     * flavours into {@link MessageType#STATUS}. Cobalt classifies the
     * stanza once during parsing into
     * {@link com.github.auties00.cobalt.message.receive.stanza.MessageType},
     * so this helper performs the equivalent normalisation over that enum
     * directly.
     *
     * @param stanzaType the parser-level message type; {@code null}
     *                   yields {@link MessageType#INDIVIDUAL}
     * @return the WAM message-type classification, defaulting to
     * {@link MessageType#INDIVIDUAL} when the parser-level type maps to
     * {@code CHAT} or {@code PEER_CHAT} or is {@code null}
     *
     * @implNote Adapts {@code WAWebWamMsgUtils.getMessageTypeFromMsgInfoType}.
     * The WA Web function throws on unmatched values; Cobalt falls back to
     * {@link MessageType#INDIVIDUAL} so callers in the receive pipeline
     * cannot crash on unexpected enum values introduced upstream.
     */
    @WhatsAppWebExport(moduleName = "WAWebWamMsgUtils", exports = "getMessageTypeFromMsgInfoType",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public MessageType getWamMessageTypeFromStanzaType(
            com.github.auties00.cobalt.message.receive.stanza.MessageType stanzaType
    ) {
        if (stanzaType == null) {
            return MessageType.INDIVIDUAL;
        }
        return switch (stanzaType) {
            // WAWebWamMsgUtils.getMessageTypeFromMsgInfoType: "chat" -> INDIVIDUAL
            case CHAT, PEER_CHAT -> MessageType.INDIVIDUAL;
            // WAWebWamMsgUtils.getMessageTypeFromMsgInfoType: "group" -> GROUP
            case GROUP -> MessageType.GROUP;
            // WAWebWamMsgUtils.getMessageTypeFromMsgInfoType: "peer_broadcast"/"other_broadcast" -> BROADCAST
            case PEER_BROADCAST, OTHER_BROADCAST -> MessageType.BROADCAST;
            // WAWebWamMsgUtils.getMessageTypeFromMsgInfoType: "direct_peer_status"/"other_status" -> STATUS
            case DIRECT_PEER_STATUS, OTHER_STATUS -> MessageType.STATUS;
        };
    }

    /**
     * Returns the WAM {@link E2eDeviceType} classification of the sender
     * JID relative to the current account.
     *
     * <p>The classification tree mirrors WA Web's {@code getWamE2eSenderType}:
     * the sender is first bucketed as {@code MY} (current account) or
     * {@code OTHER} based on whether its user JID matches the stored
     * self-JID; within each bucket the sender is further classified as
     * {@code PRIMARY}, {@code COMPANION}, or {@code HOSTED_COMPANION}
     * based on the device id and server domain.
     *
     * @param senderJid the sender's full device JID; {@code null} yields
     *                  {@code null}
     * @param selfJid   the logged-in account's primary JID; may be
     *                  {@code null} when the account is not yet bound
     * @return the WAM classification, or {@code null} when the sender is
     * not a user/LID JID, matching WA Web's {@code instanceof Wid} guard
     *
     * @implNote Adapts {@code WAWebWamMsgUtils.getWamE2eSenderType}.
     * WA Web's {@code e instanceof Wid} gate is approximated here by
     * accepting any non-null JID: the receive pipeline only ever calls
     * the helper with a resolved sender JID parsed from a stanza, so the
     * check is redundant. {@code isMeAccount} is realised through the
     * {@code selfJid.toUserJid().equals(senderJid.toUserJid())} check
     * that the rest of the receive pipeline uses.
     */
    @WhatsAppWebExport(moduleName = "WAWebWamMsgUtils", exports = "getWamE2eSenderType",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public E2eDeviceType getWamE2eSenderType(Jid senderJid, Jid selfJid) {
        if (senderJid == null) {
            return null;
        }
        // WAWebWamMsgUtils.getWamE2eSenderType: e instanceof Wid filter; Cobalt only accepts user/LID JIDs here
        if (!senderJid.hasUserServer() && !senderJid.hasLidServer()
                && !senderJid.hasHostedServer() && !senderJid.hasHostedLidServer()) {
            return null;
        }
        var isMe = selfJid != null
                && selfJid.toUserJid().equals(senderJid.toUserJid());
        var isCompanion = senderJid.hasDevice(); // WAWebWid.isCompanion: device != 0
        var isHosted = senderJid.hasHostedServer() || senderJid.hasHostedLidServer();
        if (isMe) {
            if (isCompanion) {
                // WAWebWamMsgUtils.getWamE2eSenderType: MY + companion + hosted -> MY_HOSTED_COMPANION
                return isHosted ? E2eDeviceType.MY_HOSTED_COMPANION : E2eDeviceType.MY_COMPANION;
            }
            // WAWebWamMsgUtils.getWamE2eSenderType: MY + primary -> MY_PRIMARY
            return E2eDeviceType.MY_PRIMARY;
        }
        if (isCompanion) {
            // WAWebWamMsgUtils.getWamE2eSenderType: OTHER + companion + hosted -> OTHER_HOSTED_COMPANION
            return isHosted ? E2eDeviceType.OTHER_HOSTED_COMPANION : E2eDeviceType.OTHER_COMPANION;
        }
        // WAWebWamMsgUtils.getWamE2eSenderType: OTHER + primary -> OTHER_PRIMARY
        return E2eDeviceType.OTHER_PRIMARY;
    }

    /**
     * Returns the WAM {@link MediaType} classification for an interactive
     * message based on its body variant.
     *
     * <p>The classification mirrors WA Web's {@code getInteractiveWamType}:
     * shop storefront variants map to {@link MediaType#SHOP_STOREFRONT},
     * carousels map to {@link MediaType#INTERACTIVE_CAROUSEL}, and native
     * flow variants delegate to the inner native-flow disambiguator that
     * separates {@code CTA_FLOW} ({@link MediaType#NONE}) from any other
     * native flow ({@link MediaType#INTERACTIVE_NFM}).
     *
     * @param interactive the interactive message whose body variant is
     *                    being classified; {@code null} yields
     *                    {@link MediaType#NONE}
     * @return the WAM media-type classification for the interactive
     * variant, or {@link MediaType#NONE} when no variant is set
     *
     * @implNote Adapts {@code WAWebWamMsgUtils.getInteractiveWamType}.
     * WA Web reads {@code e.interactiveType} (a string from
     * {@code WAWebInteractiveMessageType}); Cobalt switches on the
     * {@link InteractiveMessageContent} sealed hierarchy returned by
     * {@link InteractiveMessage#content()}, which carries the same
     * variant information.
     */
    @WhatsAppWebExport(moduleName = "WAWebWamMsgUtils", exports = "getInteractiveWamType",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public MediaType getInteractiveWamType(InteractiveMessage interactive) {
        if (interactive == null) {
            return MediaType.NONE;
        }
        InteractiveMessageContent content = interactive.content().orElse(null);
        if (content == null) {
            // WAWebWamMsgUtils.getInteractiveWamType: t == null -> MEDIA_TYPE.NONE
            return MediaType.NONE;
        }
        return switch (content) {
            // WAWebWamMsgUtils.getInteractiveWamType: case SHOPS_STOREFRONT -> SHOP_STOREFRONT
            case InteractiveMessage.ShopMessage ignored -> MediaType.SHOP_STOREFRONT;
            // WAWebWamMsgUtils.getInteractiveWamType: case CAROUSEL -> INTERACTIVE_CAROUSEL
            case InteractiveMessage.CarouselMessage ignored -> MediaType.INTERACTIVE_CAROUSEL;
            // WAWebWamMsgUtils.getInteractiveWamType: case NATIVE_FLOW -> d(e)
            case InteractiveMessage.NativeFlowMessage native_ -> getInteractiveNativeFlowWamType(native_);
            // Cobalt-only branch: collection messages have no WA Web counterpart in this mapper
            case InteractiveMessage.CollectionMessage ignored -> MediaType.NONE;
        };
    }

    /**
     * Disambiguates the WAM {@link MediaType} for an interactive native
     * flow message based on the resolved native flow name.
     *
     * <p>WA Web treats the {@code CTA_FLOW} (galaxy) variant as
     * {@link MediaType#NONE} since it is filtered from interactive WAM
     * reporting; any other native flow name falls through to
     * {@link MediaType#INTERACTIVE_NFM}.
     *
     * @param nativeFlow the native flow message whose name drives the
     *                   classification; must not be {@code null}
     * @return {@link MediaType#NONE} when the native flow is the
     * {@code CTA_FLOW} (galaxy) variant, otherwise
     * {@link MediaType#INTERACTIVE_NFM}
     *
     * @implNote Adapts {@code WAWebWamMsgUtils.d}, the inner helper that
     * post-processes a native flow interactive payload.
     * {@code WAWebInteractiveMessagesNativeFlowName.CTA_FLOW} resolves to
     * the literal {@code "galaxy_message"}, which Cobalt matches via the
     * first button name on the native flow message.
     */
    @WhatsAppWebExport(moduleName = "WAWebWamMsgUtils", exports = "getInteractiveWamType",
            adaptation = WhatsAppAdaptation.ADAPTED)
    private static MediaType getInteractiveNativeFlowWamType(InteractiveMessage.NativeFlowMessage nativeFlow) {
        for (var button : nativeFlow.buttons()) {
            var name = button.name().orElse(null);
            // WAWebInteractiveMessagesNativeFlowName.CTA_FLOW -> "galaxy_message"
            if ("galaxy_message".equals(name)) {
                return MediaType.NONE;
            }
        }
        return MediaType.INTERACTIVE_NFM;
    }

    /**
     * Returns the WAM {@link AgentEngagementEnumType} classification for a
     * message exchanged with a bot.
     *
     * <p>The classification mirrors WA Web's {@code getWamAgentEngagementType}:
     * messages whose chat JID is itself a bot map to
     * {@link AgentEngagementEnumType#DIRECT_CHAT}; messages whose payload is
     * recognised as a bot query or a Meta-bot response map to
     * {@link AgentEngagementEnumType#INVOKED}; otherwise the helper returns
     * {@code null} so callers can omit the property from the WAM event.
     *
     * @param chatJid       the chat JID that hosts the message; {@code null}
     *                      yields {@code null}
     * @param isBotInvoked  {@code true} when the message originates from a
     *                      bot query or Meta-bot response; this corresponds
     *                      to the disjunction of WA Web's
     *                      {@code getIsBotQuery} and {@code getIsMetaBotResponse}
     * @return the WAM agent-engagement classification, or {@code null}
     * when the message is unrelated to a bot conversation
     *
     * @implNote Adapts {@code WAWebWamMsgUtils.getWamAgentEngagementType}.
     * Cobalt does not track {@code isBotQuery} / {@code isMetaBotResponse}
     * on its message model, so the disjunction is exposed as an explicit
     * caller-supplied flag rather than recomputed here. The
     * {@code remote.isBot()} guard is realised through {@link Jid#isBot()}.
     */
    @WhatsAppWebExport(moduleName = "WAWebWamMsgUtils", exports = "getWamAgentEngagementType",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public AgentEngagementEnumType getWamAgentEngagementType(Jid chatJid, boolean isBotInvoked) {
        if (chatJid == null) {
            return null;
        }
        if (chatJid.isBot()) {
            // WAWebWamMsgUtils.getWamAgentEngagementType: e.id.remote.isBot() -> DIRECT_CHAT
            return AgentEngagementEnumType.DIRECT_CHAT;
        }
        if (isBotInvoked) {
            // WAWebWamMsgUtils.getWamAgentEngagementType: getIsBotQuery || getIsMetaBotResponse -> INVOKED
            return AgentEngagementEnumType.INVOKED;
        }
        return null;
    }

    /**
     * Returns the WAM {@link BotType} classification of a bot interaction.
     *
     * <p>The classification mirrors WA Web's {@code getWamBotType}: a
     * Meta-bot JID maps to {@link BotType#METABOT}; a 1P business bot
     * (either via the {@code BizBotType.BIZ_1P} flag or the
     * {@code BizBotAutomatedType.PARTIAL_1P} automated flag) maps to
     * {@link BotType#BOT_1P_BIZ}; a 3P business bot (via
     * {@code BizBotType.BIZ_3P} or {@code BizBotAutomatedType.FULL_3P})
     * maps to {@link BotType#BOT_3P_BIZ}; everything else falls through to
     * {@link BotType#UNKNOWN}.
     *
     * @param botJid        the JID involved in the bot interaction;
     *                      may be {@code null}
     * @param is1pBizBot    {@code true} when WA Web would classify the
     *                      interaction as {@code BizBotType.BIZ_1P} or
     *                      {@code BizBotAutomatedType.PARTIAL_1P}
     * @param is3pBizBot    {@code true} when WA Web would classify the
     *                      interaction as {@code BizBotType.BIZ_3P} or
     *                      {@code BizBotAutomatedType.FULL_3P}
     * @return the matching WAM bot type, defaulting to
     * {@link BotType#UNKNOWN}
     *
     * @implNote Adapts {@code WAWebWamMsgUtils.getWamBotType}. Cobalt does
     * not currently model {@code BizBotType} or {@code BizBotAutomatedType}
     * as Java enums, so the two boolean flags are exposed in their stead;
     * any future {@code BizBotType} enum can be added here without
     * disturbing callers.
     */
    @WhatsAppWebExport(moduleName = "WAWebWamMsgUtils", exports = "getWamBotType",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public BotType getWamBotType(Jid botJid, boolean is1pBizBot, boolean is3pBizBot) {
        if (botJid != null && botJid.isBot()) {
            // WAWebWamMsgUtils.getWamBotType: e?.isBot() -> METABOT
            return BotType.METABOT;
        }
        if (is1pBizBot) {
            // WAWebWamMsgUtils.getWamBotType: BizBotType.BIZ_1P / BizBotAutomatedType.PARTIAL_1P -> BOT_1P_BIZ
            return BotType.BOT_1P_BIZ;
        }
        if (is3pBizBot) {
            // WAWebWamMsgUtils.getWamBotType: BizBotType.BIZ_3P / BizBotAutomatedType.FULL_3P -> BOT_3P_BIZ
            return BotType.BOT_3P_BIZ;
        }
        // WAWebWamMsgUtils.getWamBotType: default -> UNKNOWN
        return BotType.UNKNOWN;
    }

    /**
     * Returns the WAM {@link InvisibleMessageCategoryType} classification
     * for the supplied stanza-level message category attribute.
     *
     * <p>WA Web defines a single recognised category, {@code MSG_CATEGORY.peer},
     * which maps to {@link InvisibleMessageCategoryType#PEER}; any other
     * value (including {@code null} and the empty string) yields
     * {@code null} so callers omit the property from the WAM event.
     *
     * @param category the {@code category} attribute carried on the
     *                 incoming stanza; may be {@code null}
     * @return {@link InvisibleMessageCategoryType#PEER} for {@code "peer"},
     * otherwise {@code null}
     *
     * @implNote Adapts {@code WAWebWamMsgUtils.getWamInvisibleMessageCatgoryType}.
     * The helper preserves WA Web's misspelling of {@code Catgory} in the
     * export name while exposing a corrected Java method name. The category
     * lookup uses the {@code WAWebHandleMsgCommon.MSG_CATEGORY} constants;
     * Cobalt mirrors that table inline because the only recognised value
     * is the literal {@code "peer"}.
     */
    @WhatsAppWebExport(moduleName = "WAWebWamMsgUtils", exports = "getWamInvisibleMessageCatgoryType",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public InvisibleMessageCategoryType getWamInvisibleMessageCategoryType(String category) {
        if (category == null || category.isEmpty()) {
            return null;
        }
        // WAWebHandleMsgCommon.MSG_CATEGORY.peer -> INVISIBLE_MESSAGE_CATEGORY_TYPE.PEER
        if ("peer".equals(category)) {
            return InvisibleMessageCategoryType.PEER;
        }
        return null;
    }

    /**
     * Returns whether any of the JIDs that participated in the message
     * exchange are LID-addressed.
     *
     * <p>WA Web's {@code msgIsLid} returns:
     * <ul>
     *   <li>for groups: the supplied {@code participantIsLid} flag
     *       (truthy-coerced).</li>
     *   <li>for status updates: whether the
     *       {@code key.id.participant} is a LID JID; missing values
     *       evaluate to {@code false}.</li>
     *   <li>otherwise: whether either the {@code from} or the {@code to}
     *       JID is LID-addressed.</li>
     * </ul>
     *
     * @param fromJid             the sender JID; may be {@code null}
     * @param toJid               the recipient JID; may be {@code null}
     * @param keyParticipantJid   the {@code key.participant} JID, used for
     *                            status updates; may be {@code null}
     * @param chatType            the WAM message-type classification of
     *                            the chat (used to disambiguate group /
     *                            status / other); must not be {@code null}
     * @param participantIsLid    {@code true} when the {@code participant}
     *                            attribute on a group stanza is LID-addressed
     * @return {@code true} when the relevant JID for the chat type is
     * LID-addressed, otherwise {@code false}
     *
     * @implNote Adapts {@code WAWebWamMsgUtils.msgIsLid}. The
     * {@code chatType.isGroup() / chatType.isStatus()} branch checks are
     * recovered here through the WAM {@link MessageType} classification
     * already produced by {@link #getWamMessageType(Jid)}, so callers do
     * not need a {@code Wid}-style helper. The fall-through case checks
     * both endpoints, matching WA Web's {@code e.from.isLid() || e.to.isLid()}.
     */
    @WhatsAppWebExport(moduleName = "WAWebWamMsgUtils", exports = "msgIsLid",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public boolean msgIsLid(
            Jid fromJid,
            Jid toJid,
            Jid keyParticipantJid,
            MessageType chatType,
            boolean participantIsLid
    ) {
        if (chatType == MessageType.GROUP) {
            // WAWebWamMsgUtils.msgIsLid: t.isGroup() -> !!n
            return participantIsLid;
        }
        if (chatType == MessageType.STATUS) {
            // WAWebWamMsgUtils.msgIsLid: t.isStatus() -> e.id.participant?.isLid() ?? false
            return keyParticipantJid != null && keyParticipantJid.hasLidServer();
        }
        // WAWebWamMsgUtils.msgIsLid: default -> e.from.isLid() || e.to.isLid()
        var fromIsLid = fromJid != null && fromJid.hasLidServer();
        var toIsLid = toJid != null && toJid.hasLidServer();
        return fromIsLid || toIsLid;
    }

    /**
     * Fixed mapping from lower-case file extensions (without the leading
     * dot) to the corresponding WAM {@link DocumentType} bucket.
     *
     * <p>Populated verbatim from WA Web's
     * {@code WAWebProcessRawMediaLogging} extension table so that
     * {@link #logSendDocumentEvent(String, long)} produces identical
     * {@code documentType} / {@code documentExt} values for every known
     * extension. Unknown extensions fall back to
     * {@link DocumentType#OTHER} with an empty {@code documentExt}, again
     * mirroring WA Web.
     *
     * @implNote WAWebProcessRawMediaLogging: module-level {@code Map}
     * literal that seeds the classification.
     */
    @WhatsAppWebExport(moduleName = "WAWebProcessRawMediaLogging", exports = "default",
            adaptation = WhatsAppAdaptation.DIRECT)
    private static final Map<String, DocumentType> DOCUMENT_EXT_TO_TYPE = Map.<String, DocumentType>ofEntries(
            Map.entry("ai", DocumentType.IMAGE),
            Map.entry("ico", DocumentType.IMAGE),
            Map.entry("jpeg", DocumentType.IMAGE),
            Map.entry("jpg", DocumentType.IMAGE),
            Map.entry("png", DocumentType.IMAGE),
            Map.entry("ps", DocumentType.IMAGE),
            Map.entry("psd", DocumentType.IMAGE),
            Map.entry("svg", DocumentType.IMAGE),
            Map.entry("tif", DocumentType.IMAGE),
            Map.entry("tiff", DocumentType.IMAGE),
            Map.entry("3g2", DocumentType.VIDEO),
            Map.entry("3gp", DocumentType.VIDEO),
            Map.entry("avi", DocumentType.VIDEO),
            Map.entry("flv", DocumentType.VIDEO),
            Map.entry("h264", DocumentType.VIDEO),
            Map.entry("m4v", DocumentType.VIDEO),
            Map.entry("mkv", DocumentType.VIDEO),
            Map.entry("mov", DocumentType.VIDEO),
            Map.entry("mp4", DocumentType.VIDEO),
            Map.entry("mpg", DocumentType.VIDEO),
            Map.entry("mpeg", DocumentType.VIDEO),
            Map.entry("rm", DocumentType.VIDEO),
            Map.entry("vob", DocumentType.VIDEO),
            // WAWebProcessRawMediaLogging: ["wmv", AUDIO] is exactly what the WA Web table has;
            // the classification is kept even though wmv is conventionally a video container.
            Map.entry("wmv", DocumentType.AUDIO),
            Map.entry("aif", DocumentType.AUDIO),
            Map.entry("cda", DocumentType.AUDIO),
            Map.entry("mpa", DocumentType.AUDIO),
            Map.entry("opus", DocumentType.AUDIO),
            Map.entry("ogg", DocumentType.AUDIO),
            Map.entry("wlp", DocumentType.AUDIO),
            Map.entry("amr", DocumentType.AUDIO),
            Map.entry("mp3", DocumentType.AUDIO),
            Map.entry("m4a", DocumentType.AUDIO),
            Map.entry("aac", DocumentType.AUDIO),
            Map.entry("wav", DocumentType.AUDIO),
            Map.entry("wma", DocumentType.AUDIO),
            Map.entry("pdf", DocumentType.DOCUMENT),
            Map.entry("doc", DocumentType.DOCUMENT),
            Map.entry("docx", DocumentType.DOCUMENT),
            Map.entry("ppt", DocumentType.DOCUMENT),
            Map.entry("pptx", DocumentType.DOCUMENT),
            Map.entry("xls", DocumentType.DOCUMENT),
            Map.entry("xlsx", DocumentType.DOCUMENT),
            Map.entry("txt", DocumentType.DOCUMENT),
            Map.entry("rtf", DocumentType.DOCUMENT),
            Map.entry("tex", DocumentType.DOCUMENT),
            Map.entry("csv", DocumentType.DOCUMENT),
            Map.entry("wpd", DocumentType.DOCUMENT),
            Map.entry("7z", DocumentType.COMPRESSED_FILE),
            Map.entry("arj", DocumentType.COMPRESSED_FILE),
            Map.entry("deb", DocumentType.COMPRESSED_FILE),
            Map.entry("pkg", DocumentType.COMPRESSED_FILE),
            Map.entry("rar", DocumentType.COMPRESSED_FILE),
            Map.entry("rpm", DocumentType.COMPRESSED_FILE),
            Map.entry("gz", DocumentType.COMPRESSED_FILE),
            Map.entry("z", DocumentType.COMPRESSED_FILE),
            Map.entry("zip", DocumentType.COMPRESSED_FILE),
            Map.entry("apk", DocumentType.EXECUTABLE),
            Map.entry("bat", DocumentType.EXECUTABLE),
            Map.entry("bin", DocumentType.EXECUTABLE),
            Map.entry("cgi", DocumentType.EXECUTABLE),
            Map.entry("pl", DocumentType.EXECUTABLE),
            Map.entry("com", DocumentType.EXECUTABLE),
            Map.entry("exe", DocumentType.EXECUTABLE),
            Map.entry("gadget", DocumentType.EXECUTABLE),
            Map.entry("jar", DocumentType.EXECUTABLE),
            Map.entry("msi", DocumentType.EXECUTABLE),
            Map.entry("py", DocumentType.EXECUTABLE),
            Map.entry("wsf", DocumentType.EXECUTABLE)
    );

    /**
     * Commits the {@code SendDocumentEvent} (id 2172) for an outgoing
     * document send.
     *
     * <p>Mirrors WA Web's {@code WAWebProcessRawMediaLogging.logSendDocumentEvent}:
     * splits the filename on {@code .} and takes the last segment as the
     * extension, looks it up in {@link #DOCUMENT_EXT_TO_TYPE} to resolve
     * the {@link DocumentType}, and populates {@code documentSize} with
     * the raw file size in bytes. When the filename has no extension or
     * the extension is not a known key, the {@code documentExt} property
     * is emitted as the empty string and the type falls back to
     * {@link DocumentType#OTHER}, matching WA Web.
     *
     * <p>The {@code documentPageSize} WAM property is declared in the
     * event spec but never populated by WA Web's emission site, so it is
     * intentionally left unset here too.
     *
     * @param filename   the user-visible document filename; when
     *                   {@code null} the extension is resolved as the
     *                   empty string, matching WA Web's
     *                   {@code e?.split(".").pop() ?? ""} fallback
     * @param size       the raw decrypted document size in bytes
     *
     * @implNote WAWebProcessRawMediaLogging.logSendDocumentEvent: the
     * single call site that constructs and commits the event, invoked by
     * {@code WAWebProcessRawMedia.processRawMedia} when the selected
     * media is classified as {@code "document"}.
     */
    @WhatsAppWebExport(moduleName = "WAWebProcessRawMediaLogging", exports = "logSendDocumentEvent",
            adaptation = WhatsAppAdaptation.DIRECT)
    public void logSendDocumentEvent(String filename, long size) {
        // WAWebProcessRawMediaLogging.logSendDocumentEvent:
        //   a = e == null ? undefined : e.split(".").pop() ?? ""
        //   i = s.has(a) ? a : ""
        //   l = s.get(i) ?? DOCUMENT_TYPE.OTHER
        String extension;
        if (filename == null || filename.isEmpty()) {
            extension = "";
        } else {
            var dotIndex = filename.lastIndexOf('.');
            var tail = dotIndex < 0 ? filename : filename.substring(dotIndex + 1);
            extension = tail.toLowerCase(Locale.ROOT);
        }
        var normalizedExt = DOCUMENT_EXT_TO_TYPE.containsKey(extension) ? extension : "";
        var documentType = DOCUMENT_EXT_TO_TYPE.getOrDefault(normalizedExt, DocumentType.OTHER);
        commit(new SendDocumentEventBuilder()
                // WAWebProcessRawMediaLogging.logSendDocumentEvent: documentSize = t (raw byte count)
                .documentSize((double) size)
                // WAWebProcessRawMediaLogging.logSendDocumentEvent: documentType = l
                .documentType(documentType)
                // WAWebProcessRawMediaLogging.logSendDocumentEvent: documentExt = i
                .documentExt(normalizedExt)
                .build());
    }
}
