package com.github.auties00.cobalt.calls2;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Adversarial P8 re-check of the Signal seam from the construction and import angle.
 *
 * <p>{@code CallKeySignalSeamTest} already fails on any textual reference to a private Signal cipher in the
 * calls2 tree. This suite is the complementary, narrower guard P8 wiring asks for: it fails on a
 * {@code new SignalSessionCipher(}/{@code new SignalGroupCipher(} construction or an {@code import} of
 * either type anywhere under {@code calls2}, and positively pins that the call-key path is wired through
 * {@code MessageService} on the service ({@link LiveCalls2Service} holds a {@code MessageService}) and the
 * crypto facade ({@code CallKeyCryptography} calls {@code MessageService.processCall} and
 * {@code MessageEncryption.encryptForDevice}). The two together leave no way for the call-key ratchet to
 * bypass the per-address lock in {@code SignalCryptoLocks}.
 */
@DisplayName("calls2 P8 Signal seam wiring re-check")
class Calls2SignalSeamWiringTest {
    private static final List<String> FORBIDDEN_CONSTRUCTIONS =
            List.of("new SignalSessionCipher(", "new SignalGroupCipher(");
    private static final List<String> FORBIDDEN_IMPORTS = List.of(
            "import com.github.auties00.libsignal.SignalSessionCipher;",
            "import com.github.auties00.libsignal.groups.SignalGroupCipher;",
            "SignalSessionCipher;", "SignalGroupCipher;");

    @Test
    @DisplayName("no calls2 class constructs or imports a raw SignalSessionCipher or SignalGroupCipher")
    void noRawCipherConstructionOrImport() {
        var root = calls2SourceRoot();
        var offenders = new ArrayList<String>();
        try (Stream<Path> files = Files.walk(root)) {
            files.filter(p -> p.toString().endsWith(".java"))
                    .forEach(p -> {
                        var raw = read(p);
                        var code = stripComments(raw);
                        for (var ctor : FORBIDDEN_CONSTRUCTIONS) {
                            if (code.contains(ctor)) {
                                offenders.add(root.relativize(p) + " constructs " + ctor);
                            }
                        }
                        for (var imp : FORBIDDEN_IMPORTS) {
                            if (code.contains(imp)) {
                                offenders.add(root.relativize(p) + " imports/names " + imp);
                            }
                        }
                    });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        assertTrue(offenders.isEmpty(),
                "the call-key path must go through MessageEncryption/MessageService, never a private Signal "
                        + "cipher (bypassing SignalCryptoLocks races the ratchet): " + offenders);
    }

    @Test
    @DisplayName("LiveCalls2Service holds a MessageService and the crypto facade routes through the message pipeline")
    void callKeyPathWiredThroughMessagePipeline() {
        var service = read(calls2SourceRoot().resolve("LiveCalls2Service.java"));
        assertTrue(service.contains("import com.github.auties00.cobalt.message.MessageService;"),
                "LiveCalls2Service must hold the MessageService that owns offer encryption and key decryption");
        assertTrue(service.contains("private final MessageService messageService;"),
                "LiveCalls2Service must keep the MessageService as an injected field");

        var crypto = calls2SourceRoot().resolve("crypto").resolve("CallKeyCryptography.java");
        assertTrue(Files.exists(crypto), "CallKeyCryptography.java must exist at " + crypto);
        var cryptoSrc = read(crypto);
        assertTrue(cryptoSrc.contains("messageService.processCall("),
                "the call-key decrypt path must call MessageService.processCall");
        assertTrue(cryptoSrc.contains("encryption.encryptForDevice("),
                "the call-key encrypt path must call MessageEncryption.encryptForDevice");
    }

    private static Path calls2SourceRoot() {
        var suffix = Path.of("src", "main", "java", "com", "github", "auties00", "cobalt", "calls2");
        var moduleSuffix = Path.of("modules", "lib").resolve(suffix);
        var start = Path.of(System.getProperty("user.dir")).toAbsolutePath();
        for (var dir = start; dir != null; dir = dir.getParent()) {
            var local = dir.resolve(suffix);
            if (Files.isDirectory(local)) {
                return local;
            }
            var fromRepoRoot = dir.resolve(moduleSuffix);
            if (Files.isDirectory(fromRepoRoot)) {
                return fromRepoRoot;
            }
        }
        throw new IllegalStateException("could not locate the calls2 source tree from user.dir=" + start);
    }

    private static String read(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException e) {
            throw new UncheckedIOException("cannot read " + path, e);
        }
    }

    /**
     * Removes block and line comments so the forbidden-identifier scan ignores the explanatory mention of
     * the rule in {@code CallKeyCryptography}'s javadoc.
     */
    private static String stripComments(String source) {
        var out = new StringBuilder(source.length());
        var inBlock = false;
        var inLine = false;
        for (var i = 0; i < source.length(); i++) {
            var c = source.charAt(i);
            var next = i + 1 < source.length() ? source.charAt(i + 1) : '\0';
            if (inLine) {
                if (c == '\n') {
                    inLine = false;
                    out.append(c);
                }
                continue;
            }
            if (inBlock) {
                if (c == '*' && next == '/') {
                    inBlock = false;
                    i++;
                }
                continue;
            }
            if (c == '/' && next == '/') {
                inLine = true;
                i++;
                continue;
            }
            if (c == '/' && next == '*') {
                inBlock = true;
                i++;
                continue;
            }
            out.append(c);
        }
        return out.toString();
    }
}
