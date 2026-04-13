package com.github.auties00.cobalt.util;

import javax.crypto.spec.GCMParameterSpec;

public final class GcmUtils {
    public static GCMParameterSpec createNonce(long counter) {
        var iv = new byte[12];
        iv[4] = (byte) (counter >> 56);
        iv[5] = (byte) (counter >> 48);
        iv[6] = (byte) (counter >> 40);
        iv[7] = (byte) (counter >> 32);
        iv[8] = (byte) (counter >> 24);
        iv[9] = (byte) (counter >> 16);
        iv[10] = (byte) (counter >> 8);
        iv[11] = (byte) (counter);
        return new GCMParameterSpec(128, iv);
    }
}
