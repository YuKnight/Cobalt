package com.github.auties00.cobalt.cloud;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.github.auties00.cobalt.model.message.MessageContainer;
import com.github.auties00.cobalt.model.message.commerce.OrderMessage;
import com.github.auties00.cobalt.model.message.contact.ContactsArrayMessage;
import com.github.auties00.cobalt.model.message.interactive.InteractiveResponseMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Inbound mapping cells for {@link CloudWebhookDecoder}: each cell wraps a single webhook message in a
 * change value, decodes it, and asserts the resulting universal {@link MessageContainer}.
 */
@DisplayName("CloudWebhookDecoder")
class CloudWebhookDecoderTest {
    private static MessageContainer decode(String messageJson) {
        var value = new JSONObject();
        value.put("messages", JSON.parseArray("[" + messageJson + "]"));
        var messages = CloudWebhookDecoder.decodeMessages(value);
        assertEquals(1, messages.size());
        return messages.getFirst().message();
    }

    private static Object content(String messageJson) {
        return decode(messageJson).content();
    }

    @Test
    @DisplayName("nfm_reply decodes into a native flow response message")
    void nfmReply() {
        var content = content("""
                {"from":"16505551234","id":"wamid.X","timestamp":"1700000000","type":"interactive",
                 "interactive":{"type":"nfm_reply","nfm_reply":{"name":"flow","body":"Sent","response_json":"{\\"a\\":1}"}}}""");
        var response = assertInstanceOf(InteractiveResponseMessage.class, content);
        var flow = assertInstanceOf(InteractiveResponseMessage.NativeFlowResponseMessage.class, response.content().orElseThrow());
        assertEquals("flow", flow.name().orElseThrow());
        assertEquals("{\"a\":1}", flow.paramsJson().orElseThrow());
        assertEquals("Sent", response.body().orElseThrow().text().orElseThrow());
    }

    @Test
    @DisplayName("contacts decodes into a contacts-array message with a reconstructed vCard")
    void contacts() {
        var content = content("""
                {"from":"16505551234","id":"wamid.X","timestamp":"1700000000","type":"contacts",
                 "contacts":[{"name":{"formatted_name":"John Smith"},
                 "phones":[{"phone":"+16505551234","wa_id":"16505551234","type":"CELL"}],
                 "org":{"company":"Acme"}}]}""");
        var array = assertInstanceOf(ContactsArrayMessage.class, content);
        assertEquals(1, array.contacts().size());
        var first = array.contacts().getFirst();
        assertEquals("John Smith", first.displayName().orElseThrow());
        var vcard = first.vcard().orElseThrow();
        assertTrue(vcard.contains("FN:John Smith"), vcard);
        assertTrue(vcard.contains("TEL;type=CELL;waid=16505551234:+16505551234"), vcard);
        assertTrue(vcard.contains("X-WA-BIZ-NAME:Acme"), vcard);
    }

    @Test
    @DisplayName("order decodes into an inquiry order message with a derived total")
    void order() {
        var content = content("""
                {"from":"16505551234","id":"wamid.X","timestamp":"1700000000","type":"order",
                 "order":{"catalog_id":"4928452840550143","text":"please deliver fast",
                 "product_items":[{"product_retailer_id":"03-Pack","quantity":1,"item_price":210,"currency":"HKD"}]},
                 "context":{"from":"16315551234","id":"wamid.PRODUCT_MSG_ID"}}""");
        var order = assertInstanceOf(OrderMessage.class, content);
        assertEquals("please deliver fast", order.message().orElseThrow());
        assertEquals(1, order.itemCount().orElseThrow());
        assertEquals(OrderMessage.OrderStatus.INQUIRY, order.status().orElseThrow());
        assertEquals(OrderMessage.OrderSurface.CATALOG, order.surface().orElseThrow());
        assertEquals("HKD", order.totalCurrencyCode().orElseThrow());
        assertEquals(210000L, order.totalAmount1000().orElseThrow());
        assertEquals("wamid.PRODUCT_MSG_ID", order.orderRequestMessageId().orElseThrow().id().orElseThrow());
    }

    @Test
    @DisplayName("system and unknown types fall back to an empty container")
    void fallback() {
        var system = decode("""
                {"from":"16505551234","id":"wamid.X","timestamp":"1700000000","type":"system",
                 "system":{"body":"changed","type":"customer_changed_number","new_wa_id":"16505559999"}}""");
        assertTrue(system.isEmpty());
        var unknown = decode("""
                {"from":"16505551234","id":"wamid.X","timestamp":"1700000000","type":"unknown",
                 "errors":[{"code":131051,"title":"Message type is not currently supported."}]}""");
        assertTrue(unknown.isEmpty());
    }
}
