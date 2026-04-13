package com.github.auties00.cobalt.util;

public final class GithubActionsUtils {
    private static final String GITHUB_ACTIONS = "GITHUB_ACTIONS";

    public static boolean isActionsEnvironment() {
        return Boolean.parseBoolean(System.getenv(GITHUB_ACTIONS));
    }
}
