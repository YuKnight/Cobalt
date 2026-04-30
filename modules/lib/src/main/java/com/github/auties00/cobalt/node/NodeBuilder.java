package com.github.auties00.cobalt.node;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.jid.JidProvider;

import java.util.*;

/**
 * Fluent builder for assembling {@link Node} stanzas.
 *
 * <p>Stanzas are the unit of communication with the WhatsApp server.
 * Every outbound stanza in Cobalt is constructed through a builder by
 * setting a tag name with {@link #description(String)}, attaching key
 * value attributes through one of the {@code attribute} overloads, and
 * setting the payload through one of the {@code content} overloads. A
 * final call to {@link #build()} returns an immutable {@link Node}
 * picked from the appropriate concrete variant based on the content type.
 *
 * <p>Attribute setters are null safe. Passing a {@code null} value, or
 * passing {@code false} for the optional {@code condition} flag, skips the
 * attribute entirely instead of writing it as a literal {@code "null"}.
 * This removes the need for a null check at every call site.
 *
 * <p>Example usage:
 * <pre>{@code
 * Node iq = new NodeBuilder()
 *         .description("iq")
 *         .attribute("id", "12345")
 *         .attribute("to", recipient)
 *         .attribute("type", "set")
 *         .attribute("xmlns", "w:profile:picture")
 *         .content(new NodeBuilder()
 *                 .description("picture")
 *                 .attribute("type", "image")
 *                 .content(imageBytes)
 *                 .build())
 *         .build();
 * }</pre>
 *
 * <p>A simple presence stanza without content:
 * <pre>{@code
 * Node presence = new NodeBuilder()
 *         .description("presence")
 *         .attribute("type", "available")
 *         .build();
 * }</pre>
 *
 * @see Node
 * @see NodeAttribute
 */
@WhatsAppWebModule(moduleName = "WAWap")
public final class NodeBuilder {
    /**
     * Pending tag name of the node under construction.
     */
    private String description;

    /**
     * Pending attributes accumulated in insertion order.
     */
    private final SequencedMap<String, NodeAttribute> attributes;

    /**
     * Pending text content, or {@code null} when not set.
     */
    private String textContent;

    /**
     * Pending JID content, or {@code null} when not set.
     */
    private JidProvider jidContent;

    /**
     * Pending binary content, or {@code null} when not set.
     */
    private byte[] bytesContent;

    /**
     * Pending child node content, or {@code null} when not set.
     */
    private SequencedCollection<Node> childrenContent;

    /**
     * Builds a fresh builder with no description, no attributes, and no
     * content.
     */
    public NodeBuilder() {
        this.attributes = new LinkedHashMap<>();
    }

    /**
     * Sets the description (tag name) of the node under construction.
     *
     * @param description the tag name
     * @return this builder
     */
    public NodeBuilder description(String description) {
        this.description = description;
        return this;
    }

    /**
     * Adds a text attribute when the value is non null.
     *
     * @implNote The {@code null} check mirrors WAWap's {@code DROP_ATTR}
     *           sentinel pattern. The JS factory accepts a {@code DROP_ATTR}
     *           placeholder which the dispatcher filters out, while the
     *           builder folds the same intent into a per call null check.
     * @param key   the attribute key
     * @param value the attribute value, or {@code null} to skip
     * @return this builder
     */
    @WhatsAppWebExport(moduleName = "WAWap", exports = "DROP_ATTR",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public NodeBuilder attribute(String key, String value) {
        if(value != null) {
            this.attributes.put(key, new NodeAttribute.TextAttribute(value));
        }
        return this;
    }

    /**
     * Adds a text attribute when the value is non null and the condition
     * is {@code true}.
     *
     * @param key       the attribute key
     * @param value     the attribute value, or {@code null} to skip
     * @param condition guard that must hold for the attribute to be
     *                  written
     * @return this builder
     */
    public NodeBuilder attribute(String key, String value, boolean condition) {
        if(value != null && condition) {
            this.attributes.put(key, new NodeAttribute.TextAttribute(value));
        }
        return this;
    }

    /**
     * Adds a numeric attribute when the value is non null. The number is
     * serialised through {@link Number#toString()}.
     *
     * @param key   the attribute key
     * @param value the numeric value, or {@code null} to skip
     * @return this builder
     */
    public NodeBuilder attribute(String key, Number value) {
        if(value != null) {
            this.attributes.put(key, new NodeAttribute.TextAttribute(value.toString()));
        }
        return this;
    }

    /**
     * Adds a numeric attribute when the value is non null and the
     * condition is {@code true}. The number is serialised through
     * {@link Number#toString()}.
     *
     * @param key       the attribute key
     * @param value     the numeric value, or {@code null} to skip
     * @param condition guard that must hold for the attribute to be
     *                  written
     * @return this builder
     */
    public NodeBuilder attribute(String key, Number value, boolean condition) {
        if(value != null && condition) {
            this.attributes.put(key, new NodeAttribute.TextAttribute(value.toString()));
        }
        return this;
    }

    /**
     * Adds a boolean attribute. The value is serialised as {@code "true"}
     * or {@code "false"}.
     *
     * @param key   the attribute key
     * @param value the boolean value
     * @return this builder
     */
    public NodeBuilder attribute(String key, boolean value) {
        this.attributes.put(key, new NodeAttribute.TextAttribute(Boolean.toString(value)));
        return this;
    }

    /**
     * Adds a boolean attribute when the condition is {@code true}. The
     * value is serialised as {@code "true"} or {@code "false"}.
     *
     * @param key       the attribute key
     * @param value     the boolean value
     * @param condition guard that must hold for the attribute to be
     *                  written
     * @return this builder
     */
    public NodeBuilder attribute(String key, boolean value, boolean condition) {
        if(condition) {
            this.attributes.put(key, new NodeAttribute.TextAttribute(Boolean.toString(value)));
        }
        return this;
    }

    /**
     * Adds a JID attribute when the value is non null.
     *
     * @param key   the attribute key
     * @param value the JID provider, or {@code null} to skip
     * @return this builder
     * @see Jid
     */
    public NodeBuilder attribute(String key, JidProvider value) {
        if(value != null) {
            this.attributes.put(key, new NodeAttribute.JidAttribute(value.toJid()));
        }
        return this;
    }

    /**
     * Adds a JID attribute when the value is non null and the condition is
     * {@code true}.
     *
     * @param key       the attribute key
     * @param value     the JID provider, or {@code null} to skip
     * @param condition guard that must hold for the attribute to be
     *                  written
     * @return this builder
     * @see Jid
     */
    public NodeBuilder attribute(String key, JidProvider value, boolean condition) {
        if(value != null && condition) {
            this.attributes.put(key, new NodeAttribute.JidAttribute(value.toJid()));
        }
        return this;
    }

    /**
     * Adds a binary attribute when the value is non null.
     *
     * @param key   the attribute key
     * @param value the binary value, or {@code null} to skip
     * @return this builder
     */
    public NodeBuilder attribute(String key, byte[] value) {
        if(value != null) {
            this.attributes.put(key, new NodeAttribute.BytesAttribute(value));
        }
        return this;
    }

    /**
     * Adds a binary attribute when the value is non null and the condition
     * is {@code true}.
     *
     * @param key       the attribute key
     * @param value     the binary value, or {@code null} to skip
     * @param condition guard that must hold for the attribute to be
     *                  written
     * @return this builder
     */
    public NodeBuilder attribute(String key, byte[] value, boolean condition) {
        if(value != null && condition) {
            this.attributes.put(key, new NodeAttribute.BytesAttribute(value));
        }
        return this;
    }

    /**
     * Copies every entry from the supplied map into the pending attribute
     * set.
     *
     * <p>Existing attributes are preserved; entries that share a key are
     * overwritten. A {@code null} map is skipped silently.
     *
     * @param attributes the entries to copy, or {@code null} to skip
     * @return this builder
     */
    public NodeBuilder attributes(Map<String, ? extends NodeAttribute> attributes) {
        if(attributes != null) {
            this.attributes.putAll(attributes);
        }
        return this;
    }

    /**
     * Sets the node content to a text value, clearing any previously set
     * content.
     *
     * @param value the textual content
     * @return this builder
     */
    public NodeBuilder content(String value) {
        this.textContent = value;
        this.jidContent = null;
        this.bytesContent = null;
        this.childrenContent = null;
        return this;
    }

    /**
     * Sets the node content to a numeric value, clearing any previously
     * set content. The number is serialised through {@link Objects#toString(Object)}.
     *
     * @param value the numeric content
     * @return this builder
     */
    public NodeBuilder content(Number value) {
        this.textContent = Objects.toString(value);
        this.jidContent = null;
        this.bytesContent = null;
        this.childrenContent = null;
        return this;
    }

    /**
     * Sets the node content to a boolean value, clearing any previously
     * set content. The value is serialised as {@code "true"} or
     * {@code "false"}.
     *
     * @param value the boolean content
     * @return this builder
     */
    public NodeBuilder content(boolean value) {
        this.textContent = Objects.toString(value);
        this.jidContent = null;
        this.bytesContent = null;
        this.childrenContent = null;
        return this;
    }

    /**
     * Sets the node content to a JID, clearing any previously set
     * content.
     *
     * @param value the JID provider
     * @return this builder
     * @see Jid
     */
    public NodeBuilder content(JidProvider value) {
        this.textContent = null;
        this.jidContent = value;
        this.bytesContent = null;
        this.childrenContent = null;
        return this;
    }

    /**
     * Sets the node content to a binary blob, clearing any previously set
     * content.
     *
     * @param value the binary content
     * @return this builder
     */
    public NodeBuilder content(byte[] value) {
        this.textContent = null;
        this.jidContent = null;
        this.bytesContent = value;
        this.childrenContent = null;
        return this;
    }

    /**
     * Appends a collection of child nodes to the pending children list,
     * clearing any non child content previously set.
     *
     * <p>The method is additive: a second call merges its argument into
     * the children already accumulated, preserving order. {@code null}
     * entries inside the supplied collection are skipped.
     *
     * @param nodes the children to append, or {@code null} to skip
     * @return this builder
     */
    public NodeBuilder content(SequencedCollection<Node> nodes) {
        this.textContent = null;
        this.jidContent = null;
        this.bytesContent = null;
        if(childrenContent == null) {
            this.childrenContent = new ArrayList<>();
        }
        if(nodes != null) {
            for(var node : nodes) {
                if(node != null) {
                    this.childrenContent.add(node);
                }
            }
        }
        return this;
    }

    /**
     * Appends a varargs sequence of child nodes to the pending children
     * list, clearing any non child content previously set.
     *
     * <p>The method is additive: a second call merges its arguments into
     * the children already accumulated, preserving order. {@code null}
     * entries are skipped.
     *
     * @param nodes the children to append
     * @return this builder
     */
    public NodeBuilder content(Node... nodes) {
        this.textContent = null;
        this.jidContent = null;
        this.bytesContent = null;
        if(childrenContent == null) {
            this.childrenContent = new ArrayList<>();
        }
        if(nodes != null) {
            for(var node : nodes) {
                if(node != null) {
                    this.childrenContent.add(node);
                }
            }
        }
        return this;
    }

    /**
     * Returns whether an attribute with the supplied key is currently set.
     *
     * @param key the attribute key
     * @return {@code true} when the attribute is present
     */
    public boolean hasAttribute(String key) {
        return attributes.containsKey(key);
    }

    /**
     * Returns whether any content slot has been populated.
     *
     * @return {@code true} when text, JID, binary, or child content has
     *         been set
     */
    public boolean hasContent() {
        return textContent != null
               || jidContent != null
               || bytesContent != null
               || childrenContent != null;
    }

    /**
     * Builds and returns the constructed {@link Node}.
     *
     * <p>The concrete variant is selected from the populated content slot:
     * <ul>
     *   <li>{@link Node.TextNode} when text content is set</li>
     *   <li>{@link Node.JidNode} when JID content is set</li>
     *   <li>{@link Node.BytesNode} when binary content is set</li>
     *   <li>{@link Node.ContainerNode} when child nodes are set</li>
     *   <li>{@link Node.EmptyNode} when no content slot is populated</li>
     * </ul>
     *
     * <p>If no description was set the resulting node carries an empty
     * tag name.
     *
     * @return the freshly built node
     * @see Node
     */
    @WhatsAppWebExport(moduleName = "WAWap", exports = "makeWapNode",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WAWap", exports = "wap",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public Node build() {
        var description = Objects.requireNonNullElse(this.description, "");
        if(textContent != null) {
            return new Node.TextNode(description, attributes, textContent);
        }else if(jidContent != null){
            return new Node.JidNode(description, attributes, jidContent.toJid());
        }else if(bytesContent != null){
            return new Node.BytesNode(description, attributes, bytesContent);
        }else if(childrenContent != null){
            return new Node.ContainerNode(description, attributes, childrenContent);
        }else {
            return new Node.EmptyNode(description, attributes);
        }
    }
}
