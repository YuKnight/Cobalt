package com.github.auties00.cobalt.util;

import org.junit.jupiter.api.Test;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Smoke tests for {@link NativeLibLoader} and the FFM toolchain.
 *
 * <p>Covers:
 *
 * <ol>
 *   <li>Classifier resolution from {@code os.name}/{@code os.arch}.</li>
 *   <li>FFM linker round-trip via the platform's standard C runtime
 *       ({@code abs(int)}) — verifies the toolchain works on the
 *       running platform.</li>
 *   <li>Offline-mode hard-failure when the requested binary is
 *       neither on the classpath nor in the cache — pins the
 *       fail-fast guarantee for download-disabled deployments.</li>
 * </ol>
 *
 * <p>The success path (download → SHA-256 verify → cache → load)
 * is exercised end-to-end by the per-binding smoke tests
 * (e.g. {@code OpusCodecTest}, {@code SpeexDspTest}, {@code VP8Test},
 * {@code H264Test}, {@code SctpAssociationTest}).
 */
public class NativeLibLoaderTest {

    /**
     * The set of classifiers Cobalt publishes natives for.
     */
    private static final Set<String> SUPPORTED_CLASSIFIERS = Set.of(
            "linux-x86_64", "linux-aarch64",
            "darwin-x86_64", "darwin-aarch64",
            "windows-x86_64");

    /**
     * Asserts that the running JVM's classifier resolves to one of
     * the supported os-arch tokens, so that natives bundles can be
     * shipped for it.
     */
    @Test
    public void classifierMatchesSupportedSet() {
        var classifier = NativeLibLoader.classifier();
        assertNotNull(classifier);
        assertTrue(SUPPORTED_CLASSIFIERS.contains(classifier),
                "classifier '" + classifier + "' is not in the supported set " + SUPPORTED_CLASSIFIERS);
    }

    /**
     * Validates the FFM toolchain end-to-end by binding
     * {@code int abs(int)} from the C runtime via the linker's
     * default lookup, invoking it, and asserting the result.
     *
     * @throws Throwable if the bound method handle's invocation fails
     */
    @Test
    public void canBindAbsFromDefaultLookup() throws Throwable {
        var linker = Linker.nativeLinker();
        var defaultLookup = linker.defaultLookup();
        var absSymbol = defaultLookup.find("abs")
                .orElseThrow(() -> new AssertionError("abs not in default lookup"));
        var abs = linker.downcallHandle(
                absSymbol,
                FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT));
        assertEquals(42, (int) abs.invokeExact(-42));
        assertEquals(0, (int) abs.invokeExact(0));
        assertEquals(123, (int) abs.invokeExact(123));
    }

    /**
     * Asking for a library with no manifest entry fails fast — the
     * loader skips the download leg entirely (no network attempt,
     * no timeout wait) since there's nothing to verify the
     * download against, and falls through to
     * {@link System#loadLibrary} which then fails for an
     * unknown name.
     */
    @Test
    public void unmanifestedLibraryFailsFastWithoutDownload() {
        try {
            NativeLibLoader.clearCache();
            var error = assertThrows(UnsatisfiedLinkError.class,
                    () -> NativeLibLoader.load("nonexistent-test-lib", Arena.global()));
            assertTrue(error.getMessage().contains("nonexistent-test-lib"),
                    "error must mention the requested library: " + error.getMessage());
        } finally {
            NativeLibLoader.clearCache();
        }
    }

    /**
     * Two synthetic manifests with disjoint keys are merged into one
     * lookup map without a conflict — the toolkit's manifest will
     * declare {@code ffmpeg-*} entries, the lib's will declare
     * {@code opus}/{@code vpx}/etc., and both must coexist on the
     * classpath at runtime.
     *
     * @throws Exception if reflection fails
     */
    @Test
    public void disjointManifestsMergeWithoutConflict() throws Exception {
        var libManifest = parseManifestReflectively(
                "lib.jar!/META-INF/native-checksums.json",
                """
                {
                  "version": "0.1.0",
                  "commitSha": "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                  "binaries": {
                    "opus/linux-x86_64": {
                      "sha256": "0000000000000000000000000000000000000000000000000000000000000000",
                      "size": 100,
                      "path": "modules/lib/dependencies/libopus/bin/linux-x86_64/libopus.so"
                    }
                  }
                }""");
        var toolkitManifest = parseManifestReflectively(
                "toolkit.jar!/META-INF/native-checksums.json",
                """
                {
                  "version": "0.1.0",
                  "commitSha": "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb",
                  "binaries": {
                    "ffmpeg-avformat/linux-x86_64": {
                      "sha256": "1111111111111111111111111111111111111111111111111111111111111111",
                      "size": 200,
                      "path": "modules/call-toolkit/dependencies/ffmpeg/bin/linux-x86_64/libavformat.so"
                    }
                  }
                }""");
        verifyNoConflictsReflectively(List.of(libManifest, toolkitManifest));
    }

    /**
     * Two manifests declaring the same {@code <lib>/<classifier>}
     * key with different SHA-256 values throw an
     * {@link IllegalStateException} naming both manifests — pins the
     * tamper-detection guarantee.
     *
     * @throws Exception if reflection fails
     */
    @Test
    public void conflictingManifestsThrow() throws Exception {
        var first = parseManifestReflectively(
                "first.jar!/META-INF/native-checksums.json",
                """
                {
                  "version": "0.1.0",
                  "commitSha": "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                  "binaries": {
                    "opus/linux-x86_64": {
                      "sha256": "0000000000000000000000000000000000000000000000000000000000000000",
                      "size": 100,
                      "path": "modules/lib/dependencies/libopus/bin/linux-x86_64/libopus.so"
                    }
                  }
                }""");
        var second = parseManifestReflectively(
                "second.jar!/META-INF/native-checksums.json",
                """
                {
                  "version": "0.1.0",
                  "commitSha": "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb",
                  "binaries": {
                    "opus/linux-x86_64": {
                      "sha256": "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff",
                      "size": 100,
                      "path": "modules/lib/dependencies/libopus/bin/linux-x86_64/libopus.so"
                    }
                  }
                }""");
        var thrown = assertThrows(InvocationTargetException.class,
                () -> verifyNoConflictsReflectively(List.of(first, second)));
        var cause = thrown.getCause();
        assertInstanceOf(IllegalStateException.class, cause);
        assertTrue(cause.getMessage().contains("first.jar")
                        && cause.getMessage().contains("second.jar"),
                "conflict message must name both manifests: " + cause.getMessage());
    }

    /**
     * Two manifests declaring identical entries (same key, same
     * SHA-256) merge silently — a transitive dep + a direct dep
     * could each pull the same module's natives without a problem.
     *
     * @throws Exception if reflection fails
     */
    @Test
    public void identicalEntriesAcrossManifestsAreAllowed() throws Exception {
        var sha = "abcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcd";
        var first = parseManifestReflectively(
                "first.jar!/META-INF/native-checksums.json",
                """
                {
                  "version": "0.1.0",
                  "commitSha": "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                  "binaries": {
                    "opus/linux-x86_64": {
                      "sha256": "%s",
                      "size": 100,
                      "path": "modules/lib/dependencies/libopus/bin/linux-x86_64/libopus.so"
                    }
                  }
                }""".formatted(sha));
        var second = parseManifestReflectively(
                "second.jar!/META-INF/native-checksums.json",
                """
                {
                  "version": "0.1.0",
                  "commitSha": "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                  "binaries": {
                    "opus/linux-x86_64": {
                      "sha256": "%s",
                      "size": 100,
                      "path": "modules/lib/dependencies/libopus/bin/linux-x86_64/libopus.so"
                    }
                  }
                }""".formatted(sha));
        verifyNoConflictsReflectively(List.of(first, second));
    }

    /**
     * Reflectively invokes the private {@code parseManifest(String,
     * String)} on {@link NativeLibLoader} to produce a {@code
     * ModuleManifest} instance for testing.
     *
     * @param source the manifest source URL string
     * @param json   the manifest body
     * @return the parsed {@code ModuleManifest} (an
     *         {@link Object} since the type is private)
     * @throws Exception if reflection fails
     */
    private static Object parseManifestReflectively(String source, String json) throws Exception {
        var method = lookupPrivateMethod("parseManifest", String.class, String.class);
        return method.invoke(null, source, json);
    }

    /**
     * Reflectively invokes the private {@code verifyNoConflicts(List)}
     * on {@link NativeLibLoader} so the test can drive its conflict
     * logic with synthetic manifests.
     *
     * @param manifests the manifests to check
     * @throws Exception if reflection fails (the underlying method
     *                   may also throw — wrapped in
     *                   {@link InvocationTargetException})
     */
    private static void verifyNoConflictsReflectively(List<?> manifests) throws Exception {
        var method = lookupPrivateMethod("verifyNoConflicts", List.class);
        method.invoke(null, manifests);
    }

    /**
     * Looks up a private static method on {@link NativeLibLoader} by
     * name + parameter types and unlocks it for invocation.
     *
     * @param name           the method name
     * @param parameterTypes the parameter types
     * @return the unlocked method
     * @throws Exception if the method cannot be found
     */
    private static Method lookupPrivateMethod(String name, Class<?>... parameterTypes) throws Exception {
        var method = NativeLibLoader.class.getDeclaredMethod(name, parameterTypes);
        method.setAccessible(true);
        return method;
    }
}
