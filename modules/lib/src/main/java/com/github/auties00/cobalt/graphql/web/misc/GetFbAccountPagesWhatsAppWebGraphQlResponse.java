package com.github.auties00.cobalt.graphql.web.misc;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.github.auties00.cobalt.graphql.web.WhatsAppWebGraphQlOperation;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.model.business.ads.FacebookPage;
import com.github.auties00.cobalt.model.business.ads.FacebookPageBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Parses the WhatsApp Web GraphQL response of the Facebook-pages query built by
 * {@link GetFbAccountPagesWhatsAppWebGraphQlRequest} into a list of {@link FacebookPage}.
 *
 * <p>Reads the linked chain {@code user -> facebook_pages -> nodes} and projects each page node onto
 * the Cobalt domain model: its name, identifier, profile picture, and the tasks the caller is
 * permitted to perform on it. WhatsApp Web keeps only the pages whose permitted tasks grant
 * advertising.
 *
 * @see GetFbAccountPagesWhatsAppWebGraphQlRequest
 */
@WhatsAppWebModule(moduleName = "WAWebGetFBAccountPagesQuery")
public final class GetFbAccountPagesWhatsAppWebGraphQlResponse implements WhatsAppWebGraphQlOperation.Response {
    /**
     * Holds the parsed promotable pages.
     */
    private final List<FacebookPage> pages;

    /**
     * Constructs a response wrapping the parsed page list.
     *
     * <p>Reserved for the static parser.
     *
     * @param pages the parsed promotable pages
     */
    private GetFbAccountPagesWhatsAppWebGraphQlResponse(List<FacebookPage> pages) {
        this.pages = pages;
    }

    /**
     * Parses the WhatsApp Web GraphQL response from the unwrapped GraphQL {@code data} object.
     *
     * <p>Reads the linked chain {@code user -> facebook_pages -> nodes} and projects each node onto a
     * {@link FacebookPage}; the returned {@link Optional} is empty when {@code data} is {@code null}.
     *
     * @param data the unwrapped GraphQL {@code data} object returned by
     *             {@link com.github.auties00.cobalt.graphql.web.WhatsAppWebGraphQlClient#send(WhatsAppWebGraphQlOperation.Request)}
     * @return the parsed response, or empty when {@code data} is {@code null}
     */
    public static Optional<GetFbAccountPagesWhatsAppWebGraphQlResponse> of(JSONObject data) {
        if (data == null) {
            return Optional.empty();
        }

        var user = data.getJSONObject("user");
        var facebookPages = user == null ? null : user.getJSONObject("facebook_pages");
        var nodes = facebookPages == null ? null : facebookPages.getJSONArray("nodes");
        return Optional.of(new GetFbAccountPagesWhatsAppWebGraphQlResponse(parsePages(nodes)));
    }

    /**
     * Projects the {@code nodes} array onto a list of {@link FacebookPage}; the page name is read from
     * the {@code name} alias of the {@code ads_name} field.
     *
     * @param arr the JSON array to project
     * @return the projected list, empty when {@code arr} is {@code null}
     */
    private static List<FacebookPage> parsePages(JSONArray arr) {
        if (arr == null) {
            return List.of();
        }

        var result = new ArrayList<FacebookPage>(arr.size());
        for (var i = 0; i < arr.size(); i++) {
            var obj = arr.getJSONObject(i);
            if (obj == null) {
                continue;
            }

            var profilePicture = obj.getJSONObject("profile_picture");
            result.add(new FacebookPageBuilder()
                    .name(obj.getString("name"))
                    .id(obj.getString("id"))
                    .profilePictureUri(profilePicture == null ? null : profilePicture.getString("uri"))
                    .permittedTasks(parsePermittedTasks(obj.getJSONArray("permitted_tasks")))
                    .build());
        }
        return result;
    }

    /**
     * Reads the {@code permitted_tasks} string array into an unmodifiable list.
     *
     * @param arr the JSON array to read
     * @return the permitted-task names, empty when {@code arr} is {@code null}
     */
    private static List<String> parsePermittedTasks(JSONArray arr) {
        if (arr == null) {
            return List.of();
        }

        var result = new ArrayList<String>(arr.size());
        for (var i = 0; i < arr.size(); i++) {
            var task = arr.getString(i);
            if (task != null) {
                result.add(task);
            }
        }
        return result;
    }

    /**
     * Returns the parsed promotable pages.
     *
     * @return the parsed pages, empty when the relay returned none
     */
    public List<FacebookPage> pages() {
        return pages;
    }
}
