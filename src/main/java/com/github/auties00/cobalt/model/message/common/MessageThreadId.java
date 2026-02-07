package com.github.auties00.cobalt.model.message.common;

import com.github.auties00.cobalt.model.info.Info;
import it.auties.protobuf.annotation.ProtobufEnum;
import it.auties.protobuf.annotation.ProtobufEnumIndex;
import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

import java.util.Optional;

/**
 * Identifies a thread (conversation thread or AI thread).
 * <p>
 * Per WhatsApp Web: ThreadID is used in MessageContextInfo to track
 * AI conversation threads. The threadKey identifies the message that
 * started the thread, and threadType indicates whether it's an AI thread.
 *
 * @apiNote WAWebProtobufsE2E.pb.ThreadID
 */
@ProtobufMessage(name = "ThreadID")
public final class MessageThreadId {
    @ProtobufProperty(index = 1, type = ProtobufType.ENUM)
    final ThreadType threadType;

    @ProtobufProperty(index = 2, type = ProtobufType.MESSAGE)
    final ChatMessageKey threadKey;

    MessageThreadId(ThreadType threadType, ChatMessageKey threadKey) {
        this.threadType = threadType;
        this.threadKey = threadKey;
    }

    public Optional<ThreadType> threadType() {
        return Optional.ofNullable(threadType);
    }

    /**
     * Gets the thread key - the message key that identifies this thread.
     * <p>
     * Per WhatsApp Web WAWebThreadMsgUtils.getMsgAiThread: the key.id
     * is used as the client_thread_id in the bot node and is hashed
     * for the hashed_ai_thread_id in the meta node.
     *
     * @return the thread key
     */
    public Optional<ChatMessageKey> threadKey() {
        return Optional.ofNullable(threadKey);
    }

    /**
     * Checks if this is an AI thread.
     *
     * @return true if this is an AI_THREAD type
     */
    public boolean isAiThread() {
        return threadType == ThreadType.AI_THREAD;
    }

    @ProtobufEnum(name = "ThreadID.ThreadType")
    public enum ThreadType {
        UNKNOWN(0),
        VIEW_REPLIES(1),
        AI_THREAD(2);

        final int index;

        ThreadType(@ProtobufEnumIndex int index) {
            this.index = index;
        }
    }
}
