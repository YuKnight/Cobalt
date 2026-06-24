package com.github.auties00.cobalt.calls2.net.transport;

import java.util.List;

/**
 * Builds the standalone WARP bandwidth-estimation configuration message the client sends to the
 * selective-forwarding unit when its downlink estimate drops.
 *
 * <p>When the downlink bandwidth estimate falls, the client asks the SFU to lower the minimum rate it
 * allocates to remote senders by sending a standalone WARP message carrying only the
 * {@link WarpAttributeFlag#BANDWIDTH_REPORT bandwidth-report} attribute. The attribute is the BWE
 * configuration block: a version byte (the engine writes {@value #BWE_CONFIG_VERSION}), an index byte,
 * and the minimum remote bandwidth estimate in kilobits per second. Because the bandwidth-report
 * attribute is legal only standalone, the result is always a {@link WarpMessage.Standalone}.
 *
 * @implNote This implementation reproduces {@code handle_sfu_bwe_config_request} (fn5164) from the
 *           wa-voip WASM module {@code ff-tScznZ8P} ({@code transport/wa_transport_warp.cc}), which
 *           builds a standalone WARP message with flags {@code 0x180}
 *           ({@link WarpAttributeFlag#EXT_FLAG} {@code 0x80} plus
 *           {@link WarpAttributeFlag#BANDWIDTH_REPORT} {@code 0x100}), version byte {@code 2}, an index
 *           byte, and the minimum-remote-BWE {@code u16} in kilobits per second; the {@code 0x180} flag
 *           combination is produced by {@link WarpMessage#encode()} from the single bandwidth-report
 *           attribute. The client also rejects an inbound server BWE config (handled by the transport,
 *           not this builder).
 */
public final class BweConfigSender {
    /**
     * The version byte the engine writes into the BWE configuration block.
     */
    public static final int BWE_CONFIG_VERSION = 2;

    /**
     * Prevents instantiation of this stateless builder holder.
     */
    private BweConfigSender() {
        throw new AssertionError("BweConfigSender is not instantiable");
    }

    /**
     * Builds the standalone WARP BWE-configuration message for a minimum remote bandwidth.
     *
     * @param index            the report index byte
     * @param minRemoteBweKbps the minimum remote bandwidth estimate in kilobits per second
     * @return a standalone WARP message carrying only the bandwidth-report attribute
     * @throws IllegalArgumentException if {@code index} is outside {@code 0..255} or
     *                                  {@code minRemoteBweKbps} is outside {@code 0..65535}
     */
    public static WarpMessage.Standalone build(int index, int minRemoteBweKbps) {
        if (index < 0 || index > 0xff) {
            throw new IllegalArgumentException("index must be in [0, 255], got " + index);
        }
        if (minRemoteBweKbps < 0 || minRemoteBweKbps > 0xffff) {
            throw new IllegalArgumentException("minRemoteBweKbps must be in [0, 65535], got " + minRemoteBweKbps);
        }
        var attribute = new WarpAttribute.BandwidthReport(BWE_CONFIG_VERSION, index, minRemoteBweKbps);
        return new WarpMessage.Standalone(List.of(attribute));
    }

    /**
     * Builds and encodes the standalone WARP BWE-configuration message to its wire bytes.
     *
     * @param index            the report index byte
     * @param minRemoteBweKbps the minimum remote bandwidth estimate in kilobits per second
     * @return the encoded WARP message bytes
     * @throws IllegalArgumentException if {@code index} or {@code minRemoteBweKbps} is out of range
     */
    public static byte[] encode(int index, int minRemoteBweKbps) {
        return build(index, minRemoteBweKbps).encode();
    }
}
