package com.github.auties00.cobalt.node.smax.biz;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * The {@code <localisation_metadata/>} projection. Uid /
 * translation-project tuple plus 0..20 {@code <parameter/>} entries.
 */
@WhatsAppWebModule(moduleName = "WASmaxInBizCtwaActionLocalisationMetadataMixin")
public final class SmaxBannerSuggestionLocalisationMetadata {
    /**
     * The mandatory {@code uid} attribute.
     */
    private final String uid;

    /**
     * The mandatory {@code translation_project} attribute.
     */
    private final String translationProject;

    /**
     * The list of {@code <parameter name value/>} entries (0..20).
     */
    private final List<Parameter> parameters;

    /**
     * Constructs a new metadata projection.
     *
     * @param uid                the uid; never {@code null}
     * @param translationProject the translation project; never
     *                           {@code null}
     * @param parameters         the parameters list; may be
     *                           {@code null} (treated as empty)
     * @throws NullPointerException if {@code uid} or
     *                              {@code translationProject} is
     *                              {@code null}
     */
    public SmaxBannerSuggestionLocalisationMetadata(String uid, String translationProject, List<Parameter> parameters) {
        this.uid = Objects.requireNonNull(uid, "uid cannot be null");
        this.translationProject = Objects.requireNonNull(translationProject,
                "translationProject cannot be null");
        this.parameters = parameters == null ? List.of() : List.copyOf(parameters);
    }

    /**
     * Returns the uid.
     *
     * @return the uid; never {@code null}
     */
    public String uid() {
        return uid;
    }

    /**
     * Returns the translation project.
     *
     * @return the project; never {@code null}
     */
    public String translationProject() {
        return translationProject;
    }

    /**
     * Returns the parameters list.
     *
     * @return an unmodifiable list of 0..20 entries; never
     *         {@code null}
     */
    public List<Parameter> parameters() {
        return parameters;
    }

    /**
     * Tries to parse the projection from the given node.
     *
     * @param node the {@code <localisation_metadata/>} node
     * @return an {@link Optional} carrying the projection, or empty
     *         when the node does not match the documented schema
     */
    public static Optional<SmaxBannerSuggestionLocalisationMetadata> of(Node node) {
        Objects.requireNonNull(node, "node cannot be null");
        if (!node.hasDescription("localisation_metadata")) {
            return Optional.empty();
        }
        var uid = node.getAttributeAsString("uid").orElse(null);
        if (uid == null) {
            return Optional.empty();
        }
        var translationProject = node.getAttributeAsString("translation_project").orElse(null);
        if (translationProject == null) {
            return Optional.empty();
        }
        var parameters = new ArrayList<Parameter>();
        var iter = node.streamChildren("parameter").iterator();
        while (iter.hasNext()) {
            var parsed = Parameter.of(iter.next());
            if (parsed.isEmpty()) {
                return Optional.empty();
            }
            parameters.add(parsed.get());
        }
        if (parameters.size() > 20) {
            return Optional.empty();
        }
        return Optional.of(new SmaxBannerSuggestionLocalisationMetadata(uid, translationProject, parameters));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxBannerSuggestionLocalisationMetadata) obj;
        return Objects.equals(this.uid, that.uid)
                && Objects.equals(this.translationProject, that.translationProject)
                && Objects.equals(this.parameters, that.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uid, translationProject, parameters);
    }

    @Override
    public String toString() {
        return "SmaxBannerSuggestionLocalisationMetadata[uid=" + uid
                + ", translationProject=" + translationProject
                + ", parameters=" + parameters + ']';
    }

    /**
     * Single {@code <parameter name value/>} entry.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInBizCtwaActionLocalisationMetadataMixin")
    public static final class Parameter {
        /**
         * The parameter name.
         */
        private final String name;

        /**
         * The parameter value.
         */
        private final String value;

        /**
         * Constructs a new parameter entry.
         *
         * @param name  the name; never {@code null}
         * @param value the value; never {@code null}
         * @throws NullPointerException if either argument is
         *                              {@code null}
         */
        public Parameter(String name, String value) {
            this.name = Objects.requireNonNull(name, "name cannot be null");
            this.value = Objects.requireNonNull(value, "value cannot be null");
        }

        /**
         * Returns the parameter name.
         *
         * @return the name; never {@code null}
         */
        public String name() {
            return name;
        }

        /**
         * Returns the parameter value.
         *
         * @return the value; never {@code null}
         */
        public String value() {
            return value;
        }

        /**
         * Tries to parse the entry from the given node.
         *
         * @param node the {@code <parameter/>} node
         * @return an {@link Optional} carrying the parsed entry,
         *         or empty when the node does not match the
         *         documented schema
         */
        public static Optional<Parameter> of(Node node) {
            Objects.requireNonNull(node, "node cannot be null");
            if (!node.hasDescription("parameter")) {
                return Optional.empty();
            }
            var name = node.getAttributeAsString("name").orElse(null);
            if (name == null) {
                return Optional.empty();
            }
            var value = node.getAttributeAsString("value").orElse(null);
            if (value == null) {
                return Optional.empty();
            }
            return Optional.of(new Parameter(name, value));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (Parameter) obj;
            return Objects.equals(this.name, that.name) && Objects.equals(this.value, that.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, value);
        }

        @Override
        public String toString() {
            return "SmaxBannerSuggestionLocalisationMetadata.Parameter[name=" + name
                    + ", value=" + value + ']';
        }
    }
}
