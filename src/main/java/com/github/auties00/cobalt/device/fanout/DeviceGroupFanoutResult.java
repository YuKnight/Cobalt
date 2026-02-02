package com.github.auties00.cobalt.device.fanout;

import com.github.auties00.cobalt.model.jid.Jid;

import java.util.Set;

/**
 * Result of group fanout calculation.
 *
 * @param devices     the set of device JIDs to send to
 * @param phash       the calculated participant hash
 */
public record DeviceGroupFanoutResult(
        Set<Jid> devices,
        String phash
) {

}
