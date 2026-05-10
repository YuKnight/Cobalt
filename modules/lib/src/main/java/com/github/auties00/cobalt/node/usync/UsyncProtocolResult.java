package com.github.auties00.cobalt.node.usync;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.node.usync.result.UsyncProtocolError;
import com.github.auties00.cobalt.node.usync.result.UsyncProtocolResponse;

/**
 * Sealed sum type for the value a USync per-user, per-protocol parser returns.
 *
 * <p>Splits into two permits. {@link UsyncProtocolResponse} covers every
 * protocol-specific success variant (the eleven concrete success types under
 * {@link com.github.auties00.cobalt.node.usync.result}) and is itself a sealed
 * interface so callers can switch over the response branch without re-handling
 * errors. {@link UsyncProtocolError} represents the shared "the relay returned
 * an error for this user/protocol pair" variant.
 *
 * <p>Callers can pattern-match at either level.
 *
 * <pre>{@code
 * // Branch on success vs error first, then narrow on success
 * switch (result) {
 *     case UsyncProtocolError e    -> handleError(e);
 *     case UsyncProtocolResponse r -> handleResponse(r);
 * }
 *
 * // Or skip straight to a specific protocol
 * switch (result) {
 *     case ContactResult c      -> handleContact(c);
 *     case DeviceResult d       -> handleDevices(d);
 *     case UsyncProtocolError e -> handleError(e);
 *     default                   -> { /* other protocols * / }
 * }
 * }</pre>
 */
@WhatsAppWebModule(moduleName = "WAWebUsync")
public sealed interface UsyncProtocolResult permits UsyncProtocolResponse, UsyncProtocolError {
}
