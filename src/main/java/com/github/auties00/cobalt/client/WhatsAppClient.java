package com.github.auties00.cobalt.client;

import com.github.auties00.cobalt.device.DeviceService;
import com.github.auties00.cobalt.exception.*;
import com.github.auties00.cobalt.message.MessageService;
import com.github.auties00.cobalt.migration.InactiveGroupLidMigrationService;
import com.github.auties00.cobalt.migration.LidMigrationService;
import com.github.auties00.cobalt.model.business.*;
import com.github.auties00.cobalt.model.chat.Chat;
import com.github.auties00.cobalt.model.chat.ChatEphemeralTimer;
import com.github.auties00.cobalt.model.chat.ChatMetadata;
import com.github.auties00.cobalt.model.chat.community.CommunityLinkedGroup;
import com.github.auties00.cobalt.model.chat.community.CommunityLinkedGroupBuilder;
import com.github.auties00.cobalt.model.chat.community.CommunityMetadataBuilder;
import com.github.auties00.cobalt.model.chat.group.GroupMetadataBuilder;
import com.github.auties00.cobalt.model.chat.group.GroupParticipant;
import com.github.auties00.cobalt.model.chat.group.GroupParticipantBuilder;
import com.github.auties00.cobalt.model.chat.group.GroupPartipantRole;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.jid.JidProvider;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.model.newsletter.Newsletter;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.SyncPendingMutation;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.mex.json.community.FetchAllSubgroupsMex;
import com.github.auties00.cobalt.node.mex.json.community.FetchAllSubgroupsMex.Response.DefaultSubGroup;
import com.github.auties00.cobalt.node.mex.json.community.FetchAllSubgroupsMex.Response.SubGroups;
import com.github.auties00.cobalt.props.ABPropsService;
import com.github.auties00.cobalt.socket.WhatsAppSocketClient;
import com.github.auties00.cobalt.socket.WhatsAppSocketListener;
import com.github.auties00.cobalt.socket.WhatsAppSocketStanza;
import com.github.auties00.cobalt.store.WhatsAppStore;
import com.github.auties00.cobalt.stream.SocketStream;
import com.github.auties00.cobalt.sync.SnapshotRecoveryService;
import com.github.auties00.cobalt.sync.WebAppStateService;
import com.github.auties00.cobalt.util.FastRandomUtils;
import com.github.auties00.cobalt.wam.WamService;
import com.github.auties00.curve25519.Curve25519;
import com.github.auties00.libsignal.SignalSessionCipher;
import com.github.auties00.libsignal.groups.SignalGroupCipher;
import com.github.auties00.libsignal.key.SignalIdentityPublicKey;
import com.github.auties00.libsignal.key.SignalPreKeyPair;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A class used to interface a user to Whatsapp
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
public final class WhatsAppClient {
    private static final byte[] SIGNAL_KEY_TYPE = {SignalIdentityPublicKey.type()};

    private static final int PROFILE_PIC_SIZE = 64;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^(.+)@(\\S+)$");

    private static final long MIN_PRE_KEYS_COUNT = 5;

    private final WhatsAppStore store;
    private final WhatsAppClientErrorHandler errorHandler;
    private final WhatsAppClientMessagePreviewHandler messagePreviewHandler;

    private final WebAppStateService webAppStateService;
    private final LidMigrationService lidMigrationService;
    private final DeviceService deviceService;
    private final ABPropsService abPropsService;
    private final InactiveGroupLidMigrationService inactiveGroupLidMigrationService;

    private final MessageService messageService;
    private final WamService wamService;

    private WhatsAppSocketClient socketClient;
    private final SocketStream socketStream;
    private final ConcurrentMap<String, WhatsAppSocketStanza> pendingSocketRequests;
    private Thread shutdownHook;

    WhatsAppClient(WhatsAppStore store, WhatsAppClientVerificationHandler.Web webVerificationHandler, WhatsAppClientMessagePreviewHandler messagePreviewHandler, WhatsAppClientErrorHandler errorHandler) {
        this.store = Objects.requireNonNull(store, "store cannot be null");
        this.errorHandler = Objects.requireNonNull(errorHandler, "errorHandler cannot be null");
        if ((store.clientType() == WhatsAppClientType.WEB) == (webVerificationHandler == null)) {
            throw new IllegalArgumentException("webVerificationHandler cannot be null when client type is WEB");
        }
        SignalSessionCipher sessionCipher = new SignalSessionCipher(store);
        SignalGroupCipher groupCipher = new SignalGroupCipher(store);
        this.abPropsService = new ABPropsService(this);
        var snapshotRecoveryService = new SnapshotRecoveryService(this, abPropsService);
        this.webAppStateService = new WebAppStateService(this, abPropsService, snapshotRecoveryService);
        this.lidMigrationService = new LidMigrationService(this, abPropsService);
        this.inactiveGroupLidMigrationService = new InactiveGroupLidMigrationService(this, abPropsService);
        this.deviceService = new DeviceService(this, abPropsService, sessionCipher);
        this.messageService = new MessageService(this, sessionCipher, groupCipher, deviceService, abPropsService);
        this.wamService = new WamService(this, abPropsService);
        this.pendingSocketRequests = new ConcurrentHashMap<>();
        this.socketStream = new SocketStream(this, webVerificationHandler, lidMigrationService, inactiveGroupLidMigrationService, messageService, abPropsService, deviceService, wamService, snapshotRecoveryService, webAppStateService);
        this.messagePreviewHandler = messagePreviewHandler;
    }

    /**
     * Creates a new builder
     *
     * @return a builder
     */
    public static WhatsAppClientBuilder builder() {
        return WhatsAppClientBuilder.INSTANCE;
    }

    //<editor-fold desc="Data">

    /**
     * Returns the store associated with this session
     *
     * @return a non-null WhatsappStore
     */
    public WhatsAppStore store() {
        return store;
    }

    public WhatsAppClientMessagePreviewHandler messagePreviewHandler() {
        return messagePreviewHandler;
    }

    public ABPropsService abPropsService() {
        return abPropsService;
    }

    public com.github.auties00.cobalt.migration.LidMigrationService lidMigrationService() {
        return lidMigrationService;
    }

    //</editor-fold>

    //<editor-fold desc="Connection">

    /**
     * Connects to Whatsapp
     */
    public WhatsAppClient connect() {
        connect(null);
        return this;
    }

    private void connect(WhatsAppClientDisconnectReason reason) {
        if (isConnected()) {
            throw new IllegalStateException("Client is already connected");
        }

        try {
            this.socketClient = WhatsAppSocketClient.newCipheredSocketClient(store);
            socketClient.connect(new WhatsAppSocketListener() {
                @Override
                public void onNode(Node node) {
                    WhatsAppClient.this.onNode(node);
                }

                @Override
                public void onError(WhatsAppException exception) {
                    handleFailure(exception);
                }

                @Override
                public void onClose() {
                    disconnect(WhatsAppClientDisconnectReason.RECONNECTING);
                }
            });
        } catch (IOException | InterruptedException throwable) {
            if (reason == WhatsAppClientDisconnectReason.RECONNECTING) {
                // TODO: Add attempts count
                handleFailure(new WhatsAppReconnectionException(throwable.getMessage(), 0, throwable));
            } else {
                handleFailure(new WhatsAppConnectionException(throwable.getMessage(), throwable));
            }
            return;
        }

        if (shutdownHook == null) {
            this.shutdownHook = Thread.ofPlatform()
                    .name("CobaltShutdownHandler")
                    .unstarted(() -> disconnect(WhatsAppClientDisconnectReason.DISCONNECTED, false));
            Runtime.getRuntime().addShutdownHook(shutdownHook);
        }
    }

    private void onNode(Node node) {
        try {
            for (var listener : store.listeners()) {
                Thread.startVirtualThread(() -> listener.onNodeReceived(this, node));
            }
            resolvePendingRequest(node);
            socketStream.handle(node);
        } catch (WhatsAppStreamException exception) {
            handleFailure(exception);
        } catch (Throwable throwable) {
            handleFailure(new WhatsAppStreamException(throwable));
        }
    }

    public void resolvePendingRequest(Node node) {
        var id = node.getAttributeAsString("id", null);
        if (id == null) {
            return;
        }

        var request = pendingSocketRequests.remove(id);
        if (request != null) {
            request.complete(node);
        }
    }

    public void disconnect(WhatsAppClientDisconnectReason reason) {
        disconnect(reason, true);
    }

    private void disconnect(WhatsAppClientDisconnectReason reason, boolean canRemoveShutdownHook) {
        // Per WA Web WAWebSocketModel.sendLogout: flush pending sentinel
        // mutations before disconnecting so key expiration is propagated
        if (reason == WhatsAppClientDisconnectReason.LOGGED_OUT
                || reason == WhatsAppClientDisconnectReason.DISCONNECTED) {
            try {
                webAppStateService.flushDirtyCollections();
            } catch (Exception e) {
                // Best-effort: don't let flush failures block disconnect
            }
        }

        wamService.close();

        if (socketClient != null) {
            socketClient.disconnect();
        }

        pendingSocketRequests.forEach((ignored, request) -> request.complete(null));
        pendingSocketRequests.clear();

        try {
            if (reason == WhatsAppClientDisconnectReason.LOGGED_OUT || reason == WhatsAppClientDisconnectReason.BANNED) {
                store.delete();
            } else {
                store.save();
            }
        } catch (IOException e) {
            // TODO
            e.printStackTrace();
        }

        socketStream.reset();
        webAppStateService.reset();
        lidMigrationService.reset();

        // Stop ADV check scheduler (will be restarted on successful reconnection)
        deviceService.stopAdvCheckScheduler();

        if (reason != WhatsAppClientDisconnectReason.RECONNECTING && shutdownHook != null && canRemoveShutdownHook) {
            Runtime.getRuntime().removeShutdownHook(shutdownHook);
            shutdownHook = null;
        }

        for (var listener : store.listeners()) {
            listener.onDisconnected(this, reason);
        }

        if (reason == WhatsAppClientDisconnectReason.RECONNECTING) {
            connect(reason);
        }
    }

    public void sendNodeWithNoResponse(Node node) {
        try {
            socketClient.sendNode(node);
        } catch (IOException exception) {
            throw new WhatsAppSessionException.Closed();
        }
        for (var listener : store.listeners()) {
            Thread.startVirtualThread(() -> listener.onNodeSent(this, node));
        }
    }

    public Node sendNode(NodeBuilder node) {
        return sendNode(node, null);
    }

    public Node sendNode(NodeBuilder node, Function<Node, Boolean> filter) {
        if (!node.hasAttribute("id")) {
            node.attribute("id", FastRandomUtils.randomHex(10));
        }

        var outgoing = node.build();
        var outgoingId = outgoing.getRequiredAttribute("id")
                .toString();
        try {
            socketClient.sendNode(outgoing);
        } catch (IOException exception) {
            throw new WhatsAppSessionException.Closed();
        }

        for (var listener : store.listeners()) {
            Thread.startVirtualThread(() -> listener.onNodeSent(this, outgoing));
        }

        var request = new WhatsAppSocketStanza(outgoing, filter);
        pendingSocketRequests.put(outgoingId, request);
        return request.waitForResponse();
    }

    /**
     * Disconnects from Whatsapp Web's WebSocket if a previous connection exists
     *
     */
    public void disconnect() {
        disconnect(WhatsAppClientDisconnectReason.DISCONNECTED);
    }

    /**
     * Disconnects and reconnects to Whatsapp Web's WebSocket if a previous connection exists
     *
     */
    public void reconnect() {
        disconnect(WhatsAppClientDisconnectReason.RECONNECTING);
    }

    /**
     * Disconnects from Whatsapp Web's WebSocket and logs out of WhatsappWeb invalidating the previous
     * saved credentials. The next time the API is used, the QR code will need to be scanned again.
     */
    public void logout() {
        var localJid = store.jid();
        if (localJid.isEmpty()) {
            disconnect(WhatsAppClientDisconnectReason.LOGGED_OUT);
        } else {
            var device = new NodeBuilder()
                    .description("remove-companion-device")
                    .attribute("value", localJid.get())
                    .attribute("reason", "user_initiated")
                    .build();
            var iqNode = new NodeBuilder()
                    .description("iq")
                    .attribute("xmlns", "md")
                    .attribute("to", JidServer.user())
                    .attribute("type", "set")
                    .content(device);
            sendNode(iqNode);
        }
    }

    /**
     * Returns whether the connection is active or not
     *
     * @return a boolean
     */
    public boolean isConnected() {
        return socketClient != null && socketClient.isConnected();
    }

    /**
     * Waits for this session to be disconnected
     */
    public WhatsAppClient waitForDisconnection() {
        if (!isConnected()) {
            return this;
        }

        var future = new CompletableFuture<Void>();
        store.listeners().add(new WhatsAppClientListener() {
            @Override
            public void onDisconnected(WhatsAppClient whatsapp, WhatsAppClientDisconnectReason reason) {
                if (reason != WhatsAppClientDisconnectReason.RECONNECTING) {
                    future.complete(null);
                }
            }
        });
        future.join();
        return this;
    }

    //</editor-fold>

    //<editor-fold desc="Error handling">

    public void handleFailure(WhatsAppException exception) {
        var result = errorHandler.handleError(this, exception);
        switch (result) {
            case BAN -> disconnect(WhatsAppClientDisconnectReason.BANNED);
            case LOG_OUT -> disconnect(WhatsAppClientDisconnectReason.LOGGED_OUT);
            case DISCONNECT -> disconnect(WhatsAppClientDisconnectReason.DISCONNECTED);
            case RECONNECT -> disconnect(WhatsAppClientDisconnectReason.RECONNECTING);
        }
    }

    public void pushWebAppState(SyncPatchType type, List<SyncPendingMutation> patches) {
        webAppStateService.pushPatches(type, patches);
    }

    public void pullWebAppState(SyncPatchType... patches) {
        webAppStateService.pullPatches(patches);
    }

    /**
     * Schedules the all-devices-responded check for missing sync key timeout.
     *
     * <p>Called when a companion device responds to a key share request without
     * providing the requested key, to trigger the grace period before fatal.
     */
    public void scheduleAllDevicesRespondedCheck() {
        webAppStateService.scheduleAllDevicesRespondedCheck();
    }

    /**
     * Retries orphan mutations across all collections.
     *
     * <p>Per WhatsApp Web {@code WAWebSyncdOrphan}: called after events that
     * may have introduced new entities (history sync, contact sync) so that
     * previously orphaned mutations can be resolved.
     */
    public void retryOrphanMutations() {
        webAppStateService.retryAllOrphanMutations();
    }

    private void updateBusinessCertificate(String newName) {
        var details = new BusinessVerifiedNameCertificateDetailsBuilder()
                .verifiedName(Objects.requireNonNullElse(newName, store.name()))
                .issuer(BusinessVerifiedNameCertificate.CertificateIssuer.SMALL_BUSINESS)
                .serial(Math.abs(ThreadLocalRandom.current().nextLong()))
                .build();
        var encodedDetails = BusinessVerifiedNameCertificateDetailsSpec.encode(details);
        var certificate = new BusinessVerifiedNameCertificateBuilder()
                .details(encodedDetails)
                .signature(Curve25519.sign(store.identityKeyPair().privateKey().toEncodedPoint(), encodedDetails))
                .build();
        var verifiedNameRequest = new NodeBuilder()
                .description("verified_name")
                .attribute("v", 2)
                .content(BusinessVerifiedNameCertificateSpec.encode(certificate))
                .build();
        var queryRequest = new NodeBuilder()
                .description("iq")
                .attribute("to", Jid.userServer())
                .attribute("type", "set")
                .attribute("xmlns", "w:biz")
                .content(verifiedNameRequest);
        var verifiedName = sendNode(queryRequest)
                .getChild("verified_name")
                .flatMap(node -> node.getAttributeAsString("id"))
                .orElse("");
        store.setVerifiedName(verifiedName);
    }

    public void sendAck(Node node) {
        var id = node.getRequiredAttributeAsString("id");
        sendAck(id, node);
    }

    public void sendAck(String id, Node node) {
        var ackBuilder = new NodeBuilder()
                .description("ack")
                .attribute("id", id);

        var ackClass = node.description();
        var isMessage = ackClass.equals("message");
        ackBuilder.attribute("class", ackClass);

        var ackTo = node.getRequiredAttributeAsJid("from");
        ackBuilder.attribute("to", ackTo);

        node.getAttributeAsJid("participant")
                .map(jid -> jid)
                .ifPresent(receiptParticipant -> ackBuilder.attribute("recipient", receiptParticipant));

        if (!isMessage) {
            node.getAttributeAsString("type")
                    .ifPresent(type -> ackBuilder.attribute("type", type));
        }

        sendNodeWithNoResponse(ackBuilder.build());
    }

    public void sendPreKeys(long keysCount) {
        keysCount = Math.max(keysCount, MIN_PRE_KEYS_COUNT);
        var startId = store.hasPreKeys() ? store.preKeys().getLast().id() + 1 : 1;
        var listBody = new ArrayList<Node>();
        var preKeys = new ArrayList<SignalPreKeyPair>();
        while (keysCount-- > 0) {
            var preKeyPair = SignalPreKeyPair.random(startId++);
            preKeys.add(preKeyPair);
            var id = new NodeBuilder()
                    .description("id")
                    .content(FastRandomUtils.intToBytes(preKeyPair.id(), 3))
                    .build();
            var value = new NodeBuilder()
                    .description("value")
                    .content(preKeyPair.publicKey().toEncodedPoint())
                    .build();
            var preKayNode = new NodeBuilder()
                    .description("key")
                    .content(id, value)
                    .build();
            listBody.add(preKayNode);
        }
        var registration = new NodeBuilder()
                .description("registration")
                .content(FastRandomUtils.intToBytes(store.registrationId(), 4))
                .build();
        var type = new NodeBuilder()
                .description("type")
                .content(SIGNAL_KEY_TYPE)
                .build();
        var identity = new NodeBuilder()
                .description("identity")
                .content(store.identityKeyPair().publicKey().toEncodedPoint())
                .build();
        var list = new NodeBuilder()
                .description("list")
                .content(listBody)
                .build();
        var skeyId = new NodeBuilder()
                .description("id")
                .content(FastRandomUtils.intToBytes(store.signedKeyPair().id(), 3))
                .build();
        var skeyValue = new NodeBuilder()
                .description("value")
                .content(store.signedKeyPair().publicKey().toEncodedPoint())
                .build();
        var skeySignature = new NodeBuilder()
                .description("signature")
                .content(store.signedKeyPair().signature())
                .build();
        var skey = new NodeBuilder()
                .description("skey")
                .content(skeyId, skeyValue, skeySignature)
                .build();
        var queryRequest = new NodeBuilder()
                .description("iq")
                .attribute("to", JidServer.user())
                .attribute("type", "set")
                .attribute("xmlns", "encrypt")
                .content(registration, type, identity, list, skey);
        sendNode(queryRequest);
        for (var preKey : preKeys) {
            store.addPreKey(preKey);
        }
    }

    public void sendReceipt(String id, Jid from, String type) {
        var me = store.jid()
                .orElse(null);
        if (me == null) {
            return;
        }

        var receipt = new NodeBuilder()
                .description("receipt")
                .attribute("id", id)
                .attribute("type", type)
                .attribute("to", from)
                .build();
        sendNodeWithNoResponse(receipt);
    }

    /**
     * Queries the metadata of a group or community.
     *
     * @param chat the target group or community
     * @return the non-{@code null} metadata
     * @throws IllegalArgumentException if the JID is not a group or
     *         community
     * @throws NoSuchElementException if the server response is invalid
     */
    public ChatMetadata queryChatMetadata(JidProvider chat) {
        if (!chat.toJid().hasServer(JidServer.groupOrCommunity())) {
            throw new IllegalArgumentException("Expected a group/community");
        }
        var jid = chat.toJid();
        var body = new NodeBuilder()
                .description("query")
                .attribute("request", "interactive")
                .build();
        var iqNode = new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "w:g2")
                .attribute("to", jid)
                .attribute("type", "get")
                .content(body);
        var response = sendNode(iqNode);
        return handleChatMetadata(response);
    }

    private ChatMetadata handleChatMetadata(Node response) {
        var metadataNode = Optional.of(response)
                .filter(entry -> entry.hasDescription("group"))
                .or(() -> response.getChild("group"))
                .orElseThrow(() -> new NoSuchElementException("Erroneous response: %s".formatted(response)));
        var metadata = parseChatMetadata(metadataNode);
        store.addChatMetadata(metadata);
        var chat = store.findChatByJid(metadata.jid())
                .orElseGet(() -> store().addNewChat(metadata.jid()));
        chat.setName(metadata.subject());
        return metadata;
    }

    private ChatMetadata parseChatMetadata(Node node) {
        var groupIdUser = node.getRequiredAttributeAsString("id");
        var groupId = Jid.of(groupIdUser, JidServer.groupOrCommunity());
        var subject = node.getAttributeAsString("subject", "");
        var subjectAuthor = node.getAttributeAsJid("s_o", null);
        var subjectTimestampSeconds = node.getAttributeAsLong("s_t", 0);
        var foundationTimestampSeconds = node.getAttributeAsLong("creation", 0);
        var founder = node.getAttributeAsJid("creator", null);
        var description = node.getChild("description")
                .flatMap(parent -> parent.getChild("body"))
                .flatMap(Node::toContentString)
                .orElse(null);
        var descriptionId = node.getChild("description")
                .flatMap(descriptionNode -> descriptionNode.getAttributeAsString("id"))
                .orElse(null);
        var ephemeral = node.getChild("ephemeral")
                .map(ephemeralNode -> ChatEphemeralTimer.of((int) ephemeralNode.getAttributeAsLong("expiration", 0)))
                .orElse(null);
        var communityNode = node.getChild("parent")
                .orElse(null);
        var lidAddressingMode = node.hasAttribute("addressing_mode", "lid");
        var linkedParent = node.getChild("linked_parent")
                .flatMap(parent -> parent.getAttributeAsJid("jid"))
                .orElse(null);
        var isIncognito = node.hasChild("incognito");
        var defaultSubgroup = node.hasAttribute("default_sub_group", true);
        if (communityNode == null) {
            var restrict = node.hasChild("announce");
            var announce = node.hasChild("restrict");
            var memberAddModeAdminOnly = node.getChild("member_add_mode")
                    .flatMap(Node::toContentString)
                    .map(mode -> Objects.equals(mode, "admin_add"))
                    .orElse(false);
            var groupMembershipApprovalMode = node.getChild("membership_approval_mode")
                    .flatMap(entry -> entry.getChild("group_join"))
                    .map(entry -> entry.hasAttribute("state", "on"))
                    .orElse(false);
            var memberLinkModeAdminOnly = node.getChild("member_link_mode")
                    .flatMap(Node::toContentString)
                    .map(mode -> Objects.equals(mode, "admin_link"))
                    .orElse(false);
            var noFrequentlyForwarded = node.hasChild("no_frequently_forwarded");
            var groupSupport = node.hasChild("support");
            var groupSuspended = node.hasChild("suspended");
            var groupReportToAdminMode = node.hasChild("allow_admin_reports");
            var generalSubgroup = node.hasChild("general_chat");
            var groupGeneralChatAutoAddDisabled = node.hasChild("auto_add_disabled");
            var hiddenSubgroup = node.hasChild("hidden_group");
            var groupHasCapi = node.hasChild("capi");
            var groupSafetyCheck = node.hasChild("group_safety_check");
            var participants = node.streamChildren("participant")
                    .filter(entry -> !entry.hasAttribute("error"))
                    .map(entry -> {
                        var id = entry.getRequiredAttributeAsJid("jid");
                        var role = entry.getAttributeAsString("type")
                                .map(GroupPartipantRole::of)
                                .orElse(GroupPartipantRole.USER);
                        return new GroupParticipantBuilder()
                                .userJid(id)
                                .rank(role)
                                .build();
                    })
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            return new GroupMetadataBuilder()
                    .jid(groupId)
                    .subject(subject)
                    .subjectAuthorJid(subjectAuthor)
                    .subjectTimestamp(subjectTimestampSeconds == 0 ? null : Instant.ofEpochSecond(subjectTimestampSeconds))
                    .foundationTimestamp(foundationTimestampSeconds == 0 ? null : Instant.ofEpochSecond(foundationTimestampSeconds))
                    .founderJid(founder)
                    .description(description)
                    .descriptionId(descriptionId)
                    .restrict(restrict)
                    .announce(announce)
                    .memberAddModeAdminOnly(memberAddModeAdminOnly)
                    .membershipApprovalMode(groupMembershipApprovalMode)
                    .memberLinkModeAdminOnly(memberLinkModeAdminOnly)
                    .noFrequentlyForwarded(noFrequentlyForwarded)
                    .participants(participants)
                    .ephemeralExpiration(ephemeral)
                    .parentCommunityJid(linkedParent)
                    .isLidAddressingMode(lidAddressingMode)
                    .isIncognito(isIncognito)
                    .defaultSubgroup(defaultSubgroup)
                    .generalSubgroup(generalSubgroup)
                    .hiddenSubgroup(hiddenSubgroup)
                    .support(groupSupport)
                    .suspended(groupSuspended)
                    .reportToAdminMode(groupReportToAdminMode)
                    .generalChatAutoAddDisabled(groupGeneralChatAutoAddDisabled)
                    .hasCapi(groupHasCapi)
                    .groupSafetyCheck(groupSafetyCheck)
                    .build();
        } else {
            var restrict = node.hasChild("locked");
            var announce = node.hasChild("announcement");
            var noFrequentlyForwarded = node.hasChild("no_frequently_forwarded");
            var communityMembershipApprovalMode = node.getChild("membership_approval_mode")
                    .flatMap(entry -> entry.getChild("group_join"))
                    .map(entry -> entry.hasAttribute("state", "on"))
                    .orElse(false);
            var memberAddModeAdminOnly = node.getChild("member_add_mode")
                    .flatMap(Node::toContentString)
                    .map(mode -> Objects.equals(mode, "admin_add"))
                    .orElse(false);
            var memberLinkModeAdminOnly = node.getChild("member_link_mode")
                    .flatMap(Node::toContentString)
                    .map(mode -> Objects.equals(mode, "admin_link"))
                    .orElse(false);
            var allowNonAdminSubGroupCreation = node.hasChild("allow_non_admin_sub_group_creation");
            var support = node.hasChild("support");
            var suspended = node.hasChild("suspended");
            var reportToAdminMode = node.hasChild("allow_admin_reports");
            var communityGeneralSubgroup = node.hasChild("general_chat");
            var generalChatAutoAddDisabled = node.hasChild("auto_add_disabled");
            var hiddenSubgroup = node.hasChild("hidden_group");
            var hasCapi = node.hasChild("capi");
            var groupSafetyCheck = node.hasChild("group_safety_check");
            var participantLabelEnabled = node.hasChild("participant_label_enabled");
            var limitSharingEnabled = node.hasChild("limit_sharing_enabled");
            var isParentGroupClosed = communityNode.hasAttribute("default_membership_approval_mode", "request_required");
            var size = node.getAttributeAsInt("size", null);
            var growthLockedNode = node.getChild("growth_locked").orElse(null);
            var growthLockType = growthLockedNode != null
                    ? growthLockedNode.getAttributeAsString("type").orElse(null)
                    : null;
            var growthLockExpirationSeconds = growthLockedNode != null
                    ? growthLockedNode.getAttributeAsLong("expiration", 0)
                    : 0L;
            var growthLockExpiration = growthLockExpirationSeconds > 0
                    ? Instant.ofEpochSecond(growthLockExpirationSeconds)
                    : null;
            var descriptionNode = node.getChild("description").orElse(null);
            var descriptionTimestampSeconds = descriptionNode != null
                    ? descriptionNode.getAttributeAsLong("t", 0)
                    : 0L;
            var descriptionTimestamp = descriptionTimestampSeconds > 0
                    ? Instant.ofEpochSecond(descriptionTimestampSeconds)
                    : null;
            var descriptionAuthor = descriptionNode != null
                    ? descriptionNode.getAttributeAsJid("participant", null)
                    : null;
            var evolutionVersion = node.getChild("evolution_version")
                    .map(ev -> ev.getAttributeAsInt("value", null))
                    .orElse(null);
            var linkedGroupsQueryBody = new NodeBuilder()
                    .description("linked_groups_participants")
                    .build();
            var linkedGroupsQueryRequest = new NodeBuilder()
                    .description("iq")
                    .attribute("xmlns", "w:g2")
                    .attribute("to", groupId)
                    .attribute("type", "get")
                    .content(linkedGroupsQueryBody);
            var linkedGroupsResponse = sendNode(linkedGroupsQueryRequest);
            var participants = linkedGroupsResponse
                    .streamChild("linked_groups_participants")
                    .flatMap(participantsNodeBody -> participantsNodeBody.streamChildren("participant"))
                    .flatMap(participantNode -> participantNode.streamAttributeAsJid("jid"))
                    .map(jid -> new GroupParticipantBuilder().userJid(jid).build())
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            var communityGroupsQuery = new FetchAllSubgroupsMex.Request(groupId.toString(), "INTERACTIVE", null);
            var communityGroupsRequestNode = communityGroupsQuery.toNode();
            var communityGroupsResponseNode = sendNode(communityGroupsRequestNode);
            var communityGroupsResponse = FetchAllSubgroupsMex.Response.of(communityGroupsResponseNode);
            var communityLinkedGroups = new LinkedHashSet<CommunityLinkedGroup>();
            communityGroupsResponse.ifPresent(response -> {
                response.defaultSubGroup().ifPresent(defaultSg -> communityLinkedGroups.add(
                        new CommunityLinkedGroupBuilder()
                                .jid(defaultSg.id().map(Jid::of).orElse(null))
                                .subject(defaultSg.subject().flatMap(DefaultSubGroup.Subject::value).orElse(null))
                                .subjectTimestamp(defaultSg.subject().flatMap(DefaultSubGroup.Subject::creationTime).orElse(null))
                                .parentGroupJid(groupId)
                                .defaultSubgroup(true)
                                .build()
                ));
                response.subGroups()
                        .stream()
                        .map(SubGroups::edges)
                        .flatMap(Collection::stream)
                        .map(SubGroups.Edges::node)
                        .flatMap(Optional::stream)
                        .map(entry -> new CommunityLinkedGroupBuilder()
                                .jid(entry.id().map(Jid::of).orElse(null))
                                .subject(entry.subject().flatMap(SubGroups.Edges.Node.Subject::value).orElse(null))
                                .subjectTimestamp(entry.subject().flatMap(SubGroups.Edges.Node.Subject::creationTime).orElse(null))
                                .parentGroupJid(groupId)
                                .generalSubgroup(entry.properties().flatMap(SubGroups.Edges.Node.Properties::generalChat).map(Boolean::parseBoolean).orElse(false))
                                .membershipApprovalMode(entry.properties().map(SubGroups.Edges.Node.Properties::membershipApprovalModeEnabled).orElse(false))
                                .hiddenSubgroup(entry.properties().flatMap(SubGroups.Edges.Node.Properties::hiddenGroup).map(Boolean::parseBoolean).orElse(false))
                                .build())
                        .forEach(communityLinkedGroups::add);
            });
            return new CommunityMetadataBuilder()
                    .jid(groupId)
                    .subject(subject)
                    .subjectAuthorJid(subjectAuthor)
                    .subjectTimestamp(Instant.ofEpochSecond(subjectTimestampSeconds))
                    .foundationTimestamp(Instant.ofEpochSecond(foundationTimestampSeconds))
                    .founderJid(founder)
                    .description(description)
                    .descriptionId(descriptionId)
                    .descriptionTimestamp(descriptionTimestamp)
                    .descriptionAuthorJid(descriptionAuthor)
                    .restrict(restrict)
                    .announce(announce)
                    .noFrequentlyForwarded(noFrequentlyForwarded)
                    .membershipApprovalMode(communityMembershipApprovalMode)
                    .memberLinkModeAdminOnly(memberLinkModeAdminOnly)
                    .allowNonAdminSubGroupCreation(allowNonAdminSubGroupCreation)
                    .memberAddModeAdminOnly(memberAddModeAdminOnly)
                    .growthLockExpiration(growthLockExpiration)
                    .growthLockType(growthLockType)
                    .reportToAdminMode(reportToAdminMode)
                    .size(size)
                    .support(support)
                    .suspended(suspended)
                    .isParentGroupClosed(isParentGroupClosed)
                    .defaultSubgroup(defaultSubgroup)
                    .generalSubgroup(communityGeneralSubgroup)
                    .hiddenSubgroup(hiddenSubgroup)
                    .groupSafetyCheck(groupSafetyCheck)
                    .generalChatAutoAddDisabled(generalChatAutoAddDisabled)
                    .hasCapi(hasCapi)
                    .evolutionVersion(evolutionVersion)
                    .participantLabelEnabled(participantLabelEnabled)
                    .limitSharingEnabled(limitSharingEnabled)
                    .participants(participants)
                    .ephemeralExpiration(ephemeral)
                    .communityGroups(communityLinkedGroups)
                    .isLidAddressingMode(lidAddressingMode)
                    .isIncognito(isIncognito)
                    .build();
        }
    }

    private Stream<GroupParticipant> parseGroupParticipant(Node node) {
        if (node.hasAttribute("error")) {
            return Stream.empty();
        }

        var id = node.getRequiredAttributeAsJid("jid");
        var role = GroupPartipantRole.of(node.getRequiredAttributeAsString("type"));
        var result = new GroupParticipantBuilder()
                .userJid(id)
                .rank(role)
                .build();
        return Stream.of(result);
    }

    // TODO: Stuff to fix

    private Node createCall(JidProvider jid) {
        return null;
    }

    public SequencedCollection<Newsletter> queryNewsletters() {
        return List.of();
    }

    public SequencedCollection<Chat> queryGroups() {
        return List.of();
    }
}
