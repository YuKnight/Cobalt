package com.github.auties00.cobalt.model.info;

import com.github.auties00.cobalt.model.button.base.ButtonBody;
import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

import java.util.Objects;

/**
 * A model class that holds the information related to a native flow.
 */
@ProtobufMessage(name = "Message.ButtonsMessage.Button.NativeFlowInfo")
public final class NativeFlowInfo implements Info, ButtonBody {
    /**
     * Native flow name for order details.
     *
     * @apiNote WAWebNativeFlowUtils: ORDER_DETAILS native flow type
     */
    public static final String ORDER_DETAILS = "order_details";

    /**
     * Native flow name for order status.
     *
     * @apiNote WAWebNativeFlowUtils: ORDER_STATUS native flow type
     */
    public static final String ORDER_STATUS = "order_status";

    /**
     * Native flow name for payment status.
     *
     * @apiNote WAWebNativeFlowUtils: PAYMENT_STATUS native flow type
     */
    public static final String PAYMENT_STATUS = "payment_status";

    /**
     * Native flow name for payment method.
     *
     * @apiNote WAWebNativeFlowUtils: PAYMENT_METHOD native flow type
     */
    public static final String PAYMENT_METHOD = "payment_method";

    @ProtobufProperty(index = 1, type = ProtobufType.STRING)
    final String name;

    @ProtobufProperty(index = 2, type = ProtobufType.STRING)
    final String parameters;

    NativeFlowInfo(String name, String parameters) {
        this.name = Objects.requireNonNull(name, "name cannot be null");
        this.parameters = Objects.requireNonNull(parameters, "parameters cannot be null");
    }

    public String name() {
        return name;
    }

    public String parameters() {
        return parameters;
    }

    /**
     * Checks if this is an order-related native flow.
     *
     * @return true if this is an order details or order status flow
     *
     * @apiNote WAWebNativeFlowUtils: detects order native flow types
     */
    public boolean isOrderFlow() {
        return ORDER_DETAILS.equals(name) || ORDER_STATUS.equals(name);
    }

    /**
     * Checks if this is a payment-related native flow.
     *
     * @return true if this is a payment status or payment method flow
     *
     * @apiNote WAWebNativeFlowUtils: detects payment native flow types
     */
    public boolean isPaymentFlow() {
        return PAYMENT_STATUS.equals(name) || PAYMENT_METHOD.equals(name);
    }

    @Override
    public Type bodyType() {
        return Type.NATIVE_FLOW;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof NativeFlowInfo that
                && Objects.equals(name, that.name)
                && Objects.equals(parameters, that.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, parameters);
    }

    @Override
    public String toString() {
        return "NativeFlowInfo[" +
                "name=" + name +
                ", parameters=" + parameters +
                ']';
    }
}