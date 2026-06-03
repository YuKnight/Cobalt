package com.github.auties00.cobalt.cloud;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.github.auties00.cobalt.model.jid.JidProvider;
import com.github.auties00.cobalt.model.message.MessageContainer;
import com.github.auties00.cobalt.model.message.commerce.ButtonsMessage;
import com.github.auties00.cobalt.model.message.contact.ContactMessage;
import com.github.auties00.cobalt.model.message.contact.ContactsArrayMessage;
import com.github.auties00.cobalt.model.message.interactive.TemplateButton;
import com.github.auties00.cobalt.model.message.interactive.TemplateMessage;
import com.github.auties00.cobalt.model.message.list.ListMessage;
import com.github.auties00.cobalt.model.message.location.LocationMessage;
import com.github.auties00.cobalt.model.message.media.AudioMessage;
import com.github.auties00.cobalt.model.message.media.DocumentMessage;
import com.github.auties00.cobalt.model.message.media.ImageMessage;
import com.github.auties00.cobalt.model.message.media.StickerMessage;
import com.github.auties00.cobalt.model.message.media.VideoMessage;
import com.github.auties00.cobalt.model.message.text.ExtendedTextMessage;
import com.github.auties00.cobalt.model.message.text.HighlyStructuredMessage;

import java.util.List;

/**
 * Translates Cobalt's universal {@link MessageContainer} model into the Cloud API {@code /messages}
 * request body.
 *
 * <p>The encoder switches over the active message variant returned by {@link MessageContainer#content()}
 * and produces the matching Cloud JSON shape: text, media (image, video, audio, document, sticker),
 * location, contacts, reaction, interactive (reply buttons and lists), and template messages. Media is
 * referenced by a hosted link when the message carries an {@code http(s)} URL and by media id
 * otherwise, so an upload id placed in the message's media-url field is sent as {@code {"id": ...}}.
 *
 * <p>A few Cloud-only refinements have no representation in the universal model and are silently
 * skipped: named template parameters, copy-code and OTP authentication buttons, and carousel or
 * limited-time-offer templates.
 */
public final class CloudMessageEncoder {
    /**
     * Private constructor; the encoder exposes only static behaviour.
     */
    private CloudMessageEncoder() {

    }

    /**
     * Encodes a message for a recipient into the Cloud {@code /messages} request body.
     *
     * @param recipient the destination user
     * @param container the message content
     * @return the JSON request body ready to post to the messages edge
     * @throws IllegalArgumentException if the message variant has no Cloud API representation
     */
    public static JSONObject encode(JidProvider recipient, MessageContainer container) {
        var root = new JSONObject();
        root.put("messaging_product", "whatsapp");
        root.put("recipient_type", "individual");
        root.put("to", recipient.toJid().user());
        encodeContent(root, container);
        return root;
    }

    /**
     * Writes the type discriminator and the type-specific node for the container's active content.
     *
     * @param root      the request body being assembled
     * @param container the message content
     * @throws IllegalArgumentException if the message variant has no Cloud API representation
     */
    private static void encodeContent(JSONObject root, MessageContainer container) {
        switch (container.content()) {
            case ExtendedTextMessage text -> {
                root.put("type", "text");
                var node = new JSONObject();
                node.put("body", text.text().orElse(""));
                node.put("preview_url", text.matchedText().isPresent());
                root.put("text", node);
            }
            case ImageMessage image -> media(root, "image", image.mediaUrl().orElse(null), image.caption().orElse(null));
            case VideoMessage video -> media(root, "video", video.mediaUrl().orElse(null), video.caption().orElse(null));
            case AudioMessage audio -> media(root, "audio", audio.mediaUrl().orElse(null), null);
            case DocumentMessage document -> media(root, "document", document.mediaUrl().orElse(null), document.caption().orElse(null));
            case StickerMessage sticker -> media(root, "sticker", sticker.mediaUrl().orElse(null), null);
            case LocationMessage location -> {
                root.put("type", "location");
                var node = new JSONObject();
                node.put("latitude", location.degreesLatitude().orElse(0));
                node.put("longitude", location.degreesLongitude().orElse(0));
                location.name().ifPresent(value -> node.put("name", value));
                location.address().ifPresent(value -> node.put("address", value));
                root.put("location", node);
            }
            case ContactMessage contact -> {
                root.put("type", "contacts");
                var contacts = new JSONArray();
                contacts.add(contactNode(contact));
                root.put("contacts", contacts);
            }
            case ContactsArrayMessage contacts -> {
                root.put("type", "contacts");
                var array = new JSONArray();
                for (var contact : contacts.contacts()) {
                    array.add(contactNode(contact));
                }
                root.put("contacts", array);
            }
            case com.github.auties00.cobalt.model.message.text.ReactionMessage reaction -> {
                root.put("type", "reaction");
                var node = new JSONObject();
                reaction.key().flatMap(key -> key.id()).ifPresent(id -> node.put("message_id", id));
                node.put("emoji", reaction.text().orElse(""));
                root.put("reaction", node);
            }
            case TemplateMessage template -> {
                root.put("type", "template");
                root.put("template", templateNode(template));
            }
            case HighlyStructuredMessage hsm -> {
                root.put("type", "template");
                root.put("template", hsmTemplateNode(hsm));
            }
            case ButtonsMessage buttons -> {
                root.put("type", "interactive");
                root.put("interactive", buttonsNode(buttons));
            }
            case ListMessage list -> {
                root.put("type", "interactive");
                root.put("interactive", listNode(list));
            }
            default -> throw new IllegalArgumentException(
                    "message type has no Cloud API representation: " + container.content().getClass().getSimpleName());
        }
    }

    /**
     * Writes a media node, referencing the asset by hosted link or by media id.
     *
     * @param root    the request body being assembled
     * @param type    the media type discriminator, for example {@code "image"}
     * @param ref     the media reference (an {@code http(s)} link or a media id), or {@code null}
     * @param caption the optional caption, or {@code null} when the type carries none
     */
    private static void media(JSONObject root, String type, String ref, String caption) {
        root.put("type", type);
        var node = new JSONObject();
        putMediaReference(node, ref);
        if (caption != null) {
            node.put("caption", caption);
        }
        root.put(type, node);
    }

    /**
     * Builds a Cloud contact node from a vCard-backed contact message.
     *
     * @param contact the contact message
     * @return the Cloud contact node
     */
    private static JSONObject contactNode(ContactMessage contact) {
        var node = new JSONObject();
        var name = new JSONObject();
        name.put("formatted_name", contact.displayName().orElse(""));
        node.put("name", name);
        contact.vcard().ifPresent(vcard -> {
            var phones = phonesFromVcard(vcard);
            if (!phones.isEmpty()) {
                node.put("phones", phones);
            }
        });
        return node;
    }

    /**
     * Extracts the {@code TEL} entries of a vCard into a Cloud {@code phones} array.
     *
     * @param vcard the raw vCard text
     * @return a {@code phones} array, empty when the vCard carried no {@code TEL} lines
     */
    private static JSONArray phonesFromVcard(String vcard) {
        var phones = new JSONArray();
        for (var line : vcard.split("\\r?\\n")) {
            var upper = line.toUpperCase();
            if (upper.startsWith("TEL")) {
                var index = line.indexOf(':');
                if (index >= 0) {
                    var phone = new JSONObject();
                    phone.put("phone", line.substring(index + 1).trim());
                    phones.add(phone);
                }
            }
        }
        return phones;
    }

    /**
     * Builds a Cloud template node from a {@link TemplateMessage}.
     *
     * <p>The structured-content title becomes the header component, the highly-structured body becomes
     * the body parameters, and the template buttons become button components keyed by their index.
     *
     * @param template the template message
     * @return the Cloud {@code template} node
     */
    private static JSONObject templateNode(TemplateMessage template) {
        var node = new JSONObject();
        var components = new JSONArray();
        var format = template.format().orElse(null);
        if (format instanceof TemplateMessage.FourRowTemplate fourRow) {
            var content = fourRow.content().orElse(null);
            if (content != null) {
                applyHsmIdentity(node, content);
                var body = bodyComponent(content);
                if (body != null) {
                    components.add(body);
                }
            }
            fourRow.title().ifPresent(title -> {
                var header = headerComponent(title);
                if (header != null) {
                    components.add(header);
                }
            });
            for (var button : fourRow.buttons()) {
                var component = buttonComponent(button);
                if (component != null) {
                    components.add(component);
                }
            }
        }
        template.templateId().ifPresent(id -> node.putIfAbsent("name", id));
        if (!components.isEmpty()) {
            node.put("components", components);
        }
        return node;
    }

    /**
     * Builds a Cloud template node directly from a highly structured message.
     *
     * @param hsm the highly structured message
     * @return the Cloud {@code template} node
     */
    private static JSONObject hsmTemplateNode(HighlyStructuredMessage hsm) {
        var node = new JSONObject();
        applyHsmIdentity(node, hsm);
        var body = bodyComponent(hsm);
        if (body != null) {
            var components = new JSONArray();
            components.add(body);
            node.put("components", components);
        }
        return node;
    }

    /**
     * Writes the template name and language from a highly structured message into a template node.
     *
     * @param node the template node being assembled
     * @param hsm  the highly structured message carrying the identity
     */
    private static void applyHsmIdentity(JSONObject node, HighlyStructuredMessage hsm) {
        hsm.elementName().ifPresent(name -> node.put("name", name));
        var language = new JSONObject();
        var code = hsm.deterministicLg().or(hsm::fallbackLg).orElse("en_US");
        language.put("code", code);
        node.put("language", language);
    }

    /**
     * Builds the body component (positional text and localisable parameters) of a template.
     *
     * @param hsm the highly structured message carrying the body parameters
     * @return the body component, or {@code null} when the message has no parameters
     */
    private static JSONObject bodyComponent(HighlyStructuredMessage hsm) {
        var parameters = new JSONArray();
        for (var param : hsm.params()) {
            var node = new JSONObject();
            node.put("type", "text");
            node.put("text", param);
            parameters.add(node);
        }
        for (var localizable : hsm.localizableParams()) {
            var node = new JSONObject();
            var currency = localizable.paramOneof()
                    .filter(value -> value instanceof HighlyStructuredMessage.HSMLocalizableParameter.HSMCurrency)
                    .map(value -> (HighlyStructuredMessage.HSMLocalizableParameter.HSMCurrency) value);
            if (currency.isPresent()) {
                node.put("type", "currency");
                var inner = new JSONObject();
                currency.get().currencyCode().ifPresent(value -> inner.put("code", value));
                inner.put("amount_1000", currency.get().amount1000().orElse(0));
                inner.put("fallback_value", localizable.defaultValue().orElse(""));
                node.put("currency", inner);
            } else if (localizable.paramOneof().orElse(null) instanceof HighlyStructuredMessage.HSMLocalizableParameter.HSMDateTime) {
                node.put("type", "date_time");
                var inner = new JSONObject();
                inner.put("fallback_value", localizable.defaultValue().orElse(""));
                node.put("date_time", inner);
            } else {
                node.put("type", "text");
                node.put("text", localizable.defaultValue().orElse(""));
            }
            parameters.add(node);
        }
        if (parameters.isEmpty()) {
            return null;
        }
        var component = new JSONObject();
        component.put("type", "body");
        component.put("parameters", parameters);
        return component;
    }

    /**
     * Builds a header component from the title slot of a four-row template.
     *
     * @param title the title variant
     * @return the header component, or {@code null} when the title is unsupported
     */
    private static JSONObject headerComponent(TemplateMessage.Title title) {
        var parameter = new JSONObject();
        switch (title) {
            case ImageMessage image -> {
                parameter.put("type", "image");
                parameter.put("image", mediaParam(image.mediaUrl().orElse(null)));
            }
            case VideoMessage video -> {
                parameter.put("type", "video");
                parameter.put("video", mediaParam(video.mediaUrl().orElse(null)));
            }
            case DocumentMessage document -> {
                parameter.put("type", "document");
                parameter.put("document", mediaParam(document.mediaUrl().orElse(null)));
            }
            case HighlyStructuredMessage text -> {
                parameter.put("type", "text");
                parameter.put("text", text.params().isEmpty() ? "" : text.params().getFirst());
            }
            default -> {
                return null;
            }
        }
        var component = new JSONObject();
        component.put("type", "header");
        var parameters = new JSONArray();
        parameters.add(parameter);
        component.put("parameters", parameters);
        return component;
    }

    /**
     * Builds a media parameter node referencing an asset by link or id.
     *
     * @param ref the media reference, or {@code null}
     * @return the media parameter node
     */
    private static JSONObject mediaParam(String ref) {
        var node = new JSONObject();
        putMediaReference(node, ref);
        return node;
    }

    /**
     * Writes a media reference into a node, as a hosted {@code link} when it is an {@code http(s)} URL
     * and as a media {@code id} otherwise.
     *
     * @param node the node to write into
     * @param ref  the media reference, or {@code null} to write nothing
     */
    private static void putMediaReference(JSONObject node, String ref) {
        if (ref == null) {
            return;
        }
        if (ref.startsWith("http://") || ref.startsWith("https://")) {
            node.put("link", ref);
        } else {
            node.put("id", ref);
        }
    }

    /**
     * Builds a button component from a template button.
     *
     * @param button the template button
     * @return the button component, or {@code null} when the button kind is unsupported
     */
    private static JSONObject buttonComponent(TemplateButton button) {
        var index = button.index().orElse(0);
        var variant = button.button().orElse(null);
        if (variant instanceof TemplateButton.QuickReplyButton quickReply) {
            var payload = new JSONObject();
            payload.put("type", "payload");
            payload.put("payload", quickReply.id().orElse(""));
            return buttonNode(index, "quick_reply", payload);
        }
        if (variant instanceof TemplateButton.URLButton urlButton) {
            var suffix = urlButton.url().map(HighlyStructuredMessage::params).orElse(List.of());
            if (suffix.isEmpty()) {
                return null;
            }
            var text = new JSONObject();
            text.put("type", "text");
            text.put("text", suffix.getFirst());
            return buttonNode(index, "url", text);
        }
        // Call buttons are static: the phone number is baked into the approved template, so no
        // runtime button parameter is sent.
        return null;
    }

    /**
     * Builds a Cloud button component wrapping a single parameter.
     *
     * @param index     the zero-based button index within the template
     * @param subType   the button sub-type, for example {@code "quick_reply"} or {@code "url"}
     * @param parameter the single button parameter
     * @return the button component
     */
    private static JSONObject buttonNode(int index, String subType, JSONObject parameter) {
        var component = new JSONObject();
        component.put("type", "button");
        component.put("sub_type", subType);
        component.put("index", String.valueOf(index));
        var parameters = new JSONArray();
        parameters.add(parameter);
        component.put("parameters", parameters);
        return component;
    }

    /**
     * Builds a Cloud interactive node of type {@code button} from a buttons message.
     *
     * @param buttons the buttons message
     * @return the Cloud {@code interactive} node
     */
    private static JSONObject buttonsNode(ButtonsMessage buttons) {
        var node = new JSONObject();
        node.put("type", "button");
        buttons.contentText().ifPresent(text -> node.put("body", textNode(text)));
        buttons.footerText().ifPresent(text -> node.put("footer", textNode(text)));
        var action = new JSONObject();
        var array = new JSONArray();
        for (var variant : buttons.buttons()) {
            variant.buttonId().ifPresent(id -> {
                var reply = new JSONObject();
                var inner = new JSONObject();
                inner.put("id", id);
                inner.put("title", variant.buttonText().flatMap(text -> text.displayText()).orElse(""));
                reply.put("type", "reply");
                reply.put("reply", inner);
                array.add(reply);
            });
        }
        action.put("buttons", array);
        node.put("action", action);
        return node;
    }

    /**
     * Builds a Cloud interactive node of type {@code list} from a list message.
     *
     * @param list the list message
     * @return the Cloud {@code interactive} node
     */
    private static JSONObject listNode(ListMessage list) {
        var node = new JSONObject();
        node.put("type", "list");
        list.description().ifPresent(text -> node.put("body", textNode(text)));
        list.footerText().ifPresent(text -> node.put("footer", textNode(text)));
        list.title().ifPresent(text -> node.put("header", headerTextNode(text)));
        var action = new JSONObject();
        list.buttonText().ifPresent(text -> action.put("button", text));
        var sections = new JSONArray();
        for (var section : list.sections()) {
            var sectionNode = new JSONObject();
            section.title().ifPresent(title -> sectionNode.put("title", title));
            var rows = new JSONArray();
            for (var row : section.rows()) {
                var rowNode = new JSONObject();
                row.rowId().ifPresent(id -> rowNode.put("id", id));
                row.title().ifPresent(title -> rowNode.put("title", title));
                row.description().ifPresent(description -> rowNode.put("description", description));
                rows.add(rowNode);
            }
            sectionNode.put("rows", rows);
            sections.add(sectionNode);
        }
        action.put("sections", sections);
        node.put("action", action);
        return node;
    }

    /**
     * Builds an interactive {@code text} body or footer node.
     *
     * @param text the text content
     * @return the node carrying the text
     */
    private static JSONObject textNode(String text) {
        var node = new JSONObject();
        node.put("text", text);
        return node;
    }

    /**
     * Builds an interactive header node of type {@code text}.
     *
     * @param text the header text
     * @return the header node
     */
    private static JSONObject headerTextNode(String text) {
        var node = new JSONObject();
        node.put("type", "text");
        node.put("text", text);
        return node;
    }
}
