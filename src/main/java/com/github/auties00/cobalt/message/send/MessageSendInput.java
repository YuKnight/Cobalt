package com.github.auties00.cobalt.message.send;

import com.github.auties00.cobalt.model.info.ChatMessageInfo;
import com.github.auties00.cobalt.model.info.NewsletterMessageInfo;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Sealed interface representing options for sending a message.
 * Permits either newsletter or chat message options.
 */
public sealed interface MessageSendInput
        permits MessageSendInput.Newsletter, MessageSendInput.Chat {

    /**
     * Sealed interface for chat message send options.
     * Each implementation wraps a {@link ChatMessageInfo} and carries only the
     * parameters relevant to that specific send case.
     */
    sealed interface Chat 
            extends MessageSendInput
            permits Chat.Bot,
                    MessageSendInput.Chat.CommunityAnnouncementGroup,
                    MessageSendInput.Chat.Default,
                    MessageSendInput.Chat.Edit,
                    MessageSendInput.Chat.Ephemeral,
                    MessageSendInput.Chat.Forwarded,
                    MessageSendInput.Chat.ViewOnce {
        int DEFAULT_MAX_RETRIES = 3;
    
        ChatMessageInfo info();
    
        default int maxRetries() {
            return DEFAULT_MAX_RETRIES;
        }
    
        default boolean skipPhashValidation() {
            return false;
        }
    
        default boolean generateMessageSecret() {
            return false;
        }
    
        default Optional<byte[]> messageSecret() {
            return Optional.empty();
        }
    
        default boolean includeReportingToken() {
            return false;
        }
    
        default Optional<byte[]> reportingTokenData() {
            return Optional.empty();
        }
    
        default Optional<String> nativeFlowName() {
            return Optional.empty();
        }
    
        default Optional<Integer> bizHostStorage() {
            return Optional.empty();
        }
    
        default Optional<Integer> bizActualActors() {
            return Optional.empty();
        }
    
        default Optional<Long> bizPrivacyModeTs() {
            return Optional.empty();
        }
    
        default Optional<byte[]> senderContentBinding() {
            return Optional.empty();
        }
    
        default Optional<byte[]> tcToken() {
            return Optional.empty();
        }
    
        default Optional<byte[]> ctwaAttribution() {
            return Optional.empty();
        }
    
        default Optional<Map<String, byte[]>> participantContentBindings() {
            return Optional.empty();
        }
    
        /**
         * Returns a copy of this options with a different {@link ChatMessageInfo}.
         * Useful for broadcast sends where individual messages are created per recipient.
         */
        default Chat withInfo(ChatMessageInfo newInfo) {
            return switch (this) {
                case Default _ -> new Default(newInfo);
                case Bot bot -> new Bot(newInfo, bot.secret(), bot.type(), bot.automatedType(), bot.clientThreadId());
                case CommunityAnnouncementGroup _ -> new CommunityAnnouncementGroup(newInfo);
                case Ephemeral eph -> new Ephemeral(newInfo, eph.durationSeconds());
                case ViewOnce _ -> new ViewOnce(newInfo);
                case Forwarded fwd -> new Forwarded(newInfo, fwd.forwardingScore());
                case Edit edit -> new Edit(newInfo, edit.editType(), edit.editMessageId());
            };
        }
    
        /**
         * Default send options for a regular chat message.
         */
        record Default(ChatMessageInfo info) implements Chat {
            public Default {
                Objects.requireNonNull(info, "info cannot be null");
            }
        }
    
        /**
         * Send options for edit, revoke, or pin operations on existing messages.
         *
         * @param info          the message info
         * @param editType      the type of edit operation
         * @param editMessageId the ID of the original message being edited/revoked/pinned
         */
        record Edit(
                ChatMessageInfo info,
                Type editType,
                String editMessageId
        ) implements Chat {
            public Edit {
                Objects.requireNonNull(info, "info cannot be null");
                Objects.requireNonNull(editType, "editType cannot be null");
                if (editMessageId == null || editMessageId.isBlank()) {
                    throw new IllegalArgumentException("editMessageId is required");
                }
            }
            
            /**
             * Edit type values for the protocol.
             * These correspond to the "edit" attribute in message stanzas.
             */
            public enum Type {
                REVOKE("1"),
                EDIT("7"),
                PIN("14");
    
                private final String protocolValue;
    
                Type(String protocolValue) {
                    this.protocolValue = protocolValue;
                }
    
                public String protocolValue() {
                    return protocolValue;
                }
            }
        }
    
    
        /**
         * Send options for bot messages.
         * Bot messages disable device fanout and include bot-specific stanza attributes.
         *
         * @param info              the message info
         * @param secret         the bot secret token (nullable)
         * @param type           the bot message type (nullable)
         * @param automatedType the local automated type (nullable)
         * @param clientThreadId    the client thread ID for bot conversations (nullable)
         */
        record Bot(
                ChatMessageInfo info,
                byte[] secret,
                Type type,
                AutomatedType automatedType,
                String clientThreadId
        ) implements Chat {
            public Bot {
                Objects.requireNonNull(info, "info cannot be null");
            }

            public boolean hasSecret() {
                return secret != null;
            }

            public boolean hasType() {
                return type != null;
            }

            public boolean hasAutomatedType() {
                return automatedType != null;
            }

            public boolean hasClientThreadId() {
                return clientThreadId != null;
            }
    
            /**
             * Bot message types.
             */
            public enum Type {
                FEEDBACK("feedback"),
                PROMPT("prompt"),
                COMMAND("command");
    
                private final String value;
    
                Type(String value) {
                    this.value = value;
                }
    
                public String value() {
                    return value;
                }
            }
    
            /**
             * Local automated type for bot messages.
             */
            public enum AutomatedType {
                FIRST_PARTY_PARTIAL("1p_partial"),
                THIRD_PARTY_FULL("3p_full");
    
                private final String value;
    
                AutomatedType(String value) {
                    this.value = value;
                }
    
                public String value() {
                    return value;
                }
            }
        }
    
        /**
         * Send options for Community Announcement Group (CAG) messages.
         * CAG messages skip sender key distribution to linked groups.
         */
        record CommunityAnnouncementGroup(ChatMessageInfo info) implements Chat {
            public CommunityAnnouncementGroup {
                Objects.requireNonNull(info, "info cannot be null");
            }
        }
    
        /**
         * Send options for ephemeral (disappearing) messages.
         *
         * @param info            the message info
         * @param durationSeconds the ephemeral duration in seconds (0 means use chat default)
         */
        record Ephemeral(
                ChatMessageInfo info,
                int durationSeconds
        ) implements Chat {
            public Ephemeral {
                Objects.requireNonNull(info, "info cannot be null");
                if (durationSeconds < 0) {
                    throw new IllegalArgumentException("durationSeconds cannot be negative");
                }
            }
        }
    
        /**
         * Send options for view-once messages.
         * Automatically enables message secret generation.
         */
        record ViewOnce(ChatMessageInfo info) implements Chat {
            public ViewOnce {
                Objects.requireNonNull(info, "info cannot be null");
            }
    
            @Override
            public boolean generateMessageSecret() {
                return true;
            }
        }
    
        /**
         * Send options for forwarded messages.
         *
         * @param info            the message info
         * @param forwardingScore the forwarding score (increments on each forward; >= 2 means "forwarded many times")
         */
        record Forwarded(
                ChatMessageInfo info,
                int forwardingScore
        ) implements Chat {
            public Forwarded {
                Objects.requireNonNull(info, "info cannot be null");
            }
    
            public boolean isForwardedMany() {
                return forwardingScore >= 2;
            }
        }
    }

    /**
     * Send options for newsletter messages (unencrypted).
     *
     * @param info the newsletter message info to send
     */
    record Newsletter(
            NewsletterMessageInfo info
    ) implements MessageSendInput {
        public Newsletter {
            Objects.requireNonNull(info, "info cannot be null");
        }
    }
}
