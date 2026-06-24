package com.github.auties00.cobalt.calls2.signaling;

import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.NodeBuilder;

/**
 * Test-only helper that stamps the universal {@code call-id}/{@code call-creator} header onto a
 * {@link NodeBuilder}, mirroring the package-private production {@code CallMessages.stampHeader} so a
 * test can hand-build a raw action node without the typed record.
 */
final class CallMessagesTestSupport {
    private CallMessagesTestSupport() {
    }

    static NodeBuilder stamp(String description, String callId, Jid callCreator) {
        return new NodeBuilder()
                .description(description)
                .attribute("call-id", callId)
                .attribute("call-creator", callCreator);
    }
}
