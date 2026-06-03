package com.github.auties00.cobalt.listener;

import com.github.auties00.cobalt.listener.cloud.WhatsAppCloudListener;
import com.github.auties00.cobalt.listener.linked.WhatsAppLinkedListener;

/**
 * The sealed root of every Cobalt event-listener interface.
 *
 * <p>The listener hierarchy is split by client flavour: {@link WhatsAppLinkedListener} groups the
 * events emitted by the socket-based Linked client and {@link WhatsAppCloudListener} groups the events
 * emitted by the Cloud API client. Every concrete per-event listener extends one of those two markers,
 * never this type directly, so a registration surface typed to a flavour marker accepts only the
 * listeners that flavour can emit.
 */
public sealed interface WhatsAppListener permits WhatsAppCloudListener, WhatsAppLinkedListener {
}