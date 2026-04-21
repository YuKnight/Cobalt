package com.github.auties00.cobalt.util;

import net.dongliu.apk.parser.ByteArrayApkFile;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

public class PlayStoreUtilsTest {
    @Test
    public void downloadsWhatsApp() throws IOException {
        assertDownloadsValidApk("com.whatsapp");
    }

    @Test
    public void downloadsWhatsAppBusiness() throws IOException {
        assertDownloadsValidApk("com.whatsapp.w4b");
    }

    private static void assertDownloadsValidApk(String packageName) throws IOException {
        try (var downloaded = PlayStoreUtils.downloadApk(packageName)) {
            assertEquals(packageName, downloaded.packageName());
            assertApkPackage(packageName, "base", downloaded.baseApk());
            for (var entry : downloaded.splits().entrySet()) {
                assertFalse(entry.getKey().isBlank(),
                        () -> packageName + " split has a blank name");
                assertApkPackage(packageName, entry.getKey(), entry.getValue());
            }
        }
    }

    private static void assertApkPackage(String expectedPackage, String label, InputStream stream) throws IOException {
        try (var parsed = new ByteArrayApkFile(stream.readAllBytes())) {
            var meta = parsed.getApkMeta();
            assertNotNull(meta, () -> expectedPackage + "/" + label + " has no parseable ApkMeta");
            assertEquals(expectedPackage, meta.getPackageName(),
                    () -> expectedPackage + "/" + label + " has unexpected package name");
        }
    }
}
